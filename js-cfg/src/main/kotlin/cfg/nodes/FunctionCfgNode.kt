package cfg.nodes

import cfg.nodes.statements.blocks.StatementsBlock
import com.oracle.js.parser.ir.FunctionNode

data class FunctionCfgNode(
    val name: String,
    val blocks: List<StatementsBlock>,
    override val node: FunctionNode,
) : AbstractCfgNode<FunctionNode>() {
    val entryBlock: StatementsBlock
        get() = blocks.first { it.isEntry }

    val terminalBlock: StatementsBlock
        get() = blocks.first { it.isTerminal }

    val allMiddleBlocks: List<StatementsBlock>
        get() = blocks.filter { !it.isEntry }

    override val line: Int = node.lineNumber
}
