package com.topwise.topos.appstore.conn.easyhttp;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.json.JSONArray;
import org.json.JSONObject;

import com.topwise.topos.appstore.conn.jsonable.JSONArrayable;
import com.topwise.topos.appstore.conn.jsonable.JSONable;
import com.topwise.topos.appstore.utils.LogEx;

public class EasyHttp {
	private static final String TAG = "EasyHttp";
	public static <T extends JSONArrayable> T post(String url,InputStream[] data,JSONArrayable.Creator<T> creator) throws Exception{
		JSONArray receiveJSON = null;
		ByteArrayInputStream request = null;
		ByteArrayOutputStream response = null;
		T t = null;
		try{
			response = new ByteArrayOutputStream(1024*10);
			post(url, data, response);
			receiveJSON = new JSONArray(new String(response.toByteArray(),"utf-8"));
			t = creator.createFromJSON(receiveJSON);
		}catch(Exception e) {
			throw e;
		}finally {
			if(request != null) {
				request.close();
			}
			if(response != null) {
				response.close();
			}
		}
		return t;
	}
	public static <T extends JSONable> T post(String url,InputStream[] data,JSONable.Creator<T> creator) throws Exception{
		JSONObject receiveJSON = null;
		ByteArrayInputStream request = null;
		ByteArrayOutputStream response = null;
		T t = null;
		try{
			response = new ByteArrayOutputStream(1024*10);
			post(url, data, response);
			receiveJSON = new JSONObject(new String(response.toByteArray(),"utf-8"));
			t = creator.createFromJSON(receiveJSON);
		}catch(Exception e) {
			throw e;
		}finally {
			if(request != null) {
				request.close();
			}
			if(response != null) {
				response.close();
			}
		}
		return t;
	}
	public static <T extends JSONable> T post(String url,JSONable data,JSONable.Creator<T> creator) throws Exception{
		JSONObject receiveJSON = null;
		JSONObject sendJSON = new JSONObject();
		ByteArrayInputStream request = null;
		ByteArrayOutputStream response = null;
		T t = null;
		try{
			data.writeToJSON(sendJSON);
			String str = sendJSON.toString();
			request = new ByteArrayInputStream(str.getBytes("utf-8"));
			response = new ByteArrayOutputStream(1024*10);
			post(url,new InputStream[]{request}, response);
			String receiveStr = new String(response.toByteArray(),"utf-8");
			LogEx.d(TAG, "receiveStr == " + receiveStr);
			receiveJSON = new JSONObject(receiveStr);
			t = creator.createFromJSON(receiveJSON);
		}catch(Exception e) {
			throw e;
		}finally {
			if(request != null) {
				request.close();
			}
			if(response != null) {
				response.close();
			}
		}
		return t;
	}
	public static void post(String url,InputStream[] request,OutputStream response) throws Exception{
		if(url.startsWith("https")){
			postByHttps(url,request,response);
		} else {
			postByHttp(url,request,response);
		}
	}
	public static void postByHttp(String url,InputStream[] request,OutputStream response) throws Exception{
		LogEx.d(TAG, "post - ");
		LogEx.d(TAG, "url == " + url);
		URL uri;
	    uri = new URL(url);
		HttpURLConnection conn = (HttpURLConnection)uri.openConnection();
		conn.setDoOutput(true);
		conn.setDoInput(true);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("connection", "keep-alive");
		conn.addRequestProperty("content-type", "application/vnd.syncml+xml; charset=UTF-8");
		OutputStream os = null;
		//如果有数据需要发送,开始发送数据//
		if(request != null) {
			try{
				os = conn.getOutputStream();
				LogEx.d(TAG, "getOutputStream");
				for(InputStream is:request) {
					byte[] buffer = new byte[1024*10];
					while(true) {
						int length = is.read(buffer);
						LogEx.d(TAG, "length == " + length);
//						LogEx.d(TAG, "" + Arrays.toString(buffer));
						if(length == -1) {
							break;
						}
						os.write(buffer,0,length);
					}
				}
			}catch(Exception e) {
				throw e;
			}finally {
				if(os != null) {
					os.flush();
					os.close();
					os = null;
				}
			}
		}
		//准备接收回应//
		int res = conn.getResponseCode();
		LogEx.d(TAG, "res == " + res);
		if(res != 200) {
			throw new Exception("responseCode == " + res);
		}
		if(response != null) {
			InputStream is = null;
			try {
				is = conn.getInputStream();
				LogEx.d(TAG, "getInputStream");
				byte[] buffer = new byte[1024*10];
				while(true) {
					int length = is.read(buffer);
					LogEx.d(TAG, "length == " + length);
//					LogEx.d(TAG, "" + Arrays.toString(buffer));
					if(length <= 0) {
						break;
					}
					response.write(buffer,0,length);
				}
			}catch(Exception e) {
				throw e;
			} finally {
				if(is != null) {
					is.close();
					is = null;
				}
			}
		}
		LogEx.d(TAG, "post + ");
	}
	public static void postByHttps(String url,InputStream[] request,OutputStream response) throws Exception{
		LogEx.d(TAG, "post - ");
		LogEx.d(TAG, "url == " + url);
		HttpPost httpPost = null;
		HttpClient httpClient = null;
	    try {
			URI uri = new URI(url);
			httpPost = new HttpPost(uri);
			
			MultipartEntity multipartEntity = new MultipartEntity();
			multipartEntity.addBinaryPart("content", request);	
			
			httpPost.setEntity(multipartEntity);
			
		    httpClient = HttpUtils.getHttpsClient();
		    
		    HttpResponse httpResponse = httpClient.execute(httpPost);
		    
	        if (httpResponse != null) {
	          StatusLine statusLine = httpResponse.getStatusLine();
	          if (statusLine != null
	              && statusLine.getStatusCode() == HttpStatus.SC_OK) {
		  			InputStream is = null;
					try {
						is = httpResponse.getEntity().getContent();
						LogEx.d(TAG, "getInputStream");
						byte[] buffer = new byte[1024*10];
						while(true) {
							int length = is.read(buffer);
							LogEx.d(TAG, "length == " + length);
							LogEx.d(TAG, "" + Arrays.toString(buffer));
							if(length <= 0) {
								break;
							}
							response.write(buffer,0,length);
						}
					}catch(Exception e) {
						throw e;
					} finally {
						if(is != null) {
							is.close();
							is = null;
						}
					}
	          }
	        }
	      } catch (Exception e) {
	    	  LogEx.e(TAG,"e == " + e);
	    	  e.printStackTrace();
	        LogEx.e(TAG, e.getMessage());
	      } finally {
	    	  if(httpClient != null) {
	    		  httpClient.getConnectionManager().shutdown();
	    	  }
	      }
		LogEx.d(TAG, "post + ");
	}
}
