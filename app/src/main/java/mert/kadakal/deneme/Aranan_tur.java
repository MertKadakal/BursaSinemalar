package mert.kadakal.deneme;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;


public class Aranan_tur extends AppCompatActivity {
    private ListView film_listesi;
    private ArrayList<String> items;
    private HtmlArrayAdapter adapter;
    private TextView topText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.aranantur);

        items = new ArrayList<>();
        film_listesi = findViewById(R.id.liste);
        adapter = new HtmlArrayAdapter(this, R.layout.list_item, items);
        film_listesi.setAdapter(adapter);
        topText = (TextView) findViewById(R.id.aranan_tur_ust);
        String toptextHtml = String.format("<b>%s türündeki filmler</b>", String.valueOf(getIntent().getStringExtra("TÜR").toUpperCase().charAt(0)) + getIntent().getStringExtra("TÜR").toLowerCase().substring(1));
        topText.setText(Html.fromHtml(toptextHtml));

        for (String item : getIntent().getStringArrayListExtra("FILMLER")) {
            items.add(item);
        }

        film_listesi.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String film_ismi = items.get(i).split("<br>")[0].split("<i>")[1].split("</i>")[0];
                String film_turu = items.get(i).split("<br>")[2];
                String film_saatleri = items.get(i).split("------")[2];
                String url = "https://www.paribucineverse.com/sinemalar/carrefour-bursa";

                Intent intent = new Intent(Aranan_tur.this, Secili_film.class);
                intent.putExtra("FILM_ISMI", film_ismi);
                intent.putExtra("FILM_TURU", film_turu);
                intent.putExtra("FILM_SAATLERI", film_saatleri);
                intent.putExtra("URL", url);
                intent.putExtra("ind", i);
                intent.putExtra("AVM", getIntent().getStringExtra("AVM_ISMI"));
                startActivity(intent);
            }
        });

    }

    private class FetchDataTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... urls) {
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            // Listeyi güncelle
            adapter.notifyDataSetChanged();
        }

    }
}

