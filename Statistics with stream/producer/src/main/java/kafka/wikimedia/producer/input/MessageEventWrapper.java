package kafka.wikimedia.producer.input;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.IOException;

// The main wrapper class
@Data
@NoArgsConstructor
public class MessageEventWrapper {
    private MessageEvent messageEvent;
    private String event;

    @Data
    @NoArgsConstructor
    public static class MessageEvent {
        @JsonProperty("$schema")
        private String schema;
        private Meta meta;
        private int id;
        private String type;
        private int namespace;
        private String title;

        @JsonProperty("title_url")
        private String titleUrl;
        private String comment;
        private long timestamp;
        private String user;
        private boolean bot;

        @JsonProperty("notify_url")
        private String notifyUrl;

        @JsonProperty("server_url")
        private String serverUrl;

        @JsonProperty("server_name")
        private String serverName;

        @JsonProperty("server_script_path")
        private String serverScriptPath;
        private String wiki;
        private String parsedcomment;

        private Length length;
        private Revision revision;
        private boolean minor;
        private boolean patrolled;

        @Data
        @NoArgsConstructor
        public static class Meta {
            private String uri;

            @JsonProperty("request_id")
            private String requestId;
            private String id;
            private String dt;
            private String domain;
            private String stream;
            private String topic;
            private int partition;
            private long offset;
        }

        @Data
        @NoArgsConstructor
        public static class Length {
            private int old;

            @JsonProperty("new")
            private int newLength; // `new` is a keyword in Java
        }

        @Data
        @NoArgsConstructor
        public static class Revision {
            private long old;

            @JsonProperty("new")
            private long newRevision; // `new` is a keyword in Java
        }
    }

    // Method to parse JSON using Jackson
    public static MessageEventWrapper fromJson(String json) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        return objectMapper.readValue(json, MessageEventWrapper.class);
    }
}