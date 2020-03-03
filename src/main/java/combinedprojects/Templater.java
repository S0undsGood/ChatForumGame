package combinedprojects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;

/**
 * If String contains ${expression} tag, it replaces to HashMap's value
 */
public class Templater {

    private static final Logger log = LogManager.getLogger();

    /*private String html = "";

    public String getHtml() {
        return html;
    }*/

    /**
     * If String contains ${expression} tag, it replaces to HashMap's value
     * @param htmlString template for html page
     * @param keyMap Hashmap
     * @return html after tag's replacement
     */
    public static String changeTAG_to_HTML(String htmlString, HashMap<String, String> keyMap) {

        String tag = "";
        String value = "";
        String result = "";

        while (htmlString.contains("${") && htmlString.contains("}")) {
            int tag_starts = 0;
            int tag_ends = 0;
            //Поиск тега
            tag_starts = htmlString.indexOf("${", tag_ends);
            tag_ends = htmlString.indexOf("}", tag_starts);
            //Если тег найден
            if (tag_starts != -1) {
                //Получение тега (убираем кавычки)
                tag = htmlString.substring(tag_starts + 2, tag_ends);
                //Замена найденного тега на контент из keymap, если таковой имеется
                if (keyMap.containsKey(tag)) {
                    value = keyMap.get(tag);
                    log.debug(value);
                    //Значение тега в keymap тоже может содержать теги
                    if (value.contains("${") && value.contains("}")) {
                        value = changeTAG_to_HTML(value, keyMap);
                    }
                    //Наконец сама замена тега на контент из keymap
                    htmlString = htmlString.replace("${" + tag + "}", value);
                } else {
                    log.info("Тег " + tag + " не найден");
                    htmlString = htmlString.replace("${" + tag + "}", "");
                    //htmlString = htmlString.replace("${" + tag + "}", "(tag = {" + tag + "} were not found)");
                }
            } else {
                result = "Tags were not found";
                log.info("Tags were not found");
            }
        }
        result = htmlString;
        //this.html = result;
        return result;
    }


}
