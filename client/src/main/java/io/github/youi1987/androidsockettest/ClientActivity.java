package io.github.youi1987.androidsockettest;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Random;

public class ClientActivity extends Activity {

    // DESIGNATE A PORT
    public static final int SERVER_PORT = 8080;

    private EditText serverIp;

    private String serverIpAddress = "";

    private boolean connected = false;

    private Socket socket;
    PrintWriter out;

    Random random = new Random();

    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client);

        serverIp = (EditText) findViewById(R.id.server_ip);
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.connect_button: {
                if (!connected) {
                    serverIpAddress = serverIp.getText().toString();
                    if (!serverIpAddress.equals("")) {
                        Thread cThread = new Thread(clientThread);
                        cThread.start();
                    }
                    ((Button) v).setText("Disconnect");
                } else {
                    connected = false;
                    try {
                        out.close();
                        socket.close();
                    } catch (IOException e) {
                        Log.e("ClientActivity", "C: Error", e);
                    }
                    Log.d("ClientActivity", "C: Closed.");
                    ((Button) v).setText("Connect");
                }
                break;
            }
            case R.id.send_button: {
                if (connected) {
                    new SendTask().execute();
                }
                break;
            }
        }
    }

    public class SendTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Log.d("ClientActivity", "C: Sending command.");

                // WHERE YOU ISSUE THE COMMANDS
                out.println("Hey Server!" + random.nextInt());
                Log.d("ClientActivity", "C: Sent.");
            } catch (Exception e) {
                Log.e("ClientActivity", "S: Error", e);
            }
            return null;
        }
    }

    public Runnable clientThread = new Runnable() {

        public void run() {
            try {
                InetAddress serverAddr = InetAddress.getByName(serverIpAddress);
                Log.d("ClientActivity", "C: Connecting...");
                socket = new Socket(serverAddr, SERVER_PORT);
                connected = true;
                out = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket
                        .getOutputStream())), true);
                Log.d("ClientActivity", "C: Connected");
            } catch (Exception e) {
                Log.e("ClientActivity", "C: Error", e);
                connected = false;
            }
        }
    };
}
