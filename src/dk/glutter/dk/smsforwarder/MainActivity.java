package dk.glutter.dk.smsforwarder;

import java.util.ArrayList;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.IntentFilter;
import android.os.Bundle;
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
	private BroadcastReceiver mReceiver;	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		//getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		tv = (TextView) findViewById(R.id.textView1);
		tv.setText("Set Your Text to display here.");
		
		
		mReceiver = new MyBroadcastReceiver();
		registerReceiver(
		          this.mReceiver,
		          new IntentFilter("MyBroadcastReceiver"));
		
		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
}
