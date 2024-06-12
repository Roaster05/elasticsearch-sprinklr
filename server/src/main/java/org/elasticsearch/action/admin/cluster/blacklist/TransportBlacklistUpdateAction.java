

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
import org.elasticsearch.common.settings.Setting;
import org.elasticsearch.common.settings.Setting.Property;

public class TransportBlacklistUpdateAction extends TransportMasterNodeAction<BlacklistUpdateRequest, BlacklistUpdateResponse> {

    public Boolean reset = false;
    public static final Setting<Boolean>BLACKLIST_RESET = Setting.boolSetting(
        "search.blacklist_reset",
        false,
        Property.NodeScope,
        Property.Dynamic
    );

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
        clusterService.getClusterSettings().addSettingsUpdateConsumer(BLACKLIST_RESET, this::setBlacklistReset);
    }

    private void setBlacklistReset(boolean blacklistReset)
    {
        if(blacklistReset)
        {
            reset = true;
            BlacklistData.getInstance().resetStorage();
            BlacklistData.getInstance().setReset(true);
        }

    }

    @Override
    protected ClusterBlockException checkBlock(BlacklistUpdateRequest request, ClusterState state) {
        // to check whether to bypass the BlacklistUpdateAction based on some criteria but set to null for now
        return null;
    }

    /**
     *
     * @param request
     * @param state
     * @param listener
     * Here the new Cluster state is being build which will be executed for publishing to all the other nodes of the cluster
     * The new clusterState is formed to just modify the clusterBlacklist parameter only
     */
    @Override
    protected void masterOperation(
        BlacklistUpdateRequest request,
        ClusterState state,
        ActionListener<BlacklistUpdateResponse> listener) {

        ClusterState.Builder newStateBuilder = ClusterState.builder(state);

        BlacklistData.getInstance().mergeAndConvertBlacklist(request.getBlacklistItem());

        newStateBuilder.clusterblacklist(BlacklistData.getInstance().convertBlacklistToString());

        ClusterState newClusterState = newStateBuilder.build();

        clusterService.submitStateUpdateTask(
            "blacklist-update",
            new AckedClusterStateUpdateTask(Priority.URGENT, request, listener.map(response -> new BlacklistUpdateResponse()) ){

                @Override
                public ClusterState execute(ClusterState currentState) throws Exception {
                    if(reset)
                    {
                        reset = false;
                        BlacklistData.getInstance().resetStorage();
                    }
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
