package com.geekbrains.cs.server.Services;

import com.geekbrains.cs.server.Contracts.EmptyResultException;
import com.geekbrains.cs.server.Server;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class AccountSignService {
    public static boolean isLoginExists(String login) {
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

    public static boolean isEmailExists(String email) {
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
    public static void create(String login, String email, String password) {
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

    public static boolean isCredentialsCorrect(String login, String password) {
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
    * FIXME Несовершенство этого метода заключается в том что мы не знаем когда можно удалить старый ключ,
    * FIXME возможно стоит пересмотреть логику с использованием refresh key или как то иначе
    * */
    public static String generateKeyByCredentials(String login) throws EmptyResultException {
        try {
            int id;

            {
                PreparedStatement statement = Server.getSqlHandler().getConnection().prepareStatement("SELECT id FROM user WHERE login = ?");
                statement.setString(1, login);

                ResultSet resultSet = statement.executeQuery();

                if (resultSet.next()) {
                    id = resultSet.getInt(1);
                } else {
                    throw new EmptyResultException("User not found");
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

            return null;
        }
    }

    public static boolean isKeyCorrect(String key) {
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

    public static String getLoginByKey(String key) throws EmptyResultException {
        String login = "";

        try {
            PreparedStatement statement = Server.getSqlHandler().getConnection().prepareStatement("SELECT login FROM user JOIN user_key ON user_key.user_id = user.id WHERE user_key.key = ?");
            statement.setString(1, key);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                login = resultSet.getString(1);
            } else {
                throw new EmptyResultException("Key not found");
            }

            resultSet.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return login;
    }
}
