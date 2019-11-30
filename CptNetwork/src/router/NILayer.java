package router;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.jnetpcap.Pcap;
import org.jnetpcap.PcapIf;
import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.packet.PcapPacketHandler;

public class NILayer implements BaseLayer{
	private int nUnderLayerCount = 0;
	private int nUpperLayerCount = 0;
	private String pLayerName = null;
	private BaseLayer p_UnderLayer = null;
	private ArrayList<BaseLayer> p_aUnderLayer = new ArrayList<>();
	private ArrayList<BaseLayer> p_aUpperLayer = new ArrayList<>();

	private int m_iNumAdapter;
	private Pcap m_AdapterObject;
	public PcapIf device;
	public List<PcapIf> m_pAdapterList;
	private StringBuilder errbuf = new StringBuilder();
	
	public NILayer(String pName) {
		pLayerName=pName;
		m_pAdapterList= new ArrayList<>();
		m_iNumAdapter=0;
		setAdapterList();
	}

	private void packetStartDriver() {
		int snaplen=64*1024;
		int flags=Pcap.MODE_PROMISCUOUS;
		int timeout=500; // 10*1000;
		m_AdapterObject=Pcap.openLive(m_pAdapterList.get(m_iNumAdapter).getName(), snaplen, flags, timeout, errbuf);
	}
	public void setAdapterNumber(int iNum) {
		m_iNumAdapter = iNum;
		packetStartDriver();
		receive();
	}

	private void setAdapterList() {
		int r=Pcap.findAllDevs(m_pAdapterList, errbuf);
		if(r==Pcap.NOT_OK || m_pAdapterList.isEmpty()) {
			System.out.printf("Can't read list of devices, error is %s",errbuf.toString());
		}
	}

	public List<String> getAdapterList(){
		List list=new ArrayList<String>();
		for(int i=0; i<m_pAdapterList.size(); i++){
			list.add(m_pAdapterList.get(i).getDescription());
		}
		return list;
	}

	public byte[] getMacAddress(int i) throws IOException {
		return m_pAdapterList.get(i).getHardwareAddress();
	}

	public byte[] getIpAddress(int i) { // getAddress해서 가져온 리스트 중 IPv4가 0번째에 위치함
		return m_pAdapterList.get(i).getAddresses().get(0).getAddr().getData();
	}

	public String getDescription(int i){
		return m_pAdapterList.get(i).getDescription();
	}

	public boolean receive() {
		Receive_Thread thread = new Receive_Thread(m_AdapterObject, this.getUpperLayer(0));
		Thread obj=new Thread(thread);
		obj.start();
		return false;
	}
	
	public boolean send(byte[] input, int length) {
		ByteBuffer buf=ByteBuffer.wrap(input);
		if(m_AdapterObject.sendPacket(buf)!=Pcap.OK) {
			System.err.println(m_AdapterObject.getErr());
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


class Receive_Thread implements Runnable {
    byte[] data;
	private Pcap adapterObject;
	private BaseLayer upperLayer;
    public Receive_Thread(Pcap m_AdapterObject, BaseLayer m_UpperLayer) {
    	adapterObject = m_AdapterObject;
    	upperLayer = m_UpperLayer;
    }

    @Override
    public void run() {
        while (true) {
        	PcapPacketHandler<String> jpacketHandler = (packet, user) -> {
				data=packet.getByteArray(0, packet.size());
				upperLayer.receive(data);
			};
        	adapterObject.loop(-1, jpacketHandler, "");
        }
    }
}