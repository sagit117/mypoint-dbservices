package ru.mypoint.dbservices.domains.users

import com.mongodb.MongoWriteException
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import org.bson.types.ObjectId
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
                    /** Инициируем класс для валидации email */
                    call.receive<UserCreateDTO>().copy()
                } catch (error: Exception) { // входные данные не верны
                    log.error(error.toString())
                    return@post call.respond(HttpStatusCode.BadRequest)
                }

                /** Блок работы с БД */
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

            route("/get") {
                /** Получить одного пользователя */
                post("/one") {
                    val userGetDTO = call.receive<UserGetDTO>()

                    val userRepository = if (userGetDTO.email != null) {
                        userService.findOneByEmail(userGetDTO.email)
                    } else {
                        userGetDTO.id?.let { id -> userService.findOneById(id) }
                    }

                    if (userRepository != null) {
                        call.respond(HttpStatusCode.OK, userRepository.copy(password = ""))
                    } else {
                        call.respond(HttpStatusCode.BadRequest)
                    }
                }

                /** Получить несколько пользователей */
                post("/all") {
                    val userGetDTO = call.receive<UsersGetDTO>()

                    val userRepositoryList = userService.findAll(userGetDTO)

                    call.respond(HttpStatusCode.OK, userRepositoryList)
                }

                /** Получить общее количество пользователей */
                post("/count") {
                    val count = userService.countAll()

                    call.respond(HttpStatusCode.OK, mapOf("count" to count))
                }

                /** Получить несколько пользователей и общее количество */
                post("/list") {
                    val userGetDTO = call.receive<UsersGetDTO>()
                    val count = userService.countAll()
                    val userRepositoryList = userService.findAll(userGetDTO)

                    call.respond(HttpStatusCode.OK, mapOf("users" to userRepositoryList, "count" to count))
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
                        userRepository.copy(password = "", hashCode = hash ?: userRepository.hashCode)
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

