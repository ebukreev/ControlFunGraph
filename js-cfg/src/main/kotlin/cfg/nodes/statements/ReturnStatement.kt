package cfg.nodes.statements

import com.oracle.js.parser.ir.ReturnNode

class ReturnStatement(
    override val node: ReturnNode
) : AbstractStatement<ReturnNode>() {
    override val line: Int = node.lineNumber
}
