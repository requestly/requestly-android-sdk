package io.requestly.android.core.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkManager {

    private const val RQ_SERVER_BASE_URL = "https://api.requestly.io/"

    private val client: Retrofit by lazy { Retrofit.Builder()
        .baseUrl(RQ_SERVER_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    }

    val requestlyApiService: RequestlyApiService by lazy {
        client.create(RequestlyApiService::class.java)
    }

    fun <S> create(serviceClass: Class<S>): S {
        return client.create(serviceClass)
    }
}
