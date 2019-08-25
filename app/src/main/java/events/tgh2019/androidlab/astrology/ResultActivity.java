package events.tgh2019.androidlab.astrology;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ResultActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        // （MainActivityからの）インテントを受け取り、データを取り出す
        Intent intent = getIntent();
        String name = intent.getStringExtra("NAME");
        String birthday = intent.getStringExtra("BIRTHDAY");
        String zodiac = getZodiac(birthday);

        TextView hdr = findViewById(R.id.tvHeader);
        hdr.setText(name + "さん（" + zodiac + "）の今日の運勢");

        // 星占いAPIを通じて占いデータを取得＆加工して画面に貼り付ける。
        // なぜこれだけで動いちゃうかについては、Astrologist クラスのコメントを参照のこと。
        Astrologist a = new Astrologist(zodiac);
        a.execute(birthday);

        ImageView iv = findViewById(R.id.imageView);
        int d = (Math.abs(birthday.hashCode()) % 2 == 0) ? R.drawable.tenchan : R.drawable.duke;
        iv.setImageResource(d);
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
    }

    /**
     * 日付文字列（yyyy/MM/dd）から黄道十二宮の星座名を示す文字列を返す
     */
    private String getZodiac(String birthday) {

        final int m, d;
        String dateElements[] = birthday.split("/");

        if (dateElements.length == 3) {
            m = 1;
            d = 2;
        } else if (dateElements.length == 2) {
            m = 0;
            d = 1;
        } else return "";

        int month = Integer.valueOf(dateElements[m]);
        int day = Integer.valueOf(dateElements[d]);
        String zodiac = "";

        switch (month) {
            case 1:
                zodiac = (day >= 1 && day <= 19) ? "山羊座" : "水瓶座";
                break;
            case 2:
                zodiac = (day >= 1 && day <= 18) ? "水瓶座" : "魚座";
                break;
            case 3:
                zodiac = (day >= 1 && day <= 20) ? "魚座" : "牡羊座";
                break;
            case 4:
                zodiac = (day >= 1 && day <= 19) ? "牡羊座" : "牡牛座";
                break;
            case 5:
                zodiac = (day >= 1 && day <= 20) ? "牡牛座" : "双子座";
                break;
            case 6:
                zodiac = (day >= 1 && day <= 21) ? "双子座" : "蟹座";
                break;
            case 7:
                zodiac = (day >= 1 && day <= 22) ? "蟹座" : "獅子座";
                break;
            case 8:
                zodiac = (day >= 1 && day <= 22) ? "獅子座" : "乙女座";
                break;
            case 9:
                zodiac = (day >= 1 && day <= 22) ? "乙女座" : "天秤座";
                break;
            case 10:
                zodiac = (day >= 1 && day <= 23) ? "天秤座" : "蠍座";
                break;
            case 11:
                zodiac = (day >= 1 && day <= 22) ? "蠍座" : "射手座";
                break;
            case 12:
                zodiac = (day >= 1 && day <= 22) ? "射手座" : "山羊座";
            default:
                break;
        } // …ふぅ。
        return zodiac;
    }

    /**
     * 星占いAPIを（非同期的に）叩いて、今日の占いを取得し、ユーザーの星座のぶんを適宜ビューに貼り付ける
     */
    private class Astrologist extends AsyncTask<String, String, String> {

        private String zodiac;
        // 本日日付をyyyy/MM/dd書式で保持→API呼出し時にURLの一部として使います。
        private String today = new SimpleDateFormat("yyyy/MM/dd").format(new Date());

        public Astrologist(String zodiac) {
            this.zodiac = zodiac;
        }

        /**
         * バックグラウンドスレッドで、HTTP通信を行い、応答データを取得して文字列として返す。
         * （この戻り値は、次工程たるonPostExecute()の引数としてわたることになっている）
         *
         * @param string
         * @return
         */
        @Override
        protected String doInBackground(String... string) {
            String baseUrl = getString(R.string.base_url);

            StringBuilder rawResult = new StringBuilder();

            try {
                URL url = new URL(baseUrl + today);
                HttpURLConnection con = (HttpURLConnection) url.openConnection();
                con.setRequestMethod("GET");
                con.connect();
                final int responseCode = con.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {

                    BufferedReader br =
                            new BufferedReader(new InputStreamReader(con.getInputStream()));

                    String line;
                    while ((line = br.readLine()) != null) {
                        rawResult.append(line);
                    }
                }
            } catch (Exception e) { // 雑ゥ。正確には、IOExceptionとMalformedURLExceptionが起こりえます。
                e.printStackTrace();
            }
            return rawResult.toString();
        }

        /**
         * doInBackgroundの仕事が終わったらUIスレッドで呼び出される。
         * 文字列をいったんJSONに変換してから、適切な項目をUIに貼り付ける
         *
         * @param result
         */
        @Override
        protected void onPostExecute(String result) {
            try {

                String content = "";
                JSONObject jsonRoot = new JSONObject(result);
                JSONArray fortunes = jsonRoot.getJSONObject("horoscope").getJSONArray(today);

                for (int i = 0; i < fortunes.length(); i++) {
                    JSONObject o = fortunes.getJSONObject(i);
                    // すべての星座ぶんの占いが配列で返ってくるので、ユーザーの生まれ星座で突き合わせて、…
                    if (this.zodiac.equals(o.getString("sign"))) {
                        // contentは長文のアドバイスのようなので、フキダシに詰めます。
                        content = o.getString("content");
                        TextView tvMsg = findViewById(R.id.tvMessage);
                        tvMsg.setText(content);

                        // テーブルレイアウト中の対応する項目に結果を流し込みます：
                        TextView tvRanking = findViewById(R.id.tvRanking);
                        tvRanking.setText(o.getString("rank") + "/12位");
                        TextView tvTotal = findViewById(R.id.tvTotal);
                        tvTotal.setText(o.getString("total"));
                        TextView tvBiz = findViewById(R.id.tvBiz);
                        tvBiz.setText(o.getString("job"));
                        TextView tvMoney = findViewById(R.id.tvMoney);
                        tvMoney.setText(o.getString("money"));
                        TextView tvLove = findViewById(R.id.tvLove);
                        tvLove.setText(o.getString("love"));
                        TextView tvItem = findViewById(R.id.tvItem);
                        tvItem.setText(o.getString("item"));
                        TextView tvColor = findViewById(R.id.tvColor);
                        tvColor.setText(o.getString("color"));

                        // ランキング上位の人にはもふ猫さん、下位の人には黒猫さんがメッセージを伝えます…。
                        ImageView iv = findViewById(R.id.imageView);
                        iv.setImageResource(
                                (Integer.valueOf(o.getString("rank")) >= 7) ?
                                        R.drawable.tenchan :
                                        R.drawable.duke);
                        break;
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }
}
