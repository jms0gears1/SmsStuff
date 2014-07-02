package com.example.smsstuff.util;

import java.lang.ref.WeakReference;

import android.content.AsyncQueryHandler;
import android.content.Context;
import android.database.Cursor;

public class AsyncCursorHandler extends AsyncQueryHandler{
	
	private WeakReference<AsyncCursorListener> mListener;
	
	public interface AsyncCursorListener{
		void onQueryCompleted(int token, Cursor cursor);
	}

	public AsyncCursorHandler(Context context, AsyncCursorListener listener) {
		super(context.getContentResolver());
		setCursorListener(listener);
	}
	
	public void setCursorListener(AsyncCursorListener listener){
		mListener = new WeakReference<AsyncCursorListener>(listener);
	}

	@Override
	protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
		final AsyncCursorListener listener = mListener.get();
		
		if(listener!=null){
			listener.onQueryCompleted(token, cursor);
		}else if (cursor!=null){
			cursor.close();
		}
	}	
}
