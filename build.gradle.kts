// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
<<<<<<< HEAD
    id("com.android.application") version "8.4.0" apply false
    id("org.jetbrains.kotlin.android") version "2.0.0" apply false
    id("org.jetbrains.kotlin.plugin.compose") version "2.0.0" apply false
}
=======
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
>>>>>>> 875cc9a843b00cb19a93e420b3d465078607f08c
