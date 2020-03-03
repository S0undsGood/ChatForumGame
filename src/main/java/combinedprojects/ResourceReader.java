package combinedprojects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

public class ResourceReader {
    private static final Logger LOGGER = LogManager.getLogger();

    public static String getContent(String requestURI) {

        /*Парсинг requestURI для получения имени файла-шаблона страницы*/
        LOGGER.info("getting fileName");
        int startIndex = requestURI.lastIndexOf("/") + 1;
        String fileName = requestURI.substring(startIndex);
        if (!fileName.contains(".html") && !fileName.contains(".css")) {
            fileName = fileName + ".html";
        }
        LOGGER.info("fileName: " + fileName);

        /*Чтение файла-шаблона*/
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader("\\opt\\Tomcat\\webapps\\combinedprojects\\WEB-INF\\classes\\templates\\" + fileName))) {
            LOGGER.info("start reading file: " + fileName);
            for (String line = ""; line != null; line = reader.readLine()) {
                content.append(line);
            }
            LOGGER.info("finish reading file: " + fileName);
        } catch (FileNotFoundException e) {
            LOGGER.error("ERROR reading file " + fileName + ": " + " File not found");
        } catch (IOException e) {
            LOGGER.error("ERROR reading file " + fileName + ": " + "IOException");
        }

        /*Передача полученной страницы темплейтеру, который заменит найденные теги
         на соответствующий контент из мапы keymap*/
        String htmlPage = Templater.changeTAG_to_HTML(content.toString(), Mapping.keymap);

        return htmlPage;
    }
}
