package mert.kadakal.deneme;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.GenericTransitionOptions;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class Film_infos  extends AppCompatActivity {
    private TextView film_ismi;
    private TextView film_infos;
    private TextView ozet_info;

    private ImageView warning1;
    private ImageView warning2;
    private ImageView warning3;
    private ImageView warning4;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.film_infos);

        warning1 = (ImageView) findViewById(R.id.warning_img1);
        warning2 = (ImageView) findViewById(R.id.warning_img2);
        warning3 = (ImageView) findViewById(R.id.warning_img3);
        warning4 = (ImageView) findViewById(R.id.warning_img4);

        film_infos = (TextView) findViewById(R.id.text_info_area);

        ozet_info = (TextView) findViewById(R.id.ozet);

        film_ismi = (TextView) findViewById(R.id.film_ismi);
        String film_ismi_html = String.format("<b>%s</b>",getIntent().getStringExtra("FILM_ISMI"));
        film_ismi.setText(Html.fromHtml(film_ismi_html));

        //gerekli bilgileri içeren linki elde etme
        new Thread(new Runnable() {
            @Override
            public void run() {
                String src = null;

                switch (getIntent().getStringExtra("AVM")) {
                    case "Carrefour":
                        String TAG = "FilmSrcLogger";
                        String url = "https://www.paribucineverse.com/sinemalar/carrefour-bursa";
                        String filmIsmi = getIntent().getStringExtra("FILM_ISMI");

                        try {
                            Document doc = Jsoup.connect(url).get();
                            Elements rows = doc.select(".row");

                            for (Element row : rows) {
                                if (filmIsmi.trim().equals(row.attr("data-movie-title").trim())) {
                                    Element cinemaDetailLink = row.selectFirst(".cinema-detail-link");
                                    src = "https://www.paribucineverse.com" + cinemaDetailLink.selectFirst("a").attr("href");
                                    break;
                                }
                            }

                            final String finalSrc = src;

                            // İkinci ağ çağrısı için yeni bir thread
                            if (finalSrc != null) {
                                new Thread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {
                                            //gerekli konumlardan selection
                                            Document filmDoc = Jsoup.connect(finalSrc).get();
                                            final String ozet = filmDoc.selectFirst(".film-summary").select("p").toString();
                                            final String textInfosArea = filmDoc.selectFirst(".film-info-text-area").select("p").toString();
                                            final String vizyon = filmDoc.select(".item-info").get(0).select("span").toString();
                                            final String runtime = filmDoc.select(".item-info").get(2).select("span").toString();

                                            for (Element img : filmDoc.selectFirst(".film-info-icons.mobile-view").select("img")) {
                                                runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        // Glide kullanarak görseli ImageView'a yükle
                                                        RequestBuilder<Drawable> glide = Glide.with(Film_infos.this).load("https://www.paribucineverse.com" + img.attr("src"));
                                                        if (filmDoc.selectFirst(".film-info-icons.mobile-view").select("img").indexOf(img) == 0) {
                                                            glide.into(warning1);
                                                            warning1.setVisibility(View.VISIBLE);
                                                        }
                                                        if (filmDoc.selectFirst(".film-info-icons.mobile-view").select("img").indexOf(img) == 1) {
                                                            glide.into(warning2);
                                                            warning2.setVisibility(View.VISIBLE);
                                                        }
                                                        if (filmDoc.selectFirst(".film-info-icons.mobile-view").select("img").indexOf(img) == 2) {
                                                            glide.into(warning3);
                                                            warning3.setVisibility(View.VISIBLE);
                                                        }
                                                        if (filmDoc.selectFirst(".film-info-icons.mobile-view").select("img").indexOf(img) == 3) {
                                                            glide.into(warning4);
                                                            warning4.setVisibility(View.VISIBLE);
                                                        }
                                                    }
                                                });
                                            }

                                            // UI güncellemesi için ana thread'e geç //setText komutları
                                            runOnUiThread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    ozet_info.setText(ozet.substring(3,ozet.length()-4));
                                                    film_infos.setText(Html.fromHtml(
                                                            textInfosArea.replace("<br>", "<br><br>")
                                                                    + vizyon + "<br>"
                                                                    + "<br><b>Süre: </b>" + runtime));
                                                }
                                            });

                                        } catch (IOException e) {
                                            Log.e(TAG, "Bir hata oluştu: ", e);
                                        }
                                    }
                                }).start();
                            } else {
                                Log.d("FilmSrcLogger", "Film bulunamadı");
                            }

                        } catch (IOException e) {
                            Log.e(TAG, "Bir hata oluştu: ", e);
                        }
                        break;

                    //KORUPARK
                    case "Korupark":

                        String finalSrc = getIntent().getStringExtra("FILM_INFO_URL");
                        // İkinci ağ çağrısı için yeni bir thread
                        if (finalSrc != null) {
                            new Thread(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        Document doc = Jsoup.connect(finalSrc).get();

                                        String ozet = doc.selectFirst("div.content-txt ").select("p").text();
                                        ozet_info.setText(ozet);

                                        Element infos_area = doc.selectFirst("div.meta-body");
                                        String vizyon_runtime = infos_area.select("div.meta-body-item.meta-body-info").text();
                                        String vizyona_giris = vizyon_runtime.split(" \\| ")[0].split(" Sinema")[0];
                                        String runtime = vizyon_runtime.split(" \\| ")[1];
                                        String director = infos_area.select("div.meta-body-item.meta-body-direction ").text().substring(9);
                                        String actors = infos_area.select("div.meta-body-item.meta-body-actor").text().substring(11);
                                        String ulkeler = doc.select("div.item:has(span:matchesOwn(Ülke))").text().substring(5);
                                        String dil = doc.select("div.item:has(span:matchesOwn(Dil))").text().substring(4);
                                        String warning = "";
                                        if (doc.select("div.certificate") != null) {
                                            warning =  "<b>Uyarı: </b>" + doc.select("div.certificate > span.certificate-text").text();
                                            if (warning.equals("<b>Uyarı: </b>")) {
                                                warning = "";
                                            }
                                        }

                                        String finalWarning = warning;
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                film_infos.setText(Html.fromHtml(String.format("<b>Yönetmen: </b>%s<br><br>" +
                                                        "<b>Oyuncular: </b>%s<br><br>" +
                                                        "<b>Tarih: </b>%s<br><br>" +
                                                        "<b>Süre: </b>%s<br><br>" +
                                                        "<b>Ülke: </b>%s<br><br>" +
                                                        "<b>Dil: </b>%s<br><br>" +
                                                        "%s", director, actors, vizyona_giris, runtime, ulkeler, dil, finalWarning)));
                                            }
                                        });

                                    } catch (IOException e) {
                                        Log.e("TAG", "Bir hata oluştu: ", e);
                                    }
                                }
                            }).start();
                        } else {
                            Log.d("FilmSrcLogger", "Film bulunamadı");
                        }
                }
            }
        }).start();


    }
}
