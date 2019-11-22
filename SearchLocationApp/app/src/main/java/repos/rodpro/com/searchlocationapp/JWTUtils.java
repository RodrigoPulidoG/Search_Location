package repos.rodpro.com.searchlocationapp;

import android.util.Base64;
import android.util.Log;

import java.io.UnsupportedEncodingException;

public class JWTUtils {
    private static final String TAG = "JWT_DECODED";
    static String header;
    static String body;

    static void decoded(String JWTEncoded) {
        try {
            String[] split = JWTEncoded.split("\\.");
            Log.d(TAG, "Header: " + getJson(split[0]));
            Log.d(TAG, "Body: " + getJson(split[1]));
            header = getJson(split[0]);
            body = getJson(split[1]);
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "decoded: " + e);
        }
    }


    private static String getJson(String strEncoded) throws UnsupportedEncodingException{
        byte[] decodedBytes = Base64.decode(strEncoded, Base64.URL_SAFE);
        return new String(decodedBytes, "UTF-8");
    }

    public static String getHeader() {
        return header;
    }

    public static String getBody(){
        return body;
    }
}
