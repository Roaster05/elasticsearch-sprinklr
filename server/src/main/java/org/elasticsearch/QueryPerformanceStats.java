package org.elasticsearch;

import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.common.io.stream.StreamOutput;
import org.elasticsearch.threadpool.ThreadPoolStats;

import java.io.IOException;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class QueryPerformanceStats {
    private final Map<String, Integer> activeThreadsDiff;
    private final Map<String, Integer> queueSizeDiff;
    private final Map<String, Long> completedTasksDiff;
    private final Map<String, Long> rejectedTasksDiff;

    private long heapMemoryUsedDiff;
    private long heapMemoryCommittedDiff;
    private long heapMemoryMaxDiff;
    private long nonHeapMemoryUsedDiff;
    private long nonHeapMemoryCommittedDiff;
    private long nonHeapMemoryMaxDiff;
    private long gcCountDiff;
    private long gcTimeDiff;

    private String nodeId;

    public QueryPerformanceStats() {
        this.activeThreadsDiff = new HashMap<>();
        this.queueSizeDiff = new HashMap<>();
        this.completedTasksDiff = new HashMap<>();
        this.rejectedTasksDiff = new HashMap<>();
    }

    public void captureInitialStats(ThreadPoolStats threadPoolStats, MemoryMXBean memoryMXBean, List<GarbageCollectorMXBean> gcBeans,
                                    String nodeId) {
        this.nodeId = nodeId;
        captureThreadPoolStats(threadPoolStats, true);
        captureMemoryStats(memoryMXBean, true);
        captureGcStats(gcBeans, true);
    }

    public void captureAndCalculateDiffs(ThreadPoolStats threadPoolStats, MemoryMXBean memoryMXBean, List<GarbageCollectorMXBean> gcBeans) {
        captureThreadPoolStats(threadPoolStats, false);
        captureMemoryStats(memoryMXBean, false);
        captureGcStats(gcBeans, false);
    }

    public QueryPerformanceStats(StreamInput in) throws IOException {
        activeThreadsDiff = in.readMap(StreamInput::readString, StreamInput::readInt);
        queueSizeDiff = in.readMap(StreamInput::readString, StreamInput::readInt);
        completedTasksDiff = in.readMap(StreamInput::readString, StreamInput::readLong);
        rejectedTasksDiff = in.readMap(StreamInput::readString, StreamInput::readLong);

        heapMemoryUsedDiff = in.readLong();
        heapMemoryCommittedDiff = in.readLong();
        heapMemoryMaxDiff = in.readLong();
        nonHeapMemoryUsedDiff = in.readLong();
        nonHeapMemoryCommittedDiff = in.readLong();
        nonHeapMemoryMaxDiff = in.readLong();
        gcCountDiff = in.readLong();
        gcTimeDiff = in.readLong();

        nodeId = in.readString();
    }

    // Method to write to StreamOutput
    public void writeTo(StreamOutput out) throws IOException {
        out.writeMap(activeThreadsDiff, StreamOutput::writeString, StreamOutput::writeInt);
        out.writeMap(queueSizeDiff, StreamOutput::writeString, StreamOutput::writeInt);
        out.writeMap(completedTasksDiff, StreamOutput::writeString, StreamOutput::writeLong);
        out.writeMap(rejectedTasksDiff, StreamOutput::writeString, StreamOutput::writeLong);

        out.writeLong(heapMemoryUsedDiff);
        out.writeLong(heapMemoryCommittedDiff);
        out.writeLong(heapMemoryMaxDiff);
        out.writeLong(nonHeapMemoryUsedDiff);
        out.writeLong(nonHeapMemoryCommittedDiff);
        out.writeLong(nonHeapMemoryMaxDiff);
        out.writeLong(gcCountDiff);
        out.writeLong(gcTimeDiff);

        out.writeString(nodeId);
    }

    private void captureThreadPoolStats(ThreadPoolStats threadPoolStats, boolean before) {
        for (ThreadPoolStats.Stats stats : threadPoolStats) {
            String name = stats.getName();
            if ("search".equals(name)) { // Only capture stats for the thread pool named "search"
                if (before) {
                    activeThreadsDiff.put(name, -stats.getActive());
                    queueSizeDiff.put(name, -stats.getQueue());
                    completedTasksDiff.put(name, -stats.getCompleted());
                    rejectedTasksDiff.put(name, -stats.getRejected());
                } else {
                    activeThreadsDiff.put(name, activeThreadsDiff.getOrDefault(name, 0) + stats.getActive());
                    queueSizeDiff.put(name, queueSizeDiff.getOrDefault(name, 0) + stats.getQueue());
                    completedTasksDiff.put(name, completedTasksDiff.getOrDefault(name, 0L) + stats.getCompleted());
                    rejectedTasksDiff.put(name, rejectedTasksDiff.getOrDefault(name, 0L) + stats.getRejected());
                }
                // Assuming there is only one thread pool named "search"
                break; // Exit the loop after capturing stats for "search"
            }
        }
    }

    private void captureMemoryStats(MemoryMXBean memoryMXBean, boolean before) {
        MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
        MemoryUsage nonHeapMemoryUsage = memoryMXBean.getNonHeapMemoryUsage();

        if (before) {
            this.heapMemoryUsedDiff = -heapMemoryUsage.getUsed();
            this.heapMemoryCommittedDiff = -heapMemoryUsage.getCommitted();
            this.heapMemoryMaxDiff = -heapMemoryUsage.getMax();

            this.nonHeapMemoryUsedDiff = -nonHeapMemoryUsage.getUsed();
            this.nonHeapMemoryCommittedDiff = -nonHeapMemoryUsage.getCommitted();
            this.nonHeapMemoryMaxDiff = -nonHeapMemoryUsage.getMax();
        } else {
            this.heapMemoryUsedDiff += heapMemoryUsage.getUsed();
            this.heapMemoryCommittedDiff += heapMemoryUsage.getCommitted();
            this.heapMemoryMaxDiff += heapMemoryUsage.getMax();

            this.nonHeapMemoryUsedDiff += nonHeapMemoryUsage.getUsed();
            this.nonHeapMemoryCommittedDiff += nonHeapMemoryUsage.getCommitted();
            this.nonHeapMemoryMaxDiff += nonHeapMemoryUsage.getMax();
        }
    }

    private void captureGcStats(List<GarbageCollectorMXBean> gcBeans, boolean before) {
        for (GarbageCollectorMXBean gcBean : gcBeans) {
            String name = gcBean.getName();
            if (before) {
                this.gcCountDiff += -gcBean.getCollectionCount();
                this.gcTimeDiff += -gcBean.getCollectionTime();
            } else {
                this.gcCountDiff += gcBean.getCollectionCount();
                this.gcTimeDiff += gcBean.getCollectionTime();
            }
        }
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    public Map<String, Integer> getActiveThreadsDiff() {
        return activeThreadsDiff;
    }

    public Map<String, Integer> getQueueSizeDiff() {
        return queueSizeDiff;
    }

    public Map<String, Long> getCompletedTasksDiff() {
        return completedTasksDiff;
    }

    public Map<String, Long> getRejectedTasksDiff() {
        return rejectedTasksDiff;
    }

    public long getHeapMemoryUsedDiff() {
        return heapMemoryUsedDiff;
    }

    public long getHeapMemoryCommittedDiff() {
        return heapMemoryCommittedDiff;
    }

    public long getHeapMemoryMaxDiff() {
        return heapMemoryMaxDiff;
    }

    public long getNonHeapMemoryUsedDiff() {
        return nonHeapMemoryUsedDiff;
    }

    public long getNonHeapMemoryCommittedDiff() {
        return nonHeapMemoryCommittedDiff;
    }

    public long getNonHeapMemoryMaxDiff() {
        return nonHeapMemoryMaxDiff;
    }

    public long getGcCountDiff() {
        return gcCountDiff;
    }

    public long getGcTimeDiff() {
        return gcTimeDiff;
    }

    public String getNodeId() {
        return nodeId;
    }

    @Override
    public String toString() {
        return "QueryPerformanceStats{" +
            "activeThreadsDiff=" + activeThreadsDiff +
            ", queueSizeDiff=" + queueSizeDiff +
            ", completedTasksDiff=" + completedTasksDiff +
            ", rejectedTasksDiff=" + rejectedTasksDiff +
            ", heapMemoryUsedDiff=" + heapMemoryUsedDiff +
            ", heapMemoryCommittedDiff=" + heapMemoryCommittedDiff +
            ", heapMemoryMaxDiff=" + heapMemoryMaxDiff +
            ", nonHeapMemoryUsedDiff=" + nonHeapMemoryUsedDiff +
            ", nonHeapMemoryCommittedDiff=" + nonHeapMemoryCommittedDiff +
            ", nonHeapMemoryMaxDiff=" + nonHeapMemoryMaxDiff +
            ", gcCountDiff=" + gcCountDiff +
            ", gcTimeDiff=" + gcTimeDiff +
            ", nodeId='" + nodeId + '\'' +
            '}';
    }
}
