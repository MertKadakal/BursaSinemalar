package mert.kadakal.deneme;

import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.util.Calendar;


public class Secili_film extends AppCompatActivity {
    private TextView film_ismi;
    private TextView film_turu;
    private TextView film_saatleri;
    private TextView en_yakin_seans;

    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.secili_film);

        film_ismi = (TextView) findViewById(R.id.film_ismi);
        film_ismi.setText(getIntent().getStringExtra("FILM_ISMI"));

        String film_turuHtml = String.format("<b>Tür</b><br><br>%s", getIntent().getStringExtra("FILM_TURU"));
        film_turu = (TextView) findViewById(R.id.film_turu);
        film_turu.setText(Html.fromHtml(film_turuHtml));

        film_saatleri = (TextView) findViewById(R.id.film_saatler);
        film_saatleri.setText(getIntent().getStringExtra("FILM_SAATLERI"));
        StringBuilder saatlerListe = new StringBuilder();
        for (String saat : film_saatleri.getText().toString().split("<br>")) {
            saatlerListe.append(saat + "<br>");
        }
        saatlerListe.delete(saatlerListe.length()-4, saatlerListe.length()-1);
        String film_saatleriHtml = String.format("<b>Seanslar</b><br>%s", saatlerListe.toString());
        film_saatleri.setText(Html.fromHtml(film_saatleriHtml));

        en_yakin_seans = (TextView) findViewById(R.id.en_yakin_seans);
        String en_yakin_seansStr = getIntent().getStringExtra("FILM_SAATLERI").toString().split("<br>")[1].substring(0,5);
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        Log.d("SAAT-DK", String.valueOf(hour));
        int current_mins = hour*60 + minute;
        int seans_hour = Integer.parseInt(en_yakin_seansStr.split(":")[0]);
        int seans_min = Integer.parseInt(en_yakin_seansStr.split(":")[1]);
        int seans_mins = seans_hour*60 + seans_min;
        en_yakin_seans.setText(String.format("En yakın seans %d saat %d dakika sonra", (seans_mins-current_mins)/60, (seans_mins-current_mins)%60));

        imageView = findViewById(R.id.imageView);

        // Görseli web sayfasından çekmek için yeni bir thread başlatın
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // Web sayfasını indir
                    Document doc = Jsoup.connect(getIntent().getStringExtra("URL")).get();

                    // Belirli img etiketini seç (class adıyla)
                    Element imgElement = doc.select("img.cinema-detail-movie-banner.img-fluid").get(getIntent().getIntExtra("ind", 0));

                    // Eğer imgElement null değilse, src attribute'unu al
                    if (imgElement != null) {
                        String imgUrl = imgElement.absUrl("src");

                        // Görsel URL'sini kullanarak resmi göster (UI işlemleri ana thread'de yapılmalı)
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // Glide kullanarak görseli ImageView'a yükle
                                Glide.with(Secili_film.this)
                                        .load(imgUrl)
                                        .into(imageView);
                            }
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}