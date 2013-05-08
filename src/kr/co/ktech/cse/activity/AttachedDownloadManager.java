/***
  Copyright (c) 2008-2012 CommonsWare, LLC
  Licensed under the Apache License, Version 2.0 (the "License"); you may not
  use this file except in compliance with the License. You may obtain	a copy
  of the License at http://www.apache.org/licenses/LICENSE-2.0. Unless required
  by applicable law or agreed to in writing, software distributed under the
  License is distributed on an "AS IS" BASIS,	WITHOUT	WARRANTIES OR CONDITIONS
  OF ANY KIND, either express or implied. See the License for the specific
  language governing permissions and limitations under the License.

  From _The Busy Coder's Guide to Android Development_
    http://commonsware.com/Android
 */
package kr.co.ktech.cse.activity;
import java.io.File;
import java.util.List;

import kr.co.ktech.cse.AppConfig;
import kr.co.ktech.cse.R;
import kr.co.ktech.cse.db.KLoungeHttpRequest;
import kr.co.ktech.cse.util.RecycleUtils;
import android.app.Activity;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.NetworkInfo.State;
import android.opengl.Visibility;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import static kr.co.ktech.cse.CommonUtilities.DOWNLOAD_PATH;

public class AttachedDownloadManager extends Activity {
	private DownloadManager mgr=null;
	private long lastDownload=-1L;
	private String notiMessage;
	private int post_id;
	private int post_user_id;
	private int ck;
	private String filename;
	private boolean isUrlLink = false;
	private TextView notiMsg;
	private KLoungeHttpRequest httprequest;
	private String _url;
	private Button close_btn;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.download_manager);

		httprequest = new KLoungeHttpRequest();
		Bundle bun = getIntent().getExtras();
		notiMessage = bun.getString("notiMessage");
		post_id = bun.getInt("p_id");
		post_user_id = bun.getInt("user_id");
		ck = bun.getInt("ck");
		filename = bun.getString("filename");
		String jspFilename = null;
		
		if(!bun.getBoolean("url_linker")){
			jspFilename = "appDataDown.jsp";
		}else{
			jspFilename = "appDataDownDataInfo.jsp";
		}
		
		notiMsg = (TextView)findViewById(R.id.download_message);
		StringBuffer showMessage = new StringBuffer();
		ConnectivityManager conMan = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		State wifi = conMan.getNetworkInfo(1).getState();
		if (wifi == NetworkInfo.State.CONNECTED || wifi == NetworkInfo.State.CONNECTING) {
			
		}else{
			showMessage.append("WI-FI 가 연결되어 있지 않습니다. 과도한 데이터가 청구될 수 있습니다.\n\n");
		}
		showMessage.append("\""+filename+"\" \n를/(을) ").append(notiMessage);
		if(filename == null){
			showMessage.setLength(0);
			showMessage.append("삭제 되었거나 존재 하지 않는 파일 입니다.");
			findViewById(R.id.start).setEnabled(false);
		}else{
			
		}
		notiMsg.setText(showMessage.toString());

		StringBuffer sb_url = new StringBuffer();
		sb_url.append(httprequest.getService_URL() + "/mobile/appdbbroker/").append(jspFilename);
		//		_url = _url + "?post_id=2125&post_user_id=17&ck=0";
		sb_url.append("?post_id="+post_id);
		sb_url.append("&post_user_id="+post_user_id);
		sb_url.append("&ck="+ck);
		_url = sb_url.toString();
		close_btn = (Button)findViewById(R.id.btn_close_dialog);
		close_btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});
		mgr=(DownloadManager)getSystemService(DOWNLOAD_SERVICE);
		registerReceiver(onComplete,
				new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
		registerReceiver(onNotificationClick,
				new IntentFilter(DownloadManager.ACTION_NOTIFICATION_CLICKED));
	}

	@Override
	public void onDestroy() {
		RecycleUtils.recursiveRecycle(getWindow().getDecorView());
		System.gc();

		super.onDestroy();

		unregisterReceiver(onComplete);
		unregisterReceiver(onNotificationClick);
	}
	String TAG = "download manager";
	public void startDownload(View v) {
		Uri uri=Uri.parse(_url);
		if(AppConfig.DEBUG)Log.d(TAG, _url);
		//  Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).mkdirs();
		File dir=new File(DOWNLOAD_PATH+"/Download/");
		if(!dir.exists()) dir.mkdirs();
		lastDownload=
				mgr.enqueue(new DownloadManager.Request(uri)
				.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI |
						DownloadManager.Request.NETWORK_MOBILE)
						.setAllowedOverRoaming(false)
						.setTitle(filename)
						.setDescription("위치: "+DOWNLOAD_PATH+"/Download/")
						.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
								filename));

		v.setEnabled(false);
		//		findViewById(R.id.query).setEnabled(true);
	}

	public void queryStatus(View v) {
		Cursor c=mgr.query(new DownloadManager.Query().setFilterById(lastDownload));

		if (c==null) {
			Toast.makeText(this, "Download not found!", Toast.LENGTH_LONG).show();
		}
		else {
			c.moveToFirst();

			Log.d(getClass().getName(), "COLUMN_ID: "+
					c.getLong(c.getColumnIndex(DownloadManager.COLUMN_ID)));
			Log.d(getClass().getName(), "COLUMN_BYTES_DOWNLOADED_SO_FAR: "+
					c.getLong(c.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)));
			Log.d(getClass().getName(), "COLUMN_LAST_MODIFIED_TIMESTAMP: "+
					c.getLong(c.getColumnIndex(DownloadManager.COLUMN_LAST_MODIFIED_TIMESTAMP)));
			Log.d(getClass().getName(), "COLUMN_LOCAL_URI: "+
					c.getString(c.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI)));
			Log.d(getClass().getName(), "COLUMN_STATUS: "+
					c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS)));
			Log.d(getClass().getName(), "COLUMN_REASON: "+
					c.getInt(c.getColumnIndex(DownloadManager.COLUMN_REASON)));

			Toast.makeText(this, statusMessage(c), Toast.LENGTH_LONG).show();
		}
	}

	public void viewLog(View v) {
//		startActivity(new Intent(DownloadManager.ACTION_VIEW_DOWNLOADS));
		viewFile(this, DOWNLOAD_PATH+"/Download", filename);
	}

	private int statusMessage(Cursor c) {
		String msg="???";
		int status = -1;
		switch(c.getInt(c.getColumnIndex(DownloadManager.COLUMN_STATUS))) {
		case DownloadManager.STATUS_FAILED:
			msg="Download failed!";

			break;

		case DownloadManager.STATUS_PAUSED:
			msg="Download paused!";
			break;

		case DownloadManager.STATUS_PENDING:
			msg="Download pending!";
			break;

		case DownloadManager.STATUS_RUNNING:
			msg="Download in progress!";
			break;

		case DownloadManager.STATUS_SUCCESSFUL:
			msg="Download complete!";
			status = 1;
			break;

		default:
			msg="Download is nowhere in sight";
			break;
		}

		return status;
	}

	BroadcastReceiver onComplete=new BroadcastReceiver() {
		public void onReceive(Context ctxt, Intent intent) {
			Cursor c=mgr.query(new DownloadManager.Query().setFilterById(lastDownload));
			findViewById(R.id.start).setVisibility(View.GONE);
			notiMsg.setText("다운로드가 성공적으로 완료되었습니다.");
			close_btn.setText("닫기");
			findViewById(R.id.go_download).setVisibility(View.VISIBLE);
			if(c != null){
				c.close();
			}
		}
	};

	BroadcastReceiver onNotificationClick=new BroadcastReceiver() {
		public void onReceive(Context ctxt, Intent intent) {
			Toast.makeText(ctxt, "Ummmm...hi!", Toast.LENGTH_LONG).show();
		}
	};
	/**
	 * 파일의 확장자 조회
	 * 
	 * @param fileStr
	 * @return
	 */
	public static String getExtension(String fileStr) {
		return fileStr.substring(fileStr.lastIndexOf(".") + 1, fileStr.length());
	}

	/**
	 * Viewer로 연결
	 * 
	 * @param ctx
	 * @param filePath
	 * @param fileName
	 */
	public static void viewFile(Context ctx, String filePath, String fileName) {
		// TODO Auto-generated method stub
		Intent fileLinkIntent = new Intent(Intent.ACTION_VIEW);
		fileLinkIntent.addCategory(Intent.CATEGORY_DEFAULT);
		File file = new File(filePath, fileName);
		Uri uri = Uri.fromFile(file);
		//확장자 구하기
		String fileExtend = getExtension(file.getAbsolutePath());
		// 파일 확장자 별로 mime type 지정해 준다.
		if (fileExtend.equalsIgnoreCase("mp3")) {
			fileLinkIntent.setDataAndType(Uri.fromFile(file), "audio/*");
		} else if (fileExtend.equalsIgnoreCase("mp4")) {
			fileLinkIntent.setDataAndType(Uri.fromFile(file), "vidio/*");
		} else if (fileExtend.equalsIgnoreCase("jpg")
				|| fileExtend.equalsIgnoreCase("jpeg")
				|| fileExtend.equalsIgnoreCase("gif")
				|| fileExtend.equalsIgnoreCase("png")
				|| fileExtend.equalsIgnoreCase("bmp")) {
			fileLinkIntent.setDataAndType(Uri.fromFile(file), "image/*");
		} else if (fileExtend.equalsIgnoreCase("txt")) {
			fileLinkIntent.setDataAndType(Uri.fromFile(file), "text/*");
		} else if (fileExtend.equalsIgnoreCase("doc")
				|| fileExtend.equalsIgnoreCase("docx")) {
			fileLinkIntent.setDataAndType(Uri.fromFile(file), "application/msword");
		} else if (fileExtend.equalsIgnoreCase("xls")
				|| fileExtend.equalsIgnoreCase("xlsx")) {
			fileLinkIntent.setDataAndType(Uri.fromFile(file),
					"application/vnd.ms-excel");
		} else if (fileExtend.equalsIgnoreCase("ppt")
				|| fileExtend.equalsIgnoreCase("pptx")) {
			fileLinkIntent.setDataAndType(Uri.fromFile(file),
					"application/vnd.ms-powerpoint");
		} else if (fileExtend.equalsIgnoreCase("pdf")) {
			fileLinkIntent.setDataAndType(Uri.fromFile(file), "application/pdf");
		} else if (fileExtend.equalsIgnoreCase("hwp")) {
			fileLinkIntent.setDataAndType(Uri.fromFile(file),
					"application/haansofthwp");
		} else if(fileExtend.equalsIgnoreCase("zip")){
			fileLinkIntent.setDataAndType(Uri.fromFile(file), "application/zip");
		}
		PackageManager pm = ctx.getPackageManager();
		List<ResolveInfo> list = pm.queryIntentActivities(fileLinkIntent,
				PackageManager.GET_META_DATA);
		if (list.size() == 0) {
			Toast.makeText(ctx, fileName + "을 확인할 수 있는 앱이 설치되지 않았습니다.",
					Toast.LENGTH_SHORT).show();
		} else {
			ctx.startActivity(fileLinkIntent);
		}
	}
}