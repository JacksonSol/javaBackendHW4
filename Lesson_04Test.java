package Lesson_04;

import io.qameta.allure.*;
import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.*;
import org.omg.CORBA.Request;
import org.openqa.selenium.devtools.v85.fetch.model.AuthChallengeResponse;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static io.restassured.RestAssured.given;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

public class Lesson_04Test {

    //static Map<String, String> headers = new HashMap<>();   Для Авторизации
    static Properties properties = new Properties();
    static RequestSpecification requestSpecification_JSON;
    static ResponseSpecification responseSpecification_Code200_JSON_5sec;
    static ResponseSpecification responseSpecification_Code404_5sec;
    static ResponseSpecification responseSpecification_Code400_5sec;

    @BeforeAll
    static void setUp() throws IOException {
        RestAssured.filters(new AllureRestAssured());
        //headers.put("Autorization", "Bearer 993rfdscvo439"); //для Авторизации
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails(); //Логирование и вывод только ошибок
        //RestAssured.responseSpecification = responseSpec; //Если спецификация едина для всех ответов, указываем её в виде глобальной переменной

        //Чтение файла my.properties
        FileInputStream fileInputStream = new FileInputStream("src/test/resources/my.properties");
        properties.load(fileInputStream);

        requestSpecification_JSON = new RequestSpecBuilder()
                //.setAccept(ContentType.JSON)
                .setContentType(ContentType.JSON)
                .build();

        responseSpecification_Code200_JSON_5sec = new ResponseSpecBuilder()
                .expectStatusCode(200)
                .expectContentType(ContentType.JSON)
                .expectResponseTime(Matchers.lessThan(5000L))
                .build();

        responseSpecification_Code404_5sec = new ResponseSpecBuilder()
                .expectStatusCode(404)
                .expectResponseTime(Matchers.lessThan(5000L))
                .build();

        responseSpecification_Code400_5sec = new ResponseSpecBuilder()
                .expectStatusCode(400)
                .expectResponseTime(Matchers.lessThan(5000L))
                .build();
    }

   /* @AfterEach
    void tearDown() {
        headers.clear();
    }
    */

    //Перечень пользователей
    @Test
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Перечень пользователей: Все пользователи")
    @Feature(value = "Перечень пользователей")
    void getAllUsers() {
        given(requestSpecification_JSON)
                .when()
                .get((String) properties.get("URL"))
                .then()
                .statusCode(200)
                .and().body("data", notNullValue());
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Перечень пользователей: Один пользователь")
    @Feature(value = "Перечень пользователей")
    void getSingleUser() {
        ResponseDTO responseDTO =
                given(requestSpecification_JSON)
                        .when()
                        .get((String) properties.get("URL") + "/api/users/" + (String) properties.get("userid"))
                        .then()
                        .spec(responseSpecification_Code200_JSON_5sec)
                        .and()
                        .extract()
                        .body()
                        .as(ResponseDTO.class);
        assertThat(responseDTO.getData().getId(), equalTo((String) properties.get("janetID")));
        assertThat(responseDTO.getData().getFirstName(), containsString((String) properties.get("janetFirst_name")));

    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Перечень пользователей: Не найден пользователь")
    @Feature(value = "Перечень пользователей")
    void getUserNotFound() {
        given(requestSpecification_JSON)
                .when()
                .get((String) properties.get("URL") + "/api/users/" + (String) properties.get("useridNOTfound"))
                .then()
                .spec(responseSpecification_Code404_5sec);
    }

//Регистрация нового пользователя
    @Test
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("Создание нового пользователя")
    @Story(value = "Регистрация")
    void createNewUser() {
        UserRegistrationDTO userRegistrationDTO = new UserRegistrationDTO();
        userRegistrationDTO.setName("Evgen");
        userRegistrationDTO.setJob("GB_Student");

        given(requestSpecification_JSON)
                .body(userRegistrationDTO)
                .when()
                .post((String) properties.get("URL") + "/api/users/")
                .then()
                .statusCode(201)
                //Вариант 1 через body
                .and()
                .body("name", equalTo("Evgen"))
                .and()
                .body("job", equalTo("GB_Student"))
                //Вариант 2 через extract
                .and()
                .extract()
                .response()
                .jsonPath()
                .getString("name")
                .compareTo("Evgen");
    }

//Войти
    @Test
    @Severity(SeverityLevel.BLOCKER)
    @DisplayName("Войти: Регистрация (валидная)")
    @Story(value = "Вход")
    void registrationValid() {

        UserRegistrationDTO userRegistrationDTO = new UserRegistrationDTO();
        userRegistrationDTO.setEmail("eve.holt@reqres.in");
        userRegistrationDTO.setPassword("pistol");

        given(requestSpecification_JSON)
                .body(userRegistrationDTO)
                .when()
                .post((String) properties.get("URL") + "/api/register")
                .then()
                .spec(responseSpecification_Code200_JSON_5sec)
                //Вариант 1 через body
                .and()
                .body("id", equalTo(4))
                .and()
                .body("token", notNullValue())
                //Вариант 2 через extract
                .and()
                .extract()
                .response()
                .jsonPath()
                .getString("")
                .compareTo((String) properties.get("registrationValid"));
    }

    @Test
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Войти: Регистрация (не валидная)")
    @Story(value = "Вход")
    void registrationNotValid() {

        UserRegistrationDTO userRegistrationDTO = new UserRegistrationDTO();
        userRegistrationDTO.setEmail("sydney@fife");

        given(requestSpecification_JSON)
                .body(userRegistrationDTO)
                .when()
                .post((String) properties.get("URL") + "/api/register")
                .then()
                .spec(responseSpecification_Code400_5sec)
                .and()
                .body("error", equalTo("Missing password"));
    }

    @Test
    @Severity(SeverityLevel.CRITICAL)
    @DisplayName("Войти: Вход (валидный)")
    @Story(value = "Вход")
    void LoginValid() {

        UserRegistrationDTO userRegistrationDTO = new UserRegistrationDTO();
        userRegistrationDTO.setEmail("eve.holt@reqres.in");
        userRegistrationDTO.setPassword("cityslicka");

        given(requestSpecification_JSON)
                .body(userRegistrationDTO)
                .when()
                .post((String) properties.get("URL") + "/api/login")
                .then()
                .spec(responseSpecification_Code200_JSON_5sec)
                .and()
                .body("token", notNullValue());
    }

    @Test
    @Severity(SeverityLevel.MINOR)
    @DisplayName("Войти: Вход (не валидный)")
    @Story(value = "Вход")
    void LoginNotValid() {

        UserRegistrationDTO userRegistrationDTO = new UserRegistrationDTO();
        userRegistrationDTO.setEmail("peter@klaven");

        given(requestSpecification_JSON)
                .body(userRegistrationDTO)
                .when()
                .post((String) properties.get("URL") + "/api/login")
                .then()
                .spec(responseSpecification_Code400_5sec)
                .and()
                .body("error", equalTo("Missing password"));
    }

//Изменить данные пользователя
    @Test
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Изменить данные PUT")
    @Epic(value = "Изменить данные")
    void changeUserDataPUT() {

        UserRegistrationDTO userRegistrationDTO = new UserRegistrationDTO();
        userRegistrationDTO.setName("Evgen");
        userRegistrationDTO.setJob("JUST_Student");

        given(requestSpecification_JSON)
                .body(userRegistrationDTO)
                .when()
                .put((String) properties.get("URL") + "/api/users/" + (String) properties.get("userid"))
                .then()
                .spec(responseSpecification_Code200_JSON_5sec)
                .and()
                .body("job", equalTo("JUST_Student"));
    }

    @Test
    @Severity(SeverityLevel.NORMAL)
    @DisplayName("Изменить данные PATCH")
    @Epic(value = "Изменить данные")
    void changeUserDataPATCH() {

        UserRegistrationDTO userRegistrationDTO = new UserRegistrationDTO();
        userRegistrationDTO.setName("Evgen");
        userRegistrationDTO.setJob("VIP_Student");

        given(requestSpecification_JSON)
                .body(userRegistrationDTO)
                .when()
                .patch((String) properties.get("URL") + "/api/users/" + (String) properties.get("userid"))
                .then()
                .spec(responseSpecification_Code200_JSON_5sec)
                .and()
                .body("job", equalTo("VIP_Student"));
    }

//Удаление пользователя
    @Test
    @Severity(SeverityLevel.CRITICAL)
    @Story(value = "Удаление")
    void deleteUser() {
        given()
                .when()
                .delete((String) properties.get("URL") + "/api/users/" + (String) properties.get("userid"))
                .then()
                .statusCode(204);
    }

}