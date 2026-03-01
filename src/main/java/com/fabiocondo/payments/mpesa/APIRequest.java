package com.fabiocondo.payments.mpesa;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.crypto.Cipher;

//import com.fc.sdk.APIContext;
//import com.fc.sdk.APIResponse;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.JSONObject;

public class APIRequest {
    private APIContext context;

    public APIRequest(APIContext context) {
        this.context = context;
        this.createDefaultHeaders();
    }

    public APIResponse execute() {
        switch (this.context.getMethodType()) {
            case GET:
                return this.get();
            case POST:
                return this.post();
            case PUT:
                return this.put();
            default:
                return null;
        }
    }

    private APIResponse get() {
        try {
            CloseableHttpClient client = HttpClientBuilder.create().build();
            Throwable var2 = null;

            try {
                String url = this.context.getUrl() + this.context.genenrateGetParameters();
                HttpGet request = new HttpGet(url);
                Map<String, String> headers = this.context.getHeaders();
                Iterator var6 = headers.entrySet().iterator();

                while(var6.hasNext()) {
                    Map.Entry<String, String> entry = (Map.Entry)var6.next();
                    request.addHeader((String)entry.getKey(), (String)entry.getValue());
                }

                try {
                    CloseableHttpResponse response = client.execute(request);
                    Throwable var76 = null;

                    try {
                        BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                        Throwable var9 = null;

                        try {
                            StringBuilder sb = new StringBuilder();
                            String line = null;

                            while((line = br.readLine()) != null) {
                                sb.append(line);
                            }

                            APIResponse apiResponse = new APIResponse();
                            apiResponse.setStatusCode(response.getStatusLine().getStatusCode());
                            apiResponse.setReason(response.getStatusLine().getReasonPhrase());
                            apiResponse.setStatusLine(response.getStatusLine().toString());
                            apiResponse.setResult(sb.toString());
                            JSONObject jsonObj = new JSONObject(sb.toString());
                            Iterator keys = jsonObj.keys();
                            Map<String, String> parameters = new LinkedHashMap();

                            while(keys.hasNext()) {
                                String key = (String)keys.next();
                                parameters.put(key, jsonObj.get(key).toString());
                            }

                            apiResponse.setParameters(parameters);
                            APIResponse var77 = apiResponse;
                            return var77;
                        } catch (Throwable var67) {
                            var9 = var67;
                            throw var67;
                        } finally {
                            if (br != null) {
                                if (var9 != null) {
                                    try {
                                        br.close();
                                    } catch (Throwable var66) {
                                        var9.addSuppressed(var66);
                                    }
                                } else {
                                    br.close();
                                }
                            }

                        }
                    } catch (Throwable var69) {
                        var76 = var69;
                        throw var69;
                    } finally {
                        if (response != null) {
                            if (var76 != null) {
                                try {
                                    response.close();
                                } catch (Throwable var65) {
                                    var76.addSuppressed(var65);
                                }
                            } else {
                                response.close();
                            }
                        }

                    }
                } catch (Exception var71) {
                    System.err.println(var71.getMessage());
                    return null;
                }
            } catch (Throwable var72) {
                var2 = var72;
                throw var72;
            } finally {
                if (client != null) {
                    if (var2 != null) {
                        try {
                            client.close();
                        } catch (Throwable var64) {
                            var2.addSuppressed(var64);
                        }
                    } else {
                        client.close();
                    }
                }

            }
        } catch (Exception var74) {
            System.err.println(var74.getMessage());
            return null;
        }
    }

    private APIResponse post() {
        try {
            CloseableHttpClient client = HttpClientBuilder.create().build();
            Throwable var2 = null;

            try {
                String url = this.context.getUrl();
                HttpPost request = new HttpPost(url);
                Map<String, String> headers = this.context.getHeaders();
                Iterator var6 = headers.entrySet().iterator();

                while(var6.hasNext()) {
                    Map.Entry<String, String> entry = (Map.Entry)var6.next();
                    request.addHeader((String)entry.getKey(), (String)entry.getValue());
                }

                StringEntity entity = new StringEntity(this.context.generateJSON());
                request.setEntity(entity);

                try {
                    CloseableHttpResponse response = client.execute(request);
                    Throwable var8 = null;

                    try {
                        BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                        Throwable var10 = null;

                        try {
                            StringBuilder sb = new StringBuilder();
                            String line = null;

                            while((line = br.readLine()) != null) {
                                sb.append(line);
                            }

                            APIResponse apiResponse = new APIResponse();
                            apiResponse.setStatusCode(response.getStatusLine().getStatusCode());
                            apiResponse.setReason(response.getStatusLine().getReasonPhrase());
                            apiResponse.setStatusLine(response.getStatusLine().toString());
                            apiResponse.setResult(sb.toString());
                            JSONObject jsonObj = new JSONObject(sb.toString());
                            Iterator keys = jsonObj.keys();
                            Map<String, String> parameters = new LinkedHashMap();

                            while(keys.hasNext()) {
                                String key = (String)keys.next();
                                parameters.put(key, jsonObj.get(key).toString());
                            }

                            apiResponse.setParameters(parameters);
                            APIResponse var78 = apiResponse;
                            return var78;
                        } catch (Throwable var68) {
                            var10 = var68;
                            throw var68;
                        } finally {
                            if (br != null) {
                                if (var10 != null) {
                                    try {
                                        br.close();
                                    } catch (Throwable var67) {
                                        var10.addSuppressed(var67);
                                    }
                                } else {
                                    br.close();
                                }
                            }

                        }
                    } catch (Throwable var70) {
                        var8 = var70;
                        throw var70;
                    } finally {
                        if (response != null) {
                            if (var8 != null) {
                                try {
                                    response.close();
                                } catch (Throwable var66) {
                                    var8.addSuppressed(var66);
                                }
                            } else {
                                response.close();
                            }
                        }

                    }
                } catch (Exception var72) {
                    System.out.println(var72.getMessage());
                    return null;
                }
            } catch (Throwable var73) {
                var2 = var73;
                throw var73;
            } finally {
                if (client != null) {
                    if (var2 != null) {
                        try {
                            client.close();
                        } catch (Throwable var65) {
                            var2.addSuppressed(var65);
                        }
                    } else {
                        client.close();
                    }
                }

            }
        } catch (Exception var75) {
            System.out.println(var75.getMessage());
            return null;
        }
    }

    private APIResponse put() {
        try {
            CloseableHttpClient client = HttpClientBuilder.create().build();
            Throwable var2 = null;

            try {
                String url = this.context.getUrl();
                HttpPut request = new HttpPut(url);
                Map<String, String> headers = this.context.getHeaders();
                Iterator var6 = headers.entrySet().iterator();

                while(var6.hasNext()) {
                    Map.Entry<String, String> entry = (Map.Entry)var6.next();
                    request.addHeader((String)entry.getKey(), (String)entry.getValue());
                }

                StringEntity entity = new StringEntity(this.context.generateJSON());
                request.setEntity(entity);

                try {
                    CloseableHttpResponse response = client.execute(request);
                    Throwable var8 = null;

                    try {
                        BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                        Throwable var10 = null;

                        try {
                            StringBuilder sb = new StringBuilder();
                            String line = null;

                            while((line = br.readLine()) != null) {
                                sb.append(line);
                            }

                            APIResponse apiResponse = new APIResponse();
                            apiResponse.setStatusCode(response.getStatusLine().getStatusCode());
                            apiResponse.setReason(response.getStatusLine().getReasonPhrase());
                            apiResponse.setStatusLine(response.getStatusLine().toString());
                            apiResponse.setResult(sb.toString());
                            JSONObject jsonObj = new JSONObject(sb.toString());
                            Iterator keys = jsonObj.keys();
                            Map<String, String> parameters = new LinkedHashMap();

                            while(keys.hasNext()) {
                                String key = (String)keys.next();
                                parameters.put(key, jsonObj.get(key).toString());
                            }

                            apiResponse.setParameters(parameters);
                            APIResponse var78 = apiResponse;
                            return var78;
                        } catch (Throwable var68) {
                            var10 = var68;
                            throw var68;
                        } finally {
                            if (br != null) {
                                if (var10 != null) {
                                    try {
                                        br.close();
                                    } catch (Throwable var67) {
                                        var10.addSuppressed(var67);
                                    }
                                } else {
                                    br.close();
                                }
                            }

                        }
                    } catch (Throwable var70) {
                        var8 = var70;
                        throw var70;
                    } finally {
                        if (response != null) {
                            if (var8 != null) {
                                try {
                                    response.close();
                                } catch (Throwable var66) {
                                    var8.addSuppressed(var66);
                                }
                            } else {
                                response.close();
                            }
                        }

                    }
                } catch (Exception var72) {
                    System.out.println(var72.getMessage());
                    return null;
                }
            } catch (Throwable var73) {
                var2 = var73;
                throw var73;
            } finally {
                if (client != null) {
                    if (var2 != null) {
                        try {
                            client.close();
                        } catch (Throwable var65) {
                            var2.addSuppressed(var65);
                        }
                    } else {
                        client.close();
                    }
                }

            }
        } catch (Exception var75) {
            System.out.println(var75.getMessage());
            return null;
        }
    }

    private String getBearerToken(String apiKey, String publicKey) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            Cipher cipher = Cipher.getInstance("RSA");
            byte[] encodedPublicKey = Base64.decodeBase64(publicKey);
            X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(encodedPublicKey);
            PublicKey pk = keyFactory.generatePublic(publicKeySpec);
            cipher.init(1, pk);
            byte[] encryptedApiKey = Base64.encodeBase64(cipher.doFinal(apiKey.getBytes("UTF-8")));
            return new String(encryptedApiKey, "UTF-8");
        } catch (Exception var9) {
            System.out.println(var9.getMessage());
            return null;
        }
    }

    private void createDefaultHeaders() {
        this.context.addHeader("Host", this.context.getAddress());
        this.context.addHeader("Content-Type", "application/json");
        this.context.addHeader("Authorization", "Bearer " + this.getBearerToken(this.context.getApiKey(), this.context.getPublicKey()));
    }
}

