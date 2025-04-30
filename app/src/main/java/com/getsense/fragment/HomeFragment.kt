package com.getsense.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.getsense.R
import com.getsense.SharedViewModel
import io.github.senseopensource.SenseOSProtect
import io.github.senseopensource.SenseOSProtectConfig
import org.json.JSONObject
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import com.getsense.Utils.checkForStringOrTrue
import com.getsense.Utils.checkKeyForStringOrTrue
import com.google.android.flexbox.FlexDirection
import com.google.android.flexbox.FlexWrap
import com.google.android.flexbox.FlexboxLayout
import com.google.android.flexbox.JustifyContent
import org.json.JSONArray
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
data class SimInfo(
    val slot: Any,
    val carrierName: String,
    val operatorName: String,
    val isDataEnabled: Boolean,
    val isDataRoamingEnabled: Boolean
)
class HomeFragment : Fragment() {
    private lateinit var sharedViewModel: SharedViewModel
    private lateinit var textToCopy: TextView
    private lateinit var senseIdString: String
    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view: View = inflater.inflate(R.layout.home_layout, container, false)
        return view
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
        val activity = activity
        activity?.let {
            val config = SenseOSProtectConfig(
                installedApplications = listOf(
                    mapOf("name" to "Google Pay", "package" to "com.google.android.apps.nbu.paisa.user"),
                    mapOf("name" to "PhonePe", "package" to "com.phonepe.app"),
                    mapOf("name" to "Paytm", "package" to "net.one97.paytm"),
                    mapOf("name" to "BHIM", "package" to "in.org.npci.upiapp"),
                    mapOf("name" to "Amazon (Pay)", "package" to "in.amazon.mShop.android.shopping"),
                    mapOf("name" to "Chrome", "package" to "com.android.chrome")
                )

            )
            SenseOSProtect.initSDK(activity, config)
            setUpUi()
        }
    }
    private fun setUpUi() {
        context?.let { ProgressDialogManager.show(it) }
        SenseOSProtect.getSenseDetails(object : SenseOSProtect.SenseOSProtectListener {
            override fun onSuccess(data: String) {
                val jsonObject = JSONObject(data)
                val jsonData = JSONObject(jsonObject.get("app_protect").toString())
                val vpnInfo = jsonData.get("vpn")
                val vpn = JSONObject(vpnInfo.toString())
                val sim = jsonData.get("sim")

                val textView = view?.findViewById<TextView>(R.id.factory_reset)
                @RequiresApi(Build.VERSION_CODES.O)
                textView?.text  = formatUtcToReadable(jsonData.getString("isFactoryReset"))
                val unknownApps = jsonData.get("unknownApps")
                val unknownAppsListJson = JSONArray(unknownApps.toString())
                val unknownAppsList = mutableListOf<String>()
                for (i in 0 until unknownAppsListJson.length()) {
                    unknownAppsList.add(unknownAppsListJson.getString(i))
                }
                val installedApps = jsonData.get("installedApps")
                val installedAppsListJson = JSONArray(installedApps.toString())
                val installedAppsList = mutableListOf<String>()
                for (i in 0 until installedAppsListJson.length()) {
                    val item = installedAppsListJson.getJSONObject(i)
                    val appName = item.keys().next()
                    val isDetected = item.getBoolean(appName)
                    if (isDetected) {
                        installedAppsList.add(appName)
                    }
                }
                val clonedApps = jsonData.get("clonedApps")
                val clonedAppsListJson = JSONArray(clonedApps.toString())
                val clonedAppsList = mutableListOf<String>()
                for (i in 0 until clonedAppsListJson.length()) {
                    clonedAppsList.add(clonedAppsListJson.getString(i))
                }
                val unknownAppsLayout = view?.findViewById<FlexboxLayout>(R.id.unknownApps)
                val installedAppsLayout = view?.findViewById<FlexboxLayout>(R.id.installedApps)
                val clonedAppsLayout = view?.findViewById<FlexboxLayout>(R.id.clonedApps)
                val simList = mutableListOf<SimInfo>()

                if (sim.toString() != "[]" && sim.toString() != "{}") {
                    val simArray = JSONArray(sim.toString())
                    for (i in 0 until simArray.length()) {
                        val item = simArray.getJSONObject(i)
                        simList.add(
                            SimInfo(
                                slot = item.optInt("slot").toString(),
                                carrierName = item.optString("carrierName", "-"),
                                operatorName = item.optString("operatorName", "-"),
                                isDataEnabled = item.optBoolean("isDataEnabled", false),
                                isDataRoamingEnabled = item.optBoolean("isDataRoamingEnabled", false)
                            )
                        )
                    }
                } else {
                    // Default fallback if no SIM info
                    simList.add(
                        SimInfo(
                            slot = "-",
                            carrierName = "-",
                            operatorName = "-",
                            isDataEnabled = false,
                            isDataRoamingEnabled = false
                        )
                    )
                }
                val simLayouts = mapOf(
                    "1" to R.id.sim_1_details,
                    "2" to R.id.sim_2_details
                )
                simList.forEach { sim ->
                    val layoutId = simLayouts[sim.slot]
                    layoutId?.let { id ->
                        view?.findViewById<FlexboxLayout>(id)?.let { layout ->
                            context?.let { ctx ->
                                displaySimsInFlexbox(ctx, layout, listOf(sim)) // Pass single SIM per layout
                            }
                        }
                    }
                }
                context?.let {
                    if (unknownAppsLayout != null) {
                        addWrappedTextViews(it, unknownAppsLayout, unknownAppsList, false)
                    }
                    if (installedAppsLayout != null) {
                        addWrappedTextViews(it, installedAppsLayout, installedAppsList, true)
                    }
                    if (clonedAppsLayout != null) {
                        addWrappedTextViews(it, clonedAppsLayout, clonedAppsList, false)
                    }
                }
                view?.let {
                    setTextBgById(it, R.id.vpn, checkForStringOrTrue(vpn, "auxiliary"))
                    setTextBgById(
                        it,
                        R.id.emulator,
                        checkKeyForStringOrTrue(jsonData.getString("isEmulator"))
                    )
                    setTextBgById(
                        it,
                        R.id.rootAccess,
                        checkKeyForStringOrTrue(jsonData.getString("isRooted"))
                    )
                    setTextBgById(
                        it,
                        R.id.isFrida,
                        checkKeyForStringOrTrue(jsonData.getString("isFrida"))
                    )
                    setTextBgById(
                        it,
                        R.id.isRemoteControl,
                        checkKeyForStringOrTrue(jsonData.getString("isRemoteControl"))
                    )
                }

                ProgressDialogManager.dismiss()
            }
            override fun onFailure(message: String) {
                ProgressDialogManager.dismiss()
            }
        })
    }
    fun setTextById(view: View, textViewId: Int, newText: String) {
        val textView = view.findViewById<TextView>(textViewId)
        textView?.text = newText
    }
    fun addWrappedTextViews(context: Context, container: FlexboxLayout, items: List<String>, itemType: Boolean) {
        items.forEach { text ->
            val textView = TextView(context).apply {
                this.text = text
                setPadding(12, 15, 12, 15)
                if(itemType) {
                    setBackgroundResource(R.drawable.not_detected)
                    setTextAppearanceCompat(context, R.style.no_detection_txt)
                } else {
                    setBackgroundResource(R.drawable.detected)
                    setTextAppearanceCompat(context, R.style.detection_txt)
                }
            }

            val lp = FlexboxLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 16, 16, 16)
            }

            textView.layoutParams = lp
            container.addView(textView)
        }
    }
    //@RequiresApi(Build.VERSION_CODES.M)
    fun setTextBgById(
        view: View,
        textViewId: Int,
        newText: Boolean = false
    ) {
        val textView = view.findViewById<TextView>(textViewId)
        if (newText) {
            textView?.setText(R.string.detected)
            textView.setBackgroundResource(R.drawable.detected)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                textView.setTextAppearance(R.style.detection_txt)
            } else {
                textView.setTextAppearance(context, R.style.detection_txt) // API < 23 (Legacy)
            }
            textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.x_circle, 0, 0, 0)
        } else {
            textView?.setText(R.string.not_detected)
            textView.setBackgroundResource(R.drawable.not_detected)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                textView.setTextAppearance(R.style.no_detection_txt)
            } else {
                textView.setTextAppearance(context, R.style.no_detection_txt) // API < 23 (Legacy)
            }
            textView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.check_circle, 0, 0, 0)
        }
    }
    @SuppressLint("SetTextI18n")
    fun displaySimsInFlexbox(context: Context, container: FlexboxLayout, simList: List<SimInfo>) {
        container.removeAllViews()
        container.flexDirection = FlexDirection.ROW
        container.flexWrap = FlexWrap.WRAP
        container.justifyContent = JustifyContent.FLEX_START

        simList.forEach { sim ->
            // Helper function to create individual styled TextView
            fun createTextView(text: String): TextView {
                return TextView(context).apply {
                    this.text = text
                    setPadding(24, 16, 24, 16)
                    setBackgroundResource(R.drawable.dark_bg) // Applied only to this TextView
                    setTextAppearanceCompat(context, R.style.sim_txt)
                    layoutParams = FlexboxLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    ).apply {
                        setMargins(0, 8, 8, 8)
                    }
                }
            }

            // Add individual views
            container.addView(createTextView("Slot: ${sim.slot}"))
            container.addView(createTextView("Carrier: ${sim.carrierName}"))
            container.addView(createTextView("Operator: ${sim.operatorName}"))
            container.addView(createTextView("Data: ${if (sim.isDataEnabled) "ON" else "OFF"}"))
            container.addView(createTextView("Roaming: ${if (sim.isDataRoamingEnabled) "ON" else "OFF"}"))
        }
    }
    fun TextView.setTextAppearanceCompat(context: Context, style: Int) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            setTextAppearance(style)
        } else {
            setTextAppearance(context, style)
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    fun formatUtcToReadable(input: String): String {
        val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSX", Locale.getDefault())
        val outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss a", Locale.getDefault())

        val parsedDate = ZonedDateTime.parse(input, inputFormatter)
        return outputFormatter.format(parsedDate)
    }

}
