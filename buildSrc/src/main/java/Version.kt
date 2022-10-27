import org.gradle.initialization.Environment.Properties
import java.io.File

object Version {
    private const val GLOBAL_CONFIGURATION_PROPERTIES = "configuration.properties"
    private const val GLOBAL_CONFIGURATION_ARCHIVE_NAME = "archive.name"

    val versionCode: Int
        get() {
            return 1
        }

    val versionName: String
        get() {
            return "1.0.0"
        }
}