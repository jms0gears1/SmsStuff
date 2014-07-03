package com.example.smsstuff;

import java.lang.ref.WeakReference;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.example.smsstuff.SMSBroadcastReceiver.DatabaseInsertListener;

public class ConversationActivity extends Activity{

	private String mThreadId;
	private String mContactAddress;
	private String mContactName;
	
	private ListView mListView;
	private ConversationThreadAdapter mThreadAdapter;
	private static WeakReference<ViewUpdateListener> mListener;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.thread_view);
		
		processIntent(getIntent());
		initView();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	private void initView() {
		TextView name = (TextView)this.findViewById(R.id.tvContact);
		name.setText(mContactName==null?mContactAddress:mContactName);
		mListView = (ListView)this.findViewById(R.id.lvMessages);
		mListView.setAdapter(mThreadAdapter = new ConversationThreadAdapter(getApplicationContext(),mThreadId));
		mListener = new WeakReference<ViewUpdateListener>(new ViewUpdateListener());
		
	}
	
	private void processIntent(Intent intent){
		mThreadId 		= intent.getStringExtra("thread_id");
		mContactAddress = intent.getStringExtra("address");
		mContactName    = intent.getStringExtra("name");
	}
	
	public void onClick(View v){
		if(v.getId() == R.id.btnSend){
			this.findViewById(R.id.etMessage).clearFocus();
			InputMethodManager imm = (InputMethodManager)getSystemService(
					  Context.INPUT_METHOD_SERVICE);
					imm.hideSoftInputFromWindow(this.findViewById(R.id.etMessage).getWindowToken(), 0);
					
					EditText etMsg = (EditText)findViewById(R.id.etMessage);
					String msg = etMsg.getText().toString();
					
					if(msg.length() != 0){
						sendSms(msg);
						etMsg.setText("");
					}
		}
	}
	
	public void sendSms(String body){
		SmsManager smsManager = SmsManager.getDefault();
		smsManager.sendTextMessage(mContactAddress, null, body, null, null);
		startDatabaseService(body);
	}
	
	private void startDatabaseService(String body){
		Intent serviceIntent = new Intent(getApplicationContext(), SmsDatabaseService.class);
		serviceIntent.putExtra("isMine", true);
		serviceIntent.putExtra("thread_id", mThreadId);
		serviceIntent.putExtra("address", mContactAddress);
		serviceIntent.putExtra("body", body);
		getApplicationContext().startService(serviceIntent);
	}
	
	public static void databaseServiceFinished(String contact){
		ViewUpdateListener listener = mListener.get();
		if(listener!=null)listener.dataHasBeenInserted(contact);
	}
	
	private class ViewUpdateListener implements DatabaseInsertListener{
		@Override
		public void dataHasBeenInserted(String contact) {
			if(mThreadAdapter!=null){
				mThreadAdapter.refreshData();
			}
		}
	}
}
