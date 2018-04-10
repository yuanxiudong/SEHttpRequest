package com.seagle.net.http;


/**
 * Http response
 * Created by seagle on 2018/3/24.
 *
 * @author yuanxiudong66@sina.com
 */

public class HttpResponse {
    /**
     * System error.
     * An error occurred while executing the request.The response body is Throwable.
     */
    public static final int SYSTEM_ERROR = -1;

    private int mCode;
    private String mMessage;
    private HttpHeader mHeaders;
    private Object mBody;

    HttpResponse(int code, String message, HttpHeader headers) {
        mCode = code;
        mMessage = message;
        mHeaders = headers;
    }

    /**
     * Set response code internal.
     *
     * @param code response code
     */
    final void setCode(int code) {
        mCode = code;
    }

    /**
     * Set response message internal.
     *
     * @param message response message
     */
    final void setMessage(String message) {
        mMessage = message;
    }

    /**
     * Set response header internal.
     *
     * @param headers HttpHeader
     */
    final void setHeaders(HttpHeader headers) {
        mHeaders = headers;
    }

    /**
     * Set response body internal.
     *
     * @param body response body
     */
    final void setBody(Object body) {
        mBody = body;
    }

    /**
     * Return http code.
     *
     * @return code
     */
    public int getCode() {
        return mCode;
    }

    /**
     * Return http header.
     *
     * @return HttpHeader
     */
    public HttpHeader getHeader() {
        return mHeaders;
    }

    /**
     * Return http message.
     *
     * @return message
     */
    public String getMessage() {
        return mMessage;
    }

    /**
     * Return http body.
     *
     * @return body
     */
    public Object getBody() {
        return mBody;
    }
}
