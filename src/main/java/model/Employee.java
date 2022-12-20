package model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class Employee implements Serializable {

    private Integer userId;
    private String phoneNumber;

    private String title;

    private String role;
}
