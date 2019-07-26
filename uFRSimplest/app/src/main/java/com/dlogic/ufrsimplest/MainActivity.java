package com.dlogic.ufrsimplest;

import android.graphics.Color;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dlogic.uFCoder;

public class MainActivity extends AppCompatActivity {

    static {
        System.loadLibrary("uFCoder"); //Load uFCoder library
    }

    private Handler handler;
    int status;
    int thread_status;
    uFCoder uFCoder; //Create uFCoder class instance

    EditText reader_type;
    EditText port_name;
    EditText port_interface;
    EditText arg;
    CheckBox useAdvanced;
    Button btnOpen;
    TextView statusLabel;
    TextView cardStatusLabel;
    EditText cardTypeField;
    EditText cardUIDField;
    EditText cardUIDSizeField;
    Button btnFormat;
    EditText linearReadField;
    EditText linearWriteField;
    Button btnRead;
    Button btnWrite;
    LinearLayout advancedContainer;
    TextView connSTValue;
    TextView funcSTValue;
    TextView cardSTValue;
    TextView connSTStatus;

    public byte[] sak = new byte[1];
    public byte[] uid = new byte[10];
    public byte[] uidSize = new byte[1];
    private final int STATUS_IS_OK = 0;
    private final int STATUS_IS_NO_CARD = 8;
    private final int STATUS_IS_ERROR = 1;
    public boolean LOOP = false;

    public static boolean isHexChar(char c)
    {
        char hexChars[] = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'a', 'b', 'c', 'd', 'e', 'f',
                'A', 'B', 'C', 'D', 'E', 'F'};

        for(int i = 0; i < 22; i++)
        {
            if(c == hexChars[i])
            {
                return true;
            }
        }

        return false;
    }

    public static String eraseDelimiters(String hex_str)
    {
        for(int i = 0; i < hex_str.length(); i++)
        {
            if(!isHexChar(hex_str.charAt(i)))
            {
                hex_str = hex_str.substring(0, i) + hex_str.substring(i + 1);
            }
        }

        return hex_str;
    }

    public static byte[] hexStringToByteArray(String paramString) throws IllegalArgumentException {
        int j = paramString.length();

        if (j % 2 == 1) {
            throw new IllegalArgumentException("Hex string must have even number of characters");
        }

        byte[] arrayOfByte = new byte[j / 2];
        int hiNibble, loNibble;

        for (int i = 0; i < j; i += 2) {
            hiNibble = Character.digit(paramString.charAt(i), 16);
            loNibble = Character.digit(paramString.charAt(i + 1), 16);
            if (hiNibble < 0) {
                throw new IllegalArgumentException("Illegal hex digit at position " + i);
            }
            if (loNibble < 0) {
                throw new IllegalArgumentException("Illegal hex digit at position " + (i + 1));
            }
            arrayOfByte[(i / 2)] = ((byte) ((hiNibble << 4) + loNibble));
        }
        return arrayOfByte;
    }

    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String toHexadecimal(byte[] bytes, int len) {
        char[] hexChars = new char[len * 2];
        for ( int j = 0; j < len; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        uFCoder = new uFCoder(getApplicationContext());

        advancedContainer = findViewById(R.id.advancedLayout);
        reader_type = findViewById(R.id.readerTypeET);
        port_name = findViewById(R.id.portNameET);
        port_interface = findViewById(R.id.portInterfaceET);
        arg = findViewById(R.id.argET);
        useAdvanced = findViewById(R.id.checkBoxUseAdvanced);
        btnOpen = findViewById(R.id.btnReaderOpen);
        statusLabel = findViewById(R.id.functionStatus);
        cardTypeField = findViewById(R.id.cardTypeET);
        cardUIDField = findViewById(R.id.cardUIDET);
        cardUIDSizeField = findViewById(R.id.uidSizeET);
        btnFormat = findViewById(R.id.btnFormatCard);
        linearReadField = findViewById(R.id.linearReadET);
        linearWriteField = findViewById(R.id.linearWriteET);
        btnRead = findViewById(R.id.btnLinearRead);
        btnWrite = findViewById(R.id.btnLinearWrite);
        cardStatusLabel = findViewById(R.id.cardStatus);
        connSTValue = findViewById(R.id.connValue);
        cardSTValue = findViewById(R.id.cardStatusValue);
        funcSTValue = findViewById(R.id.funcStatusValue);
        connSTStatus = findViewById(R.id.connectionStatus);

        btnOpen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try
                {
                    if(useAdvanced.isChecked())
                    {
                        String reader_type_str = reader_type.getText().toString().trim();
                        String port_name_str = port_name.getText().toString().trim();
                        String arg_Str = arg.getText().toString().trim();
                        String port_interface_str = port_interface.getText().toString().trim();

                        if(reader_type_str.equals("") || port_name_str.equals("") || port_interface_str.equals("") || arg_Str.equals(""))
                        {
                            Toast.makeText(getApplicationContext(), "You have to fill all parameters for advanced Reader Open", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        int port_interface_int = (int)port_interface_str.charAt(0);
                        int int_rtype = Integer.parseInt(reader_type_str);

                        status =  uFCoder.ReaderOpenEx(int_rtype, port_name_str, port_interface_int, arg_Str);
                    }
                    else
                    {
                        status = uFCoder.ReaderOpen();
                    }
                }
                catch (Exception ex){}

                if(status == 0)
                {
                    Toast.makeText(getApplicationContext(), "Reader connected successfully", Toast.LENGTH_SHORT).show();
                    uFCoder.ReaderUISignal((byte)1,(byte)1);
                    LOOP = true;
                }
                else
                {
                    Toast.makeText(getApplicationContext(), "Reader not connected", Toast.LENGTH_SHORT).show();
                }

                connSTStatus.setText(UFR_STATUS_2_STRING(status));
                statusLabel.setText(UFR_STATUS_2_STRING(status));

                String status_val = "0x";

                if(status <= 0x0F)
                {
                    status_val += "0" + Integer.toHexString(status);
                }
                else
                {
                    status_val += Integer.toHexString(status);
                }

                connSTValue.setText(status_val);
                funcSTValue.setText(status_val);
            }
        });

        useAdvanced.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(useAdvanced.isChecked())
                {
                    reader_type.setEnabled(true);
                    port_name.setEnabled(true);
                    port_interface.setEnabled(true);
                    arg.setEnabled(true);
                    advancedContainer.setVisibility(View.VISIBLE);
                }
                else
                {
                    reader_type.setEnabled(false);
                    port_name.setEnabled(false);
                    port_interface.setEnabled(false);
                    arg.setEnabled(false);
                    advancedContainer.setVisibility(View.GONE);
                }

            }
        });

        handler = new Handler(){
            public void handleMessage(android.os.Message msg) {
                if(msg.what == STATUS_IS_OK)
                {
                    String uid_size_str = "0x";
                    String card_type_str = "0x";

                    if(uidSize[0] <= 0x0F)
                    {
                        uid_size_str += "0" + Integer.toString(uidSize[0]);
                    }
                    else
                    {
                        uid_size_str += Integer.toString(uidSize[0]);
                    }

                    if(sak[0] <= 0x0F)
                    {
                        card_type_str += "0" + Integer.toHexString(sak[0]);
                    }
                    else
                    {
                        card_type_str += Integer.toHexString(sak[0]);
                    }

                    cardUIDSizeField.setText(uid_size_str);
                    cardTypeField.setText(card_type_str);
                    cardUIDField.setText(toHexadecimal(uid, uidSize[0]));
                    cardStatusLabel.setText(UFR_STATUS_2_STRING(thread_status));
                    cardSTValue.setText("0x00");
                }
                else if(msg.what == STATUS_IS_NO_CARD)
                {
                    cardUIDSizeField.setText("0x00");
                    cardTypeField.setText("0x00");
                    cardUIDField.setText("NO CARD");
                    cardStatusLabel.setText(UFR_STATUS_2_STRING(thread_status));
                    cardSTValue.setText("0x08");
                }
                else
                {
                    String card_status_value = "0x";

                    if(thread_status <= 0x0F)
                    {
                        card_status_value += "0" + Integer.toHexString(thread_status);
                    }
                    else
                    {
                        card_status_value += Integer.toHexString(thread_status);
                    }

                    cardUIDSizeField.setText("");
                    cardTypeField.setText("");
                    cardUIDField.setText("");
                    cardStatusLabel.setText(UFR_STATUS_2_STRING(thread_status));
                    cardSTValue.setText(card_status_value);
                }
            }
        };

        new Thread() {
            public void run() {

                while(true)
                {
                    if(LOOP)
                    {
                        thread_status = uFCoder.GetCardIdEx(sak, uid, uidSize);

                        switch (thread_status) {
                            case 0:
                                handler.obtainMessage(STATUS_IS_OK, -1, -1)
                                        .sendToTarget();
                                break;

                            case 8:
                                handler.obtainMessage(STATUS_IS_NO_CARD, -1, -1)
                                        .sendToTarget();
                                break;

                            default:
                                handler.obtainMessage(STATUS_IS_ERROR, -1, -1)
                                        .sendToTarget();
                                break;
                        }

                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }

        }.start();

        btnFormat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LOOP = false;

                try {
                    Thread.sleep(150);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                byte[] keyA = {(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF};
                byte[] keyB = {(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF};
                byte[] formatted = new byte[1];

                status = uFCoder.LinearFormatCard(keyA, (byte)0x00, (byte)0x01, (byte)0x69, keyB, formatted, (byte)0x60, (byte)0);

                String status_val = "0x";

                if(status <= 0x0F)
                {
                    status_val += "0" + Integer.toHexString(status);
                }
                else
                {
                    status_val += Integer.toHexString(status);
                }

                funcSTValue.setText(status_val);
                statusLabel.setText(UFR_STATUS_2_STRING(status));
                LOOP = true;
            }
        });

        btnRead.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LOOP = false;

                try {
                    Thread.sleep(150);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                byte[] data = new byte[1024];
                int[] linearSize = new int[1];
                int[] rawSize = new int[1];
                short[] bytesReaded = new short[1];

                uFCoder.GetCardSize(linearSize, rawSize);
                status = uFCoder.LinearRead(data, (short)0, (short)linearSize[0], bytesReaded, (byte)0x60, (byte)0);

                String status_val = "0x";

                if(status <= 0x0F)
                {
                    status_val += "0" + Integer.toHexString(status);
                }
                else
                {
                    status_val += Integer.toHexString(status);
                }

                funcSTValue.setText(status_val);
                statusLabel.setText(UFR_STATUS_2_STRING(status));

                if(status == 0)
                {
                    String all_data = toHexadecimal(data, bytesReaded[0]);
                    linearReadField.setText(all_data);
                }
                else {
                    linearReadField.setText("");
                }
                LOOP = true;
            }
        });

        btnWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                LOOP = false;

                try {
                    Thread.sleep(150);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                String write_data = linearWriteField.getText().toString();
                write_data = eraseDelimiters(write_data);

                if(write_data.length() % 2 != 0 || write_data.length() == 0)
                {
                    Toast.makeText(getApplicationContext(), "Wrong data input", Toast.LENGTH_SHORT).show();
                    return;
                }

                short len = (short)(write_data.length() / 2);
                short[] returned = new short[1];

                byte[] data = hexStringToByteArray(write_data);

                status = uFCoder.LinearWrite(data, (byte)0x00, len, returned, (byte)0x60, (byte)0x00);

                String status_val = "0x";

                if(status <= 0x0F)
                {
                    status_val += "0" + Integer.toHexString(status);
                }
                else
                {
                    status_val += Integer.toHexString(status);
                }

                funcSTValue.setText(status_val);
                statusLabel.setText(UFR_STATUS_2_STRING(status));

                LOOP = true;
            }
        });
    }

    public String UFR_STATUS_2_STRING(int status)
    {
        return uFCoder.UFR_Status2String(status);
    }
}