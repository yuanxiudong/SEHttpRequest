package com.seagle.net.http;

/**
 * Http response handle exception.
 * When handle a response while throw a exception.
 * Created by seagle on 2018/3/28.
 *
 * @author yuanxiudong66@sina.com
 */
public class HttpResponseHandleException extends Exception {
    HttpResponseHandleException(Throwable throwable) {
        super(throwable);
    }
}
