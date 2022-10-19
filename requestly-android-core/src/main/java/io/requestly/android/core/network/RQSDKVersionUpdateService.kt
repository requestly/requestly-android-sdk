package io.requestly.android.core.network

import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.http.GET

data class RQSDKVersionInfo(
    @SerializedName("versionName") val versionName: String?,
    @SerializedName("versionCode") val versionCode: Int?,
    @SerializedName("displayText") val displayText: String?,
    @SerializedName("ctaText") val ctaText: String?,
    @SerializedName("redirectUrl") val redirectUrl: String?)

interface RQSDKVersionUpdateService {
    @GET("androidSdk/latestVersion")
    fun getLatestVersion(): Call<RQSDKVersionInfo>
}
