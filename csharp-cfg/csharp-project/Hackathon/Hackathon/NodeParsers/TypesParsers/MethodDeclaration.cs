using System.Linq;
using Microsoft.CodeAnalysis.CSharp.Syntax;

namespace AnalysisVerificationLab1.NodeParsers.TypesParsers
{
    public class MethodDeclaration
    {
        private string _methodDeclarationParserResult;

        public string GetMethodDeclarationParserResult(MethodDeclarationSyntax node)
        {
            _methodDeclarationParserResult = MethodDeclarationParser(node);
            return _methodDeclarationParserResult;
        }
        
        private static string MethodDeclarationParser(MethodDeclarationSyntax node)
        {
            var nodeName = node.Identifier.ValueText;
            var methodModifier = string.Join(" ", node.Modifiers.Select(x => x.ValueText));
            var methodReturnType = node.ReturnType.GetFirstToken().ValueText;
            return $"{methodModifier} {methodReturnType} {nodeName}";
        }
    }
}