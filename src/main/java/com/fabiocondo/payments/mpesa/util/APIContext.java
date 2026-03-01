package com.fabiocondo.payments.mpesa.util;

//import com.fc.sdk.APIMethodType;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class APIContext {
    private APIMethodType methodType;
    private boolean ssl;
    private String address;
    private int port;
    private String path;
    private String apiKey;
    private String publicKey;
    private Map<String, String> headers;
    private Map<String, String> parameters;
    private static final String METHOD_GET = "GET";
    private static final String METHOD_POST = "POST";
    private static final String METHOD_PUT = "PUT";

    public APIContext() {
        this.methodType = APIMethodType.GET;
        this.ssl = false;
        this.address = "localhost";
        this.port = 80;
        this.path = "/";
        this.apiKey = "";
        this.publicKey = "";
        this.headers = new LinkedHashMap();
        this.parameters = new LinkedHashMap();
    }

    public String toString() {
        StringBuilder request = new StringBuilder();
        switch (this.methodType) {
            case GET:
                StringBuilder getUrl = new StringBuilder(String.format("%s%s", this.getUrl(), this.genenrateGetParameters()));
                request.append(String.format("%s %s HTTP/1.1%n", "GET", getUrl.toString()));
                break;
            case POST:
                request.append(String.format("%s %s HTTP/1.1%n", "POST", this.getUrl()));
                break;
            case PUT:
                request.append(String.format("%s %s HTTP/1.1%n", "PUT", this.getUrl()));
        }

        Iterator var4 = this.headers.entrySet().iterator();

        while(var4.hasNext()) {
            Map.Entry<String, String> entry = (Map.Entry)var4.next();
            request.append(String.format("%s: %s%n", entry.getKey(), entry.getValue()));
        }

        return request.toString();
    }

    public String generateJSON() {
        StringBuilder p = new StringBuilder("{");
        Iterator var2 = this.parameters.entrySet().iterator();

        while(var2.hasNext()) {
            Map.Entry<String, String> entry = (Map.Entry)var2.next();
            p.append(String.format("\"%s\": \"%s\",", entry.getKey(), entry.getValue()));
        }

        return p.toString().substring(0, p.length() - 1) + "}";
    }

    public String genenrateGetParameters() {
        StringBuilder p = new StringBuilder("?");
        Iterator var2 = this.parameters.entrySet().iterator();

        while(var2.hasNext()) {
            Map.Entry<String, String> entry = (Map.Entry)var2.next();

            try {
                p.append(String.format("%s=%s&", entry.getKey(), URLEncoder.encode((String)entry.getValue(), "UTF-8")));
            } catch (UnsupportedEncodingException var5) {
                var5.printStackTrace();
            }
        }

        return p.toString().substring(0, p.length() - 1);
    }

    public String getUrl() {
        String url;
        if (this.ssl) {
            url = String.format("https://%s:%d%s", this.address, this.port, this.path);
        } else {
            url = String.format("http://%s:%d%s", this.address, this.port, this.path);
        }

        return url;
    }

    public void addParameter(String key, String value) {
        this.parameters.put(key, value);
    }

    public void addHeader(String key, String value) {
        this.headers.put(key, value);
    }

    public Map getHeaders() {
        return this.headers;
    }

    public APIMethodType getMethodType() {
        return this.methodType;
    }

    public void setMethodType(APIMethodType methodType) {
        this.methodType = methodType;
    }

    public boolean isSsl() {
        return this.ssl;
    }

    public void setSsl(boolean ssl) {
        this.ssl = ssl;
    }

    public String getAddress() {
        return this.address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPath() {
        return this.path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getApiKey() {
        return this.apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getPublicKey() {
        return this.publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }
}

