package ru.mypoint.dbservices.domains.users

import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.eq

/**
 * Сервис для работы с репозиторием user
 */
class UserService(private val collection: CoroutineCollection<UserRepository>) {
    private suspend fun findOneByEmail(email: String): UserRepository? {
        return collection.findOne(UserRepository::email eq email)
    }
}