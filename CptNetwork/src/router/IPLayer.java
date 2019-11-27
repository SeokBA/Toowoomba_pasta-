package router;

import java.util.ArrayList;

public class IPLayer implements BaseLayer {
    private int nUnderLayerCount = 0;
    private int nUpperLayerCount = 0;
    private String pLayerName = null;
    private BaseLayer p_UnderLayer = null;
    private ArrayList<BaseLayer> p_aUnderLayer = new ArrayList<>();
    private ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<>();
    Tools tools = new Tools();

    public IPLayer(String pName) {
        pLayerName = pName;
        tools = new Tools();
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

    private class _IP_HEADER {
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
            this.ipVerLen = 0x00;
            this.ipTos = 0x00;
            this.ipLen = new byte[2];
            this.ipId = new byte[2];
            this.ipFragOff = new byte[2];
            this.ipTtl = 0x00;
            this.ipProto = 0x00;
            this.ipCksum = 0x00;
            this.ipSrc = new _IP_ADDR();
            this.ipDst = new _IP_ADDR();
            this.ipData = null;
        }
    }

    _IP_HEADER ipHeader = new _IP_HEADER();

    public void setDstAddress(byte[] addr) {
        for (int i = 0; i < addr.length; i++) {
            ipHeader.ipDst.addr[i] = addr[i];
        }
    }

    public void setSrcAddress(byte[] addr) {
        for (int i = 0; i < addr.length; i++) {
            ipHeader.ipSrc.addr[i] = addr[i];
        }
    }

    public boolean send(byte[] input, int length) {
        if (isPing(input)) {
            byte[] extractDst = tools.extractSelectPart(input, 16, 20);
            setDstAddress(extractDst);
            ((EthernetLayer) this.getUnderLayer(0)).Send(input, input.length, 0, ipHeader.ipDst.addr);
        }
        byte[] c = objToByte(ipHeader, input, length);
        ((ARPLayer) this.getUnderLayer(1)).send(c, length + 19, (byte) 1);
        return true;
    }

    public boolean isPing(byte[] input) {
        byte[] data = tools.removeHeader(input, input.length, 20);
        if (data[0] == 0x08 && data[1] == 0x00) {
            return true;
        } else if (data[0] == 0x00 && data[1] == 0x00) {
            return true;
        }
        return false;
    }


    public byte[] objToByte(_IP_HEADER Header, byte[] input, int length) {
        byte[] buf = new byte[length + 19];
        buf[0] = Header.ipVerLen;
        buf[1] = Header.ipTos;
        for (int i = 2; i < 4; i++)
            buf[i] = Header.ipLen[i - 2];
        for (int i = 4; i < 6; i++)
            buf[i] = Header.ipId[i - 4];
        for (int i = 6; i < 8; i++)
            buf[i] = Header.ipFragOff[i - 6];
        buf[8] = Header.ipTtl;
        buf[9] = Header.ipProto;
        buf[10] = Header.ipCksum;
        for (int i = 11; i < 15; i++)
            buf[i] = Header.ipSrc.addr[i - 11];
        for (int i = 15; i < 19; i++)
            buf[i] = Header.ipDst.addr[i - 15];
        for (int i = 0; i < input.length; i++)
            buf[19 + i] = input[i];
        return buf;
    }

    public synchronized boolean receive(byte[] input) {
        if (input.length == 60) {
            if (this.getLayerName() == "IP_L") {
                (this.getUpperLayer(0)).getUnderLayer(2).send(input, input.length);
            } else {
                (this.getUpperLayer(0)).getUnderLayer(0).send(input, input.length);
            }
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