package Lesson_04;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)

@Data
public class UserRegistrationDTO {

    private String name;

    private String job;

    private String email;

    private String password;
}