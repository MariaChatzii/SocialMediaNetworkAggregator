package com.example.socialmedianetworkoperator;

import android.os.Build;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

public class OAuthHeaderGenerator {

    private static final String oauth_callback = "oauth_callback";
    private static final String oauth_consumer_key = "oauth_consumer_key";
    private static final String oauth_signature = "oauth_signature";
    private static final String oauth_signature_method = "oauth_signature_method";
    private static final String oauth_timestamp = "oauth_timestamp";
    private static final String oauth_token = "oauth_token";
    private static final String oauth_nonce = "oauth_nonce";
    private static final String oauth_version = "oauth_version";
    private static final String HMAC_SHA1 = "HmacSHA1";

    private final String callback;
    private final String consumerKey;
    private final String consumerSecret;
    private final String signatureMethod;
    private final String token;
    private final String tokenSecret;
    private final String version;

    public OAuthHeaderGenerator(String consumerKey, String consumerSecret, String token, String tokenSecret, String callback) {
        this.callback = callback;
        this.consumerKey = consumerKey;
        this.consumerSecret = consumerSecret;
        this.token = token;
        this.tokenSecret = tokenSecret;
        this.signatureMethod = "HMAC-SHA1";
        this.version = "1.0";
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public StringBuilder generateOAuthHeader(String httpMethod, String urlRequest, Map<String, String> requestParams){
        StringBuilder header = new StringBuilder();

        //get timestamp and nonce values
        String timestamp = getTimestamp();
        String nonce = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            nonce = getNonce();
        }
        String baseSignatureString = generateSignatureBaseString(httpMethod, urlRequest, requestParams, nonce, timestamp);
        String signature = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            signature = encryptUsingHmacSHA1(baseSignatureString);
        }

        header.append("OAuth ");
        if(callback!=null)
            append(header, oauth_callback, encode(callback));
        append(header, oauth_consumer_key, consumerKey);
        append(header, oauth_nonce, nonce);
        append(header, oauth_signature, signature);
        append(header, oauth_signature_method, signatureMethod);
        append(header, oauth_timestamp, timestamp);
        if(token!=null)
            append(header, oauth_token,token);
        append(header, oauth_version, version);

        Log.d("OAUTH_HEADER","Header: "+header);

        header.deleteCharAt(header.length() - 1);
        return header;

    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public String generateSignatureBaseString(String httpMethod, String url, Map<String, String> requestParams, String nonce, String timestamp) {
        Map<String, String> params = new HashMap<>();
        requestParams.entrySet().forEach(entry -> put(params, entry.getKey(), entry.getValue()));

        if(callback!=null)
            put(params, oauth_callback, callback);
        put(params, oauth_consumer_key, consumerKey);
        put(params, oauth_nonce, nonce);
        put(params, oauth_signature_method, signatureMethod);
        put(params, oauth_timestamp, timestamp);

        if(token!=null)
            put(params, oauth_token, token);
        put(params, oauth_version, version);

        Map<String, String> sortedParams = params.entrySet().stream().sorted(Map.Entry.comparingByKey())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (oldValue, newValue) -> oldValue, LinkedHashMap::new));
        StringBuilder base = new StringBuilder();
        sortedParams.entrySet().forEach(entry -> base.append(entry.getKey()).append("=").append(entry.getValue()).append("&"));
        base.deleteCharAt(base.length() - 1);
        String baseString = httpMethod.toUpperCase() + "&" + encode(url) + "&" + encode(base.toString());
        return baseString;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public String getNonce() {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 10;
        Random random = new Random();

        String generatedString = random.ints(leftLimit, rightLimit + 1).filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97)).limit(targetStringLength)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString();
        return generatedString;
    }

    public String getTimestamp() {
        return Math.round((new Date()).getTime() / 1000.0) + "";
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    public String encryptUsingHmacSHA1(String input) {
        StringBuilder secretStringBuilder = new StringBuilder();
        secretStringBuilder.append(encode(consumerSecret)).append("&");
        if(tokenSecret!=null)
            secretStringBuilder.append(encode(tokenSecret));
        String secret = secretStringBuilder.toString();

        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        SecretKey key = new SecretKeySpec(keyBytes, HMAC_SHA1);
        Mac mac;
        try {
            mac = Mac.getInstance(HMAC_SHA1);
            mac.init(key);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            e.printStackTrace();
            return null;
        }
        byte[] signatureBytes = mac.doFinal(input.getBytes(StandardCharsets.UTF_8));
        String result =  new String(Base64.getEncoder().encode(signatureBytes));
        return encode(result);
    }

    /**
     * Percentage encode String as per RFC 3986, Section 2.1
     */
    public String encode(String value) {
        String encoded = "";
        try {
            encoded = URLEncoder.encode(value, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        StringBuilder sb = new StringBuilder();
        char focus;
        for (int i = 0; i < encoded.length(); i++) {
            focus = encoded.charAt(i);
            if (focus == '*') {
                sb.append("%2A");
            } else if (focus == '+') {
                sb.append("%20");
            } else if (focus == '%' && i + 1 < encoded.length() && encoded.charAt(i + 1) == '7' && encoded.charAt(i + 2) == 'E') {
                sb.append('~');
                i += 2;
            } else {
                sb.append(focus);
            }
        }
        return sb.toString();
    }

    public void put(Map<String, String> map, String key, String value) {
        map.put(encode(key), encode(value));
    }

    private void append(StringBuilder builder, String key, String value) {
        builder.append(key).append("=\"").append(value).append("\",");
    }
}