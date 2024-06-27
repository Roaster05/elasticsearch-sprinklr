/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0 and the Server Side Public License, v 1; you may not use this file except
 * in compliance with, at your election, the Elastic License 2.0 or the Server
 * Side Public License, v 1.
 */

package org.elasticsearch.rest.action.admin.cluster;

import org.elasticsearch.Blacklist;
import org.elasticsearch.BlacklistData;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.cluster.blacklist.BlacklistUpdateResponse;
import org.elasticsearch.client.node.NodeClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.BytesRestResponse;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.xcontent.XContentBuilder;
import org.elasticsearch.xcontent.XContentFactory;

import java.io.IOException;
import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableList;
import static org.elasticsearch.rest.RestRequest.Method.POST;

public class RestUnblacklistIdentifierAction extends BaseRestHandler {

    @Override
    public List<Route> routes() {
        return unmodifiableList(
            asList(
                new Route(POST, "/unblacklist"),
                new Route(POST, "/unblacklist/{identifier}")
            )
        );
    }

    @Override
    public String getName() {
        return "unblacklist_action";
    }

    @Override
    public RestChannelConsumer prepareRequest(final RestRequest request, final NodeClient client) throws IOException {
        String[] identifiers = Strings.splitStringByCommaToArray(request.param("identifier"));
        List<String> successfulUnblacklist = BlacklistData.getInstance().deleteEntriesByIdentifiers(identifiers);

        return channel -> {
            Blacklist blacklistUpdate = BlacklistData.getInstance().getBlacklist();
            client.updateBlacklist(blacklistUpdate, new ActionListener<BlacklistUpdateResponse>() {
                @Override
                public void onResponse(BlacklistUpdateResponse blacklistUpdateResponse) {
                    try {
                        // Build a response using XContentBuilder
                        XContentBuilder builder = XContentFactory.jsonBuilder();
                        builder.startObject();
                        builder.field("acknowledged", true);
                        if (successfulUnblacklist.isEmpty()) {
                            builder.field("message", "No identifiers were unblacklisted because they were not found in the blacklist.");
                        } else {
                            builder.field("message", "Identifiers unblacklisted successfully.");
                            builder.field("unblacklisted_identifiers", successfulUnblacklist);
                        }
                        builder.endObject();

                        channel.sendResponse(new BytesRestResponse(RestStatus.OK, builder));
                    } catch (IOException e) {
                        onFailure(e);
                    }
                }

                @Override
                public void onFailure(Exception e) {
                    try {
                        // Build a failure response using XContentBuilder
                        XContentBuilder builder = XContentFactory.jsonBuilder();
                        builder.startObject();
                        builder.field("acknowledged", false);
                        builder.field("error", e.getMessage());
                        builder.endObject();

                        channel.sendResponse(new BytesRestResponse(RestStatus.INTERNAL_SERVER_ERROR, builder));
                    } catch (IOException ioException) {
                        channel.sendResponse(new BytesRestResponse(RestStatus.INTERNAL_SERVER_ERROR, "Failed to update blacklist"));
                    }
                }
            });
        };
    }
}
