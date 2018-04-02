package com.seagle.net.http.request;

import com.seagle.net.http.HttpRequest;
import com.seagle.net.http.HttpResponseHandler;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Http form request.
 * For key-value POST or GET request.
 * Created by seagle on 2018/3/24.
 *
 * @author yuanxiudong66@sina.com
 */
public class HttpFormRequest extends HttpRequest {
    private List<FormParam> mFormParams = new CopyOnWriteArrayList<>();
    private Map<String, FormParam> mFormParamMap = new ConcurrentHashMap<>();

    public HttpFormRequest(String url, HttpResponseHandler<?>  responseHandler) {
        super(url, responseHandler);
    }

    /**
     * Add http form request param.
     * Only can be called before submit.
     *
     * @param name  param name
     * @param value param value
     */
    public void addParam(String name, String value) {
        FormParam param = new FormParam(name, value);
        addParam(param);
    }

    /**
     * Add http form request param.
     * Only can be called before submit.
     *
     * @param param param
     */
    public void addParam(FormParam param) {
        if (!mSubmitted) {
            if (param == null || param.name == null || param.name.trim().length() == 0) {
                throw new IllegalArgumentException("Params name is null!");
            }
            FormParam existParam = mFormParamMap.get(param.name);
            if (existParam == null) {
                mFormParams.add(param);
                mFormParamMap.put(param.name, param);
            } else {
                existParam.value = param.value;
            }
        }
    }

    /**
     * Batch add http form request params
     * Only can be called before submit.
     *
     * @param params params
     */
    public void addParams(Map<String, String> params) {
        if (params != null && params.size() == 0) {
            for (String name : params.keySet()) {
                addParam(name, params.get(name));
            }
        }
    }

    /**
     * Remove param.
     * Only can be called before submit.
     *
     * @param name param name
     */
    public void removeParam(String name) {
        if (!mSubmitted) {
            if (name != null) {
                FormParam namePare = mFormParamMap.get(name);
                if (namePare != null) {
                    mFormParams.remove(namePare);
                    mFormParamMap.remove(name);
                }
            }
        }
    }

    /**
     * Remove all params
     */
    public void removeAllParams() {
        mFormParams.clear();
        mFormParamMap.clear();
    }

    @Override
    protected InputStream getInputStream() throws Exception {
        String params = toKeyValueString();
        byte[] dataBytes = params.getBytes();
        if (dataBytes.length > 0) {
            mBodyStream = new ByteArrayInputStream(dataBytes);
        }
        return super.getInputStream();
    }

    private String toKeyValueString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (FormParam namePare : mFormParams) {
            stringBuilder.append("&");
            stringBuilder.append(String.format("%s=%s", namePare.name, namePare.value));
        }
        return stringBuilder.toString().replaceFirst("&", "");
    }

    /**
     * Http form key-value param
     */
    public static class FormParam {
        private String name;
        private String value;

        public FormParam(String name, String value) {
            this.name = name;
            this.value = value;
        }
    }
}
