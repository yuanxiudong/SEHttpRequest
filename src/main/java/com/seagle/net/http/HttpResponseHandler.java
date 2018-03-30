package com.seagle.net.http;

import java.io.InputStream;

/**
 * Http response handler.
 * Created by seagle on 2018/3/28.
 *
 * @author yuanxiudong66@sina.com
 */

public abstract class HttpResponseHandler<T> {

    /**
     * Handle response body stream.
     *
     * @param responseBody response input stream
     * @return T
     * @throws Exception handle exception
     */
    protected abstract T handleResponseBody(InputStream responseBody) throws Exception;
}
