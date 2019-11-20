package router;
import java.util.ArrayList;

public class IPLayer implements BaseLayer{
    private int nUnderLayerCount = 0;
    private int nUpperLayerCount = 0;
    private String pLayerName = null;
    private BaseLayer p_UnderLayer = null;
    private ArrayList<BaseLayer> p_aUnderLayer = new ArrayList<>();
    private ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<>();
    Tools tools;

    _IP_Frame m_sHeader = new _IP_Frame();

    public IPLayer(String pName) {
        pLayerName = pName;
        tools =new Tools();
    }

    public void ResetHeader() {
        m_sHeader = new _IP_Frame();
    }

    private class _IP_ADDR {
        private byte[] addr = new byte[4];

        public _IP_ADDR() {
            this.addr[0] = (byte) 0x00;
            this.addr[1] = (byte) 0x00;
            this.addr[2] = (byte) 0x00;
            this.addr[3] = (byte) 0x00;
        }
    }

    private class _IP_Frame {
        byte ip_ver; // 1byte
        byte ip_type; // 1byte
        byte[] ip_len; // 2byte 4
        byte[] ip_id; // 2byte 6
        byte[] ip_fragoff; // 2byte 8
        byte ip_totlen;
        byte ip_proto;
        byte ip_checksum;
        _IP_ADDR ip_srcaddr;
        _IP_ADDR ip_dstaddr;
        byte[] ip_data;

        public _IP_Frame() {
            this.ip_ver = 0x00;
            this.ip_type = 0x00;
            this.ip_len = new byte[2];
            this.ip_id = new byte[2];
            this.ip_fragoff = new byte[2];
            this.ip_totlen = 0x00;
            this.ip_proto = 0x00;
            this.ip_checksum = 0x00;
            this.ip_srcaddr = new _IP_ADDR();
            this.ip_dstaddr = new _IP_ADDR();
            this.ip_data = null;
        }
    }

    public boolean send(byte[] input, int length) {
        byte[] extractData= tools.extractSelectPart(input,0,length);
        String extractDataStr=new String(extractData);
        String[] extractDataStrSplit=extractDataStr.split("\\.|:");
        // Send할 데이터가 IPv4의 형식인지 검사
        if (extractDataStrSplit.length == 4) {
            byte[] dstIPByte = new byte[4];
            try {
                for (int i = 0; i < extractDataStrSplit.length; i++) {
                    int part = Integer.parseInt(extractDataStrSplit[i]);
                    if (part > 127) {
                        dstIPByte[i] = (byte) (part - 256);
                    } else {
                        dstIPByte[i] = (byte) part;
                    }
                }
            } catch (Exception e) {
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

    public synchronized boolean receive(byte[] input) {
        // 자기가 보낸 패킷이거나, 목적지가 내가 아니면 버림
        if(isItMyPacket(input)||(!isTargetMe(input)))
            return false;
        byte[] data = tools.removeCappHeader(input, input.length,19);
        this.getUpperLayer(0).receive(data);
        return true;
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

    public boolean isMyPacket(byte[] input) {
        for (int i = 0; i < 6; i++)
            if (m_sHeader.ip_srcaddr.addr[i] != input[15 + i])
                return false;
        return true;
    }

    public void setIPSrcAddress(byte[] addr) {
        for (int i = 0; i < addr.length; i++) {
            m_sHeader.ip_srcaddr.addr[i] = addr[i];
        }
    }

    public void setIPDstAddress(byte[] addr) {
        for (int i = 0; i < addr.length; i++) {
            m_sHeader.ip_dstaddr.addr[i] = addr[i];
        }
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
