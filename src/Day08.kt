import kotlin.test.assertEquals

fun main() {
  fun part1(input: List<String>): Int {
    var visibleTreeCount = (input.size - 1) * 4
    for (row in 1 until input.size - 1) {
      for (col in 1 until input.first().length - 1) {
        val currentTree = input[row][col].digitToInt()
        var isVisibleFromLeft = true
        for (k in 0 until col) {
          val aLeftTree = input[row][k]
          if (aLeftTree.digitToInt() >= currentTree) {
            isVisibleFromLeft = false
            break
          }
        }
        var isVisibleFromRight = true
        for (k in col + 1 until input.first().length) {
          val aRightTree = input[row][k]
          if (aRightTree.digitToInt() >= currentTree) {
            isVisibleFromRight = false
            break
          }
        }
        var isVisibleFromTop = true
        for (k in 0 until row) {
          val aTopTree = input[k][col]
          if (aTopTree.digitToInt() >= currentTree) {
            isVisibleFromTop = false
            break
          }
        }
        var isVisibleFromBottom = true
        for (k in row + 1 until input.size) {
          val aBottomTree = input[k][col]
          if (aBottomTree.digitToInt() >= currentTree) {
            isVisibleFromBottom = false
            break
          }
        }
        if (isVisibleFromLeft || isVisibleFromRight || isVisibleFromTop || isVisibleFromBottom)
            visibleTreeCount++
      }
    }

    return visibleTreeCount
  }

  fun part2(input: List<String>): Int {
    val scenicScores = mutableListOf<Int>()
    for (row in 1 until input.size - 1) {
      for (col in 1 until input.first().length - 1) {
        val currentTree = input[row][col].digitToInt()
        var leftViewDistance = 0
        for (k in col - 1 downTo 0) {
          leftViewDistance++
          val aLeftTree = input[row][k]
          if (aLeftTree.digitToInt() >= currentTree) {
            break
          }
        }
        var rightViewDistance = 0
        for (k in col + 1 until input.first().length) {
          rightViewDistance++
          val aRightTree = input[row][k]
          if (aRightTree.digitToInt() >= currentTree) {
            break
          }
        }
        var topViewDistance = 0
        for (k in row - 1 downTo 0) {
          topViewDistance++
          val aTopTree = input[k][col]
          if (aTopTree.digitToInt() >= currentTree) {
            break
          }
        }
        var bottomViewDistance = 0
        for (k in row + 1 until input.size) {
          bottomViewDistance++
          val aBottomTree = input[k][col]
          if (aBottomTree.digitToInt() >= currentTree) {
            break
          }
        }

        val scenicScore =
            leftViewDistance * rightViewDistance * topViewDistance * bottomViewDistance
        scenicScores.add(scenicScore)
      }
    }
    return scenicScores.max()
  }

  // test if implementation meets criteria from the description, like:
  val testInput = readInput("Day08_test")
  assertEquals(21, part1(testInput))
  assertEquals(8, part2(testInput))

  val input = readInput("Day08")
  println(part1(input))
  println(part2(input))
}
