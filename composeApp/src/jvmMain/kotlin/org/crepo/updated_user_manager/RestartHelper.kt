package org.crepo.updated_user_manager

import java.awt.Desktop
import java.io.File
import java.nio.file.Paths

object RestartHelper {
    fun restart() {
        val currentJar = Paths.get(System.getProperty("java.class.path")).toAbsolutePath()
        val javaExecutable = Paths.get(System.getProperty("java.home"), "bin", "java.exe")

        val jarPath = if (currentJar.fileName.toString().endsWith(".jar")) {
            currentJar
        } else {
            val buildLibs = Paths.get("build", "libs")
            val jarFiles = buildLibs.toFile().listFiles { _, name -> name.endsWith(".jar") }
            jarFiles?.firstOrNull()?.toPath() ?: currentJar
        }

        if (!jarPath.toFile().exists()) {
            println("❌ Jar not found: $jarPath")
            return
        }

        try {
            val processBuilder = ProcessBuilder(
                javaExecutable.toString(),
                "-jar",
                jarPath.toString()
            )
            processBuilder.start()
            System.exit(0)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}