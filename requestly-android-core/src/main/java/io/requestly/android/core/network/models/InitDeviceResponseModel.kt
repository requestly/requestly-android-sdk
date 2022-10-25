package io.requestly.android.core.network.models

import com.google.gson.annotations.SerializedName

data class InitDeviceResponseModel(
    @SerializedName("success")
    var success: String?,

    @SerializedName("device-id")
    var deviceId: String?,
)
