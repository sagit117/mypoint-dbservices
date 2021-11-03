package ru.mypoint.dbservices.domains.users

import com.mongodb.client.model.IndexOptions
import com.mongodb.client.model.UpdateOptions
import com.mongodb.client.result.InsertOneResult
import com.mongodb.client.result.UpdateResult
import org.litote.kmongo.*
import org.litote.kmongo.coroutine.CoroutineCollection
import ru.mypoint.dbservices.domains.users.dto.UserChangeDataDTO
import ru.mypoint.dbservices.domains.users.dto.UserCreateDTO
import ru.mypoint.dbservices.utils.randomCode
import ru.mypoint.dbservices.utils.sha256
import kotlin.reflect.full.memberProperties

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

    suspend fun updateOneByEmail(userChangeDataDTO: UserChangeDataDTO): UpdateResult {
        val setProps = mutableSetOf<SetTo<Any>>()
        if (userChangeDataDTO.address != null) setProps.add(SetTo(UserRepository::address, userChangeDataDTO.address))
        if (userChangeDataDTO.zipCode != null) setProps.add(SetTo(UserRepository::zipCode, userChangeDataDTO.zipCode))
        if (userChangeDataDTO.fullName != null) setProps.add(SetTo(UserRepository::fullName, userChangeDataDTO.fullName))

        return collection.updateOne(
            UserRepository::email eq userChangeDataDTO.email,
            set(
                *setProps.toTypedArray()
            )
        )
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