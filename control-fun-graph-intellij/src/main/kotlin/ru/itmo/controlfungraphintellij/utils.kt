package ru.itmo.controlfungraphintellij

fun buildTransitionTree(dotFile: String, isKotlin: Boolean): Map<String, List<List<String>>> {
    val transitions = buildTransition(dotFile, isKotlin)
    val allPaths = findAllPaths(transitions)

    val resultTransitions = mutableMapOf<String, MutableList<List<String>>>()

    for (path in allPaths) {
        val splitPath = path.split(", ")
        val lastNode = splitPath.last()

        val pathWithoutLast = splitPath.takeWhile { it != lastNode }.joinToString(", ")
        val splitPathWithoutLast = pathWithoutLast.split(", ")

        val totalList = mutableListOf<String>()

        for (value in splitPathWithoutLast) {
            totalList.add(value)
        }

        resultTransitions.getOrPut(lastNode) { mutableListOf() }.add(totalList)

    }
    return resultTransitions
}

private fun buildTransition(dotFile: String, isKotlin: Boolean): Map<String, List<String>> {
    val transitions = mutableMapOf<String, MutableList<String>>()
    val lines = dotFile.split("\n")

    for (line in lines) {
        val trimmedLine = line.trim()
        if (trimmedLine.isNotEmpty() && trimmedLine.contains("->")) {
            var parentNode: String
            var childNode: String;

            if (!isKotlin) {
                val transitionNodes = trimmedLine.split("->").map { it.trim() }
                parentNode = transitionNodes[0].split("[")[0].split(":")[0].trim()
                childNode = transitionNodes[1].split(" ")[0].split(":")[0].split("[")[0].trim()
            } else {
                val transitionNodes = trimmedLine.split("->").map { it.trim() }
                parentNode = transitionNodes[0].trim().split("\"")[1]
                childNode = transitionNodes[1].trim().split(" [")[0].split("\"")[1]
            }

            transitions.getOrPut(parentNode, { mutableListOf() }).add(childNode)
        }
    }
    return transitions
}

private fun findAllPaths(transitions: Map<String, List<String>>): List<String> {
    val paths = mutableListOf<String>()

    for (startNode in transitions.keys) {
        findAllPathsHelper(startNode, startNode, transitions, mutableListOf(startNode), paths)
    }

    return paths.toList().filter { it -> it.split(", ")[0] == transitions.keys.first() }
}

private fun findAllPathsHelper(
    startNode: String,
    currentNode: String,
    transitions: Map<String, List<String>>,
    currentPath: MutableList<String>,
    paths: MutableList<String>
) {
    for (neighbor in transitions[currentNode] ?: emptyList()) {
        if (neighbor !in currentPath) {
            currentPath.add(neighbor)
            paths.add(currentPath.joinToString(", "))
            findAllPathsHelper(startNode, neighbor, transitions, currentPath, paths)
            currentPath.remove(neighbor)
        }
    }
}
