class CFGBuilder(private val program: Program) {
    private var stack: MutableList<Program.ProgramBlock> = mutableListOf()
    private var settings: MutableList<String> = mutableListOf()

    private var blockStack: MutableList<Program.ProgramBlock> = mutableListOf()

    fun build(): String {
        val functions = mutableListOf<String>()

        for (function in program.functions) {
            val functionGraph = buildFunctionGraph(function)
            functions.add(functionGraph)

            stack = mutableListOf()
            settings = mutableListOf()
        }

        return """
            digraph cfg {
                node[shape=box];
                // graph[splines=ortho];
                ${functions.joinToString("\n")}
            }
        """.trimIndent()
    }

    private fun buildFunctionGraph(function: Program.Function): String {
        val name = function.name

        walkStatements(function.statements, null)

        return """
            subgraph cluster_$name {
                label="$name";
                ${settings.joinToString("\n")}
            }
        """.trimIndent()
    }

    data class LabeledProgramBlock(val block: Program.ProgramBlock, val label: String = "") {
        fun withLabel(newLabel: String): LabeledProgramBlock {
            return LabeledProgramBlock(block, "$label $newLabel".trim())
        }
    }

    private fun walkStatements(statements: List<Program.ProgramBlock>, parentStatement: Program.ProgramBlock?, branchLabel: String = ""): List<LabeledProgramBlock> {
        val exitStatements = mutableListOf<LabeledProgramBlock>()
        var prevStatements = mutableListOf<LabeledProgramBlock>()

        if (parentStatement != null) {
            prevStatements.add(LabeledProgramBlock(parentStatement))
        }

        for ((idx, statement) in statements.withIndex()) {
            if (statement !is Program.ContinueStatement && statement !is Program.BreakStatement) {
                for (prevStmt in prevStatements) {
                    if (prevStmt.block is Program.ReturnStatement || prevStmt.block is Program.ContinueStatement) continue

                    if (idx == 0) {
                        addEdge(prevStmt.block.id, statement.id, branchLabel)
                    } else {
                        addEdge(prevStmt.block.id, statement.id, prevStmt.label)
                    }
                }
            }

            val lastPrevStatements = prevStatements
            prevStatements = mutableListOf()

            when (statement) {
                is Program.StatementBlock -> {
                    addInfo(statement.id, statement.line, statement.text)
                    prevStatements.add(LabeledProgramBlock(statement))
                }
                is Program.ConditionBlock -> {
                    blockStack.add(statement)

                    addInfo(statement.id, statement.line, statement.condition, "diamond")
                    val thenStmt = walkStatements(statement.thenStatements, statement, "true")
                    val elseStmt = walkStatements(statement.elseStatements, statement, "false")
                    prevStatements.addAll(thenStmt)

                    if (elseStmt.isNotEmpty()) {
                        prevStatements.addAll(elseStmt)
                    } else {
                        prevStatements.add(LabeledProgramBlock(statement))
                    }

                    blockStack.removeLast()
                }
                is Program.LoopBlock -> {
                    blockStack.add(statement)

                    if (statement.pat != null) {
                        // Foreach loop
                        addInfo(statement.id, statement.line, "(${statement.pat} = ( ${statement.expr}).next() ) != null", "diamond")
                    } else {
                        // While loop
                        addInfo(statement.id, statement.line, statement.expr, "diamond")
                    }

                    val lastStmt = walkStatements(statement.statements, statement, "true")

                    if (lastStmt.none { it.block is Program.ContinueStatement }) {
                        prevStatements.add(LabeledProgramBlock(statement))
                    }

                    for (stmt in lastStmt) {
                        if (stmt.label == "break" && stmt.block is Program.ReturnStatement) {
                            prevStatements.add(stmt)
                        } else {
                            addEdge(stmt.block.id, statement.id)
                        }
                    }

                    blockStack.removeLast()
                }
                is Program.BreakStatement -> {
                    val lastPrevStatement = lastPrevStatements.lastOrNull() ?: break
                    prevStatements.add(LabeledProgramBlock(lastPrevStatement.block, "break"))

                    break
                }
                is Program.ReturnStatement -> {
                    addInfo(statement.id, statement.line, statement.text)
                    exitStatements.add(LabeledProgramBlock(statement))
                    break
                }
                is Program.ContinueStatement -> {
                    val nearestLoop = blockStack.findLast { it is Program.LoopBlock } ?: break

                    for (stmt in lastPrevStatements) {
                        addEdge(stmt.block.id, nearestLoop.id, "continue")
                    }

                    break
                }
            }
        }

        exitStatements.addAll(prevStatements)

        return exitStatements
    }

    private fun addInfo(id: Int, lineNumber: Int, text: String, shape: String = "box") {
        val name = "s$id"
        val escapedText = text.replace("\"", "\\\"");

        settings.add("""$name[label="$escapedText", shape="$shape", linenumber=$lineNumber];""")
    }

    private fun addEdge(from: Int, to: Int, label: String = "") {
        val fromName = "s$from"
        val toName = "s$to"

        settings.add("""$fromName -> $toName [label = "$label"];""")
    }
}