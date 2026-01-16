/*
 * Copyright (C) 2024 The Android Open Source Project
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
 */
package com.afwsamples.testdpc.delay;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Utility class for serializing and deserializing DPM action parameters.
 */
public class DPMAction {

    /**
     * Serialize action parameters to JSON string.
     */
    public static String serialize(Object... params) {
        try {
            JSONObject json = new JSONObject();
            JSONArray arr = new JSONArray();
            for (Object param : params) {
                if (param == null) {
                    arr.put(JSONObject.NULL);
                } else if (param instanceof Boolean) {
                    arr.put(param);
                } else if (param instanceof Integer) {
                    arr.put(param);
                } else if (param instanceof Long) {
                    arr.put(param);
                } else if (param instanceof String) {
                    arr.put(param);
                } else if (param instanceof String[]) {
                    JSONArray strArr = new JSONArray();
                    for (String s : (String[]) param) {
                        strArr.put(s);
                    }
                    arr.put(strArr);
                } else if (param instanceof List) {
                    JSONArray listArr = new JSONArray();
                    for (Object item : (List<?>) param) {
                        listArr.put(item != null ? item.toString() : JSONObject.NULL);
                    }
                    arr.put(listArr);
                } else if (param instanceof Set) {
                    JSONArray setArr = new JSONArray();
                    for (Object item : (Set<?>) param) {
                        setArr.put(item != null ? item.toString() : JSONObject.NULL);
                    }
                    arr.put(setArr);
                } else {
                    // For complex types, store as string representation
                    arr.put(param.toString());
                }
            }
            json.put("params", arr);
            return json.toString();
        } catch (JSONException e) {
            throw new RuntimeException("Failed to serialize action", e);
        }
    }

    /**
     * Deserialize parameters from JSON string.
     */
    public static Object[] deserialize(String json) {
        try {
            JSONObject obj = new JSONObject(json);
            JSONArray arr = obj.getJSONArray("params");
            Object[] result = new Object[arr.length()];
            for (int i = 0; i < arr.length(); i++) {
                Object val = arr.get(i);
                if (val == JSONObject.NULL) {
                    result[i] = null;
                } else if (val instanceof JSONArray) {
                    // Convert to List<String>
                    JSONArray jsonArr = (JSONArray) val;
                    List<String> list = new ArrayList<>();
                    for (int j = 0; j < jsonArr.length(); j++) {
                        Object item = jsonArr.get(j);
                        list.add(item == JSONObject.NULL ? null : item.toString());
                    }
                    result[i] = list;
                } else {
                    result[i] = val;
                }
            }
            return result;
        } catch (JSONException e) {
            throw new RuntimeException("Failed to deserialize action", e);
        }
    }

    public static boolean getBoolean(Object[] params, int index) {
        return (Boolean) params[index];
    }

    public static int getInt(Object[] params, int index) {
        Object val = params[index];
        if (val instanceof Long) {
            return ((Long) val).intValue();
        }
        return (Integer) val;
    }

    public static long getLong(Object[] params, int index) {
        Object val = params[index];
        if (val instanceof Integer) {
            return ((Integer) val).longValue();
        }
        return (Long) val;
    }

    public static String getString(Object[] params, int index) {
        Object val = params[index];
        return val == null ? null : val.toString();
    }

    @SuppressWarnings("unchecked")
    public static List<String> getStringList(Object[] params, int index) {
        Object val = params[index];
        if (val == null) return null;
        if (val instanceof List) {
            return (List<String>) val;
        }
        return null;
    }

    public static String[] getStringArray(Object[] params, int index) {
        List<String> list = getStringList(params, index);
        return list == null ? null : list.toArray(new String[0]);
    }

    public static Set<String> getStringSet(Object[] params, int index) {
        List<String> list = getStringList(params, index);
        return list == null ? null : new HashSet<>(list);
    }
}
