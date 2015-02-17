package org.drools.actors.reteoo;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.japi.Creator;
import org.drools.actors.messages.AckMessage;
import org.drools.actors.messages.PropagationMessage;
import org.drools.core.reteoo.AlphaNode;
import org.drools.core.reteoo.ObjectSink;
import org.drools.core.reteoo.ObjectSource;
import org.drools.core.reteoo.builder.BuildContext;
import org.drools.core.spi.AlphaNodeFieldConstraint;

import static org.drools.actors.util.ActorFactory.actorOf;

public class ActorBasedAlphaNode extends AlphaNode implements ActorBasedNode {

    private final ActorRef alphaActor;

    public ActorBasedAlphaNode(int id,
                               AlphaNodeFieldConstraint constraint,
                               ObjectSource objectSource,
                               BuildContext context) {
        super(id, constraint, objectSource, context);
        this.alphaActor = actorOf(props(this));
    }

    @Override
    public ActorRef getActor() {
        return alphaActor;
    }

    public static class AlphaNodeActor extends UntypedActor {

        private final PropagationTracker propagationTracker = new PropagationTracker();

        private final AlphaNode alphaNode;

        public AlphaNodeActor(AlphaNode alphaNode) {
            this.alphaNode = alphaNode;
        }

        @Override
        public void onReceive(Object message) throws Exception {
            if (message instanceof PropagationMessage) {
                PropagationMessage msg = (PropagationMessage) message;

                // TODO: should there be an actor for each workingMemory
                // holding the reference to its own AlphaMemory (?)
                AlphaMemory memory = (AlphaMemory) msg.workingMemory.getNodeMemory(alphaNode);

                if (alphaNode.getConstraint().isAllowed(msg.handle,
                                                        msg.workingMemory,
                                                        memory.context)) {

                    ObjectSink[] sinks = alphaNode.getSinkPropagator().getSinks();

                    if (sinks.length == 0) {
                        getSender().tell(new AckMessage(msg.messageId), self());
                    } else {
                        propagationTracker.startPropagation(getSender(), msg.messageId, sinks.length);
                    }

                    for (ObjectSink sink : sinks) {
                        ActorRef otnActor = ((ActorBasedNode) sink).getActor();
                        otnActor.tell(message, self());
                    }
                } else {
                    getSender().tell(new AckMessage(msg.messageId), self());
                }
            } else if (message instanceof AckMessage) {
                propagationTracker.ackPropagation(getSelf(), (AckMessage) message);
            }
        }
    }

    public static Props props(final AlphaNode alphaNode) {
        return Props.create(new Creator<AlphaNodeActor>() {
            @Override
            public AlphaNodeActor create() throws Exception {
                return new AlphaNodeActor(alphaNode);
            }
        });
    }
}
