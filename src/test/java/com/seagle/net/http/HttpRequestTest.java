package com.seagle.net.http;

import com.seagle.net.http.request.HttpFormRequest;
import com.seagle.net.http.response.HttpFileDownloadResponseHandler;
import com.seagle.net.http.response.HttpTextResponseHandler;

import static org.junit.Assert.*;

import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Http request test.
 * Created by seagle on 2018/3/28.
 */
public class HttpRequestTest {
    @org.junit.Test
    public void doGet() throws Exception {
        HttpTextResponseHandler handler = new HttpTextResponseHandler();
        HttpRequest httpRequest = new HttpRequest("http://ip.taobao.com/service/getIpInfo.php?ip=210.21.220.218", handler);
        httpRequest.setReadTimeout(10000, TimeUnit.MILLISECONDS);
        httpRequest.setConnectTimeout(10000, TimeUnit.MILLISECONDS);
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
        httpRequest.cancel();
    }

    @Test
    public void doFormGet() {
        HttpTextResponseHandler handler = new HttpTextResponseHandler();
        HttpFormRequest httpRequest = new HttpFormRequest("http://ip.taobao.com/service/getIpInfo.php", handler);
        httpRequest.addParam("ip", "210.21.220.218");
        httpRequest.addParam(new HttpFormRequest.FormParam("ip","210.21.220.218"));
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

    @Test
    public void doPost() throws Exception {
        HttpTextResponseHandler handler = new HttpTextResponseHandler();
        HttpFormRequest httpRequest = new HttpFormRequest("http://ip.taobao.com/service/getIpInfo.php", handler);
        httpRequest.addParam("ip", "210.21.220.218");
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

    @Test
    public void fileDownload() throws IOException, IllegalAccessException {
        final CountDownLatch latch = new CountDownLatch(1);
        String fileUrl = "http://img.zcool.cn/community/01e44d5711c84f6ac72513437994cb.jpg@2o.jpg";
        File file = new File("D:/1234.png");
        file.createNewFile();
        HttpFileDownloadResponseHandler handler = new HttpFileDownloadResponseHandler(file);
        final HttpFormRequest httpRequest = new HttpFormRequest(fileUrl, handler);
        httpRequest.setReadTimeout(10, TimeUnit.MINUTES);
        httpRequest.setConnectTimeout(10, TimeUnit.MINUTES);
        httpRequest.doGet(null, new HttpCallback() {
            @Override
            public void onRequestComplete(HttpRequest request,HttpResponse httpResponse) {
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

    }
}