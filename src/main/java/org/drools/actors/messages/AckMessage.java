package org.drools.actors.messages;

public class AckMessage {

    public final long messageId;

    public AckMessage(long messageId) {
        this.messageId = messageId;
    }
}
