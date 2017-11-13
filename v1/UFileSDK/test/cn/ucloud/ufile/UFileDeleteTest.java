package cn.ucloud.ufile;


/**
 * 删除文件测试
 *
 * @author michael
 */
public class UFileDeleteTest {

	public static void main(String args[]) {
		String configPath = "";

		String httpMethod = "DELETE";
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
		ufileSDK.delete(request, new PrintCallback());

		UFileSDK.shutdown();
	}
	
}
