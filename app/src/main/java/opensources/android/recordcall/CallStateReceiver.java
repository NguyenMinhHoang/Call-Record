package opensources.android.recordcall;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;


/**
 * The receiver receive broadcast from call system when have incoming call, outgoing call...whenever have 'call state' change
 */
public class CallStateReceiver extends BroadcastReceiver{
    private static final String TAG = CallStateReceiver.class.getSimpleName();
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG, "onReceive ...");
        PhoneStatusListener phoneListener = new PhoneStatusListener(context);
        TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        telephony.listen(phoneListener, PhoneStateListener.LISTEN_CALL_STATE);
    }

    /**
     * check phone state to detect when have incoming call, outgoing call or end call
     */
    public class PhoneStatusListener extends PhoneStateListener{
        private Context context;

        public PhoneStatusListener(Context c) {
            context = c;
        }

        @Override
        public void onCallStateChanged (int state, String incomingNumber)
        {
            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE:
                    Log.d(TAG, "onCallStateChanged ... CALL_STATE_IDLE");
                    Boolean stopped = context.stopService(new Intent(context, CallRecordService.class));
                    break;
                case TelephonyManager.CALL_STATE_RINGING:
                    Log.d(TAG, "onCallStateChanged ... CALL_STATE_RINGING");
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    Log.d(TAG, "onCallStateChanged ... CALL_STATE_OFFHOOK");
                    Intent callIntent = new Intent(context, CallRecordService.class);
                    context.startService(callIntent);
                    break;
            }
        }
    }
}

