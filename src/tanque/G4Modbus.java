package tanque;

import android.serialport.SerialPort;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
* Created by CCR, JFCC on 7/9/13.
*/
public class G4Modbus {
    InputStream Rx  = null;
    OutputStream Tx = null;

    boolean ArrayBits[][] = new boolean[4][8];
    int ArrayAI[] = new int [2];
    boolean ForceSingleCoilSuccess;
    String CoilCommand = "";

    // TimeOut = TIMEOUT_VALUE * 100 ms
    public static int TIMEOUT_VALUE = 3;
    private String DeviceAddress;
    private int Step = 3;

    /*
     * Constructor
     * Author: CCR, JCC
     *
     * */
    public G4Modbus(String port_path, int baudrate, int address){
        if (address > 9){
            DeviceAddress = "0x"+String.valueOf(address);
        }
        else{
            DeviceAddress = "0x0"+String.valueOf(address);
        }
        try {
            SerialPort port = new SerialPort(new File(port_path),baudrate);
            Tx = port.getOutputStream();
            Rx = port.getInputStream();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
    * This method should be called by the user every time the information update is needed.
    * Author: CCR, JCC
    *
    * */
    public void HeartBeat (){
        switch (Step){
            case 1:
                /* G4Modbus command 1 (? coils) */
                readCoilPolling();
                break;
            case 3:
                /* G4Modbus command 3 (16 words) */
                readHoldingRegistersPolling();
                break;
            case 10:
                /* G4Modbus No polling */
                Log.i("G4MB - HeartBeat","No polling");
                SendCommand(CoilCommand);
                Step = 3;
                break;
            default:
                break;
        }
    }

    /*
    * This method returns the value of a given DI
    * Usage: getBit(PU#|SD#|BC#|ED#)
    * Example: getBit(ED2) returns Bit 2 of EntradasDigitales
    * Author: CCR, JCC
    *
    * */
    public boolean getBit (String input){
        if(input.substring(0,2).equals("PU")){
            return ArrayBits[0][8-Integer.valueOf(input.substring(2,3))];
        }else if(input.substring(0,2).equals("SD")){
            return ArrayBits[1][Integer.valueOf(input.substring(2,3))-1];
        }else if(input.substring(0,2).equals("BC")){
            return ArrayBits[2][7-(Integer.valueOf(input.substring(2,3)))];
        }else if (input.substring(0,2).equals("ED")){
            return ArrayBits[3][Integer.valueOf(input.substring(2,3))-1];
        }
        //TODO throw exception implementation needed
        return false;
    }

    /*
    * This method returns the value of a given AI
    * Author: CCR, JCC
    *
    * */
    public int getAI (int input){
        if ((input >= 0) || (input <= 1)){
            return ArrayAI[input];
        }
        else {
            /* Error! ESC only has 2 AI */
            return -1;
        }
    }

    /*
    * This method returns the value of a given DO
    * Author: CCR, JCC
    *
    * */
    public boolean setDO (int output, boolean value){
        return false;
    }

    /*
    * This method returns the value of a given Coil
    * output: 0 for first coil. 8 coils
    * Author: CCR, JCC
    *
    * */
    public boolean setCoil (int output, boolean value){
        String out;
        String val;

        if (output < 9){
            out = "0x0"+String.valueOf(output-1);
        }
        else {
            // Input error
            return false;
        }
        if (value){
            val = "0xFF";
        }
        else {
            val = "0x00";
        }

        // Turns polling off
        Step = 10;
        CoilCommand = DeviceAddress+",0x05,0x00,"+out+","+val+",0x00";
        return ForceSingleCoilSuccess;
    }

   /*
   * This method Polls all devices configured to get coils
   * Author: CCR, JCC
   *
   * */
    private void readCoilPolling(){
            //TODO Check the correct cmd
            SendCommand("0x00,0x01,0x00,0x00,0x00,0x10");
    }

   /*
   * This method polls all devices configured to get Holding registers
   * Author: CCR, JCC
   *
   * */
    private void readHoldingRegistersPolling(){
        SendCommand("0x01,0x03,0x00,0x02,0x00,0x10");
    }

    /*
    * This method decodes commands from string (0x08) to byte[]
    * Author: CCR, JCC
    *
    * */
    private byte[] ComandosExploracion(String cmd){
        byte[] comando= new byte[6];
        byte[] comandoCRC= new byte[8];

        comando[0]= (byte) (int) Integer.decode(cmd.substring(0, 4));
        comando[1]= (byte) (int) Integer.decode(cmd.substring(5, 9));
        comando[2]= (byte) (int) Integer.decode(cmd.substring(10, 14));
        comando[3]= (byte) (int) Integer.decode(cmd.substring(15,19));
        comando[4]= (byte) (int) Integer.decode(cmd.substring(20,24));
        comando[5]= (byte) (int) Integer.decode(cmd.substring(25,29));
        System.arraycopy(comando,0,comandoCRC,0,comando.length);
        System.arraycopy(CRC16IBM(comando),0,comandoCRC,comando.length,2);

        return comandoCRC;
    }

    /*
     * Sends and Receives G4Modbus command
     * Author: CCR, JCC
     *
     * */
    private void SendCommand(String command){
        final int NUMBER_OF_DATA_BYTES_LOCATION = 2;
        final int CRC_NUMBER_OF_BYTES = 2;
        final int ADDRESS_BYTE = 1;
        final int FUNCTION_BYTE = 1;
        final int NUMBER_OF_BYTES_BYTE = 1;
        int bytesToRx = ADDRESS_BYTE + FUNCTION_BYTE + NUMBER_OF_BYTES_BYTE + CRC_NUMBER_OF_BYTES;
        final int BUFFER_SIZE = 256;
        byte[] buffer = new byte[BUFFER_SIZE];
        int i = 0;
        int timeout = 0;
        boolean cmdFiveOrSixDetected = false;
        try {
            // Tx
            Tx.flush();
            Tx.write(ComandosExploracion(command));
            Log.i("G4MB - SendCmd",""+command);
            // Rx
            do {
                if ( Rx.available() != 0 ) {

                    /* Blocking until byte Rx */
                    buffer[i] = (byte) Rx.read();
                    timeout = 0;

                    if (i == NUMBER_OF_DATA_BYTES_LOCATION){
                        bytesToRx += Integer.valueOf(buffer[i]);
                        //Command 5 and 6 response is the echo of the request
                        if(buffer[1] == 5 || buffer[1] == 6){
                            bytesToRx += 3;
                            cmdFiveOrSixDetected = true;
                        }
                    }
                    i++;
                }
                else {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    timeout++;
                    if (timeout >= TIMEOUT_VALUE){
                        //TODO throw timeout exception
                        Log.i("G4MB - Rx","Time Out!");
                        return;
                    }
                }
            } while (i < bytesToRx);
            byte[] WithCRC = new byte[i];
            System.arraycopy(buffer, 0, WithCRC, 0, i);
            if(!cmdFiveOrSixDetected){
                // Check CRC
                if (CRCValido(WithCRC)){
                    // Process Result
                    byte[] NoCRC = new byte[i-2];
                    System.arraycopy(WithCRC,0,NoCRC,0,i-2);
                    ProcessResult(NoCRC);
                }
                else{
                    Log.i("G4MB - CRC","Invalid CRC!");
                }
            }
            else{
                ProcessResult(WithCRC);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /*
     * Process the response of a G4Modbus slave, fills ArrayBits and
     * ArrayAI.
     * Author: CCR, JCC
     *
     * */
    private void ProcessResult (byte[] response){
        switch (response[1]) {

            case 3: //Read Holding Register
                BitState myBits = new BitState();

                /* Process Bits */
                String tempRes = "";
                for (int i=0;i<response.length;i++){
                    tempRes=tempRes+"|"+String.valueOf(response[i]);
                }
                byte[] BitAccessible = new byte[4];
                System.arraycopy(response, 3, BitAccessible, 0, 4);
                String ArrayBitsToPrint = "";
                for (int B=0;B<4;B++){
                    for (int b=0;b<8;b++){
                        ArrayBits[B][b] = myBits.getBitState(BitAccessible[B],b);
                        int temp;
                        if(ArrayBits[B][b]) temp = 1; else temp = 0;
                        ArrayBitsToPrint = ArrayBitsToPrint+"|"+temp;
                    }
                    ArrayBitsToPrint = ArrayBitsToPrint+"|\n";
                }
                Log.i("G4MB - DI ","Bits:\n"+ArrayBitsToPrint);

                /* Process Analog Inputs */
                byte[] AnalogInputsBytes = new byte[4];
                System.arraycopy(response, 31, AnalogInputsBytes, 0, 4);

                // &0xFF will store the byte value in int to make it "unsigned" (the sign bit
                // goes to the MSb of the 32 bits int)

                ArrayAI[0] = (256*(AnalogInputsBytes[0]&0xFF)) + (AnalogInputsBytes[1]&0xFF);
                ArrayAI[1] = (256*(AnalogInputsBytes[2]&0xFF)) + (AnalogInputsBytes[3]&0xFF);
                Log.i("G4MB - AI ","AI1: "+ArrayAI[0]);
                Log.i("G4MB - AI ","AI2: "+ArrayAI[1]);
                break;

            case 5: //Force Single Coil
                byte[] CC = ComandosExploracion(CoilCommand);

                if (Arrays.equals(CC,response)){
                    ForceSingleCoilSuccess = true;
                }
                else {
                    ForceSingleCoilSuccess = false;
                }
                Log.i("G4MB - Force Single Coil","Success? "+ForceSingleCoilSuccess);
        }
    }

    /*
    * This method returns a 2 byte CRC for a given buffer
    * Author: CCR, JCC
    *
    * */
    private byte[] CRC16IBM (byte[] buffer) {

        int[] table = {
                0x0000, 0xC0C1, 0xC181, 0x0140, 0xC301, 0x03C0, 0x0280, 0xC241,
                0xC601, 0x06C0, 0x0780, 0xC741, 0x0500, 0xC5C1, 0xC481, 0x0440,
                0xCC01, 0x0CC0, 0x0D80, 0xCD41, 0x0F00, 0xCFC1, 0xCE81, 0x0E40,
                0x0A00, 0xCAC1, 0xCB81, 0x0B40, 0xC901, 0x09C0, 0x0880, 0xC841,
                0xD801, 0x18C0, 0x1980, 0xD941, 0x1B00, 0xDBC1, 0xDA81, 0x1A40,
                0x1E00, 0xDEC1, 0xDF81, 0x1F40, 0xDD01, 0x1DC0, 0x1C80, 0xDC41,
                0x1400, 0xD4C1, 0xD581, 0x1540, 0xD701, 0x17C0, 0x1680, 0xD641,
                0xD201, 0x12C0, 0x1380, 0xD341, 0x1100, 0xD1C1, 0xD081, 0x1040,
                0xF001, 0x30C0, 0x3180, 0xF141, 0x3300, 0xF3C1, 0xF281, 0x3240,
                0x3600, 0xF6C1, 0xF781, 0x3740, 0xF501, 0x35C0, 0x3480, 0xF441,
                0x3C00, 0xFCC1, 0xFD81, 0x3D40, 0xFF01, 0x3FC0, 0x3E80, 0xFE41,
                0xFA01, 0x3AC0, 0x3B80, 0xFB41, 0x3900, 0xF9C1, 0xF881, 0x3840,
                0x2800, 0xE8C1, 0xE981, 0x2940, 0xEB01, 0x2BC0, 0x2A80, 0xEA41,
                0xEE01, 0x2EC0, 0x2F80, 0xEF41, 0x2D00, 0xEDC1, 0xEC81, 0x2C40,
                0xE401, 0x24C0, 0x2580, 0xE541, 0x2700, 0xE7C1, 0xE681, 0x2640,
                0x2200, 0xE2C1, 0xE381, 0x2340, 0xE101, 0x21C0, 0x2080, 0xE041,
                0xA001, 0x60C0, 0x6180, 0xA141, 0x6300, 0xA3C1, 0xA281, 0x6240,
                0x6600, 0xA6C1, 0xA781, 0x6740, 0xA501, 0x65C0, 0x6480, 0xA441,
                0x6C00, 0xACC1, 0xAD81, 0x6D40, 0xAF01, 0x6FC0, 0x6E80, 0xAE41,
                0xAA01, 0x6AC0, 0x6B80, 0xAB41, 0x6900, 0xA9C1, 0xA881, 0x6840,
                0x7800, 0xB8C1, 0xB981, 0x7940, 0xBB01, 0x7BC0, 0x7A80, 0xBA41,
                0xBE01, 0x7EC0, 0x7F80, 0xBF41, 0x7D00, 0xBDC1, 0xBC81, 0x7C40,
                0xB401, 0x74C0, 0x7580, 0xB541, 0x7700, 0xB7C1, 0xB681, 0x7640,
                0x7200, 0xB2C1, 0xB381, 0x7340, 0xB101, 0x71C0, 0x7080, 0xB041,
                0x5000, 0x90C1, 0x9181, 0x5140, 0x9301, 0x53C0, 0x5280, 0x9241,
                0x9601, 0x56C0, 0x5780, 0x9741, 0x5500, 0x95C1, 0x9481, 0x5440,
                0x9C01, 0x5CC0, 0x5D80, 0x9D41, 0x5F00, 0x9FC1, 0x9E81, 0x5E40,
                0x5A00, 0x9AC1, 0x9B81, 0x5B40, 0x9901, 0x59C0, 0x5880, 0x9841,
                0x8801, 0x48C0, 0x4980, 0x8941, 0x4B00, 0x8BC1, 0x8A81, 0x4A40,
                0x4E00, 0x8EC1, 0x8F81, 0x4F40, 0x8D01, 0x4DC0, 0x4C80, 0x8C41,
                0x4400, 0x84C1, 0x8581, 0x4540, 0x8701, 0x47C0, 0x4680, 0x8641,
                0x8201, 0x42C0, 0x4380, 0x8341, 0x4100, 0x81C1, 0x8081, 0x4040,
        };

        int crc = 0xFFFF;
        for (byte b : buffer) {
            crc = (crc >>> 8) ^ table[(crc ^ b) & 0xff];
        }
        byte[] arrayCRC = new byte[2];
        arrayCRC [0] = (byte) (crc & 0xff);
        arrayCRC [1] = (byte) (crc >> 8 & 0xff);
        return(arrayCRC);
    }

    /*
    * This method verifies that the received buffer has a correct CRC
    * Author: CCR, JCC
    *
    * */
    private boolean CRCValido (byte[] buffer){
        try {
            if (buffer.length > 2){
				/* Saca Mensaje de lo Rx */
                byte[] mensaje = new byte[buffer.length - 2];
                System.arraycopy(buffer,0,mensaje,0,buffer.length - 2);

				/* Calcula CRC */
                byte[] temp = CRC16IBM(mensaje);

				/* Saca CRC de lo Rx */
                byte[] CRC = new byte[2];
                System.arraycopy(buffer,buffer.length-2,CRC,0,2);

				/* Compara con lo Rx */
                return(Arrays.equals(CRC, temp));
            }else{
                return(false);
            }
        } catch (NegativeArraySizeException e) {
            e.printStackTrace();
            return(false);
        }
    }
}
