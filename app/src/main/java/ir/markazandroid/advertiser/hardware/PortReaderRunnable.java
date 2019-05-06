package ir.markazandroid.advertiser.hardware;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import ir.markazandroid.advertiser.AdvertiserApplication;
import ir.markazandroid.advertiser.hardware.serial.SerialPort;
import ir.markazandroid.advertiser.hardware.serial.SerialPortFinder;
import ir.markazandroid.advertiser.signal.Signal;
import ir.markazandroid.advertiser.signal.SignalManager;
import ir.markazandroid.advertiser.util.Roozh;

/**
 * Coded by Ali on 01/03/2018.
 */

public class PortReaderRunnable implements Runnable {

    private Context context;
    private Handler handler;
    private boolean isBlocked = false;
    private InputParser inputParser;
    private SerialPort serialPort;
    boolean isCancelled = false;
    public static String lastData = "NA";
    private InputStream inputStream;
    private Timer timer;
    private int blockAcc, unBlockAcc;

    public PortReaderRunnable(Context context) {
        this.context = context;
        blockAcc = 0;
        unBlockAcc = 0;


        /*for (SerialPort port:ports){
            Log.e("port", "PortReaderRunnable: "+port.getDescriptivePortName()+" : "+port.getSystemPortName() );
        }*/


        handler = new Handler(context.getMainLooper());
        inputParser = new InputParser('\n', cmd -> {
            try {
                Log.e("command", cmd);
            /*if(cmd.equals("OFF")){
                isBlocked=true;
                sendBlockViewSignal();
            }*/
                cmd = cmd.replace('$', '&')
                        .replaceAll("&", "")
                        .replaceAll("\r", "")
                        .replaceAll("\n", "");
                lastData = cmd;

                String[] dataArray = cmd.split(";");
                Map<String, String> dataMap = new HashMap<>();
                for (String data : dataArray) {
                    String[] d = data.split(":");
                    dataMap.put(d[0], d[1]);
                }
                int d = Integer.parseInt(dataMap.get("d"));

                if (d < 100 && d > 0) {
                    if (!isBlocked) {
                        blockAcc++;
                        if (blockAcc >= 3) {
                            blockAcc = 0;
                            isBlocked = true;
                            sendBlockViewSignal();
                        }
                    } else {
                        unBlockAcc = 0;
                    }
                } else {
                    if (isBlocked) {
                        unBlockAcc++;
                        if (unBlockAcc >= 0) {
                            unBlockAcc = 0;
                            isBlocked = false;
                            sendUnBlockViewSignal();
                        }
                    } else {
                        blockAcc = 0;
                    }
                }
                //handler.post(()-> Toast.makeText(context,dataMap.get("d"),Toast.LENGTH_SHORT).show());
                Log.e("distance", dataMap.get("d") + "  ");
            } catch (Exception ignored) {

            }

        });
        inputParser.init();


        /*PortInfo[] list = Serial.listPorts();

        for(PortInfo info:list){
            Log.e("port", "PortReaderRunnable: "+info.description+" : "+info.hardwareId+" : "+info.port);
        }*/


        timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                //TODO
                save(lastData);
            }
        }, 10_000, 10_000);
    }


    @Override
    public void run() {

        /*final UsbManager manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        List<UsbSerialDriver> availableDrivers = UsbSerialProber.getDefaultProber().findAllDrivers(manager);
        if (availableDrivers.isEmpty()) {
            return;
        }


// Open a connection to the first available driver.
        final UsbSerialDriver driver = availableDrivers.get(0);
        final UsbDeviceConnection connection = manager.openDevice(driver.getDevice());
        if (connection == null) {

            handler.post(new Runnable() {
                @Override
                public void run() {
                    PendingIntent mPermissionIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
                    IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
                    context.registerReceiver(mUsbReceiver, filter);
                    manager.requestPermission(driver.getDevice(), mPermissionIntent);
                }
            });
            //showToast("NO DRIVER FOUND!");
            // You probably need to call UsbManager.requestPermission(driver.getDevice(), ..)
            return;
        }*/


// Read some data! Most have just one port (port 0).
        //port = driver.getPorts().get(0);

        boolean found = false;
        SerialPortFinder portFinder = new SerialPortFinder();
        String[] devices = portFinder.getAllDevicesPath();
        String portName = "";

        try {
            serialPort = new SerialPort(new File("/dev/ttyS4"), 9600, 0);
            inputStream = serialPort.getInputStream();
            found = true;
            portName = "/dev/ttyS4";
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (!found) {
            try {
                serialPort = new SerialPort(new File("/dev/ttyS2"), 9600, 0);
                inputStream = serialPort.getInputStream();
                found = true;
                portName = "/dev/ttyS2";
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (!found) {
            handler.post(() -> Toast.makeText(context, "Primary Port Not Found", Toast.LENGTH_SHORT).show());
            for (String device : devices) {
                //new File("/dev/ttyS2")
                try {
                    serialPort = new SerialPort(new File(device), 9600, 0);
                    inputStream = serialPort.getInputStream();
                    if (inputStream.available() < 1) {
                        Thread.sleep(200);
                        if (inputStream.available() > 0) {
                            found = true;
                            portName = device;
                            handler.post(() -> Toast.makeText(context, "Port Found " + device, Toast.LENGTH_SHORT).show());
                            break;
                        }
                        serialPort.close();
                    } else {
                        portName = device;
                        found = true;
                        handler.post(() -> Toast.makeText(context, "Port Found " + device, Toast.LENGTH_SHORT).show());
                        break;
                    }
                    serialPort.close();

                } catch (Exception e) {
                    e.printStackTrace();
                    try {
                        if (serialPort != null) {
                            serialPort.close();
                        }
                    } catch (Exception ignored) {
                    }

                }
            }
        } else {
            String finalPortName = portName;
            handler.post(() -> Toast.makeText(context, "Port Found " + finalPortName, Toast.LENGTH_SHORT).show());
        }


        try {
            //port.open(connection);
            //port.setParameters(9600, 8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
            byte[] buffer = new byte[1024];

            //isCancelled
            while (!isCancelled) {
                String read = readChar(inputStream, buffer);
                //Log.e("read",read);
                inputParser.addInput(read);

                //SystemClock.sleep(2000);
                /*if (Integer.parseInt(in.charAt(2)+"")==1)
                    if (!isBlocked) {
                        isBlocked=true;
                        sendBlockViewSignal();
                    }
                else if (isBlocked) {
                        isBlocked=false;
                        sendUnBlockViewSignal();
                    }*/

            }
        } catch (Exception e) {
            e.printStackTrace();
            handler.post(() -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show());
        } finally {
            try {
                if (serialPort != null) {
                    serialPort.close();
                }
            } catch (Exception ignored) {
            }

        }
        /*serialPort.openPort();
        serialPort.setComPortTimeouts(SerialPort.TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
        try {
            while (true)
            {
                byte[] readBuffer = new byte[1024];
                int numRead = serialPort.readBytes(readBuffer, readBuffer.length);
                Log.e("Read",numRead+"");
            }
        } catch (Exception e) { e.printStackTrace(); }
        serialPort.closePort();*/
    }

    public String readChar(InputStream inputStream, byte[] buffer) throws IOException {
        int numRead = inputStream.read(buffer);
        while (numRead < 1) {
            try {
                if (isCancelled) break;
                Thread.sleep(1);
                numRead = inputStream.read(buffer);
            } catch (Exception e) {
                handler.post(() -> Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show());
                e.printStackTrace();
                break;
            }
        }
        return new String(buffer, 0, numRead);
    }

    /*private void showToast(final String msg){
        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context,msg,Toast.LENGTH_LONG).show();
            }
        });
    }
*/
    private SignalManager getSignalManager() {
        return ((AdvertiserApplication) context.getApplicationContext()).getSignalManager();
    }

    private void sendBlockViewSignal() {
        //handler.post(()-> Toast.makeText(context,"Block",Toast.LENGTH_SHORT).show());
        Signal signal = new Signal("screen block", Signal.SIGNAL_SCREEN_BLOCK);
        getSignalManager().sendMainSignal(signal);
    }

    private void sendUnBlockViewSignal() {
        //handler.post(()-> Toast.makeText(context,"Unblock",Toast.LENGTH_SHORT).show());
        Signal signal = new Signal("screen unblock", Signal.SIGNAL_SCREEN_UNBLOCK);
        getSignalManager().sendMainSignal(signal);
    }

    private static final String ACTION_USB_PERMISSION =
            "com.android.example.USB_PERMISSION";
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            /*PortReaderRunnable portReader = new PortReaderRunnable(context);
                            ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
                            threadPoolExecutor.execute(portReader);*/
                        }
                    } else {
                        Log.d("tag", "permission denied for device " + device);
                    }
                }
            }
        }
    };

    public void close() {
        try {
            isCancelled = true;
            if (timer != null)
                timer.cancel();
            if (serialPort != null) {
                serialPort.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void save(String finalBitmap) {

        //getFilesDir()
        //openFileInput()
        //openFileOutput()
        String root = Environment.getExternalStorageDirectory().toString();
        File myDir = new File(root + "/advertiser/logs");
        myDir.mkdirs();
        String fname = Roozh.getCurrentTimeNo() + ".txt";
        File file = new File(myDir, fname);
        try {
            FileWriter out = new FileWriter(file, true);
            out.append(Roozh.getTime(System.currentTimeMillis()))
                    .append("  --  ")
                    .append(finalBitmap)
                    .append("\r\n");
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
