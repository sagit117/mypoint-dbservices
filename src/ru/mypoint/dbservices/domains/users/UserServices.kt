package ru.mypoint.dbservices.domains.users

import com.mongodb.client.result.InsertOneResult
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.eq
import ru.mypoint.dbservices.domains.users.dto.UserCreateDTO
import ru.mypoint.dbservices.utils.sha256

/**
 * Сервис для работы с репозиторием user
 */
class UserService(private val collection: CoroutineCollection<UserRepository>) {
    suspend fun findOneByEmail(email: String): UserRepository? {
        return collection.findOne(UserRepository::email eq email)
    }

    suspend fun insertOne(userCreateDTO: UserCreateDTO): InsertOneResult {
        return collection.insertOne(UserRepository(
            email = userCreateDTO.email,
            password = userCreateDTO.password.sha256(),
            address = userCreateDTO.address ?: "",
            zipCode = userCreateDTO.zipCode ?: 0,
            fullName = userCreateDTO.fullName ?: "",
            isBlocked = userCreateDTO.isBlocked ?: false,
            isConfirmEmail = userCreateDTO.isConfirmEmail ?: false,
            isNeedsPassword = userCreateDTO.isNeedsPassword ?: false,
        ))
    }
}