import java.lang.Integer.min
import kotlin.test.assertEquals

fun main() {
  fun part1(input: List<String>): Int {
    lateinit var start: Node
    lateinit var target: Node
    val map =
        input.mapIndexed { y, line ->
          line.toCharArray().mapIndexed { x, height ->
            Node(x, y, height.value()).also {
              if (height == 'S') start = it
              if (height == 'E') target = it
            }
          }
        }
    start.distanceFromStart = 0

    return dijkstra(start, target, map)
  }

  fun part2(input: List<String>): Int {
    val possibleStarts = mutableListOf<Node>()
    lateinit var target: Node
    val map =
        input.mapIndexed { y, line ->
          line.toCharArray().mapIndexed { x, height ->
            Node(x, y, height.value()).also {
              if (height in listOf('S', 'a')) possibleStarts.add(it)
              if (height == 'E') target = it
            }
          }
        }

    return possibleStarts.minOf { start ->
      val copyOfMap = map.copy()
      val startNode = copyOfMap[start.y][start.x].apply { distanceFromStart = 0 }
      dijkstra(startNode, copyOfMap[target.y][target.x], copyOfMap)
    }
  }

  // test if implementation meets criteria from the description, like:
  val testInput = readInput("Day12_test")
  assertEquals(31, part1(testInput))
  assertEquals(29, part2(testInput))

  val input = readInput("Day12")
  println(part1(input))
  println(part2(input))
}

fun List<List<Node>>.copy(): List<List<Node>> {
  return this.map { it.map(Node::copy) }
}

fun Char.value() =
    when (this) {
      'S' -> 'a'.code
      'E' -> 'z'.code
      else -> this.code
    }

fun dijkstra(
    startNode: Node,
    target: Node,
    map: List<List<Node>>,
): Int {
  val unvisitedNodes = mutableSetOf<Node>()
  var currentNode = startNode
  while (true) {
    for (node in currentNode.getUnvisitedNeighbors(map)) {
      unvisitedNodes.add(node)
      val tentativeDistance = currentNode.distanceFromStart + 1
      node.distanceFromStart = min(tentativeDistance, node.distanceFromStart)
    }
    currentNode.isVisited = true
    unvisitedNodes.remove(currentNode)
    if (currentNode == target) {
      break
    }
    val nodeWithLowestDistance = unvisitedNodes.minOrNull()
    if (nodeWithLowestDistance != null) {
      currentNode = nodeWithLowestDistance
    } else {
      break
    }
  }

  return target.distanceFromStart
}

data class Node(
    val x: Int,
    val y: Int,
    val height: Int,
    var isVisited: Boolean = false,
    var distanceFromStart: Int = Int.MAX_VALUE,
) : Comparable<Node> {

  fun getUnvisitedNeighbors(map: List<List<Node>>): List<Node> {
    val neighbors = mutableListOf<Node>()
    val xMax = map[0].lastIndex
    val yMax = map.lastIndex
    val maxSlope = 1

    if (x > 0) {
      val left = map[y][x - 1]
      if (left.height - height <= maxSlope) neighbors.add(left)
    }

    if (x < xMax) {
      val right = map[y][x + 1]
      if (right.height - height <= maxSlope) neighbors.add(right)
    }

    if (y < yMax) {
      val lower = map[y + 1][x]
      if (lower.height - height <= maxSlope) neighbors.add(lower)
    }

    if (y > 0) {
      val upper = map[y - 1][x]
      if (upper.height - height <= maxSlope) neighbors.add(upper)
    }

    return neighbors.filterNot { it.isVisited }
  }

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (javaClass != other?.javaClass) return false

    other as Node

    if (x != other.x) return false
    if (y != other.y) return false

    return true
  }

  override fun hashCode(): Int {
    var result = x
    result = 31 * result + y
    return result
  }

  override fun compareTo(other: Node) = distanceFromStart.compareTo(other.distanceFromStart)
}
