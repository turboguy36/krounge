package kr.co.ktech.cse.adapter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import kr.co.ktech.cse.CommonUtilities;
import kr.co.ktech.cse.model.GroupInfo;
import kr.co.ktech.cse.util.RecycleUtils;
import android.content.Context;
import android.graphics.Color;
import android.text.Layout;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

public class SpinAdapter extends ArrayAdapter<GroupInfo> {
	private static final int SPINNER_TEXT_COLOR = 0xAA000000;
	private static final int SPINNER_TEXT_SIZE = 17;
	private static final int SPINNER_HEIGHT = 47;
	private static final int SPINNER_BGCOLOR = 0xFFBECCD7;
	private Context context;
	//private List<GroupInfo> group_list;
	private GroupInfo[] group_list;
	private List<WeakReference<View>> mRecycleList = new ArrayList<WeakReference<View>>();

	public SpinAdapter(final Context context, final int textViewResourceId, final List<GroupInfo> objects) {
		super(context, textViewResourceId, objects);
		this.context = context;
		//		this.group_list = objects;
		this.group_list = new GroupInfo[objects.size()];
		for(int i=0; i<group_list.length; i++) {
			group_list[i] = new GroupInfo();
			group_list[i].setGroup_id(objects.get(i).getGroup_id());
			group_list[i].setGroup_name(objects.get(i).getGroup_name());
		}
		//this.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	}

	//onDestory에서 쉽게 해제할 수 있도록 메소드 생성
	public void recycle() {
		for (WeakReference<View> ref : mRecycleList) {
			RecycleUtils.recursiveRecycle(ref.get());
		}
	}

	@Override
	public int getCount() {
		//        return group_list.size();
		return group_list.length;
	}
	@Override
	public GroupInfo getItem(int position) {
		//        return group_list.get(position);
		return group_list[position];
	}
	@Override
	public long getItemId(int position) {
		//        return group_list.get(position).hashCode();
		return position;
	}

	public GroupInfo getGroupInfo(int pos) {
		return group_list[pos];
	}
	@Override
	public int getPosition(GroupInfo ginfo) {
		int pos;
		for(pos = 0; pos < group_list.length; pos++) {
			GroupInfo gi = group_list[pos];
			if(gi.getGroup_id() == ginfo.getGroup_id()) break;
		}
		return pos;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		int left,top,right,bottom;
		left = CommonUtilities.DPFromPixel(context, 20);
		top = CommonUtilities.DPFromPixel(context, 0);
		right = CommonUtilities.DPFromPixel(context, 0);
		bottom = CommonUtilities.DPFromPixel(context, 0);
		// I created a dynamic TextView here, but you can reference your own
		// custom layout for each spinner item
		TextView label = new TextView(context);
		label.setGravity(Gravity.CENTER_VERTICAL);
		// Then you can get the current item using the values array (Users array) and the current position
		// You can NOW reference each method you has created in your bean object (User class)
		label.setText(group_list[position].getGroup_name());
		label.setTextColor(SPINNER_TEXT_COLOR);
		label.setTextSize(TypedValue.COMPLEX_UNIT_SP, SPINNER_TEXT_SIZE-1);
		label.setHeight(CommonUtilities.DPFromPixel(context, SPINNER_HEIGHT));
		label.setPadding(left, top, right, bottom);
//		label.setBackgroundColor(SPINNER_BGCOLOR);
		// And finally return your dynamic (or custom) view for each spinner item
		// 메모리 해제할 View를 추가
		mRecycleList.add(new WeakReference<View>(label));
		return label;
	}

	// And here is when the "chooser" is popped up
	// Normally is the same view, but you can customize it if you want
	@Override
	public View getDropDownView(int position, View convertView, ViewGroup parent) {
		int left,top,right,bottom;
		left = CommonUtilities.DPFromPixel(context, 20);
		top = CommonUtilities.DPFromPixel(context, 10);
		right = CommonUtilities.DPFromPixel(context, 0);
		bottom = CommonUtilities.DPFromPixel(context, 10);
		TextView label = new TextView(context);
		label.setGravity(Gravity.CENTER_VERTICAL);
		label.setText(group_list[position].getGroup_name());
		label.setTextColor(SPINNER_TEXT_COLOR);
		label.setTextSize(TypedValue.COMPLEX_UNIT_SP, SPINNER_TEXT_SIZE);
		label.setHeight(CommonUtilities.DPFromPixel(context, SPINNER_HEIGHT +23));
		label.setPadding(left, top, right, bottom);
		label.setBackgroundColor(SPINNER_BGCOLOR);
		// 메모리 해제할 View를 추가
		mRecycleList.add(new WeakReference<View>(label));
		
		return label;
	}	
}
