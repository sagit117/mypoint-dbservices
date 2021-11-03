package ru.mypoint.dbservices.domains.users.dto

data class UserChangeDataDTO(
    val email: String,
    val fullName: String? = "",
    val zipCode: Int? = 0,
    val address: String? = "",
)