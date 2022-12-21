package day21

import kotlin.test.assertEquals
import readInput

fun main() {
  fun part1(input: List<String>): Long {
    return Expressions(input).root().value()
  }

  fun part2(input: List<String>): Long {
    return Expressions(input).resolveHumn()
  }

  // test if implementation meets criteria from the description, like:
  val testInput = readInput("Day21_test")
  assertEquals(152, part1(testInput))
  assertEquals(301, part2(testInput))

  val input = readInput("Day21")
  println(part1(input))
  println(part2(input))
}

val mathOperationLinePattern = Regex("(\\w{4}): (\\w{4}) ([+\\-*/]) (\\w{4})")
val specificNumberLinePattern = Regex("(\\w{4}): (\\d+)")

typealias VariableName = String

class Expressions(input: List<String>) {
  private val references = mutableMapOf<VariableName, Reference>()
  init {
    input.forEach { parse(it) }
  }

  fun root(): MathOperation = references["root"]!!.actualVariable as MathOperation

  fun resolveHumn(): Long {
    root().operator = "compare"
    references.remove("humn")
    return resolve("humn").value()
  }

  private fun resolve(name: VariableName): Variable {
    if (references.containsKey(name) && references[name]!!.actualVariable is Value)
        return references[name]!!

    references.remove(name)
    val referenceToOperation = references.values.single { it.canResolve(name) }

    if (referenceToOperation.name == name) return referenceToOperation

    val operation = referenceToOperation.actualVariable as MathOperation
    return if (operation.variable1.name == name)
        when (operation.operator) {
          "+" -> MathOperation(name, resolve(operation.name), operation.variable2, "-")
          "-" -> MathOperation(name, resolve(operation.name), operation.variable2, "+")
          "*" -> MathOperation(name, resolve(operation.name), operation.variable2, "/")
          "/" -> MathOperation(name, resolve(operation.name), operation.variable2, "*")
          "compare" -> operation.variable2
          else -> error("unknown operator, should not happen")
        }
    else
        when (operation.operator) {
          "+" -> MathOperation(name, resolve(operation.name), operation.variable1, "-")
          "-" -> MathOperation(name, operation.variable1, resolve(operation.name), "-")
          "*" -> MathOperation(name, resolve(operation.name), operation.variable1, "/")
          "/" -> MathOperation(name, operation.variable1, resolve(operation.name), "/")
          "compare" -> operation.variable1
          else -> error("unknown operator, should not happen")
        }
  }

  private fun parse(input: String): Variable {
    return if (mathOperationLinePattern.matches(input)) {
      val (name, nameVariable1, operator, nameVariable2) =
          mathOperationLinePattern.matchEntire(input)!!.groupValues.drop(1)
      MathOperation(
              name,
              references.getOrPut(nameVariable1) { Reference(nameVariable1) },
              references.getOrPut(nameVariable2) { Reference(nameVariable2) },
              operator)
          .also { references.getOrPut(name) { Reference(name) }.actualVariable = it }
    } else {
      val (name, number) = specificNumberLinePattern.matchEntire(input)!!.groupValues.drop(1)
      Value(name, number.toLong()).also {
        references.getOrPut(name) { Reference(name) }.actualVariable = it
      }
    }
  }
}

sealed interface Variable {

  val name: VariableName
  fun value(): Long

  fun canResolve(name: VariableName): Boolean
}

class Value(override val name: VariableName, val number: Long) : Variable {
  override fun value(): Long = number
  override fun canResolve(name: VariableName) = this.name == name
}

class MathOperation(
    override val name: VariableName,
    val variable1: Variable,
    val variable2: Variable,
    var operator: String
) : Variable {
  override fun value(): Long {
    return when (operator) {
      "+" -> variable1.value() + variable2.value()
      "-" -> variable1.value() - variable2.value()
      "*" -> variable1.value() * variable2.value()
      "/" -> variable1.value() / variable2.value()
      "compare" -> variable1.value().compareTo(variable2.value()).toLong()
      else -> error("unknown operator, should not happen")
    }
  }

  override fun canResolve(name: VariableName) =
      variable1.name == name || variable2.name == name || this.name == name
}

class Reference(override val name: VariableName) : Variable {
  lateinit var actualVariable: Variable
  override fun value() = actualVariable.value()
  override fun canResolve(name: VariableName) = actualVariable.canResolve(name)
}
