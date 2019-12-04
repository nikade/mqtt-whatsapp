package `in`.edak.whatsapp

import com.google.gson.Gson
import org.openqa.selenium.Cookie
import org.openqa.selenium.html5.LocalStorage
import java.io.File
import java.io.FileNotFoundException

class LocalStorageFile(val fileName: String   ) {
    private val gson = Gson().newBuilder().create()

    companion object {
        val KEY_EQ_VALUE_REGEXP = "^(.+?):\\s(.*)$".toRegex()
    }

    fun saveLocalStorage(localStorage: LocalStorage) {
        File(fileName)
            .writeText(
                localStorage.keySet().joinToString("\n") { "${it}: ${localStorage.getItem(it)}" }
            )
    }

    fun loadLocalStorage(localStorage: LocalStorage) {
        try {
            File(fileName).readLines().forEach { line ->
                KEY_EQ_VALUE_REGEXP.find(line)?.let {
                    localStorage.setItem(it.groupValues[1], it.groupValues[2])
                }
            }
        } catch (e: FileNotFoundException) {
        }
    }

    fun saveCookies(cookies: Set<Cookie>) {
        File(fileName)
            .writeText(
                cookies.joinToString("\n") { gson.toJson(it) },
                Charsets.UTF_8
            )
    }

    fun loadCookies(): Set<Cookie> {
        return try {
            val file = File(fileName)
            file.readLines(Charsets.UTF_8).map { jsonLine ->
                gson.fromJson(jsonLine, Cookie::class.java)
            }.toSet()
        } catch (e: FileNotFoundException) {
            setOf<Cookie>()
        }
    }
}
