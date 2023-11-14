using Microsoft.CodeAnalysis.CSharp.Syntax;

namespace AnalysisVerificationLab1.NodeParsers.TypesParsers
{
    public class PrefixUnaryExpression
    {
        private string _prefixUnaryExpressionParserResult;

        public string GetPrefixUnaryExpressionParserResult(PrefixUnaryExpressionSyntax node)
        {
            _prefixUnaryExpressionParserResult = PreIncrementDecrementParser(node);
            return _prefixUnaryExpressionParserResult;
        }

        private static string PreIncrementDecrementParser(PrefixUnaryExpressionSyntax node)
        {
            if (node.Parent is not AssignmentExpressionSyntax nodeParent) return null;
            var variableName = (nodeParent.Left as IdentifierNameSyntax)?.Identifier.ValueText;
            var operationTokenFirst = nodeParent.OperatorToken.ValueText;
            var operationTokenSecond = node.OperatorToken.ValueText;
            var operandName = (node.Operand as IdentifierNameSyntax)?.Identifier.ValueText;

            return $"{variableName} {operationTokenFirst} {operationTokenSecond}{operandName}";
        }
    }
}