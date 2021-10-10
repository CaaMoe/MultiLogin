package moe.caa.multilogin.core.loader.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.var;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

/**
 * 网络工具类
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HttpUtil {

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
        if (out.exists()) out.delete();
        var downloadingFile = new File(out.getParent(), out.getName() + ".downloading");

        if (downloadingFile.exists()) {
            try (var fileWriter = new FileWriter(downloadingFile)) {
                fileWriter.write("");
                fileWriter.flush();
            }
        } else {
            downloadingFile.createNewFile();
        }

        var httpURLConnection = (HttpURLConnection) new URL(urlEncode(url)).openConnection();
        httpURLConnection.setDoInput(true);
        httpURLConnection.setDoOutput(false);
        httpURLConnection.connect();

        if (httpURLConnection.getResponseCode() == 200) {
            try (var inputStream = httpURLConnection.getInputStream(); var fileOutputStream = new FileOutputStream(downloadingFile)) {
                var b = new byte[1024];
                int n;
                while ((n = inputStream.read(b)) != -1) {
                    fileOutputStream.write(b, 0, n);// 写入数据
                }
                fileOutputStream.flush();
            }
            return downloadingFile.renameTo(out);
        }
        return false;
    }
}
