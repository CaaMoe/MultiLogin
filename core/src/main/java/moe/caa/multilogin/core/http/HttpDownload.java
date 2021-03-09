/*
 * Copyleft (c) 2021 ksqeib,CaaMoe. All rights reserved.
 * @author  ksqeib <ksqeib@dalao.ink> <https://github.com/ksqeib445>
 * @author  CaaMoe <miaolio@qq.com> <https://github.com/CaaMoe>
 * @github  https://github.com/CaaMoe/MultiLogin
 *
 * moe.caa.multilogin.core.http.HttpDownload
 *
 * Use of this source code is governed by the GPLv3 license that can be found via the following link.
 * https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE
 */

package moe.caa.multilogin.core.http;

import moe.caa.multilogin.core.MultiCore;
import moe.caa.multilogin.core.util.I18n;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.concurrent.Callable;

public class HttpDownload implements Callable<Boolean> {
    String url;
    private File downloadFile;
    private boolean success;

    public HttpDownload(String url, File downloadFile) {
        this.url = url;
        this.downloadFile = downloadFile;
    }

    public void run() {
        download();
    }

    private void download() {
        HttpURLConnection httpurlconnection = null;

        try {
//            文件检测
            File downloadingFile = new File(downloadFile.getParent(), downloadFile.getName() + ".downloading");
            if (downloadingFile.exists()) {
                downloadingFile.delete();
            }
            downloadingFile.createNewFile();

            httpurlconnection = (HttpURLConnection) (new URL(urlEncode(url))).openConnection();
            httpurlconnection.setDoInput(true);
            httpurlconnection.setDoOutput(false);
            httpurlconnection.connect();

            int repCode = httpurlconnection.getResponseCode();

            if (repCode == 200) {
                InputStream inputStream = httpurlconnection.getInputStream();
                save(inputStream, downloadingFile);
                inputStream.close();
                downloadingFile.renameTo(downloadFile);
                success = true;
                MultiCore.info(I18n.getTransString("plugin_download_success", url));
            } else {
//                请求失败
                success = false;
                MultiCore.info(I18n.getTransString("plugin_severe_download_fail", url));
            }
        } catch (Exception exception) {
//            下载失败
            success = false;
            MultiCore.info(I18n.getTransString("plugin_severe_download_fail", url));
            exception.printStackTrace();
        } finally {
            if (httpurlconnection != null) {
                httpurlconnection.disconnect();
            }
        }
    }

    private void save(InputStream inputStream, File file) throws Exception {
        try (FileOutputStream fileOutputStream = new FileOutputStream(file)) {
            byte[] b = new byte[1024];
            int n;
            while ((n = inputStream.read(b)) != -1) {
                fileOutputStream.write(b, 0, n);// 写入数据
            }
        }
    }

    private String urlEncode(String url) throws UnsupportedEncodingException {
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
            if (i != 0) ns = URLEncoder.encode(ns, "utf-8");
            sb.append(ns);
            if (i != urls.length - 1)
                sb.append("/");
        }
        return sb.toString();
    }

    @Override
    public Boolean call() throws Exception {
        download();
        return success;
    }
}
