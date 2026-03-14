import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.compose.desktop.application.dsl.WindowsPlatformSettings

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
}



kotlin {
    jvm()

    sourceSets {
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation("cafe.adriel.voyager:voyager-navigator:1.0.0")
            implementation("cafe.adriel.voyager:voyager-screenmodel:1.0.0")
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
        }
    }
}

//tasks.register<Jar>("createJar") {
//    archiveBaseName.set("UserManager")
//    //from(configurations.runtimeClasspath.get().resolvedConfiguration.files.map { if (it.isDirectory) it else zipTree(it) })
//    from(configurations.runtimeClasspath.get().resolvedConfiguration.files.map { if (it.isDirectory) it else zipTree(it) })
//    into("lib")
//    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
//    manifest {
//        attributes["Main-Class"] = "com.crepo.usermanager.MainKt"
//    }
//}

compose.desktop {
    application {
        mainClass = "org.crepo.updated_user_manager.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Exe)
            //targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.crepo.updated_user_manager"
            packageVersion = "0.1.0"

            windows {
                menuGroup = "User Manager"
                upgradeUuid = "c20bdfc7-d38a-4161-a93f-31c169c18a9e"
                //exePackageIcon = null  // Можно добавить иконку (путь к .ico)
            }
        }
    }
}

