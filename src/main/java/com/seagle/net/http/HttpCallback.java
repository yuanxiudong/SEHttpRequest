package com.seagle.net.http;

/**
 * Http request callback.
 * Created by seagle on 2018/3/24.
 *
 * @author yuanxiudong66@sina.com
 */

public interface HttpCallback {
    /**
     * HTTP request complete.
     *
     * @param httpRequest  request
     * @param httpResponse response
     */
    void onRequestComplete(HttpRequest httpRequest, HttpResponse httpResponse);
}
