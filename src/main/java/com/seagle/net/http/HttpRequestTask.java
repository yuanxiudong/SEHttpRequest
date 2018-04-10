package com.seagle.net.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import static com.seagle.net.http.HttpResponse.SYSTEM_ERROR;

/**
 * Http request internal task.
 * Created by seagle on 2018/3/26.
 *
 * @author yuanxiudong66@sina.com
 */

class HttpRequestTask implements Callable<HttpResponse> {

    public enum HttpMethod {
        GET("GET"), POST("POST");

        private String value;

        HttpMethod(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    private HttpURLConnection mUrlConnection;
    private int mConnectTimeout;
    private int mReadTimeout;
    private volatile boolean mCanceled;
    private HttpRequest mHttpRequest;


    private HttpRequestTask(HttpRequest httpRequest) {
        mHttpRequest = httpRequest;
        mCanceled = false;
    }

    @Override
    public HttpResponse call() {
        if (mCanceled) {
            return null;
        } else {
            try {
                if (HttpMethod.POST == mHttpRequest.getHttpMethod()) {
                    return httpPost();
                } else {
                    return httpGet();
                }
            } catch (Exception ex) {
                HttpResponse httpResponse = null;
                if (!mCanceled) {
                    httpResponse = new HttpResponse(SYSTEM_ERROR, "System error:" + ex.getMessage(), null);
                    httpResponse.setBody(ex);
                }
                if (mUrlConnection != null) {
                    mUrlConnection.disconnect();
                }
                return httpResponse;
            } finally {
                mHttpRequest = null;
                mCanceled = false;
            }
        }
    }

    private HttpResponse httpGet() throws Exception {
        String params = null;
        InputStream bodyStream = mHttpRequest.getInputStream();
        if (bodyStream != null) {
            String tempStr;
            StringBuilder stringBuilder = new StringBuilder();
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(bodyStream));
            while ((tempStr = bufferedReader.readLine()) != null) {
                stringBuilder.append(tempStr);
            }
            params = stringBuilder.toString();
        }
        String requestURL = mHttpRequest.getURL();
        if (params != null && params.trim().length() > 0) {
            requestURL = String.format("%s?%s", requestURL, params);
        }
        URL url = new URL(requestURL);
        mUrlConnection = (HttpURLConnection) url.openConnection();
        mUrlConnection.setRequestMethod(HttpMethod.GET.getValue());
        mUrlConnection.setDoOutput(false);
        mUrlConnection.setDoInput(true);
        mUrlConnection.setConnectTimeout(mConnectTimeout);
        mUrlConnection.setReadTimeout(mReadTimeout);
        Map<String, String> headers = mHttpRequest.getHeader().getAllHeaders();
        for (String key : headers.keySet()) {
            mUrlConnection.setRequestProperty(key, headers.get(key));
        }
        mUrlConnection.connect();
        return parseResponse();

    }

    private HttpResponse httpPost() throws Exception {
        URL url = new URL(mHttpRequest.getURL());
        mUrlConnection = (HttpURLConnection) url.openConnection();
        mUrlConnection.setRequestMethod(HttpMethod.POST.getValue());
        mUrlConnection.setChunkedStreamingMode(0);
        mUrlConnection.setDoOutput(true);
        mUrlConnection.setDoInput(true);
        mUrlConnection.setConnectTimeout(mConnectTimeout);
        mUrlConnection.setReadTimeout(mReadTimeout);

        Map<String, String> headers = mHttpRequest.getHeader().getAllHeaders();
        for (String key : headers.keySet()) {
            mUrlConnection.setRequestProperty(key, headers.get(key));
        }

        mUrlConnection.connect();
        InputStream bodyStream = mHttpRequest.getInputStream();
        if (bodyStream != null) {
            OutputStream outputStream = mUrlConnection.getOutputStream();
            try {
                byte[] buffer = new byte[1024];
                int readLen;
                while ((readLen = bodyStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, readLen);
                }
                outputStream.flush();
            } finally {
                try {
                    bodyStream.close();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
        return parseResponse();
    }

    private HttpResponse parseResponse() throws IOException, HttpResponseHandleException {
        int code = mUrlConnection.getResponseCode();
        String message = mUrlConnection.getResponseMessage();
        HttpHeader header = parseRespHeader(mUrlConnection);
        HttpResponse httpResponse = new HttpResponse(code, message, header);
        if (HttpURLConnection.HTTP_OK == code) {
            InputStream responseStream = mUrlConnection.getInputStream();
            if (mHttpRequest.getResponseHandler() != null) {
                try {
                    httpResponse.setBody(mHttpRequest.getResponseHandler().handleResponseBody(responseStream));
                } catch (Exception ex) {
                    ex.printStackTrace();
                    throw new HttpResponseHandleException(ex);
                } finally {
                    mUrlConnection.disconnect();
                }
            } else {
                httpResponse.setBody(responseStream);
            }
        }
        return httpResponse;
    }

    private HttpHeader parseRespHeader(HttpURLConnection urlConnection) {
        Map<String, List<String>> headMap = urlConnection.getHeaderFields();
        StringBuilder headValueBuilder;
        HttpHeader responseHeader = new HttpHeader();
        for (String key : headMap.keySet()) {
            List<String> values = headMap.get(key);
            String headValue = "";
            if (values != null) {
                if (values.size() == 1) {
                    headValue = values.get(0);
                } else {
                    headValueBuilder = new StringBuilder();
                    for (String str : values) {
                        headValueBuilder.append(";").append(str);
                    }
                    headValue = headValueBuilder.toString().replaceFirst(";", "");
                }
            }
            if (key == null) {
                responseHeader.setVersion(headValue);
            } else {
                responseHeader.setHeader(key, headValue);
            }
        }
        return responseHeader;
    }

    void cancel() {
        mCanceled = true;
        if (mUrlConnection != null) {
            mUrlConnection.disconnect();
        }
    }

    void setConnectTimeout(int time) {
        mConnectTimeout = time;
    }

    void setReadTimeout(int time) {
        mReadTimeout = time;
    }

    static HttpRequestTask buildRequestTask(HttpRequest request) {
        return new HttpRequestTask(request);
    }
}
