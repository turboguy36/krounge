package kr.co.ktech.cse.activity;

import static kr.co.ktech.cse.CommonUtilities.DOWNLOAD_PATH;

import java.util.List;
import kr.co.ktech.cse.R;
import kr.co.ktech.cse.adapter.ImageAdapter;
import kr.co.ktech.cse.db.KLoungeGroupRequest;
import kr.co.ktech.cse.model.AppUser;
import kr.co.ktech.cse.model.GroupMemberInfo;
import kr.co.ktech.cse.util.RecycleUtils;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.GridLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class KLoungeGroupMember extends Activity {
	private String TAG = "KLoungeGroupMember";
	private ImageView imageView;
	private TextView textView;
	private ProgressDialog pd;
	List<GroupMemberInfo> gmList = null;
	int group_id = 0;
	GridLayout gl;
	GridView gv;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		LayoutInflater GroupMemberLayout = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View GroupMemberView = (View)GroupMemberLayout.inflate(R.layout.activity_klounge_group_member, null);

		//custom title bar 
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(GroupMemberView);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
		imageView = (ImageView)findViewById(R.id.favicon);
		textView = (TextView)findViewById(R.id.right_text);

		TextView tvGroupName = (TextView)GroupMemberView.findViewById(R.id.klounge_group_name);
		TextView tvGroupTotalNumber = (TextView)GroupMemberView.findViewById(R.id.klounge_group_total_number);
		gv = (GridView)GroupMemberView.findViewById(R.id.klounge_group_member_list);

		Intent intent = getIntent();
		group_id = intent.getIntExtra("group_id", 0);
		String group_name = intent.getStringExtra("group_name");
		int group_total_number = intent.getIntExtra("group_total_number", 0);
		tvGroupName.setText(group_name);
		tvGroupTotalNumber.setText(group_total_number+"명");
		//		Log.i(TAG, "ID: "+String.valueOf(AppUser.user_id) +"GROUP ID: " + group_id);

		createThreadAndDialog();
		
	}
	
	public void setGroupMemberList(List<GroupMemberInfo> gmList) {

		gv.setAdapter(new ImageAdapter(this, gmList));

		gv.setOnItemClickListener(new OnItemClickListener(){
			public void onItemClick(AdapterView<?> parent, View v, int position, long id){
				String user_id = String.valueOf(((TextView)v.findViewById(R.id.grid_item_userId)).getText());
				String user_name = String.valueOf(((TextView)v.findViewById(R.id.grid_item_label)).getText());
				String imageUrl = String.valueOf(((TextView)v.findViewById(R.id.grid_item_uri)).getText());
				String user_photo = imageUrl;//KLOUNGE_STORAGE_LOCATION + "/" + user_id + "USER_" + imageUrl.substring(imageUrl.lastIndexOf("/") + 1);
				//v.findViewById(id);
				Intent intent = new Intent(KLoungeGroupMember.this, PersonalLounge.class);
				intent.putExtra("puser_id", user_id);
				intent.putExtra("puser_name", user_name);
				intent.putExtra("puser_photo", user_photo);

				KLoungeGroupMember.this.startActivity(intent);
			}
		});
	}

	void createThreadAndDialog() {
		pd = ProgressDialog.show(this, "", getResources().getText(R.string.msg_contents), true);
		Thread thread = new Thread(new Runnable() {
			public void run() {

				KLoungeGroupRequest krgr = new KLoungeGroupRequest();
				gmList= krgr.getGroupMemberList(AppUser.user_id, group_id);
				
				handler.sendEmptyMessage(0);
			}
		});
		thread.start();
	}

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			// View
			if(gmList.size()==0) {
				if(pd!=null)pd.dismiss();
				Toast.makeText(KLoungeGroupMember.this, getResources().getText(R.string.sorry_empty_list), Toast.LENGTH_LONG).show();
			}else if(gmList.size() < 0){
				if(pd!=null)pd.dismiss();
				Toast.makeText(KLoungeGroupMember.this, getResources().getText(R.string.sorry_text), Toast.LENGTH_SHORT).show();
				Log.i(TAG,"Loading Message List Error "+this.getClass().toString());
				finish();
			}else{
				setGroupMemberList(gmList);
			}
			if(pd!=null)pd.dismiss();
		}
	};

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_klounge_group_member, menu);
		return true;
	}

	@Override
	protected void onDestroy() {
		RecycleUtils.recursiveRecycle(getWindow().getDecorView());
		System.gc();
		super.onDestroy();
	}
}
