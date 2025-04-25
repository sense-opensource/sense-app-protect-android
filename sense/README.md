## Sense - Android SDK

Sense is a device intelligence and identification tool. This tool collects a comprehensive set of attributes unique to a device or browser, forming an identity that will help businesses.
Requirements

```
* Use Android 5.1 (API level 21) and above.
* Use Kotlin version 1.6.10 and above.
* Add READ_PHONE_STATE Permission in Android Manifest for deivce information(Optional)
```

Note: If the application does not have the listed permissions, the values collected using those permissions will be ignored. To provide a valid device details, we recommend employing as much permission as possible based on your use-case.

#### Step 1 - Add Dependency

Add the dependency in the app level build.gradle:

```
dependencies {
    implementation 'co.getsense:android:0.0.6'
}
```

#### Step 2 - Import SDK

```
import co.getsense.android.Sense
import co.getsense.android.SenseConfig
```

#### Step 3 - Initialize SDK

Add the following line of code to initialize it with the api key you obtained from the Sense Client panel. If you don't have a api key create new one.

```
val config = SenseConfig(
    apiKey = "Your API Key",
    senseInfo = true, // true or false
    allowGeoLocation = true, // true or false
    tag = ""// Whatever you need, just mention the string here like home, contact, etc.
)
Sense.initSDK(activity, config)
```

#### Step 4 - Get Device Details

Use the below code to get the Device Details

```
Sense.getSenseDetails(this)
```

#### Step 5 - Implement Listener

Set and Implement our listener to receive the Callback details

```
Sense.getDeviceDetails(object : Sense.SenseListener {
    override fun onSuccess(data: String) {
        // success callback 
    }
    override fun onFailure(message: String) {
        // failure callback
    }
})
```

#### Step 6 - Location Permission (Optional)

````
You have to add this permission in AndroidManifest.xml to get Device Location Information and to get Retrieve call state, Network state, Network information, Sim datas from READ_PHONE_STATE and READ_PRIVILEGED_PHONE_STATE.

<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
<uses-permission android:name="android.permission.READ_PRIVILEGED_PHONE_STATE"
tools:ignore="ProtectedPermissions"/>

````

#### Step 7 - Progurad Rules (Optional)

If you encounter any problem in your integration related to Proguard, you can use add the below lines Proguard Rules.

```
-keep,allowobfuscation,allowshrinking interface retrofit2.Call
-keep,allowobfuscation,allowshrinking class retrofit2.Response
-keep,allowobfuscation,allowshrinking class kotlin.coroutines.** { *; }
-dontwarn co.getsense.**
-keep class co.getsense.** {*;}
```

#### Sample Program

Here you can find the demonstration to do the integration.

```
import co.getsense.android.Sense
import co.getsense.android.SenseConfig

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val config = SenseConfig(
            apiKey = "Your API Key", //Replace with your Client Public API Key
             senseInfo = true, // true or false
             allowGeoLocation = true, // true or false
             tag = ""// Whatever you need, just mention the string here like home, contact, etc.
        )

        //Initialize SDK
        Sense.initSDK(this, config)

        // Fetch device details
        getDeviceDtails();
    }
    private fun getDeviceDtails() {
        Sense.getDeviceDetails(object : Sense.SenseListener {
            override fun onSuccess(data: String) {
                // Handle success callback
            }
            override fun onFailure(message: String) {
                // Handle failure callback
            }
        })
    }
}
```