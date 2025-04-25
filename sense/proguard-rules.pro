#-keep,allowobfuscation,allowshrinking interface retrofit2.Call
#-keep,allowobfuscation,allowshrinking class retrofit2.Response
#-keep,allowobfuscation,allowshrinking class kotlin.coroutines.** { *; }

# -keep,allowobfuscation,allowshrinking class kotlin.coroutines.Continuation

#-dontwarn co.getsense.**
#-keep class co.getsense.** {*;}
#-keep class co.getsense.android.Sense { *; }
#-keep class co.getsense.android.SenseConfig { *; }
# Optional: remove everything else
#-dontwarn co.getsense.android.core.**
#-dontwarn co.getsense.android.utils.**

-dontwarn io.github.**
-keep class io.github.** {*;}
