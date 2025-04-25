package io.github.senseopensource

import android.app.Activity
import io.github.senseopensource.core.getDetails
import org.json.JSONObject

class Sense private constructor(val activity: Activity) {
    private var initData: SenseOSProtectConfig? = null
    companion object {
        private var instance: Sense? = null
        fun initSDK(activity: Activity, initData: SenseOSProtectConfig) {
            if (instance == null) {
                instance = Sense(activity)
            }
            instance?.apply {
                this.initData = initData
            }
        }

        fun getSenseDetails(listener: SenseListener) {
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

    interface SenseListener {
        fun onSuccess(data: String)
        fun onFailure(message: String)
    }
}
