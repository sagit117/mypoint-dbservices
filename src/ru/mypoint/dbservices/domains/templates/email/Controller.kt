package ru.mypoint.dbservices.domains.templates.email

import com.mongodb.MongoWriteException
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import ru.mypoint.dbservices.connectors.DataBase
import ru.mypoint.dbservices.domains.templates.email.dto.TemplateEmailCreateDTO
import ru.mypoint.dbservices.domains.templates.email.dto.TemplateEmailGetDTO

@Suppress("unused")
fun Application.controllerTemplatesEmailModule() {
    routing {
        route("/v1/templates/email") {
            val templateEmailCollection = DataBase.getCollection<TemplateEmailRepository>()
            val templateEmailService = TemplateEmailService(templateEmailCollection)

            post("add") {
                val template = try {
                    call.receive<TemplateEmailCreateDTO>().copy()
                } catch (error: Exception) { // входные данные не верны
                    log.error(error.toString())
                    return@post call.respond(HttpStatusCode.BadRequest)
                }

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
            }

            post("get") {
                val templateEmailGetDTO = call.receive<TemplateEmailGetDTO>()

                val templateEmailRepository = templateEmailService.findOneByName(templateEmailGetDTO.name)

                if (templateEmailRepository != null) {
                    call.respond(HttpStatusCode.OK, templateEmailRepository)
                } else {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }
        }
    }
}