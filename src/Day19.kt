import java.util.LinkedList
import java.util.Queue
import kotlin.math.max
import kotlin.math.min
import kotlin.system.measureTimeMillis

fun main() {
  fun part1(input: List<String>): Int {
    val blueprints = parseBlueprints(input)
    return blueprints.sumOf { it.qualityLevel(24) }
  }

  fun part2(input: List<String>): Int {
    val blueprints = parseBlueprints(input)
    return blueprints.take(3).foldRight(1) { blueprint, acc -> blueprint.maxGeodeCount(32) * acc }
  }

  // test if implementation meets criteria from the description, like:
  val testInput = readInput("Day19_test")
  println(
      "took part1: " +
          measureTimeMillis {
            println(part1(testInput))
            println(33)
          })
  // part2 takes about 1 min
  println(
      "took part2: " +
          measureTimeMillis {
            println(part2(testInput))
            println(56 * 62)
          })

  val input = readInput("Day19")
  println("took real part1: " + measureTimeMillis { println(part1(input)) })
  // part2 takes about 20 seconds
  println("took real part2: " + measureTimeMillis { println(part2(input)) })
}

private fun parseBlueprints(input: List<String>): List<Blueprint> {
  return input.map { Blueprint.parse(it) }
}

private val linePatternDay19 =
    Regex(
        "Blueprint (\\d+):" +
            " Each ore robot costs (\\d+) ore\\." +
            " Each clay robot costs (\\d+) ore\\." +
            " Each obsidian robot costs (\\d+) ore and (\\d+) clay\\." +
            " Each geode robot costs (\\d+) ore and (\\d+) obsidian\\.")

private data class Blueprint(
    val number: Int,
    val oreRobotCostOre: Int,
    val clayRobotCostOre: Int,
    val obsidianRobotCostOre: Int,
    val obsidianRobotCostClay: Int,
    val geodeRobotCostOre: Int,
    val geodeRobotCostObsidian: Int,
) {
  val maxOreRobots =
      listOf(
              clayRobotCostOre,
              obsidianRobotCostOre,
              geodeRobotCostOre,
          )
          .max()
  val maxClayRobots = obsidianRobotCostClay
  val maxObsidianRobots = geodeRobotCostObsidian

  fun qualityLevel(timeLeft: Int) = maxGeodeCount(timeLeft) * number

  fun maxGeodeCount(timeLeft: Int) =
      maxGeodeCount(SimulationState(timeLeft)).also {
        println("Blueprint #$number, checked paths: $checkPathsCount")
      }

  var checkPathsCount = 0L
  private fun maxGeodeCount(initialState: SimulationState): Int {
    val discoveredStates: MutableSet<SimulationState> = mutableSetOf()
    val queue: Queue<SimulationState> = LinkedList()
    queue.add(initialState)

    var maxGeode = 0
    while (queue.isNotEmpty()) {
      val state = queue.poll()
      if (state.timeLeft == 0) {
        maxGeode = max(maxGeode, state.geodeCount)
        checkPathsCount++
        continue
      }

      val nextStates = state.getPossibleNextStates(this)
      for (nextState in nextStates) {
        if (!discoveredStates.contains(nextState)) {
          discoveredStates.add(nextState)
          queue.add(nextState)
        }
      }

      if (nextStates.isEmpty()) {
        maxGeode = max(maxGeode, state.geodeCount + state.timeLeft * state.geodeRobotCount)
        checkPathsCount++
      }
    }

    return maxGeode
  }

  companion object {
    fun parse(input: String): Blueprint {
      val values = linePatternDay19.matchEntire(input)!!.groupValues.drop(1).map { it.toInt() }
      return Blueprint(values[0], values[1], values[2], values[3], values[4], values[5], values[6])
    }
  }
}

private data class SimulationState(
    val timeLeft: Int,
    val oreCount: Int = 0,
    val clayCount: Int = 0,
    val obsidianCount: Int = 0,
    val geodeCount: Int = 0,
    val oreRobotCount: Int = 1,
    val clayRobotCount: Int = 0,
    val obsidianRobotCount: Int = 0,
    val geodeRobotCount: Int = 0,
) {
  fun buildNoRobot(): SimulationState =
      copy(
          oreCount = oreCount + oreRobotCount,
          clayCount = clayCount + clayRobotCount,
          obsidianCount = obsidianCount + obsidianRobotCount,
          geodeCount = geodeCount + geodeRobotCount,
          timeLeft = timeLeft - 1)

  fun buildOreRobot(blueprint: Blueprint): SimulationState =
      copy(
          oreCount = oreCount + oreRobotCount - blueprint.oreRobotCostOre,
          clayCount = clayCount + clayRobotCount,
          obsidianCount = obsidianCount + obsidianRobotCount,
          geodeCount = geodeCount + geodeRobotCount,
          oreRobotCount = oreRobotCount + 1,
          timeLeft = timeLeft - 1)

  fun buildClayRobot(blueprint: Blueprint): SimulationState =
      copy(
          oreCount = oreCount + oreRobotCount - blueprint.clayRobotCostOre,
          clayCount = clayCount + clayRobotCount,
          obsidianCount = obsidianCount + obsidianRobotCount,
          geodeCount = geodeCount + geodeRobotCount,
          clayRobotCount = clayRobotCount + 1,
          timeLeft = timeLeft - 1)

  fun buildObsidianRobot(blueprint: Blueprint): SimulationState =
      copy(
          oreCount = oreCount + oreRobotCount - blueprint.obsidianRobotCostOre,
          clayCount = clayCount + clayRobotCount - blueprint.obsidianRobotCostClay,
          obsidianCount = obsidianCount + obsidianRobotCount,
          geodeCount = geodeCount + geodeRobotCount,
          obsidianRobotCount = obsidianRobotCount + 1,
          timeLeft = timeLeft - 1)

  fun buildGeodeRobot(blueprint: Blueprint): SimulationState =
      copy(
          oreCount = oreCount + oreRobotCount - blueprint.geodeRobotCostOre,
          clayCount = clayCount + clayRobotCount,
          obsidianCount = obsidianCount + obsidianRobotCount - blueprint.geodeRobotCostObsidian,
          geodeCount = geodeCount + geodeRobotCount,
          geodeRobotCount = geodeRobotCount + 1,
          timeLeft = timeLeft - 1)

  fun canBuildOreRobot(blueprint: Blueprint) = oreCount >= blueprint.oreRobotCostOre

  fun canBuildClayRobot(blueprint: Blueprint) = oreCount >= blueprint.clayRobotCostOre

  fun canBuildObsidianRobot(blueprint: Blueprint) =
      clayCount >= blueprint.obsidianRobotCostClay && oreCount >= blueprint.obsidianRobotCostOre

  fun canBuildGeodeRobot(blueprint: Blueprint) =
      obsidianCount >= blueprint.geodeRobotCostObsidian && oreCount >= blueprint.geodeRobotCostOre

  fun getPossibleNextStates(blueprint: Blueprint): List<SimulationState> {
    return buildList {
      // tried to filter out unnecessary paths to get down to a lower number of combinations
      val canBuildOreRobot = canBuildOreRobot(blueprint)
      val canBuildClayRobot = canBuildClayRobot(blueprint)
      val canBuildObsidianRobot = canBuildObsidianRobot(blueprint)
      val canBuildGeodeRobot = canBuildGeodeRobot(blueprint)

      val requiredObsidianRobots = blueprint.maxObsidianRobots - obsidianRobotCount
      val geodeRobotCountPossible =
          min(
              (oreCount + timeLeft * oreRobotCount - blueprint.obsidianRobotCostOre) /
                  blueprint.geodeRobotCostOre,
              timeLeft - 1)
      val obsidianRobotCountPossible =
          min(
              (oreCount + timeLeft * oreRobotCount - blueprint.clayRobotCostOre) /
                  blueprint.obsidianRobotCostOre,
              requiredObsidianRobots)

      val needsObsidian =
          obsidianCount + timeLeft * obsidianRobotCount <
              geodeRobotCountPossible * blueprint.geodeRobotCostObsidian
      val needsClay =
          needsObsidian &&
              clayCount + obsidianRobotCountPossible * clayRobotCount <
                  obsidianRobotCountPossible * blueprint.obsidianRobotCostClay

      val obsidianRobotsReached =
          obsidianRobotCount == blueprint.maxObsidianRobots || !needsObsidian || timeLeft < 4
      val clayRobotsReached =
          clayRobotCount == blueprint.maxClayRobots ||
              obsidianRobotsReached ||
              !needsClay ||
              timeLeft <
                  6 // makes no sense to build clay robot if timeLeft <= 5, because 1 min is spend
      // for building, then minimum 1 min to generate clay (otherwise it makes no sense to build
      // clay robot if higher rate is not used). then the same for obsidian and geode as clay makes
      // only sense if i want to build obsidian robot and then geode robot -> minimum 6 minutes
      // needed for clay robot to make sense

      val notNeedsOre =
          obsidianRobotsReached &&
              oreCount + timeLeft * oreRobotCount - blueprint.oreRobotCostOre + (timeLeft - 1) >=
                  (timeLeft - 1) * blueprint.geodeRobotCostOre
      val oreRobotsReached =
          oreRobotCount == blueprint.maxOreRobots ||
              obsidianRobotsReached && oreRobotCount == blueprint.geodeRobotCostOre ||
              clayRobotsReached &&
                  oreRobotCount ==
                      max(blueprint.geodeRobotCostOre, blueprint.obsidianRobotCostOre) ||
              notNeedsOre ||
              timeLeft < 4 ||
              timeLeft < 5 &&
                  oreCount >=
                      blueprint.geodeRobotCostOre // no need for ore robot as i already can build an
      // geode

      if (canBuildOreRobot && !oreRobotsReached) add(buildOreRobot(blueprint))
      if (canBuildClayRobot && !clayRobotsReached) add(buildClayRobot(blueprint))
      if (canBuildObsidianRobot && !obsidianRobotsReached) add(buildObsidianRobot(blueprint))
      if (canBuildGeodeRobot && timeLeft > 1) // minimum 2 min needed to profit from higher rate
       add(buildGeodeRobot(blueprint))

      when {
        (canBuildGeodeRobot || timeLeft < 2) &&
            (oreRobotsReached || canBuildOreRobot) &&
            (clayRobotsReached || canBuildClayRobot) &&
            (obsidianRobotsReached || canBuildObsidianRobot) -> {}
        obsidianRobotCount == 0 &&
            canBuildObsidianRobot &&
            (oreRobotsReached || canBuildOreRobot) &&
            (clayRobotsReached || canBuildClayRobot) -> {}
        clayRobotCount == 0 && canBuildClayRobot && (oreRobotsReached || canBuildOreRobot) -> {}
        else -> add(buildNoRobot())
      }
    }
  }
}
