package org.elasticsearch;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("checkstyle:MissingJavadocType")
public class BlacklistData {
    private static BlacklistData instance;
    private List<BlacklistEntry> blacklist;
    private final ScheduledExecutorService scheduler;
    public long threshold1 = 3000;
    public long threshold2 = 1000;
    public boolean allowed = false;

    private BlacklistData() {
        blacklist = new ArrayList<>();
        scheduler = Executors.newScheduledThreadPool(1);
        startDecayTask();
    }

    public static synchronized BlacklistData getInstance() {
        if (instance == null) {
            instance = new BlacklistData();
        }
        return instance;
    }

    public List<BlacklistEntry> getBlacklist() {
        return blacklist;
    }

    public long getThreshold1() {
        return threshold1;
    }

    public long getThreshold2() {
        return threshold2;
    }

    public boolean getAllowed() { return allowed; }

    public void setThreshold1(long threshold1) {
        this.threshold1 = threshold1;
    }

    public void setThreshold2(long threshold2) {
        this.threshold2 = threshold2;
    }

    public void setAllowed(boolean allowed) { this.allowed = allowed; }

    public void addToBlacklist(String query, String identifier, long tookInMillis) {
        blacklist.add(new BlacklistEntry(query, identifier, tookInMillis, LocalDateTime.now()));
    }

    @SuppressWarnings({"checkstyle:MissingJavadocMethod", "checkstyle:DescendantToken"})
    public int shouldAllowRequest(String query, String identifier) {

        if(!allowed)
            return 0;

        double identifierScore = 0.0;

        for (BlacklistEntry entry : blacklist) {
            if (Objects.equals(entry.getIdentifier(), identifier)) {
                if (entry.getExecutionTime() > threshold1) {
                    identifierScore += 1.5;
                } else if (entry.getExecutionTime() > threshold2) {
                    identifierScore += 1.0;
                }
            }
        }


        if(identifierScore>=20)
            return 2;

        long queryCount = blacklist.stream()
            .filter(entry -> Objects.equals(entry.getQuery(), query) && entry.getExecutionTime() > threshold1)
            .count();

        if (queryCount >= 5) {
            return 1;
        }
        else
            return 0;
    }

    public void resetStorage() {
        blacklist.clear();
    }

    private void startDecayTask() {
        scheduler.scheduleAtFixedRate(this::removeExpiredEntries, 0, 1, TimeUnit.HOURS);
    }

    private void removeExpiredEntries() {
        LocalDateTime now = LocalDateTime.now();
        blacklist.removeIf(entry -> Duration.between(entry.getTimestamp(), now).toHours() >= 12);
    }

    @SuppressWarnings("checkstyle:MissingJavadocType")
    public static class BlacklistEntry {
        private final String query;
        private final String identifier;
        private final long executionTime;
        private final LocalDateTime timestamp;

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
    }
}
