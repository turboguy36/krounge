package kr.co.ktech.cse.activity;

import static kr.co.ktech.cse.CommonUtilities.KLOUNGE_STORAGE_LOCATION;

import java.io.File;

import kr.co.ktech.cse.R;
import kr.co.ktech.cse.R.id;
import kr.co.ktech.cse.R.layout;
import kr.co.ktech.cse.db.LoginRequest;
import kr.co.ktech.cse.util.RecycleUtils;
import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class LoginActivity extends Activity {
	private String TAG = "** KLoungeActivity **";

	private String filename = "MySampleFile.txt";
	private String filepath = "kloungeFileStorage";
	File myInternalFile;
	EditText etID;
	EditText etPasswd;
	private RelativeLayout whole_view;
	boolean LOGIN = false;
	boolean loginState = false;
	SharedPreferences pref;

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setContentView(R.layout.login);
		Intent intent = new Intent("kr.co.ktech.activity.Service");
		startService(intent);

		pref = getSharedPreferences("KLounge", 0);
		loginState = pref.getBoolean("loginState", false);
		whole_view = (RelativeLayout)findViewById(R.id.LoginLayout);
		//Log.i("login state", String.valueOf(loginState));
		File dir = getCacheDir();
		KLOUNGE_STORAGE_LOCATION=dir.getPath();
		//		KLOUNGE_STORAGE_LOCATION = CommonUtilities.DOWNLOAD_PATH;
		// 로그인 상태 확인
		if(loginState) {
			intent = new Intent(LoginActivity.this, KLoungeActivity.class);
			LoginActivity.this.startActivity(intent);
			finish();
		} else {

			etID = (EditText)findViewById(R.id.etID);
			etPasswd = (EditText)findViewById(R.id.etPasswd);
			final ImageView iv = (ImageView)findViewById(R.id.ivLogin);
			iv.setOnClickListener(new View.OnClickListener() {

				public void onClick(View v) {
					final String id = etID.getText().toString();
					final String pw = etPasswd.getText().toString();
//					Log.i("id, pw", id+"  "+pw);

					Thread thread = new Thread(new Runnable() {

						public void run() {
							LoginRequest loginproc = new LoginRequest(); 
//							LOGIN = loginproc.login("hscho", "1111", pref);
							LOGIN = loginproc.login(id, pw, pref);

							handler.sendEmptyMessage(0);
						}
					});
					thread.start();
				}
			});
			etID.setOnKeyListener(new OnKeyListener() {
				@Override
				public boolean onKey(View v, int keyCode, KeyEvent event) {
					if(keyCode == KeyEvent.KEYCODE_ENTER){
						if(event.getAction()== KeyEvent.ACTION_DOWN){

						}else if(event.getAction()== KeyEvent.ACTION_UP){
							etID.setText(etID.getText().toString().trim());
							etPasswd.requestFocus();
						}
						return false;

					}
					return false;
				}
			});
			etPasswd.setOnEditorActionListener(new OnEditorActionListener() {

				@Override
				public boolean onEditorAction(TextView v,
						int actionId, KeyEvent event) {
					if(actionId == EditorInfo.IME_ACTION_DONE){
						final String id = etID.getText().toString();
						final String pw = etPasswd.getText().toString();

						Thread thread = new Thread(new Runnable() {
							public void run() {
								LoginRequest loginproc = new LoginRequest(); 
								LOGIN = loginproc.login(id, pw, pref);
								handler.sendEmptyMessage(0);
							}
						});
						thread.start();
					}
					return false;
				};
			});

		}

		whole_view.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
				imm.hideSoftInputFromWindow(whole_view.getWindowToken(), 0);
			}
		});
	}	
	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if(LOGIN) {
				Log.i(TAG,KLOUNGE_STORAGE_LOCATION);

				SharedPreferences.Editor edit = pref.edit();
				edit.putBoolean("loginState", true);

				edit.commit();

				File dir = getFilesDir();

				if(!dir.exists()){

					dir.mkdirs();
				}else{
					//Log.i("original dir","Name: "+KLOUNGE_STORAGE_LOCATION);
				}

				Intent intent = new Intent(LoginActivity.this, KLoungeActivity.class);
				LoginActivity.this.startActivity(intent);

				ContextWrapper cwp = new ContextWrapper(getApplicationContext());
				File directory = cwp.getDir(filepath, Context.MODE_PRIVATE);
				myInternalFile = new File(directory, filename);

				finish();
			} else {
				Toast.makeText(LoginActivity.this, "아이디와 비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
			}
		}
	};
	@Override
	protected void onDestroy() {
		// Adapter가 있으면 어댑터에서 생성한 recycle메소드를 실행
		RecycleUtils.recursiveRecycle(getWindow().getDecorView());
		System.gc();

		super.onDestroy();
	}
}
