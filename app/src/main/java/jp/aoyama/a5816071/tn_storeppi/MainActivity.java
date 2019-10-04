package jp.aoyama.a5816071.tn_storeppi;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.wearable.activity.WearableActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MainActivity extends WearableActivity implements SensorEventListener {

    private static final String TAG = "PPIActivity";    //Log用のタグ名
    private SensorManager sensorManager;
    private int cnt;
    private ArrayList<Float> ppiData;                   //測定値を格納する配列
    private ArrayList<Float> timeData;                  //測定時間を格納する配列
    private ArrayList<Float> DateData;
    private int state = 0;                              //測定中(1) or 待機中(0)
    private static final float NS2MS = 1.0f / 1000000.0f;
    private float time;
    private float timestamp;
    private TextView buttonView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setAmbientEnabled();

        buttonView = (TextView) findViewById(R.id.touched);

        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        List<Sensor> ppi_sensor = sensorManager.getSensorList(65547);
        sensorManager.registerListener(this, ppi_sensor.get(0), SensorManager.SENSOR_DELAY_FASTEST);

        Button button = findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {                    //ボタンが押された場合の処理
                if (state == 0) {                               // 1回目のクリックで測定を開始
                    state = 1;
                    cnt = 0;
                    time = 0;
                    timestamp = 0;
                    ppiData = new ArrayList();
                    timeData = new ArrayList();
                    DateData=new ArrayList();
                    Log.d(TAG, "Button clicked (Start)");
                    buttonView.setText("Started");
                } else {                                        // 2回目のクリックでデータを保存
                    state = 0;
                    Log.d(TAG, "Button clicked (Stop)");
                    buttonView.setText("Stopped");
                    createFile();
                }
            }
        });
        Log.d(TAG, "Created");
    }

    public void count_1(View view){
        buttonView.setText("Button1 was pressed");
        labelFileOutput(1);
    }
    public void count_2(View view){
        buttonView.setText("Button2 was pressed");
        labelFileOutput(2);
    }
    public void count_3(View view){
        buttonView.setText("Button3 was pressed");
        labelFileOutput(3);
    }
    public void count_4(View view){
        buttonView.setText("Button4 was pressed");
        labelFileOutput(4);
    }

    @Override

    protected void onResume() {
        super.onResume();
    }

    private void createFile() {
        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd_kkmmss");
        String filename = sdf.format(date) + ".csv";
        Log.d(TAG, filename);
        try {
            FileOutputStream fout = openFileOutput(filename, MODE_PRIVATE);
            int i;
            String comma = ",";
            String newline = "\n";
            for (i = 0; i < ppiData.size(); i++) {
                fout.write(String.valueOf(ppiData.get(i)).getBytes());
                fout.write(comma.getBytes());
                fout.write(String.valueOf(timeData.get(i)).getBytes());
                fout.write(comma.getBytes());
                fout.write(String.valueOf(DateData.get(i)).getBytes());
                fout.write(newline.getBytes());
            }
            fout.close();
            Log.d(TAG, "File created.");
        } catch (FileNotFoundException e) {
            Log.d(TAG, "Cannot open file.");
            e.printStackTrace();
        } catch (IOException e) {
            Log.d(TAG, "Cannot write string.");
            e.printStackTrace();
        }
    }

    private void labelFileOutput(int number){
        OutputStream out;
        Date date=new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("kkmmss");
        String saveDate = sdf.format(date);
        try {
            out = openFileOutput("label", MODE_PRIVATE | MODE_APPEND);
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(out, "UTF-8"));

            writer.append(number + ", " + saveDate +"\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    protected void onPause() {
        super.onPause();
        Log.d(TAG, "Paused");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG, "Stop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Destroy");
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        Date date = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("kkmmss");
        String filename =sdf.format(date);
        float db=Float.parseFloat(filename);
        if (state == 1) {
            if (timestamp != 0) {
                final float dT = (event.timestamp - timestamp) * NS2MS;
                time += dT;
            }
            timestamp = event.timestamp;
            cnt++;
            Log.d(TAG, "ppi:"+ cnt + ", 0:" + event.values[0] + ", 1(信頼度？):" + event.values[1] +", time:" + Math.round(time));
            ppiData.add(event.values[0]);
            timeData.add(time);
            DateData.add(db);
        } else {
            Log.d(TAG, "ppi:"+ cnt + ", 0:" + event.values[0] + "(no)");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
    }
}
