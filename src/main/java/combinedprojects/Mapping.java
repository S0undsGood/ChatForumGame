package combinedprojects;

import DAO.Database;
import combinedprojects.forum.TopicList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;


public class Mapping {
    private static final Logger LOGGER = LogManager.getLogger();

    public static HashMap<String, String> keymap = new HashMap<>();
    static{
        LOGGER.info("keymap initialization");
    }

    public static String methodGET(HttpServletRequest request, HttpServletResponse response) {

        String requestURI = request.getRequestURI();
        String html = "";
        User user = new User();

        //Маршрутизация кода в зависимости от полученного запроса
        if (requestURI.contains(".css")) {
            response.setContentType("text/css");
            html = ResourceReader.getContent(requestURI);
        } else if (requestURI.contains("registration") || requestURI.contains("authorization")) {
            html = ResourceReader.getContent(requestURI);
        } else {
            /* Отправка запроса в класс Database, там чтение кукис и поиск session_id в БД. Запись данных пользователя в User.
            *  Если user_id = 0 - сессия не найдена;
                    user_id = -1 - срок сессии истек
                    user_id = [value] - для этого юзера сессия активна*/
            Database.checkIfAuthorized(request, user);

            /* Действия в зависимости от значения user_id */
            if (user.getUser_id().equals("-1") || user.getUser_id().equals("0")) {
                String path = request.getContextPath() + "/authorization";
                LOGGER.info("user not authorized, redirecting to " + path);
                try {
                    response.sendRedirect(path);
                } catch (IOException e) {
                    LOGGER.error("IOException when redirecting to " + path);
                }
            } else {
                LOGGER.info("going to " + requestURI);
                keymap.put("login", user.getLogin());
                if (requestURI.contains("forum")) {
                    keymap.put("topic_list", new TopicList().toString());
                }
                html = ResourceReader.getContent(requestURI);
            }
        }
        return html;
    }

    public static String methodPOST(HttpServletRequest request, HttpServletResponse response) {

        String requestURI = request.getRequestURI();
        String html = "";
        User user = new User();

        //Маршрутизация кода в зависимости от полученного запроса
        if (requestURI.contains("registration")) {
            Database.newUserRegistration(request, user);
            if (user.getRegistrationResult().contains("exist")) {
                keymap.put("registrationResult", user.getRegistrationResult());
                html = ResourceReader.getContent(requestURI);
            } else if (user.getRegistrationResult().contains("registered")) {
                createCookie(response, user);
                redirectTo(request,response, "/mainpage");
            }
        } else if (requestURI.contains("authorization")) {
            Database.enter(request, user);
            if (user.getEnteringResult().contains("matched")) {
                createCookie(response, user);
                redirectTo(request,response, "/mainpage");
            } else {
                keymap.put("authorizationResult", user.getEnteringResult());
                html = ResourceReader.getContent(requestURI);
            }
        } else if (requestURI.contains("create_topic")) {
            String topic_name = request.getParameter("topic_name");
            String question = request.getParameter("question");
            Cookie[] cookies = request.getCookies();
            String session_id = "";
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("session_id")) {
                    session_id = cookie.getValue();
                }
            }
            Database.createTopic(user, session_id, topic_name, question);
            redirectTo(request, response, "/forum");
        }

        createCookie(response, user);

        return html;
    }

    private static void redirectTo(HttpServletRequest request, HttpServletResponse response, String to) {
        String path = request.getContextPath() + to;
        LOGGER.info("redirecting to " + path);
        try {
            response.sendRedirect(path);
        } catch (IOException e) {
            LOGGER.error("IOException when redirecting to " + path);
        }
    }

    private static void createCookie(HttpServletResponse response, User user) {
        Cookie cookie = new Cookie("session_id", user.getSession_id());
        response.addCookie(cookie);
    }
}
