import kotlin.math.min
import kotlin.test.assertEquals

fun main() {
  fun part1(input: List<String>): Int {
    return parsePairsOfPackets(input)
        .mapIndexedNotNull { index, pair ->
          if (pair.isInRightOrder()) {
            index + 1
          } else {
            null
          }
        }
        .sum()
  }

  fun part2(input: List<String>): Int {
    val dividerPackets = listOf("[[2]]", "[[6]]").map { PacketParser(it).parse() }
    val packets = input.filter { it.isNotEmpty() }.map { PacketParser(it).parse() }
    val allPackets = (packets + dividerPackets).sorted()

    val index1 = allPackets.indexOf(dividerPackets.first()) + 1
    val index2 = allPackets.indexOf(dividerPackets.last()) + 1
    return index1 * index2
  }

  // test if implementation meets criteria from the description, like:
  val testInput = readInput("Day13_test")
  assertEquals(13, part1(testInput))
  assertEquals(140, part2(testInput))

  val input = readInput("Day13")
  println(part1(input))
  println(part2(input))
}

private fun parsePairsOfPackets(input: List<String>): List<Pair<Packet, Packet>> {
  return input.chunked(3).map {
    val first = PacketParser(it[0]).parse()
    val second = PacketParser(it[1]).parse()
    first to second
  }
}

private class PacketParser(val input: String) {
  private var index = 1

  fun parse(): Packet {
    val currentNumber = StringBuilder()
    val packetValues = mutableListOf<PacketValue>()
    while (index <= input.lastIndex) {
      val char = input[index++]
      when {
        char.isDigit() -> currentNumber.append(char)
        char == ',' -> {
          if (currentNumber.isNotEmpty()) {
            packetValues.add(IntValue(currentNumber.toString().toInt()))
            currentNumber.clear()
          }
        }
        char == '[' -> packetValues.add(parse())
        char == ']' -> {
          if (currentNumber.isNotEmpty()) {
            packetValues.add(IntValue(currentNumber.toString().toInt()))
            currentNumber.clear()
          }
          return Packet(packetValues)
        }
      }
    }
    error("there was no closing bracket, should not happen")
  }
}

private fun Pair<Packet, Packet>.isInRightOrder(): Boolean = first <= second

private typealias Packet = ListValue

private sealed interface PacketValue : Comparable<PacketValue>

private data class ListValue(val value: List<PacketValue>) : PacketValue {
  constructor(vararg values: PacketValue) : this(values.toList())

  override fun compareTo(other: PacketValue): Int {
    when (other) {
      is IntValue -> return this.compareTo(ListValue(other))
      is ListValue -> {
        for (i in 0..min(value.lastIndex, other.value.lastIndex)) {
          when {
            value[i] < other.value[i] -> return -1
            value[i] > other.value[i] -> return +1
            else -> {
              // continue checking list
            }
          }
        }
        return value.size.compareTo(other.value.size)
      }
    }
  }

  override fun toString() = value.toString()
}

private data class IntValue(val value: Int) : PacketValue {
  override fun compareTo(other: PacketValue): Int {
    return when (other) {
      is IntValue -> value.compareTo(other.value)
      is ListValue -> ListValue(this).compareTo(other)
    }
  }

  override fun toString() = value.toString()
}
