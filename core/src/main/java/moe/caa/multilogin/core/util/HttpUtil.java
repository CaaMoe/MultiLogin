/*
 * Copyleft (c) 2021 ksqeib,CaaMoe. All rights reserved.
 * @author  ksqeib <ksqeib@dalao.ink> <https://github.com/ksqeib445>
 * @author  CaaMoe <miaolio@qq.com> <https://github.com/CaaMoe>
 * @github  https://github.com/CaaMoe/MultiLogin
 *
 * moe.caa.multilogin.core.util.HttpUtil
 *
 * Use of this source code is governed by the GPLv3 license that can be found via the following link.
 * https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE
 */

package moe.caa.multilogin.core.util;

import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

/**
 * 处理 Http 请求程序
 */
public class HttpUtil {

    public static URL getUrlFromString(String url) {
        try {
            return new URL(url);
        } catch (MalformedURLException exception) {
            return null;
        }
    }

    public static String httpGet(URL url, int timeOut) throws IOException {
        URLConnection conn = url.openConnection();
        conn.setConnectTimeout(timeOut);
        conn.setReadTimeout(timeOut);
        InputStream input = conn.getInputStream();
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = input.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString(StandardCharsets.UTF_8.name());
    }

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

    public static String httpPostJson(URL url, String content, int timeOut) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        byte[] raw = content.getBytes(StandardCharsets.UTF_8);
        connection.setRequestProperty("Content-Length", String.valueOf(raw.length));
        connection.setRequestProperty("User-Agent", "MultiLogin");
        connection.setConnectTimeout(timeOut);
        connection.setReadTimeout(timeOut);
        OutputStream output = connection.getOutputStream();
        output.write(raw);
        output.flush();
        InputStream input = connection.getInputStream();
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = input.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString(StandardCharsets.UTF_8.name());
    }

    public static String httpPostJson(URL url, String content, int timeOut, int retry) throws IOException {
        IOException thr = null;
        for (int i = 0; i < retry; i++) {
            try {
                return httpPostJson(url, content, timeOut);
            } catch (IOException e) {
                thr = e;
            }
        }
        throw thr == null ? new IOException("unknown") : thr;
    }

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
        String[] urls = url.split("/");
        for (int i = 0; i < urls.length; i++) {
            String ns = urls[i];
            if (i != 0) ns = URLEncoder.encode(ns, "UTF-8");

            sb.append(ns);
            if (i != urls.length - 1)
                sb.append("/");
        }
        return sb.toString();
    }

    public static boolean downloadFile(String url, File out) throws IOException {
        if(out.exists()) out.delete();
        //            文件检测 这里是防止下载一半断掉 别动！艹！动了砍死你
        File downloadingFile = new File(out.getParent(), out.getName() + ".downloading");
        if (downloadingFile.exists()) {
            downloadingFile.delete();
        }
        downloadingFile.createNewFile();

        HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(urlEncode(url)).openConnection();
        httpURLConnection.setDoInput(true);
        httpURLConnection.setDoOutput(false);
        httpURLConnection.connect();

        int repCode = httpURLConnection.getResponseCode();

        if (repCode == 200) {
            try (InputStream inputStream = httpURLConnection.getInputStream();
                 FileOutputStream fileOutputStream = new FileOutputStream(downloadingFile)) {
                byte[] b = new byte[1024];
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
