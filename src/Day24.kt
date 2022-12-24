import java.util.LinkedList
import java.util.Queue
import kotlin.math.min
import kotlin.test.assertEquals

fun main() {
  fun part1(input: List<String>): Int {
    return Valley(input.drop(1).dropLast(1).map { it.drop(1).dropLast(1) }).minutesToGoal()
  }

  fun part2(input: List<String>): Int {
    return Valley(input.drop(1).dropLast(1).map { it.drop(1).dropLast(1) })
        .minutesToGoalBackToStartAndBackToGoal()
  }

  // test if implementation meets criteria from the description, like:
  val testInput = readInput("Day24_test")
  assertEquals(18, part1(testInput))
  assertEquals(54, part2(testInput))

  val input = readInput("Day24")
  println(part1(input))
  println(part2(input))
}

class Valley(val map: List<String>) {
  private val maxRows = map.size
  private val maxCols = map[0].length

  fun minutesToGoal() = minutesTo(maxRows - 1, maxCols - 1, State(-1, 0, 0))

  fun minutesToGoalBackToStartAndBackToGoal(): Int {
    val minutesPart1 = minutesTo(maxRows - 1, maxCols - 1, State(-1, 0, 0))
    val minutesPart1And2 = minutesTo(0, 0, State(maxRows, maxCols - 1, minutesPart1))
    return minutesTo(maxRows - 1, maxCols - 1, State(-1, 0, minutesPart1And2))
  }

  private fun minutesTo(targetRow: Int, targetCol: Int, initialState: State): Int {
    val discoveredStates = mutableSetOf<State>()
    val queue: Queue<State> = LinkedList()
    queue.add(initialState)

    var minMinutes = Int.MAX_VALUE
    while (queue.isNotEmpty()) {
      val state = queue.poll()
      if (state.row == targetRow && state.column == targetCol) {
        minMinutes = min(minMinutes, state.time + 1)
        continue
      }

      val nextValidStates = state.nextStates().filter { it.isValid() }
      for (nextState in nextValidStates) {
        if (!discoveredStates.contains(nextState) && nextState.time < minMinutes) {
          discoveredStates.add(nextState)
          queue.add(nextState)
        }
      }
    }

    return minMinutes
  }

  private fun State.isValid(): Boolean {
    return isStartPosition() ||
        isGoalPosition() ||
        row in 0 until maxRows &&
            column in 0 until maxCols &&
            map[row][(column + time).mod(maxCols)] != '<' &&
            map[row][(column - time).mod(maxCols)] != '>' &&
            map[(row + time).mod(maxRows)][column] != '^' &&
            map[(row - time).mod(maxRows)][column] != 'v'
  }

  private fun State.isStartPosition() = row == -1 && column == 0
  private fun State.isGoalPosition() = row == maxRows && column == maxCols - 1

  inner class State(val row: Int, val column: Int, val time: Int) {
    fun nextStates() =
        listOf(
            State(row, column + 1, time + 1),
            State(row, column - 1, time + 1),
            State(row - 1, column, time + 1),
            State(row + 1, column, time + 1),
            State(row, column, time + 1),
        )

    override fun equals(other: Any?): Boolean {
      if (this === other) return true
      if (other !is State) return false

      if (row != other.row) return false
      if (column != other.column) return false
      if (time.mod(maxRows * maxCols) != other.time.mod(maxRows * maxCols)) return false

      return true
    }

    override fun hashCode(): Int {
      var result = row
      result = 31 * result + column
      result = 31 * result + time.mod(maxRows * maxCols)
      return result
    }
  }
}
