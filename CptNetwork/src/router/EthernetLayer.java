package router;

import java.util.ArrayList;
import java.util.Arrays;

public class EthernetLayer implements BaseLayer{
    private int nUnderLayerCount = 0;
    private int nUpperLayerCount = 0;
    private String pLayerName = null;
    private BaseLayer p_UnderLayer = null;
    private ArrayList<BaseLayer> p_aUnderLayer = new ArrayList<>();
    private ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<>();
    Tools tools;

    public EthernetLayer(String pName) {
        pLayerName = pName;
        tools =new Tools();
    }

    private class _ETHERNET_ADDR{
        private byte[] addr = new byte[6];
        public _ETHERNET_ADDR() {
            this.addr[0]=(byte)0x00;
            this.addr[1]=(byte)0x00;
            this.addr[2]=(byte)0x00;
            this.addr[3]=(byte)0x00;
            this.addr[4]=(byte)0x00;
            this.addr[5]=(byte)0x00;
        }
    }
    private class _ETHERNET_Frame{
        _ETHERNET_ADDR enet_dstaddr;
        _ETHERNET_ADDR enet_srcaddr;
        byte[] enet_type;
        byte[] enet_data;
        public _ETHERNET_Frame() {
            this.enet_dstaddr = new _ETHERNET_ADDR();
            this.enet_srcaddr = new _ETHERNET_ADDR();
            this.enet_type=new byte[2];
            this.enet_data=null;
        }
    }

    _ETHERNET_Frame efHeader = new _ETHERNET_Frame();

    public void setDstAddress(byte[] addr){
        for(int i=0; i<addr.length; i++){
            efHeader.enet_dstaddr.addr[i]=addr[i];
        }
    }

    public void setEnetDstAddress(String address) {
        String[] sp=address.split(":");
        for(int i=0; i<sp.length; i++){
            byte toByte;
            int toInt = Integer.decode("0x"+sp[i]);
            if(toInt>127)
                toByte=(byte)(toInt-256);
            else
                toByte=(byte)toInt;
            efHeader.enet_dstaddr.addr[i]=toByte;
        }
    }

    public void setEnetSrcAddress(byte[] addr){
        for(int i=0; i<addr.length; i++){
            efHeader.enet_srcaddr.addr[i]=addr[i];
        }
    }

    public void setType(byte[] type){
        efHeader.enet_type[0]=type[0];
        efHeader.enet_type[1]=type[1];
    }

    public byte[] objToByte(_ETHERNET_Frame Header, byte[] input, int length, int isArp, byte[] dstAddr) {
        byte[] buf = new byte[length + 14];
        if(isArp>0) { // ARPLayer에서 내려옴
            setType(tools.hexToByte2(806));
            setDstAddress(dstAddr);
        }
        else { // message
            setType(tools.hexToByte2(0));
        }
        for(int i=0; i<6; i++) // Receiver
            buf[i] = Header.enet_dstaddr.addr[i];
        for(int i=6; i<12; i++) // Sender
            buf[i] = Header.enet_srcaddr.addr[i-6];
        buf[12] = Header.enet_type[0];
        buf[13] = Header.enet_type[1];
        for (int i = 0; i < input.length; i++)
            buf[14 + i] = input[i];
        return buf;
    }

    public boolean Send(byte[] input, int length, int isArp, byte[] dstAddr) {
        byte[] c= objToByte(efHeader, input, length, isArp, dstAddr);
        this.getUnderLayer(0).send(c,length+14);
        return true;
    }

    public synchronized boolean receive(byte[] input) {
//        System.out.println(Arrays.toString(input));
        // 자기가 보낸 패킷, 브로드캐스트가 아니면서 내가 타겟이 아니면 false
        if(isItMyPacket(input)||(!isBroadCast(input)&&!isTargetMe(input)))
            return false;
        byte[] data = tools.removeHeader(input, input.length,14);
        if(isArp(input)) // arp
            this.getUpperLayer(1).receive(data);
        else // message
            this.getUpperLayer(0).receive(data);
        return true;
    }

    public boolean isItMyPacket(byte[] input) {
        for (int i = 0; i < 6; i++) {
            if (efHeader.enet_srcaddr.addr[i] != input[6 + i])
                return false;
        }
        return true;
    }

    public boolean isBroadCast(byte[] input) {
        for (int i = 0; i < 6; i++) {
            if ((byte)0xFF != input[i])
                return false;
        }
        return true;
    }

    public boolean isTargetMe(byte[] input){
        for (int i = 0; i < 6; i++) {
            if (input[i] != efHeader.enet_srcaddr.addr[i])
                return false;
        }
        return true;
    }

    public boolean isArp(byte[] input){
        byte[] type= tools.extractSelectPart(input,12,14);
        byte[] arpPacketType= tools.hexToByte2(806);
        for(int i=0; i<arpPacketType.length; i++){
            if(type[i]!=arpPacketType[i])
                return false;
        }
        return true;
    }

    @Override
    public String getLayerName() {
        return pLayerName;
    }

    @Override
    public BaseLayer getUnderLayer(int nindex) {
        if (nindex < 0 || nindex > nUnderLayerCount || nUnderLayerCount < 0)
            return null;
        return p_aUnderLayer.get(nindex);
    }

    @Override
    public BaseLayer getUpperLayer(int nindex) {
        if (nindex < 0 || nindex > nUpperLayerCount || nUpperLayerCount < 0)
            return null;
        return p_aUpperLayer.get(nindex);
    }

    @Override
    public void setUnderLayer(BaseLayer pUnderLayer) {
        if (pUnderLayer == null)
            return;
        this.p_aUnderLayer.add(nUnderLayerCount++, pUnderLayer);
    }

    @Override
    public void setUpperLayer(BaseLayer pUpperLayer) {
        if (pUpperLayer == null)
            return;
        this.p_aUpperLayer.add(nUpperLayerCount++, pUpperLayer);
    }

    @Override
    public void setUpperUnderLayer(BaseLayer pUULayer) {
        this.setUpperLayer(pUULayer);
        pUULayer.setUnderLayer(this);
    }
}