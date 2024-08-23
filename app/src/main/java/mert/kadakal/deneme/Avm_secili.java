package mert.kadakal.deneme;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.Arrays;


public class Avm_secili extends AppCompatActivity {
    private ListView listView;
    private ArrayList<String> items;
    private HtmlArrayAdapter adapter;
    private TextView toptext;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.avm);

        listView = findViewById(R.id.liste);
        items = new ArrayList<>();
        adapter = new HtmlArrayAdapter(this, R.layout.list_item, items);
        listView.setAdapter(adapter);
        toptext = (TextView) findViewById(R.id.secili_avm_ust);

        if (getIntent().getStringExtra("AVM_ISMI").equals("Carrefour")) {
            String url = "https://www.paribucineverse.com/sinemalar/carrefour-bursa";
            new FetchDataTask().execute(url);
        }


    }

    private class FetchDataTask extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... urls) {

            String url = urls[0];
            try {
                toptext.setText(getIntent().getStringExtra("AVM_ISMI") + " AVM'de vizyondaki filmler");
                Document document = Jsoup.connect(url).get();
                Elements films = document.select(".item-list-detail");

                // Veriyi işleyin
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
                        times.append(time.text() + "<br>");
                    }
                    times.deleteCharAt(times.length() - 1);

                    // HTML formatında stil ekleyin
                    items.add(String.format(
                            "<b><i>%s</i></b><br>------<br>%s<br>------<br>%s",
                            movieTitle, movieGenre, String.valueOf(times)
                    ));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

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
                    intent.putExtra("URL", url);
                    intent.putExtra("ind", i);
                    startActivity(intent);
                }
            });

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            // Listeyi güncelle
            adapter.notifyDataSetChanged();
        }

    }
}

