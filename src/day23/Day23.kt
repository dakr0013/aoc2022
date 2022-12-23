package day23

import day23.Direction.*
import kotlin.test.assertEquals
import readInput

fun main() {
  fun part1(input: List<String>): Int {
    val grove = Grove.parse(input)
    grove.simulate(10)
    return grove.emptyGroundTiles()
  }

  fun part2(input: List<String>): Int {
    val grove = Grove.parse(input)
    return grove.firstRoundWhereNoElfMoves()
  }

  // test if implementation meets criteria from the description, like:
  val testInput = readInput("Day23_test")
  assertEquals(110, part1(testInput))
  assertEquals(20, part2(testInput))

  val input = readInput("Day23")
  println(part1(input))
  println(part2(input))
}

class Grove(private val elves: List<Elf>) {
  private val directions = mutableListOf(NORTH, SOUTH, WEST, EAST)

  fun simulate(rounds: Int) {
    repeat(rounds) { simulateRound() }
  }

  private fun simulateRound(): Int {
    val elvesWhichCanProposeMove = elves.filter { it.hasAdjacentElf() }
    val elvesWhichProposedMove = elvesWhichCanProposeMove.mapNotNull { it.proposeMove() }
    val elvesToMove =
        elvesWhichProposedMove
            .groupBy { it.proposedPosition }
            .values
            .filter { it.size == 1 }
            .flatten()
    elvesToMove.forEach { it.move() }
    directions.add(directions.removeFirst())
    return elvesToMove.size
  }

  fun firstRoundWhereNoElfMoves(): Int {
    var round = 1
    while (simulateRound() != 0) {
      round++
    }
    return round
  }

  fun emptyGroundTiles(): Int {
    val minX = elves.minOf { it.currentPosition.x }
    val maxX = elves.maxOf { it.currentPosition.x }
    val minY = elves.minOf { it.currentPosition.y }
    val maxY = elves.maxOf { it.currentPosition.y }
    val width = maxX - minX + 1
    val height = maxY - minY + 1
    return width * height - elves.size
  }

  private fun Elf.hasAdjacentElf(): Boolean {
    val adjacentPositions =
        listOf(
                Vector2D(0, -1),
                Vector2D(1, -1),
                Vector2D(1, 0),
                Vector2D(1, 1),
                Vector2D(0, 1),
                Vector2D(-1, 1),
                Vector2D(-1, 0),
                Vector2D(-1, -1),
            )
            .map { this.currentPosition + it }
    return adjacentPositions.any { adjacentPosition ->
      elves.any { elf -> elf.currentPosition == adjacentPosition }
    }
  }

  private fun Elf.proposeMove(): Elf? {
    for (direction in directions) {
      if (this.canMoveIn(direction)) {
        return this.apply { proposedPosition = currentPosition + direction.moveTo }
      }
    }
    return null
  }

  private fun Elf.canMoveIn(direction: Direction): Boolean {
    return direction.toCheck
        .map { this.currentPosition + it }
        .all { positionToCheck -> elves.all { it.currentPosition != positionToCheck } }
  }

  companion object {
    fun parse(input: List<String>): Grove {
      var elfCount = 0
      val elves =
          input
              .mapIndexed { y, s ->
                s.mapIndexed { x, c ->
                      if (c == '#') {
                        Elf(elfCount++, Vector2D(x, y), Vector2D(x, y))
                      } else null
                    }
                    .filterNotNull()
              }
              .flatten()
      return Grove(elves)
    }
  }
}

data class Elf(val number: Int, var currentPosition: Vector2D, var proposedPosition: Vector2D) {
  fun move() {
    currentPosition = proposedPosition
  }
}

enum class Direction(val moveTo: Vector2D, val toCheck: Set<Vector2D>) {
  NORTH(Vector2D(0, -1), setOf(Vector2D(-1, -1), Vector2D(0, -1), Vector2D(1, -1))),
  SOUTH(Vector2D(0, 1), setOf(Vector2D(-1, 1), Vector2D(0, 1), Vector2D(1, 1))),
  WEST(Vector2D(-1, 0), setOf(Vector2D(-1, -1), Vector2D(-1, 0), Vector2D(-1, 1))),
  EAST(Vector2D(1, 0), setOf(Vector2D(1, -1), Vector2D(1, 0), Vector2D(1, 1))),
}

data class Vector2D(val x: Int, val y: Int) {
  operator fun plus(other: Vector2D) = Vector2D(x + other.x, y + other.y)
}
