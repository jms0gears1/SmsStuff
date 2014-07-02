package com.example.smsstuff;

import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.Service;
import android.content.ContentValues;
import android.content.Intent;
import android.os.IBinder;
import android.provider.Telephony.Sms;
import android.provider.Telephony.Sms.Inbox;
import android.telephony.SmsMessage;
import android.text.TextUtils;
import android.util.Log;

import com.example.smsstuff.util.Utils;

public class SmsDatabaseService extends Service{

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		Log.d("SmsDatabaseService", "received intent, service started");
		SmsMessage[] msg = Sms.Intents.getMessagesFromIntent(intent);
		this.pushSmsToDatabase(msg);
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	
	public void pushSmsToDatabase(SmsMessage[] msg){
		ContentValues values = extractContentValues(msg[0]);
		
		if(msg.length == 1){
			values.put(Sms.Inbox.BODY, replaceFormFeeds(msg[0].getDisplayMessageBody()));
		}else{
			StringBuilder body = new StringBuilder();
			for(SmsMessage sms: msg){
				body.append(sms.getDisplayMessageBody());
			}
		}
		
		Long threadId = values.getAsLong(Sms.THREAD_ID);
		String address = values.getAsString(Sms.ADDRESS);
		
		if(TextUtils.isEmpty(address)){
			address = "0000000000";
		}
		
		values.put(Sms.THREAD_ID, threadId);
		values.put(Sms.ADDRESS, address);
		
		getApplicationContext().getContentResolver().insert(Utils.SMS_INBOX_URI, values);
		SMSBroadcastReceiver.finishDatabaseService(Utils.getContactIDFromPhoneNumber(getApplicationContext(), address));
		this.stopSelf();
	}
	
	private ContentValues extractContentValues(SmsMessage sms) {
        // Store the message in the content provider.
        ContentValues values = new ContentValues();

        values.put(Sms.Inbox.ADDRESS, sms.getDisplayOriginatingAddress());

        // Use now for the timestamp to avoid confusion with clock
        // drift between the handset and the SMSC.
        // Check to make sure the system is giving us a non-bogus time.
        Calendar buildDate = new GregorianCalendar(2011, 8, 18);    // 18 Sep 2011
        Calendar nowDate = new GregorianCalendar();
        long now = System.currentTimeMillis();
        nowDate.setTimeInMillis(now);

        if (nowDate.before(buildDate)) {
            // It looks like our system clock isn't set yet because the current time right now
            // is before an arbitrary time we made this build. Instead of inserting a bogus
            // receive time in this case, use the timestamp of when the message was sent.
            now = sms.getTimestampMillis();
        }

        values.put(Inbox.DATE, new Long(now));
        values.put(Inbox.DATE_SENT, Long.valueOf(sms.getTimestampMillis()));
        values.put(Inbox.PROTOCOL, sms.getProtocolIdentifier());
        values.put(Inbox.READ, 0);
        values.put(Inbox.SEEN, 0);
        if (sms.getPseudoSubject().length() > 0) {
            values.put(Inbox.SUBJECT, sms.getPseudoSubject());
        }
        values.put(Inbox.REPLY_PATH_PRESENT, sms.isReplyPathPresent() ? 1 : 0);
        values.put(Inbox.SERVICE_CENTER, sms.getServiceCenterAddress());
        return values;
    }
	
	 public static String replaceFormFeeds(String s) {
	     // Some providers send formfeeds in their messages. Convert those formfeeds to newlines.
		 return s == null ? "" : s.replace('\f', '\n');
	 }

}
