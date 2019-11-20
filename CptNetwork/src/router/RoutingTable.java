package router;

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

    public Map<String, RoutingRecord> getTable(){
        return routingTable;
    }

    public String[][] getStringArray(){
        String[][] arr = new String[routingTable.size()][];
        int idx = 0;
        for(String i:routingTable.keySet()){
            arr[idx] = routingTable.get(i).getStringArray();
            idx++;
        }
        return arr;
    }
}

class RoutingRecord {
    private String dstAddr;
    private String netmask;
    private String gateway;
    private String flag;
    private int interfaceNum;
    private int metric;

    public RoutingRecord(String dstAddr, String netmask, String gateway, String flag, int interfaceNum, int metric) {
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

    public String[] getStringArray(){
        return new String[]{dstAddr, netmask, gateway, flag, String.valueOf(interfaceNum), String.valueOf(metric)};
    }
}
