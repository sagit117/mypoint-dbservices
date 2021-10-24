package ru.mypoint.dbservices.domains.users

import io.ktor.application.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*

@Suppress("unused")
fun Application.controllers() {
    routing {
        route("/users") {
            get("/ping") {
                call.respond(HttpStatusCode.OK, "OK")
            }
        }
    }
}