package kr.co.ktech.cse.activity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.ResponseHandler;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import kr.co.ktech.cse.AppConfig;
import kr.co.ktech.cse.R;
import kr.co.ktech.cse.CommonUtilities;
import kr.co.ktech.cse.bitmapfun.ui.ImageGridActivity;
import kr.co.ktech.cse.model.AppUser;
import kr.co.ktech.cse.model.GroupInfo;
import kr.co.ktech.cse.util.FileUploadUsingLoopj;
import kr.co.ktech.cse.util.FileUploader;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MoreTab extends Activity {

	private Context context;
	DisplayMetrics metrics;
	Button button;
	SharedPreferences pref;
	RelativeLayout baseLayout;
	private static final int FILE_SELECT_CODE = 0;
	String TAG = "MoreTab";
	String utf = "UTF-8";
	String iso = "ISO-8859-1";
	String euc = "EUC-KR";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = getApplicationContext();
		setContentView(R.layout.activity_more_tab);
//		makeView();
	}
	/*
	public void makeView(){
		baseLayout = (RelativeLayout)findViewById(R.id.moretab);
		button = new Button(context);
		button.setId(100010);
		RelativeLayout.LayoutParams params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		params.addRule(RelativeLayout.CENTER_HORIZONTAL);
		button.setText("request http client");
		button.setLayoutParams(params);
//		baseLayout.addView(button);
		button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				FileUploadUsingLoopj fj = new FileUploadUsingLoopj();
				fj.foo();
			}
		});
		RelativeLayout.LayoutParams params_pic = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
		params_pic.addRule(RelativeLayout.BELOW, button.getId());
		params_pic.addRule(RelativeLayout.CENTER_HORIZONTAL);
		Button pic_button = new Button(context);
		pic_button.setId(100011);
		pic_button.setText("file search");
		pic_button.setLayoutParams(params_pic);
//		baseLayout.addView(pic_button);
		pic_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				showFileChooser();
			}
		});
		RelativeLayout.LayoutParams params_upload = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
		params_upload.addRule(RelativeLayout.BELOW, pic_button.getId());
		params_upload.addRule(RelativeLayout.CENTER_HORIZONTAL);
		Button upload_button = new Button(context);
		upload_button.setId(100012);
		upload_button.setText("file upload");
		upload_button.setLayoutParams(params_upload);
//		baseLayout.addView(upload_button);
		upload_button.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Thread thread = new Thread(new Runnable() {
					
					@Override
					public void run() {
						// TODO Auto-generated method stub
						runOnUiThread(new Runnable() {
							
							@Override
							public void run() {
								// TODO Auto-generated method stub
								tv.setText("uploading started....");
							}
						});
						int response = fu.uploadFile(dir+"/"+filename , String.valueOf(AppUser.user_id));
						if(AppConfig.DEBUG)Log.d(TAG, "res: "+response);
					}
				});
				thread.start();
			}
		});
		RelativeLayout.LayoutParams params_text = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
		params_text.addRule(RelativeLayout.BELOW, upload_button.getId());
		params_text.addRule(RelativeLayout.CENTER_HORIZONTAL);
		tv = new TextView(context);
		tv.setText("State");
		tv.setLayoutParams(params_text);
//		baseLayout.addView(tv);
	}
	*/
	FileUploader fu = new FileUploader();
	TextView tv;
	private void showFileChooser(){
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("*/*");
		intent.addCategory(Intent.CATEGORY_OPENABLE);
		try{
			startActivityForResult(Intent.createChooser(intent, "Select a File to Upload"), FILE_SELECT_CODE);
		}catch(android.content.ActivityNotFoundException ex){
			Toast.makeText(this, "Please install a File Manager", Toast.LENGTH_SHORT).show();
		}
	}
	String filename = null;
	String dir = null;
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case FILE_SELECT_CODE:      
			if (resultCode == RESULT_OK) {  
				// Get the Uri of the selected file 
				Uri uri = data.getData();
				String filePath = uri.toString();
				
				// Get the path (한글 포함)
				try {
					String hangul = URLDecoder.decode(filePath, utf);
					int dir_divider = hangul.lastIndexOf("/");
					filename = hangul.substring(dir_divider+1);
					dir = hangul.substring(0, dir_divider);
					dir = dir.replace("file://", "");
					if(AppConfig.DEBUG)Log.d(TAG, "dir= "+dir +"/" + "filename= "+filename);
				} catch (UnsupportedEncodingException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				tv.setText(dir+"/"+filename);
				
//				String path = FileUtils.getPath(this, uri);
//				Log.d(TAG, "File Path: " + path);
				
				// Get the file instance
				// File file = new File(path);
				// Initiate the upload
			}           
			break;
		}
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		// Inflate the menu; this adds items to the action bar if it is present.
		//getMenuInflater().inflate(R.menu.more_tab, menu);
		return true;
	}

	@Override
	public void onBackPressed() {
		this.getParent().onBackPressed();
	}

}
