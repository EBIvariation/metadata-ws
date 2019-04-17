/*
 *
 * Copyright 2019 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package uk.ac.ebi.ampt2d.metadata.cucumber;

import org.springframework.test.web.servlet.ResultActions;

import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

public class CommonStates {

    private static Map<String, String> urlMap = new HashMap<>();

    private static Map<String, ZonedDateTime> timeMap = new HashMap<>();

    private static ResultActions resultActions;

    public static void clear() {
        urlMap.clear();
        timeMap.clear();
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

    public static void setTime(String key, ZonedDateTime value) {
        timeMap.put(key, value);
    }

    public static ZonedDateTime getTime(String key) {
        return timeMap.get(key);
    }

    public static List<String> getUrls(String commaDelimitedKeys) {
        if (commaDelimitedKeys == "NONE") {
            return null;
        }

        return Arrays.stream(commaDelimitedKeys.split(","))
                .map(key -> getUrl(key))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
}