package com.example.smsstuff;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.TextView;

public class ConversationActivity extends Activity{

	private String mThreadId;
	private String mContactAddress;
	private String mContactName;
	
	private ListView mListView;
//	private ConversationThreadAdapter mThreadAdapter;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.thread_view);
		
		processIntent(getIntent());
		initView();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
	}

	private void initView() {
		TextView name = (TextView)this.findViewById(R.id.tvContact);
		name.setText(mContactName==null?mContactAddress:mContactName);
		mListView = (ListView)this.findViewById(R.id.lvMessages);
		mListView.setAdapter(new ConversationThreadAdapter(getApplicationContext(),mThreadId));
		
	}
	
	private void processIntent(Intent intent){
		mThreadId 		= intent.getStringExtra("thread_id");
		mContactAddress = intent.getStringExtra("address");
		mContactName    = intent.getStringExtra("name");
	}
}
