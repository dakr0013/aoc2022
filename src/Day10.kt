import kotlin.test.assertEquals

fun main() {
  fun part1(input: List<String>): Long {
    val program = parseInstructions(input)
    return execute(program)
  }

  fun part2(input: List<String>): String {
    val program = parseInstructions(input)
    return execute2(program)
  }

  // test if implementation meets criteria from the description, like:
  val testInput = readInput("Day10_test")
  assertEquals(13140, part1(testInput))
  val expectedImage =
      """
    ##..##..##..##..##..##..##..##..##..##..
    ###...###...###...###...###...###...###.
    ####....####....####....####....####....
    #####.....#####.....#####.....#####.....
    ######......######......######......####
    #######.......#######.......#######.....
  """
          .trimIndent()
  assertEquals(expectedImage, part2(testInput))

  val input = readInput("Day10")
  println(part1(input))
  println(part2(input))
}

private fun parseInstructions(input: List<String>): List<Instruction> =
    input.flatMap {
      val instruction = it.split(" ").first()
      when (instruction) {
        "addx" -> listOf(NoopInstruction, AddInstruction(it.split(" ").last().toInt()))
        else -> listOf(NoopInstruction)
      }
    }

private fun execute(instructions: List<Instruction>): Long {
  var cycle = 1
  var registerX: Long = 1
  var sumOfSignalStrength: Long = 0

  for (instruction in instructions) {
    if ((cycle - 20) % 40 == 0) {
      sumOfSignalStrength += cycle * registerX
    }

    when (instruction) {
      is NoopInstruction -> {}
      is AddInstruction -> {
        registerX += instruction.amount
      }
    }
    cycle++
  }
  return sumOfSignalStrength
}

private fun execute2(instructions: List<Instruction>): String {
  var cycle = 1
  var registerX: Long = 1
  val crt = StringBuilder()

  for (instruction in instructions) {
    if ((cycle - 1) in (registerX - 1..registerX + 1)) {
      crt.append("#")
    } else {
      crt.append(".")
    }

    if (cycle % 40 == 0) {
      registerX += 40
      crt.appendLine()
    }

    when (instruction) {
      is NoopInstruction -> {}
      is AddInstruction -> {
        registerX += instruction.amount
      }
    }
    cycle++
  }
  return crt.toString().trim()
}

private sealed interface Instruction

private class AddInstruction(val amount: Int) : Instruction

private object NoopInstruction : Instruction
