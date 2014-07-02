package com.example.smsstuff;

import java.lang.ref.WeakReference;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

public class SMSBroadcastReceiver extends BroadcastReceiver{
	
	public interface DatabaseInsertListener{
		public void dataHasBeenInserted(String contact);
	}
	
	private static WeakReference<DatabaseInsertListener> mListener;
	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle bundle = intent.getExtras();
	
		if(bundle!=null){
			intent.setClass(context, SmsDatabaseService.class);
			intent.putExtra("result", getResultCode());
			startDatabaseService(context, intent);
		}
	}
	
	public void setListener(DatabaseInsertListener listener){
		mListener = new WeakReference<DatabaseInsertListener>(listener);
	}
	
	public void startDatabaseService(Context context, Intent intent){
		context.startService(intent);
	}
	
	public static void finishDatabaseService(String contact){
		final DatabaseInsertListener listener = mListener.get();
		listener.dataHasBeenInserted(contact);
	}
}