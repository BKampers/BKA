/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans, humanity, the environment or the universe.
*/

package run.pascal;

import java.util.*;
import run.*;
import uml.structure.*;


/**
 * Access to a field of a record value.
 */
public final class MemberAccessExpression implements Assignable {

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
        MutableObject target = requireRecord(execution, scope);
        return execution.evaluate(Execution.asEvaluable(target.get(findRecordAttribute(target, member))), scope);
    }

    @Override
    public void assign(Execution execution, java.lang.Object value, ObjectScope scope) {
        MutableObject target = requireRecord(execution, scope);
        Attribute attribute = findRecordAttribute(target, member);
        target.set(attribute, PascalValues.valueOf(attribute.getType().get(), value));
    }

    private MutableObject requireRecord(Execution execution, ObjectScope scope) {
        java.lang.Object value = execution.evaluate(receiver, scope);
        if (value instanceof MutableObject mutableObject) {
            return mutableObject;
        }
        throw new IllegalStateException("Not a record reference: " + receiver);
    }

    private static Attribute findRecordAttribute(MutableObject object, String name) {
        return object.getAttributes().stream()
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
