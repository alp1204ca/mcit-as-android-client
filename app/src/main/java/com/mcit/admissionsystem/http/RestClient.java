package com.mcit.admissionsystem.http;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RestClient<T>{

    private String server = "http://localhost:8090/admission-system/";

    private RestTemplate rest;
    private HttpHeaders headers;
    private HttpStatus status;

    private static RestClient instance = null;

    public static RestClient getInstance() {
        if (instance == null)
            instance = new RestClient();
        return instance;
    }

    public void cleanUpHeaders() {
        this.headers = new HttpHeaders();
        headers.add("Content-Type", "application/json");
        headers.add("Accept", "*/*");
    }


    private RestClient() {
        this.rest = new RestTemplate();
        cleanUpHeaders();
    }

    public String get(String uri) {
        HttpEntity<String> requestEntity = new HttpEntity<String>("", headers);
        ResponseEntity<String> responseEntity = rest.exchange(server + uri, HttpMethod.GET, requestEntity, String.class);
        this.setStatus(responseEntity.getStatusCode());
        return responseEntity.getBody();
    }

    public String postAndSetSession(String uri, String json) {
        HttpEntity<String> requestEntity = new HttpEntity<String>(json, headers);
        ResponseEntity<String> responseEntity = rest.exchange(server + uri, HttpMethod.POST, requestEntity, String.class);
        this.setStatus(responseEntity.getStatusCode());
        Set<String> headerKeys = responseEntity.getHeaders().keySet();
        if (this.status == HttpStatus.OK && headerKeys.contains("Set-Cookie"))
            for(String c :  responseEntity.getHeaders().get("Set-Cookie"))
                if (c.contains("JSESSIONID"))
                    this.headers.add("Cookie", c);

        return responseEntity.getBody();
    }

    public String post(String uri, T entity) {
        HttpEntity<T> requestEntity = new HttpEntity<>(entity, headers);
        ResponseEntity<String> responseEntity = rest.exchange(server + uri, HttpMethod.POST, requestEntity, String.class);
        this.setStatus(responseEntity.getStatusCode());
        return responseEntity.getBody();
    }

    public void put(String uri, String json) {
        HttpEntity<String> requestEntity = new HttpEntity<String>(json, headers);
        Map<String, String> variables = null;
        ResponseEntity<String> responseEntity = rest.exchange(server + uri, HttpMethod.PUT, requestEntity, String.class, variables);
        this.setStatus(responseEntity.getStatusCode());
    }

    public String delete(String uri, long id) {
        HttpEntity<String> requestEntity = new HttpEntity<String>("", headers);
        Map<String, Long> variables = new HashMap<>();
        variables.put("id", id);
        ResponseEntity<String> responseEntity = rest.exchange(server + uri + "/" + id, HttpMethod.DELETE, requestEntity, String.class, variables);
        this.setStatus(responseEntity.getStatusCode());
        return responseEntity.getBody();
    }

    public String delete(String uri, long id1, long id2) {
        HttpEntity<String> requestEntity = new HttpEntity<String>("", headers);
        Map<String, Long> variables = new HashMap<>();
        variables.put("id1", id1);
        variables.put("id2", id2);

        ResponseEntity<String> responseEntity = rest.exchange(server + uri + "/" + id1 + "/" + id2, HttpMethod.DELETE, requestEntity, String.class, variables);
        this.setStatus(responseEntity.getStatusCode());
        return responseEntity.getBody();
    }

    public HttpStatus getStatus() {
        return status;
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }
}
