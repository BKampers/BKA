package run;

import java.util.*;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;
import uml.structure.*;


public class ObjectScopeTest {

    @Test
    public void loadAndStoreLocalVariable() throws MemoryException {
        ObjectScope scope = createScope("count", integerType(), 0);
        scope.store("count", 7);
        assertEquals(7, scope.load("count"));
    }

    @Test
    public void loadDelegatesToParent() throws MemoryException {
        ObjectScope parent = createScope("result", integerType(), 10);
        ObjectScope child = createScope(parent, "local", integerType(), StateMachine.UNINITIALIZED);
        assertEquals(10, child.load("result"));
    }

    @Test
    public void storeDelegatesToParent() throws MemoryException {
        ObjectScope parent = createScope("result", integerType(), 10);
        ObjectScope child = createScope(parent, "local", integerType(), StateMachine.UNINITIALIZED);
        child.store("result", 20);
        assertEquals(20, parent.load("result"));
        assertEquals(20, child.load("result"));
    }

    @Test
    public void loadUnknownIdentifierThrows() {
        ObjectScope scope = createScope("count", integerType(), 0);
        assertThrows(MemoryException.class, () -> scope.load("missing"));
    }

    @Test
    public void storeUnknownIdentifierThrows() {
        ObjectScope scope = createScope("count", integerType(), 0);
        assertThrows(MemoryException.class, () -> scope.store("missing", 1));
    }

    private static ObjectScope createScope(String name, Type type, java.lang.Object initialValue) {
        return createScope(null, name, type, initialValue);
    }

    private static ObjectScope createScope(Memory parent, String name, Type type, java.lang.Object initialValue) {
        UmlClassBuilder builder = new UmlClassBuilder("scope");
        Attribute attribute = builder.withAttribute(name, type, Member.Visibility.PRIVATE);
        uml.structure.Class scopeType = builder.build();
        Map<Attribute, Expression> values = Map.of(attribute, new ValueExpression(initialValue, type));
        return new ObjectScope(parent, new MutableObject(null, scopeType, values));
    }

    private static Type integerType() {
        return UmlTypeFactory.create("INTEGER");
    }

}
