package org.sagebionetworks.schema.generator.handler.schema03;

import static org.junit.Assert.*;

import java.io.StringWriter;
import java.util.List;

import org.junit.Before;
import org.junit.Test;
import org.sagebionetworks.schema.ObjectSchema;
import org.sagebionetworks.schema.ObjectSchemaImpl;
import org.sagebionetworks.schema.TYPE;

import com.sun.codemodel.JBlock;
import com.sun.codemodel.JClassAlreadyExistsException;
import com.sun.codemodel.JCodeModel;
import com.sun.codemodel.JDeclaration;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFormatter;
import com.sun.codemodel.JMethod;
import com.sun.codemodel.JMod;
import com.sun.codemodel.JPackage;
import com.sun.codemodel.JType;

public class HashAndEqualsHandlerImpl03Test {

	
	JCodeModel codeModel;
	JPackage _package;
	JDefinedClass sampleClass;
	JDefinedClass sampleInterface;
	JType type;
	ObjectSchema schema;
	ObjectSchema schemaInterface;

	@Before
	public void before() throws JClassAlreadyExistsException,
			ClassNotFoundException {
		codeModel = new JCodeModel();
		_package = codeModel._package("org.sample");
		sampleClass = codeModel._class("Sample");
		sampleInterface = _package._interface("SampleInterface");
		schema = new ObjectSchemaImpl();
		schema.setType(TYPE.OBJECT);
		// Create a schema interface
		schemaInterface = new ObjectSchemaImpl();
		schemaInterface.setType(TYPE.INTERFACE);
		schemaInterface.putProperty("fromInterface", new ObjectSchemaImpl(TYPE.BOOLEAN));
	}
	
	@Test
	public void testHashCodeString(){
		// Add a string property to the object
		sampleClass.field(JMod.PRIVATE, codeModel.ref(String.class), "propertyName");
		ObjectSchema propertySchema = new ObjectSchemaImpl();
		propertySchema.setType(TYPE.STRING);
		schema.putProperty("propertyName", propertySchema);
		// Create the handler and add it
		HashAndEqualsHandlerImpl03 handler = new HashAndEqualsHandlerImpl03();
		JMethod method = handler.addHashCode(schema, sampleClass);
		assertNotNull(method);
		// It should be public
		assertEquals(JMod.PUBLIC, method.mods().getValue());
		assertEquals("hashCode", method.name());
		assertEquals(codeModel.INT, method.type());
		assertNotNull(method.annotations());
		assertEquals(1, method.annotations().size());
		assertEquals(codeModel.ref(Override.class), method.annotations().iterator().next().getAnnotationClass());
		
		String methodString = declareToString(method);
//		System.out.println(methodString);
		assertTrue(methodString.indexOf("result = ((prime*result)+((propertyName == null)? 0 :propertyName.hashCode()));") > 0);
	}
	
	@Test
	public void testHashCodeSuper() throws JClassAlreadyExistsException {
		// For this case we want to use class that has the sample as a base class
		ObjectSchema childSchema = new ObjectSchemaImpl();
		childSchema.setExtends(schema);
		JDefinedClass childClasss = codeModel._class("ChildOfSample");
		childClasss._extends(sampleClass);
		// Now handle the
		HashAndEqualsHandlerImpl03 handler = new HashAndEqualsHandlerImpl03();
		JMethod method = handler.addHashCode(childSchema, childClasss);
		assertNotNull(method);
		JBlock body = method.body();
		assertNotNull(body);
		// Now get the string and check it.
		String methodString = declareToString(method);
//		System.out.println(methodString);
		// Make sure there is a call to super.
		assertTrue(methodString.indexOf("int result = super.hashCode();") > 0);
//		printClassToConsole(childClasss);
	}
	
	@Test
	public void testHashImplements() throws JClassAlreadyExistsException {
		// For this case we want to use class that has the sample as a base class
		ObjectSchema childSchema = new ObjectSchemaImpl();
		childSchema.setImplements(new ObjectSchema[]{schemaInterface});
		JDefinedClass childClasss = codeModel._class("ImplementsInterface");
		childClasss._implements(sampleInterface);
		childClasss.field(JMod.PRIVATE, codeModel.BOOLEAN, "fromInterface");
		// Now handle the
		HashAndEqualsHandlerImpl03 handler = new HashAndEqualsHandlerImpl03();
		JMethod method = handler.addHashCode(childSchema, childClasss);
		assertNotNull(method);
		JBlock body = method.body();
		assertNotNull(body);
		// Now get the string and check it.
		String methodString = declareToString(method);
//		System.out.println(methodString);
		// Make sure there is a call to super.
		assertTrue(methodString.indexOf("result = ((prime*result)+((fromInterface == null)? 0 :fromInterface.hashCode()));") > 0);
	}
	
	@Test
	public void testHashCodeObject(){
		// Add a string property to the object
		sampleClass.field(JMod.PRIVATE, sampleClass, "propertyName");
		schema.putProperty("propertyName", schema);
		// Create the handler and add it
		HashAndEqualsHandlerImpl03 handler = new HashAndEqualsHandlerImpl03();
		JMethod method = handler.addHashCode(schema, sampleClass);
		assertNotNull(method);		
		String methodString = declareToString(method);
//		System.out.println(methodString);
		assertTrue(methodString.indexOf("result = ((prime*result)+((propertyName == null)? 0 :propertyName.hashCode()));") > 0);
	}
	
	@Test
	public void testHashCodeArray(){
		// Add a string property to the object
		ObjectSchema propertySchema = new ObjectSchemaImpl();
		propertySchema.setType(TYPE.ARRAY);
		String propName = "propertyName";
		ObjectSchema arrayTypeSchema = schema;
		arrayTypeSchema.setType(TYPE.OBJECT);
		propertySchema.setItems(arrayTypeSchema);
		schema.putProperty(propName, propertySchema);
		// Make sure this field exits
		sampleClass.field(JMod.PRIVATE, codeModel.ref(List.class).narrow(sampleClass), propName);
		// Create the handler and add it
		HashAndEqualsHandlerImpl03 handler = new HashAndEqualsHandlerImpl03();
		JMethod method = handler.addHashCode(schema, sampleClass);
		assertNotNull(method);
		String methodString = declareToString(method);
//		System.out.println(methodString);
		assertTrue(methodString.indexOf("result = ((prime*result)+((propertyName == null)? 0 :propertyName.hashCode()));") > 0);
	}
	
	@Test
	public void testHashCodeDouble(){
		// Use two doubles because a temp variable is used and must be recycled 
		// the second
		sampleClass.field(JMod.PRIVATE, codeModel.DOUBLE, "propertyName");
		sampleClass.field(JMod.PRIVATE, codeModel.DOUBLE, "propertyName2");
		ObjectSchema propertySchema = new ObjectSchemaImpl();
		propertySchema.setType(TYPE.NUMBER);
		schema.putProperty("propertyName", propertySchema);
		schema.putProperty("propertyName2", propertySchema);
		// Create the handler and add it
		HashAndEqualsHandlerImpl03 handler = new HashAndEqualsHandlerImpl03();
		JMethod method = handler.addHashCode(schema, sampleClass);
		assertNotNull(method);		
		String methodString = declareToString(method);
//		System.out.println(methodString);
		assertTrue(methodString.indexOf("result = ((prime*result)+((propertyName == null)? 0 :propertyName.hashCode()));") > 0);
		assertTrue(methodString.indexOf("result = ((prime*result)+((propertyName2 == null)? 0 :propertyName2 .hashCode()));") > 0);
	}
	
	@Test
	public void testHashCodeLong(){
		// Setup a long
		sampleClass.field(JMod.PRIVATE, codeModel.LONG, "propertyName");
		ObjectSchema propertySchema = new ObjectSchemaImpl();
		propertySchema.setType(TYPE.INTEGER);
		schema.putProperty("propertyName", propertySchema);
		// Create the handler and add it
		HashAndEqualsHandlerImpl03 handler = new HashAndEqualsHandlerImpl03();
		JMethod method = handler.addHashCode(schema, sampleClass);
		assertNotNull(method);		
		String methodString = declareToString(method);
//		System.out.println(methodString);
		assertTrue(methodString.indexOf("result = ((prime*result)+((propertyName == null)? 0 :propertyName.hashCode()));") > 0);
	}
	
	@Test
	public void testHashCodeBoolean(){
		// Setup a long
		sampleClass.field(JMod.PRIVATE, codeModel.BOOLEAN, "propertyName");
		ObjectSchema propertySchema = new ObjectSchemaImpl();
		propertySchema.setType(TYPE.BOOLEAN);
		schema.putProperty("propertyName", propertySchema);
		// Create the handler and add it
		HashAndEqualsHandlerImpl03 handler = new HashAndEqualsHandlerImpl03();
		JMethod method = handler.addHashCode(schema, sampleClass);
		assertNotNull(method);		
		String methodString = declareToString(method);
//		System.out.println(methodString);
		assertTrue(methodString.indexOf("result = ((prime*result)+((propertyName == null)? 0 :propertyName.hashCode()));") > 0);
	}
	
	@Test
	public void testEqualsString(){
		// Add a string property to the object
		sampleClass.field(JMod.PRIVATE, codeModel.ref(String.class), "propertyName");
		ObjectSchema propertySchema = new ObjectSchemaImpl();
		propertySchema.setType(TYPE.STRING);
		schema.putProperty("propertyName", propertySchema);
		// Create the handler and add it
		HashAndEqualsHandlerImpl03 handler = new HashAndEqualsHandlerImpl03();
		JMethod method = handler.addEquals(schema, sampleClass);
		assertNotNull(method);
		// It should be public
		assertEquals(JMod.PUBLIC, method.mods().getValue());
		assertEquals("equals", method.name());
		assertEquals(codeModel.BOOLEAN, method.type());
		assertNotNull(method.annotations());
		assertEquals(1, method.annotations().size());
		assertEquals(codeModel.ref(Override.class), method.annotations().iterator().next().getAnnotationClass());
		assertNotNull(method.params());
		assertEquals(1, method.params().size());
		assertEquals(codeModel.ref(Object.class), method.params().iterator().next().type());
		String methodString = declareToString(method);
//		System.out.println(methodString);
		assertTrue(methodString.indexOf("if (this == obj) {") > 0);
		assertTrue(methodString.indexOf("if (obj == null) {") > 0);
		// Super should not be called for this case
		assertFalse(methodString.indexOf("if (!super.equals(obj)) {") > 0);
		assertTrue(methodString.indexOf("if (this.getClass()!= obj.getClass()) {") > 0);
		assertTrue(methodString.indexOf("Sample other = ((Sample) obj);") > 0);
		assertTrue(methodString.indexOf("if (propertyName == null) {") > 0);
		assertTrue(methodString.indexOf("if (other.propertyName!= null) {") > 0);
		assertTrue(methodString.indexOf("if (!propertyName.equals(other.propertyName)) {") > 0);
	}
	
	@Test
	public void testEqualsSuper() throws JClassAlreadyExistsException {
		// For this case we want to use class that has the sample as a base class
		ObjectSchema childSchema = new ObjectSchemaImpl();
		childSchema.setExtends(schema);
		JDefinedClass childClasss = codeModel._class("ChildOfSample");
		childClasss._extends(sampleClass);
		// Now handle the
		HashAndEqualsHandlerImpl03 handler = new HashAndEqualsHandlerImpl03();
		JMethod method = handler.addEquals(childSchema, childClasss);
		assertNotNull(method);
		JBlock body = method.body();
		assertNotNull(body);
		// Now get the string and check it.
		String methodString = declareToString(method);
//		System.out.println(methodString);
		// Make sure there is a call to super.
		assertTrue(methodString.indexOf("if (!super.equals(obj)) {") > 0);
		assertFalse(methodString.indexOf("if (obj == null) {") > 0);
		assertTrue(methodString.indexOf("ChildOfSample other = ((ChildOfSample) obj);") > 0);
//		printClassToConsole(childClasss);
	}
	
	@Test
	public void testEqualsImplements() throws JClassAlreadyExistsException {
		// For this case we want to use class that has the sample as a base class
		ObjectSchema childSchema = new ObjectSchemaImpl();
		childSchema.setImplements(new ObjectSchema[]{schemaInterface});
		JDefinedClass childClasss = codeModel._class("ImplementsInterface");
		childClasss._implements(sampleInterface);
		childClasss.field(JMod.PRIVATE, codeModel.BOOLEAN, "fromInterface");
		// Now handle the
		HashAndEqualsHandlerImpl03 handler = new HashAndEqualsHandlerImpl03();
		JMethod method = handler.addEquals(childSchema, childClasss);
		assertNotNull(method);
		JBlock body = method.body();
		assertNotNull(body);
		// Now get the string and check it.
		String methodString = declareToString(method);
//		System.out.println(methodString);
		// Make sure there is a call to super.
		assertTrue(methodString.indexOf("if (!fromInterface.equals(other.fromInterface))") > 0);
	}
	
	@Test
	public void testEqualsObject(){
		// Add a string property to the object
		sampleClass.field(JMod.PRIVATE, sampleClass, "propertyName");
		schema.putProperty("propertyName", schema);
		// Create the handler and add it
		HashAndEqualsHandlerImpl03 handler = new HashAndEqualsHandlerImpl03();
		JMethod method = handler.addEquals(schema, sampleClass);
		assertNotNull(method);		
		String methodString = declareToString(method);
//		System.out.println(methodString);
		assertTrue(methodString.indexOf("if (propertyName == null) {") > 0);
		assertTrue(methodString.indexOf("if (other.propertyName!= null) {") > 0);
		assertTrue(methodString.indexOf("if (!propertyName.equals(other.propertyName)) {") > 0);
	}
	
	@Test
	public void testEqualsArray(){
		// Add a string property to the object
		ObjectSchema propertySchema = new ObjectSchemaImpl();
		propertySchema.setType(TYPE.ARRAY);
		String propName = "propertyName";
		ObjectSchema arrayTypeSchema = schema;
		arrayTypeSchema.setType(TYPE.OBJECT);
		propertySchema.setItems(arrayTypeSchema);
		schema.putProperty(propName, propertySchema);
		// Make sure this field exits
		sampleClass.field(JMod.PRIVATE, codeModel.ref(List.class).narrow(sampleClass), propName);
		// Create the handler and add it
		HashAndEqualsHandlerImpl03 handler = new HashAndEqualsHandlerImpl03();
		JMethod method = handler.addEquals(schema, sampleClass);
		assertNotNull(method);
		String methodString = declareToString(method);
//		System.out.println(methodString);
		assertTrue(methodString.indexOf("if (propertyName == null) {") > 0);
		assertTrue(methodString.indexOf("if (other.propertyName!= null) {") > 0);
		assertTrue(methodString.indexOf("if (!propertyName.equals(other.propertyName)) {") > 0);
	}
	
	@Test
	public void testEqualsDouble(){
		// Test a double
		sampleClass.field(JMod.PRIVATE, codeModel.DOUBLE, "propertyName");
		ObjectSchema propertySchema = new ObjectSchemaImpl();
		propertySchema.setType(TYPE.NUMBER);
		schema.putProperty("propertyName", propertySchema);
		// Create the handler and add it
		HashAndEqualsHandlerImpl03 handler = new HashAndEqualsHandlerImpl03();
		JMethod method = handler.addEquals(schema, sampleClass);
		assertNotNull(method);		
		String methodString = declareToString(method);
//		System.out.println(methodString);
		assertTrue(methodString.indexOf("if (!propertyName.equals(other.propertyName))") > 0);
	}
	
	@Test
	public void testEqualsLong(){
		// Setup a long
		sampleClass.field(JMod.PRIVATE, codeModel.LONG, "propertyName");
		ObjectSchema propertySchema = new ObjectSchemaImpl();
		propertySchema.setType(TYPE.INTEGER);
		schema.putProperty("propertyName", propertySchema);
		// Create the handler and add it
		HashAndEqualsHandlerImpl03 handler = new HashAndEqualsHandlerImpl03();
		JMethod method = handler.addEquals(schema, sampleClass);
		assertNotNull(method);		
		String methodString = declareToString(method);
//		System.out.println(methodString);
		assertTrue(methodString.indexOf("if (!propertyName.equals(other.propertyName))") > 0);
	}
	
	@Test
	public void testEqualsBoolean(){
		// Setup a long
		sampleClass.field(JMod.PRIVATE, codeModel.BOOLEAN, "propertyName");
		ObjectSchema propertySchema = new ObjectSchemaImpl();
		propertySchema.setType(TYPE.BOOLEAN);
		schema.putProperty("propertyName", propertySchema);
		// Create the handler and add it
		HashAndEqualsHandlerImpl03 handler = new HashAndEqualsHandlerImpl03();
		JMethod method = handler.addEquals(schema, sampleClass);
		assertNotNull(method);		
		String methodString = declareToString(method);
//		System.out.println(methodString);
		assertTrue(methodString.indexOf("if (!propertyName.equals(other.propertyName)) {") > 0);
	}
	
	/**
	 * Helper to declare a model object to string.
	 * @param toDeclare
	 * @return
	 */
	public String declareToString(JDeclaration toDeclare){
		StringWriter writer = new StringWriter();
		JFormatter formatter = new JFormatter(writer);
		toDeclare.declare(formatter);
		return writer.toString();
	}

	
}
