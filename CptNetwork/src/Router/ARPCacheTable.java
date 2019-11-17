package Router;

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

    public Map<String, ARPCacheRecord> getCacheTable(){
        return arpCacheTable;
    }
}

class ARPCacheRecord{
    String hardwareAddr;
    String status;

    public ARPCacheRecord(String hardwareAddr, String status) {
        this.hardwareAddr = hardwareAddr;
        this.status = status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}