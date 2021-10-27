package ru.mypoint.dbservices.domains.dto

data class ResponseDTO(val status: String)

enum class ResponseStatus(val value: String) {
    OK("OK"),
    NoValidate("data is not validated")
}