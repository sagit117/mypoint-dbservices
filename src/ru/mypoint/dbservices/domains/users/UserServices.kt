package ru.mypoint.dbservices.domains.users

import com.mongodb.client.model.IndexOptions
import com.mongodb.client.result.InsertOneResult
import com.mongodb.client.result.UpdateResult
import org.litote.kmongo.SetTo
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.eq
import org.litote.kmongo.set
import org.litote.kmongo.setValue
import ru.mypoint.dbservices.domains.users.dto.UserCreateDTO
import ru.mypoint.dbservices.utils.randomCode
import ru.mypoint.dbservices.utils.sha256

/**
 * Сервис для работы с репозиторием user
 */
class UserService(private val collection: CoroutineCollection<UserRepository>) {
    suspend fun findOneByEmail(email: String): UserRepository? {
        return collection.findOne(UserRepository::email eq email)
    }

    suspend fun insertOne(userCreateDTO: UserCreateDTO): InsertOneResult {
        collection.createIndex("{'email':1}", IndexOptions().unique(true))

        return collection.insertOne(UserRepository(
            email = userCreateDTO.email,
            password = userCreateDTO.password.sha256(),
            address = userCreateDTO.address ?: "",
            zipCode = userCreateDTO.zipCode ?: 0,
            fullName = userCreateDTO.fullName ?: "",
        ))
    }

    suspend fun needsPasswordComplete(email: String): String? {
        val hash = randomCode(10)

        return if (collection.updateOne(
            UserRepository::email eq email,
            set(
                SetTo(UserRepository::isNeedsPassword, false),
                SetTo(UserRepository::hashCode, hash)
            )
        ).wasAcknowledged()) {
             hash
        } else {
            null
        }
    }
}