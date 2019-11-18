package Router;

import java.util.ArrayList;

public class EthernetLayer implements BaseLayer {
    public int nUnderLayerCount = 0;
    public int nUpperLayerCount = 0;
    public String pLayerName = null;
    public BaseLayer p_UnderLayer = null;
    public ArrayList<BaseLayer> p_aUnderLayer = new ArrayList<>();
    public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<>();
    Tool tool;

    _ETHERNET_Frame m_sHeader = new _ETHERNET_Frame();

    public EthernetLayer(String pName) {
        pLayerName = pName;
        ResetHeader();
    }

    public void ResetHeader() {
        m_sHeader = new _ETHERNET_Frame();
    }

    private class _ETHERNET_ADDR {
        private byte[] addr = new byte[6];

        public _ETHERNET_ADDR() {
            this.addr[0] = (byte) 0x00;
            this.addr[1] = (byte) 0x00;
            this.addr[2] = (byte) 0x00;
            this.addr[3] = (byte) 0x00;
            this.addr[4] = (byte) 0x00;
            this.addr[5] = (byte) 0x00;
        }
    }

    private class _ETHERNET_Frame {
        _ETHERNET_ADDR enet_dstaddr;
        _ETHERNET_ADDR enet_srcaddr;
        byte[] enet_type;
        byte[] enet_data;

        public _ETHERNET_Frame() {
            this.enet_dstaddr = new _ETHERNET_ADDR();
            this.enet_srcaddr = new _ETHERNET_ADDR();
            this.enet_type = new byte[2];
            this.enet_data = null;
        }
    }

    public byte[] ObjToByte(_ETHERNET_Frame Header, byte[] input, int length, int isArp, byte[] dstAddr) {
        byte[] buf = new byte[length + 14];

        SetEnetDstAddress(dstAddr);
        if (isArp > 0) // todo: 다시볼것
            setType(tool.hexToByte2(806));
        else
            setType(tool.hexToByte2(0));

        for (int i = 0; i < 6; i++) { // Receiver
            buf[i] = Header.enet_dstaddr.addr[i];
            buf[i + 6] = Header.enet_srcaddr.addr[i]; // Sender
        }
        buf[12] = Header.enet_type[0];
        buf[13] = Header.enet_type[1];
        for (int i = 0; i < length; i++)
            buf[14 + i] = input[i];

        return buf;
    }

    public void setType(byte[] input) {
        m_sHeader.enet_type = input;
    }

    public boolean Send(byte[] input, int length, int isArp, byte[] dstAddr) {
        byte[] c = ObjToByte(m_sHeader, input, length, isArp, dstAddr);
        this.GetUnderLayer(0).Send(c, length + 14);
        return true;
    }

    public byte[] RemoveEthernetHeader(byte[] input, int length) {
        byte[] cpyInput = new byte[length - 14];
        System.arraycopy(input, 14, cpyInput, 0, length - 14);
        input = cpyInput;
        return input;
    }

    public synchronized boolean Receive(byte[] input) {
        byte[] data;

        if ((chkAddr(input) || (isBroadcast(input))) && !isMyPacket(input)) {
            data = RemoveEthernetHeader(input, input.length);
            if (input[12] == (byte) 0x03 && input[13] == (byte) 0x26) // arp
                this.GetUpperLayer(1).Receive(data);
            else // message
                this.GetUpperLayer(0).Receive(data);
        }
        return false;
    }

    private boolean isBroadcast(byte[] bytes) {
        for (int i = 0; i < 6; i++)
            if (bytes[i] != (byte) 0xff)
                return false;
        return true;
    }

    private boolean isMyPacket(byte[] input) {
        for (int i = 0; i < 6; i++)
            if (m_sHeader.enet_srcaddr.addr[i] != input[6 + i])
                return false;
        return true;
    }

    private boolean chkAddr(byte[] input) {
        // todo: 내 네트워크 안에 포함된 기기들도 true로 반환, proxytable을 봐줘야 함
        for (int i = 0; i < 6; i++)
            if (m_sHeader.enet_srcaddr.addr[i] != input[i])
                return false;
        return true;
    }

    public void SetEnetSrcAddress(byte[] srcAddress) {
        // TODO Auto-generated method stub
        m_sHeader.enet_srcaddr.addr = srcAddress;
    }

    public void SetEnetDstAddress(byte[] dstAddress) {
        // TODO Auto-generated method stub
        m_sHeader.enet_dstaddr.addr = dstAddress;
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