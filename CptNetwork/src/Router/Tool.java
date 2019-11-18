package Router;

import java.io.UnsupportedEncodingException;

public class Tool {
    private String toUTF8(String kor)
            throws UnsupportedEncodingException {
        return new String(kor.getBytes("MS949"), "UTF-8");
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

    public String ipAddrByteToString(byte[] addr) {
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

    String ptAddrByteToString(byte[] addr) {
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
}
