package cfg

import cfg.nodes.statements.*
import cfg.nodes.statements.blocks.ElseConditionType
import cfg.nodes.statements.blocks.ExpressionConditionType
import cfg.nodes.statements.blocks.StatementsBlock
import com.oracle.js.parser.ir.*

class CfgBlocksHelper {
    private val leadingOrNotStatements = mutableMapOf<AbstractStatement<*>, Boolean>()

    fun getCfg(statements: List<AbstractStatement<*>>, parentName: String): List<StatementsBlock> {
        val blocks = createBlocks(statements, parentName)
        linkBlocks(blocks)
        updateBlocksWithJumpStatements(blocks)
        setTargetsToContinues(blocks)
        setTargetsToBreaks(blocks)

        return addMissingBlocks(blocks)
    }

    private fun createBlocks(statements: List<AbstractStatement<*>>, parentName: String): List<StatementsBlock> {
        val result = mutableListOf<StatementsBlock>()

        var currentBlock = StatementsBlock(parentName, isEntry = true)
        result.add(currentBlock)

        currentBlock = StatementsBlock()
        var isPreviousBlockJump = false

        for (statement in statements) {
            if (statement.isLeading || isPreviousBlockJump) {
                if (currentBlock.statements.isNotEmpty()) {
                    result.add(currentBlock)
                }

                currentBlock = StatementsBlock()
                isPreviousBlockJump = statement.isLeading
            } else {
                isPreviousBlockJump = false
            }

            currentBlock.statements.add(statement)
        }

        if (currentBlock.statements.isNotEmpty()) {
            result.add(currentBlock)
        }

        result.add(StatementsBlock(parentName, isExit = true))

        return result
    }

    private fun linkBlocks(blocks: List<StatementsBlock>) {
        var previousBlock = blocks.getOrNull(0) ?: return

        for (currentBlock in blocks.drop(1)) {
            previousBlock.successors.add(currentBlock)

            val blockLeadingStatement = currentBlock.statements.firstOrNull()
            previousBlock = when (blockLeadingStatement) {
                is ControlFlowStatement<*> -> blockLeadingStatement.exitBlock
                else -> currentBlock
            }
        }
    }

    private fun updateBlocksWithJumpStatements(blocks: List<StatementsBlock>) {
        for (block in blocks) {
            val leadingStatement = block.statements.firstOrNull() ?: continue

            when (leadingStatement) {
                is IfStatement -> {
                    block.conditionalSuccessors[ExpressionConditionType(leadingStatement.condition)] = leadingStatement.then
                    block.conditionalSuccessors[ElseConditionType] = leadingStatement.`else`
                }

                is LoopStatement -> {
                    block.conditionalSuccessors[ExpressionConditionType(leadingStatement.node.test)] = leadingStatement.mainBlock
                    block.conditionalSuccessors[ElseConditionType] = leadingStatement.exitBlock

                    leadingStatement.mainBlock.getExitBlocks().forEach { exitBlock ->
                        exitBlock.backSuccessors.add(block)
                    }
                }

                is ReturnStatement -> {
                    block.successors.removeIf { !it.isTerminal }
                    block.conditionalSuccessors.clear()
                    block.backSuccessors.clear()
                }

                else -> {}
            }
        }
    }

    private fun addMissingBlocks(blocks: List<StatementsBlock>): List<StatementsBlock> {
        val missingBlocks = mutableListOf<StatementsBlock>()
        val statements = blocks.flatMap { it.statements }

        for (statement in statements) {
            if (statement is ControlFlowStatement<*>) {
                missingBlocks.addAll(statement.allBlocks)
            }
        }

        return blocks + missingBlocks
    }

    private fun setTargetsToContinues(
        blocks: List<StatementsBlock>,
        currentTarget: StatementsBlock? = null
    ) {
        for (block in blocks) {
            val leadingStatement = block.statements.firstOrNull() ?: continue

            if (leadingStatement is LoopStatement) {
                setTargetsToContinues(leadingStatement.allBlocks, block)
            }

            if (leadingStatement is LoopStatement) {
                setTargetsToContinues(leadingStatement.allBlocks, currentTarget)
            }

            if (leadingStatement is ContinueStatement && currentTarget != null) {
                block.successors.clear()
                block.backSuccessors.clear()
                block.backSuccessors.add(currentTarget)
            }
        }
    }

    private fun setTargetsToBreaks(
        blocks: List<StatementsBlock>,
        currentTarget: StatementsBlock? = null
    ) {
        for (block in blocks) {
            val leadingStatement = block.statements.firstOrNull() ?: continue

            if (leadingStatement is LoopStatement) {
                setTargetsToBreaks(leadingStatement.allBlocks, leadingStatement.exitBlock)
            }

            if (leadingStatement is BreakStatement && currentTarget != null) {
                block.successors.clear()
                block.successors.add(currentTarget)
                block.backSuccessors.clear()
            }
        }
    }

    fun updateTerminalNodes(
        block: StatementsBlock,
        functionTerminalBlock: StatementsBlock,
        visited: Set<StatementsBlock> = setOf(),
    ) {
        if (block in visited) {
            return
        }

        if (block == functionTerminalBlock) {
            return
        }

        val leadingStatement = block.statements.firstOrNull()

        if (block.isTerminal || leadingStatement is ReturnStatement) {
            block.successors.add(functionTerminalBlock)

            return
        }

        block.allForwardSuccessors.forEach { successor ->
            updateTerminalNodes(successor, functionTerminalBlock, visited + block)
        }
    }

    fun eliminateFakeNodes(
        previous: StatementsBlock?,
        block: StatementsBlock,
        visited: Set<StatementsBlock> = setOf(),
    ) {
        if (block in visited) {
            return
        }

        val leading = block.statements.firstOrNull()

        var newPreviousBlock: StatementsBlock = block

        if (leading is BreakStatement || leading is ContinueStatement || leading is ReturnStatement) {
            previous!!.successors.remove(block)
            previous.successors.addAll(block.allSuccessors)

            newPreviousBlock = previous
        }

        block.allSuccessors.forEach { successor ->
            eliminateFakeNodes(newPreviousBlock, successor, visited + block)
        }
    }

    fun desugarLoops(
        previous: StatementsBlock?,
        block: StatementsBlock,
        visited: Set<StatementsBlock> = setOf()
    ) {
        if (block in visited) {
            return
        }

        val leading = block.statements.firstOrNull()

        var newPreviousBlock = block

        if (leading is LoopStatement &&
            leading.node is ForNode &&
            !(leading.node as ForNode).isForInOrOf) {
            val mainExitBlock = StatementsBlock(name = "desugared-for-post")

            removeLinksToLoop(leading.mainBlock, block)
            leading.mainBlock.getExitBlocks().forEach { e ->
                if (!e.isTerminal) {
                    e.successors.add(mainExitBlock)
                }
            }

            val loopNode = leading.node as ForNode
            val initNode = loopNode.init
            val initAstNode = AstExpressionAsStatement(initNode, loopNode.lineNumber)
            val desugaredBlock = StatementsBlock(name = "desugared-for")
            desugaredBlock.statements.add(initAstNode)

            val ifBlock = StatementsBlock()
            val fakeIfNode = IfNode(0, 0, 0, loopNode.test, null, null)
            val ifStatement = IfStatement(
                loopNode.test,
                leading.mainBlock,
                leading.exitBlock,
                node = fakeIfNode,
                allBlocks = leading.allBlocks
            )

            ifBlock.conditionalSuccessors[ExpressionConditionType(loopNode.test)] = leading.mainBlock
            ifBlock.conditionalSuccessors[ElseConditionType] = leading.exitBlock

            ifBlock.statements.add(ifStatement)
            desugaredBlock.successors.add(ifBlock)

            val postExpression = loopNode.modify
            val postExpressionAsStatementAstNode = AstExpressionAsStatement(postExpression, loopNode.lineNumber)

            previous?.successors?.remove(block)
            previous?.successors?.add(desugaredBlock)

            newPreviousBlock = ifStatement.exitBlock

            mainExitBlock.statements.add(postExpressionAsStatementAstNode)
            mainExitBlock.backSuccessors.add(ifBlock)

            patchContinues(leading.mainBlock, mainExitBlock)
        }

        block.allSuccessors.forEach { successor ->
            desugarLoops(newPreviousBlock, successor, visited + block)
        }
    }

    private fun removeLinksToLoop(start: StatementsBlock, loopBlock: StatementsBlock) {
        start.backSuccessors.remove(loopBlock)
        start.successors.remove(loopBlock)

        start.allSuccessors.forEach { s -> removeLinksToLoop(s, loopBlock) }
    }

    private fun patchContinues(block: StatementsBlock, to: StatementsBlock) {
        val leading = block.statements.firstOrNull()

        if (leading is ContinueStatement) {
            block.backSuccessors.clear()
            block.successors.add(to)
        }

        block.allForwardSuccessors.forEach { s -> patchContinues(s, to) }
    }

    private val AbstractStatement<*>.isLeading: Boolean
        get() {
            return leadingOrNotStatements.getOrPut(this) {
                node is Statement && (node as Statement).isConditionalJump
            }
        }

    private val Statement.isConditionalJump: Boolean
        get() = this is IfNode || this is JumpStatement || this is LoopNode || this is ReturnNode
}
