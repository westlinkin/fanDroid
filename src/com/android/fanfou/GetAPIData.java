package com.android.fanfou;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Html;
import android.util.Log;
import android.widget.ListView;

import com.android.fanfou.FanfouApi.ApiException;
import com.android.fanfou.FanfouApi.AuthException;

public class GetAPIData {
	
	public static  FanfouApi mApi=new FanfouApi();
	
	private static String TAG = "FanDroidActivity";
	private ListView mlv_list;
	private Context context;
	
	
	
	
	public void fillFriendsTimeline(){
		JSONArray jsonArray = null;
		try {
	        jsonArray = mApi.getTimeline(1);
	    } catch (IOException e) {
	        Log.e(TAG, e.getMessage(), e);
	    } catch (AuthException e) {
	        Log.i(TAG, "Invalid authorization.");
	    } catch (ApiException e) {
	        Log.e(TAG, e.getMessage(), e);
	    }
	      
	    if(jsonArray != null){	 
	    	List<Map<String,Object>> data = new ArrayList<Map<String,Object>>();
	    	
	    	JSONObject[] objects = new JSONObject[jsonArray.length()];	
	    	
	    	for(int i = 0; i < jsonArray.length(); i++){
	    		try {
	    			Map<String, Object> item = new HashMap<String, Object>();

					objects[i] = jsonArray.getJSONObject(i);
					
					String user_img_url = "";
					String user_text = "";
					String user_id = "";
										
					boolean fav = objects[i].getBoolean("favorited");				
					String Msg_id = objects[i].getString("id");
					String Msg_text = Html.fromHtml(objects[i].getString("text")).toString();
					String meta_text = objects[i].getString("created_at").substring(0, 19)+ "  ю╢вт"
										 + Html.fromHtml(objects[i].getString("source")).toString();
					
					user_img_url = objects[i].getJSONObject("user").getString("profile_image_url");
					user_text = objects[i].getJSONObject("user").getString("name");
					
					user_id = objects[i].getJSONObject("user").getString("id");
					
					if(fav) {
	    				item.put("tweet_fav", R.drawable.fav_on);
	    			}
					if(!fav){
						item.put("tweet_fav" , R.drawable.none);
					}
					
					if(objects[i].has("photo")){
						item.put("tweet_has_image", R.drawable.pic);
						String imgUrl = objects[i].getJSONObject("photo").getString("largeurl");
						Msg_text = objects[i].getString("text");
						Msg_text =Msg_text.replaceAll("<a href=\"(.*)\">(.+?)</a>", imgUrl);						
					}
					else{
						item.put("tweet_has_image", R.drawable.none);
					}
					
					item.put("fav", fav);
					item.put("tweet_user_id", user_id);
					item.put("tweet_id", Msg_id);
	    			item.put("profile_image", getBitMap(user_img_url));
	    			item.put("tweet_user_text", user_text);
	    			item.put("tweet_text", Msg_text);
	    			item.put("tweet_meta_text", meta_text);
	    			
	    			data.add(item);
	    			
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.e("jsonArray Error", e.toString());
				}
				
	    	}
		
	    	String[] from = new String[]{"profile_image","tweet_user_text","tweet_text","tweet_meta_text","tweet_fav","tweet_has_image"};
	    	int[] to = new int[]{R.id.profile_image,R.id.tweet_user_text,R.id.tweet_text,R.id.tweet_meta_text,R.id.tweet_fav,R.id.tweet_has_image};
//	    	MyAdapter TweetAdapter = new MyAdapter(context, data, R.layout.tweet, from, to);
//
//	        mlv_list.setAdapter(TweetAdapter);
	        Log.i(TAG, "jsonArray");
	    }
	      
	}
	
	
	public Bitmap getBitMap(String url) {  
	    URL myFileUrl = null;  
	    Bitmap bitmap = null;  
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
	    return bitmap;  
	} 

}
