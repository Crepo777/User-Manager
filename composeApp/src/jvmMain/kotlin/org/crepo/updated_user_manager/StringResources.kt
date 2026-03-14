package org.crepo.updated_user_manager

import java.util.*
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

object StringResources {
    fun getString(key: String): String {
        val locale = AppLanguage.current
        return try {
            val bundle = ResourceBundle.getBundle("strings.strings", locale, UTF8Control())
            bundle.getString(key)
        } catch (e: Exception) {
            "[MISSING: $key]"
        }
    }

    fun getString(key: String, vararg args: Any): String {
        return getString(key).format(*args)
    }
}

class UTF8Control : ResourceBundle.Control() {
    override fun newBundle(
        baseName: String,
        locale: Locale,
        format: String,
        loader: ClassLoader,
        reload: Boolean
    ): ResourceBundle? {
        val resourceName = toResourceName(toBundleName(baseName, locale), "properties")
        val stream = loader.getResourceAsStream(resourceName) ?: return null
        return InputStreamReader(stream, StandardCharsets.UTF_8).use { reader ->
            PropertyResourceBundle(reader)
        }
    }
}