/*
 * Copyright (C) 2012 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package kr.co.ktech.cse.bitmapfun.ui;

import java.util.ArrayList;
import java.util.List;

import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract.Directory;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewTreeObserver;
import android.webkit.WebView.FindListener;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import kr.co.ktech.cse.R;
import kr.co.ktech.cse.CommonUtilities;
import kr.co.ktech.cse.activity.PersonalLounge;
import kr.co.ktech.cse.AppConfig;
import kr.co.ktech.cse.bitmapfun.provider.ImagesFromServer;
import kr.co.ktech.cse.bitmapfun.util.ImageFetcher;
import kr.co.ktech.cse.bitmapfun.util.Utils;
import kr.co.ktech.cse.bitmapfun.util.ImageCache.ImageCacheParams;
import kr.co.ktech.cse.db.KLoungeGroupRequest;
import kr.co.ktech.cse.model.AppUser;
import kr.co.ktech.cse.model.GroupInfo;
import kr.co.ktech.cse.model.GroupMemberInfo;

/**
 * The main fragment that powers the ImageGridActivity screen. Fairly straight forward GridView
 * implementation with the key addition being the ImageWorker class w/ImageCache to load children
 * asynchronously, keeping the UI nice and smooth and caching thumbnails for quick retrieval. The
 * cache is retained over configuration changes like orientation change so the images are populated
 * quickly if, for example, the user rotates the device.
 */
public class CopyOfImageGridFragment extends Fragment implements AdapterView.OnItemClickListener {
	private static final String TAG = "ImageGridFragment";
	private static final String IMAGE_CACHE_DIR = "thumbs";
	private final String NO_IMAGE_URL = CommonUtilities.SERVICE_URL + "/images/sns/no_photo_small.gif";
	private int mImageThumbSize;
	private int mImageThumbSpacing;
	private ImageAdapter mAdapter;
	private ImageFetcher mImageFetcher;
	int group_id = -1;
	ImagesFromServer images;
	List<GroupMemberInfo> gmList = null;
	private ProgressDialog pd;
	
	/**
	 * Empty constructor as per the Fragment documentation
	 */
	public CopyOfImageGridFragment() {}

	@Override
	public void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
//		Log.i(TAG, "user id: "+AppUser.user_id );
		String gname = "";
		int gnum = -1;
		Intent intent = getActivity().getIntent();
		group_id = intent.getIntExtra("group_id", 0);
		gname = intent.getStringExtra("group_name");
		gnum = intent.getIntExtra("group_total_number", 0);

		createThreadAndDialog();
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		int gmlistSize = 0;
		
		try{
			gmlistSize =gmList.size();
		}catch(NullPointerException e){
			e.printStackTrace();
		}
		images = new ImagesFromServer(gmlistSize);
//		Log.i(TAG, "size: "+gmList.size() + ""); // normalcy
		for(int i=0;i<gmlistSize;i++){
			String bigImg = gmList.get(i).getPhoto();
//			Log.d(TAG, bigImg);
			String lowerCase = bigImg.toLowerCase();
			if(lowerCase.contains("jpg") || lowerCase.contains("gif") || lowerCase.contains("bmp") || lowerCase.contains("png")){
				images.imageThumbUrls[i] = getThumbImgUrls(bigImg);	
			}else{
				images.imageThumbUrls[i] = getThumbImgUrls(NO_IMAGE_URL);
			}
			
		}
		mImageThumbSize = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
		mImageThumbSpacing = getResources().getDimensionPixelSize(R.dimen.image_thumbnail_spacing);

		mAdapter = new ImageAdapter(getActivity(), gmList);

		ImageCacheParams cacheParams = new ImageCacheParams(getActivity(), IMAGE_CACHE_DIR);

		cacheParams.setMemCacheSizePercent(0.25f); // Set memory cache to 25% of app memory

		// The ImageFetcher takes care of loading images into our ImageView children asynchronously
		mImageFetcher = new ImageFetcher(getActivity(), mImageThumbSize);
//		mImageFetcher.setLoadingImage(R.drawable.no_photo);
		mImageFetcher.addImageCache(getActivity().getSupportFragmentManager(), cacheParams);

	}
	private String getThumbImgUrls(String photoURL){
		String bigImg = photoURL;
		String filename = "";
		String directory  ="";
		String ext ="";
		bigImg = bigImg.replace(" ", "%20");
		try{
			filename = bigImg.substring(bigImg.lastIndexOf("/")+1);
			directory = bigImg.replaceAll(filename, "");
			ext = filename.substring(filename.lastIndexOf("."));
//			Log.i("intent ext",ext);
			filename = filename.substring(0, filename.lastIndexOf("."));
		}catch(StringIndexOutOfBoundsException e){
			e.printStackTrace();
		}
		String thumbImg = "";
		if(filename.contains("no_photo")){
//			Log.i("intent absent",thumbImg);
			thumbImg = filename+ext;
		}else{
//			Log.i("intent present ",thumbImg);
			thumbImg = filename+ext;
			thumbImg = filename+"_thumb"+ext;
		}
		return directory + thumbImg;
	}
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		final View v = inflater.inflate(R.layout.activity_klounge_group_member, container, false);
		final GridView mGridView = (GridView) v.findViewById(R.id.klounge_group_member_list);
		final TextView groupName = (TextView)v.findViewById(R.id.klounge_group_name);
		final TextView groupNum = (TextView)v.findViewById(R.id.klounge_group_total_number);
		String group_name = getActivity().getIntent().getStringExtra("group_name");
		groupName.setText(group_name);
		
//		TableRow.LayoutParams params = new TableRow.LayoutParams(1,1, 0.1f);
//		groupName.setLayoutParams(params);
		groupNum.setText(getActivity().getIntent().getIntExtra("group_total_number", 0) + "명");

		mGridView.setAdapter(mAdapter);
		mGridView.setOnItemClickListener(this);
		mGridView.setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView absListView, int scrollState) {
				// Pause fetcher to ensure smoother scrolling when flinging
				if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
					mImageFetcher.setPauseWork(true);
				} else {
					mImageFetcher.setPauseWork(false);
				}
			}

			@Override
			public void onScroll(AbsListView absListView, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {
			}
		});

		// This listener is used to get the final width of the GridView and then calculate the
		// number of columns and the width of each column. The width of each column is variable
		// as the GridView has stretchMode=columnWidth. The column width is used to set the height
		// of each view so we get nice square thumbnails.
		mGridView.getViewTreeObserver().addOnGlobalLayoutListener(
				new ViewTreeObserver.OnGlobalLayoutListener() {
					@Override
					public void onGlobalLayout() {
						if (mAdapter.getNumColumns() == 0) {
							final int numColumns = (int) Math.floor(
									mGridView.getWidth() / (mImageThumbSize + mImageThumbSpacing));
							if (numColumns > 0) {
								final int columnWidth =
										(mGridView.getWidth() / numColumns) - mImageThumbSpacing;
								mAdapter.setNumColumns(numColumns);
								mAdapter.setItemHeight(columnWidth);
								if (AppConfig.DEBUG) {
									Log.d(TAG, "onCreateView - numColumns set to " + numColumns);
								}
							}
						}
					}
				});

		return v;
	}
	void createThreadAndDialog() {
		pd = ProgressDialog.show(getActivity(), "", getResources().getText(R.string.msg_contents), true);
		Thread thread = new Thread(new Runnable() {
			public void run() {

				KLoungeGroupRequest krgr = new KLoungeGroupRequest();
//				Log.i(TAG, "id: "+ AppUser.user_id +"/"+ group_id);
				gmList= krgr.getGroupMemberList(AppUser.user_id, group_id);

				handler.sendEmptyMessage(0);
			}
		});

		thread.start();
		try {
			Thread.sleep(300);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			// View
			if(gmList.size()==0) {
				if(pd!=null)pd.dismiss();
				Toast.makeText(getActivity(), getResources().getText(R.string.sorry_empty_list), Toast.LENGTH_LONG).show();
			}else if(gmList.size() < 0){
				if(pd!=null)pd.dismiss();
				Toast.makeText(getActivity(), getResources().getText(R.string.sorry_text), Toast.LENGTH_SHORT).show();
				Log.i(TAG,"Loading Message List Error "+this.getClass().toString());
				getActivity().finish();
			}else{
				//				setGroupMemberList(gmList);
			}
			if(pd!=null)pd.dismiss();
		}
	};

	@Override
	public void onResume() {
		super.onResume();
		mImageFetcher.setExitTasksEarly(false);
		mAdapter.notifyDataSetChanged();
	}

	@Override
	public void onPause() {
		super.onPause();
		mImageFetcher.setPauseWork(false);
		mImageFetcher.setExitTasksEarly(true);
		mImageFetcher.flushCache();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mImageFetcher.closeCache();
	}

	@TargetApi(16)
	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		String user_id = String.valueOf(((TextView)v.findViewById(R.id.grid_item_userId)).getText());
		String user_name = String.valueOf(((TextView)v.findViewById(R.id.grid_item_label)).getText());
		String imageUrl = String.valueOf(((TextView)v.findViewById(R.id.grid_item_uri)).getText());
		String user_photo = imageUrl;
		final Intent intent = new Intent(getActivity(), PersonalLounge.class);
		if(gmList != null){
			intent.putExtra("puser_id", user_id);
			intent.putExtra("puser_name", user_name);
			intent.putExtra("puser_photo", user_photo);
			
			startActivity(intent);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.main_menu, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.clear_cache:
			mImageFetcher.clearCache();
			Toast.makeText(getActivity(), R.string.clear_cache_complete_toast,
					Toast.LENGTH_SHORT).show();
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	/**
	 * The main adapter that backs the GridView. This is fairly standard except the number of
	 * columns in the GridView is used to create a fake top row of empty views as we use a
	 * transparent ActionBar and don't want the real top row of images to start off covered by it.
	 */
	private class ImageAdapter extends BaseAdapter {

		private final Context mContext;
		private int mItemHeight = 0;
		private int mNumColumns = 0;
		private int mActionBarHeight = 0;
		private GridView.LayoutParams mImageViewLayoutParams;
		private List<GroupMemberInfo> memberList;
		public ImageAdapter(Context context, List<GroupMemberInfo> adapter_gmlist) {
			super();
			mContext = context;
			mImageViewLayoutParams = new GridView.LayoutParams(
					LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
			// Calculate ActionBar height
			TypedValue tv = new TypedValue();
			if (context.getTheme().resolveAttribute(
					android.R.attr.actionBarSize, tv, true)) {
				mActionBarHeight = TypedValue.complexToDimensionPixelSize(
						tv.data, context.getResources().getDisplayMetrics());
			}
//			Log.i(TAG,"size: "+ adapter_gmlist.size());
			memberList = adapter_gmlist;
		}

		@Override
		public int getCount() {
			int count =0;
			try{
				count = memberList.size();
			}catch(NullPointerException n){
				n.printStackTrace();
			}
			return count;
		}

		@Override
		public Object getItem(int position) {
			return memberList.get(position);//position < mNumColumns ? null : images.imageThumbUrls[position - mNumColumns];
		}

		@Override
		public long getItemId(int position) {
			int id = memberList.get(position).getId();
			return id;//position < mNumColumns ? 0 : position - mNumColumns;
		}

		@Override
		public boolean hasStableIds() {
			return true;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup container) {
			ViewHolder holder;
			View vi = convertView;
			LayoutInflater inflator = null;
			View gridView;
			
			// Now handle the main ImageView thumbnails
			if (convertView == null) { // if it's not recycled, instantiate and initialize
				inflator = (LayoutInflater)mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				gridView = inflator.inflate(R.layout.one_member, null);
			} else { // Otherwise re-use the converted view
				gridView = convertView;
			}
			
			final GroupMemberInfo gmi = memberList.get(position);
			if(gmi != null){
				//Toast.makeText(mContext, gmi.getMember() , Toast.LENGTH_SHORT).show();
				gridView.setTag(gmi);
			} else {
				Toast.makeText(mContext, "Failed to load", Toast.LENGTH_SHORT).show();
			}
			GroupMemberInfo loadgmi = (GroupMemberInfo)gridView.getTag();
			final ImageView imageView = (ImageView)gridView.findViewById(R.id.grid_item_image);
			final TextView textView = (TextView)gridView.findViewById(R.id.grid_item_label);
			textView.setText(loadgmi.getMember());
			
			TextView hiddenView = (TextView)gridView.findViewById(R.id.grid_item_userId);
			hiddenView.setText(String.valueOf(gmi.getUserId()));
			TextView uri_view = (TextView)gridView.findViewById(R.id.grid_item_uri);
			uri_view.setText(String.valueOf(gmi.getPhoto()));
			
			mImageFetcher.loadImage(images.imageThumbUrls[position], imageView);
			return gridView;
		}
		
		/**
		 * Sets the item height. Useful for when we know the column width so the height can be set
		 * to match.
		 *
		 * @param height
		 */
		public void setItemHeight(int height) {
			if (height == mItemHeight) {
				return;
			}
			mItemHeight = height;
			mImageViewLayoutParams =
					new GridView.LayoutParams(LayoutParams.MATCH_PARENT, mItemHeight);
			mImageFetcher.setImageSize(height);
			notifyDataSetChanged();
		}

		public void setNumColumns(int numColumns) {
			mNumColumns = numColumns;
		}

		public int getNumColumns() {
			return mNumColumns;
		}
	}
	static class ViewHolder{
		ImageView imgPhoto;
		TextView textName;
	}
}
