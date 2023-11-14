package cfg

import cfg.nodes.FileCfgNode
import cfg.nodes.FunctionCfgNode
import cfg.nodes.statements.*
import cfg.nodes.statements.blocks.StatementsBlock
import cfg.nodes.statements.blocks.getExitBlocks
import com.oracle.js.parser.Token
import com.oracle.js.parser.TokenType
import com.oracle.js.parser.ir.*

@Suppress("MemberVisibilityCanBePrivate")
class CfgBuilder {
    // In graal-js, the whole sources are represented as a function
    // with declarations in its body
    fun visitFile(node: FunctionNode): FileCfgNode {
        check(node.body.isSynthetic) { "not a synthetic block!" }

        val functionDeclarations = node
            .body
            .statements
            .filterIsInstance<VarNode>()
            .filter { it.isFunctionDeclaration }
            .map { it.init }
            .filterIsInstance<FunctionNode>()

        val functions = functionDeclarations.map { fn -> visitFunction(fn) }

        return FileCfgNode(functions, node, node.lineNumber)
    }

    fun visitFunction(node: FunctionNode): FunctionCfgNode {
        val blocks = node.body.toBlocks(node.name)
        val function = FunctionCfgNode(
            name = node.name,
            blocks = blocks,
            node = node
        )

        blocks.first { it.isExit }.isTerminal = true

        updateTerminalNodes(function.entryBlock, function.terminalBlock)

        return function
    }

    fun visitStatement(statement: Statement): AbstractStatement<*> {
        return when (statement) {
            is IfNode -> visitIfStatement(statement)
            is LoopNode -> visitLoopStatement(statement)
            is BlockStatement -> tryVisitForLoop(statement) ?: AstStatement(statement, statement.lineNumber)
            is ContinueNode -> visitContinueStatement(statement)
            is ReturnNode -> visitReturnNode(statement)
            is BreakNode -> visitBreakStatement(statement)
            else -> AstStatement(statement, statement.lineNumber)
        }
    }

    fun visitIfStatement(statement: IfNode): IfStatement {
        var thenBlocks = statement.pass?.toBlocks("if-then").orEmpty()
        var elseBlocks = statement.fail?.toBlocks("if-else").orEmpty()

        if (thenBlocks.isEmpty()) {
            thenBlocks = listOf(StatementsBlock("then-empty", isExit = true))
        }

        if (elseBlocks.isEmpty()) {
            elseBlocks = listOf(StatementsBlock("else-empty", isExit = true))
        }

        val ifStatement = IfStatement(
            condition = statement.test,
            then = thenBlocks.first(),
            `else` = elseBlocks.first(),
            allBlocks = thenBlocks + elseBlocks,
            statement
        )

        thenBlocks.getExitBlocks().forEach { block ->
            block.successors.add(ifStatement.exitBlock)
        }

        elseBlocks.getExitBlocks().forEach { block ->
            block.successors.add(ifStatement.exitBlock)
        }

        return ifStatement
    }

    fun tryVisitForLoop(statement: BlockStatement): LoopStatement? {
        val statements = statement.block.statements
        if (statements.size !in 1..2) {
            return null
        }

        val forNode = statements.firstOrNull { it is ForNode } as? ForNode ?: return null
        val varNode = statements.firstOrNull { it is VarNode } as? VarNode

        val loop = visitLoopStatement(forNode)

        if (varNode != null) {
            val initExpression = BinaryNode(Token.recast(0, TokenType.ASSIGN_INIT), varNode.name, varNode.init)
            loop.node = (loop.node as ForNode).setInit(null, initExpression)
        }

        return loop

    }

    fun visitLoopStatement(node: LoopNode): LoopStatement {
        val bodyBlocks = node.body.toBlocks("loop-node")

        return LoopStatement(
            bodyBlocks.firstOrNull() ?: StatementsBlock("loop-empty", isEntry = true),
            bodyBlocks,
            node,
        )
    }

    fun visitContinueStatement(node: ContinueNode): ContinueStatement {
        return ContinueStatement(node)
    }

    fun visitReturnNode(node: ReturnNode): ReturnStatement {
        return ReturnStatement(node)
    }

    fun visitBreakStatement(node: BreakNode): BreakStatement {
        return BreakStatement(node)
    }

    private fun Block.toBlocks(parentName: String): List<StatementsBlock> {
        val statements = statements.map(::visitStatement)
        val cfgBlocksHelper = CfgBlocksHelper()

        return cfgBlocksHelper.getCfg(statements, parentName = parentName)
    }

    private fun updateTerminalNodes(block: StatementsBlock, exitNode: StatementsBlock) {
        val cfgBlocksHelper = CfgBlocksHelper()
        cfgBlocksHelper.updateTerminalNodes(block, exitNode)
        cfgBlocksHelper.desugarLoops(null, block)
        cfgBlocksHelper.eliminateFakeNodes(null, block)
    }
}
