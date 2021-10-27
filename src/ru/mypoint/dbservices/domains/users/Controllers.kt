package ru.mypoint.dbservices.domains.users

import com.google.gson.Gson
import com.mongodb.client.model.IndexOptions
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.litote.kmongo.eq
import ru.mypoint.dbservices.connectors.DataBase
import ru.mypoint.dbservices.domains.dto.ResponseDTO
import ru.mypoint.dbservices.domains.dto.ResponseStatus
import ru.mypoint.dbservices.domains.users.dto.UserCreateDTO

@Suppress("unused")
fun Application.controllersModule() {
    routing {
        route("/users") {
            val userCollection = DataBase.getCollection<UserRepository>()

            val userService = UserService(userCollection)

            get("/ping") {
                call.respond(HttpStatusCode.OK, "OK")
            }

            post("/add") {
                // todo: подумать куда перенести
                userCollection.createIndex("{'email':1}", IndexOptions().unique(true))

                val userDTO = call.receive<UserCreateDTO>()
                val user = try {
                    userDTO.copy()
                } catch (error: Exception) {
                    log.error(error.toString())
                    null
                }

                if (user != null) {
                    /** TODO блок работы с БД */
                    val wasAcknowledged: Boolean = try {
                        userService.insertOne(user).wasAcknowledged()
                    } catch (error: Exception) {
                        // todo: сделать обработку ошибок
                        log.error(error.toString())
                        false
                    }

                    if (wasAcknowledged) {
                        call.respond(HttpStatusCode.OK, Gson().toJson(ResponseDTO(ResponseStatus.OK.value)))
                    } else {
                        call.respond(HttpStatusCode.Conflict)
                    }
                } else {
                    println(ResponseStatus.NoValidate.value)
                    call.respond(HttpStatusCode.BadRequest, Gson().toJson(ResponseDTO(ResponseStatus.NoValidate.value))) // входные данные не верны
                }
            }
        }
    }
}

