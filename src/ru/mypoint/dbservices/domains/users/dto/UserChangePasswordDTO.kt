package ru.mypoint.dbservices.domains.users.dto

data class UserChangePasswordDTO(val email: String, val newPassword: String)
