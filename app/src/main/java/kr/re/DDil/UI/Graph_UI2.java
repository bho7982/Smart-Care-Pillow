package kr.re.DDil.UI;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

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
import kr.re.DDil.Graph.BreathGraph;

/**
 * Created by inthetech on 2017-01-31.
 */

public class Graph_UI2 extends Activity {
    private String Final_UserID, Final_UserName, Final_UserAge, Final_UserGender, Final_UserWeight, Final_PillowAir;
    Button show_Day, show_Week, show_Mount;
    TextView show_Text;
    TextView AVG_Lux_TextView, AVG_Breath_TextView, AVG_Apnea_TextView, AVG_Snore_TextView;
    Integer Lux_AVG, Breath_AVG, Apnea_AVG, Snore_AVG;
    SQLiteDatabase sql;
    ArrayList<Entry> valsComp1 = new ArrayList<Entry>();
    ArrayList<Entry> valsComp2 = new ArrayList<Entry>();
    ArrayList<Entry> valsComp3 = new ArrayList<Entry>();
    ArrayList<Entry> valsComp4 = new ArrayList<Entry>();
    ArrayList<String> xVals = new ArrayList<String>();
    LuxDB luxDB;
    Start_UserDB start_userDB;
    LineDataSet setComp1;   //MPAndroidChart
    LineDataSet setComp2;   //MPAndroidChart
    LineDataSet setComp3;   //MPAndroidChart
    LineDataSet setComp4;   //MPAndroidChart




    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.graph_ui2);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        show_Text = (TextView) findViewById(R.id.textView7);
        luxDB = new LuxDB(this);
        start_userDB = new Start_UserDB(this);

        AVG_Lux_TextView = (TextView)findViewById(R.id.textView);
        AVG_Breath_TextView = (TextView)findViewById(R.id.textView2);
        AVG_Apnea_TextView = (TextView)findViewById(R.id.textView3);
        AVG_Snore_TextView = (TextView)findViewById(R.id.textView5);

        Lux_AVG = 0;
        Breath_AVG = 0;
        Apnea_AVG = 0;
        Snore_AVG = 0;

        sql = start_userDB.getReadableDatabase();
        Cursor cursor0;
        cursor0 = sql.rawQuery("SELECT * FROM MEMBER;", null);
        if (cursor0 != null && cursor0.getCount() != 0) {

            //cursor0.moveToNext는 SQL쿼리문을 실행 한 뒤에 한칸씩 내려가는 명령. 참고 [http://arabiannight.tistory.com/entry/368]
            while (cursor0.moveToNext()) {
                Final_UserID = cursor0.getString(0);
                Final_UserName = cursor0.getString(1);
                Final_UserAge = cursor0.getString(2);
                Final_UserGender = cursor0.getString(3);
                Final_UserWeight = cursor0.getString(4);
            }
        }
        sql.close();
        cursor0.close();

        valsComp1.clear();
        xVals.clear();

        sql = luxDB.getReadableDatabase();
        Cursor cursor;
        /////////////////////////////////////////
        cursor = sql.rawQuery("SELECT strftime(\"%H\",member3.Input_date), IFNULL(AVG(RESULT.lux_val),0) " +
                "FROM Member3 LEFT OUTER JOIN (SELECT member.Input_date, member.lux_val FROM MEMBER WHERE user_id = '" + Final_UserID + "' AND name = '" + Final_UserName + "') AS RESULT ON strftime(\"%Y/%m/%d %H:00\",RESULT.Input_date) = strftime(\"%Y/%m/%d %H:00\",member3.Input_date) " +
                "GROUP BY strftime(\"%Y/%m/%d %H\",member3.Input_date)", null);

        while (cursor.moveToNext()) {
            valsComp1.add(new Entry(Float.parseFloat(cursor.getString(1)), Integer.parseInt(cursor.getString(0))));
            xVals.add("" + cursor.getString(0));
            Lux_AVG += (int) Float.parseFloat(cursor.getString(1));
        }
        Lux_AVG = Lux_AVG/24;
        AVG_Lux_TextView.setText("평균 조도값\n" + Lux_AVG);
        cursor.close();

        //////////////////////////////////////////////
        cursor = sql.rawQuery("SELECT strftime(\"%H\",member3.Input_date), IFNULL(AVG(RESULT.breath_val),0) " +
                "FROM Member3 LEFT OUTER JOIN (SELECT member.Input_date, member.breath_val FROM MEMBER WHERE user_id = '" + Final_UserID + "' AND name = '" + Final_UserName + "') AS RESULT ON strftime(\"%Y/%m/%d %H:00\",RESULT.Input_date) = strftime(\"%Y/%m/%d %H:00\",member3.Input_date) " +
                "GROUP BY strftime(\"%Y/%m/%d %H\",member3.Input_date)", null);


        while (cursor.moveToNext()) {
            valsComp2.add(new Entry(Float.parseFloat(cursor.getString(1)), Integer.parseInt(cursor.getString(0))));
            Breath_AVG += (int) Float.parseFloat(cursor.getString(1));
        }
        Breath_AVG = Breath_AVG/24;
        AVG_Breath_TextView.setText("평균 호흡수\n"+Breath_AVG);
        cursor.close();


        /////////////////////////////////////////////////////
        cursor = sql.rawQuery("SELECT strftime(\"%H\",member3.Input_date), IFNULL(AVG(RESULT.snore_val),0) " +
                "FROM Member3 LEFT OUTER JOIN (SELECT member.Input_date, member.snore_val FROM MEMBER WHERE user_id = '" + Final_UserID + "' AND name = '" + Final_UserName + "') AS RESULT ON strftime(\"%Y/%m/%d %H:00\",RESULT.Input_date) = strftime(\"%Y/%m/%d %H:00\",member3.Input_date) " +
                "GROUP BY strftime(\"%Y/%m/%d %H\",member3.Input_date)", null);


        while (cursor.moveToNext()) {
            valsComp3.add(new Entry(Float.parseFloat(cursor.getString(1)), Integer.parseInt(cursor.getString(0))));
            Snore_AVG += (int) Float.parseFloat(cursor.getString(1));
        }
        Snore_AVG = Snore_AVG/24;
        AVG_Snore_TextView.setText("평균 코골이 횟수\n"+Snore_AVG);
        cursor.close();


        /////////////////////////////////////////////////////
        cursor = sql.rawQuery("SELECT strftime(\"%H\",member3.Input_date), IFNULL(AVG(RESULT.apnea_val),0) " +
                "FROM Member3 LEFT OUTER JOIN (SELECT member.Input_date, member.apnea_val FROM MEMBER WHERE user_id = '" + Final_UserID + "' AND name = '" + Final_UserName + "') AS RESULT ON strftime(\"%Y/%m/%d %H:00\",RESULT.Input_date) = strftime(\"%Y/%m/%d %H:00\",member3.Input_date) " +
                "GROUP BY strftime(\"%Y/%m/%d %H\",member3.Input_date)", null);


        while (cursor.moveToNext()) {
            valsComp4.add(new Entry(Float.parseFloat(cursor.getString(1)), Integer.parseInt(cursor.getString(0))));
            Apnea_AVG += (int) Float.parseFloat(cursor.getString(1));
        }
        Apnea_AVG = Apnea_AVG/24;
        AVG_Apnea_TextView.setText("평균 무호흡 횟수\n"+Apnea_AVG);
        cursor.close();


        /////////////////////////////////////////////////////
        sql.close();
        setComp1 = new LineDataSet(valsComp1, "조도 값");
        setComp1.setValueFormatter(new MyYValueFormatter());
        //setComp1.setDrawValues(false);
        setComp1.setAxisDependency(YAxis.AxisDependency.LEFT);
        setComp1.setDrawCircles(false);

        setComp2 = new LineDataSet(valsComp2, "호흡 수");
        setComp2.setValueFormatter(new MyYValueFormatter());
        //setComp1.setDrawValues(false);
        setComp2.setAxisDependency(YAxis.AxisDependency.LEFT);
        setComp2.setColor(Color.MAGENTA);
        setComp2.setFillColor(Color.MAGENTA);
        setComp2.setDrawCircles(false);

        setComp3 = new LineDataSet(valsComp3, "코곤 횟수");
        setComp3.setValueFormatter(new MyYValueFormatter());
        //setComp1.setDrawValues(false);
        setComp3.setAxisDependency(YAxis.AxisDependency.LEFT);
        setComp3.setColor(Color.DKGRAY);
        setComp3.setFillColor(Color.DKGRAY);
        setComp3.setDrawCircles(false);

        setComp4 = new LineDataSet(valsComp4, "무호흡 수");
        setComp4.setValueFormatter(new MyYValueFormatter());
        //setComp1.setDrawValues(false);
        setComp4.setAxisDependency(YAxis.AxisDependency.LEFT);
        setComp4.setColor(Color.YELLOW);
        setComp4.setFillColor(Color.YELLOW);
        setComp4.setDrawCircles(false);

        //기존의 그래프들과는 달리 하나의 그래프에 4개의 데이터를 넣어준다.
        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
        dataSets.add(setComp1);
        dataSets.add(setComp2);
        dataSets.add(setComp3);
        dataSets.add(setComp4);

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

        chart.setScaleEnabled(true);   //줌 드래그 됨.

        show_Day = (Button)findViewById(R.id.button23);
        show_Day.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show_Text.setText("일일 데이터");

                valsComp1.clear();
                valsComp2.clear();
                valsComp3.clear();
                valsComp4.clear();
                xVals.clear();


                sql = luxDB.getReadableDatabase();
                Cursor cursor;

                /////////////////////////////////////////
                cursor = sql.rawQuery("SELECT strftime(\"%H\",member3.Input_date), IFNULL(AVG(RESULT.lux_val),0) " +
                        "FROM Member3 LEFT OUTER JOIN (SELECT member.Input_date, member.lux_val FROM MEMBER WHERE user_id = '" + Final_UserID + "' AND name = '" + Final_UserName + "') AS RESULT ON strftime(\"%Y/%m/%d %H:00\",RESULT.Input_date) = strftime(\"%Y/%m/%d %H:00\",member3.Input_date) " +
                        "GROUP BY strftime(\"%Y/%m/%d %H\",member3.Input_date)", null);

                String luxvalue = "DB" + "\r\n";

                int count = 0;
                while (cursor.moveToNext()) {
                    valsComp1.add(new Entry(Float.parseFloat(cursor.getString(1)), Integer.parseInt(cursor.getString(0))));
                    xVals.add("" + cursor.getString(0));
                    Lux_AVG += (int) Float.parseFloat(cursor.getString(1));
                    count++;
                }
                Lux_AVG = Lux_AVG/24;
                AVG_Lux_TextView.setText("평균 조도값\n" + Lux_AVG);
                cursor.close();

                Log.v("조도값은",""+count);
                //////////////////////////////////////////////
                cursor = sql.rawQuery("SELECT strftime(\"%H\",member3.Input_date), IFNULL(AVG(RESULT.breath_val),0) " +
                        "FROM Member3 LEFT OUTER JOIN (SELECT member.Input_date, member.breath_val FROM MEMBER WHERE user_id = '" + Final_UserID + "' AND name = '" + Final_UserName + "') AS RESULT ON strftime(\"%Y/%m/%d %H:00\",RESULT.Input_date) = strftime(\"%Y/%m/%d %H:00\",member3.Input_date) " +
                        "GROUP BY strftime(\"%Y/%m/%d %H\",member3.Input_date)", null);


                while (cursor.moveToNext()) {
                    valsComp2.add(new Entry(Float.parseFloat(cursor.getString(1)), Integer.parseInt(cursor.getString(0))));
                    Breath_AVG += (int) Float.parseFloat(cursor.getString(1));
                }
                Breath_AVG = Breath_AVG/24;
                AVG_Breath_TextView.setText("평균 호흡수\n"+Breath_AVG);
                cursor.close();


                /////////////////////////////////////////////////////
                cursor = sql.rawQuery("SELECT strftime(\"%H\",member3.Input_date), IFNULL(AVG(RESULT.snore_val),0) " +
                        "FROM Member3 LEFT OUTER JOIN (SELECT member.Input_date, member.snore_val FROM MEMBER WHERE user_id = '" + Final_UserID + "' AND name = '" + Final_UserName + "') AS RESULT ON strftime(\"%Y/%m/%d %H:00\",RESULT.Input_date) = strftime(\"%Y/%m/%d %H:00\",member3.Input_date) " +
                        "GROUP BY strftime(\"%Y/%m/%d %H\",member3.Input_date)", null);


                while (cursor.moveToNext()) {
                    valsComp3.add(new Entry(Float.parseFloat(cursor.getString(1)), Integer.parseInt(cursor.getString(0))));
                    Snore_AVG += (int) Float.parseFloat(cursor.getString(1));
                }
                Snore_AVG = Snore_AVG/24;
                AVG_Snore_TextView.setText("평균 코골이 횟수\n"+Snore_AVG);
                cursor.close();


                /////////////////////////////////////////////////////
                cursor = sql.rawQuery("SELECT strftime(\"%H\",member3.Input_date), IFNULL(AVG(RESULT.apnea_val),0) " +
                        "FROM Member3 LEFT OUTER JOIN (SELECT member.Input_date, member.apnea_val FROM MEMBER WHERE user_id = '" + Final_UserID + "' AND name = '" + Final_UserName + "') AS RESULT ON strftime(\"%Y/%m/%d %H:00\",RESULT.Input_date) = strftime(\"%Y/%m/%d %H:00\",member3.Input_date) " +
                        "GROUP BY strftime(\"%Y/%m/%d %H\",member3.Input_date)", null);


                while (cursor.moveToNext()) {
                    valsComp4.add(new Entry(Float.parseFloat(cursor.getString(1)), Integer.parseInt(cursor.getString(0))));
                    Apnea_AVG += (int) Float.parseFloat(cursor.getString(1));
                }
                Apnea_AVG = Apnea_AVG/24;
                AVG_Apnea_TextView.setText("평균 무호흡 횟수\n"+Apnea_AVG);
                cursor.close();


                /////////////////////////////////////////////////////
                sql.close();
                setComp1 = new LineDataSet(valsComp1, "조도 값");
                setComp1.setValueFormatter(new MyYValueFormatter());
                //setComp1.setDrawValues(false);
                setComp1.setAxisDependency(YAxis.AxisDependency.LEFT);
                setComp1.setDrawCircles(false);

                setComp2 = new LineDataSet(valsComp2, "호흡 수");
                setComp2.setValueFormatter(new MyYValueFormatter());
                //setComp1.setDrawValues(false);
                setComp2.setAxisDependency(YAxis.AxisDependency.LEFT);
                setComp2.setColor(Color.MAGENTA);
                setComp2.setFillColor(Color.MAGENTA);
                setComp2.setDrawCircles(false);

                setComp3 = new LineDataSet(valsComp3, "코곤 횟수");
                setComp3.setValueFormatter(new MyYValueFormatter());
                //setComp1.setDrawValues(false);
                setComp3.setAxisDependency(YAxis.AxisDependency.LEFT);
                setComp3.setColor(Color.DKGRAY);
                setComp3.setFillColor(Color.DKGRAY);
                setComp3.setDrawCircles(false);

                setComp4 = new LineDataSet(valsComp4, "무호흡 수");
                setComp4.setValueFormatter(new MyYValueFormatter());
                //setComp1.setDrawValues(false);
                setComp4.setAxisDependency(YAxis.AxisDependency.LEFT);
                setComp4.setColor(Color.YELLOW);
                setComp4.setFillColor(Color.YELLOW);
                setComp4.setDrawCircles(false);

                ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
                dataSets.add(setComp1);
                dataSets.add(setComp2);
                dataSets.add(setComp3);
                dataSets.add(setComp4);


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


                chart.setScaleEnabled(false);   //줌 드래그 안됨.

            }
        });

        show_Week = (Button) findViewById(R.id.button24);
        show_Week.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show_Text.setText("주간 데이터");

                valsComp1.clear();
                valsComp2.clear();
                valsComp3.clear();
                valsComp4.clear();

                xVals.clear();

                sql = luxDB.getReadableDatabase();
                Cursor cursor;

                /////////////////////////////////////////
                cursor = sql.rawQuery("SELECT strftime(\"%Y/%m/%d\",member2.Input_date) , IFNULL(AVG(Result.lux_val),0) "
                        + "FROM Member2 LEFT OUTER JOIN (SELECT member.Input_date, member.lux_val FROM MEMBER WHERE user_id = '" + Final_UserID + "' AND name = '" + Final_UserName + "') AS RESULT ON strftime(\"%Y/%m/%d\", RESULT.Input_date) = strftime(\"%Y/%m/%d\", member2.Input_date) "
                        + "GROUP BY strftime(\"%Y/%m/%d\", member2.Input_date) "
                        + "HAVING member2.Input_date >= date('now','weekday 0', '-7 days', 'localtime') AND member2.Input_date <= date('now','weekday 0', '-1 days', 'localtime') "
                        + "ORDER BY MEMBER2.Input_date;", null);

                String DayString;
                int CheckNumberData = 0;

                while (cursor.moveToNext()) {
                    valsComp1.add(new Entry(Float.parseFloat(cursor.getString(1)), CheckNumberData));
                    CheckNumberData++;
                    xVals.add(cursor.getString(0).substring(8));
                    Lux_AVG += (int) Float.parseFloat(cursor.getString(1));
                }
                Lux_AVG = Lux_AVG/CheckNumberData;
                AVG_Lux_TextView.setText("평균 조도값\n" + Lux_AVG);
                cursor.close();

                //////////////////////////////////////////////
                cursor = sql.rawQuery("SELECT strftime(\"%Y/%m/%d\",member2.Input_date) , IFNULL(AVG(Result.breath_val),0) "
                        + "FROM Member2 LEFT OUTER JOIN (SELECT member.Input_date, member.breath_val FROM MEMBER WHERE user_id = '" + Final_UserID + "' AND name = '" + Final_UserName + "') AS RESULT ON strftime(\"%Y/%m/%d\", RESULT.Input_date) = strftime(\"%Y/%m/%d\", member2.Input_date) "
                        + "GROUP BY strftime(\"%Y/%m/%d\", member2.Input_date) "
                        + "HAVING member2.Input_date >= date('now','weekday 0', '-7 days', 'localtime') AND member2.Input_date <= date('now','weekday 0', '-1 days', 'localtime') "
                        + "ORDER BY MEMBER2.Input_date;", null);

                CheckNumberData = 0;
                while (cursor.moveToNext()) {
                    valsComp2.add(new Entry(Float.parseFloat(cursor.getString(1)), CheckNumberData));
                    CheckNumberData++;
                    Breath_AVG += (int) Float.parseFloat(cursor.getString(1));
                }
                Breath_AVG = Breath_AVG/CheckNumberData;
                AVG_Breath_TextView.setText("평균 호흡수\n"+Breath_AVG);
                cursor.close();


                /////////////////////////////////////////////////////
                cursor = sql.rawQuery("SELECT strftime(\"%Y/%m/%d\",member2.Input_date) , IFNULL(AVG(Result.snore_val),0) "
                        + "FROM Member2 LEFT OUTER JOIN (SELECT member.Input_date, member.snore_val FROM MEMBER WHERE user_id = '" + Final_UserID + "' AND name = '" + Final_UserName + "') AS RESULT ON strftime(\"%Y/%m/%d\", RESULT.Input_date) = strftime(\"%Y/%m/%d\", member2.Input_date) "
                        + "GROUP BY strftime(\"%Y/%m/%d\", member2.Input_date) "
                        + "HAVING member2.Input_date >= date('now','weekday 0', '-7 days', 'localtime') AND member2.Input_date <= date('now','weekday 0', '-1 days', 'localtime') "
                        + "ORDER BY MEMBER2.Input_date;", null);

                CheckNumberData = 0;
                while (cursor.moveToNext()) {
                    valsComp3.add(new Entry(Float.parseFloat(cursor.getString(1)), CheckNumberData));
                    CheckNumberData++;
                    Snore_AVG += (int) Float.parseFloat(cursor.getString(1));
                }
                Snore_AVG = Snore_AVG/CheckNumberData;
                AVG_Snore_TextView.setText("평균 코골이 횟수\n"+Snore_AVG);
                cursor.close();


                /////////////////////////////////////////////////////
                cursor = sql.rawQuery("SELECT strftime(\"%Y/%m/%d\",member2.Input_date) , IFNULL(AVG(Result.apnea_val),0) "
                        + "FROM Member2 LEFT OUTER JOIN (SELECT member.Input_date, member.apnea_val FROM MEMBER WHERE user_id = '" + Final_UserID + "' AND name = '" + Final_UserName + "') AS RESULT ON strftime(\"%Y/%m/%d\", RESULT.Input_date) = strftime(\"%Y/%m/%d\", member2.Input_date) "
                        + "GROUP BY strftime(\"%Y/%m/%d\", member2.Input_date) "
                        + "HAVING member2.Input_date >= date('now','weekday 0', '-7 days', 'localtime') AND member2.Input_date <= date('now','weekday 0', '-1 days', 'localtime') "
                        + "ORDER BY MEMBER2.Input_date;", null);

                CheckNumberData = 0;
                while (cursor.moveToNext()) {
                    valsComp4.add(new Entry(Float.parseFloat(cursor.getString(1)), CheckNumberData));
                    CheckNumberData++;
                    Apnea_AVG += (int) Float.parseFloat(cursor.getString(1));
                }
                Apnea_AVG = Apnea_AVG/CheckNumberData;
                AVG_Apnea_TextView.setText("평균 무호흡 횟수\n"+Apnea_AVG);
                cursor.close();


                /////////////////////////////////////////////////////
                sql.close();
                setComp1 = new LineDataSet(valsComp1, "조도 값");
                setComp1.setValueFormatter(new MyYValueFormatter());
                //setComp1.setDrawValues(false);
                setComp1.setAxisDependency(YAxis.AxisDependency.LEFT);
                setComp1.setDrawCircles(false);

                setComp2 = new LineDataSet(valsComp2, "호흡 수");
                setComp2.setValueFormatter(new MyYValueFormatter());
                //setComp1.setDrawValues(false);
                setComp2.setAxisDependency(YAxis.AxisDependency.LEFT);
                setComp2.setColor(Color.MAGENTA);
                setComp2.setFillColor(Color.MAGENTA);
                setComp2.setDrawCircles(false);

                setComp3 = new LineDataSet(valsComp3, "코곤 횟수");
                setComp3.setValueFormatter(new MyYValueFormatter());
                //setComp1.setDrawValues(false);
                setComp3.setAxisDependency(YAxis.AxisDependency.LEFT);
                setComp3.setColor(Color.DKGRAY);
                setComp3.setFillColor(Color.DKGRAY);
                setComp3.setDrawCircles(false);

                setComp4 = new LineDataSet(valsComp4, "무호흡 수");
                setComp4.setValueFormatter(new MyYValueFormatter());
                //setComp1.setDrawValues(false);
                setComp4.setAxisDependency(YAxis.AxisDependency.LEFT);
                setComp4.setColor(Color.YELLOW);
                setComp4.setFillColor(Color.YELLOW);
                setComp4.setDrawCircles(false);

                ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
                dataSets.add(setComp1);
                dataSets.add(setComp2);
                dataSets.add(setComp3);
                dataSets.add(setComp4);

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

        show_Mount = (Button) findViewById(R.id.button25);
        show_Mount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                show_Text.setText("월간 데이터");

                valsComp1.clear();
                valsComp2.clear();
                valsComp3.clear();
                valsComp4.clear();
                xVals.clear();

                sql = luxDB.getReadableDatabase();
                Cursor cursor;
                /////////////////////////////////////////
                cursor = sql.rawQuery("SELECT strftime(\"%Y/%m/%d\", member2.Input_date), IFNULL(AVG(Result.lux_val),0) "
                        + "FROM Member2 LEFT OUTER JOIN (SELECT member.Input_date, member.lux_val FROM MEMBER WHERE user_id = '" + Final_UserID + "' AND name = '" + Final_UserName + "') AS RESULT ON strftime(\"%Y/%m/%d\", RESULT.Input_date) = strftime(\"%Y/%m/%d\", member2.Input_date) "
                        + "GROUP BY strftime(\"%Y/%m/%d\",member2.Input_date) "
                        + "HAVING member2.Input_date >= date('now','start of month','localtime') AND member2.Input_date <= date('now','start of month','+1 month','-1 day','localtime') "
                        + "ORDER BY MEMBER2.Input_date;", null);

                String luxvalue = "DB" + "\r\n";

                int CheckNumberData = 0;

                while (cursor.moveToNext()) {
                    valsComp1.add(new Entry(Float.parseFloat(cursor.getString(1)), CheckNumberData));
                    CheckNumberData++;
                    xVals.add("" + CheckNumberData);

                    luxvalue += cursor.getString(0) + " " + cursor.getString(1) + "\r\n";
                    Lux_AVG += (int) Float.parseFloat(cursor.getString(1));
                }
                Lux_AVG = Lux_AVG/CheckNumberData;
                AVG_Lux_TextView.setText("평균 조도값\n" + Lux_AVG);
                cursor.close();

                //////////////////////////////////////////////
                cursor = sql.rawQuery("SELECT strftime(\"%Y/%m/%d\", member2.Input_date), IFNULL(AVG(Result.breath_val),0) "
                        + "FROM Member2 LEFT OUTER JOIN (SELECT member.Input_date, member.breath_val FROM MEMBER WHERE user_id = '" + Final_UserID + "' AND name = '" + Final_UserName + "') AS RESULT ON strftime(\"%Y/%m/%d\", RESULT.Input_date) = strftime(\"%Y/%m/%d\", member2.Input_date) "
                        + "GROUP BY strftime(\"%Y/%m/%d\",member2.Input_date) "
                        + "HAVING member2.Input_date >= date('now','start of month','localtime') AND member2.Input_date <= date('now','start of month','+1 month','-1 day','localtime') "
                        + "ORDER BY MEMBER2.Input_date;", null);

                CheckNumberData = 0;
                while (cursor.moveToNext()) {
                    valsComp2.add(new Entry(Float.parseFloat(cursor.getString(1)), CheckNumberData));
                    CheckNumberData++;
                    Breath_AVG += (int) Float.parseFloat(cursor.getString(1));
                }
                Breath_AVG = Breath_AVG/CheckNumberData;
                AVG_Breath_TextView.setText("평균 호흡수\n"+Breath_AVG);
                cursor.close();


                /////////////////////////////////////////////////////
                cursor = sql.rawQuery("SELECT strftime(\"%Y/%m/%d\", member2.Input_date), IFNULL(AVG(Result.snore_val),0) "
                        + "FROM Member2 LEFT OUTER JOIN (SELECT member.Input_date, member.snore_val FROM MEMBER WHERE user_id = '" + Final_UserID + "' AND name = '" + Final_UserName + "') AS RESULT ON strftime(\"%Y/%m/%d\", RESULT.Input_date) = strftime(\"%Y/%m/%d\", member2.Input_date) "
                        + "GROUP BY strftime(\"%Y/%m/%d\",member2.Input_date) "
                        + "HAVING member2.Input_date >= date('now','start of month','localtime') AND member2.Input_date <= date('now','start of month','+1 month','-1 day','localtime') "
                        + "ORDER BY MEMBER2.Input_date;", null);

                CheckNumberData=0;
                while (cursor.moveToNext()) {
                    valsComp3.add(new Entry(Float.parseFloat(cursor.getString(1)), CheckNumberData));
                    CheckNumberData++;
                    Snore_AVG += (int) Float.parseFloat(cursor.getString(1));
                }
                Snore_AVG = Snore_AVG/CheckNumberData;
                AVG_Snore_TextView.setText("평균 코골이 횟수\n"+Snore_AVG);
                cursor.close();


                /////////////////////////////////////////////////////
                cursor = sql.rawQuery("SELECT strftime(\"%Y/%m/%d\", member2.Input_date), IFNULL(AVG(Result.apnea_val),0) "
                        + "FROM Member2 LEFT OUTER JOIN (SELECT member.Input_date, member.apnea_val FROM MEMBER WHERE user_id = '" + Final_UserID + "' AND name = '" + Final_UserName + "') AS RESULT ON strftime(\"%Y/%m/%d\", RESULT.Input_date) = strftime(\"%Y/%m/%d\", member2.Input_date) "
                        + "GROUP BY strftime(\"%Y/%m/%d\",member2.Input_date) "
                        + "HAVING member2.Input_date >= date('now','start of month','localtime') AND member2.Input_date <= date('now','start of month','+1 month','-1 day','localtime') "
                        + "ORDER BY MEMBER2.Input_date;", null);

                CheckNumberData =0;
                while (cursor.moveToNext()) {
                    valsComp4.add(new Entry(Float.parseFloat(cursor.getString(1)), CheckNumberData));
                    CheckNumberData++;
                    Apnea_AVG += (int) Float.parseFloat(cursor.getString(1));
                }
                Apnea_AVG = Apnea_AVG/CheckNumberData;
                AVG_Apnea_TextView.setText("평균 무호흡 횟수\n"+Apnea_AVG);
                cursor.close();


                /////////////////////////////////////////////////////
                sql.close();
                setComp1 = new LineDataSet(valsComp1, "조도 값");
                setComp1.setValueFormatter(new MyYValueFormatter());
                setComp1.setDrawValues(true);
                setComp1.setAxisDependency(YAxis.AxisDependency.LEFT);
                setComp1.setDrawCircles(false);

                setComp2 = new LineDataSet(valsComp2, "호흡 수");
                setComp2.setValueFormatter(new MyYValueFormatter());
                setComp1.setDrawValues(true);
                setComp2.setAxisDependency(YAxis.AxisDependency.LEFT);
                setComp2.setColor(Color.MAGENTA);
                setComp2.setFillColor(Color.MAGENTA);
                setComp2.setDrawCircles(false);

                setComp3 = new LineDataSet(valsComp3, "코곤 횟수");
                setComp3.setValueFormatter(new MyYValueFormatter());
                setComp1.setDrawValues(true);
                setComp3.setAxisDependency(YAxis.AxisDependency.LEFT);
                setComp3.setColor(Color.DKGRAY);
                setComp3.setFillColor(Color.DKGRAY);
                setComp3.setDrawCircles(false);

                setComp4 = new LineDataSet(valsComp4, "무호흡 수");
                setComp4.setValueFormatter(new MyYValueFormatter());
                setComp1.setDrawValues(true);
                setComp4.setAxisDependency(YAxis.AxisDependency.LEFT);
                setComp4.setColor(Color.YELLOW);
                setComp4.setFillColor(Color.YELLOW);
                setComp4.setDrawCircles(false);

                ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
                dataSets.add(setComp1);
                dataSets.add(setComp2);
                dataSets.add(setComp3);
                dataSets.add(setComp4);

                Log.v("CheckNumberData", ""+CheckNumberData);
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

    }

    public boolean onOptionsItemSelected(android.view.MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent intent = new Intent(getApplicationContext(), UserSetting.class);
                intent.putExtra("유저아이디", Final_UserID);
                intent.putExtra("유저이름", Final_UserName);
                intent.putExtra("유저나이", Final_UserAge);
                intent.putExtra("유저성별", Final_UserGender);
                intent.putExtra("유저체중", Final_UserWeight);
                intent.putExtra("베개공기", Final_PillowAir);
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
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
            return mFormat.format(value) + ""; // e.g. append a dollar-sign
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


}
