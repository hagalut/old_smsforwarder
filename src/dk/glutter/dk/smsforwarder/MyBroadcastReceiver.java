package dk.glutter.dk.smsforwarder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.widget.Toast;

public class MyBroadcastReceiver extends BroadcastReceiver {

	static String beskedOld = "";
	String phoneNr ="";
	
	@Override
	public void onReceive(Context context, Intent intent) {
		SmsMessage[] msg = null;
		String besked = "";
		
		Bundle bundle = intent.getExtras();
		Object[] pdus = (Object[]) bundle.get("pdus");
		msg = new SmsMessage[pdus.length];
		for (int i = 0; i < pdus.length; i++) {
			msg[i] = SmsMessage.createFromPdu((byte[]) pdus[i]);
			phoneNr = msg[i].getOriginatingAddress();
		}

		SmsMessage sms = msg[0];
		try {
			if (msg.length == 1 || sms.isReplace()) {
				besked = sms.getDisplayMessageBody();
			} else {
				StringBuilder bodyText = new StringBuilder();
				for (int i = 0; i < msg.length; i++) {
					bodyText.append(msg[i].getMessageBody());
				}
				besked = bodyText.toString();	
			}
		} catch (Exception e) {
		}
		
		if (besked.equals(beskedOld)) {
			Toast.makeText(context, " == ", 222).show();
		}else
		{
			//Toast.makeText(context, " != ", 222).show();
			SmsBehandler smsBehandler = new SmsBehandler(context, phoneNr, besked);
		}
	
		beskedOld = besked;
	}
	
	

}