package run.pascal;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import run.Engine;
import run.MutableObject;
import uml.structure.Attribute;
import uml.structure.Type;


/**
 * Access to a field of a record value.
 */
public final class MemberAccessExpression extends AbstractPascalExpression {

    public MemberAccessExpression(AbstractPascalExpression receiver, String member) {
        this.receiver = Objects.requireNonNull(receiver);
        this.member = Objects.requireNonNull(member);
    }

    public AbstractPascalExpression getReceiver() {
        return receiver;
    }

    public String getMember() {
        return member;
    }

    @Override
    public Optional<Type> getType() {
        uml.structure.Class targetClass = (uml.structure.Class) receiver.getType().get();
        return targetClass.getAttributes()
            .stream().filter(attribute -> attribute.getName().isPresent() && member.equalsIgnoreCase(attribute.getName().get()))
            .findAny().get().getType();
    }

    @Override
    public java.lang.Object evaluate(Engine engine) {
        MutableObject record = engine.mutableObject(receiver);
        return engine.evaluate((AbstractPascalExpression) record.get(findRecordAttribute(record, member)));
    }

    public static Attribute findRecordAttribute(MutableObject record, String name) {
        return record.getAttributes().stream()
            .filter(attribute -> attribute.getName().isPresent() && name.equalsIgnoreCase(attribute.getName().get()))
            .findAny()
            .orElseThrow(() -> new NoSuchElementException("No such field: " + name));
    }

    @Override
    public String toString() {
        return receiver + "." + member;
    }

    private final AbstractPascalExpression receiver;
    private final String member;

}
