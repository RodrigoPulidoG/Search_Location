package repos.rodpro.com.searchlocationapp;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class DecodeActivity extends AppCompatActivity {
    private static final String TAG = "TOKEN_DECODED";
    private String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IlJvZHJpZ28gUHVsaWRvIiwiaWF0IjoxNTE2MjM5MDIyfQ.BifBlGWD-9ooFkvbNkmvDTTBCPdq6QZBY5LN9Fkt4lM";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_decode);
        decodeToken();
        getResults();
    }

    private void getResults() {
        setValuesViews(JWTUtils.getHeader(), JWTUtils.getBody());
    }

    private void decodeToken() {
        try {
            JWTUtils.decoded(token);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setValuesViews(String header, String body) {
        Log.d(TAG, "setValuesViews, Header: " + header + ", Body: " + body);
        TextView alg = findViewById(R.id.alg);
        TextView typ = findViewById(R.id.typ);
        TextView sub = findViewById(R.id.sub);
        TextView name = findViewById(R.id.name);
        TextView iat = findViewById(R.id.iat);
        try {
            JSONObject headerJon = new JSONObject(header);
            alg.setText(headerJon.getString("alg"));
            typ.setText(headerJon.getString("typ"));
            JSONObject bodyJon = new JSONObject(body);
            sub.setText(bodyJon.getString("sub"));
            name.setText(bodyJon.getString("name"));
            iat.setText(bodyJon.getString("iat"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


}
