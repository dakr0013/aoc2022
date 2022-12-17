import kotlin.test.assertEquals

fun main() {
  fun part1(input: List<String>): Long {
    val chamber = CaveChamber(JetPattern(input.first()))
    repeat(2022) { chamber.dropRock() }
    return chamber.towerSize
  }

  fun part2(input: List<String>): Long {
    val chamber = CaveChamber(JetPattern(input.first()))
    val (initialPart, repeatingPart) = chamber.findRepeatingPattern()

    var remainingRocks = 1_000_000_000_000
    var towerSize = 0L

    towerSize += initialPart.towerSize
    remainingRocks -= initialPart.rockCount

    val possibleRepeats = remainingRocks / repeatingPart.rockCount
    towerSize += possibleRepeats * repeatingPart.towerSize
    remainingRocks %= repeatingPart.rockCount

    val tempTowerSize = chamber.towerSize
    for (i in 1..remainingRocks) {
      chamber.dropRock()
    }
    val lastPartTowerSize = chamber.towerSize - tempTowerSize

    return towerSize + lastPartTowerSize
  }

  // test if implementation meets criteria from the description, like:
  val testInput = readInput("Day17_test")
  assertEquals(3068, part1(testInput))
  assertEquals(1_514_285_714_288, part2(testInput))

  val input = readInput("Day17")
  println(part1(input))
  println(part2(input))
}

data class TowerPart(val towerSize: Long, val rockCount: Long)

data class RepeatingResult(val initialPart: TowerPart, val repeatingPart: TowerPart)

class CaveChamber(private val jetPattern: JetPattern) {
  private val rockSpawner = RockSpawner()
  private val walls = 0b1_0000000_1
  private val floor = 0b1_1111111_1
  private val largestRockSize = 4
  private val airBufferBetweenTowerTopAndNewRock = 3
  private val airBufferSize = largestRockSize + airBufferBetweenTowerTopAndNewRock
  private val chamber: MutableList<Int> =
      buildList {
            repeat(airBufferSize) { add(walls) }
            add(floor)
          }
          .toMutableList()

  var towerSize = 0L
    private set

  fun dropRock() {
    var rock = rockSpawner.next()

    var indexOfUpperRockEdge = largestRockSize - rock.size
    while (true) {
      // push by jet of hot gas
      val nextRock = rock.push(jetPattern.next())
      var isNextRockValid = true
      for (i in rock.lastIndex downTo 0) {
        val rockLineIntersects = (nextRock[i] and chamber[i + indexOfUpperRockEdge]) != 0
        if (rockLineIntersects) {
          isNextRockValid = false
          break
        }
      }
      if (isNextRockValid) rock = nextRock

      // fall down
      var isMoveDownValid = true
      for (i in rock.lastIndex downTo 0) {
        val rockLineIntersects = (rock[i] and chamber[i + indexOfUpperRockEdge + 1]) != 0
        if (rockLineIntersects) {
          isMoveDownValid = false
          break
        }
      }
      if (isMoveDownValid) indexOfUpperRockEdge++ else break
    }

    // add stopped rock to chamber
    for (i in rock.indices) {
      chamber[indexOfUpperRockEdge + i] = chamber[indexOfUpperRockEdge + i] or rock[i]
    }

    // calculate added height by this rock
    val addedHeight = airBufferSize - indexOfUpperRockEdge
    if (addedHeight > 0) {
      towerSize += addedHeight
      repeat(addedHeight) { chamber.add(0, walls) }
    }

    // just some random values to keep list size small
    // this way chamber only has around top 100 lines
    // it is unlikely that a rock will fall down more than 100
    if (chamber.size > 100) repeat(5) { chamber.removeLast() }
  }

  fun findRepeatingPattern(): RepeatingResult {
    data class JetPatternTripResult(
        val rockCountDeltaSinceLastTrip: Long,
        val towerSizeDeltaSinceLastTrip: Long,
        val nextJetPatternIndex: Int,
        val nextRockType: Int,
    ) {
      override fun toString(): String =
          "JetPatternTripResult($rockCountDeltaSinceLastTrip, $towerSizeDeltaSinceLastTrip, $nextJetPatternIndex, $nextRockType)"
    }

    val tripResults: MutableList<JetPatternTripResult> = mutableListOf()
    var jetPatternRoundTripCount = 0
    var towerSizeLastTrip = 0L
    var rockCountLastTrip = 0L
    var rockCount = 0L
    while (true) {
      dropRock().also { rockCount++ }
      if (jetPattern.roundTripCount > jetPatternRoundTripCount) {
        jetPatternRoundTripCount++
        val tripResult =
            JetPatternTripResult(
                rockCountDeltaSinceLastTrip = rockCount - rockCountLastTrip,
                towerSizeDeltaSinceLastTrip = towerSize - towerSizeLastTrip,
                nextJetPatternIndex = jetPattern.nextIndex,
                nextRockType = rockSpawner.nextRockType,
            )
        tripResults.add(tripResult)

        val sameResultIndex = tripResults.dropLast(1).indexOf(tripResult)
        if (sameResultIndex >= 0) {
          val patternSize = tripResults.lastIndex - sameResultIndex
          if (tripResults.size >= patternSize * 2) {
            val pattern1 = tripResults.slice(sameResultIndex + 1 - patternSize..sameResultIndex)
            val pattern2 = tripResults.slice(sameResultIndex + 1..tripResults.lastIndex)
            if (pattern1 == pattern2) {
              val initialPattern = tripResults.slice(0..sameResultIndex - patternSize)
              return RepeatingResult(
                  initialPart =
                      TowerPart(
                          initialPattern.sumOf { it.towerSizeDeltaSinceLastTrip },
                          initialPattern.sumOf { it.rockCountDeltaSinceLastTrip }),
                  repeatingPart =
                      TowerPart(
                          pattern1.sumOf { it.towerSizeDeltaSinceLastTrip },
                          pattern1.sumOf { it.rockCountDeltaSinceLastTrip }),
              )
            }
          }
        }

        rockCountLastTrip = rockCount
        towerSizeLastTrip = towerSize
      }
    }
  }

  override fun toString(): String {
    return "Chamber: \n" + chamber.joinToString("\n") { it.toString(2).replace("0", " ") }
  }
}

data class JetPattern(private val pattern: String) {
  var roundTripCount = 0
    private set
  var nextIndex = 0
    private set

  /** @return true if next direction is left, else false */
  fun next(): Boolean =
      (pattern[nextIndex] == '<').also {
        nextIndex = (nextIndex + 1) % pattern.length
        if (nextIndex == 0) roundTripCount++
      }
}

class RockSpawner {
  var nextRockType = 0
    private set
  fun next(): IntArray =
      when (nextRockType) {
        0 ->
            intArrayOf(
                0b0_0011110_0,
            )
        1 ->
            intArrayOf(
                0b0_0001000_0,
                0b0_0011100_0,
                0b0_0001000_0,
            )
        2 ->
            intArrayOf(
                0b0_0000100_0,
                0b0_0000100_0,
                0b0_0011100_0,
            )
        3 ->
            intArrayOf(
                0b0_0010000_0,
                0b0_0010000_0,
                0b0_0010000_0,
                0b0_0010000_0,
            )
        else ->
            intArrayOf(
                0b0_0011000_0,
                0b0_0011000_0,
            )
      }.also { nextRockType = (nextRockType + 1) % 5 }
}

fun IntArray.push(shiftLeft: Boolean): IntArray = if (shiftLeft) this.shl() else this.shr()

fun IntArray.shl(): IntArray = IntArray(size) { i -> this[i].shl(1) }

fun IntArray.shr(): IntArray = IntArray(size) { i -> this[i].ushr(1) }
