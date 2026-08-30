package run;

import java.util.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import uml.factory.*;
import uml.structure.*;


public class ObjectScopeTest {

    @Test
    public void storeAndFindLocalVariable() {
        ObjectScope scope = createScope("count", integerType(), 0);
        scope.store("count", new ValueExpression(7, integerType()));
        assertEquals(7, value(scope, "count"));
    }

    @Test
    public void findDoesNotSeeParent() {
        ObjectScope parent = createScope("result", integerType(), 10);
        ObjectScope child = createScope(parent, "local", integerType(), ObjectScope.UNINITIALIZED);
        assertTrue(child.find("result").isEmpty());
        assertEquals(10, value(parent, "result"));
    }

    @Test
    public void storeDelegatesToParent() {
        Type type = integerType();
        ObjectScope parent = createScope("result", type, 10);
        ObjectScope child = createScope(parent, "local", type, ObjectScope.UNINITIALIZED);
        child.store("result", new ValueExpression(20, type));
        assertEquals(20, value(parent, "result"));
        assertTrue(child.find("result").isEmpty());
    }

    @Test
    public void findUnknownIdentifierIsEmpty() {
        ObjectScope scope = createScope("count", integerType(), 0);
        assertTrue(scope.find("missing").isEmpty());
    }

    @Test
    public void storeUnknownIdentifierThrows() {
        ObjectScope scope = createScope("count", integerType(), 0);
        assertThrows(IllegalStateException.class, () -> scope.store("missing", new ValueExpression(1, integerType())));
    }

    private static java.lang.Object value(ObjectScope scope, String name) {
        Expression expression = scope.find(name).orElseThrow();
        return ((ValueExpression) expression).getValue();
    }

    private static ObjectScope createScope(String name, Type type, java.lang.Object initialValue) {
        return createScope(null, name, type, initialValue);
    }

    private static ObjectScope createScope(ObjectScope parent, String name, Type type, java.lang.Object initialValue) {
        UmlClassBuilder builder = new UmlClassBuilder("scope");
        Attribute attribute = builder.withAttribute(name, type, Member.Visibility.PRIVATE);
        uml.structure.Class scopeType = builder.build();
        Map<Attribute, Expression> values = Map.of(attribute, new ValueExpression(initialValue, type));
        return new ObjectScope(parent, MutableObject.constructAnonymous(scopeType, values));
    }

    private static Type integerType() {
        return UmlTypeFactory.create("INTEGER");
    }

}
