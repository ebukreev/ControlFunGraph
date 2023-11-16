package ru.itmo.controlfungraphintellij

fun buildTransitionTree(dotFile: String, isKotlin: Boolean): Map<String, List<List<String>>> {
    val transitions = buildTransition(dotFile, isKotlin)
    val pathsWithoutCycles = findAllPaths(transitions)

    val pathsWithCyclesAndDuplicates = findAllCyclicPaths(transitions)
    val pathsWithCycles = findPathsWithCycles(pathsWithCyclesAndDuplicates)

    val allPaths = findAllPathsWithCycles(pathsWithCycles, pathsWithoutCycles)

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
    for ((key, value) in resultTransitions) {
        resultTransitions[key] = (value.map { it.sorted().distinct() }.toSet()).toMutableList()
    }

    return resultTransitions
}

private fun findAllPathsWithCycles(pathsWithCycles: List<String>, pathsWithoutCycles: List<String>): List<String> {
    val result = pathsWithoutCycles.toMutableList()

    // Проходимся по всем существующим путям
    for (i in pathsWithoutCycles.indices) {
        // Получаем последний элемент
        val lastElement = pathsWithoutCycles[i].split(", ").last()

        val pathElements = pathsWithoutCycles[i].split(", ")

        // Если размер найденного пути с last = lastElement >= 3
        if (pathElements.size >= 3) {

            // Получим центральную часть этого пути
            val middleElementsInPath = pathElements.subList(1, pathElements.size - 1).joinToString(", ")

            // Найдем все циклы, у которых first содержится в центральной части, но также они сами не содержат lastElement
            val cyclesToCheck =
                pathsWithCycles.filter {
                    middleElementsInPath.contains(
                        it.split(", ").first()
                    ) && !it.contains(lastElement)
                }

            // Если мы нашли такие циклы ->
            if (cyclesToCheck.isNotEmpty()) {
                // Если такой цикл один -> просто добавим новый путь с учетом цикла в result
                if (cyclesToCheck.size == 1) {
                    result.add(
                        pathsWithoutCycles[i].replaceFirst(
                            cyclesToCheck.first().split(", ").first(),
                            cyclesToCheck.first()
                        )
                    )
                } else {
                    // Сначала просто добавим в result измененный путь с учетом каждого из циклов
                    for (cycleToCheck in cyclesToCheck) {
                        result.add(
                            pathsWithoutCycles[i].replaceFirst(
                                cycleToCheck.split(", ").first(),
                                cycleToCheck
                            )
                        )
                    }

                    // Найдем все комбинации таких циклов
                    val allIndexesCombinations = findAllCombinations(cyclesToCheck.size)

                    // Добавим в result измененный путь с применением каждой из комбинации
                    for (indexesCombinations in allIndexesCombinations) {
                        var replacedPath = pathsWithoutCycles[i]
                        for (index in indexesCombinations) {
                            replacedPath = replacedPath.replaceFirst(
                                cyclesToCheck[index - 1].split(", ").first(),
                                cyclesToCheck[index - 1]
                            )
                        }
                        result.add(replacedPath)
                    }
                }
            }
        }
    }
    return result
}

private fun findAllCombinations(size: Int): List<List<Int>> {
    val result = mutableListOf<List<Int>>()

    for (i in 1 until (1 shl size)) {
        val combination = mutableListOf<Int>()
        for (j in 0 until size) {
            if ((i and (1 shl j)) > 0) {
                combination.add(j + 1)
            }
        }
        result.add(combination)
    }

    return result.filter { it.size > 1 }
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

private fun findPathsWithCycles(pathsWithCyclesAndDuplicates: List<String>): List<String> {
    val toDelete = mutableListOf<String>()
    val toAdd = mutableListOf<String>()

    // TODO - не учитываются случаи с присутсвием двух и более зацикликований внутри другого -> добавить комбинации
    for ((index, str) in pathsWithCyclesAndDuplicates.withIndex()) {
        val names = str.split(", ")
        val first = names.first()
        for (i in pathsWithCyclesAndDuplicates.indices) {
            if (i != index && pathsWithCyclesAndDuplicates[i].contains(first)
                && pathsWithCyclesAndDuplicates[i].split(", ")[0] != first
                && pathsWithCyclesAndDuplicates[i].split(", ").last() != first
            ) {
                toDelete.add(pathsWithCyclesAndDuplicates[index])
                toAdd.add(pathsWithCyclesAndDuplicates[i].replaceFirst(first, str))
            }
        }
    }

    val pathsWithCycles = (pathsWithCyclesAndDuplicates + toAdd).filter { !toDelete.contains(it) }

    return pathsWithCycles.toList()
}

private fun findAllCyclicPaths(transitions: Map<String, List<String>>): List<String> {
    val result = mutableListOf<List<String>>()

    fun findCyclicPaths(node: String, currentPath: List<String>, visited: Set<String>) {
        for (neighbor in transitions[node] ?: emptyList()) {
            if (neighbor in currentPath && neighbor != currentPath.first()) {
                val cyclicPath = currentPath.subList(currentPath.indexOf(neighbor), currentPath.size) + neighbor
                result.add(cyclicPath)
            } else if (neighbor !in visited) {
                findCyclicPaths(neighbor, currentPath + neighbor, visited + node)
            }
        }
    }

    transitions.keys.forEach { startNode ->
        findCyclicPaths(startNode, listOf(startNode), emptySet())
    }

    val totalResult = ((result.map { it.toSet() }.toSet().map { it.toList() }).map { subList ->
        val firstElement = subList.first()
        subList + firstElement
    }).map { it.joinToString(", ") }

    return totalResult
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
