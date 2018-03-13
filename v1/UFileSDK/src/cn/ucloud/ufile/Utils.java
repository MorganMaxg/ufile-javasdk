package cn.ucloud.ufile;

import sun.misc.BASE64Encoder;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.*;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 工具类
 *
 * @author michael
 */
public class Utils {

    /**
     * 根据文件后缀获取mime type<br>
     * 获取类型有限，可自行拓展
     */
    public static String guessMimeType(String path) {
        FileNameMap fileNameMap = URLConnection.getFileNameMap();
        return fileNameMap.getContentTypeFor(path);
    }

    /**
     * 对url进行utf-8编码
     */
    public static String urlEncode(String key) {
        String result = key;
        try {
            result = URLEncoder.encode(key, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 计算SHA1
     */
    public static String calcSHA1(String filePath) {
        File file = new File(filePath);
        long fileLength = file.length();

        if (fileLength <= 4 * 1024 * 1024) {
            return smallFileSHA1(file);
        } else {
            return largeFileSHA1(file);
        }
    }

    /**
     * 计算 HmacSHA1
     *
     * @param key
     * @param data
     * @return
     */
    public static String hmacSHA1(String key, String data) {
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(), "HmacSHA1");
        Mac mac = null;
        try {
            mac = Mac.getInstance("HmacSHA1");
            mac.init(secretKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
        }
        if (mac != null) {
            return new BASE64Encoder().encode(mac.doFinal(data.getBytes()));
        }
        return null;
    }

    private static String largeFileSHA1(File file) {
        try (InputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
            MessageDigest gsha1 = MessageDigest.getInstance("SHA1");
            MessageDigest sha1 = MessageDigest.getInstance("SHA1");
            byte[] buffer = new byte[1024];
            int nRead = 0;
            int block = 0;
            int count = 0;
            final int blockSize = 4 * 1024 * 1024;
            while ((nRead = inputStream.read(buffer)) != -1) {
                count += nRead;
                sha1.update(buffer, 0, nRead);
                if (blockSize == count) {
                    gsha1.update(sha1.digest());
                    sha1 = MessageDigest.getInstance("SHA1");
                    block++;
                    count = 0;
                }
            }
            if (count != 0) {
                gsha1.update(sha1.digest());
                block++;
            }
            byte[] digest = gsha1.digest();

            byte[] blockBytes = ByteBuffer.allocate(4)
                    .order(ByteOrder.LITTLE_ENDIAN).putInt(block).array();

            byte[] result = ByteBuffer.allocate(4 + digest.length)
                    .put(blockBytes, 0, blockBytes.length)
                    .put(digest, 0, digest.length).array();

            return new BASE64Encoder().encode(result);
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String smallFileSHA1(File file) {
        try (InputStream inputStream = new BufferedInputStream(new FileInputStream(file))) {
            MessageDigest md = MessageDigest.getInstance("SHA1");
            byte[] buffer = new byte[1024];
            int read;
            while ((read = inputStream.read(buffer)) != -1) {
                md.update(buffer, 0, read);
            }
            byte[] digest = md.digest();
            byte[] blockBytes = ByteBuffer.allocate(4)
                    .order(ByteOrder.LITTLE_ENDIAN).putInt(1).array();
            byte[] result = ByteBuffer.allocate(4 + digest.length)
                    .put(blockBytes, 0, 4).put(digest, 0, digest.length)
                    .array();
            return new BASE64Encoder().encode(result);
        } catch (IOException | NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void saveTofile(InputStream inputStream, String path) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(inputStream);
        FileOutputStream fos = new FileOutputStream(path);
        byte[] buff = new byte[1024];
        int length;
        while ((length = bis.read(buff)) != -1) {
            fos.write(buff, 0, length);
        }
        bis.close();
        fos.close();
    }

}
