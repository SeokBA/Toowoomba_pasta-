package router;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class RoutingTable {
    private ArrayList<RoutingRecord> routingTable = new ArrayList<>();

    private static class _Holder{
        // instance 최초 1회 할당.
        private static final RoutingTable instance=new RoutingTable();
    }

    public static RoutingTable getInstance(){
        return _Holder.instance;
    }

    public ArrayList<RoutingRecord> getTable() {
        return routingTable;
    }

    public String[][] getStringArray(){
        String[][] arr = new String[routingTable.size()][];
        int idx = 0;
        for(RoutingRecord i:routingTable){
            arr[idx] = i.getStringArray();
            idx++;
        }
        return arr;
    }
    
    public void sortTable() {
    	int bitCount00 = 0;
    	int bitCount01 = 0;
    	
    	for(int i=0; i<routingTable.size()-1; i++) {
    		for(int j=0; j<routingTable.get(i).getNetmask().length; j++) {
    			bitCount00 += Integer.bitCount(routingTable.get(i).getNetmask()[j]);
    			bitCount01 += Integer.bitCount(routingTable.get(i+1).getNetmask()[j]);
    		}
    		if(bitCount00 < bitCount01)
    			swapElement(i, i+1);
    		bitCount00 = 0;
    		bitCount01 = 0;
    	}
    }
    
    public void swapElement(int i, int j) {
    	RoutingRecord temp = routingTable.get(i);
    	routingTable.set(i, routingTable.get(j));
    	routingTable.set(j, temp);
    }
    
    public int getSize() {
    	return routingTable.size();
    }
    
}

class RoutingRecord {
    private byte[] dstAddr;
    private byte[] netmask;
    private byte[] gateway;
    private String flag;
    private int interfaceNum;
    private int metric;

    private Tools tools = new Tools();

    public RoutingRecord(String dstAddr, String netmask, String gateway, String flag, int interfaceNum, int metric) {
        this.dstAddr = tools.ipAddrStringToByte(dstAddr);
        this.netmask = tools.ipAddrStringToByte(netmask);
        if(gateway.equals("*"))
            this.gateway= new byte[]{-1, -1, -1, -1};
        else
            this.gateway = tools.ipAddrStringToByte(gateway);
        this.metric = metric;
        this.flag = tools.setFlag(flag);
        this.interfaceNum = interfaceNum;
    }

    public byte[] getDstAddr() {
        return dstAddr;
    }

    public void setDstAddr(byte[] dstAddr) {
        this.dstAddr = dstAddr;
    }

    public byte[] getNetmask() {
        return netmask;
    }

    public void setSubNetmask(byte[] netmask) {
        this.netmask = netmask;
    }

    public byte[] getGateway() {
        return gateway;
    }

    public void setGateway(byte[] gateway) {
        this.gateway = gateway;
    }

    public int getMetric() {
        return metric;
    }

    public void setMetric(int metric) {
        this.metric = metric;
    }

    public int getInterfaceNum() {
        return interfaceNum;
    }

    public void setInterfaceNum(int interfaceNum) {
        this.interfaceNum = interfaceNum;
    }

    public String getFlag() {
        return flag;
    }

    public String[] getStringArray() {
        return new String[] { tools.ipAddrByteToString(dstAddr), tools.ipAddrByteToString(netmask),
                tools.ipAddrByteToString(gateway), flag, String.valueOf(interfaceNum), String.valueOf(metric) };
    }
}
