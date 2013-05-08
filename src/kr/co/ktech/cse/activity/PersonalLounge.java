package kr.co.ktech.cse.activity;


import java.util.ArrayList;
import java.util.List;
import kr.co.ktech.cse.R;
import kr.co.ktech.cse.CommonUtilities;
import kr.co.ktech.cse.adapter.PersonalSpinAdapter;
import kr.co.ktech.cse.AppConfig;
import kr.co.ktech.cse.bitmapfun.util.ImageFetcher;
import kr.co.ktech.cse.db.KLoungeGroupRequest;
import kr.co.ktech.cse.db.KLoungeRequest;
import kr.co.ktech.cse.model.AppUser;
import kr.co.ktech.cse.model.GroupInfo;
import kr.co.ktech.cse.model.SnsAppInfo;
import kr.co.ktech.cse.processes.MessageLayoutSetting;
import kr.co.ktech.cse.util.RecycleUtils;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.util.Log;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ImageView.ScaleType;
import android.widget.Toast;
import static kr.co.ktech.cse.CommonUtilities.GROUP_MESSAGE_LIST;

public class PersonalLounge extends Activity implements OnItemSelectedListener {
	private static boolean IS_MORE_MESSAGE = true;

	List<GroupInfo> group_list;
	GroupInfo view_groupinfo;

//	private KLoungeGroupRequest grouprequest;
	private int messageNumber;
	private RelativeLayout userInfoLayout;
	private PersonalSpinAdapter adapter;
	private Spinner spin_group_list;
	private LinearLayout linear;
	private Button btn_write_message;
	private ImageView personal_image;
	private TextView personal_user_name;
	private ImageView imageView;
	private TextView textView;
	private TextView message_empty;
	public ProgressDialog pd;
	private int puser_id = 0;
	private Context context;
	private String puser_name = "";
	private String puser_photo = "";
	private PersonalKLoungeMessageThread message_thread;
	//	private Context mContext = getParent();
	private ImageFetcher mImageFetcher;
	DisplayUtil du;
	SparseArray<List<GroupInfo>> memInfoArray = new SparseArray<List<GroupInfo>>();
	private String TAG = "PersonalLounge";
	@Override
	public void onCreate(Bundle savedInstanceState) {
		group_list =  new ArrayList<GroupInfo>();
		view_groupinfo = new GroupInfo();
		
		context = getApplicationContext();
		
		makeBackgroundTask();
		
		createProgressDialog();
		du = new DisplayUtil(context);
		Intent intent = getIntent();
		puser_id = Integer.parseInt(intent.getStringExtra("puser_id"));
		puser_name = intent.getStringExtra("puser_name");
		puser_photo = intent.getStringExtra("puser_photo").replace(" ", "%20");
		
//		memInfoArray = AppUser.MEMBER_INFO;
//		
//		group_list = memInfoArray.get(puser_id);
		
//		if(group_list == null){
//			
//			if(AppConfig.DEBUG)Log.d(TAG, "AppUser Member Info List is null");
//			// AppUser 에 아직 저장이 안 되어 있다면, 화면 열 때 
//			// 사용자 그룹 리스트 및 정보를 가져 와야한다.
//			// 타 사용자 그룹리스트 설정
//			grouprequest = new KLoungeGroupRequest();
//			Thread groupList_thread = new Thread(new Runnable() {
//				public void run() {
//					group_list = grouprequest.getGroupList(AppUser.user_id, puser_id);
//				}
//			});
//			groupList_thread.start();
//		}
//		try {
//			Thread.sleep(500);
//		} catch (InterruptedException e) {
//			Log.i(TAG,e.toString());
//		}
		
		if(view_groupinfo == null){
			if(AppConfig.DEBUG)Log.d(TAG, "view group info null");
		}else if(group_list == null){
			if(AppConfig.DEBUG)Log.d(TAG, "group list null");
		}
		int gsize = group_list.size();
//		int groupId = AppUser.SHARED_GROUPID;
		// 그룹 리스트 중 제일 첫번째 그룹을 보여준다.
		
		if(gsize > 0 && view_groupinfo.getGroup_id() <= 0) {
			view_groupinfo = group_list.get(0);
		} else if (gsize < 0 && view_groupinfo.getGroup_id() < 0){
			view_groupinfo.setGroup_id(0);
			view_groupinfo.setGroup_name("공개라운지");
		}
		
		mImageFetcher = AppUser.mImageFetcher;
//		mImageFetcher.setLoadingImage(R.drawable.no_photo);
		
		super.onCreate(savedInstanceState);

		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_personal_lounge);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
		imageView = (ImageView)findViewById(R.id.favicon);
		textView = (TextView)findViewById(R.id.right_text);
		imageView.setImageResource(R.drawable.icon_klounge);
		
		spin_group_list = (Spinner)findViewById(R.id.personal_group_list_spinner);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		makeView();
		
//		int pos = adapter.getPosition(view_groupinfo);
//		if(AppConfig.DEBUG)Log.d(TAG, "position : "+pos);
//		spin_group_list.setSelection(pos);
//		if(AppConfig.DEBUG)Log.d(TAG, "last of creat");
	}
	void makeView(){
		personal_user_name = (TextView)findViewById(R.id.personal_user_name);
		personal_user_name.setText(puser_name);

		personal_image = (ImageView)findViewById(R.id.personal_imageview);
		LinearLayout.LayoutParams ivParams = new LinearLayout.LayoutParams(du.PixelToDP(100), du.PixelToDP(100));
		//        ivParams.setMargins(du.PixelToDP(10), du.PixelToDP(10), 0, 0);
		if(AppConfig.DEBUG)Log.d(TAG, "user photo: "+puser_photo);
		puser_photo = puser_photo.replace(" ", "%20");
		mImageFetcher.loadImage(puser_photo, personal_image);
		personal_image.setLayoutParams(ivParams);
		personal_image.setAdjustViewBounds(true);
//		personal_image.setMinimumHeight(du.PixelToDP(100));
//		personal_image.setMinimumWidth(du.PixelToDP(100));
//		personal_image.setMaxHeight(du.PixelToDP(120));
//		personal_image.setMaxWidth(du.PixelToDP(120));

		personal_image.setScaleType(ImageView.ScaleType.FIT_XY);
//		personal_image.setScaleType(ScaleType.CENTER_CROP);
		//		AppClickListener acListener = new AppClickListener(puser_photo, mContext);//new AppClickListener
		//		personal_image.setOnClickListener(acListener);

//		adapter = new PersonalSpinAdapter(this, android.R.layout.simple_spinner_item); 
//		spin_group_list.getLayoutParams().width = CommonUtilities.DPFromPixel(context, 300);
//		spin_group_list.setPrompt("그룹리스트"); // 스피너 제목
//		spin_group_list.setAdapter(adapter);

		spin_group_list.setOnItemSelectedListener(this);

		linear = (LinearLayout)findViewById(R.id.personal_lounge_message_layout);
		
//		message_thread = new PersonalKLoungeMessageThread(handler, pd);
//		message_thread.setParameter(view_groupinfo.getGroup_id(), puser_id, 0);
//		message_thread.start();

		btn_write_message = (Button)findViewById(R.id.personal_write_message_btn);
		btn_write_message.getLayoutParams().width = CommonUtilities.DPFromPixel(context, 110);
		btn_write_message.getLayoutParams().height = CommonUtilities.DPFromPixel(context, 55);
		btn_write_message.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(PersonalLounge.this, WriteMessage.class);
				intent.putExtra("to_group_id", view_groupinfo.getGroup_id());
				intent.putExtra("to_group_name", view_groupinfo.getGroup_name());
				intent.putExtra("to_puser_id", puser_id);
				PersonalLounge.this.startActivity(intent);
			}
		});

		ScrollView sc = (ScrollView)findViewById(R.id.personal_lounge_scrollview);
		sc.setOnTouchListener(new View.OnTouchListener() {
			public boolean onTouch(View v, MotionEvent event) {
				ScrollView sv = (ScrollView)v;
				int totalHeight = sv.getChildAt(0).getHeight();
				int currentPos = sv.getHeight()+sv.getScrollY();
				if(totalHeight * 0.8 < currentPos) {
					if(IS_MORE_MESSAGE) {
						IS_MORE_MESSAGE = false;
						message_thread = new PersonalKLoungeMessageThread(handler);
						message_thread.setParameter(view_groupinfo.getGroup_id(), puser_id);
						message_thread.start();
					}
				}
				return false;
			}
		});
		
	}
	
	private void makeBackgroundTask(){
		mTask.execute();
	}
	
	Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			switch(msg.what){
				case GROUP_MESSAGE_LIST :
//				if(msg.what == GROUP_MESSAGE_LIST) {
					List<SnsAppInfo> messageList = (List<SnsAppInfo>)msg.obj;
					messageNumber  = msg.arg1;
					if(messageNumber == 0){
						if(pd!=null)pd.dismiss();
						Toast.makeText(context, getResources().getText(R.string.sorry_empty_list), Toast.LENGTH_SHORT).show();
					}else if(messageNumber < 0){
						if(pd!=null)pd.dismiss();
						Toast.makeText(context, getResources().getText(R.string.sorry_text), Toast.LENGTH_SHORT).show();
						Log.i(TAG,"Loading Message List Error "+this.getClass().toString());
						finish();
					}
					MessageLayoutSetting mls = new MessageLayoutSetting(PersonalLounge.this, linear);
	
					if(messageList.size() > 0) {
						for(int i=0; i<messageList.size(); i++) {
							SnsAppInfo sInfo = messageList.get(i);
							mls.setMessageContentUsingRelativeLayout(sInfo, mImageFetcher);
						}
						IS_MORE_MESSAGE = true;			    	
					}
					break;
//				}
			}
			if(pd!=null)pd.dismiss();
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_personal_lounge, menu);
		return true;
	}

	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		GroupInfo group_info = (GroupInfo)spin_group_list.getSelectedItem();
		//		Log.i("group_id", String.valueOf(group_info.getGroup_id()));

		view_groupinfo.setGroup_id(group_info.getGroup_id());
		view_groupinfo.setGroup_name(group_info.getGroup_name());
		//personallounge.setPersonalLoungeMessageList(this, linear, view_groupinfo.getGroup_id(), puser_id, false);
		linear.removeAllViews();
		message_thread = new PersonalKLoungeMessageThread(handler, pd);
		message_thread.setParameter(view_groupinfo.getGroup_id(), puser_id, 0);
		message_thread.start();
	}

	public void onNothingSelected(AdapterView<?> arg0) {
	}
	@Override
	public void onResume() {
//		int gid = AppUser.SHARED_GROUPID;
//		
//		if(AppConfig.DEBUG)Log.d(TAG, "gid: "+gid);
//		if(gid >= 0){
//			view_groupinfo.setGroup_id(gid);
//			if(AppConfig.DEBUG)Log.d(TAG, "name : "+view_groupinfo.getGroup_name());
//		}
//		
		super.onResume();
	}

	@Override
	public void onPause() {
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
	class DisplayUtil {
		private static final float DEFAULT_HDIP_DENSITY_SCALE = 1.5f;

		private final float scale;

		public DisplayUtil(Context context) {
			scale = context.getResources().getDisplayMetrics().density;
		}
		public int PixelToDP(int pixel) {
			return (int) (pixel / DEFAULT_HDIP_DENSITY_SCALE * scale);
		}
		public int DPToPixel(final Context context, int DP) {
			return (int) (DP / scale * DEFAULT_HDIP_DENSITY_SCALE);
		}
	}
//	private ProgressDialog dialog;
	
	private AsyncTask<Void, Integer, List<GroupInfo>> mTask = new AsyncTask<Void, Integer, List<GroupInfo>>(){
		KLoungeGroupRequest grouprequest = new KLoungeGroupRequest();
		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onPostExecute(java.lang.Object)
		 */
		@Override
		protected void onPostExecute(List<GroupInfo> result) {
			adapter = new PersonalSpinAdapter(context, android.R.layout.simple_spinner_item, result);
			spin_group_list.getLayoutParams().width = CommonUtilities.DPFromPixel(context, 300);
			spin_group_list.setPrompt("그룹리스트"); // 스피너 제목
			spin_group_list.setAdapter(adapter);
			int groupId = AppUser.SHARED_GROUPID;
			if(groupId >= 0){
				view_groupinfo.setGroup_id(groupId);
				int pos = adapter.getPosition(view_groupinfo);
				if(AppConfig.DEBUG)Log.d(TAG, "position : "+pos);
				spin_group_list.setSelection(pos);
			}
//			dialog.dismiss();
//			Toast.makeText(getApplicationContext(), "로딩완료", 0).show();
		}

		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onPreExecute()
		 */
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
		}

		/* (non-Javadoc)
		 * @see android.os.AsyncTask#onProgressUpdate(Progress[])
		 */
		@Override
		protected void onProgressUpdate(Integer[] values) {
	                  switch(values[0]) {
	                  case -1:
//	                        Toast.makeText(getApplicationContext(), "통신 오류", 0).show();
	                        break;
	                  case 1:
//	                        dialog.setMessage("접속 중...");
	                        break;
	                  case 2:
//	                        dialog.setMessage("데이터 수신 중...");
	                        break;
	                  case 3:
//	                        dialog.setMessage("데이터 분석 중...");
	                        break;
	                  }
//	                  dialog.setProgress(values[0]);
	            }

		@Override
		protected List<GroupInfo> doInBackground(Void... Params) {
			try {
				publishProgress(2);
				group_list = grouprequest.getGroupList(AppUser.user_id, puser_id);
			}catch(Exception e){
				Log.d(TAG, "background exception - "+e);
			}
			return group_list;
		}
	};
	
}
class PersonalKLoungeMessageThread extends Thread {
	private static final String INNER_TAG = "PersonalLoungeChildThread";
	private Handler handler;
	private List<SnsAppInfo> messageList;
	private KLoungeRequest kloungehttp;
	private int group_id;
	private int puser_id;
	private static int RELOAD = 0;

	public PersonalKLoungeMessageThread(Handler handler) {
		this.handler = handler;
		kloungehttp = new KLoungeRequest();
		messageList = new ArrayList<SnsAppInfo>();

		group_id = 0;
		puser_id = 0;
	}
	public PersonalKLoungeMessageThread(Handler handler, ProgressDialog pdialog) {
		this.handler = handler;
		kloungehttp = new KLoungeRequest();
		messageList = new ArrayList<SnsAppInfo>();
		pdialog.show();
		group_id = 0;
		puser_id = 0;
	}

	public void setParameter(int group_id, int puser_id) {
		this.group_id = group_id;
		this.puser_id = puser_id;
	}
	public void setParameter(int group_id, int puser_id, int reload) {
		this.group_id = group_id;
		this.puser_id = puser_id;
		this.RELOAD = reload;
	}
	@Override
	public void run() {
		messageList = kloungehttp.getPersonalLoungeMessageList(AppUser.user_id, group_id, puser_id, RELOAD);
		int messageListSize = -1;
		messageListSize = messageList.size();
//		String strListSize = String.valueOf(messageListSize);
//		ArrayList<String> msgList = new ArrayList<String>();

		//		Log.i(TAG,"MESSAGE LIST SIZE: "+messageListSize);

//		for(int i=0;i<messageListSize;i++){
//			msgList.add(messageList.get(i).getBody());
//			//			Log.i(TAG,messageList.get(i).getBody());
//		}

		Message msg = Message.obtain();

		msg.what = GROUP_MESSAGE_LIST;
		msg.obj = messageList;
		msg.arg1 = messageListSize;
		
		handler.sendMessage(msg);

		RELOAD++;
	}	
}