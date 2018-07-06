package com.seagle.net.http.response;

import com.seagle.net.http.HttpResponseHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Text response handler.
 * Created by seagle on 2018/3/28.
 *
 * @author yuanxiudong66@sina.com
 */

public class HttpTextResponseHandler extends HttpResponseHandler<String> {
    @Override
    public String handleResponseBody(InputStream bodyStream) throws IOException {
        StringBuilder builder = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(bodyStream));
        String tempStr;
        while ((tempStr = reader.readLine()) != null) {
            builder.append(tempStr);
        }
        return builder.length() == 0 ? null : builder.toString();
    }
}
