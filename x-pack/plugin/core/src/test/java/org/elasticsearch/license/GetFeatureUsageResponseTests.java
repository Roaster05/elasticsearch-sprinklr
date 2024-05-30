/*
 * Copyright Elasticsearch B.V. and/or licensed to Elasticsearch B.V. under one
 * or more contributor license agreements. Licensed under the Elastic License
 * 2.0; you may not use this file except in compliance with the Elastic License
 * 2.0.
 */

package org.elasticsearch.license;

import org.elasticsearch.Version;
import org.elasticsearch.common.io.stream.BytesStreamOutput;
import org.elasticsearch.common.io.stream.StreamInput;
import org.elasticsearch.license.GetFeatureUsageResponse.FeatureUsageInfo;
import org.elasticsearch.test.ESTestCase;
import org.elasticsearch.test.VersionUtils;

import java.io.IOException;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

public class GetFeatureUsageResponseTests extends ESTestCase {

    public void assertStreamInputOutput(Version version, String family, String context) throws IOException {
        ZonedDateTime zdt = ZonedDateTime.now();
        FeatureUsageInfo fui = new FeatureUsageInfo(family, "feature", zdt, context, "gold");
        GetFeatureUsageResponse originalResponse = new GetFeatureUsageResponse(org.elasticsearch.core.List.of(fui));
        BytesStreamOutput output = new BytesStreamOutput();
        output.setVersion(version);
        originalResponse.writeTo(output);

        StreamInput input = output.bytes().streamInput();
        input.setVersion(version);
        GetFeatureUsageResponse finalResponse = new GetFeatureUsageResponse(input);
        assertThat(finalResponse.getFeatures(), hasSize(1));
        FeatureUsageInfo fui2 = finalResponse.getFeatures().get(0);
        assertThat(fui2.getFamily(), equalTo(family));
        assertThat(fui2.getName(), equalTo("feature"));
        // time is truncated to nearest second
        assertThat(fui2.getLastUsedTime(), equalTo(zdt.withZoneSameInstant(ZoneOffset.UTC).withNano(0)));
        assertThat(fui2.getContext(), equalTo(context));
        assertThat(fui2.getLicenseLevel(), equalTo("gold"));
    }

    public void testPre715StreamFormat() throws IOException {
        assertStreamInputOutput(VersionUtils.getPreviousVersion(Version.V_7_15_0), null, null);
    }

    public void testStreamFormat() throws IOException {
        assertStreamInputOutput(Version.CURRENT, "family", "context");
        // family and context are optional
        assertStreamInputOutput(Version.CURRENT, null, null);
    }
}
