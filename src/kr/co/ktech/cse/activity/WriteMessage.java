package kr.co.ktech.cse.activity;

import kr.co.ktech.cse.R;
import kr.co.ktech.cse.CommonUtilities;
import kr.co.ktech.cse.db.KLoungeRequest;
import kr.co.ktech.cse.listener.*;
import kr.co.ktech.cse.model.AppUser;
import kr.co.ktech.cse.util.RecycleUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.View.OnTouchListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
public class WriteMessage extends Activity{
	final static String TAG = "KLOUNGE";
	final static int BTN_AVAIL = R.drawable.btn_send;
	final static int BTN_INAVAIL = R.drawable.btn_disabled;
	final static int RESULT_LOAD_IMAGE = 1;
	final static int COMPLETED_MESSAGE = 2;
	private int LENGTH_TO_SHOW = Toast.LENGTH_SHORT;
	private boolean FILE_CHECK = false;
	private RelativeLayout bottomBarLayout;
	//	private LinearLayout linear_layout;
	private TextView tv_group_name;
	private TextView tv_message_body;
	private TextView tv_filename;
	private Button btn_load_image;
	private Button btn_go_message;
	private ImageView imageView;
	private TextView textView;
	private ImageView attach_img_thumb;
	private String FILE_PATH;
	private int group_id = 0;
	private String group_name = "";
	private int puser_id = 0;
	Context context;
	int height; 
	private ImageView attached_img;
	Rect r = null;
	FrameLayout.LayoutParams thumbParams = null;
	int zeroline = 75;
	int cur_line = 1;
	private boolean mFlag = false;
	final int CLOSE_MESSAGE = 1;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//custom title bar
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.activity_wirte_message);
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
		imageView = (ImageView)findViewById(R.id.favicon);
		textView = (TextView)findViewById(R.id.right_text);
		imageView.setImageResource(R.drawable.icon_klounge);

		context = getApplicationContext();
		Intent intent = getIntent();

		group_id = intent.getIntExtra("to_group_id", 0);
		group_name = intent.getStringExtra("to_group_name");
		puser_id = intent.getIntExtra("to_puser_id", 0);

		makeView("",null);
		r = new Rect();
		//		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.activity_wirte_message, menu);
		return true;
	}
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		//사진 첨부 하고 돌아 왔을 때 다시 화면을 구성하는 메소드
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
			Uri selectedImage = data.getData();
			String[] filePathColumn = { MediaStore.Images.Media.DATA };

			Cursor cursor = getContentResolver().query(selectedImage, filePathColumn, null, null, null);
			cursor.moveToFirst();

			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			String picturePath = cursor.getString(columnIndex);
			FILE_PATH = cursor.getString(cursor.getColumnIndex(MediaStore.MediaColumns.DATA));
			cursor.close();

			// 사진 썸네일을 뷰에 덧붙이기
			addAttachedPicture(picturePath);

			int bottom = 0;
			int top = 0;
			int left = CommonUtilities.PixelFromDP(context, 5);
			int right = CommonUtilities.PixelFromDP(context, 5);
			FrameLayout.LayoutParams btmBarParams = 
					new FrameLayout.LayoutParams(
							ViewGroup.LayoutParams.WRAP_CONTENT, 
							ViewGroup.LayoutParams.WRAP_CONTENT);
			btmBarParams.gravity = Gravity.BOTTOM; 
			bottomBarLayout.setLayoutParams(btmBarParams);
			bottomBarLayout.setPadding(left, top, right, bottom);
			attach_img_thumb.setTag(picturePath);
			FILE_CHECK = true;
		}
	}

	@Override
	protected void onDestroy() {
		RecycleUtils.recursiveRecycle(getWindow().getDecorView());
		System.gc();
		super.onDestroy();
	}
	public void addAttachedPicture(String pic_path){

		int left=0;int top =0;int right=0;int bottom = 0;

		int thumbnail_size = CommonUtilities.DPFromPixel(context, 110);
		thumbParams = new FrameLayout.LayoutParams(thumbnail_size,thumbnail_size);
		int oneLine_height = tv_message_body.getLineHeight();
		int current_line = tv_message_body.getLineCount();

		left = CommonUtilities.DPFromPixel(context, 10);
		top = CommonUtilities.DPFromPixel(context, zeroline) + ((current_line-1)*oneLine_height);
		thumbParams.gravity = Gravity.TOP;
		thumbParams.setMargins(left, top, right, bottom);
		attach_img_thumb = (ImageView)findViewById(R.id.attached_pic_view);
		attach_img_thumb.setVisibility(View.VISIBLE);

		BitmapFactory.Options bfo = new BitmapFactory.Options();
		bfo.inSampleSize = 4;
		attach_img_thumb.setImageBitmap(BitmapFactory.decodeFile(pic_path, bfo));
		attach_img_thumb.setLayoutParams(thumbParams);
	}
	private void makeView(String text, String path){

		int left, top, right, bottom;

		left = CommonUtilities.DPFromPixel(context, 10);
		top = CommonUtilities.DPFromPixel(context, 10);
		right = CommonUtilities.DPFromPixel(context, 10);
		bottom = CommonUtilities.DPFromPixel(context, 0);
		
		// 최 상단 그룹명
		tv_group_name = (TextView)findViewById(R.id.to_group_name_textview);
		tv_group_name.setText(group_name);

		// 글 입력 View
		tv_message_body = (TextView)findViewById(R.id.input_message_form);
		if(text !=null || text.length() > 0){
			tv_message_body.setText(text);
			if(path != null){
				addAttachedPicture(path);
				FILE_CHECK = true;
			}
		}

		tv_message_body.addTextChangedListener(new TextWatcher(){
			@Override
			public void afterTextChanged(Editable arg0) {

			}
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count, int after) {

			}
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if(s.length() == 0){
					btn_go_message.setBackgroundResource(R.drawable.button_disabled);
					btn_go_message.setTextColor(getApplication().getResources().getColor(R.color.textcolor_disable));
				}else{
					btn_go_message.setBackgroundResource(R.drawable.button_enabled);
					btn_go_message.setTextColor(getApplication().getResources().getColor(R.color.textcolor_enable));
					//					Log.i("write_cur_line", cur_line+"");
					//					Log.i("write_past_line", tv_message_body.getLineCount()+"");
				}
			}
		});

		tv_message_body.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// If the event is a key-down event on the "enter" button
				int bound = tv_message_body.getLineBounds(tv_message_body.getLineCount()-1, r);
				int oneLine_height = tv_message_body.getLineHeight();

				if(attach_img_thumb != null){
					if(attach_img_thumb.getVisibility() == View.VISIBLE){
						if ((event.getAction() == KeyEvent.ACTION_DOWN) &&
								(keyCode == KeyEvent.KEYCODE_ENTER)) {
							thumbParams.setMargins(
									CommonUtilities.PixelFromDP(context, 15),
									CommonUtilities.PixelFromDP(context, zeroline)+oneLine_height+bound,
									0, 0);
							attach_img_thumb.setLayoutParams(thumbParams);
							tv_message_body.append(System.getProperty("line.separator"));
							return true;
						}
						else if((event.getAction() == KeyEvent.ACTION_DOWN) &&
								(keyCode == KeyEvent.KEYCODE_DEL)){
							//							Log.i("cur_line", cur_line+", past_line "+tv_message_body.getLineCount());
							if(cur_line > tv_message_body.getLineCount()){
								thumbParams.setMargins(
										CommonUtilities.PixelFromDP(context, 15),
										CommonUtilities.PixelFromDP(context, zeroline)+bound,
										0, 0);
							}
							cur_line = tv_message_body.getLineCount();
							attach_img_thumb.setLayoutParams(thumbParams);
						}
					}
				}
				return false;
			}
		});
		tv_message_body.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
				imm.showSoftInput(tv_message_body, 0);
			}
		});


		//하단 바 (사진첨부버튼, 전송버튼)
		bottomBarLayout = (RelativeLayout)findViewById(R.id.bottomBarLayout);

		btn_load_image = (Button)findViewById(R.id.add_image_button);
		btn_load_image.invalidate();
		btn_load_image.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
				startActivityForResult(intent, RESULT_LOAD_IMAGE);
			}
		});

		// 전송 버튼
		RelativeLayout.LayoutParams btnParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		btnParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		btnParams.addRule(RelativeLayout.CENTER_VERTICAL);
		btn_go_message = (Button)findViewById(R.id.go_message_button);
		btn_go_message.setLayoutParams(btnParams);
		btn_go_message.setTextSize(15);
		btn_go_message.setBackgroundResource(R.drawable.button_disabled);
		btn_go_message.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				//				Toast.makeText(context, "puser_id: "+puser_id + " group_id: "+group_id+" user_id: "+AppUser.user_id, Toast.LENGTH_LONG).show();
				final String message_body = tv_message_body.getText().toString();

				if(message_body.trim().length()>0){
					//Enter 나 빈칸만 입력 했을 경우는 글 입력이 안됨.
					Thread mThread = new Thread(new Runnable() {
						public void run() {
							KLoungeRequest kreq = new KLoungeRequest();
							if(FILE_CHECK) {
								if(puser_id > 0) {
									try {
										kreq.sendMessageWithImage(group_id, AppUser.user_id, message_body, FILE_PATH, puser_id, context);
									} catch (OutOfMemoryError e) {
										Log.i(TAG+"_OutOfMemoryError",e.toString());
									} catch (Exception e) {
										Log.i(TAG+"_Exception",e.toString());
									}
								} else {
									try {
										kreq.sendMessageWithImage(group_id, AppUser.user_id, message_body, FILE_PATH, 0, context);
									} catch (OutOfMemoryError e) {
										Log.i(TAG+"_OutOfMemoryError",e.toString());
									} catch (Exception e) {
										Log.i(TAG+"_Exception",e.toString());
									}
								}
							} else {
								if(puser_id > 0) {
									kreq.sendMessage(group_id, AppUser.user_id, message_body, 0, "", puser_id);
								} else {
									kreq.sendMessage(group_id, AppUser.user_id, message_body, 0, "", 0);
								}
							}
						}
					});
					mThread.start();
					Toast.makeText(WriteMessage.this, "메시지가 게시되었습니다.", LENGTH_TO_SHOW).show();
					finish();
				} else {
					Toast.makeText(WriteMessage.this, "게시할 메시지를 입력 해 주세요.", LENGTH_TO_SHOW).show();
				}
			}
		});
		
		attached_img = (ImageView)findViewById(R.id.attached_pic_view);
	}
	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			if(msg.what == CLOSE_MESSAGE) {
				mFlag = false;
			}
		}
	};
	@Override
	public void onBackPressed() {
		String title = "글 작성 에서 나가기";
		String message = "이 페이지를 벗어나면 작성중인 내용은 저장되지 않습니다.";

		new AlertDialog.Builder(this)
		.setIcon(android.R.drawable.ic_dialog_alert)
		.setTitle(title)
		.setMessage(message)
		.setPositiveButton("확인", new DialogInterface.OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which) {
				finish();    
			}

		})
		.setNegativeButton("취소", null)
		.show();
	}
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}
}
