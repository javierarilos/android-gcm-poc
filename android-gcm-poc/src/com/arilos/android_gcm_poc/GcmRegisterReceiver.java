package com.arilos.android_gcm_poc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class GcmRegisterReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		String regId = intent.getExtras().getString("registration_id");
		if (regId != null && !regId.equals("")) {
			System.out.println(">>>>>>>>>>>>>> RECEIVED REGISTRATION ID: "
					+ regId);
			PocUtil util = PocUtil.getInstance(null);
			util.storeRegistrationId(regId);
			util.finishRegister();

		}

	}

}
