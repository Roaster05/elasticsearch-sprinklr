package org.elasticsearch.common.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


/**
 * Tracks memory usage for requests involving BigArray allocations. Provides methods
 * to initialize new entries, update memory usage, and manage entries, including
 * removing the entry with the highest memory usage.
 */
public class BigArrayTracker {

    /**
     * Represents a unique key for identifying requests in the tracker.
     */
    private static class RequestKey {
        private final String identifier;
        private final String query;

        @SuppressWarnings("checkstyle:RedundantModifier")
        public RequestKey(String identifier, String query) {
            this.identifier = identifier;
            this.query = query;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            RequestKey that = (RequestKey) o;
            return identifier.equals(that.identifier) && query.equals(that.query);
        }

        @Override
        public int hashCode() {
            return Objects.hash(identifier, query);
        }

        @Override
        public String toString() {
            return "RequestKey{" +
                "identifier='" + identifier + '\'' +
                ", query='" + query + '\'' +
                '}';
        }
    }

    /**
     * Represents the value associated with a request in the tracker, including
     * the identifier, query, and memory usage.
     */
    public static class RequestValue {
        private final String identifier;
        private final String query;
        private long usedMemory;

        public RequestValue(String identifier, String query, long usedMemory) {
            this.identifier = identifier;
            this.query = query;
            this.usedMemory = usedMemory;
        }

        public String getIdentifier() {
            return identifier;
        }

        public String getQuery() {
            return query;
        }

        public long getUsedMemory() {
            return usedMemory;
        }

        public void addUsedMemory(long additionalMemory) {
            this.usedMemory += additionalMemory;
        }

        @Override
        public String toString() {
            return "RequestValue{" +
                "identifier='" + identifier + '\'' +
                ", query='" + query + '\'' +
                ", usedMemory=" + usedMemory +
                '}';
        }
    }

    private final Map<RequestKey, RequestValue> trackerMap = new HashMap<>();

    /**
     * Adds or updates an entry in the tracker with the specified identifier, query, and
     * additional memory usage. If the memory usage is zero or negative, the entry is removed.
     *
     * @param identifier the identifier associated with the request
     * @param query the query associated with the request
     * @param usedMemoryToAdd additional memory used by the request
     */
    public synchronized void addOrUpdateEntry(String identifier, String query, long usedMemoryToAdd) {
        RequestKey key = new RequestKey(identifier, query);
        RequestValue existingValue = trackerMap.get(key);
        if (existingValue != null) {
            existingValue.addUsedMemory(usedMemoryToAdd);
            if (existingValue.getUsedMemory() <=0 ) {
                trackerMap.remove(key);

            }
        } else {
            if(usedMemoryToAdd>0) {
                RequestValue newValue = new RequestValue(identifier, query, usedMemoryToAdd);
                trackerMap.put(key, newValue);
            }
        }
    }

    public synchronized RequestValue removeEntry(String identifier, String query) {
        RequestKey key = new RequestKey(identifier, query);
        return trackerMap.remove(key);
    }

    public synchronized RequestValue getEntry(String identifier, String query) {
        RequestKey key = new RequestKey(identifier, query);
        return trackerMap.get(key);
    }

    /**
     * Removes the entry with the highest memory usage from the tracker and returns it.
     * If multiple entries have the same highest memory usage, the first encountered
     * entry is removed and returned.
     *
     * @return the entry with the highest memory usage, or {@code null} if the tracker is empty
     */
    public synchronized RequestValue removeEntryWithHighestMemory() {
        if (trackerMap.isEmpty()) {
            return null;
        }

        Map.Entry<RequestKey, RequestValue> maxEntry = null;
        for (Map.Entry<RequestKey, RequestValue> entry : trackerMap.entrySet()) {
            if (maxEntry == null || entry.getValue().getUsedMemory() > maxEntry.getValue().getUsedMemory()) {
                maxEntry = entry;
            }
        }

        if (maxEntry != null) {
            return trackerMap.remove(maxEntry.getKey());
        }
        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("BigArrayTracker{\n");
        for (Map.Entry<RequestKey, RequestValue> entry : trackerMap.entrySet()) {
            sb.append("\tKey: ").append(entry.getKey()).append(", Value: ").append(entry.getValue()).append("\n");
        }
        sb.append("}");
        return sb.toString();
    }
}
