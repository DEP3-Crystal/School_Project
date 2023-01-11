package utils;

import model.emails.TopTeacher;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TopTeachersList {

    public static List<TopTeacher> getTopTeachersList(String driver, String hostName, String username, String password) throws ClassNotFoundException, SQLException {
        Class.forName(driver);
        Connection connection = DriverManager.getConnection(hostName, username, password);
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery("SELECT u.first_name, u.last_name, " +
                "ROUND(t.rating_sum::decimal / t.rating_count, 2) " +
                "AS rating FROM users u INNER JOIN teacher t ON u.user_id = t.user_id " +
                "ORDER BY rating DESC LIMIT 6");
        List<TopTeacher> teachers = new ArrayList<>();
        while (resultSet.next()) {
            TopTeacher teacher = new TopTeacher(resultSet.getString("first_name"),
                    resultSet.getString("last_name"), resultSet.getDouble("rating"));
            teachers.add(teacher);
        }
        return teachers;
    }
}
