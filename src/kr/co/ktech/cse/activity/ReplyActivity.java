package kr.co.ktech.cse.activity;

import java.util.List;

import kr.co.ktech.cse.R;
import kr.co.ktech.cse.CommonUtilities;
import kr.co.ktech.cse.bitmapfun.util.ImageFetcher;
import kr.co.ktech.cse.bitmapfun.util.ImageCache.ImageCacheParams;
import kr.co.ktech.cse.db.KLoungeRequest;
import kr.co.ktech.cse.listener.*;
import kr.co.ktech.cse.model.AppUser;
import kr.co.ktech.cse.model.SnsAppInfo;
import kr.co.ktech.cse.model.SnsInfo;
import kr.co.ktech.cse.processes.MessageLayoutSetting;
import kr.co.ktech.cse.util.KLoungeFormatUtil;
import kr.co.ktech.cse.util.RecycleUtils;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.*;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import android.widget.ImageView.ScaleType;

public class ReplyActivity extends Activity {

	final String MESSAGE_REPLY_TYPE = "reply";
	final char TO_REPLY_MARK = '@';
	final String SEPARATOR= " ";
	final int REPLY_POST_ID_TAG = 1;
	String contentBG = MessageLayoutSetting.contentBG;
	private int LENGTH_TO_SHOW = Toast.LENGTH_SHORT;
	private EditText reply_text;
	private Button send_reply_btn;
	private ImageView imageView;
	private TextView textView;
	SnsAppInfo snsinfo = null;
	private String reply_body = null;
	private ImageFetcher mImageFetcher;
	//	private ReplyMessageThread reply_thread;
	AsyncTask<Void, Void, Void> mTask;
	AsyncTask<Integer, Void, Void> mWriteTask;
	LinearLayout baseLayout;
	KLoungeRequest kloungehttp;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_CUSTOM_TITLE);
		setContentView(R.layout.view_reply);
		mImageFetcher = AppUser.mImageFetcher;
		//custom title bar
		getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE, R.layout.custom_title);
		imageView = (ImageView)findViewById(R.id.favicon);
		textView = (TextView)findViewById(R.id.right_text);
		imageView.setImageResource(R.drawable.icon_klounge);

		makeMainView();
		makeReplyView();
		getIntent().removeExtra("snsAppInfo");
	}
	void makeReplyView(){
		// 답글 출력하기
		kloungehttp = new KLoungeRequest();
		baseLayout = (LinearLayout)findViewById(R.id.reply_message);

		mTask = new AsyncTask<Void, Void, Void>() {
			List<SnsAppInfo> replyList;
			@Override
			protected Void doInBackground(Void... params) {
				try {
					if(snsinfo.getSuperId() > 0) replyList = kloungehttp.getReplyMessageList(snsinfo.getSuperId());
					else replyList = kloungehttp.getReplyMessageList(snsinfo.getPostId());
				} catch (Exception e) {
					e.printStackTrace();
				}
				return null;
			}

			@Override
			protected void onPostExecute(Void result) {
				baseLayout.removeAllViews();

				MessageLayoutSetting mls = new MessageLayoutSetting(ReplyActivity.this, baseLayout);
				if(replyList.size() > 0) {
					for(int i=0; i<replyList.size(); i++) {
						SnsAppInfo sInfo = replyList.get(i);	
						mls.setReplyLayout(ReplyActivity.this, sInfo, mImageFetcher);
					}
				}
				mTask = null;
			}
		};
		mTask.execute(null, null, null);

		reply_text = (EditText)findViewById(R.id.reply_text);

		send_reply_btn = (Button)findViewById(R.id.send_reply);
		send_reply_btn.setBackgroundResource(R.drawable.btn_disabled);
		send_reply_btn.setEnabled(false);
		send_reply_btn.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				//KLoungeRequest kreq = new KLoungeRequest();
				snsinfo.setPuser_id(snsinfo.getUserId());

				reply_body = reply_text.getText().toString();
				if(reply_text != null || reply_text.length() != 0){
					int reply_post_id = 0;
					try{
						if(reply_body.charAt(0) == TO_REPLY_MARK) {
							SnsInfo si = (SnsInfo)reply_text.getTag();
							if(si != null) reply_post_id = si.getPostId();
							reply_body = reply_body.substring(reply_body.indexOf(SEPARATOR)+1);
						}
					}catch(StringIndexOutOfBoundsException s){
						s.printStackTrace();
					}
					//				Log.i("reply body", reply_body);
					//				Log.i("reply post_id", String.valueOf(reply_post_id));

					mWriteTask = new AsyncTask<Integer, Void, Void>() {
						List<SnsAppInfo> replyList;
						@Override
						protected Void doInBackground(Integer... params) {
							try {
								final int reply_post_id = params[0].intValue();
								Log.i("ReplyActivity reply post_id", String.valueOf(reply_post_id));
								if(reply_post_id > 0 ) {
									kloungehttp.sendMessage(snsinfo.getGroupId(), AppUser.user_id, reply_body, reply_post_id, MESSAGE_REPLY_TYPE, snsinfo.getPuser_id());
								}
								else kloungehttp.sendMessage(snsinfo.getGroupId(), AppUser.user_id, reply_body, snsinfo.getPostId(), "", snsinfo.getPuser_id());

								Thread.sleep(50);
								replyList = kloungehttp.getReplyMessageList(snsinfo.getPostId());

							} catch (InterruptedException e) {
								System.err.println("Error: Thread Interrupt Exception");
								e.printStackTrace();
							} catch (Exception e) {
								e.printStackTrace();
							}
							return null;
						}

						@Override
						protected void onPostExecute(Void result) {
							baseLayout.removeAllViews();

							MessageLayoutSetting mls = new MessageLayoutSetting(ReplyActivity.this, baseLayout);
							if(replyList.size() > 0) {
								for(int i=0; i<replyList.size(); i++) {
									SnsAppInfo sInfo = replyList.get(i);
									mls.setReplyLayout(ReplyActivity.this, sInfo, mImageFetcher);
								}
							}
							if(reply_text.isFocusable()) {
								reply_text.setText("");
								reply_text.clearFocus();
								InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
								imm.hideSoftInputFromWindow(reply_text.getWindowToken(),0);
							}
							mWriteTask = null;
						}

					};
					mWriteTask.execute(reply_post_id, null, null);

					//Log.i("reply_text focus", String.valueOf(reply_text.isFocusable()));

					Toast.makeText(ReplyActivity.this, "댓글이 등록되었습니다.", LENGTH_TO_SHOW).show();
					//klounge.setReplyList(ReplyActivity.this, tbl, snsinfo.getPostId());
				}else{
					Toast.makeText(ReplyActivity.this, "글을 입력 해 주세요.", LENGTH_TO_SHOW).show();
				}
			}
		});
		reply_text.addTextChangedListener(new TextWatcher() {
			
			@Override
			public void onTextChanged(CharSequence s, int start, int before, int count) {
				if(s.length() == 0){
					send_reply_btn.setBackgroundResource(R.drawable.btn_disabled);
					send_reply_btn.setEnabled(false);
				}else if(s.length() > 0){
					send_reply_btn.setBackgroundResource(R.drawable.btn_send);
					send_reply_btn.setEnabled(true);
				}
			}
			
			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub
				
			}
		});
		reply_text.setOnEditorActionListener(new TextView.OnEditorActionListener() {

			public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
				v.clearFocus();
				return false;
			}
		});

	}
	void makeMainView(){
//		imagedownloader = new ImageDownloader(this);

		TextView tvMainMessageUserName = (TextView)findViewById(R.id.main_message_user_name);
		TextView tvMainMessageWriteDate = (TextView)findViewById(R.id.main_message_date);
		ImageView ivMainMessageUserPhoto = (ImageView)findViewById(R.id.main_message_photo);
		TextView tvMainMessageBody = (TextView)findViewById(R.id.main_message_body);
		ImageView ivMainMessageAttachedPhoto = (ImageView)findViewById(R.id.reply_imageview);

		// 메인 메시지 정보 받아오기
		Bundle bundle = getIntent().getExtras();
		snsinfo = bundle.getParcelable("snsAppInfo");
		String attached_img_url = snsinfo.getPhotoVideo();
		tvMainMessageUserName.setText(snsinfo.getUserName());
		tvMainMessageWriteDate.setText(snsinfo.getWrite_date());
		if(attached_img_url != null && !attached_img_url.equals("")) {
			int left = 0;
			int top = CommonUtilities.DPFromPixel(this, 5);
			int right = 0;
			int bottom = CommonUtilities.DPFromPixel(this, 10);
			ivMainMessageAttachedPhoto.setPadding(left, top, right, bottom);
			ivMainMessageAttachedPhoto.setBackgroundColor(Color.parseColor(contentBG));
			
//			imagedownloader.download(img_url, ivMainMessageAttachedPhoto);
			if(attached_img_url.contains(".flv")){
				ivMainMessageAttachedPhoto.setBackgroundResource(R.drawable.no_flv);
			}else{
				mImageFetcher.loadImage(attached_img_url, ivMainMessageAttachedPhoto);
			}
			ivMainMessageAttachedPhoto.setScaleType(ScaleType.FIT_START);
			LinearLayout.LayoutParams ivParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			ivParams.gravity = Gravity.CENTER;
			left = 0;
			top = 0;
			right = 0;
			bottom = CommonUtilities.DPFromPixel(this, 5);
			ivParams.setMargins(left, top, right, bottom);
			ivMainMessageAttachedPhoto.setLayoutParams(ivParams);
			
			kr.co.ktech.cse.listener.AppClickListener acListener = new kr.co.ktech.cse.listener.AppClickListener(snsinfo, this);//new AppClickListener(added_imageurl, replyCount, htmlmessage);
			ivMainMessageAttachedPhoto.setOnClickListener(acListener);
		}

		String user_imgUrl = snsinfo.getPhoto().toString().replace(" ", "%20");
		
		mImageFetcher.loadImage(user_imgUrl,ivMainMessageUserPhoto);
		ivMainMessageUserPhoto.setScaleType(ScaleType.FIT_XY);
		
		String htmlmessage = KLoungeFormatUtil.bodyURLFormat(snsinfo.getBody()).toString();
		
		tvMainMessageBody.setText(Html.fromHtml(htmlmessage));
		tvMainMessageBody.invalidate();
	}
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		
		super.onSaveInstanceState(outState);
	}
	@Override
	public void onResume() {
		super.onResume();
	}

	@Override
	public void onPause() {
		super.onPause();
	}
	@Override
	protected void onDestroy() {
		super.onRestart();
		// Adapter가 있으면 어댑터에서 생성한 recycle메소드를 실행
		RecycleUtils.recursiveRecycle(getWindow().getDecorView());
//		Log.i("ReplyActivity destroy", "remove snsAppInfo intent");
		getIntent().removeExtra("snsAppInfo");
		System.gc();
	}
}
