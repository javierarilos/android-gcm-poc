package com.arilos.android_gcm_poc;

import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.concurrent.ExecutionException;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.gcm.GoogleCloudMessaging;

public class PocUtil {
	private static final String PROPERTY_REG_ID = "com.arilos.android_gcm_poc.REG_ID";
	private static final String PROPERTY_APP_VERSION = "com.arilos.android_gcm_poc.APP_VERSION";
	private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	protected static final String PROJECT_NUMBER = "439693372930";
	private static final String PROPERTY_RECVR_ID = "com.arilos.android_gcm_poc.RECVR_ID";
	private static final String PROPERTY_DOMAIN = "com.arilos.android_gcm_poc.DOMAIN";
	private Context context;
	private static PocUtil instance;

	public static PocUtil getInstance(Context context) {
		if (instance == null) {
			if (context == null)
				throw new InvalidParameterException();
			instance = new PocUtil();
			instance.context = context;
		}
		return instance;
	}

	private PocUtil() {
	}

	public AsyncTask<String, Void, Integer> backgroundPostRegistrationToBackend(
			String recvr_id, String domain, String registrationId) {
		return new AsyncTask<String, Void, Integer>() {
			@Override
			protected Integer doInBackground(String... args) {
				// Create a new HttpClient and Post Header

				if (args.length != 3)
					throw new IllegalArgumentException(
							"Expected three String arguments, in order: recvr_id, domain, registrationId");

				String recvr_id = args[0];
				String domain = args[1];
				String registrationId = args[2];

				return postRegistrationToBackend(recvr_id, domain,
						registrationId);
			}
		}.execute(recvr_id, domain, registrationId);
	}

	private Integer postRegistrationToBackend(String recvr_id, String domain,
			String registrationId) {
		try {
			HttpClient httpclient = new DefaultHttpClient();
			HttpPost httppost = new HttpPost(
					String.format(
							"http://androidgcmpoc-corralito.rhcloud.com/register/%s/%s/%s",
							recvr_id, domain, registrationId));
			StringEntity entity = new StringEntity(String.format(
					"{\"user\":\"%s\",\"tag\":\"%s\", \"body\":\"%s\"}",
					recvr_id, domain, registrationId));
			httppost.setEntity(entity);

			// Execute HTTP Post Request
			HttpResponse response = httpclient.execute(httppost);
			System.out.println("RESPONSE ::: "
					+ response.getStatusLine().getStatusCode()
					+ EntityUtils.toString(response.getEntity()));
			return response.getStatusLine().getStatusCode();
		} catch (ClientProtocolException e) {
			System.out.println("CLIENT PROTOCOL EXCEPTION ::: " + e);
		} catch (IOException e) {
			System.out.println("ioEXCEPTION ::: " + e);
		}
		return -1;
	}

	/**
	 * Stores the registration ID and app versionCode in the application's
	 * {@code SharedPreferences}.
	 * 
	 * @param context
	 *            application's context.
	 * @param regId
	 *            registration ID
	 */
	public void storeRegistrationId(String regId) {
		final SharedPreferences prefs = getGCMPreferences();
		int appVersion = getAppVersion(context);
		System.out.println("Saving regId on app version " + appVersion);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_REG_ID, regId);
		editor.putInt(PROPERTY_APP_VERSION, appVersion);
		editor.commit();
	}

	public void storeSprayerId(String recvr_id, String domain) {
		final SharedPreferences prefs = getGCMPreferences();
		SharedPreferences.Editor editor = prefs.edit();
		editor.putString(PROPERTY_RECVR_ID, recvr_id);
		editor.putString(PROPERTY_DOMAIN, domain);
		editor.commit();
	}

	/**
	 * Gets the current registration ID for application on GCM service.
	 * <p>
	 * If result is empty, the app needs to register.
	 * 
	 * @return registration ID, or empty string if there is no existing
	 *         registration ID.
	 */
	public String getRegistrationId() {
		final SharedPreferences prefs = getGCMPreferences();
		String registrationId = prefs.getString(PROPERTY_REG_ID, "");
		if (registrationId == null || registrationId.equals("")) {
			System.out.println("Registration not found.");
			return "";
		}
		// Check if app was updated; if so, it must clear the registration ID
		// since the existing regID is not guaranteed to work with the new
		// app version.
		int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION,
				Integer.MIN_VALUE);
		int currentVersion = getAppVersion(context);
		if (registeredVersion != currentVersion) {
			System.out.println("App version changed.");
			return "";
		}
		return registrationId;
	}

	public String getReceiverId() {
		final SharedPreferences prefs = getGCMPreferences();
		return prefs.getString(PROPERTY_RECVR_ID, "");
	}

	public String getDomain() {
		final SharedPreferences prefs = getGCMPreferences();
		return prefs.getString(PROPERTY_DOMAIN, "");
	}

	private SharedPreferences getGCMPreferences() {
		// This sample app persists the registration ID in shared preferences,
		// but how we store the regID in your app is up to us.
		return context.getSharedPreferences(MainActivity.class.getSimpleName(),
				Context.MODE_PRIVATE);
	}

	/**
	 * @return Application's version code from the {@code PackageManager}.
	 */
	private static int getAppVersion(Context context) {
		try {
			PackageInfo packageInfo = context.getPackageManager()
					.getPackageInfo(context.getPackageName(), 0);
			return packageInfo.versionCode;
		} catch (NameNotFoundException e) {
			// should never happen
			throw new RuntimeException("Could not get package name: " + e);
		}
	}

	/**
	 * Check the device to make sure it has the Google Play Services APK. If it
	 * doesn't, display a dialog that allows users to download the APK from the
	 * Google Play Store or enable it in the device's system settings.
	 */
	public boolean checkPlayServices(Activity activity) {
		int resultCode = GooglePlayServicesUtil
				.isGooglePlayServicesAvailable(activity);
		if (resultCode != ConnectionResult.SUCCESS) {
			if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
				GooglePlayServicesUtil.getErrorDialog(resultCode, activity,
						PLAY_SERVICES_RESOLUTION_REQUEST).show();
			} else {
				System.out.println("This device is not supported.");
				activity.finish();
			}
			return false;
		}
		return true;
	}

	public AsyncTask<Void, Void, String> backgroundRegisterToGoogle() {
		return new AsyncTask<Void, Void, String>() {
			@Override
			protected String doInBackground(Void... params) {
				return registerToGoogle();
			}

		}.execute(null, null, null);
	}

	private String registerToGoogle() {
		String msg = "";
		try {
			GoogleCloudMessaging gcm = GoogleCloudMessaging
					.getInstance(context);
			long start = System.currentTimeMillis();
			String registrationId = gcm.register(PROJECT_NUMBER);
			long end = System.currentTimeMillis();
			System.out.println(String.format(
					"Device registered in %d ms, registration ID=%s", end
							- start, registrationId));
			return registrationId;
		} catch (IOException ex) {
			msg = "Error : REGISTERING TO GOOGLE" + ex.getMessage();
			System.out.println(msg + ex.getStackTrace());
			// TODO: If there is an error, don't just keep trying to
			// register. Require the user to click a button again, or
			// perform exponential back-off.
		}
		return null;
	}

	public Integer register(String recvr_id, String domain) {
		// tries to assure registration to google and our backend.
		Integer backendResult = -1;
		storeSprayerId(recvr_id, domain);

		String registrationId = getRegistrationId();
		if (registrationId == null || registrationId.length() == 0) {
			// unregistered. do the register process. 1st register to google
			registrationId = registerToGoogle();
			if (registrationId != null && registrationId.length() > 0) {
				// register to google was successful, update registration
				// id.
				storeRegistrationId(registrationId);
			} else {
				System.out
						.println(">> register was not successful to google: googleRegId : "
								+ registrationId);
			}
		}

		if (registrationId != null && registrationId.length() > 0) {
			// registration to google was successful, finish register.
			backendResult = postRegistrationToBackend(recvr_id, domain,
					registrationId);
		}
		return backendResult;
	}

	public Integer finishRegister() {
		// restores stored registrationId, recvr_id and domain and posts them to
		// our backend.
		String registrationId = getRegistrationId();
		String recvr_id = getReceiverId();
		String domain = getDomain();

		AsyncTask<String, Void, Integer> rb = backgroundPostRegistrationToBackend(
				recvr_id, domain, registrationId);
		Integer backendResult = -1;
		try {
			backendResult = rb.get();
			if (backendResult != 200)
				System.out
						.println(">> register was not successful to backend: googleRegId : \""
								+ registrationId
								+ "\" backendResult : \""
								+ backendResult + "\"");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return backendResult;
	}
}
