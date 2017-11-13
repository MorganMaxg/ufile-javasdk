package cn.ucloud.ufile;

import okhttp3.*;

import java.io.IOException;

/**
 * 请求回调，打印返回信息
 *
 * @author michael
 */
public class PrintCallback implements Callback {

    @Override
    public void onFailure(Call call, IOException e) {
        e.printStackTrace();
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        print(response, true);
    }

    public static void print(Response response, boolean isPrintContent) {
        try {
            ResponseBody responseBody = response.body();
            System.out.println("----------------------------------------");
            System.out.println("[Response]\n");
            System.out.println("status line: " + response.code());
            Headers headers = response.headers();
            for (int i = 0; i < headers.size(); i++) {
                System.out.println("header " + headers.name(i) + " : " + headers.value(i));
            }
            if (responseBody == null || responseBody.contentLength() == 0) {
                System.out.println("body length: 0");
                System.out.println("========================================");
            } else if (isPrintContent) {
                System.out.println("body length: " + responseBody.contentLength());
                // 调用responseBody.string()后流关闭，多次调用会出错
                System.out.println("body content:\n" + responseBody.string());
                System.out.println("========================================");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
