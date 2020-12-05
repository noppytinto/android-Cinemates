package mirror42.dev.cinemates.tmdbAPI;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class JsonUtilities {

    /**
     * Opens a connection to this url
     * and returns any raw data in String format.
     *
     * @return  raw data in String format.
     *          If openStream()) yield to an invalid response (IOException)
     *          returns null.
     */
    private static String getRawDataFromURL(String url) {
        String result = null;

        try (InputStream is = new URL(url).openStream()) {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            int cp;
            while ((cp = rd.read()) != -1) {
                sb.append((char) cp);
            }

            result = sb.toString();
        }
        catch (IOException e1) {
            return null;
        }
        catch (Exception e) {
            e.getMessage();
            e.printStackTrace();
        }

        return result;
    }


    /**
     * Opens a connection to this url
     * and returns a json in {@code JSONObject} format.
     *
     * @return  json in JSONObject format.
     *          If url is invalid, returns null.
     *
     * @see     #getRawDataFromURL(String)
     */
    public static JSONObject getJsonObjectFromUrl(String url) {
        JSONObject json = null;

        try {
            String jsonText = getRawDataFromURL(url);

            if(jsonText!=null)
                json = new JSONObject(jsonText);
        }
        catch (Exception e) {
            e.getMessage();
            e.printStackTrace();
        }
        return json;
    }

}// end JsonUtilities class
