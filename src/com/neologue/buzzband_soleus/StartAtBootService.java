package com.neologue.buzzband_soleus;

import java.io.File;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

public class StartAtBootService extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
	        try {
	           	Intent i = new Intent(context, notifserver.class);
	           	context.startService(i);
	        } catch (Exception e) {
	        }
		}
	}
}
