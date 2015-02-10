package dk.glutter.dk.smsforwarder;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.telephony.SmsManager;
import android.util.Log;

public class SmsBehandler
{
	final static String DEVELOPR_NR = "61770122";
	final static String ADMIN_NR = "61770122";
	
	Context context;
	SmsManager smsManager = null;
	private MyContacts myContacs;
	private ArrayList<String> iFragmentList = null;
	private ArrayList<String> allGroupNames = null;
	private ArrayList<String> currentGroupNumbers = null;
	private String fromPhoneNr;
	private String besked;
	private String currentName;
	private String beskedLowCase;
	private String currentGroup;
    private String currSmsId;
	private boolean isTilmelding;
	private boolean isAfmelding;
	private boolean stopNow; // if there is an String exception
	


	SmsBehandler (Context context, String fromNr, String msg, String currSmsId)
	{
		this.context = context;
        this.currSmsId = currSmsId;
		smsManager = SmsManager.getDefault();
		myContacs = new MyContacts(context);
		fromPhoneNr = fromNr;
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
				sendSms(DEVELOPR_NR, besked, currSmsId);
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
				sendSms(fromPhoneNr, "der gik noget galt prøv igen", currSmsId);
			}
			
			
			
			/* -------------- LOGGGG ----------------
			 * 
			Log.d("IMUSMS currentGroup: ", currentGroup);
			Log.d("IMUSMS currentName: ", currentName);
			Log.d("IMUSMS fromPhoneNr: ", fromPhoneNr);
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

            return groupName.toUpperCase().replace(" ", "");
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

            return groupName.toUpperCase().replace(" ", "");
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
				sendSms(fromPhoneNr, "husk at indtaste : efter gruppe navn. Eksempel Gruppe1: og din besked", currSmsId);
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
				myContacs.createGoogleContact(currentName, "", fromPhoneNr, currentGroup);
				
				Log.d("IMUSMS sending...", currentName);
				sendSms(fromPhoneNr,"Du er tilmeldt til "
				+ currentGroup
				+ " sms-fon. For at sende sms til alle i gruppen skriv "
				+ currentGroup +" og din besked ", currSmsId);

                return;
			}
			if (isAfmelding)
			{
				sendSms(ADMIN_NR, currentName + " har sendt: " + besked, currSmsId);
				//removeUser(fromPhoneNr, currentGroup);
				// TODO: send en besked to Admin for at afmelde bruger - indtil remove virker
				// TODO: remove user - make it work
				//sendSms(fromPhoneNr,"Afmeld funktionen kører ikke korrekt, skriv en mail til uperfektfelleskab@gmail.com for at blive afmeldt. Mvh Sms telefonen");
                return;
			}
			else
			{

                ArrayList<String> tempNumbers = currentGroupNumbers;
                int groupUsersCount = tempNumbers.size();

                // send to all numbers in group
				for (int i = 0; i < groupUsersCount; i++)
				{
					sendSms(tempNumbers.get(i), besked, currSmsId);
					Log.d("IMUSMS sending to", tempNumbers.get(i));
				}

                return;
			}
		}else
			sendSms(fromPhoneNr, "Gruppen eksisterer ikke", currSmsId);
	}

	public boolean sendSms(final String aDestination, String aMessageText, final String currSmsId)
	{
		iFragmentList = smsManager.divideMessage (aMessageText);

        try {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    smsManager.sendMultipartTextMessage(aDestination, null, iFragmentList, null, null);

                    delete_thread(currSmsId);
                }
            }, 3300);
        }
        catch (Exception e)
        {
            Log.d("FAILED sending to: ", aDestination);
            return false;
        }
		return true;
	}

    public void delete_thread(String _id)
    {
        Cursor c = context.getContentResolver().query(
                Uri.parse("content://sms/"),new String[] {
                        "_id", "thread_id", "address", "person", "date","body" }, null, null, null);

        try {
            while (c.moveToNext())
            {
                int id = c.getInt(0);
                String address = c.getString(2);
                if (id == Integer.parseInt(_id))
                {
                    context.getContentResolver().delete(
                            Uri.parse("content://sms/" + id), null, null);
                }

            }
        } catch (Exception e) {

        }
    }
	
	// ------------------------------------------------------ Afmeld bruger
	private void removeUser(String phoneNr, String besked){
		
		String lowMSG = besked.toLowerCase();
		String group = lowMSG.substring(7, 10);
				if (besked.regionMatches(true, 0, "Afmeld "+group+":", 0, 11))
				{
					myContacs.deleteContactFromGroup( phoneNr, group.toUpperCase());
					sendSms(phoneNr,"Du er afmeldt fra "+group+" sms-fon. ", currSmsId);
				}
	}
}