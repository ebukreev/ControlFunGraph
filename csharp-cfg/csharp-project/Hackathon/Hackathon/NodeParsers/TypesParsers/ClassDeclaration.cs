using Microsoft.CodeAnalysis.CSharp.Syntax;

namespace AnalysisVerificationLab1.NodeParsers.TypesParsers
{
    public class ClassDeclaration
    {
        private string _classDeclarationParserResult;

        public string GetClassDeclarationParserResult(ClassDeclarationSyntax node)
        {
            _classDeclarationParserResult = ClassDeclarationParser(node);
            return _classDeclarationParserResult;
        }
        
        private static string ClassDeclarationParser(ClassDeclarationSyntax node)
        {
            var nodeType = node.Keyword.ValueText;
            var nodeName = node.Identifier.ValueText;
            return $"{nodeType} {nodeName}";
        }
    }
}