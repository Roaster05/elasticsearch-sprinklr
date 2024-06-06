package org.elasticsearch;

import java.util.ArrayList;
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
            .collect(Collectors.joining(";"));
    }

    @SuppressWarnings("checkstyle:DescendantToken")
    public static Blacklist fromString(String str) {
        Blacklist blacklist = new Blacklist();
        if (str != null && !str.isEmpty()) {
            String[] entryStrings = str.split(";");
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
}
