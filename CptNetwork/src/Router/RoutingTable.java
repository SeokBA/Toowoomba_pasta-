package Router;

import java.util.HashMap;
import java.util.Map;

public class RoutingTable {
    private static Map<String, RoutingRecord> routingTable = new HashMap<>();

    private static class _Holder{
        // instance 최초 1회 할당.
        private static final RoutingTable instance=new RoutingTable();
    }

    public static RoutingTable getInstance(){
        return _Holder.instance;
    }

    public Map<String, RoutingRecord> getCacheTable(){
        return routingTable;
    }
}

class RoutingRecord {
    private String dstAddr;
    private String gateway;
    private int metric;
    private String flag;
    private int interfaceNum;

    public RoutingRecord(String dstAddr, String gateway, int metric, String flag, int interfaceNum) {
        this.dstAddr = dstAddr;
        this.gateway = gateway;
        this.metric = metric;
        this.flag = setFlag(flag);
        this.interfaceNum = interfaceNum;
    }

    public String setFlag(String flag){
        String flagTemp = "";
        if(flag.contains("U"))
            flagTemp += "U";
        if(flag.contains("G"))
            flagTemp += "G";
        if(flag.contains("H"))
            flagTemp += "H";
        if(flag.contains("D"))
            flagTemp += "D";
        if(flag.contains("M"))
            flagTemp += "M";
        return flagTemp;
    }
}
