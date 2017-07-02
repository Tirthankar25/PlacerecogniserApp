package com.example.userpc.placerecognizer;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Base64;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class UploadImage extends AsyncTask<Void,Void,Void> {

    FinishedUploading callback;
    Bitmap image;
    Httpcall httpcall;
    String response;
    private String url="https://skytourist.herokuapp.com/polls/";
    List<NameValuePair> param;

    private String generateBase64String(Bitmap bitmap)
    {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream .toByteArray();
        String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);
        return encoded;
    }

    UploadImage(FinishedUploading obj,Bitmap str){
        callback=obj;
        image=str;
    }

    @Override
    protected void onPreExecute() {
        httpcall=new Httpcall();
        param=new ArrayList<NameValuePair>();

    }

    @Override
    protected Void doInBackground(Void... params) {
        try {
            param.add(new BasicNameValuePair("image", generateBase64String(image)));
        }
        catch (OutOfMemoryError e)
        {
            e.printStackTrace();
            response="{\"status\":-2,\"output\":\"Image too large\"}";
            return null;
        }
        response=httpcall.makeServiceCall(url,Httpcall.POST,param);
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        callback.onFinished(response);
    }
}
