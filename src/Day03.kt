import kotlin.streams.toList
import kotlin.test.assertEquals

fun main() {
  fun part1(input: List<String>): Int {
    return input
        .map { rucksack ->
          val firstCompartment =
              rucksack.subSequence(0, rucksack.length / 2).chars().toList().toSet()
          val secondCompartment =
              rucksack.subSequence(rucksack.length / 2, rucksack.length).chars().toList().toSet()
          val intersection = firstCompartment.intersect(secondCompartment)
          intersection.first().also { println(it.toChar()) }
        }
        .map { itemType ->
          if (itemType in 'a'.code..'z'.code) {
            itemType - 'a'.code + 1
          } else {
            itemType - 'A'.code + 27
          }
        }
        .sum()
  }

  fun part2(input: List<String>): Int {
    return input
        .windowed(3, 3)
        .map { group ->
          group
              .map { it.chars().toList().toSet() }
              .reduce { acc, ints -> acc.intersect(ints) }
              .first()
        }
        .map { itemType ->
          if (itemType in 'a'.code..'z'.code) {
            itemType - 'a'.code + 1
          } else {
            itemType - 'A'.code + 27
          }
        }
        .sum()
  }

  // test if implementation meets criteria from the description, like:
  val testInput = readInput("Day03_test")
  assertEquals(157, part1(testInput))
  assertEquals(70, part2(testInput))

  val input = readInput("Day03")
  println(part1(input))
  println(part2(input))
}
