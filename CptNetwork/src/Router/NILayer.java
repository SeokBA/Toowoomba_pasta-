package Router;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import org.jnetpcap.Pcap;
import org.jnetpcap.PcapIf;
import org.jnetpcap.packet.PcapPacket;
import org.jnetpcap.packet.PcapPacketHandler;

public class NILayer implements BaseLayer{
	public int nUnderLayerCount=0;
	public int nUpperLayerCount=0;
	public String pLayerName=null;
	public BaseLayer p_UnderLayer=null;
	public ArrayList<BaseLayer> p_aUnderLayer = new ArrayList<BaseLayer>();
	public ArrayList<BaseLayer> p_aUpperLayer=new ArrayList<BaseLayer>();
	
	int m_iNumAdapter;
	public Pcap m_AdapterObject;
	public PcapIf device;
	public List<PcapIf> m_pAdapterList;
	StringBuilder errbuf = new StringBuilder();
	
	public NILayer(String pName) {
		pLayerName=pName;
		m_pAdapterList=new ArrayList<PcapIf>();
		m_iNumAdapter=0;
		SetAdapterList();
	}
	
	public void PacketStartDriver() {
		int snaplen=64*1024;
		int flags=Pcap.MODE_PROMISCUOUS;
		int timeout=3*1000; // 10*1000;
		m_AdapterObject=Pcap.openLive(m_pAdapterList.get(m_iNumAdapter).getName(), snaplen, flags, timeout, errbuf);
	}
	public void SetAdapterNumber(int iNum) {
		m_iNumAdapter = iNum;
		PacketStartDriver();
		Receive();
	}
	
	public void SetAdapterList() {
		int r=Pcap.findAllDevs(m_pAdapterList, errbuf);
		if(r==Pcap.NOT_OK || m_pAdapterList.isEmpty()) {
			System.out.printf("Can't read list of devices, error is %s",errbuf.toString());
		}
	}

	public List<String> GetAdapterList(){
		List list=new ArrayList<String>();
		for(int i=0; i<m_pAdapterList.size(); i++){
			list.add(m_pAdapterList.get(i).getDescription());
		}
		return list;
	}

	public byte[] GetMacAddress(int i) throws IOException {
		return m_pAdapterList.get(i).getHardwareAddress();
	}

	public String GetDescription(int i){
		return m_pAdapterList.get(i).getDescription();
	}

	public boolean Receive() {
		Receive_Thread thread = new Receive_Thread(m_AdapterObject, this.GetUpperLayer(0));
		Thread obj=new Thread(thread);
		obj.start();
		return false;
	}
	
	public boolean Send(byte[] input, int length) {
		ByteBuffer buf=ByteBuffer.wrap(input);
		if(m_AdapterObject.sendPacket(buf)!=Pcap.OK) {
			System.err.println(m_AdapterObject.getErr());
			return false;
		}
		return true;
	}
	
	@Override
	public String GetLayerName() {
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
		if (nindex < 0 || nindex > nUpperLayerCount || nUpperLayerCount < 0)
			return null;
		return p_aUpperLayer.get(nindex);
	}

	@Override
	public void SetUnderLayer(BaseLayer pUnderLayer) {
		// TODO Auto-generated method stub
		if (pUnderLayer == null)
			return;
		this.p_UnderLayer = pUnderLayer;
	}

	@Override
	public void SetUpperLayer(BaseLayer pUpperLayer) {
		// TODO Auto-generated method stub
		if (pUpperLayer == null)
			return;
		this.p_aUpperLayer.add(nUpperLayerCount++, pUpperLayer);
		// nUpperLayerCount++;
	}

	@Override
	public void SetUpperUnderLayer(BaseLayer pUULayer) {
		this.SetUpperLayer(pUULayer);
		pUULayer.SetUnderLayer(this);
	}	
}


class Receive_Thread implements Runnable {
    byte[] data;
    Pcap AdapterObject;
    BaseLayer UpperLayer;
    public Receive_Thread(Pcap m_AdapterObject, BaseLayer m_UpperLayer) {
    	AdapterObject = m_AdapterObject;
    	UpperLayer = m_UpperLayer;
    }

    @Override
    public void run() {
        while (true) {
        	PcapPacketHandler<String> jpacketHandler = new PcapPacketHandler<String>() {
				@Override
				public void nextPacket(PcapPacket packet, String user) {
					data=packet.getByteArray(0, packet.size());
					UpperLayer.Receive(data);
				}
        	};
        	
        	AdapterObject.loop(100000, jpacketHandler, "");
        }
    }
}