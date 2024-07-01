package org.elasticsearch;

import org.elasticsearch.common.util.BigArrayTracker;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


/**
 * The Blacklist Data is a NodeScoped storage instance for blacklisted queries.
 * It supports concurrency with the cluster-level blacklist storage by keeping itself
 * updated with the `cluster_state` storage.
 *
 * The class contains a Blacklist object that stores queries as BlacklistEntry objects
 * and a BigArrayTracker object that tracks the BigArray usage of queries executing for that particular node.
 *
 * It supports methods for adding new entries into the blacklist storage and includes a scheduler
 * that performs decaying logic on the blacklist entries by removing those aged beyond a certain time threshold.
 *
 * Additionally, it provides methods to stop a blacklisted query from execution.
 */

public class BlacklistData {
    private static BlacklistData instance;
    private Blacklist blacklist;
    private BigArrayTracker bigArrayTracker;
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
        bigArrayTracker = new BigArrayTracker();
        scheduler = Executors.newScheduledThreadPool(1);
        startCleanupTask();
    }

    public static synchronized BlacklistData getInstance() {
        if (instance == null) {
            instance = new BlacklistData();
        }
        return instance;
    }

    /**
     * Schedules the cleanUp tasks by removing the expired entries
     */
    private void startCleanupTask() {
        scheduler.scheduleAtFixedRate(() -> {
            synchronized (this) {
                blacklist.removeExpiredEntries();
            }
        }, 0, 1, TimeUnit.MINUTES); // Cleanup every minute
    }

    /**
     * Maintains concurrency with the cluster-level blacklist storage, which is managed at the cluster state.
     * Takes the published cluster state from `cluster_state` and applies it to the instance to update
     * the blacklist storage, effectively performing a merge operation.
     */

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
            if (exists) {
            } else {
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

    public BigArrayTracker getBigArrayTracker() { return this.bigArrayTracker; }

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


    /**
     * Adds the specified queries to the blacklist.
     * Kibana and migration-based queries are excluded from blacklisting
     * as they might be added during setup processes.
     */
    @SuppressWarnings("checkstyle:DescendantToken")
    public void addToBlacklist(String query, String identifier, long tookInMillis) {
        if (query != null && !query.contains("kibana") && !query.contains("migration")) {
            lock = true;
            blacklist.addEntry(new BlacklistEntry(query, identifier, tookInMillis, LocalDateTime.now()));
        }
    }


    /**
     * This method identifies the potential threat of a search request based on the query and identifier received from the request.
     * It evaluates the threat level of both the query and the identifier based on previously identified bad scoring.
     * The return value is used to determine the reason for blacklisting to be displayed.
     */

    public int shouldAllowRequest(String query, String identifier) {
        if (allowed) {
            double identifierScore = 0.0;

            for (BlacklistEntry entry : blacklist.getEntries()) {
                if (Objects.equals(entry.getIdentifier(), identifier)) {
                    identifierScore += 1.0;
                }
            }
            if (identifierScore >= 50) {
                return 2;
            }
            long queryCount = blacklist.getEntries().stream()
                .filter(entry -> Objects.equals(entry.getQuery(), query))
                .count();

            if (queryCount >= 5) {
                return 1;
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }

    public void resetStorage() {
        blacklist.clear();
    }


    /**
     * Unblacklists specified identifiers by removing their entries from the blacklist.
     * Filters out the provided identifier IDs and returns a list of successfully updated identifiers
     * to create a response for the API call.
     *
     * @param identifiers a list of identifier IDs to be unblacklisted
     * @return a list of successfully unblacklisted identifier IDs
     */
    public List<String> deleteEntriesByIdentifiers(String[] identifiers) {
        if (identifiers == null || identifiers.length == 0) {
            resetStorage();
            return Collections.emptyList();
        }

        List<String> identifiersToDelete = Arrays.asList(identifiers);
        List<String> successfulUnblacklist = new ArrayList<>();

        Iterator<BlacklistEntry> iterator = blacklist.getEntries().iterator();
        while (iterator.hasNext()) {
            BlacklistEntry entry = iterator.next();
            if (identifiersToDelete.contains(entry.getIdentifier())) {
                iterator.remove();
                successfulUnblacklist.add(entry.getIdentifier());
            }
        }

        return successfulUnblacklist;
    }
}
