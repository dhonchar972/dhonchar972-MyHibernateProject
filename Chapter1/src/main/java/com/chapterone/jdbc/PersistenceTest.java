package com.chapterone.jdbc;

import com.chapterone.pojo.MessageEntity;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;

public class PersistenceTest {
    Connection getConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://localhost:3306/testHibernateBase", "root", "1111");
    }
    @BeforeClass
    public void setup() {
        final String DROP = "DROP TABLE IF EXISTS messages;";
        final String CREATE = "create table messages(" +
                "id bigint auto_increment," +
                "text varchar(256) not null," +
                "constraint messages_pk" +
                " primary key (id));";
        try (Connection connection = getConnection()) {
// clear out the old data, if any, so we know the state of the DB
            try (PreparedStatement ps =
                         connection.prepareStatement(DROP)) {
                ps.execute();
            }
// create the table...
            try (PreparedStatement ps =
                         connection.prepareStatement(CREATE)) {
                ps.execute();
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
    public MessageEntity saveMessage(String text) {
        final String INSERT = "INSERT INTO messages(text) VALUES (?)";
        MessageEntity message = null;
        try (Connection connection = getConnection()) {
            try (PreparedStatement ps =
                         connection.prepareStatement(INSERT,
                                 Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, text);
                ps.execute();
                try (ResultSet keys = ps.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("No generated keys");
                    }
                    message = new MessageEntity(keys.getLong(1), text);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return message;
    }
    @Test
    public void readMessage() {
        final String text = "Hello, World!";
        MessageEntity message = saveMessage(text);
        final String SELECT = "SELECT id, text FROM messages";
        List<MessageEntity> list = new ArrayList<>();
        try (Connection connection = getConnection()) {
            try (PreparedStatement ps =
                         connection.prepareStatement(SELECT)) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        MessageEntity newMessage = new MessageEntity();
                        newMessage.setId(rs.getLong(1));
                        newMessage.setText(rs.getString(2));
                        list.add(message);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        assertEquals(list.size(), 1);
        for (MessageEntity m : list) {
            System.out.printf("Id: %s, Text: %s", m.getId().toString(), m.getText());
        }
        assertEquals(list.get(0), message);
    }
}
