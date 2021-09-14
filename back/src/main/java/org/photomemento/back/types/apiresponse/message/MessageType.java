package org.photomemento.back.types.apiresponse.message;

/**
 * The message type.
 */
public enum MessageType {
    CRITICAL("CRITICAL"),
    FATAL("FATAL"),
    ERROR("ERROR"),
    WARNING("WARNING");

    private final String text;

    MessageType(String text) {
        this.text = text;
    }

    public String text() {
        return this.text;
    }
}
