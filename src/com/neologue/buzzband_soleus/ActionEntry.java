package com.neologue.buzzband_soleus;

import java.io.ByteArrayOutputStream;

import android.app.PendingIntent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

/*
 * Class for holding/passing some information about a notification's
 * 	actions. When passed to another app the PendingIntent is left behind.
 * 	When passed an ActionEntry is Serialised first.
 * 	Since notifications typically have more than one action available,
 * 	what is passed is an array of ActionEntries. To pass the array, abyte array
 * 	is made first of the same length at the ActionEntry array, then the byte array elements
 * 	are set to the Serialised versions of the ActionEntry objects. Finally,
 * 	the array of byte arrays is itself serialied into a single byte array for transmission.
 * 	The method 'decodeonelevel' is used at the end of the transmission path to turn the byte
 * 	array back into an array of byte arrays each element of which can be DeSerialised to an
 * 	ActionEntry object.
 * 
 * 	The zeroeth element of the array of ActionEntry objects always exists and represents the
 * 	action of simply jumping to the app that generated the notification. Elements from 1 onwards
 * 	are the actions associated with the notification.
 */
public class ActionEntry {
	public String title = "";
	public PendingIntent pintent = null;
	public Drawable icon = null;
	
    public static Bitmap drawableToBitmap (Drawable drawable) {
        try {
            if (drawable instanceof BitmapDrawable) {
                return ((BitmapDrawable)drawable).getBitmap();
            }
            int width = drawable.getIntrinsicWidth();
            width = width > 0 ? width : 1;
            int height = drawable.getIntrinsicHeight();
            height = height > 0 ? height : 1;

            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);

            return bitmap;
        } catch (Exception e) {
            ////Log.d("BuzzBand", e.getMessage());
        }
        return null;
    }

    public static byte[] BitmapToBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }

	public byte [] Serialise() {
		byte [] btitle = null;
		int titlelen = 0;
		if (title != null) {
			btitle = title.getBytes();
			titlelen = btitle.length;
		}
		byte [] bicon = null;
		int iconlen = 0;
		if (icon != null) {
			bicon = BitmapToBytes(drawableToBitmap(icon));
			iconlen = bicon.length;
		}
        ////Log.d("Buzzby", "serial title="+title+" icon="+Integer.toString(iconlen));
		byte [] res = new byte[4+titlelen+iconlen];
		int offset = 2;
		res[0] = (byte) (titlelen/128);
		res[1] = (byte) (titlelen%128);
		for (int i=0; i<titlelen; i++) {
			res[offset] = btitle[i];
			offset += 1;
		}
		res[offset] = (byte) (iconlen/128);
		offset += 1;
		res[offset] = (byte) (iconlen%128);
		offset += 1;
		for (int i=0; i<iconlen; i++) {
			res[offset] = bicon[i];
			offset += 1;
		}
		return res;
	}
	
	public static ActionEntry DeSerialise(byte [] orig) {
		ActionEntry res = new ActionEntry();
		int titlelen = orig[0]*128+orig[1];
		int offset = 2;
		if (titlelen > 0) {
			res.title = new String(orig, 2, titlelen);
			offset += titlelen;
		}
		int iconlen = orig[offset]*128+orig[offset+1];
		offset += 2;
		if (iconlen > 0) {
			res.icon = new BitmapDrawable(BitmapFactory.decodeByteArray(orig, offset, iconlen));
		}
		return res;
	}

}

