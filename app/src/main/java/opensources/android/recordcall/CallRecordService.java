package opensources.android.recordcall;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 *
 */
public class CallRecordService extends Service implements MediaRecorder.OnInfoListener, MediaRecorder.OnErrorListener{
    private static String TAG = CallRecordService.class.getSimpleName();
    private MediaRecorder recorder = null;
    private boolean isRecording = false;
    private String mRecordFile;
    @Override
    public void onCreate() {
        super.onCreate();
        recorder = new MediaRecorder();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        try {
            if (isRecording) return super.onStartCommand(intent, flags, startId);
            Log.d(TAG, "onStartCommand start recording....");
            //start recording call
            recorder.reset();
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_HHmmss");
            String fileName = sdf.format(Calendar.getInstance().getTime());
            String path = getCacheDir(getApplicationContext());
            mRecordFile = File.createTempFile(fileName, ".mpeg4", new File(path)).getAbsolutePath();
            recorder.setOutputFile(mRecordFile);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            recorder.setOnInfoListener(this);
            recorder.setOnErrorListener(this);
            recorder.prepare();
            recorder.start();
            isRecording = true;
            Log.d(TAG, "onStartCommand end recording....");
        } catch (java.io.IOException e) {
            e.printStackTrace();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (null != recorder) {
            Log.d(TAG, "onDestroy stop recording service....");
            recorder.release();
            isRecording = false;
            Toast.makeText(getApplicationContext(), "The file is saved at : " + mRecordFile, Toast.LENGTH_LONG).show();
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onError(MediaRecorder mr, int what, int extra) {

    }

    @Override
    public void onInfo(MediaRecorder mr, int what, int extra) {

    }

    public static String getCacheDir(Context context) {
        // Check if media is mounted or storage is built-in, if so, try and use external cache dir
        // otherwise use internal cache dir
        final String cachePath =
                Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||
                        !Environment.isExternalStorageRemovable() ? context.getExternalCacheDir().getPath() :
                        context.getCacheDir().getPath();

        return cachePath;
    }
}
