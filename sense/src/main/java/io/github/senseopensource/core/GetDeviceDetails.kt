package io.github.senseopensource.core
import android.app.Activity
import android.os.Build
import co.getsense.android.utils.isEmulator
import co.getsense.android.utils.detectVirtualDisplays
import co.getsense.android.utils.getInstalledApps
import co.getsense.android.utils.getSimDetails
import co.getsense.android.utils.isAppCloned
import co.getsense.android.utils.isDeviceRooted
import co.getsense.android.utils.isFactoryResetDetected
import co.getsense.android.utils.isFridaDetected
import co.getsense.android.utils.isVpnEnabled
import co.getsense.android.utils.unknownApps

internal fun getDetails(activity: Activity, packageList: List<Map<String, String>>): MutableMap<String, Any> {
    val simDetails = mapOf(
        "slot" to "Unknown",
        "carrierName" to "Unknown",
        "operatorName" to "Unknown",
        "isDataEnabled" to "Unknown",
        "isDataRoamingEnabled" to "Unknown"
    )
    val device = mutableMapOf(
       "isFrida" to isFridaDetected(),
       "isRooted" to isDeviceRooted(),
       "isEmulator" to isEmulator(),
       "installedApps" to getInstalledApps(activity, packageList),
       "clonedApps" to isAppCloned(activity),
       "unknownApps" to unknownApps(activity),
       "vpn" to isVpnEnabled(activity),
       "sim" to if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
           getSimDetails(activity)
       } else {simDetails},
       "isFactoryReset" to isFactoryResetDetected(activity),
       "isRemoteControl" to detectVirtualDisplays(activity)
   )
   return device
}