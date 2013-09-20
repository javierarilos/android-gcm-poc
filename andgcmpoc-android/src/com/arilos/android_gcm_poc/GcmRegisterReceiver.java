package com.arilos.android_gcm_poc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;

public class GcmRegisterReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Bundle extras = intent.getExtras();
		String regId = extras.getString("registration_id");
		if (regId != null && !regId.equals("")) {
			System.out.println(">>>>>>>>>>>>>> RECEIVED REGISTRATION ID: "
					+ regId);
			PocUtil util = PocUtil.getInstance(null);
			util.storeRegistrationId(regId);
			Integer backendResult = util.finishRegister();
			NotificationsActivity.broadcastRegisterStatusChangedMessage(
					context, backendResult);
		}

	}
}
