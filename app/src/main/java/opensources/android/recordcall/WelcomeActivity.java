package opensources.android.recordcall;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

public class WelcomeActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PERMISSION_RECORD_AUDIO = 101;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //request permission on android 6 or later
        checkPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] requestPermissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, requestPermissions, grantResults);
        switch (requestCode) {
            case REQUEST_CODE_PERMISSION_RECORD_AUDIO: {
                if (isPermissionGranted(requestPermissions, grantResults)) {
                    Toast.makeText(this,"SUCCESSFUL ! This permission need to demonstrate this sample.", Toast.LENGTH_LONG).show();
                }else {
                    Toast.makeText(this,"FAIL ! This permission need to demonstrate this sample.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    private void checkPermissions() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_PHONE_STATE}, REQUEST_CODE_PERMISSION_RECORD_AUDIO);
    }

    public static boolean isPermissionGranted(String[] requestPermissions, int[] grantResults) {
        for (int i = 0; i < requestPermissions.length; i++) {
            if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                return false;
            }
        }
        return true;
    }
}