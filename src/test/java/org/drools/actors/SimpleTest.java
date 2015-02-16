package org.drools.actors;

import org.drools.actors.conf.ActorsConfiguration;
import org.junit.Test;
import org.kie.api.KieBase;
import org.kie.api.KieServices;
import org.kie.api.builder.KieFileSystem;
import org.kie.api.runtime.KieContainer;
import org.kie.api.runtime.KieSession;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class SimpleTest {

    @Test
    public void testAlpha() {
        String str =
                "import " + Person.class.getCanonicalName() + ";\n" +
                "global java.util.List list\n" +
                "rule R when\n" +
                "  $p : Person(name == \"Mark\")\n" +
                "then\n" +
                "  list.add($p.getAge());\n" +
                "end";

        KieSession ksession = getKieSession(str);
        List<Integer> list = new ArrayList<>();
        ksession.setGlobal("list", list);

        ksession.insert(new Person("Mark", 37));
        ksession.insert(new Person("Edson", 35));
        ksession.insert(new Person("Mario", 40));

        try {
            Thread.sleep(100L);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        ksession.fireAllRules();

        assertEquals(1, list.size());
        assertEquals(37, (int)list.get(0));
    }

    private KieSession getKieSession(String str) {
        KieServices ks = KieServices.Factory.get();
        KieFileSystem kfs = ks.newKieFileSystem().write( "src/main/resources/r1.drl", str );
        ks.newKieBuilder( kfs ).buildAll();
        KieContainer kieContainer = ks.newKieContainer(ks.getRepository().getDefaultReleaseId());
        KieBase kieBase = kieContainer.newKieBase(new ActorsConfiguration());
        return kieBase.newKieSession();
    }
}
