import org.gradle.api.Project
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets

fun Project.getTagInfo(): GitTag {
        val tag = executeInShell("echo \$CI_COMMIT_TAG").takeIf { !it.isNullOrBlank() } ?: GitTag.DEFAULT
        return GitTag.fromString(tag)
    }

fun Project.getPropertyFromAnywhere(propertyName: String, defaultValue: String): String {
    return when {
        rootProject.hasProperty(propertyName) -> rootProject.property(propertyName) as String
        System.getProperties().contains(propertyName) -> System.getProperty(propertyName, defaultValue)
        else -> System.getenv(propertyName) ?: defaultValue
    }
}

fun Project.executeInShell(command: String): String? {

    if (System.getProperty("os.name").lowercase().contains("windows")) {
        print("Doesn't work in Windows")
        return null
    }

    val stdout = ByteArrayOutputStream()

    exec {
        commandLine("bash", "-c", command)
        commandLine
        standardOutput = stdout
    }

    return stdout.toString(StandardCharsets.UTF_8.name()).trim()
}