package Network;

import android.os.StrictMode;
import org.json.JSONArray;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

public class JSONParser {
    public JSONParser(){
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
    }

    // Return JSONArray from url's response.
    public JSONArray getJSONArray(String url){
        JSONArray jsonArray = null;
        String json = null;
        try{
            json = getContents(url);
            jsonArray = new JSONArray(json);
        }catch(Exception e){
            System.out.println(e.toString());
        }
        return jsonArray;
    }

    // Returns content of url response.
    private String getContents(String urlString) throws IOException{
        URL url = new URL(urlString);
        URLConnection con = url.openConnection();
        InputStream in = con.getInputStream();
        String body = convertStreamToString(in);
        return trimLastChar(body);
    }

    // Converts input stream to a usable string.
    private String convertStreamToString(InputStream is) throws UnsupportedEncodingException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is, "UTF-8"));
        StringBuilder sb = new StringBuilder();
        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }

    // Removes last character from string.
    private String trimLastChar(String str) {
        str = str.substring(0, str.length()-1);
        return str;
    }

}