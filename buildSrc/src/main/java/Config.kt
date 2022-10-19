import org.gradle.api.JavaVersion

object AppConfig {
    const val applicationId = "ru.marslab.pocketwordtranslator"
    const val minSdk = 26
    const val completeSdk = 32
    const val targetSdk = 32
    const val jvmTarget = "11"
    val javaVersion = JavaVersion.VERSION_11
}

object Releases {
    const val versionCode = 2
    const val versionName = "2.0.0"
}

object Module {
    const val app = ":app"
    const val data = ":data"
    const val domain = ":domain"
    const val presentation = ":presentation"
    const val core = ":core"
}
