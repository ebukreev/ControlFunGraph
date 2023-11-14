package cfg.nodes.statements

import cfg.nodes.statements.blocks.StatementsBlock
import com.oracle.js.parser.ir.Statement

sealed class ControlFlowStatement <T : Statement>(
    private val _allBlocks: List<StatementsBlock>
) : AbstractStatement<T>() {
    val exitBlock: StatementsBlock = StatementsBlock(name = this::class.simpleName, isExit = true)

    val allBlocks: List<StatementsBlock>
        get() = _allBlocks.plus(exitBlock)

    override val line: Int
        get() = node.lineNumber
}
