package mert.kadakal.deneme;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;


public class Avm_secili extends AppCompatActivity {
    private ListView listView;
    private ArrayList<String> items;
    private HtmlArrayAdapter adapter;
    private TextView toptext;
    private EditText turAra;
    private String enteredText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.avm);

        listView = findViewById(R.id.liste);
        items = new ArrayList<>();
        adapter = new HtmlArrayAdapter(this, R.layout.list_item, items);
        listView.setAdapter(adapter);
        toptext = findViewById(R.id.secili_avm_ust);
        turAra = findViewById(R.id.tur_giriniz);

        if (getIntent().getStringExtra("AVM_ISMI").equals("Carrefour")) {
            String url = "https://www.paribucineverse.com/sinemalar/carrefour-bursa";
            new FetchDataTask().execute(url);
        }

        turAra.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
                    enteredText = turAra.getText().toString().toLowerCase();
                    ArrayList<String> filteredItems = turFiltrele(enteredText);
                    if (filteredItems.isEmpty()) {
                        Toast.makeText(Avm_secili.this, "İstenen türde film bulunamadı", Toast.LENGTH_SHORT).show();
                    } else {
                        try {
                            Intent intent = new Intent(Avm_secili.this, Aranan_tur.class);
                            intent.putExtra("FILMLER", filteredItems.toString());
                            intent.putExtra("TÜR", enteredText);
                            startActivity(intent);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    return true;
                }
                return false;
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                String film_ismi = items.get(i).split("<br>")[0].split("<i>")[1].split("</i>")[0];
                String film_turu = items.get(i).split("<br>")[2];
                String film_saatleri = items.get(i).split("------")[2];

                Intent intent = new Intent(Avm_secili.this, Secili_film.class);
                intent.putExtra("FILM_ISMI", film_ismi);
                intent.putExtra("FILM_TURU", film_turu);
                intent.putExtra("FILM_SAATLERI", film_saatleri);
                intent.putExtra("URL", getIntent().getStringExtra("URL"));
                intent.putExtra("ind", i);
                startActivity(intent);
            }
        });
    }

    private ArrayList<String> turFiltrele(String aranan) {
        ArrayList<String> aranan_turdekiler = new ArrayList<>();
        for (String item : items) {
            if (item.toLowerCase().split("<br>")[2].equals(aranan)) {
                aranan_turdekiler.add(item);
            }
        }
        return aranan_turdekiler;
    }

    private class FetchDataTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... urls) {
            String url = urls[0];
            try {
                String topTextHtml = String.format("<b>%s AVM'de vizyondaki filmler</b>", getIntent().getStringExtra("AVM_ISMI"));
                runOnUiThread(() -> toptext.setText(Html.fromHtml(topTextHtml)));

                Document document = Jsoup.connect(url).get();
                Elements films = document.select(".item-list-detail");

                for (Element film : films) {
                    String movieTitle = film.select("div.row").attr("data-movie-title");
                    String movieGenre = film.select("div[data-genre]").attr("data-genre");
                    StringBuilder times = new StringBuilder();
                    for (Element time : film.select("div.times-area").select("a[data-time]")) {
                        int to_add = Integer.parseInt(time.text().split(":")[0]);
                        int last_added = 0;
                        try {
                            last_added = Integer.parseInt(times.toString().split("<br>")[times.toString().split("<br>").length-1].split(":")[0]);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (to_add < last_added) {
                            times.append("***<br>");
                        }
                        times.append(time.text()).append("<br>");
                    }
                    times.deleteCharAt(times.length() - 1);

                    items.add(String.format(
                            "<b><i>%s</i></b><br>------<br>%s<br>------<br>%s",
                            movieTitle, movieGenre, times.toString()
                    ));
                    Log.d("saatler", items.get(items.size()-1));
                }

                int most_time = 0;
                for (String item : items) {
                    for (String saat : item.split("------")[2].split("<br>")) {
                        if (!(saat.equals("***"))) {
                            if (Integer.parseInt(saat.substring(0,5).split(":")[0])*60 + Integer.parseInt(saat.substring(0,5).split(":")[1]) > most_time) {
                                most_time = Integer.parseInt(saat.split(":")[0])*60 + Integer.parseInt(saat.split(":")[1]);
                            }
                        }
                    }

                }
                Toast.makeText(Avm_secili.this, String.valueOf(most_time), Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }

            runOnUiThread(() -> adapter.notifyDataSetChanged());

            return null;
        }
    }
}

