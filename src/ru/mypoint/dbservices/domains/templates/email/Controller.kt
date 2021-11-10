package ru.mypoint.dbservices.domains.templates.email

import com.mongodb.MongoWriteException
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import ru.mypoint.dbservices.connectors.DataBase
import ru.mypoint.dbservices.domains.templates.email.dto.TemplateEmailCreateDTO

@Suppress("unused")
fun Application.controllerTemplatesEmailModule() {
    routing {
        route("/v1/templates/email") {
            val templateEmailCollection = DataBase.getCollection<TemplateEmailRepository>()
            val templateEmailService = TemplateEmailService(templateEmailCollection)

            post("add") {
                val templateEmailCreateDTO = call.receive<TemplateEmailCreateDTO>()

                val template = try {
                    templateEmailCreateDTO.copy()
                } catch (error: Exception) {
                    log.error(error.toString())
                    null
                }

                if (template != null) {
                    /** блок работы с БД */
                    val wasAcknowledged: Boolean = try {
                        templateEmailService.insertOne(template).wasAcknowledged()
                    } catch (error: Throwable) {
                        when(error) {
                            is MongoWriteException -> return@post call.respond(HttpStatusCode.Conflict)

                            else -> log.error(error.message)
                        }

                        false
                    }

                    if (wasAcknowledged) {
                        call.respond(HttpStatusCode.OK)
                    } else {
                        // любая не обработанная ошибка
                        call.respond(HttpStatusCode.InternalServerError)
                    }
                } else {
                    // входные данные не верны
                    call.respond(HttpStatusCode.BadRequest)
                }
            }
        }
    }
}