package dk.glutter.dk.smsforwarder;

import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager.WakeLock;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class MainActivity extends Activity {

	TextView tv; TextView tv2;
	SmsManager smsManager = SmsManager.getDefault();
    SmsBehandler smsHandler;
	WakeLock mWakeLock;
	ArrayList<String> numbers = new ArrayList<String>();
	Handler handler;
	String text = "";
	String currSmsId = "";
	String currMsg = "";
	String currNr = "";
	int messageCount = 0;
    Runnable runnable = null;

    MyReceiver myReceiver = null;
    Boolean myReceiverIsRegistered = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);

		tv = (TextView) findViewById(R.id.textView1);

        run();

	}

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

    @Override
    protected void onPause() {super.onPause();}
    @Override
    protected void onResume() {super.onResume();}

    private void run()
    {
        if(runnable != null)
            handler.removeCallbacks(runnable);

        if(handler == null) {
            handler = new Handler();

            runnable = new Runnable() {
                public void run() {

                    messageCount = getAllSms().size();

                    text = "There are " + messageCount + " sms's in your inbox : ";
                    currSmsId = null;

                    if (messageCount > 0) {
                        for (int i = 0; i < messageCount; i++) {
                            currMsg = getAllSms().get(i).getMsg();

                            text = "besked " + i + " fra " + "  " + getAllSms().get(i).getAddress() + ": " + currMsg;
                            currSmsId = getAllSms().get(i).getId();

                            smsHandler = new SmsBehandler(getApplicationContext(), getAllSms().get(i).getAddress(), currMsg, currSmsId);
                        }
                    }

                    if (currSmsId == null)
                        text = "There are currently " + messageCount + " messages in your inbox : ";

                    tv.setText(text);

                    handler.postDelayed(this, 60000); // now is every 1 minutes
                }
            };

            handler.postDelayed(runnable , 3300); // Every 120000 ms (2 minutes)
        }

    }

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private List<Sms> getAllSms() {

        int currentApiVersion = Build.VERSION.SDK_INT;
        List<Sms> lstSms = new ArrayList<Sms>();

        if (currentApiVersion > 10) {

            Sms objSms = new Sms();
            Uri message = Uri.parse("content://sms/");

            CursorLoader cl = new CursorLoader(getApplicationContext());
            cl.setUri(message);
            //cl.setSelection("content://sms/");
            Cursor c = cl.loadInBackground();


            int totalSMS = c.getCount();

            if (c.moveToFirst()) {
                for (int i = 0; i < totalSMS; i++) {

                    objSms = new Sms();
                    objSms.setId(c.getString(c.getColumnIndexOrThrow("_id")));
                    objSms.setAddress(c.getString(c
                            .getColumnIndexOrThrow("address")));
                    objSms.setMsg(c.getString(c.getColumnIndexOrThrow("body")));
                    objSms.setReadState(c.getString(c.getColumnIndex("read")));
                    objSms.setTime(c.getString(c.getColumnIndexOrThrow("date")));
                    if (c.getString(c.getColumnIndexOrThrow("type")).contains("1")) {
                        objSms.setFolderName("inbox");
                    } else {
                        objSms.setFolderName("sent");
                    }

                    lstSms.add(objSms);
                    c.moveToNext();
                }
            }
            c.close();

            return lstSms;
        }
        else
        {
            lstSms = getAllSmsAPI10();
        }
		return lstSms;
	}

    @TargetApi(Build.VERSION_CODES.GINGERBREAD_MR1)
    public List<Sms> getAllSmsAPI10() {
        List<Sms> lstSms = new ArrayList<Sms>();
        Sms objSms = new Sms();
        Uri message = Uri.parse("content://sms/");
        ContentResolver cr = this.getContentResolver();

        Cursor c = cr.query(message, null, null, null, null);

        this.startManagingCursor(c);


        int totalSMS = c.getCount();

        if (c.moveToFirst()) {
            for (int i = 0; i < totalSMS; i++) {

                objSms = new Sms();
                objSms.setId(c.getString(c.getColumnIndexOrThrow("_id")));
                objSms.setAddress(c.getString(c
                        .getColumnIndexOrThrow("address")));
                objSms.setMsg(c.getString(c.getColumnIndexOrThrow("body")));
                objSms.setReadState(c.getString(c.getColumnIndex("read")));
                objSms.setTime(c.getString(c.getColumnIndexOrThrow("date")));
                if (c.getString(c.getColumnIndexOrThrow("type")).contains("1")) {
                    objSms.setFolderName("inbox");
                } else {
                    objSms.setFolderName("sent");
                }

                lstSms.add(objSms);
                c.moveToNext();
            }
        }
        c.close();

        return lstSms;
    }
	
	public void deleteAllSmsS() 
	{ 
	  Cursor c = getApplicationContext().getContentResolver().query(
	  Uri.parse("content://sms/"),new String[] { 
	  "_id", "thread_id", "address", "person", "date","body" }, null, null, null);

	 try {
	  while (c.moveToNext()) 
	      {
	    int id = c.getInt(0);
	     getApplicationContext().getContentResolver().delete(
	     Uri.parse("content://sms/" + id), null, null);

	       }
	} catch (Exception e) {

	  }
	}
}
