package cfg.nodes.statements

import com.oracle.js.parser.ir.Statement

class AstStatement(
    override val node: Statement,
    override val line: Int
) : AbstractStatement<Statement>() {
    override fun toString(): String {
        return node.toString()
    }
}
