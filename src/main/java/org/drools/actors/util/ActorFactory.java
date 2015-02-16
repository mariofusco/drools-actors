package org.drools.actors.util;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

public class ActorFactory {
    private static final ActorSystem ACTOR_SYSTEM = ActorSystem.create("MySystem");

    public static ActorRef actorOf(Class<?> actorClass) {
        return actorOf(Props.create(actorClass));
    }

    public static ActorRef actorOf(Props props) {
        return ACTOR_SYSTEM.actorOf(props);
    }
}
