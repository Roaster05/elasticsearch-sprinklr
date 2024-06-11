package org.elasticsearch;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class BlacklistEntry {
    private final String query;
    private final String identifier;
    private final long executionTime;
    private final LocalDateTime timestamp;

    private static final DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;

    public BlacklistEntry(String query, String identifier, long executionTime, LocalDateTime timestamp) {
        this.query = query;
        this.identifier = identifier;
        this.executionTime = executionTime;
        this.timestamp = timestamp;
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

    @Override
    public String toString() {
        return query + "@" + identifier + "@" + executionTime + "@" + timestamp.format(formatter);
    }

    public static BlacklistEntry fromString(String str) {
        String defaultQuery = "";
        String defaultIdentifier = "";
        long defaultExecutionTime = 0L;
        LocalDateTime defaultTimestamp = LocalDateTime.MIN;

        if (str == null || str.isEmpty()) {
            return new BlacklistEntry(defaultQuery, defaultIdentifier, defaultExecutionTime, defaultTimestamp);
        }

        String[] parts = str.split("@");
        if (parts.length != 4) {
            return new BlacklistEntry(defaultQuery, defaultIdentifier, defaultExecutionTime, defaultTimestamp);
        }

        try {
            String query = parts[0];
            String identifier = parts[1];
            long executionTime = Long.parseLong(parts[2]);
            LocalDateTime timestamp = LocalDateTime.parse(parts[3], formatter);
            return new BlacklistEntry(query, identifier, executionTime, timestamp);
        } catch (Exception e) {
            return new BlacklistEntry(defaultQuery, defaultIdentifier, defaultExecutionTime, defaultTimestamp);
        }
    }
}

