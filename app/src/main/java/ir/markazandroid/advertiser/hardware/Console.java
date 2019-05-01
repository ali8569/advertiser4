package ir.markazandroid.advertiser.hardware;

import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

/**
 * Coded by Ali on 1/20/2019.
 */
public class Console implements Closeable {

    private BufferedWriter writer;

    private Process process;

    public Console() {
       /* try {
            //change su to sh -- ali gholami
            process = Runtime.getRuntime().exec("su -e log -v console ali is good");
            init();
        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }


    private void init() {
        writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()));
    }

    public void write(String s) {
        try {
            process = Runtime.getRuntime().exec("su");
            ReaderThread readerThread = new ReaderThread(process);
            readerThread.start();
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(s + "\n");
            os.flush();
            os.writeBytes("exit\n");
            os.flush();
            process.waitFor();
            //writer.write(s);
            //writer.flush();
            //process.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void update(String s) {
        try {
            process = Runtime.getRuntime().exec("su");
            ReaderThread readerThread = new ReaderThread(process);
            readerThread.start();
            DataOutputStream os = new DataOutputStream(process.getOutputStream());
            os.writeBytes(s + "\n");
            os.flush();
            //TODO
            //os.writeBytes("exit\n");
            //os.flush();
            //process.waitFor();
            //writer.write(s);
            //writer.flush();
            //process.waitFor();
        } catch (IOException e) {
            e.printStackTrace();
        } //catch (InterruptedException e) {
        //    e.printStackTrace();
        // }
    }


    @Override
    public void close() {
        process.destroy();
    }

    private static class ReaderThread extends Thread {
        private Process process;
        private BufferedReader reader, errorReader;

        private ReaderThread(Process process) {
            this.process = process;
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            errorReader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        }

        @Override
        public void run() {
            // byte[] buffer = new byte[1024];
            String s;
            String e;
            while (true) {
                try {
                    if (reader == null || errorReader == null) break;
                    //int length =process.getErrorStream().read(buffer);
                    //e = new String(buffer,0,length);
                    while ((s = reader.readLine()) != null) {
                        Log.d("console", "Console out:" + s);
                    }
                    while ((e = errorReader.readLine()) != null) {
                        Log.e("console", "Console error:" + e);
                    }
                    Thread.sleep(5);
                    Log.e("console", "process code=" + process.exitValue());
                    break;

                } catch (IllegalThreadStateException err) {

                } catch (InterruptedException err) {
                    err.printStackTrace();
                    break;
                } catch (Exception err) {
                    err.printStackTrace();
                    break;
                }

            }

        }
    }
}
