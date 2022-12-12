import java.io.File
import java.io.IOException
import java.util.concurrent.TimeUnit

private const val WAIT_EXECUTE_COMMAND_TIME = 1000L

object Version {
    val versionCode: Int = 254
    val versionName: String
        get() =
            if (readCurrentBranch().contains("master", ignoreCase = true)) {
                Release.versionName()
            } else {
                Debug.versionName()
            }.also { println("Version name -  $it") }

    object NameSufix {
        const val stage = "stage"
        const val dev = "dev"
    }

    private object Debug : CurrentVersion {
        override val major: Int = 0
        override val minor: Int = 62
        override val patch: Int = 2

        override fun versionName(): String {
            return "${super.versionName()}-${getSufix()}"
        }

        private fun getSufix(): String {
            val branchName = readCurrentBranch()
            val build = versionCode
            return when {
                branchName.contains("develop") -> "beta.$build"
                branchName.contains("release") -> "rc$build"
                branchName.contains("hotfix") -> "rc$build"
                branchName.contains("master") -> "release.$build"
                branchName.startsWith("tags/") -> {
                    when {
                        branchName.contains("beta") -> "beta.$build"
                        branchName.contains("rc") -> "rc.$build"
                        branchName.contains("hotfix") -> "rc.$build"
                        branchName.contains("uat") -> "uat.$build"
                        else -> "alpha.$build"
                    }
                }

                else -> "alpha.$build"
            }
        }
    }

    private object Release : CurrentVersion {
        override val major: Int = 1
        override val minor: Int = 2
        override val patch: Int = 2
    }

    private fun readCurrentBranch(): String =
        commandExec("git name-rev --name-only HEAD", File(".")).orEmpty()

    private fun commandExec(command: String, workingDir: File): String? {
        try {
            val proc = ProcessBuilder(*command.split("\\s".toRegex()).toTypedArray())
                .directory(workingDir)
                .redirectOutput(ProcessBuilder.Redirect.PIPE)
                .redirectError(ProcessBuilder.Redirect.PIPE)
                .start()

            proc.waitFor(WAIT_EXECUTE_COMMAND_TIME, TimeUnit.MILLISECONDS)
            return proc.inputStream.bufferedReader().readText()
        } catch (e: IOException) {
            e.printStackTrace()
            return null
        }
    }

    private interface CurrentVersion {
        val major: Int
        val minor: Int
        val patch: Int
        fun versionName(): String =
            "$major.$minor.$patch"
    }
}
