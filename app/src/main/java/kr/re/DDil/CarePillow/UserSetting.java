package kr.re.DDil.CarePillow;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import kr.re.DDil.BluetoothEcho.R;
import kr.re.DDil.DataBase.LuxDB;
import kr.re.DDil.DataBase.Start_UserDB;
import kr.re.DDil.DataBase.myDB;

/**
 * Created by inthetech on 2017-01-06.
 */

public class UserSetting extends Activity {

    //DB 선언
    myDB my;
    LuxDB luxDB;
    Start_UserDB start_userDB;
    SQLiteDatabase sql;

    //데이터를 리셋
    Button UserDataResetButton;

    //유저 리스트 다이어로그
    public ArrayList<String> mUserNameArrayList = new ArrayList<String>();
    public AlertDialog mUserListDialog;

    private Menu mMenu;

    //유저를 추가할 시 새로 입력받는 유저 데이터
    EditText UserName_EditText;
    EditText UserAge_EditText;
    EditText UserGender_EditText;
    EditText UserWeight_EditText;

    TextView muser_setting_userID;
    TextView muser_setting_userName;
    TextView muser_setting_userAge;
    TextView muser_setting_userGender;
    TextView muser_setting_userWeight;

    private String Final_UserID, Final_UserName, Final_UserAge, Final_UserGender, Final_UserWeight, Final_PillowAir;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        String currentDateTimeString = DateFormat.getDateInstance().format(new Date());
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user_setting);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        //DB선언
        my = new myDB(this);
        start_userDB = new Start_UserDB(this);

        muser_setting_userID = (TextView) findViewById(R.id.user_setting_userID);
        muser_setting_userName = (TextView) findViewById(R.id.user_setting_userName);
        muser_setting_userAge = (TextView) findViewById(R.id.user_setting_userAge);
        muser_setting_userGender = (TextView) findViewById(R.id.user_setting_userGender);
        muser_setting_userWeight = (TextView) findViewById(R.id.user_setting_userWeight);

        //건내 받은 값을 UI에 표시하기위에 가져옴 (없다면 초기값인 Guest가, 유저를 선택한다면 DB에 저장된 값이)
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

        //저장된 유저 데이터를 토대로 UI 변경
        muser_setting_userID.setText(Final_UserID);
        muser_setting_userName.setText(Final_UserName);
        muser_setting_userAge.setText(Final_UserAge);
        muser_setting_userGender.setText(Final_UserGender);
        muser_setting_userWeight.setText(Final_UserWeight);

        //유저 데이터 리셋 버튼을 눌렀을 때
        UserDataResetButton = (Button) findViewById(R.id.button20);
        UserDataResetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alert_confirm = new AlertDialog.Builder(UserSetting.this);

                alert_confirm.setMessage("정말로 삭제 하시겠습니까?").setCancelable(false).setNegativeButton("취소",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 'NO'
                                Toast.makeText(getApplicationContext(), "취소되었습니다.", Toast.LENGTH_SHORT).show();
                            }
                        }).setPositiveButton("확인",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 'YES'
                                sql = luxDB.getWritableDatabase();
                                luxDB.onUpgrade(sql, 1, 2);
                                sql.close();

                                //member2를 초기화 하고 데이터를 집어넣음
                                SimpleDateFormat fm1 = new SimpleDateFormat("yyyy-MM");
                                String date = fm1.format(new Date());
                                Calendar calendar = Calendar.getInstance();
                                sql = luxDB.getWritableDatabase();
                                sql.execSQL("DROP TABLE IF EXISTS member2");
                                sql.execSQL("create table member2 (_id INTEGER PRIMARY KEY AUTOINCREMENT, lux_val char(20), breath_val char(20), snore_val char(20), Input_date datetime(10))");
                                for (int i = 1; i <= calendar.getActualMaximum(Calendar.DATE); i++) {
                                    if (i < 10) {
                                        sql.execSQL("INSERT INTO member2 VALUES(null,0,0,0,'"
                                                + date + "-0" + i + "');"
                                        );
                                    } else {
                                        sql.execSQL("INSERT INTO member2 VALUES(null,0,0,0,'"
                                                + date + "-" + i + "');"
                                        );
                                    }
                                }

                                //member3를 초기화 하고 데이터를 집어넣음
                                SimpleDateFormat fm2 = new SimpleDateFormat("yyyy-MM-dd ");
                                String date2 = fm2.format(new Date());

                                sql = luxDB.getWritableDatabase();
                                sql.execSQL("DROP TABLE IF EXISTS member3");
                                sql.execSQL("create table member3 (_id INTEGER PRIMARY KEY AUTOINCREMENT, lux_val char(20), breath_val char(20), snore_val char(20), Input_date datetime(10))");
                                for (int i = 0; i < 24; i++) {
                                    if (i < 10) {
                                        sql.execSQL("INSERT INTO member3 VALUES(null,0,0,0,'"
                                                + date2 + " 0" + i + ":00');"
                                        );
                                    } else {
                                        sql.execSQL("INSERT INTO member3 VALUES(null,0,0,0,'"
                                                + date2 + " " + i + ":00');"
                                        );
                                    }
                                }
                                Toast.makeText(getApplicationContext(), "삭제 완료", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        });
                AlertDialog alert = alert_confirm.create();
                alert.show();
            }
        });

        System.gc();//가비지컬렉터
        System.gc();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {             //메뉴 활성화
        getMenuInflater().inflate(R.menu.user_setting_menu, menu);
        mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(android.view.MenuItem item) {      //메뉴 선택시
        switch (item.getItemId()) {
            case R.id.UserSetting2:         //유저 선택시
                initUserListDialog();
                mUserListDialog.show();
                break;
            case android.R.id.home:         //Main으로 돌아갈 시
                // NavUtils.navigateUpFromSameTask(this);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public void addUserOnClick(View v) {        //새로운 유저를 추가할 때
        switch (v.getId()) {
            case R.id.user_setting_useraddButton:

                LayoutInflater inflater = getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.user_add_dialog, null);

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("새로운 유저 추가");
                builder.setView(dialogView);
                builder.setPositiveButton("추가하기", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        UserName_EditText = (EditText) dialogView.findViewById(R.id.editText);
                        UserAge_EditText = (EditText) dialogView.findViewById(R.id.editText3);
                        UserGender_EditText = (EditText) dialogView.findViewById(R.id.editText4);
                        UserWeight_EditText = (EditText) dialogView.findViewById(R.id.editText5);

                        sql = my.getWritableDatabase();
                        sql.execSQL("INSERT INTO member VALUES(null,'"
                                + UserName_EditText.getText().toString() + "','"
                                + UserAge_EditText.getText().toString() + "','"
                                + UserGender_EditText.getText().toString() + "','"
                                + UserWeight_EditText.getText().toString() + "');"
                        );
                        sql.close();


                        //String name = UserName_EditText.getText().toString();
                        Toast.makeText(getApplicationContext(), "Save Complete", Toast.LENGTH_SHORT).show();
                    }
                });

                AlertDialog dialog = builder.create();

                dialog.show();

                break;
            case R.id.user_setting_userclearButton:             //유저 목록을 초기화 할 시
                AlertDialog.Builder alert_confirm = new AlertDialog.Builder(UserSetting.this);

                alert_confirm.setMessage("정말로 삭제 하시겠습니까?").setCancelable(false).setNegativeButton("취소",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 'NO'
                                Toast.makeText(getApplicationContext(), "취소되었습니다.", Toast.LENGTH_SHORT).show();
                            }
                        }).setPositiveButton("확인",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // 'YES'
                                sql = my.getWritableDatabase();
                                my.onUpgrade(sql, 1, 2);
                                sql.close();
                                Toast.makeText(getApplicationContext(), "사용자 목록이 초기화 되었습니다.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        });
                AlertDialog alert = alert_confirm.create();
                alert.show();
                break;
        }
    }

    public void initUserListDialog() {  //유저 리스트 불러오기
        mUserNameArrayList.clear();
        mUserNameArrayList.add("0,Guest,-,-,-,--:--");

        sql = my.getReadableDatabase();
        Cursor cursor;
        cursor = sql.rawQuery("SELECT * FROM MEMBER;", null);


        while (cursor.moveToNext()) {
            mUserNameArrayList.add(cursor.getString(0) + "," + cursor.getString(1) + "," + cursor.getString(2) + "," + cursor.getString(3) + "," + cursor.getString(4));
        }

        final String[] items = mUserNameArrayList.toArray(new String[mUserNameArrayList.size()]);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("유저를 선택하세요");
        //유저를 선택하면 선택한 값을 토대로 앱 재실행시 사용자가 자동으로 연결되게 DB에 저장함.
        builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String[] str = new String(items[which]).split(",");

                Final_UserID = str[0];
                Final_UserName = str[1];
                Final_UserAge = str[2];
                Final_UserGender = str[3];
                Final_UserWeight = str[4];
                Toast.makeText(getApplicationContext(), str[0] + "," + str[1], Toast.LENGTH_SHORT).show();

                sql = start_userDB.getWritableDatabase();
                start_userDB.onUpgrade(sql, 1, 2);
                sql.execSQL("INSERT INTO member VALUES('"
                        + Final_UserID + "','"
                        + Final_UserName + "','"
                        + Final_UserAge + "','"
                        + Final_UserGender + "','"
                        + Final_UserWeight + "');"
                );
                sql.close();

                //받아온 값들을 토대로 UI변경
                muser_setting_userID.setText(Final_UserID);
                muser_setting_userName.setText(Final_UserName);
                muser_setting_userAge.setText(Final_UserAge);
                muser_setting_userGender.setText(Final_UserGender);
                muser_setting_userWeight.setText(Final_UserWeight);
            }
        });
        //유저를 선택하지않고 취소를 누르면 Dialog 창 종료.
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        mUserListDialog = builder.create();
        mUserListDialog.setCanceledOnTouchOutside(false);
    }
}
