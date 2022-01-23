package ru.mypoint.dbservices.domains.users

import com.mongodb.client.model.Field
import com.mongodb.client.model.IndexOptions
import com.mongodb.client.result.InsertOneResult
import com.mongodb.client.result.UpdateResult
import org.bson.types.ObjectId
import org.litote.kmongo.*
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.CoroutineFindPublisher
import org.litote.kmongo.id.toId
import org.litote.kmongo.util.idValue
import org.litote.kreflect.setPropertyValue
import ru.mypoint.dbservices.domains.users.dto.UserChangeDataDTO
import ru.mypoint.dbservices.domains.users.dto.UserChangePasswordDTO
import ru.mypoint.dbservices.domains.users.dto.UserCreateDTO
import ru.mypoint.dbservices.domains.users.dto.UsersGetDTO
import ru.mypoint.dbservices.utils.randomCode
import ru.mypoint.dbservices.utils.sha256
import kotlin.reflect.KProperty
import kotlin.reflect.jvm.internal.impl.load.kotlin.JvmType

/**
 * Сервис для работы с репозиторием user
 */
class UserService(private val collection: CoroutineCollection<UserRepository>) {
    suspend fun findOneByEmail(email: String): UserRepository? {
        return collection.findOne(UserRepository::email eq email)
    }

    suspend fun findOneById(id: String): UserRepository? {
        return collection.findOneById(ObjectId(id))
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

    /** Метод обновляет не основные данные пользователя */
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

    /** Метод снимает блокировку требующую ввод пароля */
    suspend fun needsPasswordComplete(email: String): String? {
        val hash = randomCode(10)

        return if (collection.updateOne(
            UserRepository::email eq email,
            set(
                SetTo(UserRepository::isNeedsPassword, false),
                SetTo(UserRepository::hashCode, hash) // обновляем хэш код, что-бы текущие токены стали не действительными
            )
        ).wasAcknowledged()) {
             hash
        } else {
            null
        }
    }

    /** подтверждение email */
    suspend fun confirmationEmail(email: String): UpdateResult {
        return collection.updateOne(UserRepository::email eq email, setValue(UserRepository::isConfirmEmail, true))
    }

    /** смена пароля */
    suspend fun changePassword(changePasswordDTO: UserChangePasswordDTO): UpdateResult {
        return collection
            .updateOne(
                UserRepository::email eq changePasswordDTO.email,
                setValue(UserRepository::password, changePasswordDTO.newPassword.sha256())
            )
    }

    /** Получить всех пользователей */
    suspend fun findAll(usersGetDTO: UsersGetDTO): List<UserRepository> {
        return collection
            .find()
            .limit(usersGetDTO.limit)
            .skip(usersGetDTO.skip)
            .projection(
                fields(
                    exclude(UserRepository::password),
                ),
            )
            .toList()
    }

    /** Получить количество пользователей всего */
    suspend fun countAll(): Long {
        return collection.countDocuments()
    }
}