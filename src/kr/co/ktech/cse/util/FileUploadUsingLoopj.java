package kr.co.ktech.cse.util;

import kr.co.ktech.cse.AppConfig;

import android.util.Log;

import com.loopj.android.http.*;

public class FileUploadUsingLoopj {
	String TAG = "FileUploadUsingLoopj";
	AsyncHttpClient client = new AsyncHttpClient();
	
	public void foo(){
		client.get("http://www.google.com", new AsyncHttpResponseHandler(){
			@Override
			public void onSuccess(String response){
				if(AppConfig.DEBUG)Log.d(TAG, response);
			}
		});
	}
}
