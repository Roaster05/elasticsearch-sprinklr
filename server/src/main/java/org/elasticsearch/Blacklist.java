package org.elasticsearch;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;

/**
 * The `Blacklist` class serves as a storage mechanism for blacklisted queries, represented as `BlacklistEntry` objects.
 * It manages both local and cluster-level blacklist storage.
 */
public class Blacklist {
    private List<BlacklistEntry> entries;

    public Blacklist() {
        this.entries = new ArrayList<>();
    }

    public void addEntry(BlacklistEntry entry) {
        this.entries.add(entry);
    }

    public List<BlacklistEntry> getEntries() {
        return entries;
    }

    public void clear() {
        entries.clear();
    }

    public int size() {
        return entries.size();
    }

    /**
     * Removes entries from the list if their status has changed to expired.
     * Since entries are ordered by their timestamp, we can break the loop
     * once we encounter a non-expired entry, ensuring that subsequent queries
     * will persist in the storage.
     */
    public void removeExpiredEntries() {
        Iterator<BlacklistEntry> iterator = entries.iterator();
        while (iterator.hasNext()) {
            BlacklistEntry entry = iterator.next();
            if (entry.isExpired()) {
                iterator.remove();
            } else {
                break;
            }
        }
    }

    public static Blacklist readFrom(StreamInput in) throws IOException {
        Blacklist blacklist = new Blacklist();
        int size = in.readVInt();
        for (int i = 0; i < size; i++) {
            BlacklistEntry entry = new BlacklistEntry(in);
            blacklist.addEntry(entry);
        }
        return blacklist;
    }

    public void writeTo(StreamOutput out) throws IOException {
        out.writeVInt(entries.size());
        for (BlacklistEntry entry : entries) {
            entry.writeTo(out);
        }
    }
}
