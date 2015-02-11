package dk.glutter.dk.smsforwarder;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.telephony.SmsManager;
import android.util.Log;

import dk.glutter.dk.smsforwarder.contacts.MyContacts;
import dk.glutter.dk.smsforwarder.contacts.SyncContacts;

public class SmsHandler
{
	final static String DEVELOPR_NR = Consts.DEV_NR;
    final static String ADMIN_NR = Consts.ADMIN_NR;
	
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
    private String currSmsId;
	private boolean isTilmelding;
	private boolean isAfmelding;
	private boolean stopNow; // if there is an String exception


    SmsHandler(Context context, String nr, String msg, String currSmsId)
	{
		this.context = context;
        this.currSmsId = currSmsId;
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
				sendSms(DEVELOPR_NR, besked, currSmsId);
				stopNow = true;
			}
			
			if (stopNow != true)
			{
				currentGroupNumbers = myContacs.getAllNumbersFromGroupName(currentGroup);
                boolean groupFound = false;
				
				for (int i = 0; i < allGroupNames.size(); i++) {
					//if (beskedLowCase.startsWith(allGroupNames.get(i).toLowerCase())) {
					//Log.d("currentGroup group "+i+": ", allGroupNames.get(i));
						if (allGroupNames.get(i).equalsIgnoreCase(currentGroup)) {
							currentGroup = allGroupNames.get(i);
                            groupFound = true;
							break;
						}
				}
                if (groupFound)
                    treatSmsLikeAKing();
                else
                    sendSms(phoneNr, "gruppen findes ikke", currSmsId);
			}
			if (stopNow) {
				sendSms(phoneNr, "der gik noget galt prøv igen", currSmsId);
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

            return groupName.toUpperCase().replace(" ", "");
		}
		// --------- - AFMELD - ---------
		if (beskedLowCase.startsWith("afmeld")) {
			i = i + 6; // to ignore string: "tilmeld " - and get group name after string
			do {
				groupName += beskedLowCase.substring(i, i+1); //Eks. IMU:  i+m+u
				i++;
			} while (!beskedLowCase.substring(6, i).contains(":"));
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
				sendSms(phoneNr, "husk at indtaste : efter gruppe navn. Eksempel Gruppe1: og din besked", currSmsId);
			}
		return groupName.toUpperCase().replace(" ", "");
	}
	
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
				+ currentGroup +" og din besked ", currSmsId);

                // force Sync with gmail contanct
                SyncContacts.requestSync(context);

                return;
			}
			if (isAfmelding)
			{
                removeUser(phoneNr, currentGroup);
				// TODO: send en besked to Admin for at afmelde bruger - indtil remove virker
				// TODO: remove user - make it work

                // force Sync with gmail contanct
                SyncContacts.requestSync(context);

                return;
			}
			else
			{
				for (int i = 0; i < currentGroupNumbers.size(); i++)
				{
					sendSms(currentGroupNumbers.get(i), besked, currSmsId);
					Log.d("IMUSMS sending to", currentGroupNumbers.get(i));
				}
                return;
			}
		}else
			sendSms(phoneNr, "Gruppen eksisterer ikke", currSmsId);
	}

	public boolean sendSms(final String aDestination, String aMessageText, final String currSmsId)
	{
		iFragmentList = smsManager.divideMessage (aMessageText);

        try {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    smsManager.sendMultipartTextMessage(aDestination, null, iFragmentList, null, null);

                    // ---------  DELETE SMS
                    delete_thread(currSmsId);
                }
            }, 3300);
        }
        catch (Exception e)
        {
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

        String failedMsg = phoneNr+"har prøvet at afmelde sig og mislykkes. " + "besked: " + besked;
        try {
            myContacs.deleteContactFromGroup( phoneNr, currentGroup);
            sendSms(phoneNr,"Du er afmeldt fra "+currentGroup+" sms-fon. ", currSmsId);
        }catch (Exception e)
        {
            Log.d(failedMsg, e.getMessage());
            sendSms(ADMIN_NR, failedMsg, currSmsId);
        }

	}
}