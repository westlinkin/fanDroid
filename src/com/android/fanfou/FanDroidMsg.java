package com.android.fanfou;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

public class FanDroidMsg extends Message {
  @SuppressWarnings("unused")
  private static final String TAG = "Tweet";

  public String source;

  public boolean isReply() {
    // TODO: this is so wrong.
    String username = FanDroidApplication.mApi.getUsername();
    Pattern namePattern = Pattern.compile("\\B\\@\\Q" + username + "\\E\\b");
    Matcher matcher = namePattern.matcher(text);

    return matcher.find();
  }

  public static FanDroidMsg create(JSONObject jsonObject) throws JSONException {
    FanDroidMsg FDMsg = new FanDroidMsg();

    FDMsg.id = jsonObject.getString("id") + "";
    FDMsg.text = Utils.decodeTwitterJson(jsonObject.getString("text"));
    FDMsg.createdAt = Utils.parseDateTime(jsonObject.getString("created_at"));

    JSONObject user = jsonObject.getJSONObject("user");
    FDMsg.screenName = Utils.decodeTwitterJson(user.getString("screen_name"));
    FDMsg.profileImageUrl = user.getString("profile_image_url");
    FDMsg.userId = user.getString("id");
    FDMsg.source = Utils.decodeTwitterJson(jsonObject.getString("source")).
        replaceAll("\\<.*?>", "");

    return FDMsg;
  }

  public static FanDroidMsg createFromSearchApi(JSONObject jsonObject) throws JSONException {
    FanDroidMsg FDMsg = new FanDroidMsg();

    FDMsg.id = jsonObject.getString("id") + "";
    FDMsg.text = Utils.decodeTwitterJson(jsonObject.getString("text"));
    FDMsg.createdAt = Utils.parseSearchApiDateTime(jsonObject.getString("created_at"));

    FDMsg.screenName = Utils.decodeTwitterJson(jsonObject.getString("from_user"));
    FDMsg.profileImageUrl = jsonObject.getString("profile_image_url");
    FDMsg.userId = jsonObject.getString("from_user_id");
    FDMsg.source = Utils.decodeTwitterJson(jsonObject.getString("source")).
        replaceAll("\\<.*?>", "");

    return FDMsg;
  }

  public static String buildMetaText(StringBuilder builder,
      Date createdAt, String source) {
    builder.setLength(0);

    builder.append(Utils.getRelativeDate(createdAt));
    builder.append(" ");

    builder.append("·¢ËÍ×Ô ");
    builder.append(source);

    return builder.toString();
  }

}
