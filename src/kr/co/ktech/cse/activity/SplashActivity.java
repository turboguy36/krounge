package kr.co.ktech.cse.activity;

import kr.co.ktech.cse.R;
import kr.co.ktech.cse.R.layout;
import kr.co.ktech.cse.util.RecycleUtils;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.view.Menu;

public class SplashActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        
        Handler handler = new Handler(){
        	@Override
        	public void handleMessage(Message msg) {
        		finish();
        	}
        };
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        handler.sendEmptyMessageDelayed(0,2500);
    } 
    @Override
	protected void onDestroy() {
//		Adapter가 있으면 어댑터에서 생성한 recycle메소드를 실행
		RecycleUtils.recursiveRecycle(getWindow().getDecorView());
		System.gc();

		super.onDestroy();
	}
}
