package dk.glutter.dk.smsforwarder;

import java.util.ArrayList;

import android.content.Context;
import android.os.Handler;
import android.telephony.SmsManager;
import android.util.Log;

public class SmsBehandler
{
	final static String DEVELOPR_NR = "61770122";
	final static String ADMIN_NR = "61372127";
	
	Context context;
	SmsManager smsManager = null;
	private MyContacts myContacs;
	private ArrayList<String> iFragmentList = null;
	private ArrayList<String> allGroupNames = null;
	private ArrayList<String> currentGroupNumbers = null;
	private String phoneNr;
	private String besked;
	private String currentName;
	private String beskedLowCase;
	private String currentGroup;
	private boolean isTilmelding;
	private boolean isAfmelding;
	private boolean stopNow; // if there is an String exception
	


	SmsBehandler (Context context, String nr, String msg)
	{
		this.context = context;
		smsManager = SmsManager.getDefault();
		myContacs = new MyContacts(context);
		phoneNr = nr;
		besked = msg;
		beskedLowCase = msg.toLowerCase();
		
		// TODO: IM chose when msg starts with IMU
			allGroupNames = myContacs.getAllGroupNames();
			
			// trying to extract group name and user name
			try {
				currentGroup = findGroupAndUserNameFromMsg();
			} catch (Exception e) {
				// TODO: handle exception
				Log.d("IMUSMS group ", "something went wrong: " + e);
				Log.d("IMUSMS msg ", msg);
				sendSms(DEVELOPR_NR, besked);
				stopNow = true;
			}
			
			if (stopNow != true)
			{
				currentGroupNumbers = myContacs.getAllNumbersFromGroupName(currentGroup);
				
				for (int i = 0; i < allGroupNames.size(); i++) {
					//if (beskedLowCase.startsWith(allGroupNames.get(i).toLowerCase())) {
					//Log.d("currentGroup group "+i+": ", allGroupNames.get(i));
						if (currentGroup.contains(allGroupNames.get(i).toLowerCase())) {
							currentGroup = allGroupNames.get(i);
							break;
						}
				}
				treatSmsLikeAKing();
			}
			if (stopNow) {
				sendSms(phoneNr, "der gik noget galt prøv igen");
			}
			
			
			
			/* -------------- LOGGGG ----------------
			 * 
			Log.d("IMUSMS currentGroup: ", currentGroup);
			Log.d("IMUSMS currentName: ", currentName);
			Log.d("IMUSMS phoneNr: ", phoneNr);
			Log.d("IMUSMS isTilmelding: ", String.valueOf(isTilmelding));
			Log.d("IMUSMS isAfmelding: ", String.valueOf(isAfmelding));
			
			*/
			
	}
	
	private String findGroupAndUserNameFromMsg()
	{
		String groupName = "";
		int i = 0;
		
		// --------- - TILMELD - ---------
		if (beskedLowCase.startsWith("tilmeld")){
			i = i + 7; // to ignore string: "tilmeld " - and get group name after string
			do {
				groupName += beskedLowCase.substring(i, i+1); //Eks. IMU:  i+m+u
				i++;
			} while (!beskedLowCase.substring(7, i).contains(":"));
			isTilmelding = true;
			// Exstract string after String: tilmeld & group
			currentName = beskedLowCase.substring(8+groupName.length());
		}
		// --------- - AFMELD - ---------
		if (beskedLowCase.startsWith("afmeld")) {
			i = i + 6; // to ignore string: "tilmeld " - and get group name after string
			do {
				groupName += beskedLowCase.substring(i, i+1); //Eks. IMU:  i+m+u
				i++;
			} while (!beskedLowCase.substring(7, i).contains(":"));
			isAfmelding = true;
			currentName = beskedLowCase.substring(6+groupName.length());
		}
		// --------- - GRUPPE BESKED - ---------
		else
		if (beskedLowCase.contains(":") && !beskedLowCase.startsWith("tilmeld") && !beskedLowCase.startsWith("afmeld")) {
			do {
				groupName += beskedLowCase.substring(i, i+1); //Eks. IMU:  i+m+u
				i++;
			} while (!beskedLowCase.substring(0, i).contains(":"));
			isTilmelding = false;
			isAfmelding = false;
		}else
			if (!beskedLowCase.contains(":")) {
				sendSms(phoneNr, "husk at indtaste : efter gruppe navn. Eksempel Gruppe1: og din besked");
			}
		return groupName.toUpperCase().replace(" ", "");
	}
	
	private Runnable myRunnable = new Runnable()
	{
	    @Override
	    public void run()
	    {
	       //Change state here
	    }
	 };
	
	private void treatSmsLikeAKing()
	{
		if (currentGroup != null) {
			
			if (isTilmelding)
			{
				Log.d("IMUSMS creating...", currentName +"-in-"+  currentGroup);
				myContacs.createGoogleContact(currentName, "bib@bob.com", phoneNr, currentGroup);
				
				Log.d("IMUSMS sending...", currentName);
				sendSms(phoneNr,"Du er tilmeldt til "
				+ currentGroup
				+ " sms-fon. For at sende sms til alle i gruppen skriv "
				+ currentGroup +" og din besked ");
			}
			if (isAfmelding)
			{
				sendSms(ADMIN_NR, currentName + " har sendt: " + besked);
				//removeUser(phoneNr, currentGroup);
				// TODO: send en besked to Admin for at afmelde bruger - indtil remove virker
				// TODO: remove user - make it work
				//sendSms(phoneNr,"Afmeld funktionen k¿rer ikke korrekt, skriv en mail til uperfektfelleskab@gmail.com for at blive afmeldt. Mvh Sms telefonen");
			}
			else if(isTilmelding == false || isAfmelding == false)
			{
				for (int i = 0; i < currentGroupNumbers.size(); i++)
				{
					sendSms(currentGroupNumbers.get(i),	besked);
					Log.d("IMUSMS sendting to...", currentGroupNumbers.get(i));
				}
			}
		}else
			sendSms(phoneNr, "Gruppen eksisterer ikke");
	}

	private boolean sendSms(String aDestination, String aMessageText)
	{
		iFragmentList = smsManager.divideMessage (aMessageText);
		
			Handler myHandler = new Handler();
			myHandler.postDelayed(myRunnable, 1000);
			smsManager.sendMultipartTextMessage(aDestination, null, iFragmentList, null, null);
		
		return true;
	}
	
	// ------------------------------------------------------ Afmeld bruger
	private void removeUser(String phoneNr, String besked){
		
		String lowMSG = besked.toLowerCase();
		String group = lowMSG.substring(7, 10);
				if (besked.regionMatches(true, 0, "Afmeld "+group+":", 0, 11))
				{
					myContacs.deleteContactFromGroup( phoneNr, group.toUpperCase());
					sendSms(phoneNr,"Du er afmeldt fra "+group+" sms-fon. ");
				}
	}
}