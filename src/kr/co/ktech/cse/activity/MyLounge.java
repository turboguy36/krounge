package kr.co.ktech.cse.activity;

import static kr.co.ktech.cse.CommonUtilities.GROUP_MESSAGE_LIST;
import static kr.co.ktech.cse.CommonUtilities.MESSAGE_VIEW_NUMBER;
import static kr.co.ktech.cse.CommonUtilities.MY_MESSAGE_LIST;
import static kr.co.ktech.cse.CommonUtilities.PERSONAL_MESSAGE_LIST;
import static kr.co.ktech.cse.CommonUtilities.REPLY_MESSAGE_LIST;

import java.util.ArrayList;
import java.util.List;

import kr.co.ktech.cse.AppConfig;
import kr.co.ktech.cse.R;
import kr.co.ktech.cse.R.layout;
import kr.co.ktech.cse.R.menu;
import kr.co.ktech.cse.CommonUtilities;
import kr.co.ktech.cse.adapter.SpinAdapter;
import kr.co.ktech.cse.bitmapfun.util.ImageFetcher;
import kr.co.ktech.cse.bitmapfun.util.ImageCache.ImageCacheParams;
import kr.co.ktech.cse.db.KLoungeRequest;
import kr.co.ktech.cse.model.AppUser;
import kr.co.ktech.cse.model.GroupInfo;
import kr.co.ktech.cse.model.SnsAppInfo;
import kr.co.ktech.cse.processes.MessageLayoutSetting;
import kr.co.ktech.cse.util.RecycleUtils;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ImageView.ScaleType;
import android.widget.TableLayout.LayoutParams;
import android.widget.Toast;

public class MyLounge extends Activity implements OnItemSelectedListener {

	private static int MY_MESSAGE_LIST = 1;
	private static boolean IS_MORE_MESSAGE = true;
	private SharedPreferences pref;
	private List<GroupInfo> group_list;
	private GroupInfo view_groupinfo;
	private Context context;
	private SpinAdapter adapter;
	private Spinner spin_group_list;
	private LinearLayout linear;
	private Button btn_write_message;
	private ProgressDialog pd;
	private LinearLayout myLounge_topLayout;
	private MyKLoungeMessageThread message_thread;
	private ImageFetcher mImageFetcher;
	private String TAG = "MyLounge";
	private int previous_gid = -1;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		createProgressDialog();
		context = getApplicationContext();
		group_list =  new ArrayList<GroupInfo>();
		pref = getSharedPreferences("KLounge", 0);

		AppUser.user_id = pref.getInt("user_id", 0);
		AppUser.user_name = pref.getString("user_name", "");
		AppUser.CURRENT_TAB = AppUser.MYLOUNGE_TAB;
		view_groupinfo = new GroupInfo();

		String strGroupList = pref.getString("group_list", "");
		//    	Log.i("pref_group_list", "MyLounge.java:"+strGroupList);
		if(!strGroupList.equals("")) {
			// 문자열로 된 그룹 리스트 파싱 id_name,id_name,...
			String[] arrGroup = strGroupList.split(",");
			for(String strGroup: arrGroup) {
				GroupInfo gInfo = new GroupInfo();
				String[] arrGinfo = strGroup.split("\\|");
				gInfo.setGroup_id(Integer.parseInt(arrGinfo[0]));
				gInfo.setGroup_name(arrGinfo[1]);
				group_list.add(gInfo);
			}
		}
		/*
	    	Intent intent = getParent().getIntent();
	    	int group_id = 0;
	    	group_id = intent.getIntExtra("group_id", 0);
	    	Log.i("view_group_id", String.valueOf(group_id));
	    	view_groupinfo.setGroup_id(group_id);
		 */
		// 그룹 리스트 중 제일 첫번째 그룹을 보여준다. 
		if(group_list.size() > 0 && view_groupinfo.getGroup_id() <= 0) {
			view_groupinfo = group_list.get(0);
		} else if (group_list.size() < 0 && view_groupinfo.getGroup_id() < 0){
			view_groupinfo.setGroup_id(0);
			view_groupinfo.setGroup_name("공개라운지");
		}
		AppUser.GROUP_LIST = group_list;
		//    	Log.i("view_group", view_groupinfo.getGroup_id()+"_"+view_groupinfo.getGroup_name());

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_my_lounge);
		if(AppConfig.DEBUG){}
		LinearLayout.LayoutParams spinParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		
		spin_group_list = (Spinner)findViewById(R.id.mylounge_group_list_spinner);
		adapter = new SpinAdapter(this, android.R.layout.simple_spinner_item, group_list); 
		spin_group_list.setPrompt("그룹리스트"); // 스피너 제목
		spin_group_list.getLayoutParams().width = CommonUtilities.DPFromPixel(context, 320);
		spin_group_list.setAdapter(adapter);
		spin_group_list.setOnItemSelectedListener(this);
		int pos = adapter.getPosition(view_groupinfo);
		spin_group_list.setSelection(pos);
		//spin_group_list.setLayoutParams(spinParams);
		spin_group_list.invalidate();
		
		LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
		
		btn_write_message = (Button)findViewById(R.id.btn_send_my_message);
		//btn_write_message.setLayoutParams(btnParams);
		btn_write_message.getLayoutParams().width = CommonUtilities.DPFromPixel(context, 120);
		
		btn_write_message.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(MyLounge.this, WriteMessage.class);
				intent.putExtra("to_group_id", view_groupinfo.getGroup_id());
				intent.putExtra("to_group_name", view_groupinfo.getGroup_name());
				MyLounge.this.startActivity(intent);
			}
		});

		linear = (LinearLayout)findViewById(R.id.layout_mylounge_message);
		//mylounge.setMyLoungeMessageList(this, linear, view_groupinfo.getGroup_id(), false);
		final ScrollView sc = (ScrollView)findViewById(R.id.mylounge_msg_scrollview);
		sc.setOnTouchListener(new View.OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {
				int totalHeight = sc.getChildAt(0).getHeight();
				int currentPos = sc.getHeight()+sc.getScrollY();
				if(totalHeight * 0.8 < currentPos) {
					if(IS_MORE_MESSAGE) {
//						Log.i("실행 수", "  ");
						IS_MORE_MESSAGE = false;
						
						message_thread = new MyKLoungeMessageThread(handler);
						message_thread.setParameter(view_groupinfo.getGroup_id());
						message_thread.start();
						
					}
				}
				return false;
			}
		});
		mImageFetcher = AppUser.mImageFetcher;
	}

	Handler handler = new Handler() {
		public void handleMessage(Message msg) {

			if(msg.what == MY_MESSAGE_LIST) {
				List<SnsAppInfo> messageList = (List<SnsAppInfo>)msg.obj; 
				if(messageList.size() <=0 && msg.arg1 < 1){
					Toast.makeText(MyLounge.this, getResources().getText(R.string.sorry_empty_list), Toast.LENGTH_LONG).show();
				}
				MessageLayoutSetting mls = new MessageLayoutSetting(MyLounge.this, linear);
				if(messageList.size() > 0) {
					for(int i=0; i<messageList.size(); i++) {
						SnsAppInfo sInfo = messageList.get(i);

						//mls.setMessageLayout(context, sInfo, linear);
						//			    		mls.setMessageContentUsingRelativeLayout(linear, sInfo);
						mls.setMessageContentUsingRelativeLayout(sInfo, mImageFetcher);
					}
					IS_MORE_MESSAGE = true;
				}
			}
			if(pd != null)pd.dismiss();
		}
	};
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_my_lounge, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_logout:
			if(pref==null) pref = getSharedPreferences("KLounge", 0);
			SharedPreferences.Editor edit = pref.edit();
			edit.putBoolean("loginState", false);
			edit.putInt("user_id", 0);
			edit.putString("user_name", "");
			edit.putString("group_list", "");

			edit.clear();
			edit.commit();

			finish();

			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}
	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		GroupInfo group_info = (GroupInfo)spin_group_list.getSelectedItem();
		Log.i("group_id", "onItemSelected"+String.valueOf(group_info.getGroup_id()));

		view_groupinfo.setGroup_id(group_info.getGroup_id());
		view_groupinfo.setGroup_name(group_info.getGroup_name());
		//mylounge.setMyLoungeMessageList(this, linear, view_groupinfo.getGroup_id(), false);

		linear.removeAllViews();
		message_thread = new MyKLoungeMessageThread(handler,pd);
		message_thread.setParameter(view_groupinfo.getGroup_id(), 0);
		message_thread.start();
	}

	public void onNothingSelected(AdapterView<?> arg0) {
	}

	@Override
	public void onBackPressed() {
		this.getParent().onBackPressed();
	}
	
	@Override
	public void onResume() {
//		if(AppConfig.DEBUG)Log.d(TAG,"onResume");
		int shared_group_id = AppUser.SHARED_GROUPID;
		
//		if(AppConfig.DEBUG)Log.d(TAG,"pre / cur  "+previous_gid+"/"+shared_group_id);
		if(shared_group_id < 0){
			// 탭이동, 제일 처음 일 때
		}else{
			if(AppUser.SHARED_GROUPID == previous_gid){
//				if(AppConfig.DEBUG)Log.d(TAG,"same do nothing");
			}else{
				view_groupinfo.setGroup_id(shared_group_id);
//				if(AppConfig.DEBUG)Log.d(TAG,"make "+view_groupinfo.getGroup_id()+" view ");
				int pos = adapter.getPosition(view_groupinfo);
//				if(AppConfig.DEBUG)Log.d(TAG, "position : "+pos);
				spin_group_list.setSelection(pos);
			}
			view_groupinfo.setGroup_id(shared_group_id);
		}
		super.onResume();
	}

	@Override
	public void onPause() {
		AppUser.SHARED_GROUPID = view_groupinfo.getGroup_id();
		previous_gid = view_groupinfo.getGroup_id();
//		if(AppConfig.DEBUG)Log.d(TAG, "on pause id : "+previous_gid);
		super.onPause();
	}
	
	@Override
	protected void onDestroy() {
		// Adapter가 있으면 어댑터에서 생성한 recycle메소드를 실행
		if (adapter != null)
			adapter.recycle();
		RecycleUtils.recursiveRecycle(getWindow().getDecorView());
		System.gc();

		super.onDestroy();
	}
	void createProgressDialog(){
		pd = ProgressDialog.show(this, "", getResources().getText(R.string.msg_contents), true, true);
	}
}

class MyKLoungeMessageThread extends Thread {
	private Handler handler;
	private List<SnsAppInfo> messageList;
	private KLoungeRequest kloungehttp;

	private int group_id;
	private static int RELOAD = 0;
	public MyKLoungeMessageThread(Handler handler, ProgressDialog pdialog) {
		this.handler = handler;
		kloungehttp = new KLoungeRequest();
		messageList = new ArrayList<SnsAppInfo>();
		pdialog.show();
		group_id = 0;
	}
	public MyKLoungeMessageThread(Handler handler) {
		this.handler = handler;
		kloungehttp = new KLoungeRequest();
		messageList = new ArrayList<SnsAppInfo>();
		
		group_id = 0;
	}

	public void setParameter(int group_id) {
		this.group_id = group_id;
	}
	public void setParameter(int group_id, int reload) {
		this.group_id = group_id;
		this.RELOAD = reload;
	}
	@Override
	public void run() {

		messageList = kloungehttp.getMyLoungeMessageList(AppUser.user_id, group_id, RELOAD);

		Message msg = Message.obtain();

		msg.what = MY_MESSAGE_LIST;
		msg.obj = messageList;
		msg.arg1 = RELOAD;
		handler.sendMessage(msg);

		RELOAD++;
	}	
}