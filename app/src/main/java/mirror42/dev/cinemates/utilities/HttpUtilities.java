package mirror42.dev.cinemates.utilities;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class HttpUtilities {


    /************* DEPRECATED
     * Opens a connection to this url
     * and returns any raw data in String format.
     *
     * @return  raw data in String format.
     *          If openStream()) yield to an invalid response (IOException)
     *          returns null.
     ************ DEPRECATED*/
//    private static String getRawDataStringFromURL_classic(String urlString) {
//        String result = null;
//
//        try {
//            URL url = new URL(urlString);
//            HttpsURLConnection urlConnection = (HttpsURLConnection) url.openConnection();
//            urlConnection.connect();
//            InputStream is = urlConnection.getInputStream();
//
//            result = buildStringFromInputStream(is);
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return result;
//    }// end getRawDataStringFromURL_classic()
//
//    public static String buildStringFromInputStream(InputStream in) {
//        String result = null;
//
//        try {
//            // creating reader
//            BufferedReader rd = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
//
//            // converting inputstream into string
//            StringBuilder sb = new StringBuilder();
//            int cp;
//            while ((cp = rd.read()) != -1) {
//                sb.append((char) cp);
//            }
//
//            //
//            result = sb.toString();
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return result;
//
//    }// end buildStringFromInputStream()


    /*************** NEWEST VERSION (with okHttp)
     *
     */
    private static String getRawDataStringFromURL(String urlString) {
        String result = null;
        final OkHttpClient client = OkHttpSingleton.getClient();
        Request request = new Request.Builder()
                .url(urlString)
                .build();

        // calling
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Unexpected code " + response);
            }

            result = response.body().string();

        }catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }// end getRawDataStringFromURL()

    /**
     * Opens a connection to this url
     * and returns a json in {@code JSONObject} format.
     *
     * @return  json in JSONObject format.
     *          If url is invalid, returns null.
     *
     * @see     #getRawDataStringFromURL(String)
     */
    public static JSONObject getJsonObjectFromUrl(String url) {
        JSONObject json = null;

        try {
            String jsonString = getRawDataStringFromURL(url);

            if(jsonString!=null)
                json = new JSONObject(jsonString);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        return json;
    }// end getJsonObjectFromUrl()

    /**
     * build Http GET request (okHttp3)
     * for postgres
     */
    public static Request buildPostgresGETrequest(HttpUrl httpUrl, String token) {
        Request request = null;
        try {

//            HttpUrl httpUrl = new HttpUrl.Builder()
//                    .scheme("https")
//                    .host(remoteConfigServer.getAzureHostName())
//                    .addPathSegments(remoteConfigServer.getPostgrestPath())
//                    .addPathSegment(dbFunction)
//                    .build();

            request = new Request.Builder()
                    .url(httpUrl)
                    .header("User-Agent", "OkHttp Headers.java")
                    .addHeader("Accept", "application/json; q=0.5")
                    .addHeader("Accept", "application/vnd.github.v3+json")
                    .addHeader("Authorization", "Bearer " + token)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return request;
    }

    /**
     * build Http POST request (okHttp3)
     * for postgres
     */
    public static Request buildPostgresPOSTrequest(HttpUrl httpUrl, RequestBody requestBody, String token) {
        Request request = null;
        try {
            request = new Request.Builder()
                    .url(httpUrl)
                    .header("User-Agent", "OkHttp Headers.java")
                    .addHeader("Accept", "application/json; q=0.5")
                    .addHeader("Accept", "application/vnd.github.v3+json")
                    .addHeader("Authorization", "Bearer " + token)
                    .post(requestBody)
                    .build();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return request;
    }

    public static HttpUrl buildHttpURL(String dbFunctionName) {
        HttpUrl httpUrl = new HttpUrl.Builder()
                .scheme("https")
                .host(RemoteConfigServer.getInstance().getAzureHostName())
                .addPathSegments(RemoteConfigServer.getInstance().getPostgrestPath())
                .addPathSegment(dbFunctionName)
                .build();

        return httpUrl;
    }

//    public static Request buildRemoteDBrequest(String dbFunctionName, String token) {
//        RemoteConfigServer remoteConfigServer = RemoteConfigServer.getInstance();
//        Request request = null;
//
//        try {
//            request = new Request.Builder()
//                    .url(remoteConfigServer.getAzureBaseUrl()+ dbFunctionName)
//                    .header("User-Agent", "OkHttp Headers.java")
//                    .addHeader("Accept", "application/json; q=0.5")
//                    .addHeader("Accept", "application/vnd.github.v3+json")
//                    .addHeader("Authorization", "Bearer " + token)
//                    .build();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        return request;
//    }

}// end HttpUtilities class
