package kr.co.ktech.cse.activity;


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
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ImageView.ScaleType;
import android.widget.TableLayout.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.Toast;
import static kr.co.ktech.cse.CommonUtilities.FLAG_REPLY_POST;
import static kr.co.ktech.cse.CommonUtilities.GROUP_MESSAGE_LIST;
import static kr.co.ktech.cse.CommonUtilities.IMAGE_CACHE_DIR;
import static kr.co.ktech.cse.CommonUtilities.MESSAGE_VIEW_NUMBER;
import static kr.co.ktech.cse.CommonUtilities.MY_MESSAGE_LIST;
import static kr.co.ktech.cse.CommonUtilities.PERSONAL_MESSAGE_LIST;
import static kr.co.ktech.cse.CommonUtilities.REPLY_MESSAGE_LIST;
import static kr.co.ktech.cse.CommonUtilities.TAG;

public class KLoungeMsg extends FragmentActivity implements OnItemSelectedListener{

	private static int KLOUNGE_WRITE_MESSAGE = 1;
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
	private Intent intent;
	private KLoungeMessageThread message_thread;
	private ImageFetcher mImageFetcher;
	private String TAG = "KLoungeMsg";
	private int previous_gid = -1;
	private int mImageThumbSize;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		if(AppConfig.DEBUG)Log.d(TAG,"onCreate");
		
		createProgressDialog();
		
		group_list =  new ArrayList<GroupInfo>();
		context = getApplicationContext();
		pref = getSharedPreferences("KLounge", 0);
		intent = getParent().getIntent();
		
		AppUser.user_id = pref.getInt("user_id", 0);
		AppUser.user_name = pref.getString("user_name", "");
		AppUser.CURRENT_TAB = AppUser.KLOUNGE_TAB;

		view_groupinfo = new GroupInfo();

		String strGroupList = pref.getString("group_list", "");
		//Log.i("pref_group_list", "KLoungeMsg.java:"+strGroupList);

		if(!strGroupList.equals("")) {
			// 문자열로 된 그룹 리스트 파싱 id_name, id_name,...
			String[] arrGroup = strGroupList.split(",");
			for(String strGroup: arrGroup) {
				GroupInfo gInfo = new GroupInfo();
				String[] arrGinfo = strGroup.split("\\|");
				gInfo.setGroup_id(Integer.parseInt(arrGinfo[0]));
				gInfo.setGroup_name(arrGinfo[1]);

				group_list.add(gInfo);
			}
		}
		AppUser.GROUP_LIST = group_list;
		
		int group_id = 0;
		group_id = intent.getIntExtra("group_id", 0);
		String group_name = intent.getStringExtra("group_name");
		
		view_groupinfo.setGroup_id(group_id);
		view_groupinfo.setGroup_name(group_name);
		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_klounge_msg);
		// 그룹 리스트 중 제일 첫번째 그룹을 보여준다.
		if(group_list.size() > 0 && view_groupinfo.getGroup_id() <= 0) {
			view_groupinfo = group_list.get(0);
		} else if (group_list.size() < 0 && view_groupinfo.getGroup_id() < 0){
			view_groupinfo.setGroup_id(0);
			view_groupinfo.setGroup_name("공개라운지");
		}
		
		mImageThumbSize = getResources().getDimensionPixelSize(R.dimen.big_image_size);
		AppUser.mImageFetcher = new ImageFetcher(this, mImageThumbSize);
		AppUser.mImageFetcher.setLoadingImage(R.drawable.no_photo);
		
		/* 순서 바꾸지 말 것 */
		ImageCacheParams cacheParams = new ImageCacheParams(this, IMAGE_CACHE_DIR);
		cacheParams.setMemCacheSizePercent(0.25f); 
		AppUser.mImageFetcher.addImageCache(getSupportFragmentManager(), cacheParams);
		
		mImageFetcher = AppUser.mImageFetcher;
		makeView();
	}
	
	@Override
	protected void onResume() {
		// AppUser 에 
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
//		if(AppConfig.DEBUG)Log.d(TAG,"onPause");
		previous_gid = view_groupinfo.getGroup_id();
		AppUser.SHARED_GROUPID =previous_gid;
		if(AppConfig.DEBUG)Log.d(TAG, "on pause id : "+previous_gid);
		
		super.onPause();
	}
	
	private void makeView(){

		spin_group_list = (Spinner)findViewById(R.id.group_list_spinner);
		spin_group_list.setPrompt("그룹리스트"); // 스피너 제목
		spin_group_list.getLayoutParams().width = CommonUtilities.DPFromPixel(context, 320);
		//      adapter = new SpinAdapter(KLoungeMsg.this, android.R.layout.simple_spinner_item, group_list);
		adapter = new SpinAdapter(KLoungeMsg.this, R.layout.spinner_style, group_list);
		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spin_group_list.setAdapter(adapter);

		spin_group_list.setOnItemSelectedListener(this);

		int pos = adapter.getPosition(view_groupinfo);

		//		Log.i("KLOUNGE_MSG+ adapter pos", String.valueOf(pos));
		spin_group_list.setSelection(pos);

		linear = (LinearLayout)findViewById(R.id.layout_klounge_message);

		btn_write_message = (Button)findViewById(R.id.btn_send_main_message);
		btn_write_message.getLayoutParams().width = CommonUtilities.DPFromPixel(context, 120);
		btn_write_message.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(KLoungeMsg.this, WriteMessage.class);
				intent.putExtra("to_group_id", view_groupinfo.getGroup_id());
				intent.putExtra("to_group_name", view_groupinfo.getGroup_name());
				KLoungeMsg.this.startActivityForResult(intent, KLOUNGE_WRITE_MESSAGE);
			}
		});

		final ScrollView sc = (ScrollView)findViewById(R.id.klounge_msg_scrollview);
		sc.setOnTouchListener(new View.OnTouchListener() {

			public boolean onTouch(View v, MotionEvent event) {

				int totalHeight = sc.getChildAt(0).getHeight();
				int currentPos = sc.getHeight()+sc.getScrollY();
				if(totalHeight * 0.8 < currentPos) {
					//Log.i("totalheight", String.valueOf(totalHeight));
					//Log.i("current pos", String.valueOf(currentPos));
					if(IS_MORE_MESSAGE) {
						//						Log.i("실행 수", "  ");
						IS_MORE_MESSAGE = false;
						message_thread = new KLoungeMessageThread(handler);
						message_thread.setParameter(view_groupinfo.getGroup_id());
						message_thread.start();
					}
				}
				return false;
			}
		});

		// 댓글 알림
		String type = intent.getStringExtra("type");
		if(type != null && type.length() > 0) {
			if(type.equals(FLAG_REPLY_POST)) {
				//				Log.i("KLounge reply open", group_name);
				Intent replyIntent = new Intent(this, ReplyActivity.class);

				SnsAppInfo snsinfo = intent.getParcelableExtra("snsAppInfo");
				replyIntent.putExtra("snsAppInfo", snsinfo);

				startActivity(replyIntent);
				intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
			}
		}
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_klounge_msg, menu);
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
	public void onNothingSelected(AdapterView<?> parent) {

	}

	public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {

		linear.removeAllViews();
		//if(pos==0) {Log.i("spinner pos", "0");}
		//else {
		GroupInfo group_info = (GroupInfo)spin_group_list.getSelectedItem();
		//GoupInfo group_info = (GroupInfo)adapter.getItem(pos);
		//		Log.i("KLOUNGE_MSG", "onItemSelected +groupid: "+String.valueOf(group_info.getGroup_id()));

		view_groupinfo.setGroup_id(group_info.getGroup_id());
		view_groupinfo.setGroup_name(group_info.getGroup_name());

		message_thread = new KLoungeMessageThread(handler,pd);
		message_thread.setParameter(view_groupinfo.getGroup_id(), 0);
		message_thread.start();
		//klounge.setKloungeGroupMessageList(this, linear, view_groupinfo.getGroup_id(), false);
		//}
		intent.putExtra("group_id", view_groupinfo.getGroup_id());
		intent.putExtra("group_name", view_groupinfo.getGroup_name());
	}

	@Override
	protected  void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(requestCode == KLOUNGE_WRITE_MESSAGE) {
			//klounge.setKloungeGroupMessageList(this, linear, view_groupinfo.getGroup_id(), false);
			linear.removeAllViews();
			message_thread = new KLoungeMessageThread(handler);
			message_thread.setParameter(view_groupinfo.getGroup_id(), 0);
			message_thread.start();
		}
	}

	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if(msg.what == GROUP_MESSAGE_LIST) {
				List<SnsAppInfo> messageList = (List<SnsAppInfo>)msg.obj;

				if(messageList.size() <=0 && msg.arg1 < 1){
					Toast.makeText(KLoungeMsg.this, getResources().getText(R.string.sorry_empty_list), Toast.LENGTH_SHORT).show();
				}
				MessageLayoutSetting mls = new MessageLayoutSetting(KLoungeMsg.this, linear);
				if(messageList.size() > 0) {
					for(int i=0; i<messageList.size(); i++) {
						SnsAppInfo sInfo = messageList.get(i);

						mls.setMessageContentUsingRelativeLayout(sInfo , mImageFetcher);
					}
					IS_MORE_MESSAGE = true;
				}
			}
			if(pd != null)pd.dismiss();
		}
	};

	@Override
	public void onBackPressed() {
		this.getParent().onBackPressed();
	}

	/* (non-Javadoc)
	 * @see android.app.Activity#onStart()
	 */
	@Override
	protected void onStart() {
		if(AppConfig.DEBUG)Log.d(TAG,"onStart");
		super.onStart();
	}
	/* (non-Javadoc)
	 * @see android.app.Activity#onStop()
	 */
	@Override
	protected void onStop() {
		if(AppConfig.DEBUG)Log.d(TAG,"onStop");
		super.onStop();
	}
	

	@Override
	protected void onDestroy() {
		if(AppConfig.DEBUG)Log.d(TAG,"onDestroy");
		// Adapter가 있으면 어댑터에서 생성한 recycle메소드를 실행
		if (adapter != null)
			adapter.recycle();
		RecycleUtils.recursiveRecycle(getWindow().getDecorView());
		System.gc();

		super.onDestroy();
	}
	public void createProgressDialog(){
		pd = ProgressDialog.show(this, "", getResources().getText(R.string.msg_contents), true);
	}
}

class KLoungeMessageThread extends Thread {
	private Handler handler;
	private List<SnsAppInfo> messageList;
	private KLoungeRequest kloungehttp;
	private int group_id;
	private String TAG = "KLOUNGE_MSG_THREAD";
	private static int RELOAD = 0;

	public KLoungeMessageThread(Handler handler, ProgressDialog pdialog) {
		this.handler = handler;
		kloungehttp = new KLoungeRequest();
		messageList = new ArrayList<SnsAppInfo>();
		pdialog.show();

	}
	public KLoungeMessageThread(Handler handler) {
		this.handler = handler;
		kloungehttp = new KLoungeRequest();
		messageList = new ArrayList<SnsAppInfo>();
	}

	public void setParameter(int group_id) {
		this.group_id = group_id;
	}
	public void setParameter(int group_id, int reload) {
		//		Log.i(TAG, "GID: "+group_id);
		this.group_id = group_id;
		this.RELOAD = reload;
	}
	@Override
	public void run() {
		messageList = kloungehttp.getGroupMessageList(AppUser.user_id, group_id, RELOAD);

		Message msg = Message.obtain();

		msg.what = GROUP_MESSAGE_LIST;
		msg.arg1 = RELOAD; 
		msg.obj = messageList;
		handler.sendMessage(msg);

		RELOAD++;
	}	
}
