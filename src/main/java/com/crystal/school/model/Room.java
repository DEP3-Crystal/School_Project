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
public class Room implements Serializable {

   private Integer roomId;
   private Integer schoolId;
   private Integer floor;
   private Integer doorNumber;

   private String type;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Room room = (Room) o;
        return roomId.equals(room.roomId) && schoolId.equals(room.schoolId) && floor.equals(room.floor) && doorNumber.equals(room.doorNumber) && type.equals(room.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(roomId, schoolId, floor, doorNumber, type);
    }
}
