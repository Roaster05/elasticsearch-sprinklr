package org.elasticsearch;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;

/**
 * BlacklistEntry represents a blacklisted query. It stores the query, the identifier
 * that triggered it, the execution time/memory used by the query, the timestamp of its
 * blacklisting, and the node where it was blacklisted.
 */
public class BlacklistEntry {
    private final String query;
    private final String identifier;
    private final long executionTime;
    private final LocalDateTime timestamp;
    private final String node;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
    private static final long EXPIRATION_INTERVAL_HOURS = 12; // Entry expiration interval in Hours

    public BlacklistEntry(String query, String identifier, long executionTime, LocalDateTime timestamp) {
        this.query = query;
        this.identifier = identifier;
        this.executionTime = executionTime;
        this.timestamp = timestamp;
        this.node = BlacklistData.getInstance().getnode();
    }

    public BlacklistEntry(StreamInput in) throws IOException {
        this.query = in.readString();
        this.identifier = in.readString();
        this.executionTime = in.readLong();
        this.timestamp = LocalDateTime.parse(in.readString(), formatter);
        this.node = in.readString();
    }

    public String getQuery() {
        return query;
    }

    public String getNode() { return node; }

    public String getIdentifier() {
        return identifier;
    }

    public long getExecutionTime() {
        return executionTime;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(timestamp.plusHours(EXPIRATION_INTERVAL_HOURS));
    }

    public void writeTo(StreamOutput out) throws IOException {
        out.writeString(query);
        out.writeString(identifier);
        out.writeLong(executionTime);
        out.writeString(timestamp.format(formatter));
        out.writeString(node);
    }
}
