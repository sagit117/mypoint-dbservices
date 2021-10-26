package ru.mypoint.dbservices.domains.users

import com.google.gson.Gson
import io.ktor.application.*
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import ru.mypoint.dbservices.connectors.DataBase
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
                val userDTO = call.receive<UserCreateDTO>()
                val user = try {
                    userDTO.copy()
                } catch (error: Exception) {
                    log.error(error.toString())
                    null
                }

                if (user != null) {

                    /** TODO блок работы с БД */

                    call.respond(HttpStatusCode.OK, Gson().toJson(user))
                } else {
                    call.respond(HttpStatusCode.BadRequest)
                }
            }
        }
    }
}