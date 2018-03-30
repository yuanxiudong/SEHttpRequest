package com.seagle.net.http.response;

import com.seagle.net.http.HttpResponseHandler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Http file download response handler.
 * Created by seagle on 2018/3/28.
 *
 * @author yuanxiudong66@sina.com
 */

public class HttpFileDownloadResponseHandler extends HttpResponseHandler<File> {

    private final File mFile;

    public HttpFileDownloadResponseHandler(File desFile) throws FileNotFoundException, IllegalAccessException {
        mFile = desFile;
        if (mFile == null || !mFile.exists() || mFile.isDirectory()) {
            throw new FileNotFoundException();
        }
        if (!mFile.canWrite()) {
            throw new IllegalAccessException("File can not be write!");
        }
    }

    @Override
    protected File handleResponseBody(InputStream bodyStream) throws IOException, IllegalAccessException {
        if (mFile == null || !mFile.exists() || mFile.isDirectory()) {
            throw new FileNotFoundException();
        }
        if (!mFile.canWrite()) {
            throw new IllegalAccessException("File can not be write!");
        }
        FileOutputStream fos = new FileOutputStream(mFile);
        int readLen;
        byte[] buffer = new byte[1024];
        while ((readLen = bodyStream.read(buffer)) > 0) {
            fos.write(buffer, 0, readLen);
        }
        fos.flush();
        try {
            fos.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return mFile;
    }
}
