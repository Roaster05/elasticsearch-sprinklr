package org.elasticsearch.action.admin.cluster.blacklist;

import org.elasticsearch.action.ActionType;
import org.elasticsearch.common.logging.HeaderWarning;

public class BlacklistUpdateAction extends ActionType<BlacklistUpdateResponse> {

    public static final BlacklistUpdateAction INSTANCE = new BlacklistUpdateAction();
    public static final String NAME = "cluster:admin/blacklist/update";

    private BlacklistUpdateAction() {

        super(NAME, BlacklistUpdateResponse::new);
        HeaderWarning.addWarning("update action");
    }
}
