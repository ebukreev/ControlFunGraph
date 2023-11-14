using Microsoft.CodeAnalysis.CSharp.Syntax;

namespace AnalysisVerificationLab1.NodeParsers.TypesParsers
{
    public class BreakStatement
    {
        private string _breakStatementParserResult;

        public string GetBreakStatementParserResult(BreakStatementSyntax node)
        {
            _breakStatementParserResult = BreakStatementParser(node);
            return _breakStatementParserResult;
        }
        
        private static string BreakStatementParser(BreakStatementSyntax node)
        {
            var result = node.BreakKeyword.ValueText;
            return $"{result};";
        }
    }
}