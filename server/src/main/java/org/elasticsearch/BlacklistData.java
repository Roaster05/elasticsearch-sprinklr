package org.elasticsearch;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("checkstyle:MissingJavadocType")
public class BlacklistData {
    private static BlacklistData instance;
    private Blacklist blacklist;
    public long threshold1 = 3000;
    public long threshold2 = 1000;
    public boolean allowed = false;
    public String nodename = "";
    public boolean lock = false;
    public boolean reset = false;

    private ScheduledExecutorService scheduler;

    public String getnode() {
        return nodename;
    }

    public void setnode(String nodename) {
        this.nodename = nodename;
    }

    private BlacklistData() {
        blacklist = new Blacklist();
        scheduler = Executors.newScheduledThreadPool(1);
        startCleanupTask();
    }

    public static synchronized BlacklistData getInstance() {
        if (instance == null) {
            instance = new BlacklistData();
        }
        return instance;
    }

    private void startCleanupTask() {
        scheduler.scheduleAtFixedRate(() -> {
            synchronized (this) {
                blacklist.removeExpiredEntries();
            }
        }, 0, 1, TimeUnit.MINUTES); // Cleanup every minute
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

    public boolean getLock() {
        return lock;
    }

    public void setLock(boolean val) {
        this.lock = val;
    }

    public boolean getReset() {
        return lock;
    }

    public void setReset(boolean val) {
        this.lock = val;
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
    /*
    The queries here are added to the blacklist, had to remove kibana and migration based queries
    as they might get added while setting up these.
     */
    public void addToBlacklist(String query, String identifier, long tookInMillis) {
        if (query != null && !query.contains("kibana") && !query.contains("migration")) {
            lock = true;
            blacklist.addEntry(new BlacklistEntry(query, identifier, tookInMillis, LocalDateTime.now()));
        }
    }

    @SuppressWarnings({"checkstyle:MissingJavadocMethod", "checkstyle:DescendantToken"})
    /*
    These method on the basis of the query and identifier received from the request identifies
    the potential threat of a search request based on the previously identified bad scoring,
    Implemented checks on both threats of the query and the identifier
    Here the return value is accessed to determine the reason of blacklisting to be displayed.
     */
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
        if (identifierScore >= 50) {
            return 2;
        }
        long queryCount = blacklist.getEntries().stream()
            .filter(entry -> Objects.equals(entry.getQuery(), query))
            .count();

        if (queryCount >= 15) {
            return 1;
        } else {
            return 0;
        }
    }

    public void resetStorage() {
        blacklist.clear();
    }

    public String convertBlacklistToString() {
        // Before converting to string, ensure only valid (non-expired) entries are considered
        blacklist.removeExpiredEntries();
        return blacklist.toString();
    }

    /**
     *
     * @param newEntriesStr are the new entries which are being received from the newly applied cluster state and then this state is being
     *                      applied to the node based cache which is our blacklist List, also the concurrency is being maintained which
     *                      avoids any obvious reason of state corruption.
     * @return
     */
    public String mergeAndConvertBlacklist(String newEntriesStr) {
        Blacklist newEntries = Blacklist.fromString(newEntriesStr);
        // Remove expired entries from the new blacklist
        newEntries.removeExpiredEntries();
        // Remove expired entries from the existing blacklist before merging
        blacklist.removeExpiredEntries();
        setBlacklist(getBlacklist(newEntries)); // Merge new entries with existing ones
        return convertBlacklistToString();
    }
}
