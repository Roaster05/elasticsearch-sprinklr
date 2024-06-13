package org.elasticsearch;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

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

    @Override
    public String toString() {
        return entries.stream()
            .map(BlacklistEntry::toString)
            .collect(Collectors.joining("#"));
    }

    @SuppressWarnings("checkstyle:DescendantToken")
    public static Blacklist fromString(String str) {
        Blacklist blacklist = new Blacklist();
        if (str != null && !str.isEmpty()) {
            String[] entryStrings = str.split("#");
            for (String entryString : entryStrings) {
                if (!entryString.isEmpty()) {
                    blacklist.addEntry(BlacklistEntry.fromString(entryString));
                }
            }
        }
        return blacklist;
    }

    public void clear() {
        entries.clear();
    }

    public int size() {
        return entries.size();
    }

    /**
     * removes the entries from the list if their status is been changes to expired , we can break as the entries will be
     * in ordering of their timestamp
     */
    public void removeExpiredEntries() {
        Iterator<BlacklistEntry> iterator = entries.iterator();
        while (iterator.hasNext()) {
            BlacklistEntry entry = iterator.next();
            if (entry.isExpired()) {
                iterator.remove();
            }
            else
                break;
        }
    }
}
