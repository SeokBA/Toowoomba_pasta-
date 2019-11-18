package Router;

import java.util.*;
import java.util.Timer;

import javax.swing.*;


public class ARPLayer implements BaseLayer {
    public int nUnderLayerCount = 0;
    public int nUpperLayerCount = 0;
    public String pLayerName = null;
    public BaseLayer p_UnderLayer = null;
    public ArrayList<BaseLayer> p_aUnderLayer = new ArrayList<>();
    public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<>();
    DefaultListModel proxyModel;
    Tool tool;

    Timer timer = new Timer();
    boolean returnFlag = false;

    TimerTask checkTime = new TimerTask() {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            System.out.println("Reached Final Limit Time 5 sec");
            if (returnFlag() == true) {
                System.out.println("Good");
            } else {
                System.out.println("Failed");
                byte[] tableAddr = getDstPTAddress();
                String tableKey = tool.ptAddrByteToString(tableAddr);
                if (arpCacheTable.isEmpty() == false) {
                    arpCacheTable.remove(tableKey);
                    ((RouterDlg) GetUpperLayer(0)).updateCacheTable();
                    System.out.println(tableKey + " is Deleted from Cache Table");

                }
            }
        }

        private boolean returnFlag() {
            return returnFlag;
        }
    };

    TimerTask checkCompleted = new TimerTask() {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            System.out.println("Cache Table Time Over : Delete Completed");
            String tableKey = keyInfoStoredQueue.peek();

            arpCacheTable.remove(tableKey);
        }
    };

    public void setProxyModel(DefaultListModel proxyModel) {
        this.proxyModel = proxyModel;
    }

    Map<String, ARPCacheRecord> arpCacheTable;
    Queue<String> keyInfoStoredQueue = new LinkedList<>();
    Map<String, ProxyARPRecord> proxyArpEntry;

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
            this.hardwareType = new byte[2];
            this.hardwareType[0] = 0; // default
            this.hardwareType[1] = 1; // 이 부분만 사용
            this.protocolType = new byte[2];
            byte[] hexToByte = tool.hexToByte2(800);
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
//        arpMessage.opcode[1] = opcode;
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

    public byte[] getSrcPTAddr() {
        return arpMessage.srcPTAddr.addr;
    }

    public byte[] getDstPTAddress() {
        return arpMessage.dstPTAddr.addr;
    }

    public void setDstHWAddress(String address) {
        if (address == null) // 상대방 맥주소 몰라요
            address = "0:0:0:0:0:0";
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

    public void setSrcPTAddress(byte[] addr) {
        // 보내는 사람의 IP주소, IPLayer에서 보낸 값으로 세팅
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

    public byte[] fillDstHWAddress(byte[] input) {
        for (int i = 18; i < 24; i++)
            input[i] = arpMessage.srcHWAddr.addr[i - 18];
        return input;
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


    public boolean Send(byte[] input, int length, byte opcode) {
        if (opcode == 1) { // req, gArp req도 별 다를게 없음 위에서 세팅 다 진행해줬으므로
            setOpcode(opcode);
            setDstPTAddress(input); // IPLayer에서 내려와야함
            setDstHWAddress(null);
            byte[] c = ObjToByte(arpMessage, input, length);
            ((EthernetLayer) this.GetUnderLayer(0)).Send(c, length + 28, 1);

            // Timer Seq
            System.out.println("Send Request");
            timer.schedule(checkTime, 5000);
            // wait 5 sec and Judging Success or Fail


        } else if (opcode == 2) { // reply
            input = fillDstHWAddress(input);
            input = swapping(input);
            input[7] = opcode;
            byte[] dstAddr = new byte[6];
            for (int i = 18; i < 24; i++)
                dstAddr[i - 18] = input[i];
            ((EthernetLayer) this.GetUnderLayer(0)).Send(input, length, 2, dstAddr);
            return true;
        }
        return false;
    }

    public boolean Send(byte[] input, int length, byte opcode, String addr) {
        // reply
        byte[] hostMacAddr = tool.hwAddrStringToByte(addr);
        for (int i = 18; i < 24; i++)
            input[i] = hostMacAddr[i - 18];
        input = swapping(input);
        input[7] = opcode;
        byte[] dstAddr = new byte[6];
        for (int i = 18; i < 24; i++)
            dstAddr[i - 18] = input[i];
        ((EthernetLayer) this.GetUnderLayer(0)).Send(input, length, 2, dstAddr);
        return true;
    }


    public byte[] ObjToByte(_ARP_MESSAGE msg, byte[] input, int length) {
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

    public byte[] RemoveCappHeader(byte[] input, int length) {
        byte[] buf = new byte[length - 28];
        for (int i = 0; i < length - 28; i++) {
            buf[i] = input[i + 28];
        }
        return buf;
    }

    // todo 하드웨어타입 프로토콜타입이런거도 다 확인해줘야함
    public synchronized boolean Receive(byte[] input) {
        // 자기가 보낸거면 뭐 할거 없으므로 걸러냄
        byte[] senderHWaddr = new byte[6];
        for (int i = 8; i < 14; i++)
            senderHWaddr[i - 8] = input[i];
        boolean isMyPacket = true;
        for (int i = 0; i < 6; i++) {
            if (senderHWaddr[i] != arpMessage.srcHWAddr.addr[i]) {
                isMyPacket = false;
                break;
            }
        }
        if (isMyPacket)
            return false;

        byte[] protocolType = tool.hexToByte2(800);
        for (int i = 2; i < 4; i++) {
            if (input[i] != protocolType[i - 2])
                return false;
        }

        arpCacheTable = ARPCacheTable.getInstance().getCacheTable();

        if (input[7] == 1) { // 상대방이 보낸 req받았을 때
            // 받아왔는데 자기가 아니고 테이블에 저장되있는 맥주소가 아니면 업데이트
            byte[] senderPTaddr = new byte[4];
            for (int i = 14; i < 18; i++)
                senderPTaddr[i - 14] = input[i];

            String senderPTaddrStr = tool.ptAddrByteToString(senderPTaddr);

            // sender의 PTaddr과 내 PTaddr이 같다면 gARP 송수신 과정에서 IP충돌이 발생한 것
            if (senderPTaddr == getSrcPTAddr()) {
                // IP 충돌이 일어난 경우
                ((RouterDlg) p_aUpperLayer.get(0)).IPCrash();

                return false;
            }

            String senderHWaddrStr = tool.hwAddrByteToString(senderHWaddr, ':');
            if (arpCacheTable.get(senderPTaddrStr) == null) {
                arpCacheTable.put(senderPTaddrStr, new ARPCacheRecord(senderHWaddrStr, "Complete"));

                // Timer Seq
                keyInfoStoredQueue.add(senderPTaddrStr);
                System.out.println("Cache Table Remaining Time Checking Start");
                timer.schedule(checkCompleted, 10000);

                System.out.println(senderHWaddrStr);
                ((RouterDlg) GetUpperLayer(0)).updateCacheTable();
                // todo GUI에 이 테이블 데이터 업데이트 과정 필요
            }
            // 자기가 아닌데 테이블에 이미 있었으면 gArp할때 보낸 것임.
            // 만약 arp에서 처리했다고 해도 테이블에 들어갈 내용은 변화가 없으니 상관없음
            // 엥 뭐여 그럼 같은 동작하는거네
            else {
                arpCacheTable.put(senderPTaddrStr, new ARPCacheRecord(senderHWaddrStr, "Complete"));

                // Timer Seq
                keyInfoStoredQueue.add(senderPTaddrStr);
                System.out.println("Cache Table Remaining Time Checking Start");
                timer.schedule(checkCompleted, 10000);

                System.out.println(senderHWaddrStr);
                ((RouterDlg) GetUpperLayer(0)).updateCacheTable();
            }

            // 맥주소를 알고싶은 타겟이 자신인지 확인
            byte[] targetPTaddr = new byte[4];
            boolean isTarget = true;
            for (int i = 24; i < 28; i++)
                targetPTaddr[i - 24] = input[i];

            String targetPTaddrStr = tool.ptAddrByteToString(targetPTaddr);
            proxyArpEntry = ProxyARPTable.getInstance().getProxyArpEntry();
            if (ProxyARPTable.getInstance().isInProxyArpEntry(targetPTaddrStr)) {
                Send(input, input.length, (byte) 2, ProxyARPTable.getInstance().getMacAddr(targetPTaddrStr));
                System.out.println("proxyARP send");
            } else {
                for (int i = 0; i < 4; i++) {
                    if (targetPTaddr[i] != arpMessage.srcPTAddr.addr[i]) {
                        isTarget = false;
                        break;
                    }
                }
                if (isTarget) {
                    Send(input, input.length, (byte) 2);
                }
            }
            // 내 맥주소를 알려줄께!
            byte[] data;
            data = RemoveCappHeader(input, input.length);

        } else if (input[7] == 2) { // reply도착했을 때
            returnFlag = true;
            String dstPTAddr = tool.ptAddrByteToString(getDstPTAddress());
            byte[] srcHWAddr = new byte[6];
            for (int i = 8; i < 14; i++)
                srcHWAddr[i - 8] = input[i];
            String srcHWAddrStr = tool.hwAddrByteToString(srcHWAddr, ':');
            arpCacheTable.put(dstPTAddr, new ARPCacheRecord(srcHWAddrStr, "Complete"));

            // Timer Seq
            keyInfoStoredQueue.add(dstPTAddr);
            System.out.println("Cache Table Remaining Time Checking Start");
            timer.schedule(checkCompleted, 10000);
            ((RouterDlg) GetUpperLayer(0)).updateCacheTable();
        }
        returnFlag = false;
        return true;
    }



    public ARPLayer(String pName) {
        pLayerName = pName;
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