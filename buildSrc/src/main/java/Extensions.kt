import org.gradle.api.artifacts.dsl.DependencyHandler

fun DependencyHandler.moduleBaseDependencies() {
    addImplementation(Dependencies.Jetpack.Compose.runtime)
    addImplementation(Dependencies.Kotlin.coroutines)
    addImplementation(Dependencies.Jetpack.viewModel)
    addImplementation(Dependencies.Jetpack.Compose.Voyager.core)
    addImplementation(Dependencies.KoDeIn.core)
}

fun DependencyHandler.applicationBaseDependencies() {
    addImplementation(Dependencies.Jetpack.core)
    addImplementation(Dependencies.Jetpack.viewModel)
    addImplementation(Dependencies.Jetpack.Compose.ui)
    addImplementation(Dependencies.Jetpack.Compose.material)
    addImplementation(Dependencies.Jetpack.Compose.uiToolingPreview)
    addImplementation(Dependencies.Jetpack.Compose.activity)

    addImplementation(Dependencies.Jetpack.Compose.Voyager.core)
    addImplementation(Dependencies.KoDeIn.jetpackCompose)

    addTestImplementation(Dependencies.Test.junit)
    addAndroidTestImplementation(Dependencies.Test.junitExt)
    addAndroidTestImplementation(Dependencies.Test.espresso)
    addAndroidTestImplementation(Dependencies.Test.composeUi)
    addDebugImplementation(Dependencies.Jetpack.Compose.uiTooling)
    addDebugImplementation(Dependencies.Test.composeUiTestManifest)
}

fun DependencyHandler.addImplementation(dependency: String) {
    add("implementation", dependency)
}

fun DependencyHandler.addTestImplementation(dependency: String) {
    add("testImplementation", dependency)
}

fun DependencyHandler.addAndroidTestImplementation(dependency: String) {
    add("androidTestImplementation", dependency)
}

fun DependencyHandler.addDebugImplementation(dependency: String) {
    add("debugImplementation", dependency)
}

fun DependencyHandler.addKapt(dependency: String) {
    add("kapt", dependency)
}
