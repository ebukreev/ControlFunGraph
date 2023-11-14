package cfg.nodes.statements

import cfg.nodes.statements.blocks.StatementsBlock
import com.oracle.js.parser.ir.LoopNode

class LoopStatement(
    val mainBlock: StatementsBlock,
    allBlocks: List<StatementsBlock>,
    override var node: LoopNode
) : ControlFlowStatement<LoopNode>(allBlocks) {
}
