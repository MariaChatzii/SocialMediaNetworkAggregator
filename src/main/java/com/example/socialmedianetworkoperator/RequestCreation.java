package com.example.socialmedianetworkoperator;

import okhttp3.Request;
import okhttp3.RequestBody;

public class RequestCreation {

    public Request createRequest(String HTTPMethod,String url, RequestBody body, String headerName, String headerValue){

        Request request;
        if(headerValue==null){
            request = new Request.Builder()
                    .url(url)
                    .method(HTTPMethod, body)
                    .build();
        }else{
            request = new Request.Builder()
                    .url(url)
                    .method(HTTPMethod, body)
                    .addHeader(headerName, headerValue)
                    .build();
        }

        return request;
    }



}
