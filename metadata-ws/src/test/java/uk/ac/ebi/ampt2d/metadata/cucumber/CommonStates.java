package uk.ac.ebi.ampt2d.metadata.cucumber;

import org.springframework.test.web.servlet.ResultActions;

import java.util.HashMap;
import java.util.Map;

public class CommonStates {

    private static Map<String, String> URL_MAP = new HashMap<>();

    private static ResultActions resultActions;

    public static void clear() {
        URL_MAP.clear();
        resultActions = null;
    }

    public static String getUrl(String key) {
        return URL_MAP.get(key);
    }

    public static void setUrl(String key, String value) {
        URL_MAP.put(key, value);
    }

    public static ResultActions getResultActions() {
        return resultActions;
    }

    public static void setResultActions(ResultActions resultActions) {
        CommonStates.resultActions = resultActions;
    }
}
