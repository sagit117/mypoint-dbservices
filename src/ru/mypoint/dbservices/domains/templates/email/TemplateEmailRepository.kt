package ru.mypoint.dbservices.domains.templates.email

import org.bson.codecs.pojo.annotations.BsonId
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import ru.mypoint.dbservices.domains.users.UserRepository

data class TemplateEmailRepository(
    @BsonId
    val _id: Id<UserRepository> = newId(),
    val name: String,
    val template: String,
    val subject: String = "",
    val altMsgText: String = "",
    val dateTimeAtCreation: Long = System.currentTimeMillis(),
)
