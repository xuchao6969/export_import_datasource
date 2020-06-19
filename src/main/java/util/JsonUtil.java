package util;


import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;


public class JsonUtil {

    private static final ObjectMapper mObjectMapper = new ObjectMapper();

    static {
        mObjectMapper.configure(Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER, true);
        mObjectMapper.configure(Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
        mObjectMapper.configure(Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
        mObjectMapper.configure(Feature.ALLOW_SINGLE_QUOTES, true);
        mObjectMapper.configure(Feature.ALLOW_NUMERIC_LEADING_ZEROS, true);
        mObjectMapper.configure(Feature.ALLOW_NON_NUMERIC_NUMBERS, true);

        mObjectMapper.configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true);
        mObjectMapper.configure(DeserializationFeature.READ_ENUMS_USING_TO_STRING, true);
        mObjectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true);
        mObjectMapper.configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true);
        mObjectMapper.configure(DeserializationFeature.READ_DATE_TIMESTAMPS_AS_NANOSECONDS, true);

        DateFormat myDateFormat = new SimpleDateFormat("yyyy-MM-DD hh:mm:ss");
        mObjectMapper.getSerializationConfig().with(myDateFormat);
        mObjectMapper.getDeserializationConfig().with(myDateFormat);
    }

    /**
     * parameters key
     */
    private static final String PARA_KEY = "parameters";

    /**
     * @param jsonString
     * @return
     */
    public static Map<?, ?> json2Map(String jsonString) {
        try {
            Map<?, ?> map = mObjectMapper.readValue(jsonString, Map.class);
            return map;
        } catch (JsonMappingException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String list(List<?> list) {
        try {
            String ret = "";
            ret = mObjectMapper.writeValueAsString(list);
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @param jsonString
     * @return
     * @description
     */
    public static List<Map<?, ?>> json2MapOfArrayList(String jsonString) {
        try {
            JavaType javaType = getCollectionType(ArrayList.class, Map.class);
            @SuppressWarnings("unchecked")
            List<Map<?, ?>> arrayList = (List<Map<?, ?>>) mObjectMapper.readValue(jsonString, javaType);

            return arrayList;
        } catch (JsonMappingException e) {
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @param collectionClass
     * @param elementClasses
     * @return
     */
    public static JavaType getCollectionType(Class<?> collectionClass, Class<?>... elementClasses) {
        return mObjectMapper.getTypeFactory().constructParametricType(collectionClass, elementClasses);
    }

    /**
     * @param map
     * @return
     * @description map to json string
     */
    public static String map2Json(Map<?, ?> map) {
        try {
            String ret = "";
            ret = mObjectMapper.writeValueAsString(map);
            // remove all "\"
            ret = ret.replaceAll("\\\\", "");
            if (ret.contains("\"[")) {
                ret = ret.replaceAll("\"\\[", "\\[");
            }
            if (ret.contains("]\"")) {
                ret = ret.replaceAll("\\]\"", "\\]");
            }
            if (ret.contains("\"{")) {
                ret = ret.replaceAll("\"\\{", "\\{");
            }
            if (ret.contains("}\"")) {
                ret = ret.replaceAll("\\}\"", "\\}");
            }
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @param list
     * @return
     */
    public static String listMap2Json(List<Map<String, Object>> list) {
        try {
            String ret = "";
            ret = mObjectMapper.writeValueAsString(list);
            // remove all "\"
            ret = ret.replaceAll("\\\\", "");
            return ret;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * @param strContent
     * @return
     * @description
     */
    public static String getJsonString(String strContent) {
        String ret = "";

        if (!strContent.contains(PARA_KEY)) {
            return ret;
        }
        String regex = "parameters[\\s]+=";
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(strContent);
        if (m.find()) {
            strContent = strContent.replaceFirst(regex, "");
        } else {
            regex = "parameters=";
            p = Pattern.compile(regex);
            m = p.matcher(strContent);
            if (m.find()) {
                strContent = strContent.replaceFirst(regex, "");
            }
        }
        strContent = strContent.trim();
        return strContent;
    }

    /**
     * @param byteArray
     * @return
     * @description
     */
    public static String bytes2Hex(byte[] byteArray) {
        StringBuffer strBuf = new StringBuffer();
        for (int i = 0; i < byteArray.length; i++) {
            if (byteArray[i] >= 0 && byteArray[i] < 16) {
                strBuf.append("0");
            }
            strBuf.append(Integer.toHexString(byteArray[i] & 0xFF));
        }
        return strBuf.toString();
    }

    /**
     * method_name: mapFormatString2List
     * <p>
     * parameters: mapString format is: [ { id=1, time=2013-11-09 09:00:00 }, {
     * id=2, time=2013-11-10 09:00:00 } ]
     * <p>
     * <p>
     * return: List<Map<String, Object>>
     */
    public static List<Map<String, Object>> mapFormatString2List(String strContent) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        String ret = "";
        String regex = "\\{[^}]+\\}"; // \\{[^}]+\\} {[^}]*}
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(strContent);
        while (m.find()) {
            ret = m.group();
            ret = ret.replaceAll("\\{", "");
            ret = ret.replaceAll("\\}", "");
            ret = ret.trim();
            Map<String, Object> map = transStringToMap(ret);
            list.add(map);
        }
        return list;
    }

    /**
     * method_name: transStringToMap
     * <p>
     * parameters: mapString format is: id=1, time=2013-11-09 09:00:00 (delim:",",
     * token:"=")
     * <p>
     * return: Map
     */
    public static Map<String, Object> transStringToMap(String mapString) {
        Map<String, Object> map = new HashMap<String, Object>();
        StringTokenizer items;
        for (StringTokenizer entrys = new StringTokenizer(mapString, ","); entrys.hasMoreTokens(); map
                .put(items.nextToken().trim(), items.hasMoreTokens() ? ((Object) (items.nextToken().trim())) : null)) {
            items = new StringTokenizer(entrys.nextToken(), "=");
        }
        return map;
    }

    /**
     * @param str
     * @return
     * @description trim
     */
    public static String trimAll(String str) {
        if (null == str || str.length() <= 0) {
            return str;

        } else {
            return str.replaceAll("^[ ]+|[ ]+$", "");
        }
    }

}
