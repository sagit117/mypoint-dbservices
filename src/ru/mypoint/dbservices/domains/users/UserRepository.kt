package ru.mypoint.dbservices.domains.users

import org.bson.codecs.pojo.annotations.BsonId
import org.litote.kmongo.Id
import org.litote.kmongo.newId
import ru.mypoint.dbservices.utils.randomCode

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
    val roles: MutableList<String> = mutableListOf(),
    val hashCode: String = randomCode(10) // хэш код используется для верификации токена
)