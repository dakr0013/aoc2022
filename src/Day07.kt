import kotlin.test.assertEquals

fun main() {
  fun part1(input: List<String>): Long {
    val commands = parseCommands(input.drop(1))

    val root = Directory("/")
    Terminal(root).execute(commands)

    return root.getAllDirectories().filter { it.size() < 100_000 }.sumOf { it.size() }
  }

  fun part2(input: List<String>): Long {
    val totalAvailableDiskSpace = 70_000_000
    val totalRequiredForUpdate = 30_000_000
    val commands = parseCommands(input.drop(1))

    val root = Directory("/")
    Terminal(root).execute(commands)

    val usedSpace = root.size()
    val availableForUpdate = totalAvailableDiskSpace - usedSpace
    val requiredForUpdate = totalRequiredForUpdate - availableForUpdate
    val eligibleDirectorySizes =
        root.getAllDirectories().map { it.size() }.filter { it >= requiredForUpdate }
    return eligibleDirectorySizes.min()
  }

  // test if implementation meets criteria from the description, like:
  val testInput = readInput("Day07_test")
  assertEquals(95437, part1(testInput))
  assertEquals(24933642, part2(testInput))

  val input = readInput("Day07")
  println(part1(input))
  println(part2(input))
}

private fun parseCommands(input: List<String>): List<Command> {
  val commands = mutableListOf<Command>()
  var i = 0
  while (i < input.size) {
    val currentLine = input[i]
    val words = currentLine.split(" ")
    val command: Command =
        when (words[1]) {
          "ls" -> {
            val files = mutableListOf<AbstractFile>()
            while ((i + 1) < input.size && !input[i + 1].startsWith("$")) {
              i++
              val (size, filename) = input[i].split(" ")
              files.add(if (size == "dir") Directory(filename) else File(filename, size.toLong()))
            }
            LsCommand(files)
          }
          "cd" -> CdCommand(words[2])
          else -> error("unknown command, should not happen")
        }
    commands.add(command)
    i++
  }
  return commands
}

private class Terminal(var pwd: Directory) {
  fun execute(command: Command) {
    when (command) {
      is CdCommand -> pwd = pwd.get(command.targetPath)
      is LsCommand -> pwd.create(command.output)
    }
  }

  fun execute(commands: List<Command>) {
    commands.forEach { execute(it) }
  }
}

private sealed interface Command

private class CdCommand(val targetPath: String) : Command

private class LsCommand(val output: List<AbstractFile>) : Command

private sealed interface AbstractFile {
  val name: String
  fun size(): Long
}

private class Directory(
    override val name: String,
    var childs: MutableList<AbstractFile> = mutableListOf(),
    var parent: Directory? = null
) : AbstractFile {
  override fun size(): Long = childs.sumOf { it.size() }

  fun get(filepath: String): Directory {
    return if (filepath == "..") {
      parent!!
    } else {
      childs.first { it.name == filepath } as Directory
    }
  }

  fun getAllDirectories(): List<Directory> {
    val directories = childs.filterIsInstance<Directory>()
    return directories + directories.flatMap { it.getAllDirectories() }
  }

  fun create(files: List<AbstractFile>) {
    for (file in files) {
      if (!childs.map { it.name }.contains(file.name)) {
        if (file is Directory) {
          file.parent = this
        }
        childs.add(file)
      }
    }
  }
}

private class File(override val name: String, val size: Long) : AbstractFile {
  override fun size(): Long = size
}
