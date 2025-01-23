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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

public class ForwardToTcpActivity extends SerialPortActivity {

    SendingThread mSendingThread;
    //ReceivingThread mReceivingThread;
    private ServerSocket mTcpServerSocket = null;
    private Socket mTcpSocket = null;
    private int mPORT = 19999;
    private OutputStream mTcpOut = null;
    private InputStream mTcpIn = null;

    // open TCP server socket here, create sending thread, receiving thread.
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.forwarduart);

        Thread tcpThread = new TcpThread();
        tcpThread.start();

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
            Log.i("","starting sending thread");
            while (!isInterrupted()) {
                try {
                    if (mOutputStream != null && mTcpIn != null) {
                        if (mTcpIn.available() > 0) {
                            Log.i("","got data on tcp");
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

    private class TcpThread extends Thread {
        @Override
        public void run() {
            Log.i("", "starting Tcp thread");
            try {
                mTcpServerSocket = new ServerSocket(mPORT);
                mTcpSocket = mTcpServerSocket.accept();
                mTcpIn = mTcpSocket.getInputStream();
                mTcpOut = mTcpSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /*
    private class ReceivingThread extends Thread {
        @Override
        public void run() {
            while (!isInterrupted()) {
                try {
                    if (mInputStream != null) {
                        // wirte to TCP here...
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
     */
}
