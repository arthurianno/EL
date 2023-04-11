import org.gradle.api.JavaVersion

object AppConfig {
    const val applicationId = "com.elta.android"
    const val minSdk = 28
    const val completeSdk = 33
    const val targetSdk = 32
    const val jvmTarget = "11"
    val javaVersion = JavaVersion.VERSION_11

    object DeppLink {
        const val host = "stage2.vdiabete.com"
        const val schema = "elta"
    }

    object LogEnabled {
        const val debug = true
        const val release = true
    }
}

enum class BackendVariant(val path: String) {
    prod("https://vdiabete.com"),
    stage("https://stage2.vdiabete.com"),
    dev("https://dev.vdiabete.com")
}

object Module {
    const val core_hardware = ":core-hardware"
    const val core_all = ":core-all"
    const val core_rx_location = ":core-rx-location"
    const val core_adapter = ":core-adapter"
    const val core_adapter_ktx = ":core-adapter-ktx"
    const val core_rx_contacts = ":core-rx-contacts"
    const val core_rx = ":core-rx"
    const val core_collections = ":core-collections"
    const val core_preferences = ":core-preferences"
    const val core_common = ":core-common"
    const val core_interactor = ":core-interactor"
    const val core_security = ":core-security"
    const val core_intents = ":core-intents"
    const val core_date = ":core-date"
    const val core_resources = ":core-resources"
    const val core_ui = ":core-ui"
    const val core_font = ":core-font"
    const val app = ":app"
    const val presentation = ":presentation"
    const val domain = ":domain"
    const val data = ":data"
    const val common = ":common"
}
