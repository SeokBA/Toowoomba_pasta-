package router;

import java.util.ArrayList;

public class IPLayer implements BaseLayer {
    private int nUnderLayerCount = 0;
    private int nUpperLayerCount = 0;
    private String pLayerName = null;
    private BaseLayer p_UnderLayer = null;
    private ArrayList<BaseLayer> p_aUnderLayer = new ArrayList<>();
    private ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<>();
    private String targetIP; //내가 요청한 ARP 의 ip 주소
    private byte[] ICMPbuffer;
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
    
    public byte[] getSrcAddress(){
    	byte[] srcAddr = new byte[4];
        for(int i=0; i<srcAddr.length; i++){
            srcAddr[i]=ipHeader.ipSrc.addr[i];
        }
        return srcAddr;
    }
    
    public boolean send(byte[] input, int length) {
        byte[] c = objToByte(ipHeader, input, length);
        ((ARPLayer) this.getUnderLayer(1)).send(c, length + 19, (byte) 1);
        return true;
    }


	public boolean send(byte[] input, int length, RoutingRecord findRecord) {
        ICMPbuffer = input;
		byte[] dstaddr = tools.extractSelectPart(input, 16, 20);
		if (findRecord.getFlag().equals("U")) { // 직접 연결이 되어있으면
		    targetIP=tools.ipAddrByteToString(dstaddr);
			if (getMacAddrArpRecord(targetIP) != null) { // 직접연결되어있고 테이블에 있으면
				((EthernetLayer) this.getUnderLayer(0)).Send(ICMPbuffer, ICMPbuffer.length, 0, dstaddr);
			} else {
				send(targetIP.getBytes(), targetIP.getBytes().length);// 연결이 되어있고 테이블에 없으면 ARP request
			}
		}

		else if (findRecord.getFlag().equals("UG")) {
			// 직접연결이 아닌 게이트웨이로 전달해야되는 경우
            String targetGateWay = tools.ipAddrByteToString(findRecord.getGateway());
			String macAddrStr = getMacAddrArpRecord(targetGateWay);
			if (macAddrStr != null) { // mac주소가 있으면 바로 보냄 받은 ICMP에 ethernet에서 주소만 바꿔서 보냄.
				((EthernetLayer) this.getUnderLayer(0)).Send(ICMPbuffer, ICMPbuffer.length, 0, findRecord.getGateway());
			} else {
				send(targetGateWay.getBytes(), targetGateWay.getBytes().length);// arp테이블에 주소가 없으면 request
			}
		}
		return false;
	}

 	//ARPCacheTable에 mac주소 찾기
	public String getMacAddrArpRecord(String targetIP) {
		if (Tools.getARPCacheTable().isInArpEntry(targetIP)) {
			return Tools.getARPCacheTable().getMacAddr(targetIP);
		}
		return null;
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
	
	public boolean dstIsMe(byte[] dstaddr, byte[] input) {
		byte[] src = getSrcAddress();
		for (int i = 0; i < 4; i++) {
			if (dstaddr[i] != src[i])
				return false;
		}
		return true;
	}

	public synchronized boolean receive(byte[] input) {
    	byte[] dstaddr = tools.extractSelectPart(input, 16, 20);
    	if(dstIsMe(dstaddr,input))
    		return true;
    	
    	if(findRoutingRecord(dstaddr) != null) {//라우팅테이블에서  맞는 gateway가져오고
	    	RoutingRecord findRecord = findRoutingRecord(dstaddr);
//	    	String targetGateWay = tools.bytePTAddrToString(findRecord.getGateway()); //라우팅에서 찾은 Gateway
	    	//라우팅테이블에 맞는 인터페이스에게 데이터 전달.
	    	if(findRecord.getInterfaceNum() != 0) {
	    		 (this.getUpperLayer(0)).getUnderLayer(2).send(input, input.length, findRecord);
	    	}else {
	    		 (this.getUpperLayer(0)).getUnderLayer(0).send(input, input.length, findRecord);
	    	}
	    	return true;
    	}
    	return false;
    }
	
	 //subnetMask와 &연산
    public RoutingRecord findRoutingRecord(byte[] input) {
    	RoutingTable routingtable = RoutingTable.getInstance();
    	for(RoutingRecord element : routingtable.getTable()) {
            boolean findflag = true;
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
	
    public void notifiedReply(String ipAddrStr, byte[] PTAddr) {//gateway에 대한 요청 arp가 reply가 오면 mac주소 바꿔서 send
    	if(targetIP!=null && targetIP.equals(ipAddrStr)) {
    		 ((EthernetLayer)this.getUnderLayer(0)).Send(ICMPbuffer,ICMPbuffer.length,0,PTAddr);
    		 targetIP = null;
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