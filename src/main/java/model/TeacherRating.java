package model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

@Data
@AllArgsConstructor
public class TeacherRating implements Serializable {

    private Integer userId;
    private Integer teacherId;
    private Integer rating;
}
