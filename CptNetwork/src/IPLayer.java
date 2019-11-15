import java.util.ArrayList;

public class IPLayer implements BaseLayer{
    public int nUpperLayerCount=0;
    public String pLayerName=null;
    public BaseLayer p_UnderLayer=null;
    public ArrayList<BaseLayer> p_aUpperLayer=new ArrayList<>();

    public IPLayer(String pName) {
        pLayerName = pName;
    }

    private class _IP_ADDR{
        private byte[] addr = new byte[4];
        public _IP_ADDR() {
            this.addr[0]=(byte)0x00;
            this.addr[1]=(byte)0x00;
            this.addr[2]=(byte)0x00;
            this.addr[3]=(byte)0x00;
        }
    }

    private class _IP_HEADER{
        byte ipVersion; // 1byte
        byte ipType; // 1byte
        byte[] ipLength; // 2byte 4
        byte[] ipId; // 2byte 6
        byte[] ipFragOff; // 2byte 8
        byte ipTtl;
        byte ipProto;
        byte ipCheckSum;
        _IP_ADDR ipSrc;
        _IP_ADDR ipDst;
        byte[] ipData;

        public _IP_HEADER() {
            this.ipVersion=0x00;
            this.ipType=0x00;
            this.ipLength=new byte[2];
            this.ipId=new byte[2];
            this.ipFragOff=new byte[2];
            this.ipTtl=0x00;
            this.ipProto=0x00;
            this.ipCheckSum=0x00;
            this.ipSrc = new _IP_ADDR();
            this.ipDst = new _IP_ADDR();
            this.ipData=null;
        }
    }

    _IP_HEADER ipHeader = new _IP_HEADER();

    public void setDstAddress(String address) {
        address=address.trim(); // 혹시나 붙을 공백 제거
        String[] sp=address.split(".");
        for(int i=0; i<sp.length; i++){
            byte toByte;
            int toInt = Integer.parseInt(sp[i]);
            if(toInt>127)
                toByte=(byte)(toInt-256);
            else
                toByte=(byte)toInt;
            ipHeader.ipDst.addr[i]=toByte;
        }
    }

    public void setSrcAddress(String address) {
        address=address.trim(); // 혹시나 붙을 공백 제거
        String[] sp=address.split(".");
        for(int i=0; i<sp.length; i++){
            byte toByte;
            int toInt = Integer.parseInt(sp[i]);
            if(toInt>127)
                toByte=(byte)(toInt-256);
            else
                toByte=(byte)toInt;
            ipHeader.ipSrc.addr[i]=toByte;
        }
    }

    public boolean IsItMyPacket(byte[] input) {
        for (int i = 0; i < 6; i++) {
            if (ipHeader.ipSrc.addr[i] != input[15 + i])
                return false;
        }
        return true;
    }

    public boolean Send(byte[] input, int length) {
        byte[] c=ObjToByte(ipHeader, input, length);
        this.GetUnderLayer().Send(c,length+19);
        return true;
    }


    public byte[] ObjToByte(_IP_HEADER Header, byte[] input, int length) {
        byte[] buf = new byte[length + 19];
        buf[0]=Header.ipVersion;
        buf[1]=Header.ipType;
        for(int i=2; i<4; i++)
            buf[i] = Header.ipLength[i-2];
        for(int i=4; i<6; i++)
            buf[i] = Header.ipId[i-4];
        for(int i=6; i<8; i++)
            buf[i] = Header.ipFragOff[i-6];
        buf[8]=Header.ipTtl;
        buf[9]=Header.ipProto;
        buf[10]=Header.ipCheckSum;

        for(int i=11; i<15; i++)
            buf[i] = Header.ipSrc.addr[i-11];
        for(int i=15; i<19; i++)
            buf[i] = Header.ipDst.addr[i-15];
        for (int i = 0; i < input.length; i++)
            buf[19 + i] = input[i];
        return buf;
    }

    public byte[] RemoveCappHeader(byte[] input, int length) {
        byte[] buf = new byte[length-19];
        for(int i=0; i<length-19; i++){
            buf[i]=input[i+19];
        }
        return buf;
    }

    public synchronized boolean Receive(byte[] input) {
        if(IsItMyPacket(input)) // 자기껀 버림
            return false;
        byte[] data;
        for(int i=0; i<15; i++){
            if(input[i]!=ipHeader.ipSrc.addr[i])
                return false;
        }
        data = RemoveCappHeader(input, input.length);
        this.GetUpperLayer(0).Receive(data);
        return true;
    }

    byte[] intToByte2(int value) {
        byte[] temp = new byte[2];
        temp[1] = (byte) (value >> 8);
        temp[0] = (byte) value;
        return temp;
    }

    byte[] intToByte4(int value) {
        byte[] temp = new byte[4];
        temp[0]|=(byte)((value&0xFF000000)>>24);
        temp[1]|=(byte)((value&0xFF0000)>>16);
        temp[2]|=(byte)((value&0xFF00)>>8);
        temp[3]|=(byte)(value&0xFF);
        return temp;
    }

    @Override
    public String GetLayerName() {
        return pLayerName;
    }

    @Override
    public BaseLayer GetUnderLayer() {
        if (p_UnderLayer == null)
            return null;
        return p_UnderLayer;
    }

    @Override
    public BaseLayer GetUpperLayer(int nindex) {
        if (nindex < 0 || nindex > nUpperLayerCount || nUpperLayerCount < 0)
            return null;
        return p_aUpperLayer.get(nindex);
    }

    @Override
    public void SetUnderLayer(BaseLayer pUnderLayer) {
        if (pUnderLayer == null)
            return;
        this.p_UnderLayer = pUnderLayer;
    }

    @Override
    public void SetUpperLayer(BaseLayer pUpperLayer) {
        if (pUpperLayer == null)
            return;
        this.p_aUpperLayer.add(nUpperLayerCount++, pUpperLayer);
    }

    @Override
    public void SetUpperUnderLayer(BaseLayer pUULayer) {
        this.SetUpperLayer(pUULayer);
        pUULayer.SetUnderLayer(this);
    }
}
