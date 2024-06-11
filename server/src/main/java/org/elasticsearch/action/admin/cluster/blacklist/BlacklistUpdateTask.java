/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0 and the Server Side Public License, v 1; you may not use this file except
 * in compliance with, at your election, the Elastic License 2.0 or the Server
 * Side Public License, v 1.
 */

package org.elasticsearch.action.admin.cluster.blacklist;

import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.ClusterStateTaskExecutor;
import org.elasticsearch.common.Priority;
import org.elasticsearch.common.logging.HeaderWarning;
import org.elasticsearch.common.logging.Loggers;

import java.util.List;
import java.util.function.Consumer;

public class BlacklistUpdateTask implements ClusterStateTaskExecutor<BlacklistUpdateRequest> {

    private final ClusterState newState;
    private final Consumer<BlacklistUpdateResponse> responseConsumer;

    public BlacklistUpdateTask(ClusterState newState, Consumer<BlacklistUpdateResponse> responseConsumer) {
        this.newState = newState;
        this.responseConsumer = responseConsumer;
    }

    @SuppressWarnings("checkstyle:LineLength")
    @Override
    public ClusterTasksResult<BlacklistUpdateRequest> execute(ClusterState currentState, List<BlacklistUpdateRequest> tasks) throws Exception {
        // Merge the new cluster state into the current state
        ClusterState newClusterState = newState;
        return ClusterTasksResult.<BlacklistUpdateRequest>builder()
            .successes(tasks)
            .build(newClusterState);
    }

    public void onFailure(String source, Exception e) {
        responseConsumer.accept(new BlacklistUpdateResponse(false));
    }


    public void clusterStateProcessed(String source, ClusterState oldState, ClusterState newState) {
        responseConsumer.accept(new BlacklistUpdateResponse(true));
    }

    public Priority priority() {
        return Priority.NORMAL;
    }

    @Override
    public boolean runOnlyOnMaster() {
        return true;
    }
}
