package model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class Teacher implements Serializable {

    private Integer userId;
    private String credentials;
    private Integer ratingSum;
    private Integer ratingCount;
}
