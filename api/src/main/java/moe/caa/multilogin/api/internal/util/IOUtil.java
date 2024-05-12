package moe.caa.multilogin.api.internal.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.ApiStatus;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

/**
 * 流工具
 */
@ApiStatus.Internal
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class IOUtil {

    /**
     * 拷贝流
     */
    public static void copy(InputStream is, OutputStream os) throws IOException {
        byte[] buffer = new byte[1024];
        int n;
        while ((n = is.read(buffer)) != -1) {
            os.write(buffer, 0, n);
        }
        os.flush();
    }

    /**
     * 递归删除文件
     */
    public static void removeAllFiles(File file) throws IOException {
        if (!file.exists()) return;
        if (!file.isFile()) {
            File[] files = file.listFiles();
            if (files == null) return;
            for (File f : files) {
                removeAllFiles(f);
            }
        }
        Files.delete(file.toPath());
    }
}
