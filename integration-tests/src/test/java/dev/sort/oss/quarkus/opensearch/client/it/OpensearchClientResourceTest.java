package dev.sort.oss.quarkus.opensearch.client.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class OpensearchClientResourceTest {

    @Test
    public void testHelloEndpoint() {
        given()
                .when().get("/opensearch-client")
                .then()
                .statusCode(200)
                .body(is("Hello opensearch-client"));
    }
}
