import java.io.File
import kotlin.random.Random
import kotlin.system.exitProcess

const val nanosecondInSec = 1_000_000_000L
const val resetColor = "\u001b[0m"

fun main(args: Array<String>) {
    checkFileExistence(args)
    checkFileContents(args)
    playGame(args)
}

fun bgColor(n: Int) = "\u001b[48;5;${n}m"

fun checkFileExistence(args: Array<String>) {
    when {
        args.size != 2 -> {
            println("Error: Wrong number of arguments.")
            exitProcess(-1)
        }

        !File(args[0]).exists() -> {
            println("Error: The words file ${File(args[0]).name} doesn't exist.")
            exitProcess(-1)
        }

        !File(args[1]).exists() -> {
            println("Error: The candidate words file ${File(args[1]).name} doesn't exist.")
            exitProcess(-1)
        }
    }
}

fun checkFileContents(args: Array<String>) {
    val wordsFileList = File(args[0]).readLines().map { it.lowercase() }
    val candidatesFileList = File(args[1]).readLines().map { it.lowercase() }
    val wordsFileName = File(args[0]).name
    val candidatesFileName = File(args[1]).name
    val invalidWordsInWordsFileSize = getInvalidWords(wordsFileList)
    val invalidWordsInCandidateFileSize = getInvalidWords(candidatesFileList)
    val notIncludedCandidateWordsCount = candidatesFileList.filter { !wordsFileList.contains(it) }.size

    when {
        invalidWordsInWordsFileSize > 0 -> {
            println("Error: $invalidWordsInWordsFileSize invalid words were found in the $wordsFileName file.")
            exitProcess(-1)
        }

        invalidWordsInCandidateFileSize > 0 -> {
            println("Error: $invalidWordsInCandidateFileSize invalid words were found in the $candidatesFileName file.")
            exitProcess(-1)
        }

        notIncludedCandidateWordsCount > 0 -> {
            println("Error: $notIncludedCandidateWordsCount candidate words are not included in the $wordsFileName file.")
            exitProcess(-1)
        }
    }
}

fun getInvalidWords(strings: List<String>): Int {
    return strings.filter {
        it.length != 5 || !it.matches(Regex("[a-z]+")) || it.toSet().size < it.length
    }.size
}

fun checkInputLetter(letter: String, words: List<String>): Boolean {
    return when {
        letter.length != 5 -> {
            println("The input isn't a 5-letter word.")
            false
        }

        !letter.matches(Regex("[a-zA-Z]+")) -> {
            println("One or more letters of the input aren't valid.")
            false
        }

        letter.toSet().size < letter.length -> {
            println("The input has duplicate letters.")
            false
        }

        !words.contains(letter) -> {
            println("The input word isn't included in my words list.")
            false
        }

        else -> true
    }
}

fun playGame(args: Array<String>) {
    val start = System.nanoTime()

    var attempt = 1
    var durationTime: Long = 0

    val wordsFileList = File(args[0]).readLines().map { it.lowercase() }
    val candidatesFileList = File(args[1]).readLines().map { it.lowercase() }
    val secretWord = candidatesFileList[Random.nextInt(0, candidatesFileList.size)]

    println("Secret word is $secretWord")

    var correctCharacters = MutableList(secretWord.length) { "" }
    val clueSet = emptySet<String>().toMutableSet()
    val incorrectCharacters = StringBuilder()

    println("Words Virtuoso")
    while (true) {

        println("Input a 5-letter word:")
        val guessWord = readln().lowercase()

        when (guessWord) {
            "exit" -> {
                println("\nThe game is over.")
                exitProcess(0)
            }

            secretWord -> {
                clueSet.forEach(::println)
                println()
//                println(bgColor(20) + guessWord.uppercase() + resetColor)
                guessWord.map { bgColor(10) + it.uppercase() + resetColor }.joinToString("").also(::println)
                println()
                println("Correct!")
                return when (attempt) {
                    1 -> println("Amazing luck! The solution was found at once.")
                    else -> println("The solution was found after $attempt tries in $durationTime seconds.\n")
                }
            }
        }

        while (checkInputLetter(guessWord, wordsFileList)) {
            for (i in guessWord.indices) {
                when {
                    secretWord[i] == guessWord[i] -> {
                        correctCharacters[i] = bgColor(10) + guessWord[i].uppercaseChar() + resetColor
                    }

                    secretWord.contains(guessWord[i]) -> {
                        correctCharacters[i] = bgColor(11) + guessWord[i].uppercaseChar() + resetColor
                    }

                    else -> {
                        correctCharacters[i] = bgColor(7) + guessWord[i].uppercaseChar() + resetColor
                        incorrectCharacters.append(guessWord[i].uppercaseChar())
                    }

                }
            }
            clueSet.add(correctCharacters.joinToString(""))
            println()
            clueSet.forEach(::println)
            correctCharacters = MutableList(secretWord.length) { "" }
            println()
            println(bgColor(14) + incorrectCharacters.toSortedSet().joinToString("") + resetColor)
            println()
            break
        }

        attempt++

        val end = System.nanoTime()
        durationTime = (end - start) / nanosecondInSec
    }
}