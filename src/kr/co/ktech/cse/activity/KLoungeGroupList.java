package kr.co.ktech.cse.activity;

import java.util.ArrayList;
import java.util.List;
import kr.co.ktech.cse.AppConfig;
import kr.co.ktech.cse.R;
import kr.co.ktech.cse.CommonUtilities;
import kr.co.ktech.cse.bitmapfun.ui.ImageGridActivity;
import kr.co.ktech.cse.bitmapfun.util.ImageFetcher;
import kr.co.ktech.cse.bitmapfun.util.ImageCache.ImageCacheParams;
import kr.co.ktech.cse.db.KLoungeGroupRequest;
import kr.co.ktech.cse.model.AppUser;
import kr.co.ktech.cse.model.GroupInfo;
import kr.co.ktech.cse.model.GroupMemberInfo;
import kr.co.ktech.cse.util.RecycleUtils;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.app.Activity;
import android.app.TabActivity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.util.SparseArray;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.TableLayout.LayoutParams;
import static kr.co.ktech.cse.CommonUtilities.IMAGE_CACHE_DIR;

public class KLoungeGroupList extends FragmentActivity{
	String TAG = "KLoungeGroupList";
	private static final int GROUP_NAME_SIZE = 17;
	private static final int GROUP_MEMBER_WIDTH = 80;
	private static final int GROUP_MEMBER_HEIGHT = 45;
	private static final int GROUP_LOUNGE_IMAGE_WIDTH = 40;
	private static final int GROUP_LOUNGE_IMAGE_HEIGHT = 40;
	public static final int tabsBG = 0xFFCFDDE8;
	SharedPreferences pref;
	int user_id;
	TableLayout tbl;
	Vibrator vibrator;
	private static final Long VIBRATE_PERIOD = CommonUtilities.VIBRATE_TIME;
	private KLoungeGroupRequest kloungehttp;
	
	SparseArray<List<GroupMemberInfo>> group_member_list;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		vibrator = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_group_list);
		kloungehttp = new KLoungeGroupRequest();
		tbl = (TableLayout)findViewById(R.id.group_list_box);
		user_id = AppUser.user_id;
		AppUser.CURRENT_TAB = AppUser.GROUP_LIST_TAB;
		setGroupList();
	}
	
	
	void createThread(final List<GroupInfo> list){
		// group list 안에 들어 갈 member list 들을 모두 받아 저장한다.
		group_member_list = new SparseArray<List<GroupMemberInfo>>();
		Thread thread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				if(AppConfig.DEBUG)Log.d(TAG, "size: "+list.size());
				for(int i=0;i<list.size()-1;i++){
					List<GroupMemberInfo> gmInfo = new ArrayList<GroupMemberInfo>();
					int group_id = list.get(i).getGroup_id();
					if(AppConfig.DEBUG)Log.d(TAG, AppUser.user_id+"/"+group_id);
					gmInfo = kloungehttp.getGroupMemberList(AppUser.user_id, group_id);
					group_member_list.append(group_id, gmInfo);
				}
				Message msg = Message.obtain();
				msg.obj = group_member_list;
				handler.sendMessage(msg);
			}
		});
		thread.start();
	}
	Handler handler = new Handler(){
		public void handleMessage(Message msg){
			SparseArray<List<GroupMemberInfo>> gminfo = (SparseArray<List<GroupMemberInfo>>) msg.obj;
			AppUser.GROUP_MEMBER = gminfo;
		}
	};
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_group_list, menu);
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
	public void setGroupList() {

		pref = getSharedPreferences("KLounge", 0);

		String strGroupList = pref.getString("group_list", "");

		//hard_coding by hglee It has to be retrieved
		strGroupList = strGroupList + "|0";
		List<GroupInfo> groupList = new ArrayList<GroupInfo>();

		if(!strGroupList.equals("")) {
			// 문자열로 된 그룹 리스트 파싱 id_name,id_name,...
			//			Log.i(TAG+" STR GL: ",strGroupList);
			String[] arrGroup = strGroupList.split(",");
			for(String strGroup: arrGroup) {
				GroupInfo gInfo = new GroupInfo();
				String[] arrGinfo = strGroup.split("\\|");
				gInfo.setGroup_id(Integer.parseInt(arrGinfo[0]));
				gInfo.setGroup_name(arrGinfo[1]);
				gInfo.setGroup_total_number(Integer.parseInt(arrGinfo[2]));
				//	    		Log.i(TAG+" GROUP TOTAL NUM",String.valueOf(gInfo.getGroup_total_number()));

				groupList.add(gInfo);
			}
		}

		TableRow tr = null;
		TableLayout.LayoutParams trParams = new TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
		trParams.setMargins(CommonUtilities.DPFromPixel(this,15), CommonUtilities.DPFromPixel(this,15), CommonUtilities.DPFromPixel(this,15), 0);

		// 그룹 리스트 출력. groupList.size()-1을 하는 이유는 마지막 "전체"를 제외시키기 위해서 
		for(int i=0; i<groupList.size()-1; i++) {
			TableRow emptyRow = new TableRow(this);
			View gapView = new View(this);
			TableRow.LayoutParams gapRowParams = new TableRow.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
			gapRowParams.height = 1;
			gapRowParams.span = 4;
			gapRowParams.bottomMargin = CommonUtilities.DPFromPixel(this, 10);
			gapRowParams.topMargin = CommonUtilities.DPFromPixel(this, 20);
			gapView.setLayoutParams(gapRowParams);
			gapView.setBackgroundColor(tabsBG);

			gapView.invalidate();
			emptyRow.addView(gapView);

			GroupInfo groupinfo = groupList.get(i);
			final int group_id = groupinfo.getGroup_id();
			final String group_name = groupinfo.getGroup_name();
			final int group_total_number = groupinfo.getGroup_total_number();

			tr = new TableRow(this);
			tr.setLayoutParams(trParams);
			tr.setGravity(Gravity.CENTER);
			tr.setPadding(0, CommonUtilities.DPFromPixel(this,5), 0, 0);
			tr.setBackgroundColor(Color.WHITE);

			ImageView ivCrossIcon = new ImageView(this);
			TableRow.LayoutParams ivIconParams = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 2.0f);
			ivIconParams.gravity = Gravity.CENTER_VERTICAL;
			ivCrossIcon.setImageResource(R.drawable.cross_expand01);
			tr.addView(ivCrossIcon);

			TextView tvGroupName = new TextView(this);
			tvGroupName.setText(group_name);
			tvGroupName.setGravity(Gravity.LEFT);
			tvGroupName.setTextColor(Color.BLACK);
			tvGroupName.setTextSize(TypedValue.COMPLEX_UNIT_SP, GROUP_NAME_SIZE);
			TableRow.LayoutParams tvParams = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 2.0f);
			tvParams.leftMargin = CommonUtilities.DPFromPixel(this, 5);
			tvGroupName.setLayoutParams(tvParams);
			tvGroupName.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					vibrator.vibrate(VIBRATE_PERIOD);
					//					Log.i("current_group_id", String.valueOf(group_id));
					// K-Lounge 에 메시지 출력
					TabActivity tabHost = (TabActivity) KLoungeGroupList.this.getParent();
					//Intent intent = new Intent(KLoungeGroupList.this, KLoungeGroupMember.class);
					Intent intent = getParent().getIntent();
					intent.putExtra("group_id", group_id);
					intent.putExtra("group_name", group_name);
					AppUser.SHARED_GROUPID = group_id;

					tabHost.getTabHost().setCurrentTab(AppUser.KLOUNGE_TAB);
				}
			});
			tr.addView(tvGroupName);

			// 그룹 메시지 보기 아이콘 (go KLoungeMsg)
			ImageView ivMessage = new ImageView(this); 
			ivMessage.setImageResource(R.drawable.balloon_active);
			TableRow.LayoutParams ivParams2 = new TableRow.LayoutParams(CommonUtilities.DPFromPixel(this, GROUP_LOUNGE_IMAGE_WIDTH),
					CommonUtilities.DPFromPixel(this, GROUP_LOUNGE_IMAGE_HEIGHT), 1.0f);
			ivMessage.setLayoutParams(ivParams2);
			// group_id 저장
			ivMessage.setTag(group_id);
			ivMessage.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) { 
					vibrator.vibrate(VIBRATE_PERIOD);
					//Log.i("current_group_id", String.valueOf(group_id));
					// K-Lounge 에 메시지 출력
					TabActivity tabHost = (TabActivity) KLoungeGroupList.this.getParent();
					//Intent intent = new Intent(KLoungeGroupList.this, KLoungeGroupMember.class);
					Intent intent = getParent().getIntent();
					intent.putExtra("group_id", group_id);
					intent.putExtra("group_name", group_name);
					AppUser.SHARED_GROUPID = group_id;

					tabHost.getTabHost().setCurrentTab(AppUser.KLOUNGE_TAB);
				}
			});
			tr.addView(ivMessage);
			// 그룹 멤버 보기 아이콘
			TextView tvGroupMember = new TextView(this); 
			tvGroupMember.setBackgroundResource(R.drawable.textbox);
			TableRow.LayoutParams tvGMParams = new TableRow.LayoutParams(CommonUtilities.DPFromPixel(this, GROUP_MEMBER_WIDTH), 
					CommonUtilities.DPFromPixel(this, GROUP_MEMBER_HEIGHT), 1.0f);
			tvGMParams.leftMargin = 15;
			tvGMParams.gravity = Gravity.CENTER_VERTICAL;

			tvGroupMember.setLayoutParams(tvGMParams);

			tvGroupMember.setGravity(Gravity.CENTER);
			tvGroupMember.setTextSize(15);
			tvGroupMember.setTextColor(Color.WHITE);
			tvGroupMember.setText(group_total_number+"명");

			tvGroupMember.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					vibrator.vibrate(VIBRATE_PERIOD);
					//					Log.i("group_id", String.valueOf(group_id));
					// 그룹 멤버 출력
					Intent intent = new Intent(KLoungeGroupList.this, ImageGridActivity.class);

					intent.putExtra("group_id", group_id);
					intent.putExtra("group_name", group_name);
					intent.putExtra("group_total_number", group_total_number);
					KLoungeGroupList.this.startActivity(intent);
				}
			});
			tr.addView(tvGroupMember);

			tbl.addView(tr, new TableLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
			tbl.addView(emptyRow);
		}
		createThread(groupList);
	}

	@Override
	public void onBackPressed() {
		this.getParent().onBackPressed();
	}


	@Override
	protected void onDestroy() {
		RecycleUtils.recursiveRecycle(getWindow().getDecorView());
		System.gc();

		super.onDestroy();
	}
}
