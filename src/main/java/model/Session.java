package model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Session implements Serializable {

    private Integer sessionId;
    private Integer departmentId;
    private String title;
    private String description;
    private String type;
    private String difficultyLevel;
    private String keywords;
    private Timestamp startTime;
    private Timestamp endTime;
    private Integer ratingSum;
    private Integer ratingCount;


}
