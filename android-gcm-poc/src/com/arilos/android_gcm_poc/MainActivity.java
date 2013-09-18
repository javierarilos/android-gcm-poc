package com.arilos.android_gcm_poc;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;

//http://developer.android.com/google/gcm/gs.html#libs
//sending a gcm message from command line:
//curl --header "Authorization:key=AIzaSyAU2xRA8uQLVcH90RRLjay98QCQbybBAzw" --header "Content-Type:application/json" "https://android.googleapis.com/gcm/send" --data '{"data": { "hello": "android from sprayer 27" }, "registration_ids":["APA91bFD5sdYO4mSNBWwWT_hotMit-NAfcSQiqSE-TW8sBHASGKNLrwA8YBMaBnvLA8w3vaeylYeennnvjISSodzcnTMfv3zXiSpsjRZXEB80ErpoI-Y3LI7OzQJ9_TOFIXkPz_56jdm7YQM2D8_zK6gvowSECdZ6dI0e2bZoeJ9EpHh2ROLPJI"]}'{"multicast_id":4965462207190267259,"success":1,"failure":0,"canonical_ids":0,"results":[{"message_id":"0:1379512056945380%e56d3d5df9fd7ecd"}]}

public class MainActivity extends Activity {

	public static final String EXTRA_MENSAJE = "com.arilos.android_gcm_poc.MENSAJE";
	protected static final String PROJECT_NUMBER = "439693372930";
	StringBuilder text = new StringBuilder("");
	private String registrationId = null;
	private static MainActivity instance = null;
	private PocUtil util = PocUtil.getInstance(this);

	public static MainActivity getInstance() {
		return instance;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// TODO: revisit getInstance use: probably this is an anti-pattern. At
		// least it is ugly.
		instance = this;
		setContentView(R.layout.activity_main);
		if (!util.checkPlayServices(this))
			System.out
					.println(">>> THIS SHOULD NEVER HAPPEN. should be handled by app correctly.");
	}

	@Override
	protected void onResume() {
		super.onResume();
		// http://developer.android.com/google/play-services/setup.html
		if (!util.checkPlayServices(this))
			System.out
					.println(">>> THIS SHOULD NEVER HAPPEN. should be handled by app correctly.");
		return;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	public void register(View view) {
		// Registers against google, if successful, registers to our backend.
		// Always launch notifications activity.

		String recvr_id = ((EditText) findViewById(R.id.recvr_id)).getText()
				.toString();
		String domain = ((EditText) findViewById(R.id.domain)).getText()
				.toString();

		Integer backendResult = util.register(recvr_id, domain);

		startNotificationsActivity(backendResult);
	}

	private void startNotificationsActivity(Integer backendResult) {
		Intent intent = new Intent(this, NotificationsActivity.class);
		intent.putExtra(EXTRA_MENSAJE, "" + backendResult);
		startActivity(intent);
	}
}
