using Microsoft.CodeAnalysis.CSharp.Syntax;

namespace AnalysisVerificationLab1.NodeParsers.TypesParsers
{
    public class ReturnStatement
    {
        private string _returnStatementParserResult;

        public string GetReturnStatementParserResult(ReturnStatementSyntax node)
        {
            _returnStatementParserResult = ReturnStatementParser(node);
            return _returnStatementParserResult;
        }

        private static string ReturnStatementParser(ReturnStatementSyntax node)
        {
            var operationName = node.ReturnKeyword.ValueText;
            var nodeExpression = node.Expression;
            switch (nodeExpression)
            {
                // return a + b or return b - d + e
                case BinaryExpressionSyntax binaryExpression:
                {
                    var operationToken = binaryExpression.OperatorToken.ValueText;
                    var leftRightParser = new LeftRightParser();
                    var leftRingParsingResult = leftRightParser.GetLeftRightParsingParserResult(binaryExpression);
                    return $"{operationName} {operationToken} {leftRingParsingResult}";
                }

                // exception
                case null:
                    return "error";

                // return A or return 5
                default:
                {
                    var variableName = nodeExpression.GetFirstToken().ValueText;
                    return $"{operationName} {variableName}";
                }
            }
        }
    }
}