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
        get() = findNormalSuccessors() + findNormalConditionalSuccessors().values + findNormalBackSuccessors()

    val allForwardSuccessors: Set<StatementsBlock>
        get() = findNormalSuccessors() + findNormalConditionalSuccessors().values

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

fun StatementsBlock.findNormalSuccessors(): Set<StatementsBlock> {
    val normalSuccessors = mutableSetOf<StatementsBlock>()
    for (successor in successors) {
        if (successor.isEntry || successor.isExit) {
            normalSuccessors.addAll(successor.findNormalSuccessors())
        } else {
            normalSuccessors.add(successor)
        }
    }
    return normalSuccessors
}

fun StatementsBlock.findNormalConditionalSuccessors(): Map<ConditionType, StatementsBlock> {
    val normalSuccessors = mutableMapOf<ConditionType, StatementsBlock>()
    for ((type, successor) in conditionalSuccessors) {
        val normalSuccessor = successor.findNormalSuccessors().firstOrNull()
        if (normalSuccessor != null) {
            normalSuccessors[type] = normalSuccessor
        }
    }
    return normalSuccessors
}

fun StatementsBlock.findNormalBackSuccessors(): Set<StatementsBlock> {
    val normalSuccessors = mutableSetOf<StatementsBlock>()
    for (successor in backSuccessors) {
        if (successor.isEntry || successor.isExit) {
            normalSuccessors.addAll(successor.findNormalBackSuccessors())
        } else {
            normalSuccessors.add(successor)
        }
    }
    return normalSuccessors
}
