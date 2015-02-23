package dk.glutter.dk.smsforwarder;

/**
 * Created by izbrannick on 23-02-2015.
 */
public class StringValidator {



    // checks if message contains requested char ':'
    public static boolean isMessageValid(String message)
    {
        if (!message.isEmpty()) {
            String[] splitedMessage = message.split(" ");
            if (splitedMessage.length > 1) {
                String firstAndSecondWord = splitedMessage[0] + splitedMessage[1];
                if (firstAndSecondWord.contains(":")) {
                    return true;
                }
            }
        }
        return false;
    }
}



