package com.alvaroMM.gcmUtils;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import com.alvaroMM.alvarogcmclient.MainActivity;
import com.google.android.gms.gcm.GoogleCloudMessaging;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

public class AlvaroGCM {

	private static final String TAG = "AlvaroGCM";
	private static final String PROPERTY_REG_ID = "registration_id";
	private static final String PROPERTY_APP_VERSION = "appVersion";
	private String SENDER_ID;
	private MainActivity MAIN_ACTIVITY;
	private Context CONTEXT;
	
	GoogleCloudMessaging gcm;
	String regid;
	AtomicInteger msgId = new AtomicInteger();
	
	public AlvaroGCM (String SENDER_ID, MainActivity mainActivity){
		this.SENDER_ID = SENDER_ID;
		this.MAIN_ACTIVITY = mainActivity;
		this.CONTEXT = mainActivity.getApplicationContext();
		this.gcm = GoogleCloudMessaging.getInstance(CONTEXT);
		this.regid = getRegistrationId(CONTEXT);
		if(regid.isEmpty()){
			registerInBackground();
		}
	}
	
	/**
	 * Gets the current registration ID for application on GCM service.
	 * <p>
	 * If result is empty, the app needs to register.
	 *
	 * @return registration ID, or empty string if there is no existing
	 *         registration ID.
	 */
	private String getRegistrationId(Context context) {
	    final SharedPreferences prefs = getGCMPreferences(context);
	    String registrationId = prefs.getString(PROPERTY_REG_ID, "");
	    if (registrationId.isEmpty()) {
	        Log.i(TAG, "Registration not found.");
	        return "";
	    }
	    // Check if app was updated; if so, it must clear the registration ID
	    // since the existing regID is not guaranteed to work with the new
	    // app version.
	    int registeredVersion = prefs.getInt(PROPERTY_APP_VERSION, Integer.MIN_VALUE);
	    int currentVersion = getAppVersion(context);
	    if (registeredVersion != currentVersion) {
	        Log.i(TAG, "App version changed.");
	        return "";
	    }
	    return registrationId;
	}
	/**
	 * @return Application's {@code SharedPreferences}.
	 */
	private SharedPreferences getGCMPreferences(Context context) {
	    // This sample app persists the registration ID in shared preferences, but
	    // how you store the regID in your app is up to you.
	    return CONTEXT.getSharedPreferences(MAIN_ACTIVITY.getClass().getSimpleName(),
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
	 * Registers the application with GCM servers asynchronously.
	 * <p>
	 * Stores the registration ID and app versionCode in the application's
	 * shared preferences.
	 */
	private void registerInBackground() {
	    new AsyncTask(){

			@Override
			protected String doInBackground(Object... arg0) {
				String msg = "";
	            try {
	                if (gcm == null) {
	                    gcm = GoogleCloudMessaging.getInstance(CONTEXT);
	                }
	                regid = gcm.register(SENDER_ID);
	                msg = "Device registered, registration ID=" + regid;

	                // You should send the registration ID to your server over HTTP,
	                // so it can use GCM/HTTP or CCS to send messages to your app.
	                // The request to your server should be authenticated if your app
	                // is using accounts.
	                sendRegistrationIdToBackend();

	                // For this demo: we don't need to send it because the device
	                // will send upstream messages to a server that echo back the
	                // message using the 'from' address in the message.

	                // Persist the regID - no need to register again.
	                storeRegistrationId(CONTEXT, regid);
	            } catch (IOException ex) {
	                msg = "Error :" + ex.getMessage();
	                // If there is an error, don't just keep trying to register.
	                // Require the user to click a button again, or perform
	                // exponential back-off.
	            }
	            return msg;
			}
	    }.execute(null, null, null);
	}
	
	/**
	 * Sends the registration ID to your server over HTTP, so it can use GCM/HTTP
	 * or CCS to send messages to your app. Not needed for this demo since the
	 * device sends upstream messages to a server that echoes back the message
	 * using the 'from' address in the message.
	 */
	private void sendRegistrationIdToBackend() {
	    // Your implementation here.
	}
	
	/**
	 * Stores the registration ID and app versionCode in the application's
	 * {@code SharedPreferences}.
	 *
	 * @param context application's context.
	 * @param regId registration ID
	 */
	private void storeRegistrationId(Context context, String regId) {
	    final SharedPreferences prefs = getGCMPreferences(context);
	    int appVersion = getAppVersion(context);
	    Log.i(TAG, "Saving regId on app version " + appVersion);
	    SharedPreferences.Editor editor = prefs.edit();
	    editor.putString(PROPERTY_REG_ID, regId);
	    editor.putInt(PROPERTY_APP_VERSION, appVersion);
	    editor.commit();
	}
	
	public void sendGCMMessage(final String message){
		new AsyncTask() {
			@Override
			protected String doInBackground(Object... params) {
				String msg = "";
			    try {
			        Bundle data = new Bundle();
			            data.putString("my_message", message);
			            data.putString("my_action",
			                    "com.google.android.gcm.demo.app.ECHO_NOW");
			            String id = Integer.toString(msgId.incrementAndGet());
			            gcm.send(SENDER_ID + "@gcm.googleapis.com", id, data);
			            msg = "Sent message";
			    } catch (IOException ex) {
			        msg = "Error :" + ex.getMessage();
			    }
			    return msg;
			}

			@Override
			protected void onPostExecute(Object result) {
				// TODO Auto-generated method stub
				Toast.makeText(CONTEXT, "Mensaje enviado: "+message, Toast.LENGTH_SHORT).show();
			}
	    }.execute(null, null, null);     
	}	
}
