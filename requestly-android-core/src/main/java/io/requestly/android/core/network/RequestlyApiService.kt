package io.requestly.android.core.network

import io.requestly.android.core.network.models.InitDeviceResponseModel
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers

interface RequestlyApiService {
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
