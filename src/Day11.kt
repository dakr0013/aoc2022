import kotlin.test.assertEquals

fun main() {
  fun part1(input: List<String>): Long {
    val monkeys = parseMonkeys(input)

    repeat(20) {
      for (monkey in monkeys) {
        monkey.takeTurn(monkeys) { it / 3 }
      }
    }

    return monkeys.map { it.inspectedItems }.sorted().takeLast(2).reduce { acc, i -> acc * i }
  }

  fun part2(input: List<String>): Long {
    val monkeys = parseMonkeys(input)
    val divisor = calculateDivisor(input)

    repeat(10_000) {
      for (monkey in monkeys) {
        // use modulo to keep relief levels manageable. chosen divisor to be a number which is
        // divisible by all test divisors, to keep outcome of test same as if modulo was not
        // applied. this way item gets thrown to same monkey as if modulo where not applied and
        // therefore the inspected items per monkey is the same as if modulo was not used
        monkey.takeTurn(monkeys) { it % divisor }
      }
    }

    return monkeys.map { it.inspectedItems }.sorted().takeLast(2).reduce { acc, i -> acc * i }
  }

  // test if implementation meets criteria from the description, like:
  val testInput = readInput("Day11_test")
  assertEquals(10605L, part1(testInput))
  assertEquals(2713310158L, part2(testInput))

  val input = readInput("Day11")
  println(part1(input))
  println(part2(input))
}

private fun parseMonkeys(input: List<String>): List<Monkey> {
  return input.chunked(7).map {
    val startingItemsInput = it[1]
    val operationInput = it[2]
    val testInput = it.slice(3..5)
    Monkey(
        parseStartingItems(startingItemsInput),
        parseOperation(operationInput),
        parseWorryLevelTester(testInput),
    )
  }
}

private fun calculateDivisor(input: List<String>): Long =
    input.chunked(7).map { it[3].trim().split(" ").last().toLong() }.reduce { acc, l -> acc * l }

private class Monkey(
    val items: MutableList<WorryLevel>,
    val operation: Operation,
    val test: WorryLevelTester
) {
  var inspectedItems: Long = 0
    private set

  fun takeTurn(monkeys: List<Monkey>, reliefReduction: Operation) {
    for (item in items) {
      inspectedItems++
      val newWorryLevel = reliefReduction(operation(item))
      val target = test(newWorryLevel)
      monkeys[target].items.add(newWorryLevel)
    }
    items.clear()
  }
}

fun parseStartingItems(input: String): MutableList<WorryLevel> {
  require(input.trim().startsWith("Starting items:"))

  return input
      .trim()
      .removePrefix("Starting items: ")
      .split(", ")
      .map { it.toLong() }
      .toMutableList()
}

fun parseOperation(input: String): Operation {
  require(input.trim().startsWith("Operation:"))

  val expression = input.split("=").last().trim()
  val (leftRaw, operatorRaw, rightRaw) = expression.split(" ")

  return { old ->
    val left =
        when (leftRaw) {
          "old" -> old
          else -> leftRaw.toLong()
        }
    val right =
        when (rightRaw) {
          "old" -> old
          else -> rightRaw.toLong()
        }
    when (operatorRaw) {
      "+" -> left + right
      "-" -> left - right
      "*" -> left * right
      "/" -> left / right
      else -> error("unknown operator, should not happen")
    }
  }
}

fun parseWorryLevelTester(input: List<String>): WorryLevelTester {
  require(input.size == 3)
  require(input[0].trim().startsWith("Test:"))
  require(input[1].trim().startsWith("If true:"))
  require(input[2].trim().startsWith("If false:"))

  val divisor = input[0].split(" ").last().toLong()
  val targetMonkeyIfTrue = input[1].split(" ").last().toInt()
  val targetMonkeyIfFalse = input[2].split(" ").last().toInt()

  return { worryLevel ->
    if (worryLevel % divisor == 0L) {
      targetMonkeyIfTrue
    } else {
      targetMonkeyIfFalse
    }
  }
}

typealias WorryLevel = Long

typealias Operation = (old: WorryLevel) -> WorryLevel

typealias WorryLevelTester = (worryLevel: WorryLevel) -> Int
