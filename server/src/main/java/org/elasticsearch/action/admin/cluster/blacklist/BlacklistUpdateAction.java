package org.elasticsearch.action.admin.cluster.blacklist;

import org.elasticsearch.action.ActionType;


/**
 * Represents an action type for updating the blacklist in the cluster. Extends {@link ActionType}.
 *
 * <p>This action type is specifically for handling updates to the cluster blacklist.
 *
 * <p>Provides a static instance {@code INSTANCE} and a unique name {@code "cluster:admin/blacklist/update"}.
 */
public class BlacklistUpdateAction extends ActionType<BlacklistUpdateResponse> {

    public static final BlacklistUpdateAction INSTANCE = new BlacklistUpdateAction();
    public static final String NAME = "cluster:admin/blacklist/update";

    private BlacklistUpdateAction() {

        super(NAME, BlacklistUpdateResponse::new);
    }
}
