package com.alvaroMM.alvarogcmclient;

import com.alvaroMM.gcmUtils.AlvaroGCM;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private final static int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
	private final static String TAG = "MainActivity";
	private static final String SENDER_ID = "761171306029";
	//caca
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		if(checkPlayServices()){
		
			TextView tReceived = (TextView) findViewById(R.id.tReceived);
			Button bSend = (Button) findViewById(R.id.bSend);
			final EditText eSend = (EditText) findViewById(R.id.eSend);
			
			final AlvaroGCM gcmHelper = new AlvaroGCM(SENDER_ID, this);
			
			bSend.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View arg0) {
					String mens = eSend.getText().toString();
					gcmHelper.sendGCMMessage(mens);
				}
			});
			
		}
		else{
			// Avisa si no encontró los Google Play Services
			Toast.makeText(getApplicationContext(), "Google Play Services no disponibles!", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		checkPlayServices();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	
	private boolean checkPlayServices() {
	    int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
	    if (resultCode != ConnectionResult.SUCCESS) {
	        if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
	            GooglePlayServicesUtil.getErrorDialog(resultCode, this,
	                    PLAY_SERVICES_RESOLUTION_REQUEST).show();
	        } else {
	            Log.i(TAG, "This device is not supported.");
	            finish();
	        }
	        return false;
	    }
	    return true;
	}
	
}
