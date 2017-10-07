package kr.re.DDil.Graph;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

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
import java.util.ArrayList;

import kr.re.DDil.BluetoothEcho.R;
import kr.re.DDil.CarePillow.UserSetting;
import kr.re.DDil.DataBase.LuxDB;
import kr.re.DDil.DataBase.Start_UserDB;

/**
 * Created by DDil on 2017-02-02.
 */

public class BreathGraph extends Activity {
    LuxDB luxDB;
    Start_UserDB start_userDB;
    SQLiteDatabase sql;
    Button DayBreathDataButton, WeekBreathDataButton, MountBreathDataButton;
    private String Final_UserID, Final_UserName, Final_UserAge, Final_UserGender, Final_UserWeight, Final_PillowAir;

    LineDataSet setComp1;   //MPAndroidChart

    ArrayList<Entry> valsComp1 = new ArrayList<Entry>();
    ArrayList<String> xVals = new ArrayList<String>();

    protected void onCreate(Bundle savedInstanceState) {    //전반적인 소스코드가 LuxGraph와 유사함. 해당 클래스 주석 참고
        luxDB = new LuxDB(this);
        start_userDB = new Start_UserDB(this);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.graph_breath);
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

        DayBreathDataButton = (Button)findViewById(R.id.button);
        DayBreathDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                valsComp1.clear();
                xVals.clear();

                sql = luxDB.getReadableDatabase();
                Cursor cursor;
                cursor = sql.rawQuery("SELECT strftime(\"%H\",member3.Input_date), IFNULL(AVG(RESULT.breath_val),0) " +
                        "FROM Member3 LEFT OUTER JOIN (SELECT member.Input_date, member.breath_val FROM MEMBER WHERE user_id = '" + Final_UserID + "' AND name = '" + Final_UserName + "') AS RESULT ON strftime(\"%Y/%m/%d %H:00\",RESULT.Input_date) = strftime(\"%Y/%m/%d %H:00\",member3.Input_date) " +
                        "GROUP BY strftime(\"%Y/%m/%d %H\",member3.Input_date)", null);

                while (cursor.moveToNext()) {
                    valsComp1.add(new Entry(Float.parseFloat(cursor.getString(1)), Integer.parseInt(cursor.getString(0))));
                    xVals.add("" + cursor.getString(0));
                }

                cursor.close();
                sql.close();
                setComp1 = new LineDataSet(valsComp1, "호흡 수");
                setComp1.setValueFormatter(new MyYValueFormatter());
                //setComp1.setDrawValues(false);
                setComp1.setDrawCubic(true);
                setComp1.setDrawFilled(true);
                setComp1.setAxisDependency(YAxis.AxisDependency.LEFT);
                setComp1.setColor(Color.MAGENTA);
                setComp1.setFillColor(Color.MAGENTA);
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
                xAxis.setValueFormatter(new MyXAxisValueFormatter_Day());
                YAxis yAxis = chart.getAxisLeft();
                yAxis.setValueFormatter(new MyYAxisValueFormatter());

            }
        });

        WeekBreathDataButton = (Button)findViewById(R.id.button7);
        WeekBreathDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                valsComp1.clear();
                xVals.clear();

                sql = luxDB.getReadableDatabase();
                Cursor cursor;
                cursor = sql.rawQuery("SELECT strftime(\"%Y/%m/%d\",member2.Input_date) , IFNULL(AVG(Result.breath_val),0) "
                        + "FROM Member2 LEFT OUTER JOIN (SELECT member.Input_date, member.breath_val FROM MEMBER WHERE user_id = '" + Final_UserID + "' AND name = '" + Final_UserName + "') AS RESULT ON strftime(\"%Y/%m/%d\", RESULT.Input_date) = strftime(\"%Y/%m/%d\", member2.Input_date) "
                        + "GROUP BY strftime(\"%Y/%m/%d\", member2.Input_date) "
                        + "HAVING member2.Input_date >= date('now','weekday 0', '-7 days', 'localtime') AND member2.Input_date <= date('now','weekday 0', '-1 days', 'localtime') "
                        + "ORDER BY MEMBER2.Input_date;", null);

                int CheckNumberData = 0;

                while (cursor.moveToNext()) {
                    valsComp1.add(new Entry(Float.parseFloat(cursor.getString(1)), CheckNumberData));
                    CheckNumberData++;
                    xVals.add("" + CheckNumberData);
                }

                cursor.close();
                sql.close();
                setComp1 = new LineDataSet(valsComp1, "호흡 수");
                setComp1.setValueFormatter(new MyYValueFormatter());
                //setComp1.setDrawValues(false);
                setComp1.setDrawCubic(true);
                setComp1.setDrawFilled(true);
                setComp1.setAxisDependency(YAxis.AxisDependency.LEFT);
                setComp1.setColor(Color.MAGENTA);
                setComp1.setFillColor(Color.MAGENTA);
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

            }
        });

        MountBreathDataButton = (Button)findViewById(R.id.button8);
        MountBreathDataButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                valsComp1.clear();
                xVals.clear();

                sql = luxDB.getReadableDatabase();
                Cursor cursor;
                cursor = sql.rawQuery("SELECT strftime(\"%Y/%m/%d\", member2.Input_date), IFNULL(AVG(Result.breath_val),0) "
                        + "FROM Member2 LEFT OUTER JOIN (SELECT member.Input_date, member.breath_val FROM MEMBER WHERE user_id = '" + Final_UserID + "' AND name = '" + Final_UserName + "') AS RESULT ON strftime(\"%Y/%m/%d\", RESULT.Input_date) = strftime(\"%Y/%m/%d\", member2.Input_date) "
                        + "GROUP BY strftime(\"%Y/%m/%d\",member2.Input_date) "
                        + "HAVING member2.Input_date >= date('now','start of month','localtime') AND member2.Input_date <= date('now','start of month','+1 month','-1 day','localtime') "
                        + "ORDER BY MEMBER2.Input_date;", null);

                int CheckNumberData = 0;

                while (cursor.moveToNext()) {
                    valsComp1.add(new Entry(Float.parseFloat(cursor.getString(1)), CheckNumberData));

                    int CheckNumberDate = CheckNumberData +1;
                    xVals.add("" + CheckNumberDate);
                    CheckNumberData++;
                }

                cursor.close();
                sql.close();

                setComp1 = new LineDataSet(valsComp1, "호흡 수");
                setComp1.setValueFormatter(new MyYValueFormatter());
                //setComp1.setDrawValues(false);
                setComp1.setDrawCubic(true);
                setComp1.setDrawFilled(true);
                setComp1.setAxisDependency(YAxis.AxisDependency.LEFT);
                setComp1.setColor(Color.MAGENTA);
                setComp1.setFillColor(Color.MAGENTA);
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
            }
        });

        System.gc();
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

    public class MyYAxisValueFormatter implements YAxisValueFormatter { //Y축 포맷 설정

        private DecimalFormat mFormat;

        public MyYAxisValueFormatter() {
            mFormat = new DecimalFormat("###,###,##0"); // use one decimal
        }

        @Override
        public String getFormattedValue(float value, YAxis yAxis) {
            // write your logic here
            // access the YAxis object to get more information
            return mFormat.format(value) + "회"; // e.g. append a dollar-sign
        }
    }
}
