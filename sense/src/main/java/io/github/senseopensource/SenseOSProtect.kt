package io.github.senseopensource

import android.app.Activity
import io.github.senseopensource.core.getDetails
import org.json.JSONObject

class SenseOSProtect private constructor(val activity: Activity) {
    private var initData: SenseOSProtectConfig? = null
    companion object {
        private var instance: SenseOSProtect? = null
        fun initSDK(activity: Activity, initData: SenseOSProtectConfig) {
            if (instance == null) {
                instance = SenseOSProtect(activity)
            }
            instance?.apply {
                this.initData = initData
            }
        }

        fun getSenseDetails(listener: SenseOSProtectListener) {
            val sdk = instance
            val deviceDetails = sdk?.let { sdk.initData?.let { it1 -> getDetails(it.activity, it1.installedApplications ) } }
            val parameters = JSONObject(
                mapOf(
                    "app_protect" to deviceDetails
                )
            )
            listener.onSuccess(parameters.toString())
        }
    }

    interface SenseOSProtectListener {
        fun onSuccess(data: String)
        fun onFailure(message: String)
    }
}
