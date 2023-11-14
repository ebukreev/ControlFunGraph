package cfg.nodes.statements

import com.oracle.js.parser.ir.BreakNode

class BreakStatement(
    override val node: BreakNode,
    override val line: Int = 0
) : AbstractStatement<BreakNode>() {
}
