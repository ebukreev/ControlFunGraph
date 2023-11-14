using System.Linq;
using Microsoft.CodeAnalysis.CSharp.Syntax;

namespace AnalysisVerificationLab1.NodeParsers.TypesParsers
{
    public class ParameterList
    {
        private string _parameterListParserResult;

        public string GetParameterListParserResult(ParameterListSyntax node)
        {
            _parameterListParserResult = ParameterListParser(node);
            return _parameterListParserResult;
        }
        
        private static string ParameterListParser(ParameterListSyntax node)
        {
            var parameters = node.Parameters;
            if (parameters.Count != 0)
            {
                var result =
                    parameters.Aggregate("",
                        (current, parameter) => current + (parameter.Identifier.ValueText + ", "))[..^2];
                return $"{result}";
            }
            
            return $"empty";
        }
    }
}