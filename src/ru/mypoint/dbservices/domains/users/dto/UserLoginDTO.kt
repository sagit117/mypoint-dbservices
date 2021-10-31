package ru.mypoint.dbservices.domains.users.dto

/**
 * DTO для авторизации пользователя
 */
data class UserLoginDTO(val email: String, val password: String)