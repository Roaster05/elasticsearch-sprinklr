package org.elasticsearch.action.admin.cluster.blacklist;

import org.elasticsearch.action.support.master.MasterNodeRequest;
import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.cluster.ack.AckedRequest;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.core.TimeValue;

import java.io.IOException;

public class BlacklistUpdateRequest extends MasterNodeRequest<BlacklistUpdateRequest> implements AckedRequest {

    private String blacklistItem;

    public BlacklistUpdateRequest(StreamInput in) throws IOException {
        super(in);
        blacklistItem = in.readString();
    }

    public BlacklistUpdateRequest(String blacklistItem) {
        this.blacklistItem = blacklistItem;
    }

    public String getBlacklistItem() {
        return blacklistItem;
    }

    @Override
    public ActionRequestValidationException validate() {
        return null; // Add validation logic if necessary
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        out.writeString(blacklistItem);
    }

    @Override
    public TimeValue ackTimeout() {
        return null;
    }
}
