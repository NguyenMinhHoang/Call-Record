package opensources.android.recordcall;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.os.IBinder;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 *
 */
public class CallRecordService extends Service implements MediaRecorder.OnInfoListener, MediaRecorder.OnErrorListener, Loader.OnLoadCompleteListener<Cursor> {
    private static String TAG = CallRecordService.class.getSimpleName();
    private static int CALLLOG_LOADER = 11;

    private MediaRecorder recorder = null;
    private boolean isRecording = false;
    private boolean isSaving = false;
    private String mRecordFile;
    private CursorLoader mCursorLoader;
    @Override
    public void onCreate() {
        super.onCreate();
        isSaving = false;
        isRecording = false;
        recorder = new MediaRecorder();
        mCursorLoader = new CursorLoader(this, CallLog.Calls.CONTENT_URI, null, null, null, CallLog.Calls.DATE + " DESC LIMIT 1");
        mCursorLoader.registerListener(CALLLOG_LOADER, this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            Log.e(TAG, "The intent is null somehow.");
            return super.onStartCommand(intent, flags, startId);
        }
        try {
            int action = intent.getIntExtra("action", 0);
            switch (action) {
                case 1://record
                    if (isRecording) return super.onStartCommand(intent, Service.START_FLAG_REDELIVERY, startId);
                    isRecording = true;
                    Log.d(TAG, "onStartCommand start recording....");
                    //start recording call
                    recorder.reset();
                    recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                    recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault());
                    String fileName = sdf.format(Calendar.getInstance().getTimeInMillis());
                    String path = getCacheDir(getApplicationContext());
                    mRecordFile = File.createTempFile(fileName, ".m4a", new File(path)).getAbsolutePath();
                    recorder.setOutputFile(mRecordFile);
                    recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                    recorder.setOnInfoListener(this);
                    recorder.setOnErrorListener(this);
                    recorder.prepare();
                    recorder.start();
                    break;
                case 2://save file
                    if (isSaving || !isRecording) return super.onStartCommand(intent, Service.START_FLAG_REDELIVERY, startId);
                    isSaving = true;
                    stopRecording();
                    Log.d(TAG, "onStartCommand is saving file...");
                    mCursorLoader.startLoading();
                    break;
            }
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
        return super.onStartCommand(intent, Service.START_FLAG_REDELIVERY, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.w(TAG, "onDestroy stop recording service....");
        try {
            stopRecording();
            // Stop the cursor loader
            if (mCursorLoader != null) {
                isSaving = false;
                mCursorLoader.unregisterListener(this);
                mCursorLoader.cancelLoad();
                mCursorLoader.stopLoading();
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onLoadComplete(Loader<Cursor> loader, Cursor cursor) {
        readCallLog(cursor);
    }

    @Override
    public void onError(MediaRecorder mr, int what, int extra) {

    }

    @Override
    public void onInfo(MediaRecorder mr, int what, int extra) {

    }

    private void stopRecording() {
        try {
            if (null != recorder) {
                Log.d(TAG, "stop recording...");
                recorder.release();
                isRecording = false;
                recorder = null;
            }
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String getCacheDir(Context context) {
        // Check if media is mounted or storage is built-in, if so, try and use external cache dir
        // otherwise use internal cache dir
        final String cachePath = context.getFilesDir().getAbsolutePath();
                /*Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                        !Environment.isExternalStorageRemovable() ? Environment.getExternalStorageDirectory().getAbsolutePath()*//*context.getExternalCacheDir().getPath()*//* :
                        //context.getCacheDir().getPath();
                        context.getFilesDir().getAbsolutePath();*/

        return cachePath;
    }

    private String phoneNumber = "";
    private void readCallLog(Cursor calllogCursor) {
        StringBuffer sb = new StringBuffer();
        int number = calllogCursor.getColumnIndex(CallLog.Calls.NUMBER);
        int type = calllogCursor.getColumnIndex(CallLog.Calls.TYPE);
        int date = calllogCursor.getColumnIndex(CallLog.Calls.DATE);
        int duration = calllogCursor.getColumnIndex(CallLog.Calls.DURATION);
        //CallLog.Calls.
        sb.append("Call Log :");
        if (calllogCursor.moveToFirst()) {
            String recordId = calllogCursor.getString(calllogCursor.getColumnIndex(CallLog.Calls._ID));
            String phNumber = calllogCursor.getString(number);
            String callType = calllogCursor.getString(type);
            String callDate = calllogCursor.getString(date);
            Date callDayTime = new Date(Long.valueOf(callDate));
            String callDuration = calllogCursor.getString(duration);
            String dir = null;
            int dircode = Integer.parseInt(callType);
            switch (dircode) {
                case CallLog.Calls.OUTGOING_TYPE:
                    dir = "OUTGOING";
                    break;
                case CallLog.Calls.INCOMING_TYPE:
                    dir = "INCOMING";
                    break;
                case CallLog.Calls.MISSED_TYPE:
                    dir = "MISSED";
                    break;
            }
            sb.append("\nPhone Number:--- " + phNumber + " \nCall Type:--- " + dir + " \nCall Date:--- " + callDayTime + " \nCall duration in sec :--- " + callDuration);
            sb.append("\n----------------------------------");
            Log.d("HoangNM", "calllog: " + sb);
            phoneNumber = phNumber;
            readContacts(phoneNumber);
            renameFile();
            Log.d(TAG, "Saving file completely...");
            stopSelf();//stop service
            //readContacts(phNumber);
            //saveRecord(recordId, contactId, callDayTime, dircode, Long.valueOf(callDuration), mRecordFilePath);
            //RecordLogAccess recordLogAccess = new RecordLogAccess(this);
            //RecordLogModel recordLogModel = recordLogAccess.getRecordLog(recordId);
            //Log.d("HoangNM", "record id: " + recordLogModel.getRecordId());
        }
    }

    private String contactName = "NoName";
    private void readContacts(String phoneNumber) {
        ContentResolver cr = getContentResolver();
        // encode the phone number and build the filter URI
        Uri contactUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cur = cr.query(contactUri, null, null, null, null);
        if (cur != null) {
            //if have contact info
            if (cur.moveToFirst()) {
                contactName = cur.getString(cur.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                Log.d(TAG, "contact info: " + phoneNumber + " --- " + contactName);
                cur.close();
            }else {//this is strange call
                //contactPhone = phoneNumber;
            }
        }
    }

    private void renameFile() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss", Locale.getDefault());
        String dateFormat = sdf.format(Calendar.getInstance().getTimeInMillis());
        String path = getCacheDir(getApplicationContext());
        File newFile = new File(path + File.separator + contactName + "_" + phoneNumber + "_" + dateFormat + ".m4a");
        File currentFile = new File(mRecordFile);
        boolean ok = currentFile.renameTo(newFile);
        if (ok) {
            Toast.makeText(getApplicationContext(), "File is saved.", Toast.LENGTH_SHORT).show();
        }
    }
}
