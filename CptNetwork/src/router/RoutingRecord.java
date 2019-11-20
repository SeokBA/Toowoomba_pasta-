package router;

public class RoutingRecord {
    enum Flag {
        U("U"), G("G"), H("H"), D("D"), M("M");

        Flag(String flag) {
        }
    }

    private String gateway;
    private int metric;
    private Flag flag;

    public RoutingRecord(String gateway, int metric, String flag) {
        this.gateway = gateway;
        this.metric = metric;
        this.flag = Flag.valueOf(flag);
    }
}
