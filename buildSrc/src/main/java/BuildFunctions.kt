import org.gradle.api.Project
import java.io.ByteArrayOutputStream
import java.nio.charset.StandardCharsets

fun Project.getTagInfo(): GitTag {
    // Пытаемся получить текущий Git-тег
    val gitTag = executeInShell("git describe --tags --exact-match 2>/dev/null")?.trim()

    return if (!gitTag.isNullOrBlank()) {
        println("Using Git tag: $gitTag")
        GitTag.fromString(gitTag)
    } else {
        // Fallback: используем хардкод или дефолтное значение
        println("No Git tag found, using default version")
        GitTag.fromString("v1.0.0.376-release") // Явно указываем последний тег
    }
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
        standardOutput = stdout
    }

    return stdout.toString(StandardCharsets.UTF_8.name()).trim()
}