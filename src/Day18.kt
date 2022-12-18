import kotlin.test.assertEquals

fun main() {
  fun part1(input: List<String>): Int {
    val lavaDroplet = LavaDroplet.parse(input)
    return lavaDroplet.surfaceArea()
  }

  fun part2(input: List<String>): Int {
    val lavaDroplet = LavaDroplet.parse(input)
    return lavaDroplet.surfaceAreaAccountingAirPockets()
  }

  // test if implementation meets criteria from the description, like:
  val testInput = readInput("Day18_test")
  assertEquals(64, part1(testInput))
  assertEquals(58, part2(testInput))

  val input = readInput("Day18")
  println(part1(input))
  println(part2(input))
}

class LavaDroplet(private val shape: Array<Array<BooleanArray>>) {
  fun surfaceArea(): Int {
    var surfaceArea = 0
    val maxXYZ = shape.lastIndex
    for (x in shape.indices) {
      for (y in shape.indices) {
        for (z in shape.indices) {
          if (!shape[x][y][z]) continue
          if (x == 0 || !shape[x - 1][y][z]) surfaceArea++
          if (x == maxXYZ || !shape[x + 1][y][z]) surfaceArea++
          if (y == 0 || !shape[x][y - 1][z]) surfaceArea++
          if (y == maxXYZ || !shape[x][y + 1][z]) surfaceArea++
          if (z == 0 || !shape[x][y][z - 1]) surfaceArea++
          if (z == maxXYZ || !shape[x][y][z + 1]) surfaceArea++
        }
      }
    }
    return surfaceArea
  }

  fun surfaceAreaAccountingAirPockets(): Int {
    var surfaceArea = 0
    val maxXYZ = shape.lastIndex
    for (x in shape.indices) {
      for (y in shape.indices) {
        for (z in shape.indices) {
          if (!shape[x][y][z]) continue
          if (x == 0 || isAir(x - 1, y, z)) surfaceArea++
          if (x == maxXYZ || isAir(x + 1, y, z)) surfaceArea++
          if (y == 0 || isAir(x, y - 1, z)) surfaceArea++
          if (y == maxXYZ || isAir(x, y + 1, z)) surfaceArea++
          if (z == 0 || isAir(x, y, z - 1)) surfaceArea++
          if (z == maxXYZ || isAir(x, y, z + 1)) surfaceArea++
        }
      }
    }
    return surfaceArea
  }

  /** @return true if cube at x,y,z is air but not an air pocket */
  private fun isAir(
      x: Int,
      y: Int,
      z: Int,
      checkedCubes: MutableSet<Cube> = mutableSetOf()
  ): Boolean {
    checkedCubes.add(Cube(x, y, z))
    if (shape[x][y][z]) return false

    val maxXYZ = shape.lastIndex
    if (x == 0 || x == maxXYZ || y == 0 || y == maxXYZ || z == 0 || z == maxXYZ) return true

    return listOf(
            Cube(x - 1, y, z),
            Cube(x + 1, y, z),
            Cube(x, y - 1, z),
            Cube(x, y + 1, z),
            Cube(x, y, z - 1),
            Cube(x, y, z + 1),
        )
        .filter { it !in checkedCubes }
        .any { isAir(it.x, it.y, it.z, checkedCubes) }
  }

  companion object {
    fun parse(input: List<String>): LavaDroplet {
      val maxCoordinate = input.flatMap { it.split(",") }.maxOf { it.toInt() }
      val shape =
          Array(maxCoordinate + 1) { Array(maxCoordinate + 1) { BooleanArray(maxCoordinate + 1) } }
      val cubes =
          input.map {
            val (x, y, z) = it.split(",")
            Cube(x.toInt(), y.toInt(), z.toInt())
          }
      for (cube in cubes) {
        shape[cube.x][cube.y][cube.z] = true
      }
      return LavaDroplet(shape)
    }
  }
}

data class Cube(val x: Int, val y: Int, val z: Int)
