package cn.ucloud.ufile;

/**
 * 下载文件测试
 *
 * @author michael
 */
public class UFileGetTest {

	public static void main(String args[]) throws InterruptedException {
		String configPath = "";

		String httpMethod = "GET";
		String key = "";
		String contentType = "";
		String contentMD5 = "";
		String date = "";
		String saveAsPath = "";

		UFileRequest request = new UFileRequest();
		request.setHttpMethod(httpMethod);
		request.setKey(key);
		request.setContentType(contentType);
		request.setContentMD5(contentMD5);
		request.setDate(date);

		UFileSDK ufileSDK = new UFileSDK();
		ufileSDK.loadConfig(configPath);

		System.out.println("[Request]\n");
		ufileSDK.get(request, new SaveCallback(saveAsPath));

		UFileSDK.shutdown();
	}
	
}
