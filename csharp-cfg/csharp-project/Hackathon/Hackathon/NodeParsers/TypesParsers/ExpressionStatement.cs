using Microsoft.CodeAnalysis;
using Microsoft.CodeAnalysis.CSharp;
using Microsoft.CodeAnalysis.CSharp.Syntax;

namespace AnalysisVerificationLab1.NodeParsers.TypesParsers
{
    public class ExpressionStatement
    {
        private string _expressionStatementParserResult;

        public string GetExpressionStatementParserResult(ExpressionStatementSyntax node)
        {
            _expressionStatementParserResult = ExpressionStatementParser(node);
            return _expressionStatementParserResult;
        }

        private static string ExpressionStatementParser(ExpressionStatementSyntax node)
        {
            switch (node.Expression)
            {
                // Post-increment/decrement (x++)
                case PostfixUnaryExpressionSyntax postfixUnaryExpression
                    when postfixUnaryExpression.IsKind(SyntaxKind.PostIncrementExpression) ||
                         postfixUnaryExpression.IsKind(SyntaxKind.PostDecrementExpression):
                {
                    var postfixUnaryExpressionInstance = new PostfixUnaryExpression();
                    return postfixUnaryExpressionInstance.GetPostfixUnaryExpressionParserResult(postfixUnaryExpression);
                }
                // Pre-increment/decrement (x = ++y)
                case PrefixUnaryExpressionSyntax prefixUnaryExpression
                    when prefixUnaryExpression.IsKind(SyntaxKind.PreIncrementExpression) ||
                         prefixUnaryExpression.IsKind(SyntaxKind.PreDecrementExpression):
                {
                    var prefixUnaryExpressionInstance = new PrefixUnaryExpression();
                    return prefixUnaryExpressionInstance.GetPrefixUnaryExpressionParserResult(prefixUnaryExpression);
                }
            }

            if (node.Expression is not AssignmentExpressionSyntax nodeExpression) return null;

            var variableName = (nodeExpression.Left as IdentifierNameSyntax)?.Identifier.ValueText;
            var operationToken = nodeExpression.OperatorToken.ValueText;
            var rightOperand = nodeExpression.Right;

            switch (rightOperand)
            {
                // a += 4 or b *= 23
                case LiteralExpressionSyntax literalExpression:
                    var rightOperandValue = literalExpression.Token.ValueText;
                    return $"{variableName} {operationToken} {rightOperandValue}";

                // a += c or b *= a
                case IdentifierNameSyntax identifierName:
                    var rightOperandName = identifierName.Identifier.ValueText;
                    return $"{variableName} {operationToken} {rightOperandName}";

                // a = b - 4 or c = a * 10 + b - 32
                default:
                    var binaryExpression = rightOperand as BinaryExpressionSyntax;
                    var leftRightParser = new LeftRightParser();
                    var leftRingParsingResult = leftRightParser.GetLeftRightParsingParserResult(binaryExpression);
                    return $"{variableName} {operationToken} {leftRingParsingResult}";
            }
        }
    }
}