import Material.*
import java.awt.Point
import kotlin.test.assertEquals

fun main() {
  fun part1(input: List<String>): Int {
    val cave = Cave.parse(input) // .also { println(it) }
    val sandSource = Point(500, 0)
    var steps = 0
    while (cave.pourSand(sandSource)) {
      steps++
    }
    return steps
  }

  fun part2(input: List<String>): Int {
    val cave = Cave.parse(input, true) // .also { println(it) }
    val sandSource = Point(500, 0)
    var steps = 0
    while (cave.pourSand(sandSource)) {
      steps++
    }
    return steps
  }

  // test if implementation meets criteria from the description, like:
  val testInput = readInput("Day14_test")
  assertEquals(24, part1(testInput))
  assertEquals(93, part2(testInput))

  val input = readInput("Day14")
  println(part1(input))
  println(part2(input))
}

private enum class Material(val isSolid: Boolean) {
  SAND(true),
  ROCK(true),
  AIR(false);

  override fun toString() =
      when (this) {
        SAND -> "o"
        ROCK -> "#"
        AIR -> "."
      }
}

private data class Structure(val material: Material, val path: List<Point>)

private class Line(a: Point, b: Point) : Iterable<Point> {
  val start: Point = if (a.x < b.x || a.y < b.y) a else b
  val end: Point = if (a.x < b.x || a.y < b.y) b else a

  init {
    require(start.x == end.x || start.y == end.y) {
      "Points must represent a horizontal or vertical line"
    }
  }

  override fun iterator(): Iterator<Point> = PointByPointIterator()

  private inner class PointByPointIterator : Iterator<Point> {
    val isHorizontalLine = start.y == end.y
    var currentOffset = 0
    override fun hasNext(): Boolean {
      return if (isHorizontalLine) {
        start.x + currentOffset <= end.x
      } else {
        start.y + currentOffset <= end.y
      }
    }

    override fun next(): Point {
      if (!hasNext())
          throw NoSuchElementException("No further points on line: end of line already reached.")
      return if (isHorizontalLine) {
        Point(start.x + currentOffset++, start.y)
      } else {
        Point(start.x, start.y + currentOffset++)
      }
    }
  }
}

private class Cave(val map: Array<Array<Material>>) {

  /** @return true if sand comes to rest and source is not blocked */
  fun pourSand(source: Point): Boolean {
    val isSourceBlocked = map[source.x][source.y].isSolid
    if (isSourceBlocked) return false

    val restPoint = simulateFallingMaterial(source)
    if (restPoint != null) {
      map[restPoint.x][restPoint.y] = SAND
    }
    return restPoint != null
  }

  /** @return point where falling material comes to rest or null if it falls into void */
  private fun simulateFallingMaterial(source: Point): Point? {
    for (y in source.y..map[source.x].lastIndex) {
      if (map[source.x][y].isSolid) {
        return when {
          !map[source.x - 1][y].isSolid -> simulateFallingMaterial(Point(source.x - 1, y))
          !map[source.x + 1][y].isSolid -> simulateFallingMaterial(Point(source.x + 1, y))
          else -> Point(source.x, y - 1)
        }
      }
    }
    return null
  }

  private fun add(structures: List<Structure>) {
    for (structure in structures) {
      for (i in 0 until structure.path.lastIndex) {
        val line = Line(structure.path[i], structure.path[i + 1])
        for (point in line) {
          map[point.x][point.y] = structure.material
        }
      }
    }
  }

  override fun toString() = buildString {
    for (y in map[0].indices) {
      for (x in 490..510) {
        append(map[x][y])
      }
      appendLine()
    }
  }

  companion object {
    fun parse(input: List<String>, withFloor: Boolean = false): Cave {
      val structures =
          input
              .map { line ->
                line.split(" -> ").map { xyPair ->
                  Point(
                      xyPair.split(",").first().toInt(),
                      xyPair.split(",").last().toInt(),
                  )
                }
              }
              .map { points -> Structure(ROCK, points) }

      val yOfLowestStructure = structures.flatMap { it.path }.maxOf { it.y }
      val xOfMostRightStructure = structures.flatMap { it.path }.maxOf { it.x }
      val floor =
          Structure(
              ROCK,
              listOf(
                  Point(0, yOfLowestStructure + 2),
                  Point(xOfMostRightStructure * 2, yOfLowestStructure + 2)),
          )
      val map = Array(xOfMostRightStructure * 2 + 1) { Array(yOfLowestStructure + 2 + 1) { AIR } }

      return Cave(map).apply {
        add(structures)
        if (withFloor) add(listOf(floor))
      }
    }
  }
}
