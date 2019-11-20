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

    public Map<String, ProxyARPRecord> getProxyArpEntry() {
        return proxyArpTable;
    }

    public boolean isInProxyArpEntry(String hostIpAddr) {
        boolean state;
        for (Map.Entry<String, ProxyARPRecord> entry : proxyArpTable.entrySet()) {
            if (entry.getValue().hostIpAddr.equals(hostIpAddr)) {
                return true;
            }
        }
        return false;
    }

    public String getMacAddr(String hostID) {
        for (Map.Entry<String, ProxyARPRecord> entry : proxyArpTable.entrySet()) {
            if (entry.getValue().hostIpAddr.equals(hostID)) {
                return entry.getValue().routerMacAddr;
            }
        }
        return null;
    }
}

class ProxyARPRecord {
    String hostIpAddr;
    String routerMacAddr;

    public ProxyARPRecord(String hostIpAddr, String routerMacAddr) {
        this.hostIpAddr = hostIpAddr;
        this.routerMacAddr = routerMacAddr;
    }
}