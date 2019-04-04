package io.bos.accountmanager.net.eosRes;

import com.google.gson.annotations.Expose;

/**
 * Created by wuwei on 2018/8/28.
 */

public class PledgeListBean {
    /**
     * from : v5v5v5v5v5v5
     * to : 123452345345
     * net_weight : 0.1000 EOS
     * cpu_weight : 0.5800 EOS
     */

    @Expose
    private String from;
    @Expose
    private String to;
    @Expose
    private String net_weight;
    @Expose
    private String cpu_weight;

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getNet_weight() {
        return net_weight;
    }

    public void setNet_weight(String net_weight) {
        this.net_weight = net_weight;
    }

    public String getCpu_weight() {
        return cpu_weight;
    }

    public void setCpu_weight(String cpu_weight) {
        this.cpu_weight = cpu_weight;
    }
}
