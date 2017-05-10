package io.github.youi1987.androidsockettest;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

public class ServerActivity extends Activity {

    private TextView serverStatus;

    // DEFAULT IP
    public static String SERVER_IP = "10.0.2.15";

    // DESIGNATE A PORT
    public static final int SERVER_PORT = 8080;

    private Handler handler = new Handler();

    private ServerSocket serverSocket;

    private boolean reset = false;
    private boolean serverStart = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_server);

        serverStatus = (TextView) findViewById(R.id.server_status);

        SERVER_IP = getLocalIpAddress();

        startServer();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.reset_button: {
                reset = true;
                break;
            }
        }

    }

    private void startServer() {
        Thread fst = new Thread(ServerThread);
        fst.start();
    }

    public Runnable ServerThread = new Runnable() {

        @Override
        public void run() {
            try {
                if (SERVER_IP != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            serverStatus.setText("Listening on IP: " + SERVER_IP);
                        }
                    });
                    serverSocket = new ServerSocket(SERVER_PORT);
                    while (true) {
                        // LISTEN FOR INCOMING CLIENTS
                        final Socket client = serverSocket.accept();
                        reset = false;
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                serverStatus.setText("Connected:" + client.getRemoteSocketAddress());
                            }
                        });

                        try {
                            BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                            String line = null;
                            while ((line = in.readLine()) != null && !reset) {
                                Log.d("ServerActivity", line);
                                final String text = line;
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        // DO WHATEVER YOU WANT TO THE FRONT END
                                        // THIS IS WHERE YOU CAN BE CREATIVE
                                        serverStatus.setText("Client:" + text);
                                    }
                                });
                            }
                            break;
                        } catch (Exception e) {
                            handler.post(new Runnable() {
                                @Override
                                public void run() {
                                    serverStatus.setText("Oops. Connection interrupted. Please reconnect your phones.");
                                }
                            });
                            e.printStackTrace();
                        }
                    }
                } else {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            serverStatus.setText("Couldn't detect internet connection.");
                        }
                    });
                }
            } catch (Exception e) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        serverStatus.setText("Error");
                    }
                });
                e.printStackTrace();
            }
        }
    };

    // GETS THE IP ADDRESS OF YOUR PHONE'S NETWORK
    private String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException ex) {
            Log.e("ServerActivity", ex.toString());
        }
        return null;
    }

    @Override
    protected void onStop() {
        super.onStop();
        try {
            // MAKE SURE YOU CLOSE THE SOCKET UPON EXITING
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
