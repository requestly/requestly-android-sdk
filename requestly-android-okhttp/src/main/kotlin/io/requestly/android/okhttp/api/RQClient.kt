@file:Suppress("TooGenericExceptionCaught")
package io.requestly.android.okhttp.api

import android.util.Log
import com.google.gson.annotations.SerializedName
import io.requestly.android.okhttp.internal.RQConstants
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.GET
import java.lang.Exception


public class RQClient(
    public var deviceId: String?, private val sdkId: String, public var captureEnabled: Boolean = false
) {
    private val api = Retrofit.Builder()
                        .baseUrl(RQConstants.RQ_SERVER_BASE_URL)
                        .addConverterFactory(GsonConverterFactory.create())
                        .build()
                        .create<RQService>()

    internal suspend fun initDevice(): String? {
        val deviceModel: String = android.os.Build.MODEL
        val deviceName: String = android.os.Build.MANUFACTURER + "_" + android.os.Build.PRODUCT

        try {
            val response = api.initDevice(deviceId, sdkId, deviceModel, deviceName, captureEnabled)

            if(response.isSuccessful) {
                val responseBody: InitDeviceResponseModel? = response.body()
                Log.d(RQConstants.LOG_TAG, "Hurray Success" + response.body().toString())
                Log.d(RQConstants.LOG_TAG, "Capture Enabled" + this.captureEnabled)
                deviceId = responseBody?.deviceId
                return responseBody?.deviceId
            } else {
                Log.d(RQConstants.LOG_TAG, "Error Status Code" + response.errorBody().toString() )
            }
        } catch (err: Exception) {
            Log.d(RQConstants.LOG_TAG, "Exception\n $err")
        }
        return null
    }

    public suspend fun updateCaptureEnabled(newStatus: Boolean) {
        captureEnabled = newStatus
        initDevice()
    }

    private interface RQService {
        @Headers("Content-Type: application/json")
        @GET("initSdkDevice")
        suspend fun initDevice(
            @Header("device_id") deviceId: String?,
            @Header("sdk_id") sdkId: String,
            @Header("device_model") deviceModel: String,
            @Header("device_name") deviceName: String,
            @Header("capture_enabled") captureEnabled: Boolean,
        ): Response<InitDeviceResponseModel>
    }

    private data class InitDeviceResponseModel(
        @SerializedName("success")
        var success: String?,

        @SerializedName("device-id")
        var deviceId: String?,
    )
}

