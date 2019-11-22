package router;
import java.util.ArrayList;

public class IPLayer implements BaseLayer{
    private int nUnderLayerCount = 0;
    private int nUpperLayerCount = 0;
    private String pLayerName = null;
    private BaseLayer p_UnderLayer = null;
    private ArrayList<BaseLayer> p_aUnderLayer = new ArrayList<>();
    private ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<>();
    private RoutingTable routingtable;
    Tools tools;

    public IPLayer(String pName) {
        pLayerName = pName;
        tools =new Tools();
        routingtable=RoutingTable.getInstance();
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
        byte ipVerLen; // 1byte
        byte ipTos; // 1byte
        byte[] ipLen; // 2byte 4
        byte[] ipId; // 2byte 6
        byte[] ipFragOff; // 2byte 8
        byte ipTtl;
        byte ipProto;
        byte ipCksum;
        _IP_ADDR ipSrc;
        _IP_ADDR ipDst;
        byte[] ipData;

        public _IP_HEADER() {
            this.ipVerLen =0x00;
            this.ipTos =0x00;
            this.ipLen =new byte[2];
            this.ipId=new byte[2];
            this.ipFragOff=new byte[2];
            this.ipTtl=0x00;
            this.ipProto=0x00;
            this.ipCksum =0x00;
            this.ipSrc = new _IP_ADDR();
            this.ipDst = new _IP_ADDR();
            this.ipData=null;
        }
    }

    _IP_HEADER ipHeader = new _IP_HEADER();

    public void setDstAddress(byte[] addr){
        for(int i=0; i<addr.length; i++){
            ipHeader.ipDst.addr[i]=addr[i];
        }
    }

    public void setSrcAddress(byte[] addr){
        for(int i=0; i<addr.length; i++){
            ipHeader.ipSrc.addr[i]=addr[i];
        }
    }

    public boolean send(byte[] input, int length) {
        byte[] extractData= tools.extractSelectPart(input,0,length);
        String extractDataStr=new String(extractData);
        String[] extractDataStrSplit=extractDataStr.split("\\.|:");
        // Send할 데이터가 IPv4의 형식인지 검사
        if(extractDataStrSplit.length==4){
            byte[] dstIPByte=new byte[4];
            try{
                for(int i=0; i<extractDataStrSplit.length; i++){
                    int part=Integer.parseInt(extractDataStrSplit[i]);
                    if(part>127){
                        dstIPByte[i]=(byte)(part-256);
                    } else {
                        dstIPByte[i]=(byte)part;
                    }
                }
            } catch (Exception e){
                e.printStackTrace();
            }
            setDstAddress(dstIPByte);
        // 입력받은 값이 mac형식일때, 즉 gARP 사용할 때는 여기서 설정해줄 것이 없음
        }
        // srcAddress는 GUI에서 세팅
        byte[] c= objToByte(ipHeader, input, length);
        // 옵코드를 1로 보냄
        ((ARPLayer)this.getUnderLayer(1)).send(c,length+19,(byte)1);
        return true;
    }


    public byte[] objToByte(_IP_HEADER Header, byte[] input, int length) {
        byte[] buf = new byte[length + 19];
        buf[0]=Header.ipVerLen;
        buf[1]=Header.ipTos;
        for(int i=2; i<4; i++)
            buf[i] = Header.ipLen[i-2];
        for(int i=4; i<6; i++)
            buf[i] = Header.ipId[i-4];
        for(int i=6; i<8; i++)
            buf[i] = Header.ipFragOff[i-6];
        buf[8]=Header.ipTtl;
        buf[9]=Header.ipProto;
        buf[10]=Header.ipCksum;
        for(int i=11; i<15; i++)
            buf[i] = Header.ipSrc.addr[i-11];
        for(int i=15; i<19; i++)
            buf[i] = Header.ipDst.addr[i-15];
        for (int i = 0; i < input.length; i++)
            buf[19 + i] = input[i];
        return buf;
    }

    public synchronized boolean receive(byte[] input) {
        // 자기가 보낸 패킷이거나, 목적지가 내가 아니면 버림
        /*if(isItMyPacket(input)||(!isTargetMe(input)))
            return false;
        byte[] data = tools.removeCappHeader(input, input.length,19);
        this.getUpperLayer(0).receive(data);
        return true;*/
        // ~ 1차 과제

        //findRoutingRecord(input);
        return false;
    }

    //subnetMask와 &연산
    public RoutingRecord findRoutingRecord(byte[] input) {
        boolean findflag = true;
        for(RoutingRecord element : routingtable.getTable()) {
            byte[] subNetmask = element.getNetmask();
            byte[] dstAddr = element.getDstAddr();

            for(int i =0; i<4; i++) {
                if(dstAddr[i] != (input[i]&subNetmask[i])) {
                    findflag = false;
                }
            }
            if(findflag) {
                return element;
            }
        }
        return null;
    }

    public void routingTableAdd(RoutingRecord routeData) {
        routingtable.getTable().add(routeData);
    }

    public String[][] getStringArray(){
        String[][] arr = new String[routingtable.getTable().size()][];
        for(int i = 0; i<routingtable.getTable().size(); i++)
            arr[i] = routingtable.getTable().get(i).getStringArray();
        return arr;
    }

    public boolean isItMyPacket(byte[] input) {
        for (int i = 0; i < 6; i++) {
            if (ipHeader.ipSrc.addr[i] != input[15 + i])
                return false;
        }
        return true;
    }

    public boolean isTargetMe(byte[] input){
        for (int i = 0; i < 4; i++) {
            if (input[i+15] !=ipHeader.ipSrc.addr[i])
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