import kotlin.test.assertEquals

fun main() {
  fun part1(input: List<String>): Int {
    val pathDescription = parsePathDescription(input.last())
    val board = Board.parse(input.dropLast(2))
    return board.getPassword(pathDescription)
  }

  fun part2(input: List<String>): Int {
    return input.size
  }

  // test if implementation meets criteria from the description, like:
  val testInput = readInput("Day22_test")
  assertEquals(6032, part1(testInput))
  // assertEquals(24000, part2(testInput))

  val input = readInput("Day22")
  println(part1(input))
  println(part2(input))
}

class Board(
    val map: Array<CharArray>,
    private val rowRangeByColumnIndex: List<IntRange>,
    private val columnRangeByRowIndex: List<IntRange>
) {
  private var currentPosition = Position(0, columnRangeByRowIndex[0].first)

  fun getPassword(pathDescription: List<String>): Int {
    for (description in pathDescription) {
      if (description in listOf("L", "R")) {
        currentPosition = currentPosition.turn(description)
      } else {
        val numMoves = description.toInt()
        when (currentPosition.facing) {
          RIGHT -> {
            for (i in 1..numMoves) {
              var nextColumnIndex = (currentPosition.columnIndex + 1)
              if (nextColumnIndex > columnRangeByRowIndex[currentPosition.rowIndex].last) {
                nextColumnIndex = columnRangeByRowIndex[currentPosition.rowIndex].first
              }
              if (map[currentPosition.rowIndex][nextColumnIndex] == '#') break
              currentPosition = currentPosition.copy(columnIndex = nextColumnIndex)
            }
          }
          DOWN -> {
            for (i in 1..numMoves) {
              var nextRowIndex = (currentPosition.rowIndex + 1)
              if (nextRowIndex > rowRangeByColumnIndex[currentPosition.columnIndex].last) {
                nextRowIndex = rowRangeByColumnIndex[currentPosition.columnIndex].first
              }
              if (map[nextRowIndex][currentPosition.columnIndex] == '#') break
              currentPosition = currentPosition.copy(rowIndex = nextRowIndex)
            }
          }
          LEFT -> {
            for (i in 1..numMoves) {
              var nextColumnIndex = (currentPosition.columnIndex - 1)
              if (nextColumnIndex < columnRangeByRowIndex[currentPosition.rowIndex].first) {
                nextColumnIndex = columnRangeByRowIndex[currentPosition.rowIndex].last
              }
              if (map[currentPosition.rowIndex][nextColumnIndex] == '#') break
              currentPosition = currentPosition.copy(columnIndex = nextColumnIndex)
            }
          }
          UP -> {
            for (i in 1..numMoves) {
              var nextRowIndex = (currentPosition.rowIndex - 1)
              if (nextRowIndex < rowRangeByColumnIndex[currentPosition.columnIndex].first) {
                nextRowIndex = rowRangeByColumnIndex[currentPosition.columnIndex].last
              }
              if (map[nextRowIndex][currentPosition.columnIndex] == '#') break
              currentPosition = currentPosition.copy(rowIndex = nextRowIndex)
            }
          }
        }
      }
    }

    return 1000 * (currentPosition.rowIndex + 1) +
        4 * (currentPosition.columnIndex + 1) +
        currentPosition.facing
  }

  companion object {
    fun parse(input: List<String>): Board {
      val rowCount = input.size
      val rowSize = input.maxOf { it.length }
      val rowRangeByColumnIndex =
          (0 until rowSize).map { colIndex ->
            IntRange(
                input.indexOfFirst { it.padEnd(rowSize, ' ')[colIndex] != ' ' },
                input.indexOfLast { it.padEnd(rowSize, ' ')[colIndex] != ' ' },
            )
          }
      val columnRangeByRowIndex =
          input.map { row ->
            IntRange(
                row.indexOfFirst { it != ' ' },
                row.indexOfLast { it != ' ' },
            )
          }

      val map = Array(rowCount) { CharArray(rowSize) { ' ' } }
      for (rowIndex in 0 until rowCount) {
        for (columnIndex in columnRangeByRowIndex[rowIndex]) {
          map[rowIndex][columnIndex] = input[rowIndex][columnIndex]
        }
      }

      return Board(map, rowRangeByColumnIndex, columnRangeByRowIndex)
    }
  }

  override fun toString() = map.joinToString("\n") { it.joinToString("") }
}

fun parsePathDescription(input: String): List<String> {
  val movements = input.split(Regex("[LR]"))
  val turns = input.split(Regex("\\d+")).drop(1).dropLast(1)
  return movements.zip(turns).flatMap { it.toList() } + movements.last()
}

const val RIGHT = 0
const val DOWN = 1
const val LEFT = 2
const val UP = 3

data class Position(val rowIndex: Int, val columnIndex: Int, val facing: Int = RIGHT) {
  fun turn(input: String): Position {
    val newFacing =
        if (input == "R") {
          (facing + 1).mod(4)
        } else {
          (facing - 1).mod(4)
        }
    return copy(facing = newFacing)
  }
}
