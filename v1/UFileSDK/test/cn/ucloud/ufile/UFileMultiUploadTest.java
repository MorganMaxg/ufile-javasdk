package cn.ucloud.ufile;

import com.google.gson.Gson;
import okhttp3.Headers;
import okhttp3.Response;

import java.io.File;
import java.io.IOException;

/**
 * 分片上传测试，包括初始化分片、并发上传分片、完成分片上传
 *
 * @author michael
 */
public class UFileMultiUploadTest {

    private static UFileSDK ufileSDK;
    private static InitMultiBody initMultiBody;
    private static String etags = "";

    private static String key = "";
    private static String contentType = "";
    private static String contentMD5 = "";
    private static String filePath = "";

    public static void main(String args[]) {
        String configPath = "config.properties";
        ufileSDK = new UFileSDK();
        ufileSDK.loadConfig(configPath);

        // step 1. 初始化分片
        initMulti();

        // step 2. 上传分片
        uploadMulti();

        // 取消分片上传
//        cancelMulti();

        // step 3. 完成分片上传
        finishMulti();

        // 释放资源
        UFileSDK.shutdown();
    }

    private static void initMulti() {
        String httpMethod = "POST";
        String date = "";

        UFileRequest request = new UFileRequest();
        request.setHttpMethod(httpMethod);
        request.setKey(key);
        request.setContentType(contentType);
        request.setContentMD5(contentMD5);
        request.setDate(date);
        request.setFilePath(filePath);

        System.out.println("[InitMulti]\n");
        Response response = ufileSDK.initMulti(request);
        PrintCallback.print(response, false);
        if (!response.isSuccessful()) {
            return;
        }
        Gson gson = new Gson();
        initMultiBody = gson.fromJson(response.body().charStream(), InitMultiBody.class);
        String bodyJson = gson.toJson(initMultiBody);
        System.out.println("initMultiBody\n" + bodyJson);
        System.out.println("----------------------------------------");
    }

    private static void finishMulti() {
        String httpMethod = "POST";
        String date = "";

        UFileRequest request = new UFileRequest();
        request.setHttpMethod(httpMethod);
        request.setKey(key);
        request.setContentType(contentType);
        request.setContentMD5(contentMD5);
        request.setDate(date);
        request.setFilePath(filePath);

        String newKey = "new_" + request.getKey();
        System.out.println("[FinishPart]\n");
        ufileSDK.finishPart(request, initMultiBody.getUploadId(), etags, newKey, new PrintCallback());
    }

    private static void uploadMulti() {
        String httpMethod = "PUT";
        String date = "";

        UFileRequest request = new UFileRequest();
        request.setHttpMethod(httpMethod);
        request.setKey(key);
        request.setContentType(contentType);
        request.setContentMD5(contentMD5);
        request.setDate(date);
        request.setFilePath(filePath);

        String uploadId = initMultiBody.getUploadId();
        int partSize = initMultiBody.getBlkSize();
        if (partSize <= 0) {
            System.out.println("[ERROR] part size is 0!");
            System.out.println("----------------------------------------");
            return;
        }
        int partCount = calPartCount(request.getFilePath(), partSize);
        File file = new File(request.getFilePath());
        try {
            for (int i = 0; i < partCount; i++) {
                System.out.println("[UploadMulti] part " + i + "\n");
                int offset = partSize * i;
                int length = (int) Math.min(partSize, file.length() - offset);
                Response response = ufileSDK.uploadPart(request, uploadId, i, offset, length);
                PrintCallback.print(response, false);
                if (!response.isSuccessful()) {
                    System.out.println("[ERROR] upload part failed!");
                    System.out.println("----------------------------------------");
                    return;
                }
                System.out.println("body content\n" + response.body().string());
                System.out.println("----------------------------------------");

                // get etag
                Headers headers = response.headers();
                for (int j=0; j < headers.size(); j++) {
                    if (headers.name(j).equalsIgnoreCase("etag")) {
                        etags += "," + headers.value(j);
                    }
                }
            }
            etags = etags.substring(1, etags.length());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void cancelMulti() {
        String httpMethod = "DELETE";
        String date = "";

        UFileRequest request = new UFileRequest();
        request.setHttpMethod(httpMethod);
        request.setKey(key);
        request.setContentType(contentType);
        request.setContentMD5(contentMD5);
        request.setDate(date);

        System.out.println("[Request]\n");
        ufileSDK.cancelPart(request, initMultiBody.getUploadId(), new PrintCallback());

        UFileSDK.shutdown();
    }

    private static int calPartCount(String filePath, int partSize) {
        File file = new File(filePath);
        int partCount = (int) (file.length() / partSize);
        if (file.length() % partSize != 0) {
            partCount++;
        }
        return partCount;
    }

}
