package cn.ucloud.ufile;

import okhttp3.*;

import java.io.*;

/**
 * 请求回调，打印返回信息
 *
 * @author michael
 */
public class SaveCallback implements Callback {

    private String path;

    public SaveCallback(String path) {
        this.path = path;
    }

    @Override
    public void onFailure(Call call, IOException e) {
        e.printStackTrace();
    }

    @Override
    public void onResponse(Call call, Response response) throws IOException {
        save(response);
    }

    public void save(Response response) {
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
            } else {
                System.out.println("body length: " + responseBody.contentLength());
                System.out.println("========================================");

                if (path.isEmpty()) {
                    return;
                }

                // save to file
                Utils.saveTofile(responseBody.byteStream(), path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
