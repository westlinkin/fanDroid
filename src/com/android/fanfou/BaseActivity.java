package com.android.fanfou;

//import com.fanfou.LoginActivity;
//import com.fanfou.Preferences;
//import com.fanfou.TwitterApi;
//import com.fanfou.TwitterApplication;
//import com.fanfou.TwitterService;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import android.view.Menu;

public class BaseActivity extends Activity {

	
	private static final String TAG = "BaseActivity";
	protected static String user_show_name=null;
	protected static String user_id = null;
	protected static String atWhoStr;
	protected static FanDroidCache mCache;
	//protected static AppCache cache;
	
	protected static boolean newMention=false;
	
	protected static List<Map<String,Object>> data = new ArrayList<Map<String,Object>>();
	protected static List<Map<String,Object>> FDActivity = null;
	protected static List<Map<String,Object>> AtMeActivityCache = null;
	protected static List<Map<String,Object>> CookiesActivityCache = null;
	
	
	
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		mCache=new FanDroidCache(this);
		//cache=new AppCache(this);
//		try {
//			JSONObject userName = getApi().getUsrName(FanDroidApplication.mUsername, FanDroidApplication.mPassword);	
//			user_show_name = userName.getString("name");
//			user_id = userName.getString("id");
//			String userInfo=FanDroidApplication.mUsername+","+FanDroidApplication.mPassword;
//			
//			cache.SaveData(userInfo);
//			
//		} catch (IOException e11) {
//			// TODO Auto-generated catch block
//			e11.printStackTrace();
//		} catch (AuthException e11) {
//			// TODO Auto-generated catch block
//			e11.printStackTrace();
//		} catch (ApiException e11) {
//			// TODO Auto-generated catch block
//			e11.printStackTrace();
//		} catch (JSONException e11){
//			e11.printStackTrace();
//		}
	}
	
	
	public final static DateFormat DB_DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd' 'HH:mm", Locale.US);
	 public Bitmap getMyBitmap(int bitSq){
			String uri="/sdcard/FanDroid/proImg/profileImg"+bitSq+".png";
			Bitmap bmp=BitmapFactory.decodeFile(uri);
			return bmp;
		}
	 
	 public boolean saveMyBitmap(Bitmap mBitmap,int bitNameSq) throws IOException {
			File dir=new File("/sdcard/FanDroid/proImg");
			if(!dir.exists()){
				dir.mkdirs();
			}
			 
			
	        File f = new File("/sdcard/FanDroid/proImg/profileImg"+bitNameSq+".png");
	        
	        if(f.exists()){
	        	return false;
	        }
	        if(!f.createNewFile()){
	        	return false;
	        }
	       
	        FileOutputStream fOut = null;
	        try {
	                fOut = new FileOutputStream(f);
	        } catch (FileNotFoundException e) {
	                e.printStackTrace();
	                return false;
	        }
	        mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
	        try {
	                fOut.flush();
	        } catch (IOException e) {
	                e.printStackTrace();
	                return false;
	        }
	        try {
	                fOut.close();
	        } catch (IOException e) {
	                e.printStackTrace();
	                return false;
	        }
	        return true;
		}
	 
	 
	 
	 
	 public Bitmap getBitMap(String url) {  
		    URL myFileUrl = null;  
		    Bitmap bitmap = null;  
		    
		    if(!mCache.isImgExist(url)){
				mCache.InsertImg(url);
		    }
		    
		    int imgSq=mCache.getImgSq(url);
			bitmap =getMyBitmap(imgSq);
			if(bitmap==null){
				try {  
			        myFileUrl = new URL(url);  
			    } catch (MalformedURLException e) {  
			        e.printStackTrace();  
			    }  
			    try {  
			        HttpURLConnection conn = (HttpURLConnection) myFileUrl  
			                .openConnection();  
			        conn.setDoInput(true);  
			        conn.connect();  
			        InputStream is = conn.getInputStream();  
			        
			        bitmap = BitmapFactory.decodeStream(is);  
			        is.close();  
			    } catch (IOException e) {  
			        e.printStackTrace();  
			    }  
				
				try{
					saveMyBitmap(bitmap, imgSq);
				}
				catch(IOException e){
					e.printStackTrace();
				}
				
				bitmap=getMyBitmap(imgSq);
			}
		    
		    
		    
		    return bitmap;  
		} 
	 
	 
	 
	 

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
	}
	
	
	@Override
	   public boolean onCreateOptionsMenu(Menu menu){
		   menu.add(1,1,1,"刷新").setIcon(R.drawable.con_refresh);
		   menu.add(1,2,2,"搜索").setIcon(R.drawable.con_search);
		   menu.add(1,3,3,"注销").setIcon(R.drawable.con_logout);		   
		   menu.add(1,4,4,"退出").setIcon(R.drawable.con_shutdown);
		   menu.add(1,5,5,"随便看看").setIcon(R.drawable.con_public);
		   menu.add(1,6,6,"关于").setIcon(R.drawable.con_info);
		   return true;		   
	   }
	   
	   
	
	protected void handleLoggedOut(){
		
		
		if(isTaskRoot()){
			showLogin();
		}
		else{
			setResult(RESULT_LOGOUT);
			
		}
		finish();
	}
	
	protected void showLogin() {
	    Intent intent = new Intent(this, Login.class);
	    // TODO: might be a hack?
	    intent.putExtra(Intent.EXTRA_INTENT, getIntent());

	    startActivity(intent);
	  }
	
	
	protected boolean isLoggedIn() {
	    return getApi().isLoggedIn();
	  }
	
	protected FanfouApi getApi() {
	    return FanDroidApplication.mApi;
	  }
	
//	protected void manageUpdateChecks() {
//	    boolean isEnabled = mPreferences.getBoolean(
//	        Preferences.CHECK_UPDATES_KEY, false);
//
//	    if (isEnabled) {
//	      TwitterService.schedule(this);
//	    } else {
//	      TwitterService.unschedule(this);
//	    }
//	  }

	
	
	
protected void FDSaveInMemCache(List<Map<String,Object>> data){
		
		FDActivity=null;
		FDActivity=new ArrayList<Map<String,Object>>();
			
			
		int count =20;
		if(data.size()<=20){
			count=data.size();
		}
		for(int i=0;i<count;i++){
			FDActivity.add(data.get(i));
		}
	}
	
protected void AtMeSaveInMemCache(List<Map<String,Object>> data){

    AtMeActivityCache=null;
	AtMeActivityCache=new ArrayList<Map<String,Object>>();
		
		
	int count =20;
	if(data.size()<=20){
		count=data.size();
	}
	
	for(int i=0;i<count;i++){
		AtMeActivityCache.add(data.get(i));
	}
}


protected void CookiesSaveInMemCache(List<Map<String,Object>> data){

    CookiesActivityCache=null;
    CookiesActivityCache=new ArrayList<Map<String,Object>>();
		
		
	
	for(int i=0;i<data.size();i++){
		CookiesActivityCache.add(data.get(i));
	}
}

	
	
	
	private static final int RESULT_LOGOUT = RESULT_FIRST_USER + 1;
}
