package run.pascal;

import java.util.*;
import run.*;
import uml.structure.*;


/**
 * Access to a field of a record value.
 */
public final class MemberAccessExpression implements Evaluable {

    public MemberAccessExpression(Evaluable receiver, String member) {
        this.receiver = Objects.requireNonNull(receiver);
        this.member = Objects.requireNonNull(member);
    }

    public Evaluable getReceiver() {
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
    public java.lang.Object evaluate(Execution execution, ObjectScope scope) {
        MutableObject record = execution.mutableObject(receiver, scope);
        return execution.evaluate(Execution.asEvaluable(record.get(findRecordAttribute(record, member))), scope);
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

    private final Evaluable receiver;
    private final String member;

}
