package com.thiago.android.breakingnews.di.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.ContentType

import io.ktor.http.URLProtocol
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json


import org.koin.dsl.module

val networkModule = module {
    single<HttpClient> {
        HttpClient {
            install(Logging) {
                logger = object:  io.ktor.client.plugins.logging.Logger {
                    override fun log(message: String) {
                        println(message)
                    }
                }
            }
            install (ContentNegotiation) {
                json(
                    Json {
                        prettyPrint = true
                        isLenient = true
                        ignoreUnknownKeys = true
                    }

                )
            }
            defaultRequest {
                url {
                    protocol = URLProtocol.HTTPS
                    host = "newsapi.org"
                    contentType(ContentType.Application.Json)
                }
            }
        }
    }
}