package com.geekbrains.cloud_storage.Service;

import com.geekbrains.cloud_storage.Server;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;
import java.util.UUID;

public class AccountSignService {
    public boolean isLoginExists(String login) {
        boolean result = false;

        try {
            PreparedStatement statement = Server.getSqlHandler().getConnection().prepareStatement("SELECT count(*) FROM user WHERE login = ?");
            statement.setString(1, login);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                result = resultSet.getInt(1) > 0;
            }

            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    public boolean isEmailExists(String email) {
        boolean result = false;

        try {
            PreparedStatement statement = Server.getSqlHandler().getConnection().prepareStatement("SELECT count(*) FROM user WHERE email = ?");
            statement.setString(1, email);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                result = resultSet.getInt(1) > 0;
            }

            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    /*
    * TODO не хранить пароли в открытом виде
    * */
    public void create(String login, String email, String password) {
        try {
            PreparedStatement statement = Server.getSqlHandler().getConnection().prepareStatement("INSERT INTO user (login, email, password) VALUES (?, ?, ?);");
            statement.setString(1, login);
            statement.setString(2, email);
            statement.setString(3, password);

            statement.execute();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean isCredentialsCorrect(String login, String password) {
        boolean result = false;

        try {
            PreparedStatement statement = Server.getSqlHandler().getConnection().prepareStatement("SELECT count(*) FROM user WHERE login = ? AND password = ?");
            statement.setString(1, login);
            statement.setString(2, password);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                result = resultSet.getInt(1) > 0;
            }

            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    /*
    * FIXME несовершенство этого метода заключается в том что мы не знаем когда можно удалить старый ключ,
    * FIXME пересмотреть логику с использованием refresh key или как то иначе
    * */
    public String generateKeyByCredentials(String login) throws Exception {
        try {
            int id;

            {
                PreparedStatement statement = Server.getSqlHandler().getConnection().prepareStatement("SELECT id FROM user WHERE login = ?");
                statement.setString(1, login);

                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    id = resultSet.getInt(1);
                } else {
                    throw new Exception("user not found");
                }

                resultSet.close();
            }

            String key;

            {
                key = UUID.randomUUID().toString();

                PreparedStatement statement = Server.getSqlHandler().getConnection().prepareStatement("INSERT INTO user_key (user_id, key) VALUES (?, ?)");
                statement.setInt(1, id);
                statement.setString(2, key);

                statement.execute();
            }

            return key;
        } catch (SQLException e) {
            e.printStackTrace();

            return null; // FIXME возможно стоит пересмотреть логику конкретно этой строки
        }
    }

    public boolean isKeyCorrect(String key) {
        boolean result = false;

        try {
            PreparedStatement statement = Server.getSqlHandler().getConnection().prepareStatement("SELECT * FROM user JOIN user_key ON user.id = user_key.user_id WHERE user_key.key = ?");
            statement.setString(1, key);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                result = resultSet.getInt(1) > 0;
            }

            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }
}
