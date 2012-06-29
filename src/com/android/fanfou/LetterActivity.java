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

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.Toast;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabWidget;
import android.widget.TextView;

import com.android.fanfou.FanfouApi.ApiException;
import com.android.fanfou.FanfouApi.AuthException;

public class LetterActivity extends BaseActivity implements OnTouchListener, OnGestureListener{
	private static String TAG = "LetterActivity";
	
	private ImageView bt_Write;
	private TextView tv_UserID;
	private ImageView bt_Refresh;
	
	
	private TextView bt_HomePage;
	private TextView bt_Me;
	private TextView bt_Letter;
	private TextView bt_Space;
	private TextView bt_Search;
	private TextView bt_Cookies;
	private TabHost tabHost;
	
	private ListView mlv_list;
	
	private FanfouApi mApi;
	private String toUserId;
	private String[] Ids;
	private String[] Names;
	
	//用于长按事件
	private String contextMenu_dm_user_text;
	private String contextMenu_dm_text;
	private String contextMenu_dm_id;
	private String contextMenu_dm_user_id;
	private int contextMenu_position;
	
	private GestureDetector mGestureDetector; 
	private String[] from = new String[]{"profile_image","dm_sender_text","dm_meta_text","dm_text"};
	private int[] to = new int[]{R.id.profile_image,R.id.dm_sender_text,R.id.dm_meta_text,R.id.dm_text};
	private List<Map<String,Object>> data = new ArrayList<Map<String,Object>>();//消息数据
	private MyAdapter TweetAdapter;

	private String status = "GET";
	private ProgressDialog pDlg;
	private int position_more;
	private Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			switch(msg.arg1){
			case 0 :TweetAdapter = new MyAdapter(LetterActivity.this, data, R.layout.dm, from, to);
			    	mlv_list.setAdapter(TweetAdapter);
			    	mlv_list.setSelectionFromTop(1, 0);
			    	pDlg.dismiss();
			    	break;
			default:TweetAdapter = new MyAdapter(LetterActivity.this, data, R.layout.dm, from, to);
			    	mlv_list.setAdapter(TweetAdapter);
			    	mlv_list.setSelectionFromTop(msg.arg1, 0);
			    	pDlg.dismiss();
			    	break;
			}
		}
	};
	
	
	@Override
	   public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        requestWindowFeature(Window.FEATURE_NO_TITLE); 
	        setContentView(R.layout.letter);
	        
	        mApi = getApi();
	        mGestureDetector = new GestureDetector(this);
	       
	        bt_Write = (ImageView)findViewById(R.id.bt_Write);  
	        tv_UserID=(TextView)findViewById(R.id.tv_UserID);
	        bt_Refresh=(ImageView)findViewById(R.id.bt_Refresh);
	        
	        bt_HomePage=(TextView)findViewById(R.id.bt_HomePage);
	        bt_Me=(TextView)findViewById(R.id.bt_Me);
	        bt_Letter=(TextView)findViewById(R.id.bt_Letter);
	        bt_Space=(TextView)findViewById(R.id.bt_Space);
	        bt_Search=(TextView)findViewById(R.id.bt_Search);
	        bt_Cookies=(TextView)findViewById(R.id.bt_Cookies);
	        
	        mlv_list = (ListView)findViewById(R.id.mlv_list);
	        mlv_list.setOnTouchListener(this);
	        registerForContextMenu(mlv_list);
	        bt_Letter.setClickable(false);
	        bt_Letter.setBackgroundColor(this.getResources().getColor(R.color.focused));
	        
	        bt_Write.setImageResource(R.drawable.dm);
	        
	        if(user_show_name==null){
				tv_UserID.setText("");
			}
			else{	
				tv_UserID.setText(user_show_name);
			}
	        
	        tabHost = (TabHost)findViewById(R.id.tabHost);
	        tabHost.setup();
	        TabWidget tabWidget = tabHost.getTabWidget();
	      
	        //设置默认第一个tab打开
	        tabHost.setCurrentTab(0);
	        
	        tabHost.addTab(tabHost.newTabSpec("我收到的私信").setIndicator("我收到的私信", getResources().getDrawable(R.drawable.inbox)).setContent(R.id.view1));
	        tabHost.addTab(tabHost.newTabSpec("我发出的私信").setIndicator("我发出的私信", getResources().getDrawable(R.drawable.outbox)).setContent(R.id.view2));
	        
	        tabHost.setOnTabChangedListener(new OnTabChangeListener() {
				
				@Override
				public void onTabChanged(String tabId) {
					// TODO Auto-generated method stub
					if(tabId.equals("我收到的私信")){
						
						data.clear();
						status = "GET";
						//显示刷新和更多按钮，如果数据库中有数据，则在刷新和更多中间填入消息
						init_reflesh();//增加刷新按钮
					
						//从数据库中拿数据，若无，则不拿
												
						
						init_more();//增加更多按钮
						TweetAdapter = new MyAdapter(LetterActivity.this, data, R.layout.dm, from, to);
						mlv_list.setAdapter(TweetAdapter);
						mlv_list.setSelectionFromTop(1, 0);
					}
					else if(tabId.equals("我发出的私信")){
						data.clear();
						status = "TO";
						//显示刷新和更多按钮，如果数据库中有数据，则在刷新和更多中间填入消息
						init_reflesh();//增加刷新按钮
					
						//从数据库中拿数据，若无，则不拿
						
						init_more();//增加更多按钮
						TweetAdapter = new MyAdapter(LetterActivity.this, data, R.layout.dm, from, to);
						mlv_list.setAdapter(TweetAdapter);
						mlv_list.setSelectionFromTop(1, 0);
					}
				}
	        });
	        
	        //显示刷新和更多按钮，如果数据库中有数据，则在刷新和更多中间填入消息
			init_reflesh();//增加刷新按钮
		
			//从数据库中拿数据，若无，则不拿
			
			
			init_more();//增加更多按钮
			TweetAdapter = new MyAdapter(LetterActivity.this, data, R.layout.dm, from, to);
			mlv_list.setAdapter(TweetAdapter);
			mlv_list.setSelectionFromTop(1, 0);
			
			
	        //写的按钮事件
	        bt_Write.setOnClickListener(new OnClickListener(){
	        	@Override
				public void onClick(View v) {
	        	//私信
	        		sendDM(LetterActivity.this);
	        	}
	        });
	        //刷新的按钮事件
	        bt_Refresh.setOnClickListener(new OnClickListener(){
	        	@Override
				public void onClick(View v) {
	        		 pDlg = new ProgressDialog(LetterActivity.this);
		        		pDlg.setMessage("载入数据中...");
		        		pDlg.show();
		        		new Thread(){
		        			@Override
		        			public void run(){
		        				reflesh_status();
		        				Message msg = handler.obtainMessage();
		        				msg.arg1 = 0;	//0:reflesh
		        				handler.sendMessage(msg);
		        			}
		        		}.start();
	        	}
	        	
	        });
	        
	        //首页
	        bt_HomePage.setOnClickListener(new OnClickListener(){
	        	@Override
				public void onClick(View v) {
	        		//Toast.makeText(AtMeActivity.this, "HomePage", Toast.LENGTH_SHORT).show();
	        		Intent intent_home = new Intent(LetterActivity.this, FanDroidActivity.class);
	        		startActivity(intent_home);
					finish();
	        	}
	        	
	        });
	        //@我
	        bt_Me.setOnClickListener(new OnClickListener(){
	        	@Override
				public void onClick(View v) {
	        		//Toast.makeText(LetterActivity.this, "Me", Toast.LENGTH_SHORT).show();
	        		Intent intent_me = new Intent(LetterActivity.this, AtMeActivity.class);
	        		startActivity(intent_me);
					finish();
	        	}
	        	
	        });
//	        //私信
//	        bt_Letter.setOnClickListener(new OnClickListener(){
//	        	@Override
//				public void onClick(View v) {
//	        		//Toast.makeText(AtMeActivity.this, "私信", Toast.LENGTH_SHORT).show();
//	        		Intent intent_letter = new Intent(LetterActivity.this, LetterActivity.class);
//	        		startActivity(intent_letter);
//	        	}
//	        	
//	        });
	        //空间
	        bt_Space.setOnClickListener(new OnClickListener(){
	        	@Override
				public void onClick(View v) {
	        		//Toast.makeText(AtMeActivity.this, "Space", Toast.LENGTH_SHORT).show();
	        		Intent intent_space = new Intent(LetterActivity.this, SpaceActivity.class);
	        		intent_space.putExtra("user_show_name", user_show_name);
	        		intent_space.putExtra("user_id", user_id);
	        		startActivity(intent_space);
					finish();
	        	}
	        	
	        });
	        //搜索
	        bt_Search.setOnClickListener(new OnClickListener(){
	        	@Override
				public void onClick(View v) {
	        		//Toast.makeText(FanDroidActivity.this, "搜索", Toast.LENGTH_SHORT).show();
	        		Intent intent_search = new Intent(LetterActivity.this, SearchActivity.class);
					startActivity(intent_search);
					finish();
	        	}
	        	
	        });
	        //收藏
	        bt_Cookies.setOnClickListener(new OnClickListener(){
	        	@Override
				public void onClick(View v) {
	        		//Toast.makeText(AtMeActivity.this, "收藏", Toast.LENGTH_SHORT).show();
	        		Intent intent_cookies = new Intent(LetterActivity.this, CookiesActivity.class);
					startActivity(intent_cookies);
					finish();
	        	}
	        	
	        });
	        
	        mlv_list.setOnItemLongClickListener(new OnItemLongClickListener() {

				@Override
				public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					// TODO Auto-generated method stub
					//获得该条信息的特征，将参数传入onCreateContextMenu
					 
					 String info = arg0.getItemAtPosition(arg2).toString();
					// Log.v("1111",info);
					 info = info.substring(1, info.length() - 1);

					 String[] tokens = info.split(",");
					// Log.v("1111",info);
					 contextMenu_position = arg2;
					 for(int i = 0; i < tokens.length; i++){
						 if(tokens[i].contains("dm_id")){
							 contextMenu_dm_id = (tokens[i].substring(7));
						 }
						 else if(tokens[i].contains("dm_text")){
							 contextMenu_dm_text = (tokens[i].substring(9));
						 }
						 else if(tokens[i].contains("dm_sender_id")){
							 contextMenu_dm_user_id = (tokens[i].substring(13));
						 }
						 else if(tokens[i].contains("dm_sender_text")){
							 contextMenu_dm_user_text = (tokens[i].substring(16));
						 }
						 
					 }	
				
					 if(contextMenu_dm_id.equals("")){
						 return true;
					 }
//					 Log.v("contextMenu_dm_id",contextMenu_dm_id);
//					 Log.v("contextMenu_dm_text",contextMenu_dm_text);
//					 Log.v("contextMenu_dm_user_id",contextMenu_dm_user_id);
//					 Log.v("contextMenu_dm_user_text",contextMenu_dm_user_text);

					 return false;
				}
			});
	        mlv_list.setOnItemClickListener(new OnItemClickListener() {
				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					// TODO Auto-generated method stub
					String info = arg0.getItemAtPosition(arg2).toString();
					info = info.substring(1, info.length() - 1);
					Log.v("111",info);
					String[] tokens = info.split(",");
					
					if(info.contains("dm_id=")){
						String refleshandmore_text = "";
						 for(int i = 0; i < tokens.length; i++){
							 if(tokens[i].contains("dm_text")){
								 refleshandmore_text = (tokens[i].substring(9));
							 }
						 }

						 if(refleshandmore_text.equals("\t\t\t\t\t\t\t刷新")){
							 Log.v("刷新","reflesh_status");	
							 //reflesh_status();
							 pDlg = new ProgressDialog(LetterActivity.this);
				        		pDlg.setMessage("载入数据中...");
				        		pDlg.show();
				        		new Thread(){
				        			@Override
				        			public void run(){
				        				reflesh_status();
				        				Message msg = handler.obtainMessage();
				        				msg.arg1 = 0;	//0:reflesh
				        				handler.sendMessage(msg);
				        			}
				        		}.start();
						 }
						 else if(refleshandmore_text.equals("\t\t\t\t\t\t\t更多")){

							 Log.v("更多","more_status");						 
							 //more_status();
							 pDlg = new ProgressDialog(LetterActivity.this);
				        		pDlg.setMessage("载入数据中...");
				        		pDlg.show();
				        		new Thread(){
				        			@Override
				        			public void run(){
				        				more_status();
				        				Message msg = handler.obtainMessage();
				        				msg.arg1 = position_more;	//more:position_more
				        				Log.v("pos", String.valueOf(position_more));
				        				handler.sendMessage(msg);
				        			}
				        		}.start();
						 }
					 }
				}
			});
	
	   }
	   
	
	
	 
	   @Override
	   public boolean onOptionsItemSelected(MenuItem item){
		   Log.v("111",String.valueOf(item.getItemId()));
		   switch(item.getItemId()){
			   case 1:  pDlg = new ProgressDialog(LetterActivity.this);
			       		pDlg.setMessage("载入数据中...");
			    		pDlg.show();
			    		new Thread(){
			    			@Override
			    			public void run(){
			    				reflesh_status();
			    				Message msg = handler.obtainMessage();
			    				msg.arg1 = 0;	//0:reflesh
			    				handler.sendMessage(msg);
			    			}
			    		}.start();
				   		break;
			   case 2: Intent intent_search = new Intent(this, SearchActivity.class);
					   startActivity(intent_search);
					   this.finish();
					   break;
			   case 3: LoginInfo loginInfo=mCache.getLoginInfo();
				   	   loginInfo.AutoCheckIn=0;
				   	   mCache.SaveLoginInfo(loginInfo);
				   	   getApi().logout();
				       Intent intent_login = new Intent(this, Login.class);
			   		   startActivity(intent_login);
			   		   this.finish();
			   		   break;
			   case 4: finish();
			   		   System.exit(0);
			   		   break;
			   case 5: Intent intent_pub = new Intent(this, PublicTimeline.class);
			   			startActivity(intent_pub);
				   		break;
			   case 6: Intent intent_about = new Intent(this, AboutActivity.class);
					   startActivity(intent_about);
					   break;
			   
			   		   
		   }
		   return true;
	   }
	   
	   
	   @Override
	   public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo){
		   //处理长按事件
		   if(status.equals("GET")){
			   menu.add(1,1,1,"回复");
			   menu.add(1,2,2,"取消");
		   }
	   }
	   
	   @Override
	   public boolean onContextItemSelected(MenuItem item){
		   switch(item.getItemId()){
			   case 1:  sendDMId(LetterActivity.this, contextMenu_dm_user_id, contextMenu_dm_id);
				   		break;
			   case 2: break;
		   }
		   return true;
	   }

	@Override
	public boolean onDown(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		// TODO Auto-generated method stub
		if (e1.getX() - e2.getX() > 100 && Math.abs(velocityX) > 200 ) {
			Intent intent_space = new Intent(this, SpaceActivity.class);
			intent_space.putExtra("user_show_name", user_show_name);
    		intent_space.putExtra("user_id", user_id);
			startActivity(intent_space);
			this.finish();
			return true;
		} else if (e2.getX() - e1.getX() > 100 && Math.abs(velocityX) > 200 ) {
			Intent intent_me = new Intent(this, AtMeActivity.class);
			startActivity(intent_me);
			this.finish();
			return true;
		}
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onTouch(View v, MotionEvent event) {
		// TODO Auto-generated method stub
		return mGestureDetector.onTouchEvent(event); 
	}
	
	public void fillGetDM(String howToGet, String id){
		JSONArray jsonArray = null;
		try {
			if(howToGet.equals("getTimeline")){
				jsonArray = mApi.getDirectMessages();
			}
			else if(howToGet.equals("sinceId")){
				jsonArray = mApi.getDirectMessagesSinceId(id);
			}
			else if(howToGet.equals("maxId")){
				jsonArray = mApi.getDirectMessagesMaxId(id);
			}
	    } catch (IOException e) {
	        Log.e(TAG, e.getMessage(), e);
	    } catch (AuthException e) {
	        Log.i(TAG, "Invalid authorization.");
	    } catch (ApiException e) {
	        Log.e(TAG, e.getMessage(), e);
	    }
	      
	    if(jsonArray != null){	 
	    	JSONObject[] objects = new JSONObject[jsonArray.length()];	
	    	
	    	for(int i = 0; i < jsonArray.length(); i++){
	    		try {
	    			Map<String, Object> item = new HashMap<String, Object>();

					objects[i] = jsonArray.getJSONObject(i);
					
					String user_img_url = "";
					String dm_sender_text = "";
					
					String dm_id = objects[i].getString("id");
					String dm_text = Html.fromHtml(objects[i].getString("text")).toString();
					String dm_meta_text = changeDate(objects[i].getString("created_at"));
					
					user_img_url = objects[i].getJSONObject("sender").getString("profile_image_url");
					dm_sender_text = "来自：" + objects[i].getString("sender_screen_name");
					
					String dm_sender_id = objects[i].getString("sender_id");
					
					
					item.put("dm_id", dm_id);
					item.put("dm_text", dm_text);
	    			item.put("profile_image", getBitMap(user_img_url));
	    			item.put("dm_meta_text", dm_meta_text);
	    			item.put("dm_sender_id", dm_sender_id);
	    			item.put("dm_sender_text", dm_sender_text);
	    			
	    			data.add(item);
	    			
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.e("jsonArray Error", e.toString());
				}
				
	    	}
		
	    //	TweetAdapter = new MyAdapter(this, data, R.layout.tweet, from, to);

	    //    mlv_list.setAdapter(TweetAdapter);
	        Log.i(TAG, "jsonArray");
	    }
	      
	}
	
	public void fillFromDM(String howToGet, String id){
		JSONArray jsonArray = null;
		try {
			if(howToGet.equals("getTimeline")){
				jsonArray = mApi.getDirectMessagesSent();
			}
			else if(howToGet.equals("sinceId")){
				jsonArray = mApi.getDirectMessagesSentSinceId(id);
			}
			else if(howToGet.equals("maxId")){
				jsonArray = mApi.getDirectMessagesSentMaxId(id);
			}
	    } catch (IOException e) {
	        Log.e(TAG, e.getMessage(), e);
	    } catch (AuthException e) {
	        Log.i(TAG, "Invalid authorization.");
	    } catch (ApiException e) {
	        Log.e(TAG, e.getMessage(), e);
	    }
	      
	    if(jsonArray != null){	 
	    	JSONObject[] objects = new JSONObject[jsonArray.length()];	
	    	
	    	for(int i = 0; i < jsonArray.length(); i++){
	    		try {
	    			Map<String, Object> item = new HashMap<String, Object>();

					objects[i] = jsonArray.getJSONObject(i);
					
					String user_img_url = "";
					String dm_sender_text = "";
					
					String dm_id = objects[i].getString("id");
					String dm_text = Html.fromHtml(objects[i].getString("text")).toString();
					String dm_meta_text = changeDate(objects[i].getString("created_at"));
					
					user_img_url = objects[i].getJSONObject("recipient").getString("profile_image_url");
					dm_sender_text = "发至：" + objects[i].getString("recipient_screen_name");
					
					String dm_sender_id = objects[i].getString("recipient_id");
					
					
					item.put("dm_id", dm_id);
					item.put("dm_text", dm_text);
	    			item.put("profile_image", getBitMap(user_img_url));
	    			item.put("dm_meta_text", dm_meta_text);
	    			item.put("dm_sender_id", dm_sender_id);
	    			item.put("dm_sender_text", dm_sender_text);
	    			
	    			data.add(item);
	    			
				} catch (JSONException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Log.e("jsonArray Error", e.toString());
				}
				
	    	}
		
	    //	TweetAdapter = new MyAdapter(this, data, R.layout.tweet, from, to);

	    //    mlv_list.setAdapter(TweetAdapter);
	        Log.i(TAG, "jsonArray");
	    }
	      
	}
	
	
	public String changeDate(String strTime){
		String[] tokens = strTime.split(" ");
		if(tokens[1].equals("Jan")) tokens[1] = "01";
		else if(tokens[1].equals("Feb")) tokens[1] = "02";
		else if(tokens[1].equals("Mar")) tokens[1] = "03";
		else if(tokens[1].equals("Apr")) tokens[1] = "04";
		else if(tokens[1].equals("May")) tokens[1] = "05";
		else if(tokens[1].equals("Jun")) tokens[1] = "06";
		else if(tokens[1].equals("Jul")) tokens[1] = "07";
		else if(tokens[1].equals("Aug")) tokens[1] = "08";
		else if(tokens[1].equals("Sep")) tokens[1] = "09";
		else if(tokens[1].equals("Out")) tokens[1] = "10";
		else if(tokens[1].equals("Nov")) tokens[1] = "11";
		else if(tokens[1].equals("Dec")) tokens[1] = "12";
		String time = tokens[5] + "-" + tokens[1] + "-" + tokens[2] + " " + tokens[3];
		return time;
	}
	
	public void init_reflesh(){
		Map<String, Object> item_reflesh = new HashMap<String, Object>();
		item_reflesh.put("dm_id", "");
		item_reflesh.put("dm_text", "\t\t\t\t\t\t\t刷新");
		item_reflesh.put("profile_image","");
		item_reflesh.put("dm_meta_text", "");
		item_reflesh.put("dm_sender_id", "");
		item_reflesh.put("dm_sender_text", "");
		
		data.add(item_reflesh);

	//	TweetAdapter = new MyAdapter(this, data, R.layout.tweet, from, to);
	//	mlv_list.setAdapter(TweetAdapter);
	}
	
	public void init_more(){
		Map<String, Object> item_more = new HashMap<String, Object>();
		item_more.put("dm_id", "");
		item_more.put("dm_text", "\t\t\t\t\t\t\t更多");
		item_more.put("profile_image","");
		item_more.put("dm_meta_text", "");
		item_more.put("dm_sender_id", "");
		item_more.put("dm_sender_text", "");
    	
		data.add(item_more);
	//	TweetAdapter = new MyAdapter(this, data, R.layout.tweet, from ,to);
	//	mlv_list.setAdapter(TweetAdapter);
	}
	
	public void reflesh_status(){
		if(status.equals("GET")){
			if(mlv_list.getAdapter().getCount() <= 2){
				data.clear();
				init_reflesh();
				
				fillGetDM("getTimeline", "");
				
				init_more();
			}
			else{//有数据的情况
				String sinceId = (String)data.get(1).get("dm_id");
				List<Map<String,Object>> data_temp = new ArrayList<Map<String,Object>>();
				
				for(int i = 0; i < data.size(); i++){
					data_temp.add(data.get(i));
				}
	
				data.clear();
				
				init_reflesh();
				
				fillGetDM("sinceId", sinceId);
				
			    for(int i = 1; i < data_temp.size();i++){
			    	data.add(data_temp.get(i));
			    }
		//	    TweetAdapter = new MyAdapter(this, data, R.layout.tweet, from, to);
		//        mlv_list.setAdapter(TweetAdapter);
			}
		}
		else if(status.equals("TO")){
			if(mlv_list.getAdapter().getCount() <= 2){
				data.clear();
				init_reflesh();
				
				fillFromDM("getTimeline", "");
				
				init_more();
			}
			else{//有数据的情况
				String sinceId = (String)data.get(1).get("dm_id");
				List<Map<String,Object>> data_temp = new ArrayList<Map<String,Object>>();
				
				for(int i = 0; i < data.size(); i++){
					data_temp.add(data.get(i));
				}
	
				data.clear();
				
				init_reflesh();
				
				fillFromDM("sinceId", sinceId);
				
			    for(int i = 1; i < data_temp.size();i++){
			    	data.add(data_temp.get(i));
			    }
			}
		}
	}
	
	public void more_status(){
		if(status.equals("GET")){
			if(mlv_list.getAdapter().getCount() <= 2){
				data.clear();
				init_reflesh();
				
				fillGetDM("getTimeline", "");
				
				init_more();
			}
			else{//有数据的情况
				String maxId = (String)data.get(data.size() - 2).get("dm_id");
				position_more = data.size() - 2;
				
				data.remove(data.size() - 1);
				data.remove(data.size() - 1);
				
				fillGetDM("maxId", maxId);
				
				init_more();
			    
		//		mlv_list.setSelectionFromTop(position, 0);
			}
		}
	
		else if(status.equals("TO")){
			if(mlv_list.getAdapter().getCount() <= 2){
				data.clear();
				init_reflesh();
				
				fillFromDM("getTimeline", "");
				
				init_more();
			}
			else{//有数据的情况
				String maxId = (String)data.get(data.size() - 2).get("dm_id");
				position_more = data.size() - 2;
				
				data.remove(data.size() - 1);
				data.remove(data.size() - 1);
				
				fillFromDM("maxId", maxId);
				
				init_more();
			}
		}
	}
	
	public void sendDM(final Context context){
		final LinearLayout ll = (LinearLayout)getLayoutInflater().inflate(R.layout.dm_dlg, null);
		
		Button bt = (Button)ll.findViewById(R.id.bt_friendsId);
		bt.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				JSONArray jsonArray = null;
				try {
					jsonArray = mApi.getFollowing(user_id, 1);
					Ids = new String[jsonArray.length()];
					Names = new String[jsonArray.length()];
					for(int i = 0; i < jsonArray.length(); i++){
						JSONObject object= jsonArray.getJSONObject(i);
						Ids[i] = object.getString("id");
						Names[i] = object.getString("name");
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (AuthException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ApiException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (JSONException e){
					e.printStackTrace();
				}
				
				
				new AlertDialog.Builder(context).setTitle("选择收信人ID").setItems(Names, new DialogInterface.OnClickListener() {
					
					@Override
					public void onClick(DialogInterface dialog, int which) {
						// TODO Auto-generated method stub
						toUserId = Ids[which];
						TextView tv = (TextView)ll.findViewById(R.id.tv_word_number);
						tv.setText("给" + Names[which] + "发送私信");
					}
				}).show();
			}
		});
        new AlertDialog.Builder(context).setView(ll).setTitle("发送私信")
        .setPositiveButton("发送", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				try {
					EditText et_temp = (EditText)ll.findViewById(R.id.et_word_number);
					if(!et_temp.getText().toString().equals("")){
						mApi.sendDirectMessage(toUserId, et_temp.getText().toString(), "");
						Toast.makeText(context, "私信已发送", Toast.LENGTH_SHORT).show();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (AuthException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ApiException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		}).show();
		
	}
	
	public void sendDMId(final Context context, final String id, final String replyId){
		final LinearLayout ll = (LinearLayout)getLayoutInflater().inflate(R.layout.sendmsg_dialog, null);
		
		TextView tv = (TextView)ll.findViewById(R.id.tv_word_number);
		
		tv.setText("给" + id + "发送私信");
		
		new AlertDialog.Builder(context).setView(ll).setTitle("发送私信")
		.setPositiveButton("发送",new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				try {
					EditText et_temp = (EditText)ll.findViewById(R.id.et_word_number);
					if(!et_temp.getText().toString().equals("")){
						mApi.sendDirectMessage(id, et_temp.getText().toString(), replyId);
						Toast.makeText(context, "私信已发送", Toast.LENGTH_SHORT).show();
					}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					Log.v("IOException",e.toString());
				} catch (AuthException e) {
					// TODO Auto-generated catch block
					Log.v("AuthException",e.toString());
				} catch (ApiException e) {
					// TODO Auto-generated catch block
					Log.v("ApiException",e.toString());
				}				
			}
		})
		.setNegativeButton("取消", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		}).show();
	}
}
