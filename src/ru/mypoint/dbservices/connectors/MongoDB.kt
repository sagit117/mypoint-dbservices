package ru.mypoint.dbservices.connectors

import io.ktor.application.*
import org.litote.kmongo.coroutine.CoroutineCollection
import org.litote.kmongo.coroutine.CoroutineDatabase
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo

@Suppress("unused") // Referenced in application.conf
fun Application.mongoModule() {
    val connectionString = environment.config.propertyOrNull("mongodb.connectionString")?.getString()
    val dataBaseName = environment.config.propertyOrNull("mongodb.dataBaseName")?.getString()

    val mongoClient = connectionString?.let { KMongo.createClient(it).coroutine }
    val dataBase = dataBaseName?.let { mongoClient?.getDatabase(it) }

    if (dataBase != null) DataBase.setDB(dataBase)
}

/**
 * Хранит данные о подключение к mongo
 */
object DataBase {
    private lateinit var db: CoroutineDatabase

    fun setDB(database: CoroutineDatabase) {
        db = database
    }

    fun getDB(): CoroutineDatabase {
        return db
    }

    inline fun <reified T: Any> getCollection(): CoroutineCollection<T> {
        return getDB().getCollection()
    }
}