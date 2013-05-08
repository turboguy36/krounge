package kr.co.ktech.cse.db;

import kr.co.ktech.cse.model.GroupInfo;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.SharedPreferences;
import android.util.Log;

public class LoginRequest {
	
	KLoungeHttpRequest httprequest;
	
	public LoginRequest() {
		httprequest = new KLoungeHttpRequest();
	}
	
	public boolean login(String id, String passwd, SharedPreferences pref) {
		Boolean result = false;
		
		try {
			String addr = httprequest.getService_URL() + "/mobile/appdbbroker/appLogin.jsp";
			String parameter = "login_id="+id+"&passwd="+passwd;
			addr = addr+"?"+parameter;
			
			String strJSON = httprequest.getJSONHttpURLConnection(addr);
			//String strJSON = httprequest.getJSONHttpGet(addr);
//			Log.i("json",strJSON);
			
			if(strJSON.length() > 0) {
				//파싱
				JSONObject jsonObj = new JSONObject(strJSON);
				int login_result = Integer.parseInt(jsonObj.getString("login_result"));
				if(login_result == 1) result = true;
				
				JSONObject userObj = jsonObj.getJSONObject("user");
				int user_id = Integer.parseInt(userObj.getString("user_id"));
				String user_name = userObj.getString("user_name");
				//Log.i("user", user_id+", "+user_name);
				
	//			JSONObject groupObj = jsonObj.getJSONObject("viewGroup");
	//			int group_id = Integer.parseInt(groupObj.getString("group_id"));
	//			int group_name = Integer.parseInt(groupObj.getString("group_name"));
	//			Log.i("group", group_id+", "+group_name);
				JSONArray groupArray = jsonObj.getJSONArray("group");
				StringBuffer grouplist = new StringBuffer();
				for(int i=0; i<groupArray.length(); i++){
					JSONObject groupObj = groupArray.getJSONObject(i);
					int group_id = Integer.parseInt(groupObj.getString("group_id"));
					String group_name = groupObj.getString("group_name");
					int group_tot_num = groupObj.getInt("group_total_number");
					
					//if(i==groupArray.length()-1) grouplist.append(group_id+"_"+group_name);
					//else 
					grouplist.append(group_id+"|"+group_name+"|"+group_tot_num+",");
//					Log.i("group", group_id+", "+group_name+", "+group_tot_num);
				}
				
				// 모든 메시지를 볼 수 있는 "전체" 카테고리를 삽입
				grouplist.append("0|공개라운지|999");
				
				// 사용자 로그인 정보 저장 - 로그인 상태, user_id, 사용자 이름
				SharedPreferences.Editor edit = pref.edit();
				edit.putInt("user_id", user_id);
				edit.putString("user_name", user_name);
				edit.putString("group_list", grouplist.toString());
//				Log.i("KLOUNGE",grouplist.toString());
				
				edit.commit();
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
		
}
