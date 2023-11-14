package cfg.nodes.statements

import com.oracle.js.parser.ir.ContinueNode

class ContinueStatement(
    override val node: ContinueNode,
    override val line: Int = 0
) : AbstractStatement<ContinueNode>()
