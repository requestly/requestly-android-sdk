package io.requestly.android.core

import android.app.Application
import android.content.Context
import android.content.Intent
import android.util.Log
import io.requestly.android.core.internal.support.ListNotificationHelper
import io.requestly.android.core.internal.support.MetadataNotificationHelper
import io.requestly.android.core.modules.logs.lib.lynx.main.LynxConfig
import io.requestly.android.core.modules.logs.lib.lynx.main.model.*
import io.requestly.android.core.modules.logs.lib.lynx.main.presenter.LynxRequestlyPresenter
import io.requestly.android.core.network.NetworkManager
import io.requestly.android.core.network.models.InitDeviceResponseModel
import io.requestly.android.core.ui.MainRequestlyActivity
import kotlinx.coroutines.*
import java.lang.Exception
import java.util.UUID

class Requestly {
    companion object {
        private lateinit var INSTANCE: Requestly

        @JvmStatic
        fun getInstance(): Requestly {
            return INSTANCE
        }

        fun getLaunchIntent(context: Context, startingFlow: RequestlyLaunchFlow): Intent {
            return MainRequestlyActivity.getLaunchIntent(
                context,
                Intent.FLAG_ACTIVITY_RETAIN_IN_RECENTS
                    or Intent.FLAG_ACTIVITY_NEW_DOCUMENT
                    or Intent.FLAG_ACTIVITY_SINGLE_TOP,
                startingFlow
            )
        }
    }

    lateinit var applicationContext: Context
    lateinit var listNotificationHelper: ListNotificationHelper
    lateinit var metadataNotificationHelper: MetadataNotificationHelper

    // Logs Helpers
    var logsConfig: LynxConfig = LynxConfig()
    lateinit var logsLynx: Lynx
    lateinit var logsLynxPresenter: LynxRequestlyPresenter

    class Builder(
        private val application: Application,
        private val appToken: String? = null,
    ) {
        private var applicationContext: Context = application.applicationContext

        private val job = Job()
        private val applicationScope = CoroutineScope(Dispatchers.Main + job)

        // Start Configuration: Different Configurations of Builder
        private var networkLoggerUIState = true

        // module/logs
        private var logsConfig = LynxConfig()

        // End Configuration

        fun build() {
            Log.d(Constants.LOG_TAG, "Start: Building Core")

            // Create Requestly Instance
            INSTANCE = Requestly()
            getInstance().applicationContext = applicationContext
            getInstance().listNotificationHelper = ListNotificationHelper(applicationContext)

            // Init KeyValueStorageManager
            KeyValueStorageManager.initialize(applicationContext)

            applicationScope.launch {
                // Init SettingsManager Singleton
                // Overrides the token set previously
                appToken?.let {
                    Log.d(Constants.LOG_TAG, "appToken initialized: $it")
                    SettingsManager.getInstance().setAppToken(it)
                    // Setting this false after verifying whether SDK key exist or not (/initSdkDevice)
                    SettingsManager.getInstance().setIsAnonymousSession(true)
                }

                // Build individual features modules
                buildLogsModule()
                Log.d(Constants.LOG_TAG, "Finish: Building Core")

                // Init Notifications
                // TODO: Create Notification Manager Class
                initNotifications()

                // Init SDK
                initSdk()
            }
        }

        private fun showNotifications() {
            getInstance().metadataNotificationHelper.show(
                SettingsManager.getInstance().getUniqueDeviceId(),
                SettingsManager.getInstance().getCaptureEnabled()
            )
        }

        private fun initNotifications() {
            getInstance().metadataNotificationHelper = MetadataNotificationHelper(getInstance().applicationContext)
        }

        /** START: Build Individual Modules **/
        private fun buildLogsModule() {
            // LynxConfig init
            // Lynx init
            // LynxRequestlyPresenter init
            val lynx = Lynx(Logcat(), AndroidMainThread(), TimeProvider())
            lynx.config = logsConfig
            val presenter = LynxRequestlyPresenter(lynx, logsConfig.maxNumberOfTracesToShow)
            presenter.resume()

            getInstance().logsConfig = logsConfig
            getInstance().logsLynx = lynx
            getInstance().logsLynxPresenter = presenter
        }

        /** END: Build Individual Modules **/

        private suspend fun initSdk() {
            val appToken = SettingsManager.getInstance().getAppToken()
            if(
                this.appToken==null &&
                (appToken == null || !appToken.startsWith(Constants.ANONYMOUS_APP_TOKEN_PREFIX))
            ){
                initAnonymousSdk()
            }
            Log.d(Constants.LOG_TAG, "appToken: ${SettingsManager.getInstance().getAppToken()}")
            initDevice()
        }

        // Called only once if sdkId is not given. Later on fetched from storage in SettingsManager
        private fun initAnonymousSdk() {
            // initUUID
            val randomUUID = UUID.randomUUID().toString()
            val anonAppToken = "${Constants.ANONYMOUS_APP_TOKEN_PREFIX}$randomUUID"
            Log.d(Constants.LOG_TAG, "random appToken initialized: $anonAppToken")

            SettingsManager.getInstance().setAppToken(anonAppToken)
            SettingsManager.getInstance().setCaptureEnabled(false)
            SettingsManager.getInstance().setIsAnonymousSession(true)
        }

        private suspend fun initDevice() {
            // Call /initDevice (null/non-null)
            // set in Settings Manager from Response
                // set deviceId storage
            val deviceModel: String = android.os.Build.MODEL
            val deviceName: String = android.os.Build.MANUFACTURER + "_" + android.os.Build.PRODUCT
            val deviceId = SettingsManager.getInstance().getUniqueDeviceId()
            val appToken = SettingsManager.getInstance().getAppToken()!! //initAnonymousSdk called before

            withContext(Dispatchers.IO) {
                try {
                    Log.d(Constants.LOG_TAG, "/initSdkDevice called; deviceId:$deviceId; appToken:$appToken")
                    val response = NetworkManager.requestlyApiService.initDevice(
                        deviceId,
                        appToken,
                        deviceModel,
                        deviceName,
                        false, // Sending this false only
                    )
                    if(response.isSuccessful) {
                        val responseBody: InitDeviceResponseModel? = response.body()
                        Log.d(Constants.LOG_TAG, "Success /initSdkDevice " + response.body().toString() )
                        responseBody?.deviceId?.let {
                            SettingsManager.getInstance().setUniqueDeviceId(it)
                        }
                        SettingsManager.getInstance().setIsAnonymousSession(
                             responseBody?.isAnonymousSession?:true
                        )
                    } else {
                        Log.d(Constants.LOG_TAG, "Error /initSdkDevice; ${response.code()}; ${response.errorBody()?.string()}" )
                        SettingsManager.getInstance().setIsAnonymousSession(true)
                    }
                } catch (err: Exception) {
                    Log.d(Constants.LOG_TAG, "Exception /initSdkDevice \n$err" )
                    SettingsManager.getInstance().setIsAnonymousSession(true)
                }
                showNotifications()
            }
        }

        /** START: Set Individual features configs **/
        fun setNetworkLoggerUIState(visible: Boolean = true): Builder {
            this.networkLoggerUIState = visible
            return this
        }

        fun setLoggerConfig(
            maxNumberOfTracesToShow: Int = 2500,
            filter: String? = null,
            filterTraceLevel: TraceLevel? = null,
            textSizeInPx: Float = 36F,
            samplingRate: Int = 150): Builder
        {
            logsConfig.maxNumberOfTracesToShow = maxNumberOfTracesToShow
            logsConfig.filter = filter
            logsConfig.filterTraceLevel = filterTraceLevel
            logsConfig.textSizeInPx = textSizeInPx
            logsConfig.samplingRate = samplingRate
            return this
        }
        /** END: Set Individual features configs **/
    }
}
