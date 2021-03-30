/*
 * Copyleft (c) 2021 ksqeib,CaaMoe. All rights reserved.
 * @author  ksqeib <ksqeib@dalao.ink> <https://github.com/ksqeib445>
 * @author  CaaMoe <miaolio@qq.com> <https://github.com/CaaMoe>
 * @github  https://github.com/CaaMoe/MultiLogin
 *
 * moe.caa.multilogin.core.http.HttpGetter
 *
 * Use of this source code is governed by the GPLv3 license that can be found via the following link.
 * https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE
 */

package moe.caa.multilogin.core.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

import static moe.caa.multilogin.core.data.data.PluginData.getTimeOut;

public class HttpGetter {
    /**
     * 向指定的url发送GET请求
     *
     * @param url 指定的url
     * @return 请求结果
     */
    public static String httpGet(URL url) throws IOException {
        URLConnection connection = url.openConnection();
        connection.setConnectTimeout((int) getTimeOut());
        connection.setReadTimeout((int) getTimeOut());
        InputStream input = connection.getInputStream();
        ByteArrayOutputStream result = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int length;
        while ((length = input.read(buffer)) != -1) {
            result.write(buffer, 0, length);
        }
        return result.toString(StandardCharsets.UTF_8.name());
    }

    public static String httpGet(URL url, int retry) throws IOException {
        IOException thr = null;
        for (int i = 0; i < retry; i++) {
            try {
                return httpGet(url);
            } catch (IOException e) {
                thr = e;
            }
        }
        throw thr == null ? new IOException("unknown") : thr;
    }

    public static String httpPost(URL url, String content) throws IOException {
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setRequestProperty("Accept", "application/json");
        byte[] raw = content.getBytes(StandardCharsets.UTF_8);
        connection.setRequestProperty("Content-Length", String.valueOf(raw.length));
        connection.setRequestProperty("User-Agent", "MultiLogin");
        connection.setConnectTimeout((int) getTimeOut());
        connection.setReadTimeout((int) getTimeOut());
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

    public static String httpPost(URL url, String content, int retry) throws IOException {
        IOException thr = null;
        for (int i = 0; i < retry; i++) {
            try {
                return httpPost(url, content);
            } catch (IOException e) {
                thr = e;
            }
        }
        throw thr == null ? new IOException("unknown") : thr;
    }

    /**
     * 向指定的url发送GET请求
     *
     * @param url 指定的url
     * @return 请求结果
     */
    public static String httpGet(String url) throws IOException {
        return httpGet(new URL(url));
    }

    public static String httpPost(String url, String context) throws IOException {
        return httpPost(new URL(url), context);
    }

    public static String httpGet(String url, int retry) throws IOException {
        return httpGet(new URL(url), retry);
    }

    public static String httpPost(String url, String context, int retry) throws IOException {
        return httpPost(new URL(url), context, retry);
    }
}
