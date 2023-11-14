using Microsoft.CodeAnalysis.CSharp.Syntax;

namespace AnalysisVerificationLab1.NodeParsers.TypesParsers
{
    public class ContinueStatement
    {
        private string _continueStatementParserResult;

        public string GetContinueStatementParserResult(ContinueStatementSyntax node)
        {
            _continueStatementParserResult = ContinueStatementParser(node);
            return _continueStatementParserResult;
        }

        private static string ContinueStatementParser(ContinueStatementSyntax node)
        {
            var result = node.ContinueKeyword.ValueText;
            return $"{result};";
        }
    }
}