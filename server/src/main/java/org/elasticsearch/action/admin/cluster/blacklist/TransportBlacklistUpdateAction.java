package org.elasticsearch.action.admin.cluster.blacklist;

import org.elasticsearch.BlacklistData;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.support.ActionFilters;
import org.elasticsearch.action.support.master.TransportMasterNodeAction;
import org.elasticsearch.cluster.AckedClusterStateUpdateTask;
import org.elasticsearch.cluster.ClusterState;
import org.elasticsearch.cluster.block.ClusterBlockException;
import org.elasticsearch.cluster.metadata.IndexNameExpressionResolver;
import org.elasticsearch.cluster.service.ClusterService;
import org.elasticsearch.common.Priority;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.threadpool.ThreadPool;
import org.elasticsearch.transport.TransportService;

public class TransportBlacklistUpdateAction extends TransportMasterNodeAction<BlacklistUpdateRequest, BlacklistUpdateResponse> {

    @Inject
    public TransportBlacklistUpdateAction(
        TransportService transportService,
        ClusterService clusterService,
        ThreadPool threadPool,
        ActionFilters actionFilters,
        IndexNameExpressionResolver indexNameExpressionResolver) {
        super(
            BlacklistUpdateAction.NAME,
            transportService,
            clusterService,
            threadPool,
            actionFilters,
            BlacklistUpdateRequest::new,
            indexNameExpressionResolver,
            BlacklistUpdateResponse::new,
            ThreadPool.Names.SAME
        );
    }

    @Override
    protected ClusterBlockException checkBlock(BlacklistUpdateRequest request, ClusterState state) {
        return null; // Implement your logic to check cluster block exceptions
    }

    @Override
    protected void masterOperation(
        BlacklistUpdateRequest request,
        ClusterState state,
        ActionListener<BlacklistUpdateResponse> listener) {
        // Create a new cluster state builder
        ClusterState.Builder newStateBuilder = ClusterState.builder(state);

        // Merge and convert the new blacklist data
        BlacklistData.getInstance().mergeAndConvertBlacklist(request.getBlacklistItem());

        // Set the updated blacklist in the new state
        newStateBuilder.clusterblacklist(BlacklistData.getInstance().convertBlacklistToString());

        // Build the new cluster state
        ClusterState newClusterState = newStateBuilder.build();

        // Submit a cluster state update task with the new state
        clusterService.submitStateUpdateTask(
            "create-inde",
            new AckedClusterStateUpdateTask(Priority.URGENT, request, listener.map(response -> new BlacklistUpdateResponse()) ){

                @Override
                public ClusterState execute(ClusterState currentState) throws Exception {
                    return ClusterState.builder(currentState).clusterblacklist(BlacklistData.getInstance().convertBlacklistToString()).build();
                }

                @Override
                public void onFailure(String source, Exception e) {
                    super.onFailure(source, e);
                }
            }
        );
    }
}
