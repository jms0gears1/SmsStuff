package com.example.smsstuff;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.graphics.Color;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.smsstuff.util.AsyncCursorHandler;
import com.example.smsstuff.util.AsyncCursorHandler.AsyncCursorListener;
import com.example.smsstuff.util.Utils;
import com.example.smsstuff.util.Utils.MessageItem;

public class ConversationThreadAdapter extends BaseAdapter{
	
	private final static String TAG = Class.class.getSimpleName();
	
	protected final static int TOKEN_INBOX = 1000;
	protected final static int TOKEN_SENT  = 1001;

	private AsyncCursorHandler handler;
	private String mThreadId;
	private MessageItem[] messages;
	private MessageItem[] inboxMessages;
	private MessageItem[] sentboxMessages;
	private Context context;
	
	
	public ConversationThreadAdapter(Context context, String ThreadId){
		this.context = context;
		this.mThreadId = ThreadId;
		this.refreshData();
	}
	
	public void refreshData(){
		Log.d(TAG, "refreshData()");
		initCursor();		
		getConversations();
	}
	
	public void initCursor(){
		handler = new AsyncCursorHandler(
				context,
				new ConversationQueryHandler()
				);	
	}
	
	public void getConversations(){		
		getInbox();
	}
	
	public void getInbox(){
		handler.cancelOperation(TOKEN_INBOX);
		
		handler.startQuery(
				TOKEN_INBOX, 
				null, Utils.SMS_INBOX_URI, 
				new String[]{"_id", "date", "body"}, 
				"thread_id=?", new String[]{mThreadId}, 
				"date ASC");
	}
	
	public void getSendBox(){
		handler.cancelOperation(TOKEN_SENT);
		
		handler.startQuery(
				TOKEN_SENT, 
				null, Utils.SMS_SENT_URI, 
				new String[]{"_id", "date", "body"}, 
				"thread_id=?", new String[]{mThreadId}, 
				"date ASC");
	}
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return messages==null?0:messages.length;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View mView = null;
		
		if(convertView == null){
			LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mView = inflater.inflate(R.layout.message_adapter, null);
		}else{
			mView = convertView;
		}

		boolean isMine = messages[position].isMine;
		TextView msg = (TextView)mView.findViewById(R.id.tvMessage);
		msg.setText(messages[position].body);
		msg.setGravity(isMine?Gravity.RIGHT:Gravity.LEFT);
		msg.setTextColor(isMine?Color.GRAY:Color.BLACK);
		msg.setPadding(isMine?200:10, 5, isMine?10:200, 5);
		
		return mView;
	}
	
	public void setData(MessageItem[] msg){
		this.messages = msg;
		this.notifyDataSetChanged();
	}
	
	private class ConversationQueryHandler implements AsyncCursorListener{

		@Override
		public void onQueryCompleted(int token, Cursor cursor) {
			switch(token){
			case TOKEN_SENT:
				if(cursor!=null && cursor.moveToFirst()){
					sentboxMessages = new MessageItem[cursor.getCount()];
					int index = 0;
					do{
						MessageItem item = new MessageItem();
						
						item.isMine=true;
						item.id = cursor.getString(cursor.getColumnIndex("_id"));
						item.body = cursor.getString(cursor.getColumnIndex("body"));
						item.date = cursor.getString(cursor.getColumnIndex("date"));
						sentboxMessages[index++] = item;
					}while(cursor.moveToNext());
					
					messages = Utils.mergeMessages(inboxMessages, sentboxMessages);
					setData(messages);
					cursor.close();
				}else{
					messages = inboxMessages;
					cursor.close();
				}
				break;
			
			case TOKEN_INBOX:
				if(cursor!=null && cursor.moveToFirst()){
					inboxMessages = new MessageItem[cursor.getCount()];
					int index = 0;
					do{
						MessageItem item = new MessageItem();
						
						item.isMine = false;
						
						item.id = cursor.getString(cursor.getColumnIndex("_id"));
						item.body = cursor.getString(cursor.getColumnIndex("body"));
						item.date = cursor.getString(cursor.getColumnIndex("date"));
						inboxMessages[index++] = item;
					}while(cursor.moveToNext());
					
					getSendBox();
					cursor.close();
				}else{
					getSendBox();
					cursor.close();
				}
				break;
			}
		}
		
	}

}
