package kr.co.ktech.cse.activity;

import java.util.ArrayList;
import java.util.List;

import kr.co.ktech.cse.R;
import kr.co.ktech.cse.R.drawable;
import kr.co.ktech.cse.R.id;
import kr.co.ktech.cse.R.layout;
import kr.co.ktech.cse.R.string;
import kr.co.ktech.cse.CommonUtilities;
import kr.co.ktech.cse.AppConfig;
import kr.co.ktech.cse.bitmapfun.util.ImageFetcher;
import kr.co.ktech.cse.model.AppUser;
import kr.co.ktech.cse.model.GroupInfo;
import kr.co.ktech.cse.util.RecycleUtils;
import android.app.ProgressDialog;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;
public class KLoungeActivity extends TabActivity {

	final static int KLOUNGELIST_VIEW = 0;
	final static int GROUPLIST_VIEW = 1;
	final static int MYHOMELIST_VIEW = 2;
	final int CLOSE_MESSAGE = 1;
	private int LENGTH_TO_SHOW = Toast.LENGTH_SHORT;
	private String TAG = "** KLoungeActivity **";
	AsyncTask<Void, Void, Void> mRegisterTask;
	AsyncTask<Void, Void, Void> mCheckTask;
	// 핸들러, 플래그 선언.
	//private Handler handler;
	private Context context;
	private boolean mFlag = false;
	private ImageView imageView;
	private TextView textView;
	private ProgressDialog pd;
	long start = 0L;
	SharedPreferences pref;
	List<GroupInfo> group_list;
	GroupInfo view_groupinfo;
	TabHost tabHost;
	Intent intent;
	ImageFetcher mImageFetcher;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		startActivity(new Intent(this, SplashActivity.class));
		context = getApplicationContext();
		super.onCreate(savedInstanceState);
		/*
		    if(!isOnline()){startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
		    }else{}
		 */
		intent = new Intent("kr.co.ktech.activity.Service");
		//		startService(intent);

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.klounge_main);

		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
		imageView = (ImageView)findViewById(R.id.favicon);
		textView = (TextView)findViewById(R.id.right_text);
		imageView.setImageResource(R.drawable.icon_klounge);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		mImageFetcher = AppUser.mImageFetcher;
		createThreadAndDialog();
	}

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if(msg.what == CLOSE_MESSAGE) {
				mFlag = false;
			} else {
				// View갱신
				tabHost = getTabHost();

				LayoutInflater twGroupLayout = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View twGroupView = (View)twGroupLayout.inflate(R.layout.tabwidget_group, null);

				LayoutInflater twKLoungeLayout = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View twKLoungeView = (View)twKLoungeLayout.inflate(R.layout.tabwidget_klounge, null);

				LayoutInflater twMyLoungeLayout = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View twMyLoungeView = (View)twMyLoungeLayout.inflate(R.layout.tabwidget_mylounge, null);

				LayoutInflater twMoreLayout = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				View twMoreView = (View)twMoreLayout.inflate(R.layout.tabwidget_more, null);

				
				tabHost.addTab(tabHost.newTabSpec("klounge").setIndicator(twKLoungeView).setContent(new Intent(context, KLoungeMsg.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_SINGLE_TOP)));
				tabHost.addTab(tabHost.newTabSpec("mylonge").setIndicator(twMyLoungeView).setContent(new Intent(context, MyLounge.class)));
				tabHost.addTab(tabHost.newTabSpec("group").setIndicator(twGroupView).setContent(new Intent(context, KLoungeGroupList.class)));
				tabHost.addTab(tabHost.newTabSpec("more").setIndicator(twMoreView).setContent(new Intent(context, MoreTab.class)));

				for(int tab=0; tab<tabHost.getTabWidget().getChildCount(); ++tab){
					tabHost.getTabWidget().getChildAt(tab).getLayoutParams().height = CommonUtilities.DPFromPixel(context, 95);
					tabHost.getTabWidget().getChildAt(tab).setBackgroundResource(R.drawable.tab_bg);
				}
			}

		}
	};

	void createThreadAndDialog() {
		Thread thread = new Thread(new Runnable() {
			public void run() {
				// 초기화 작업 처리
				group_list =  new ArrayList<GroupInfo>();
				pref = getSharedPreferences("KLounge", 0);

				AppUser.user_id = pref.getInt("user_id", 0);
				AppUser.user_name = pref.getString("user_name", "");

				AppUser.CURRENT_TAB = pref.getInt("current_tab", 0);

				String strGroupList = pref.getString("group_list", "");
				
//				if(AppConfig.DEBUG)Log.d(TAG, strGroupList);
				//hard_coding by hglee It has to be retrieved
				//				strGroupList = strGroupList + "|0";
				//	        	Log.i("pref_group_list", "test:"+strGroupList);
				if(!strGroupList.equals("")) {
					// 문자열로 된 그룹 리스트 파싱 id_name,id_name,...
					String[] arrGroup = strGroupList.split(",");

					for(String strGroup: arrGroup) {
						//						Log.i("strGroup", strGroup);
						GroupInfo gInfo = new GroupInfo();
						String[] arrGinfo = strGroup.split("\\|");
						//						Log.i("str",arrGinfo[0]+"");
						gInfo.setGroup_id(Integer.parseInt(arrGinfo[0]));
						gInfo.setGroup_name(arrGinfo[1]);

						gInfo.setGroup_total_number(Integer.parseInt(arrGinfo[2]));
						group_list.add(gInfo);
					}
				}
				// 그룹 리스트 중 제일 첫번째 그룹을 보여준다. 
				if(group_list.size() > 0) view_groupinfo = group_list.get(0);
				else {
					view_groupinfo = new GroupInfo();
					view_groupinfo.setGroup_id(0);
					view_groupinfo.setGroup_name("공개라운지");
				}
				AppUser.GROUP_LIST = group_list;

				handler.sendEmptyMessage(0);
			}
		});
		thread.start();
	}

	@Override
	public void onBackPressed() {
		//Log.i("Back Key", "KEY DOWN");
		if(!mFlag) {
			Toast.makeText(context, "'뒤로' 버튼을 한번 더 누르면 종료됩니다.", LENGTH_TO_SHOW).show();
			mFlag = true;
			handler.sendEmptyMessageDelayed(CLOSE_MESSAGE, 1000);
			//bResult = false;
		} else {
			finish();
		}
	}

	@Override
	protected void onDestroy() {
		Log.i("Destory", "app is destroyed");
		if (mRegisterTask != null) {
			mRegisterTask.cancel(true);
		}

		RecycleUtils.recursiveRecycle(getWindow().getDecorView());
		System.gc();

		super.onDestroy();
		stopService(intent);
		Log.i("Destory", "app is destroyed");
		if(mImageFetcher != null){
			mImageFetcher.closeCache();
		}
	}
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	/* (non-Javadoc)
	 * @see android.app.ActivityGroup#onStop()
	 */
	@Override
	protected void onPause() {
		stopService(intent);
		super.onPause();
		if(mImageFetcher != null){
			mImageFetcher.setExitTasksEarly(true);
			mImageFetcher.flushCache();
		}
	}
	@Override
	public void onResume() {
		super.onResume();
		if(mImageFetcher != null){
			mImageFetcher.setExitTasksEarly(false);
		}
	}
	public IBinder onBind(Intent intent) {
		return null;
	}

}