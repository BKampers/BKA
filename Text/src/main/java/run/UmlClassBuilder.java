/*
** © Bart Kampers
*/
package run;

import java.util.*;
import uml.annotation.*;
import uml.structure.*;


public class UmlClassBuilder {

    public UmlClassBuilder() {
        this(Optional.empty(), false);
    }

    public UmlClassBuilder(String name) {
        this(Optional.of(name), false);
    }

    private UmlClassBuilder(Optional<String> name, boolean isAbstract) {
        umlClass = new UmlClass(name, isAbstract);
    }

    public UmlClassBuilder withAttribute(String name, Type type) {
        checkBuilt();
        umlClass.addAttribute(new UmlAttribute(Optional.of(name), Optional.of(type), Member.Visibility.PRIVATE, false));
        return this;
    }

    public UmlClassBuilder withOperation(String name, Member.Visibility visibility, Set<Stereotype> stereotypes) {
        checkBuilt();
        umlClass.addOperation(new UmlOperation(Optional.of(name), Collections.emptyList(), Optional.empty(), visibility, false, false, stereotypes));
        return this;
    }

    public UmlClassBuilder withOperation(String name, List<Parameter> parameters, Type type, Member.Visibility visibility) {
        checkBuilt();
        umlClass.addOperation(new UmlOperation(Optional.of(name), parameters, Optional.of(type), visibility, false, false, Collections.emptySet()));
        return this;
    }

    public uml.structure.Class build() {
        checkBuilt();
        built = true;
        umlClass.setMemberOwners();
        return umlClass;
    }

    private void checkBuilt() {
        if (built) {
            throw new IllegalStateException("Already built");
        }
    }

    private class UmlAttribute implements Attribute {

        private UmlAttribute(Optional<String> name, Optional<Type> type, Member.Visibility visibility, boolean isClassScoped) {
            this.name = name;
            this.type = type;
            this.visibility = visibility;
            this.isClassScoped = isClassScoped;
        }

        @Override
        public Optional<String> getName() {
            return name;
        }

        @Override
        public Optional<Type> getType() {
            return type;
        }

        @Override
        public Member.Visibility getVisibility() {
            return visibility;
        }

        @Override
        public boolean isClassScoped() {
            return isClassScoped;
        }

        @Override
        public Type getOwner() {
            return owner;
        }

        private void setOwner(uml.structure.Class owner) {
            this.owner = owner;
        }

        private final Optional<String> name;
        private final Optional<Type> type;
        private final Member.Visibility visibility;
        private final boolean isClassScoped;
        private uml.structure.Class owner;
    }


    private class UmlOperation implements Operation {

        public UmlOperation(Optional<String> name, List<Parameter> parameters, Optional<Type> type, Visibility visibility, boolean isClassScoped, boolean isAbstract, Set<Stereotype> stereotypes) {
            this.name = name;
            this.parameters = new ArrayList<>(parameters);
            this.type = type;
            this.visibility = visibility;
            this.isAbstract = isAbstract;
            this.isClassScoped = isClassScoped;
            this.stereotypes = new HashSet<>(stereotypes);
        }

        @Override
        public Optional<String> getName() {
            return name;
        }

        @Override
        public List<Parameter> getParameters() {
            return Collections.unmodifiableList(parameters);
        }

        @Override
        public Optional<Type> getType() {
            return type;
        }

        @Override
        public Member.Visibility getVisibility() {
            return visibility;
        }

        @Override
        public boolean isClassScoped() {
            return isClassScoped;
        }

        @Override
        public boolean isAbstract() {
            return isAbstract;
        }

        @Override
        public Type getOwner() {
            return owner;
        }

        private void setOwner(uml.structure.Class owner) {
            this.owner = owner;
        }

        @Override
        public Set<Stereotype> getStereotypes() {
            return Collections.unmodifiableSet(stereotypes);
        }

        private final Optional<String> name;
        private final Optional<Type> type;
        private final Member.Visibility visibility;
        private final List<Parameter> parameters;
        private final boolean isAbstract;
        private final boolean isClassScoped;
        private uml.structure.Class owner;
        private final Set<Stereotype> stereotypes;
    }


    private class UmlClass implements uml.structure.Class {

        public UmlClass(Optional<String> name, boolean isAbstract) {
            this.name = name;
            this.isAbstract = isAbstract;
        }

        @Override
        public List<uml.structure.Class> getParents() {
            return Collections.unmodifiableList(parents);
        }

        @Override
        public Optional<String> getName() {
            return name;
        }

        @Override
        public List<Attribute> getAttributes() {
            return Collections.unmodifiableList(attributes);
        }

        @Override
        public List<Operation> getOperations() {
            return Collections.unmodifiableList(operations);
        }

        @Override
        public boolean isAbstract() {
            return isAbstract;
        }

        private void addAttribute(UmlAttribute attribute) {
            attributes.add(attribute);
        }

        private void addOperation(UmlOperation operation) {
            operations.add(operation);
        }

        private void setMemberOwners() {
            attributes.forEach(attribute -> attribute.setOwner(this));
            operations.forEach(operation -> operation.setOwner(this));
        }

        private final Optional<String> name;
        private final boolean isAbstract;
        private final List<uml.structure.Class> parents = new ArrayList<>();
        private final List<UmlAttribute> attributes = new ArrayList<>();
        private final List<UmlOperation> operations = new ArrayList<>();
    }


    private boolean built;
    private final UmlClass umlClass;
}
