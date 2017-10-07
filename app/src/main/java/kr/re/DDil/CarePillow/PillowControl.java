package kr.re.DDil.CarePillow;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import kr.re.DDil.BluetoothEcho.R;
import kr.re.DDil.DataBase.Start_UserDB;

import static kr.re.DDil.BluetoothEcho.R.id.radioButton;

/**
 * Created by DDil on 2017-02-03.
 */

public class PillowControl extends Activity {
    MainActivity mainActivity;
    SQLiteDatabase sql;
    Start_UserDB start_userDB;

    private String Final_UserID, Final_UserName, Final_UserAge, Final_UserGender, Final_UserWeight, Final_PillowAir;
    Button AIR_PLUS, AIR_MINER, AIR_STOP, LED_ON, LED_OFF, LED_PLUS, LED_MINER, LED_AUTO, LED_PASS;
    RadioGroup radioGroup;
    RadioButton radioButton1, radioButton2, radioButton3, radioButton4;
    Integer Select_Airbag;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.pillow_control);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        mainActivity = new MainActivity();          //sendStringData를 사용하기 위해 main을 선언
        start_userDB = new Start_UserDB(this);

        Select_Airbag = 1;      //에어백 초기화

        sql = start_userDB.getReadableDatabase();
        Cursor cursor;
        cursor = sql.rawQuery("SELECT * FROM MEMBER;", null);
        if (cursor != null && cursor.getCount() != 0) {

            //cursor.moveToNext는 SQL쿼리문을 실행 한 뒤에 한칸씩 내려가는 명령. 참고 [http://arabiannight.tistory.com/entry/368]
            while (cursor.moveToNext()) {
                Final_UserID = cursor.getString(0);
                Final_UserName = cursor.getString(1);
                Final_UserAge = cursor.getString(2);
                Final_UserGender = cursor.getString(3);
                Final_UserWeight = cursor.getString(4);
            }
        }
        sql.close();
        cursor.close();

        //베개 1번부터 4번까지 radiobutton을 group으로 묶음
        radioGroup = (RadioGroup) findViewById(R.id.RadioGroup);
        /*
        radioButton1 = (RadioButton) findViewById(R.id.radioButton);
        radioButton2 = (RadioButton) findViewById(R.id.radioButton2);
        radioButton3 = (RadioButton) findViewById(R.id.radioButton3);
        radioButton4 = (RadioButton) findViewById(R.id.radioButton4);
*/

        //라디오버튼 그룹에서 각 버튼들이 클릭 될 시
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radioButton:
                        Select_Airbag = 1;
                        break;
                    case R.id.radioButton2:
                        Select_Airbag = 2;
                        break;
                    case R.id.radioButton3:
                        Select_Airbag = 3;
                        break;
                    case R.id.radioButton4:
                        Select_Airbag = 4;
                        break;
                }
            }
        });


        //에어백에 공기 넣기 선택 시 선택된 에어백에 따라 다른 값을 아트메가로 보낸다.
        AIR_PLUS = (Button) findViewById(R.id.button26);
        AIR_PLUS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (Select_Airbag)
                {
                    case 1:
                        sendStringData("0");
                        break;
                    case 2:
                        sendStringData("1");
                        break;
                    case 3:
                        sendStringData("2");
                        break;
                    case 4:
                        sendStringData("3");
                        break;
                }
            }
        });

        //에어백 중단 선택 시
        AIR_STOP = (Button) findViewById(R.id.button27);
        AIR_STOP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendStringData("4");
            }
        });

        //에어백 공기 빼기 선택 시
        AIR_MINER = (Button) findViewById(R.id.button28);
        AIR_MINER.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (Select_Airbag)
                {
                    case 1:
                        sendStringData("5");
                        break;
                    case 2:
                        sendStringData("6");
                        break;
                    case 3:
                        sendStringData("7");
                        break;
                    case 4:
                        sendStringData("8");
                        break;
                }
            }
        });

        //LED 켜고 끄기 버튼들
        LED_ON = (Button) findViewById(R.id.button30);
        LED_ON.setEnabled(false);
        LED_ON.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendStringData("N");
            }
        });

        LED_OFF = (Button) findViewById(R.id.button29);
        LED_OFF.setEnabled(false);
        LED_OFF.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendStringData("F");
            }
        });

        LED_PLUS = (Button) findViewById(R.id.button32);
        LED_PLUS.setEnabled(false);
        LED_PLUS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendStringData("U");
            }
        });

        LED_MINER = (Button) findViewById(R.id.button31);
        LED_MINER.setEnabled(false);
        LED_MINER.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendStringData("D");
            }
        });

        LED_AUTO = (Button)findViewById(R.id.button3);
        LED_AUTO.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendStringData("B");
                LED_ON.setEnabled(false);
                LED_OFF.setEnabled(false);
                LED_PLUS.setEnabled(false);
                LED_MINER.setEnabled(false);
            }
        });

        LED_PASS = (Button)findViewById(R.id.button33);
        LED_PASS.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LED_ON.setEnabled(true);
                LED_OFF.setEnabled(true);
                LED_PLUS.setEnabled(true);
                LED_MINER.setEnabled(true);
            }
        });

    }

    //Main 으로 돌아가는 버튼 선택 시 값을 넘겨준다.
    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // NavUtils.navigateUpFromSameTask(this);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //아트메가로 데이터를 보내기 위해  메인의 sendStringData를 가져옴.
    public void sendStringData(String data) {
        data += '\0';
        byte[] buffer = data.getBytes();

        if (mainActivity.mBTHandler.write(buffer)) {
        }

    }
}
