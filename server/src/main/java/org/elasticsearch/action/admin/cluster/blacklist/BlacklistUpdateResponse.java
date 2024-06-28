package org.elasticsearch.action.admin.cluster.blacklist;

import org.elasticsearch.action.ActionResponse;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.common.xcontent.StatusToXContentObject;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.xcontent.ToXContent;
import org.elasticsearch.xcontent.XContentBuilder;

import java.io.IOException;


/**
 * Represents a response for updating the blacklist in the cluster. Extends {@link ActionResponse}
 * and implements {@link StatusToXContentObject}.
 *
 * <p>The response includes whether the update was acknowledged by the cluster.
 *
 * <p>Provides methods to serialize and deserialize from streams, and to generate XContent for REST responses.
 */
public class BlacklistUpdateResponse extends ActionResponse implements StatusToXContentObject{

    private boolean acknowledged;

    public BlacklistUpdateResponse(StreamInput in) throws IOException {
        super(in);
        acknowledged = in.readBoolean();
    }

    public BlacklistUpdateResponse() {
        super();
    }

    public boolean isAcknowledged() {
        return acknowledged;
    }

    @Override
    public void writeTo(StreamOutput out) throws IOException {
        // Call to the superclass method
        out.writeBoolean(acknowledged);
    }

    public XContentBuilder toXContent(XContentBuilder builder, ToXContent.Params params) throws IOException {
        builder.startObject();
        builder.field("acknowledged", acknowledged);
        builder.endObject();
        return builder;
    }

    @Override
    public RestStatus status() {
        return RestStatus.OK;
    }
}
