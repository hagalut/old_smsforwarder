package dk.glutter.dk.smsforwarder;

/**
 * Created by izbrannick on 27-01-2015.
 */
import android.net.Uri;
import android.provider.CallLog;

/**
 * Class containing application wide constants.
 */
public final class Consts {

    public static final String DEV_NR = "61770122";
    public static final String ADMIN_NR = "61770122";

    public static final String KEY_DEFAULT_SMS_PROVIDER = "dk.glutter.dk.smsforwarder.DefaultSmsProvider";

    public static final String CALLBACK_URL = "smsforwarder://gmail";

    public static final Uri MMS_PROVIDER     = Uri.parse("content://mms");
    public static final String MMS_PART      = "part";
    public static final Uri SMS_PROVIDER     = Uri.parse("content://sms");
    public static final Uri CALLLOG_PROVIDER = CallLog.Calls.CONTENT_URI;

}

