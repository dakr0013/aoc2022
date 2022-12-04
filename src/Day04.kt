import kotlin.test.assertEquals

fun main() {
  fun part1(input: List<String>): Int {
    return input
        .map { pair ->
          val ranges =
              pair.split(",").map { range -> range.split("-").let { it[0].toInt()..it[1].toInt() } }
          ranges[0] to ranges[1]
        }
        .map {
          if (it.first.contains(it.second) || it.second.contains(it.first)) {
            1
          } else {
            0
          }
        }
        .sum()
  }

  fun part2(input: List<String>): Int {
    return input
        .map { pair ->
          val ranges =
              pair.split(",").map { range -> range.split("-").let { it[0].toInt()..it[1].toInt() } }
          ranges[0] to ranges[1]
        }
        .map {
          if (it.first.overlaps(it.second)) {
            1
          } else {
            0
          }
        }
        .sum()
  }

  // test if implementation meets criteria from the description, like:
  val testInput = readInput("Day04_test")
  assertEquals(2, part1(testInput))
  assertEquals(4, part2(testInput))

  val input = readInput("Day04")
  println(part1(input))
  println(part2(input))
}

private fun IntRange.contains(other: IntRange) =
    other.first >= this.first && other.last <= this.last

private fun IntRange.overlaps(other: IntRange) =
    contains(other.first) || contains(other.last) || other.contains(first) || other.contains(last)
