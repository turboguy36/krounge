package kr.co.ktech.cse.model;

import java.util.ArrayList;
import java.util.List;

import kr.co.ktech.cse.bitmapfun.util.ImageFetcher;

import android.util.SparseArray;

public class AppUser {
	
	public static int user_id;
	public static String user_name;
	public static String user_photo;
	public static List<GroupInfo> GROUP_LIST = new ArrayList<GroupInfo>();
	public static SparseArray<List<GroupMemberInfo>>GROUP_MEMBER = new SparseArray<List<GroupMemberInfo>>();
	public static SparseArray<List<GroupInfo>>MEMBER_INFO = new SparseArray<List<GroupInfo>>();
	
	final public static int KLOUNGE_TAB = 0;
	final public static int MYLOUNGE_TAB = 1;
	final public static int GROUP_LIST_TAB = 2;
	final public static int MORE_TAB = 3;
	
	public static ImageFetcher mImageFetcher;
	
	public static int CURRENT_TAB = 0;
	public static int SHARED_GROUPID= -1;
	
}

