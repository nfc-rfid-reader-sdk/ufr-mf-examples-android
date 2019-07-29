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
        for(int i = 0; i < ERRORCODES.values().length; i++)
        {
            if(ERRORCODES.values()[i].value == status)
            {
                return ERRORCODES.values()[i].toString();
            }
        }
        return "UFR_STATUS_NOT_FOUND";
    }

    public enum ERRORCODES {

        UFR_OK(0x00),
        UFR_COMMUNICATION_ERROR(0x01),
        UFR_CHKSUM_ERROR(0x02),
        UFR_READING_ERROR(0x03),
        UFR_WRITING_ERROR(0x04),
        UFR_BUFFER_OVERFLOW(0x05),
        UFR_MAX_ADDRESS_EXCEEDED(0x06),
        UFR_MAX_KEY_INDEX_EXCEEDED(0x07),
        UFR_NO_CARD(0x08),
        UFR_COMMAND_NOT_SUPPORTED(0x09),
        UFR_FORBIDEN_DIRECT_WRITE_IN_SECTOR_TRAILER(0x0A),
        UFR_ADDRESSED_BLOCK_IS_NOT_SECTOR_TRAILER(0x0B),
        UFR_WRONG_ADDRESS_MODE(0x0C),
        UFR_WRONG_ACCESS_BITS_VALUES(0x0D),
        UFR_AUTH_ERROR(0x0E),
        UFR_PARAMETERS_ERROR(0x0F),
        UFR_MAX_SIZE_EXCEEDED(0x10),
        UFR_UNSUPPORTED_CARD_TYPE(0x11),
        UFR_COUNTER_ERROR(0x12),
        UFR_WRITE_VERIFICATION_ERROR(0x70),
        UFR_BUFFER_SIZE_EXCEEDED(0x71),
        UFR_VALUE_BLOCK_INVALID(0x72),
        UFR_VALUE_BLOCK_ADDR_INVALID(0x73),
        UFR_VALUE_BLOCK_MANIPULATION_ERROR(0x74),
        UFR_WRONG_UI_MODE(0x75),
        UFR_KEYS_LOCKED(0x76),
        UFR_KEYS_UNLOCKED(0x77),
        UFR_WRONG_PASSWORD(0x78),
        UFR_CAN_NOT_LOCK_DEVICE(0x79),
        UFR_CAN_NOT_UNLOCK_DEVICE(0x7A),
        UFR_DEVICE_EEPROM_BUSY(0x7B),
        UFR_RTC_SET_ERROR(0x7C),
        ANTI_COLLISION_DISABLED(0x7D),
        NO_TAGS_ENUMERRATED(0x7E),
        CARD_ALREADY_SELECTED(0x7F),
        UFR_COMMUNICATION_BREAK(0x50),
        UFR_NO_MEMORY_ERROR(0x51),
        UFR_CAN_NOT_OPEN_READER(0x52),
        UFR_READER_NOT_SUPPORTED(0x53),
        UFR_READER_OPENING_ERROR(0x54),
        UFR_READER_PORT_NOT_OPENED(0x55),
        UFR_CANT_CLOSE_READER_PORT(0x56),
        UFR_TIMEOUT_ERR(0x90),
        UFR_FT_STATUS_ERROR_1(0xA0),
        UFR_FT_STATUS_ERROR_2(0xA1),
        UFR_FT_STATUS_ERROR_3(0xA2),
        UFR_FT_STATUS_ERROR_4(0xA3),
        UFR_FT_STATUS_ERROR_5(0xA4),
        UFR_FT_STATUS_ERROR_6(0xA5),
        UFR_FT_STATUS_ERROR_7(0xA6),
        UFR_FT_STATUS_ERROR_8(0xA7),
        UFR_FT_STATUS_ERROR_9(0xA8),
        UFR_WRONG_NDEF_CARD_FORMAT(0x80),
        UFR_NDEF_MESSAGE_NOT_FOUND(0x81),
        UFR_NDEF_UNSUPPORTED_CARD_TYPE(0x82),
        UFR_NDEF_CARD_FORMAT_ERROR(0x83),
        UFR_MAD_NOT_ENABLED(0x84),
        UFR_MAD_VERSION_NOT_SUPPORTED(0x85),
        UFR_DEVICE_WRONG_HANDLE(0x100),
        UFR_DEVICE_INDEX_OUT_OF_BOUND(0x101),
        UFR_DEVICE_ALREADY_OPENED(0x102),
        UFR_DEVICE_ALREADY_CLOSED(0x103),
        UFR_DEVICE_IS_NOT_CONNECTED(0x104),
        UFR_NOT_NXP_GENUINE(0x200),
        UFR_OPEN_SSL_DYNAMIC_LIB_FAILED(0x201),
        UFR_OPEN_SSL_DYNAMIC_LIB_NOT_FOUND(0x202),
        UFR_NOT_IMPLEMENTED(0x1000),
        UFR_COMMAND_FAILED(0x1001),
        UFR_LIB_TIMEOUT_ERR(0x1002),
        UFR_FILE_SYSTEM_ERROR(0x1003),
        UFR_FILE_SYSTEM_PATH_NOT_EXISTS(0x1004),
        UFR_FILE_NOT_EXISTS(0x1005),
        UFR_APDU_JC_APP_NOT_SELECTED(0x6000),
        UFR_APDU_JC_APP_BUFF_EMPTY(0x6001),
        UFR_APDU_WRONG_SELECT_RESPONSE(0x6002),
        UFR_APDU_WRONG_KEY_TYPE(0x6003),
        UFR_APDU_WRONG_KEY_SIZE(0x6004),
        UFR_APDU_WRONG_KEY_PARAMS(0x6005),
        UFR_APDU_WRONG_SIGNING_ALGORITHM(0x6006),
        UFR_APDU_PLAIN_TEXT_MAX_SIZE_EXCEEDED(0x6007),
        UFR_APDU_UNSUPPORTED_KEY_SIZE(0x6008),
        UFR_APDU_UNSUPPORTED_ALGORITHMS(0x6009),
        UFR_APDU_PKI_OBJECT_NOT_FOUND(0x600A),
        UFR_APDU_SW_TAG(0x000A0000),
        UFR_APDU_SW_WRONG_LENGTH(0x000A6700),
        UFR_APDU_SW_SECURITY_STATUS_NOT_SATISFIED(0x000A6982),
        UFR_APDU_SW_AUTHENTICATION_METHOD_BLOCKED(0x000A6983),
        UFR_APDU_SW_DATA_INVALID(0x000A6984),
        UFR_APDU_SW_CONDITIONS_NOT_SATISFIED(0x000A6985),
        UFR_APDU_SW_WRONG_DATA(0x000A6A80),
        UFR_APDU_SW_FILE_NOT_FOUND(0x000A6A82),
        UFR_APDU_SW_RECORD_NOT_FOUND(0x000A6A83),
        UFR_APDU_SW_DATA_NOT_FOUND(0x000A6A88),
        UFR_APDU_SW_ENTITY_ALREADY_EXISTS(0x000A6A89),
        UFR_APDU_SW_INS_NOT_SUPPORTED(0x000A6D00),
        UFR_APDU_SW_NO_PRECISE_DIAGNOSTIC(0x000A6F00),
        READER_ERROR(2999),
        NO_CARD_DETECTED(3000),
        CARD_OPERATION_OK(3001),
        WRONG_KEY_TYPE(3002),
        KEY_AUTH_ERROR(3003),
        CARD_CRYPTO_ERROR(3004),
        READER_CARD_COMM_ERROR(3005),
        PC_READER_COMM_ERROR(3006),
        COMMIT_TRANSACTION_NO_REPLY(3007),
        COMMIT_TRANSACTION_ERROR(3008),
        DESFIRE_CARD_NO_CHANGES(0x0C0C),
        DESFIRE_CARD_OUT_OF_EEPROM_ERROR(0x0C0E),
        DESFIRE_CARD_ILLEGAL_COMMAND_CODE(0x0C1C),
        DESFIRE_CARD_INTEGRITY_ERROR(0x0C1E),
        DESFIRE_CARD_NO_SUCH_KEY(0x0C40),
        DESFIRE_CARD_LENGTH_ERROR(0x0C7E),
        DESFIRE_CARD_PERMISSION_DENIED(0x0C9D),
        DESFIRE_CARD_PARAMETER_ERROR(0x0C9E),
        DESFIRE_CARD_APPLICATION_NOT_FOUND(0x0CA0),
        DESFIRE_CARD_APPL_INTEGRITY_ERROR(0x0CA1),
        DESFIRE_CARD_AUTHENTICATION_ERROR(0x0CAE),
        DESFIRE_CARD_ADDITIONAL_FRAME(0x0CAF),
        DESFIRE_CARD_BOUNDARY_ERROR(0x0CBE),
        DESFIRE_CARD_PICC_INTEGRITY_ERROR(0x0CC1),
        DESFIRE_CARD_COMMAND_ABORTED(0x0CCA),
        DESFIRE_CARD_PICC_DISABLED_ERROR(0x0CCD),
        DESFIRE_CARD_COUNT_ERROR(0x0CCE),
        DESFIRE_CARD_DUPLICATE_ERROR(0x0CDE),
        DESFIRE_CARD_EEPROM_ERROR_DES(0x0CEE),
        DESFIRE_CARD_FILE_NOT_FOUND(0x0CF0),
        DESFIRE_CARD_FILE_INTEGRITY_ERROR(0x0CF1);

        private int value;

        private ERRORCODES(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }
}