package io.requestly.rqinterceptor.internal.support

import com.google.gson.Gson
import com.google.gson.GsonBuilder

internal object JsonConverter {

    val nonNullSerializerInstance: Gson by lazy {
        GsonBuilder()
            .disableHtmlEscaping()
            .setPrettyPrinting()
            .create()
    }

    val instance: Gson by lazy {
        nonNullSerializerInstance.newBuilder()
            .serializeNulls()
            .create()
    }
}
