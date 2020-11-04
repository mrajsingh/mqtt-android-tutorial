package com.frost.mqtttutorial;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import helpers.ChartHelper;
import helpers.MqttHelper;

public class MainActivity extends AppCompatActivity {

    MqttHelper mqttHelper;
    ChartHelper mChart;
    LineChart chart;

    TextView dataReceived;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dataReceived = (TextView) findViewById(R.id.dataReceived);
        chart = (LineChart) findViewById(R.id.chart);
        //mChart = new ChartHelper(chart);

        startMqtt();
    }

    private void startMqtt(){
        mqttHelper = new MqttHelper(getApplicationContext());
        mqttHelper.mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
                Log.w("Debug","Connected");
            }

            @Override
            public void connectionLost(Throwable throwable) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                Log.w("Mqtt",mqttMessage.toString());
                dataReceived.setText(mqttMessage.toString());
                //mChart.addEntry(Float.valueOf(mqttMessage.toString()));
                MqttMessage msg = new MqttMessage("Hello".getBytes());
                Log.w("Mqtt","Message to be published"+msg.toString());

                    MqttException e= (MqttException) mqttHelper.mqttAndroidClient.publish("/test_return", msg);
                    Log.w("Mqtt","Publish failed with Exception"+e.getCause());


            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
    }
}
