package io.github.senseopensource.core
import android.app.Activity
import android.os.Build
import io.github.senseopensource.utils.isEmulator
import io.github.senseopensource.utils.detectVirtualDisplays
import io.github.senseopensource.utils.getInstalledApps
import io.github.senseopensource.utils.getSimDetails
import io.github.senseopensource.utils.isAppCloned
import io.github.senseopensource.utils.isDeviceRooted
import io.github.senseopensource.utils.isFactoryResetDetected
import io.github.senseopensource.utils.isFridaDetected
import io.github.senseopensource.utils.isVpnEnabled
import io.github.senseopensource.utils.unknownApps

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