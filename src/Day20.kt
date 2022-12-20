import kotlin.test.assertEquals

fun main() {
  fun part1(input: List<String>): Long {
    val numbers = input.mapIndexed { index, s -> Number(index, s.toLong()) }
    val file = GroveCoordinateFile(numbers.toMutableList())
    file.mix()
    return file.sumGroveCoordinates()
  }

  fun part2(input: List<String>): Long {
    val decryptionKey = 811589153
    val numbers = input.mapIndexed { index, s -> Number(index, s.toLong() * decryptionKey) }
    val file = GroveCoordinateFile(numbers.toMutableList())
    repeat(10) { file.mix() }
    return file.sumGroveCoordinates()
  }

  // test if implementation meets criteria from the description, like:
  val testInput = readInput("Day20_test")
  assertEquals(3, part1(testInput))
  assertEquals(1623178306, part2(testInput))

  val input = readInput("Day20")
  println(part1(input))
  println(part2(input))
}

private class GroveCoordinateFile(val numbers: MutableList<Number>) {

  fun mix() {
    val maxIndex = numbers.lastIndex
    for (i in 0..maxIndex) {
      val currentIndex = numbers.indexOfFirst { it.initialIndex == i }
      val currentNumber = numbers.removeAt(currentIndex)
      val newIndex = (currentIndex + currentNumber.value).mod(numbers.size)
      numbers.add(newIndex, currentNumber)
    }
  }

  fun sumGroveCoordinates(): Long {
    val indexOfZero = numbers.indexOfFirst { it.value == 0L }
    val firstIndex = (indexOfZero + 1000).mod(numbers.size)
    val secondIndex = (indexOfZero + 2000).mod(numbers.size)
    val thirdIndex = (indexOfZero + 3000).mod(numbers.size)
    return numbers[firstIndex].value + numbers[secondIndex].value + numbers[thirdIndex].value
  }
}

data class Number(val initialIndex: Int, val value: Long) {
  override fun toString() = value.toString()
}
