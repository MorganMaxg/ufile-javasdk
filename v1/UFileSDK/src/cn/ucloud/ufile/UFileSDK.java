package cn.ucloud.ufile;

import okhttp3.*;
import okio.BufferedSink;

import java.io.*;
import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

/**
 * UFile SDK，具体使用可参考test目录
 * <p>
 * SDK分别提供了同步、异步两种http请求方式，异步可进行并发请求
 *
 * @author michael
 */
public class UFileSDK {

    /**
     * Http header
     */
    private static final String USER_AGENT = "User-Agent";
    private static final String AUTHORIZATION = "Authorization";
    private static final String CANONICAL_PREFIX = "X-UCloud";
    private static final String JAVA_SDK_VER = "JavaSDK/2.0.0;JavaVersion/" + System.getProperty("java.version");

    private static volatile OkHttpClient okHttpClient;

    /**
     * 存储bucket
     */
    private String bucket;

    /**
     * 上传域名
     */
    private String upHost;

    /**
     * 下载域名
     */
    private String dlHost;

    /**
     * 公钥
     */
    private String publicKey;

    /**
     * 私钥
     */
    private String privateKey;

    /**
     * 加载配置文件
     *
     * @param configPath 配置文件路径
     */
    public void loadConfig(String configPath) {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(configPath);
            Properties properties = new Properties();
            properties.load(fis);
            String bucket = properties.getProperty("Bucket");
            String publicKey = properties.getProperty("UCloudPublicKey");
            String privateKey = properties.getProperty("UCloudPrivateKey");
            String proxySuffix = properties.getProperty("ProxySuffix");
            String dlProxySuffix = properties.getProperty("GlobalDownloadProxySuffix");
            String upProxySuffix = properties.getProperty("GlobalUploadProxySuffix");
            String cdnHost = properties.getProperty("CDNHost");

            if (cdnHost != null && !cdnHost.isEmpty()) {
                initCDN(bucket, cdnHost, publicKey, privateKey);
            } else if (upProxySuffix != null && !upProxySuffix.isEmpty()) {
                initGlobal(bucket, upProxySuffix, dlProxySuffix, publicKey, privateKey);
            } else {
                init(bucket, proxySuffix, publicKey, privateKey);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 单地域初始化，上传下载同一个域名
     *
     * @param bucket
     * @param proxySuffix 域名后缀
     * @param publicKey   公钥
     * @param privateKey  私钥
     */
    public void init(String bucket, String proxySuffix, String publicKey, String privateKey) {
        this.bucket = bucket;
        this.upHost = "http://" + bucket + proxySuffix;
        this.dlHost = "http://" + bucket + proxySuffix;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    /**
     * 全球化初始化，上传下载不同域名，例如：
     * <p>
     * .up.ufileos.com
     * .dl.ufileos.com
     *
     * @param bucket
     * @param upProxySuffix 上传域名后缀
     * @param dlProxySuffix 下载域名后缀
     * @param publicKey     公钥
     * @param privateKey    私钥
     */
    public void initGlobal(String bucket, String upProxySuffix, String dlProxySuffix, String publicKey, String privateKey) {
        this.bucket = bucket;
        this.upHost = "http://" + bucket + upProxySuffix;
        this.dlHost = "http://" + bucket + dlProxySuffix;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    /**
     * CDN加速初始化，上传下载同一个域名
     *
     * @param bucket
     * @param host       加速域名
     * @param publicKey  公钥
     * @param privateKey 私钥
     */
    public void initCDN(String bucket, String host, String publicKey, String privateKey) {
        this.bucket = bucket;
        this.upHost = host;
        this.dlHost = host;
        this.publicKey = publicKey;
        this.privateKey = privateKey;
    }

    /**
     * GET 下载文件
     *
     * @param ufileRequest
     * @param callback
     */
    public void get(UFileRequest ufileRequest, Callback callback) {
        String url = dlHost + "/" + Utils.urlEncode(ufileRequest.getKey());
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        request(ufileRequest, builder, callback);
    }

    /**
     * PUT 上传文件
     *
     * @param ufileRequest
     * @param callback
     */
    public void put(UFileRequest ufileRequest, Callback callback) {
        String url = upHost + "/" + Utils.urlEncode(ufileRequest.getKey());
        MediaType mediaType = MediaType.parse(ufileRequest.getContentType());
        File file = new File(ufileRequest.getFilePath());
        RequestBody requestBody = RequestBody.create(mediaType, file);
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        builder.put(requestBody);
        request(ufileRequest, builder, callback);
    }

    /**
     * POST 表单上传
     *
     * @param ufileRequest
     * @param callback
     */
    public void post(UFileRequest ufileRequest, Callback callback) {
        String url = upHost + "/";
        File file = new File(ufileRequest.getFilePath());
        MediaType mediaType = MediaType.parse(ufileRequest.getContentType());
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("Authorization", getAuthorities(ufileRequest))
                .addFormDataPart("FileName", ufileRequest.getKey())
                .addFormDataPart("file", ufileRequest.getKey(), RequestBody.create(mediaType, file))
                .build();
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        builder.post(requestBody);
        request(ufileRequest, builder, callback);
    }

    /**
     * DELETE 删除文件
     *
     * @param ufileRequest
     * @param callback
     */
    public void delete(UFileRequest ufileRequest, Callback callback) {
        String url = dlHost + "/" + Utils.urlEncode(ufileRequest.getKey());
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        builder.delete();
        request(ufileRequest, builder, callback);
    }

    /**
     * HEAD 查询文件基本信息
     *
     * @param ufileRequest
     * @param callback
     */
    public void head(UFileRequest ufileRequest, Callback callback) {
        String url = dlHost + "/" + Utils.urlEncode(ufileRequest.getKey());
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        builder.head();
        request(ufileRequest, builder, callback);
    }

    /**
     * UploadHit 秒传
     *
     * @param ufileRequest
     * @param callback
     */
    public void uploadHit(UFileRequest ufileRequest, Callback callback) {
        String hash = Utils.calcSHA1(ufileRequest.getFilePath());
        long length = new File(ufileRequest.getFilePath()).length();
        String param = Utils.urlEncode("Hash") + "=" + Utils.urlEncode(hash)
                + "&" + Utils.urlEncode("FileName") + "=" + Utils.urlEncode(ufileRequest.getKey())
                + "&" + Utils.urlEncode("FileSize") + "=" + Utils.urlEncode(String.valueOf(length));
        String url = upHost + "/" + "uploadhit?" + param;
        RequestBody requestBody = RequestBody.create(null, "");
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        builder.post(requestBody);
        request(ufileRequest, builder, callback);
    }

    /**
     * initMulti 初始化分片 同步请求，可自行修改成异步
     *
     * @param ufileRequest
     */
    public Response initMulti(UFileRequest ufileRequest) {
        String url = upHost + "/" + Utils.urlEncode(ufileRequest.getKey())
                + "?" + Utils.urlEncode("uploads");
        RequestBody requestBody = RequestBody.create(null, "");
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        builder.post(requestBody);
        return request(ufileRequest, builder);
    }

    /**
     * uploadPart 上传分片 同步请求，可自行修改成异步
     *
     * @param ufileRequest
     */
    public Response uploadPart(UFileRequest ufileRequest, String uploadId, int partNumber, int offset, int length) {
        String url = upHost + "/" + Utils.urlEncode(ufileRequest.getKey())
                + "?" + Utils.urlEncode("uploadId") + "=" + Utils.urlEncode(uploadId)
                + "&" + Utils.urlEncode("partNumber") + "=" + Utils.urlEncode("" + partNumber);

        byte[] data = new byte[length];
        File file = new File(ufileRequest.getFilePath());
        try (FileInputStream fis = new FileInputStream(file)) {
            fis.skip(offset);
            fis.read(data, 0, length);
        } catch (IOException e) {
            e.printStackTrace();
        }

        MediaType mediaType = MediaType.parse(ufileRequest.getContentType());
        RequestBody requestBody = RequestBody.create(mediaType, data, 0, length);
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        builder.put(requestBody);
        return request(ufileRequest, builder);
    }

    /**
     * finishPart 完成分片上传
     */
    public void finishPart(UFileRequest ufileRequest, String uploadId, String etags, String newKey, Callback callback) {
        String url = upHost + "/" + Utils.urlEncode(ufileRequest.getKey())
                + "?" + Utils.urlEncode("uploadId") + "=" + Utils.urlEncode(uploadId)
                + "&" + Utils.urlEncode("newKey") + "=" + Utils.urlEncode(newKey);
        RequestBody requestBody = RequestBody.create(null, etags);
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        builder.post(requestBody);
        request(ufileRequest, builder, callback);
    }

    /**
     * cancelPart 取消分片上传
     *
     * @param ufileRequest
     * @param uploadId
     */
    public void cancelPart(UFileRequest ufileRequest, String uploadId, Callback callback) {
        String url = dlHost + "/" + Utils.urlEncode(ufileRequest.getKey())
                + "?" + Utils.urlEncode("uploadId") + "=" + Utils.urlEncode(uploadId);
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        builder.delete();
        request(ufileRequest, builder, callback);
    }

    /**
     * PUT 上传String
     *
     * @param ufileRequest
     * @param callback
     */
    public void putString(UFileRequest ufileRequest, Callback callback, String string) {
        String url = upHost + "/" + Utils.urlEncode(ufileRequest.getKey());
        MediaType mediaType = MediaType.parse(ufileRequest.getContentType());
        RequestBody requestBody = RequestBody.create(mediaType, string.getBytes());
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        builder.put(requestBody);
        request(ufileRequest, builder, callback);
    }

    /**
     * PUT 上传stream
     *
     * @param ufileRequest
     * @param callback
     */
    public void putStream(UFileRequest ufileRequest, Callback callback, InputStream is) {
        String url = upHost + "/" + Utils.urlEncode(ufileRequest.getKey());
        MediaType mediaType = MediaType.parse(ufileRequest.getContentType());
        RequestBody requestBody = new RequestBody() {
            @Override
            public MediaType contentType() {
                return mediaType;
            }

            @Override
            public void writeTo(BufferedSink bufferedSink) throws IOException {
                byte[] buffer = new byte[1024];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    bufferedSink.write(buffer, 0, read);
                }
            }
        };
        Request.Builder builder = new Request.Builder();
        builder.url(url);
        builder.put(requestBody);
        request(ufileRequest, builder, callback);
    }

    /**
     * 异步请求
     *
     * @param ufileRequest
     * @param builder
     * @param callback
     */
    private void request(UFileRequest ufileRequest, Request.Builder builder, Callback callback) {
        String authorization = getAuthorities(ufileRequest);
        builder.headers(Headers.of(ufileRequest.getHeaders()));
        builder.addHeader(USER_AGENT, JAVA_SDK_VER);
        builder.addHeader(AUTHORIZATION, authorization);

        Request request = builder.build();
        System.out.println(request.method() + " " + request.url());
        System.out.println(request.headers());
        getOkHttpClient().newCall(request).enqueue(callback);
    }

    /**
     * 同步请求
     *
     * @param ufileRequest
     * @param builder
     * @return
     */
    private Response request(UFileRequest ufileRequest, Request.Builder builder) {
        String authorization = getAuthorities(ufileRequest);
        builder.headers(Headers.of(ufileRequest.getHeaders()));
        builder.addHeader(USER_AGENT, JAVA_SDK_VER);
        builder.addHeader(AUTHORIZATION, authorization);

        Request request = builder.build();
        System.out.println(request.method() + " " + request.url());
        System.out.println(request.headers());
        Response response = null;
        try {
            response = getOkHttpClient().newCall(request).execute();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return response;
    }

    /**
     * 计算签名 https://docs.ucloud.cn/storage_cdn/ufile/api
     *
     * @param request
     * @return
     */
    private String getAuthorities(UFileRequest request) {
        String httpMethod = request.getHttpMethod();
        String contentMD5 = request.getContentMD5();
        String contentType = request.getContentType();
        String date = request.getDate();
        String canonicalUCloudHeaders = getCanonicalHeaders(request);
        String canonicalResource = "/" + bucket + "/" + request.getKey();
        String stringToSign = httpMethod + "\n" + contentMD5 + "\n" + contentType + "\n" + date + "\n"
                + canonicalUCloudHeaders + canonicalResource;
        String signature = Utils.hmacSHA1(privateKey, stringToSign);
        return "UCloud" + " " + publicKey + ":" + signature;
    }

    private String getCanonicalHeaders(UFileRequest request) {
        Map<String, String> headers = request.getHeaders();
        Map<String, String> sortedMap = new TreeMap<>();
        if (headers != null) {
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                if (entry.getKey().startsWith(CANONICAL_PREFIX)) {
                    sortedMap.put(entry.getKey().toLowerCase(), entry.getValue());
                }
            }
            String result = "";
            for (Map.Entry<String, String> entry : sortedMap.entrySet()) {
                result += entry.getKey() + ":" + entry.getValue() + "\n";
            }
            return result;
        } else {
            return "";
        }
    }

    /**
     * 关闭，释放资源
     */
    public static void shutdown() {
        if (okHttpClient != null) {
            okHttpClient.dispatcher().executorService().shutdown();
            okHttpClient = null;
        }
    }

    /**
     * 创建okhttp单例
     */
    private OkHttpClient getOkHttpClient() {
        OkHttpClient client = okHttpClient;
        if (client == null) {
            synchronized (UFileSDK.class) {
                client = okHttpClient;
                if (client == null) {
                    client = okHttpClient = new OkHttpClient();
                }
            }
        }

        return client;
    }

}
