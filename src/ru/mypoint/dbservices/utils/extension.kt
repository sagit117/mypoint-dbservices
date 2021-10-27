package ru.mypoint.dbservices.utils

import java.security.MessageDigest

/**
 * Функции расширения
 */

/**
 * Закодировать строку в sha256
 */
fun hash(input: String): String {
    val bytes = input.toByteArray()
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(bytes)
    return digest.fold("") { str, it -> str + "%02x".format(it) }
}

fun String.sha256(): String {
    return hash(this)
}