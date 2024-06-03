

package org.elasticsearch;

import org.elasticsearch.common.io.stream.StreamInput;

import java.io.IOException;

public class ElasticsearchBlacklistException extends ElasticsearchException {
    public ElasticsearchBlacklistException(StreamInput in) throws IOException {
        super(in);
    }

    public ElasticsearchBlacklistException(Throwable cause) {
        super(cause);
    }

    public ElasticsearchBlacklistException(String message, Object... args) {
        super(message, args);
    }

    public ElasticsearchBlacklistException(String message, Throwable cause, Object... args) {
        super(message, cause, args);
    }
}
