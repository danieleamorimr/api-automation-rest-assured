package User;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.lessThan;

import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import com.github.javafaker.Faker;

import Entities.User;
import io.restassured.RestAssured;
import io.restassured.config.LogConfig;
import io.restassured.http.ContentType;
import io.restassured.module.jsv.JsonSchemaValidator;
import io.restassured.specification.RequestSpecification;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class UserTests {

    private static User user;
    public static Faker faker;
    public static RequestSpecification request;

    @BeforeAll
    public static void setup() {
        RestAssured.baseURI = "https://petstore.swagger.io/v2";

        faker = new Faker();

        user = new User(faker.name().username(),
                faker.name().firstName(),
                faker.name().lastName(),
                faker.internet().safeEmailAddress(),
                faker.internet().password(8, 10),
                faker.phoneNumber().toString());
    }

    @BeforeEach
    void setRequest() {
        request = RestAssured.given()
        .config(RestAssured.config().logConfig(LogConfig.logConfig().enableLoggingOfRequestAndResponseIfValidationFails()))
                .header("api-key", "special-key")
                .contentType(ContentType.JSON);

    }

    @Test
    @Order(1)
    public void CreateNewUser_WithValidData_ReturnOk() {

        request
                .body(user)
                .when()
                .post("/user")
                .then()
                .assertThat().statusCode(200).and()
                .body("code", equalTo(200))
                .body("type", equalTo("unknown"))
                .body("message", isA(String.class))
                .body("size()", equalTo(3));
    }

    @Test
    @Order(2)
    public void GetLogin_ValidUser_ReturnOk() {

        request
                .param("username", user.getUsername())
                .param("password", user.getPassword())
                .when()
                .get("/user/login")
                .then()
                .assertThat()
                .statusCode(200)
                .and().time(lessThan(2000L))
                .and().body(JsonSchemaValidator.matchesJsonSchemaInClasspath("loginResponseSchema.json"));
    }

    @Test
    @Order(3)
    public void GetUserByUsername_userIsValid_ReturnOk() {
        request
                .when()
                .get("/user/" + user.getUsername())
                .then()
                .assertThat().statusCode(HttpStatus.SC_OK).and()
                .time(lessThan(2000L))
                .and().body("firstName", equalTo(user.getFirstName()));

    }
   @Test
   @Order(4)
    public void DeleteUser_UserExists_ReturnOk() {

        request
                .when()
                .delete("/user/" + user.getUsername())
                .then()
                .assertThat().statusCode(HttpStatus.SC_OK).and()
                .time(lessThan(2000L))
                .log();

    }

}
