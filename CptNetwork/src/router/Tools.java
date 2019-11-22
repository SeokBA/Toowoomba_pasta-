package router;

import java.io.UnsupportedEncodingException;

public class Tools {
    public String setFlag(String flag){
        String flagTemp = "";
        if(flag.contains("U"))
            flagTemp += "U";
        if(flag.contains("G"))
            flagTemp += "G";
        if(flag.contains("H"))
            flagTemp += "H";
        if(flag.contains("D"))
            flagTemp += "D";
        if(flag.contains("M"))
            flagTemp += "M";
        return flagTemp;
    }

    byte[] intToByte2(int value) {
        byte[] temp = new byte[2];
        temp[1] = (byte) (value >> 8);
        temp[0] = (byte) value;
        return temp;
    }

    byte[] intToByte4(int value) {
        byte[] temp = new byte[4];
        temp[0] |= (byte) ((value & 0xFF000000) >> 24);
        temp[1] |= (byte) ((value & 0xFF0000) >> 16);
        temp[2] |= (byte) ((value & 0xFF00) >> 8);
        temp[3] |= (byte) (value & 0xFF);
        return temp;
    }

    byte[] hexToByte2(int hexValue) {
        String hex = "0x" + hexValue;
        int toInt = Integer.decode(hex);
        return intToByte2(toInt);
    }

    public String ipAddrByteToString(byte[] addr) { // pt 포함
        StringBuilder sb = new StringBuilder();
        int temp;
        for (int j = 0; j < addr.length; j++) {
            if (sb.length() != 0)
                sb.append('.');
            if (addr[j] < 0)
                temp = addr[j] + 256;
            else
                temp = addr[j];
            sb.append(temp);
        }
        return sb.toString();
    }

    String hwAddrByteToString(byte[] addr, char s) {
        StringBuilder sb = new StringBuilder();
        int temp = 0;
        for (int j = 0; j < addr.length; j++) {
            if (sb.length() != 0)
                sb.append(s);
            if (addr[j] >= 0 && addr[j] < 16)
                sb.append('0');
            if (addr[j] < 0)
                temp = addr[j] + 256;
            else
                temp = addr[j];
            String hex = Integer.toHexString(temp).toUpperCase();
            sb.append(hex);
        }

        return sb.toString();
    }

    private byte[] ipAddrStringToByte(String address) {
        String[] Stringarray = address.split("\\.");
        byte[] intarray = new byte[4];
        for (int i = 0; i < intarray.length; i++) {
            intarray[i] = (byte) Integer.parseInt(Stringarray[i]);
        }
        return intarray;
    }

    public byte[] hwAddrStringToByte(String address) {
        byte addr[] = new byte[6];
        String[] sp = address.split(":");
        for (int i = 0; i < sp.length; i++) {
            byte toByte;
            int toInt = Integer.decode("0x" + sp[i]);
            if (toInt > 127)
                toByte = (byte) (toInt - 256);
            else
                toByte = (byte) toInt;
            addr[i] = toByte;
        }
        return addr;
    }

    String byteHWAddrToString(byte[] addr) {
        StringBuilder sb = new StringBuilder();
        int temp = 0;
        for (int j = 0; j < addr.length; j++) {
            if (sb.length() != 0)
                sb.append(':');
            if (addr[j] >= 0 && addr[j] < 16)
                sb.append('0');
            if (addr[j] < 0)
                temp = addr[j] + 256;
            else
                temp = addr[j];
            String hex = Integer.toHexString(temp).toUpperCase();
            sb.append(hex);
        }

        return sb.toString();
    }

    String bytePTAddrToString(byte[] addr) {
        StringBuilder sb = new StringBuilder();
        for (int j = 0; j < addr.length; j++) {
            if (sb.length() != 0)
                sb.append('.');
            if (addr[j] < 0)
                sb.append(addr[j] + 256);
            else
                sb.append(addr[j]);
        }

        return sb.toString();
    }


    public byte[] string2HWaddr(String address) {
        byte addr[]=new byte[6];
        String[] sp = address.split(":");
        for (int i = 0; i < sp.length; i++) {
            byte toByte;
            int toInt = Integer.decode("0x" + sp[i]);
            if (toInt > 127)
                toByte = (byte) (toInt - 256);
            else
                toByte = (byte) toInt;
            addr[i] = toByte;
        }
        return addr;
    }

    public byte[] ipStringtoByte(String address){
        String[] Stringarray = address.split("\\.");
        byte[] intarray = new byte[4];
        for(int i=0 ; i<intarray.length; i++) {
            intarray[i] = (byte)Integer.parseInt(Stringarray[i]);
        }
        return intarray;
    }

    public byte[] stringHWaddrToByte(String address){
        String[] hexarray = address.split(":");
        StringBuilder hexstring = new StringBuilder();
        for(int i=0 ; i<hexarray.length; i++) {
            hexstring.append(hexarray[i]);
        }
        String hexString = hexstring.toString();
        return hexStringToByteArray(hexString);
    }

    public byte[] hexStringToByteArray(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character.digit(s.charAt(i+1), 16));
        }
        return data;
    }

    public byte[] extractSelectPart(byte[] input, int startIndex, int finishIndex){
        byte[] extracted=new byte[finishIndex-startIndex];
        for(int i=startIndex; i<finishIndex; i++){
            extracted[i-startIndex]=input[i];
        }
        return extracted;
    }

    public byte[] removeCappHeader(byte[] input, int length, int headerLength) {
        byte[] buf = new byte[length-headerLength];
        for(int i=0; i<length-headerLength; i++){
            buf[i]=input[i+headerLength];
        }
        return buf;
    }
}
