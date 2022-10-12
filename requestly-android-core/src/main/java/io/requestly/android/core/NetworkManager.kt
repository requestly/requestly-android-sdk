package io.requestly.android.core

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object NetworkManager {

    private const val RQ_SERVER_BASE_URL = "http://demo6221484.mockable.io/" // TODO

    private val client: Retrofit by lazy { Retrofit.Builder()
        .baseUrl(RQ_SERVER_BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    }

    fun <S> create(serviceClass: Class<S>): S {
        return client.create(serviceClass)
    }
}
