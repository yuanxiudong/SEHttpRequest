package com.seagle.net.http;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Http headers.
 * Include http headers for request or response.
 * Created by seagle on 2018/3/24.
 *
 * @author yuanxiudong66@sina.com
 */

public class HttpHeader {

    private final Map<String, String> mHeaders = new ConcurrentHashMap<>();

    /**
     * Http version.
     */
    private String mVersion;

    /**
     * Set http header.
     * If header value is null,will remove the header.
     *
     * @param header header name
     * @param value  header value
     */
    public void setHeader(String header, String value) {
        if (header == null) {
            throw new IllegalArgumentException("Header should not be null!");
        }
        if (value == null) {
            mHeaders.remove(header);
        } else {
            mHeaders.put(header, value);
        }
    }

    /**
     * Return http header value.
     *
     * @param header header name
     * @return header value
     */
    public String getHeader(String header) {
        return mHeaders.get(header);
    }

    /**
     * Return http header value.
     *
     * @param header header name
     * @return header value
     */
    public String getHeader(String header, String defaultValue) {
        if (mHeaders.containsKey(header)) {
            return mHeaders.get(header);
        } else {
            return defaultValue;
        }
    }

    /**
     * Return http version
     *
     * @return version
     */
    public String getVersion() {
        return mVersion;
    }

    /**
     * Set http version.
     *
     * @param version version
     */
    public void setVersion(String version) {
        mVersion = version;
    }

    /**
     * Return all http headers with a unmodifiable map.
     *
     * @return headers
     */
    public Map<String, String> getAllHeaders() {
        return Collections.unmodifiableMap(mHeaders);
    }
}
