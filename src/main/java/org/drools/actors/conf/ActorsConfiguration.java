package org.drools.actors.conf;

import org.drools.actors.reteoo.builder.ActorsNodeFactory;
import org.drools.core.RuleBaseConfiguration;
import org.drools.core.reteoo.KieComponentFactory;

public class ActorsConfiguration extends RuleBaseConfiguration {
    @Override
    public KieComponentFactory getComponentFactory() {
        KieComponentFactory componentFactory = super.getComponentFactory();
        componentFactory.setNodeFactoryProvider(new ActorsNodeFactory());
        return componentFactory;
    }
}
