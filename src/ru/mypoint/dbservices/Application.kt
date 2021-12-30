package ru.mypoint.dbservices

import com.google.gson.JsonDeserializer
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import io.ktor.application.*
import io.ktor.request.*
import io.ktor.features.*
import io.ktor.gson.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*
import org.litote.kmongo.Id
import org.litote.kmongo.toId

fun main(args: Array<String>): Unit = io.ktor.server.jetty.EngineMain.main(args)

@Suppress("unused") // Referenced in application.conf
@kotlin.jvm.JvmOverloads
fun Application.module(testing: Boolean = false) {
    install(CallLogging) {
        // TODO: убрать лишние логи (БД)
        filter { call -> call.request.path().startsWith("/") }
    }

    install(ContentNegotiation) {
        gson {
            val gsonBuilder = this;
            gsonBuilder.registerTypeAdapter(
                Id::class.java,
                JsonSerializer<Id<Any>> { id, _, _ -> JsonPrimitive(id?.toString()) }
            )
            gsonBuilder.registerTypeAdapter(
                Id::class.java,
                JsonDeserializer<Id<Any>> { id, _, _ -> id.asString.toId() }
            )
            gsonBuilder.create()
        }
    }

    routing {
        get("/ping") {
            call.respond(HttpStatusCode.OK, mapOf("status" to "OK"))
        }
    }
}

