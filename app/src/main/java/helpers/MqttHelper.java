package helpers;

import android.content.Context;
import android.util.Log;

import com.frost.mqtttutorial.R;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.DisconnectedBufferOptions;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Properties;
import java.util.Set;
import java.util.logging.ErrorManager;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

/**
 * Created by wildan on 3/19/2017.
 */

public class MqttHelper {
    private static final String LOG = "MqttHelper" ;
    public MqttAndroidClient mqttAndroidClient;

    //final String serverUri = "tcp://broker.hivemq.com:1883";
    //final String serverUri = "tcp://maqiatto.com:1883";
    final String serverUri = "ssl://192.168.0.191:8883";

    final String clientId = "ExampleAndroidClient";
    //final String subscriptionTopic = "sensor/+";
    final String subscriptionTopic = "/test";

    //final String username = "xxxxxxx";
    final String username ="mohanishpachlore@rediffmail.com";
    //final String password = "yyyyyyy";
    final String password = "1234";

    public MqttHelper(Context context){
        mqttAndroidClient = new MqttAndroidClient(context, serverUri, clientId);
        mqttAndroidClient.setCallback(new MqttCallbackExtended() {
            @Override
            public void connectComplete(boolean b, String s) {
                Log.w("mqtt", s);
            }

            @Override
            public void connectionLost(Throwable throwable) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage mqttMessage) throws Exception {
                Log.w("Mqtt", mqttMessage.toString());
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

            }
        });
        SSLSocketFactory sslSocketFactory = createSSLSocketFactory(context);
        connect(sslSocketFactory);
    }

    public void setCallback(MqttCallbackExtended callback) {
        mqttAndroidClient.setCallback(callback);
    }

    private void connect(SSLSocketFactory sslSocketFactory){
        MqttConnectOptions mqttConnectOptions = new MqttConnectOptions();
        mqttConnectOptions.setAutomaticReconnect(true);
        mqttConnectOptions.setCleanSession(false);
        mqttConnectOptions.setUserName(username);
        mqttConnectOptions.setPassword(password.toCharArray());

        mqttConnectOptions.setSocketFactory(sslSocketFactory);


        try {

            mqttAndroidClient.connect(mqttConnectOptions, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {

                    DisconnectedBufferOptions disconnectedBufferOptions = new DisconnectedBufferOptions();
                    disconnectedBufferOptions.setBufferEnabled(true);
                    disconnectedBufferOptions.setBufferSize(100);
                    disconnectedBufferOptions.setPersistBuffer(false);
                    disconnectedBufferOptions.setDeleteOldestMessages(false);
                    mqttAndroidClient.setBufferOpts(disconnectedBufferOptions);
                    subscribeToTopic();
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w("Mqtt", "Failed to connect to: " + serverUri + exception.toString());
                }
            });


        } catch (MqttException ex){
            ex.printStackTrace();
        }
    }


    private void subscribeToTopic() {
        try {
            mqttAndroidClient.subscribe(subscriptionTopic, 0, null, new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.w("Mqtt","Subscribed!");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.w("Mqtt", "Subscribed fail!");
                }
            });

        } catch (MqttException ex) {
            System.err.println("Exception whilst subscribing");
            ex.printStackTrace();
        }
    }

    private SSLSocketFactory createSSLSocketFactory(Context context)
    {
        try
        {
            CertificateFactory caCF = CertificateFactory.getInstance("X.509");

            InputStream caInput = context.getAssets().open("ca.crt");
            Log.d("Mqtt", "Read Ca");
            Certificate ca;
            try {
                ca = caCF.generateCertificate(caInput);
                Log.d("Mqtt","ca=" + ((X509Certificate) ca).getSubjectDN());
            } finally {
                caInput.close();
            }
            KeyStore caKeyStore = KeyStore.getInstance(KeyStore.getDefaultType());


            caKeyStore.load(null, null);
            caKeyStore.setCertificateEntry("An MQTT broker", ca);

            TrustManagerFactory tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            tmf.init(caKeyStore);

            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, tmf.getTrustManagers(), null);
            return sslContext.getSocketFactory();
        }
        catch (IOException | KeyStoreException | KeyManagementException | NoSuchAlgorithmException | CertificateException e)
        {
            //LOG.error("Creating ssl socket factory failed", e);
            return null;
        }
    }
}
