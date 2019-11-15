import java.util.ArrayList;

public class EthernetLayer implements BaseLayer{
	public int nUpperLayerCount=0;
	public String pLayerName=null;
	public BaseLayer p_UnderLayer=null;
	public ArrayList<BaseLayer> p_aUpperLayer=new ArrayList<>();

	private class _ETHERNET_ADDR{
		private byte[] addr = new byte[6];
		public _ETHERNET_ADDR() {
			this.addr[0]=(byte)0x00;
			this.addr[1]=(byte)0x00;
			this.addr[2]=(byte)0x00;
			this.addr[3]=(byte)0x00;
			this.addr[4]=(byte)0x00;
			this.addr[5]=(byte)0x00;
		}
	}
	private class _ETHERNET_Frame{
		_ETHERNET_ADDR enet_dstaddr;
		_ETHERNET_ADDR enet_srcaddr;
		byte[] enet_type;
		byte[] enet_data;
		public _ETHERNET_Frame() {
			this.enet_dstaddr = new _ETHERNET_ADDR();
			this.enet_srcaddr = new _ETHERNET_ADDR();
			this.enet_type=new byte[2];
			this.enet_data=null;
		}
	}

	_ETHERNET_Frame efHeader = new _ETHERNET_Frame();

	public void setDstBroadCase(){
		for(int i=0; i<6; i++){
			efHeader.enet_dstaddr.addr[i]=-1; // 0xFF
		}
	}

	public void setDstAddress(byte[] addr){
		for(int i=0; i<addr.length; i++){
			efHeader.enet_dstaddr.addr[i]=addr[i];
		}
	}

	public void setDstAddress(String address) {
		String[] sp=address.split(":");
		for(int i=0; i<sp.length; i++){
			byte toByte;
			int toInt = Integer.decode("0x"+sp[i]);
			if(toInt>127)
				toByte=(byte)(toInt-256);
			else
				toByte=(byte)toInt;
			efHeader.enet_dstaddr.addr[i]=toByte;
		}
	}

	public void setSrcAddress(String address) {
		String[] sp=address.split(":");
		for(int i=0; i<sp.length; i++){
			byte toByte;
			int toInt = Integer.decode("0x"+sp[i]);
			if(toInt>127)
				toByte=(byte)(toInt-256);
			else
				toByte=(byte)toInt;
			efHeader.enet_srcaddr.addr[i]=toByte;
		}
	}

	public boolean Send(byte[] input, int length, int isArp) {
		byte[] c=ObjToByte(efHeader, input, length, isArp, null);
		this.GetUnderLayer().Send(c,length+14);
		return true;
	}

	public boolean Send(byte[] input, int length, int isArp, byte[] dstAddr) { // opcode 2일때
		byte[] c=ObjToByte(efHeader, input, length, isArp, dstAddr);
		this.GetUnderLayer().Send(c,length+14);
		return true;
	}

	public void setType(byte[] type){
        efHeader.enet_type[0]=type[1];
        efHeader.enet_type[1]=type[2];
    }

	public byte[] ObjToByte(_ETHERNET_Frame Header, byte[] input, int length, int isArp, byte[] dstAddr) {
		byte[] buf = new byte[length + 14];

		if(isArp>0) { // ARPLayer에서 내려옴
			setType(hexToByte2(8086));
			if(isArp==1){
				setDstBroadCase();
			} else if(isArp==2){
				setDstAddress(dstAddr);
			}
		}
		else // message
			setType(hexToByte2(0));

		for(int i=0; i<6; i++) // Receiver
			buf[i] = Header.enet_dstaddr.addr[i];
		for(int i=6; i<12; i++) // Sender
			buf[i] = Header.enet_srcaddr.addr[i-6];

        buf[12] = Header.enet_type[0];
        buf[13] = Header.enet_type[1];
		for (int i = 0; i < input.length; i++)
			buf[14 + i] = input[i];
		return buf;
	}

	public byte[] RemoveCappHeader(byte[] input, int length) {
		byte[] buf = new byte[length-14];
		for(int i=0; i<length-14; i++){
			buf[i]=input[i+14];
		}
		return buf;
	}

	public boolean IsItMyPacket(byte[] input) {
		for (int i = 0; i < 6; i++) {
			if (efHeader.enet_srcaddr.addr[i] == input[6 + i])
				continue;
			else
				return false;
		}
		return true;
	}

	public synchronized boolean Receive(byte[] input) {
		if(IsItMyPacket(input)) // 자기껀 버림
			return false;

		byte[] data;

		// broadcast & 올바른 주소 체크
		boolean isBroad=false;
		for(int i=0; i<6; i++){
			if(input[i]==(byte)0xFF&&input[12]==(byte)9){
				isBroad=true;
			}
			else{
				isBroad=false;
				break;
			}
		}
		if(!isBroad) {
			for (int i = 0; i < 6; i++) {
				if (input[i] != efHeader.enet_srcaddr.addr[i]) {
					return false;
				}
			}
		}

		data = RemoveCappHeader(input, input.length);
//
		boolean isArp=true;
		byte[] type=new byte[2];
		type[0]=input[12];
		type[1]=input[13];

		byte[] arpPacket=hexToByte2(8086);
		for(int i=0; i<arpPacket.length; i++){
			if(type[i]!=arpPacket[i]){
				isArp=false;
				break;
			}
		}

		// todo : 레이어 연결 확인하고 수정 필요
		if(isArp) // arp
			this.GetUpperLayer(0).Receive(data);
		else // message
			this.GetUpperLayer(1).Receive(data);
		return true;
	}

	byte[] hexToByte2(int hexValue){
		String hex="0x"+hexValue;
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
		temp[0]|=(byte)((value&0xFF000000)>>24);
		temp[1]|=(byte)((value&0xFF0000)>>16);
		temp[2]|=(byte)((value&0xFF00)>>8);
		temp[3]|=(byte)(value&0xFF);
		return temp;
	}

	public EthernetLayer(String pName) {
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
