package Lesson_04;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class ResponseDTO extends CommonResponse <ResponseDTO.ResponseDTOdata> {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Data
    public class ResponseDTOdata {

        private String id;
        private String email;

        @JsonProperty("first_name")
        private String firstName;

        @JsonProperty("last_name")
        private String lastName;
        private String avatar;
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Data
    public class Support {
        @JsonIgnore
        private String url;
        @JsonIgnore
        private String text;
    }
}