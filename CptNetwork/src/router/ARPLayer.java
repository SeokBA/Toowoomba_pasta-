package router;

import java.util.ArrayList;
import java.util.Map;

public class ARPLayer implements BaseLayer {
    private int nUnderLayerCount = 0;
    private int nUpperLayerCount = 0;
    private String pLayerName = null;
    private BaseLayer p_UnderLayer = null;
    private ArrayList<BaseLayer> p_aUnderLayer = new ArrayList<>();
    private ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<>();

    Map<String, ARPCacheRecord> cacheTable;

    Tools tools;

    public ARPLayer(String pName) {
        pLayerName = pName;
        tools = new Tools();
    }

    private class _ADDR {
        private byte[] addr;

        public void setAddrLength(byte length) {
            this.addr = new byte[length];
        }

        public void setAddr(byte[] addr) {
            for (int i = 0; i < addr.length; i++) {
                this.addr[i] = addr[i];
            }
        }
    }

    private class _ARP_MESSAGE {
        byte[] hardwareType; // 2 byte
        byte[] protocolType; // 0x0800 : IP주소 사용
        byte lengOfHardwareAddr;
        byte lengOfProtocolAddr;
        byte[] opcode; // 2 byte
        _ADDR srcHWAddr;
        _ADDR srcPTAddr;
        _ADDR dstHWAddr;
        _ADDR dstPTAddr;

        public _ARP_MESSAGE() {
            if(tools ==null)
                tools = new Tools();
            this.hardwareType = new byte[2];
            this.hardwareType[0] = 0; // default
            this.hardwareType[1] = 1; // 이 부분만 사용
            this.protocolType = new byte[2];
            byte[] hexToByte = tools.hexToByte2(800);
            this.protocolType[0] = hexToByte[0];
            this.protocolType[1] = hexToByte[1];
            this.lengOfHardwareAddr = 6; // default
            this.lengOfProtocolAddr = 4; // default
            this.opcode = new byte[2];
            this.srcHWAddr = new _ADDR();
            this.srcHWAddr.setAddrLength(lengOfHardwareAddr); // 6byte
            this.srcPTAddr = new _ADDR();
            this.srcPTAddr.setAddrLength(lengOfProtocolAddr); // 4byte
            this.dstHWAddr = new _ADDR();
            this.dstHWAddr.setAddrLength(lengOfHardwareAddr); // 6byte
            this.dstPTAddr = new _ADDR();
            this.dstPTAddr.setAddrLength(lengOfProtocolAddr); // 4byte
        }
    }

    _ARP_MESSAGE arpMessage = new _ARP_MESSAGE();

    public void setProtocolType(byte[] type) {
        arpMessage.protocolType[0] = type[0];
        arpMessage.protocolType[1] = type[1];
    }

    public void setOpcode(byte opcode) {
        arpMessage.opcode[1] = opcode;
    }

    public void setSrcHWAddress(byte[] addr) {
        // 받는 사람의 IP주소, IPLayer에서 보낸 값으로 세팅
        for (int i = 0; i < 6; i++) {
            arpMessage.srcHWAddr.addr[i] = addr[i];
        }
    }

    public byte[] getSrcHWAddress() {
        return arpMessage.srcHWAddr.addr;
    }

    public byte[] getDstPTAddress() {
        return arpMessage.dstPTAddr.addr;
    }

    public void setDstHWAddress(String address) {
        String[] sp = address.split(":");
        for (int i = 0; i < sp.length; i++) {
            byte toByte;
            int toInt = Integer.decode("0x" + sp[i]);
            if (toInt > 127)
                toByte = (byte) (toInt - 256);
            else
                toByte = (byte) toInt;
            arpMessage.dstHWAddr.addr[i] = toByte;
        }
    }

    public byte[] setDstHWAddress(byte[] input) {
        for (int i = 18; i < 24; i++)
            input[i] = arpMessage.srcHWAddr.addr[i - 18];
        return input;
    }

    public void setSrcPTAddress(byte[] addr) {
        // 보내는 사람의 IP주소, GUI에서 세팅
        for (int i = 0; i < 4; i++) {
            arpMessage.srcPTAddr.addr[i] = addr[i];
        }
    }

    public void setDstPTAddress(byte[] input) {
        // 받는 사람의 IP주소, IPLayer에서 보낸 값으로 세팅
        for (int i = 0; i < 4; i++) {
            arpMessage.dstPTAddr.addr[i] = input[i + 15];
        }
    }

    public byte[] swapping(byte[] input) {
        byte[] temp = new byte[10];
        for (int i = 8; i < 18; i++) {
            temp[i - 8] = input[i];
        }
        for (int i = 18; i < 28; i++) {
            input[i - 10] = input[i];
        }
        for (int i = 18; i < 28; i++) {
            input[i] = temp[i - 18];
        }
        return input;
    }

    public boolean send(byte[] input, int length, byte opcode) {
        if (opcode == 1) { // request
            setOpcode(opcode);
            setDstPTAddress(input); // IPLayer에서 내려와야함
            setDstHWAddress("0:0:0:0:0:0"); // 아직 상대방 맥주소를 모름
            byte[] c = objToByte(arpMessage, input, length);
            byte[] broadcast = {(byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF};
            ((EthernetLayer) this.getUnderLayer(0)).Send(c, length + 28, 1, broadcast);
            return true;
        } else if (opcode == 2) { // reply
            input = setDstHWAddress(input);
            input = swapping(input);
            input[7] = opcode;
            byte[] dstAddr = new byte[6];
            for (int i = 18; i < 24; i++)
                dstAddr[i - 18] = input[i];
            ((EthernetLayer) this.getUnderLayer(0)).Send(input, length, 2, dstAddr);
            return true;
        }
        return false;
    }

    public boolean send(byte[] input, int length, byte opcode, String addr) {
        // reply
        byte[] hostMacAddr = tools.string2HWaddr(addr);
        for (int i = 18; i < 24; i++)
            input[i] = hostMacAddr[i - 18];
        input = swapping(input);
        input[7] = opcode;
        byte[] dstAddr = tools.extractSelectPart(input, 18, 24);
        ((EthernetLayer) this.getUnderLayer(0)).Send(input, length, 2, dstAddr);
        return true;
    }

    public byte[] objToByte(_ARP_MESSAGE msg, byte[] input, int length) {
        byte[] buf = new byte[length + 28];
        buf[0] = msg.hardwareType[0];
        buf[1] = msg.hardwareType[1];
        for (int i = 2; i < 4; i++)
            buf[i] = msg.protocolType[i - 2];
        buf[4] = msg.lengOfHardwareAddr;
        buf[5] = msg.lengOfProtocolAddr;
        buf[6] = msg.opcode[0];
        buf[7] = msg.opcode[1];
        for (int i = 8; i < 14; i++)
            buf[i] = msg.srcHWAddr.addr[i - 8];
        for (int i = 14; i < 18; i++)
            buf[i] = msg.srcPTAddr.addr[i - 14];
        for (int i = 18; i < 24; i++)
            buf[i] = msg.dstHWAddr.addr[i - 18];
        for (int i = 24; i < 28; i++)
            buf[i] = msg.dstPTAddr.addr[i - 24];
        for (int i = 0; i < input.length; i++)
            buf[28 + i] = input[i];
        return buf;
    }

    public synchronized boolean receive(byte[] input) {
        // 자기가 보낸거면 뭐 할거 없으므로 걸러냄
        if (isItMyPacket(input))
            return false;
        byte[] protocolType = tools.hexToByte2(800); // 프로토콜 타입 검사
        for (int i = 2; i < 4; i++) {
            if (input[i] != protocolType[i - 2])
                return false;
        }
        cacheTable = ARPCacheTable.getInstance().getTable();

        if (input[7] == 1) { // 상대방이 보낸 req받았을 때
            // 받아왔는데 자기가 아니고 테이블에 저장되있는 맥주소가 아니면 업데이트
            byte[] senderPTaddr = tools.extractSelectPart(input,14,18);
            byte[] senderHWaddr = tools.extractSelectPart(input, 8, 14);
            String senderPTaddrStr = tools.bytePTAddrToString(senderPTaddr);
            String senderHWaddrStr = tools.byteHWAddrToString(senderHWaddr);
            cacheTable.put(senderPTaddrStr, new ARPCacheRecord(senderHWaddrStr, "Complete"));
            ((ARPDlg) getUpperLayer(0)).updateCacheTable();

            byte[] targetPTaddr = tools.extractSelectPart(input, 24, 28);
            String targetPTaddrStr = tools.bytePTAddrToString(targetPTaddr);
            String srcHWaddrStr = tools.byteHWAddrToString(getSrcHWAddress());
            if (ProxyARPTable.getInstance().isInProxyArpEntry(targetPTaddrStr)) // proxyArp 데이터 먼저 검사
                send(input, input.length, (byte) 2, srcHWaddrStr);
            else if (isTarget(input)) // 내 맥주소를 알려줄께!
                send(input, input.length, (byte) 2);

        } else if (input[7] == 2) { // reply도착했을 때
            String dstPTAddrStr = tools.bytePTAddrToString(getDstPTAddress());
            byte[] srcHWAddr = tools.extractSelectPart(input, 8, 14);
            String srcHWAddrStr = tools.byteHWAddrToString(srcHWAddr);
            cacheTable.put(dstPTAddrStr, new ARPCacheRecord(srcHWAddrStr, "Complete"));
            ((ARPDlg) getUpperLayer(0)).updateCacheTable();
        }
        return true;
    }

    private boolean isItMyPacket(byte[] input) {
        for (int i = 0; i < 6; i++) {
            if (arpMessage.srcHWAddr.addr[i] != input[8 + i])
                return false;
        }
        return true;
    }

    private boolean isTarget(byte[] input) {
        for (int i = 0; i < 4; i++) {
            if (arpMessage.srcPTAddr.addr[i] != input[24 + i])
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