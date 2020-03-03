package combinedprojects.forum;

import DAO.Database;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;

public class TopicList {
    private static final Logger log = LogManager.getLogger();

    private ArrayList<Topic> list;

    public TopicList() {
        log.info("start creating topic list");
        this.list = Database.getTopicsFromDatabase();
    }

    public String toString() {
        StringBuilder topicsHTML = new StringBuilder();

        /*Преобразование содержимого списка тем в HTML.
        Далее Templater по тегу найдет эту строку в keymap и вставит в HTML*/
        for (Topic topic : list) {
            topicsHTML.append("<tr>");
            topicsHTML.append("<th><a href='http://localhost:9713/combinedprojects/forum/"
                    + topic.getTopicID() + "'>" + topic.getTopicName() + "</a></th>");
            topicsHTML.append("<th>" + topic.getUser().getLogin() + "</th>");
            topicsHTML.append("<th>" + topic.getUpdated() + "</th>");
            topicsHTML.append("</tr>");
        }
        return topicsHTML.toString();
    }
}
