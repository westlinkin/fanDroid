package com.android.fanfou;


import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.fanfou.FanfouApi.ApiException;
import com.android.fanfou.FanfouApi.AuthException;


public class PublicTimeline extends BaseActivity implements OnTouchListener {
	private static String TAG = "PublicTimeline";
	
	private ImageView bt_Write;
	private TextView tv_UserID;
	private ImageView bt_Refresh;
	
	
	private ListView mlv_list;
	
	//public static  FanfouApi getApi()=new FanfouApi();

	//private String user_show_name;
	//用于长按事件
	private String contextMenu_tweet_user_text;
	private String contextMenu_tweet_text;
	private String contextMenu_tweet_id;
	private String contextMenu_tweet_user_id;
	private String contextMenu_fav;
	private int contextMenu_position;
	
	
	//public AppCache cache=new AppCache(PublicTimeline.this);
	
	
	private String[] from = new String[]{"profile_image","tweet_user_text","tweet_text","tweet_meta_text","tweet_fav","tweet_has_image"};
	private int[] to = new int[]{R.id.profile_image,R.id.tweet_user_text,R.id.tweet_text,R.id.tweet_meta_text,R.id.tweet_fav,R.id.tweet_has_image};
	private List<Map<String,Object>> data = new ArrayList<Map<String,Object>>();//消息数据
	private MyAdapter TweetAdapter;

	
	private ProgressDialog pDlg;
	private Handler handler = new Handler(){
		@Override
		public void handleMessage(Message msg){
			switch(msg.arg1){
			case 0 :TweetAdapter = new MyAdapter(PublicTimeline.this, data, R.layout.tweet, from, to);
			    	mlv_list.setAdapter(TweetAdapter);
			    	mlv_list.setSelectionFromTop(1, 0);
			    	pDlg.dismiss();
			    	break;

			}
		}
	};
	@Override
	   public void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        requestWindowFeature(Window.FEATURE_NO_TITLE); 
	        setContentView(R.layout.public_timeline);
	        
	        bt_Write = (ImageView)findViewById(R.id.bt_Write);  
	        tv_UserID=(TextView)findViewById(R.id.tv_UserID);
	        bt_Refresh=(ImageView)findViewById(R.id.bt_Refresh);
	        
	      
	        
	        mlv_list = (ListView)findViewById(R.id.mlv_list);
	        mlv_list.setOnTouchListener(this);
	        registerForContextMenu(mlv_list);
	       
	        
	            
				if(user_show_name==null){
					tv_UserID.setText("");
				}
				else{	
					tv_UserID.setText(user_show_name);
				}
			
				
				pDlg = new ProgressDialog(PublicTimeline.this);
        		pDlg.setMessage("载入数据中...");
        		pDlg.show();
        		new Thread(){
        			@Override
        			public void run(){
        				reflesh_status();
        				Message msg = handler.obtainMessage();
        				msg.arg1 = 0;		//0:reflesh
        				handler.sendEmptyMessage(0);
        			}
        		}.start();
				
		        //写的按钮事件
		        bt_Write.setOnClickListener(new OnClickListener(){
		        	@Override
					public void onClick(View v) {
		        	//	Toast.makeText(FanDroidActivity.this, "Write Interface.", Toast.LENGTH_SHORT).show();
		        		sendMsgDlg(PublicTimeline.this,"","");//参数：第二个为对话框内填入的话，第三个为回复消息的ID，非回复用户的ID
		        		//发送图片，由相机调用的太小；大图发布出去，估计是登陆的问题
		        	}
		        	
		        });
		        //刷新的按钮事件
		        bt_Refresh.setOnClickListener(new OnClickListener(){
		        	@Override
					public void onClick(View v) {
		        		
		        		//reflesh_status();
		        		pDlg = new ProgressDialog(PublicTimeline.this);
		        		pDlg.setMessage("载入数据中...");
		        		pDlg.show();
		        		new Thread(){
		        			@Override
		        			public void run(){
		        				reflesh_status();
		        				Message msg = handler.obtainMessage();
		        				msg.arg1 = 0;		//0:reflesh
		        				handler.sendEmptyMessage(0);
		        			}
		        		}.start();
		        		
		        		//Toast.makeText(FanDroidActivity.this, "Refresh Interface.", Toast.LENGTH_SHORT).show();
		        	}
		        	
		        });
	        
	        mlv_list.setOnItemLongClickListener(new OnItemLongClickListener() {

				@Override
				public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
						int arg2, long arg3) {
					// TODO Auto-generated method stub
					//获得该条信息的特征，将参数传入onCreateContextMenu
					 
					 String info = arg0.getItemAtPosition(arg2).toString();
					 info = info.substring(1, info.length() - 1);

					 String[] tokens = info.split(",");
				//	 Log.v("1111",info);
					 contextMenu_position = arg2;
					 for(int i = 0; i < tokens.length; i++){
						 if(tokens[i].contains("tweet_user_text")){
							 contextMenu_tweet_user_text = (tokens[i].substring(17));
						 }
						 else if(tokens[i].contains("tweet_user_id")){
							 contextMenu_tweet_user_id = (tokens[i].substring(15));
						 }
						 else if(tokens[i].contains("tweet_id")){
							 contextMenu_tweet_id = (tokens[i].substring(10));
						 }
						 else if(tokens[i].contains("tweet_text")){
							 contextMenu_tweet_text = (tokens[i].substring(12));
						 }
						 else if(tokens[i].contains("fav")){
							 contextMenu_fav = (tokens[i].substring(5));
						 }
					 }	
				
					 if(contextMenu_tweet_id.equals("")){
						 return true;
					 }
//					 Log.v("contextMenu_tweet_user_text",contextMenu_tweet_user_text);
//					 Log.v("contextMenu_tweet_user_id",contextMenu_tweet_user_id);
//					 Log.v("contextMenu_tweet_id",contextMenu_tweet_id);
//					 Log.v("contextMenu_tweet_text",contextMenu_tweet_text);
//					 Log.v("contextMenu_fav",contextMenu_fav);
					 
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

					String[] tokens = info.split(",");
					Log.v("tweet_id",info);
					if(info.contains("tweet_id=,")){
						String refleshandmore_text = "";
						 for(int i = 0; i < tokens.length; i++){
							 if(tokens[i].contains("tweet_text")){
								 refleshandmore_text = (tokens[i].substring(12));
							 }
						 }

						 if(refleshandmore_text.equals("\t\t\t\t\t\t刷新")){
							 Log.v("刷新","reflesh_status");	
							 //reflesh_status();
							 pDlg = new ProgressDialog(PublicTimeline.this);
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
						
					 }
				}
			});
	   }
		@Override
	   public boolean onCreateOptionsMenu(Menu menu){
			menu.add(1,1,1,"刷新").setIcon(R.drawable.con_refresh);
			menu.add(1,2,2,"后退").setIcon(R.drawable.con_back);
		    return true;		   
	   }
		
		@Override
		public boolean onOptionsItemSelected(MenuItem item){
			   Log.v("111",String.valueOf(item.getItemId()));
			   switch(item.getItemId()){
			   case 1:   pDlg = new ProgressDialog(PublicTimeline.this);
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
			   case 2:  finish();
				   		break;
			   }
			   return true;
		}
	
	   
	   @Override
	   public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo menuInfo){
		   //处理长按事件		   
		   menu.add(1,1,1,"进入" + contextMenu_tweet_user_text + "的空间");
		   menu.add(1,2,2,"转发");		   
		   menu.add(1,3,3,"收藏/取消收藏");
		   menu.add(1,5,5,"回复");
		   menu.add(1,6,6,"取消");
	   }
	   
	   @Override
	   public boolean onContextItemSelected(MenuItem item){
		   switch(item.getItemId()){
			   case 1: 	Intent intent_space= new Intent(PublicTimeline.this, SpaceActivity.class);
			       		intent_space.putExtra("user_show_name", contextMenu_tweet_user_text);
			       		intent_space.putExtra("user_id", contextMenu_tweet_user_id);
						startActivity(intent_space);
				   		break;
			   case 2: 	sendMsgDlg(PublicTimeline.this, "转@" + contextMenu_tweet_user_text + " " + contextMenu_tweet_text, "");
				   		break;
			   case 3: 	try {
				   			if(contextMenu_fav.equals("false")){
						   		getApi().addFav(contextMenu_tweet_id);
						   		Toast.makeText(this, "已收藏该消息", Toast.LENGTH_SHORT).show();
						   		
						   		Map<String, Object> item_fav = new HashMap<String, Object>();
						   		item_fav = data.get(contextMenu_position);
								boolean fav = true;		
								if(fav) {
									item_fav.put("tweet_fav", R.drawable.fav_on);
						   		}
								if(!fav){
									item_fav.put("tweet_fav" , R.drawable.none);
								}
								item_fav.put("fav", fav);
								TweetAdapter = new MyAdapter(this, data, R.layout.tweet, from, to);
								mlv_list.setAdapter(TweetAdapter);
								mlv_list.setSelectionFromTop(contextMenu_position, 0);
				   			}
				   			if(contextMenu_fav.equals("true")){
				   				getApi().deleteFav(contextMenu_tweet_id);				   				
						   		Toast.makeText(this, "已取消收藏该消息", Toast.LENGTH_SHORT).show();
						   		
						   		Map<String, Object> item_fav = new HashMap<String, Object>();
						   		item_fav = data.get(contextMenu_position);
								boolean fav = false;	
								if(fav) {
									item_fav.put("tweet_fav", R.drawable.fav_on);
						   		}
								if(!fav){
									item_fav.put("tweet_fav" , R.drawable.none);
								}
								item_fav.put("fav", fav);
								TweetAdapter = new MyAdapter(this, data, R.layout.tweet, from, to);
								mlv_list.setAdapter(TweetAdapter);
								mlv_list.setSelectionFromTop(contextMenu_position, 0);
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
				   		break;
			   case 5: 	sendMsgDlg(PublicTimeline.this, "@" + contextMenu_tweet_user_text + " ", contextMenu_tweet_id);
			   			break;
			   case 6: break;
		   }
		   return true;
	   }
	   

public  void sendMsgDlg(final Context context, String atwho, final String replyto){
		atWhoStr = atwho;
		final LinearLayout ll = (LinearLayout)getLayoutInflater().inflate(R.layout.sendmsg_dialog, null);
		
		EditText et = (EditText)ll.findViewById(R.id.et_word_number);
		if(!atwho.equals("")){
			TextView tv = (TextView)ll.findViewById(R.id.tv_word_number);
			et.setText(atwho);
			int len = 140 - (atwho.length());
			if(len >= 0){
				tv.setText("还可以输入 " + len + " 个字");
			}
			else{
				tv.setText("你输入已超过 " + -len + " 个字");
			}
		}
		et.addTextChangedListener((new TextWatcher(){
	           @Override
	           public void afterTextChanged(Editable s) {
	               // TODO Auto-generated method stub
	        	   TextView tv_temp = (TextView)ll.findViewById(R.id.tv_word_number);
	        	   EditText et_temp = (EditText)ll.findViewById(R.id.et_word_number);
	        	   int length = 140 - et_temp.getText().toString().length();
	        	   if(length >= 0){
	        		   tv_temp.setText("还可以输入 " + length + " 个字");
		   			}
		   			else{
		   				tv_temp.setText("你输入已超过 " + -length + " 个字");
		   			}
	           }

	           @Override
	           public void beforeTextChanged(CharSequence s, int start, int count,
	                   int after) {
	               // TODO Auto-generated method stub
	        	   
	           }

	           @Override
	           public void onTextChanged(CharSequence s, int start, int before,
	                   int count) {
	               // TODO Auto-generated method stub
	              
	               
	           }
	       }));
		
        new AlertDialog.Builder(context).setView(ll).setTitle("发送消息")
        .setPositiveButton("发布", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				try {
					EditText et_temp = (EditText)ll.findViewById(R.id.et_word_number);
					if(!et_temp.getText().toString().equals("")){
						getApi().update(et_temp.getText().toString(), replyto);
						Toast.makeText(context, "新消息已发送", Toast.LENGTH_SHORT).show();
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
		}).setNeutralButton("发图", new DialogInterface.OnClickListener() {
			//弹出对话框，选择相册或拍照
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				final CharSequence[] items = { "相册", "拍照" };
				new AlertDialog.Builder(context).setTitle("选择图片").setItems(items,
						new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,int item) {									
								if(item == 1){ 
									Intent getImageByCamera= new Intent("android.media.action.IMAGE_CAPTURE"); 
									startActivityForResult(getImageByCamera, 1);   
								}else{
									Intent getImage = new Intent(Intent.ACTION_GET_CONTENT); 
							        getImage.addCategory(Intent.CATEGORY_OPENABLE); 
							        getImage.setType("image/*"); 
							        startActivityForResult(getImage, 0); 
								}
							}
						}).show();

			}
		}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		}).show();
	}
	
	@Override 
	protected void onActivityResult(int requestCode, int resultCode, Intent data) { 
		
        //图库
        if (requestCode == 0) { 
            try { 
            	//获得图片的uri 
                Uri originalUri = data.getData(); 				
                sendMsgDlg_pic(PublicTimeline.this, atWhoStr, originalUri);
                
            } catch (Exception e) { 
                Log.v("1", e.toString()); 
            } 

        }
        //camera
        else if(requestCode == 1){
        	try {
	        	super.onActivityResult(requestCode, resultCode, data);	        	
	        	Bundle extras = data.getExtras(); 
	        	Bitmap bitmap = (Bitmap)extras.get("data");
        		sendMsgDlg_cam(PublicTimeline.this, atWhoStr, bitmap);
		    	
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

        }

    } 
	public void sendMsgDlg_pic(final Context context, String string, final Uri uri){
		
		final LinearLayout ll_2 = (LinearLayout)getLayoutInflater().inflate(R.layout.sendmsg_dialog, null);
		EditText et = (EditText)ll_2.findViewById(R.id.et_word_number);
		if(!string.equals("")){
			TextView tv = (TextView)ll_2.findViewById(R.id.tv_word_number);
			et.setText( string );
			int len = 140 - (string.length());
			if(len >= 0){
				tv.setText("还可以输入 " + len + " 个字");
			}
			else{
				tv.setText("你输入已超过 " + -len + " 个字");
			}
		}
		et.addTextChangedListener((new TextWatcher(){
	           @Override
	           public void afterTextChanged(Editable s) {
	               // TODO Auto-generated method stub
	        	   TextView tv_temp = (TextView)ll_2.findViewById(R.id.tv_word_number);
	        	   EditText et_temp = (EditText)ll_2.findViewById(R.id.et_word_number);
	        	   int length = 140 - et_temp.getText().toString().length();
	        	   if(length >= 0){
	        		   tv_temp.setText("还可以输入 " + length + " 个字");
		   			}
		   			else{
		   				tv_temp.setText("你输入已超过 " + -length + " 个字");
		   			}
	           }

	           @Override
	           public void beforeTextChanged(CharSequence s, int start, int count,
	                   int after) {
	               // TODO Auto-generated method stub
	        	   
	           }

	           @Override
	           public void onTextChanged(CharSequence s, int start, int before,
	                   int count) {
	               // TODO Auto-generated method stub
	              
	               
	           }
	       }));
		
		new AlertDialog.Builder(context).setView(ll_2).setTitle("发送图片")
        .setPositiveButton("发布图片", new DialogInterface.OnClickListener() {
        	@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				try {
					EditText et_temp = (EditText)ll_2.findViewById(R.id.et_word_number);
					Cursor cursor = getContentResolver().query(uri, null, null, null, null);
					cursor.moveToFirst();

					getApi().postTwitPic(new File(cursor.getString(1)), et_temp.getText().toString());

					Toast.makeText(context, "新消息已发送", Toast.LENGTH_SHORT).show();
					
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
        	
        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		}).show();
	}
	
	public void sendMsgDlg_cam(final Context context, String string, final Bitmap bitmap){
		
		final LinearLayout ll_2 = (LinearLayout)getLayoutInflater().inflate(R.layout.sendmsg_dialog, null);
		EditText et = (EditText)ll_2.findViewById(R.id.et_word_number);
		if(!string.equals("")){
			TextView tv = (TextView)ll_2.findViewById(R.id.tv_word_number);
			et.setText( string);
			int len = 140 - (string.length());
			if(len >= 0){
				tv.setText("还可以输入 " + len + " 个字");
			}
			else{
				tv.setText("你输入已超过 " + -len + " 个字");
			}
		}
		et.addTextChangedListener((new TextWatcher(){
	           @Override
	           public void afterTextChanged(Editable s) {
	               // TODO Auto-generated method stub
	        	   TextView tv_temp = (TextView)ll_2.findViewById(R.id.tv_word_number);
	        	   EditText et_temp = (EditText)ll_2.findViewById(R.id.et_word_number);
	        	   int length = 140 - et_temp.getText().toString().length();
	        	   if(length >= 0){
	        		   tv_temp.setText("还可以输入 " + length + " 个字");
		   			}
		   			else{
		   				tv_temp.setText("你输入已超过 " + -length + " 个字");
		   			}
	           }

	           @Override
	           public void beforeTextChanged(CharSequence s, int start, int count,
	                   int after) {
	               // TODO Auto-generated method stub
	        	   
	           }

	           @Override
	           public void onTextChanged(CharSequence s, int start, int before,
	                   int count) {
	               // TODO Auto-generated method stub
	              
	               
	           }
	       }));
		
		new AlertDialog.Builder(context).setView(ll_2).setTitle("发送图片")
        .setPositiveButton("发布图片", new DialogInterface.OnClickListener() {
        	@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				try {
					EditText et_temp = (EditText)ll_2.findViewById(R.id.et_word_number);
					String filePath = "/sdcard/temp.jpeg";
					File f = new File(filePath);
					OutputStream os = new FileOutputStream(f); 					
					bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os); 
					os.close();
					getApi().postTwitPic(f, et_temp.getText().toString());
					f.delete();
					bitmap.recycle();
					Toast.makeText(context, "新消息已发送", Toast.LENGTH_SHORT).show();
					
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
        	
        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				dialog.dismiss();
			}
		}).show();
	}
	
	
	public void fillFriendsTimeline(){
		JSONArray jsonArray = null;
		try {
			
			jsonArray = getApi().getPublicTimeline(20);
			
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
					String user_text = "";
					String user_id = "";
										
					boolean fav = objects[i].getBoolean("favorited");				
					String tweet_id = objects[i].getString("id");
					String tweet_text = Html.fromHtml(objects[i].getString("text")).toString();
					String meta_text = changeDate(objects[i].getString("created_at"))+ "  来自"
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
						tweet_text = objects[i].getString("text");
						tweet_text =tweet_text.replaceAll("<a href=\"(.*)\">(.+?)</a>", imgUrl);						
					}
					else{
						item.put("tweet_has_image", R.drawable.none);
					}
					
					item.put("fav", fav);
					item.put("tweet_user_id", user_id);
					item.put("tweet_id", tweet_id);
	    			item.put("profile_image", getBitMap(user_img_url));
	    			item.put("tweet_user_text", user_text);
	    			item.put("tweet_text", tweet_text);
	    			item.put("tweet_meta_text", meta_text);
	    			
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
    	item_reflesh.put("fav", false);
    	item_reflesh.put("tweet_fav",R.drawable.none);
    	item_reflesh.put("tweet_user_id", "");
    	item_reflesh.put("tweet_id", "");
    	item_reflesh.put("profile_image", R.drawable.none);
    	item_reflesh.put("tweet_user_text","" );
    	item_reflesh.put("tweet_text", "\t\t\t\t\t\t刷新");
    	item_reflesh.put("tweet_meta_text", "");
    	item_reflesh.put("tweet_has_image", R.drawable.none);

		data.add(item_reflesh);

	//	TweetAdapter = new MyAdapter(this, data, R.layout.tweet, from, to);
	//	mlv_list.setAdapter(TweetAdapter);
	}
	
	
	
	public void reflesh_status(){
		
			data.clear();
			
			fillFriendsTimeline();
			init_reflesh();
	
	}
	
	@Override
	public boolean onTouch(View arg0, MotionEvent arg1) {
		// TODO Auto-generated method stub
		return false;
	}

}

