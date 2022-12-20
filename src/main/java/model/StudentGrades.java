package model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class StudentGrades implements Serializable {

    private Integer userId;
    private Integer sessionId;
    private Integer grade;
}
