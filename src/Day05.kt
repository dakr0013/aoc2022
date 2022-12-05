import kotlin.test.assertEquals

fun main() {
  fun part1(input: List<String>): String {
    val emptyLineIndex = input.indexOf("")
    val stackInput = input.take(emptyLineIndex)
    val rearrangementProcedureInput = input.drop(emptyLineIndex + 1)

    val stacks = parseInitialStacks(stackInput)
    val rearrangementActions = parseRearrangementProcedure(rearrangementProcedureInput)

    for (action in rearrangementActions) {
      repeat(action.quantity) { stacks[action.to].add(stacks[action.from].removeLast()) }
    }

    return stacks.map { it.last() }.joinToString("")
  }

  fun part2(input: List<String>): String {
    val emptyLineIndex = input.indexOf("")
    val stackInput = input.take(emptyLineIndex)
    val rearrangementProcedureInput = input.drop(emptyLineIndex + 1)

    val stacks = parseInitialStacks(stackInput)
    val rearrangementActions = parseRearrangementProcedure(rearrangementProcedureInput)

    for (action in rearrangementActions) {
      stacks[action.to].addAll(stacks[action.from].takeLast(action.quantity))
      stacks[action.from] = stacks[action.from].dropLast(action.quantity).toMutableList()
    }

    return stacks.map { it.last() }.joinToString("")
  }

  // test if implementation meets criteria from the description, like:
  val testInput = readInput("Day05_test")
  assertEquals("CMZ", part1(testInput))
  assertEquals("MCD", part2(testInput))

  val input = readInput("Day05")
  println(part1(input))
  println(part2(input))
}

private fun parseInitialStacks(input: List<String>): MutableList<MutableList<Char>> {
  val numberOfStacks = input.last().windowed(4, 4, true).size
  val stacks: MutableList<MutableList<Char>> = MutableList(numberOfStacks) { mutableListOf() }

  for (currentLine in input.asReversed().drop(1)) {
    for ((columnIndex, columnContent) in currentLine.windowed(4, 4, true).withIndex()) {
      val stackItem = columnContent[1]
      if (stackItem.isLetter()) {
        stacks[columnIndex].add(stackItem)
      }
    }
  }

  return stacks
}

private fun parseRearrangementProcedure(input: List<String>): List<RearrangementAction> {
  return input.map {
    val words = it.split(" ")
    RearrangementAction(words[1].toInt(), words[3].toInt() - 1, words[5].toInt() - 1)
  }
}

data class RearrangementAction(val quantity: Int, val from: Int, val to: Int)
