package org.acme.quarkus.scala.three.it;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.is;

import org.junit.jupiter.api.Test;

import io.quarkus.test.junit.QuarkusTest;

@QuarkusTest
public class QuarkusScalaThreeResourceTest {

    @Test
    public void testHelloEndpoint() {
        given()
                .when().get("/quarkus-scala-three")
                .then()
                .statusCode(200)
                .body(is("Hello quarkus-scala-three"));
    }
}
