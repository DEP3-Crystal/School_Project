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
public class Department implements Serializable {

    private Integer departmentId;
    private String name;

    private Integer userId;


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Department that = (Department) o;
        return departmentId.equals(that.departmentId) && name.equals(that.name) && userId.equals(that.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(departmentId, name, userId);
    }
}
