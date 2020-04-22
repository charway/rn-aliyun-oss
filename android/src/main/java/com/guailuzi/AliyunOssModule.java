package com.guailuzi;


import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;


import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.ClientException;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.ServiceException;
import com.alibaba.sdk.android.oss.callback.OSSCompletedCallback;
import com.alibaba.sdk.android.oss.callback.OSSProgressCallback;
import com.alibaba.sdk.android.oss.common.OSSConstants;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSCustomSignerCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSFederationCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSFederationToken;
import com.alibaba.sdk.android.oss.common.auth.OSSStsTokenCredentialProvider;
import com.alibaba.sdk.android.oss.common.utils.IOUtils;
import com.alibaba.sdk.android.oss.internal.OSSAsyncTask;
import com.alibaba.sdk.android.oss.model.AbortMultipartUploadRequest;
import com.alibaba.sdk.android.oss.model.AppendObjectRequest;
import com.alibaba.sdk.android.oss.model.AppendObjectResult;
import com.alibaba.sdk.android.oss.model.CompleteMultipartUploadRequest;
import com.alibaba.sdk.android.oss.model.CompleteMultipartUploadResult;
import com.alibaba.sdk.android.oss.model.CopyObjectRequest;
import com.alibaba.sdk.android.oss.model.CopyObjectResult;
import com.alibaba.sdk.android.oss.model.DeleteBucketRequest;
import com.alibaba.sdk.android.oss.model.DeleteBucketResult;
import com.alibaba.sdk.android.oss.model.DeleteObjectRequest;
import com.alibaba.sdk.android.oss.model.DeleteObjectResult;
import com.alibaba.sdk.android.oss.model.GetObjectRequest;
import com.alibaba.sdk.android.oss.model.GetObjectResult;
import com.alibaba.sdk.android.oss.model.HeadObjectRequest;
import com.alibaba.sdk.android.oss.model.HeadObjectResult;
import com.alibaba.sdk.android.oss.model.InitiateMultipartUploadRequest;
import com.alibaba.sdk.android.oss.model.InitiateMultipartUploadResult;
import com.alibaba.sdk.android.oss.model.ListBucketsRequest;
import com.alibaba.sdk.android.oss.model.ListBucketsResult;
import com.alibaba.sdk.android.oss.model.ListObjectsRequest;
import com.alibaba.sdk.android.oss.model.ListObjectsResult;
import com.alibaba.sdk.android.oss.model.ListPartsRequest;
import com.alibaba.sdk.android.oss.model.ListPartsResult;
import com.alibaba.sdk.android.oss.model.ObjectMetadata;
import com.alibaba.sdk.android.oss.model.PartETag;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;
import com.alibaba.sdk.android.oss.model.ResumableUploadRequest;
import com.alibaba.sdk.android.oss.model.ResumableUploadResult;
import com.alibaba.sdk.android.oss.model.UploadPartRequest;
import com.alibaba.sdk.android.oss.model.UploadPartResult;


import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AliyunOssModule extends ReactContextBaseJavaModule {

    private final ReactApplicationContext reactContext;

    private OSS mOSS;
    private AliyunAuthManager mAuth;
    private AliyunUploadManager mUploadManager;
    private AliyunDownloadManager mDownloadManager;

    public AliyunOssModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        mAuth = new AliyunAuthManager(reactContext.getApplicationContext(), new AliyunAuthManager.AuthListener() {
            @Override
            public void onAuthFinished(OSS oss) {
                init(oss);
            }
        });
    }

    @Override
    public String getName() {
        return "AliyunOss";
    }

    /**
     * enable dev log
     */
    @ReactMethod
    public void enableDevMode() {
        OSSLog.enableLog();
    }

    /**
     * init oss ReactMethod
     * @param oss
     */
    private void init(OSS oss) {
        mOSS = oss;
        mUploadManager = new AliyunUploadManager(mOSS);
        mDownloadManager = new AliyunDownloadManager(mOSS);
    }

     /**
     * initWithSigner ReactMethod
     * @param signature
     * @param accessKey
     * @param endPoint
     * @param configuration
     */
    @ReactMethod
    public void initWithSigner(final String signature, final String accessKey, String endPoint, ReadableMap configuration) {
        mAuth.initWithSigner(signature, accessKey, endPoint, configuration);
    }

    /**
     * initWithPlainTextAccessKey ReactMethod
     * @param accessKeyId
     * @param accessKeySecret
     * @param endPoint
     * @param configuration
     */
    @ReactMethod
    public void initWithPlainTextAccessKey(String accessKeyId, String accessKeySecret, String endPoint, ReadableMap configuration) {
        mAuth.initWithPlainTextAccessKey(accessKeyId, accessKeySecret, endPoint, configuration);
    }

    /**
     * initWithSecurityToken ReactMethod
     * @param securityToken
     * @param accessKeyId
     * @param accessKeySecret
     * @param endPoint
     * @param configuration
     */
    @ReactMethod
    public void initWithSecurityToken(String securityToken, String accessKeyId, String accessKeySecret, String endPoint, ReadableMap configuration) {
        mAuth.initWithSecurityToken(securityToken, accessKeyId, accessKeySecret, endPoint, configuration);
    }

    /**
     * initWithServerSTS ReactMethod
     * @param server
     * @param endPoint
     * @param configuration
     */
    @ReactMethod
    public void initWithServerSTS(final String server, String endPoint, ReadableMap configuration) {
        mAuth.initWithServerSTS(server, endPoint, configuration);
    }

    /**
     * async Upload ReactMethod
     * @param bucketName
     * @param ossFile
     * @param sourceFile
     * @param promise
     */
    @ReactMethod
    public void asyncUpload(String bucketName, String ossFile, String sourceFile,ReadableMap options, final Promise promise) {
        mUploadManager.asyncUpload(getReactApplicationContext(), bucketName, ossFile, sourceFile, options, promise);
    }

    /**
     * asyncDownload and image process ReactMethod
     * @param bucketName
     * @param ossFile
     * @param updateDate
     * @param options
     * @param promise
     */
    @ReactMethod
    public void asyncDownload(String bucketName, String ossFile, String updateDate,ReadableMap options, final Promise promise) {
        mDownloadManager.asyncDownload(getReactApplicationContext(), bucketName, ossFile, updateDate, options, promise);
    }
}
