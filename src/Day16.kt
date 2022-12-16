import kotlin.test.assertEquals

fun main() {
  fun part1(input: List<String>): Int {
    val releaseRates = parseReleaseRates(input)
    val directNeighbors = parseDirectNeighbors(input)
    val travelTimes = getTravelTimesBetweenValves(releaseRates.keys, directNeighbors)

    val openableValves = releaseRates.filterValues { it > 0 }.keys.toMutableSet()

    fun maxReleasablePressure(
        startValve: ValveName,
        timeLeft: Int,
        releasedPressure: Int,
        openableValves: Set<ValveName>
    ): Int {
      if (openableValves.isEmpty()) return releasedPressure
      return openableValves.maxOf { nextValve ->
        val releaseRate = releaseRates[nextValve]!!
        val travelTime = travelTimes[startValve]!![nextValve]!!

        val newTimeLeft = timeLeft - travelTime - 1
        if (newTimeLeft > 0) {
          maxReleasablePressure(
              nextValve,
              newTimeLeft,
              releasedPressure + newTimeLeft * releaseRate,
              openableValves - nextValve)
        } else {
          releasedPressure
        }
      }
    }

    return maxReleasablePressure("AA", 30, 0, openableValves)
  }

  fun part2(input: List<String>): Int {
    val releaseRates = parseReleaseRates(input)
    val directNeighbors = parseDirectNeighbors(input)
    val travelTimes = getTravelTimesBetweenValves(releaseRates.keys, directNeighbors)

    val openableValves = releaseRates.filterValues { it > 0 }.keys.toMutableSet()

    fun maxReleasablePressure(
        startValve: ValveName,
        startValve2: ValveName,
        timeLeft: Int,
        timeLeft2: Int,
        releasedPressure: Int,
        openableValves: Set<ValveName>
    ): Int {
      if (openableValves.isEmpty()) return releasedPressure
      if (openableValves.size == 1) {
        val nextValve = openableValves.first()
        val releaseRate = releaseRates[nextValve]!!
        val travelTime = travelTimes[startValve]!![nextValve]!!
        val travelTime2 = travelTimes[startValve2]!![nextValve]!!

        val newTimeLeft = timeLeft - travelTime - 1
        val newTimeLeft2 = timeLeft2 - travelTime2 - 1
        return buildList {
              add(releasedPressure)
              if (newTimeLeft > 0) add(releasedPressure + newTimeLeft * releaseRate)
              if (newTimeLeft2 > 0) add(releasedPressure + newTimeLeft2 * releaseRate)
            }
            .max()
      }

      return openableValves.maxOf { nextValve ->
        (openableValves - nextValve).maxOf { nextValve2 ->
          val releaseRate = releaseRates[nextValve]!!
          val releaseRate2 = releaseRates[nextValve2]!!
          val travelTime = travelTimes[startValve]!![nextValve]!!
          val travelTime2 = travelTimes[startValve2]!![nextValve2]!!

          val newTimeLeft = (timeLeft - travelTime - 1).coerceAtLeast(0)
          val newTimeLeft2 = (timeLeft2 - travelTime2 - 1).coerceAtLeast(0)

          if (newTimeLeft > 0 && newTimeLeft2 > 0) {
            maxReleasablePressure(
                nextValve,
                nextValve2,
                newTimeLeft,
                newTimeLeft2,
                releasedPressure + newTimeLeft * releaseRate + newTimeLeft2 * releaseRate2,
                (openableValves - nextValve) - nextValve2)
          } else {
            releasedPressure + newTimeLeft * releaseRate + newTimeLeft2 * releaseRate2
          }
        }
      }
    }

    return maxReleasablePressure("AA", "AA", 26, 26, 0, openableValves)
  }

  // test if implementation meets criteria from the description, like:
  val testInput = readInput("Day16_test")
  assertEquals(1651, part1(testInput))
  assertEquals(1707, part2(testInput))

  val input = readInput("Day16")
  println(part1(input))
  println(part2(input))
}

private val linePattern =
    Regex("Valve ([A-Z]{2}) has flow rate=(\\d+); tunnels? leads? to valves? ([A-Z,\\s]+)")

private fun parseReleaseRates(input: List<String>): Map<ValveName, Int> {
  return input.associate { line ->
    val (name, releaseRate, _) = linePattern.matchEntire(line)!!.groupValues.drop(1)
    name to releaseRate.toInt()
  }
}

private fun parseDirectNeighbors(input: List<String>): Map<ValveName, List<ValveName>> {
  return input.associate { line ->
    val (name, _, neighbors) = linePattern.matchEntire(line)!!.groupValues.drop(1)
    name to neighbors.split(", ")
  }
}

private fun getTravelTimesBetweenValves(
    valves: Set<ValveName>,
    neighbors: Map<ValveName, List<ValveName>>
): Map<ValveName, Map<ValveName, Int>> {
  class Node(val name: String, var distanceFromStart: Int = Int.MAX_VALUE) {
    override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (other !is Node) return false

      if (name != other.name) return false

      return true
    }
    override fun hashCode(): Int {
      return name.hashCode()
    }
  }

  fun dijkstra(
      start: ValveName,
      target: ValveName,
  ): Int {
    val visitedNodes = mutableSetOf<Node>()
    val unvisitedNodes = mutableSetOf<Node>()
    var currentNode = Node(start, 0)
    while (true) {
      for (nodeName in neighbors[currentNode.name]!!) {
        val node = Node(nodeName)
        if (node !in visitedNodes) {
          unvisitedNodes.add(node)
          val tentativeDistance = currentNode.distanceFromStart + 1
          node.distanceFromStart = Integer.min(tentativeDistance, node.distanceFromStart)
        }
      }
      visitedNodes.add(currentNode)
      unvisitedNodes.remove(currentNode)
      if (currentNode.name == target) {
        break
      }
      val nodeWithLowestDistance = unvisitedNodes.minByOrNull { it.distanceFromStart }
      if (nodeWithLowestDistance != null) {
        currentNode = nodeWithLowestDistance
      } else {
        break
      }
    }
    return visitedNodes.first { it.name == target }.distanceFromStart
  }

  return valves.associateWith { start ->
    (valves - start).associateWith { target -> dijkstra(start, target) }
  }
}

typealias ValveName = String
