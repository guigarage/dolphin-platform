package com.canoo.dp.impl.platform.client.http;

import com.canoo.dp.impl.platform.core.http.HttpHeaderConstants;
import com.canoo.platform.client.PlatformClient;
import com.canoo.platform.core.http.ByteArrayProvider;
import com.canoo.platform.core.http.HttpClient;
import com.canoo.platform.core.http.HttpResponse;
import com.google.gson.Gson;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import spark.Spark;

import java.net.ServerSocket;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;

public class HttpClientTests {

    private final int freePort;

    public HttpClientTests() {
        try (ServerSocket socket = new ServerSocket(0)) {
            socket.setReuseAddress(true);
            freePort = socket.getLocalPort();
        } catch (Exception e) {
            throw new RuntimeException();
        }
    }

    @BeforeClass
    public void startSpark() {
        Spark.port(freePort);
        Spark.get("/", (req, res) -> "Spark Server for HTTP client integration tests");
        Spark.get("/", HttpHeaderConstants.JSON_MIME_TYPE,  (req, res) -> {

            final Gson gson = new Gson();
            final DummyJson dummy = new DummyJson("Joe", 33, true);
            final String json = gson.toJson(dummy);
            res.type(HttpHeaderConstants.JSON_MIME_TYPE);

            return json;
        });
        Spark.awaitInitialization();
    }

    @AfterClass
    public void destroySpark() {
        Spark.stop();
    }

    @Test
    public void testSimpleGet() throws Exception {
        //given:
        final HttpClient client = PlatformClient.getService(HttpClient.class);

        //when:
        CompletableFuture<HttpResponse<Void>> future = client.get("http://localhost:" + freePort).
                withoutContent().
                withoutResult().
                execute();

        //then:
        future.get(1_000, TimeUnit.MILLISECONDS);
    }

    @Test
    public void testSimpleGetWithStringContent() throws Exception {
        //given:
        final HttpClient client = PlatformClient.getService(HttpClient.class);

        //when:
        CompletableFuture<HttpResponse<String>> future = client.get("http://localhost:" + freePort)
                .withoutContent()
                .readString()
                .execute();

        //then:
        final String content = future.get(1_000, TimeUnit.MILLISECONDS).getContent();
        assertThat("String content does not match", content, is("Spark Server for HTTP client integration tests"));
    }

    @Test
    public void testSimpleGetWithByteContent() throws Exception {
        //given:
        final HttpClient client = PlatformClient.getService(HttpClient.class);

        //when:
        CompletableFuture<HttpResponse<ByteArrayProvider>> future = client.get("http://localhost:" + freePort)
                .withoutContent()
                .readBytes()
                .execute();

        //then:
        final byte[] bytes = future.get(1_000, TimeUnit.MILLISECONDS).getContent().get();
        assertThat("Byte content does not match", bytes, is("Spark Server for HTTP client integration tests".getBytes()));
    }

    @Test
    public void testSimpleGetWithJsonContentType() throws Exception {
        //given:
        final HttpClient client = PlatformClient.getService(HttpClient.class);

        //when:
        CompletableFuture<HttpResponse<String>> future = client.get("http://localhost:" + freePort)
                .withoutContent()
                .readString(HttpHeaderConstants.JSON_MIME_TYPE)
                .execute();

        //then:
        final String json = future.get(1_000, TimeUnit.MILLISECONDS).getContent();
        final Gson gson = new Gson();
        final DummyJson dummy = gson.fromJson(json, DummyJson.class);
        assertThat("No JSON object created", dummy, notNullValue());
        assertThat("Wrong name", dummy.getName(), is("Joe"));
        assertThat("Wrong age", dummy.getAge(), is(33));
        assertThat("Wrong isJavaChampion", dummy.isJavaChampion(), is(true));
    }

    @Test(enabled = false)
    public void testGetWithContent() throws Exception {
        //given:
        final HttpClient client = PlatformClient.getService(HttpClient.class);

        //when:
        CompletableFuture<HttpResponse<String>> future = client.get("http://localhost:" + freePort)
                .withContent("String")
                .readString()
                .execute();

        //then:
        final String content = future.get(1_000, TimeUnit.MILLISECONDS).getContent();
    }

    /**
     * Dummy class for JSON serialization and deserialization.
     */
    public class DummyJson {
        String name;
        int age;
        boolean isJavaChampion;

        public DummyJson(final String name, final int age, final boolean isJavaChampion) {
            this.name = name;
            this.age = age;
            this.isJavaChampion = isJavaChampion;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }

        public boolean isJavaChampion() {
            return isJavaChampion;
        }
    }

}
