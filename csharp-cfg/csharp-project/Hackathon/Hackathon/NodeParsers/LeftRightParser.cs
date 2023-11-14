using Microsoft.CodeAnalysis;
using Microsoft.CodeAnalysis.CSharp;
using Microsoft.CodeAnalysis.CSharp.Syntax;

namespace AnalysisVerificationLab1
{
    public class LeftRightParser
    {
        private string _leftRightParsingResult;

        public string GetLeftRightParsingParserResult(BinaryExpressionSyntax node)
        {
            _leftRightParsingResult = LeftRightParsing(node);
            return _leftRightParsingResult;
        }
        private static string LeftRightParsing(BinaryExpressionSyntax binaryExpression)
        {
            var operationToken = binaryExpression.OperatorToken.ValueText;
            
            var currentLeft = binaryExpression.Left;
            var currentRight = binaryExpression.Right;

            var leftOperand = "";
            var rightOperand = "";
            
            while (!currentLeft.IsKind(SyntaxKind.IdentifierName) && leftOperand.Length==0)
            {
                if (currentLeft is LiteralExpressionSyntax literalLeftExpression)
                {
                    leftOperand = literalLeftExpression.Token.ValueText;
                }
                else
                {
                    if (currentLeft is ParenthesizedExpressionSyntax syntax)
                    {
                        var leftExpression = syntax.Expression;
                        leftOperand = "(" + LeftRightParsing(leftExpression as BinaryExpressionSyntax) + ")";
                    }
                    else
                    {
                        leftOperand = LeftRightParsing(currentLeft as BinaryExpressionSyntax);
                    }
                }
            }

            if (leftOperand.Length == 0)
            {
                leftOperand = currentLeft.GetFirstToken().ValueText;
            }

            while (!currentRight.IsKind(SyntaxKind.IdentifierName) && rightOperand.Length==0)
            {
                if (currentRight is LiteralExpressionSyntax literalRightExpression)
                {
                    rightOperand = literalRightExpression.Token.ValueText;
                }
                else
                {
                    if (currentRight is ParenthesizedExpressionSyntax syntax)
                    {
                        var rightExpression = syntax.Expression;
                        rightOperand = "(" + LeftRightParsing(rightExpression as BinaryExpressionSyntax) + ")";
                    }
                    else
                    {
                        rightOperand = LeftRightParsing(currentRight as BinaryExpressionSyntax);
                    }
                }
            }
            
            if (rightOperand.Length == 0)
            {
                rightOperand = currentRight.GetFirstToken().ValueText;
            }
            
            var totalRes = $"{leftOperand} {operationToken} {rightOperand}";
            
            return totalRes;
        }
    }
}