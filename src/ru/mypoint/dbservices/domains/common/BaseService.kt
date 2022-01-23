package ru.mypoint.dbservices.domains.common

import org.bson.conversions.Bson
import org.bson.types.ObjectId
import org.litote.kmongo.coroutine.CoroutineCollection
import ru.mypoint.dbservices.domains.users.dto.ListGetDTO

open class BaseService<T : Any>(private val collection: CoroutineCollection<T>) {
    /** Поиск по ID */
    suspend fun findOneById(id: String): T? {
        return collection.findOneById(ObjectId(id))
    }

    /** Получить количество записей всего */
    suspend fun countAll(): Long {
        return collection.countDocuments()
    }

    /** Получение всех записей */
    suspend fun findAll(listGetDTO: ListGetDTO, fieldsOpt: Bson): List<T> {
        return collection
            .find()
            .limit(listGetDTO.limit)
            .skip(listGetDTO.skip)
            .projection(
                fieldsOpt
            )
            .toList()
    }
}