# Call-Record

## This demostrate how to record a call on android.
### When have a incoming call or outgoing call, the application will record voice automatically and save on storage in folder android/data/opensources.android.recordcall/cache.

The project include 3 main class: 

* WelcomeActivity : This class is a 'activity' show some instruction and request permission on android M or later serve for app for recording, for catching event when have call.
* CallStateReceiver : This class is a 'receiver' will receive some broadcast event when have a call exactly it is call state change
* CallRecordService : This class is a 'service' will record call
 

Enjoy it.
