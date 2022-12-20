package model;

import lombok.*;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class School implements Serializable {

    private Integer schoolId;
    private String location;
    private String name;

}
