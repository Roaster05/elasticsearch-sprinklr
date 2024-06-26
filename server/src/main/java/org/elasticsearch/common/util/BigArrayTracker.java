package org.elasticsearch.common.util;

import org.elasticsearch.common.logging.HeaderWarning;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class BigArrayTracker {

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
            return identifier.equals(that.identifier) &&
                query.equals(that.query);
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

    public static class RequestValue {
        private final String identifier;
        private final String query;
        private long usedMemory;

        @SuppressWarnings("checkstyle:RedundantModifier")
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
                "valuePart1='" + identifier + '\'' +
                ", valuePart2='" + query + '\'' +
                ", usedMemory=" + usedMemory +
                '}';
        }
    }

    private final Map<RequestKey, RequestValue> trackerMap = new HashMap<>();

    public void addOrUpdateEntry(String identifier, String query, long usedMemoryToAdd) {
        HeaderWarning.addWarning(String.valueOf(usedMemoryToAdd));
        RequestKey key = new RequestKey(identifier, query);
        RequestValue existingValue = trackerMap.get(key);

        if (existingValue != null) {
            existingValue.addUsedMemory(usedMemoryToAdd);
            if (existingValue.getUsedMemory() == 0) {
                trackerMap.remove(key);
            }
        } else {
            if (usedMemoryToAdd != 0) {
                RequestValue newValue = new RequestValue(identifier, query, usedMemoryToAdd);
                trackerMap.put(key, newValue);
            }
        }
    }

    public RequestValue removeEntry(String identifier, String query) {
        RequestKey key = new RequestKey(identifier, query);
        return trackerMap.remove(key);
    }

    public RequestValue getEntry(String identifier, String query) {
        RequestKey key = new RequestKey(identifier, query);
        return trackerMap.get(key);
    }

    public RequestValue removeEntryWithHighestMemory() {
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
