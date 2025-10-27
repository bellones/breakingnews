package com.thiago.android.breakingnews.data.repository

import com.thiago.android.breakingnews.data.mapping.toModel
import com.thiago.android.breakingnews.data.response.news.NewsResponse
import com.thiago.android.breakingnews.domain.model.news.NewsModel
import com.thiago.android.breakingnews.domain.repository.HomeRepository
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class HomeRepositoryImpl (
    private val httpClient: HttpClient
): HomeRepository {
    override fun getBreakingNews(): Flow<NewsModel>  = flow {
       val result = httpClient.get(GET_NEWS_ENDPOINT) {
           url {
               parameters.append(COUNTRY_PARAM,  value = COUNTRY)
               parameters.append(API_PARAM, value = KEY_API)
           }
       }.body<NewsResponse>().toModel()
        emit(result)

    }
    companion object {
        const val KEY_API = "4d0b3ebe12b840feaa342a5a5d5d38ec"
        const val API_PARAM = "apiKey"
        const val COUNTRY = "us"
        const val COUNTRY_PARAM = "country"
        const val GET_NEWS_ENDPOINT = "v2/top-headlines"
    }
}