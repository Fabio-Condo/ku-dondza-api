package com.fabiocondo.aws.model;

public class S3ObjectFilter {

    private String prefix;
    private int maxKeys;
    private String continuationToken;
    private String nextContinuationToken;

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public int getMaxKeys() {
        return maxKeys;
    }

    public void setMaxKeys(int maxKeys) {
        this.maxKeys = maxKeys;
    }

    public String getContinuationToken() {
        return continuationToken;
    }

    public void setContinuationToken(String continuationToken) {
        this.continuationToken = continuationToken;
    }

    public String getNextContinuationToken() {
        return nextContinuationToken;
    }

    public void setNextContinuationToken(String nextContinuationToken) {
        this.nextContinuationToken = nextContinuationToken;
    }
}
