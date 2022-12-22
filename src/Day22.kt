import kotlin.math.max
import kotlin.math.min
import kotlin.test.assertEquals

fun main() {
  fun part1(input: List<String>): Int {
    val pathDescription = parsePathDescription(input.last())
    val board = Board.parse(input.dropLast(2))
    return board.getPassword(pathDescription)
  }

  fun part2(input: List<String>): Int {
    val pathDescription = parsePathDescription(input.last())
    val cube = CubeMap.parse(input.dropLast(2))
    return cube.getPassword(pathDescription)
  }

  // test if implementation meets criteria from the description, like:
  val testInput = readInput("Day22_test")
  assertEquals(6032, part1(testInput))
  assertEquals(5031, part2(testInput))

  val input = readInput("Day22")
  println(part1(input))
  println(part2(input))
}

class CubeMap(val regions: Map<Int, MapRegion>) {
  fun getPassword(pathDescription: List<String>): Int {
    var currentPosition = Position2(1, 0, 0, RIGHT)
    for (description in pathDescription) {
      currentPosition =
          if (description in listOf("L", "R")) {
            currentPosition.turn(description)
          } else {
            val numMoves = description.toInt()
            currentPosition.move(numMoves, this)
          }
    }

    return 1000 *
        (currentPosition.rowIndex + regions[currentPosition.regionNumber]!!.rowOffset + 1) +
        4 * (currentPosition.columnIndex + regions[currentPosition.regionNumber]!!.colOffset + 1) +
        currentPosition.facing
  }

  companion object {
    fun parse(input: List<String>): CubeMap {
      val numColumns = input.maxOf { it.length }
      val input = input.map { it.padEnd(numColumns, ' ') }
      val minMapWidth = input.minOf { row -> row.count { it != ' ' } }
      val minMapHeight =
          (0 until numColumns).minOf { colIndex -> input.count { it[colIndex] != ' ' } }
      val cubeSize = min(minMapWidth, minMapHeight)

      val isValidMapRegion: Array<Array<Boolean>> =
          Array(input.size / cubeSize) { regionRowIndex ->
            Array(numColumns / cubeSize) { regionColumnIndex ->
              val rowIndex = regionRowIndex * cubeSize
              val columnIndex = regionColumnIndex * cubeSize
              input[rowIndex][columnIndex] != ' '
            }
          }

      val faceRegionMapping: Array<Array<CubeFace?>> =
          Array(input.size / cubeSize) { Array(numColumns / cubeSize) { null } }

      faceRegionMapping[0][isValidMapRegion[0].indexOf(true)] =
          CubeFace(1, DiceUtils.diceEdges[1]!!)
      repeat(5) {
        for (i in faceRegionMapping.indices) {
          for (j in faceRegionMapping[i].indices) {
            if (faceRegionMapping[i][j] != null) {
              // right
              if (j < faceRegionMapping[i].lastIndex &&
                  isValidMapRegion[i][j + 1] &&
                  faceRegionMapping[i][j + 1] == null) {
                faceRegionMapping[i][j + 1] = faceRegionMapping[i][j]!!.face(RIGHT)
              }

              // left
              if (j > 0 && isValidMapRegion[i][j - 1] && faceRegionMapping[i][j - 1] == null) {
                faceRegionMapping[i][j - 1] = faceRegionMapping[i][j]!!.face(LEFT)
              }

              // down
              if (i < faceRegionMapping.lastIndex &&
                  isValidMapRegion[i + 1][j] &&
                  faceRegionMapping[i + 1][j] == null) {
                faceRegionMapping[i + 1][j] = faceRegionMapping[i][j]!!.face(DOWN)
              }

              // up
              if (i > 0 && isValidMapRegion[i - 1][j] && faceRegionMapping[i - 1][j] == null) {
                faceRegionMapping[i - 1][j] = faceRegionMapping[i][j]!!.face(UP)
              }
            }
          }
        }
      }

//      println(
//          faceRegionMapping.joinToString("\n") {
//            it.joinToString("") { if (it == null) " " else it.number.toString() }
//          })

      val regions =
          faceRegionMapping
              .mapIndexed { rowIndex, cubeFaces ->
                cubeFaces
                    .mapIndexed { colIndex, cubeFace ->
                      cubeFace?.let { cubeFace ->
                        MapRegion(
                            cubeFace.number,
                            rowIndex * cubeSize,
                            colIndex * cubeSize,
                            input
                                .slice(rowIndex * cubeSize until rowIndex * cubeSize + cubeSize)
                                .map {
                                  it.slice(colIndex * cubeSize until colIndex * cubeSize + cubeSize)
                                },
                            mapOf(
                                RIGHT to
                                    {
                                      val newRegion = cubeFace.faceNumber(RIGHT)
                                      val nextFace =
                                          faceRegionMapping.flatten().filterNotNull().first {
                                            it.number == newRegion
                                          }
                                      val turnAmount = cubeFace.turnAmount(RIGHT, nextFace)
                                      val newFacing = (it.facing + turnAmount).mod(4)
                                      when (turnAmount) {
                                        3 ->
                                            Position2(
                                                newRegion, it.columnIndex, it.rowIndex, newFacing)
                                        0 -> Position2(newRegion, it.rowIndex, 0, newFacing)
                                        1 ->
                                            Position2(
                                                newRegion, 0, cubeSize - 1 - it.rowIndex, newFacing)
                                        2 ->
                                            Position2(
                                                newRegion,
                                                cubeSize - 1 - it.rowIndex,
                                                it.columnIndex,
                                                newFacing)
                                        else -> error("should not happen: $turnAmount")
                                      }
                                    },
                                LEFT to
                                    {
                                      val newRegion = cubeFace.faceNumber(LEFT)
                                      val nextFace =
                                          faceRegionMapping.flatten().filterNotNull().first {
                                            it.number == newRegion
                                          }
                                      val turnAmount = cubeFace.turnAmount(LEFT, nextFace)
                                      val newFacing = (it.facing + turnAmount).mod(4)
                                      when (turnAmount) {
                                        1 ->
                                            Position2(
                                                newRegion,
                                                cubeSize - 1,
                                                cubeSize - 1 - it.rowIndex,
                                                newFacing)
                                        0 ->
                                            Position2(
                                                newRegion, it.rowIndex, cubeSize - 1, newFacing)
                                        3 ->
                                            Position2(
                                                newRegion, it.columnIndex, it.rowIndex, newFacing)
                                        2 ->
                                            Position2(
                                                newRegion,
                                                cubeSize - 1 - it.rowIndex,
                                                it.columnIndex,
                                                newFacing)
                                        else -> error("should not happen")
                                      }
                                    },
                                DOWN to
                                    {
                                      val newRegion = cubeFace.faceNumber(DOWN)
                                      val nextFace =
                                          faceRegionMapping.flatten().filterNotNull().first { c ->
                                            c.number == newRegion
                                          }
                                      val turnAmount = cubeFace.turnAmount(DOWN, nextFace)
                                      val newFacing = (it.facing + turnAmount).mod(4)
                                      when (turnAmount) {
                                        1 ->
                                            Position2(
                                                newRegion, it.columnIndex, it.rowIndex, newFacing)
                                        0 -> Position2(newRegion, 0, it.columnIndex, newFacing)
                                        3 ->
                                            Position2(
                                                newRegion,
                                                cubeSize - 1 - it.columnIndex,
                                                0,
                                                newFacing)
                                        2 ->
                                            Position2(
                                                newRegion,
                                                it.rowIndex,
                                                cubeSize - 1 - it.columnIndex,
                                                newFacing)
                                        else -> error("should not happen")
                                      }
                                    },
                                UP to
                                    {
                                      val newRegion = cubeFace.faceNumber(UP)
                                      val nextFace =
                                          faceRegionMapping.flatten().filterNotNull().first {
                                            it.number == newRegion
                                          }
                                      val turnAmount = cubeFace.turnAmount(UP, nextFace)
                                      val newFacing = (it.facing + turnAmount).mod(4)
                                      when (turnAmount) {
                                        1 ->
                                            Position2(
                                                newRegion, it.columnIndex, it.rowIndex, newFacing)
                                        0 ->
                                            Position2(
                                                newRegion, cubeSize - 1, it.columnIndex, newFacing)
                                        3 ->
                                            Position2(
                                                newRegion,
                                                cubeSize - 1,
                                                cubeSize - 1 - it.rowIndex,
                                                newFacing)
                                        2 ->
                                            Position2(
                                                newRegion,
                                                it.rowIndex,
                                                cubeSize - 1 - it.columnIndex,
                                                newFacing)
                                        else -> error("should not happen")
                                      }
                                    },
                            ),
                        )
                      }
                    }
                    .filterNotNull()
              }
              .flatten()

      //println(regions.joinToString("\n"))
      return CubeMap(regions.associateBy { it.number })
    }
  }
}

class MapRegion(
    val number: Int,
    val rowOffset: Int,
    val colOffset: Int,
    val tiles: List<String>,
    val wrappingRules: Map<Direction, WrappingRule>,
) {
  override fun toString() = "Region $number:\n${tiles.joinToString("\n")}"
}

typealias WrappingRule = (currentPosition: Position2) -> Position2

typealias Direction = Int

const val RIGHT = 0
const val DOWN = 1
const val LEFT = 2
const val UP = 3

data class Position2(
    val regionNumber: Int,
    val rowIndex: Int,
    val columnIndex: Int,
    val facing: Direction,
) {
  fun turn(input: String): Position2 {
    val newFacing =
        if (input == "R") {
          (facing + 1).mod(4)
        } else {
          (facing - 1).mod(4)
        }
    return copy(facing = newFacing)
  }

  fun move(amount: Int, map: CubeMap): Position2 {
    var currentPosition = this
    for (i in 1..amount) {
      val nextPosition = currentPosition.nextPosition(map)
      if (nextPosition.isInvalid(map)) break
      currentPosition = nextPosition
    }
    return currentPosition
  }

  private fun isInvalid(map: CubeMap) =
      map.regions[regionNumber]!!.tiles[rowIndex][columnIndex] == '#'

  private fun nextPosition(map: CubeMap): Position2 {
    val currentRegion = map.regions[regionNumber]!!
    return when (facing) {
      RIGHT -> {
        val shouldWrap = columnIndex == currentRegion.tiles[rowIndex].lastIndex
        if (shouldWrap) {
          currentRegion.wrappingRules[RIGHT]!!.invoke(this)
        } else {
          copy(columnIndex = columnIndex + 1)
        }
      }
      DOWN -> {
        val shouldWrap = rowIndex == currentRegion.tiles.lastIndex
        if (shouldWrap) {
          currentRegion.wrappingRules[DOWN]!!.invoke(this)
        } else {
          copy(rowIndex = rowIndex + 1)
        }
      }
      LEFT -> {
        val shouldWrap = columnIndex == 0
        if (shouldWrap) {
          currentRegion.wrappingRules[LEFT]!!.invoke(this)
        } else {
          copy(columnIndex = columnIndex - 1)
        }
      }
      UP -> {
        val shouldWrap = rowIndex == 0
        if (shouldWrap) {
          currentRegion.wrappingRules[UP]!!.invoke(this)
        } else {
          copy(rowIndex = rowIndex - 1)
        }
      }
      else -> error("should not happen")
    }
  }
}

/** Represents cube face on the map, edges are ordered: right,bottom,left,top */
class CubeFace(
    val number: Int,
    val edges: List<CubeEdge>,
) {
  fun faceNumber(direction: Int): Int = edges[direction].faceNumbers.first { it != number }

  fun face(direction: Int): CubeFace {
    val faceNumber = faceNumber(direction)
    return CubeFace(
        faceNumber,
        DiceUtils.getEdges(
            faceNumber,
            CubeEdge(faceNumber, number),
            direction.opposite(),
        ),
    )
  }

  fun turnAmount(direction: Int, nextFace: CubeFace): Int {
    val connectingEdge = CubeEdge(number, nextFace.number)
    assertEquals(
        direction, edges.indexOf(connectingEdge), "edges are: $edges, did not find $connectingEdge")
    return (nextFace.edges.indexOf(connectingEdge) - edges.indexOf(connectingEdge).opposite()).mod(
        4)
  }

  override fun toString(): String {
    return "Face: $number, ${edges.map { it.faceNumbers.first { it != number } }}"
  }
}

object DiceUtils {
  /** face number to edge mapping of a dice. edges are ordered clockwise. */
  val diceEdges =
      mapOf(
          1 to listOf(CubeEdge(1, 3), CubeEdge(1, 2), CubeEdge(1, 4), CubeEdge(1, 5)),
          2 to listOf(CubeEdge(2, 3), CubeEdge(2, 6), CubeEdge(2, 4), CubeEdge(1, 2)),
          3 to listOf(CubeEdge(3, 5), CubeEdge(3, 6), CubeEdge(2, 3), CubeEdge(1, 3)),
          4 to listOf(CubeEdge(2, 4), CubeEdge(4, 6), CubeEdge(4, 5), CubeEdge(1, 4)),
          5 to listOf(CubeEdge(3, 5), CubeEdge(1, 5), CubeEdge(4, 5), CubeEdge(5, 6)),
          6 to listOf(CubeEdge(3, 6), CubeEdge(5, 6), CubeEdge(4, 6), CubeEdge(2, 6)),
      )

  /** Returns edges of a dice in clockwise order given its face number */
  fun getEdges(faceNumber: Int, with: CubeEdge, atIndex: Int): List<CubeEdge> {
    val edges = diceEdges[faceNumber]!!
    val indexOfFirstEdge = (edges.indexOf(with) - atIndex).mod(4)
    return edges.slice(indexOfFirstEdge..edges.lastIndex) + edges.slice(0 until indexOfFirstEdge)
  }
}

fun Int.opposite(): Int = (this + 2).mod(4)

/** Represents an edge of a cube, the two face numbers correspond the faces this edge connects */
data class CubeEdge(val num1: Int, val num2: Int) {
  val faceNumbers = setOf(num1, num2)

  override fun equals(other: Any?): Boolean {
    if (this === other) return true
    if (other !is CubeEdge) return false

    if (min(num1, num2) != min(other.num1, other.num2)) return false
    if (max(num1, num2) != max(other.num1, other.num2)) return false

    return true
  }

  override fun hashCode(): Int {
    var result = min(num1, num2)
    result = 31 * result + max(num1, num2)
    return result
  }

  override fun toString(): String {
    return "CubeEdge(${min(num1, num2)}, ${max(num1, num2)})"
  }
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
