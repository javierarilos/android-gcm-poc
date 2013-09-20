package com.arilos.android_gcm_poc;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;


/**
 * @author javier.arilos
 * 
 * For using this code, you will need:
 * PROJECT_NUMBER see PocUtil.
 * 
 */
public class MainActivity extends Activity {

	StringBuilder text = new StringBuilder("");
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
		NotificationsActivity.startNotificationsActivity(this, -1);
		backgroundRegister(this, recvr_id, domain);
		
	}
	
	public AsyncTask<String, Void, String> backgroundRegister(final Context context, final String recvr_id, final String domain) {
		return new AsyncTask<String, Void, String>() {
			@Override
			protected String doInBackground(String... args) {
				Integer backendResult = util.register(recvr_id, domain);
				NotificationsActivity.broadcastRegisterStatusChangedMessage(context, backendResult);
				return domain;
			}
		}.execute(null, null, null);
	}
}
