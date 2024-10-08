package mert.kadakal.deneme;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;

public class Secili_film extends AppCompatActivity {
    private TextView film_ismi;
    private TextView film_turu;
    private TextView film_saatleri;
    private TextView en_yakin_seans;
    private ImageButton info_button;
    private ImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.secili_film);

        film_ismi = (TextView) findViewById(R.id.film_ismi);
        film_ismi.setText(Html.fromHtml(String.format("<b>%s</b>", getIntent().getStringExtra("FILM_ISMI").trim())));

        //film bilgileri tuşu
        info_button = (ImageButton) findViewById(R.id.info_button);
        info_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Secili_film.this, Film_infos.class);
                intent.putExtra("FILM_ISMI", getIntent().getStringExtra("FILM_ISMI").trim());
                intent.putExtra("AVM", getIntent().getStringExtra("AVM"));
                intent.putExtra("FILM_INFO_URL", getIntent().getStringExtra("FILM_INFO_URL"));
                startActivity(intent);
            }
        });

        //film türünü kapağın sol altında listele
        String film_turuHtml;
        if (getIntent().getStringExtra("FILM_TURU").split(",").length > 0) {
            StringBuilder genres = new StringBuilder();
            for (String genre : getIntent().getStringExtra("FILM_TURU").split(", ")) {
                genres.append(genre + "<br>");
            }
            film_turuHtml = "<b>Tür</b><br><br>" + genres;
        }
        else {
            film_turuHtml = String.format("<b>Tür</b><br><br>%s", getIntent().getStringExtra("FILM_TURU"));
        }
        film_turu = (TextView) findViewById(R.id.film_turu);
        film_turu.setText(Html.fromHtml(film_turuHtml));

        //en yakın 3 seansı kapağın sağ altına listele
        film_saatleri = (TextView) findViewById(R.id.film_saatler);
        film_saatleri.setText(getIntent().getStringExtra("FILM_SAATLERI"));
        StringBuilder saatlerListe = new StringBuilder();
        for (String saat : film_saatleri.getText().toString().split("<br>")) {
            saatlerListe.append(saat + "<br>");
        }
        saatlerListe.delete(saatlerListe.length()-4, saatlerListe.length()-1);
        StringBuilder gosterilecek_seanslar = new StringBuilder();
        for (int i = 1; i < 4; i++) {
            if (i < saatlerListe.toString().split("<br>").length) {
                gosterilecek_seanslar.append(saatlerListe.toString().split("<br>")[i] + "<br>");
            }
        }
        if (saatlerListe.toString().split("<br>").length > 4) {
            gosterilecek_seanslar.append(":::");
        }
        String film_saatleriHtml = String.format("<b>Seanslar</b><br><br>%s", gosterilecek_seanslar);
        film_saatleri.setText(Html.fromHtml(film_saatleriHtml));

        en_yakin_seans = (TextView) findViewById(R.id.en_yakin_seans);
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);
        int current_mins = hour*60 + minute;
        int seans_hour = 0;
        int seans_min = 0;
        int seans_mins = 0;
        ArrayList<Integer> diffs = new ArrayList<>();
        int stars_count = 0;
        for (String seans : getIntent().getStringExtra("FILM_SAATLERI").toString().substring(4, getIntent().getStringExtra("FILM_SAATLERI").toString().length()-3).split("<br>")) {
            if (!(seans.equals("***"))) {
                seans_hour = Integer.parseInt(seans.split(":")[0]);
                seans_min = Integer.parseInt(seans.split(":")[1].substring(0,2));
                if ((seans_hour*60 + seans_min) > current_mins) {
                    diffs.add((seans_hour*60 + seans_min) - current_mins);
                }
                else {
                    diffs.add(null);
                }
            }
            else {
                stars_count++;
            }
        }
        int min_diff = 9999;
        for (Integer element : diffs) {
            if (element != null && element < min_diff) {
                min_diff = element;
            }
        }
        if (!(min_diff == 9999)) {
            int ind_min_diff = diffs.indexOf(min_diff);
            String seans = getIntent().getStringExtra("FILM_SAATLERI").toString().substring(4, getIntent().getStringExtra("FILM_SAATLERI").toString().length()-3).split("<br>")[ind_min_diff+stars_count];
            seans_hour = Integer.parseInt(seans.split(":")[0]);
            seans_min = Integer.parseInt(seans.split(":")[1]);
            seans_mins = seans_hour*60 + seans_min;
        }

        //en yakın seansa kalan süreyi yaz
        if ((seans_mins-current_mins)%60 < 0) {
            en_yakin_seans.setText("Tüm seanslar geride kaldı");
        }
        else {
            if ((seans_mins-current_mins) < 60) {
                en_yakin_seans.setText(String.format("En yakın seans %d dakika sonra", (seans_mins-current_mins)%60));
            }
            else if ((seans_mins-current_mins)%60 == 0) {
                en_yakin_seans.setText(String.format("En yakın seans %d saat sonra", (seans_mins-current_mins)/60));
            }
            else {
                en_yakin_seans.setText(String.format("En yakın seans %d saat %d dakika sonra", (seans_mins-current_mins)/60, (seans_mins-current_mins)%60));
            }
        }

        imageView = findViewById(R.id.imageView);

        // Görseli web sayfasından çekmek için yeni bir thread başlatın
        new Thread(new Runnable() {
            @Override
            public void run() {

                //AVM'ye göre seçilen filmin kapağını görüntüle
                switch (getIntent().getStringExtra("AVM")) {
                    //CARREFOUR
                    case "Carrefour":
                        // Logcat için Tag
                        String TAG = "secilifilm";

                        // İlgili URL
                        String url = "https://www.paribucineverse.com/sinemalar/carrefour-bursa";

                        // Aranan film ismi
                        String filmIsmi = getIntent().getStringExtra("FILM_ISMI");

                        try {
                            // URL'deki HTML içeriğini indir ve parse et
                            Document doc = Jsoup.connect(url).get();

                            // "row" classı olan tüm elementleri seçiyoruz
                            Elements rows = doc.select(".row");

                            // Her "row" elementini kontrol ediyoruz
                            for (Element row : rows) {
                                // Eğer "data-movie-title" attribute'u aradığımız film ismine eşitse
                                if (filmIsmi.equals(row.attr("data-movie-title"))) {
                                    // "cinema-detail-link" classını buluyoruz
                                    Element cinemaDetailLink = row.selectFirst(".cinema-detail-link");

                                    // "cinema-detail-link" classı içerisindeki img etiketinin "src" attribute'unu alıyoruz
                                    String src = cinemaDetailLink.selectFirst("img").attr("src");

                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            // Glide kullanarak görseli ImageView'a yükle
                                            Glide.with(Secili_film.this).load(src).into(imageView);
                                        }
                                    });

                                    // İlgili film bulunduğu için döngüyü sonlandırıyoruz
                                    break;
                                }
                            }

                        } catch (IOException e) {
                            Log.e(TAG, "Bir hata oluştu: ", e);
                        }
                        break;

                    //KORUPARK
                    case "Korupark":
                        // Logcat için Tag
                        TAG = "secilifilm";

                        // İlgili URL
                        url = "https://www.beyazperde.com/sinemalar/sinema-T0085/";

                        // Aranan film ismi
                        filmIsmi = getIntent().getStringExtra("FILM_ISMI");

                        try {
                            // URL'deki HTML içeriğini indir ve parse et
                            Document doc = Jsoup.connect(url).get();

                            // Belirli bir class ismine sahip tüm <a> etiketlerini seç
                            Elements links = doc.select(".movie-card-theater a[href]");

                            // Her bir <a> etiketinin href değerini yazdır
                            for (Element link : links) {
                                String info_url = "https://www.beyazperde.com" + link.attr("href");
                                Document doc_info = Jsoup.connect(info_url).get();

                                // <img> etiketini seç ve src özniteliğini al
                                Element img = doc_info.select("img.thumbnail-img").first();
                                if ((img != null) && (img.attr("alt").trim().equals(filmIsmi))) {
                                    String src = img.attr("src");
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            // Glide kullanarak görseli ImageView'a yükle
                                            Glide.with(Secili_film.this).load(src).into(imageView);
                                            String info_url_final = "https://www.beyazperde.com" + link.attr("href");;
                                        }
                                    });
                                    break;
                                }
                            }
                        } catch (IOException e) {
                            Log.d("err", e.toString());
                        }
                        break;
                }
            }
        }).start();
    }
}