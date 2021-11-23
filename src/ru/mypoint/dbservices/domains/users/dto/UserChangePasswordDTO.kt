package ru.mypoint.dbservices.domains.users.dto

/** класс для принятия данных об изменение пароля */
data class UserChangePasswordDTO(val email: String, val newPassword: String)
