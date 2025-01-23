/*
 * Copyright 2011 Cedric Priscal
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License. 
 */

package android.serialport.sample;

import android.os.Bundle;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

public class ForwardToTcpActivity extends SerialPortActivity {

    private static final String TAG = "FTTA";
    SendingThread mSendingThread;
    private OutputStream mTcpOut = null;
    private InputStream mTcpIn = null;
    private SocketServer socketServer;

    // open TCP server socket here, create sending thread, receiving thread.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forwarduart);


        socketServer = new SocketServer();
        new Thread(socketServer).start();

        Log.d(TAG, "tcp server started");

        //mBuffer = new byte[1024];
        //Arrays.fill(mBuffer, (byte) 0x55);
        if (mSerialPort != null) {
            mSendingThread = new SendingThread();
            mSendingThread.start();
            /*
            mReceivingThread = new ReceivingThread();
            mReceivingThread.start();
             */
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (socketServer != null) {
            socketServer.stopServer();
        }
    }

    // create socket here to send over TCP
    @Override
    protected void onDataReceived(byte[] buffer, int size) {
        // forward data to TCP here...
        try {
            if (mTcpOut != null) {
                mTcpOut.write(buffer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private class SendingThread extends Thread {
        @Override
        public void run() {
            Log.i(TAG,"starting sending thread");
            while (!isInterrupted()) {
                try {
                    if (mOutputStream != null && mTcpIn != null) {
                        if (mTcpIn.available() > 0) {
                            Log.i(TAG,"got data on tcp");
                            mOutputStream.write(mTcpIn.read());
                        }
                    } else {
                        return;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }
        }
    }

    private class SocketServer implements Runnable {
        private static final String TAG = "SocketServer";
        private static final int PORT = 19999;
        private boolean isRunning = true;
        private ServerSocket serverSocket;

        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(PORT);
                Log.i(TAG, "Server started on port " + PORT);

                while (isRunning) {
                    Socket socket = serverSocket.accept();
                    Log.i(TAG, "New client connected: " + socket.getInetAddress());

                    // Handle client connection in a new thread
                    new Thread(() -> handleClient(socket)).start();
                }

            } catch (Exception e) {
                Log.e(TAG, "Server error: " + e.getMessage());
            }
        }

        private void handleClient(Socket socket) {
            try {
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(socket.getInputStream())
                );

                mTcpIn = socket.getInputStream();
                String line;
                while ((line = reader.readLine()) != null) {
                    Log.i(TAG, "Received: " + line);

                }

            } catch (Exception e) {
                Log.e(TAG, "Error handling client: " + e.getMessage());
            } finally {
                try {
                    socket.close();
                } catch (Exception e) {
                    Log.e(TAG, "Error closing socket: " + e.getMessage());
                }
            }
        }

        public void stopServer() {
            isRunning = false;
            try {
                if (serverSocket != null) {
                    serverSocket.close();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error stopping server: " + e.getMessage());
            }
        }
    }
}
