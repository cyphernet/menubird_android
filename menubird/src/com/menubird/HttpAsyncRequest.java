package com.menubird;

import java.io.IOException;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.TextView;

public class HttpAsyncRequest extends AsyncTask<HttpPost, HttpResponse, HttpResponse> {
	MainActivity activityContext;
	
	public HttpAsyncRequest(MainActivity activity) {
		this.activityContext = activity;
	}
	
	@Override
	protected HttpResponse doInBackground(HttpPost... params) {
		HttpClient httpClient = new DefaultHttpClient();
		HttpResponse response = null;
		try {
			response = httpClient.execute(params[0]);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return response;
	}
	
	protected void onPostExecute(HttpResponse result) {
        try {
        	JSONObject obj = obj=new JSONObject(EntityUtils.toString(result.getEntity()));
        	//process 
        	
        	TextView resultView = (TextView)activityContext.findViewById(R.id.text_result);
        	resultView.setText(obj.toString());
        	
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}