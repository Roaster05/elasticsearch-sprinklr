package org.elasticsearch;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;

public class BlacklistEntry {
    private final String query;
    private final String identifier;
    private final long executionTime;
    private final LocalDateTime timestamp;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
    private static final long EXPIRATION_INTERVAL_HOURS = 6; // Entry expiration interval in minutes

    public BlacklistEntry(String query, String identifier, long executionTime, LocalDateTime timestamp) {
        this.query = query;
        this.identifier = identifier;
        this.executionTime = executionTime;
        this.timestamp = timestamp;
    }

    public BlacklistEntry(StreamInput in) throws IOException {
        this.query = in.readString();
        this.identifier = in.readString();
        this.executionTime = in.readLong();
        this.timestamp = LocalDateTime.parse(in.readString(), formatter);
    }

    public String getQuery() {
        return query;
    }

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
    }
}
