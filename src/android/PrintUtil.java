package android_serialport_api;

import org.apache.cordova.*;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidParameterException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import android_serialport_api.SerialPort;
import android_serialport_api.SerialPort;
import android_serialport_api.SerialPortFinder;

public class PrintUtil extends CordovaPlugin {
    private CallbackContext mCallbackContext;
    private static SerialPort mSerialPortOp = null;
    protected OutputStream mOutputStream;

    public static SerialPort getSerialPort() throws SecurityException, IOException, InvalidParameterException {
        if (mSerialPortOp == null)
            mSerialPortOp = new SerialPort(new File("/dev/ttyS1"), 115200, 0, true);
        return mSerialPortOp;
    }

    public static void closeSerialPort() {
        if (mSerialPortOp != null) {
            mSerialPortOp.close();
            mSerialPortOp = null;
        }
    }

    public static byte[] getGbk(String paramString) {
        byte[] arrayOfByte = null;
        try {
            arrayOfByte = paramString.getBytes("GBK"); // 斛剕温婓try啭符褫眕
        } catch (Exception ex) {
            ;
        }
        return arrayOfByte;

    }

    public static byte[] CutPaper() // ピ祧˙ GS V 66D 0D
    {
        byte[] arrayOfByte = new byte[] { 0x1D, 0x56, 0x42, 0x00 };
        return arrayOfByte;
    }

    public static byte[] PrintBarcode(String paramString) // Tungsten GS k
    {
        byte[] arrayOfByte = new byte[13 + paramString.length()];
        // Query only
        arrayOfByte[0] = 0x1D;
        arrayOfByte[1] = 'h';
        arrayOfByte[2] = 0x60; // 1善255

        // 扢沭沭遵遵
        arrayOfByte[3] = 0x1D;
        arrayOfByte[4] = 'w';
        arrayOfByte[5] = 2; // 2善6

        // 扢沭沭沭沭沭沭
        arrayOfByte[6] = 0x1D;
        arrayOfByte[7] = 'H';
        arrayOfByte[8] = 2; // 0善3

        // Lake 荂39沭tungsten
        arrayOfByte[9] = 0x1D;
        arrayOfByte[10] = 'k';
        arrayOfByte[11] = 0x45;
        arrayOfByte[12] = ((byte) paramString.length());
        System.arraycopy(paramString.getBytes(), 0, arrayOfByte, 13, paramString.getBytes().length);
        return arrayOfByte;
    }

    public static byte[] setAlignCenter(char paramChar) // 勤ぅ ESC a
    {
        byte[] arrayOfByte = new byte[3];
        arrayOfByte[0] = 0x1B;
        arrayOfByte[1] = 0x61;

        switch (paramChar) // 1-酘勤ぅ˙2-懈笢勤ぅ˙3-衵勤ぅ
        {
        case '2':
            arrayOfByte[2] = 0x01;
            break;
        case '3':
            arrayOfByte[2] = 0x02;
            break;
        default:
            arrayOfByte[2] = 0x00;
            break;
        }
        return arrayOfByte;
    }

    public static byte[] setBold(boolean paramBoolean) // Floor cotton ESC E
    {
        byte[] arrayOfByte = new byte[3];
        arrayOfByte[0] = 0x1B;
        arrayOfByte[1] = 0x45;
        if (paramBoolean) // Barrel
        {
            arrayOfByte[2] = 0x01;
        } else {
            arrayOfByte[2] = 0x00;
        }
        return arrayOfByte;
    }

    public static byte[] setLineH(int h) // 扢离俴ㄛㄛh 0-255
    {
        byte[] arrayOfByte = new byte[3];
        arrayOfByte[0] = 0x1B;
        arrayOfByte[1] = 0x33;
        arrayOfByte[2] = (byte) (h & 255);
        return arrayOfByte;
    }

    public static byte[] setWH(char paramChar) // GS ! 扢荂趼荂趼荂趼荂趼
    {
        byte[] arrayOfByte = new byte[3]; // GS ! 11H 捷遵捷詢
        arrayOfByte[0] = 0x1D;
        arrayOfByte[1] = 0x21;

        switch (paramChar) // 1-拸˙2-捷遵˙3-捷詢˙ 4-捷遵捷詢
        {
        case '2':
            arrayOfByte[2] = 0x10;
            break;
        case '3':
            arrayOfByte[2] = 0x01;
            break;
        case '4':
            arrayOfByte[2] = 0x11;
            break;
        default:
            arrayOfByte[2] = 0x00;
            break;
        }

        return arrayOfByte;
    }

    /***************************************************************************
     * add by yidie 2012-01-10 髡夔ㄩ扢离湖荂橈勤弇离 統杅ㄩ int 婓絞ゴ俴ㄛ隅弇嫖梓弇离ㄛ龰硉毓峓0祫576萸 佽隴ㄩ
     * 婓趼极都寞湮苤狟ㄛ藩犖趼24萸ㄛ荎恅趼睫12萸 ⺼弇衾菴n跺犖趼綴ㄛ寀position=24*n ⺼弇衾菴n跺圉褒趼睫綴ㄛ寀position=12*n
     ****************************************************************************/

    public static byte[] setCusorPosition(int position) {
        byte[] returnText = new byte[4]; // 絞ゴ俴ㄛ扢离橈勤湖荂弇离 ESC $ bL bH
        returnText[0] = 0x1B;
        returnText[1] = 0x24;
        returnText[2] = (byte) (position % 256);
        returnText[3] = (byte) (position / 256);
        return returnText;
    }

    public boolean printJSON(JSONArray ob) {
        byte[] byteArray = (ob.toString()).getBytes();
        return (printBytes(byteArray));
    }

    public static boolean printBytes(byte[] printText) {
        boolean returnValue = true;
        try {
            OutputStream mOutputStream = getSerialPort().getOutputStream();

            // printText=CutPaper();
            mOutputStream.write(printText);
        } catch (Exception ex) {
            returnValue = false;
        }
        return returnValue;
    }

    public static boolean printString(String paramString) {
        return printBytes(getGbk(paramString));
    }

    public static boolean printTest() {
        boolean returnValue = true;
        try {
            OutputStream mOutputStream = getSerialPort().getOutputStream();

            byte[] printText = new byte[4];

            printText[0] = 0x1F;
            printText[1] = 0x1B;
            printText[2] = 0x1F;
            printText[3] = 0x53;
            mOutputStream.write(printText, 0, 4);
        } catch (Exception ex) {
            returnValue = false;
        }
        return returnValue;
    }

    public static boolean printDemo() {
        boolean returnValue = true;
        try {
            OutputStream mOutputStream = getSerialPort().getOutputStream();

            int iNum = 0;

            byte[] printText = new byte[1024];
            String strTmp = "";

            byte[] oldText = setAlignCenter('1');
            System.arraycopy(oldText, 0, printText, iNum, oldText.length);
            iNum += oldText.length;

            oldText = setWH('3');
            System.arraycopy(oldText, 0, printText, iNum, oldText.length);
            iNum += oldText.length;

            oldText = getGbk("隆等瘍ㄩ");
            System.arraycopy(oldText, 0, printText, iNum, oldText.length);
            iNum += oldText.length;

            oldText = setWH('4');
            System.arraycopy(oldText, 0, printText, iNum, oldText.length);
            iNum += oldText.length;

            oldText = getGbk("0032");
            System.arraycopy(oldText, 0, printText, iNum, oldText.length);
            iNum += oldText.length;

            oldText = setWH('1');
            System.arraycopy(oldText, 0, printText, iNum, oldText.length);
            iNum += oldText.length;

            oldText = setCusorPosition(324);
            System.arraycopy(oldText, 0, printText, iNum, oldText.length);
            iNum += oldText.length;

            strTmp = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.SIMPLIFIED_CHINESE).format(new Date());
            oldText = getGbk(strTmp + "湖荂\n\n");
            System.arraycopy(oldText, 0, printText, iNum, oldText.length);
            iNum += oldText.length;

            oldText = setAlignCenter('2');
            System.arraycopy(oldText, 0, printText, iNum, oldText.length);
            iNum += oldText.length;

            oldText = setWH('4');
            System.arraycopy(oldText, 0, printText, iNum, oldText.length);
            iNum += oldText.length;

            oldText = setBold(true);
            System.arraycopy(oldText, 0, printText, iNum, oldText.length);
            iNum += oldText.length;

            oldText = getGbk("捶泬极桄虛");
            System.arraycopy(oldText, 0, printText, iNum, oldText.length);
            iNum += oldText.length;

            oldText = setWH('1');
            System.arraycopy(oldText, 0, printText, iNum, oldText.length);
            iNum += oldText.length;

            oldText = getGbk("\n\n");
            System.arraycopy(oldText, 0, printText, iNum, oldText.length);
            iNum += oldText.length;

            oldText = setAlignCenter('1');
            System.arraycopy(oldText, 0, printText, iNum, oldText.length);
            iNum += oldText.length;

            oldText = setBold(false);
            System.arraycopy(oldText, 0, printText, iNum, oldText.length);
            iNum += oldText.length;

            oldText = setWH('3');
            System.arraycopy(oldText, 0, printText, iNum, oldText.length);
            iNum += oldText.length;

            strTmp = new SimpleDateFormat("yyyy-MM-dd", Locale.SIMPLIFIED_CHINESE).format(new Date());
            oldText = getGbk("俋冞奀潔ㄩ" + strTmp + " 鴃辦冞湛\n");
            System.arraycopy(oldText, 0, printText, iNum, oldText.length);
            iNum += oldText.length;

            oldText = getGbk("俋冞華硊ㄩ詢陔Е褪撮笢繚6瘍斐珛湮狪\n");
            System.arraycopy(oldText, 0, printText, iNum, oldText.length);
            iNum += oldText.length;

            oldText = getGbk("備網萇趕ㄩ桲ʊ猿珂汜 13501234567\n");
            System.arraycopy(oldText, 0, printText, iNum, oldText.length);
            iNum += oldText.length;

            oldText = setWH('1');
            System.arraycopy(oldText, 0, printText, iNum, oldText.length);
            iNum += oldText.length;

            oldText = getGbk("        ⅲ靡            等歎    杅講    踢塗\n");
            System.arraycopy(oldText, 0, printText, iNum, oldText.length);
            iNum += oldText.length;

            oldText = getGbk("----------------------------------------------\n");
            System.arraycopy(oldText, 0, printText, iNum, oldText.length);
            iNum += oldText.length;

            oldText = setWH('3');
            System.arraycopy(oldText, 0, printText, iNum, oldText.length);
            iNum += oldText.length;
            // 粕ⅲ靡郔嗣10跺犖趼ㄗ20跺趼睫ㄘ˙等歎郔嗣8跺趼睫˙杅講郔嗣4跺趼睫˙踢塗郔嗣8跺趼睫˙笢潔煦路跪2跺諾跡

            strTmp = "翌摁呫騷票菟";
            oldText = getGbk(strTmp);
            System.arraycopy(oldText, 0, printText, iNum, oldText.length);
            iNum += oldText.length;

            strTmp = "12.00     1     12.00\n";
            oldText = setCusorPosition(552 - 12 * strTmp.length());
            System.arraycopy(oldText, 0, printText, iNum, oldText.length);
            iNum += oldText.length;

            oldText = getGbk(strTmp);
            System.arraycopy(oldText, 0, printText, iNum, oldText.length);
            iNum += oldText.length;

            strTmp = "呫騷阨彆標陑醱婦";
            oldText = getGbk(strTmp);
            System.arraycopy(oldText, 0, printText, iNum, oldText.length);
            iNum += oldText.length;

            strTmp = "12.00     2     24.00\n";
            oldText = setCusorPosition(552 - 12 * strTmp.length());
            System.arraycopy(oldText, 0, printText, iNum, oldText.length);
            iNum += oldText.length;

            oldText = getGbk(strTmp);
            System.arraycopy(oldText, 0, printText, iNum, oldText.length);
            iNum += oldText.length;

            strTmp = "匊④皏尪醱婦";
            oldText = getGbk(strTmp);
            System.arraycopy(oldText, 0, printText, iNum, oldText.length);
            iNum += oldText.length;

            strTmp = "4.00     4     16.00\n";
            oldText = setCusorPosition(552 - 12 * strTmp.length());
            System.arraycopy(oldText, 0, printText, iNum, oldText.length);
            iNum += oldText.length;

            oldText = getGbk(strTmp);
            System.arraycopy(oldText, 0, printText, iNum, oldText.length);
            iNum += oldText.length;

            strTmp = "眅傀⻏朊粥詹";
            oldText = getGbk(strTmp);
            System.arraycopy(oldText, 0, printText, iNum, oldText.length);
            iNum += oldText.length;

            strTmp = "8.00     3     24.00\n";
            oldText = setCusorPosition(552 - 12 * strTmp.length());
            System.arraycopy(oldText, 0, printText, iNum, oldText.length);
            iNum += oldText.length;

            oldText = getGbk(strTmp);
            System.arraycopy(oldText, 0, printText, iNum, oldText.length);
            iNum += oldText.length;

            oldText = setWH('1');
            System.arraycopy(oldText, 0, printText, iNum, oldText.length);
            iNum += oldText.length;

            oldText = getGbk("----------------------------------------------\n");
            System.arraycopy(oldText, 0, printText, iNum, oldText.length);
            iNum += oldText.length;

            oldText = setWH('3');
            System.arraycopy(oldText, 0, printText, iNum, oldText.length);
            iNum += oldText.length;

            strTmp = "冞絃煤ㄩ";
            oldText = getGbk(strTmp);
            System.arraycopy(oldText, 0, printText, iNum, oldText.length);
            iNum += oldText.length;

            strTmp = "3.00\n";
            oldText = setCusorPosition(552 - 12 * strTmp.length());
            System.arraycopy(oldText, 0, printText, iNum, oldText.length);
            iNum += oldText.length;

            oldText = getGbk(strTmp);
            System.arraycopy(oldText, 0, printText, iNum, oldText.length);
            iNum += oldText.length;

            strTmp = "軞數ㄩ";
            oldText = getGbk(strTmp);
            System.arraycopy(oldText, 0, printText, iNum, oldText.length);
            iNum += oldText.length;

            strTmp = "10     79.00\n";
            oldText = setCusorPosition(552 - 12 * strTmp.length());
            System.arraycopy(oldText, 0, printText, iNum, oldText.length);
            iNum += oldText.length;

            oldText = getGbk(strTmp);
            System.arraycopy(oldText, 0, printText, iNum, oldText.length);
            iNum += oldText.length;

            oldText = getGbk("掘蛁ㄩワ樓冞珨棒俶絮脫\n");
            System.arraycopy(oldText, 0, printText, iNum, oldText.length);
            iNum += oldText.length;

            oldText = setWH('1');
            System.arraycopy(oldText, 0, printText, iNum, oldText.length);
            iNum += oldText.length;

            oldText = setAlignCenter('2');
            System.arraycopy(oldText, 0, printText, iNum, oldText.length);
            iNum += oldText.length;

            oldText = getGbk("\n");
            System.arraycopy(oldText, 0, printText, iNum, oldText.length);
            iNum += oldText.length;

            strTmp = new SimpleDateFormat("yyyyMMdd", Locale.SIMPLIFIED_CHINESE).format(new Date()) + "0032";
            oldText = PrintBarcode(strTmp);
            System.arraycopy(oldText, 0, printText, iNum, oldText.length);
            iNum += oldText.length;

            oldText = setAlignCenter('1');
            System.arraycopy(oldText, 0, printText, iNum, oldText.length);
            iNum += oldText.length;

            oldText = getGbk("\n");
            System.arraycopy(oldText, 0, printText, iNum, oldText.length);
            iNum += oldText.length;

            oldText = CutPaper();
            System.arraycopy(oldText, 0, printText, iNum, oldText.length);
            iNum += oldText.length;

            mOutputStream.write(printText);
        } catch (Exception ex) {
            returnValue = false;
        }
        return returnValue;
    }

}
