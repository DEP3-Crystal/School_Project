package model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class User implements Serializable {

    private Integer userId;
    private String firstName;
    private String lastName;
    private String email;
    private String gender;
    private String biography;
    private String password;
    private Integer departmentId;
    private boolean isEmployee;
}
