import kotlin.test.assertEquals

fun main() {
  fun part1(input: List<String>): Int {
    return input
        .map { it.split(" ")[0] to it.split(" ")[1] }
        .map {
          val opponentShape =
              when (it.first) {
                "A" -> 0
                "B" -> 1
                "C" -> 2
                else -> error("should not happen")
              }
          val myShape =
              when (it.second) {
                "X" -> 0
                "Y" -> 1
                "Z" -> 2
                else -> error("should not happen")
              }
          val shapeScore = myShape + 1
          val winningShape = (opponentShape + 1).mod(3)
          val outcomeScore =
              if (opponentShape == myShape) {
                3
              } else if (winningShape == myShape) {
                6
              } else {
                0
              }
          shapeScore + outcomeScore
        }
        .sum()
  }

  fun part2(input: List<String>): Int {
    return input
        .map { it.split(" ")[0] to it.split(" ")[1] }
        .map {
          val opponentShape =
              when (it.first) {
                "A" -> 0
                "B" -> 1
                "C" -> 2
                else -> error("should not happen")
              }
          val diffToTargetShape =
              when (it.second) {
                "X" -> -1
                "Y" -> 0
                "Z" -> +1
                else -> error("should not happen")
              }
          val targetShape = (opponentShape + diffToTargetShape).mod(3)
          val shapeScore = targetShape + 1
          val outcomeScore =
              when (it.second) {
                "X" -> 0
                "Y" -> 3
                "Z" -> 6
                else -> error("should not happen")
              }
          shapeScore + outcomeScore
        }
        .sum()
  }

  // test if implementation meets criteria from the description, like:
  val testInput = readInput("Day02_test")
  assertEquals(15, part1(testInput))
  assertEquals(12, part2(testInput))

  val input = readInput("Day02")
  println(part1(input))
  println(part2(input))
}
