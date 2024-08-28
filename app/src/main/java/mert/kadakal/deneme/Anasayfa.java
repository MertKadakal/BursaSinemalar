package mert.kadakal.deneme;

import android.content.Intent;
import android.content.om.FabricatedOverlay;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class Anasayfa extends AppCompatActivity {
    private Button butonCarrefour;
    private Button butonKorupark;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.anasayfa);

        butonCarrefour = (Button) findViewById(R.id.Carrefour);
        butonKorupark = (Button) findViewById(R.id.Korupark);

        View.OnClickListener listener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Anasayfa.this, Avm_secili.class);
                intent.putExtra("AVM_ISMI", getResources().getResourceEntryName(v.getId()));
                startActivity(intent);
            }
        };

        butonKorupark.setOnClickListener(listener);
        butonCarrefour.setOnClickListener(listener);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finishAffinity();
    }
}