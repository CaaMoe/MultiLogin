/*
 * Copyleft (c) 2021 ksqeib,CaaMoe. All rights reserved.
 * @author  ksqeib <ksqeib@dalao.ink> <https://github.com/ksqeib445>
 * @author  CaaMoe <miaolio@qq.com> <https://github.com/CaaMoe>
 * @github  https://github.com/CaaMoe/MultiLogin
 *
 * moe.caa.multilogin.core.util.FileUtil
 *
 * Use of this source code is governed by the GPLv3 license that can be found via the following link.
 * https://github.com/CaaMoe/MultiLogin/blob/master/LICENSE
 */

package moe.caa.multilogin.core.util;

import java.io.*;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class FileUtil {

    /**
     * 将多个文件压缩
     *
     * @param files 文件列表
     * @param f     输出文件
     */
    public static void toZip(List<File> files, File f) throws IOException {
        try (FileOutputStream out = new FileOutputStream(f);
             ZipOutputStream zos = new ZipOutputStream(out)) {
            for (File file : files) {
                try (FileInputStream in = new FileInputStream(file)) {
                    byte[] buf = new byte[1024];
                    zos.putNextEntry(new ZipEntry(file.getName()));
                    int length;
                    while ((length = in.read(buf)) != -1) {
                        zos.write(buf, 0, length);
                    }
                    zos.finish();
                    zos.closeEntry();
                }
            }
            out.flush();
        }
    }

    /**
     * 文件复制
     *
     * @param in 源文件
     * @param to 目标文件
     */
    public static void fileCopy(File in, File to) throws IOException {
        createNewFileOrFolder(to, false);
        try (FileReader reader = new FileReader(in);
             FileWriter writer = new FileWriter(to)) {
            char[] buffer = new char[1024];
            int len;
            while ((len = reader.read(buffer)) != -1) {
                writer.write(buffer, 0, len);
            }
            writer.flush();
        }
    }

    /**
     * 清空文件内容
     *
     * @param file 源文件
     */
    public static void clearFile(File file) throws IOException {
        createNewFileOrFolder(file, false);
        FileWriter fileWriter = new FileWriter(file);
        fileWriter.write("");
        fileWriter.flush();
        fileWriter.close();
    }

    public static boolean renameFile(File source, String newName) {
        File parent = source.getParentFile();
        File target = parent == null ? new File(newName) : new File(parent, newName);
        return source.renameTo(target);
    }

    /**
     * 创建一个空文件或文件夹
     *
     * @param target 目标文件
     * @param folder 是否是文件夹
     * @return 文件是否存在
     */
    public static boolean createNewFileOrFolder(File target, boolean folder) throws IOException {
        if (target.exists())
            return true;
        if (folder) {
            return target.mkdirs();
        } else {
            return target.createNewFile();
        }
    }

    /**
     * 保存流为文件
     *
     * @param input 输入流
     * @param file  目标文件
     * @param cover 是否覆盖
     */
    public static void saveResource(InputStream input, File file, boolean cover) throws IOException {
        if (file.exists() && !cover) return;
        try (FileOutputStream fOut = new FileOutputStream(file, false)) {
            byte[] buf = new byte[1024];
            int len;
            while ((len = input.read(buf)) > 0) {
                fOut.write(buf, 0, len);
            }
            fOut.flush();
        }
    }


}
