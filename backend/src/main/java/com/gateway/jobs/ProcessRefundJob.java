package com.gateway.jobs;

import java.io.Serializable;

public class ProcessRefundJob implements Serializable {
    private String refundId;

    public ProcessRefundJob() {}

    public ProcessRefundJob(String refundId) {
        this.refundId = refundId;
    }

    public String getRefundId() { return refundId; }
    public void setRefundId(String refundId) { this.refundId = refundId; }
}