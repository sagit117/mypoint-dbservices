package ru.mypoint.dbservices.domains.users

import com.mongodb.MongoWriteException
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.litote.kmongo.json
import ru.mypoint.dbservices.connectors.DataBase
import ru.mypoint.dbservices.domains.users.dto.*
import ru.mypoint.dbservices.utils.sha256

@Suppress("unused")
fun Application.controllerUsersModule() {
    routing {
        route("/v1/users") {
            val userCollection = DataBase.getCollection<UserRepository>()
            val userService = UserService(userCollection)

            post("/add") {
                val user = try {
                    call.receive<UserCreateDTO>().copy()
                } catch (error: Exception) { // входные данные не верны
                    log.error(error.toString())
                    return@post call.respond(HttpStatusCode.BadRequest)
                }

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

            route("/update") {
                post("/data") {
                    val userChangeDataDTO = call.receive<UserChangeDataDTO>()

                    if (userService.updateOneByEmail(userChangeDataDTO).wasAcknowledged()) {
                        call.respond(HttpStatusCode.OK)
                    } else {
                        call.respond(HttpStatusCode.BadRequest)
                    }
                }

                post("/password") {
                    val userChangePasswordDTO = call.receive<UserChangePasswordDTO>()

                    if (userService.changePassword(userChangePasswordDTO).wasAcknowledged()) {
                        call.respond(HttpStatusCode.OK)
                    } else {
                        call.respond(HttpStatusCode.BadRequest)
                    }
                }
            }


            post("/confirmation/email") {
                val confirmationEmailDTO = call.receive<ConfirmationEmailDTO>()

                if (userService.confirmationEmail(confirmationEmailDTO.email).wasAcknowledged()) {
                    call.respond(HttpStatusCode.OK)
                } else {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }
        }
    }
}

