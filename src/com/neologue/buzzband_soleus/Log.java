package com.neologue.buzzband_soleus;

import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import android.os.Environment;
import android.os.SystemClock;
import android.widget.Toast;

/*
 * A replacement for the Java Log class.
 * This one produce a log file in the public folder which is the app name
 * The log file swaps to a new file after the file reaches 0.5MB to avoid
 * growing to excess.
 */
public class Log {
		public static File dfile = null;
		public static FileOutputStream dstream;
		public static File root = Environment.getExternalStorageDirectory();
		
		public static String homefolder = "Placeholder";

		public static synchronized void d(String t, String m) {
				try {
					if (!Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
						try {
							if (notifserver.buzzbandhandle != null) {
								try {
			      		   			Toast.makeText(notifserver.buzzbandhandle, notifserver.buzzbandhandle.getString(R.string.sd_gone), Toast.LENGTH_LONG).show();
			      		   			SystemClock.sleep(3000);
								} catch (Exception e) {
								}
							} 
							if (notifserver.srvhandle != null) {
								notifserver.srvhandle.stopSelf();
								System.exit(0);
							}
						} catch (Exception e) {
						}
						return;
					}
					if (dfile == null) {
						if (root.canWrite()){
							File fdir = new File(root.getAbsolutePath()+"/"+homefolder);
							fdir.mkdirs();
							dfile = new File(root.getAbsolutePath()+"/"+homefolder+"/debug.txt");
						} else {
							return;
						}
					}
					if (!dfile.exists()) {
						boolean oknew = dfile.createNewFile();
						dstream = new FileOutputStream(dfile);
						dstream.write(("Start new log file\r\n").getBytes());
					} else {
						dstream = new FileOutputStream(dfile, true);
					}
					SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
					String s =  format.format(new Date());
					dstream.write((s+":"+m+"\r\n").getBytes());
					if (dfile.length() > 500000) {
						dstream.write(("Swap to new log file\r\n").getBytes());
						dstream.close();
						File old = new File(root.getAbsolutePath()+"/"+homefolder+"/debug-prev.txt");
						if (old.exists()) {
							old.delete();
						}
						dfile.renameTo(old);
						dfile = null;
					} else {
						dstream.close();
					}
				} catch (Exception e) {
					try {
						Toast.makeText(notifserver.buzzbandhandle, "Log exception="+e.getMessage(), Toast.LENGTH_LONG).show();
					} catch (Exception ee) {
					}
					dfile = null;
				}
		}
		
		public static void e(String t, String m) {
			if ( t != null || m != null) return;
			try {
				if (dfile == null) {
					File root = Environment.getExternalStorageDirectory();
					if (root.canWrite()){
						File bad = new File(root.getAbsolutePath()+"/"+homefolder+"/debug.txt");
						bad.mkdirs();
						bad.delete();
						dfile = new File(root.getAbsolutePath()+"/"+homefolder+"/debug.txt");
						boolean oknew = dfile.createNewFile();
						dstream = new FileOutputStream(dfile);
					} else {
						return;
					}
				}
				dstream.write((t+":"+m+"\r\n").getBytes());
				dstream.flush();
			} catch (Exception e) {
				if (dfile == null) {
					return;
				}
				dfile = null;
			}
			
		}
		
}
