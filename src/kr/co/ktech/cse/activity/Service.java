package kr.co.ktech.cse.activity;

import kr.co.ktech.cse.R;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import android.provider.Settings;

public class Service extends android.app.Service {
	String TAG = "NetWorkServiceTest";
	Context context;
	IntentFilter mWifiFilter;
	AlertDialog alert;
	boolean hasBeenShown = false;
	public void onCreate(){
		Log.i(TAG, "start service");
		context = getApplicationContext();
		mWifiFilter = new IntentFilter(WifiManager.WIFI_STATE_CHANGED_ACTION);
		mWifiFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
		mWifiFilter.addAction(WifiManager.RSSI_CHANGED_ACTION);
		registerReceiver(mWifiReceiver, mWifiFilter);
	}

	public void onStart(Intent intent , int startId){
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		return super.onStartCommand(intent, flags, startId);
	}

	public void onDestroy(){
		unregisterReceiver(mWifiReceiver);
	}
	public IBinder onBind(Intent intent) {
		return null;
	}

	private final BroadcastReceiver mWifiReceiver = new BroadcastReceiver() {
		
		public void onReceive(Context context, Intent intent) {
			final String action = intent.getAction();
//			Log.i(TAG , "Action : " + action);

			if(!isOnline()){
				Log.i(TAG , "!isOnline()");
				if(!hasBeenShown){
					Bundle bun = new Bundle();
					bun.putString("notiMessage", "인터넷 연결이 끊겼습니다. 데이터 연결 설정 후 다시 시도하세요.");
	
					Intent popupIntent = new Intent(context, DialogActivity.class);
					
					popupIntent.putExtras(bun);
					
					PendingIntent pi = PendingIntent.getActivity(context, 0, popupIntent, PendingIntent.FLAG_ONE_SHOT);
					try{
						pi.send();
					}catch(Exception e){
						Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
					}
				}
				hasBeenShown = true;
				//startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
				//startActivity(new Intent(Settings.ACTION_WIRELESS_SETTINGS));
				/*
				Intent i = new Intent("android.settings.WIFI_SETTINGS");
				i.addCategory("android.intent.category.DEFAULT");
				i.setFlags(0x30800000 );
				i.setComponent(new ComponentName("com.android.settings" , "com.android.settings.wifi.WifiSettings"));
				context.startActivity(i);
				 */
				/*

				Intent i = new Intent("android.intent.action.MAIN ");
				i.setComponent(new ComponentName("com.android.settings" , "com.android.settings..WirelessSettings"));
				context.startActivity(i);
				 */
				/*
				WifiManager wifi = (WifiManager)getSystemService(Context.WIFI_SERVICE);
				if(!wifi.isWifiEnabled()){
					if(wifi.getWifiState() != WifiManager.WIFI_STATE_ENABLING){
						wifi.setWifiEnabled(true);
						Toast.makeText(context, "Wifi Enable", Toast.LENGTH_SHORT).show(); 
					}
				}
				*/
			}else{
				/*
				  if(!hasBeenShown){
				 
					Bundle bun = new Bundle();
					bun.putString("notiMessage", "'K-라운지' 앱은 데이터 송 수신 통화료가 부가 될 수 있습니다.");

					Intent popupIntent = new Intent(context, DialogActivity.class);

					popupIntent.putExtras(bun);

					PendingIntent pi = PendingIntent.getActivity(context, 0, popupIntent, PendingIntent.FLAG_ONE_SHOT);
					try{
						pi.send();
					}catch(Exception e){
						Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
					}
				}
				hasBeenShown = true;
				*/
			}
		}

	};
	private boolean isOnline() {
//		Log.i(TAG , "isOnline()");
		/*
		ConnectivityManager connectivityManager = (ConnectivityManager)
		getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
		boolean connected = networkInfo != null && networkInfo.isAvailable() && networkInfo.isConnected();
		return connected;
		 */
		boolean connected = false;
		ConnectivityManager conMan = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		State mobile = conMan.getNetworkInfo(0).getState(); //mobile
		State wifi = conMan.getNetworkInfo(1).getState(); //wifi
		if (mobile == NetworkInfo.State.CONNECTED || mobile == NetworkInfo.State.CONNECTING) {
//			Log.i(TAG , "isOnline() : mobile");
			connected = true;
		} else if (wifi == NetworkInfo.State.CONNECTED || wifi == NetworkInfo.State.CONNECTING) {
//			Log.i(TAG , "isOnline() : wifi");
			connected = true;
		}

		return connected;
	}
}