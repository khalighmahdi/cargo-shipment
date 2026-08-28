plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.kotlin.compose)
  alias(libs.plugins.ksp)
  alias(libs.plugins.roborazzi)
  alias(libs.plugins.secrets)
}

apply(plugin = "org.jetbrains.kotlin.plugin.compose")

android {
  namespace = "com.example"
  compileSdk = 35

  defaultConfig {
    applicationId = "com.aistudio.cargoshipment.kzpwhq"
    minSdk = 24
    targetSdk = 35
    versionCode = 5
    versionName = "1.4"
    versionName = "1.3"
    versionName = "1.2"
    versionName = "1.1"
    versionName = "1.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    release {
      isCrunchPngs = false
      isMinifyEnabled = false
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }
  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }
  buildFeatures {
    compose = true
    buildConfig = true
  }
  testOptions { unitTests { isIncludeAndroidResources = true } }
  packaging {
    resources.excludes.addAll(
      listOf(
        "META-INF/INDEX.LIST",
        "META-INF/DEPENDENCIES",
        "META-INF/LICENSE",
        "META-INF/LICENSE.txt",
        "META-INF/NOTICE",
        "META-INF/NOTICE.txt",
        "META-INF/NOTICE.md",
        "META-INF/LICENSE.md",
        "META-INF/io.netty.versions.properties",
        "META-INF/io.netty.versions"
      )
    )
  }
}

secrets {
  propertiesFileName = ".env"
  defaultPropertiesFileName = ".env.example"
}

dependencies {
  implementation(platform(libs.androidx.compose.bom))
  implementation(libs.accompanist.permissions)
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.compose.material.icons.core)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.graphics)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.navigation.compose)
  implementation(libs.androidx.room.ktx)
  implementation(libs.androidx.room.runtime)

  // Ktor for Local Network Sharing
  val ktor_version = "2.3.11"
  implementation("io.ktor:ktor-server-core:$ktor_version")
  implementation("io.ktor:ktor-server-netty:$ktor_version")
  implementation("io.ktor:ktor-server-html-builder:$ktor_version")
  implementation("io.ktor:ktor-server-content-negotiation:$ktor_version")
  implementation("io.ktor:ktor-serialization-kotlinx-json:$ktor_version")

  // CSV export
  implementation("com.opencsv:opencsv:5.9")

  implementation(libs.coil.compose)
  implementation(libs.converter.moshi)
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.core)
  implementation(libs.logging.interceptor)
  implementation(libs.moshi.kotlin)
  implementation(libs.okhttp)
  implementation(libs.retrofit)
  testImplementation(libs.androidx.compose.ui.test.junit4)
  testImplementation(libs.androidx.core)
  testImplementation(libs.androidx.junit)
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
  testImplementation(libs.robolectric)
  testImplementation(libs.roborazzi)
  testImplementation(libs.roborazzi.compose)
  testImplementation(libs.roborazzi.junit.rule)
  androidTestImplementation(platform(libs.androidx.compose.bom))
  androidTestImplementation(libs.androidx.compose.ui.test.junit4)
  androidTestImplementation(libs.androidx.espresso.core)
  androidTestImplementation(libs.androidx.junit)
  androidTestImplementation(libs.androidx.runner)
  debugImplementation(libs.androidx.compose.ui.test.manifest)
  debugImplementation(libs.androidx.compose.ui.tooling)
  ksp(libs.androidx.room.compiler)
  ksp(libs.moshi.kotlin.codegen)
}
