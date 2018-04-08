package com.seagle.net.http.request;

import com.seagle.net.http.HttpCallback;
import com.seagle.net.http.HttpRequest;
import com.seagle.net.http.HttpResponseHandler;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.SequenceInputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;

/**
 * Multiple http form request.
 * For file and form mixture request.
 * Created by seagle on 2018/3/24.
 *
 * @author yuanxiudong66@sina.com
 */
public class HttpMultipleRequest extends HttpRequest {

    private List<MultipleFormParam> mFormParams = new CopyOnWriteArrayList<>();
    private Map<String, MultipleFormParam> mFormParamMap = new ConcurrentHashMap<>();
    private String mBoundary = System.currentTimeMillis() + "";

    /**
     * Multiple request.
     *
     * @param url             request url
     * @param boundary        multiple split boundary or null
     * @param responseHandler response handler
     */
    public HttpMultipleRequest(String url, String boundary, HttpResponseHandler<?> responseHandler) {
        super(url, responseHandler);
        if (boundary != null && boundary.trim().length() != 0) {
            mBoundary = boundary;
        }
    }

    /**
     * Add http form request param.
     * Only can be called before submit.
     *
     * @param param param
     */
    public void addParam(MultipleFormParam param) {
        if (!mSubmitted) {
            if (param == null || param.name == null || param.name.trim().length() == 0) {
                throw new IllegalArgumentException("Params name is null!");
            }
            MultipleFormParam existParam = mFormParamMap.get(param.name);
            if (existParam == null) {
                mFormParams.add(param);
                mFormParamMap.put(param.name, param);
            } else {
                if (existParam.type == MultipleFormParam.FILE_PARAM) {
                    existParam.file = param.file;
                } else {
                    existParam.value = param.value;
                }
            }
        }
    }

    /**
     * Add http form request param.
     * Only can be called before submit.
     *
     * @param name  param name
     * @param value param value
     */
    public void addParam(String name, String value) {
        MultipleFormParam param = new MultipleFormParam(name, value, null);
        addParam(param);
    }

    /**
     * Add http file upload request param.
     * Only can be called before submit.
     *
     * @param name param name
     * @param file param file
     * @throws FileNotFoundException  file not found or is a directory
     * @throws IllegalAccessException file can't read
     */
    public void addParam(String name, File file) throws FileNotFoundException, IllegalAccessException {
        MultipleFormParam param = new MultipleFormParam(name, file, null);
        addParam(param);

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
        if (name != null) {
            MultipleFormParam namePare = mFormParamMap.get(name);
            if (namePare != null) {
                mFormParams.remove(namePare);
                mFormParamMap.remove(name);
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

    public HttpRequest doGet(ExecutorService executor, HttpCallback callback) {
        return super.doPost(executor, callback);
    }

    public HttpRequest doPost(ExecutorService executor, HttpCallback callback) {
        mHeaders.setHeader("Content-Type", " multipart/form-data; boundary=-----------#" + mBoundary);
        return super.doPost(executor, callback);
    }

    protected InputStream getInputStream() throws Exception {
        if (mFormParams.size() == 0) {
            throw new IllegalArgumentException("Multiple request body is empty!");
        } else {
            List<InputStream> streamList = new ArrayList<>();
            for (MultipleFormParam param : mFormParams) {
                StringBuilder builder = new StringBuilder();
                builder.append("-----------#").append(mBoundary).append("\r\n");
                if (MultipleFormParam.FILE_PARAM == param.type) {
                    String fileName = param.file.getName();
                    builder.append("Content-Disposition: form-data; name=\"").append(param.name).append("\"; filename=\"").append(fileName).append("\"\r\n");
                    if (param.contentType != null && param.contentType.trim().length() > 0) {
                        builder.append("Content-Type:").append(param.contentType).append("\r\n\r\n");
                    } else {
                        builder.append("Content-Type: application/octet-stream").append("\r\n\r\n");
                    }
                    builder.append("Content-Transfer-Encoding: binary" + "\r\n\r\n");
                    InputStream headStream = new ByteArrayInputStream(builder.toString().getBytes());
                    InputStream fileStream = new FileInputStream(param.file);
                    streamList.add(headStream);
                    streamList.add(fileStream);
                } else {
                    builder.append("Content-Disposition: form-data; name=\"").append(param.name).append("\"\r\n");
                    if (param.contentType != null && param.contentType.trim().length() > 0) {
                        builder.append("Content-Type:").append(param.contentType).append("\r\n\r\n");
                    }
                    builder.append(param.value).append("\r\n");
                    InputStream headStream = new ByteArrayInputStream(builder.toString().getBytes());
                    streamList.add(headStream);
                }
            }
            Enumeration<InputStream> enumeration = Collections.enumeration(streamList);
            mBodyStream = new SequenceInputStream(enumeration);
            return super.getInputStream();
        }
    }

    /**
     * Http multiple form param
     */
    public static class MultipleFormParam {
        private static final int FILE_PARAM = 1;
        private String name;
        private String value;
        private File file;
        private String contentType;
        private int type;

        /**
         * Multiple form  string param.
         *
         * @param name        param name
         * @param value       param value
         * @param contentType param value type or null
         */
        public MultipleFormParam(String name, String value, String contentType) {
            this.name = name;
            this.value = value;
            this.contentType = contentType;
            this.type = 0;
        }

        /**
         * Multiple form file param.
         * the content type can be null!
         *
         * @param name        param key
         * @param file        param file
         * @param contentType file content type or null
         * @throws FileNotFoundException  file not found or is a directory
         * @throws IllegalAccessException file can't read
         */
        public MultipleFormParam(String name, File file, String contentType) throws FileNotFoundException, IllegalAccessException {
            if (file == null || file.isDirectory() || !file.exists()) {
                throw new FileNotFoundException();
            }
            if (!file.canRead()) {
                throw new IllegalAccessException("File can not read!");
            }
            this.name = name;
            this.file = file;
            this.contentType = contentType;
            this.type = FILE_PARAM;
        }
    }
}
