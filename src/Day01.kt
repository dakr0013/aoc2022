import kotlin.test.assertEquals

fun main() {
  fun part1(input: List<String>): Int {
    return input
        .joinToString("\n")
        .split("\n\n")
        .map { inventory -> inventory.split("\n").sumOf { it.toInt() } }
        .max()
  }

  fun part2(input: List<String>): Int {
    return input
        .joinToString("\n")
        .split("\n\n")
        .map { inventory -> inventory.split("\n").sumOf { it.toInt() } }
        .sorted()
        .takeLast(3)
        .sum()
  }

  // test if implementation meets criteria from the description, like:
  val testInput = readInput("Day01_test")
  assertEquals(24000, part1(testInput))
  assertEquals(45000, part2(testInput))

  val input = readInput("Day01")
  println(part1(input))
  println(part2(input))
}
