package io.requestly.android.sample

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.StrictMode
import android.text.method.LinkMovementMethod
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import io.requestly.android.event.MainActivity
import io.requestly.android.okhttp.api.RQ.getLaunchIntent
import io.requestly.android.sample.databinding.ActivityMainSampleBinding
import io.requestly.android.okhttp.api.RQ

private val interceptorTypeSelector = InterceptorTypeSelector()

class MainActivity : AppCompatActivity() {

    private lateinit var mainBinding: ActivityMainSampleBinding

    private val client by lazy {
        createOkHttpClient(applicationContext, interceptorTypeSelector)
    }

    private val httpTasks by lazy {
        listOf(HttpBinHttpTask(client), DummyImageHttpTask(client), PostmanEchoHttpTask(client))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mainBinding = ActivityMainSampleBinding.inflate(layoutInflater)

        with(mainBinding) {
            setContentView(root)
            doHttp.setOnClickListener {
                for (task in httpTasks) {
                    task.run()
                }
            }
            launchRqevents?.setOnClickListener {

                startActivity(Intent(this@MainActivity, MainActivity::class.java))
            }

            launchRqinterceptorDirectly.visibility = if (RQ.isOp) View.VISIBLE else View.GONE
            launchRqinterceptorDirectly.setOnClickListener { launchRqInterceptorDirectly() }

            interceptorTypeLabel.movementMethod = LinkMovementMethod.getInstance()
            useApplicationInterceptor.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    interceptorTypeSelector.value = InterceptorType.APPLICATION
                }
            }
            useNetworkInterceptor.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    interceptorTypeSelector.value = InterceptorType.NETWORK
                }
            }
        }

        StrictMode.setVmPolicy(
            StrictMode.VmPolicy.Builder()
                .detectLeakedClosableObjects()
                .penaltyLog()
//                .penaltyDeath()
                .build()
        )

        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
//                .detectDiskReads()
//                .detectDiskWrites()
                .penaltyLog()
                .penaltyDeath()
                .build()
        )
    }



    private fun launchRqInterceptorDirectly() {
        // Optionally launch Chucker directly from your own app UI
        startActivity(getLaunchIntent(this))
    }
}
