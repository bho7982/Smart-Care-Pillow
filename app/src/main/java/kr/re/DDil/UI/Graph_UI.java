package kr.re.DDil.UI;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.github.mikephil.charting.charts.Chart;
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
import kr.re.DDil.CarePillow.MainActivity;
import kr.re.DDil.CarePillow.UserSetting;
import kr.re.DDil.DataBase.LuxDB;
import kr.re.DDil.DataBase.Start_UserDB;
import kr.re.DDil.Graph.ApneaGraph;
import kr.re.DDil.Graph.BreathGraph;
import kr.re.DDil.Graph.SnoreGraph;

/**
 * Created by inthetech on 2017-01-26.
 */

public class Graph_UI extends Activity {

    private String Final_UserID, Final_UserName, Final_UserAge, Final_UserGender, Final_UserWeight, Final_PillowAir;
    LuxDB luxDB;
    Start_UserDB start_userDB;
    SQLiteDatabase sql;
    LineDataSet setComp1;   //MPAndroidChart

    Button Lux_avg_Button, Breath_avg_Button, Snore_avg_Button, Apnea_avg_Button, MoreInformation_Button;
    TextView mgraphname_textView;

    ArrayList<Entry> valsComp1 = new ArrayList<Entry>();
    ArrayList<String> xVals = new ArrayList<String>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.graph_ui);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        //데이터베이스 불러옴
        luxDB = new LuxDB(this);
        start_userDB = new Start_UserDB(this);

        //값을 받아옴
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


        //그래프 이름을 보여주는 textView
        mgraphname_textView = (TextView) findViewById(R.id.graphname_textView);

        //화면에 일주일 조도 그래프를 그려준다.
        //그래프 관련 소스는 LuxGraph 주석 참조.
        valsComp1.clear();
        xVals.clear();
        sql = luxDB.getReadableDatabase();
        Cursor cursor3;
        cursor3 = sql.rawQuery("SELECT strftime(\"%Y/%m/%d\",member2.Input_date) , IFNULL(AVG(Result.lux_val),0) "
                + "FROM Member2 LEFT OUTER JOIN (SELECT member.Input_date, member.lux_val FROM MEMBER WHERE user_id = '" + Final_UserID + "' AND name = '" + Final_UserName + "') AS RESULT ON strftime(\"%Y/%m/%d\", RESULT.Input_date) = strftime(\"%Y/%m/%d\", member2.Input_date) "
                + "GROUP BY strftime(\"%Y/%m/%d\", member2.Input_date) "
                + "HAVING member2.Input_date >= date('now','weekday 0', '-7 days', 'localtime') AND member2.Input_date <= date('now','weekday 0', '-1 days', 'localtime') "
                + "ORDER BY MEMBER2.Input_date;", null);

        int CheckNumberData = 0;

        while (cursor3.moveToNext()) {
            valsComp1.add(new Entry(Float.parseFloat(cursor3.getString(1)), CheckNumberData));
            CheckNumberData++;
            xVals.add("" + CheckNumberData);
        }

        cursor3.close();
        sql.close();
        setComp1 = new LineDataSet(valsComp1, "조도 값");
        setComp1.setValueFormatter(new MyYValueFormatter());
        setComp1.setDrawCubic(true);
        setComp1.setDrawFilled(true);
        setComp1.setAxisDependency(YAxis.AxisDependency.LEFT);
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

        chart.setScaleEnabled(false);   //줌 드래그 안됨.

        Lux_avg_Button = (Button) findViewById(R.id.button2);
        Lux_avg_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mgraphname_textView.setText("평균 조도 값");

                valsComp1.clear();
                xVals.clear();
                sql = luxDB.getReadableDatabase();
                Cursor cursor3;
                cursor3 = sql.rawQuery("SELECT strftime(\"%Y/%m/%d\",member2.Input_date) , IFNULL(AVG(Result.lux_val),0) "
                        + "FROM Member2 LEFT OUTER JOIN (SELECT member.Input_date, member.lux_val FROM MEMBER WHERE user_id = '" + Final_UserID + "' AND name = '" + Final_UserName + "') AS RESULT ON strftime(\"%Y/%m/%d\", RESULT.Input_date) = strftime(\"%Y/%m/%d\", member2.Input_date) "
                        + "GROUP BY strftime(\"%Y/%m/%d\", member2.Input_date) "
                        + "HAVING member2.Input_date >= date('now','weekday 0', '-7 days', 'localtime') AND member2.Input_date <= date('now','weekday 0', '-1 days', 'localtime') "
                        + "ORDER BY MEMBER2.Input_date;", null);

                int CheckNumberData = 0;

                while (cursor3.moveToNext()) {
                    valsComp1.add(new Entry(Float.parseFloat(cursor3.getString(1)), CheckNumberData));
                    CheckNumberData++;
                    xVals.add("" + CheckNumberData);
                }

                cursor3.close();
                sql.close();
                setComp1 = new LineDataSet(valsComp1, "조도 값");
                setComp1.setValueFormatter(new MyYValueFormatter());
                setComp1.setDrawCubic(true);
                setComp1.setDrawFilled(true);
                setComp1.setAxisDependency(YAxis.AxisDependency.LEFT);
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

                chart.setScaleEnabled(false);   //줌 드래그 안됨.
            }
        });

        Breath_avg_Button = (Button) findViewById(R.id.button4);
        Breath_avg_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mgraphname_textView.setText("평균 호흡 값");
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

        Snore_avg_Button = (Button) findViewById(R.id.button5);
        Snore_avg_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mgraphname_textView.setText("평균 코곤 횟수");
                valsComp1.clear();
                xVals.clear();

                sql = luxDB.getReadableDatabase();
                Cursor cursor;
                cursor = sql.rawQuery("SELECT strftime(\"%Y/%m/%d\",member2.Input_date) , IFNULL(AVG(Result.snore_val),0) "
                        + "FROM Member2 LEFT OUTER JOIN (SELECT member.Input_date, member.snore_val FROM MEMBER WHERE user_id = '" + Final_UserID + "' AND name = '" + Final_UserName + "') AS RESULT ON strftime(\"%Y/%m/%d\", RESULT.Input_date) = strftime(\"%Y/%m/%d\", member2.Input_date) "
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
                setComp1 = new LineDataSet(valsComp1, "코골이 횟수");
                setComp1.setValueFormatter(new MyYValueFormatter());
                //setComp1.setDrawValues(false);
                setComp1.setDrawCubic(true);
                setComp1.setDrawFilled(true);
                setComp1.setAxisDependency(YAxis.AxisDependency.LEFT);
                setComp1.setColor(Color.DKGRAY);
                setComp1.setFillColor(Color.DKGRAY);
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

        Apnea_avg_Button = (Button) findViewById(R.id.button35);
        Apnea_avg_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mgraphname_textView.setText("평균 무호흡 횟수");
                valsComp1.clear();
                xVals.clear();

                sql = luxDB.getReadableDatabase();
                Cursor cursor;
                cursor = sql.rawQuery("SELECT strftime(\"%Y/%m/%d\",member2.Input_date) , IFNULL(AVG(Result.apnea_val),0) "
                        + "FROM Member2 LEFT OUTER JOIN (SELECT member.Input_date, member.apnea_val FROM MEMBER WHERE user_id = '" + Final_UserID + "' AND name = '" + Final_UserName + "') AS RESULT ON strftime(\"%Y/%m/%d\", RESULT.Input_date) = strftime(\"%Y/%m/%d\", member2.Input_date) "
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
                setComp1 = new LineDataSet(valsComp1, "무호흡 횟수");
                setComp1.setValueFormatter(new MyYValueFormatter());
                //setComp1.setDrawValues(false);
                setComp1.setDrawCubic(true);
                setComp1.setDrawFilled(true);
                setComp1.setAxisDependency(YAxis.AxisDependency.LEFT);
                setComp1.setColor(Color.YELLOW);
                setComp1.setFillColor(Color.YELLOW);
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

        MoreInformation_Button = (Button) findViewById(R.id.button12);
        MoreInformation_Button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), Graph_UI2.class);
                intent.putExtra("유저아이디", Final_UserID);
                intent.putExtra("유저이름", Final_UserName);
                intent.putExtra("유저나이", Final_UserAge);
                intent.putExtra("유저성별", Final_UserGender);
                intent.putExtra("유저체중", Final_UserWeight);
                intent.putExtra("베개공기", Final_PillowAir);
                startActivity(intent);
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
            return mFormat.format(i + 1) + "일";
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
}
