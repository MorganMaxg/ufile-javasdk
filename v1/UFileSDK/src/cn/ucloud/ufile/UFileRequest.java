package cn.ucloud.ufile;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * http请求参数类
 */
public class UFileRequest {

    /* http header name */
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String CONTENT_MD5 = "Content-MD5";
    private static final String CONTENT_LENGTH = "Content-Length";
    private static final String DATE = "Date";

    private static final String DEFAULT_CONTENT_TYPE = "application/octet-stream";

    /**
     * http请求方式
     */
    private String httpMethod;

    /**
     * http请求key，文件名
     */
    private String key;

    /**
     * http内容类型
     */
    private String contentType;

    /**
     * http内容md5
     */
    private String contentMD5;

    /**
     * http内容长度
     */
    private long contentLength;

    /**
     * 请求日期
     */
    private String date;

    /**
     * 文件路径
     */
    private String filePath;

    private Map<String, String> headers = new HashMap<>();

    public UFileRequest() {

    }

    public void setHttpMethod(String httpMethod) {
        this.httpMethod = httpMethod;
    }

    public String getHttpMethod() {
        return httpMethod == null ? "" : httpMethod;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getKey() {
        return key == null ? "" : key;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
        this.headers.put(CONTENT_TYPE, contentType);
    }

    public String getContentType() {
        return contentType == null ? "" : contentType;
    }

    public void setContentMD5(String contentMD5) {
        this.contentMD5 = contentMD5;
        this.headers.put(CONTENT_MD5, contentMD5);
    }

    public String getContentMD5() {
        return contentMD5 == null ? "" : contentMD5;
    }

    public void setContentLength(long contentLength) {
        this.contentLength = contentLength;
        this.headers.put(CONTENT_LENGTH, String.valueOf(contentLength));
    }

    public long getContentLength() {
        return contentLength < 0 ? 0 : contentLength;
    }

    public void setDate(String date) {
        this.date = date;
        this.headers.put(DATE, date);
    }

    public String getDate() {
        return date == null ? "" : date;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
        setContentLength(new File(filePath).length());
        if (contentType == null && filePath != null) {
            String contentType = Utils.guessMimeType(filePath);
            if (contentType == null) {
                contentType = DEFAULT_CONTENT_TYPE;
            }
            setContentType(contentType);
        }
    }

    public String getFilePath() {
        return filePath == null ? "" : filePath;
    }

    public void addHeader(String key, String value) {
        this.headers.put(key, value);
    }

    public Map<String, String> getHeaders() {
        return this.headers;
    }

}
