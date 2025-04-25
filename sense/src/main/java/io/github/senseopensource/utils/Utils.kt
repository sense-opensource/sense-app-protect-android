package co.getsense.android.utils

import android.annotation.SuppressLint
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.hardware.display.DisplayManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.telephony.SubscriptionManager
import android.telephony.TelephonyManager
import androidx.annotation.RequiresApi
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

internal fun checkInstalledApps(
    context: Context,
    packageList: List<Map<String, String>>
): Map<String, Boolean> {
    val result = mutableMapOf<String, Boolean>()
    for (item in packageList) {
        val packageName = item["name"] ?: continue
        val packageCode = item["package"] ?: continue

        val isInstalled = try {
            context.packageManager.getPackageInfo(packageCode, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }

        result[packageName] = isInstalled
    }
    return result
}

internal fun getInstalledApps(
    context: Context,
    appList: List<Map<String, String>>
): List<Map<String, Boolean>> {
    val installedApps = checkInstalledApps(context, appList)
    return installedApps.map { (packageName, isInstalled) ->
        mapOf(packageName to isInstalled)
    }
}

// Get the device status is real or emulator
internal fun isEmulator(): Boolean {
    return (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")
            || Build.FINGERPRINT.startsWith("generic")
            || Build.FINGERPRINT.startsWith("unknown")
            || Build.HARDWARE.contains("goldfish")
            || Build.HARDWARE.contains("ranchu")
            || Build.MODEL.contains("google_sdk")
            || Build.MODEL.contains("Emulator")
            || Build.MODEL.contains("Android SDK built for x86")
            || Build.MANUFACTURER.contains("Genymotion")
            || Build.PRODUCT.contains("sdk_google")
            || Build.PRODUCT.contains("google_sdk")
            || Build.PRODUCT.contains("sdk")
            || Build.PRODUCT.contains("sdk_x86")
            || Build.PRODUCT.contains("sdk_gphone64_arm64")
            || Build.PRODUCT.contains("vbox86p")
            || Build.PRODUCT.contains("emulator")
            || Build.PRODUCT.contains("simulator"))
}

internal fun isVpnEnabled(context: Context): Map<String, Any> {
    val result: MutableMap<String, Any> = mutableMapOf()

    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networks = connectivityManager.allNetworks

    for (network in networks) {
        val networkCapabilities = connectivityManager.getNetworkCapabilities(network)
        if (networkCapabilities?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true) {
            result["auxiliary"] = true
            return result
        }
    }
    result["auxiliary"] = false
    return result
}

internal fun isFactoryResetDetected(context: Context): String {
    return try {
        val timeStampList = mutableListOf<Long>()
        val packageManager = context.packageManager
        val packages = try {
            packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        } catch (e: Exception) {
            emptyList<ApplicationInfo>()
        }

        for (packageIn in packages) {
            val packageInfo = try {
                packageManager.getPackageInfo(packageIn.packageName, 0)
            } catch (e: Exception) {
                continue
            }

            val installer = try {
                packageManager.getInstallerPackageName(packageInfo.packageName)
            } catch (e: Exception) {
                null
            }

            val packageTime = packageInfo.firstInstallTime
            val packageName = packageInfo.packageName

            if ((packageTime >= 1503273600000) && (
                        packageName.contains("xiaomi", ignoreCase = true) ||
                                packageName.contains("com.miui", ignoreCase = true) ||
                                packageName.contains("com.mi.global", ignoreCase = true) ||
                                packageName.contains("com.google", ignoreCase = true) ||
                                (installer?.contains("com.android.vending") == true) ||
                                (installer?.contains("com.xiaomi") == true) ||
                                (installer?.contains("com.facebook") == true) ||
                                (installer?.contains("com.samsung") == true)
                        )
            ) {
                timeStampList.add(packageTime)
            }
        }

        val installDate = timeStampList.minOrNull()
        if (installDate != null) {
            val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
                timeZone = TimeZone.getTimeZone("UTC")
            }
            sdf.format(Date(installDate))
        } else {
            "" // No eligible apps found
        }
    } catch (e: Exception) {
        e.printStackTrace()
        ""
    }
}

internal fun getFirstAppInstallTime(context: Context): Long {
    val timeStampList = mutableListOf<Long>()
    val packageManager = context.packageManager
    //val packages: List<ApplicationInfo> = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
    val packages = try {
        packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
    } catch (e: Exception) {
        emptyList<ApplicationInfo>()
    }

    for (packageIn in packages) {
        val packageInfo = packageManager.getPackageInfo(packageIn.packageName, 0)
        val installer = try {
            packageManager.getInstallerPackageName(packageInfo.packageName)
        } catch (e: Exception) {
            null
        }
        val packageTime = packageInfo.firstInstallTime
        val packageName = packageInfo.packageName;
        if((installer !== null && packageTime >= 1503273600000) && ((packageName.contains("xiaomi", ignoreCase = true) || packageName.contains("com.miui", ignoreCase = true) || installer == "com.android.vending") || packageName.contains("com.mi.global", ignoreCase = true) || installer.toString().contains("com.xiaomi", ignoreCase = true) || installer.toString().contains("com.facebook", ignoreCase = true) || installer.toString().contains("com.samsung", ignoreCase = true) || packageName.contains("com.google", ignoreCase = true))){
            timeStampList.add(packageTime)
        }
    }
    val installDate = timeStampList.minOrNull() ?: 0L
    return installDate;
}

internal fun unknownApps(context: Context): List<String> {
    val packageManager = context.packageManager
    val systemApps = mutableListOf<String>()
    try {
        val installedPackages: List<PackageInfo> = try {
            packageManager.getInstalledPackages(PackageManager.GET_META_DATA)
        } catch (e: Exception) {
            emptyList<PackageInfo>()
        }
        for (packageInfo in installedPackages) {
            val appInfo: ApplicationInfo? = packageInfo.applicationInfo
            if (appInfo != null) {
                if ((appInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0){
                    val installerPackageName = try {
                        packageManager.getInstallerPackageName(packageInfo.packageName)
                    } catch (e: Exception) {
                        null
                    }
                    val packageName = packageInfo.packageName;
                    val packageTime = packageInfo.firstInstallTime
                    val pm: PackageManager = context.packageManager
                    val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                    sdf.timeZone = TimeZone.getTimeZone("UTC");
                    val appList = pm.getInstalledApplications(PackageManager.GET_META_DATA)
                        .filter { it.sourceDir.contains("/system/") }.map { it.packageName }
                    val appTest = appList.any { packageName.contains(it, ignoreCase = true) ?: false }
                    val appCheck = appList.any { installerPackageName?.contains(it, ignoreCase = true) ?: false }

                    if(!(appTest || appCheck || compareDate(packageTime, getFirstAppInstallTime(context))) || (installerPackageName == "com.google.android.packageinstaller") || (installerPackageName.toString().contains("filemanager")) ||
                        (packageName.contains("netmirror", ignoreCase = true))){
                        val appName = try {
                            packageManager.getApplicationLabel(appInfo).toString()
                        } catch (e: Exception) {
                            packageName
                        }
                        if (appName.isNotEmpty()) {
                            systemApps.add(appName)
                        }
                    }
                }
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return systemApps
}

internal fun compareDate(timestamp1: Long, timestamp2: Long): Boolean {
    val calendar1 = Calendar.getInstance().apply { timeInMillis = timestamp1 }
    val calendar2 = Calendar.getInstance().apply { timeInMillis = timestamp2 }
    return calendar1.get(Calendar.YEAR) <= calendar2.get(Calendar.YEAR) &&
            calendar1.get(Calendar.DAY_OF_YEAR) <= calendar2.get(Calendar.DAY_OF_YEAR)
}

internal fun isAppCloned(context: Context): List<String> {
    val installedAppsList =  getAllInstalledApps(context)
    val pm = context.packageManager
    val clonedList = mutableListOf<String>()
    val knownInstallers = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        .filter { it.sourceDir.contains("/system/") }.map { it.packageName }
    val knownAppInstallers = pm.getInstalledApplications(PackageManager.GET_META_DATA).map {it.packageName}
    val mergedList = (knownInstallers + knownAppInstallers).distinct()
    for (packageInfo in installedAppsList) {
        val packageName = packageInfo["packageNameId"] ?: continue
        val installer = try {
            pm.getInstallerPackageName(packageName)
        } catch (e: Exception) {
            null
        }
        val isKnownInstaller = mergedList.any { keyword ->

            installer?.contains(keyword, ignoreCase = true) == true
        }
        if (
            isKnownInstaller
        ) {
            packageInfo["appName"]?.let { clonedList.add(it) }
        }
    }
    return clonedList
}

fun getAllInstalledApps(context: Context): List<Map<String, String>> {
    val pm = context.packageManager
    val resultSet = mutableListOf<Map<String, String>>()

    // Fetch the list of all installed applications
    val packages = pm.getInstalledApplications(PackageManager.GET_META_DATA)

    // Loop through each package
    for (packageInfo in packages) {
        val packageName = packageInfo.packageName

        // Fetch installer package name
        val installer = try {
            pm.getInstallerPackageName(packageName)
        } catch (e: Exception) {
            null // No installer info available, return null
        }

        // If installer is not the Play Store (com.android.vending), add to result
        if (installer != null && installer != "com.android.vending") {
            val appName = try {
                pm.getApplicationLabel(packageInfo).toString()
            } catch (e: Exception) {
                packageName
            }
            resultSet.add(mapOf("packageNameId" to packageName, "appName" to appName))
        }
    }

    return resultSet
}

/* Frida Detection */ // FridaDetection.isFridaDetected()

internal fun isFridaDetected(): Boolean {
    return try {
        val processList = Runtime.getRuntime().exec("ps").inputStream.bufferedReader().readText()
        processList.contains("frida-server") || processList.contains("frida-gadget")
    } catch (e: Exception) {
        false
    }
}

internal fun findBinary(binaryName: String): Boolean {
    var found = false
    if (!found) {
        val places = arrayOf(
            "/sbin/", "/system/bin/", "/system/xbin/",
            "/data/local/xbin/", "/data/local/bin/",
            "/system/sd/xbin/", "/system/bin/failsafe/", "/data/local/"
        )
        for (where in places) {
            if (File(where + binaryName).exists()) {
                found = true

                break
            }
        }
    }
    return found
}

internal fun isDeviceRooted(): Boolean {
    return findBinary("su")
}

@SuppressLint("MissingPermission")
@RequiresApi(Build.VERSION_CODES.Q)
internal fun getSimDetails(context: Context): List<Map<String, Any?>> {
    val telephonyManager = context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
    val subscriptionManager = context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
    val simDetailsList = mutableListOf<Map<String, Any?>>()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
        val activeSubscriptionInfoList = subscriptionManager.activeSubscriptionInfoList
        activeSubscriptionInfoList?.forEach { subscriptionInfo ->
            val subscriptionId = subscriptionInfo.subscriptionId
            val telephonyManagerForSim = telephonyManager.createForSubscriptionId(subscriptionId)
            val simDetails = mapOf(
                "slot" to subscriptionInfo.simSlotIndex+1,
                "carrierName" to telephonyManagerForSim.simCarrierIdName,
                "operatorName" to telephonyManagerForSim.simOperatorName,
                "isDataEnabled" to telephonyManagerForSim.isDataEnabled,
                "isDataRoamingEnabled" to telephonyManagerForSim.isDataRoamingEnabled
            )
            simDetailsList.add(simDetails)
        }
    } else{
        val simState = when {
            telephonyManager.getSimState(0) == TelephonyManager.SIM_STATE_READY -> 1
            telephonyManager.getSimState(1) == TelephonyManager.SIM_STATE_READY -> 2
            else -> 0
        }
        simDetailsList.add(
            mapOf(
                "slot" to simState,
                "carrierName" to telephonyManager.simCarrierIdName,
                "operatorName" to telephonyManager.simOperatorName,
                "isDataEnabled" to telephonyManager.isDataEnabled,
                "isDataRoamingEnabled" to telephonyManager.isDataRoamingEnabled
            )
        )
    }
    return simDetailsList
}

internal fun detectVirtualDisplays(context: Context): Boolean {
    val displayManager = context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
    val displays = displayManager.displays
    var displayInfo : Boolean = false
    for (display in displays) {
        if(display.name.contains("screencap") || display.name.contains("teamviewer")){
            displayInfo = true
        }
    }
    return displayInfo;
}