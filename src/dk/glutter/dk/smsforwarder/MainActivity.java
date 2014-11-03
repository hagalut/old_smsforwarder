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

	TextView tv; TextView tv2;
	SmsManager smsManager = SmsManager.getDefault();
	WakeLock mWakeLock;
	ArrayList<String> numbers = new ArrayList<String>();
	Handler handler;
	String text = "";
	String currSmsId = "";
	String currMsg = "";
	String currNr = "";
	int messageCount = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		
		tv = (TextView) findViewById(R.id.textView1);
		
		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
		
		handler = new Handler();
		handler.postDelayed(new Runnable() {
			public void run() {

				messageCount = getAllSms().size();

				text = "There are " + messageCount + " sms's in your inbox : ";
				currSmsId = null;
				
				if (messageCount > 0)
				for (int i = 0; i < messageCount; i++) {
					
					currMsg = getAllSms().get(i).getMsg();
					
					text = "besked " + i + " fra " + "  " + getAllSms().get(i).getAddress() + ": " + currMsg;
					currSmsId = getAllSms().get(i).getId();
					
					
					// TODO: 61770122 to be replaced with  getAllSms().get(i).getAddress()
					SmsBehandler smsHandler = new SmsBehandler(getApplicationContext() , "61770122", currMsg);
					delete_thread(currSmsId);
					
				}
				if (currSmsId != null){
				
					//delete_thread(currSmsId);
					
					
					deleteAllSmsS();
				}
				else
					text = "There are currently " + messageCount + " messages in your inbox : ";

				tv.setText(text);

				handler.postDelayed(this, 60000); // now is every 1 minutes
			}
		}, 3300); // Every 120000 ms (2 minutes)
		
		//Toast.makeText(context, " != ", 222).show();
		//SmsBehandler smsBehandler = new SmsBehandler(context, phoneNr, besked);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
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
		c.close();

		return lstSms;
	}
	
	
	public void delete_thread(String _id) 
	{ 
	  Cursor c = getApplicationContext().getContentResolver().query(
	  Uri.parse("content://sms/"),new String[] { 
	  "_id", "thread_id", "address", "person", "date","body" }, null, null, null);

	 try {
	  while (c.moveToNext()) 
	      {
	    int id = c.getInt(0);
	    String address = c.getString(2);
	    if (id == Integer.parseInt(_id))
	        {
	     getApplicationContext().getContentResolver().delete(
	     Uri.parse("content://sms/" + id), null, null);
	    }

	       }
	} catch (Exception e) {

	  }
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
