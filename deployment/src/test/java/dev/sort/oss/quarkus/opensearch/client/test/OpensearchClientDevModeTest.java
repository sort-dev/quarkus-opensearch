package dev.sort.oss.quarkus.opensearch.client.test;

import io.quarkus.test.QuarkusDevModeTest;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.hamcrest.Matchers.equalTo;

public class OpensearchClientDevModeTest {
    @RegisterExtension
    static QuarkusDevModeTest test = new QuarkusDevModeTest()
            .withApplicationRoot((jar) -> jar
                    .addClass(TestResource.class));

    @Test
    public void testDatasource() throws Exception {
        var fruit = new TestResource.Fruit();
        fruit.id = "1";
        fruit.name = "banana";
        fruit.color = "yellow";

        RestAssured
                .given().body(fruit).contentType("application/json")
                .when().post("/fruits")
                .then().statusCode(204);

        RestAssured.when().get("/fruits/search?term=color&match=yellow")
                .then()
                .statusCode(200)
                .body(equalTo("[{\"id\":\"1\",\"name\":\"banana\",\"color\":\"yellow\"}]"));

        RestAssured.when().get("/fruits/search_async?term=color&match=yellow")
                .then()
                .statusCode(200)
                .body(equalTo("[{\"id\":\"1\",\"name\":\"banana\",\"color\":\"yellow\"}]"));
    }
}
