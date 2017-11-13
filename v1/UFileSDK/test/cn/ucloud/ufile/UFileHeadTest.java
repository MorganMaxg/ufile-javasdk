package cn.ucloud.ufile;


/**
 * Head 请求测试
 *
 * @author michael
 */
public class UFileHeadTest {

	public static void main(String args[]) {
		String configPath = "";

		String httpMethod = "HEAD";
		String key = "";
		String contentType = "";
		String contentMD5 = "";
		String date = "";

		UFileRequest request = new UFileRequest();
		request.setHttpMethod(httpMethod);
		request.setKey(key);
		request.setContentType(contentType);
		request.setContentMD5(contentMD5);
		request.setDate(date);

		UFileSDK ufileSDK = new UFileSDK();
		ufileSDK.loadConfig(configPath);

		System.out.println("[Request]\n");
		ufileSDK.head(request, new PrintCallback());

		UFileSDK.shutdown();
	}
	
}
