package model;

import lombok.*;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class Department implements Serializable {

    private Integer departmentId;
    private String name;

    private Integer userId;

}
