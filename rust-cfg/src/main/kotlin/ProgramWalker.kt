import RustParser.*
import org.antlr.v4.runtime.CharStreams
import org.antlr.v4.runtime.CodePointCharStream
import org.antlr.v4.runtime.CommonTokenStream
import org.antlr.v4.runtime.ParserRuleContext

class ProgramWalker(private val programText: String) {
    private var statementIdx: Int = 0

    private val charStream: CodePointCharStream = CharStreams.fromString(programText)
    private val lexer: RustLexer = RustLexer(charStream)
    private val tokens: CommonTokenStream = CommonTokenStream(lexer)
    private val parser: RustParser = RustParser(tokens)

    fun walkProgram(): Program {
        val functions = mutableListOf<Program.Function>()

        for (item in parser.crate().item().iterator()) {
            val function = walkFunction(item.visItem().function_())
            functions.add(function)
        }

        return Program(functions)
    }

    private fun walkFunction(function: Function_Context): Program.Function {
        val name = function.identifier().originalText
        val blockExpr = function.blockExpression()

        if (blockExpr != null) {
            val statements = walkBlockExpr(blockExpr).toMutableList()

            if (statements.lastOrNull() !is Program.ReturnStatement) {
                statements.add(Program.ReturnStatement(statementIdx++, blockExpr.lineNumber, "return"))
            }

            return Program.Function(name, statements)
        }

        return Program.Function(name, listOf())
    }

    private fun walkBlockExpr(blockExpr: BlockExpressionContext): List<Program.ProgramBlock> {
        val statements = mutableListOf<Program.ProgramBlock>()

        if (blockExpr.statements() == null) return statements

        for (statement in blockExpr.statements().statement()) {
            val letStatement = statement.letStatement()
            val exprStatement = statement.expressionStatement()

            if (letStatement != null) {
                statements.add(Program.StatementBlock(statementIdx++, letStatement.lineNumber, letStatement.originalText))
            } else if (exprStatement != null) {
                statements.add(walkExpr(exprStatement))
            } else {
                statements.add(Program.StatementBlock(statementIdx++, statement.lineNumber, statement.originalText))
            }
        }

        return statements
    }

    private fun walkExpr(expr: ExpressionStatementContext): Program.ProgramBlock {
        val exprExpr = expr.expression()
        val exprBlock = expr.expressionWithBlock()

        if (exprExpr != null) {
            val originalText = exprExpr.originalText;
            if (originalText.startsWith("return")) {
                return Program.ReturnStatement(statementIdx++, exprExpr.lineNumber, exprExpr.originalText)
            } else if (originalText.startsWith("break")) {
                return Program.BreakStatement(statementIdx++, exprExpr.lineNumber)
            } else if (originalText.startsWith("continue")) {
                return Program.ContinueStatement(statementIdx++, exprExpr.lineNumber)
            }

            return Program.StatementBlock(statementIdx++, exprExpr.lineNumber, exprExpr.originalText)
        } else if (exprBlock != null) {
            return walkExprBlock(exprBlock)
        }

        return Program.StatementBlock(statementIdx++, expr.lineNumber, expr.originalText)
    }

    private fun walkExprBlock(exprBlock: ExpressionWithBlockContext): Program.ProgramBlock {
        val ifExpr = exprBlock.ifExpression()
        val loopExpr = exprBlock.loopExpression()

        if (ifExpr != null) {
            return walkIfExpr(ifExpr)
        } else if (loopExpr != null) {
            return walkLoopExpr(loopExpr)
        }

        return Program.StatementBlock(statementIdx++, exprBlock.lineNumber, exprBlock.originalText)
    }

    private fun walkIfExpr(ifExpr: IfExpressionContext): Program.ConditionBlock {
        val expr = ifExpr.expression()
        val blockExpr = ifExpr.blockExpression()

        val thenBlock = walkBlockExpr(blockExpr.first())

        val anotherIfExpr = ifExpr.ifExpression()
        val elseExpr = blockExpr.getOrNull(1)
        val elseBlock = if (elseExpr != null) {
            walkBlockExpr(elseExpr)
        } else if (anotherIfExpr != null) {
            listOf(walkIfExpr(anotherIfExpr))
        } else {
            listOf()
        }

        return Program.ConditionBlock(statementIdx++, expr.lineNumber, expr.originalText, thenBlock, elseBlock)
    }

    private fun walkLoopExpr(loopExpr: LoopExpressionContext): Program.LoopBlock {
        val forLoop = loopExpr.iteratorLoopExpression()
        val whileLoop = loopExpr.predicateLoopExpression()

        if (forLoop != null) {
            val pat = forLoop.pattern()
            val expr = forLoop.expression()
            val block = forLoop.blockExpression()
            val statements = walkBlockExpr(block)
            return Program.LoopBlock(statementIdx++, expr.lineNumber, pat.originalText, expr.originalText, statements)
        } else if (whileLoop != null) {
            val expr = whileLoop.expression()
            val block = whileLoop.blockExpression()
            val statements = walkBlockExpr(block)
            return Program.LoopBlock(statementIdx++, expr.lineNumber, null, expr.originalText, statements)
        }

        return Program.LoopBlock(statementIdx++, -1, "", "", listOf())
    }

    private val ParserRuleContext.originalText: String
        get() = programText.substring(start.startIndex, stop.stopIndex+1)

    private val ParserRuleContext.lineNumber: Int
        get() = start.line
}