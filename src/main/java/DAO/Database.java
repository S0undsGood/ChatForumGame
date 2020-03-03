package DAO;

import combinedprojects.HTTPServlet;
import combinedprojects.User;
import combinedprojects.forum.Topic;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.UUID;

public class Database {

    private static final Logger LOGGER = LogManager.getLogger();

    /*Чтение кукис и поиск session_id в БД. Возвращение user_id по результату*/
    public static void checkIfAuthorized(HttpServletRequest req, User user) {
        LOGGER.info("Searching cookies");
        String cookieValue = "";
        Cookie[] cookies = req.getCookies();
        LOGGER.info("Cookies length: " + cookies.length);
        for (Cookie cookie : cookies) {
            LOGGER.debug("name = " + cookie.getName() + ", value = " + cookie.getValue());
            if (cookie.getName().equals("session_id")) {
                cookieValue = cookie.getValue();
                LOGGER.info("Cookies is found: cookieValue = " + cookieValue);
                /*Обращение к БД для поиска сессии и получении user_id*/
                checkSessionInDatabase(cookieValue, user);
                break;
            } else {
                user.setUser_id("0"); // КОСТЫЛЬ
            }
        }
    }

    private static void checkSessionInDatabase(String cookieValue, User user) {
        LOGGER.info("Start checking the session status");
        String user_id = "0"; // 0 - сессия не найдена

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            LOGGER.error("Class org.postgresql.Driver not found");
        }

        //try (Connection connection = DriverManager.getConnection("jdbc:postgresql://127.0.0.1/combinedprojects", "postgres", "admin1")) {
        try(Connection connection = HTTPServlet.getPullConnection().getConnection()) {
            LOGGER.info("Connected to the database");

            Statement stmt = connection.createStatement();
            ResultSet resultSet = stmt.executeQuery("SELECT * FROM sessions");
            while(resultSet.next()) {
                if (resultSet.getString("session_id").equals(cookieValue)) {
                    LOGGER.info("Users session " + cookieValue + " is found");
                    if (resultSet.getString("status").equals("1")) {
                        user_id = resultSet.getString("user_id");
                        LOGGER.info("Session is active, user_id = " + user_id);
                    } else {
                        LOGGER.info("Session expired, user_id = -1");
                        user_id = "-1"; // -1 означает, что сессия истекла
                    }
                }
            }
            LOGGER.info("Session status: user_id = " + user_id);
            user.setSession_id(cookieValue);
            user.setUser_id(user_id);
            stmt.close();
            resultSet.close();
            fillUserByID(connection, user, user_id);

        } catch (SQLException e) {
            LOGGER.error("SQLException during setting a connection to the database");
        }
    }

    /************************ ENTER *******************************/
    public static void enter(HttpServletRequest req, User user) {
        LOGGER.info("enter into the system, checking login and password");

        String login = req.getParameter("login");
        String password = req.getParameter("password");

        String result = "wrong login";

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try (Connection connection = HTTPServlet.getPullConnection().getConnection()) {
            LOGGER.info("successful connection to the database");

            Statement stmt = connection.createStatement();
            ResultSet resultSet = stmt.executeQuery("SELECT * FROM users;");

            boolean doesExist = false;
            while (resultSet.next()) {
                String login_existed = resultSet.getString("login");
                if (login.equals(login_existed)) {
                    String password_existed = resultSet.getString("password");
                    if (password.equals(password_existed)) {
                        LOGGER.info("user " + login + " exists in database");
                        result = "matched";
                        user.setUser_id(resultSet.getString("user_id"));
                        doesExist = true;
                    } else {
                        result = "wrong password";
                        break;
                    }
                }
            }
            // Создание сессии, если пользователь найден
            if (doesExist) {
                createSession(stmt, user, login);
            }
        } catch (SQLException e) {
            result = "Exception. No connection\n";
            LOGGER.error("SQLException in method 'enter'");
        }
        user.setEnteringResult(result);
    }

    /************************ REGISTRATION *******************************/
    public static void newUserRegistration(HttpServletRequest req, User user) {
        String result = "";

        try (Connection connection = HTTPServlet.getPullConnection().getConnection()) {
            LOGGER.info("new user registration");

            String new_email = req.getParameter("new_email");
            String first_name = req.getParameter("first_name");
            String last_name = req.getParameter("last_name");
            String new_login = req.getParameter("new_login");
            String new_password = req.getParameter("new_password");

            Statement stmt = connection.createStatement();
            ResultSet resultSet = stmt.executeQuery("SELECT * FROM users");

            boolean user_already_exist = false;

            while (resultSet.next()) {
                if (resultSet.getString("login").equals(new_login)) {
                    user_already_exist = true;
                    LOGGER.info("user with login = " + new_login + " has already registered");
                    break;
                }
            }
            if (!user_already_exist) {
                LOGGER.info("login = " + new_login + " is new. Registration into the database");
                stmt.executeUpdate("INSERT INTO users(login, password, first_name, last_name, email)" +
                        " values('" + new_login + "', '" + new_password + "', '" + first_name +
                        "', '" + last_name + "', '" + new_email + "');");
                LOGGER.debug("new user INSERTED");

                //ПРОВЕРКА
                resultSet = stmt.executeQuery("SELECT * FROM users WHERE login='" + new_login + "';");
                boolean inserted = false;
                while (resultSet.next()) {
                    if (resultSet.getString("login").equals(new_login)) {
                        if (resultSet.getString("password").equals(new_password)) {
                            result = "You have successfully registered!";
                            LOGGER.info("user " + new_login + " successfully registered");
                            String user_id = resultSet.getString("user_id");
                            user.setUser_id(user_id);
                            inserted = true;
                            break;
                        }
                    }
                }

                /* Если регистрация прошла успешно - создать сессию */
                if (inserted) {
                    createSession(stmt, user, new_login);
                } else {
                    result = "Something wrong with our database. We will check soon. Please try a bit later";
                    LOGGER.info("Something wrong with our database. We will check soon. Please try a bit later");
                }
            } else{
                result = "This username already exist. Try another one!";
                LOGGER.info("login " + new_login + " already exists.");
            }
        } catch (SQLException e) {
            result = "Connection to the database was unsuccessful. No connection. !!!Exeption!!!";
            System.out.println(e.getMessage());
            ;
        }

        user.setRegistrationResult(result);
        //return result;
    }

    private static void createSession(Statement stmt, User user, String new_login) {
        // Creating a random UUID (Universally unique identifier).
        UUID uuid = UUID.randomUUID();
        String session_id = uuid.toString();

        user.setSession_id(session_id);
        user.setLogin(new_login);

        String session_created = getCurrentDate();
        String user_id = user.getUser_id();

        // добавить в таблицу sessions новую сессию
        try {
            stmt.executeUpdate("INSERT INTO sessions(session_id, status, user_id, created) " +
                    "values('" + session_id + "', '1', '" + user_id + "', '" + session_created + "' );");
            LOGGER.info("session is created");
        } catch (SQLException e) {
            LOGGER.error("SQLException creating session");
        }
    }

    private static String getCurrentDate() {
        Date time = new Date();
        DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ssX");
        return formatter.format(time);
    }


    /******************* GETTING TOPIC LIST **************************/
    public static ArrayList<Topic> getTopicsFromDatabase(/*int decade*/) {
        LOGGER.info("getting topics from database");
        ArrayList<Topic> list = new ArrayList<>();
        String topicID;
        String user_id;
        String topic_name;
        String updated;

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try (Connection connection = HTTPServlet.getPullConnection().getConnection()) {
            LOGGER.info("successful connection to the database");
            Statement stmt = connection.createStatement();
            //TODO запрос должен возвращать 10 записей из нужной декады
            ResultSet resultSet = stmt.executeQuery("SELECT * FROM topics ORDER BY updated DESC LIMIT 10");

            while (resultSet.next()) {
                Topic topic = new Topic();
                topicID = resultSet.getString("id");
                user_id = resultSet.getString("user_id");
                topic_name = resultSet.getString("topic_name");
                updated = resultSet.getString("updated");

                topic.setTopicID(topicID);
                topic.setUser_id(user_id);
                User user = new User();
                fillUserByID(connection, user, user_id);
                topic.setUser(user);
                topic.setTopicName(topic_name);
                topic.setUpdated(updated);

                list.add(topic);
            }
            resultSet.close();
            stmt.close();

            LOGGER.info("The list of 10 topics created");

        } catch (SQLException e) {
            LOGGER.error("SqlException during getting topic list");
        }
        return list;
    }

    /******************* NEW TOPIC CREATION ***********************/
    public static void createTopic(User user, String session_id, String topic_name, String question) {
        LOGGER.info("start new topic creation");

        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try (Connection connection = HTTPServlet.getPullConnection().getConnection()) {
            fillUserBySessionID(connection, user, session_id);

            Statement stmt = connection.createStatement();
            String sql = "INSERT INTO topics(user_id, topic_name, updated)" +
                    " values('" + user.getUser_id() + "', '" + topic_name + "', '" + getCurrentDate() + "');";
            LOGGER.trace(sql);
            stmt.executeUpdate(sql);

            stmt.close();
        } catch (SQLException e) {
            LOGGER.error("SQLException during creating a new topic");
        }
    }

    public static void fillUserBySessionID(Connection connection, User user, String session_id) {
        try {
            Statement stmt = connection.createStatement();
            String sql1 = "SELECT * FROM sessions WHERE session_id='" + session_id + "';";
            LOGGER.trace(sql1);
            ResultSet resultSet = stmt.executeQuery(sql1);
            String user_id = "";

            while (resultSet.next()) {
                user_id = resultSet.getString("user_id");
            }

            fillUserByID(connection, user, user_id);

            /*String sql = "SELECT * FROM users WHERE user_id=" + user_id + ";";
            LOGGER.trace(sql);
            resultSet = stmt.executeQuery(sql);
            while (resultSet.next()) {
                user.setUser_id(resultSet.getString("user_id"));
                user.setLogin(resultSet.getString("login"));
                user.setSession_id(session_id);
                user.setEmail(resultSet.getString("email"));
                user.setFirst_name(resultSet.getString("first_name"));
                user.setLast_name(resultSet.getString("last_name"));
            }*/

            resultSet.close();
            stmt.close();

            LOGGER.info("filling user by session_id has finished");

        } catch (SQLException e) {
            LOGGER.error("SQLException during filling the user by session id");
        }
    }

    public static void fillUserByID(Connection connection, User user, String user_id) {
        try {
            Statement stmt = connection.createStatement();
            ResultSet resultSet = stmt.executeQuery("SELECT * FROM users WHERE user_id=" + user_id + ";");

            while (resultSet.next()) {
                user.setUser_id(user_id);
                user.setLogin(resultSet.getString("login"));
                user.setEmail(resultSet.getString("email"));
                user.setFirst_name(resultSet.getString("first_name"));
                user.setLast_name(resultSet.getString("last_name"));
            }
            LOGGER.info("user user_id = " + user_id + " are filled");

            stmt.close();
            resultSet.close();
        } catch (SQLException e) {
            LOGGER.error("SQLException during filling user parameters");
        }
    }
}
