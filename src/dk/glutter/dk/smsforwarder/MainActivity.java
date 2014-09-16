package dk.glutter.dk.smsforwarder;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager.WakeLock;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public class MainActivity extends Activity {

	TextView tv;
	SmsManager smsManager = SmsManager.getDefault();
	WakeLock mWakeLock;
	ArrayList<String> numbers = new ArrayList<String>();
	Handler handler;
	String text = "";
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		tv = (TextView) findViewById(R.id.textView1);
		tv.setText("Set Your Text to display here.");
		
		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		
		handler = new Handler();
		
		handler.postDelayed(new Runnable() {
		    public void run() {
		    	text = "";
		    	for (int i = 0; i < getAllSms().size(); i++) {
					text += "besked " + i + ": " + getAllSms().get(i).toString() + " - ";
				}
		    	tv.setText(text);
		    	
		    	if ( getAllSms().size() > 0)
		    	{
		    		deleteSMS(getAllSms().get(0).getId());
		    	}

		        handler.postDelayed(this, 3300); //repeat every 120000 ms (2 minutes)
		    }
		 }, 3300); //Every 120000 ms (2 minutes)
		
		
		//Toast.makeText(context, " != ", 222).show();
		
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	public void deleteSMS( String _id ) {
	    try {
	        //mLogger.logInfo("Deleting SMS from inbox");
	        Uri uriSms = Uri.parse("content://sms/inbox");
	        Cursor c = this.getContentResolver().query(uriSms, new String[] { "_id", "thread_id" }, null, null, null);

	        if (c != null && c.moveToFirst()) {
	            do {
	                String id = c.getString(0);
	                //long threadId = c.getLong(1);

	                if (id.equals(_id)) {
	                    //mLogger.logInfo("Deleting SMS with id: " + threadId);
	                    this.getContentResolver().delete(
	                        Uri.parse("content://sms/" + id), null, null);
	                }
	            } while (c.moveToNext());
	        }
	    } catch (Exception e) {
	        //mLogger.logError("Could not delete SMS from inbox: " + e.getMessage());
	    }
	}
	
	@SuppressWarnings("deprecation")
	public List<Sms> getAllSms() {
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
		// else {
		// throw new RuntimeException("You have no SMS");
		// }
		c.close();

		return lstSms;
	}
}
