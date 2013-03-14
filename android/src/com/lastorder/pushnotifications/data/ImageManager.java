package com.lastorder.pushnotifications.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;


public class ImageManager {
	
	private Context context;
	
	public ImageManager() {
		
	}
	
	public ImageManager(Context cont) {
		context = cont;
	}

	
	
	 public final void storeThumbail(final Bitmap source, final String title) {
	      
	
		 
		 StringBuilder name = new StringBuilder();
		 name.append(title.substring(title.lastIndexOf("/"), title.length() - 4));
		
		 name.append("-t.jpg");
		 String path = title.substring(0, title.lastIndexOf("/"));

         FileOutputStream fos = null;

	         File file = new File(path, name.toString());
	         

	         try {
				fos = new FileOutputStream(file);
				source.compress(Bitmap.CompressFormat.JPEG, 75, fos);
		         //Log.d("ImageManager", "Thumbail stored: " + Environment.getExternalStorageDirectory() + name.toString());
				fos.close();
	         } catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}



}
	   

	
	
	public final Bitmap resizeBitmap(final Bitmap source, final int width, final int height) {
	      // create the matrix to scale it
	      /*
	      Matrix matrix = new Matrix();     
	      float scaleX = width / source.getWidth();
	      float scaleY = height / source.getHeight();
	      matrix.setScale(scaleX, scaleY);
	      return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
	      */

	      final int bitmapWidth = source.getWidth();
	      final int bitmapHeight = source.getHeight();
	      final float scale = Math.min((float) width / (float) bitmapWidth, (float) height / (float) bitmapHeight);
	      final int scaledWidth = (int) (bitmapWidth * scale);
	      final int scaledHeight = (int) (bitmapHeight * scale);
	      return Bitmap.createScaledBitmap(source, scaledWidth, scaledHeight, true);
	   }
	
	
	
}
