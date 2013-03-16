/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lastorder.pushnotifications.data;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.SoftReference;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.lastorder.pushnotifications.R;

/**
 * This helper class download images from the Internet and binds those with the provided ImageView.
 *
 * <p>It requires the INTERNET permission, which should be added to your application's manifest
 * file.</p>
 *
 * A local cache of downloaded images is maintained internally to improve performance.
 */
public class ImageDownloader {
    private static final String LOG_TAG = "ImageDownloader";
    private WeakReference<Context> myWeakContext;
    private ArrayList<String> urls = new ArrayList<String>();
    private boolean hasSDCard = true;
    /**
     * Download the specified image from the Internet and binds it to the provided ImageView. The
     * binding is immediate if the image is found in the cache and will be done asynchronously
     * otherwise. A null bitmap will be associated to the ImageView if an error occurs.
     *
     * @param url The URL of the image to download.
     * @param imageView The ImageView to bind the downloaded image to.
     */
 
    public void download(String url, ImageView imageView, Context context, ProgressBar bar) {
        resetPurgeTimer();
        Bitmap bitmap = getBitmapFromCache(url);
        myWeakContext = new WeakReference<Context>(context);
        
        //Log.d(LOG_TAG, "url: " + url);
        
        if (bitmap == null) {
            bar.setVisibility(0);
            imageView.setVisibility(8);
        	forceDownload(url, imageView, bar);
        } else {
        	bar.setVisibility(8);
            cancelPotentialDownload(url, imageView);
            imageView.setImageBitmap(bitmap);
            imageView.setVisibility(0);
            imageView.bringToFront();
        }
    }

    /*
     * Same as download but the image is always downloaded and the cache is not used.
     * Kept private at the moment as its interest is not clear.
       private void forceDownload(String url, ImageView view) {
          forceDownload(url, view, null);
       }
     */

    /**
     * Same as download but the image is always downloaded and the cache is not used.
     * Kept private at the moment as its interest is not clear.
     */
    private void forceDownload(String url, ImageView imageView, ProgressBar bar) {
        // State sanity: url is guaranteed to never be null in DownloadedDrawable and cache keys.
        if (url == null || !url.contains(".") || url.length() <= 6 || url.contains("null")) {
        	bar.setVisibility(8);
            imageView.setImageResource(R.drawable.ic_launcher);
            imageView.setVisibility(0);
            return;
        }

        if (cancelPotentialDownload(url, imageView)) {
                    BitmapDownloaderTask task = new BitmapDownloaderTask(imageView, bar);
                    DownloadedDrawable downloadedDrawable = new DownloadedDrawable(task);
                    imageView.setImageDrawable(downloadedDrawable);
                    imageView.setMinimumHeight(156);
                    task.execute(url);
                    //Log.d(LOG_TAG, "url: " + url);
        }
    }

    /**
     * Returns true if the current download has been canceled or if there was no download in
     * progress on this image view.
     * Returns false if the download in progress deals with the same url. The download is not
     * stopped in that case.
     */
    private static boolean cancelPotentialDownload(String url, ImageView imageView) {
        BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);

        if (bitmapDownloaderTask != null) {
            String bitmapUrl = bitmapDownloaderTask.url;
            if ((bitmapUrl == null) || (!bitmapUrl.equals(url))) {
                bitmapDownloaderTask.cancel(true);
            } else {
                // The same URL is already being downloaded.
                return false;
            }
        }
        return true;
    }

    /**
     * @param imageView Any imageView
     * @return Retrieve the currently active download task (if any) associated with this imageView.
     * null if there is no such task.
     */
    private static BitmapDownloaderTask getBitmapDownloaderTask(ImageView imageView) {
        if (imageView != null) {
            Drawable drawable = imageView.getDrawable();
            if (drawable instanceof DownloadedDrawable) {
                DownloadedDrawable downloadedDrawable = (DownloadedDrawable)drawable;
                return downloadedDrawable.getBitmapDownloaderTask();
            }
        }
        return null;
    }

    Bitmap downloadBitmap(String url, boolean isCanceled) throws IllegalArgumentException {
        final int IO_BUFFER_SIZE = 4 * 1024;

        // AndroidHttpClient is not allowed to be used from the main thread
        final DefaultHttpClient client = new DefaultHttpClient();
        //Si la url viene mal pincha
        
        // [IMG] [/] (creo)
        //Log.d("ImageDownloader", "URL: " + url.toString());
        final HttpGet getRequest = new HttpGet(url);
        
        
       
        try {
            HttpResponse response = client.execute(getRequest);
           
            
            final int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK || isCanceled) {
               // Log.w("ImageDownloader", "Error " + statusCode +
                 //       " while retrieving bitmap from " + url);
                return null;
            }

            final HttpEntity entity = response.getEntity();
            if (entity != null) {
                InputStream inputStream = null;
                try {
                    inputStream = entity.getContent();
                    // return BitmapFactory.decodeStream(inputStream);
                    // Bug on slow connections, fixed in future release.
                    return BitmapFactory.decodeStream(new FlushedInputStream(inputStream));
                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    entity.consumeContent();
                }
            }
        } catch (IOException e) {
            getRequest.abort();
            Log.w(LOG_TAG, "I/O error while retrieving bitmap from " + url, e);
        } catch (IllegalStateException e) {
            getRequest.abort();
            Log.w(LOG_TAG, "Incorrect URL: " + url);
        } catch (Exception e) {
            getRequest.abort();
            Log.w(LOG_TAG, "Error while retrieving bitmap from " + url, e);
        } 
        client.getConnectionManager().shutdown();
        return null;
    }

    /*
     * An InputStream that skips the exact number of bytes provided, unless it reaches EOF.
     */
 
    
    static class FlushedInputStream extends FilterInputStream {
        public FlushedInputStream(InputStream inputStream) {
            super(inputStream);
        }

        @Override
        public long skip(long n) throws IOException {
            long totalBytesSkipped = 0L;
            while (totalBytesSkipped < n) {
                long bytesSkipped = in.skip(n - totalBytesSkipped);
                if (bytesSkipped == 0L) {
                    int b = read();
                    if (b < 0) {
                        break;  // we reached EOF
                    } else {
                        bytesSkipped = 1; // we read one byte
                    }
                }
                totalBytesSkipped += bytesSkipped;
            }
            return totalBytesSkipped;
        }
    }

    /**
     * The actual AsyncTask that will asynchronously download the image.
     */
    class BitmapDownloaderTask extends AsyncTask<String, Void, Bitmap> {
        private String url;
        private WeakReference<ProgressBar> barReference;
        private WeakReference<ImageView> imageViewReference;
        private Context taskContext;
        public BitmapDownloaderTask(ImageView imageView, ProgressBar bar) {
            barReference = new WeakReference<ProgressBar>(bar);
            imageViewReference = new WeakReference<ImageView>(imageView);
        }

        /**
         * Actual download method.
         */
        @Override
        protected Bitmap doInBackground(String... params) {
            url = params[0];
            //Log.d(LOG_TAG, "params "+params[0]);
            try {
            	 return downloadBitmap(url, isCancelled());
            } catch (IllegalArgumentException e) {
            	return null;
			}
        }

        /**
         * Once the image is downloaded, assoc :iates it to the imageView
         */
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            if (isCancelled()) {
                bitmap = null;
            }
            Bitmap resized = null;
            try {
            ImageManager manager = new ImageManager();
            resized = manager.resizeBitmap(bitmap, 200, 200);
            addBitmapToCache(url, bitmap, resized);
            } catch (NullPointerException e) {
            	//Log.d(LOG_TAG, "Failed to download image: " + url);
            	
            }
            if (imageViewReference != null && barReference != null) {
                ProgressBar bar = barReference.get();
                ImageView imageView = imageViewReference.get();

                BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);
                // Change bitmap only if this process is still associated with it
                // Or if we don't use any bitmap to task association (NO_DOWNLOADED_DRAWABLE mode)

                if ((this == bitmapDownloaderTask)) {
                	try{
                		if(resized == null) {
                			imageView.setImageResource(R.drawable.ic_launcher);
                		} else {
                			imageView.setImageBitmap(resized);
                		}
                		bar.setVisibility(8);
                        imageView.setVisibility(0);
                        imageView.bringToFront();
                	} catch (NullPointerException e) {
                		e.printStackTrace();
                		bar.setVisibility(0);
                	}
                }
            }
        }
    }


    /**
     * A fake Drawable that will be attached to the imageView while the download is in progress.
     *
     * <p>Contains a reference to the actual download task, so that a download task can be stopped
     * if a new binding is required, and makes sure that only the last started download process can
     * bind its result, independently of the download finish order.</p>
     */
    static class DownloadedDrawable extends ColorDrawable {
        private final WeakReference<BitmapDownloaderTask> bitmapDownloaderTaskReference;

        public DownloadedDrawable(BitmapDownloaderTask bitmapDownloaderTask) {
            super(Color.BLACK);
            bitmapDownloaderTaskReference =
                new WeakReference<BitmapDownloaderTask>(bitmapDownloaderTask);
        }

        public BitmapDownloaderTask getBitmapDownloaderTask() {
            return bitmapDownloaderTaskReference.get();
        }
    }


    
    /*
     * Cache-related fields and methods.
     * 
     * We use a hard and a soft cache. A soft reference cache is too aggressively cleared by the
     * Garbage Collector.
     */
    
    private static final int HARD_CACHE_CAPACITY = 10;
    private static final int DELAY_BEFORE_PURGE = 10 * 1000; // in milliseconds

    // Hard cache, with a fixed maximum capacity and a life duration
    private final HashMap<String, Bitmap> sHardBitmapCache =
        new LinkedHashMap<String, Bitmap>(HARD_CACHE_CAPACITY / 2, 0.75f, true) {
        @Override
        protected boolean removeEldestEntry(LinkedHashMap.Entry<String, Bitmap> eldest) {
            if (size() > HARD_CACHE_CAPACITY) {
                // Entries push-out of hard reference cache are transferred to soft reference cache
                sSoftBitmapCache.put(eldest.getKey(), new SoftReference<Bitmap>(eldest.getValue()));
                return true;
            } else
                return false;
        }
    };

    // Soft cache for bitmaps kicked out of hard cache
    private final static ConcurrentHashMap<String, SoftReference<Bitmap>> sSoftBitmapCache =
        new ConcurrentHashMap<String, SoftReference<Bitmap>>(HARD_CACHE_CAPACITY / 2);

    private final Handler purgeHandler = new Handler();

    private final Runnable purger = new Runnable() {
        public void run() {
            clearCache();
        }
    };

    /**
     * Adds this bitmap to the cache.
     * @param bitmap The newly downloaded bitmap.
     * @param resized 
     */
    private void addBitmapToCache(String url, Bitmap bitmap, Bitmap resized) {
        if (bitmap != null) {
        	String name = (hasSDCard ? Environment.getExternalStorageDirectory() : Environment.getRootDirectory()) + "/.tmpProms/" + url.hashCode() +".jpg";
			String rname = (hasSDCard ? Environment.getExternalStorageDirectory() : Environment.getRootDirectory()) + "/.tmpProms/" + url.hashCode() +"-t.jpg";
				hasExternalStoragePublicPicture(name);
				saveToSDCard(bitmap, name,  url.hashCode() +".jpg");
				saveToSDCard(resized, rname, url.hashCode() +"-t.jpg");
				urls.add(url);
				sHardBitmapCache.put(url, resized);
        }
    }

    /**
     * @param url The URL of the image that will be retrieved from the cache.
     * @return The cached bitmap or null if it was not found.
     */
    private Bitmap getBitmapFromCache(String url) {
        // First try the hard reference cache
    	try {
        synchronized (sHardBitmapCache) {
        	
            final Bitmap bitmap = sHardBitmapCache.get(url);
            if (bitmap != null) {
                // Bitmap found in hard cache
                // Move element to first position, so that it is removed last
                sHardBitmapCache.remove(url);
                sHardBitmapCache.put(url, bitmap);
                return bitmap;
            }
        }

        // Then try the soft reference cache
        SoftReference<Bitmap> bitmapReference = sSoftBitmapCache.get(url);
        if (bitmapReference != null) {
            final Bitmap bitmap = bitmapReference.get();
            if (bitmap != null) {
                // Bitmap found in soft cache
                return bitmap;
            } else {
                // Soft reference has been Garbage Collected
                sSoftBitmapCache.remove(url);
            }
        }
        
        
        	final String pathName = (hasSDCard ? Environment.getExternalStorageDirectory() : Environment.getRootDirectory()) + "/.tmpProms/" + url.hashCode() +"-t.jpg";
        	final Bitmap bitmap = BitmapFactory.decodeFile(pathName);
        	
        	if(bitmap != null) {
        		return bitmap;
        	}
        	
        

        return null;
    	} catch (OutOfMemoryError e) {
    		return null;
    	}
    }
 
    /**
     * Clears the image cache used internally to improve performance. Note that for memory
     * efficiency reasons, the cache will automatically be cleared after a certain inactivity delay.
     */
    public void clearCache() {
        sHardBitmapCache.clear();
        sSoftBitmapCache.clear();
    }

    /**
     * Allow a new delay before the automatic cache clear is done.
     */
    private void resetPurgeTimer() {
        purgeHandler.removeCallbacks(purger);
        purgeHandler.postDelayed(purger, DELAY_BEFORE_PURGE);
    }
    
    private boolean hasExternalStoragePublicPicture(String name) {
    	
    	
    
    		File f = new File(Environment.getExternalStorageDirectory() + ".tmpProms");
    		if(!f.exists()) {
    			try {
    				f.createNewFile();
    			} catch (IOException e) {
    				// TODO Auto-generated catch block
    				e.printStackTrace();
    				hasSDCard = false;
    				f = new File(Environment.getRootDirectory() + ".tmpProms");
    				try {
						f.createNewFile();
					
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
    			}
    		}

    		
    		
    	
		File file = new File(name);
		if (file != null) {
			file.delete();
		}
	
			try {
				file.createNewFile();
			
			} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		
			
			}
		
		return file.exists();
	}
	
	public void saveToSDCard(Bitmap bitmap, String name, String nam) {
		boolean mExternalStorageAvailable = false;
		boolean mExternalStorageWriteable = false;
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state) || !hasSDCard) {
			mExternalStorageAvailable = mExternalStorageWriteable = true;
			Log.v(LOG_TAG, "SD Card is available for read and write "
					+ mExternalStorageAvailable + mExternalStorageWriteable);
			saveFile(bitmap, name, nam);
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
			mExternalStorageAvailable = true;
			mExternalStorageWriteable = false;
			Log.v(LOG_TAG, "SD Card is available for read "
					+ mExternalStorageAvailable);
		} else {
			mExternalStorageAvailable = mExternalStorageWriteable = false;
			Log.v(LOG_TAG, "Please insert a SD Card to save your Video "
					+ mExternalStorageAvailable + mExternalStorageWriteable);
		}
	}
	
	private void saveFile(Bitmap bitmap, String fullname, String nam) {
		
		ContentValues values = new ContentValues();
	
		File outputFile = new File(fullname);
		values.put(MediaStore.MediaColumns.DATA, outputFile.toString());
		values.put(MediaStore.MediaColumns.TITLE, nam);
		values.put(MediaStore.MediaColumns.DATE_ADDED, System
				.currentTimeMillis());
		values.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpg");
		Uri uri = myWeakContext.get().getContentResolver().insert(
				android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI,

				values);;

		try {
			OutputStream outStream = myWeakContext.get().getContentResolver()
					.openOutputStream(uri);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outStream);

			outStream.flush();
			outStream.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch(IllegalStateException e) {
			e.printStackTrace();
		}
		if(!nam.contains("-t.jpg")) {
			bitmap.recycle();
		}
	}
}
