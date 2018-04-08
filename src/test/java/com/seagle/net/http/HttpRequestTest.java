package com.seagle.net.http;

import com.seagle.net.http.request.HttpFormRequest;
import com.seagle.net.http.request.HttpMultipleRequest;
import com.seagle.net.http.response.HttpFileDownloadResponseHandler;
import com.seagle.net.http.response.HttpTextResponseHandler;

import static org.junit.Assert.*;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;


/**
 * Http request test.
 * Created by seagle on 2018/3/28.
 */
public class HttpRequestTest {
    /**
     * Test simple sync http get request.
     */
    @Test
    public void doSyncGet() throws Exception {
        HttpTextResponseHandler handler = new HttpTextResponseHandler();
        HttpRequest httpRequest = new HttpRequest("http://ip.taobao.com/service/getIpInfo.php?ip=210.21.220.218", handler);
        httpRequest.setReadTimeout(10000, TimeUnit.MILLISECONDS);
        httpRequest.setConnectTimeout(10000, TimeUnit.MILLISECONDS);
        httpRequest.getHeader().setHeader("Content-Type", "text/plain");
        HttpResponse response = httpRequest.doGet(null, null).getResponse();
        int code = response.getCode();
        String message = response.getMessage();
        HttpHeader header = response.getHeader();
        Object body = response.getBody();
        System.out.println(String.format("Response[code:%d  message:%s]", code, message));
        if (response.getCode() == 200) {
            String bodyStr = body.toString();
            System.out.println(header.getVersion());
            for (String key : header.getAllHeaders().keySet()) {
                System.out.println(String.format("%s:%s", key, header.getHeader(key)));
            }
            System.out.println();
            System.out.println(bodyStr);
        } else {
            Throwable throwable = (Throwable) body;
            throwable.printStackTrace();
            fail("Request failed: (" + response.getCode() + ", " + response.getMessage() + ")");
        }
    }

    /**
     * Test simple sync http post request.
     */
    @Test
    public void doSyncPost() {
        HttpTextResponseHandler handler = new HttpTextResponseHandler();
        HttpRequest httpRequest = new HttpRequest("http://ip.taobao.com/service/getIpInfo.php", handler);
        String params = "ip=210.21.220.218";
        //httpRequest.setBodyData(params.getBytes());
        httpRequest.setInputStream(new ByteArrayInputStream(params.getBytes()));
        httpRequest.setReadTimeout(10000, TimeUnit.MILLISECONDS);
        httpRequest.setConnectTimeout(10000, TimeUnit.MILLISECONDS);
        HttpResponse response = httpRequest.doPost(null, null).getResponse();
        int code = response.getCode();
        String message = response.getMessage();
        HttpHeader header = response.getHeader();
        Object body = response.getBody();
        System.out.println(String.format("Response[code:%d  message:%s]", code, message));
        if (response.getCode() == 200) {
            String bodyStr = body.toString();
            System.out.println(header.getVersion());
            for (String key : header.getAllHeaders().keySet()) {
                System.out.println(String.format("%s:%s", key, header.getHeader(key)));
            }
            System.out.println();
            System.out.println(bodyStr);
        } else if (response.getCode() == HttpResponse.SYSTEM_ERROR) {
            Throwable throwable = (Throwable) body;
            throwable.printStackTrace();
            fail("Request failed: (" + response.getCode() + ", " + response.getMessage() + ")");
        } else {
            fail("Request failed: (" + response.getCode() + ", " + response.getMessage() + ")");
        }
    }

    /**
     * Test sync form http get request.
     */
    @Test
    public void doSyncFormGet() {
        HttpTextResponseHandler handler = new HttpTextResponseHandler();
        HttpFormRequest httpRequest = new HttpFormRequest("http://ip.taobao.com/service/getIpInfo.php", handler);
        httpRequest.addParam("ip", "210.21.220.218");
        //httpRequest.addParam(new HttpFormRequest.FormParam("ip", "210.21.220.218"));
        httpRequest.setReadTimeout(10, TimeUnit.MINUTES);
        httpRequest.setConnectTimeout(10, TimeUnit.MINUTES);
        httpRequest.doGet(null, null);
        HttpResponse response = httpRequest.getResponse();
        int code = response.getCode();
        String message = response.getMessage();
        HttpHeader header = response.getHeader();
        Object body = response.getBody();
        System.out.println(String.format("Response[code:%d  message:%s]", code, message));
        if (response.getCode() == 200) {
            String bodyStr = body.toString();
            System.out.println(header.getVersion());
            for (String key : header.getAllHeaders().keySet()) {
                System.out.println(String.format("%s:%s", key, header.getHeader(key)));
            }
            System.out.println();
            System.out.println(bodyStr);
        } else {
            Throwable throwable = (Throwable) body;
            throwable.printStackTrace();
            fail("Request failed: (" + response.getCode() + ", " + response.getMessage() + ")");
        }
    }

    /**
     * Test sync form http post request.
     */
    @Test
    public void doSyncFormPost() throws Exception {
        HttpTextResponseHandler handler = new HttpTextResponseHandler();
        HttpFormRequest httpRequest = new HttpFormRequest("http://ip.taobao.com/service/getIpInfo.php", handler);
        //httpRequest.addParam("ip", "210.21.220.218");
        httpRequest.addParam(new HttpFormRequest.FormParam("ip", "210.21.220.218"));
        httpRequest.setReadTimeout(10, TimeUnit.MINUTES);
        httpRequest.setConnectTimeout(10, TimeUnit.MINUTES);
        httpRequest.doPost(null, null);
        HttpResponse response = httpRequest.getResponse();
        int code = response.getCode();
        String message = response.getMessage();
        HttpHeader header = response.getHeader();
        Object body = response.getBody();
        System.out.println(String.format("Response[code:%d  message:%s]", code, message));
        if (response.getCode() == 200) {
            String bodyStr = body.toString();
            System.out.println(header.getVersion());
            for (String key : header.getAllHeaders().keySet()) {
                System.out.println(String.format("%s:%s", key, header.getHeader(key)));
            }
            System.out.println();
            System.out.println(bodyStr);
        } else {
            Throwable throwable = (Throwable) body;
            throwable.printStackTrace();
            fail("Request failed: (" + response.getCode() + ", " + response.getMessage() + ")");
        }
    }

    /**
     * Test async http file download request.
     */
    @Test
    public void doFileDownload() throws IOException, IllegalAccessException {
        final CountDownLatch latch = new CountDownLatch(1);
        String fileUrl = "http://img.zcool.cn/community/01e44d5711c84f6ac72513437994cb.jpg@2o.jpg";
        File file = new File("D:/1234.png");
        if (file.createNewFile()) {
            HttpFileDownloadResponseHandler handler = new HttpFileDownloadResponseHandler(file);
            final HttpFormRequest httpRequest = new HttpFormRequest(fileUrl, handler);
            httpRequest.setReadTimeout(10, TimeUnit.MINUTES);
            httpRequest.setConnectTimeout(10, TimeUnit.MINUTES);
            httpRequest.doGet(null, new HttpCallback() {
                @Override
                public void onRequestComplete(HttpRequest request, HttpResponse httpResponse) {
                    latch.countDown();
                    HttpResponse response = httpRequest.getResponse();
                    int code = response.getCode();
                    String message = response.getMessage();
                    HttpHeader header = response.getHeader();
                    Object body = response.getBody();
                    System.out.println(String.format("Response[code:%d  message:%s]", code, message));
                    if (response.getCode() == 200) {
                        File bodyFile = (File) body;
                        System.out.println(header.getVersion());
                        for (String key : header.getAllHeaders().keySet()) {
                            System.out.println(String.format("%s:%s", key, header.getHeader(key)));
                        }
                        System.out.println();
                        System.out.println(bodyFile.exists());
                    } else {
                        Throwable throwable = (Throwable) body;
                        if (throwable != null) {
                            throwable.printStackTrace();
                        }
                        fail("Request failed: (" + response.getCode() + ", " + response.getMessage() + ")");
                    }
                }
            });
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            fail();
        }
    }

    /**
     * Test async http file upload request.
     */
    @Test
    public void doFileUpload() throws FileNotFoundException, IllegalAccessException {
        final CountDownLatch latch = new CountDownLatch(1);
        final HttpMultipleRequest request = new HttpMultipleRequest("http://ip.taobao.com/service/getIpInfo.php", null, null);
        File file = new File("D:/123.png");
        HttpMultipleRequest.MultipleFormParam param = new HttpMultipleRequest.MultipleFormParam("file", file, "image/x-png");
        request.addParam(param);
        request.setConnectTimeout(10, TimeUnit.SECONDS);
        request.setReadTimeout(10, TimeUnit.SECONDS);
        request.doPost(null, new HttpCallback() {
            @Override
            public void onRequestComplete(HttpRequest httpRequest, HttpResponse httpResponse) {
                latch.countDown();
                HttpResponse response = httpRequest.getResponse();
                int code = response.getCode();
                String message = response.getMessage();
                HttpHeader header = response.getHeader();
                Object body = response.getBody();
                System.out.println(String.format("Response[code:%d  message:%s]", code, message));
                if (code == 200) {
                    System.out.println(header.getVersion());
                    for (String key : header.getAllHeaders().keySet()) {
                        System.out.println(String.format("%s:%s", key, header.getHeader(key)));
                    }
                    System.out.println();
                    System.out.println("Upload file success");
                } else if (code == HttpResponse.SYSTEM_ERROR) {
                    Throwable throwable = (Throwable) body;
                    if (throwable != null) {
                        throwable.printStackTrace();
                    }
                    fail("Request failed: (" + response.getCode() + ", " + response.getMessage() + ")");
                } else {
                    System.out.println();
                    System.out.println("Upload file failed: " );
                }
            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}