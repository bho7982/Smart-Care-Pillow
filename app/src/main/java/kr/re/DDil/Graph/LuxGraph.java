package kr.re.DDil.Graph;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.formatter.XAxisValueFormatter;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import kr.re.DDil.BluetoothEcho.R;
import kr.re.DDil.CarePillow.UserSetting;
import kr.re.DDil.DataBase.LuxDB;
import kr.re.DDil.DataBase.Start_UserDB;

/**
 * Created by inthetech on 2017-01-09.
 */

public class LuxGraph extends Activity {
    LuxDB luxDB;
    Start_UserDB start_userDB;
    SQLiteDatabase sql;
    Button DayLuxDataButton, WeekLuxDataButton, MountLuxDataButton;
    private String Final_UserID, Final_UserName, Final_UserAge, Final_UserGender, Final_UserWeight;

    LineDataSet setComp1;   //MPAndroidChart

    //그래프를 그리기 위한 리스트들 (Y값, X값)
    ArrayList<Entry> valsComp1 = new ArrayList<Entry>();
    ArrayList<String> xVals = new ArrayList<String>();

    protected void onCreate(Bundle savedInstanceState) {
        luxDB = new LuxDB(this);
        start_userDB = new Start_UserDB(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.graph_lux);
        getActionBar().setDisplayHomeAsUpEnabled(true);

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

        //일일 조도값 버튼
        DayLuxDataButton = (Button) findViewById(R.id.Day_Luxdata_Button);
        DayLuxDataButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                //valsComp1는 데이터를 그래프에 추가하기 위한 리스트
                valsComp1.clear();
                xVals.clear();

                sql = luxDB.getReadableDatabase();
                Cursor cursor;
                cursor = sql.rawQuery("SELECT strftime(\"%H\",member3.Input_date), IFNULL(AVG(RESULT.lux_val),0) " +
                        "FROM Member3 LEFT OUTER JOIN (SELECT member.Input_date, member.lux_val FROM MEMBER WHERE user_id = '" + Final_UserID + "' AND name = '" + Final_UserName + "') AS RESULT ON strftime(\"%Y/%m/%d %H:00\",RESULT.Input_date) = strftime(\"%Y/%m/%d %H:00\",member3.Input_date) " +
                        "GROUP BY strftime(\"%Y/%m/%d %H\",member3.Input_date)", null);

                String luxvalue = "DB" + "\r\n";

                while (cursor.moveToNext()) {
                    valsComp1.add(new Entry(Float.parseFloat(cursor.getString(1)), Integer.parseInt(cursor.getString(0))));
                    xVals.add("" + cursor.getString(0));

                    luxvalue += cursor.getString(0) + " " + cursor.getString(1) + "\r\n";
                }

                cursor.close();
                sql.close();

                //값을 그래프에 추가하는 부분. 리스트 valsComp가 setComp에 들어간다.
                setComp1 = new LineDataSet(valsComp1, "조도 값");  //리스트 valsComp1 값이 그래프에 들어감. x축에 들어갈 데이터 이름 설정
                setComp1.setValueFormatter(new MyYValueFormatter());    //그래프 Y 값 표시 형식은 MyYValueFormatter함수를 따른다.
                //setComp1.setDrawValues(false);
                setComp1.setDrawCubic(true);    //그래프를 부드럽게 만든다.
                setComp1.setDrawFilled(true);   //그래프 선 아래를 색으로 채운다.
                setComp1.setAxisDependency(YAxis.AxisDependency.LEFT);
                //setComp1.setColor(Color.RED);             //그래프 선 색
                //setComp1.setFillColor(Color.MAGENTA);     //그래프 선 아래 색
                setComp1.setDrawCircles(false);

                ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();       //최종적으로 그래프에 추가할 dataSets 설정
                dataSets.add(setComp1);                                     //위에서 적용한 그래프 모양 + 데이터를 dataSets에 넣음

                LineData data = new LineData(xVals, dataSets);
                LineChart chart = (LineChart) findViewById(R.id.chart);

                chart.setData(data);
                chart.getAxisRight().setDrawLabels(false);
                chart.invalidate();
                chart.setDescription("");

                XAxis xAxis = chart.getXAxis();
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setValueFormatter(new MyXAxisValueFormatter_Day());       //X축 값 형식은 MyXAxisValueFormatter_Day함수를 따른다.

                YAxis yAxis = chart.getAxisLeft();
                yAxis.setValueFormatter(new MyYAxisValueFormatter());           //Y축 값 형식은 MyYAxisValueFormatter함수를 따른다.

                Log.v("Day버튼", "눌림");
            }
        });

        WeekLuxDataButton = (Button) findViewById(R.id.Week_Luxdata_Button);
        WeekLuxDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                valsComp1.clear();
                xVals.clear();

                sql = luxDB.getReadableDatabase();
                Cursor cursor;
                cursor = sql.rawQuery("SELECT strftime(\"%Y/%m/%d\",member2.Input_date) , IFNULL(AVG(Result.lux_val),0) "
                        + "FROM Member2 LEFT OUTER JOIN (SELECT member.Input_date, member.lux_val FROM MEMBER WHERE user_id = '" + Final_UserID + "' AND name = '" + Final_UserName + "') AS RESULT ON strftime(\"%Y/%m/%d\", RESULT.Input_date) = strftime(\"%Y/%m/%d\", member2.Input_date) "
                        + "GROUP BY strftime(\"%Y/%m/%d\", member2.Input_date) "
                        + "HAVING member2.Input_date >= date('now','weekday 0', '-7 days', 'localtime') AND member2.Input_date <= date('now','weekday 0', '-1 days', 'localtime') "
                        + "ORDER BY MEMBER2.Input_date;", null);

                int CheckNumberData = 0;

                String luxvalue = "DB" + "\r\n";

                while (cursor.moveToNext()) {
                    valsComp1.add(new Entry(Float.parseFloat(cursor.getString(1)), CheckNumberData));
                    CheckNumberData++;
                    xVals.add("" + CheckNumberData);

                    luxvalue += cursor.getString(0) + " " + cursor.getString(1) + "\r\n";
                }

                cursor.close();
                sql.close();
                setComp1 = new LineDataSet(valsComp1, "조도 값");
                setComp1.setValueFormatter(new MyYValueFormatter());
                //setComp1.setDrawValues(false);
                setComp1.setDrawCubic(true);
                setComp1.setDrawFilled(true);
                setComp1.setAxisDependency(YAxis.AxisDependency.LEFT);
                //setComp1.setColor(Color.RED);
                //setComp1.setFillColor(Color.MAGENTA);
                setComp1.setDrawCircles(false);
                ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
                dataSets.add(setComp1);

                LineData data = new LineData(xVals, dataSets);
                LineChart chart = (LineChart) findViewById(R.id.chart);

                chart.setData(data);
                chart.getAxisRight().setDrawLabels(false);
                chart.invalidate();
                chart.setDescription("");
                XAxis xAxis = chart.getXAxis();
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setValueFormatter(new MyXAxisValueFormatter_Week());
                YAxis yAxis = chart.getAxisLeft();
                yAxis.setValueFormatter(new MyYAxisValueFormatter());
                Log.v("Week버튼", "눌림");

            }
        });

        MountLuxDataButton = (Button) findViewById(R.id.Mount_Luxdata_Button);
        MountLuxDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                valsComp1.clear();
                xVals.clear();

                sql = luxDB.getReadableDatabase();
                Cursor cursor;
                cursor = sql.rawQuery("SELECT strftime(\"%Y/%m/%d\", member2.Input_date), IFNULL(AVG(Result.lux_val),0) "
                        + "FROM Member2 LEFT OUTER JOIN (SELECT member.Input_date, member.lux_val FROM MEMBER WHERE user_id = '" + Final_UserID + "' AND name = '" + Final_UserName + "') AS RESULT ON strftime(\"%Y/%m/%d\", RESULT.Input_date) = strftime(\"%Y/%m/%d\", member2.Input_date) "
                        + "GROUP BY strftime(\"%Y/%m/%d\",member2.Input_date) "
                        + "HAVING member2.Input_date >= date('now','start of month','localtime') AND member2.Input_date <= date('now','start of month','+1 month','-1 day','localtime') "
                        + "ORDER BY MEMBER2.Input_date;", null);

                int CheckNumberData = 0;

                String luxvalue = "DB" + "\r\n";

                while (cursor.moveToNext()) {
                    valsComp1.add(new Entry(Float.parseFloat(cursor.getString(1)), CheckNumberData));

                    int CheckNumberDate = CheckNumberData +1;
                    xVals.add("" + CheckNumberDate);
                    CheckNumberData++;

                    luxvalue += cursor.getString(0) + " " + cursor.getString(1) + "\r\n";
                }

                cursor.close();
                sql.close();

                setComp1 = new LineDataSet(valsComp1, "조도 값");
                setComp1.setValueFormatter(new MyYValueFormatter());
                //setComp1.setDrawValues(false);
                setComp1.setDrawCubic(true);
                setComp1.setDrawFilled(true);
                setComp1.setAxisDependency(YAxis.AxisDependency.LEFT);
                //setComp1.setColor(Color.RED);
                //setComp1.setFillColor(Color.MAGENTA);
                setComp1.setDrawCircles(false);
                ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
                dataSets.add(setComp1);

                LineData data = new LineData(xVals, dataSets);
                LineChart chart = (LineChart) findViewById(R.id.chart);

                chart.setData(data);
                chart.getAxisRight().setDrawLabels(false);
                chart.invalidate();
                chart.setDescription("");
                XAxis xAxis = chart.getXAxis();
                xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                xAxis.setValueFormatter(new MyXAxisValueFormatter_Week());
                YAxis yAxis = chart.getAxisLeft();
                yAxis.setValueFormatter(new MyYAxisValueFormatter());
                Log.v("Mount버튼", "눌림");

            }
        });

        System.gc();        //가비지 컬렉터
        System.gc();
    }

    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // NavUtils.navigateUpFromSameTask(this);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class MyYAxisValueFormatter implements YAxisValueFormatter { //Y축 포맷 설정

        private DecimalFormat mFormat;

        public MyYAxisValueFormatter() {
            mFormat = new DecimalFormat("###,###,##0"); // use one decimal
        }

        @Override
        public String getFormattedValue(float value, YAxis yAxis) {
            // write your logic here
            // access the YAxis object to get more information
            return mFormat.format(value) + "Lux"; // e.g. append a dollar-sign
        }
    }

    public class MyYValueFormatter implements ValueFormatter {  //Y값 포맷 설정
        private DecimalFormat decimalFormat;

        public MyYValueFormatter() {
            decimalFormat = new DecimalFormat("###,###,##0");
        }

        @Override
        public String getFormattedValue(float v, Entry entry, int i, ViewPortHandler viewPortHandler) {
            return decimalFormat.format((int) v);
        }
    }

    public class MyXAxisValueFormatter_Day implements XAxisValueFormatter { //Y축 포맷 설정

        private DecimalFormat mFormat;

        public MyXAxisValueFormatter_Day() {
            mFormat = new DecimalFormat("###,###,##0"); // use one decimal
        }

        @Override
        public String getXValue(String s, int i, ViewPortHandler viewPortHandler) {
            return mFormat.format(i) + "시";
        }
    }

    public class MyXAxisValueFormatter_Week implements XAxisValueFormatter { //Y축 포맷 설정

        private DecimalFormat mFormat;

        public MyXAxisValueFormatter_Week() {
            mFormat = new DecimalFormat("###,###,##0"); // use one decimal
        }

        @Override
        public String getXValue(String s, int i, ViewPortHandler viewPortHandler) {
            return mFormat.format(i+ 1) + "일";
        }
    }
}