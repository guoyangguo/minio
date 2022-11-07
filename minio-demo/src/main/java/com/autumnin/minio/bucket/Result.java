package com.autumnin.minio.bucket;

import io.minio.messages.Bucket;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.List;

/**
 * @author Guoyang
 * @description MinIo bucket 操作结果
 * @date 2022/11/6
 **/
public class Result {
    private final String url;
    private final Type type;
    private final State state;

    private final InputStream ins;

    private final Bucket bucket;

    private final List<Bucket> buckets;

    private final Exception exception;

    private final LocalDateTime updateTime;

    private Result(Builder builder) {
        this.url = builder.url;
        this.type = builder.type;
        this.state = builder.state;
        this.bucket = builder.bucket;
        this.buckets = builder.buckets;
        this.ins = builder.ins;
        this.exception = builder.exception;
        this.updateTime = builder.updateTime;
    }

    public String getUrl() {
        return url;
    }

    public Type getType() {
        return type;
    }

    public State getState() {
        return state;
    }

    public InputStream getIns() {
        return ins;
    }

    public Bucket getBucket() {
        return bucket;
    }

    public List<Bucket> getBuckets() {
        return buckets;
    }

    public Exception getException() {
        return exception;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public static Builder builder() {
        return new Builder();
    }

    protected static class Builder {
        private String url;
        private Type type;
        private State state;
        private InputStream ins;
        private Bucket bucket;
        private List<Bucket> buckets;
        private Exception exception;

        private LocalDateTime updateTime = LocalDateTime.now();

        public Builder url(String url) {
            this.url = url;
            return this;
        }

        public Builder type(Type type) {
            this.type = type;
            return this;
        }

        public Builder state(State state) {
            this.state = state;
            return this;
        }

        public Builder updateTime(LocalDateTime updateTime) {
            this.updateTime = updateTime;
            return this;
        }

        public Builder bucket(Bucket bucket) {
            this.bucket = bucket;
            return this;
        }

        public Builder buckets(List<Bucket> buckets) {
            this.buckets = buckets;
            return this;
        }

        public Builder ins(InputStream ins) {
            this.ins = ins;
            return this;
        }

        public Builder exception(Exception exception) {
            this.exception = exception;
            return this;
        }

        public Result build() {
            if (this.state == State.FAILED && this.exception == null) {
                throw new RuntimeException("exception can not be null when state is failed");
            }
            return new Result(this);
        }
    }

    enum State {
        SUCCESS,
        FAILED,
    }

    enum Type {
        BUCKET,
        OBJECT
    }

    @Override
    public String toString() {
        return "Result{" +
                "url='" + url + '\'' +
                ", type=" + type +
                ", state=" + state +
                ", ins=" + ins +
                ", bucket=" + bucket +
                ", buckets=" + buckets +
                ", exception=" + exception +
                ", updateTime=" + updateTime +
                '}';
    }
}
