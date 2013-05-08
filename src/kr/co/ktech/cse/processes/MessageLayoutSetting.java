package kr.co.ktech.cse.processes;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender.SendIntentException;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.sax.StartElementListener;
import android.text.ClipboardManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView.ScaleType;
import android.widget.*;
import android.widget.LinearLayout.LayoutParams;
import kr.co.ktech.cse.R;
import kr.co.ktech.cse.CommonUtilities;
import kr.co.ktech.cse.activity.AttachedDownloadManager;
import kr.co.ktech.cse.activity.DialogActivity;
import kr.co.ktech.cse.activity.PersonalLounge;
import kr.co.ktech.cse.activity.ReplyActivity;
import kr.co.ktech.cse.AppConfig;
import kr.co.ktech.cse.bitmapfun.util.ImageFetcher;
import kr.co.ktech.cse.bitmapfun.util.Utils;
import kr.co.ktech.cse.db.KLoungeHttpRequest;
import kr.co.ktech.cse.db.KLoungeRequest;
import kr.co.ktech.cse.listener.*;
import kr.co.ktech.cse.model.AppUser;
import kr.co.ktech.cse.model.SnsAppInfo;
import kr.co.ktech.cse.model.SnsInfo;
import kr.co.ktech.cse.util.FileDownloader;
import kr.co.ktech.cse.util.ImageDownloader;
import kr.co.ktech.cse.util.KLoungeFormatUtil;
import kr.co.ktech.cse.util.LoadImageUtil;
public class MessageLayoutSetting{

	private final int MAIN_MESSAGE = 1;
	private final int REPLY_MESSAGE = 0;
	private final int USER_PHOTO_ID = 1001;
	private final int TEXTVIEW_NAME_ID = 1002;
	private final int TEXTVIEW_DATE_ID = 1003;
	private final int TEXTVIEW_FILE_ID = 1004;
	private final int FILE_IMG_ID = 1005;
	private final int FILE_TEXT_ID = 10051;
	private final int TEXTVIEW_REPLY_ID = 1006;
	private final int TEXTVIEW_DELETE_ID = 1007;
	private final int IMAGEVIEW_DELETE_ID = 10071;
	private final int TEXTVIEW_MESSAGE_ID = 1008;
	private final int ADDED_IMAGE_ID = 1009;
	private final int ADDED_VIDEO_ID = 10010;
	private final int REDIRECT_ATTACH_ID = 10011;
	private final int REDIRECT_ATTACH_IMGID = 10012;
	
	private final int REPLY_RL_HEAD = 2000;
	private final int REPLY_USER_IMG = 2001;
	private final int REPLY_USER_NAME = 2002;
	private final int REPLY_TOUSER_NAME = 2003;
	private final int REPLY_BODY = 2004;
	private final int REPLY_WRITE_DATE = 2005;
	private final int REPLY_GO_WRITE_REPLY = 2006;
	private final int REPLY_DELETE_BTN = 2007;
	
	private final int REPLY_USER_IMG_ID = 0;
	private final int REPLY_USER_NAME_ID = 1;
	private final int REPLY_TOUSER_NAME_ID = 2;
	private final int REPLY_BODY_ID = 3;
	private final int REPLY_WRITE_DATE_ID = 4;
	private final int REPLY_GO_WRITE_REPLY_ID = 0;
	private final int REPLY_DELETE_BTN_ID = 1;
	
	private static final int MESSAGE_BODY_TEXT_SIZE = 13;
	private static final int MESSAGE_NAME_TEXT_SIZE = 14;
	private static final int MESSAGE_DATE_TEXT_SIZE = 12;
	private static final int MESSAGE_ADDED_FILE_TEXT_SIZE = 12;
	private static final int MESSAGE_REPLY_TEXT_SIZE = 12;
	private static final int MESSAGE_DELETE_TEXT_SIZE = 13;
	private static final int MESSAGE_IMAGE_MAX_WIDTH = 70;
	private static final int MESSAGE_IMAGE_MAX_HEIGHT = 70;
	private static final int MESSAGE_IMAGE_MIN_WIDTH = 80;
	private static final int MESSAGE_IMAGE_MIN_HEIGHT = 80;
	private static final int MESSAGE_ADDED_IMAGE_HEIGHT = 20;
	private static final int MESSAGE_ADDED_IMAGE_WIDTH = 30;
	private static final int MESSAGE_DEL_IMAGE_MAX_WIDTH = 25;
	private static final int MESSAGE_DEL_IMAGE_MAX_HEIGHT = 25;

	public static final String contentBG = "#FFFFFF";
	public static final String tabsBG = "#CFDDE8";
	public static final String bottomContentBG = "#55dadada";
	public static final String thickString = "#FF000000";
	public static final String slimString = "#AA111111";
	public static final String linkString = "#FF0669B2";
	public static final String lightString = "#99757575";
	public static final String brightString = "#FFFFFF";
	public static final String bodyString = "#777777";
	public static final String link_color = "#14148C";
	public static final String visited_link_color = "#8C008C";
	public static final String inactive_group = "#767676";
	public static final String color_active_group = "#0064FF";
	public final int REPLY_TAIL_HEIGHT = 50;
	private SnsAppInfo sns_info;
	private Context context;
	private LinearLayout baseLinearLayout;
	private LinearLayout inputReplyLinearLayout;
	private TableLayout replyTableLayout;
//	private RelativeLayout baseRelativeLayout;
//	private LoadImageUtil imageUtil;
	private EditText etReplyText;
	final String TO_REPLY_MARK = "@";
	final String SEPARATOR= " ";
	final int REPLY_POST_ID_TAG = 1;

	private Handler handler;

	private DisplayUtil du;
	AsyncTask<Void, Void, Void> mTask;
	private AsyncTask<Void, Void, Void> mDeleteTask;

	private int totalMessage;
	TextView file = null;
	ImageView file_img = null;
	String url_filename;
	String htmlmessage;
	int download_Pid = 0;
	Vibrator vibrator;
	private static final Long VIBRATE_PERIOD = CommonUtilities.VIBRATE_TIME;
	private String TAG = "MessageLayoutSetting";
	public MessageLayoutSetting(final Context context, LinearLayout linear) {
//		Log.d(TAG, "생성자");
		vibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
		// 그룹SNS or 내라운지 
		this.context = context;
		this.baseLinearLayout = linear;
		du = new DisplayUtil(context);
		totalMessage = 0;
	}
	/*
	public MessageLayoutSetting(final Context context, LinearLayout linear) {
		vibrator = (Vibrator)context.getSystemService(Context.VIBRATOR_SERVICE);
		// 댓글
		this.context = context;
		this.baseRelativeLayout = r;
		du = new DisplayUtil(context);
		totalMessage = 0;
	}
	*/
	public int getTotalMessage() {
		return totalMessage;
	}
	public void setTotalMessage(int totalMessage) {
		this.totalMessage = totalMessage;
	}
	
	
	/*------------------------------------R--E--P--L--Y _ V--I--E--W _  A--C--T--I--V--I--T--Y---------------------------------------------------------*/
	/*
	 * baseRelativeLayout
	 * 	|_ rl
	 * 	   |_ rl_tail
	 */
	// 댓글을 클릭했을 때의 댓글의 레이아웃(본문 제외)
	public void setReplyLayout(final Context context, SnsAppInfo snsInfo, ImageFetcher mImageFetcher) {
		this.sns_info = snsInfo;
		boolean state = false;
		int left = 0;
		int top = 0;
		int right = 0;
		int bottom = 0;
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		RelativeLayout.LayoutParams Relative_params = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		RelativeLayout rl = new RelativeLayout(context); //하나의 글
		
		RelativeLayout rl_head = new RelativeLayout(context); // 상단 댓글( repler_photo, repler_name, reply_toWhom_name, etc...)
		rl_head.setId(REPLY_RL_HEAD);
		RelativeLayout rl_tail = new RelativeLayout(context); // 아래의 bar (댓글달기, 삭제)
		
		ImageView repler_photo = new ImageView(context);
			repler_photo.setId(REPLY_USER_IMG);
		TextView repler_name = new TextView(context);
			repler_name.setId(REPLY_USER_NAME);
		TextView reply_toWhom_name = new TextView(context);
			reply_toWhom_name.setId(REPLY_TOUSER_NAME);
		TextView reply_body = new TextView(context);
			reply_body.setId(REPLY_BODY);
		TextView reply_date = new TextView(context);
			reply_date.setId(REPLY_WRITE_DATE);
		Button go_write_reply = new Button(context);
			go_write_reply.setId(REPLY_GO_WRITE_REPLY);
		ImageView btn_reply = new ImageView(context);
			btn_reply.setId(TEXTVIEW_DELETE_ID);
		
		state = make_repler_photo(repler_photo, mImageFetcher, snsInfo, rl_head);
		state = make_reply_body(repler_name, reply_toWhom_name, reply_body, reply_date, snsInfo, rl_head);
		state = make_reply_tail(go_write_reply, btn_reply, snsInfo, rl_tail);
		
		rl.addView(rl_head);
		rl.addView(rl_tail);
		
		Relative_params.addRule(RelativeLayout.BELOW, rl_head.getId());
		rl_tail.setLayoutParams(Relative_params);
		baseLinearLayout.setLayoutParams(params);
		baseLinearLayout.addView(rl);

	}
	
	boolean make_reply_tail(Button go_write_reply, ImageView btn_reply, final SnsAppInfo snsInfo, RelativeLayout parent){
		boolean result = false;
		boolean state = false;
		int left = 0;
		int top = 0;
		int right = 0;
		int bottom = 0;
		RelativeLayout.LayoutParams params = null;
		
		// 댓글 달기 버튼
		String reply_text = "댓글";
		go_write_reply.setText(reply_text);
		params = new RelativeLayout.LayoutParams(
				ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.MATCH_PARENT);
		
		params.setMargins(left, top, right, bottom);
		params.addRule(RelativeLayout.BELOW, REPLY_USER_IMG);// parameter 썻다면 removeRull 로 지우자.
		params.addRule(RelativeLayout.CENTER_VERTICAL);
		go_write_reply.setLayoutParams(params);
		
		go_write_reply.setTextSize(TypedValue.COMPLEX_UNIT_SP, MESSAGE_REPLY_TEXT_SIZE);
		go_write_reply.setTextColor(Color.parseColor(thickString));
		
		go_write_reply.setBackgroundResource(R.drawable.button_lines);
		left = du.PixelToDP(30);
		top = du.PixelToDP(15);
		right = du.PixelToDP(30);
		bottom = du.PixelToDP(15);
		go_write_reply.setPadding(left, top, right, bottom);
		left = 0;
		right = 0;
		top = 0;
		bottom = 0;
		if(Utils.hasJellyBean()){
//			params.removeRule(RelativeLayout.BELOW);
//			params.removeRule(RelativeLayout.CENTER_VERTICAL);
		}else{
			params = null;
//			int delBtn_size = du.PixelToDP(MESSAGE_DEL_IMAGE_MAX_WIDTH);
			params = new RelativeLayout.LayoutParams(
					ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.MATCH_PARENT);
			params.setMargins(left, top, right, bottom);
			params.addRule(RelativeLayout.BELOW, REPLY_USER_IMG);
		}
		go_write_reply.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				try {
					LinearLayout inputbox = (LinearLayout)v.getRootView().findViewById(R.id.inputReplyBox);
					if(inputbox.getVisibility() > 0){
						inputbox.setVisibility(0);
					}

					etReplyText = (EditText)v.getRootView().findViewById(R.id.reply_text);
					if(etReplyText != null) {
						etReplyText.setText(TO_REPLY_MARK+snsInfo.getUserName()+SEPARATOR);
						etReplyText.setTag(snsInfo);
						etReplyText.setSelection(etReplyText.length());

						etReplyText.requestFocus();
						if(etReplyText.isFocusable()) {
							InputMethodManager mInputMethodManager = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
							mInputMethodManager.showSoftInput(etReplyText, InputMethodManager.SHOW_IMPLICIT);
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}								
			}
		});
		parent.addView(go_write_reply, REPLY_GO_WRITE_REPLY_ID);
		
		// 삭제
		if(snsInfo.getUserId() == AppUser.user_id) {
			
			left = 0;
			top = 0;
			right = 0;
			bottom = 0;
			params.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			params.addRule(RelativeLayout.CENTER_VERTICAL);
			
			btn_reply.setLayoutParams(params);
			btn_reply.setBackgroundResource(R.drawable.button_line_with_img);
			
			state = replyDelButton(btn_reply, snsInfo.getUserId(), snsInfo.getPostId(),  REPLY_MESSAGE);
			// making delete button event
			
			parent.addView(btn_reply, REPLY_DELETE_BTN_ID);
		}
		
		parent.setBackgroundColor(Color.parseColor(bottomContentBG));
		return result;
		
	}

	private boolean replyDelButton(ImageView ivDelete, final int user_id, final int post_id, final int r) {
		boolean result = false;
		ivDelete.setTag(sns_info.getPostId());
		
		ivDelete.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Log.i("message delete", user_id+" / "+ post_id);

				mTask = new AsyncTask<Void, Void, Void>() {
					@Override
					protected Void doInBackground(Void... params) {
						try {
							// DB 삭제 처리 추가
							KLoungeRequest kreq = new KLoungeRequest();
							kreq.deleteMessage(user_id, post_id, r);

							Thread.sleep(50);
						} catch (Exception e) {
							e.printStackTrace();
						}
						return null;
					}

					@Override
					protected void onPostExecute(Void result) {
						if(r == MAIN_MESSAGE) {

						} else if(r == REPLY_MESSAGE){
							for(int i=0; i<baseLinearLayout.getChildCount(); i++) {
								
								View tv = (View)baseLinearLayout.getChildAt(i).findViewById(TEXTVIEW_DELETE_ID);
								Integer id = 0;
								try{
									id = (Integer)tv.getTag();
								}catch(NullPointerException n){
									Log.e(TAG, "NULL POINTER EXCEPTION "+i+"st."+n);
								}
								int tag_post_id = id.intValue();
								Log.i("tag_post_id", String.valueOf(tag_post_id));
								if(tag_post_id==post_id) {
									Log.i("delete index", String.valueOf(i));
									try{
										baseLinearLayout.removeViewAt(i);
									}catch(ClassCastException c){
										c.printStackTrace();
									}
									
									break;
								}
							}		
						}
						mTask = null;
					}

				};
				AlertDialog.Builder alertDlg = new AlertDialog.Builder(context);
				alertDlg.setTitle("확인");
				alertDlg.setMessage("삭제 하시겠습니까?");
				alertDlg.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						mTask.execute(null, null, null);
					}
				});
				alertDlg.setNegativeButton("취소", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
					}
				});
				alertDlg.show();
			}
		});
		return result;
	}
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private boolean make_reply_body(TextView repler_name, TextView reply_toUser_name, 
			TextView reply_body, TextView reply_date, SnsAppInfo snsInfo, RelativeLayout parent) throws NullPointerException{
		int left = du.PixelToDP(0);
		int top = 0;
		int right = 0;
		int bottom = 0;
		RelativeLayout.LayoutParams params = null;
		
		boolean result = false;
		String name = snsInfo.getUserName();
		String to_user_name = snsInfo.getReply_to_user_name();
		String body =KLoungeFormatUtil.bodyURLFormat(snsInfo.getBody()).toString();
		String date = snsInfo.getWrite_date();
		
		// name
		repler_name.setText(name);
		params = new RelativeLayout.LayoutParams(
				ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		params.addRule(RelativeLayout.RIGHT_OF, REPLY_USER_IMG);
		params.setMargins(left, top, right, bottom);
		repler_name.setLayoutParams(params);
		repler_name.setTextColor(Color.parseColor(thickString));
		if(Utils.hasJellyBean()){
//			params.removeRule(RelativeLayout.ALIGN_PARENT_TOP);
		}else{
			params = null;
			params = new RelativeLayout.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			params.setMargins(left, top, right, bottom);
			params.addRule(RelativeLayout.RIGHT_OF, REPLY_USER_IMG);
		}
		
		// to user name
//		Log.d(TAG, isToUserName(to_user_name)+"/"+to_user_name);
		if(isToUserName(to_user_name)){
			reply_toUser_name.setText("To. "+to_user_name);
			params.addRule(RelativeLayout.BELOW, repler_name.getId());
			reply_toUser_name.setLayoutParams(params);
			reply_toUser_name.setTextColor(Color.parseColor(bodyString));
			reply_toUser_name.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
		}else{		
		}
		if(Utils.hasJellyBean()){
//			params.removeRule(RelativeLayout.BELOW);
		}else{
			params = null;
			params = new RelativeLayout.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			params.setMargins(left, top, right, bottom);
			params.addRule(RelativeLayout.RIGHT_OF, REPLY_USER_IMG);
		}
		
		//body
		if(isToUserName(to_user_name)){
//			Log.d(TAG, "TRUE");
			params.addRule(RelativeLayout.BELOW, reply_toUser_name.getId());
		}else{
//			Log.d(TAG, "FALSE");
			params.addRule(RelativeLayout.BELOW, repler_name.getId());
		}
		reply_body.setLayoutParams(params);
		if(Utils.hasHoneycomb()){
			reply_body.setTextIsSelectable(true);
		}
		reply_body.setText(Html.fromHtml(body));
		reply_body.setMovementMethod(LinkMovementMethod.getInstance());
		reply_body.setTextColor(Color.parseColor(slimString));
		reply_body.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
		if(Utils.hasJellyBean()){
//			params.removeRule(RelativeLayout.BELOW);
		}else{
			params = null;
			params = new RelativeLayout.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			params.setMargins(left, top, right, bottom);
			params.addRule(RelativeLayout.RIGHT_OF, REPLY_USER_IMG);
		}
		
		// date
		reply_date.setText(date);
		params.addRule(RelativeLayout.BELOW, reply_body.getId());
		reply_date.setLayoutParams(params);
		
		parent.addView(repler_name, REPLY_USER_NAME_ID);
		parent.addView(reply_toUser_name, REPLY_TOUSER_NAME_ID);
		parent.addView(reply_body, REPLY_BODY_ID);
		parent.addView(reply_date, REPLY_WRITE_DATE_ID);
		
		return result;
	}
	private boolean isToUserName(String toUser){
		boolean result = false;
		if(toUser.length() != 0){
//			Log.d(TAG,toUser.length()+"");
			result = true;
		}
		return result;
	}
	private boolean make_repler_photo(ImageView repler_photo, 
			ImageFetcher imageFetcher, SnsAppInfo snsInfo, RelativeLayout parent){
		
		boolean result = false;
		int imgView_size = du.PixelToDP(MESSAGE_IMAGE_MAX_WIDTH);
//		int imgView_height = du.PixelToDP(MESSAGE_IMAGE_MAX_HEIGHT);
		RelativeLayout.LayoutParams params = 
				new RelativeLayout.LayoutParams(imgView_size, imgView_size);
		int left, top, right, bottom;
		String img_url = snsInfo.getPhoto().replace(" ", "%20");
		
		left = du.PixelToDP(5);
		top = du.PixelToDP(5);
		right = du.PixelToDP(5);
		bottom = du.PixelToDP(5);
		
		params.setMargins(left, top, right, bottom);
		params.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		imageFetcher.loadImage(img_url, repler_photo);
		repler_photo.setAdjustViewBounds(true);
		repler_photo.setLayoutParams(params);
		repler_photo.setScaleType(ImageView.ScaleType.FIT_XY);
		
		parent.addView(repler_photo, REPLY_USER_IMG_ID);
		
		return result;
	}
	/*---------------------------------------------------------------------------------------------------------------------------------------------*/
	public void setMidView(){
		TableRow midRow = new TableRow(context);
	}
	
	public String getFilename(int p_id)throws IOException{
		// redirectLink.jsp 의 파라미터 들에는 파일 이름에 대한 정보가 들어 있지 않아
		// 서버의 DB 에 접근 하여 알아 오는 수 밖에 없다. 
		String result = "";
		int response = -1;
		KLoungeHttpRequest httprequest = new KLoungeHttpRequest();
		String post_id = String.valueOf(p_id);

		StringBuffer sb_url = new StringBuffer();
		sb_url.append(httprequest.getService_URL() + "/mobile/appdbbroker/appDataDownDataInfo.jsp");
		sb_url.append("?post_id="+post_id);
		sb_url.append("&post_user_id="+"1");
		sb_url.append("&ck="+MAIN_MESSAGE*0);

		String _url = sb_url.toString();
		if(AppConfig.DEBUG)Log.i(TAG,"URL: "+_url);

		URL url = new URL(_url);
		URLConnection conn = url.openConnection();

		if (!(conn instanceof HttpURLConnection))                    
			throw new IOException("Not an HTTP connection");
		HttpURLConnection httpConn = null;
		String disposition=null;
		try {
			httpConn = (HttpURLConnection) url.openConnection();

			response = httpConn.getResponseCode();	
			if(AppConfig.DEBUG)Log.d(TAG, "resp"+ response);
			
			if (response == HttpURLConnection.HTTP_OK) {
				disposition = httpConn.getHeaderField("Content-Disposition");
			}else {
				return "";
			}
		} catch (Exception ex) {
			Log.e(TAG, "Exception -"+ex);
			throw new IOException("Error connecting");           
		}finally{
			if(httpConn != null){
				httpConn.disconnect();
			}
		}
		
		if(disposition !=null){
			result = new String(disposition.getBytes("8859-1"), "utf-8");
		}
		if(AppConfig.DEBUG)Log.d(TAG, "result: "+result);
		return result;
	}
	/*
	 * baseLinearLayout
	 * 	|_ rl
	 * 	    |_rl2
	 */
	public void setMessageContentUsingRelativeLayout(final SnsAppInfo snsinfo, ImageFetcher mImageFetcher){
		
		int left = 0;
		int top = 0;
		int right = 0;
		int bottom = 0;
		RelativeLayout rl = new RelativeLayout(context); //하나의 글(user_photo, user_name, updated_date, body, video_photo)
		RelativeLayout rl2 = new RelativeLayout(context); // 아래의 bar (댓글, 첨부파일, 삭제)
		RelativeLayout.LayoutParams params = 
				new RelativeLayout.LayoutParams(
						RelativeLayout.LayoutParams.WRAP_CONTENT, 
						RelativeLayout.LayoutParams.WRAP_CONTENT);
		params.setMargins(
				du.PixelToDP(10), //left
				du.PixelToDP(10), //top
				du.PixelToDP(10), //right
				du.PixelToDP(10)); //bottom
		rl.setBackgroundColor(Color.parseColor(contentBG)); //white

		rl.setLayoutParams(params);
		View topGapView = new View(context);
		LinearLayout.LayoutParams gapParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,ViewGroup.LayoutParams.WRAP_CONTENT);
		left = 0;
		top = du.PixelToDP(15);
		right = 0;
		bottom = du.PixelToDP(15);
		gapParams.setMargins(left, top, right, bottom);
		topGapView.setLayoutParams(gapParams);
		baseLinearLayout.addView(topGapView);
		// 사용자 사진
		final ImageView user_photo = new ImageView(context);
		RelativeLayout.LayoutParams ivParams = new RelativeLayout.LayoutParams(du.PixelToDP(MESSAGE_IMAGE_MAX_WIDTH), du.PixelToDP(MESSAGE_IMAGE_MAX_HEIGHT));
		ivParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		ivParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		//        ivParams.setMargins(du.PixelToDP(10), du.PixelToDP(10), 0, 0);

		user_photo.setLayoutParams(ivParams);

		final String imageurl = snsinfo.getPhoto().replace(" ", "%20");
//		imageUtil.loadImage(user_photo, imageurl, snsinfo.getUserId());
		mImageFetcher.loadImage(imageurl, user_photo);
		user_photo.setId(USER_PHOTO_ID);
		user_photo.setAdjustViewBounds(true);
		user_photo.setMinimumHeight(du.PixelToDP(MESSAGE_IMAGE_MIN_HEIGHT));
		user_photo.setMinimumWidth(du.PixelToDP(MESSAGE_IMAGE_MIN_WIDTH));
		user_photo.setMaxHeight(du.PixelToDP(MESSAGE_IMAGE_MAX_HEIGHT));
		user_photo.setMaxWidth(du.PixelToDP(MESSAGE_IMAGE_MAX_WIDTH));

		user_photo.setScaleType(ImageView.ScaleType.FIT_XY);
		user_photo.setPadding(du.PixelToDP(5), du.PixelToDP(5), 0, 0);

		rl.addView(user_photo);

		user_photo.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(context, PersonalLounge.class);
				String user_id = String.valueOf(snsinfo.getUserId());
				String user_name = snsinfo.getUserName();
				String user_photo = imageurl;
				intent.putExtra("puser_id", user_id);
				intent.putExtra("puser_name", user_name);
				intent.putExtra("puser_photo", user_photo);
				context.startActivity(intent);
			}
		});

		// 사용자 이름 및 날짜
		TextView name = new TextView(context);
		name.setId(TEXTVIEW_NAME_ID);
		RelativeLayout.LayoutParams tvNameParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		tvNameParams.addRule(RelativeLayout.RIGHT_OF, user_photo.getId());
		tvNameParams.addRule(RelativeLayout.ALIGN_TOP, user_photo.getId());
		name.setLayoutParams(tvNameParams);
		left = du.PixelToDP(7);
		top = du.PixelToDP(3);
		right = 0;
		bottom = 0;
		name.setPadding(left, top, right, bottom);
		name.setText(snsinfo.getUserName());
		name.setTextSize(TypedValue.COMPLEX_UNIT_SP, MESSAGE_NAME_TEXT_SIZE);
		name.setTextColor(Color.parseColor(linkString));

		rl.addView(name);
		name.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if(!context.getClass().equals(PersonalLounge.class)){//PersonalLounge 안에서 또다시 PersonalLounge 가려는거 막아야지
					Intent intent = new Intent(context, PersonalLounge.class);
					String user_id = String.valueOf(snsinfo.getUserId());
					String user_name = snsinfo.getUserName();
					String user_photo = imageurl;
					intent.putExtra("puser_id", user_id);
					intent.putExtra("puser_name", user_name);
					intent.putExtra("puser_photo", user_photo);
					//					Log.i("onClick",context.getClass().toString());
					context.startActivity(intent);	
				}else{

				}

			}
		});
		TextView date = new TextView(context);
		date.setId(TEXTVIEW_DATE_ID);
		RelativeLayout.LayoutParams tvDateParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		tvDateParams.addRule(RelativeLayout.RIGHT_OF, user_photo.getId());
		tvDateParams.addRule(RelativeLayout.ALIGN_BOTTOM, user_photo.getId());
		//        tvDateParams.setMargins(du.PixelToDP(10), 0, 0, 0);
		date.setLayoutParams(tvDateParams);
		left = 5;
		top=0;
		right=0;
		bottom=2;
		date.setPadding(left, top, right, bottom);
		date.setText(snsinfo.getWrite_date());
		date.setTextSize(TypedValue.COMPLEX_UNIT_SP, MESSAGE_DATE_TEXT_SIZE);
		date.setTextColor(Color.parseColor(lightString));
		rl.addView(date);

		// 메시지 바디
		TextView msg = new TextView(context);
		msg.setId(TEXTVIEW_MESSAGE_ID);
		RelativeLayout.LayoutParams tvMsgParam = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
		tvMsgParam.addRule(RelativeLayout.BELOW, user_photo.getId());
		tvMsgParam.addRule(RelativeLayout.RIGHT_OF, user_photo.getId());
		tvMsgParam.setMargins(du.PixelToDP(5), du.PixelToDP(5), du.PixelToDP(10), du.PixelToDP(5));
		msg.setTextSize(TypedValue.COMPLEX_UNIT_SP, MESSAGE_BODY_TEXT_SIZE);
		msg.setLayoutParams(tvMsgParam);

		// 메시지
		htmlmessage = KLoungeFormatUtil.bodyURLFormat(snsinfo.getBody()).toString();

		if(htmlmessage.contains("/common/redirectLink.jsp?")){
			// redirectLink 이면서 ck 가 (3 or 4) 일 때 즉, dataListView.jsp 를 요구 할 때
			// URL 로 가는게 아닌, 파일 다운로드를 직접 할 것을 요구함.
//			if(AppConfig.DEBUG)Log.d(TAG, "1: "+htmlmessage);
			String post_id = null;
			String ck = null;
			try{
				post_id = htmlmessage.substring(htmlmessage.indexOf("?p_id=")+6, htmlmessage.indexOf("&check"));
				ck = htmlmessage.substring(htmlmessage.indexOf("&check=")+7, htmlmessage.indexOf("&group_id"));
			}catch(StringIndexOutOfBoundsException e){
				e.printStackTrace();
			}
			if(post_id !=null){
				try{
					download_Pid = Integer.parseInt(post_id);
				}catch(NumberFormatException e2){
					e2.printStackTrace();
				}
			}
			int intCk = -1;
			try{
				intCk = Integer.parseInt(ck);
			}catch(NumberFormatException e1){
				e1.printStackTrace();
			}
			if(intCk == 3 || intCk==4){
				
				RelativeLayout attachLayout = new RelativeLayout(context);
				RelativeLayout.LayoutParams relParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
				relParams.addRule(RelativeLayout.RIGHT_OF, TEXTVIEW_REPLY_ID);
				attachLayout.setLayoutParams(relParams);
				attachLayout.setBackgroundResource(R.drawable.button_lines_attach);
				left = du.PixelToDP(5);
				top = du.PixelToDP(15);
				right = du.PixelToDP(5);
				bottom = du.PixelToDP(15);
				attachLayout.setPadding(left, top, right, bottom);
				TextView attachFile = new TextView(context);
					attachFile.setId(REDIRECT_ATTACH_ID);
				ImageView attachFileImg = new ImageView(context);
					attachFileImg.setId(REDIRECT_ATTACH_IMGID);
				
				RelativeLayout.LayoutParams tvfileParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
				tvfileParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
				tvfileParams.addRule(RelativeLayout.CENTER_VERTICAL);
				tvfileParams.setMargins(du.PixelToDP(10), du.PixelToDP(0), du.PixelToDP(10), 0);
				attachFile.setLayoutParams(tvfileParams);

				attachFile.setText("첨부파일");
				attachFile.setTextSize(TypedValue.COMPLEX_UNIT_SP, MESSAGE_ADDED_FILE_TEXT_SIZE);
				attachFile.setTextColor(Color.parseColor(thickString));
				
				attachFileImg.setImageResource(R.drawable.file_01);
				attachFileImg.setMaxHeight(du.PixelToDP(MESSAGE_ADDED_IMAGE_HEIGHT));
				attachFileImg.setMaxWidth(du.PixelToDP(MESSAGE_ADDED_IMAGE_WIDTH));
				attachFileImg.setScaleType(ImageView.ScaleType.FIT_XY);
				RelativeLayout.LayoutParams ivFileParams = new RelativeLayout.LayoutParams(
						du.PixelToDP(MESSAGE_ADDED_IMAGE_WIDTH),
						du.PixelToDP(MESSAGE_ADDED_IMAGE_HEIGHT));
				
				ivFileParams.addRule(RelativeLayout.RIGHT_OF, attachFile.getId());
				ivFileParams.addRule(RelativeLayout.CENTER_VERTICAL);
				left = du.PixelToDP(0);
				top = du.PixelToDP(0);
				right = du.PixelToDP(10);
				bottom =0;
				ivFileParams.setMargins(left, top, right, bottom);
				attachFileImg.setLayoutParams(ivFileParams);
//				attachFileImg.setPadding(du.PixelToDP(5), du.PixelToDP(5), 0, 0);
				// 첨부파일 다운로드
				attachLayout.addView(attachFile, 0);
				attachLayout.addView(attachFileImg, 1);
				final int pid = download_Pid;
				attachLayout.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						vibrator.vibrate(VIBRATE_PERIOD);
						Thread htmlThread = new Thread(new Runnable() {
							@Override
							public void run() {
								try {
									url_filename = getFilename(pid);
									if(url_filename.equals("") || url_filename.length() == 0){
										Log.d(TAG, "no file");
										return;
									}
									
								} catch (IOException e) {
									Log.e(TAG, "IOException -"+e);
								} catch (StringIndexOutOfBoundsException e1){
									Log.e(TAG, "StringIndexOutOfBoundsException -"+e1);
								}
							}
						});
						htmlThread.start();
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						Bundle bun = new Bundle();
						bun.putString("notiMessage", "다운로드 하시겠습니까?");
						String bundle_filename = null;
						try{
							bundle_filename =url_filename.substring(url_filename.indexOf("filename=")+9);
						}catch(StringIndexOutOfBoundsException e){
							e.printStackTrace();
						}catch(NullPointerException e1){
							e1.printStackTrace();
						}
						//Log.i("HTML", bundle_filename);
						bun.putInt("p_id", pid);
						bun.putInt("user_id", Integer.parseInt("1"));
						bun.putInt("ck", MAIN_MESSAGE*0);
						bun.putString("filename", bundle_filename);
						bun.putBoolean("url_linker", true);
						Intent popupIntent = new Intent(context, AttachedDownloadManager.class);

						popupIntent.putExtras(bun);

						PendingIntent pi = PendingIntent.getActivity(context, 0, popupIntent, PendingIntent.FLAG_ONE_SHOT);
						try{
							pi.send();
						}catch(CanceledException e){
							Log.e(TAG, "Cancel Exception -"+e);
						}
					}
				});
				
				try{
//					rl2.addView(attachFile);
//					rl2.addView(attachFileImg);
					rl2.addView(attachLayout);
				}catch(IllegalStateException e){
					e.printStackTrace();
				}
				
			}
			
			String displayMsg = htmlmessage;
			displayMsg = displayMsg.replaceAll("<[^>]*>","");
			displayMsg = displayMsg.replaceAll("&nbsp;", "");
			displayMsg = displayMsg.replaceAll("&amp;", "&");
			displayMsg = displayMsg.replaceAll("&gt;", "<");
			displayMsg = displayMsg.replaceAll("&lt;", ">");
			msg.setText(displayMsg);
			
		}else{
			msg.setText(Html.fromHtml(htmlmessage));
			msg.setMovementMethod(LinkMovementMethod.getInstance());
		}
		msg.setTextColor(Color.parseColor(thickString));

		rl.addView(msg);

		// 첨부된 사진
		ImageView added_image = null;
		String added_imageurl = snsinfo.getPhotoVideo();
		
		if(added_imageurl != null && !added_imageurl.equals("")) {
			added_imageurl = added_imageurl.replace(" ", "%20");
			added_image = new ImageView(context);
			added_image.setId(ADDED_IMAGE_ID);
			if(added_imageurl.contains(".flv")){// 동영상 파일 일 때
				added_image.setBackgroundResource(R.drawable.no_flv);
			}else{
				mImageFetcher.loadImage(added_imageurl, added_image);
			}
			added_image.setScaleType(ImageView.ScaleType.FIT_START);

			RelativeLayout.LayoutParams ivImageParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			ivImageParams.addRule(RelativeLayout.BELOW, msg.getId());
//			ivImageParams.addRule(RelativeLayout.RIGHT_OF,user_photo.getId());
			ivImageParams.addRule(RelativeLayout.CENTER_HORIZONTAL);
			ivImageParams.setMargins(du.PixelToDP(5), 0, du.PixelToDP(5), du.PixelToDP(5));
			
			added_image.setLayoutParams(ivImageParams);
			
			//			added_image.setAdjustViewBounds(true);
			added_image.setPadding(0, 0, du.PixelToDP(5), 0);
			rl.addView(added_image);
		}
		if(added_imageurl != null && !added_imageurl.equals("")) {
//			Log.i("MSG", snsinfo.getReply_count()+"");
			if(added_imageurl.contains(".flv")){// 동영상 파일 일 때
				
			}else{
				AppClickListener acListener = new AppClickListener(snsinfo, context);//new AppClickListener(added_imageurl, replyCount, htmlmessage);
				added_image.setOnClickListener(acListener);
			}
		}
		
		RelativeLayout.LayoutParams params2 = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
		//		params2.setMargins(du.PixelToDP(15), 0, 0, du.PixelToDP(5));
		rl2.setPadding(du.PixelToDP(0), du.PixelToDP(0), 0, du.PixelToDP(0));
		rl2.setLayoutParams(params2);
		if(added_image != null)	params2.addRule(RelativeLayout.BELOW, added_image.getId());
		else params2.addRule(RelativeLayout.BELOW, msg.getId());
		rl2.setBackgroundColor(Color.parseColor(bottomContentBG));

		rl.addView(rl2);

		// 댓글
		TextView tvReply = new TextView(context);
		tvReply.setId(TEXTVIEW_REPLY_ID);
		RelativeLayout.LayoutParams tvReplyParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
		
		tvReplyParams.addRule(RelativeLayout.CENTER_VERTICAL);
		tvReplyParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
		tvReplyParams.setMargins(0, du.PixelToDP(0), du.PixelToDP(0), 0);
		tvReply.setLayoutParams(tvReplyParams);

		tvReply.setTextSize(TypedValue.COMPLEX_UNIT_SP, MESSAGE_REPLY_TEXT_SIZE);
		int replyCount = snsinfo.getReply_count();

		tvReply.setText("댓글("+replyCount+")");
		tvReply.setTextColor(Color.parseColor(thickString));
		tvReply.setBackgroundResource(R.drawable.button_lines);
		left = du.PixelToDP(10);
		top = du.PixelToDP(15);
		right = du.PixelToDP(10);
		bottom = du.PixelToDP(15);
		tvReply.setPadding(left, top, right, bottom);
		tvReply.setOnClickListener(new View.OnClickListener() {

			public void onClick(View v) {
				vibrator.vibrate(VIBRATE_PERIOD);
				// 새로운 창 추가
				try {
					Intent intent = new Intent(context, ReplyActivity.class);

					intent.putExtra("snsAppInfo", snsinfo);

					context.startActivity(intent);

				} catch (Exception e) {
					e.printStackTrace();
				}								
			}
		});

		rl2.addView(tvReply);
		
		// 첨부파일
		final String attach = snsinfo.getAttach();
		if(!attach.equals("") && !attach.equals("null")) {
			RelativeLayout attachLayout = new RelativeLayout(context);
			RelativeLayout.LayoutParams relParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
			relParams.addRule(RelativeLayout.RIGHT_OF, TEXTVIEW_REPLY_ID);
			attachLayout.setLayoutParams(relParams);
			attachLayout.setBackgroundResource(R.drawable.button_lines_attach);
			left = du.PixelToDP(5);
			top = du.PixelToDP(15);
			right = du.PixelToDP(5);
			bottom = du.PixelToDP(15);
			attachLayout.setPadding(left, top, right, bottom);
			file = new TextView(context);
			file.setId(TEXTVIEW_FILE_ID);
			RelativeLayout.LayoutParams tvfileParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
			tvfileParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
			tvfileParams.addRule(RelativeLayout.CENTER_VERTICAL);
			tvfileParams.setMargins(du.PixelToDP(5), du.PixelToDP(0), du.PixelToDP(10), 0);
			file.setLayoutParams(tvfileParams);

			file.setText("첨부파일");
			file.setTextSize(TypedValue.COMPLEX_UNIT_SP, MESSAGE_ADDED_FILE_TEXT_SIZE);
			file.setTextColor(Color.parseColor(thickString));

			file_img = new ImageView(context);
			file_img.setId(FILE_IMG_ID);
			file_img.setImageResource(R.drawable.file);
			file_img.setMaxHeight(du.PixelToDP(MESSAGE_ADDED_IMAGE_HEIGHT));
			file_img.setMaxWidth(du.PixelToDP(MESSAGE_ADDED_IMAGE_WIDTH));
			file_img.setScaleType(ImageView.ScaleType.FIT_XY);

			RelativeLayout.LayoutParams ivFileParams = new RelativeLayout.LayoutParams(
					du.PixelToDP(MESSAGE_ADDED_IMAGE_WIDTH),
					du.PixelToDP(MESSAGE_ADDED_IMAGE_HEIGHT));
			
			ivFileParams.addRule(RelativeLayout.RIGHT_OF, file.getId());
			ivFileParams.addRule(RelativeLayout.CENTER_VERTICAL);
			ivFileParams.setMargins(du.PixelToDP(0), du.PixelToDP(0), du.PixelToDP(10), 0);
			
			file_img.setLayoutParams(ivFileParams);
//			file_img.setPadding(du.PixelToDP(5), du.PixelToDP(5), 0, 0);
			
			// 첨부파일 다운로드
			attachLayout.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					vibrator.vibrate(VIBRATE_PERIOD);
					Bundle bun = new Bundle();
					bun.putString("notiMessage", "다운로드 하시겠습니까?");
					bun.putInt("p_id", snsinfo.getPostId());
					bun.putInt("user_id", snsinfo.getUserId());
					bun.putInt("ck", MAIN_MESSAGE*0);
					bun.putString("filename", attach);
					Intent popupIntent = new Intent(context, AttachedDownloadManager.class);

					popupIntent.putExtras(bun);

					PendingIntent pi = PendingIntent.getActivity(context, 0, popupIntent, PendingIntent.FLAG_ONE_SHOT);
					try{
						pi.send();
					}catch(Exception e){
						Toast.makeText(context, e.toString(), Toast.LENGTH_LONG).show();
					}
				}
			});
			
			attachLayout.addView(file, 0);
			attachLayout.addView(file_img,1);
			rl2.addView(attachLayout);
		}

		// 삭제
		if(AppUser.user_id == snsinfo.getUserId()) {
			ImageView ivDelete = new ImageView(context);
			ivDelete.setId(TEXTVIEW_DELETE_ID);
			
			RelativeLayout.LayoutParams ivDeleteParam = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT);
			ivDeleteParam.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
			ivDeleteParam.addRule(RelativeLayout.CENTER_VERTICAL);
			ivDeleteParam.setMargins(0, 0, du.PixelToDP(0), 0);
			ivDelete.setLayoutParams(ivDeleteParam);

			ivDelete.setImageResource(R.drawable.button_line_with_img);
			
//			ivDelete.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

			ivDelete.setTag(snsinfo.getPostId());

			ivDelete.setOnClickListener(new View.OnClickListener() {

				public void onClick(View v) {
					try {
						mDeleteTask = new AsyncTask<Void, Void, Void>() {
							@Override
							protected Void doInBackground(Void... params) {
								Log.i("message delete", AppUser.user_id+" / "+ snsinfo.getPostId());
								// DB 삭제 처리 추가
								KLoungeRequest kreq = new KLoungeRequest();
								kreq.deleteMessage(AppUser.user_id, snsinfo.getPostId(), MAIN_MESSAGE);

								return null;
							}
							@Override
							protected void onPostExecute(Void result) {
								// 메인과 답글에 따라 새롭게 메시지 리스트 가져오기
								for(int i=0; i<baseLinearLayout.getChildCount(); i++) {
									View tv = (View)baseLinearLayout.getChildAt(i).findViewById(TEXTVIEW_DELETE_ID);
									Integer id = 0;//(Integer)tv.getTag();
									try{
										id = (Integer)tv.getTag();
									}catch(NullPointerException e){
										e.printStackTrace();
									}
									int tag_post_id = id.intValue();
									Log.i("tag_post_id", String.valueOf(tag_post_id));
									if(tag_post_id==snsinfo.getPostId()) {
										Log.i("delete index", String.valueOf(i));
										baseLinearLayout.removeViewAt(i);
										break;
									}
								}
								mDeleteTask = null;
							}
						};

					} catch (Exception e) {
						e.printStackTrace();
					}

					AlertDialog.Builder alertDlg = new AlertDialog.Builder(context);
					alertDlg.setTitle("확인");
					alertDlg.setMessage("삭제 하시겠습니까?");
					alertDlg.setPositiveButton("삭제", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
							mDeleteTask.execute(null, null, null);
						}
					});
					alertDlg.setNegativeButton("취소", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int which) {
						}
					});
					alertDlg.show();
				}
			});

			rl2.addView(ivDelete);
		}
		baseLinearLayout.addView(rl);
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
}
