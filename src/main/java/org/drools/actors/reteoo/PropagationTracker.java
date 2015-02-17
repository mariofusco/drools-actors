package org.drools.actors.reteoo;

import akka.actor.ActorRef;
import org.drools.actors.messages.AckMessage;

import java.util.HashMap;
import java.util.Map;

public class PropagationTracker {
    private final Map<Long, PropagationInfo> propagationMap = new HashMap<>();

    public void startPropagation(ActorRef sender, long id, int sinkNr) {
        propagationMap.put(id, new PropagationInfo(sender, sinkNr));
    }

    public void ackPropagation(ActorRef self, AckMessage ackMessage) {
        PropagationInfo propInfo = propagationMap.get(ackMessage.messageId);
        propInfo.counter--;
        if (propInfo.counter == 0) {
            propagationMap.remove(ackMessage.messageId);
            propInfo.sender.tell(ackMessage, self);
        }
    }

    private static class PropagationInfo {
        private final ActorRef sender;
        private int counter;

        private PropagationInfo(ActorRef sender, int counter) {
            this.sender = sender;
            this.counter = counter;
        }
    }
}
