package com.seagle.net.http;


import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSocketFactory;

import static com.seagle.net.http.HttpResponse.SYSTEM_ERROR;

/**
 * <h1>HTTP request.</h1>
 * <p>
 * This class support async and sync http request.<br>
 * When new a HttpRequest,if HttpCallback is null,
 * you can call {@link #getResponse()} to get the request's response ,
 * this will block caller thread.
 * <p>
 * if you want request some key-value params,
 * see this sub class {@link com.seagle.net.http.request.HttpFormRequest},
 * and multiple form request see {@link com.seagle.net.http.request.HttpMultipleRequest}.<br>
 * You can also create self Request class by extend this class.
 * <p>
 * Request response's body will be different as the param HttpResponseHandler you give,and you have to force convert {@link HttpResponse#getBody()} <br>
 * There are two handler class:
 * <ul>
 * <li>{@link com.seagle.net.http.response.HttpFileDownloadResponseHandler} for file download,the response body is a File</li>
 * <li>{@link com.seagle.net.http.response.HttpTextResponseHandler} for text response,the response body is a String</li>
 * <li>If HttpResponseHandler is null,the response body is a InputStream,you must call {@link #cancel()} after the use of this InputStream</li>
 * <li>If http request had an exception,the response code is {@link HttpResponse#SYSTEM_ERROR} and body is a Exception</li>
 * </ul>
 * You can also create yourself handler to handle the response body,such as return a Entity object.
 * <p>
 * Created by seagle on 2018/3/16.
 *
 * @author yuanxiudong66@sina.com
 */
public class HttpRequest {

    private static volatile ExecutorService sDefaultExecutor;
    private final String mURL;
    private HttpRequestTask.HttpMethod mHttpMethod;
    protected final HttpHeader mHeaders = new HttpHeader();
    protected InputStream mBodyStream;
    private final HttpResponseHandler<?> mResponseHandler;

    private HttpCallback mCallback;
    private volatile HttpRequestSession mRequestSession;
    private Integer mConnectTimeout;
    private Integer mReadTimeout;
    protected volatile boolean mSubmitted;
    private volatile SSLSocketFactory mSSLSocketFactory;
    private volatile HostnameVerifier mHostnameVerifier;


    /**
     * Constructor.
     * Be careful that,if HttpResponseHandler is null,you should call {@link #cancel()} to cancel all resource after response success of failed!
     *
     * @param url             the request url
     * @param responseHandler response handler
     */
    public HttpRequest(String url, HttpResponseHandler<?> responseHandler) {
        mURL = url;
        mSubmitted = false;
        mHttpMethod = HttpRequestTask.HttpMethod.GET;
        mResponseHandler = responseHandler;
    }

    /**
     * Return http request url for internal.
     *
     * @return url
     */
    protected String getURL() {
        return mURL;
    }

    /**
     * Return http method for internal.
     *
     * @return HttpMethod
     */
    protected HttpRequestTask.HttpMethod getHttpMethod() {
        return mHttpMethod;
    }

    /**
     * Return http method.
     *
     * @return http method
     */
    public String getMethod() {
        return mHttpMethod.getValue();
    }

    /**
     * Return http request headers.
     * Header settings are valid only before submission {@link #doGet(ExecutorService, HttpCallback)} or {@link #doPost(ExecutorService, HttpCallback)}
     *
     * @return HttpHeader.
     */
    public HttpHeader getHeader() {
        return mHeaders;
    }

    /**
     * Setting http body data stream.
     * For request,send data from the input stream to server.
     * For response,the method is invalid.
     *
     * @param inputStream body data stream
     */
    public void setInputStream(InputStream inputStream) {
        mBodyStream = inputStream;
    }

    /**
     * Return http body data stream.
     *
     * @return body data stream.
     */
    protected InputStream getInputStream() throws Exception {
        return mBodyStream;
    }

    /**
     * Set https ssl socket factory.
     * Call this before request submit.
     *
     * @param factory SSLSocketFactory
     */
    public void setSSLSocketFactory(SSLSocketFactory factory) {
        mSSLSocketFactory = factory;
    }

    /**
     * Return https ssl socket factory.
     *
     * @return SSLSocketFactory
     */
    SSLSocketFactory getSSLSocketFactory() {
        return mSSLSocketFactory;
    }

    /**
     * Set https host name verifier.
     * Call this before request submit.
     *
     * @param verifier HostnameVerifier
     */
    public void setHostnameVerifier(HostnameVerifier verifier) {
        mHostnameVerifier = verifier;
    }

    /**
     * Return https host name verifier.
     *
     * @return HostnameVerifier
     */
    HostnameVerifier getHostnameVerifier() {
        return mHostnameVerifier;
    }


    /**
     * Return response handler for internal.
     *
     * @return HttpResponseHandler
     */
    HttpResponseHandler getResponseHandler() {
        return mResponseHandler;
    }

    /**
     * Setting http body data.
     * For request,send the data to server.
     * For response,the method is invalid.
     *
     * @param data body data
     */
    public void setBodyData(byte[] data) {
        if (data != null && data.length > 0) {
            mBodyStream = new ByteArrayInputStream(data);
        }
    }

    /**
     * Set request connect timeout.
     *
     * @param time     timeout
     * @param timeUnit time unit
     */
    public void setConnectTimeout(int time, TimeUnit timeUnit) {
        mConnectTimeout = (int) timeUnit.convert(time, TimeUnit.MILLISECONDS);
    }

    /**
     * Set request read timeout.
     *
     * @param time     timeout
     * @param timeUnit time unit
     */
    public void setReadTimeout(int time, TimeUnit timeUnit) {
        mReadTimeout = (int) timeUnit.convert(time, TimeUnit.MILLISECONDS);
    }

    /**
     * Perform http GET request.
     * If executor param is null,will use default thread pool to submit the request.
     * if callback param is null,call {@link #getResponse()} to get Response.
     * Be careful that,if HttpResponseHandler is null,you should call {@link #cancel()} to cancel all resource after response success of failed!
     *
     * @param executor request thread pool or null.
     * @param callback Async request callback or null.
     * @return HttpRequest
     */
    public HttpRequest doGet(ExecutorService executor, HttpCallback callback) {
        if (mSubmitted) {
            throw new IllegalStateException("Request has been submit!");
        }
        mSubmitted = true;
        mCallback = callback;
        mHttpMethod = HttpRequestTask.HttpMethod.GET;
        submit(executor);
        return this;
    }

    /**
     * Perform http POST request.
     * If executor param is null,will use default thread pool to submit the request.
     * if callback param is null,call {@link #getResponse()} to get Response.
     * Be careful that,if HttpResponseHandler is null,you should call {@link #cancel()} to cancel all resource after response success of failed!
     *
     * @param executor request thread pool or null.
     * @param callback Async request callback or null.
     * @return HttpRequest
     */
    public HttpRequest doPost(ExecutorService executor, HttpCallback callback) {
        if (mSubmitted) {
            throw new IllegalStateException("Request has been submit!");
        }
        mSubmitted = true;
        mCallback = callback;
        mHttpMethod = HttpRequestTask.HttpMethod.POST;
        submit(executor);
        return this;
    }

    /**
     * Cancel http request.
     * Suggest:call this whether http request response succeeds or fails.
     */
    public void cancel() {
        if (mRequestSession != null) {
            mCallback = null;
            mRequestSession.cancel(true);
        }
    }

    /**
     * Return http request response.
     * Will block the call thread if http response not return.
     *
     * @return HttpResponse
     */
    public HttpResponse getResponse() {
        if (mRequestSession != null && !mRequestSession.isCancelled()) {
            HttpResponse response;
            try {
                response = mRequestSession.get();
            } catch (Exception ex) {
                ex.printStackTrace();
                response = new HttpResponse(SYSTEM_ERROR, "System error:" + ex.getMessage(), null);
                response.setBody(ex);
            }
            return response;
        }
        return null;
    }

    /**
     * Submit http request.
     *
     * @param executor executor
     */
    private void submit(ExecutorService executor) {
        HttpRequestTask requestTask = HttpRequestTask.buildRequestTask(this);
        if (mConnectTimeout != null) {
            requestTask.setConnectTimeout(mConnectTimeout);
        }
        if (mReadTimeout != null) {
            requestTask.setReadTimeout(mReadTimeout);
        }
        HttpRequestSession session = new HttpRequestSession(requestTask);
        executor = executor == null ? getDefaultExecutor() : executor;
        executor.execute(session);
        mRequestSession = session;
    }

    /**
     * Default http request executor.
     *
     * @return ExecutorService
     */
    private static synchronized ExecutorService getDefaultExecutor() {
        if (sDefaultExecutor == null) {
            int coreSize = Runtime.getRuntime().availableProcessors();
            sDefaultExecutor = Executors.newFixedThreadPool(coreSize * 2 + 1, new ThreadFactory() {
                @Override
                public Thread newThread(Runnable r) {
                    Thread thread = new Thread(r, "Http_Request_Thread");
                    thread.setDaemon(true);
                    return thread;
                }
            });
        }
        return sDefaultExecutor;
    }

    /**
     * Http request session.
     * Cancelable request.
     */
    private class HttpRequestSession extends FutureTask<HttpResponse> {

        private final HttpRequestTask mRequestTask;

        private HttpRequestSession(HttpRequestTask callable) {
            super(callable);
            mRequestTask = callable;
        }

        public void done() {
            if (!isCancelled() && mCallback != null) {
                HttpResponse response;
                try {
                    response = get();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    response = new HttpResponse(SYSTEM_ERROR, "System error:" + ex.getMessage(), null);
                    response.setBody(ex);
                }
                if (!isCancelled() && mCallback != null && response != null) {
                    HttpCallback callback = mCallback;
                    if (callback != null) {
                        callback.onRequestComplete(HttpRequest.this, response);
                    }
                }
                mCallback = null;
            }
        }

        public boolean cancel(boolean mayInterruptIfRunning) {
            mRequestTask.cancel();
            return super.cancel(true);
        }
    }
}
