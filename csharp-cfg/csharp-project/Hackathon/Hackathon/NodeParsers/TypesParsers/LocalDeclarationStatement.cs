using System.Linq;
using Microsoft.CodeAnalysis;
using Microsoft.CodeAnalysis.CSharp.Syntax;

namespace AnalysisVerificationLab1.NodeParsers.TypesParsers
{
    public class LocalDeclarationStatement
    {
        private string _localDeclarationStatementParserResult;

        public string GetLocalDeclarationStatementParserResult(LocalDeclarationStatementSyntax node)
        {
            _localDeclarationStatementParserResult = LocalDeclarationStatementParser(node);
            return _localDeclarationStatementParserResult;
        }

        private static string LocalDeclarationStatementParser(LocalDeclarationStatementSyntax node)
        {
            var variable = node.Declaration.Variables[0];
            var variableType = node.Declaration.Type.GetLastToken().ValueText;
            var variableName = variable.Identifier.ValueText;

            if (variable.Initializer == null) return $"{variableName}";

            var operation = variable.Initializer.EqualsToken.ValueText;
            var value = variable.Initializer.Value.GetFirstToken().ValueText;

            // var a = 4; string c = "qwe"; var b = c; double k;
            if (variable.Initializer.Value is not ObjectCreationExpressionSyntax objectCreationExpression)
                return $"{variableName} {operation} {value}";
            
            // new List<string> or new HashMap<int, string>
            var keywordValue = objectCreationExpression.NewKeyword.ValueText;

            var objectCreationType = objectCreationExpression.Type;
            var objectName = objectCreationType.GetFirstToken().ValueText;

            var genericName = objectCreationType as GenericNameSyntax;
            if (genericName == null) return $"{variableName} {operation} {value}";
            var argumentsList = genericName.TypeArgumentList.Arguments;

            var fullValue = keywordValue + " " + objectName + "<";

            fullValue = argumentsList.Select(argument => (argument as PredefinedTypeSyntax)?.Keyword.ValueText)
                .Aggregate(fullValue, (current, argumentName) => current + (argumentName + ", "));

            fullValue = fullValue[..^2] + ">";
            value = fullValue;

            return $"{variableName} {operation} {value}";
        }
    }
}