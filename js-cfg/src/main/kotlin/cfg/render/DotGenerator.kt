package cfg.render

import cfg.nodes.FileCfgNode
import cfg.nodes.FunctionCfgNode
import cfg.nodes.statements.AbstractStatement
import cfg.nodes.statements.IfStatement
import cfg.nodes.statements.blocks.ExpressionConditionType
import cfg.nodes.statements.blocks.StatementsBlock
import com.oracle.js.parser.ir.Node

class DotGenerator {
    private val indent = " ".repeat(2)

    private val nodesToIds = mutableMapOf<Any, Int>()
    private var lastNodeId = 0

    private val nodesOnGraph = hashSetOf<StatementsBlock>()

    fun toDot(fileCfgNode: FileCfgNode): String {
       return buildString {
           appendLine("digraph G {")

           indent {
               for (function in fileCfgNode.functions) {
                   addFunction(function)
               }
           }
           appendLine("}")
       }
    }

    private fun StringBuilder.addFunction(functionCfgNode: FunctionCfgNode) {
        appendLine("subgraph cluster_func_${functionCfgNode.name} {")
        indent {
            appendLine("label = \"function ${functionCfgNode.name}\"")

            val entryBlock = functionCfgNode.entryBlock
            addBlocksRecursively(entryBlock)
        }
        appendLine("}")
    }

    private fun StringBuilder.addBlocksRecursively(block: StatementsBlock) {
        if (block in nodesOnGraph) {
            return
        }
        nodesOnGraph.add(block)

        val blockAsText = block.text
        val dotStyle = block.getDotStyle()
        val lines = block.statements.map { s -> s.line }

        val nodeSourceText = buildString {
            append(block.graphId)
            append("[label = \"")
            append(blockAsText)
            append("\" ")
            append(dotStyle)
            append(" ")
            append("comment = \"${lines.joinToString(", ")}\"")
            append(" ")
            append("]")

            addBlockEdges(block)
        }

        appendLine(nodeSourceText)

        for (successor in block.allSuccessors) {
            addBlocksRecursively(successor)
        }
    }

    private val StatementsBlock.leading: AbstractStatement<*>?
        get() = this.statements.firstOrNull()

    private val StatementsBlock.text: String
        get() {
            val body = when {
                this.isEntry -> "<entry>"
                this.isExit -> "<exit>"
                else -> statements.joinToString(separator = "\\n") { s -> s.node.text }
            }

            return buildString {
                append(this@text.name ?: "")

                if (body.isNotBlank() && this@text.name != null) {
                    append(": ")
                }

                append(body)
            }

        }

    private fun StatementsBlock.getDotStyle(): String {
        return buildString {

            when (leading) {
                is IfStatement -> {
                    append("shape=diamond")
                }
                else -> {
                    append("shape=box")
                }
            }

//            when {
//                this@getDotStyle.isTerminal -> {
//                    append(" color=red")
//                }
//            }
        }
    }

    private fun StringBuilder.addBlockEdges(block: StatementsBlock) {
        for (successor in block.successors) {
            appendLine("${block.graphId} -> ${successor.graphId}:n")
        }

        for ((type, successorBlock) in block.conditionalSuccessors) {
            if (type is ExpressionConditionType) {
                appendLine("${block.graphId}:w -> " +
                        "${successorBlock.graphId} [label=\"${type.expression}\"]")
            } else {
                appendLine("${block.graphId}:e -> " +
                        "${successorBlock.graphId} [label=\"else\"]")
            }
        }

        for (successor in block.backSuccessors) {
            appendLine("${block.graphId} -> ${successor.graphId}:s")
        }
    }

    private fun StringBuilder.indent(block: StringBuilder.() -> Unit) {
        val result = StringBuilder()
        result.block()

        result.lines().forEach { line ->
            append(indent)
            appendLine(line)
        }
    }

    private val Any.graphId: Int
        get() = nodesToIds.getOrPut(this) { lastNodeId++ }

    private val Node.text: String
        get() = this.toString().formatString

    private val String.formatString: String
        get() = this.replace("\n", "\\n")
}
