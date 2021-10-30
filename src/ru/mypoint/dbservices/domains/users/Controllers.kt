package ru.mypoint.dbservices.domains.users

import com.google.gson.Gson
import com.mongodb.MongoWriteException
import com.mongodb.client.model.IndexOptions
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import ru.mypoint.dbservices.connectors.DataBase
import ru.mypoint.dbservices.domains.users.dto.UserCreateDTO
import ru.mypoint.dbservices.domains.users.dto.UserLoginDTO
import ru.mypoint.dbservices.utils.sha256

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
                    /** блок работы с БД */
                    val wasAcknowledged: Boolean = try {
                        userService.insertOne(user).wasAcknowledged()
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

            post("/login") {
                val userDTO = call.receive<UserLoginDTO>()

                val userRepository = try {
                    userService.findOneByEmail(userDTO.email)
                } catch (error: Throwable) {
                    log.error(error.message)
                    return@post call.respond(HttpStatusCode.InternalServerError)
                }

                println(userDTO.toString())

                if (userRepository != null && !userRepository.isBlocked && userRepository.password == userDTO.password.sha256()) {
                    // TODO: снять блок

                    call.respond(HttpStatusCode.OK, Gson().toJson(userRepository.copy(password = "")))
                } else {
                    call.respond(HttpStatusCode.Unauthorized)
                }
            }
        }
    }
}

