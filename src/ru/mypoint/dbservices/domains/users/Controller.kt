package ru.mypoint.dbservices.domains.users

import com.mongodb.MongoWriteException
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.litote.kmongo.json
import ru.mypoint.dbservices.connectors.DataBase
import ru.mypoint.dbservices.domains.users.dto.UserChangeDataDTO
import ru.mypoint.dbservices.domains.users.dto.UserCreateDTO
import ru.mypoint.dbservices.domains.users.dto.UserGetDTO
import ru.mypoint.dbservices.domains.users.dto.UserLoginDTO
import ru.mypoint.dbservices.utils.sha256

@Suppress("unused")
fun Application.controllerUsersModule() {
    routing {
        route("/v1/users") {
            val userCollection = DataBase.getCollection<UserRepository>()
            val userService = UserService(userCollection)

            post("/add") {
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

            post("/get") {
                val userGetDTO = call.receive<UserGetDTO>()

                val userRepository = userService.findOneByEmail(userGetDTO.email)

                if (userRepository != null) {
                    call.respond(HttpStatusCode.OK, userRepository.copy(password = "").json)
                } else {
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

                if (userRepository != null && !userRepository.isBlocked && userRepository.password == userDTO.password.sha256()) {
                    // снять блок
                    val hash = if (userRepository.isNeedsPassword) {
                        userService.needsPasswordComplete(userRepository.email)
                    } else {
                        null
                    }

                    call.respond(
                        HttpStatusCode.OK,
                        userRepository.copy(password = "", hashCode = hash ?: userRepository.hashCode).json
                    )
                } else {
                    call.respond(HttpStatusCode.Unauthorized)
                }
            }

            post("/update/data") {
                val userChangeDataDTO = call.receive<UserChangeDataDTO>()

                if (userService.updateOneByEmail(userChangeDataDTO).wasAcknowledged()) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }
        }
    }
}
