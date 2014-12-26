package com.neologue.buzzband_soleus;

import java.io.File;
import java.io.FileInputStream;

import android.app.Notification;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.service.notification.StatusBarNotification;

/*
 * Listens for notficiations passed on from the Buzzby app.
 * The 'Pass...' checkbox in the Buzz tab of Buzzby must be checked.
 * The 'Allow...' checkbox must also be checked.
 * Only notifications which are passed on are those which escape
 * being blocked by a buzz rule.
 * 
 * This listener picks up the information and sends it to the band
 * via the 'lebuzz' method.
 */
public class NotifReceiver extends BroadcastReceiver {
	
	/*
	 * Turns the byte array of serialised ActionEntry objects
	 * into a 2D array of bytes ready for deserialisation.
	 */
	public static byte [][] decodeonelevel(byte [] bae) {
		int len = (bae[0]&0xff)*128+(bae[1]&0xff);
    	////Log.d("BuzzWear", "dimensions are="+Integer.toString(len)+ " totallen="+Integer.toString(bae.length));
		byte [][] res = new byte[len][];
		int offset = 2;
		int [] blen = new int[len];
		for (int i=0; i<len; i++) {
			blen[i] = (bae[offset]&0xff)*128+(bae[offset+1]&0xff);
        	////Log.d("BuzzWear", "dimension="+Integer.toString(i)+ " len="+Integer.toString(blen[i]));
			offset += 2;
		}
		for (int i=0; i<len; i++) {
			res[i] = new byte[blen[i]];
			for (int j=0; j<blen[i]; j++) {
				res[i][j] = bae[offset];
				offset += 1;
			}
		}
		return res;
	}
    
	@Override
	public void onReceive(Context context, Intent intent) {
		// extract all available parameters fron Buzzby even though many may not be used for this band
    	//Log.d("BuzzBand","Enter notifreceiver");
    	String appname = intent.getStringExtra("AppName");
    	String pkgname = intent.getStringExtra("PackageName");
    	String ticker = intent.getStringExtra("TickerText");
    	String extracted = intent.getStringExtra("ExtractedText");
    	String isdefaultsound = intent.getStringExtra("IsDefaultSound");
    	String soundtype = intent.getStringExtra("SoundType");
    	String soundinfo = intent.getStringExtra("SoundInfo");
    	String link1 = intent.getStringExtra("Link1"); // link to website in ticker may be null or ""
    	String link2 = intent.getStringExtra("Link2"); // link to website in ticker may be null or ""
    	String tag = intent.getStringExtra("SBN_Tag"); // from the statusbar notification
    	byte [] bae = intent.getByteArrayExtra("ActionEntries"); // serialised version of the notifications action list stored in an array of ActionEntry objects
    	ActionEntry [] ae = null;
    	if (bae != null) {
        	byte [][] baes = decodeonelevel(bae);
        	ae = new ActionEntry[baes.length];
        	for (int i=0; i<baes.length; i++) {
        		ae[i] = ActionEntry.DeSerialise(baes[i]);
        	}

    	}
    	String key = intent.getStringExtra("SBN_Key"); // from the statusbar notification (only in Android 5.0 and above)
    	int notifid = intent.getIntExtra("SBN_Id", 0); // from the statusbar notification
    	long rowid = intent.getLongExtra("RowId", 0); // a unique identifier for the notification (rowid of the notification in the Buzzby database)
    	long timestamp = intent.getLongExtra("TimeStamp", 0); // value assigned to the notification in Buzzby when the notification arrived.
    	boolean isongoing = intent.getBooleanExtra("SBN_Ongoing", false); // from the statusbar notification
    	boolean phoneautodismiss = intent.getBooleanExtra("AutoDismiss", false); // from the Buzz tab checkbox
    	String bfname = intent.getStringExtra("LargeIcon"); // the large icon is too big to include in the Extra data, so just filename is sent
    	//Log.d("BuzzBand","got large icon file name="+bfname); // too big to pass in the intent so only the full file name is passed
    	byte [] blargeicon = null;
    	if (bfname != null && bfname.length() > 1) {
    		File f = new File (bfname);
    		// Buzzby leaves the largicon as bytes in a file. The receiving program should remove the file after picking it up.
    		if (f.exists()) {
    	    	//Log.d("BuzzBand","got large icon file OK");
    	    	try {
    	    		FileInputStream fs = new FileInputStream(f);
        			blargeicon = new byte[(int) f.length()];
        			fs.read(blargeicon);
        			fs.close();
        			f.delete(); // buzzband must delete each largeicon file otherwise they will pile up in te Buzzby folder
    	    	} catch (Exception e) {
        	    	//Log.d("BuzzBand","large icon file pickup failed="+e.getMessage());
    	    	}
    		}
    	}
    	// parameter extraction complete
    	//Log.d("BuzzBand","app="+appname+" ticker="+ticker+" pkg="+pkgname+" ext="
    	//		+extracted+" soundtype="+soundtype+" soundinfo="+soundinfo
    	//		+" isdefaultsound="+isdefaultsound);
    	// Filter the notifications if the user wants to reject any based on the selected sound
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
    	// the SOLEUS band can only do a small amount of text, so most of the other
    	//  information is not passed to the code which buzzes the band.
    	notifserver.lebuzz(appname, ticker, extracted);
    	//Log.d("BuzzBand","Exit notifreceiver");
	}
}
