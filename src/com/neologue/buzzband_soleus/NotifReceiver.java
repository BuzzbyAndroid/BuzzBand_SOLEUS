package com.neologue.buzzband_soleus;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class NotifReceiver extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context context, Intent intent) {
    	//Log.d("BuzzBand","Enter notifreceiver");
    	String appname = intent.getStringExtra("AppName");
    	String pkgname = intent.getStringExtra("PackageName");
    	String ticker = intent.getStringExtra("TickerText");
    	String extracted = intent.getStringExtra("ExtractedText");
    	String isdefaultsound = intent.getStringExtra("IsDefaultSound");
    	String soundtype = intent.getStringExtra("SoundType");
    	String soundinfo = intent.getStringExtra("SoundInfo");
    	Notification notif = null;
    	try {
        	notif = intent.getParcelableExtra("Notification"); // in case greater details can be used
    	} catch (Exception e) {
        	//Log.d("BuzzBand","notification missing from app="+appname+" ticker="+ticker);
    	}
    	//Log.d("BuzzBand","app="+appname+" ticker="+ticker+" pkg="+pkgname+" ext="
    	//		+extracted+" soundtype="+soundtype+" soundinfo="+soundinfo
    	//		+" isdefaultsound="+isdefaultsound);
    	if (isdefaultsound.compareTo("1") == 0) {
	    	//Log.d("BuzzBand","is defaultsnd");
    		if (!notifserver.allow_defaultsnd) {
    	    	//Log.d("BuzzBand","Exit notifreceiver defaultsnd disabled");
    			return;
    		}
    	}
    	switch (soundtype) {
    	case notifserver.NO_SOUND:
	    	//Log.d("BuzzBand","is nosnd");
    		if (!notifserver.allow_nosnd) {
    	    	//Log.d("BuzzBand","Exit notifreceiver nosnd disabled");
    			return;
    		}
    		break;
    	case notifserver.BUILTIN_SOUND:
    		if (!notifserver.allow_builtinsnd) {
    			return;
    		}
    		break;
    	case notifserver.NOTIF_SOUND:
    		if (!notifserver.allow_notifsnd) {
    			return;
    		}
    		break;
    	case notifserver.FILE_SOUND:
    		if (!notifserver.allow_filesnd) {
    			return;
    		}
    		break;
    	}
    	notifserver.lebuzz(appname, ticker, extracted);
    	//Log.d("BuzzBand","Exit notifreceiver");
	}
}
