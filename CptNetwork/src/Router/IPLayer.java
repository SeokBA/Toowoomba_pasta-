package Router;

import java.util.ArrayList;

public class IPLayer implements BaseLayer {
    public int nUnderLayerCount = 0;
    public int nUpperLayerCount = 0;
    public String pLayerName = null;
    public BaseLayer p_UnderLayer = null;
    public ArrayList<BaseLayer> p_aUnderLayer = new ArrayList<>();
    public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<>();
    Tool tool;

    _IP_Frame m_sHeader = new _IP_Frame();

    public IPLayer(String pName) {
        pLayerName = pName;
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

    public byte[] ObjToByte(_IP_Frame Header, byte[] input, int length) {
        byte[] buf = new byte[length + 19];
        buf[0] = Header.ip_ver;
        buf[1] = Header.ip_type;
        for (int i = 2; i < 4; i++)
            buf[i] = Header.ip_len[i - 2];
        for (int i = 4; i < 6; i++)
            buf[i] = Header.ip_id[i - 4];
        for (int i = 6; i < 8; i++)
            buf[i] = Header.ip_fragoff[i - 6];
        buf[8] = Header.ip_totlen;
        buf[9] = Header.ip_proto;
        buf[10] = Header.ip_checksum;

        for (int i = 11; i < 15; i++)
            buf[i] = Header.ip_srcaddr.addr[i - 11];
        for (int i = 15; i < 19; i++)
            buf[i] = Header.ip_dstaddr.addr[i - 15];
        for (int i = 0; i < input.length; i++)
            buf[19 + i] = input[i];
        return buf;
    }

    public boolean Send(byte[] input, int length) {
        byte[] extractData = new byte[length - 28];
        for (int i = 0; i < extractData.length; i++) {
            extractData[i] = input[i + 28];
        }

        String extractDataStr = new String(extractData);
        String[] extractDataStrSplit = extractDataStr.split("\\.|:");
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
            setIPDstAddress(dstIPByte);

            // 입력받은 값이 mac형식일때, 즉 gARP 사용할 때!
        } else if (extractDataStrSplit.length == 6) {
            // gArp보냅니다~~
            // ARPDlg에서 setDstAddress진행함
        }
        // srcAddress는 GUI에서 세팅
        byte[] c = ObjToByte(m_sHeader, input, length);
        // 옵코드를 1로 보냄
        ((ARPLayer) this.GetUnderLayer(1)).Send(c, length + 19, (byte) 1);

        return true;
    }

    public byte[] RemoveIPHeader(byte[] input, int length) {
        byte[] buf = new byte[length - 19];
        for (int i = 0; i < length - 19; i++) {
            buf[i] = input[i + 19];
        }
        return buf;
    }

    public synchronized boolean Receive(byte[] input) {
        if (isMyPacket(input)) // 자기껀 버림
            return false;
        byte[] data;
        for (int i = 0; i < 15; i++) {
            if (input[i] != m_sHeader.ip_srcaddr.addr[i])
                return false;
        }
        data = RemoveIPHeader(input, input.length);
        this.GetUpperLayer(0).Receive(data);
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
    public String GetLayerName() {
        return pLayerName;
    }

    @Override
    public BaseLayer GetUnderLayer(int nindex) {
        if (nindex < 0 || nindex > nUnderLayerCount || nUnderLayerCount < 0)
            return null;
        return p_aUnderLayer.get(nindex);
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
        this.p_aUnderLayer.add(nUnderLayerCount++, pUnderLayer);
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