package com.android.volley.extend;

import java.util.Map;

import android.content.Context;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

public class NetworkClient {

	private Context contex;
	private RequestQueue mQueue;

	private VisitListener visitListener;

	public void setVisitListener(VisitListener visitListener) {
		this.visitListener = visitListener;
	}

	public interface VisitListener {
		public void onResponse(boolean success, String response);

		// public void onErrorResponse(VolleyError error);
	}

	public NetworkClient(Context contex) {
		this.contex = contex;
		mQueue = Volley.newRequestQueue(contex);
	}

	private Map<String, String> coverMap(Map<String, String> src, Map<String, String> dest) {
		if (dest == null)
			return src;
		if (src == null)
			return dest;
		dest.putAll(src);
		return dest;
	}

	// get string request
	public void visitWithPost(String url, final Map<String, String> map) {
		StringRequest stringPostRequest = new StringRequest(Method.POST, url, listener, errorListener) {
			@Override
			protected Map<String, String> getParams() throws AuthFailureError {
				return coverMap(map, super.getParams());
			}

			@Override
			protected Response<String> parseNetworkResponse(NetworkResponse response) {
				return super.parseNetworkResponse(response);
			}
		};
		mQueue.add(stringPostRequest);
	}

	public void visitWithHeader(String url, final Map<String, String> map) {
		StringRequest stringPostRequest = new StringRequest(Method.POST, url, listener, errorListener) {

			@Override
			public Map<String, String> getHeaders() throws AuthFailureError {
				return coverMap(map, super.getHeaders());
			}
		};
		mQueue.add(stringPostRequest);
	}

	private Response.Listener<String> listener = new Response.Listener<String>() {

		@Override
		public void onResponse(String response) {
			if (visitListener != null) {
				visitListener.onResponse(true, response);
			}
		}
	};
	private Response.ErrorListener errorListener = new Response.ErrorListener() {
		@Override
		public void onErrorResponse(VolleyError error) {
			visitListener.onResponse(false, error.getMessage());
		}
	};

}
