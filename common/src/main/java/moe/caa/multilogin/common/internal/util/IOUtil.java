package moe.caa.multilogin.common.internal.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

public class IOUtil {

    public static byte[] readNestResource(String filename) throws IOException {
        URL url = IOUtil.class.getClassLoader().getResource(filename);
        if (url == null) {
            return null;
        }

        URLConnection connection = url.openConnection();
        try (InputStream inputStream = connection.getInputStream()) {
            return inputStream.readAllBytes();
        }
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
