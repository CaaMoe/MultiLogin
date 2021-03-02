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

    /**
     * 向指定的url发送GET请求
     *
     * @param url 指定的url
     * @return 请求结果
     */
    public static String httpGet(String url) throws IOException {
        return httpGet(new URL(url));
    }
}
