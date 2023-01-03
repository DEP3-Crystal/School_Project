package model.emails;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class TopTeacher implements Serializable {
    private String firstName;
    private String lastName;
    private Double rating;
}
