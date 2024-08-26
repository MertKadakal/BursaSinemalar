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
import java.util.Calendar;
import java.util.GregorianCalendar;


public class Avm_secili extends AppCompatActivity {
    private ListView listView;
    private ArrayList<String> items;
    private HtmlArrayAdapter adapter;
    private TextView toptext;
    private EditText turAra;
    private String enteredText;
    private TextView filmyok;

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
        filmyok = (TextView) findViewById(R.id.film_yok);

        String url = null;
        switch (getIntent().getStringExtra("AVM_ISMI")) {
            case "Carrefour":
                url = "https://www.paribucineverse.com/sinemalar/carrefour-bursa";
                break;
            case "Korupark":
                url = "https://www.beyazperde.com/sinemalar/sinema-T0085/";
                break;
        }
        new FetchDataTask().execute(url);

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
                intent.putExtra("AVM", getIntent().getStringExtra("AVM_ISMI"));
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

                //AVM ismine göre filmleri listele
                Document document = Jsoup.connect(url).get();
                switch (getIntent().getStringExtra("AVM_ISMI")) {

                    //CARREFOUR
                    case "Carrefour":
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
                        }
                        break;

                    //KORUPARK
                    case "Korupark":

                        for (Element film : document.select("div.card.entity-card.entity-card-list.movie-card-theater.cf.hred")) {
                            //film ismi
                            String movie_title = film.select("div.meta")
                                    .select("h2 > a").text();

                            //filmin vizyon tarihi + türleri + ülkeleri
                            String vizyon_tur_ulke = film.select("div.meta")
                                    .select("div.meta-body > div.meta-body-item.meta-body-info").text();

                            String vizyona_giris = vizyon_tur_ulke.split(" \\|")[0];
                            String türler = vizyon_tur_ulke.split("\\| ")[1].split(" /")[0];
                            String ülkeler = vizyon_tur_ulke.split("/ ")[1];

                            items.add(String.format("%s\n%s", movie_title, türler));
                            Log.d("tag", türler);
                        }
                        break;

                }

                //----TÜM SEANSLARIN GEÇİP GEÇMEDİĞİ----
                //tüm saatlerden en geç olanını belirle
                int most_time = 0;
                for (String item : items) {
                    for (int i = 0; i<item.split("------")[2].split("<br>").length;i++) {
                        if (item.split("------")[2].split("<br>")[i].length()>4) {
                            String saat = item.split("------")[2].split("<br>")[i].substring(0,5);
                            int mins = Integer.parseInt(saat.split(":")[0])*60 + Integer.parseInt(saat.split(":")[1]);
                            if (mins > most_time) {
                                most_time = mins;
                            }
                        }
                    }
                }

                /*
                //belirlenen en son saatten de geçmişse ekrana "film yok" mesajı yaz
                Calendar calendar = Calendar.getInstance();
                int hour = calendar.get(Calendar.HOUR_OF_DAY);
                int minute = calendar.get(Calendar.MINUTE);
                if (hour*60 + minute > most_time) {
                    items.subList(1,items.size()).clear();
                    items.set(0, "");
                    listView.setVisibility(View.INVISIBLE);
                    turAra.setVisibility(View.INVISIBLE);
                    filmyok.setVisibility(View.VISIBLE);
                }
                else {
                    filmyok.setVisibility(View.INVISIBLE);
                }
                */
                
            } catch (Exception e) {
                Log.d("error bulundu", e.toString());
            }

            runOnUiThread(() -> adapter.notifyDataSetChanged());

            return null;
        }
    }
}

