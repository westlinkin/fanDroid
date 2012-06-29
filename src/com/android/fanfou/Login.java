package com.android.fanfou;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.fanfou.FanfouApi.ApiException;
import com.android.fanfou.FanfouApi.AuthException;
import com.support.UserTask;

public class Login extends Activity {
    /** Called when the activity is first created. */
	private TextView tv_register;
	private Button btn_login;
	private EditText et_usrname;
	private EditText et_usrpsw;
	private FanfouApi mApi;
	private CheckBox ck_auto;
	private SharedPreferences mPreferences;
	private UserTask<Void, String, Boolean> mLoginTask;
	private FanDroidCache mCache;
	private LoginInfo loginInfo;
	
	private static final String TAG = "Login";
	private ProgressDialog pDlg;
	private Handler handler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO Auto-generated method stub
			super.handleMessage(msg);
			if(msg.what==0){
				pDlg.dismiss();
			}
		}
		
	};
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE); 
        mCache=new FanDroidCache(this);
        setContentView(R.layout.login);
        loginInfo=mCache.getLoginInfo();
        
        
        
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        
       
        mApi = new FanfouApi();
        tv_register = (TextView)findViewById(R.id.tv_register);  
        tv_register.setText(Html.fromHtml("<u>"+"没有账号? 点击注册"+"</u>"));  
        tv_register.setOnClickListener(new OnClickListener(){       	
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				try{
					Uri uri=Uri.parse("http://fanfou.com/register/aXjQb0YJIm24");
					Intent intent=new Intent(Intent.ACTION_VIEW,uri);
					startActivity(intent);
				}catch(Exception e){
					Toast.makeText(Login.this, "出现异常，请联系开发人员反馈BUG.", Toast.LENGTH_SHORT).show();
				}			
			}
        });
        
        ck_auto=(CheckBox)findViewById(R.id.ck_auto);
        
        
        
        et_usrname = (EditText)findViewById(R.id.et_usrname);
        et_usrpsw = (EditText)findViewById(R.id.et_usrpsw);
        
        
        
        
        et_usrname.setText(loginInfo.UserName);
//        et_usrpsw.setText(loginInfo.Password);
        
//        String LoginInfo=new AppCache(this).GetLoginInfo();
//        if(LoginInfo!=null){
//        	String[] lgInfo=LoginInfo.split(",");
//        	et_usrname.setText(lgInfo[0]);
//        	et_usrpsw.setText(lgInfo[1]);
//        	btn_login.setClickable(false);
////        	String usrname = et_usrname.getText().toString();
////			String usrpsw = et_usrpsw.getText().toString();
////        	doLogin(usrname,usrpsw);
//        }
        btn_login = (Button)findViewById(R.id.btn_login);
        btn_login.setOnClickListener(new OnClickListener() {			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String usrname = et_usrname.getText().toString();
				String usrpsw = et_usrpsw.getText().toString();
				
				if(usrname.equals("")){
					Toast.makeText(Login.this, "请输入用户名", Toast.LENGTH_SHORT).show();
				}
				else if(usrpsw.equals("")){
					Toast.makeText(Login.this, "请输入密码", Toast.LENGTH_SHORT).show();
				}
				else{
					doLogin(usrname, usrpsw);
				}
			}
		});
        
	    
    }
   
    
    @Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		 if(loginInfo.AutoCheckIn==1){
		        if((!loginInfo.UserName.equals(""))&&(!loginInfo.Password.equals(""))){
//		        	pDlg=new ProgressDialog(Login.this);
//		        	pDlg.setMessage("验证中...");
//		        	pDlg.show();
		        	if(canLogin(loginInfo.UserName,loginInfo.Password).equals("Done")){
		        		mCache.SaveLoginInfo(loginInfo);
		                
		                
		                FanDroidApplication.mUsername=loginInfo.UserName;
		        		FanDroidApplication.mPassword=loginInfo.Password;
		        		
		        		
		        		FanDroidApplication.mApi.setCredentials(loginInfo.UserName, loginInfo.Password);
//		        		pDlg.dismiss();
		        		
		        		
//		        		pDlg=new ProgressDialog(Login.this);
//		        		pDlg.setMessage("登录中...");
//		        		pDlg.show();
//		        		FanDroidApplication.mApi.setCredentials(loginInfo.UserName, loginInfo.Password);
//		        		
//		        		try{
//		        		JSONObject userName = FanDroidApplication.mApi.getUsrName(FanDroidApplication.mUsername, FanDroidApplication.mPassword);
//		        		BaseActivity.user_show_name=userName.getString("name");
//		        		BaseActivity.user_id=userName.getString("id");
//		        		} catch (IOException e11) {
//		        			// TODO Auto-generated catch block
//		        			e11.printStackTrace();
//		        		} catch (AuthException e11) {
//		        			// TODO Auto-generated catch block
//		        			e11.printStackTrace();
//		        		} catch (ApiException e11) {
//		        			// TODO Auto-generated catch block
//		        			e11.printStackTrace();
//		        		} catch (JSONException e11){
//		        			e11.printStackTrace();
//		        		}
//		        		pDlg.dismiss();
		        		//handler.sendEmptyMessage(0);
		    	        Intent intent = new Intent(this, FanDroidActivity.class); 
		    	        intent.putExtra("usrname", loginInfo.UserName);
		    	        intent.putExtra("usrpsw", loginInfo.Password);
		    	        startActivity(intent);
		    	        this.finish();
		        	}
		        	else{
//		        		pDlg.dismiss();
		        		//handler.sendEmptyMessage(0);
		        	
		        	}
		        	
		        }
	        }
	}


	private void doLogin(String usrname, String usrpsw){
//    	//调用API做登录处理,验证用户名和密码
    	pDlg=new ProgressDialog(Login.this);
    	pDlg.setMessage("验证中...");
    	pDlg.show();
    	if(canLogin(usrname, usrpsw).equals("Done")){
	    	//跳转到主activity页面
    		//new AppCache(this).SaveLogin(usrname, usrpsw);
    		et_usrname.setText("");
            et_usrpsw.setText("");
    		btn_login.setClickable(false);
    		et_usrname.setEnabled(false);
    		et_usrpsw.setEnabled(false);
    		ck_auto.setEnabled(false);
    		LoginInfo loginInfo=new LoginInfo();
    		loginInfo.UserName=usrname;
    		loginInfo.Password=usrpsw;
    		
    		if(ck_auto.isChecked()){
    			loginInfo.AutoCheckIn=1;
    		}
    		String username = et_usrname.getText().toString();
            String password = et_usrpsw.getText().toString();
            

           
            
            
            mCache.SaveLoginInfo(loginInfo);
           
            
            FanDroidApplication.mUsername=usrname;
    		FanDroidApplication.mPassword=usrpsw;
    		
    		
    		
//    		pDlg.dismiss();
    		handler.sendEmptyMessage(0);
    		
    		
    		pDlg=new ProgressDialog(Login.this);
    		pDlg.setMessage("登录中...");
    		pDlg.show();
    		FanDroidApplication.mApi.setCredentials(usrname, usrpsw);
    		
    		try{
    		JSONObject userName = FanDroidApplication.mApi.getUsrName(FanDroidApplication.mUsername, FanDroidApplication.mPassword);
    		BaseActivity.user_show_name=userName.getString("name");
    		BaseActivity.user_id=userName.getString("id");
    		} catch (IOException e11) {
    			// TODO Auto-generated catch block
    			e11.printStackTrace();
    		} catch (AuthException e11) {
    			// TODO Auto-generated catch block
    			e11.printStackTrace();
    		} catch (ApiException e11) {
    			// TODO Auto-generated catch block
    			e11.printStackTrace();
    		} catch (JSONException e11){
    			e11.printStackTrace();
    		}
//    		pDlg.dismiss();
    		handler.sendEmptyMessage(0);
    		
	        Intent intent = new Intent(this, FanDroidActivity.class); 
	        intent.putExtra("usrname", usrname);
	        intent.putExtra("usrpsw", usrpsw);
	        startActivity(intent);
	        this.finish();
    	}
    	else if(canLogin(usrname, usrpsw).equals("IOException")){
//    		pDlg.dismiss();
    		handler.sendEmptyMessage(0);
    		
    		Toast.makeText(Login.this, "网络连接出现问题", Toast.LENGTH_SHORT).show();
    	}   	
    	else {
//    		pDlg.dismiss();
    		handler.sendEmptyMessage(0);
    		
    		Toast.makeText(Login.this, "用户名或密码错误，请重新输入", Toast.LENGTH_SHORT).show();
    	}
    	//mLoginTask=new LoginTask().execute();
    }
    public String canLogin(String usrname, String usrpsw){
    	try {
			mApi.login(usrname, usrpsw);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.v("IOException",e.toString());
			return "IOException";
		} catch (AuthException e) {
			// TODO Auto-generated catch block
			Log.v("AuthException",e.toString());
			return "AuthException";
		} catch (ApiException e) {
			// TODO Auto-generated catch block
			Log.v("ApiException",e.toString());
			return "ApiException";
		}
    	return "Done";
    }
    
    
    
    
    
    
    
    

		
    
}