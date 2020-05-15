package org.sagebionetworks.schema;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;
import java.util.Date;

import org.junit.jupiter.api.Test;
import org.sagebionetworks.ABImpl;
import org.sagebionetworks.ABImpl2;
import org.sagebionetworks.ABImpl2newversion;
import org.sagebionetworks.InterfaceA;
import org.sagebionetworks.schema.adapter.JSONObjectAdapterException;
import org.sagebionetworks.schema.adapter.org.json.EntityFactory;
import org.sagebionetworks.schema.adapter.org.json.JSONArrayAdapterImpl;
import org.sagebionetworks.schema.adapter.org.json.JSONObjectAdapterImpl;

public class ExtraFieldsTest {
	
	@Test
	public void testRoundTripToNewVersion() throws JSONObjectAdapterException {
		ABImpl2 impl = new ABImpl2();
		impl.setFromMe2("from me2 value");
		impl.setAlsoFromInterfaceA(123.456);
		impl.setFromInterfaceA("from A value");
		impl.setAlsoFromInterfaceB(new Date(0));
		impl.setFromInterfaceB("from B value");
		
		// Now make the round trip
		String jsonString = EntityFactory.createJSONStringForEntity(impl);
		assertNotNull(jsonString);
		jsonString = jsonString.replace("ABImpl2", "ABImpl2newversion");
		// Clone it
		ABImpl2newversion clone = EntityFactory.createEntityFromJSONString(jsonString, ABImpl2newversion.class);
		assertNotNull(clone);
		assertEquals(impl.getFromMe2(), clone.getFromMe2());
		assertEquals(impl.getAlsoFromInterfaceA(), clone.getAlsoFromInterfaceA());
		assertEquals(impl.getAlsoFromInterfaceB(), clone.getAlsoFromInterfaceB());
		assertNull(clone.getNewField());
		assertNull(clone.getExtraFieldFromInterfaceB());

		jsonString = EntityFactory.createJSONStringForEntity(clone);
		jsonString = jsonString.replace("ABImpl2newversion", "ABImpl2");
		ABImpl2 originalClone = EntityFactory.createEntityFromJSONString(jsonString, ABImpl2.class);
		assertEquals(impl, originalClone);
	}

	@Test
	public void testToOldVersion() throws JSONObjectAdapterException {
		ABImpl2newversion impl = new ABImpl2newversion();
		impl.setFromMe2("from me2 value");
		impl.setAlsoFromInterfaceA(123.456);
		impl.setFromInterfaceA("from A value");
		impl.setAlsoFromInterfaceB(new Date(0));
		impl.setFromInterfaceB("from B value");
		impl.setNewField("new value");
		impl.setExtraFieldFromInterfaceB("extra");
		impl.setNewInterfaceList(Collections.<InterfaceA> singletonList(new ABImpl()));
		impl.setNewRef(new ABImpl());
		impl.setNewList(Collections.singletonList("one"));
		impl.setNewMap(Collections.singletonMap("key", 10L));

		// Now make the round trip
		String jsonString = EntityFactory.createJSONStringForEntity(impl);
		assertNotNull(jsonString);
		jsonString = jsonString.replace("ABImpl2newversion", "ABImpl2");
		// Clone it
		ABImpl2 clone = EntityFactory.createEntityFromJSONString(jsonString, ABImpl2.class);
		assertNotNull(clone);
		assertEquals(impl.getFromMe2(), clone.getFromMe2());
		assertEquals(impl.getAlsoFromInterfaceA(), clone.getAlsoFromInterfaceA());
		assertEquals(impl.getAlsoFromInterfaceB(), clone.getAlsoFromInterfaceB());
	}
	
	@Test
	public void testExtraFieldArray() throws JSONObjectAdapterException {
		JSONObjectAdapterImpl adapter = new JSONObjectAdapterImpl();
		adapter.put("fromMe2", "fromMe2");
		JSONArrayAdapterImpl array = new JSONArrayAdapterImpl();
		array.put(0, "value one");
		adapter.put("someExtra", array);
		
		ABImpl2 impl = new ABImpl2(adapter);
		assertEquals("fromMe2", impl.getFromMe2());
		
		JSONObjectAdapterImpl clone = new JSONObjectAdapterImpl();
		impl.writeToJSONObject(clone);
		assertEquals("fromMe2", clone.get("fromMe2"));
		assertThrows(JSONObjectAdapterException.class, () ->
			clone.get("someExtra")
		);
	}
}
