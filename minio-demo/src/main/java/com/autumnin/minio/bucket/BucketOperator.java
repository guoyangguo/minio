package com.autumnin.minio.bucket;

import com.autumnin.minio.config.MinIOProperties;
import io.minio.*;
import io.minio.http.Method;
import io.minio.messages.Bucket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Guoyang
 * @description bucket 操作类
 * @date 2022/11/5
 **/
@Component
public class BucketOperator {
    private final static Logger logger = LoggerFactory.getLogger(BucketOperator.class);
    private final MinioClient minioClient;
    private final MinIOProperties properties;

    public BucketOperator(MinioClient minioClient, MinIOProperties properties) {
        this.minioClient = minioClient;
        this.properties = properties;
    }


    /**
     * 创建给定 bucketName的bucket
     *
     * @param bucketName bucket name
     * @return Result
     */
    public Result create(String bucketName) {
        try {
            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
                String url = String.format("%s/%s", properties.getEndpoint(), bucketName);
                return Result.builder().url(url).type(Result.Type.BUCKET).state(Result.State.SUCCESS).build();
            } else {
                return Result.builder().type(Result.Type.BUCKET).state(Result.State.FAILED).exception(new RuntimeException(String.format("[%s] bucket already exists", bucketName))).build();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.builder().type(Result.Type.BUCKET).state(Result.State.FAILED).exception(e).build();
        }
    }

    /**
     * 创建给定 bucketName的bucket
     *
     * @param bucketName bucket name
     * @return Result
     */
    public Result createIfNotExists(String bucketName) {
        try {
            if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
            } else {
                logger.warn(String.format("[%s] bucket already exists", bucketName));
            }
            String url = String.format("%s/%s", properties.getEndpoint(), bucketName);
            return Result.builder().url(url).type(Result.Type.BUCKET).state(Result.State.SUCCESS).build();
        } catch (Exception e) {
            return Result.builder().type(Result.Type.BUCKET).state(Result.State.FAILED).exception(e).build();
        }
    }


    /**
     * 获取所有的buket
     *
     * @return List<Bucket>
     */
    public Result getAll() {
        try {
            List<Bucket> buckets = minioClient.listBuckets();
            return Result.builder().buckets(buckets).type(Result.Type.BUCKET).state(Result.State.SUCCESS).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.builder().type(Result.Type.BUCKET).state(Result.State.FAILED).exception(e).build();
        }
    }

    /**
     * 根据 bucket name 获取指定的bucket
     *
     * @param bucketName bucket 名称
     * @return Bucket
     */
    public Result get(String bucketName) {
        try {
            Bucket bucket = minioClient.listBuckets().stream().filter(item -> item.name().equals(bucketName)).findFirst().orElse(null);
            return Result.builder().bucket(bucket).type(Result.Type.BUCKET).state(Result.State.SUCCESS).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.builder().type(Result.Type.BUCKET).state(Result.State.FAILED).exception(e).build();
        }
    }

    /**
     * 移除给定bucket name的bucket
     *
     * @param bucketName bucket name
     * @result Result
     */
    public Result remove(String bucketName) {
        try {
            minioClient.removeBucket(RemoveBucketArgs.builder().bucket(bucketName).build());
            return Result.builder().type(Result.Type.BUCKET).state(Result.State.SUCCESS).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.builder().exception(e).build();
        }
    }

    /**
     * 获取给定bucket中的object
     *
     * @param bucketName bucket name
     * @param objectName object name
     * @return Result
     */
    public Result getObject(String bucketName, String objectName) {
        try {
            GetObjectResponse response = minioClient.getObject(GetObjectArgs.builder().bucket(bucketName).object(objectName).build());
            return Result.builder().type(Result.Type.OBJECT).state(Result.State.SUCCESS).ins(response).build();
        } catch (Exception e) {
            return Result.builder().type(Result.Type.OBJECT).state(Result.State.FAILED).exception(e).build();
        }
    }

    /**
     * 获取对象的URL
     *
     * @param bucketName bucket name
     * @param objectName object name
     * @return
     */
    public Result getObjectUrl(Method method, String bucketName, String objectName, int expire, TimeUnit unit) {
        try {
            String url = minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder().bucket(bucketName).object(objectName).expiry(expire, unit).method(method).build());
            return Result.builder().url(url).type(Result.Type.OBJECT).state(Result.State.SUCCESS).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.builder().type(Result.Type.OBJECT).state(Result.State.FAILED).exception(e).build();
        }
    }

    /**
     * @param bucketName  bucket name
     * @param objectName  object name
     * @param ins         文件流
     * @param size        文件大小
     * @param contentType 文件类型
     */
    public Result putObject(String bucketName, String objectName, InputStream ins, long size, String contentType) {
        try {
            Result exists = createIfNotExists(bucketName);
            if (exists.getException() != null) {
                return Result.builder().type(Result.Type.OBJECT).state(Result.State.FAILED).exception(exists.getException()).build();
            }
            minioClient.putObject(PutObjectArgs.builder().bucket(bucketName).object(objectName).stream(ins, size, -1).contentType(contentType).build());
            String url = String.format("%s/%s%s", properties.getEndpoint(), bucketName, objectName);
            return Result.builder().url(url).type(Result.Type.OBJECT).state(Result.State.SUCCESS).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.builder().type(Result.Type.OBJECT).state(Result.State.FAILED).exception(e).build();
        }
    }

    /**
     * @param bucketName bucket name
     * @param objectName object name
     * @param ins        file input stream
     */
    public Result putObject(String bucketName, String objectName, InputStream ins) {
        try {
            String contentType = objectName.substring(objectName.lastIndexOf("."));
            int size = ins.available();
            return putObject(bucketName, objectName, ins, size, contentType);
        } catch (Exception e) {
            e.printStackTrace();
            return Result.builder().type(Result.Type.OBJECT).state(Result.State.FAILED).exception(e).build();
        }
    }

    /**
     * @param file
     * @param bucketName
     * @param objectName
     */
    public Result putObject(MultipartFile file, String bucketName, String objectName) {
        if (file == null) {
            logger.error("file can not be null");
            return Result.builder().type(Result.Type.OBJECT).state(Result.State.FAILED).exception(new Exception("file can not be null")).build();
        }
        if (objectName == null) {
            objectName = file.getOriginalFilename();
        }
        try {
            InputStream ins = file.getInputStream();
            long size = file.getSize();
            String contentType = file.getContentType();
            return putObject(bucketName, objectName, ins, size, contentType);
        } catch (IOException e) {
            e.printStackTrace();
            return Result.builder().type(Result.Type.OBJECT).state(Result.State.FAILED).exception(e).build();
        }

    }

    /**
     * 删除给定的 bucket的object
     *
     * @param bucketName bucket name
     * @param objectName object name
     */
    public Result removeObject(String bucketName, String objectName) {
        try {
            minioClient.removeObject(RemoveObjectArgs.builder().bucket(bucketName).object(objectName).build());
            return Result.builder().type(Result.Type.OBJECT).state(Result.State.SUCCESS).build();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.builder().type(Result.Type.OBJECT).state(Result.State.FAILED).exception(e).build();
        }
    }


}
