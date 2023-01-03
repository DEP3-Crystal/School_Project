package model.emails;

import lombok.Data;

import java.io.Serializable;

@Data
public class Subscriber implements Serializable {
    private String firstName;
    private String lastName;
    private String email;
}
