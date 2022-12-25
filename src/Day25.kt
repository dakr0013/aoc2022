import java.lang.StringBuilder
import kotlin.math.pow
import kotlin.test.assertEquals

fun main() {
  fun part1(input: List<String>): SNAFU {
    return input.sumOf { SNAFU(it).toLong() }.toSNAFU()
  }

  val decimalSnafuPairs =
      mapOf(
          1 to "1",
          2 to "2",
          3 to "1=",
          4 to "1-",
          5 to "10",
          6 to "11",
          7 to "12",
          8 to "2=",
          9 to "2-",
          10 to "20",
          15 to "1=0",
          20 to "1-0",
          2022 to "1=11-2",
          12345 to "1-0---0",
          314159265 to "1121-1110-1=0",
      )

  // test snafu to decimal and decimal to snafu conversion
  for (decimalSnafuPair in decimalSnafuPairs) {
    val decimal = decimalSnafuPair.key.toLong()
    val snafu = SNAFU(decimalSnafuPair.value)
    assertEquals(decimal, snafu.toLong())
    assertEquals(snafu, decimal.toSNAFU())
  }

  // test if implementation meets criteria from the description, like:
  val testInput = readInput("Day25_test")
  assertEquals(SNAFU("2=-1=0"), part1(testInput))

  val input = readInput("Day25")
  println(part1(input))
}

data class SNAFU(val value: String) {
  fun toLong(): Long {
    return value
        .reversed()
        .mapIndexed { index, c ->
          5.0.pow(index).toLong() *
              when (c) {
                '=' -> -2
                '-' -> -1
                '0' -> 0
                '1' -> 1
                '2' -> 2
                else -> error("unknown SNAFU digit '$c', should not happen")
              }
        }
        .sum()
  }

  override fun toString() = value
}

fun Long.toSNAFU(): SNAFU {
  val snafuValue = StringBuilder()
  var quotient = this
  var carry = 0
  while (quotient != 0L) {
    val remainder = quotient.mod(5)
    when (remainder + carry) {
      0 -> {
        snafuValue.insert(0, "0")
        carry = 0
      }
      1 -> {
        snafuValue.insert(0, "1")
        carry = 0
      }
      2 -> {
        snafuValue.insert(0, "2")
        carry = 0
      }
      3 -> {
        snafuValue.insert(0, "=")
        carry = 1
      }
      4 -> {
        snafuValue.insert(0, "-")
        carry = 1
      }
      5 -> {
        snafuValue.insert(0, "0")
        carry = 1
      }
    }
    quotient /= 5
  }
  if (carry != 0) {
    snafuValue.insert(0, carry)
  }
  return SNAFU(snafuValue.toString())
}
