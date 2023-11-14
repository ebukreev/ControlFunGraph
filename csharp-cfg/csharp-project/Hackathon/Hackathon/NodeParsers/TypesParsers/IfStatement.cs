using System.Linq;
using Microsoft.CodeAnalysis.CSharp.Syntax;

namespace AnalysisVerificationLab1.NodeParsers.TypesParsers
{
    public class IfStatement
    {
        private string _ifStatementParserResult;

        public string GetIfStatementParserResult(IfStatementSyntax node)
        {
            _ifStatementParserResult = IfStatementParser(node);
            return _ifStatementParserResult;
        }

        private static string IfStatementParser(IfStatementSyntax node)
        {
            // if (condition)
            var condition = GetCondition(node.Condition);

            // then
            var statement = GetStatement(node.Statement);

            // else
            ElseClauseSyntax elseClause = node.Else;

            return $"if {condition}";
        }

        private static string GetCondition(ExpressionSyntax condition)
        {
            if (condition is not BinaryExpressionSyntax currentCondition) return "ErrorIfCondition";

            var firstVariable = currentCondition.Left is IdentifierNameSyntax syntaxLeft
                ? syntaxLeft.Identifier.ValueText
                : (currentCondition.Right as LiteralExpressionSyntax)?.Token.ValueText;
            
            var secondVariable = currentCondition.Right is IdentifierNameSyntax syntaxRight
                ? syntaxRight.Identifier.ValueText
                : (currentCondition.Right as LiteralExpressionSyntax)?.Token.ValueText;

            var operationToken = currentCondition.OperatorToken.ValueText;
            return $"{firstVariable} {operationToken} {secondVariable}";
        }

        private static string GetStatement(StatementSyntax statement)
        {
            if (statement is not BlockSyntax currentStatementsBlock) return "error";

            var statementsList = currentStatementsBlock.Statements;

            var expressionList = statementsList
                .Select(x => x as ExpressionStatementSyntax)
                .Select(y => y?.Expression);


            var result = "";
            foreach (var state in statementsList)
            {
                var curState = "";
                switch (state)
                {
                    case LocalDeclarationStatementSyntax localDeclarationStatement:

                        var localDeclarationStatementInstance = new LocalDeclarationStatement();
                        curState =
                            localDeclarationStatementInstance.GetLocalDeclarationStatementParserResult(
                                localDeclarationStatement);
                        break;

                    case ExpressionStatementSyntax expressionStatement:
                        var expressionStatementInstance = new ExpressionStatement();
                        curState = expressionStatementInstance.GetExpressionStatementParserResult(expressionStatement);

                        if (curState == null)
                        {
                            if (expressionStatement.Expression is PostfixUnaryExpressionSyntax)
                            {
                                var postfixUnaryExpressionInstance = new PostfixUnaryExpression();
                                curState =
                                    postfixUnaryExpressionInstance
                                        .GetPostfixUnaryExpressionParserResult(
                                            (PostfixUnaryExpressionSyntax)expressionStatement.Expression);
                            }
                        }

                        break;
                }

                if (curState != null)
                {
                    result += $"{curState}\n";
                }
            }

            return result;
        }
    }
}