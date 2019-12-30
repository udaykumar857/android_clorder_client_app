package com.clorderclientapp.httpClient;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NetworkResponse;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.util.HashMap;
import java.util.Map;

public class VolleyRequest extends Request {

    private final String TAG = "VolleyRequest";
    protected static final String PROTOCOL_CHARSET = "utf-8";
    private static final String PROTOCOL_CONTENT_TYPE =
            String.format("application/json; charset=%s", PROTOCOL_CHARSET);
    private String mRequestBody;
    private static Response.ErrorListener errorListener;
    private onSuccess responseListener;
    private onFailure failureListener;
    private int successCode = 0, failureCode = -1;
    private Map<String, String> requestHeaders;

    static {

        errorListener = new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

            }
        };
    }

    /**
     * Callback Interface for delivering success responses
     * <p class="note"><strong>Note:</strong> See {@link  #onResponse(int, Object)} for more info</p>
     **/
    public interface onSuccess {

        /**
         * Callback method fired after successful request
         *
         * @param statusCode The status code received on request completion
         * @param response   The body of the HTTP response from the server
         */

        void onResponse(int statusCode, Object response);
    }

    /**
     * Callback Interface for delivering error responses
     * <p class="note"><strong>Note:</strong> See {@link  #onError(int, Object)} for more info</p>
     **/
    public interface onFailure {

        /**
         * Callback method fired when a request fails to complete
         *
         * @param statusCode    The status code received on request completion
         * @param errorResponse A description on underlying cause for the request failure
         */
        void onError(int statusCode, Object errorResponse);
    }

    public VolleyRequest(int method,
                         String url,
                         Response.ErrorListener errorListener) {
        super(method, url, errorListener);
    }

    /**
     * Creates a new VolleyRequest Object for GET Requests
     *
     * @param url              The url of the request to be made
     * @param responseListener Listener for handling success responses {@link onSuccess}
     * @param failureListener  Listener for handling error responses {@link onFailure}
     * @see #VolleyRequest(int, String, JSONObject, onSuccess, onFailure)
     */
    public VolleyRequest(String url,
                         onSuccess responseListener,
                         onFailure failureListener) {
        this(Method.GET, url, errorListener);
        this.responseListener = responseListener;
        this.failureListener = failureListener;
    }

    /**
     * Creates a new VolleyRequest Object for GET/POST Requests
     *
     * @param method           Any one of the values from {@link com.android.volley.Request.Method}
     * @param url              The url of the request to be made
     * @param requestBody      Raw data to be sent along with the request.
     *                         Can be null if no data needs to be sent
     * @param responseListener Listener for handling success responses {@link onSuccess}
     * @param failureListener  Listener for handling error responses {@link onFailure}
     */
    public VolleyRequest(int method,
                         String url,
                         JSONObject requestBody,
                         onSuccess responseListener,
                         onFailure failureListener) {
        this(method, url, errorListener);
        mRequestBody = requestBody.toString();
        this.responseListener = responseListener;
        this.failureListener = failureListener;
    }

    @Override
    protected Response<String> parseNetworkResponse(NetworkResponse response) {
        try {
            Log.d(TAG, "ParseNetworkResponse" + "\t" + response.statusCode);
            successCode = response.statusCode;
            String json = new String(
                    response.data,
                    HttpHeaderParser.parseCharset(response.headers));
            return Response.success(json,
                    HttpHeaderParser.parseCacheHeaders(response));
        } catch (Exception e) {
            return Response.error(new ParseError(e));
        }
    }

    @Override
    public Map<String, String> getHeaders() throws AuthFailureError {
        if (this.requestHeaders != null && this.requestHeaders.size() > 0) {
            return this.requestHeaders;
        }
        return new HashMap<>();
    }

    public void setRequestHeaders(Map<String, String> requestHeaders) {
        this.requestHeaders = requestHeaders;
    }

    @Override
    protected VolleyError parseNetworkError(VolleyError volleyError) {
        return super.parseNetworkError(volleyError);
    }

    @Override
    protected void deliverResponse(Object response) {
        Log.d(TAG, " Response" + "\t" + response);
        this.responseListener.onResponse(successCode, response);
    }

    @Override
    public void deliverError(VolleyError error) {
        Log.d(TAG, " Failure" + "\t" + error);
        failureCode = parseVolleyError(error);
        this.failureListener.onError(failureCode, error);
    }

    @Override
    public String getBodyContentType() {
        return PROTOCOL_CONTENT_TYPE;
    }

    @Override
    public byte[] getBody() {
        try {
            return mRequestBody == null ? null : mRequestBody.getBytes(PROTOCOL_CHARSET);
        } catch (UnsupportedEncodingException uee) {
            VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s",
                    mRequestBody, PROTOCOL_CHARSET);
            return null;
        }
    }

    private int parseVolleyError(final VolleyError error) {
        int statusCode = -1;
        if (error instanceof TimeoutError || error instanceof NoConnectionError || error instanceof NetworkError) {
            statusCode = HttpURLConnection.HTTP_CLIENT_TIMEOUT;
        } else if (error instanceof AuthFailureError) {
            statusCode = HttpURLConnection.HTTP_UNAUTHORIZED;
        } else if (error instanceof ServerError) {
            statusCode = HttpURLConnection.HTTP_INTERNAL_ERROR;
        } else if (error instanceof ParseError) {
            statusCode = HttpURLConnection.HTTP_BAD_REQUEST;
        }
        return statusCode;
    }
}