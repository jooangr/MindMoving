// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {
    extra.apply {
        set("room_version", "2.6.0") //   Se define la versión de Room aquí
    }
    val agp_version by extra("8.6.0") //   Se define la versión del Android Gradle Plugin
}

plugins {
    id("com.android.application") version "8.6.0" apply false
    id("com.android.library") version "8.6.0" apply false
    id("org.jetbrains.kotlin.android") version "2.1.0" apply false
}
