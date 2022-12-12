package com.crystal.school.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Objects;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class School implements Serializable {

    private Integer schoolId;
    private String location;
    private String name;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        School school = (School) o;
        return schoolId.equals(school.schoolId) && location.equals(school.location) && name.equals(school.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(schoolId, location, name);
    }
}
