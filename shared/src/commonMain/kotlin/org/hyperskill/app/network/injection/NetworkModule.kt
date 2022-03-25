package org.hyperskill.app.network.injection

import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.features.logging.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.modules.SerializersModule

object NetworkModule {
    fun provideJson(): Json =
        Json {
            coerceInputValues = true
            ignoreUnknownKeys = true
            serializersModule = SerializersModule {
//                contextual(UTCDateSerializer())
            }
        }

    fun provideClient(
        json: Json
    ): HttpClient =
        HttpClient {
            install(JsonFeature) {
                serializer = KotlinxSerializer(json)
            }
            install(Logging) {
                logger = Logger.SIMPLE
                level = LogLevel.ALL
            }

            // TODO Build host from BuildKonfig
            defaultRequest {
                url {
                    protocol = URLProtocol.HTTPS
                    host = "hyperskill.org/api"
                }
            }
        }
}