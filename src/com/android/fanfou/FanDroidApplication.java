package com.android.fanfou;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class FanDroidApplication extends Application {
	  
	  public static final String TAG = "TwitterApplication";
	  
//	  public static ImageManager mImageManager;
//	  public static TwitterDbAdapter mDb; 
	  public static FanfouApi mApi;

	  public static String mUsername=null;
	  public static String mPassword=null;
	  
	  @Override
	  public void onCreate() {
	    super.onCreate();

//	    mImageManager = new ImageManager(this);
//	    mDb = new TwitterDbAdapter(this);
//	    mDb.open();
	    mApi = new FanfouApi();
	    
//	    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);        
//
//	    String username = preferences.getString(Preferences.USERNAME_KEY, "");
//	    String password = preferences.getString(Preferences.PASSWORD_KEY, "");
	    
	    if(mUsername==null||mPassword==null){
	    	mUsername="fandroid";
	    	mPassword="fandroid";
	    }
//	    if (FanfouApi.isValidCredentials(username, password)) {
//	      mApi.setCredentials(username, password);
//	    }
	  }

	  @Override
	  public void onTerminate() {
//	    cleanupImages();
//	    mDb.close();
	    
	    super.onTerminate();
	  }
	  
//	  private void cleanupImages() {
//	    HashSet<String> keepers = new HashSet<String>();
//	    
//	    Cursor cursor = mDb.fetchAllTweets();
//	    
//	    if (cursor.moveToFirst()) {
//	      int imageIndex = cursor.getColumnIndexOrThrow(
//	          TwitterDbAdapter.KEY_PROFILE_IMAGE_URL);
//	      do {
//	        keepers.add(cursor.getString(imageIndex));
//	      } while (cursor.moveToNext());
//	    }
	    
//	    cursor.close();
//	    
//	    cursor = mDb.fetchAllDms();
//	    
//	    if (cursor.moveToFirst()) {
//	      int imageIndex = cursor.getColumnIndexOrThrow(
//	          TwitterDbAdapter.KEY_PROFILE_IMAGE_URL);
//	      do {
//	        keepers.add(cursor.getString(imageIndex));
//	      } while (cursor.moveToNext());
//	    }
//	    
//	    cursor.close();
//	    
//	    mImageManager.cleanup(keepers);
//	  }
	    
	}
