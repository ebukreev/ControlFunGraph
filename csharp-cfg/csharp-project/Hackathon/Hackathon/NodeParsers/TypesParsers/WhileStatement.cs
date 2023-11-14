using Microsoft.CodeAnalysis;
using Microsoft.CodeAnalysis.CSharp;
using Microsoft.CodeAnalysis.CSharp.Syntax;

namespace AnalysisVerificationLab1.NodeParsers.TypesParsers
{
    public class WhileStatement
    {
        private string _whileStatementParserResult;

        public string GetWhileStatementParserResult(WhileStatementSyntax node)
        {
            _whileStatementParserResult = WhileStatementParser(node);
            return _whileStatementParserResult;
        }

        // TODO
        private static string WhileStatementParser(WhileStatementSyntax node)
        {
            var conditionNode = node.Condition;
            
            //CreateConditionText
            var binaryCondition = conditionNode as BinaryExpressionSyntax;
            var leftOperand = binaryCondition.Left;
            var leftOperandText ="0";
            var leftOperandKind = leftOperand.Kind();
            if(leftOperand.IsKind(SyntaxKind.IdentifierName))
            {
                leftOperandText = leftOperand.GetFirstToken().ValueText;
            }
            else if (leftOperand.IsKind(SyntaxKind.NumericLiteralExpression))
            {
                leftOperandText = $"{(leftOperand as LiteralExpressionSyntax).Token.ValueText}";
            }
            else if (leftOperand.IsKind(SyntaxKind.SimpleMemberAccessExpression))
            {
                leftOperandText = $"{(leftOperand as MemberAccessExpressionSyntax).Expression.GetFirstToken().ValueText}" +
                                  $"{(leftOperand as MemberAccessExpressionSyntax).OperatorToken.ValueText}" +
                                  $"{(leftOperand as MemberAccessExpressionSyntax).Name.Identifier.ValueText}";
            }

            var rightOperand = binaryCondition.Right;
            var rightOperandText ="0";
            var rightOperandKind = rightOperand.Kind();
            if(rightOperand.IsKind(SyntaxKind.IdentifierName))
            {
                rightOperandText = rightOperand.GetFirstToken().ValueText;
            }
            else if (rightOperand.IsKind(SyntaxKind.NumericLiteralExpression))
            {
                rightOperandText = $"{(rightOperand as LiteralExpressionSyntax).Token.ValueText}";
            }
            else if (rightOperand.IsKind(SyntaxKind.SimpleMemberAccessExpression))
            {
                rightOperandText = $"{(rightOperand as MemberAccessExpressionSyntax).Expression.GetFirstToken().ValueText}" +
                                   $"{(rightOperand as MemberAccessExpressionSyntax).OperatorToken.ValueText}" +
                                   $"{(rightOperand as MemberAccessExpressionSyntax).Name.Identifier.ValueText}";
            }
            
            var conditionText =
                $"{leftOperandText} " +
                $"{binaryCondition.OperatorToken.ValueText} " +
                $"{rightOperandText}";

            return $"while;{conditionText}";
        }
    }
}