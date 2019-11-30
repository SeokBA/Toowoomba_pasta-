package router;
import java.util.HashMap;
import java.util.Map;

public class ARPCacheTable {
    private static Map<String, ARPCacheRecord> arpCacheTable = new HashMap<>();

    private static class _Holder{
        // instance 최초 1회 할당.
        private static final ARPCacheTable instance=new ARPCacheTable();
    }

    public static ARPCacheTable getInstance(){
        return _Holder.instance;
    }

    public Map<String, ARPCacheRecord> getTable(){
        return arpCacheTable;
    }
    
    public boolean isInArpEntry(String hostIpAddr) {
        for (Map.Entry<String, ARPCacheRecord> entry : arpCacheTable.entrySet()) {
            if (entry.getValue().ipAddr.equals(hostIpAddr)) {
                return true;
            }
        }
        return false;
    }
    
    public String getMacAddr(String hostID) {
        for (Map.Entry<String, ARPCacheRecord> entry : arpCacheTable.entrySet()) {
            if (entry.getValue().ipAddr.equals(hostID)) {
                return entry.getValue().hwAddr;
            }
        }
        return null;
    }

    public String[][] getStringArray(){
        String[][] arr = new String[arpCacheTable.size()][];
        int idx = 0;
        for(String i:arpCacheTable.keySet()){
            arr[idx] = arpCacheTable.get(i).getStringArray();
            idx++;
        }
        return arr;
    }
}

class ARPCacheRecord{
    String ipAddr;
    String hwAddr;
    String status;

    public ARPCacheRecord(String ipAddr, String hwAddr, String status) {
        this.ipAddr = ipAddr;
        this.hwAddr = hwAddr;
        this.status = status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String[] getStringArray(){
        return new String[]{ipAddr, hwAddr, status};
    }
}