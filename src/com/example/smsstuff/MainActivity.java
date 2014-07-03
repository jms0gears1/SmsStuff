package com.example.smsstuff;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.v4.app.NotificationCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;

import com.example.smsstuff.SMSBroadcastReceiver.DatabaseInsertListener;
import com.example.smsstuff.util.Utils.ThreadItem;

public class MainActivity extends Activity{
	
	public final static String 	TAG = MainActivity.class.getSimpleName().toString();
	
	private ListView mListView;
	private SMSBroadcastReceiver receiver;
	public TextMessageThreadAdapter mThreadAdapter;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		
		if(receiver==null){
			receiver = new SMSBroadcastReceiver();
			this.registerReceiver(receiver, new IntentFilter("android.provider.Telephony.SMS_DELIVER"));
			this.receiver.setListener(new ViewUpdateListener());
		}
		initListView();	
	}
	
	public void registerBroadcastReceiver(Context context){
		
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	public void initListView(){
		mListView = (ListView)this.findViewById(R.id.lvConversations);
		mListView.setAdapter(mThreadAdapter = new TextMessageThreadAdapter(getApplicationContext()));
		mListView.setOnItemClickListener(new onListItemSelect());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	
	public boolean isSmsEnabled(){
		String defaultSmsApp = Telephony.Sms.getDefaultSmsPackage(getApplicationContext());
		
		if(defaultSmsApp!=null && defaultSmsApp.equals(getPackageName())){
			return true;
		}
		
		return false;
	}
	
	public void sendNotification(String contact){
		NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		
		Intent intent = new Intent(this, MainActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, intent, 0);
		
		Uri sound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
		
		NotificationCompat.Builder builder = 
				new NotificationCompat.Builder(getApplicationContext())
				.setContentTitle("Youve got text!")
				.setContentText("Text From: " + contact)
				.setSmallIcon(R.drawable.iconstuff)
				.setAutoCancel(true)
				.setSound(sound)
				.setVibrate(new long[]{1000})
				.setContentIntent(pendingIntent);
		
		manager.notify(0, builder.build());
		
	}
	
	public class onListItemSelect implements OnItemClickListener{

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			ThreadItem item = (ThreadItem)mThreadAdapter.getItem(position);
			
			Intent intent = new Intent(MainActivity.this, ConversationActivity.class);
			intent.putExtra("thread_id", item.thread_id);
			intent.putExtra("address", item.address);
			intent.putExtra("name", item.name);
			MainActivity.this.startActivity(intent);
		}
	}
	
	private class ViewUpdateListener implements DatabaseInsertListener{
		@Override
		public void dataHasBeenInserted(String contact) {
			if(mListView != null && mThreadAdapter != null){
				mThreadAdapter.refreshData();
				sendNotification(contact);
			}
		}
	}
}
