package io.requestly.android.core.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import io.requestly.android.core.BuildConfig
import io.requestly.android.core.network.NetworkManager
import io.requestly.android.core.network.RQSDKVersionInfo
import io.requestly.android.core.network.RQSDKVersionUpdateService
import retrofit2.Call
import retrofit2.Callback;
import retrofit2.Response

data class NewVersionNotifyStripData(val displayText: String, val ctaText: String, val url: String)

class MainRequestlyActivityViewModel : ViewModel() {

    private var _versionUpdateLiveData: MutableLiveData<NewVersionNotifyStripData?> =
        MutableLiveData()
    val versionUpdateLiveData: LiveData<NewVersionNotifyStripData?> = _versionUpdateLiveData

    init {
        try {
            NetworkManager.create(RQSDKVersionUpdateService::class.java)
                .getLatestVersion()
                .enqueue(object : Callback<RQSDKVersionInfo> {
                    override fun onResponse(
                        call: Call<RQSDKVersionInfo>,
                        response: Response<RQSDKVersionInfo>
                    ) {
                        val responseBody = response.body()
                        if (response.isSuccessful &&
                            responseBody?.versionCode != null &&
                            responseBody.versionCode > BuildConfig.VERSION_CODE
                        ) {
                            _versionUpdateLiveData.value = NewVersionNotifyStripData(
                                displayText = responseBody.displayText ?: "New Version Available",
                                ctaText = responseBody.ctaText ?: "Update now",
                                url = responseBody.redirectUrl ?: "https://github.com/requestly/requestly-android-sdk/releases"
                            )
                            return
                        }

                        _versionUpdateLiveData.value = null
                    }

                    override fun onFailure(call: Call<RQSDKVersionInfo>, t: Throwable) {
                        _versionUpdateLiveData.value = null
                    }
                })
        } catch (e: java.lang.Exception) {
            _versionUpdateLiveData.value = null
        }
    }
}
