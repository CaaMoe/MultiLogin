package moe.caa.multilogin.core.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.var;
import moe.caa.multilogin.core.logger.LoggerLevel;
import moe.caa.multilogin.core.logger.MultiLogger;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

/**
 * 网络操作工具类
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HttpUtil {

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
                MultiLogger.getLogger().log(LoggerLevel.DEBUG, String.format("Response: %s. (%s)", response, url));
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
        MultiLogger.getLogger().log(LoggerLevel.DEBUG, String.format("Writing POST request data(%s) to %s", content, url));
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
                    MultiLogger.getLogger().log(LoggerLevel.DEBUG, String.format("Response: %s. (%s)", response, url));
                    return response;
                }

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
        throw thr == null ? new IOException(String.format("retry: %d", retry)) : thr;
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
        throw thr == null ? new IOException(String.format("retry: %d", retry)) : thr;
    }
}
