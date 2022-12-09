import kotlin.test.assertEquals

fun main() {
  fun part1(input: List<String>): Int {
    val seriesOfMotions = parseSeriesOfMotions(input)
    return simulateSeriesOfMotions(seriesOfMotions, Rope(2))
  }

  fun part2(input: List<String>): Int {
    val seriesOfMotions = parseSeriesOfMotions(input)
    return simulateSeriesOfMotions(seriesOfMotions, Rope(10))
  }

  // test if implementation meets criteria from the description, like:
  assertEquals(13, part1(readInput("Day09_test")))
  assertEquals(36, part2(readInput("Day09_test2")))

  val input = readInput("Day09")
  println(part1(input))
  println(part2(input))
}

private fun simulateSeriesOfMotions(motions: List<Motion>, rope: Rope): Int {
  val visitedTailPositions = mutableSetOf(rope.tail())

  for (motion in motions) {
    repeat(motion.steps) {
      rope.moveHead(motion.direction)
      visitedTailPositions.add(rope.tail())
    }
  }

  return visitedTailPositions.size
}

private fun parseSeriesOfMotions(input: List<String>): List<Motion> {
  return input.map {
    val (direction, stepsString) = it.split(" ")
    val steps = stepsString.toInt()
    when (direction) {
      "R" -> Motion(Vector(1, 0), steps)
      "L" -> Motion(Vector(-1, 0), steps)
      "U" -> Motion(Vector(0, 1), steps)
      "D" -> Motion(Vector(0, -1), steps)
      else -> error("unknown direction, should not happen")
    }
  }
}

private data class Rope(val numberOfKnots: Int) {
  private val knotPositions = Array(numberOfKnots) { Vector(0, 0) }

  fun moveHead(direction: Vector) {
    knotPositions[0] += direction
    for (i in 0 until numberOfKnots - 1) {
      if (knotPositions[i + 1].isNotAdjacent(knotPositions[i])) {
        knotPositions[i + 1] = knotPositions[i + 1].moveTowards(knotPositions[i])
      }
    }
  }

  fun tail() = knotPositions.last()
}

private data class Motion(val direction: Vector, val steps: Int)

private data class Vector(val x: Int, val y: Int) {
  operator fun plus(other: Vector) = Vector(this.x + other.x, this.y + other.y)
  operator fun minus(other: Vector) = Vector(this.x - other.x, this.y - other.y)

  fun isNotAdjacent(other: Vector) =
      !(this.x in (other.x - 1)..(other.x + 1) && this.y in (other.y - 1)..(other.y + 1))

  fun moveTowards(other: Vector): Vector {
    val direction = (other - this).coerceIn(1)
    return this + direction
  }

  fun coerceIn(maxAbsoluteValue: Int) =
      Vector(
          this.x.coerceIn(-maxAbsoluteValue, maxAbsoluteValue),
          this.y.coerceIn(-maxAbsoluteValue, maxAbsoluteValue),
      )
}
