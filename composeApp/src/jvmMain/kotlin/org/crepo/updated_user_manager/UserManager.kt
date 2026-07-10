package org.crepo.updated_user_manager

import java.io.File

data class User(
    val name: String,
    val fullName: String,
    val comment: String,
    val lastLogon: String,
    val status: String
)

object UserManager {
    fun getAllUsers(): List<User> {
        val process = ProcessBuilder("net", "user").start()
        val exitCode = process.waitFor()

        if (exitCode != 0) return emptyList()

        val output = process.inputStream.bufferedReader().readText()
        return parseUserList(output)
    }

    private fun parseUserList(output: String): List<User> {
        val users = mutableListOf<User>()
        var isUserSection = false

        output.lines().forEach { line ->
            if (line.contains("----------")) {
                isUserSection = true
                return@forEach
            }

            if (isUserSection && line.isNotBlank() && !line.contains("----------")) {
                line.split("\\s+".toRegex())
                    .filter { it.isNotBlank() }
                    .forEach { username ->
                        users.add(User(
                            name = username,
                            fullName = "",
                            comment = "",
                            lastLogon = "",
                            status = "Active"
                        ))
                    }
            }
        }

        return users
    }

    fun exportUsersToCsv(): String {
        val users = getAllUsers()
        return users.joinToString("\n") { user ->
            "\"${user.name}\",\"${user.fullName}\",\"${user.comment}\",\"${user.lastLogon}\",\"${user.status}\""
        }
    }
}