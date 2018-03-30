# SEHttpRequest
简单封装了一下HttpUrlConnection，纯Java语言实现，基于AndroidStudio Gradle 构建，
可用于Java项目和Android项目。体积小巧，实现简单，能够胜任一般简单的HTTP请求开发需求。

### 功能：

1.文件上传。

2.文件下载。

3.表单提交。

4.Multiple表单提交。

5.支持同步请求和异步请求。

### 工程目录结构

本工程是基于Android Studio Gradle构建的一个单独的Module，需要导入到具体的工程中运行。

![工程目录结构](./doc/project_structure.png)
### 类结构




### 使用说明
#### 同步请求
```
//构建HttpRequest对象，以及请求的参数，请求头等
HttpTextResponseHandler handler = new HttpTextResponseHandler();
HttpRequest httpRequest = new HttpRequest("http://ip.taobao.com/service/getIpInfo.php?ip=210.21.220.218", handler);
httpRequest.setReadTimeout(10000, TimeUnit.MILLISECONDS);
httpRequest.setConnectTimeout(10000, TimeUnit.MILLISECONDS);
httpRequest.getHeader().setHeader("Content-Type","text/plain");
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
        httpRequest.cancel();

```