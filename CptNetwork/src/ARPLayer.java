import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ARPLayer implements BaseLayer {
    public int nUpperLayerCount = 0;
    public String pLayerName = null;
    public BaseLayer p_UnderLayer = null;
    public ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<BaseLayer>();
    public Map<String, _Data> cacheTable = new HashMap<>();

    private class _Data {
        String hardwareAddr;
        String status;

        public _Data(String hardwareAddr, String status) {
            this.hardwareAddr = hardwareAddr;
            this.status = status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
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
            this.hardwareType=new byte[2];
            this.hardwareType[0] = 0; // default
            this.hardwareType[1] = 1; // 이 부분만 사용
            this.protocolType=new byte[2];
            setProtocolType(hexToByte2(800));
            this.lengOfHardwareAddr = 6; // default
            this.lengOfProtocolAddr = 4; // default
            this.opcode=new byte[2];
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

    public void setSrcHWAddress(String address) {
        // 보내는 사람의 맥주소, GUI에서 세팅
        String[] sp = address.split(":");
        for (int i = 0; i < sp.length; i++) {
            byte toByte;
            int toInt = Integer.decode("0x" + sp[i]);
            if (toInt > 127)
                toByte = (byte) (toInt - 256);
            else
                toByte = (byte) toInt;
            arpMessage.srcHWAddr.addr[i] = toByte;
        }
    }

    public byte[] getSrcHWAddress() {
        return arpMessage.srcHWAddr.addr;
    }

    public byte[] getDstPTAddress() {
        return arpMessage.dstPTAddr.addr;
    }

    public void setDstHWAddress(String address) {
        if (address == null) // 상대방 맥주소 몰라요
            address = "0.0.0.0";
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

    public void setSrcPTAddress(String address) {
        // 보내는 사람의 IP주소, GUI에서 세팅
        address = address.trim(); // 혹시나 붙을 공백 제거
        String[] sp = address.split(".");
        for (int i = 0; i < sp.length; i++) {
            byte toByte;
            int toInt = Integer.parseInt(sp[i]);
            if (toInt > 127)
                toByte = (byte) (toInt - 256);
            else
                toByte = (byte) toInt;
            arpMessage.srcPTAddr.addr[i] = toByte;
        }
    }

    public void setDstPTAddress(String address) {
        // 받는 사람의 IP주소, IPLayer에서 보낸 값으로 세팅
        address = address.trim(); // 혹시나 붙을 공백 제거
        String[] sp = address.split(".");
        for (int i = 0; i < sp.length; i++) {
            byte toByte;
            int toInt = Integer.parseInt(sp[i]);
            if (toInt > 127)
                toByte = (byte) (toInt - 256);
            else
                toByte = (byte) toInt;
            arpMessage.dstPTAddr.addr[i] = toByte;
        }
    }

    public void setSrcPTAddress(byte[] input) {
        // 받는 사람의 IP주소, IPLayer에서 보낸 값으로 세팅
        for (int i = 0; i < 4; i++) {
            arpMessage.srcPTAddr.addr[i] = input[i + 11];
        }
    }

    public void setDstPTAddress(byte[] input) {
        // 받는 사람의 IP주소, IPLayer에서 보낸 값으로 세팅
        for (int i = 0; i < 4; i++) {
            arpMessage.dstPTAddr.addr[i] = input[i + 15];
        }
    }

    public byte[] fillDstHWAddress(byte[] input){
        for (int i = 18; i < 24; i++)
            input[i] = arpMessage.dstHWAddr.addr[i-18];
        return input;
    }

    public byte[] swapping(byte[] input){
        byte[] temp=new byte[10];
        for(int i=8; i<18; i++){
            temp[i-8]=input[i];
        }
        for(int i=18; i<28; i++){
            input[i-10]=input[i];
        }
        for(int i=18; i<28; i++){
            input[i]=temp[i-18];
        }
        return input;
    }


    public boolean Send(byte[] input, int length, byte opcode) {
        if(opcode==1){ // req

            setOpcode(opcode);
            setSrcPTAddress(input); // IPLayer에서 내려와야함
            setDstPTAddress(input); // IPLayer에서 내려와야함
            setDstHWAddress(null);
            setSrcHWAddress("127.0.0.1"); // GUI에서 채워줘야함

            String dstPTAddr=bytePTAddrToString(getDstPTAddress());
            cacheTable.put(dstPTAddr,new _Data("????????", "Incomplete"));

            byte[] c = ObjToByte(arpMessage, input, length);
            ((EthernetLayer) this.GetUnderLayer()).Send(c, length + 26, 1);
            return true;
        } else if(opcode==2){ // reply
            input=fillDstHWAddress(input);
            input=swapping(input);
            input[7]=opcode;
            byte[] dstAddr=new byte[6];
            for (int i = 18; i < 24; i++)
                dstAddr[i-18] = input[i];

            ((EthernetLayer) this.GetUnderLayer()).Send(input, length, 2, dstAddr);
            return true;
        }
        return false;
    }


    public byte[] ObjToByte(_ARP_MESSAGE msg, byte[] input, int length) {
        byte[] buf = new byte[length + 28];
        buf[0] = msg.hardwareType[0];
        buf[1] = msg.hardwareType[1];
        for (int i = 2; i < 4; i++)
            buf[i] = msg.protocolType[i];
        buf[4] = msg.lengOfHardwareAddr;
        buf[5] = msg.lengOfProtocolAddr;
        buf[6] = msg.opcode[0];
        buf[7] = msg.opcode[1];
        for (int i = 8; i < 14; i++)
            buf[i] = msg.srcHWAddr.addr[i-8];
        for (int i = 14; i < 18; i++)
            buf[i] = msg.srcPTAddr.addr[i-14];
        for (int i = 18; i < 24; i++)
            buf[i] = msg.dstHWAddr.addr[i-18];
        for (int i = 24; i < 28; i++)
            buf[i] = msg.dstPTAddr.addr[i-24];
        for (int i = 0; i < input.length; i++)
            buf[28 + i] = input[i];
        return buf;
    }

    public byte[] RemoveCappHeader(byte[] input, int length) {
        byte[] buf = new byte[length - 26];
        for (int i = 0; i < length - 26; i++) {
            buf[i] = input[i + 26];
        }
        return buf;
    }

    public synchronized boolean Receive(byte[] input) {
        // 자기가 보낸거면 뭐 할거 없으므로 걸러냄
        byte[] senderHWaddr=new byte[6];
        for (int i = 8; i < 14; i++)
            senderHWaddr[i-8] = input[i];
        boolean isMyPacket=true;
        for(int i=0; i<6; i++){
            if(senderHWaddr[i]!=arpMessage.srcHWAddr.addr[i]){
                isMyPacket=false;
                break;
            }
        }
        if (isMyPacket)
            return false;

        if(input[7]==1){ // 상대방이 보낸 req받았을 때
            // 받아왔는데 자기가 아니고 테이블에 저장되있는 맥주소가 아니면 업데이트
            byte[] senderPTaddr=new byte[4];
            for (int i = 14; i < 18; i++)
                senderPTaddr[i-14] = input[i];

            String senderPTaddrStr=bytePTAddrToString(senderPTaddr);
            if(cacheTable.get(senderPTaddrStr)==null){
                String senderHWaddrStr=bytePTAddrToString(senderHWaddr);
                cacheTable.put(senderPTaddrStr,new _Data(senderHWaddrStr,"Complete"));
                // todo GUI에 이 테이블 데이터 업데이트 과정 필요
            }
        } else if(input[7]==2) { // reply도착했을 때
            String dstPTAddr=bytePTAddrToString(getDstPTAddress());
            byte[] srcHWAddr=new byte[6];
            for (int i = 8; i < 14; i++)
                srcHWAddr[i-8] = input[i];
            String srcHWAddrStr=byteHWAddrToString(srcHWAddr);
            cacheTable.put(dstPTAddr,new _Data(srcHWAddrStr, "Complete"));
        }


        // 맥주소를 알고싶은 타겟이 자신인지 확인
        byte[] TargetPTaddr=new byte[4];
        boolean isTarget=true;
        for (int i = 24; i < 28; i++)
            TargetPTaddr[i-24] = input[i];
        for(int i=0; i<6; i++){
            if(TargetPTaddr[i]!=arpMessage.srcHWAddr.addr[i]){
                isTarget=false;
                break;
            }
        }
        // 내 맥주소를 알려줄께!
        if(isTarget){
            Send(input,input.length,(byte)2);
        }



        byte[] data;
        data = RemoveCappHeader(input, input.length);
//        this.GetUpperLayer(0).Receive(data);
        return true;
    }

    byte[] hexToByte2(int hexValue) {
        String hex = "0x" + hexValue;
        int toInt = Integer.decode(hex);
        return intToByte2(toInt);
    }

    byte[] intToByte2(int value) {
        byte[] temp = new byte[2];
        temp[1] = (byte) (value >> 8);
        temp[0] = (byte) value;
        return temp;
    }

    byte[] intToByte4(int value) {
        byte[] temp = new byte[4];
        //temp[1] = (byte) (value >> 8);
        //temp[0] = (byte) value;
        temp[0] |= (byte) ((value & 0xFF000000) >> 24);
        temp[1] |= (byte) ((value & 0xFF0000) >> 16);
        temp[2] |= (byte) ((value & 0xFF00) >> 8);
        temp[3] |= (byte) (value & 0xFF);
        return temp;
    }

    String byteHWAddrToString(byte[] addr) {
        StringBuilder sb = new StringBuilder();
        int temp = 0;
        for (int j = 0; j < addr.length; j++) {
            if (sb.length() != 0)
                sb.append(':');
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

    String bytePTAddrToString(byte[] addr) {
        StringBuilder sb = new StringBuilder();
        int temp = 0;
        for (int j = 0; j < addr.length; j++) {
            if (sb.length() != 0)
                sb.append('.');
            if (addr[j] < 0)
                sb.append(addr[j] + 256);
//                temp = addr[j] + 256;
            else
                sb.append(addr[j]);
//                temp = addr[j];
//            String hex = Integer.toHexString(temp).toUpperCase();
            sb.append(temp);
        }

        return sb.toString();
    }

    public ARPLayer(String pName) {
        pLayerName = pName;

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
