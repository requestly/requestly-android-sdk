package io.requestly.android.core

/**
 * - Contains all the common settings that are required by Requestly SDK
 * - Contains all the feature states
 */
class SettingsManager {
    private var appToken: String? = null
    private var uniqueDeviceId: String? = null
    private var captureEnabled: Boolean = false
    private var isAnonymousSession: Boolean = true // This will enable/disable the `CONNECT` button

    private var featuresState = HashMap<Feature, Boolean>()

    companion object {
        private var INSTANCE: SettingsManager? = null

        fun getInstance(): SettingsManager {
            if (INSTANCE == null) {
                INSTANCE = SettingsManager()
            }

            return INSTANCE as SettingsManager
        }
    }

    init {
        // Get configs from shared preferences
        this.uniqueDeviceId = KeyValueStorageManager.getString(Constants.STORAGE_DEVICE_ID_KEY)
        this.appToken = KeyValueStorageManager.getString(Constants.STORAGE_APP_TOKEN_KEY)
        this.captureEnabled = KeyValueStorageManager.getBoolean(Constants.STORAGE_CAPTURE_ENABLED_KEY, false)
    }

    fun setAppToken(appToken: String) {
        this.appToken = appToken
        KeyValueStorageManager.putString(Constants.STORAGE_APP_TOKEN_KEY, appToken)
    }

    fun getAppToken(): String? {
        return this.appToken
    }

    fun setUniqueDeviceId(uniqueDeviceId: String) {
        this.uniqueDeviceId = uniqueDeviceId
        KeyValueStorageManager.putString(Constants.STORAGE_DEVICE_ID_KEY, uniqueDeviceId)
    }

    fun getUniqueDeviceId(): String? {
        return this.uniqueDeviceId
    }

    fun setCaptureEnabled(captureEnabled: Boolean) {
        this.captureEnabled = captureEnabled
        KeyValueStorageManager.putBoolean(Constants.STORAGE_CAPTURE_ENABLED_KEY, captureEnabled)
    }

    fun getCaptureEnabled(): Boolean {
        return this.captureEnabled
    }

    fun setIsAnonymousSession(isAnonymousSession: Boolean) {
        this.isAnonymousSession = isAnonymousSession
    }

    fun getIsAnonymousSession(): Boolean {
        return this.isAnonymousSession
    }

    fun setFeatureState(feature: Feature, state: Boolean) {
        this.featuresState[feature] = state
    }
}
