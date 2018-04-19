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
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;


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
        HttpFormRequest httpRequest = new HttpFormRequest("https://gitee.com/seagle", handler);
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
        if (file.exists()) {
            file.delete();
        }
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
        HttpTextResponseHandler handler = new HttpTextResponseHandler();
        final HttpMultipleRequest request = new HttpMultipleRequest("http://localhost:8080/SEWebServer/file_upload.do", "-------23281168279961", handler);
        File file = new File("D:/1.log");
        HttpMultipleRequest.MultipleFormParam param = new HttpMultipleRequest.MultipleFormParam("file", file, null);
        request.addParam(param);
        request.addParam("ip", "210.21.220.218");
        request.addParam("random", "" + System.currentTimeMillis());
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
                    System.out.println("Upload file success: " + body.toString());
                } else if (code == HttpResponse.SYSTEM_ERROR) {
                    Throwable throwable = (Throwable) body;
                    if (throwable != null) {
                        throwable.printStackTrace();
                    }
                    fail("Request failed: (" + response.getCode() + ", " + response.getMessage() + ")");
                } else {
                    System.out.println();
                    System.out.println("Upload file failed: ");
                }
            }
        });
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * Test async http file upload request.
     */
    @Test
    public void doHttps() {
        String httpsUrl = "https://www.cnblogs.com/";
        HttpTextResponseHandler handler = new HttpTextResponseHandler();
        HttpRequest httpRequest = new HttpRequest(httpsUrl, handler);
        httpRequest.setReadTimeout(10000, TimeUnit.MILLISECONDS);
        httpRequest.setConnectTimeout(10000, TimeUnit.MILLISECONDS);
        httpRequest.getHeader().setHeader("Content-Type", "text/plain");
        httpRequest.setSSLSocketFactory(getSSLFactory());
        httpRequest.setHostnameVerifier(new HostnameVerifier() {
            @Override
            public boolean verify(String s, SSLSession sslSession) {
                System.out.println("C: " + sslSession);
                return "www.cnblogs.com".equalsIgnoreCase(s);
            }
        });
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

    private SSLSocketFactory getSSLFactory() {
        TrustManager[] trustAllCerts = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                        System.out.println("A: "+s);
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
                        System.out.println("B: "+s);
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                }
        };
        try {
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustAllCerts, null);
            return sslContext.getSocketFactory();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}