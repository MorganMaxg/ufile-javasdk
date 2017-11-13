package cn.ucloud.ufile;

/**
 * 分片初始化类
 */
public class InitMultiBody {

	/**
	 * 分片上传所需id
	 */
	private String UploadId;

	/**
	 * 分片大小
	 */
	private int BlkSize;

	/**
	 * 分片上传bucket
	 */
	private String Bucket;

	/**
	 * 分片上传指定文件名
	 */
	private String Key;
	
	public InitMultiBody() {
		
	}

	public String getUploadId() {
		return UploadId;
	}

	public void setUploadId(String uploadId) {
		UploadId = uploadId;
	}

	public int getBlkSize() {
		return BlkSize;
	}

	public void setBlkSize(int blkSize) {
		BlkSize = blkSize;
	}

	public String getBucket() {
		return Bucket;
	}

	public void setBucket(String bucket) {
		Bucket = bucket;
	}

	public String getKey() {
		return Key;
	}

	public void setKey(String key) {
		Key = key;
	}

}
