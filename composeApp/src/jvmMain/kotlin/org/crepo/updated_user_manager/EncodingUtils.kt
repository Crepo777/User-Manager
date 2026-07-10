package org.crepo.updated_user_manager

import java.nio.charset.Charset
object EncodingUtils {
    fun getConsoleOutput(process: Process): String {
        return process.inputStream.bufferedReader(Charset.forName("windows-1251")).readText()
    }
}