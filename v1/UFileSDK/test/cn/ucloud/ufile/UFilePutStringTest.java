package cn.ucloud.ufile;

/**
 * Put上传测试
 *
 * @author michael
 */
public class UFilePutStringTest {

	public static void main(String args[]) {
		String configPath = "";

		String httpMethod = "PUT";
		String key = "";
		String contentType = "text/plain; charset=utf-8";
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
		String testStr = "put-string-stream-test \r\n  www.ucloud.cn \r\n 优刻得 优秀是通过刻苦努力得到的";
		ufileSDK.putString(request, new PrintCallback(), testStr);

		UFileSDK.shutdown();
	}
	
}
