package com.android.fanfou;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.auth.AuthSchemeRegistry;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.auth.BasicSchemeFactory;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

public class FanfouApi {
        private static final String TAG = "FanfouApi";

        private static final String UPDATE_URL = "http://api.fanfou.com/statuses/update.json";
        private static final String FAVORITES_URL = "http://api.fanfou.com/favorites.json";
        private static final String ADD_FAV_URL = "http://api.fanfou.com/favorites/create/%s.json";
        private static final String DEL_FAV_URL = "http://api.fanfou.com/favorites/destroy/%s.json";
        private static final String VERIFY_CREDENTIALS_URL = "http://api.fanfou.com/account/verify_credentials.json";
        private static final String FRIENDS_TIMELINE_URL = "http://api.fanfou.com/statuses/friends_timeline.json";
        private static final String REPLIES_URL = "http://api.fanfou.com/statuses/mentions.json";
        private static final String DIRECT_MESSAGES_URL = "http://api.fanfou.com/direct_messages.json";
        private static final String DIRECT_MESSAGES_SENT_URL = "http://api.fanfou.com/direct_messages/sent.json";
        private static final String DIRECT_MESSAGES_DESTROY_URL = "http://api.fanfou.com/direct_messages/destroy/%s.json";
        private static final String DIRECT_MESSAGES_NEW_URL = "http://api.fanfou.com/direct_messages/new.json";
        private static final String FOLLOWERS_IDS_URL = "http://api.fanfou.com/followers/ids.json";
        private static final String FRIENDS_IDS_URL = "http://api.fanfou.com/friends/ids.json";
        private static final String FOLLOWING_URL = "http://api.fanfou.com/users/friends.json";
        private static final String FOLLOWER_URL = "http://api.fanfou.com/users/followers.json";
        private static final String USER_TIMELINE_URL = "http://api.fanfou.com/statuses/user_timeline.json";
        private static final String FRIENDSHIPS_EXISTS_URL = "http://api.fanfou.com/friendships/exists.json";
        private static final String FRIENDSHIPS_CREATE_URL = "http://api.fanfou.com/friendships/create/%s.json";
        private static final String FRIENDSHIPS_DESTROY_URL = "http://api.fanfou.com/friendships/destroy/%s.json";
        private static final String SEARCH_URL = "http://api.fanfou.com/search/public_timeline.json";
        private static final String PUBLIC_TIMELINE_URL = "http://api.fanfou.com/statuses/public_timeline.json";
        private static final String USER_INFO_URL = "http://api.fanfou.com/users/show.json";
        private static final String HOT_WORD_URL = "http://api.fanfou.com/trends.json";
        
        private static final String UPLOAD_AND_POST_URL = "http://api.fanfou.com/photos/upload.json";

        private static final String FANFOU_HOST = "api.fanfou.com";
        
     
        private static final String FANFOU_SOURCE = "FanDroid";

        private DefaultHttpClient mClient;
        private AuthScope mAuthScope;

        private String mUsername;
        private String mPassword;

        private static final String METHOD_GET = "GET";
        private static final String METHOD_POST = "POST";
        private static final String METHOD_DELETE = "DELETE";

        public static final int RETRIEVE_LIMIT = 20;  
        
        public class AuthException extends Exception {
                private static final long serialVersionUID = 1703735789572778599L;
        }

        public class ApiException extends Exception {
                public int mCode;

                public ApiException(int code, String string) {
                        super(string);

                        mCode = code;
                }

                private static final long serialVersionUID = -3755642135241860532L;
        }

        private static final int CONNECTION_TIMEOUT_MS = 30 * 1000;
        private static final int SOCKET_TIMEOUT_MS = 30 * 1000;

        public FanfouApi() {
                prepareHttpClient();
        }

        public static boolean isValidCredentials(String username, String password) {
                return !Utils.isEmpty(username) && !Utils.isEmpty(password);
        }

        public boolean isLoggedIn() {
                return isValidCredentials(mUsername, mPassword);
        }

        public String getUsername() {
                return mUsername;
        }

        private void prepareHttpClient() {
                mAuthScope = new AuthScope(FANFOU_HOST, AuthScope.ANY_PORT);
                mClient = new DefaultHttpClient();
                BasicScheme basicScheme = new BasicScheme();
                AuthSchemeRegistry authRegistry = new AuthSchemeRegistry();
                authRegistry.register(basicScheme.getSchemeName(),
                                new BasicSchemeFactory());
                mClient.setAuthSchemes(authRegistry);
                mClient.setCredentialsProvider(new BasicCredentialsProvider());
        }

        public void setCredentials(String username, String password) {
                mUsername = username;
                mPassword = password;
                mClient.getCredentialsProvider().setCredentials(mAuthScope,
                                new UsernamePasswordCredentials(username, password));
        }

        public void postTwitPic(File file, String message) throws IOException,
                        AuthException, ApiException {
                URI uri;

                try {
                        uri = new URI(UPLOAD_AND_POST_URL);
                } catch (URISyntaxException e) {
                        Log.e(TAG, e.getMessage(), e);
                        throw new IOException("Invalid URL.");
                }

                //DefaultHttpClient client = new DefaultHttpClient();
                HttpPost post = new HttpPost(uri);
                MultipartEntity entity = new MultipartEntity();
                entity.addPart("source", new StringBody(FANFOU_SOURCE));
                // Don't try this. Server does not appear to support chunking.
                // entity.addPart("media", new InputStreamBody(imageStream, "media"));
                entity.addPart("photo", new FileBody(file));
                entity.addPart("status", new StringBody(message));
                post.setEntity(entity);

                HttpConnectionParams.setConnectionTimeout(post.getParams(),
                                CONNECTION_TIMEOUT_MS);
                HttpConnectionParams.setSoTimeout(post.getParams(), SOCKET_TIMEOUT_MS);

                HttpResponse response;

                try {
                        response = mClient.execute(post);
                } catch (ClientProtocolException e) {
                        Log.e(TAG, e.getMessage(), e);
                        throw new IOException("HTTP protocol error.");
                }

                int statusCode = response.getStatusLine().getStatusCode();

                if (statusCode != 200) {
                        Log.e(TAG, Utils.stringifyStream(response.getEntity()
                                                        .getContent()));
                        throw new IOException("Non OK response code: " + statusCode);
                }
        }

        // TODO: return a custom object that has a finish method
        // that calls finish on the HttpEntity and stream.
        private InputStream requestData(String url, String httpMethod,
                        ArrayList<NameValuePair> params) throws IOException, AuthException,
                        ApiException {
                Log.i(TAG, "Sending " + httpMethod + " request to " + url);

                URI uri;

                try {
                        uri = new URI(url);
                } catch (URISyntaxException e) {
                        Log.e(TAG, e.getMessage(), e);
                        throw new IOException("Invalid URL.");
                }

                HttpUriRequest method;

                if (METHOD_POST.equals(httpMethod)) {
                        HttpPost post = new HttpPost(uri);
                        // See this:
                        // http://groups.google.com/group/twitter-development-talk/browse_thread/
                        // thread/e178b1d3d63d8e3b
                        post.getParams().setBooleanParameter(
                                        "http.protocol.expect-continue", false);
                        post.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
                        method = post;
                } else if (METHOD_DELETE.equals(httpMethod)) {
                        method = new HttpDelete(uri);
                } else {
                        method = new HttpGet(uri);
                }

                HttpConnectionParams.setConnectionTimeout(method.getParams(),
                                CONNECTION_TIMEOUT_MS);
                HttpConnectionParams
                                .setSoTimeout(method.getParams(), SOCKET_TIMEOUT_MS);

                HttpResponse response;

                try {
                        response = mClient.execute(method);
                } catch (ClientProtocolException e) {
                        Log.e(TAG, e.getMessage(), e);
                        throw new IOException("HTTP protocol error.");
                }

                int statusCode = response.getStatusLine().getStatusCode();

                if (statusCode == 401) {
                        throw new AuthException();
                } else if (statusCode == 403) {
                        try {
                                JSONObject json = new JSONObject(Utils.stringifyStream(response
                                                .getEntity().getContent()));
                                throw new ApiException(statusCode, json.getString("error"));
                        } catch (IllegalStateException e) {
                                throw new IOException("Could not parse error response.");
                        } catch (JSONException e) {
                                throw new IOException("Could not parse error response.");
                        }
                } else if (statusCode != 200) {
                        Log
                                        .e(TAG, Utils.stringifyStream(response.getEntity()
                                                        .getContent()));
                        throw new IOException("Non OK response code: " + statusCode);
                }

                return response.getEntity().getContent();
        }

        public void login(String username, String password) throws IOException,
                        AuthException, ApiException {
                Log.i(TAG, "Login attempt for " + username);
                setCredentials(username, password);
                InputStream data = requestData(VERIFY_CREDENTIALS_URL, METHOD_GET, null);
                data.close();
        }

        public void logout() {
                setCredentials("", "");
                BaseActivity.FDActivity=null;
                BaseActivity.AtMeActivityCache=null;
                BaseActivity.CookiesActivityCache=null;
        }
        
 
        public JSONObject addFav(String tweed_id) throws IOException,
        					AuthException, ApiException {
			Log.i(TAG, "ad fav " + tweed_id);
			
			String url = String.format(ADD_FAV_URL, tweed_id);
			
			InputStream data = requestData(url, METHOD_POST, new ArrayList<NameValuePair>());
			JSONObject json = null;
			
			try {
			        json = new JSONObject(Utils.stringifyStream(data));
			} catch (JSONException e) {
			        Log.e(TAG, e.getMessage(), e);
			        throw new IOException("Could not parse JSON.");
			} finally {
			        data.close();
			}
			
			return json;
		}
        public JSONObject deleteFav(String id) throws IOException,
        						AuthException, ApiException {
			Log.i(TAG, "delete fav: " + id);
			
			String url = String.format(DEL_FAV_URL, id);
			
			InputStream data = requestData(url, METHOD_DELETE, null);
			JSONObject json = null;
			
			try {
			        json = new JSONObject(Utils.stringifyStream(data));
			} catch (JSONException e) {
			        Log.e(TAG, e.getMessage(), e);
			        throw new IOException("Could not parse JSON.");
			} finally {
			        data.close();
			}
			
			return json;
		}
        
        public JSONObject getUsrName(String username, String password)throws IOException,
        				AuthException, ApiException{
        	Log.i(TAG, "get user name  " + username);
        	
        	InputStream data = requestData(VERIFY_CREDENTIALS_URL, METHOD_GET, null);
        	JSONObject json = null;
        	try{
        		json = new JSONObject(Utils.stringifyStream(data));
        	}catch(JSONException e){
        		Log.e(TAG, e.toString(), e);
        		throw new IOException("Could not parse JSON.");
        	}
        	finally{
        		data.close();
        	}
        	return json;
        }
        
        
        public JSONArray getPublicTimeline(int num) throws IOException, AuthException,
				        ApiException {
				Log.i(TAG, "Requesting public timeline.");
				
				String url = PUBLIC_TIMELINE_URL + "?format=html&count="
				                + URLEncoder.encode(num + "", HTTP.UTF_8);
				
				InputStream data = requestData(url, METHOD_GET, null);
				JSONArray json = null;
				
				try {
				        json = new JSONArray(Utils.stringifyStream(data));
				} catch (JSONException e) {
				        Log.e(TAG, e.getMessage(), e);
				        throw new IOException("Could not parse JSON.");
				} finally {
				        data.close();
				}
				
				return json;
		}
        
        public JSONArray getTimeline(int num) throws IOException, AuthException,
                        ApiException {
                Log.i(TAG, "Requesting friends timeline.");

                String url = FRIENDS_TIMELINE_URL + "?format=html&count="
                                + URLEncoder.encode(num + "", HTTP.UTF_8);

                InputStream data = requestData(url, METHOD_GET, null);
                JSONArray json = null;

                try {
                        json = new JSONArray(Utils.stringifyStream(data));
                } catch (JSONException e) {
                        Log.e(TAG, e.getMessage(), e);
                        throw new IOException("Could not parse JSON.");
                } finally {
                        data.close();
                }

                return json;
        }

        public JSONArray getTimelineSinceId(String sinceId,int count) throws IOException,
                        AuthException, ApiException {
	        	if(count<1){
	        		count=1;
	        	}
	        	if(count>20){
	        		count =20;
	        	}
                Log.i(TAG, "Requesting friends timeline since id.");

                String url = FRIENDS_TIMELINE_URL + "?format=html&count="
                                + URLEncoder.encode(count + "", HTTP.UTF_8);

                if (sinceId != null) {
                        url += "&since_id=" + URLEncoder.encode(sinceId + "", HTTP.UTF_8);
                }

                InputStream data = requestData(url, METHOD_GET, null);
                JSONArray json = null;

                try {
                        json = new JSONArray(Utils.stringifyStream(data));
                } catch (JSONException e) {
                        Log.e(TAG, e.getMessage(), e);
                        throw new IOException("Could not parse JSON.");
                } finally {
                        data.close();
                }

                return json;
        }
    
        public JSONArray getTimelineMaxId(String maxId,int count) throws IOException,
				        AuthException, ApiException {
	        	if(count<1){
	        		count=1;
	        		
	        	}
	        	if(count>20){
	        		count=20;
	        	}
				Log.i(TAG, "Requesting friends timeline max id.");
				
				String url = FRIENDS_TIMELINE_URL + "?format=html&count="
				                + URLEncoder.encode(count + "", HTTP.UTF_8);
				
				if (maxId != null) {
				        url += "&max_id=" + URLEncoder.encode(maxId + "", HTTP.UTF_8);
				}
				
				InputStream data = requestData(url, METHOD_GET, null);
				JSONArray json = null;
				
				try {
				        json = new JSONArray(Utils.stringifyStream(data));
				} catch (JSONException e) {
				        Log.e(TAG, e.getMessage(), e);
				        throw new IOException("Could not parse JSON.");
				} finally {
				        data.close();
				}
				
				return json;
		}
        
        
        public JSONArray getMention(int num) throws IOException, AuthException,
                        ApiException {
                Log.i(TAG, "Requesting mention timeline.");

                String url = REPLIES_URL + "?format=html&count="
                                + URLEncoder.encode(num + "", HTTP.UTF_8);

                InputStream data = requestData(url, METHOD_GET, null);
                JSONArray json = null;

                try {
                        json = new JSONArray(Utils.stringifyStream(data));
                } catch (JSONException e) {
                        Log.e(TAG, e.getMessage(), e);
                        throw new IOException("Could not parse JSON.");
                } finally {
                        data.close();
                }

                return json;
        }

        public JSONArray getMentionSinceId(String sinceId,int count) throws IOException,
                        AuthException, ApiException {
                Log.i(TAG, "Requesting mentions timeline since id.");

                String url = REPLIES_URL + "?format=html&count="
                                + URLEncoder.encode(count + "", HTTP.UTF_8);

                if (sinceId != null) {
                        url += "&since_id=" + URLEncoder.encode(sinceId + "", HTTP.UTF_8);
                }

                InputStream data = requestData(url, METHOD_GET, null);
                JSONArray json = null;

                try {
                        json = new JSONArray(Utils.stringifyStream(data));
                } catch (JSONException e) {
                        Log.e(TAG, e.getMessage(), e);
                        throw new IOException("Could not parse JSON.");
                } finally {
                        data.close();
                }

                return json;
        }
        
        public JSONArray getFavorites(int page)throws IOException,
        					AuthException, ApiException{
        	Log.i(TAG, "Requesting fav timeline ");

            String url = FAVORITES_URL + "?format=html&count="
                            + URLEncoder.encode(RETRIEVE_LIMIT + "", HTTP.UTF_8);

            
            url += "&page=" + URLEncoder.encode(page + "", HTTP.UTF_8);
            

            InputStream data = requestData(url, METHOD_GET, null);
            JSONArray json = null;

            try {
                    json = new JSONArray(Utils.stringifyStream(data));
            } catch (JSONException e) {
                    Log.e(TAG, e.getMessage(), e);
                    throw new IOException("Could not parse JSON.");
            } finally {
                    data.close();
            }

            return json;
        }
        
        
        public JSONArray getFavorites(int count,String signature)throws IOException,
					AuthException, ApiException{
			Log.i(TAG, "Requesting fav timeline ");
			
			String url = FAVORITES_URL + "?format=html&count="
			        + URLEncoder.encode(count + "", HTTP.UTF_8);
			
			
			//url += "&page=" + URLEncoder.encode(page + "", HTTP.UTF_8);
			
			
			InputStream data = requestData(url, METHOD_GET, null);
			JSONArray json = null;
			
			try {
			json = new JSONArray(Utils.stringifyStream(data));
			} catch (JSONException e) {
			Log.e(TAG, e.getMessage(), e);
			throw new IOException("Could not parse JSON.");
			} finally {
			data.close();
			}
			
			return json;
}
        
        
        
        public JSONArray getUserFavorites(String id,int page)throws IOException,
						AuthException, ApiException{
				Log.i(TAG, "Requesting user fav timeline ");
				
				String url = FAVORITES_URL + "?format=html&count="
				        + URLEncoder.encode(RETRIEVE_LIMIT + "", HTTP.UTF_8) + "&id="
				        + URLEncoder.encode(id + "", HTTP.UTF_8);
				
				
				url += "&page=" + URLEncoder.encode(page + "", HTTP.UTF_8);
				
				
				InputStream data = requestData(url, METHOD_GET, null);
				JSONArray json = null;
				
				try {
				json = new JSONArray(Utils.stringifyStream(data));
				} catch (JSONException e) {
				Log.e(TAG, e.getMessage(), e);
				throw new IOException("Could not parse JSON.");
				} finally {
				data.close();
				}
				
				return json;
		}
        public JSONArray getMentionMaxId(String maxId,int count) throws IOException,
				        AuthException, ApiException {
				Log.i(TAG, "Requesting mentions timeline max id.");
				
				String url = REPLIES_URL + "?format=html&count="
				                + URLEncoder.encode(count + "", HTTP.UTF_8);
				
				if (maxId != null) {
				        url += "&max_id=" + URLEncoder.encode(maxId + "", HTTP.UTF_8);
				}
				
				InputStream data = requestData(url, METHOD_GET, null);
				JSONArray json = null;
				
				try {
				        json = new JSONArray(Utils.stringifyStream(data));
				} catch (JSONException e) {
				        Log.e(TAG, e.getMessage(), e);
				        throw new IOException("Could not parse JSON.");
				} finally {
				        data.close();
				}
				
				return json;
		}

        public JSONArray getDirectMessages() throws IOException, AuthException,
                        ApiException {
                Log.i(TAG, "Requesting direct messages.");

                
                InputStream data = requestData(DIRECT_MESSAGES_URL, METHOD_GET, null);
                JSONArray json = null;

                try {
                        json = new JSONArray(Utils.stringifyStream(data));
                } catch (JSONException e) {
                        Log.e(TAG, e.getMessage(), e);
                        throw new IOException("Could not parse JSON.");
                } finally {
                        data.close();
                }

                return json;
        }
        
        public JSONArray getDirectMessagesSinceId(String sinceId) throws IOException, AuthException,
				        ApiException {
				Log.i(TAG, "Requesting direct messages since id.");
				String url = DIRECT_MESSAGES_URL;
                if (sinceId != null) {
			        url += "?since_id=" + URLEncoder.encode(sinceId + "", HTTP.UTF_8);
                }
				InputStream data = requestData(url, METHOD_GET, null);
				JSONArray json = null;
				
				try {
				        json = new JSONArray(Utils.stringifyStream(data));
				} catch (JSONException e) {
				        Log.e(TAG, e.getMessage(), e);
				        throw new IOException("Could not parse JSON.");
				} finally {
				        data.close();
				}
				
				return json;
		}
        
        public JSONArray getDirectMessagesMaxId(String maxId) throws IOException, AuthException,
				        ApiException {
				Log.i(TAG, "Requesting direct messages max id.");
				
				String url = DIRECT_MESSAGES_URL;
                if (maxId != null) {
			        url += "?max_id=" + URLEncoder.encode(maxId + "", HTTP.UTF_8);
                }
				InputStream data = requestData(url, METHOD_GET, null);
				JSONArray json = null;
				
				try {
				        json = new JSONArray(Utils.stringifyStream(data));
				} catch (JSONException e) {
				        Log.e(TAG, e.getMessage(), e);
				        throw new IOException("Could not parse JSON.");
				} finally {
				        data.close();
				}
				
				return json;
        }

        public JSONArray getDirectMessagesSent() throws IOException, AuthException,
                        ApiException {
                Log.i(TAG, "Requesting sent direct messages.");

                InputStream data = requestData(DIRECT_MESSAGES_SENT_URL, METHOD_GET,
                                null);
                JSONArray json = null;

                try {
                        json = new JSONArray(Utils.stringifyStream(data));
                } catch (JSONException e) {
                        Log.e(TAG, e.getMessage(), e);
                        throw new IOException("Could not parse JSON.");
                } finally {
                        data.close();
                }

                return json;
        }
        
        public JSONArray getDirectMessagesSentSinceId(String sinceId) throws IOException, AuthException,
				        ApiException {
				Log.i(TAG, "Requesting sent direct messages since id.");
				String url = DIRECT_MESSAGES_SENT_URL;
				if (sinceId != null) {
			        url += "?since_id=" + URLEncoder.encode(sinceId + "", HTTP.UTF_8);
                }
				InputStream data = requestData(url, METHOD_GET,
				                null);
				JSONArray json = null;
				
				try {
				        json = new JSONArray(Utils.stringifyStream(data));
				} catch (JSONException e) {
				        Log.e(TAG, e.getMessage(), e);
				        throw new IOException("Could not parse JSON.");
				} finally {
				        data.close();
				}
				
				return json;
		}
        
        public JSONArray getDirectMessagesSentMaxId(String maxId) throws IOException, AuthException,
				        ApiException {
				Log.i(TAG, "Requesting sent direct messages max id.");
				String url = DIRECT_MESSAGES_SENT_URL;
                if (maxId != null) {
			        url += "?max_id=" + URLEncoder.encode(maxId + "", HTTP.UTF_8);
                }
				InputStream data = requestData(url, METHOD_GET,
				                null);
				JSONArray json = null;
				
				try {
				        json = new JSONArray(Utils.stringifyStream(data));
				} catch (JSONException e) {
				        Log.e(TAG, e.getMessage(), e);
				        throw new IOException("Could not parse JSON.");
				} finally {
				        data.close();
				}
				
				return json;
		}

        public JSONObject destroyDirectMessage(String id) throws IOException,
                        AuthException, ApiException {
                Log.i(TAG, "Deleting direct message: " + id);

                String url = String.format(DIRECT_MESSAGES_DESTROY_URL, id);

                InputStream data = requestData(url, METHOD_DELETE, null);
                JSONObject json = null;

                try {
                        json = new JSONObject(Utils.stringifyStream(data));
                } catch (JSONException e) {
                        Log.e(TAG, e.getMessage(), e);
                        throw new IOException("Could not parse JSON.");
                } finally {
                        data.close();
                }

                return json;
        }

        public JSONObject sendDirectMessage(String user, String text, String replyId)
                        throws IOException, AuthException, ApiException {
                Log.i(TAG, "Sending dm.");

                ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("user", user));
                params.add(new BasicNameValuePair("text", text));
                
                if (replyId != null && !replyId.equals("")) {
                    params.add(new BasicNameValuePair("in_reply_to_id",replyId));
                }
                InputStream data = requestData(DIRECT_MESSAGES_NEW_URL, METHOD_POST, params);
                JSONObject json = null;

                try {
                        json = new JSONObject(Utils.stringifyStream(data));
                } catch (JSONException e) {
                        Log.e(TAG, e.getMessage(), e);
                        throw new IOException("Could not parse JSON.");
                } finally {
                        data.close();
                }

                return json;
        }

        public JSONObject update(String status, String reply_to)
                        throws IOException, AuthException, ApiException {
                Log.i(TAG, "Updating status.");

                ArrayList<NameValuePair> params = new ArrayList<NameValuePair>();
                params.add(new BasicNameValuePair("status", status));
                params.add(new BasicNameValuePair("source", FANFOU_SOURCE));
                if (reply_to != null && !reply_to.equals("")) {
                        params
                                        .add(new BasicNameValuePair("in_reply_to_status_id",
                                                        reply_to));
                }

                InputStream data = requestData(UPDATE_URL, METHOD_POST, params);
                JSONObject json = null;

                try {
                        json = new JSONObject(Utils.stringifyStream(data));
                } catch (JSONException e) {
                        Log.e(TAG, e.getMessage(), e);
                        throw new IOException("Could not parse JSON.");
                } finally {
                        data.close();
                }

                return json;
        }

        public JSONArray getDmsSinceId(String sinceId, boolean isSent)
                        throws IOException, AuthException, ApiException {
                Log.i(TAG, "Requesting DMs since id.");

                String url = isSent ? DIRECT_MESSAGES_SENT_URL : DIRECT_MESSAGES_URL;

                if (sinceId != null) {
                        url += "?since_id=" + URLEncoder.encode(sinceId + "", HTTP.UTF_8);
                }

                InputStream data = requestData(url, METHOD_GET, null);
                JSONArray json = null;

                try {
                        json = new JSONArray(Utils.stringifyStream(data));
                } catch (JSONException e) {
                        Log.e(TAG, e.getMessage(), e);
                        throw new IOException("Could not parse JSON.");
                } finally {
                        data.close();
                }

                return json;
        }

        public ArrayList<String> getFollowersIds() throws IOException,
                        AuthException, ApiException {
                Log.i(TAG, "Requesting followers ids.");

                InputStream data = requestData(FOLLOWERS_IDS_URL, METHOD_GET, null);
                ArrayList<String> followers = new ArrayList<String>();

                try {
                        JSONArray jsonArray = new JSONArray(Utils.stringifyStream(data));
                        for (int i = 0; i < jsonArray.length(); ++i) {
                                followers.add(jsonArray.getString(i));
                        }
                } catch (JSONException e) {
                        Log.e(TAG, e.getMessage(), e);
                        throw new IOException("Could not parse JSON.");
                } finally {
                        data.close();
                }

                return followers;
        }
        
        public ArrayList<String> getFriendsIds() throws IOException,
				        AuthException, ApiException {
				Log.i(TAG, "Requesting friends ids.");
				
				InputStream data = requestData(FRIENDS_IDS_URL, METHOD_GET, null);
				ArrayList<String> friends = new ArrayList<String>();
				
				try {
				        JSONArray jsonArray = new JSONArray(Utils.stringifyStream(data));
				        for (int i = 0; i < jsonArray.length(); ++i) {
				        	friends.add(jsonArray.getString(i));
				        }
				} catch (JSONException e) {
				        Log.e(TAG, e.getMessage(), e);
				        throw new IOException("Could not parse JSON.");
				} finally {
				        data.close();
				}
				
				return friends;
		}

        public JSONArray getUserTimeline(String user,int count) throws IOException,
                        AuthException, ApiException {
        		if(count<1){
        			count =1;
        		}
        		if(count>20){
        			count=20;
        		}
                Log.i(TAG, "Requesting user timeline.");

                String url = USER_TIMELINE_URL + "?screen_name="
                                + URLEncoder.encode(user, HTTP.UTF_8) + "&format=html";
                
                url+="&count="
                + URLEncoder.encode(count + "", HTTP.UTF_8);

                InputStream data = requestData(url, METHOD_GET, null);
                JSONArray json = null;

                try {
                        json = new JSONArray(Utils.stringifyStream(data));
                } catch (JSONException e) {
                        Log.e(TAG, e.getMessage(), e);
                        throw new IOException("Could not parse JSON.");
                } finally {
                        data.close();
                }

                return json;
        }
        public JSONArray getUserTimelineSinceId(String user,String sinceId,int count) throws IOException,
				        AuthException, ApiException {
	        	if(count<1){
	        		count=1;
	        	}
	        	if(count>20){
	        		count=20;
	        	}
				Log.i(TAG, "Requesting user timeline since id.");
				
				String url = USER_TIMELINE_URL + "?screen_name="
				                + URLEncoder.encode(user, HTTP.UTF_8) + "&since_id=" 
				                + URLEncoder.encode(sinceId, HTTP.UTF_8) + "&format=html";
				url+="&count="
	                + URLEncoder.encode(count + "", HTTP.UTF_8);
				
				InputStream data = requestData(url, METHOD_GET, null);
				JSONArray json = null;
				
				try {
				        json = new JSONArray(Utils.stringifyStream(data));
				} catch (JSONException e) {
				        Log.e(TAG, e.getMessage(), e);
				        throw new IOException("Could not parse JSON.");
				} finally {
				        data.close();
				}
				
				return json;
		}
        public JSONArray getUserTimelineMaxId(String user, String maxId,int count) throws IOException,
				        AuthException, ApiException {
	        	if(count<1){
	        		count=1;
	        	}
	        	if(count>20){
	        		count=20;
	        	}
				Log.i(TAG, "Requesting user timeline max id.");
				
				String url = USER_TIMELINE_URL + "?screen_name="
				                + URLEncoder.encode(user, HTTP.UTF_8) + "&max_id=" 
				                + URLEncoder.encode(maxId, HTTP.UTF_8) + "&format=html";
				url+="&count="
	                + URLEncoder.encode(count + "", HTTP.UTF_8);
				
				InputStream data = requestData(url, METHOD_GET, null);
				JSONArray json = null;
				
				try {
				        json = new JSONArray(Utils.stringifyStream(data));
				} catch (JSONException e) {
				        Log.e(TAG, e.getMessage(), e);
				        throw new IOException("Could not parse JSON.");
				} finally {
				        data.close();
				}
				
				return json;
		}
        public boolean isFollows(String a, String b) throws IOException,
                        AuthException, ApiException {
                Log.i(TAG, "Check follows.");

                String url = FRIENDSHIPS_EXISTS_URL + "?user_a="
                                + URLEncoder.encode(a, HTTP.UTF_8) + "&user_b="
                                + URLEncoder.encode(b, HTTP.UTF_8);

                InputStream data = requestData(url, METHOD_GET, null);

                try {
                        return "true".equals(Utils.stringifyStream(data).trim());
                } finally {
                        data.close();
                }
        }

        public JSONObject createFriendship(String id) throws IOException,
                        AuthException, ApiException {
                Log.i(TAG, "Following: " + id);

                String url = String.format(FRIENDSHIPS_CREATE_URL, id);

                InputStream data = requestData(url, METHOD_POST,
                                new ArrayList<NameValuePair>());
                JSONObject json = null;

                try {
                        json = new JSONObject(Utils.stringifyStream(data));
                } catch (JSONException e) {
                        Log.e(TAG, e.getMessage(), e);
                        throw new IOException("Could not parse JSON.");
                } finally {
                        data.close();
                }

                return json;
        }

        public JSONObject destroyFriendship(String id) throws IOException,
                        AuthException, ApiException {
                Log.i(TAG, "Unfollowing: " + id);

                String url = String.format(FRIENDSHIPS_DESTROY_URL, id);

                InputStream data = requestData(url, METHOD_DELETE, null);
                JSONObject json = null;

                try {
                        json = new JSONObject(Utils.stringifyStream(data));
                } catch (JSONException e) {
                        Log.e(TAG, e.getMessage(), e);
                        throw new IOException("Could not parse JSON.");
                } finally {
                        data.close();
                }

                return json;
        }

        public JSONArray search(String query) throws IOException,
                        AuthException, ApiException {
                Log.i(TAG, "Searching.");
                
                String url = SEARCH_URL + "?q=" + URLEncoder.encode(query, HTTP.UTF_8);

                InputStream data = requestData(url, METHOD_GET, null);
                JSONArray json = null;

                try {
                        json = new JSONArray(Utils.stringifyStream(data));
                } catch (JSONException e) {
                        Log.e(TAG, e.getMessage(), e);
                        throw new IOException("Could not parse JSON.");
                } finally {
                        data.close();
                }

                return json;
        }

        public JSONArray searchMaxId(String query, String maxId) throws IOException,
				        AuthException, ApiException {
				Log.i(TAG, "Searching.");
				
				String url = SEARCH_URL + "?q=" + URLEncoder.encode(query, HTTP.UTF_8)
							+ "&max_id=" + URLEncoder.encode(maxId + "", HTTP.UTF_8);
				
				InputStream data = requestData(url, METHOD_GET, null);
				JSONArray json = null;
				
				try {
				        json = new JSONArray(Utils.stringifyStream(data));
				} catch (JSONException e) {
				        Log.e(TAG, e.getMessage(), e);
				        throw new IOException("Could not parse JSON.");
				} finally {
				        data.close();
				}
				
				return json;
		}        
        
        public JSONObject getUserInfo(String id) throws IOException,
				        AuthException, ApiException {
				Log.i(TAG, "getUserInfo: " + id);
				
				String url = USER_INFO_URL + "?id="
                			+ URLEncoder.encode(id, HTTP.UTF_8);
				
				InputStream data = requestData(url, METHOD_GET, null);
				JSONObject json = null;
				
				try {
				        json = new JSONObject(Utils.stringifyStream(data));
				} catch (JSONException e) {
				        Log.e(TAG, e.getMessage(), e);
				        throw new IOException("Could not parse JSON.");
				} finally {
				        data.close();
				}
				
				return json;
		}
    
        public JSONArray getFollowing(String user,int page) throws IOException,
		        AuthException, ApiException {
				Log.i(TAG, "Requesting friends page.");
				
				String url = FOLLOWING_URL + "?id="
				                + URLEncoder.encode(user + "", HTTP.UTF_8) + "&page=";
				url +=URLEncoder.encode(page + "", HTTP.UTF_8);
				
				InputStream data = requestData(url, METHOD_GET, null);
				JSONArray json = null;
				
				try {
				        json = new JSONArray(Utils.stringifyStream(data));
				} catch (JSONException e) {
				        Log.e(TAG, e.getMessage(), e);
				        throw new IOException("Could not parse JSON.");
				} finally {
				        data.close();
				}
				
				return json;
		}
		
		public JSONArray getFollower(String user,int page) throws IOException,
				        AuthException, ApiException {
				Log.i(TAG, "Requesting follower.");
				
				String url = FOLLOWER_URL + "?id="
                			+ URLEncoder.encode(user + "", HTTP.UTF_8) + "&page=";
				url += URLEncoder.encode(page + "", HTTP.UTF_8);

				InputStream data = requestData(url, METHOD_GET, null);
				JSONArray json = null;
				
				try {
				        json = new JSONArray(Utils.stringifyStream(data));
				} catch (JSONException e) {
				        Log.e(TAG, e.getMessage(), e);
				        throw new IOException("Could not parse JSON.");
				} finally {
				        data.close();
				}
				
				return json;
		}
		
		public JSONObject getHotWord() throws IOException,
						AuthException, ApiException {
				Log.i(TAG, "get hot word " );
				
				
				
				InputStream data = requestData(HOT_WORD_URL, METHOD_GET, null);
				JSONObject json = null;
				
				try {
				json = new JSONObject(Utils.stringifyStream(data));
				} catch (JSONException e) {
				Log.e(TAG, e.getMessage(), e);
				throw new IOException("Could not parse JSON.");
				} finally {
				data.close();
				}
				
				return json;
		}
		
}
