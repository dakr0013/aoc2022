import java.awt.Point
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.min
import kotlin.test.assertEquals

fun main() {

  fun part1(input: List<String>, testRow: Int = 2_000_000): Int {
    val sensors = parseSensors(input)

    val beaconCountInTestRow = sensors.map { it.closestBeacon }.toSet().count { it.y == testRow }

    return merge(sensors.mapNotNull { it.getCoverageForRow(testRow) }).sumOf { it.size() } -
        beaconCountInTestRow
  }

  fun part2(input: List<String>, maxSearchSpace: Int = 4_000_000): Long {
    val sensors = parseSensors(input)
    for (y in 0..maxSearchSpace) {
      val rowCoverages =
          merge(sensors.mapNotNull { it.getCoverageForRow(y) }).filter {
            it.first <= maxSearchSpace
          }
      when (rowCoverages.size) {
        1 -> {
          val coverageRange = rowCoverages.first()
          val isLowerBoundNotCovered = !coverageRange.contains(0)
          if (isLowerBoundNotCovered) {
            return tuningFrequency(0, y)
          }
          val isUpperBoundNotCovered = !coverageRange.contains(maxSearchSpace)
          if (isUpperBoundNotCovered) {
            return tuningFrequency(maxSearchSpace, y)
          }
        }
        2 -> {
          val firstCoverageRange = rowCoverages.minBy { it.first }
          val xOfDistressBeacon = firstCoverageRange.last + 1
          return tuningFrequency(xOfDistressBeacon, y)
        }
        else -> error("contains multiple possible positions for distress beacon, should not happen")
      }
    }
    error("no distress beacon found, should not happen")
  }

  // test if implementation meets criteria from the description, like:
  val testInput = readInput("Day15_test")
  assertEquals(26, part1(testInput, 10))
  assertEquals(56000011, part2(testInput, 20))

  val input = readInput("Day15")
  println(part1(input))
  println(part2(input))
}

private fun parseSensors(input: List<String>): List<Sensor> {
  val inputPattern =
      Regex("Sensor at x=(-?\\d+), y=(-?\\d+): closest beacon is at x=(-?\\d+), y=(-?\\d+)")
  return input
      .map { line -> inputPattern.matchEntire(line)!!.groupValues.drop(1).map { it.toInt() } }
      .map { Sensor(Point(it[0], it[1]), Point(it[2], it[3])) }
}

private data class Sensor(val position: Point, val closestBeacon: Point) {
  val distanceToClosestBeacon = manhattenDistance(position, closestBeacon)

  fun getCoverageForRow(y: Int): XRange? {
    val xDiffMax = distanceToClosestBeacon - (position.y - y).absoluteValue
    return if (xDiffMax >= 0) {
      (position.x - xDiffMax)..(position.x + xDiffMax)
    } else {
      null
    }
  }
}

private fun merge(ranges: List<IntRange>): List<IntRange> {
  val rangesToMerge = ranges.toMutableList()
  val result = mutableListOf<IntRange>()
  var currentRange = rangesToMerge.removeFirst()

  while (rangesToMerge.isNotEmpty()) {
    val nextRange = rangesToMerge.firstOrNull { it.canMerge(currentRange) }
    if (nextRange == null) {
      result.add(currentRange)
      currentRange = rangesToMerge.removeFirst()
    } else {
      rangesToMerge.remove(nextRange)
      currentRange = currentRange.merge(nextRange)
    }
  }

  return result.apply { add(currentRange) }
}

private fun IntRange.canMerge(other: IntRange): Boolean =
    contains(other.first) ||
        contains(other.last) ||
        other.contains(first) ||
        other.contains(last) ||
        last + 1 == other.first ||
        other.last + 1 == first

private fun IntRange.merge(other: IntRange) = min(first, other.first)..max(last, other.last)

private fun IntRange.size() = (last - first) + 1

private fun tuningFrequency(x: Int, y: Int): Long = x * 4_000_000L + y

private fun manhattenDistance(p1: Point, p2: Point): Int =
    (p1.x - p2.x).absoluteValue + (p1.y - p2.y).absoluteValue

typealias XRange = IntRange
