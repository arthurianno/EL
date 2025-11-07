import org.gradle.api.Project
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.charset.StandardCharsets
import java.util.Properties

fun Project.getTagInfo(): GitTag {
    // Пытаемся получить текущий Git-тег
    val gitTag = executeInShell("git describe --tags --exact-match 2>/dev/null")?.trim()

    return if (!gitTag.isNullOrBlank()) {
        println("Using Git tag: $gitTag")
        GitTag.fromString(gitTag)
    } else {
        // Fallback: пытаемся прочитать из version.properties
        println("No Git tag found, trying version.properties")
        getVersionFromProperties() ?: run {
            // Определяем buildType из gradle task для hardcoded fallback
            val taskNames = gradle.startParameter.taskNames.joinToString(" ").lowercase()
            val buildType = when {
                taskNames.contains("huawei") -> "huawei"
                taskNames.contains("release") -> "release"
                taskNames.contains("debug") -> "debug"
                else -> "debug"
            }
            println("Using hardcoded fallback version with buildType: $buildType")
            GitTag.fromString("v2.10.5.395-$buildType")
        }
    }
}

/**
 * Читает версию из version.properties файла
 * Возвращает null если файл не найден или некорректен
 */
fun Project.getVersionFromProperties(): GitTag? {
    val versionFile = File(rootProject.projectDir, "version.properties")

    if (!versionFile.exists()) {
        println("version.properties not found")
        return null
    }

    return try {
        val props = Properties()
        versionFile.inputStream().use { props.load(it) }

        val major = props.getProperty("major", "2")
        val minor = props.getProperty("minor", "10")
        val patch = props.getProperty("patch", "5")
        val build = props.getProperty("build", "395")

        // Определяем buildType из gradle task
        val taskNames = gradle.startParameter.taskNames.joinToString(" ").lowercase()
        val buildType = when {
            taskNames.contains("huawei") -> "huawei"
            taskNames.contains("release") -> "release"
            taskNames.contains("debug") -> "debug"
            else -> "debug" // По умолчанию debug для разработки
        }

        val versionString = "v$major.$minor.$patch.$build-$buildType"
        println("Version from version.properties: $versionString (detected buildType: $buildType)")
        GitTag.fromString(versionString)
    } catch (e: Exception) {
        println("Error reading version.properties: ${e.message}")
        null
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
        println("Doesn't work in Windows")
        return null
    }

    val stdout = ByteArrayOutputStream()
    try {
        exec {
            commandLine("bash", "-c", command)
            standardOutput = stdout
            isIgnoreExitValue = true // Игнорируем ненулевой код выхода
        }
        return stdout.toString(StandardCharsets.UTF_8.name()).trim().takeIf { it.isNotBlank() }
    } catch (e: Exception) {
        println("Error executing command: $command, error: ${e.message}")
        return null
    }
}