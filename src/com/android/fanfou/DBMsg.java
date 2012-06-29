package com.android.fanfou;

import java.util.Date;

import android.graphics.Bitmap;

public class DBMsg {
	public String id;
	public String currentUser;//调用getApi().getUserName()
	public short fav;
	public String text;
	public Date createdAt;
	public String Source;
	public String profileImage;//本地uri
	public String screenName;
	public String userId;
	public short hasPhoto=0;//0:false; 1: true;
	public String largeUrl="";

}
