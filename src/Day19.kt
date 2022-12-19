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
  // part 2 takes about 10 min
  //  println(
  //      "took part2: " +
  //          measureTimeMillis {
  //             println(part2(testInput))
  //            println(56 * 62)
  //          })

  val input = readInput("Day19")
  println("took real part1: " + measureTimeMillis { println(part1(input)) })
  // part 2 takes about 10 min
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
      maxGeodeCount(timeLeft, Environment()).also {
        println("Blueprint #$number, checked comb: $checkedCombinations")
      }

  var checkedCombinations = 0L
  private fun maxGeodeCount(timeLeft: Int, env: Environment): Int {
    if (timeLeft == 0) return env.geodeCount.also { checkedCombinations++ }

    val blueprint = this
    val options = buildList {
      // tried to filter out unnecessary paths to get down to a lower number of combinations
      val canBuildOreRobot = env.canBuildOreRobot(blueprint)
      val canBuildClayRobot = env.canBuildClayRobot(blueprint)
      val canBuildObsidianRobot = env.canBuildObsidianRobot(blueprint)
      val canBuildGeodeRobot = env.canBuildGeodeRobot(blueprint)

      val requiredObsidianRobots = maxObsidianRobots - env.obsidianRobotCount
      val geodeRobotCountPossible =
          min(
              (env.oreCount + timeLeft * env.oreRobotCount - obsidianRobotCostOre) /
                  geodeRobotCostOre,
              timeLeft - 1)
      val obsidianRobotCountPossible =
          min(
              (env.oreCount + timeLeft * env.oreRobotCount - clayRobotCostOre) /
                  obsidianRobotCostOre,
              requiredObsidianRobots)

      val needsObsidian =
          env.obsidianCount + timeLeft * env.obsidianRobotCount <
              geodeRobotCountPossible * geodeRobotCostObsidian
      val needsClay =
          needsObsidian &&
              env.clayCount + obsidianRobotCountPossible * env.clayRobotCount <
                  obsidianRobotCountPossible * obsidianRobotCostClay

      val obsidianRobotsReached =
          env.obsidianRobotCount == maxObsidianRobots || !needsObsidian || timeLeft <= 2
      val clayRobotsReached =
          env.clayRobotCount == maxClayRobots ||
              obsidianRobotsReached ||
              !needsClay ||
              timeLeft <= 3

      val notNeedsOre =
          obsidianRobotsReached &&
              env.oreCount + timeLeft * env.oreRobotCount - oreRobotCostOre + (timeLeft - 1) >=
                  (timeLeft - 1) * geodeRobotCostOre
      val oreRobotsReached =
          env.oreRobotCount == maxOreRobots ||
              obsidianRobotsReached && env.oreRobotCount == geodeRobotCostOre ||
              clayRobotsReached &&
                  env.oreRobotCount == max(geodeRobotCostOre, obsidianRobotCostOre) ||
              notNeedsOre ||
              timeLeft <= 2 ||
              timeLeft <= 3 && env.oreCount >= geodeRobotCostOre

      if (canBuildOreRobot && !oreRobotsReached) add(env.buildOreRobot(blueprint))
      if (canBuildClayRobot && !clayRobotsReached) add(env.buildClayRobot(blueprint))
      if (canBuildObsidianRobot && !obsidianRobotsReached) add(env.buildObsidianRobot(blueprint))
      if (canBuildGeodeRobot) add(env.buildGeodeRobot(blueprint))

      when {
        canBuildGeodeRobot &&
            (oreRobotsReached || canBuildOreRobot) &&
            (clayRobotsReached || canBuildClayRobot) &&
            (obsidianRobotsReached || canBuildObsidianRobot) -> {}
        env.obsidianRobotCount == 0 &&
            canBuildObsidianRobot &&
            (oreRobotsReached || canBuildOreRobot) &&
            (clayRobotsReached || canBuildClayRobot) -> {}
        env.clayRobotCount == 0 && canBuildClayRobot && (oreRobotsReached || canBuildOreRobot) -> {}
        else -> add(env.buildNoRobot())
      }
    }
    return options.maxOfOrNull { maxGeodeCount(timeLeft - 1, it) } ?: env.geodeCount
  }

  companion object {
    fun parse(input: String): Blueprint {
      val values = linePatternDay19.matchEntire(input)!!.groupValues.drop(1).map { it.toInt() }
      return Blueprint(values[0], values[1], values[2], values[3], values[4], values[5], values[6])
    }
  }
}

private data class Environment(
    val oreCount: Int = 0,
    val clayCount: Int = 0,
    val obsidianCount: Int = 0,
    val geodeCount: Int = 0,
    val oreRobotCount: Int = 1,
    val clayRobotCount: Int = 0,
    val obsidianRobotCount: Int = 0,
    val geodeRobotCount: Int = 0,
) {
  fun buildNoRobot(): Environment =
      copy(
          oreCount = oreCount + oreRobotCount,
          clayCount = clayCount + clayRobotCount,
          obsidianCount = obsidianCount + obsidianRobotCount,
          geodeCount = geodeCount + geodeRobotCount)

  fun buildOreRobot(blueprint: Blueprint): Environment =
      copy(
          oreCount = oreCount + oreRobotCount - blueprint.oreRobotCostOre,
          clayCount = clayCount + clayRobotCount,
          obsidianCount = obsidianCount + obsidianRobotCount,
          geodeCount = geodeCount + geodeRobotCount,
          oreRobotCount = oreRobotCount + 1)

  fun buildClayRobot(blueprint: Blueprint): Environment =
      copy(
          oreCount = oreCount + oreRobotCount - blueprint.clayRobotCostOre,
          clayCount = clayCount + clayRobotCount,
          obsidianCount = obsidianCount + obsidianRobotCount,
          geodeCount = geodeCount + geodeRobotCount,
          clayRobotCount = clayRobotCount + 1)

  fun buildObsidianRobot(blueprint: Blueprint): Environment =
      copy(
          oreCount = oreCount + oreRobotCount - blueprint.obsidianRobotCostOre,
          clayCount = clayCount + clayRobotCount - blueprint.obsidianRobotCostClay,
          obsidianCount = obsidianCount + obsidianRobotCount,
          geodeCount = geodeCount + geodeRobotCount,
          obsidianRobotCount = obsidianRobotCount + 1)

  fun buildGeodeRobot(blueprint: Blueprint): Environment =
      copy(
          oreCount = oreCount + oreRobotCount - blueprint.geodeRobotCostOre,
          clayCount = clayCount + clayRobotCount,
          obsidianCount = obsidianCount + obsidianRobotCount - blueprint.geodeRobotCostObsidian,
          geodeCount = geodeCount + geodeRobotCount,
          geodeRobotCount = geodeRobotCount + 1)

  fun canBuildOreRobot(blueprint: Blueprint) = oreCount >= blueprint.oreRobotCostOre

  fun canBuildClayRobot(blueprint: Blueprint) = oreCount >= blueprint.clayRobotCostOre

  fun canBuildObsidianRobot(blueprint: Blueprint) =
      clayCount >= blueprint.obsidianRobotCostClay && oreCount >= blueprint.obsidianRobotCostOre

  fun canBuildGeodeRobot(blueprint: Blueprint) =
      obsidianCount >= blueprint.geodeRobotCostObsidian && oreCount >= blueprint.geodeRobotCostOre
}
