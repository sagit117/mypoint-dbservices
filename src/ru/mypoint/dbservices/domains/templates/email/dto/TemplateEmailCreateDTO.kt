package ru.mypoint.dbservices.domains.templates.email.dto

data class TemplateEmailCreateDTO(
    val name: String,
    val template: String,
    val subject: String = "",
    val altMsgText: String = ""
)
