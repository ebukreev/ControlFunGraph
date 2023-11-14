package cfg.nodes.statements

import com.oracle.js.parser.ir.Expression

class AstExpressionAsStatement(
    override val node: Expression,
    override val line: Int
) : AbstractStatement<Expression>() {
    override fun toString(): String {
        return node.toString()
    }
}
