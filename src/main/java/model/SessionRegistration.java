package model;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.io.Serializable;
import java.sql.Timestamp;
@Data
@AllArgsConstructor
public class SessionRegistration implements Serializable {

    private Integer roomId;
    private Integer sessionId;
    private Timestamp startTime;
    private Timestamp endTime;
}
