using Microsoft.CodeAnalysis.CSharp.Syntax;

namespace AnalysisVerificationLab1.NodeParsers.TypesParsers
{
    public class PostfixUnaryExpression
    {
        private string _postfixUnaryExpressionParserResult;

        public string GetPostfixUnaryExpressionParserResult(PostfixUnaryExpressionSyntax node)
        {
            _postfixUnaryExpressionParserResult = PostIncrementDecrementParser(node);
            return _postfixUnaryExpressionParserResult;
        }
        
        private static string PostIncrementDecrementParser(PostfixUnaryExpressionSyntax node)
        {
            var variableName = (node.Operand as IdentifierNameSyntax)?.Identifier.ValueText;
            var operationToken = node.OperatorToken.ValueText;
            return $"{variableName}{operationToken}";
        }
    }
}