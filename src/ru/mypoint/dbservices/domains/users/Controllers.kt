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
    val userCollection = DataBase.getCollection<UserRepository>()
    val userService = UserService(userCollection)

    routing {
        route("/users") {
            get("/ping") {
                call.respond(HttpStatusCode.OK, "OK")
            }

            post("/add") {
                val userDTO = call.receive<UserCreateDTO>()
                val user = userDTO.copy()

                call.respond(HttpStatusCode.OK, Gson().toJson(user))
            }
        }
    }
}