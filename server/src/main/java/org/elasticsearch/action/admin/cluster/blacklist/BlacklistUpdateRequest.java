package org.elasticsearch.action.admin.cluster.blacklist;

import org.elasticsearch.Blacklist;
import org.elasticsearch.action.support.master.MasterNodeRequest;
import org.elasticsearch.action.ActionRequestValidationException;
import org.elasticsearch.cluster.ack.AckedRequest;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.core.TimeValue;

import java.io.IOException;


/**
 * Represents a request to update the blacklist in the cluster. Extends {@link MasterNodeRequest}
 * and implements {@link AckedRequest}.
 *
 * <p>The request includes a {@link Blacklist} item to be updated.
 *
 * <p>Provides methods for serialization and deserialization from streams.
 */
public class BlacklistUpdateRequest extends MasterNodeRequest<BlacklistUpdateRequest> implements AckedRequest {

    private Blacklist blacklistItem;

    public BlacklistUpdateRequest(StreamInput in) throws IOException {
        super(in);
        blacklistItem = Blacklist.readFrom(in);
    }

    public BlacklistUpdateRequest(Blacklist blacklistItem) {
        this.blacklistItem = blacklistItem;
    }

    public Blacklist getBlacklistItem() {
        return blacklistItem;
    }

    @Override
    public ActionRequestValidationException validate() {
        return null; // Add validation logic if necessary
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        super.writeTo(out);
        blacklistItem.writeTo(out);
    }

    @Override
    public TimeValue ackTimeout() {
        return null;
    }
}
