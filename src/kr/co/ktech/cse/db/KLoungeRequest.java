package kr.co.ktech.cse.db;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import kr.co.ktech.cse.CommonUtilities;
import kr.co.ktech.cse.model.SnsAppInfo;

import org.apache.http.NameValuePair;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.ByteArrayBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONObject;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import static kr.co.ktech.cse.CommonUtilities.ENCODING;
import static kr.co.ktech.cse.CommonUtilities.TAG;
public class KLoungeRequest {
	final int IMAGE_MAX_SIZE_W = 600;
	final int IMAGE_MAX_SIZE_H = 800;
	private KLoungeHttpRequest httprequest;

	public KLoungeRequest() {

		httprequest = new KLoungeHttpRequest();
	}

	public List<SnsAppInfo> getGroupMessageList(int user_id, int group_id, int reload) {
		List<SnsAppInfo> result = new ArrayList<SnsAppInfo>();

		try {
			String addr = httprequest.getService_URL() + "/mobile/appdbbroker/appKLounge.jsp";
			String parameter = "user_id="+user_id+"&group_id="+group_id+"&reload="+reload;
			addr = addr+"?"+parameter;
//			Log.i(this.getClass().toString(), addr);

			String strJSON = httprequest.getJSONHttpURLConnection(addr);
			//			Log.i(TAG,strJSON);
			//파싱
			result = parseStrJSON(strJSON);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}
	public List<SnsAppInfo> getMyLoungeMessageList(int user_id, int group_id, int reload) {
		List<SnsAppInfo> result = new ArrayList<SnsAppInfo>();

		try {
			String addr = httprequest.getService_URL() + "/mobile/appdbbroker/appKLounge.jsp";
			String parameter = "type=mylounge&user_id="+user_id+"&group_id="+group_id+"&reload="+reload;
			addr = addr+"?"+parameter;
//			Log.i("URL",addr);
			String strJSON = httprequest.getJSONHttpURLConnection(addr);

			//strJSON = "{\"klounge\": {\"message\": [{\"post_id\": \"1979\",\"user_id\": \"6\",\"photo\": \"http://www.knowledgetech.co.kr/etrihub/photo/6/2011-05-09 00.52.32.jpg\",\"user_name\": \"양재동\",\"date\": \"2012-09-24 05:20\",\"comment\":\"갤3 볼수록 괜찮네요. 아이폰 진영이 흔들릴수도 있겠다는 `생각이 들었습니다`. 안드로이드 기반 기술을 열심히 축적해야 겠습니다.\", \"attach_file\": \"\",\"reply_count\":\"0\"}]}}";
			//파싱
			result = parseStrJSON(strJSON);

		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public List<SnsAppInfo> getPersonalLoungeMessageList(int user_id, int group_id, int puser_id, int reload) {
		List<SnsAppInfo> result = new ArrayList<SnsAppInfo>();

		try {
			String addr = httprequest.getService_URL() + "/mobile/appdbbroker/appKLounge.jsp";
			String parameter = "type=personallounge&user_id="+user_id+"&group_id="+group_id+"&puser_id="+puser_id+"&reload="+reload;
			addr = addr+"?"+parameter;
//			Log.i(TAG+"ADDR: ",addr);
			String strJSON = httprequest.getJSONHttpURLConnection(addr);

			//strJSON = "{\"klounge\": {\"message\": [{\"post_id\": \"1979\",\"user_id\": \"6\",\"photo\": \"http://www.knowledgetech.co.kr/etrihub/photo/6/2011-05-09 00.52.32.jpg\",\"user_name\": \"양재동\",\"date\": \"2012-09-24 05:20\",\"comment\":\"갤3 볼수록 괜찮네요. 아이폰 진영이 흔들릴수도 있겠다는 `생각이 들었습니다`. 안드로이드 기반 기술을 열심히 축적해야 겠습니다.\", \"attach_file\": \"\",\"reply_count\":\"0\"}]}}";
			//파싱
			result = parseStrJSON(strJSON);
			//			Log.i(this.getClass().toString(),strJSON);	
		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	private List<SnsAppInfo> parseStrJSON(String strJSON) {
		List<SnsAppInfo> result = new ArrayList<SnsAppInfo>();
		try {
			JSONObject jsonObj = new JSONObject(strJSON);
			JSONObject kloungeObj = jsonObj.getJSONObject("klounge");
			JSONArray messageArray = kloungeObj.getJSONArray("message");
			int group_id = kloungeObj.getInt("group_id");
			for(int i=0; i<messageArray.length(); i++) {
				JSONObject messageObj = messageArray.getJSONObject(i);
				SnsAppInfo saInfo = new SnsAppInfo();
				saInfo.setGroupId(group_id);
				saInfo.setPostId(messageObj.getInt("post_id"));
				saInfo.setUserId(messageObj.getInt("user_id"));
				saInfo.setPhoto(messageObj.getString("photo"));
				saInfo.setUserName(messageObj.getString("user_name"));
				saInfo.setWrite_date(messageObj.getString("date"));
				saInfo.setBody(messageObj.getString("comment"));
				saInfo.setAttach(messageObj.getString("attach_file"));
				saInfo.setPhotoVideo(messageObj.getString("photo_video_file"));
				saInfo.setReply_count(messageObj.getInt("reply_count"));

				if(i<1) {
//					Log.i("message", saInfo.getUserId()+":"+saInfo.getUserName()+":"+saInfo.getWrite_date()+":"+saInfo.getBody()
//							+":"+saInfo.getBody()+":"+saInfo.getReply_count());					
				}

				result.add(saInfo);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	public List<SnsAppInfo> getReplyMessageList(int post_id) {
		List<SnsAppInfo> result = new ArrayList<SnsAppInfo>();

		try {
			String addr = httprequest.getService_URL() + "/mobile/appdbbroker/appKLounge.jsp";
			String parameter = "type=reply&post_id="+post_id;
			addr = addr+"?"+parameter;
//			Log.i(TAG,addr);
			String strJSON = httprequest.getJSONHttpURLConnection(addr);

			//파싱
			JSONObject jsonObj = new JSONObject(strJSON);
			JSONObject kloungeObj = jsonObj.getJSONObject("klounge");
			JSONArray replyArray = kloungeObj.getJSONArray("reply");

			for(int i=0; i<replyArray.length(); i++) {
				JSONObject replyObj = replyArray.getJSONObject(i);
				SnsAppInfo saInfo = new SnsAppInfo();
				saInfo.setPostId(replyObj.getInt("reply_post_id"));
				saInfo.setUserId(replyObj.getInt("reply_user_id"));
				saInfo.setPhoto(replyObj.getString("photo"));
				saInfo.setUserName(replyObj.getString("reply_user_name"));
				saInfo.setReply_to_user_name(replyObj.getString("reply_to_user_name"));
				saInfo.setWrite_date(replyObj.getString("reply_date"));
				saInfo.setBody(replyObj.getString("reply_comment"));
				saInfo.setAttach(replyObj.getString("reply_attach_file"));

				//				if(i<1) {
				//					Log.i("message", saInfo.getUserId()+":"+saInfo.getUserName()+":"+saInfo.getWrite_date()+":"+saInfo.getBody()
				//							+":"+saInfo.getBody()+":"+saInfo.getReply_count());					
				//				}

				result.add(saInfo);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return result;
	}

	public void sendMessageWithImage(
			int group_id, int user_id, String message_body, 
			String imagepath, int puser_id, Context context)
					throws Exception, OutOfMemoryError {

		Display display = ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay();
		int displayWidth = display.getWidth();
		int displayHeight = display.getHeight();

		httprequest = new KLoungeHttpRequest();

		String url = httprequest.getService_URL() + "/mobile/appdbbroker/appSendMessageWithFile.jsp";

		String imagename = new String(imagepath.substring(imagepath.lastIndexOf('/')+1));
		String image_ext = new String(imagepath.substring(imagepath.lastIndexOf('.')+1)).toLowerCase();

		//		Log.i("image file", imagename + " / " + image_ext);
		try {
			MultipartEntity entity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
			ByteArrayOutputStream bos = new ByteArrayOutputStream();

			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(imagepath, options);
			int photoWidth = options.outWidth;
			int photoHeight = options.outHeight;

			int scale = 1;
			float widthScale = photoWidth / displayWidth;
			float heightScale = photoHeight / displayHeight;
			float fltScale = widthScale > heightScale ? widthScale : heightScale;

			//			Log.i(TAG,"displayWidth: "+displayWidth);
			//			Log.i(TAG,"displayHeight: "+displayHeight);
			//			Log.i(TAG,"photoWidth: "+photoWidth);
			//			Log.i(TAG,"photoHeight: "+photoHeight);
			if(fltScale >= 8){
				scale = 8;
			}else if(fltScale >=4){
				scale = 4;
			}else if(fltScale >= 2){
				scale = 2;
			}else {
				scale = 1;
			}
			//			Log.i(TAG, "SCALE: "+scale);
			//			if (photoHeight * photoWidth >= IMAGE_MAX_SIZE_H * IMAGE_MAX_SIZE_W) {
			//				//scale = (int)Math.pow(2, (int)Math.round(Math.log(IMAGE_MAX_SIZE_W / (double) Math.max(photoHeight, photoWidth)) / Math.log(0.5)));
			//				Log.i(TAG,"SCALE: "+scale);
			//				options.inSampleSize  = scale;
			//			}
			options.inSampleSize  = scale;
			options.inJustDecodeBounds = false;
			Bitmap bitmap = BitmapFactory.decodeFile(imagepath, options);

			if(image_ext.equals("jpg") || image_ext.equals("jpeg")) 
				bitmap.compress(CompressFormat.JPEG, 100, bos);
			if(image_ext.equals("png")) 
				bitmap.compress(CompressFormat.PNG, 100, bos);
			byte[] data = bos.toByteArray();
			int len = data.length;
			//			Log.i("image file","length: "+len);

			entity.addPart("ftype", new StringBody("img"));
			entity.addPart("user_id", new StringBody(String.valueOf(user_id)));
			entity.addPart("group_id", new StringBody(String.valueOf(group_id)));
			entity.addPart("puser_id", new StringBody(String.valueOf(puser_id)));
			entity.addPart("message_body", new StringBody(URLEncoder.encode(message_body, "UTF-8")));
			entity.addPart("uploadimage", new ByteArrayBody(data, imagename));

			String success = httprequest.executeHttpPost(url, entity);

			Log.i("post result", success);

		} catch(Exception e) {
			Log.i(TAG,e.toString());
			//			e.printStackTrace();
		}

		/*
        StringBuffer sb_param = new StringBuffer();
        try {
			sb_param.append("type=").append(URLEncoder.encode("send_message", "UTF-8"));
			sb_param.append("&user_id=").append(user_id);
	        sb_param.append("&group_id=").append(group_id);
	        sb_param.append("&message_body=").append(URLEncoder.encode(message_body, "UTF-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}

        ImageUploader imageuploader = new ImageUploader();
        imageuploader.HttpFileUpload(url, sb_param.toString(), imagepath);
		 */
	}

	public void sendMessage(int group_id, int user_id, String message_body, int post_id, String type, int puser_id) {
//		Log.i("sendMessage", "group_id: " + group_id + "user_id: " + user_id + " / " + message_body);
		httprequest = new KLoungeHttpRequest();

		String url = httprequest.getService_URL() + "/mobile/appdbbroker/appSendMessage.jsp";
		ArrayList<NameValuePair> nameValuePairs = new  ArrayList<NameValuePair>();

		try {
			nameValuePairs.add(new BasicNameValuePair("user_id", String.valueOf(user_id)));
			nameValuePairs.add(new BasicNameValuePair("group_id", String.valueOf(group_id)));
			nameValuePairs.add(new BasicNameValuePair("message_body", URLEncoder.encode(message_body, "UTF-8")));
			nameValuePairs.add(new BasicNameValuePair("post_id", String.valueOf(post_id)));
			nameValuePairs.add(new BasicNameValuePair("type", URLEncoder.encode(type, "UTF-8")));
			nameValuePairs.add(new BasicNameValuePair("puser_id", String.valueOf(puser_id)));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}     
		String success = "";
		success = httprequest.executeHttpPost(url, nameValuePairs, false);

		if(success != "" && success != null) Log.i("post result", success);
	}

	public void deleteMessage(int user_id, int post_id, int r) {
//		Log.i("delete post", user_id+" / " + post_id);
		httprequest = new KLoungeHttpRequest();

		String url = httprequest.getService_URL() + "/mobile/appdbbroker/appDeleteMessage.jsp";
		ArrayList<NameValuePair> nameValuePairs = new  ArrayList<NameValuePair>();

		try {
			nameValuePairs.add(new BasicNameValuePair("user_id", String.valueOf(user_id)));
			nameValuePairs.add(new BasicNameValuePair("post_id", String.valueOf(post_id)));
			nameValuePairs.add(new BasicNameValuePair("r", String.valueOf(r)));

		} catch (Exception e) {
			e.printStackTrace();
		}     
		String success = "";
		Log.d(TAG, url);
		Log.d(TAG, user_id+"");
		Log.d(TAG, post_id+"");
		Log.d(TAG, r+"");
		success = httprequest.executeHttpPost(url, nameValuePairs, false);
		
		if(success != "" && success != null) Log.i("delete result", success);
	}

	public void dataDownload2(int post_id, int post_user_id, int ck, String filename){
//		Log.i("dataDownload ", post_user_id+" / " + post_id);
		httprequest = new KLoungeHttpRequest();

		String url = httprequest.getService_URL() + "/mobile/appdbbroker/appDataDown.jsp";
		ArrayList<NameValuePair> nameValuePairs = new  ArrayList<NameValuePair>();

		try {
			nameValuePairs.add(new BasicNameValuePair("post_id", String.valueOf(post_id)));
			nameValuePairs.add(new BasicNameValuePair("post_user_id", String.valueOf(post_user_id)));
			nameValuePairs.add(new BasicNameValuePair("ck", String.valueOf(ck))); // 메인 0 : 답글 1

			httprequest.executeDataDownload(url, nameValuePairs, filename);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void dataDownload(int post_id, int post_user_id, int ck, String filename){
		StringBuffer sb_url = new StringBuffer();
		sb_url.append(httprequest.getService_URL() + "/mobile/appdbbroker/appDataDown.jsp");
		//		_url = _url + "?post_id=2125&post_user_id=17&ck=0";
		sb_url.append("?post_id="+post_id);
		sb_url.append("&post_user_id="+post_user_id);
		sb_url.append("&ck="+ck);
		String _url = sb_url.toString();
//		Log.i("URL",_url);
		String location = CommonUtilities.DOWNLOAD_PATH+"/Download/";
		InputStream in = null;       

		try {
			in = OpenHttpConnection(_url);
			if(in!=null){
				saveToInternalStorage(location,in,filename);
				in.close();
			}
		} catch (Exception e1) {
			Log.i("EXCEPTION",e1.toString());
		}
	}
	private static InputStream OpenHttpConnection(String urlString) throws IOException {
		InputStream in = null;
		int response = -1;
		//		Log.i("URL", urlString);
		URL url = new URL(urlString);
		
		URLConnection conn = url.openConnection();
		if (!(conn instanceof HttpURLConnection))                    
			throw new IOException("Not an HTTP connection");
		try {
			System.out.println("OpenHttpConnection called");

			HttpURLConnection httpConn = (HttpURLConnection) conn;
			httpConn.setAllowUserInteraction(false);
			httpConn.setInstanceFollowRedirects(true);
			httpConn.setRequestMethod("GET");
			httpConn.setDoOutput(true);
			httpConn.connect();
			response = httpConn.getResponseCode();			

			//			Log.i("ENC", httpConn.getHeaderField("Content-Type"));
			//			Log.i("response is",response);
			//			Log.i("connection is",HttpURLConnection.HTTP_OK+"");

			if (response == HttpURLConnection.HTTP_OK) {
				in = httpConn.getInputStream();
				String disposition = httpConn.getHeaderField("Content-Disposition");
				String contentType = httpConn.getContentType();
				int contentLength = httpConn.getContentLength();
				System.out.println("Content-Type = " + contentType);
				System.out.println("Content-Disposition = " +  new String(disposition.getBytes("8859-1"), "utf-8"));
				System.out.println("Content-Length = " +contentLength);
				
				//System.out.println("Connection Ok");
				return in;
			}
		} catch (Exception ex) {
			throw new IOException("Error connecting");           
		}
		return in;
	}
	private static void saveToInternalStorage(String location,InputStream in,String filename) {

		try {
			int len1  = 0;
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(location+filename),ENCODING));
			String str_buffer =null;

			File dir=new File(location);
			if(!dir.exists()) dir.mkdirs();

			FileOutputStream fos = new FileOutputStream(location+filename);

			//byte[] buffer=new byte[4096];
			StringBuilder sbuilder = new StringBuilder();
			while ( (str_buffer = br.readLine() )!= null ) {
				bw.write(str_buffer,0,str_buffer.length());
				bw.newLine();
			}
			bw.close();
			br.close();
		} catch (Exception e) {
			Log.i("EXCEPTION",e.toString());
		} 
	}
}
