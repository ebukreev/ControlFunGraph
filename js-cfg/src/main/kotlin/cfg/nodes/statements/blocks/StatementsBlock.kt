package cfg.nodes.statements.blocks

import cfg.nodes.statements.AbstractStatement
import com.oracle.js.parser.ir.Expression

class StatementsBlock(
    val name: String? = null,
    var isEntry: Boolean = false,
    var isExit: Boolean = false,
    var isTerminal: Boolean = false,
) {
    val statements = mutableListOf<AbstractStatement<*>>()

    val successors = mutableSetOf<StatementsBlock>()
    val backSuccessors = mutableSetOf<StatementsBlock>()
    val conditionalSuccessors: MutableMap<ConditionType, StatementsBlock> = mutableMapOf()

    val allSuccessors: Set<StatementsBlock>
        get() = successors + conditionalSuccessors.values + backSuccessors

    val allForwardSuccessors: Set<StatementsBlock>
        get() = successors + conditionalSuccessors.values

    fun getExitBlocks(): List<StatementsBlock> {
        if (this.isExit && this.successors.isEmpty() && conditionalSuccessors.isEmpty()) {
            return listOf(this)
        }

        return allForwardSuccessors.flatMap { it.getExitBlocks() }
    }
}

sealed class ConditionType

data object ElseConditionType : ConditionType()
data class ExpressionConditionType(val expression: Expression) : ConditionType()
