package kr.re.DDil.CarePillow;

import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedList;
import java.util.Set;

import kr.re.DDil.Bluetooth.BluetoothSerialClient;
import kr.re.DDil.Bluetooth.BluetoothSerialClient.BluetoothStreamingHandler;
import kr.re.DDil.Bluetooth.BluetoothSerialClient.OnBluetoothEnabledListener;
import kr.re.DDil.Bluetooth.BluetoothSerialClient.OnScanListener;
import kr.re.DDil.BluetoothEcho.R;
import kr.re.DDil.DataBase.LuxDB;
import kr.re.DDil.DataBase.Start_DeviceDB;
import kr.re.DDil.DataBase.Start_UserDB;
import kr.re.DDil.DataBase.myDB;
import kr.re.DDil.Graph.ApneaGraph;
import kr.re.DDil.Graph.BreathGraph;
import kr.re.DDil.Graph.LuxGraph;
import kr.re.DDil.Graph.SnoreGraph;
import kr.re.DDil.UI.Graph_UI;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewConfiguration;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.formatter.XAxisValueFormatter;
import com.github.mikephil.charting.formatter.YAxisValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;


/**
 * Blutooth Arduino Echo.
 * <p>
 * 문자열의 끝은 '\0' 을 붙여서 구분한다.
 * <p>
 * www.dev.re.kr
 *
 * @author ice3x2@gmail.com / Beom
 */
public class MainActivity extends Activity {
    public static ArrayList<String> packet; //패킷 테스트 리스트

    private LinkedList<BluetoothDevice> mBluetoothDevices = new LinkedList<BluetoothDevice>();
    private ArrayAdapter<String> mDeviceArrayAdapter;

    private EditText mEditTextInput;
    private TextView mTextView;
    public TextView textView_DATE;
    private Button mButtonSend, mConnect_Device_Button;
    private ProgressDialog mLoadingDialog;
    public AlertDialog mDeviceListDialog;
    private Menu mMenu;
    public BluetoothSerialClient mClient;
    private String Lux = "0";
    private String Snoring = "0";
    private String Breath = "0";
    private String Apnea = "0";

    //화면전환버튼들
    private Button muserinformationButton;
    private Button mLux_GraphButton;
    private Button mSnore_GraphButton;
    private Button mBreath_GraphButton;
    private Button mApnea_GraphButton;
    private Button mPillow_ControlButton;

    private TextView mtextView_UserName; //'유저이름'

    private AsyncTask<Void, Void, Void> mTask;  //시계 Asynctask

    //데이터베이스 선언
    myDB my;
    LuxDB luxDB;
    Start_UserDB start_userDB;
    Start_DeviceDB start_deviceDB;
    SQLiteDatabase sql;

    //유저 리스트, 유저 선택 Dialog 선언
    public ArrayList<String> mUserNameArrayList = new ArrayList<String>();
    public AlertDialog mUserListDialog;

    public ListView listView;

    //다른 화면으로 넘길 값들을 선언
    private String Final_UserID = "0";
    private String Final_UserName = "Guest";
    private String Final_UserAge = "-";
    private String Final_UserGender = "-";
    private String Final_UserWeight = "-";
    private String Final_PillowAir = "0";

    private Button mUITest_Button;

    //어플리케이션 실행 시 처음 자동연결을 위한 Count_Connect
    public int Count_Connect = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        String currentDateTimeString = DateFormat.getDateTimeInstance().format(new Date()); //현재 시간을 가져와서 currentDateTimeString에 담는다.
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        packet = new ArrayList<String>();   //패킷 선언

        mClient = BluetoothSerialClient.getInstance();                  //블루투스 사용 선언
        textView_DATE = (TextView) findViewById(R.id.textView_DATE);    //현재 시간 TextView
        textView_DATE.setText(currentDateTimeString);                     //currentDateTimeString을 TextView에 넣는다.

        //블루투스 mClient가 null일때, 블루투스 오류 호술 후 앱 끝내기
        if (mClient == null) {
            Toast.makeText(getApplicationContext(), "블루투스 기기를 사용 할 수 없습니다.", Toast.LENGTH_SHORT).show();
            finish();
        }

        overflowMenuInActionBar();  //메뉴설정. 건들지 않아도 됨.
        initProgressDialog();   //건들지 않아도 됨.
        initDeviceListDialog(); //블루투스 connect의 dialog 출력부분. 건들지 않아도 됨.
        initWidget();           //블루투스. 건들지 않아도 됨.

        //유저 이름을 받아온다. "반갑습니다 textView_UserName 님."
        mtextView_UserName = (TextView) findViewById(R.id.textView_UserName);

        //DB 선언
        my = new myDB(this);
        luxDB = new LuxDB(this);
        start_userDB = new Start_UserDB(this);
        start_deviceDB = new Start_DeviceDB(this);

        //종료시 저장된 User DB를 가져오는 부분
        sql = start_userDB.getReadableDatabase();
        Cursor cursor;
        cursor = sql.rawQuery("SELECT * FROM MEMBER;", null);
        if (cursor != null && cursor.getCount() != 0) {

            //cursor.moveToNext는 SQL쿼리문을 실행 한 뒤에 한칸씩 내려가는 명령. 참고 [http://arabiannight.tistory.com/entry/368]
            while (cursor.moveToNext()) {
                mtextView_UserName.setText(cursor.getString(1));

                Final_UserID = cursor.getString(0);
                Final_UserName = cursor.getString(1);
                Final_UserAge = cursor.getString(2);
                Final_UserGender = cursor.getString(3);
                Final_UserWeight = cursor.getString(4);
            }
        } else {
            sql = start_userDB.getWritableDatabase();
            start_userDB.onUpgrade(sql, 1, 2);
            sql.execSQL("INSERT INTO member VALUES(0,'"
                    + "Guest" + "','"
                    + "-" + "','"
                    + "-" + "','"
                    + "-" + "');");

            Final_UserID = "0";
            Final_UserName = "Guest";
            Final_UserAge = "-";
            Final_UserGender = "-";
            Final_UserWeight = "-";
        }
        sql.close();
        cursor.close();

        //디바이스 커넥트 버튼을 누를때 "기기연결 버튼"
        mConnect_Device_Button = (Button) findViewById(R.id.Connect_Device_Button);
        mConnect_Device_Button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {     //버튼 누르면 종료시에 저장했던  디바이스 정보로 연결 시작
                sql = start_deviceDB.getReadableDatabase();
                Cursor cursor2;
                cursor2 = sql.rawQuery("SELECT * FROM MEMBER;", null);
                if (cursor2 != null && cursor2.getCount() != 0) {       //DB가 비어있지 않다면 if 문 실행
                    while (cursor2.moveToNext()) {

                        BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
                        String Address = cursor2.getString(2);

                        BluetoothDevice device = btAdapter.getRemoteDevice(Address);
                        connect(device);
                    }
                } else {                                                //DB가 비어있을 경우(처음 어플리케이션 실행 시) else 문 실행(DB에 임의의 Mac 주소를 넣음)
                    start_deviceDB.onUpgrade(sql, 1, 2);
                    sql.execSQL("INSERT INTO member VALUES('0','"
                            + "Name" + "','"
                            + "12:34:56:78:90:AB" + "');");
                }
                sql.close();
            }
        });

        sql = start_deviceDB.getReadableDatabase();     //위 과정이 모두 끝난 뒤 실행됨
        Cursor cursor2;
        cursor2 = sql.rawQuery("SELECT * FROM MEMBER;", null);
        if (cursor2 != null && cursor2.getCount() != 0) {       //혹시 DB가 비어있는 경우라면 다시 아래의 else문을 통해 DB에 임의의 Mac 주소를 넣음
        } else {
            start_deviceDB.onUpgrade(sql, 1, 2);
            sql.execSQL("INSERT INTO member VALUES('0','"
                    + "Name" + "','"
                    + "12:34:56:78:90:AB" + "');");
        }
        sql.close();

        ShowTimeMethod();      //매 초 Main의 UI 시계를 최신화 해주는 동기화(Asynctask)
        System.gc();    //가비지 컬렉터 (MPAnderoidChart가 무거워서 해줌)
        System.gc();

        //member2를 초기화 하고 데이터를 집어넣음 (어플리케이션 실행 시점의 한달 달력을 만듬)
        SimpleDateFormat fm1 = new SimpleDateFormat("yyyy-MM");
        String date = fm1.format(new Date());
        Calendar calendar = Calendar.getInstance();
        sql = luxDB.getWritableDatabase();
        sql.execSQL("DROP TABLE IF EXISTS member2");
        sql.execSQL("create table member2 (_id INTEGER PRIMARY KEY AUTOINCREMENT, lux_val char(20), breath_val char(20), snore_val char(20), apnea_val char(20), Input_date datetime(10))");
        for (int i = 1; i <= calendar.getActualMaximum(Calendar.DATE); i++) {
            if (i < 10) {
                sql.execSQL("INSERT INTO member2 VALUES(null,0,0,0,0,'"
                        + date + "-0" + i + "');"
                );
            } else {
                sql.execSQL("INSERT INTO member2 VALUES(null,0,0,0,0,'"
                        + date + "-" + i + "');"
                );
            }
        }

        //member3를 초기화 하고 데이터를 집어넣음 (어플리케이션 실행 시점의 날짜로 하루 24시간 Table을 만듬)
        SimpleDateFormat fm2 = new SimpleDateFormat("yyyy-MM-dd ");
        String date2 = fm2.format(new Date());

        sql = luxDB.getWritableDatabase();
        sql.execSQL("DROP TABLE IF EXISTS member3");
        sql.execSQL("create table member3 (_id INTEGER PRIMARY KEY AUTOINCREMENT, lux_val char(20), breath_val char(20), snore_val char(20), apnea_val char(20), Input_date datetime(10))");
        for (int i = 0; i < 24; i++) {
            if (i < 10) {
                sql.execSQL("INSERT INTO member3 VALUES(null,0,0,0,0,'"
                        + date2 + " 0" + i + ":00');"
                );
            } else {
                sql.execSQL("INSERT INTO member3 VALUES(null,0,0,0,0,'"
                        + date2 + " " + i + ":00');"
                );
            }
        }

        //사용자 등록 및 설정 버튼을 클릭 할 시 Main 클래스에서, intent로 해당하는 데이터를 다른 Activity로 넘겨준다. (다른 화면으로 값 전달)
        muserinformationButton = (Button) findViewById(R.id.button17);
        muserinformationButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), UserSetting.class);
                startActivity(intent);
            }
        });

        //조도 기록 버튼을 클릭 할 시
        mLux_GraphButton = (Button) findViewById(R.id.button16);
        mLux_GraphButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LuxGraph.class);
                startActivity(intent);
            }
        });

        //코골이 기록 버튼을 클릭 할 시
        mSnore_GraphButton = (Button) findViewById(R.id.button14);
        mSnore_GraphButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), SnoreGraph.class);
                startActivity(intent);
            }
        });

        //호흡 기록 버튼을 클릭 할 시
        mBreath_GraphButton = (Button) findViewById(R.id.button15);
        mBreath_GraphButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), BreathGraph.class);
                startActivity(intent);
            }
        });

        //무호흡 기록 버튼을 클릭 할 시
        mApnea_GraphButton = (Button) findViewById(R.id.button18);
        mApnea_GraphButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), ApneaGraph.class);
                startActivity(intent);
            }
        });

        //베개 제어 버튼을 클릭 할 시
        mPillow_ControlButton = (Button) findViewById(R.id.button19);
        mPillow_ControlButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), PillowControl.class);
                startActivity(intent);
            }
        });

        //현재는 UITestButton이지만, 후에는 데이터 디스플레이로 바꿀 예정.
        mUITest_Button = (Button) findViewById(R.id.button6);
        mUITest_Button.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getApplicationContext(), Graph_UI.class);
                startActivity(intent);
            }
        });

    }

    //메뉴 설정. 건드리지 않아도 됨.
    private void overflowMenuInActionBar() {
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception ex) {
            // 무시한다. 3.x 이 예외가 발생한다.
            // 또, 타블릿 전용으로 만들어진 3.x 버전의 디바이스는 보통 하드웨어 버튼이 존재하지 않는다.
        }
    }

    @Override                       //화면 전환시 호출
    protected void onPause() {
        mClient.cancelScan(getApplicationContext());
        super.onPause();
    }

    @Override
    //화면 전환시 호출2. onPause와 onResume은 순서만 다르지 화면 전환시 둘 다 호출됨. 이때 블루투스가 연결되어있는지 확인하고 연결되어있지 않다면 재 연결 시도.
    protected void onResume() {
        super.onResume();
        enableBluetooth();

        sql = start_userDB.getReadableDatabase();
        Cursor cursor;
        cursor = sql.rawQuery("SELECT * FROM MEMBER;", null);
        if (cursor != null && cursor.getCount() != 0) {

            //cursor.moveToNext는 SQL쿼리문을 실행 한 뒤에 한칸씩 내려가는 명령. 참고 [http://arabiannight.tistory.com/entry/368]
            while (cursor.moveToNext()) {
                mtextView_UserName.setText(cursor.getString(1));

                Final_UserID = cursor.getString(0);
                Final_UserName = cursor.getString(1);
                Final_UserAge = cursor.getString(2);
                Final_UserGender = cursor.getString(3);
                Final_UserWeight = cursor.getString(4);
            }
        }
        sql.close();
    }

    private void initProgressDialog() {     //건드리지 않아도 됨. 블루투스 로딩시간 관련
        mLoadingDialog = new ProgressDialog(this);
        mLoadingDialog.setCancelable(false);
    }

    private void initWidget() {             //건드리지 않아도 됨. 통신 테스트용으로 만든 버튼 동작 관련
        mTextView = (TextView) findViewById(R.id.textViewTerminal);
        mTextView.setMovementMethod(new ScrollingMovementMethod());
        mEditTextInput = (EditText) findViewById(R.id.editTextInput);
        mButtonSend = (Button) findViewById(R.id.buttonSend);
        mButtonSend.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                sendStringData(mEditTextInput.getText().toString());
                mEditTextInput.setText("");
            }
        });
    }

    private void initDeviceListDialog() {   //connect의 alertdialog 출력 (연결할때 디바이스 목록을 띄운다.)
        mDeviceArrayAdapter = new ArrayAdapter<String>(getApplicationContext(), R.layout.item_device);
        listView = new ListView(getApplicationContext());
        listView.setAdapter(mDeviceArrayAdapter);
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = (String) parent.getItemAtPosition(position);
                for (BluetoothDevice device : mBluetoothDevices) {
                    if (item.contains(device.getAddress())) {
                        connect(device);

                        sql = start_deviceDB.getWritableDatabase();
                        start_deviceDB.onUpgrade(sql, 1, 2);
                        sql.execSQL("INSERT INTO member VALUES(null,'"
                                + device.getName() + "','"
                                + device.getAddress() + "');"
                        );
                        sql.close();
                        mDeviceListDialog.cancel();
                    }
                }
            }
        });
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("연결할 기기를 선택하세요.");
        builder.setView(listView);
        builder.setPositiveButton("다른 기기 검색하기",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        scanDevices();
                    }
                });
        mDeviceListDialog = builder.create();
        mDeviceListDialog.setCanceledOnTouchOutside(false);
    }

    private void addDeviceToArrayAdapter(BluetoothDevice device) {      //건드리지 않아도 됨(블루투스)
        if (mBluetoothDevices.contains(device)) {
            mBluetoothDevices.remove(device);
            mDeviceArrayAdapter.remove(device.getName() + "\n" + device.getAddress());
        }
        mBluetoothDevices.add(device);
        mDeviceArrayAdapter.add(device.getName() + "\n" + device.getAddress());
        mDeviceArrayAdapter.notifyDataSetChanged();

    }


    private void enableBluetooth() {                                //건드리지 않아도 됨(블루투스)
        BluetoothSerialClient btSet = mClient;
        btSet.enableBluetooth(this, new OnBluetoothEnabledListener() {
            @Override
            public void onBluetoothEnabled(boolean success) {
                if (success) {
                    getPairedDevices();

                    //앱 실행시 처음에 기존 디바이스와 자동 연결을 시도하며, 성공이든 실패하든 1번만 시도한다. 앱 재실행시 Count_Connect 변수가 0으로 초기화된다.
                    if (Count_Connect == 0) {
                        if (mClient.isConnection() != true) {       //연결이 되어있지 않다면 저장되어있는(첫 연결시에는 디폴트값이 들어가있고, 이후 연결되면 가장 마지막에 연결되었던) 디바이스 Mac 주소를 가져와 연결시도함
                            sql = start_deviceDB.getReadableDatabase();
                            Cursor cursor2;
                            cursor2 = sql.rawQuery("SELECT * FROM MEMBER;", null);
                            if (cursor2 != null && cursor2.getCount() != 0) {
                                while (cursor2.moveToNext()) {

                                    BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
                                    String Address = cursor2.getString(2);

                                    BluetoothDevice device = btAdapter.getRemoteDevice(Address);
                                    connect(device);
                                }
                            }
                            sql.close();
                            Log.v("onResume", "연결X.");
                            Count_Connect++;

                        } else {
                            Log.v("onResume", "연결O.");
                            Count_Connect++;
                        }
                    }
                } else {
                    finish();
                }
            }
        });
    }

    private void addText(String text) {                     //데이터가 날아오면 onData에서 부르는 부분. 화면에 날아온 데이터를 표시한다. 필요없는 기능. 건드릴 필요 없음.
        mTextView.append(text);
        final int scrollAmount = mTextView.getLayout().getLineTop(mTextView.getLineCount()) - mTextView.getHeight();
        if (scrollAmount > 0)
            mTextView.scrollTo(0, scrollAmount);
        else
            mTextView.scrollTo(0, 0);
    }


    private void getPairedDevices() {                   //블루투스. 페어링 된 기기들을 가져온다. 건드릴 필요 없음.
        Set<BluetoothDevice> devices = mClient.getPairedDevices();
        for (BluetoothDevice device : devices) {
            addDeviceToArrayAdapter(device);
        }
    }

    private void scanDevices() {                        //블루투스. 주변 블루투스 통신 가능 기기를 스캔함. 건드릴 필요 없음.
        BluetoothSerialClient btSet = mClient;
        btSet.scanDevices(getApplicationContext(), new OnScanListener() {
            String message = "";

            @Override
            public void onStart() {
                Log.d("Test", "Scan Start.");
                mLoadingDialog.show();
                message = "검색중...";
                mLoadingDialog.setMessage("검색중...");
                mLoadingDialog.setCancelable(true);
                mLoadingDialog.setCanceledOnTouchOutside(false);
                mLoadingDialog.setOnCancelListener(new OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog) {
                        BluetoothSerialClient btSet = mClient;
                        btSet.cancelScan(getApplicationContext());
                    }
                });
            }

            @Override
            public void onFoundDevice(BluetoothDevice bluetoothDevice) {    //블루투스. 기기를 찾아서 연결 했을 때 표시됨. 건드릴 필요 없음
                addDeviceToArrayAdapter(bluetoothDevice);
                message += "\n" + bluetoothDevice.getName() + "\n" + bluetoothDevice.getAddress();
                mLoadingDialog.setMessage(message);
            }

            @Override
            public void onFinish() {            //블루투스. 스캔 끝난경우. 건드릴 필요 없음.
                Log.d("Test", "Scan finish.");
                message = "";
                mLoadingDialog.cancel();
                mLoadingDialog.setCancelable(false);
                mLoadingDialog.setOnCancelListener(null);
                mDeviceListDialog.show();
            }
        });
    }

    private void connect(BluetoothDevice device) {  //블루투스. 기기 연결시에 dialog 출력 및 연결. 건드릴 필요 없음.
        mLoadingDialog.setMessage("기기 연결중...");
        mLoadingDialog.setCanceledOnTouchOutside(true);
        mLoadingDialog.show();
        BluetoothSerialClient btSet = mClient;
        btSet.connect(getApplicationContext(), device, mBTHandler);
    }

    public BluetoothStreamingHandler mBTHandler = new BluetoothStreamingHandler() {     //블루투스 연결과 관련된 부분. onData를 제되하면 건드릴 필요 없음.
        ByteBuffer mmByteBuffer = ByteBuffer.allocate(1024);

        @Override
        public void onError(Exception e) {      //블루투스 연결에 실패할시
            mLoadingDialog.cancel();
            addText("Messgae : Connection error - " + e.toString() + "\n");
            mMenu.getItem(0).setTitle("블루투스 연결");
            Toast.makeText(getApplicationContext(), "연결할 기기를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onDisconnected() {          //블루투스 연결을 종료할시
            mMenu.getItem(0).setTitle("블루투스 연결");
            mLoadingDialog.cancel();
            addText("Messgae : Disconnected.\n");
        }

        @Override
        public void onData(byte[] buffer, int length) {     //블루투스가 데이터를보내면 호출되는 함수 (디바이스에서 데이터를 보내면 BluetoothSerialClient 클래스에서 mReadRunnable가 호출되고, 이 함수에서 onData를 호출함

            if (length == 0) return;

            if (mmByteBuffer.position() + length >= mmByteBuffer.capacity()) {
                ByteBuffer

                        newBuffer = ByteBuffer.allocate(mmByteBuffer.capacity() * 2);
                newBuffer.put(mmByteBuffer.array(), 0, mmByteBuffer.position());
                mmByteBuffer = newBuffer;
            }
            mmByteBuffer.put(buffer, 0, length);

            if (buffer[length - 1] == '\0') {

                addText(mClient.getConnectedDevice().getName() + " : " +
                        new String(mmByteBuffer.array(), 0, mmByteBuffer.position()) + '\n');


                //구분자로 나눔
                String ReceiveData_split[] = new String(mmByteBuffer.array(), 0, mmByteBuffer.position()).split(",");
                Lux = ReceiveData_split[0];
                Breath = ReceiveData_split[1];
                Snoring = ReceiveData_split[2];
                Apnea = ReceiveData_split[3];

                Log.v("Lux", Lux);
                Log.v("Breath", Breath);
                //i Log.v("Snoring",Snoring);


                //addText(mClient.getConnectedDevice().getName() + " : " + A + "," + B + '\n');


                Lux = ReceiveData_split[0];
                Breath = ReceiveData_split[1];
                Snoring = ReceiveData_split[2];

                // DB에 오늘 날짜를 넣기 위한 부분
                SimpleDateFormat fm1 = new SimpleDateFormat("yyyy-MM-dd HH:mm");
                String date = fm1.format(new Date());

                //데이터를 LuxDB에 추가하는 부분.
                sql = luxDB.getWritableDatabase();
                sql.execSQL("INSERT INTO member VALUES(null,'"  //이때 _id는 자동으로 1씩 증가한다.(LuxDB class 참조)
                        + Lux + "','"
                        + Breath + "','"
                        + Snoring + "','"
                        + Apnea + "','"
                        + Final_UserID + "','"
                        + Final_UserName + "','"
                        + date + "');"
                );
                sql.close();

                mmByteBuffer.clear();

            }

        }

        @Override
        public void onConnected() {     //기기가 연결되었을때
            Toast.makeText(getApplicationContext(), "기기가 연결 되었습니다.", Toast.LENGTH_SHORT).show();
            addText("Messgae : Connected. " + mClient.getConnectedDevice().getName() + "\n");
            mLoadingDialog.cancel();
            mMenu.getItem(0).setTitle("연결 끊기");
        }
    };

    public void sendStringData(String data) {       //어플리케이션에서 디바이스로 데이터를 보낼 때
        data += '\0';
        byte[] buffer = data.getBytes();

        if (mBTHandler.write(buffer)) {
//            addText("Me : " + data + '\n');
        }

    }

    protected void onDestroy() {        //건드릴 필요 없음.
        super.onDestroy();
        mClient.claer();
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {     //메뉴 옵션 활성화 (이게 없다면 상단메뉴에서 뒤로가기 혹은 기기 연결 등의 메뉴가 보이지 않음.)
        getMenuInflater().inflate(R.menu.main, menu);
        mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {       //메뉴 옵션을 선택했을때
        boolean connect = mClient.isConnection();
        if (item.getItemId() == R.id.connect) {                     //블루투스 연결 버튼을 눌렀을 때
            if (!connect) {                                         //연결되어있지 않다면 디바이스 목록을 보여준다.
                mDeviceListDialog.show();
            } else {
                mBTHandler.close();                               //연결되어 있다면 종료
            }
            return true;
        } else {                                                    //다른 버튼을 누를때 (유저선택)
            initUserListDialog();                                   //유저 리스트를 불러옴
            mUserListDialog.show();                                 //유저 선택 다이어로그를 보여줌
            return true;
        }
    }

    public void initUserListDialog() {                              //유저 리스트를 불러옴
        mUserNameArrayList.clear();
        mUserNameArrayList.add("0,Guest,-,-,-");

        sql = my.getReadableDatabase();
        Cursor cursor;
        cursor = sql.rawQuery("SELECT * FROM MEMBER;", null);


        while (cursor.moveToNext()) {
            mUserNameArrayList.add(cursor.getString(0) + "," + cursor.getString(1) + "," + cursor.getString(2) + "," + cursor.getString(3) + "," + cursor.getString(4));
        }

        final String[] items = mUserNameArrayList.toArray(new String[mUserNameArrayList.size()]);
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("유저를 선택하세요");
        builder.setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String[] str = new String(items[which]).split(",");
                mtextView_UserName.setText(str[1]);

                Final_UserID = str[0];
                Final_UserName = str[1];
                Final_UserAge = str[2];
                Final_UserGender = str[3];
                Final_UserWeight = str[4];
                Toast.makeText(getApplicationContext(), str[0] + "," + str[1], Toast.LENGTH_SHORT).show();

                sql = start_userDB.getWritableDatabase();       //선택된 유저를 DB에 넣어 어플리케이션 재 시작시 자동으로 해당 유저를 불러온다.
                start_userDB.onUpgrade(sql, 1, 2);
                sql.execSQL("INSERT INTO member VALUES('"
                        + Final_UserID + "','"
                        + Final_UserName + "','"
                        + Final_UserAge + "','"
                        + Final_UserGender + "','"
                        + Final_UserWeight + "');"
                );
                sql.close();
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        mUserListDialog = builder.create();
        mUserListDialog.setCanceledOnTouchOutside(false);
    }

    public void ShowTimeMethod() {              //시간을 최신화 하는 동기화(Asynctask)
        mTask = new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                while (true) {
                    try {
                        publishProgress();
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            protected void onProgressUpdate(Void... progress) {
                textView_DATE.setText(DateFormat.getDateTimeInstance().format(new Date()));
            }
        };
        mTask.execute();
    }
}
