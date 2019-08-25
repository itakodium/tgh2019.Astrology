package events.tgh2019.androidlab.astrology;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;

import java.text.SimpleDateFormat;
import java.util.Calendar;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 計算処理の対象になりうる日付データをユーザーに入力させるのは危険、という理由で、
        // DatePickerというビューを表示して強制的に選ばせる方法がとられます。
        // 以下は、DatePickerDialogという簡易的な日付選択機能を使った実装例です。
        final EditText etBirthday = findViewById(R.id.etBirthday);
        final Calendar calendar = Calendar.getInstance();
        final DatePickerDialog.OnDateSetListener dialog = new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, month);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                SimpleDateFormat fmt = new SimpleDateFormat("yyyy/MM/dd");
                etBirthday.setText(fmt.format(calendar.getTime()));
            }
        };

        etBirthday.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(
                        MainActivity.this,
                        dialog,
                        calendar.get(Calendar.YEAR ) ,
                        calendar.get(Calendar.MONTH) ,
                        calendar.get(Calendar.DAY_OF_MONTH)).show();

            }
        });

        Button b = findViewById(R.id.theButton);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EditText etName = findViewById(R.id.etName);
                String name = etName.getText().toString();
                EditText etBirthday = findViewById(R.id.etBirthday);
                String birthday = etBirthday.getText().toString();

                if (etName.getText().toString().isEmpty()){
                    etName.setError("Please type your name here");
                }
                // ユーザーが入力した名前と誕生日を次画面(ResultActivity)に丸投げする
                Intent intent = new Intent(MainActivity.this, ResultActivity.class);
                intent.putExtra("NAME", name);
                intent.putExtra("BIRTHDAY", birthday);

                startActivity(intent);
            }
        });

    }
}
