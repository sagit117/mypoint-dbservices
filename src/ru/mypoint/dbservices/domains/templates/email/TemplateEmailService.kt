package ru.mypoint.dbservices.domains.templates.email

import com.mongodb.client.model.IndexOptions
import com.mongodb.client.result.InsertOneResult
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.eq
import ru.mypoint.dbservices.domains.templates.email.dto.TemplateEmailCreateDTO

class TemplateEmailService(private val collection: CoroutineCollection<TemplateEmailRepository>) {
    suspend fun insertOne(templateEmailCreateDTO: TemplateEmailCreateDTO): InsertOneResult {
        collection.createIndex("{'name':1}", IndexOptions().unique(true))

        return collection.insertOne(TemplateEmailRepository(
            name = templateEmailCreateDTO.name,
            template = templateEmailCreateDTO.template
        ))
    }

    suspend fun findOneByName(name: String): TemplateEmailRepository? {
        return collection.findOne(TemplateEmailRepository::name eq name)
    }
}