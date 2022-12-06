import kotlin.test.assertEquals

fun main() {
  fun part1(input: List<String>): Int {
    val datastream = input.first()
    return datastream
        .withIndex()
        .windowed(4)
        .map { indexedCharSequence ->
          if (indexedCharSequence.map { it.value }.toSet().size == 4) {
            indexedCharSequence.last().index + 1
          } else {
            0
          }
        }
        .first { it > 0 }
  }

  fun part2(input: List<String>): Int {
    val datastream = input.first()
    return datastream
        .withIndex()
        .windowed(14)
        .map { indexedCharSequence ->
          if (indexedCharSequence.map { it.value }.toSet().size == 14) {
            indexedCharSequence.last().index + 1
          } else {
            0
          }
        }
        .first { it > 0 }
  }

  // test if implementation meets criteria from the description, like:
  val testInput = readInput("Day06_test")
  assertEquals(7, part1(testInput))
  assertEquals(19, part2(testInput))

  val input = readInput("Day06")
  println(part1(input))
  println(part2(input))
}
