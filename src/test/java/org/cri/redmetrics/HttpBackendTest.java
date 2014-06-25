package org.cri.redmetrics;


import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpContent;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.http.json.JsonHttpContent;
import com.google.api.client.json.GenericJson;
import com.google.api.client.json.JsonObjectParser;
import com.google.api.client.json.gson.GsonFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import java.io.IOException;

public abstract class HttpBackendTest<E extends TestEntity> {

    Server server = new Server();
    HttpRequestFactory requestFactory = new NetHttpTransport().createRequestFactory((request) -> request.setParser(new JsonObjectParser(new GsonFactory())));

    String path;
    Class<E> type;

    TestGame createdGame;

    HttpBackendTest(String path, Class<E> type) {
        this.path = path;
        this.type = type;
    }

    @BeforeClass
    public void setUp() throws IOException {
        server.start();
        init();
    }

    abstract void init() throws IOException;

    @AfterClass
    public void tearDown() {
        server.clearAllRoutes();
    }

    E get(int id) throws IOException {
        return get(path + id);
    }

    E post(TestGame game) throws IOException {
        return post(path, game);
    }

    E put(TestGame game) throws IOException {
        return put(path + game.getId(), game);
    }

    E delete(int id) throws IOException {
        return delete(path + id);
    }

    E get(String path) throws IOException {
        return requestFactory.buildGetRequest(url(path)).execute().parseAs(type);
    }

    E post(String path, GenericJson json) throws IOException {
        HttpContent content = new JsonHttpContent(new GsonFactory(), json);
        return requestFactory.buildPostRequest(url(path), content).execute().parseAs(type);
    }

    E put(String path, GenericJson json) throws IOException {
        HttpContent content = new JsonHttpContent(new GsonFactory(), json);
        return requestFactory.buildPutRequest(url(path), content).execute().parseAs(type);
    }

    E delete(String path) throws IOException {
        return requestFactory.buildDeleteRequest(url(path)).execute().parseAs(type);
    }

    int randomId() {
        return (int) Math.round(Math.random() * 1000000000) + 1000000000;
    }

    GenericUrl url(String path) {
        return new GenericUrl("http://localhost:4567/" + path);
    }

}
