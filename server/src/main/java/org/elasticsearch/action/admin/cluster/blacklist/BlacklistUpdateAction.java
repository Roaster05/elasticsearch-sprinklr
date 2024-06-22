package org.elasticsearch.action.admin.cluster.blacklist;

import org.elasticsearch.action.ActionType;

public class BlacklistUpdateAction extends ActionType<BlacklistUpdateResponse> {

    public static final BlacklistUpdateAction INSTANCE = new BlacklistUpdateAction();
    public static final String NAME = "cluster:admin/blacklist/update";

    private BlacklistUpdateAction() {

        super(NAME, BlacklistUpdateResponse::new);
    }
}
