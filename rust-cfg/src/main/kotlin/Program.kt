data class Program(val functions: List<Function>) {
    interface ProgramBlock {
        val id: Int;
        val line: Int;
    }

    data class Function(val name: String, val statements: List<ProgramBlock>)

    data class StatementBlock(
        override val id: Int,
        override val line: Int,
        val text: String
    ) : ProgramBlock

    data class BreakStatement(
        override val id: Int,
        override val line: Int
    ) : ProgramBlock

    data class ContinueStatement(
        override val id: Int,
        override val line: Int
    ) : ProgramBlock

    data class ReturnStatement(
        override val id: Int,
        override val line: Int,
        val text: String,
    ): ProgramBlock

    data class ConditionBlock(
        override val id: Int,
        override val line: Int,
        val condition: String,
        val thenStatements: List<ProgramBlock>,
        val elseStatements: List<ProgramBlock>
    ): ProgramBlock

    data class LoopBlock(
        override val id: Int,
        override val line: Int,
        val pat: String?,
        val expr: String,
        val statements: List<ProgramBlock>
    ): ProgramBlock
}