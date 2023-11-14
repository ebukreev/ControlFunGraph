using System;
using System.Collections.Generic;
using System.Linq;
using System.IO;
using System.Text;
using Microsoft.CodeAnalysis;
using Microsoft.CodeAnalysis.CSharp;
using Microsoft.CodeAnalysis.CSharp.Syntax;
using AnalysisVerificationLab1.NodeParsers.TypesParsers;

namespace Hackathon
{
    public static class Program
    {
        private static Dictionary<int, List<int>> _nodeDictionary = new();
        private static Dictionary<int, string> _linesDictionary = new();
        private static Dictionary<int, string> _numbersDictionary = new();

        static void Main(string[] args)
        {
            // Get input string from Cin
            var inputString = "namespace Hackathon.TestsClasses\n{\npublic class ClassTest\n{\n";
            var newLine = Console.ReadLine();
            var lineIndex = 1;
            while (!newLine.Equals("!!!END!!!"))
            {
                if (!string.IsNullOrEmpty(newLine) && !newLine.Trim().Equals("}"))
                {
                    _numbersDictionary[lineIndex] = newLine.Trim();
                }

                lineIndex++;

                inputString += newLine + "\n";
                newLine = Console.ReadLine();
            }

            inputString += "}\n}\n";

            var filePath =
                "D:\\RiderProjects\\Hackathon\\Hackathon\\FullResult.dot";

            // TODO
            //using StreamWriter file = new(filePath, append: false);
            //file.WriteLine("digraph G {");

            // Create StringBuilder for dot-file content
            StringBuilder dotFileContent = new StringBuilder();
            dotFileContent.AppendLine("digraph G {");

            SyntaxTree tree = CSharpSyntaxTree.ParseText(inputString);
            SyntaxNode root = tree.GetRoot();


            var rootMembersArray =
                ((ClassDeclarationSyntax)((root as CompilationUnitSyntax)?.Members[0] as NamespaceDeclarationSyntax)
                    ?.Members[0]!)!.Members;

            var testId = 1;
            foreach (var member in rootMembersArray)
            {
                // TODO
                // GenerateDotFile(member, file, testId, dotFileContent);
                GenerateDotFile(member, testId, dotFileContent);
                testId++;
            }

            // TODO
            //file.WriteLine("}");

            dotFileContent.AppendLine("}");

            // return dot-file content in Cout
            Console.WriteLine(dotFileContent);
        }

        private static void GenerateDotFile(SyntaxNode node, int testId,
            StringBuilder dotFileContent)
        {
            _nodeDictionary = new Dictionary<int, List<int>>();
            _linesDictionary = new Dictionary<int, string>();

            int nodeId = 0;
            SerializeGraphNode(node, ref nodeId);

            var result = new Dictionary<int, List<int>>();
            var newLinesDictionary = new Dictionary<int, string>(_linesDictionary);

            var clusterName = newLinesDictionary[0].Split(" ").Last();

            dotFileContent.AppendLine($"subgraph cluster_{clusterName}" + " {");
            dotFileContent.AppendLine($"label = {clusterName}");
            dotFileContent.AppendLine($"comment = \"1\"");

            // TODO
            //file.WriteLine($"subgraph cluster_{clusterName}" + " {");
            //file.WriteLine($"label = {clusterName}");
            //file.WriteLine($"comment = \"1\"");

            // remove clusterName in newLinesDictionary
            newLinesDictionary.Remove(0);

            // Create Connections Before First Block
            foreach (var (key, value) in newLinesDictionary)
            {
                // check first Block -> break
                if (value.Equals("Block"))
                {
                    break;
                }

                // it's not the first Block
                var currentIndex = Array.IndexOf(newLinesDictionary.Keys.ToArray(), key);
                if (currentIndex == -1 || currentIndex + 1 >= newLinesDictionary.Keys.Count) continue;

                var nextKey = newLinesDictionary.Keys.ToArray()[currentIndex + 1];
                newLinesDictionary.Remove(key);
                result[key] = new List<int> { nextKey };
                if (!value.Equals("empty"))
                {
                    dotFileContent.AppendLine(
                        $" node{key + testId * 1000}[label=\"{value}\", fillcolor=green, style=filled]");

                    // TODO
                    //file.WriteLine($" node{key + testId * 1000}[label=\"{value}\", fillcolor=green, style=filled]");
                }
                else
                {
                    const string entryValue = "entry";
                    dotFileContent.AppendLine(
                        $" node{key + testId * 1000}[label=\"{entryValue}\", fillcolor=green, style=filled]");
                    // TODO
                    //file.WriteLine(
                    //    $" node{key + testId * 1000}[label=\"{entryValue}\", fillcolor=green, style=filled]");
                }
            }

            // Generate current Cluster
            //TODO CreateNewConnections(_nodeDictionary, newLinesDictionary, testId, file, result, dotFileContent);
            CreateNewConnections(_nodeDictionary, newLinesDictionary, testId, result, dotFileContent);

            dotFileContent.AppendLine("}");
            // TODO
            //file.WriteLine("}");
        }

        private static void SerializeGraphNode(SyntaxNode node, ref int nodeId)
        {
            string newLine = null;
            var nodeIdCurrent = nodeId;

            List<String> unknownNodeTypes = new List<string>();

            // Parse nodeTypes
            switch (node)
            {
                // class
                case ClassDeclarationSyntax classDeclaration:
                {
                    var classDeclarationInstance = new ClassDeclaration();
                    newLine = classDeclarationInstance.GetClassDeclarationParserResult(classDeclaration);
                    break;
                }

                //method
                case MethodDeclarationSyntax methodDeclaration:
                {
                    var methodDeclarationInstance = new MethodDeclaration();
                    newLine = methodDeclarationInstance.GetMethodDeclarationParserResult(methodDeclaration);
                    break;
                }

                //arguments in Method
                case ParameterListSyntax parameterList:
                {
                    var parameterListInstance = new ParameterList();
                    newLine = parameterListInstance.GetParameterListParserResult(parameterList);
                    break;
                }

                //assigment (a = b) or (new List<string> or new HashMap<int, string>)
                case LocalDeclarationStatementSyntax localDeclarationStatement:
                {
                    var localDeclarationStatementInstance = new LocalDeclarationStatement();
                    newLine =
                        localDeclarationStatementInstance.GetLocalDeclarationStatementParserResult(
                            localDeclarationStatement);
                    break;
                }

                // operator (+,+=, -, *, ++, ...)
                case ExpressionStatementSyntax expressionStatement:
                {
                    var expressionStatementInstance = new ExpressionStatement();
                    newLine = expressionStatementInstance.GetExpressionStatementParserResult(expressionStatement);
                    break;
                }

                // if ( a>=b) or (5 < 4)
                // work only with BinaryOperations: ==, >=, <=, !=
                case IfStatementSyntax ifStatement:
                {
                    var ifStatementInstance = new IfStatement();
                    newLine = ifStatementInstance.GetIfStatementParserResult(ifStatement);
                    break;
                }

                case BlockSyntax:
                {
                    newLine = "Block";
                    break;
                }

                case ElseClauseSyntax:
                {
                    newLine = "else";
                    break;
                }

                // return
                case ReturnStatementSyntax returnStatement:
                {
                    var returnStatementInstance = new ReturnStatement();
                    newLine = returnStatementInstance.GetReturnStatementParserResult(returnStatement);
                    break;
                }

                // for (int or var i = 0; i < b; i++)
                // work only with int values (int b; b.Count - int)
                case ForStatementSyntax forStatementSyntax:
                    var forStatementInstance = new ForStatement();
                    newLine = forStatementInstance.GetForStatementParserResult(forStatementSyntax);
                    break;

                // while (x > 5) or (5 < x) or (x != y)
                // work only with int values (int x; x.Count - int)
                case WhileStatementSyntax whileStatementSyntax:
                    var whileStatementInstance = new WhileStatement();
                    newLine = whileStatementInstance.GetWhileStatementParserResult(whileStatementSyntax);
                    break;


                // break
                case BreakStatementSyntax breakStatementSyntax:
                    var breakStatementInstance = new BreakStatement();
                    newLine = breakStatementInstance.GetBreakStatementParserResult(breakStatementSyntax);
                    break;

                // TODO - optional
                // continue
                case ContinueStatementSyntax continueStatementSyntax:
                    var continueStatementInstance = new ContinueStatement();
                    newLine = continueStatementInstance.GetContinueStatementParserResult(continueStatementSyntax);
                    break;

                // unknowns syntax: switch, case, continue, do-while...
                default:
                {
                    unknownNodeTypes.Add(node.GetType().Name);
                    break;
                }
            }

            if (newLine != null)
            {
                _linesDictionary.Add(nodeIdCurrent, newLine);
            }

            foreach (var child in node.ChildNodes())
            {
                nodeId++;
                var nodeIdChild = nodeId;

                if (_nodeDictionary.ContainsKey(nodeIdCurrent))
                {
                    _nodeDictionary[nodeIdCurrent].Add(nodeIdChild);
                }
                else
                {
                    var values = new List<int> { nodeIdChild };
                    _nodeDictionary[nodeIdCurrent] = values;
                }

                SerializeGraphNode(child, ref nodeId);
            }
        }


        private static void CreateNewConnections(Dictionary<int, List<int>> nodeDictionary,
            Dictionary<int, string> linesDictionary, int testId,
            Dictionary<int, List<int>> oldResult, StringBuilder dotFileContent)
        {
            var result = new Dictionary<int, List<int>>(oldResult);
            var newLinesDictionary = new Dictionary<int, string>(linesDictionary);

            var blockDictionary = CreateBlockDictionary(nodeDictionary, newLinesDictionary);
            var forDictionary = CreateForDictionary(nodeDictionary, newLinesDictionary);
            var whileDictionary = CreateWhileDictionary(nodeDictionary, newLinesDictionary);
            var elseDictionary = CreateElseDictionary(nodeDictionary, newLinesDictionary);
            var breakDictionary =
                CreateBreakDictionary(nodeDictionary, newLinesDictionary, blockDictionary, elseDictionary);
            var continueDictionary =
                CreateContinueDictionary(nodeDictionary, newLinesDictionary, blockDictionary, elseDictionary);

            var firstBlockKey = blockDictionary.First().Key;

            // CreateBlocksConnections
            foreach (var (key, value) in blockDictionary)
            {
                var currentIndex = 0;
                result[key] = new List<int> { value[currentIndex] };
                foreach (var v in value.Where(_ => value.Count - 1 - currentIndex > 0))
                {
                    result[v] = new List<int> { value[currentIndex + 1] };
                    currentIndex++;
                }
            }

            // create Declaration-block before For-block + create Increment-block
            var incrementBlocksDictionary = new Dictionary<int, int>();
            foreach (var (key, value) in forDictionary)
            {
                var declarationBlockId = value - 1;
                var incrementBlockId = value - 2;
                var splitValue = newLinesDictionary[key].Split(";");

                var declarationValue = splitValue[1];
                var incrementValue = splitValue[3];

                // create record
                newLinesDictionary[declarationBlockId] = declarationValue;
                newLinesDictionary[incrementBlockId] = incrementValue;

                newLinesDictionary[key] = $"{splitValue[0]};{splitValue[2]}";

                //change value in result
                var preNodeKey = result.SingleOrDefault(x => x.Value.Contains(key)).Key;
                var values = result[preNodeKey];
                values[values.IndexOf(key)] = declarationBlockId;

                //create new connection in result
                result[declarationBlockId] = new List<int> { key };
                result[incrementBlockId] = new List<int> { key };

                incrementBlocksDictionary[key] = incrementBlockId;
            }

            // Parse Blocks
            var reverseKeysBlockDictionary = blockDictionary.Keys.Reverse().ToList();
            reverseKeysBlockDictionary.RemoveAt(reverseKeysBlockDictionary.Count - 1);
            var forWhileNeedConnectionToNext = new List<int>();
            var ifNeedConnectionToNext = new Dictionary<int, int>();
            var elseBranchLast = new Dictionary<int, List<int>>();
            foreach (var key in reverseKeysBlockDictionary)
            {
                var parentKey = nodeDictionary.SingleOrDefault(x => x.Value.Contains(key)).Key;
                var parentType = newLinesDictionary[parentKey];

                // Add connection from parentKey to parentKey-block
                if (!result.ContainsKey(parentKey))
                {
                    result[parentKey] = new List<int> { key };
                }
                else
                {
                    result[parentKey].Add(key);
                }

                // Add connections from If-block to Else-block
                if (parentType.Length == 4 && parentType.Equals("else"))
                {
                    var parentBlockIfKey = nodeDictionary.SingleOrDefault(x => x.Value.Contains(parentKey)).Key;
                    if (!result.ContainsKey(parentBlockIfKey))
                    {
                        result[parentBlockIfKey] = new List<int> { parentKey };
                    }
                    else
                    {
                        result[parentBlockIfKey].Add(parentKey);
                    }
                }

                // Parse Blocks with their types
                switch (parentType.Length)
                {
                    // If-parser
                    case > 3 when parentType[..3].Equals("if ") && !parentType.Equals("else"):
                    {
                        var currentIfBlock = result.SingleOrDefault(x => x.Key.Equals(parentKey));

                        // find last in block-then
                        var blockThen = result.SingleOrDefault(x => x.Key.Equals(key));
                        var lastBlocksThen = FindLastBlocksKeys(new List<int>(), result, newLinesDictionary,
                            blockThen);

                        var nextBlockKey = -1;

                        var lastBlocksElse = new List<int>();
                        foreach (var value in currentIfBlock.Value)
                        {
                            // find block-else
                            if (newLinesDictionary[value].Equals("else"))
                            {
                                var blockElse = result.SingleOrDefault(x => x.Key.Equals(value));

                                // find last in block-else
                                lastBlocksElse = FindLastBlocksKeys(new List<int>(), result, newLinesDictionary,
                                    blockElse);
                                if (lastBlocksElse.Count != 0)
                                {
                                    elseBranchLast[currentIfBlock.Key] = lastBlocksElse;
                                }
                            }

                            // find block-next
                            else if (!newLinesDictionary[value].Equals("Block"))
                            {
                                nextBlockKey = value;
                            }
                        }

                        // add connections
                        if (nextBlockKey != -1)
                        {
                            lastBlocksThen = lastBlocksThen.Distinct().ToList();
                            // then -> next
                            foreach (var value in lastBlocksThen)
                            {
                                if (result.ContainsKey(value))
                                {
                                    result[value].Add(nextBlockKey);
                                }
                                else
                                {
                                    result[value] = new List<int> { nextBlockKey };
                                }
                            }

                            // else -> next
                            if (lastBlocksElse.Count != 0)
                            {
                                lastBlocksElse = lastBlocksElse.Distinct().ToList();
                                foreach (var value in lastBlocksElse)
                                {
                                    if (result.ContainsKey(value))
                                    {
                                        result[value].Add(nextBlockKey);
                                    }
                                    else
                                    {
                                        result[value] = new List<int> { nextBlockKey };
                                    }
                                }

                                // Delete connection If->next
                                result.SingleOrDefault(x => x.Key.Equals(parentKey)).Value.Remove(nextBlockKey);
                            }
                        }

                        // need connection to next block
                        else
                        {
                            if (!elseBranchLast.ContainsKey(currentIfBlock.Key))
                            {
                                if (lastBlocksThen.Count == 0)
                                {
                                    ifNeedConnectionToNext[currentIfBlock.Key] = -1;
                                }
                                else
                                {
                                    ifNeedConnectionToNext[currentIfBlock.Key] = lastBlocksThen[0];
                                }
                            }
                        }

                        break;
                    }


                    // For-parser
                    case > 4 when parentType[..4].Equals("for;"):
                    {
                        var currentForBlock = result.SingleOrDefault(x => x.Key.Equals(parentKey));

                        // find last in block-then
                        var blockThen = result.SingleOrDefault(x => x.Key.Equals(key));
                        var lastBlocksThen = FindLastBlocksKeys(new List<int>(), result, newLinesDictionary,
                            blockThen);
                        lastBlocksThen = lastBlocksThen.Distinct().ToList();

                        // find Current Increment Block
                        var incrementBlockKey = incrementBlocksDictionary[parentKey];

                        // add connections then -> increment current For-block
                        foreach (var value in lastBlocksThen)
                        {
                            if (result.ContainsKey(value))
                            {
                                result[value].Add(incrementBlockKey);
                            }
                            else
                            {
                                result[value] = new List<int> { incrementBlockKey };
                            }
                        }

                        // need connection to next block
                        if (result[currentForBlock.Key].Count == 1)
                        {
                            forWhileNeedConnectionToNext.Add(currentForBlock.Key);
                        }

                        break;
                    }

                    // While-parser
                    case > 6 when parentType[..6].Equals("while;"):
                    {
                        var currentWhileBlock = result.SingleOrDefault(x => x.Key.Equals(parentKey));

                        // find last in block-then
                        var blockThen = result.SingleOrDefault(x => x.Key.Equals(key));
                        var lastBlocksThen = FindLastBlocksKeys(new List<int>(), result, newLinesDictionary,
                            blockThen);
                        lastBlocksThen = lastBlocksThen.Distinct().ToList();


                        // add connections then ->current While-block
                        foreach (var value in lastBlocksThen)
                        {
                            if (result.ContainsKey(value))
                            {
                                result[value].Add(currentWhileBlock.Key);
                            }
                            else
                            {
                                result[value] = new List<int> { currentWhileBlock.Key };
                            }
                        }

                        // need connection to next block
                        if (result[currentWhileBlock.Key].Count == 1)
                        {
                            forWhileNeedConnectionToNext.Add(currentWhileBlock.Key);
                        }

                        break;
                    }
                }
            }

            // create connections from For/While-blocks -> next Block
            foreach (var value in forWhileNeedConnectionToNext)
            {
                var currentNode = result.SingleOrDefault(x => x.Key.Equals(value));

                if (currentNode.Value.Count != 1) continue;

                // find and add connection
                var nextBlockKey = FindNextBlockUp(result, newLinesDictionary, currentNode, blockDictionary,
                    incrementBlocksDictionary, elseBranchLast);
                result[currentNode.Key].Add(nextBlockKey);
            }

            // create connections from Break -> next Block
            foreach (var (key, value) in breakDictionary)
            {
                var currentNode = result.SingleOrDefault(x => x.Key.Equals(value));
                if (result[value].Count == 2)
                {
                    var nextBlockKey = newLinesDictionary[result[value][0]].Equals("Block")
                        ? result[value][1]
                        : result[value][0];
                    result[key] = new List<int> { nextBlockKey };
                }
                else
                {
                    // find next block key
                    var nextBlockKey = FindNextBlockUp(result, newLinesDictionary, currentNode, blockDictionary,
                        incrementBlocksDictionary, elseBranchLast);
                    result[key] = new List<int> { nextBlockKey };
                }
            }

            // create connections from Continue -> current Cycle Block
            foreach (var (key, value) in continueDictionary)
            {
                var currentNode = result.SingleOrDefault(x => x.Key.Equals(value));

                if (forDictionary.ContainsKey(value))
                {
                    var incrementBlockKey = incrementBlocksDictionary[value];
                    result[key] = new List<int> { incrementBlockKey };
                }

                if (whileDictionary.ContainsKey(value))
                {
                    result[key] = new List<int> { value };
                }
            }

            // create connections from If-blocks -> next Block
            foreach (var (key, value) in ifNeedConnectionToNext)
            {
                var currentNode = result.SingleOrDefault(x => x.Key.Equals(key));

                // last in block then != return/break
                if (value != -1)
                {
                    // If-block haven't got next block connection yet
                    if (currentNode.Value.Count == 1)
                    {
                        // find next block key
                        var nextBlockKey = result.SingleOrDefault(x => x.Key == value).Key;

                        // last in branch then == For-block or While-block
                        if (forDictionary.ContainsKey(nextBlockKey) || whileDictionary.ContainsKey(nextBlockKey))
                        {
                            // For/while-block have got next ?
                            var nextBlockChildrenCount = result[nextBlockKey].Count;
                            nextBlockKey = nextBlockChildrenCount == 2
                                ? result[nextBlockKey].SingleOrDefault(x => !newLinesDictionary[x].Equals("Block"))
                                : FindNextBlockUp(result, newLinesDictionary, currentNode, blockDictionary,
                                    incrementBlocksDictionary, elseBranchLast);
                        }
                        else
                        {
                            nextBlockKey = result.SingleOrDefault(x => x.Key == value).Value[0];
                        }

                        // add connection
                        result[currentNode.Key].Add(nextBlockKey);
                    }
                }

                // last in block then == return
                else
                {
                    // find next block key
                    var nextBlockKey = FindNextBlockUp(result, newLinesDictionary, currentNode, blockDictionary,
                        incrementBlocksDictionary, elseBranchLast);

                    // add connection
                    result[currentNode.Key].Add(nextBlockKey);
                }
            }


            //delete connections inc->for without connection to this increment
            foreach (var (_, incKey) in incrementBlocksDictionary)
            {
                var containsFlag = false;
                foreach (var (_, values) in result)
                {
                    // try find Inc-block in Values
                    if (values.Contains(incKey))
                    {
                        containsFlag = true;
                    }
                }

                // del this inc-block
                if (!containsFlag)
                {
                    result.Remove(incKey);
                    newLinesDictionary.Remove(incKey);
                }
            }

            // Create if-labels Dictionary
            var ifLabelsDictionary = CreateIfLabelsDictionary(result, newLinesDictionary);

            // Create for-labels Dictionary
            var forLabelsDictionary = CreateForWhileLabelsDictionary(result, newLinesDictionary, true);

            // Create while-labels Dictionary
            var whileLabelsDictionary = CreateForWhileLabelsDictionary(result, newLinesDictionary, false);

            // Delete Block from If with letters "Block", "else"
            var resultAfterDelIfBlocks = DeleteIfBlocks(result, newLinesDictionary, ifLabelsDictionary);
            result = resultAfterDelIfBlocks;

            // Delete Block from For with letters "Block"
            var resultAfterDelForBlocks = DeleteForWhileBlocks(result, newLinesDictionary, forLabelsDictionary, true);
            result = resultAfterDelForBlocks;

            // Delete Block from While with letters "Block"
            var resultAfterDelWhileBlocks =
                DeleteForWhileBlocks(result, newLinesDictionary, whileLabelsDictionary, false);
            result = resultAfterDelWhileBlocks;

            // Create merged labels dictionary
            var labelsDictionary = ifLabelsDictionary.Union(forLabelsDictionary).Union(whileLabelsDictionary)
                .ToDictionary(x => x.Key, x => x.Value);

            // Delete first Block
            var resultAfterDelFirstBlock = DeleteFirstBlock(result, newLinesDictionary, firstBlockKey);
            result = resultAfterDelFirstBlock;

            // merged Return-blocks in one "Exit-block"
            var resultAfterMergedReturnBlock = MergedReturnBlocks(result, newLinesDictionary, labelsDictionary);
            result = resultAfterMergedReturnBlock;

            // Delete Break-blocks
            var resultAfterDelBreakBlocks =
                DeleteBreakContinueBlocks(result, newLinesDictionary, breakDictionary, labelsDictionary);
            result = resultAfterDelBreakBlocks;

            // Delete Continue-blocks
            var resultAfterDelContinueBlocks =
                DeleteBreakContinueBlocks(result, newLinesDictionary, continueDictionary, labelsDictionary);
            result = resultAfterDelContinueBlocks;

            // Create DOT file
            // TODO
            // PrintNodes(result, newLinesDictionary, file, labelsDictionary, testId, dotFileContent);
            PrintNodes(result, newLinesDictionary, labelsDictionary, testId, dotFileContent);
        }

        private static int FindNextBlockUp(Dictionary<int, List<int>> connections,
            Dictionary<int, string> linesDictionary, KeyValuePair<int, List<int>> currentNode,
            Dictionary<int, List<int>> blockDictionary, Dictionary<int, int> incrementBlocksDictionary,
            Dictionary<int, List<int>> elseBranchLast)
        {
            var currentNodeKey = currentNode.Key;

            // Find parent Block key
            var parentBlockKey = blockDictionary.SingleOrDefault(x => x.Value.Contains(currentNodeKey)).Key;

            // Find parent Node key
            var parentNodeKey = connections.SingleOrDefault(x => x.Value.Contains(parentBlockKey)).Key;

            if (linesDictionary[parentNodeKey].Length > 4 && linesDictionary[parentNodeKey][..4].Equals("for;"))
            {
                return incrementBlocksDictionary[parentNodeKey];
            }

            if (linesDictionary[parentNodeKey].Length > 6 && linesDictionary[parentNodeKey][..6].Equals("while;"))
            {
                return parentNodeKey;
            }

            if (linesDictionary[parentNodeKey].Length > 3 && linesDictionary[parentNodeKey][..3].Equals("if "))
            {
                var blockIfChildren = connections[parentNodeKey];
                if (blockIfChildren.Count == 2)
                {
                    // find next -> add connection
                    foreach (var value in blockIfChildren)
                    {
                        if (!linesDictionary[value].Equals("Block") && !linesDictionary[value].Equals("else"))
                        {
                            return value;
                        }
                    }

                    // find else -> try get next in else-branch or go up
                    if (elseBranchLast.ContainsKey(parentNodeKey))
                    {
                        if (connections.ContainsKey(elseBranchLast[parentNodeKey][0]))
                        {
                            return connections[elseBranchLast[parentNodeKey][0]][0];
                        }

                        var newNode = connections.SingleOrDefault(x => x.Key.Equals(parentNodeKey));
                        FindNextBlockUp(connections, linesDictionary, newNode, blockDictionary,
                            incrementBlocksDictionary,
                            elseBranchLast);
                    }
                    else
                    {
                        var newNode = connections.SingleOrDefault(x => x.Key.Equals(parentNodeKey));
                        return FindNextBlockUp(connections, linesDictionary, newNode, blockDictionary,
                            incrementBlocksDictionary,
                            elseBranchLast);
                    }
                }
                else
                {
                    var newNode = connections.SingleOrDefault(x => x.Key.Equals(parentNodeKey));
                    return FindNextBlockUp(connections, linesDictionary, newNode, blockDictionary,
                        incrementBlocksDictionary,
                        elseBranchLast);
                }
            }

            return 0;
        }

        private static List<int> FindLastBlocksKeys(List<int> curRes, Dictionary<int, List<int>> connections,
            Dictionary<int, string> linesDictionary, KeyValuePair<int, List<int>> currentNode)
        {
            // find block-For or block-While
            if (linesDictionary[currentNode.Key].Length > 4 && linesDictionary[currentNode.Key][..4].Equals("for;") ||
                linesDictionary[currentNode.Key].Length > 6 && linesDictionary[currentNode.Key][..6].Equals("while;"))
            {
                var blockForWhile = connections.SingleOrDefault(x => x.Key.Equals(currentNode.Key));
                if (blockForWhile.Value.Count > 1)
                {
                    var nextBlockInForKey = linesDictionary[blockForWhile.Value[0]].Equals("Block")
                        ? blockForWhile.Value[1]
                        : blockForWhile.Value[0];
                    if (!connections.ContainsKey(nextBlockInForKey))
                    {
                        switch (linesDictionary[nextBlockInForKey].Length)
                        {
                            // ignore Continue-block
                            case 9 when
                                linesDictionary[nextBlockInForKey].Equals("continue;"):
                                break;
                            // ignore Return-block
                            case > 7 when
                                linesDictionary[nextBlockInForKey][..7].Equals("return "):
                                break;
                            // ignore Break-block
                            case 6 when
                                linesDictionary[nextBlockInForKey].Equals("break;"):
                                break;
                            default:
                                curRes.Add(nextBlockInForKey);
                                break;
                        }
                    }
                    else
                    {
                        var newNode = connections.SingleOrDefault(x => x.Key == nextBlockInForKey);
                        FindLastBlocksKeys(curRes, connections, linesDictionary, newNode);
                    }
                }
                else
                {
                    curRes.Add(blockForWhile.Key);
                }
            }

            var isForBlock = linesDictionary[currentNode.Key].Length > 4 &&
                             linesDictionary[currentNode.Key][..4].Equals("for;");

            var iWhileBlock = linesDictionary[currentNode.Key].Length > 6 &&
                              linesDictionary[currentNode.Key][..6].Equals("while;");

            if (!isForBlock && !iWhileBlock)
            {
                foreach (var value in currentNode.Value)
                {
                    if (!connections.ContainsKey(value))
                    {
                        var linesValue = linesDictionary[value];

                        switch (linesValue.Length)
                        {
                            // ignore Continue-block
                            case 9 when linesValue.Equals("continue;"):
                                break;
                            // ignore Return-block
                            case > 7 when linesValue[..7].Equals("return "):
                                break;
                            // ignore Break-block
                            case 6 when linesValue.Equals("break;"):
                                break;
                            default:
                                curRes.Add(value);
                                break;
                        }
                    }
                    else
                    {
                        var newNode = connections.SingleOrDefault(x => x.Key == value);
                        FindLastBlocksKeys(curRes, connections, linesDictionary, newNode);
                    }
                }
            }

            return curRes;
        }

        private static Dictionary<KeyValuePair<int, int>, string> CreateIfLabelsDictionary(
            Dictionary<int, List<int>> connections, Dictionary<int, string> linesDictionary)
        {
            var result = new Dictionary<KeyValuePair<int, int>, string>();
            var ifDictionary = new Dictionary<int, List<int>>();

            foreach (var (key, value) in linesDictionary)
            {
                if (value.Length > 3 && value[..3].Equals("if "))
                {
                    ifDictionary[key] = connections[key];
                }
            }

            foreach (var (key, values) in ifDictionary)
            {
                // find block-then -> true
                var valueTrue = values.SingleOrDefault(x => linesDictionary[x].Equals("Block"));

                // find block-else -> false
                var valueFalse = values.SingleOrDefault(x => !linesDictionary[x].Equals("Block"));

                var minPair = new KeyValuePair<int, int>(key, valueTrue);
                var maxPair = new KeyValuePair<int, int>(key, valueFalse);

                result[minPair] = "true";
                result[maxPair] = "false";
            }

            return result;
        }

        // Delete Block from If with letters "Block", "else"
        private static Dictionary<int, List<int>> DeleteIfBlocks(Dictionary<int, List<int>> connections,
            Dictionary<int, string> linesDictionary, Dictionary<KeyValuePair<int, int>, string> labelsDictionary)
        {
            var result = new Dictionary<int, List<int>>(connections);
            var ifDictionary = new Dictionary<int, List<int>>();

            foreach (var (key, value) in linesDictionary)
            {
                if (value.Length > 3 && value[..3].Equals("if "))
                {
                    ifDictionary[key] = connections[key];
                }
            }

            var valuesToDeleteThen = new Dictionary<int, int>();
            var valuesToAddThen = new Dictionary<int, int>();

            var valuesToDeleteElse = new List<KeyValuePair<int, int>>();
            var valuesToAddElse = new Dictionary<int, int>();

            foreach (var (key, values) in ifDictionary)
            {
                foreach (var value in values)
                {
                    var curValue = linesDictionary[value];
                    switch (curValue.Length)
                    {
                        case 5 when curValue.Equals("Block"):
                        {
                            var nextNode = result[value][0];
                            valuesToAddThen[key] = nextNode;
                            valuesToDeleteThen[key] = value;
                            break;
                        }
                        case 4 when curValue.Equals("else"):
                        {
                            var nextBlock = result[value][0];
                            var first = new KeyValuePair<int, int>(key, value);

                            var nextNode = result[nextBlock][0];
                            valuesToAddElse[key] = nextNode;
                            var second = new KeyValuePair<int, int>(value, nextBlock);

                            valuesToDeleteElse.Add(first);
                            valuesToDeleteElse.Add(second);
                            break;
                        }
                    }
                }
            }

            foreach (var (key, value) in valuesToAddThen)
            {
                result[key].Add(value);
            }

            foreach (var (key, value) in valuesToDeleteThen)
            {
                var curPair = new KeyValuePair<int, int>(key, value);
                var tryGetValuePair = labelsDictionary.TryGetValue(curPair, out string stringValue);
                if (tryGetValuePair)
                {
                    var newValue = valuesToAddThen.SingleOrDefault(x => x.Key == key);
                    labelsDictionary.Remove(curPair);
                    labelsDictionary[newValue] = stringValue;
                }

                result.Remove(value);
                result.SingleOrDefault(x => x.Key == key).Value.Remove(value);
                linesDictionary.Remove(value);
            }

            foreach (var (key, value) in valuesToAddElse)
            {
                result[key].Add(value);
            }

            for (var i = 0; i < valuesToDeleteElse.Count; i += 2)
            {
                var curValueFirst = valuesToDeleteElse[i];
                var curValueSecond = valuesToDeleteElse[i + 1];

                var curPair = new KeyValuePair<int, int>(curValueFirst.Key, curValueFirst.Value);
                var tryGetValuePair = labelsDictionary.TryGetValue(curPair, out string stringValue);
                if (tryGetValuePair)
                {
                    var newValue = valuesToAddElse.SingleOrDefault(x => x.Key == curValueFirst.Key);
                    labelsDictionary.Remove(curPair);
                    labelsDictionary[newValue] = stringValue;
                }

                result.Remove(curValueFirst.Value);
                result.SingleOrDefault(x => x.Key == curValueFirst.Key).Value.Remove(curValueFirst.Value);
                linesDictionary.Remove(curValueFirst.Value);

                result.Remove(curValueSecond.Value);
                linesDictionary.Remove(curValueSecond.Value);
            }

            return result;
        }


        private static Dictionary<int, List<int>> DeleteBreakContinueBlocks(Dictionary<int, List<int>> connections,
            Dictionary<int, string> linesDictionary, Dictionary<int, int> breakContinueDictionary,
            Dictionary<KeyValuePair<int, int>, string> labelsDictionary)
        {
            var result = new Dictionary<int, List<int>>(connections);

            // create new connections from preBreak/Continue-Block -> nextBreak/Continue-Block
            foreach (var (key, _) in breakContinueDictionary)
            {
                // find pre/next-BlockKeys
                var preBreakContinueBlockKey = connections.SingleOrDefault(x => x.Value.Contains(key)).Key;
                var nextBreakContinueBlockKey = connections[key][0];

                // connection preBlock -> nextBlock
                result[preBreakContinueBlockKey].Add(nextBreakContinueBlockKey);

                // create newLabel from preBlock -> nextBlock
                var oldKeyValuePair = new KeyValuePair<int, int>(preBreakContinueBlockKey, key);
                if (labelsDictionary.ContainsKey(oldKeyValuePair))
                {
                    // create new Label block -> exit
                    var newKeyValuePair =
                        new KeyValuePair<int, int>(preBreakContinueBlockKey, nextBreakContinueBlockKey);
                    labelsDictionary[newKeyValuePair] = labelsDictionary[oldKeyValuePair];

                    // delete old Label block -> return
                    labelsDictionary.Remove(oldKeyValuePair);
                }

                //del old connection preBlock -> break/continue-Block
                result[preBreakContinueBlockKey].Remove(key);

                // del Break/Continue-block
                result.Remove(key);
            }

            // delete KeyValuePair in linesDictionary
            foreach (var (key, _) in breakContinueDictionary)
            {
                linesDictionary.Remove(key);
            }

            return result;
        }

        private static Dictionary<int, List<int>> MergedReturnBlocks(Dictionary<int, List<int>> connections,
            Dictionary<int, string> linesDictionary, Dictionary<KeyValuePair<int, int>, string> labelsDictionary)
        {
            var result = new Dictionary<int, List<int>>(connections);
            var returnDictionary = new Dictionary<int, List<int>>();

            foreach (var (returnKey, returnValue) in linesDictionary)
            {
                if (!(returnValue.Length > 7 && returnValue[..7].Equals("return "))) continue;

                var blockWithConnectionToReturnKeys = new List<int>();
                foreach (var (key, values) in connections)
                {
                    if (values.Contains(returnKey))
                    {
                        blockWithConnectionToReturnKeys.Add(key);
                    }
                }

                returnDictionary[returnKey] = blockWithConnectionToReturnKeys;
            }

            const int exitNodeKey = 0;

            // create new connections to block Exit and delete old to Return-block
            foreach (var (key, values) in returnDictionary)
            {
                foreach (var value in values)
                {
                    // connection block -> exit
                    result[value].Add(exitNodeKey);

                    // create newLabel from block -> exit
                    var oldKeyValuePair = new KeyValuePair<int, int>(value, key);
                    if (labelsDictionary.ContainsKey(oldKeyValuePair))
                    {
                        // create new Label block -> exit
                        var newKeyValuePair = new KeyValuePair<int, int>(value, exitNodeKey);
                        labelsDictionary[newKeyValuePair] = labelsDictionary[oldKeyValuePair];

                        // delete old Label block -> return
                        labelsDictionary.Remove(oldKeyValuePair);
                    }

                    //del old connection block -> return
                    result[value].Remove(key);
                }
            }

            // delete KeyValuePair in linesDictionary
            foreach (var (key, _) in returnDictionary)
            {
                linesDictionary.Remove(key);
            }

            linesDictionary[exitNodeKey] = "exit";

            return result;
        }


        private static Dictionary<int, List<int>> DeleteForWhileBlocks(Dictionary<int, List<int>> connections,
            Dictionary<int, string> linesDictionary, Dictionary<KeyValuePair<int, int>, string> labelsDictionary,
            bool isForBlock)
        {
            var result = new Dictionary<int, List<int>>(connections);
            var currentBlockDictionary = new Dictionary<int, List<int>>();
            if (isForBlock)
            {
                foreach (var (key, value) in linesDictionary)
                {
                    if (!(value.Length > 4 && value[..4].Equals("for;"))) continue;
                    currentBlockDictionary[key] = connections[key];
                }
            }
            else
            {
                foreach (var (key, value) in linesDictionary)
                {
                    if (!(value.Length > 6 && value[..6].Equals("while;"))) continue;
                    currentBlockDictionary[key] = connections[key];
                }
            }

            var valuesToDeleteTrue = new Dictionary<int, int>();
            var valuesToAddTrue = new Dictionary<int, int>();

            foreach (var (key, values) in currentBlockDictionary)
            {
                foreach (var value in values)
                {
                    var curValue = linesDictionary[value];
                    if (!(curValue.Length == 5 && curValue.Equals("Block"))) continue;

                    var nextNode = result[value][0];
                    valuesToAddTrue[key] = nextNode;
                    valuesToDeleteTrue[key] = value;
                }
            }

            foreach (var (key, value) in valuesToAddTrue)
            {
                result[key].Add(value);
            }

            foreach (var (key, value) in valuesToDeleteTrue)
            {
                var curPair = new KeyValuePair<int, int>(key, value);
                var tryGetValuePair = labelsDictionary.TryGetValue(curPair, out string stringValue);
                if (tryGetValuePair)
                {
                    var newValue = valuesToAddTrue.SingleOrDefault(x => x.Key == key);
                    labelsDictionary.Remove(curPair);
                    labelsDictionary[newValue] = stringValue;
                }

                result.Remove(value);
                result.SingleOrDefault(x => x.Key == key).Value.Remove(value);
                linesDictionary.Remove(value);
            }

            return result;
        }

        private static Dictionary<KeyValuePair<int, int>, string> CreateForWhileLabelsDictionary(
            Dictionary<int, List<int>> connections, Dictionary<int, string> linesDictionary, bool isBlockFor)
        {
            var result = new Dictionary<KeyValuePair<int, int>, string>();
            if (isBlockFor)
            {
                var forDictionary = new Dictionary<int, List<int>>();
                foreach (var (key, value) in linesDictionary)
                {
                    if (!(value.Length > 4 && value[..4].Equals("for;"))) continue;
                    forDictionary[key] = connections[key];
                }

                foreach (var (key, values) in forDictionary)
                {
                    foreach (var value in values)
                    {
                        var curPair = new KeyValuePair<int, int>(key, value);
                        if (linesDictionary[value].Equals("Block"))
                        {
                            result[curPair] = "true";
                        }
                        else
                        {
                            result[curPair] = "false";
                        }
                    }
                }
            }
            else
            {
                var whileDictionary = new Dictionary<int, List<int>>();
                foreach (var (key, value) in linesDictionary)
                {
                    if (!(value.Length > 6 && value[..6].Equals("while;"))) continue;
                    whileDictionary[key] = connections[key];
                }

                foreach (var (key, values) in whileDictionary)
                {
                    foreach (var value in values)
                    {
                        var curPair = new KeyValuePair<int, int>(key, value);
                        if (linesDictionary[value].Equals("Block"))
                        {
                            result[curPair] = "true";
                        }
                        else
                        {
                            result[curPair] = "false";
                        }
                    }
                }
            }

            return result;
        }

        private static Dictionary<int, List<int>> DeleteFirstBlock(Dictionary<int, List<int>> connections,
            Dictionary<int, string> linesDictionary, int firstBlockKey)
        {
            var result = new Dictionary<int, List<int>>(connections);
            //var firstBlockKey = linesDictionary.First().Key;
            var preNodeKey = result.SingleOrDefault(x => x.Value.Contains(firstBlockKey)).Key;
            var nextNodeKey = result.SingleOrDefault(x => x.Key == firstBlockKey).Value[0];

            result[preNodeKey].Add(nextNodeKey);

            result.Remove(firstBlockKey);
            result.SingleOrDefault(x => x.Key == preNodeKey).Value.Remove(firstBlockKey);
            linesDictionary.Remove(firstBlockKey);

            return result;
        }

        private static void PrintNodes(Dictionary<int, List<int>> connections, Dictionary<int, string> names,
            Dictionary<KeyValuePair<int, int>, string> labelsDictionary, int testId,
            StringBuilder dotFileContent)
        {
            // print Connections and Labels
            foreach (var (key, values) in connections)
            {
                foreach (var value in values)
                {
                    var newLine = $" node{key + testId * 1000} -> node{value + testId * 1000}";
                    var currentPair = new KeyValuePair<int, int>(key, value);
                    if (labelsDictionary.ContainsKey(currentPair))
                    {
                        var currentLabel = labelsDictionary[currentPair];
                        newLine = $" node{key + testId * 1000} -> node{value + testId * 1000}[label={currentLabel}]";
                    }

                    dotFileContent.AppendLine(newLine);

                    //TODO
                    //file.WriteLine(newLine);
                }
            }

            var numbersDictionary = new Dictionary<int, string>(_numbersDictionary);
            numbersDictionary = numbersDictionary.Skip(1).ToDictionary(pair => pair.Key, pair => pair.Value);

            var oldLinesDictionary = new Dictionary<int, string>(_linesDictionary);
            var dropIndex = 0;
            foreach (var (_, value) in oldLinesDictionary)
            {
                if (value.Equals("Block"))
                {
                    break;
                }

                dropIndex++;
            }

            oldLinesDictionary = oldLinesDictionary.Skip(dropIndex).ToDictionary(pair => pair.Key, pair => pair.Value);
            var oldLinesList = oldLinesDictionary.Keys.ToList();
            var numbersList = numbersDictionary.Keys.ToList();

            var exitList = new List<int>();
            var commentDictionary = new Dictionary<int, int>();

            for (var i = 0; i < oldLinesList.Count; i++)
            {
                if (oldLinesDictionary[oldLinesList[i]].Length > 7 &&
                    oldLinesDictionary[oldLinesList[i]][..7].Equals("return "))
                {
                    exitList.Add(numbersList[i]);
                }

                commentDictionary[oldLinesList[i]] = numbersList[i];
            }

            //print Names
            foreach (var (key, value) in names)
            {
                var currentValue = value;
                var currentShape = "box";
                if (value.Length > 3 && value[..3].Equals("if "))
                {
                    currentValue = value[3..];
                    currentShape = "diamond";
                }

                if (value.Length > 6 && value[..6].Equals("while;"))
                {
                    // TODO - можно убрать
                    //currentValue = value[6..];
                    currentShape = "diamond";
                }

                if (value.Length > 4 && value[..4].Equals("for;"))
                {
                    // TODO - можно убрать
                    //currentValue = value[4..];
                    currentShape = "diamond";
                }

                if (value.Length > 7 && value[..7].Equals("return "))
                {
                    currentShape = "ellipse";
                }

                if (value.Length == 4 && value.Equals("exit"))
                {
                    var returnPoints = exitList.Aggregate("", (current, number) => current + $"{number}, ");
                    returnPoints = returnPoints[..^2];

                    currentShape = "ellipse";
                    dotFileContent.AppendLine(
                        $" node{key + testId * 1000}[shape={currentShape}, label=\"{currentValue}\", fillcolor=red, style=filled, comment=\"{returnPoints}\"]");

                    // TODO
                    //file.WriteLine(
                    //    $" node{key + testId * 1000}[shape={currentShape}, label=\"{currentValue}\", fillcolor=red, style=filled, comment=\"{returnPoints}\"]");
                }
                else
                {
                    var currentComment = -1;
                    if (commentDictionary.ContainsKey(key))
                    {
                        currentComment = commentDictionary[key];
                    }

                    dotFileContent.AppendLine(
                        $" node{key + testId * 1000}[shape={currentShape}, label=\"{currentValue}\", comment=\"{currentComment}\"]");

                    // TODO
                    //file.WriteLine(
                    //    $" node{key + testId * 1000}[shape={currentShape}, label=\"{currentValue}\", comment=\"{currentComment}\"]");
                }
            }
        }

        private static Dictionary<int, int> CreateContinueDictionary(Dictionary<int, List<int>> nodeDictionary,
            Dictionary<int, string> linesDictionary, Dictionary<int, List<int>> blockDictionary,
            Dictionary<int, int> elseDictionary)
        {
            var result = new Dictionary<int, int>();
            foreach (var (key, value) in linesDictionary)
            {
                if (!(value.Length == 9 && value.Equals("continue;"))) continue;

                var continueKey = key;

                // Find parent cycle-block
                var parentBlockKey = blockDictionary.SingleOrDefault(x => x.Value.Contains(continueKey)).Key;
                var parentCycleBlock = nodeDictionary.SingleOrDefault(x => x.Value.Contains(parentBlockKey)).Key;

                if (elseDictionary.ContainsKey(parentCycleBlock))
                {
                    var elseParentBlock = nodeDictionary.SingleOrDefault(x => x.Value.Contains(parentCycleBlock)).Key;
                    parentCycleBlock = elseParentBlock;
                }

                while (!(linesDictionary[parentCycleBlock].Length > 4 &&
                         linesDictionary[parentCycleBlock][..4].Equals("for;")) &&
                       !(linesDictionary[parentCycleBlock].Length > 6 &&
                         linesDictionary[parentCycleBlock][..6].Equals("while;")))
                {
                    var preNodeParentBlockKey =
                        nodeDictionary.SingleOrDefault(x => x.Value.Contains(parentCycleBlock)).Key;
                    var newParentBlock = nodeDictionary.SingleOrDefault(x => x.Value.Contains(preNodeParentBlockKey))
                        .Key;

                    if (elseDictionary.ContainsKey(newParentBlock))
                    {
                        var elseParentBlock = nodeDictionary.SingleOrDefault(x => x.Value.Contains(newParentBlock)).Key;
                        newParentBlock = elseParentBlock;
                    }

                    parentCycleBlock = newParentBlock;
                }

                // add pair: continue-block -> his cycle-block
                result[continueKey] = parentCycleBlock;
            }

            return result;
        }

        private static Dictionary<int, int> CreateBreakDictionary(Dictionary<int, List<int>> nodeDictionary,
            Dictionary<int, string> linesDictionary, Dictionary<int, List<int>> blockDictionary,
            Dictionary<int, int> elseDictionary)
        {
            var result = new Dictionary<int, int>();
            foreach (var (key, value) in linesDictionary)
            {
                if (!(value.Length == 6 && value.Equals("break;"))) continue;

                var breakKey = key;

                // Find parent cycle-block
                var parentBlockKey = blockDictionary.SingleOrDefault(x => x.Value.Contains(breakKey)).Key;
                var parentCycleBlock = nodeDictionary.SingleOrDefault(x => x.Value.Contains(parentBlockKey)).Key;

                if (elseDictionary.ContainsKey(parentCycleBlock))
                {
                    var elseParentBlock = nodeDictionary.SingleOrDefault(x => x.Value.Contains(parentCycleBlock)).Key;
                    parentCycleBlock = elseParentBlock;
                }

                while (!(linesDictionary[parentCycleBlock].Length > 4 &&
                         linesDictionary[parentCycleBlock][..4].Equals("for;")) &&
                       !(linesDictionary[parentCycleBlock].Length > 6 &&
                         linesDictionary[parentCycleBlock][..6].Equals("while;")))
                {
                    var preNodeParentBlockKey =
                        nodeDictionary.SingleOrDefault(x => x.Value.Contains(parentCycleBlock)).Key;
                    var newParentBlock = nodeDictionary.SingleOrDefault(x => x.Value.Contains(preNodeParentBlockKey))
                        .Key;

                    if (elseDictionary.ContainsKey(newParentBlock))
                    {
                        var elseParentBlock = nodeDictionary.SingleOrDefault(x => x.Value.Contains(newParentBlock)).Key;
                        newParentBlock = elseParentBlock;
                    }

                    parentCycleBlock = newParentBlock;
                }

                // add pair: break-block -> his cycle-block
                result[breakKey] = parentCycleBlock;
            }

            return result;
        }

        private static Dictionary<int, int> CreateWhileDictionary(Dictionary<int, List<int>> nodeDictionary,
            Dictionary<int, string> linesDictionary)
        {
            var result = new Dictionary<int, int>();
            foreach (var (key, value) in linesDictionary)
            {
                if (!(value.Length > 6 && value[..6].Equals("while;"))) continue;

                var nodeConnections = nodeDictionary[key];
                foreach (var v in nodeConnections.Where(linesDictionary.ContainsKey))
                {
                    result[key] = v;
                    break;
                }
            }

            return result;
        }

        private static Dictionary<int, int> CreateForDictionary(Dictionary<int, List<int>> nodeDictionary,
            Dictionary<int, string> linesDictionary)
        {
            var result = new Dictionary<int, int>();
            foreach (var (key, value) in linesDictionary)
            {
                if (!(value.Length > 4 && value[..4].Equals("for;"))) continue;

                var nodeConnections = nodeDictionary[key];
                foreach (var v in nodeConnections.Where(linesDictionary.ContainsKey))
                {
                    result[key] = v;
                    break;
                }
            }

            return result;
        }

        private static Dictionary<int, List<int>> CreateBlockDictionary(Dictionary<int, List<int>> nodeDictionary,
            Dictionary<int, string> linesDictionary)
        {
            var result = new Dictionary<int, List<int>>();
            foreach (var (key, value) in linesDictionary)
            {
                if (value.Length == 5 & value.Equals("Block"))
                {
                    result[key] = nodeDictionary[key];
                }
            }

            return result;
        }

        private static Dictionary<int, int> CreateElseDictionary(Dictionary<int, List<int>> nodeDictionary,
            Dictionary<int, string> linesDictionary)
        {
            var result = new Dictionary<int, int>();
            foreach (var (key, value) in linesDictionary)
            {
                if (value.Length == 4 && value.Equals("else"))
                {
                    result[key] = nodeDictionary[key][0];
                }
            }

            return result;
        }
    }
}