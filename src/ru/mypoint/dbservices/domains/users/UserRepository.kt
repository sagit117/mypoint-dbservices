package ru.mypoint.dbservices.domains.users

/**
 * Класс для хранилища пользователей
 */
data class UserRepository(
    val email: String,
    val password: String
)