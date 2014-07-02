package com.example.smsstuff;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.smsstuff.util.AsyncCursorHandler;
import com.example.smsstuff.util.AsyncCursorHandler.AsyncCursorListener;
import com.example.smsstuff.util.Utils;
import com.example.smsstuff.util.Utils.ThreadItem;

public class TextMessageThreadAdapter extends BaseAdapter{
	
	
	public final int TOKEN_CONVERSATION_LIST = 1;
	public final int TOKEN_CONVERSATION_NAME = 2;
	
	AsyncCursorHandler handler;
	ThreadItem[] threads;
	Context context;
	
	public TextMessageThreadAdapter(Context context){
		this.context = context;
		this.refreshData();
	}
	
	public void refreshData(){
		if(handler == null){
			initCursor();
		}
		
		getConversations();
	}
	
	public void initCursor(){
		handler = new AsyncCursorHandler(
				context,
				new ConversationQueryHandler()
				);	
	}
	
	public void getConversations(){		
		handler.cancelOperation(TOKEN_CONVERSATION_LIST);
		
		handler.startQuery(
				TOKEN_CONVERSATION_LIST, 
				null, Utils.MMS_SMS_CONVERSATIONS_URI, 
				new String[]{"_id", "thread_id", "person", "address", "body"}, 
				null, null, 
				"date DESC");
	}
	
	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return threads==null?0:threads.length;
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return threads[position];
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
			mView = inflater.inflate(R.layout.text_message_adapter, null);
		}else{
			mView = convertView;
		}
		
		ImageView image = (ImageView)mView.findViewById(R.id.imageView1);
		TextView tvName = (TextView)mView.findViewById(R.id.textView1);
		TextView tvMsg = (TextView)mView.findViewById(R.id.textView2);
		
		String name = Utils.getContactIDFromPhoneNumber(context, threads[position].address);
		String snip = threads[position].body;
		
		image.setImageResource(R.drawable.ic_launcher);
		tvName.setText(name);
		tvMsg.setText(snip);
		
		return mView;
	}
	
	private void setData(ThreadItem[] threads){
		this.threads = threads;
		this.notifyDataSetChanged();
	}
	
	public class ConversationQueryHandler implements AsyncCursorListener{

		@Override
		public void onQueryCompleted(int token, Cursor cursor) {
			
			switch(token){
			case TOKEN_CONVERSATION_LIST:
				
				if(cursor!=null && cursor.moveToFirst()){
					ThreadItem[] threads = new ThreadItem[cursor.getCount()];
					int count = 0;
					do{							
						ThreadItem item = new ThreadItem(
								cursor.getString(cursor.getColumnIndex("_id")),
								cursor.getString(cursor.getColumnIndex("thread_id")), 
								cursor.getString(cursor.getColumnIndex("address")),
								cursor.getString(cursor.getColumnIndex("body"))
								);
						
						threads[count++] = item;
										
					}while(cursor.moveToNext());
					
					cursor.close();
					
					setData(threads);
				}
				break;
			}
		}
	}
}
