package kr.co.ktech.cse.listener;

import kr.co.ktech.cse.activity.PersonalLounge;
import kr.co.ktech.cse.activity.TouchImageViewActivity;
import kr.co.ktech.cse.activity.TouchUserImageViewActivity;
import kr.co.ktech.cse.model.SnsAppInfo;
import android.R.bool;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;

public class AppClickListener implements OnClickListener{
	private String url;
	private int reply_cnt;
	private String body;
	private SnsAppInfo snsInfo_inListener;
	private Context context;
	private String img_url;
	Intent intent;
	private boolean whichOfOne = false; // user_photo ? body_content_photo
	public AppClickListener(String img_url, int cnt, String html_body){
		url = img_url;
		reply_cnt = cnt;
		body = html_body;
		whichOfOne = false;
	}
	public AppClickListener(SnsAppInfo sainfo, Context mContext){
		snsInfo_inListener = sainfo;
		context = mContext;
		whichOfOne = true;
	}
	public AppClickListener(String url, Context mContext){
		img_url = url;
		context = mContext;
	}
	@Override
	public void onClick(View v) {
		if(whichOfOne){
			intent = new Intent(context, TouchImageViewActivity.class);
			intent.putExtra("snsAppInfo", snsInfo_inListener);
//			Log.i("MSG", snsInfo_inListener.getReply_count()+"+CLICK");
			context.startActivity(intent);
		}else{
			intent = new Intent(context, TouchUserImageViewActivity.class);
			intent.putExtra("user_photo", img_url);
		}
		/*
		intent.putExtra("img_url", getUrl());
		intent.putExtra("body", body);
		intent.putExtra("reply_cnt", reply_cnt);
		 */
		
	}
	/**
	 * @return the url
	 */
	public String getUrl() {
		return url;
	}
	/**
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}
}