package com.android.fanfou;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

public class FanDroidCache {
	
	public  DBHelper dbHelper;
	public FanDroidCache(Context context){
		dbHelper=new DBHelper(context);
	}
	
	
	

	public final static DateFormat DB_DATE_FORMATTER = new SimpleDateFormat(
		      "yyyy-MM-dd' 'HH:mm", Locale.US);
	public final static DateFormat DB_DATE_FORMATTER2 = new SimpleDateFormat(
		      "yyyy-MM-dd' 'HH:mm:ss.SSS", Locale.US);
	

	private static final String DATABASE_NAME="FanDroid.db";
	private static final int DATABASE_VERSION=1;
	
	
	
	public static final String User_TABLE="UserInfo";
	
	
	
	
	public static final String LoginInfo_TABLE="LoginInfo";
	public static final String Login_UserName="userName";
	public static final String Login_Password="password";
	public static final String Login_AutoLogin="auto";
	public static final String Login_UpdateTime="UpdateTime";
	
	public static final String Create_LoginTable="CREATE TABLE "+LoginInfo_TABLE+"("+"_id autoinc,"+
	Login_UserName+" text primary key on conflict replace,"+
	Login_Password+" text not null,"+
	Login_AutoLogin+" integer not null,"+
	Login_UpdateTime+" date not null);";
	
	
	
	
	public static final String FDMsg_TABLE="FanDroidActivity";
	public static final String UserFDMsg_TABLE="FDSpaceActivity";
	public static final String AtMeMsg_TABLE="AtMeActivity";
	public static final String FDMsg_id="fd_id";
	public static final String FDMsg_curUser="fd_curUser";
	public static final String FDMsg_fav="fd_fav";
	public static final String FDMsg_text="fd_text";
	public static final String FDMsg_createdAt="fd_createdAt";
	public static final String FDMsg_source="fd_source";
	public static final String FDMsg_profileImg="fd_pImg";
	public static final String FDMsg_userName="fd_userName";
	public static final String FDMsg_userId="fd_userId";
	public static final String FDMsg_hasPhoto="fd_hasPhoto";
	public static final String FDMsg_largeUrl="fd_largeUrl";
	
	public static final String Create_FDMsgTable="CREATE TABLE "+FDMsg_TABLE+"("+FDMsg_id+" text primary key on conflict replace,"+
	FDMsg_curUser+" text not null,"+
	FDMsg_fav+" short not null,"+
	FDMsg_text+" text not null,"+
	FDMsg_createdAt+" date not null,"+
	FDMsg_source+" text not null,"+
	FDMsg_profileImg+" text not null,"+
	FDMsg_userName+" text not null,"+
	FDMsg_userId+" text not null,"+
	FDMsg_hasPhoto+" short not null,"+
	FDMsg_largeUrl+" text);";
	
	
	public static final String Create_AtMeMsgTable="CREATE TABLE "+AtMeMsg_TABLE+"("+FDMsg_id+" text primary key on conflict replace,"+
	FDMsg_curUser+" text not null,"+
	FDMsg_fav+" short not null,"+
	FDMsg_text+" text not null,"+
	FDMsg_createdAt+" date not null,"+
	FDMsg_source+" text not null,"+
	FDMsg_profileImg+" text not null,"+
	FDMsg_userName+" text not null,"+
	FDMsg_userId+" text not null,"+
	FDMsg_hasPhoto+" short not null,"+
	FDMsg_largeUrl+" text);";
	
	
	public static final String Create_UserFDMsgTable="CREATE TABLE "+UserFDMsg_TABLE+"("+FDMsg_id+" text primary key on conflict replace,"+
	FDMsg_curUser+" text not null,"+
	FDMsg_fav+" short not null,"+
	FDMsg_text+" text not null,"+
	FDMsg_createdAt+" date not null,"+
	FDMsg_source+" text not null,"+
	FDMsg_profileImg+" text not null,"+
	FDMsg_userName+" text not null,"+
	FDMsg_userId+" text not null,"+
	FDMsg_hasPhoto+" short not null,"+
	FDMsg_largeUrl+" text);";
	
	
	
	
	
	
	
	
	public static final String Img_id="id";
	public static final String Img_url="url";
	public static final String Img_uri="uri";
	
	public static final String Img_Talbe="ImgInfo";
	
	public static final String Create_Img="CREATE TABLE "+Img_Talbe+"("+Img_id+" INTEGER PRIMARY KEY AUTOINCREMENT,"+
	Img_url+" text not null);";
	
	
	public boolean InsertImg(String url){
		String insertImgUri="insert into "+Img_Talbe+"("+Img_url+")values('"+url+"');";
		if(dbHelper.ExecSQL(insertImgUri)){
			return true;
		}
		else
			return false;
	}
	public boolean isImgExist(String url){
		String checkUrl="select * from "+Img_Talbe+" where "+Img_url+"='"+url+"'";
		Cursor cursor=dbHelper.query(checkUrl, null);
		if(cursor.getCount()==0){
			
			return false;
		}
		else{
			
			return true;
		}
	}
	
	public int getImgSq(String url){
		String getImgUri="select " +Img_id+
				" from "+Img_Talbe+" where "+ Img_url+"='"+url+"'";
		Cursor cursor=dbHelper.query(getImgUri, null);
		
		cursor.moveToFirst();
		int ImgSq=cursor.getInt(cursor.getColumnIndex(Img_id));
		
		return ImgSq;
	}
	
	
	//////////以下为登录信息表处理////////////////////////////////////
	public boolean SaveLoginInfo(LoginInfo loginInfo){
		if(loginInfo.UserName.equals("")&&loginInfo.Password.equals("")){
			return false;
		}
		
		String insertLoginInfo="insert into "+LoginInfo_TABLE+"( "+
		Login_UserName+","+Login_Password+","+Login_AutoLogin+","+Login_UpdateTime+
		")values('"+loginInfo.UserName+"','"+loginInfo.Password+"','"+loginInfo.AutoCheckIn+"','"+DB_DATE_FORMATTER.format(new Date())+"');";
		if(dbHelper.ExecSQL(insertLoginInfo)){
			return true;
		}
		else{
			return false;
		}
	}
	///如果该函数返回true将不执行之后的数据保存操作
	public boolean isCheckedIn(LoginInfo loginInfo){
		if(loginInfo.UserName.equals("")&&loginInfo.Password.equals("")){
			return true;
		}
		String getLoginInfo="select * from "+LoginInfo_TABLE;
		Cursor cursor=dbHelper.query(getLoginInfo, null);
		if(cursor.getCount()==0){
			return false;
		}
		else{
			
			
			while(cursor.moveToNext()){
				String UserName=cursor.getString(cursor.getColumnIndex(Login_UserName));
				if(UserName.equals(loginInfo.UserName)){
					return true;
				}
			}
			return false;
		}
	}
	
	public LoginInfo getLoginInfo(){
		String getLoginInfo="select * from "+LoginInfo_TABLE+" order by "+Login_UpdateTime+" desc";
		try{
			Cursor cursor=dbHelper.query(getLoginInfo, null);
			if(cursor.moveToFirst()){
				LoginInfo loginInfo=new LoginInfo();
				loginInfo.UserName=cursor.getString(cursor.getColumnIndex(Login_UserName));
				loginInfo.Password=cursor.getString(cursor.getColumnIndex(Login_Password));
				loginInfo.AutoCheckIn=cursor.getShort(cursor.getColumnIndex(Login_AutoLogin));
				return loginInfo;
			}
			else{
				return new LoginInfo();
			}
		}
		catch(Exception e){
			return new LoginInfo();
		}
		
	}
	/////////////////////////////////////////////////////////////////////////////////////////
	
	
	
	
//	public byte[] getBitmapByte(Bitmap bitmap){
//        ByteArrayOutputStream out = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.JPEG, 50, out);
////        try {
////            out.flush();
////            out.close();
////        } catch (IOException e) {
////            e.printStackTrace();
////        }
//        return out.toByteArray();
//    }
//
//	public Bitmap getBitmapFromByte(byte[] temp){
//        if(temp != null){
//            Bitmap bitmap = BitmapFactory.decodeByteArray(temp, 0, temp.length);
//            return bitmap;
//        }else{
//            return null;
//        }
//    }
//
//
//	
	
	//////////以下为消息信息表处理//////////////////////////////////////////////////////////////
	
	public boolean InsertFDMsg(DBMsg msg){
//		byte[] tmp=msg.profileImage;
//		String imgStr=null;
//		try{
//			imgStr=new String(tmp,"UTF-16");}
//		catch(Exception e){
//			imgStr=new String();
//		}

		String  insertFDMsg="insert into "+FDMsg_TABLE+"("+
		FDMsg_id+","+FDMsg_curUser+","+FDMsg_fav+","+FDMsg_text+","+
		FDMsg_createdAt+","+FDMsg_source+","+FDMsg_profileImg+","+
		FDMsg_userName+","+FDMsg_userId+","+FDMsg_hasPhoto+","+FDMsg_largeUrl+
		")values('"+
		msg.id+"','"+msg.currentUser+"','"+msg.fav+"','"+msg.text+"','"+DB_DATE_FORMATTER2.format(msg.createdAt)+"','"+
		msg.Source+"','"+msg.profileImage+"','"+msg.screenName+"','"+msg.userId+"','"+msg.hasPhoto+"','"+msg.largeUrl+"');";
		
		
		
		
		
		
		if(dbHelper.ExecSQL(insertFDMsg)){
			return true;
		}
		else{
			return false;
		}
		
	}
	
	
	
	
	public boolean InsertUserFDMsg(DBMsg msg){
	

		String  insertUserFDMsg="insert into "+UserFDMsg_TABLE+"("+
		FDMsg_id+","+FDMsg_curUser+","+FDMsg_fav+","+FDMsg_text+","+
		FDMsg_createdAt+","+FDMsg_source+","+FDMsg_profileImg+","+
		FDMsg_userName+","+FDMsg_userId+","+FDMsg_hasPhoto+","+FDMsg_largeUrl+
		")values('"+
		msg.id+"','"+msg.currentUser+"','"+msg.fav+"','"+msg.text+"','"+DB_DATE_FORMATTER2.format(msg.createdAt)+"','"+
		msg.Source+"','"+msg.profileImage+"','"+msg.screenName+"','"+msg.userId+"','"+msg.hasPhoto+"','"+msg.largeUrl+"');";
		
		
		
		if(dbHelper.ExecSQL(insertUserFDMsg)){
			return true;
		}
		else{
			return false;
		}
		
	}
	
	
	public boolean InsertAtMeMsg(DBMsg msg){
		

		String  insertUserFDMsg="insert into "+AtMeMsg_TABLE+"("+
		FDMsg_id+","+FDMsg_curUser+","+FDMsg_fav+","+FDMsg_text+","+
		FDMsg_createdAt+","+FDMsg_source+","+FDMsg_profileImg+","+
		FDMsg_userName+","+FDMsg_userId+","+FDMsg_hasPhoto+","+FDMsg_largeUrl+
		")values('"+
		msg.id+"','"+msg.currentUser+"','"+msg.fav+"','"+msg.text+"','"+DB_DATE_FORMATTER2.format(msg.createdAt)+"','"+
		msg.Source+"','"+msg.profileImage+"','"+msg.screenName+"','"+msg.userId+"','"+msg.hasPhoto+"','"+msg.largeUrl+"');";
		
		
		
		if(dbHelper.ExecSQL(insertUserFDMsg)){
			return true;
		}
		else{
			return false;
		}
		
	}
	
	
	
	
	/////更改数据///////////////////
	public boolean updateFDMsg(String curUser,String keyName,int value,String resKey,String resVal){
		String updateFDMsg="update "+FDMsg_TABLE+" set "+keyName+"="+value+" where "+
		resKey+"='"+resVal+"'";
		if(dbHelper.ExecSQL(updateFDMsg)){
			return true;
		}
		else{
			return false;
		}
		
		
	}
	public boolean updateUsersg(String curUser,String keyName,int value,String resKey,String resVal){
		String updateFDMsg="update "+UserFDMsg_TABLE+" set "+keyName+"="+value+" where "+
		resKey+"='"+resVal+"'";
		if(dbHelper.ExecSQL(updateFDMsg)){
			return true;
		}
		else{
			return false;
		}
		
		
	}
	public boolean updateAtMeMsg(String curUser,String keyName,int value,String resKey,String resVal){
		String updateFDMsg="update "+AtMeMsg_TABLE+" set "+keyName+"="+value+" where "+
		resKey+"='"+resVal+"'";
		if(dbHelper.ExecSQL(updateFDMsg)){
			return true;
		}
		else{
			return false;
		}
		
		
	}
	
	
	
	/////根据用户名得到指定用户（存在好友关系）的消息/////////////////////////
	public DBMsg[] getUserFDMsg(String username,String userID){

		String getFDMsg="select * from "+UserFDMsg_TABLE+" where "+FDMsg_curUser+"='"+username+
		"' AND "+FDMsg_userId+"='"+userID+"'" +" order by "+FDMsg_createdAt+" desc";
		Cursor cursor=null;
		try {
			cursor=dbHelper.query(getFDMsg, null);
		    int i=cursor.getCount();
		    if(i==0){
		    	return null;
		    }
		    
		    DBMsg[] msg=new DBMsg[i];
		    DBMsg[] rtMsg=new DBMsg[i];
		    
		    i=0;
		    cursor.moveToFirst();
		    cursor.moveToPrevious();
		    
		    
		   
		    
		    
		    
		    while(cursor.moveToNext()){
		    	msg[i]=new DBMsg();
		    	msg[i].id=cursor.getString(cursor.getColumnIndex(FDMsg_id));
		    	msg[i].currentUser=cursor.getString(cursor.getColumnIndex(FDMsg_curUser));
		    	msg[i].fav=cursor.getShort(cursor.getColumnIndex(FDMsg_fav));
		    	msg[i].text=cursor.getString(cursor.getColumnIndex(FDMsg_text));
		    	msg[i].createdAt=DB_DATE_FORMATTER.parse(cursor.getString(cursor.getColumnIndex(FDMsg_createdAt)));
		    	msg[i].Source=cursor.getString(cursor.getColumnIndex(FDMsg_source));
		    	
		    	msg[i].profileImage=cursor.getString(cursor.getColumnIndex(FDMsg_profileImg));
		    	msg[i].screenName=cursor.getString(cursor.getColumnIndex(FDMsg_userName));
		    	msg[i].userId=cursor.getString(cursor.getColumnIndex(FDMsg_userId));
		    	msg[i].hasPhoto=cursor.getShort(cursor.getColumnIndex(FDMsg_hasPhoto));
		    	msg[i].largeUrl=cursor.getString(cursor.getColumnIndex(FDMsg_largeUrl));
		    	i++;
		    	
		    }
		  
		    for(int j=0;j<i;j++){
		    	
		    	rtMsg[j]=msg[j];
		    	
		    }
		    return rtMsg;
		}
		catch(Exception e){
			return null;
		}
		
	
	}
	
	
	public DBMsg[] getUserFDMsg(String username,String userID,String sinceID){

		String getFDMsg="select * from "+UserFDMsg_TABLE+" where "+FDMsg_curUser+"='"+username+
		"' AND "+FDMsg_userId+"='"+userID+"'" +" order by "+FDMsg_createdAt+" desc";
		Cursor cursor=null;
		try {
			cursor=dbHelper.query(getFDMsg, null);
		    int i=cursor.getCount();
		    if(i==0){
		    	return null;
		    }
		    
		    DBMsg[] msg=new DBMsg[i];
		    DBMsg[] rtMsg=new DBMsg[i];
		    
		    i=0;
		    cursor.moveToFirst();
		    cursor.moveToPrevious();
		    
		    
		   
		    
		    
		    
		    while(cursor.moveToNext()){
		    	msg[i]=new DBMsg();
		    	msg[i].id=cursor.getString(cursor.getColumnIndex(FDMsg_id));
		    	msg[i].currentUser=cursor.getString(cursor.getColumnIndex(FDMsg_curUser));
		    	msg[i].fav=cursor.getShort(cursor.getColumnIndex(FDMsg_fav));
		    	msg[i].text=cursor.getString(cursor.getColumnIndex(FDMsg_text));
		    	msg[i].createdAt=DB_DATE_FORMATTER.parse(cursor.getString(cursor.getColumnIndex(FDMsg_createdAt)));
		    	msg[i].Source=cursor.getString(cursor.getColumnIndex(FDMsg_source));
		    	
		    	msg[i].profileImage=cursor.getString(cursor.getColumnIndex(FDMsg_profileImg));
		    	msg[i].screenName=cursor.getString(cursor.getColumnIndex(FDMsg_userName));
		    	msg[i].userId=cursor.getString(cursor.getColumnIndex(FDMsg_userId));
		    	msg[i].hasPhoto=cursor.getShort(cursor.getColumnIndex(FDMsg_hasPhoto));
		    	msg[i].largeUrl=cursor.getString(cursor.getColumnIndex(FDMsg_largeUrl));
		    	i++;
		    	
		    }
		  
		    for(int j=0;j<i;j++){
		    	
		    	rtMsg[j]=msg[j];
		    	
		    }
		    int Start=0;
		    
		    for(int c=0;c<rtMsg.length;c++){
		    	if(rtMsg[c].id.equals(sinceID)){
		    		Start=c;
		    	}
		    	
		    }
		    
		    int length=rtMsg.length-Start;
		    DBMsg[] newRtMsg=new DBMsg[length];
		    for(int t=0;t<length;t++){
		    	newRtMsg[t]=rtMsg[t+Start];
		    }
		    return newRtMsg;
		}
		catch(Exception e){
			return null;
		}
		
	
	}
	

	//需要传递的参数包括当前登录用户的用户名以及需要返回的Msg的条数
	public DBMsg[] getFDMsg(String curName){
		String getFDMsg="select * from "+FDMsg_TABLE+" where "+FDMsg_curUser+"='"+curName+"' order by "+FDMsg_createdAt+" desc";
		Cursor cursor=null;
		try {
			cursor=dbHelper.query(getFDMsg, null);
		    int i=cursor.getCount();
		    if(i==0){
		    	return null;
		    }
		    
		    DBMsg[] msg=new DBMsg[i];
		    DBMsg[] rtMsg=new DBMsg[i];
		    
		    i=0;
		    cursor.moveToFirst();
		    cursor.moveToPrevious();
		    
		    
		   
		    
		    
		    
		    while(cursor.moveToNext()){
		    	msg[i]=new DBMsg();
		    	msg[i].id=cursor.getString(cursor.getColumnIndex(FDMsg_id));
		    	msg[i].currentUser=cursor.getString(cursor.getColumnIndex(FDMsg_curUser));
		    	msg[i].fav=cursor.getShort(cursor.getColumnIndex(FDMsg_fav));
		    	msg[i].text=cursor.getString(cursor.getColumnIndex(FDMsg_text));
		    	msg[i].createdAt=DB_DATE_FORMATTER.parse(cursor.getString(cursor.getColumnIndex(FDMsg_createdAt)));
		    	msg[i].Source=cursor.getString(cursor.getColumnIndex(FDMsg_source));
		    	
		    	msg[i].profileImage=cursor.getString(cursor.getColumnIndex(FDMsg_profileImg));
		    	msg[i].screenName=cursor.getString(cursor.getColumnIndex(FDMsg_userName));
		    	msg[i].userId=cursor.getString(cursor.getColumnIndex(FDMsg_userId));
		    	msg[i].hasPhoto=cursor.getShort(cursor.getColumnIndex(FDMsg_hasPhoto));
		    	msg[i].largeUrl=cursor.getString(cursor.getColumnIndex(FDMsg_largeUrl));
		    	i++;
		    	
		    }
		  
		    for(int j=0;j<i;j++){
		    	
		    	rtMsg[j]=msg[j];
		    	
		    }
		    return rtMsg;
		}
		catch(Exception e){
			return null;
		}
		
	}
	
	
	///得到从sinceID开始的所有msg
	public DBMsg[] getFDMsg(String username,String sinceID){
		String getFDMsg="select * from "+FDMsg_TABLE+" where "+FDMsg_curUser+"='"+username+"' order by "+FDMsg_createdAt+" desc";
		Cursor cursor=null;
		try {
			cursor=dbHelper.query(getFDMsg, null);
		    int i=cursor.getCount();
		    if(i==0){
		    	return null;
		    }
		    
		    DBMsg[] msg=new DBMsg[i];
		    DBMsg[] rtMsg=new DBMsg[i];
		    
		    i=0;
		    cursor.moveToFirst();
		    cursor.moveToPrevious();
		    
		    
		   
		    
		    
		    
		    while(cursor.moveToNext()){
		    	msg[i]=new DBMsg();
		    	msg[i].id=cursor.getString(cursor.getColumnIndex(FDMsg_id));
		    	msg[i].currentUser=cursor.getString(cursor.getColumnIndex(FDMsg_curUser));
		    	msg[i].fav=cursor.getShort(cursor.getColumnIndex(FDMsg_fav));
		    	msg[i].text=cursor.getString(cursor.getColumnIndex(FDMsg_text));
		    	msg[i].createdAt=DB_DATE_FORMATTER.parse(cursor.getString(cursor.getColumnIndex(FDMsg_createdAt)));
		    	msg[i].Source=cursor.getString(cursor.getColumnIndex(FDMsg_source));
		    	
		    	msg[i].profileImage=cursor.getString(cursor.getColumnIndex(FDMsg_profileImg));
		    	msg[i].screenName=cursor.getString(cursor.getColumnIndex(FDMsg_userName));
		    	msg[i].userId=cursor.getString(cursor.getColumnIndex(FDMsg_userId));
		    	msg[i].hasPhoto=cursor.getShort(cursor.getColumnIndex(FDMsg_hasPhoto));
		    	msg[i].largeUrl=cursor.getString(cursor.getColumnIndex(FDMsg_largeUrl));
		    	i++;
		    	
		    }
		  
		    for(int j=0;j<i;j++){
		    	
		    	rtMsg[j]=msg[j];
		    	
		    }
		    int Start=0;
		    
		    for(int c=0;c<rtMsg.length;c++){
		    	if(rtMsg[c].id.equals(sinceID)){
		    		Start=c;
		    	}
		    	
		    }
		    
		    int length=rtMsg.length-Start;
		    DBMsg[] newRtMsg=new DBMsg[length];
		    for(int t=0;t<length;t++){
		    	newRtMsg[t]=rtMsg[t+Start];
		    }
		    return newRtMsg;
		}
		catch(Exception e){
			return null;
		}
		finally{
			cursor.close();
		}
	}
	
	
	
	//需要传递的参数包括当前登录用户的用户名以及需要返回的Msg的条数
	public DBMsg[] getAtMeMsg(String curName){
		String getFDMsg="select * from "+AtMeMsg_TABLE+" where "+FDMsg_curUser+"='"+curName+"' order by "+FDMsg_createdAt+" desc";
		Cursor cursor=null;
		try {
			cursor=dbHelper.query(getFDMsg, null);
		    int i=cursor.getCount();
		    if(i==0){
		    	return null;
		    }
		    
		    DBMsg[] msg=new DBMsg[i];
		    DBMsg[] rtMsg=new DBMsg[i];
		    
		    i=0;
		    cursor.moveToFirst();
		    cursor.moveToPrevious();
		    
		    
		   
		    
		    
		    
		    while(cursor.moveToNext()){
		    	msg[i]=new DBMsg();
		    	msg[i].id=cursor.getString(cursor.getColumnIndex(FDMsg_id));
		    	msg[i].currentUser=cursor.getString(cursor.getColumnIndex(FDMsg_curUser));
		    	msg[i].fav=cursor.getShort(cursor.getColumnIndex(FDMsg_fav));
		    	msg[i].text=cursor.getString(cursor.getColumnIndex(FDMsg_text));
		    	msg[i].createdAt=DB_DATE_FORMATTER.parse(cursor.getString(cursor.getColumnIndex(FDMsg_createdAt)));
		    	msg[i].Source=cursor.getString(cursor.getColumnIndex(FDMsg_source));
		    	
		    	msg[i].profileImage=cursor.getString(cursor.getColumnIndex(FDMsg_profileImg));
		    	msg[i].screenName=cursor.getString(cursor.getColumnIndex(FDMsg_userName));
		    	msg[i].userId=cursor.getString(cursor.getColumnIndex(FDMsg_userId));
		    	msg[i].hasPhoto=cursor.getShort(cursor.getColumnIndex(FDMsg_hasPhoto));
		    	msg[i].largeUrl=cursor.getString(cursor.getColumnIndex(FDMsg_largeUrl));
		    	i++;
		    	
		    }
		  
		    for(int j=0;j<i;j++){
		    	
		    	rtMsg[j]=msg[j];
		    	
		    }
		    return rtMsg;
		}
		catch(Exception e){
			return null;
		}
		
	}
	
	
	///得到从sinceID开始的所有msg
	public DBMsg[] getAtMeMsg(String curName,String sinceID){
		String getFDMsg="select * from "+AtMeMsg_TABLE+" where "+FDMsg_curUser+"='"+curName+"' order by "+FDMsg_createdAt+" desc";
		Cursor cursor=null;
		try {
			cursor=dbHelper.query(getFDMsg, null);
		    int i=cursor.getCount();
		    if(i==0){
		    	return null;
		    }
		    
		    DBMsg[] msg=new DBMsg[i];
		    DBMsg[] rtMsg=new DBMsg[i];
		    
		    i=0;
		    cursor.moveToFirst();
		    cursor.moveToPrevious();
		    
		    
		   
		    
		    
		    
		    while(cursor.moveToNext()){
		    	msg[i]=new DBMsg();
		    	msg[i].id=cursor.getString(cursor.getColumnIndex(FDMsg_id));
		    	msg[i].currentUser=cursor.getString(cursor.getColumnIndex(FDMsg_curUser));
		    	msg[i].fav=cursor.getShort(cursor.getColumnIndex(FDMsg_fav));
		    	msg[i].text=cursor.getString(cursor.getColumnIndex(FDMsg_text));
		    	msg[i].createdAt=DB_DATE_FORMATTER.parse(cursor.getString(cursor.getColumnIndex(FDMsg_createdAt)));
		    	msg[i].Source=cursor.getString(cursor.getColumnIndex(FDMsg_source));
		    	
		    	msg[i].profileImage=cursor.getString(cursor.getColumnIndex(FDMsg_profileImg));
		    	msg[i].screenName=cursor.getString(cursor.getColumnIndex(FDMsg_userName));
		    	msg[i].userId=cursor.getString(cursor.getColumnIndex(FDMsg_userId));
		    	msg[i].hasPhoto=cursor.getShort(cursor.getColumnIndex(FDMsg_hasPhoto));
		    	msg[i].largeUrl=cursor.getString(cursor.getColumnIndex(FDMsg_largeUrl));
		    	i++;
		    	
		    }
		  
		    for(int j=0;j<i;j++){
		    	
		    	rtMsg[j]=msg[j];
		    	
		    }
		    int Start=0;
		    
		    for(int c=0;c<rtMsg.length;c++){
		    	if(rtMsg[c].id.equals(sinceID)){
		    		Start=c;
		    	}
		    	
		    }
		    
		    int length=rtMsg.length-Start;
		    DBMsg[] newRtMsg=new DBMsg[length];
		    for(int t=0;t<length;t++){
		    	newRtMsg[t]=rtMsg[t+Start];
		    }
		    return newRtMsg;
		}
		catch(Exception e){
			return null;
		}
		finally{
			cursor.close();
		}
	}
	
	
	
	
	
	
	
	public boolean isAtMeMsgidExist(String curName,String id){
		String SelectId="select * from "+AtMeMsg_TABLE+" where "+FDMsg_curUser+"='"+curName+
		"' AND "+FDMsg_id+"='"+id+"'";
		
		
		Cursor cursor=null;
		try {
			cursor=dbHelper.query(SelectId, null);
			if(cursor.getCount()==0){
				return false;
			}
			else{
				return true;
			}
		}
		catch(Exception e){
			return false;
		}
		
	}
	
	
	
	public boolean isUserMsgidExist(String username,String id){
		String SelectId="select * from "+UserFDMsg_TABLE+" where "+FDMsg_curUser+"='"+username+
		"' AND "+FDMsg_id+"='"+id+"'";
		
		
		Cursor cursor=null;
		try {
			cursor=dbHelper.query(SelectId, null);
			if(cursor.getCount()==0){
				return false;
			}
			else{
				return true;
			}
		}
		catch(Exception e){
			return false;
		}
		
	}
	
	
	
	public boolean isMsgidExist(String username,String id){
		String SelectId="select * from "+FDMsg_TABLE+" where "+FDMsg_curUser+"='"+username+
		"' AND "+FDMsg_id+"='"+id+"'";
		Cursor cursor=null;
		try {
			cursor=dbHelper.query(SelectId, null);
			if(cursor.getCount()==0){
				return false;
			}
			else{
				return true;
			}
		}
		catch(Exception e){
			return false;
		}
		
	}
	
	
	

	
	public String getMaxIdInDB(String username){
		String getFDMsg="select * from "+FDMsg_TABLE+" where "+FDMsg_curUser+"='"+username+"' order by "+FDMsg_createdAt+" desc";
		Cursor cursor=null;
		try{
			cursor=dbHelper.query(getFDMsg, null);
			if(!cursor.moveToFirst()){
				return null;
			}
			else{
				return cursor.getString(cursor.getColumnIndex(FDMsg_id));
			}
		}
		catch(Exception e){
			return null;
		}
		finally{
			cursor.close();
		}
	}
	public String getMinIdInDB(String username){
		String getFDMsg="select * from "+FDMsg_TABLE+" where "+FDMsg_curUser+"='"+username+"' order by "+FDMsg_createdAt;
		
		Cursor cursor=null;
		try{
			cursor=dbHelper.query(getFDMsg, null);
			if(!cursor.moveToFirst()){
				return null;
			}
			else{
				return cursor.getString(cursor.getColumnIndex(FDMsg_id));
			}
		}
		catch(Exception e){
			return null;
		}
		
	}
	
	////////////////////////////////////////////////////////////////////////////////////////////
	
	
	
	
	
	
	
	private class DBHelper extends SQLiteOpenHelper{
		
		public DBHelper(Context context) {
			super(context, DATABASE_NAME, null,DATABASE_VERSION);
			// TODO Auto-generated constructor stub
		}


		@Override
		public void onCreate(SQLiteDatabase db) {
			// TODO Auto-generated method stub
			db.execSQL(Create_LoginTable);
			db.execSQL(Create_FDMsgTable);
			db.execSQL(Create_UserFDMsgTable);
			db.execSQL(Create_Img);
			db.execSQL(Create_AtMeMsgTable);
		}
	
	
	
		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			// TODO Auto-generated method stub
			 db.execSQL("DROP TABLE IF EXISTS "+Create_LoginTable);
			 db.execSQL("DROP TABLE IF EXISTS "+Create_FDMsgTable);
			 db.execSQL("DROP TABLE IF EXISTS "+Create_UserFDMsgTable);
			 db.execSQL("DROP TABLE IF EXISTS "+Create_Img);
			 db.execSQL("DROP TABLE IF EXISTS "+Create_AtMeMsgTable);
		}
	
		
		public Cursor query(String sql,String[] args){
			
			SQLiteDatabase db=this.getWritableDatabase();
			Cursor cursor=db.rawQuery(sql,args);
//			cursor.close();
			return cursor;
			
		}
		
		public boolean ExecSQL(String sql){
			SQLiteDatabase db=this.getWritableDatabase();
			try{
				db.execSQL(sql);
				
			}
			catch(Exception e){
				db.close();
				System.out.print(e.getMessage());
				return false;
			}
			db.close();
			return true;
		}
		
	}
}
