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

import kr.co.ktech.cse.AppConfig;
import kr.co.ktech.cse.bitmapfun.util.Utils;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;

/**
 * Simple FragmentActivity to hold the main {@link ImageGridFragment} and not much else.
 */
public class ImageGridActivity extends FragmentActivity {
	private static final String TAG = "ImageGridActivity";
	int group_id;
	String group_name;
	int group_total_number;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		if (AppConfig.DEBUG) {
			Utils.enableStrictMode();
		}
		
		super.onCreate(savedInstanceState);
		
		Intent intent = getIntent();
		group_id = intent.getIntExtra("group_id", 0);
		group_name = intent.getStringExtra("group_name");
		group_total_number = intent.getIntExtra("group_total_number", 0);
		
		Log.i("intent", group_id+"/"+group_name+"/"+group_total_number);
		
		if (getSupportFragmentManager().findFragmentByTag(TAG) == null) {
			final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.add(android.R.id.content, new ImageGridFragment(), TAG);
			ft.commit();
		}
		
	}

	
}
