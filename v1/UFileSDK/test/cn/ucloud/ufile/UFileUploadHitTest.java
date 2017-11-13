package cn.ucloud.ufile;

/**
 * 秒传测试，秒传是指如果UFile系统中含有待上传文件，则瞬间完成上传
 *
 * @author michael
 */
public class UFileUploadHitTest {

	public static void main(String args[]) {
		String configPath = "";

		String httpMethod = "POST";
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
		request.setFilePath(filePath);

		UFileSDK ufileSDK = new UFileSDK();
		ufileSDK.loadConfig(configPath);

		System.out.println("[Request]\n");
		ufileSDK.uploadHit(request, new PrintCallback());

		UFileSDK.shutdown();
	}
	
}
