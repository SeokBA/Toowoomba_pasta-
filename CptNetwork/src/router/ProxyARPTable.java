package router;

import java.util.HashMap;
import java.util.Map;

public class ProxyARPTable {
    // key : hostID
    // value : _ProxyArpData -> host ip address, router Mac address
    private static Map<String, ProxyARPRecord> proxyArpTable = new HashMap<>();

    private static class _Holder {
        // instance 최초 1회 할당.
        private static final ProxyARPTable instance = new ProxyARPTable();
    }

    public static ProxyARPTable getInstance() {
        return _Holder.instance;
    }

    public Map<String, ProxyARPRecord> getTable() {
        return proxyArpTable;
    }

    public boolean isInProxyArpEntry(String hostIpAddr) {
        boolean state;
        for (Map.Entry<String, ProxyARPRecord> entry : proxyArpTable.entrySet()) {
            if (entry.getValue().ipAddr.equals(hostIpAddr)) {
                return true;
            }
        }
        return false;
    }

    public String getMacAddr(String hostID) {
        for (Map.Entry<String, ProxyARPRecord> entry : proxyArpTable.entrySet()) {
            if (entry.getValue().ipAddr.equals(hostID)) {
                return entry.getValue().hwAddr;
            }
        }
        return null;
    }

    public String[][] getStringArray(){
        String[][] arr = new String[proxyArpTable.size()][];
        int idx = 0;
        for(String i:proxyArpTable.keySet()){
            arr[idx] = proxyArpTable.get(i).getStringArray();
            idx++;
        }
        return arr;
    }
}

class ProxyARPRecord {
    String ipAddr;
    String hwAddr;
    String interfaceNum;

    public ProxyARPRecord(String ipAddr, String hwAddr) {
        this.ipAddr = ipAddr;
        this.hwAddr = hwAddr;
        this.interfaceNum = "?";
    }

    public ProxyARPRecord(String ipAddr, String hwAddr, String interfaceNum) {
        this.ipAddr = ipAddr;
        this.hwAddr = hwAddr;
        this.interfaceNum = interfaceNum;
    }

    public String[] getStringArray(){
        return new String[]{ipAddr, hwAddr, interfaceNum};
    }
}