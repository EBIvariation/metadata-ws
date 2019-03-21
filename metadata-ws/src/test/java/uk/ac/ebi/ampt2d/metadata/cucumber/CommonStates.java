package uk.ac.ebi.ampt2d.metadata.cucumber;

import org.springframework.test.web.servlet.ResultActions;

import java.time.ZonedDateTime;
import java.util.HashMap;
import java.util.Map;

public class CommonStates {

    private static Map<String, String> urlMap = new HashMap<>();

    private static ResultActions resultActions;

    private static ZonedDateTime time1, time2;

    public static String STUDY_NON_EXISTING_URL = "https://nohost//studies/999";

    public static void clear() {
        urlMap.clear();
        resultActions = null;
    }

    public static String getUrl(String key) {
        return urlMap.get(key);
    }

    public static void setUrl(String key, String value) {
        urlMap.put(key, value);
    }

    public static ResultActions getResultActions() {
        return resultActions;
    }

    public static void setResultActions(ResultActions resultActions) {
        CommonStates.resultActions = resultActions;
    }

    public static void setTime1() {
        time1 = ZonedDateTime.now();
    }

    public static ZonedDateTime getTime1() {
        return time1;
    }

    public static void setTime2() {
        time2 = ZonedDateTime.now();
    }

    public static ZonedDateTime getTime2() {
        return time2;
    }
}