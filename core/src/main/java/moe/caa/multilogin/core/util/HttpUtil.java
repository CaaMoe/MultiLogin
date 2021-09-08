package moe.caa.multilogin.core.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.var;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.logger.MultiLogger;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 网络操作工具类
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HttpUtil {

    /**
     * 通过给定字符串对象生成这个 URL 对象
     *
     * @param url 给定字符串对象
     * @return 匹配的 URL 对象，否则为空
     */
    public static URL getUrlOrNull(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException exception) {
            MultiLogger.getLogger().log(LoggerLevel.DEBUG, String.format("Try to generate the URL failed. (%s)", url), exception);
        }
        return null;
    }

    /**
     * 向目标 URL 发起 HTTP GET 请求
     *
     * @param url     目标 URL
     * @param timeOut 超时时常
     * @return GET 请求返回数据
     * @throws IOException 请求异常
     */
    public static String httpGet(URL url, int timeOut) throws IOException {
        MultiLogger.getLogger().log(LoggerLevel.DEBUG, "Reading data from " + url);
        try {
            var conn = url.openConnection();
            conn.setConnectTimeout(timeOut);
            conn.setReadTimeout(timeOut);
            try (var input = conn.getInputStream();
                 var result = new ByteArrayOutputStream()) {
                byte[] buffer = new byte[1024];
                int length;
                while ((length = input.read(buffer)) != -1) {
                    result.write(buffer, 0, length);
                }
                var response = result.toString(StandardCharsets.UTF_8.name());
                MultiLogger.getLogger().log(LoggerLevel.DEBUG, "Response: " + response);
                return response;
            }
        } catch (Exception e) {
            MultiLogger.getLogger().log(LoggerLevel.DEBUG, String.format("Request failed. (%s)", url), e);
            throw e;
        }
    }

    /**
     * 向目标 URL 发起 HTTP POST 请求
     *
     * @param url         目标 URL
     * @param content     报文数据
     * @param contentType 报文类型
     * @param timeOut     超时时常
     * @return 报文请求返回数据
     * @throws IOException 请求异常
     */
    public static String httpPostJson(URL url, String content, String contentType, int timeOut) throws IOException {
        MultiLogger.getLogger().log(LoggerLevel.DEBUG, "Writing POST request data to " + url);
        try {
            var connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setDoInput(true);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", contentType);
//        connection.setRequestProperty("Accept", "application/json");
            var raw = content.getBytes(StandardCharsets.UTF_8);
            connection.setRequestProperty("Content-Length", String.valueOf(raw.length));
            connection.setRequestProperty("User-Agent", "MultiLogin");
            connection.setConnectTimeout(timeOut);
            connection.setReadTimeout(timeOut);
            try (var output = connection.getOutputStream()) {
                output.write(raw);
                output.flush();
                MultiLogger.getLogger().log(LoggerLevel.DEBUG, "Reading data from " + url);
                try (var input = connection.getInputStream();
                     var result = new ByteArrayOutputStream()) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = input.read(buffer)) != -1) {
                        result.write(buffer, 0, length);
                    }
                    var response = result.toString(StandardCharsets.UTF_8.name());
                    MultiLogger.getLogger().log(LoggerLevel.DEBUG, "Response: " + response);
                    return response;
                }

            }
        } catch (Exception e) {
            MultiLogger.getLogger().log(LoggerLevel.DEBUG, String.format("Request failed. (%s)", url), e);
            throw e;
        }
    }

    /**
     * 编码下载链接
     *
     * @param url URL 字符串
     * @return 编码后链接
     * @throws UnsupportedEncodingException 编码异常
     */
    private static String urlEncode(String url) throws UnsupportedEncodingException {
        StringBuilder sb;
        if (url.startsWith("http://")) {
            url = url.substring(7);
            sb = new StringBuilder("http://");
        } else if (url.startsWith("https://")) {
            url = url.substring(8);
            sb = new StringBuilder("https://");
        } else {
            sb = new StringBuilder();
        }
        var urls = url.split("/");
        for (int i = 0; i < urls.length; i++) {
            String ns = urls[i];
            if (i != 0) ns = URLEncoder.encode(ns, "UTF-8");

            sb.append(ns);
            if (i != urls.length - 1)
                sb.append("/");
        }
        return sb.toString();
    }

    /**
     * 向目标 URL 发起 文件下载 请求
     *
     * @param url 目标 URL
     * @param out 本机下载目标
     * @return 是否成功下载文件
     * @throws IOException 请求异常
     */
    public static boolean downloadFile(String url, File out) throws IOException {
        MultiLogger.getLogger().log(LoggerLevel.DEBUG, "Downloading file " + url + " to " + out.getAbsolutePath());
        try {
            if (out.exists()) out.delete();
            var downloadingFile = new File(out.getParent(), out.getName() + ".downloading");
            IOUtil.clearFile(downloadingFile);
            var httpURLConnection = (HttpURLConnection) new URL(urlEncode(url)).openConnection();
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(false);
            httpURLConnection.connect();

            if (httpURLConnection.getResponseCode() == 200) {
                try (var inputStream = httpURLConnection.getInputStream();
                     var fileOutputStream = new FileOutputStream(downloadingFile)) {
                    var b = new byte[1024];
                    int n;
                    while ((n = inputStream.read(b)) != -1) {
                        fileOutputStream.write(b, 0, n);// 写入数据
                    }
                    fileOutputStream.flush();
                }
                MultiLogger.getLogger().log(LoggerLevel.DEBUG, String.format("Download succeeded. (%s)", out.getName()));
                return downloadingFile.renameTo(out);
            }
            MultiLogger.getLogger().log(LoggerLevel.DEBUG, String.format("Download failed. (%s)", out.getName()));
            return false;
        } catch (Exception e) {
            MultiLogger.getLogger().log(LoggerLevel.DEBUG, String.format("Download failed. (%s)", out.getName()), e);
            throw e;
        }
    }

    /**
     * 向目标 URL 发起 HTTP POST 请求
     *
     * @param url         目标 URL
     * @param content     报文数据
     * @param contentType 报文类型
     * @param timeOut     超时时常
     * @param retry       重试次数
     * @return 报文请求返回数据
     * @throws IOException 请求异常
     */
    public static String httpPostJson(URL url, String content, String contentType, int timeOut, int retry) throws IOException {
        IOException thr = null;
        for (int i = 0; i < retry; i++) {
            try {
                return httpPostJson(url, content, contentType, timeOut);
            } catch (IOException e) {
                thr = e;
            }
        }
        throw thr == null ? new IOException("unknown") : thr;
    }

    /**
     * 向目标 URL 发起 HTTP GET 请求
     *
     * @param url     目标 URL
     * @param timeOut 超时时常
     * @param retry   重试次数
     * @return GET 请求返回数据
     * @throws IOException 请求异常
     */
    public static String httpGet(URL url, int timeOut, int retry) throws IOException {
        IOException thr = null;
        for (int i = 0; i < retry; i++) {
            try {
                return httpGet(url, timeOut);
            } catch (IOException e) {
                thr = e;
            }
        }
        throw thr == null ? new IOException("unknown") : thr;
    }
}
