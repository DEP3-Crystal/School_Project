package model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;

@Data
@AllArgsConstructor
public class StudentRegistration implements Serializable {

    private Integer userId;
    private Integer roomId;
    private Timestamp datetime;
}
