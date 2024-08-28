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
    private ListView film_listesi;
    private ArrayList<String> items;
    private HtmlArrayAdapter adapter;
    private TextView toptext;
    private EditText turAra;
    private String enteredText;
    private TextView filmyok;
    private String film_info_page_url;
    private ArrayList<String> film_info_page_url_list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.avm);

        film_listesi = findViewById(R.id.liste);
        items = new ArrayList<>();
        adapter = new HtmlArrayAdapter(this, R.layout.list_item, items);
        film_listesi.setAdapter(adapter);
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
                    ArrayList<String> filteredItems = turFiltrele(enteredText, getIntent().getStringExtra("AVM_ISMI"));
                    if (filteredItems.isEmpty()) {
                        Toast.makeText(Avm_secili.this, "İstenen türde film bulunamadı", Toast.LENGTH_SHORT).show();
                    } else {
                        try {
                            Intent intent = new Intent(Avm_secili.this, Aranan_tur.class);
                            intent.putStringArrayListExtra("FILMLER", filteredItems);
                            intent.putExtra("TÜR", enteredText);
                            intent.putExtra("AVM_ISMI", getIntent().getStringExtra("AVM_ISMI"));
                            startActivity(intent);
                            Log.d("filtre", filteredItems.toString());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    return true;
                }
                return false;
            }
        });

        //listedeki bir filmin seçilmesi
        film_listesi.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                try {
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
                    if (getIntent().getStringExtra("AVM_ISMI").equals("Korupark")) {
                        intent.putExtra("FILM_INFO_URL", film_info_page_url_list.get(i));
                    }
                    startActivity(intent);
                }catch (Exception e) {
                    Log.d("error", e.getMessage());
                }

            }
        });
    }

    private ArrayList<String> turFiltrele(String aranan, String avm_ismi) {
        ArrayList<String> aranan_turdekiler = new ArrayList<>();
        switch (avm_ismi) {
            case "Carrefour":
                for (String item : items) {
                    if (item.toLowerCase().split("<br>")[2].equals(aranan.toLowerCase())) {
                        aranan_turdekiler.add(item);
                    }
                }
                break;
            case "Korupark":
                for (String item : items) {
                    String[] turler = item.toLowerCase().split("<br>")[2].split(", ");
                    for (String tur : turler) {
                        if (tur.toLowerCase().equals(aranan.toLowerCase())) {
                            aranan_turdekiler.add(item);
                            break;
                        }
                    }
                }
                break;
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
                                    movieTitle, movieGenre, times));
                        }
                        break;

                    //KORUPARK
                    case "Korupark":

                        for (Element film : document.select("div.card.entity-card.entity-card-list.movie-card-theater.cf.hred")) {
                            //film ismi
                            String movie_title = film.select("div.meta")
                                    .select("h2 > a").text();

                            film_info_page_url = "https://www.beyazperde.com" + film.select("div.meta")
                                    .select("h2 > a").attr("href");
                            film_info_page_url_list.add(film_info_page_url);

                            //filmin vizyon tarihi + türleri + ülkeleri
                            String vizyon_tur_ulke = film.select("div.meta")
                                    .select("div.meta-body > div.meta-body-item.meta-body-info").text();

                            String vizyona_giris = vizyon_tur_ulke.split(" \\|")[0];
                            String genres = vizyon_tur_ulke.split("\\| ")[1].split(" /")[0];
                            String countries = vizyon_tur_ulke.split("/ ")[1];
                            StringBuilder showtimes = new StringBuilder();
                            for (Element showtime : film.select("span.showtimes-hour-item-value")) {
                                showtimes.append(showtime.text() + "<br>");
                            }
                            showtimes.deleteCharAt(showtimes.length()-1);

                            items.add(String.format("<b><i>%s</i></b><br>------<br>%s<br>------<br>%s", movie_title, genres, showtimes));
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

