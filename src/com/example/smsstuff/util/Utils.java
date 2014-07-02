package com.example.smsstuff.util;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
public class Utils {

	public final static Uri MMS_SMS_CONVERSATIONS_URI = Uri.parse("content://mms-sms/conversations");
	public static final Uri SMS_SENT_URI = Uri.parse("content://sms/sent");
	public static final Uri SMS_INBOX_URI = Uri.parse("content://sms/inbox");
	private static final String MMS_IDENTIFIER = "application/vnd.wap.multipart.related";
	/**
	 * 
	 * @param context the context of the application (use getApplicationContext())
	 * @param address the phone number used to look up the contact
	 * @return the display name of the contact from phone number
	 */
	public static String getContactIDFromPhoneNumber(Context context, String address){
		Uri uri = Uri.withAppendedPath(ContactsContract.CommonDataKinds.Phone.CONTENT_FILTER_URI, address);
		
		Cursor cursor = context.getContentResolver().query(
				uri, 
				new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, 
				null, null, null);
		
		if(cursor!=null && cursor.moveToFirst()){
				try{
					return cursor.getString(0);
				}finally{
				cursor.close();
			}
		}
		
		return PhoneNumberUtils.formatNumber(address);		
	}
	
	public static MessageItem[] mergeMessages(MessageItem[] inbox, MessageItem[] sentbox){
		MessageItem[] mergeList = new MessageItem[inbox.length+sentbox.length];
		int left = 0;
		int right = 0;
		int index = 0;
		
		while(left < inbox.length && right < sentbox.length){
			if(Long.parseLong(inbox[left].date) < Long.parseLong(sentbox[right].date))
				mergeList[index++] = inbox[left++];
			else
				mergeList[index++] = sentbox[right++];	
		}
		
		while(left < inbox.length){
			mergeList[index++] = inbox[left++];
		}
		
		while(right < sentbox.length){
			mergeList[index++] = sentbox[right++];
		}
		
		return mergeList;
	}
	public static boolean isMMS(MessageItem item){
		return MMS_IDENTIFIER.equals(item.ct_t);
	}
	
	public static class ThreadItem{
		public String id;
		public String thread_id;
		public String address;
		public String body;
		public String name;
		
		public ThreadItem(String id, String thread_id, String address, String body) {
			super();
			this.id = id;
			this.thread_id = thread_id;
			this.address = address;
			this.body = body==null ? "null" : body;
		}
	}
	
	public static class MessageItem{
		
		public boolean isMine;
		public String id;
		public String thread_id;
		public String address;
		public String ct_t;
		public String date;
		public String body;
	}
}
