package com.example.userpc.placerecognizer;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;
 
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.util.Log;
 
public class Httpcall {
 
    static String response = null;
    public final static int GET = 1;
    public final static int POST = 2;
 
    public  Httpcall() {
 
    }
 
    
    public String makeServiceCall(String url, int method) {
        return this.makeServiceCall(url, method, null);
    }
 
   
    public String makeServiceCall(String url, int method,
            List<NameValuePair> params) {
    	Log.e("Called", "New Called");
        try {
            // http client
            DefaultHttpClient httpClient = new DefaultHttpClient();
            HttpEntity httpEntity = null;
            HttpResponse httpResponse = null;
             
            // Checking http request method type
            if (method == POST) {
                HttpPost httpPost = new HttpPost(url);
                // adding post params
                if (params != null) {
                    httpPost.setEntity(new UrlEncodedFormEntity(params,"UTF-8"));   //change
                    Log.e("Called", "This called");
                }
                
                httpResponse = httpClient.execute(httpPost);
                
 
            } else if (method == GET) {
                // appending params to url
                if (params != null) {
                    String paramString = URLEncodedUtils
                            .format(params, "utf-8");
                    url += "?" + paramString;
                }
                HttpGet httpGet = new HttpGet(url); 
                httpResponse = httpClient.execute(httpGet); 
            }
            httpEntity = httpResponse.getEntity();
            response = EntityUtils.toString(httpEntity);
           // Log.e("Hiiii",response);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
         
        return response;
 
    }
}