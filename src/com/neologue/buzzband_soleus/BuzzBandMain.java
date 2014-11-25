package com.neologue.buzzband_soleus;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class BuzzBandMain extends Activity {

	public static void init_adapter() {
    	//Log.d("BuzzBand", "Enter initadapter");
		notifserver.bt4adapter = new ArrayAdapter <CharSequence> (notifserver.buzzbandhandle, android.R.layout.simple_spinner_item );
		notifserver.bt4adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    	notifserver.bt4spin = (Spinner) notifserver.buzzbandhandle.findViewById(R.id.BT4Spinner);
    	notifserver.bt4spin.setAdapter(notifserver.bt4adapter);
    	for (int i=0; i<notifserver.bt4arraycount; i++) {
    		notifserver.bt4adapter.add(notifserver.bt4names[i]);
    	}
    	notifserver.bt4spin.setSelection(notifserver.bt4selected);
    	notifserver.bt4spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				if (notifserver.bt4selected != position) {
					notifserver.prefbraceletaddress = notifserver.bt4address[position];
					notifserver.bt4selected = position;
					notifserver.editsettings.putString("MyBTooth4", notifserver.prefbraceletaddress);
					notifserver.editsettings.commit();
				}
			}
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}} );
    	//Log.d("BuzzBand", "Eexit initadapter");
	}
	
	public static void init_checks() {
    	//Log.d("BuzzBand", "Enter initchecks");
		CheckBox cno = (CheckBox)notifserver.mainview.findViewById(R.id.cb_nosnd);
		cno.setChecked(!notifserver.allow_nosnd);
    	//Log.d("BuzzBand", "set cb_nosnd="+Boolean.toString(notifserver.allow_nosnd));
		cno.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,	boolean isChecked) {
				notifserver.allow_nosnd = !isChecked;
		    	//Log.d("BuzzBand", "changed cb_nosnd="+Boolean.toString(notifserver.allow_nosnd));
				notifserver.editsettings.putBoolean("AllowNoSound", !isChecked);
				notifserver.editsettings.commit();
		    	//Log.d("BuzzBand", "done change");
			}});
		cno = (CheckBox)notifserver.mainview.findViewById(R.id.cb_defaultsnd);
		cno.setChecked(!notifserver.allow_defaultsnd);
    	//Log.d("BuzzBand", "set cb_defaultsnd="+Boolean.toString(notifserver.allow_defaultsnd));
		cno.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,	boolean isChecked) {
				notifserver.allow_defaultsnd = !isChecked;
		    	//Log.d("BuzzBand", "changed cb_defaultsnd="+Boolean.toString(notifserver.allow_defaultsnd));
				notifserver.editsettings.putBoolean("AllowDefaultSound", !isChecked);
				notifserver.editsettings.commit();
		    	//Log.d("BuzzBand", "done change");
			}});
		cno = (CheckBox)notifserver.mainview.findViewById(R.id.cb_builtinsnd);
		cno.setChecked(!notifserver.allow_builtinsnd);
		cno.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,	boolean isChecked) {
				notifserver.allow_builtinsnd = !isChecked;
				notifserver.editsettings.putBoolean("AllowBuiltinSound", !isChecked);
				notifserver.editsettings.commit();
			}});
		cno = (CheckBox)notifserver.mainview.findViewById(R.id.cb_notifsnd);
		cno.setChecked(!notifserver.allow_notifsnd);
		cno.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,	boolean isChecked) {
				notifserver.allow_notifsnd = !isChecked;
				notifserver.editsettings.putBoolean("AllowNotifSound", !isChecked);
				notifserver.editsettings.commit();
			}});
		cno = (CheckBox)notifserver.mainview.findViewById(R.id.cb_filesnd);
		cno.setChecked(!notifserver.allow_filesnd);
		cno.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,	boolean isChecked) {
				notifserver.allow_filesnd = !isChecked;
				notifserver.editsettings.putBoolean("AllowFileSound", !isChecked);
				notifserver.editsettings.commit();
			}});
    	//Log.d("BuzzBand", "Exit initchecks");
	}
	
    @Override
	public void onResume() {
    	super.onResume();
    	//Log.d("BuzzBand", "Enter onResume");
    	if (notifserver.bt4adapter == null) {
    		init_adapter();
    	}
		init_checks();
    	//Log.d("BuzzBand", "Exit onResume");
    }
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        notifserver.buzzbandhandle = this;
        Log.homefolder = getString(R.string.app_name);
        //Log.d("BuzzBand", " enter buzzbandmain oncreate");
        setContentView(R.layout.activity_buzz_band_main);
        
        notifserver.mainview = (View)findViewById(R.id.mainview);
        String v;
        try {
            v = getPackageManager().getPackageInfo(getPackageName(), 0).versionName;
        } catch (Exception e) {
        	v = "";
        }
        TextView version = (TextView)findViewById(R.id.Version);
        version.setText(getString(R.string.app_version)+" "+v);
        Button btscan = (Button)findViewById(R.id.FindBT4);
        btscan.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				findbt4devs(notifserver.mainview);
			}});

        Button bttest = (Button)findViewById(R.id.TestBT4);
        bttest.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				notifserver.lebuzz(getString(R.string.test_app), getString(R.string.test_ticker), getString(R.string.test_ext));
			}});

        if (notifserver.srvhandle == null) {
	        //Log.d("BuzzBand", "srvhandle was null");
           	notifserver.bt4names[0] = getString(R.string.bt_none);
        	notifserver.bt4arraycount = 1;
        	notifserver.bt4selected = 0;
        	notifserver.getprefs();
    		init_adapter();
			startService(new Intent(BuzzBandMain.this, notifserver.class));
	        //Log.d("BuzzBand", " buzzband starting service");
        } else {
	        //Log.d("BuzzBand", "srvhandle was already set");
	        if (notifserver.bt4arraycount < 2) {
		        //Log.d("BuzzBand", "no bt4 entries");
	           	notifserver.bt4names[0] = getString(R.string.bt_none);
	        	notifserver.bt4arraycount = 1;
	        	notifserver.bt4selected = 0;
	        } else {
		        //Log.d("BuzzBand", "number of bt4 entries="+Integer.toString(notifserver.bt4arraycount));
		        init_adapter();
	        }
        }
        //Log.d("BuzzBand", " exit buzzbandmain oncreate");
    }
    

	public class postsetspin implements Runnable {
		int pos;
		public postsetspin(int ppos) {
			pos = ppos;
		}
		@Override
		public void run() {
	        //Log.d("BuzzBand", "setting spinner="+Integer.toString(pos));
			notifserver.bt4spin.setSelection(pos);
			notifserver.prefbraceletaddress = notifserver.bt4address[pos];
			notifserver.bt4selected = pos;
			notifserver.editsettings.putString("MyBTooth4", notifserver.prefbraceletaddress);
			notifserver.editsettings.commit();
		}
	}

    private static final long SCAN_PERIOD = 9000;
    private static boolean scanningle = false;
    private static Timer scantimer = null;
    
    private static BluetoothAdapter.LeScanCallback lecallback = new BluetoothAdapter.LeScanCallback() {
		@Override
		public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
			try {
				String name = device.getName();
				String address = device.getAddress();
				if (name == null) {
					name = device.getAddress();
				}
		        //Log.d("BuzzBand", "onlescan name="+name+" address="+address);
				final int fidx = notifserver.bt4arraycount;
				for (int i=1; i<notifserver.bt4arraycount; i++) {
					if (notifserver.bt4address[i].compareTo(address) == 0) {
				        //Log.d("BuzzBand", "onlescan device already stored");
						return;
					}
				}
	        	notifserver.bt4names[notifserver.bt4arraycount] = name;
	        	notifserver.bt4address[notifserver.bt4arraycount] = address;
	        	notifserver.bt4array[notifserver.bt4arraycount] = device;
	        	notifserver.bt4characteristicforwrite[notifserver.bt4arraycount] = null;
	        	notifserver.bt4gatt[notifserver.bt4arraycount] = null;
		        //Log.d("BuzzBand", "device stored in our index at="+Integer.toString(notifserver.bt4arraycount)+" name="+name+" address="+address);
	        	notifserver.bt4arraycount++;
	        	if (notifserver.bt4adapter != null) {
	    	        notifserver.buzzbandhandle.runOnUiThread(new Runnable() {
	    	            @Override
	    	            public void run() {
	    	            	try {
	    				        //Log.d("BuzzBand", "enter guithread runnable");
	        		        	if (notifserver.pd != null) {
		    				        //Log.d("BuzzBand", "update pd");
	            	        		notifserver.pd.setMessage(notifserver.buzzbandhandle.getString(R.string.pd_lemess1)
	            	        				+Integer.toString(notifserver.bt4arraycount-1)
	            	        				+notifserver.buzzbandhandle.getString(R.string.pd_lemess2));
	        		        	}
            		        	notifserver.bt4adapter.clear();
            		        	for (int i=0; i<notifserver.bt4arraycount; i++) {
		    				        //Log.d("BuzzBand", "update bt4adapter="+notifserver.bt4names[i]);
                	            	notifserver.bt4adapter.add(notifserver.bt4names[i]);
            		        	}
	    				        //Log.d("BuzzBand", "notify bt4adapter");
            	            	notifserver.bt4adapter.notifyDataSetChanged();
	    	            	} catch (Exception e) {
	    				        //Log.d("BuzzBand", "Exception in lescan adapter update="+e.getMessage());
	    	            	}
	    	            }
	    	        });
	        	}
			} catch (Exception e) {
		        //Log.d("BuzzBand", "Exception in lescan callback="+e.getMessage());
			}
		}};
    
		public static BluetoothGattCallback maincallback = new BluetoothGattCallback() {
			@Override
			public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            	//Log.d("BuzzBand","device onconnectionstatechange status="+Integer.toString(status)+" newstate="+Integer.toString(newState));
            	if (status == BluetoothGatt.GATT_SUCCESS || status == 0x85) {
                	//Log.d("BuzzBand","assume ok");
        			String name = gatt.getDevice().getName();
        			if (name == null) {
        				name = "unknown";
        			}
        			String address = gatt.getDevice().getAddress();
					int devidx = 0;
                	for (int i=1; i<notifserver.bt4arraycount; i++) {
                		if (notifserver.bt4address[i].compareTo(address) == 0) {
                			devidx = i;
                			break;
                		}
                	}
                	if (devidx > 0 && notifserver.bt4gatt[devidx] == null) {
	                	//Log.d("BuzzBand","connection obtained to known device="+name+" address="+address+" at index="+Integer.toString(devidx));
                		notifserver.bt4gatt[devidx] = gatt;
                        if (newState == BluetoothProfile.STATE_CONNECTED) {
    	                	//Log.d("BuzzBand","device connect="+name+" address="+address);
    	                	notifserver.bt4isconnected[devidx] = true;
    	                	//Log.d("BuzzBand","start service discovery");
                        	gatt.discoverServices();
                        } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
    	                	//Log.d("BuzzBand","device disconnect="+name+" address="+address);
    	                	notifserver.bt4isconnected[devidx] = false;
                        }
                	}
            	} else {
            		if (notifserver.opencount < 5) {
            			BluetoothDevice dev = gatt.getDevice();
	                	//Log.d("BuzzBand","retry Gatt connection on="+dev.getAddress());
            			notifserver.opencount++;
                		gatt.close();
                		dev.connectGatt(notifserver.buzzbandhandle == null ? notifserver.srvhandle 
                				: notifserver.buzzbandhandle, true, maincallback);
            		}
            	}
            	//Log.d("BuzzBand","Exit Gatt onConnectionStateChange");
			}
			@Override
			public void onServicesDiscovered(BluetoothGatt gatt, int status) {
				if (status == BluetoothGatt.GATT_SUCCESS) {
					String devaddress = gatt.getDevice().getAddress();
					String devname = gatt.getDevice().getName();
                	//Log.d("BuzzBand","got services for device="+devname+" address="+devaddress);
					int devindex = 0;
					// match this device to our array of devices
        			for (int n=1; n<notifserver.bt4arraycount; n++) {
        				if (devaddress.compareTo(notifserver.bt4address[n]) == 0) {
        					devindex = n;
    	                	//Log.d("BuzzBand","device is at our index="+Integer.toString(n));
        				}
        			}
	            	List<BluetoothGattService> bg = gatt.getServices();
                	//Log.d("BuzzBand","got service list size="+Integer.toString(bg.size()));
	            	for (int i=0; i<bg.size(); i++) {
	            		BluetoothGattService bgone = bg.get(i);
	            		UUID bgu = bgone.getUuid();
	                	//Log.d("BuzzBand","got service="+bgu.toString());
	            		if (bgu.toString().compareTo(notifserver.ImmediateAlertServiceUUID.toString()) == 0) {
        					notifserver.bt4service[devindex] = notifserver.GATT_IMMEDIATEALERTSERVICE;
    	                	//Log.d("BuzzBand","immediate alert service found");
	            		}
	            		List<BluetoothGattCharacteristic> gbc = bgone.getCharacteristics();
	                	//Log.d("BuzzBand","got characteristic list size="+Integer.toString(gbc.size()));
	            		for (int j=0; j<gbc.size(); j++) {
	            			try {
		            			BluetoothGattCharacteristic gbcone = gbc.get(j);
		                    	//Log.d("BuzzBand","got characteristic uuid="+gbcone.getUuid().toString()
		                    	//		+" perm="+Integer.toString(gbcone.getPermissions())
		                    	//		+" prop="+Integer.toString(gbcone.getProperties())
		                    	//		+" wtype="+Integer.toString(gbcone.getWriteType())
		                    	//		);
		                    	if (devname != null) {
		                    		if (devname.toLowerCase().startsWith("soleus")) {
		                    			String ch = gbcone.getUuid().toString();
		                    			if (ch.startsWith("00001650")) {
		                    				notifserver.bt4characteristicforwrite[devindex] = gbcone;
		                    				notifserver.bt4selected = devindex;
		                    				if (notifserver.buzzbandhandle != null) {
                		                    	//Log.d("BuzzBand","call for spinner selection");
    		                    				notifserver.buzzbandhandle.runOnUiThread(notifserver.buzzbandhandle.new postsetspin(devindex));
		                    				}
            		                    	//Log.d("BuzzBand","found correct characteristic for soleus messaging at index="+Integer.toString(devindex));
		                    			}
		                    		}
		                    	}
		            			List<BluetoothGattDescriptor> gbcd = gbcone.getDescriptors();
        	                	//Log.d("BuzzBand","got descriptor list size="+Integer.toString(gbcd.size()));
		            			for (int k=0; k<gbcd.size(); k++) {
		            				try {
			            				BluetoothGattDescriptor d = gbcd.get(k);
			                        	//Log.d("BuzzBand","got descriptor uuid="+d.getUuid().toString());
			                        	//Log.d("BuzzBand","got descriptor val="+new String(d.getValue()));
		            				} catch (Exception e) {
			                        	//Log.d("BuzzBand","exception for descriptor j="+Integer.toString(j)
			                        	//		+" size="+Integer.toString(gbc.size())+" e="+e.getMessage());
		            				}
		            			}
	            			} catch (Exception e) {
	                        	//Log.d("BuzzBand","exception for characteristic j="+Integer.toString(j)
	                        	//		+" size="+Integer.toString(gbc.size())+" e="+e.getMessage());
	            			}
	            		}
	            		List<BluetoothGattService> gbs = bgone.getIncludedServices();
	                	//Log.d("BuzzBand","got included services list size="+Integer.toString(gbs.size()));
	            		for (int k=0; k<gbs.size(); k++) {
	            			BluetoothGattService s = gbs.get(k);
                        	//Log.d("BuzzBand","got sub service uuid="+s.getUuid().toString()+" type="+Integer.toString(s.getType())
                        	//		+" inst="+Integer.toString(s.getInstanceId()));
                        	List<BluetoothGattCharacteristic> cl = s.getCharacteristics();
	            			for (int n=0; n<cl.size(); n++) {
	            				BluetoothGattCharacteristic c = cl.get(n);
		                    	//Log.d("BuzzBand","got characteristic uuid="+c.getUuid().toString()
		                    	//		+" perm="+Integer.toString(c.getPermissions())
		                    	//		+" prop="+Integer.toString(c.getProperties())
		                    	//		+" wtype="+Integer.toString(c.getWriteType())
		                    	//		);
	            			}
	            		}
	            	}
                	//Log.d("BuzzBand","completed listing for this device");
				}
			}
		};
		
    @SuppressWarnings("deprecation")
	public static void findbt4devs(View rootview) {
        //Log.d("BuzzBand", "Enter findbt4devs");
		try {
	        notifserver.blueAdapter = BluetoothAdapter.getDefaultAdapter();
		} catch (Exception e) {
        	if (rootview == null) {
        		return;
        	}
			String mess = notifserver.buzzbandhandle.getString(R.string.bt_nobt);
			new AlertDialog.Builder(notifserver.buzzbandhandle)
		    .setMessage(mess)
		    .setPositiveButton(notifserver.buzzbandhandle.getString(R.string.cols_ok), null)
		    .show();
			return;
		}
		if (!notifserver.blueAdapter.isEnabled()) {
        	if (rootview == null) {
        		return;
        	}
			String mess = notifserver.buzzbandhandle.getString(R.string.bt_notenabled);
			new AlertDialog.Builder(notifserver.buzzbandhandle)
		    .setMessage(mess)
		    .setPositiveButton(notifserver.buzzbandhandle.getString(R.string.cols_ok), null)
		    .show();
			return;
		}
		if (!notifserver.testble()) {
        	if (rootview == null) {
        		return;
        	}
			String mess = notifserver.buzzbandhandle.getString(R.string.bt_noble);
			new AlertDialog.Builder(notifserver.buzzbandhandle)
		    .setMessage(mess)
		    .setPositiveButton(notifserver.buzzbandhandle.getString(R.string.cols_ok), null)
		    .show();
			return;
		}
		notifserver.mainview = rootview;
    	if (notifserver.mainview != null) {
    		notifserver.pd = new ProgressDialog(notifserver.buzzbandhandle);
    		notifserver.pd.setCancelable(false);
    		notifserver.pd.setIndeterminate(true);
    		notifserver.pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
    		notifserver.pd.setTitle(notifserver.buzzbandhandle.getString(R.string.pd_letitle));
    		notifserver.pd.setMessage(notifserver.buzzbandhandle.getString(R.string.pd_lemess1)+"0"+notifserver.buzzbandhandle.getString(R.string.pd_lemess2));
    		notifserver.pd.show();
    	}
        try {
    		notifserver.closeconnections();
        	if (notifserver.bt4arraycount > 1) {
            	notifserver.bt4arraycount = 1;
            	notifserver.bt4selected = 0;
        	}
            // Stops scanning after a pre-defined scan period.
			scantimer = new Timer();
			scantimer.schedule(new TimerTask(){
				@Override
				public void run() {
                	//Log.d("BuzzBand","end of scan for le devices");
                    scanningle = false;
                    notifserver.blueAdapter.stopLeScan(lecallback);
            		if (notifserver.pd != null) {
            			notifserver.buzzbandhandle.runOnUiThread(new Runnable() {
            				@Override
            				public void run() {
            					try {
                        			notifserver.pd.dismiss();
                        			notifserver.pd = null;
            					} catch (Exception e) {
            					}
            				}
            			});
            		}
                    if (notifserver.bt4arraycount > 1) {
	                	//Log.d("BuzzBand","now connect to any soleus device found in the scan");
	                	notifserver.opencount = 0;
                    	for (int i=1; i<notifserver.bt4arraycount; i++) {
            	        	if (notifserver.bt4names[i].toLowerCase().startsWith("soleus")) {
                        		notifserver.bt4array[i].connectGatt(
                        				notifserver.buzzbandhandle == null 
                        				? notifserver.srvhandle : notifserver.buzzbandhandle, 
                        				true, maincallback
                        				);
            	        	}
                    	}
                    }
				}
			} , SCAN_PERIOD);
            scanningle = true;
            notifserver.blueAdapter.startLeScan(lecallback);

        } catch (Exception e) {
            //Log.d("BuzzBand", "Exception in  findbt4devs="+e.getMessage());
        }
        //Log.d("BuzzBand", "Exit findbt4devs");
	}
}
