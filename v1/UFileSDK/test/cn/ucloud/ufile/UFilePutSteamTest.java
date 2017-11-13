package cn.ucloud.ufile;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

/**
 * Put上传测试
 *
 * @author michael
 */
public class UFilePutSteamTest {

	public static void main(String args[]) {
		String configPath = "";

		String httpMethod = "PUT";
		String key = "";
		String contentType = "";
		String contentMD5 = "";
		String date = "";
		String filePath = "";

		UFileRequest request = new UFileRequest();
		request.setHttpMethod(httpMethod);
		request.setKey(key);
		request.setContentType(contentType);
		request.setContentMD5(contentMD5);
		request.setDate(date);

		UFileSDK ufileSDK = new UFileSDK();
		ufileSDK.loadConfig(configPath);

		System.out.println("[Request]\n");
		try {
			ufileSDK.putStream(request, new PrintCallback(), new FileInputStream(filePath));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		UFileSDK.shutdown();
	}
	
}
