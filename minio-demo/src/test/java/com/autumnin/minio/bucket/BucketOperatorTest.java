package com.autumnin.minio.bucket;

import io.minio.http.Method;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.*;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class BucketOperatorTest {
    private final static Logger logger = LoggerFactory.getLogger(BucketOperatorTest.class);
    @Autowired
    private BucketOperator bucketOperator;

    @Test
    void create() {
        Result bucket = bucketOperator.create("bucket-test");
        assertNull(bucket.getException());
    }

    @Test
    void createIfNotExists() {
        Result result = bucketOperator.create("bucket-test");
        assertNotNull(result.getException());
        assertEquals(result.getState(), Result.State.FAILED);
    }

    @Test
    void getAll() {
        Result result = bucketOperator.getAll();
        assertNull(result.getException());
        assertEquals(result.getState(), Result.State.SUCCESS);
        assertTrue(result.getBuckets().stream().anyMatch(i -> i.name().equals("bucket-test")));
    }

    @Test
    void get() {
        Result result = bucketOperator.get("bucket-test");
        assertNull(result.getException());
        assertEquals(result.getState(), Result.State.SUCCESS);
        assertEquals(result.getBucket().name(), "bucket-test");
    }

    @Test
    void remove() {
        Result result = bucketOperator.remove("bucket-test");
        assertEquals(result.getState(), Result.State.SUCCESS);
        assertNull(result.getException());
    }

    @Test
    void putObject() {
        try {
            Resource resource = new ClassPathResource("object-test.txt");
            InputStream ins = resource.getInputStream();
            Result result = bucketOperator.putObject("bucket-test", "object.txt", ins);
            assertNull(result.getException());
            assertEquals(result.getState(), Result.State.SUCCESS);
            assertNotNull(result.getUrl());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void getObject() {
        try {
            Result result = bucketOperator.getObject("bucket-test", "object.txt");
            assertEquals(result.getState(), Result.State.SUCCESS);
            assertNull(result.getException());
            FileOutputStream fos = null;
            fos = new FileOutputStream("./object1.txt");
            byte[] b = new byte[1024];
            while ((result.getIns().read(b)) != -1) {
                fos.write(b);
            }
            result.getIns().close();
            fos.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void getObjectUrl() {
        Result result = bucketOperator.getObjectUrl(Method.GET, "bucket-test", "object.txt", 1, TimeUnit.HOURS);
        assertNull(result.getException());
        assertEquals(result.getState(), Result.State.SUCCESS);
        logger.info(result.getUrl());
    }

    @Test
    void removeObject() {
        Result result = bucketOperator.removeObject("bucket-test", "object.txt");
        assertEquals(result.getState(), Result.State.SUCCESS);
        assertNull(result.getException());
    }
}