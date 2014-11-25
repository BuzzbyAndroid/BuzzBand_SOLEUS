package com.neologue.buzzband_soleus;


import java.io.File;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import android.app.ProgressDialog;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.os.IBinder;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;


public class notifserver extends Service {

	public static File sdroot = Environment.getExternalStorageDirectory();
	public static notifserver srvhandle = null;
	public static BuzzBandMain buzzbandhandle = null;
	public static PackageManager packman = null;
	public static boolean hasble = false;
	public static BluetoothAdapter blueAdapter;
	public static Set<BluetoothDevice> pairedDevices = null;
	public static SharedPreferences settings;
	public static SharedPreferences.Editor editsettings;
	public static View mainview = null;
	public static final String PREFS_NAME = "BuzzBand-SOLEUS";

	public static ProgressDialog pd = null;
	public static BluetoothManager btm = null;
	public static String prefbraceletaddress = "";
	public static ArrayAdapter<CharSequence> bt4adapter;
	public static BluetoothDevice bt4array [] = new BluetoothDevice[30];
	public static BluetoothGatt bt4gatt [] = new BluetoothGatt[30];
	public static String bt4names [] = new String[30];
	public static String bt4address [] = new String[30];
	public static int bt4service [] = new int[30];
	public static boolean bt4isconnected [] = new boolean[30];
	public static BluetoothGattCharacteristic bt4characteristicforwrite [] = new BluetoothGattCharacteristic[30];
	public static final UUID ImmediateAlertServiceUUID = 	UUID.fromString("00001802-0000-1000-8000-00805f9b34fb");
	public static final int GATT_IMMEDIATEALERTSERVICE = 1;
	public static final UUID LinkLossServiceUUID = 	UUID.fromString("00001803-0000-1000-8000-00805f9b34fb");
	public static final int GATT_LINKLOSSSERVICE = 2;
    public static final UUID GATT_ALERTLEVEL_UUID = 		UUID.fromString("00002a06-0000-1000-8000-00805f9b34fb");
	public static final int IMMEDIATEALERT_NOALERT = 0;
	public static final int IMMEDIATEALERT_MIDALERT = 1;
	public static final int IMMEDIATEALERT_HIGHALERT = 2;
	public static int bt4arraycount = 0;
	public static int bt4selected = 0;
	public static Spinner bt4spin = null;
	public static boolean bt4closing = false;
	public static boolean inbuzz = false;

	public static final String BUILTIN_SOUND = "0";
	public static final String NOTIF_SOUND = "1";
	public static final String FILE_SOUND = "2";
	public static final String DEFAULT_SOUND = "3";
	public static final String NO_SOUND = "4";

	public static boolean allow_nosnd = true;
	public static boolean allow_defaultsnd = true;
	public static boolean allow_builtinsnd = true;
	public static boolean allow_notifsnd = true;
	public static boolean allow_filesnd = true;

	public static void getprefs() {
    	//Log.d("BuzzBand","Enter getprefs");
		if (notifserver.settings == null) {
			if (notifserver.srvhandle == null) {
	    		notifserver.settings = notifserver.buzzbandhandle.getSharedPreferences(notifserver.PREFS_NAME,0);
			} else {
	    		notifserver.settings = notifserver.srvhandle.getSharedPreferences(notifserver.PREFS_NAME,0);
			}
    		notifserver.editsettings = notifserver.settings.edit();
		}
		notifserver.prefbraceletaddress = notifserver.settings.getString("MyBTooth4", "");
		notifserver.allow_nosnd = notifserver.settings.getBoolean("AllowNoSound", true);
    	//Log.d("BuzzBand","allow_nosnd="+Boolean.toString(notifserver.allow_nosnd));
		notifserver.allow_defaultsnd = notifserver.settings.getBoolean("AllowDefaultSound", true);
    	//Log.d("BuzzBand","allow_defaultsnd="+Boolean.toString(notifserver.allow_defaultsnd));
		notifserver.allow_builtinsnd = notifserver.settings.getBoolean("AllowBuiltinSound", true);
		notifserver.allow_notifsnd = notifserver.settings.getBoolean("AllowNotifSound", true);
		notifserver.allow_filesnd = notifserver.settings.getBoolean("AllowFileSound", true);
    	//Log.d("BuzzBand","Exit getprefs");
	}
	
	class GetDevDetails extends Thread {
		
		public void run() {
        	//Log.d("BuzzBand","Enter getdevdetails");
			try {
				sleep(10000);
			} catch (Exception e) {
			}
			// get bluetooth details for available devices when started in background at boot time
			BuzzBandMain.findbt4devs(null);
        	//Log.d("BuzzBand","Exit getdevdetails");
		}
	}

	class SendData extends Thread {
		String app;
		String ext;
		String ticker;
		
		public SendData(String papp, String pext, String pticker) {
			app = papp;
			ext = pext;
			ticker = pticker;
		}
		
		public void run() {
    		if (notifserver.inbuzz) {
            	//Log.d("BuzzBand","skip this notification if SendData is currently active");
    			return;
    		}
    		if (notifserver.bt4selected == 0) {
            	//Log.d("BuzzBand","skip this notification if selected index == 0");
    			return;
    		}
    		int idx = notifserver.bt4selected;
    		if (notifserver.bt4characteristicforwrite[idx] == null) {
    			//Log.d("BuzzBand","skip this notification if null characteristic at index="+Integer.toString(idx));
            	return;
    		}
    		int connstate = notifserver.btm.getConnectionState(notifserver.bt4array[idx], BluetoothProfile.GATT);
    		if ( connstate != BluetoothGatt.STATE_CONNECTED) {
    	    	//Log.d("BuzzBand","actually not connected state="+Integer.toString(connstate));
        		notifserver.bt4array[notifserver.bt4selected].connectGatt(notifserver.srvhandle, true, opencallback);
        		return;
    		}
    	try {
    		inbuzz = true;
    		ArrayList blocks = new ArrayList();
    		String sall = ticker; //app+":"+ext;
	    	//Log.d("BuzzBand","message len="+Integer.toString(sall.length())+" text="+sall);
	    	byte[] b1 = sall.getBytes(StandardCharsets.US_ASCII); // soleus band seems to support just ASCII
	    	// remove repeats of 0x3f codes 
	    	byte [] bs = new byte[b1.length];
	    	int bsl = 1;
	    	bs[0] = b1[0];
	    	for (int i=1; i<b1.length; i++) {
	    		if (b1[i-1] == 0x3f && b1[i] == 0x3f) {
	    			continue;
	    		}
	    		bs[bsl] = b1[i];
	    		bsl += 1;
	    	}
    		if (bsl > 17) {
    			// long message needs to be broken into blocks, first block has special format
    			// we also truncate it if necessary to 48 bytes
    			int off = 0;
    			int fulllen = bsl;
    			if (fulllen > 48) {
    				fulllen = 48;
    			}
    			byte b[] = new byte[20];
        		b[0] = (byte) 0xa5;
        		b[1] = (byte)fulllen;
        		int i, j=2;
        		for (i=0; i<18; i++) {
        			b[j] = bs[i];
        			j++;
        		}
        		off += 18;
        		blocks.add(b.clone());
    	    	// now output the rest of the message using a slightly different format
    	    	boolean terminatordone = false;
    	    	if (off < bsl && off < 48) {
    	    		int len = 20;
    	    		if ((bsl-off) < 20) {
    	    			len = (bsl-off);
    	    		}
            		j=0;
            		for (i=0; i<len; i++) {
            			b[j] = bs[off+i];
            			j++;
            		}
            		if (j < 20) {
                		while (j < 19) {
                			b[j] = 0;
                			j++;
                		}
                		b[19] = (byte)0xa4;
                		terminatordone = true;
            		}
            		off += len;
            		blocks.add(b.clone());
    	    	}
    	    	if (!terminatordone && off < bsl && off < 48) {
    	    		int len = 20;
    	    		if ((bsl-off) < 20) {
    	    			len = (bsl-off);
    	    		}
            		j=0;
            		for (i=0; i<len; i++) {
            			b[j] = bs[off+i];
            			j++;
            		}
            		if (j < 20) {
                		while (j < 19) {
                			b[j] = 0;
                			j++;
                		}
                		b[19] = (byte)0xa4;
                		terminatordone = true;
            		}
            		off += len;
            		blocks.add(b.clone());
    	    	}
    	    	if (!terminatordone) {
        	    	//Log.d("BuzzBand","do terminator");
    	    		for (int k=0; k<20; k++) {
    	    			b[k] = 0;
    	    		}
            		b[19] = (byte)0xa4;
            		blocks.add(b.clone());
    	    	}
    		} else {
    			// short message that fits in one 20 byte block has format as below
        		byte [] btext = sall.getBytes();
        		byte [] b = new byte[20];
        		b[0] = (byte) 0xb5;
        		b[1] = (byte)bsl;
        		int i, j=2;
        		for (i=0; i<bsl; i++) {
        			b[j] = bs[i];
        			j++;
        		}
        		while (j < 19) {
        			b[j] = 0;
        			j++;
        		}
        		b[19] = (byte) 0xb4;
        		blocks.add(b.clone());
    		}
    		for (int i=0; i<blocks.size(); i++) {
            	notifserver.bt4characteristicforwrite[idx].setValue((byte [])blocks.get(i));
            	notifserver.bt4characteristicforwrite[idx].setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_NO_RESPONSE);
            	boolean res2 = notifserver.bt4gatt[idx].writeCharacteristic(notifserver.bt4characteristicforwrite[idx]);
    	    	//Log.d("BuzzBand","write="+Boolean.toString(res2));
    		}
        	sleep(100);
        	//Log.d("BuzzBand","exit senddata");
    	} catch (Exception e) {
        	//Log.d("BuzzBand","Exception in senddata="+e.getMessage());
    	}
    	inbuzz = false;
	}
	}

	public static String fext;
	public static String fapp;
	public static String fticker;
	public static int opencount;
	
	public static BluetoothGattCallback opencallback = new BluetoothGattCallback() {
		@Override
		public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
			try {
	        	//Log.d("BuzzBand","Enter Gatt onConnectionStateChange status="+Integer.toString(status)+" newstate="+Integer.toString(newState));
	        	if (status == BluetoothGatt.GATT_SUCCESS) {
	                if (newState == BluetoothProfile.STATE_CONNECTED) {
	                	//Log.d("BuzzBand","soleus is connected now");
	                	notifserver.bt4gatt[notifserver.bt4selected] = gatt;
	                	notifserver.bt4isconnected[notifserver.bt4selected] = true;
	                	SendData x = notifserver.srvhandle.new SendData(fapp, fext, fticker);
	                	x.start();
	                	return;
	                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
	                	//Log.d("BuzzBand","soleus is disconnected now");
	                	notifserver.bt4isconnected[notifserver.bt4selected] = false;
	                	try {
	                    	gatt.close();
	                    	opencount++;
	                    	if (opencount < 5) {
	                        	//Log.d("BuzzBand","retry connect");
	                    		notifserver.bt4array[notifserver.bt4selected].connectGatt(notifserver.srvhandle, true, opencallback);
	                    	}
	                	} catch (Exception e) {
	                    	//Log.d("BuzzBand","Exception in trying close on gatt="+e.getMessage());
	                	}
	                } else if (newState == BluetoothGatt.CONNECTION_PRIORITY_LOW_POWER 
		        			|| newState == BluetoothGatt.CONNECTION_PRIORITY_BALANCED
		        			|| newState == BluetoothGatt.CONNECTION_PRIORITY_HIGH
		        			) {
		            	//Log.d("BuzzBand","soleus might be connected now on priority setting");
		            	if (notifserver.bt4characteristicforwrite[notifserver.bt4selected] == null) {
			            	//Log.d("BuzzBand","soleus needs services");
		            		gatt.discoverServices();
		            	} else {
		                	//Log.d("BuzzBand","soleus try send data");
		                	notifserver.bt4gatt[notifserver.bt4selected] = gatt;
		                	SendData x = notifserver.srvhandle.new SendData(fapp, fext, fticker);
		                	x.start();
		            	}
		        	}
	        	}
	        	//Log.d("BuzzBand","Exit Gatt onConnectionStateChange");
			} catch (Exception e) {
            	//Log.d("BuzzBand","Exception in onconnectionstatechange="+e.getMessage());
			}
		}
	};
	
	public static boolean lebuzz(String app, String tickertext, String ext) {
    	//Log.d("BuzzBand","Enter lebuzz");
    	if (notifserver.prefbraceletaddress == null) {
        	//Log.d("BuzzBand","Exit for null pref lebuzz");
    		return false;
    	}
    	if (notifserver.bt4selected == 0) {
        	//Log.d("BuzzBand","Exit for 0 index selected lebuzz");
    		return false;
    	}
		fext = ext;
		fapp = app;
		fticker = tickertext;
    	if (notifserver.bt4isconnected[notifserver.bt4selected]) {
        	//Log.d("BuzzBand","soleus try to send data on existing connection");
        	SendData x = notifserver.srvhandle.new SendData(fapp, fext, fticker);
        	x.start();
    	} else {
    		//Log.d("BuzzBand","need to start connection first");
    		opencount = 0;
    		notifserver.bt4array[notifserver.bt4selected].connectGatt(notifserver.srvhandle, true, opencallback);
    	}
    	//Log.d("BuzzBand","Exit lebuzz");
		return false;
	}

	public static boolean testble() {
		if (notifserver.btm == null) {
			notifserver.btm = (BluetoothManager) notifserver.srvhandle.getSystemService(BLUETOOTH_SERVICE);
		}
		
	    if (notifserver.packman == null) {
	    	notifserver.packman = notifserver.srvhandle.getPackageManager();
	    }
		if (notifserver.packman.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
			notifserver.hasble = true;
			return true;
		}
		return false;
	}
    
    @Override
    public void onCreate() {
        super.onCreate();
    	if (srvhandle != null) {
            //Log.d("BuzzBand", "Service oncreate started again!");
    		return;
    	}
    	try {
    		srvhandle = this;
            Log.homefolder = getString(R.string.app_name);
            //Log.d("BuzzBand", "Service oncreate top entry");
    		if (notifserver.settings == null) {
        		notifserver.settings = getSharedPreferences(notifserver.PREFS_NAME,0);
        		notifserver.editsettings = notifserver.settings.edit();
        		notifserver.prefbraceletaddress = notifserver.settings.getString("MyBTooth4", "");
    		}
            if (notifserver.buzzbandhandle == null) {
               	notifserver.bt4names[0] = getString(R.string.bt_none);
            	notifserver.bt4arraycount = 1;
            	notifserver.bt4selected = 0;
        		// need to get device details
        		GetDevDetails x = new GetDevDetails();
        		x.start();
            }
            //Log.d("BuzzBand", "Service oncreate done");
    	} catch (Exception e) {
            //Log.d("BuzzBand", "Service oncreate exception="+e.getMessage());
    	}
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    public static void closeconnections() {
		for (int i=0; i<notifserver.bt4arraycount; i++) {
			if (notifserver.bt4isconnected[i]) {
            	//Log.d("BuzzBand",notifserver.bt4names[i]+" is currently connected");
				if (notifserver.bt4gatt[i] != null) {
                	//Log.d("BuzzBand","closing GATT connection on "+notifserver.bt4names[i]);
					try {
    					notifserver.bt4gatt[i].disconnect();
    					notifserver.bt4gatt[i].close();
					} catch (Exception e) {
					}
				}
			}
		}
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
    	try {
            //Log.d("BuzzBand", "Service destroyed");
            closeconnections();
    	} catch (Exception e) {
    	}
    }

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	

}
