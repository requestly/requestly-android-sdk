package io.requestly.android.core


class SettingsManager {
    private lateinit var appToken: String
    private lateinit var uniqueDeviceId: String

    private var featuresState = HashMap<Feature, Boolean>()

    companion object {
        private var settingsManager: SettingsManager? = null

        fun getInstance(): SettingsManager {
            if(settingsManager == null) {
                settingsManager = SettingsManager()
            }

            return settingsManager as SettingsManager
        }
    }

    fun setAppToken(appToken: String) {
        this.appToken = appToken
    }

    fun getAppToken(): String {
        return this.appToken
    }

    fun setUniqueDeviceId(uniqueDeviceId: String) {
        this.uniqueDeviceId = uniqueDeviceId
    }

    fun getUniqueDeviceId(): String {
        return this.uniqueDeviceId
    }

    fun setFeatureState(feature: Feature, state: Boolean) {
        this.featuresState[feature] = state
    }
}
