package combinedprojects;

import DAO.Database;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.ServletContext;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/")
public class HTTPServlet extends HttpServlet {
    private static final Logger LOGGER = LogManager.getLogger(HTTPServlet.class);

    // позволяет получить коннект из пулла
    private static BasicDataSource pullConnection;

    //Создание пулл коннекшен
    public static BasicDataSource getPullConnection() {
        if (pullConnection == null) {
            LOGGER.info("pullConnection == null. Creating new connection");

            pullConnection = new BasicDataSource();
            pullConnection.setDriverClassName("org.postgresql.Driver");
            pullConnection.setUrl("jdbc:postgresql://127.0.0.1/combinedprojects");
            pullConnection.setUsername("postgres");
            pullConnection.setPassword("admin1");
        }
        return pullConnection;
    }

    public void doGet(HttpServletRequest req, HttpServletResponse resp) {
        try {
            String requestURI = req.getRequestURI();
            LOGGER.info("GET: " + requestURI);
            PrintWriter out = resp.getWriter();
            resp.setContentType("text/html");

            //Передача запроса маршрутизатору, который собирает и возвращает страницу HTML
            String html = Mapping.methodGET(req, resp);

            out.println(html);

        } catch (IOException e) {
            LOGGER.error("Servlet crashed with an exception: " + e.getMessage());
        }
    }

    public void doPost(HttpServletRequest req, HttpServletResponse resp) {
        try {
            String requestURI = req.getRequestURI();
            LOGGER.info("POST: " + requestURI);
            PrintWriter out = resp.getWriter();
            resp.setContentType("text/html");

            String html = Mapping.methodPOST(req, resp);

            out.println(html);

        } catch (IOException e) {
            LOGGER.error("Servlet crashed with an exception: " + e.getMessage());
        }
    }
}
