package org.honchar.hibernate.util;



import org.hibernate.Session;
import org.hibernate.query.Query;
import org.honchar.util.model.Thing;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class SessionBuilderTest {
    @Test
    public void testSessionFactory() {
        try (Session session = SessionUtil.getSession()) {
            assertNotNull(session);
        }
    }

    //end::preamble[]
    @Test
    public void testDoWithSession() {
        SessionUtil.doWithSession(session -> {
            session.createQuery("delete from Thing").executeUpdate();

            Thing t = new Thing();
            t.setName("thingName");
            session.persist(t);
        });
        Thing thing = SessionUtil.returnFromSession(session -> {
            Query<Thing> query = session.createQuery(
                    "from Thing t where t.name=:name", Thing.class
            );
            query.setParameter("name", "thingName");
            return query.getSingleResult();
        });
        assertNotNull(thing);
        System.out.println(thing);
        assertEquals(thing.getName(), "thingName");
    }
}