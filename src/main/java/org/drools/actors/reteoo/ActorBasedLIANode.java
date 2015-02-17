package org.drools.actors.reteoo;

import akka.actor.ActorRef;
import akka.actor.Props;
import akka.actor.UntypedActor;
import akka.japi.Creator;
import org.drools.actors.messages.AckMessage;
import org.drools.actors.messages.PropagationMessage;
import org.drools.core.phreak.SegmentUtilities;
import org.drools.core.reteoo.LeftInputAdapterNode;
import org.drools.core.reteoo.ObjectSource;
import org.drools.core.reteoo.builder.BuildContext;

import static org.drools.actors.util.ActorFactory.actorOf;

public class ActorBasedLIANode extends LeftInputAdapterNode implements ActorBasedNode {

    private final ActorRef alphaActor;

    public ActorBasedLIANode(int id,
                             ObjectSource source,
                             BuildContext context)  {
        super(id, source, context);
        this.alphaActor = actorOf(props(this));
    }

    @Override
    public ActorRef getActor() {
        return alphaActor;
    }

    public static class LiaNodeActor extends UntypedActor {

        private final LeftInputAdapterNode liaNode;

        public LiaNodeActor(LeftInputAdapterNode liaNode) {
            this.liaNode = liaNode;
        }

        @Override
        public void onReceive(Object message) throws Exception {
            PropagationMessage msg = (PropagationMessage) message;

            switch (msg.type) {
                case INSERT:
                    doAssert(msg);
                    break;
                default:
                    throw new UnsupportedOperationException();
            }
        }

        private void doAssert(PropagationMessage msg) {
            LiaNodeMemory lm = ( LiaNodeMemory ) msg.workingMemory.getNodeMemory( liaNode );
            if ( lm.getSegmentMemory() == null ) {
                SegmentUtilities.createSegmentMemory(liaNode, msg.workingMemory);
            }

            doInsertObject( msg.handle, msg.context, liaNode, msg.workingMemory,
                            lm, true, true );

            getSender().tell(new AckMessage(msg.messageId), self());
        }
    }

    public static Props props(final LeftInputAdapterNode liaNode) {
        return Props.create(new Creator<LiaNodeActor>() {
            @Override
            public LiaNodeActor create() throws Exception {
                return new LiaNodeActor(liaNode);
            }
        });
    }
}