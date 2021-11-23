package ru.mypoint.dbservices.domains.users.dto

import java.util.regex.Pattern

/**
 * DTO для создания(регистрации) пользователя
 */
data class UserCreateDTO(
    var email: String,
    val password: String,
    val fullName: String? = "",
    val zipCode: Int? = 0,
    val address: String? = "",
    val isBlocked: Boolean? = false,
    val isNeedsPassword: Boolean? = false,
    val isConfirmEmail: Boolean? = false
) {
    init {
        /** валидация email */
        email = email.trim().lowercase()

        if (!isCorrectEmail()) {
            throw IllegalArgumentException("Email required")
        }
    }

    private fun isCorrectEmail(): Boolean {
        val emailPattern = Pattern.compile(
            "[a-zA-Z0-9\\+\\.\\_\\%\\-\\+]{1,256}" +
                    "\\@" +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                    "(" +
                    "\\." +
                    "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
                    ")+"
        )

        return emailPattern.matcher(email).matches()
    }
}