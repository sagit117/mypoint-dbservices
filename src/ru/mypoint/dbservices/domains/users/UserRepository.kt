package ru.mypoint.dbservices.domains.users

import org.bson.codecs.pojo.annotations.BsonId
import org.litote.kmongo.Id
import org.litote.kmongo.newId

/**
 * Класс для хранилища пользователей
 */
data class UserRepository(
    @BsonId
    val id: Id<UserRepository> = newId(),
    val email: String,
    val password: String,
    val fullName: String = "",
    val zipCode: Int = 0,
    val address: String = "",
    val isBlocked: Boolean = false,
    val isNeedsPassword: Boolean = false,
    val isConfirmEmail: Boolean = false,
    val dateTimeAtCreation: Long = System.currentTimeMillis(),
)