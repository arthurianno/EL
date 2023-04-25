import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.util.Properties
import java.util.concurrent.TimeUnit

// Формат номера версии
// develop version name 1.2.3.35-stage(123)
// reseale version name 1.2.3

private const val WAIT_EXECUTE_COMMAND_TIME = 1000L
private const val VERSION_PROPERTIES_FILE = "./version.properties"
private const val MAJOR = "major"
private const val MINOR = "minor"
private const val PATCH = "patch"
private const val DEVELOP = "develop"
private const val BUILD_NUMBER = "build"

object Version {
    private val versionProperties = Properties().apply {
        load(FileInputStream(VERSION_PROPERTIES_FILE))
    }
    private val major: Int by lazy { versionProperties[MAJOR].toString().toInt() }
    private val minor: Int by lazy { versionProperties[MINOR].toString().toInt() }
    private val patch: Int by lazy { versionProperties[PATCH].toString().toInt() }
    private val developCode: Int
        get() = versionProperties[DEVELOP].toString().toInt()
    val versionCode: Int
        get() = versionProperties[BUILD_NUMBER].toString().toInt()
    val versionName: String
        get() = "$major.$minor.$patch"

    val prodNameSuffix: String = getDebugSuffix(BackendVariant.prod)
    val stageNameSuffix: String = getDebugSuffix(BackendVariant.stage)
    val devNameSuffix: String = getDebugSuffix(BackendVariant.dev)
    private fun getDebugSuffix(equipement: BackendVariant): String =
        ".$developCode-${equipement.name}($versionCode)"

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
}
