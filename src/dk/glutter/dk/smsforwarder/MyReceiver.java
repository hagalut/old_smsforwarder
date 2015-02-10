package dk.glutter.dk.smsforwarder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

/**
 * Created by luther on 09/02/15.
 */
public class MyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // Implement code here to be performed when
        // broadcast is detected

        Intent i = new Intent();
        i.setClassName("com.zegoggles.smssync", "com.zegoggles.smssync.BACKUP");
        context.startService(i);

        PackageManager pm = context.getPackageManager();
        Intent in = pm.getLaunchIntentForPackage("com.zegoggles.smssync.BACKUP");
        context.startActivity(in);


        Log.d("Backup Sync Action: ", "com.zegoggles.smssync.BACKUP");
    }
}

