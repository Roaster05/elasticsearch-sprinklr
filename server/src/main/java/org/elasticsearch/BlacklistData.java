package org.elasticsearch;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("checkstyle:MissingJavadocType")
public class BlacklistData {
    private static BlacklistData instance;
    private Blacklist blacklist;
    private final ScheduledExecutorService scheduler;
    public long threshold1 = 3000;
    public long threshold2 = 1000;
    public boolean allowed = false;

    private BlacklistData() {
        blacklist = new Blacklist();
        scheduler = Executors.newScheduledThreadPool(1);
        startDecayTask();
    }

    public static synchronized BlacklistData getInstance() {
        if (instance == null) {
            instance = new BlacklistData();
        }
        return instance;
    }

    @SuppressWarnings("checkstyle:DescendantToken")
    public synchronized Blacklist getBlacklist(Blacklist newEntries) {
        if (newEntries == null) {
            return blacklist;
        }

        for (BlacklistEntry newEntry : newEntries.getEntries()) {
            boolean exists = blacklist.getEntries().stream().anyMatch(entry ->
                Objects.equals(entry.getQuery(), newEntry.getQuery()) &&
                    Objects.equals(entry.getIdentifier(), newEntry.getIdentifier()) &&
                    Objects.equals(entry.getExecutionTime(), newEntry.getExecutionTime()) &&
                    Objects.equals(entry.getTimestamp(), newEntry.getTimestamp())
            );
            if (!exists) {
                blacklist.addEntry(newEntry);
            }
        }

        return blacklist;
    }

    public void setBlacklist(Blacklist newEntries) {
        if (newEntries != null) {
            this.blacklist = newEntries;
        }
    }

    public Blacklist getBlacklist() {
        return blacklist;
    }

    public long getThreshold1() {
        return threshold1;
    }

    public long getThreshold2() {
        return threshold2;
    }

    public boolean getAllowed() {
        return allowed;
    }

    public void setThreshold1(long threshold1) {
        this.threshold1 = threshold1;
    }

    public void setThreshold2(long threshold2) {
        this.threshold2 = threshold2;
    }

    public void setAllowed(boolean allowed) {
        this.allowed = allowed;
    }

    @SuppressWarnings("checkstyle:DescendantToken")
    public void addToBlacklist(String query, String identifier, long tookInMillis) {
        if (query != null && !query.contains("kibana")) {
            blacklist.addEntry(new BlacklistEntry(query, identifier, tookInMillis, LocalDateTime.now()));
        }
    }

    @SuppressWarnings({"checkstyle:MissingJavadocMethod", "checkstyle:DescendantToken"})
    public int shouldAllowRequest(String query, String identifier) {
        if (!allowed) {
            return 0;
        }

        double identifierScore = 0.0;

        for (BlacklistEntry entry : blacklist.getEntries()) {
            if (Objects.equals(entry.getIdentifier(), identifier)) {
                if (entry.getExecutionTime() > threshold1) {
                    identifierScore += 1.5;
                } else if (entry.getExecutionTime() > threshold2) {
                    identifierScore += 1.0;
                }
            }
        }

        if (identifierScore >= 20) {
            return 2;
        }

        long queryCount = blacklist.getEntries().stream()
            .filter(entry -> Objects.equals(entry.getQuery(), query) && entry.getExecutionTime() > threshold1)
            .count();

        if (queryCount >= 5) {
            return 1;
        } else {
            return 0;
        }
    }

    public void resetStorage() {
        blacklist.clear();
    }

    private void startDecayTask() {
        scheduler.scheduleAtFixedRate(this::removeExpiredEntries, 0, 1, TimeUnit.HOURS);
    }

    private void removeExpiredEntries() {
        LocalDateTime now = LocalDateTime.now();
        blacklist.getEntries().removeIf(entry -> Duration.between(entry.getTimestamp(), now).toHours() >= 12);
    }

    public String convertBlacklistToString() {
        return blacklist.toString();
    }

    public String mergeAndConvertBlacklist(String newEntriesStr) {
        Blacklist newEntries = Blacklist.fromString(newEntriesStr);
        setBlacklist(getBlacklist(newEntries)); // Merge new entries with existing ones
        return convertBlacklistToString();
    }
}
