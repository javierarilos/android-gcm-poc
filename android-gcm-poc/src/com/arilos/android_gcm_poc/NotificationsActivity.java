package com.arilos.android_gcm_poc;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v4.content.LocalBroadcastManager;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.EditText;

public class NotificationsActivity extends Activity {

	public static final String NOTIFICATION_CONTENT = "notification_content";
	public static final String NOTIFICATION_RECEIVED = "notification_received";

	@SuppressLint("NewApi")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		LocalBroadcastManager.getInstance(this).registerReceiver(
				mNotificationReceiver, new IntentFilter(NOTIFICATION_RECEIVED));
		
		setContentView(R.layout.notifications_activity);

		Intent intent = getIntent();
		String txt_result = intent.getStringExtra(MainActivity.EXTRA_MENSAJE);
		Integer result = Integer.valueOf(txt_result);
		if (result != null && result > 0) {
			Boolean registered_to_sprayer = true;
			Boolean google_status = true;

			((CheckBox) findViewById(R.id.registered_to_sprayer))
					.setChecked(registered_to_sprayer);
			((CheckBox) findViewById(R.id.google_status))
					.setChecked(google_status);
		}

	}

	/**
	 * Set up the {@link android.app.ActionBar}, if the API is available.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
	private void setupActionBar() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
//		LocalBroadcastManager.getInstance(this).registerReceiver(
//				mNotificationReceiver, new IntentFilter(NOTIFICATION_RECEIVED));
	}

	@Override
	public void onPause() {
//		LocalBroadcastManager.getInstance(this).unregisterReceiver(
//				mNotificationReceiver);
		super.onResume();
	}

	// handler for received Intents for the NOTIFICATION_RECEIVED event
	private BroadcastReceiver mNotificationReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			// Extract data included in the Intent
			Bundle message = intent.getBundleExtra(NOTIFICATION_CONTENT);
			((EditText) findViewById(R.id.notifications_edittext))
					.append(">> Received notification: \""
							+ message.getString("hello") + "\"\n");
		}
	};

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// This ID represents the Home or Up button. In the case of this
			// activity, the Up button is shown. Use NavUtils to allow users
			// to navigate up one level in the application structure. For
			// more details, see the Navigation pattern on Android Design:
			//
			// http://developer.android.com/design/patterns/navigation.html#up-vs-back
			//
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

}
