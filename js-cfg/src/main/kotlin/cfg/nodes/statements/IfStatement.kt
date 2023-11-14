package cfg.nodes.statements

import cfg.nodes.statements.blocks.StatementsBlock
import com.oracle.js.parser.ir.Expression
import com.oracle.js.parser.ir.IfNode

class IfStatement(
    val condition: Expression,
    val then: StatementsBlock,
    val `else`: StatementsBlock,
    allBlocks: List<StatementsBlock>,
    override val node: IfNode
) : ControlFlowStatement<IfNode>(allBlocks)
