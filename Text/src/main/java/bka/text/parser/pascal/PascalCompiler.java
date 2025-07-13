/*
** © Bart Kampers
*/

package bka.text.parser.pascal;

import java.util.*;
import java.util.function.*;
import java.util.logging.*;
import java.util.stream.*;
import run.*;
import uml.*;
import uml.annotation.*;
import uml.statechart.*;
import uml.structure.*;


public class PascalCompiler {

    public java.lang.Object createObject(List<PascalParser.Node> tree) {
        return switch (tree.getFirst().getSymbol()) {
            case "PROGRAM\\b" ->
                createProgramClass(tree);
            case "IdentifierList" ->
                createIdentifiers(tree.getFirst());
            case "Identifier" ->
                tree.getFirst().getChildren().getFirst().content();
            default ->
                tree.getFirst().content();
        };
    }

    public Collection<Transition<Event, GuardCondition, Action>> getMethod(Operation operation) {
        return Collections.unmodifiableCollection(methods.get(operation));
    }

    private uml.structure.Class createProgramClass(List<PascalParser.Node> nodes) {
        return new uml.structure.Class() {

            @Override
            public Optional<String> getName() {
                return Optional.of(nodes.get(1).content());
            }

            @Override
            public List<Attribute> getAttributes() {
                return createProgramVariables(this, nodes.get(3));
            }

            @Override
            public List<Operation> getOperations() {
                return createOperations(this, nodes.get(3), nodes.get(4));
            }

            @Override
            public boolean isAbstract() {
                return false;
            }

        };
    }

    private List<Attribute> createProgramVariables(Type owner, PascalParser.Node declarations) {
        if (declarations.getChildren().isEmpty()) {
            return Collections.emptyList();
        }
        ArrayList<Attribute> attributes = new ArrayList<>();
        if ("VariableDeclaration".equals(declarations.getChildren().getFirst().getSymbol())) {
            attributes.addAll(createVariables(owner, declarations.getChildren().getFirst()));
        }
        attributes.addAll(createProgramVariables(owner, declarations.getChildren().getLast()));
        return attributes;
    }

    private List<Attribute> createVariables(Type owner, PascalParser.Node variableDeclaration) {
        return createAttributes(owner, variableDeclaration.getChildren().get(variableDeclaration.getChildren().size() - 2));
    }

    private Type createType(PascalParser.Node typeDeclarationExpression) {
        final PascalParser.Node expression = typeDeclarationExpression.getChildren().getFirst();
        return switch (expression.getSymbol()) {
            case "TypeExpression" ->
                createType(expression.getChildren().getFirst().content());
            case "RangeExpression" ->
                createRangeType(expression);
            case "\\(" ->
                createEnumerationType(typeDeclarationExpression.getChildren().get(1));
            case "ARRAY\\b" ->
                createArrayType(typeDeclarationExpression.getChildren().get(2), typeDeclarationExpression.getChildren().get(5));
            case "RECORD\\b" ->
                createRecordType(typeDeclarationExpression);
            default ->
                throw new IllegalStateException("UnsupportedType " + typeDeclarationExpression.getChildren().getFirst().getSymbol());
        };
    }

    private static Type createType(String name) {
        return new Type() {

            @Override
            public boolean isAbstract() {
                return false;
            }

            @Override
            public Optional<String> getName() {
                return Optional.of(name);
            }

            @Override
            public String toString() {
                return "Type " + name;
            }

        };
    }

    private Type createRangeType(PascalParser.Node rangeExpression) {
        return new Type() {

            @Override
            public boolean isAbstract() {
                return false;
            }

            @Override
            public Optional<String> getName() {
                return Optional.of(rangeString(rangeExpression));
            }

            @Override
            public String toString() {
                return "Type " + rangeString(rangeExpression);
            }

        };
    }

    private Type createEnumerationType(PascalParser.Node identifierList) {
        return new Type() {

            @Override
            public boolean isAbstract() {
                return false;
            }

            @Override
            public Optional<String> getName() {
                return Optional.of(createIdentifiers(identifierList).stream().collect(Collectors.joining(", ", "( ", " )")));
            }

            @Override
            public String toString() {
                return "Type " + getName().get();
            }

        };
    }

    private static Type createArrayType(PascalParser.Node rangeExpression, PascalParser.Node typeExpression) {
        return new Type() {

            @Override
            public boolean isAbstract() {
                return false;
            }

            @Override
            public Optional<String> getName() {
                return Optional.of("ARRAY [" + rangeString(rangeExpression) + "] OF " + typeExpression.content());
            }

            @Override
            public String toString() {
                return "Type " + getName().get();
            }

        };
    }

    private uml.structure.Class createRecordType(PascalParser.Node typeDeclarationExpression) {
        return new uml.structure.Class() {
            @Override
            public List<Attribute> getAttributes() {
                return createAttributes(this, typeDeclarationExpression.getChildren().get(1));
            }

            @Override
            public List<Operation> getOperations() {
                return Collections.emptyList();
            }

            @Override
            public boolean isAbstract() {
                return false;
            }

            @Override
            public Optional<String> getName() {
                return Optional.of(typeDeclarationExpression.content());
            }

            @Override
            public String toString() {
                return "Type " + getName().get();
            }

        };
    }

    private List<Attribute> createAttributes(Type owner, PascalParser.Node variableDeclarationList) {
        List<Attribute> attributes = new ArrayList<>();
        attributes.addAll(createAttributesFromExpression(owner, variableDeclarationList.getChildren().getFirst()));
        if (variableDeclarationList.getChildren().size() > 1) {
            attributes.addAll(createAttributes(owner, variableDeclarationList.getChildren().getLast()));
        }
        return attributes;
    }

    private List<Attribute> createAttributesFromExpression(Type owner, PascalParser.Node variableDeclarationExpression) {
        List<Attribute> attributes = new ArrayList<>();
        Type type = createType(variableDeclarationExpression.getChildren().get(2));
        createIdentifiers(variableDeclarationExpression.getChildren().getFirst()).forEach(name -> attributes.add(createAttribute(owner, name, type)));
        return attributes;
    }

    private static String rangeString(PascalParser.Node rangeExpression) {
        return rangeExpression.getChildren().getFirst().content() + " .. " + rangeExpression.getChildren().getLast().content();
    }

    private static List<String> createIdentifiers(PascalParser.Node identifierList) {
        List<String> identifiers = new ArrayList<>();
        identifiers.add(identifierList.getChildren().getFirst().content());
        if (identifierList.getChildren().size() > 1) {
            identifiers.addAll(createIdentifiers(identifierList.getChildren().getLast()));
        }
        return identifiers;
    }

    private Attribute createAttribute(Type owner, String name, Type type) {
        return new Attribute() {

            @Override
            public Type getOwner() {
                return owner;
            }

            @Override
            public Member.Visibility getVisibility() {
                return Member.Visibility.PRIVATE;
            }

            @Override
            public boolean isClassScoped() {
                return false;
            }

            @Override
            public Optional<String> getName() {
                return Optional.of(name);
            }

            @Override
            public Optional<Type> getType() {
                return Optional.of(type);
            }
        };
    }

    private List<Operation> createOperations(Type owner, PascalParser.Node declarations, PascalParser.Node mainBody) {
        List<Operation> operations = new ArrayList<>();
        Operation main = createMainOperation(owner);
        methods.put(main, createBody(mainBody.getChildren().get(1)));
        operations.add(main);
        return operations;
    }


    private Operation createMainOperation(Type owner) {
        return new Operation() {

            @Override
            public Type getOwner() {
                return owner;
            }

            @Override
            public Member.Visibility getVisibility() {
                return Member.Visibility.PUBLIC;
            }

            @Override
            public Optional<String> getName() {
                return Optional.empty();
            }

            @Override
            public boolean isClassScoped() {
                return false;
            }

            @Override
            public boolean isAbstract() {
                return false;
            }

            @Override
            public List<Parameter> getParameters() {
                return Collections.emptyList();
            }

            @Override
            public Optional<Type> getType() {
                return Optional.empty();
            }

            @Override
            public Set<Stereotype> getStereotypes() {
                return createStereotypes("Main");
            }
        };
    }

    private Collection<Transition<Event, GuardCondition, Action>> createBody(PascalParser.Node compoundStatement) {
        Collection<Transition<Event, GuardCondition, Action>> body = new ArrayList<>();
        Collection<TransitionSource> leaves = new ArrayList<>(List.of(createInitialState()));
        createBody(compoundStatement, body, leaves);
        leaves.forEach(leave -> body.add(createTransition(leave, createFinalState())));
        return body;
    }

    private void createBody(PascalParser.Node compoundStatement, Collection<Transition<Event, GuardCondition, Action>> body, Collection<TransitionSource> leaves) {
        PascalParser.Node statements = compoundStatement;
        while (statements != null) {
            Statement statement = new Statement(statements.getChildren().getFirst());
            statement.getTransitions(body, leaves);
            statements = (statements.getChildren().size() > 1) ? statements.getChildren().getLast() : null;
        }
    }

    private static Collection<Transition<Event, GuardCondition, Action>> createTransitions(Collection<TransitionSource> sources, TransitionTarget target) {
        return sources.stream().map(source -> createTransition(source, target)).collect(Collectors.toList());
    }

    private static InitialState createInitialState() {
        return new InitialState() {

            @Override
            public String toString() {
                return "Initial state";
            }

        };
    }

    private static FinalState createFinalState() {
        return new FinalState() {

            @Override
            public String toString() {
                return "Final state";
            }

        };
    }

    private ActionState<Action> createActionState(Action action) {
        return new ActionState() {
            @Override
            public Optional<Action> getAction() {
                return Optional.of(action);
            }

            @Override
            public String toString() {
                return String.format("Action state (%s)", action);
            }
        };
    }

    private ActionState<Action> createActionState(Statement statement) {
        return new ActionState() {

            @Override
            public Optional<Action> getAction() {
                return Optional.of(createAction(statement));
            }

            @Override
            public String toString() {
                return String.format("Action state (%s)", statement);
            }

        };
    }

    private static Action createAction(Statement statement) {
        return new Action() {
            @Override
            public void perform(Memory memory) throws StateMachineException {
                Optional<ParseTreeExpression> expression = statement.getExpressionTree();
                if (expression.isPresent()) {
                    Value value = expression.get().evaluate(memory);
                    Optional<PascalParser.Node> assignable = statement.getAssignable();
                    if (assignable.isPresent()) {
                        memory.store(assignable.get().content(), value.get());
                    }
                }
            }

            @Override
            public String toString() {
                return String.format("Action (%s)", statement);
            }
        };
    }

    private static Decision<Expression> createDecision(ParseTreeExpression expression) {
        return new Decision() {

            @Override
            public Expression getExpression() {
                return expression;
            }

            @Override
            public Optional<Type> getType() {
                return Optional.of(createType(expression.type()));
            }

            @Override
            public Optional<String> getName() {
                return Optional.of(expression.toString());
            }

            @Override
            public String toString() {
                return "Decision (" + typeString(getType()) + ") " + expression.value();
            }

            private String typeString(Optional<Type> type) {
                if (type.isEmpty()) {
                    return "Void";
                }
                if (type.get().getName().isEmpty()) {
                    return "Anonimous";
                }
                return type.get().getName().get();
            }

        };
    }

    private static Transition<Event, GuardCondition, Action> createTransition(TransitionSource source, TransitionTarget target) {
        return createTransition(source, target, Optional.empty(), Optional.empty(), Collections.emptySet());
    }

    private static Transition<Event, GuardCondition, Action> createTransition(TransitionSource source, TransitionTarget target, Set<Stereotype> stereotypes) {
        return createTransition(source, target, Optional.empty(), Optional.empty(), stereotypes);
    }

    private static Transition<Event, GuardCondition, Action> createTransition(TransitionSource source, TransitionTarget target, GuardCondition guardCondition, Set<Stereotype> stereotypes) {
        return createTransition(source, target, Optional.of(guardCondition), Optional.empty(), stereotypes);
    }

    private static Transition<Event, GuardCondition, Action> createTransition(TransitionSource source, TransitionTarget target, GuardCondition guardCondition) {
        return createTransition(source, target, Optional.of(guardCondition), Optional.empty(), Collections.emptySet());
    }

    private static Transition<Event, GuardCondition, Action> createTransition(TransitionSource source, TransitionTarget target, Action action) {
        return createTransition(source, target, Optional.empty(), Optional.of(action), Collections.emptySet());
    }

    private static Transition<Event, GuardCondition, Action> createTransition(TransitionSource source, TransitionTarget target, Optional<GuardCondition> guardCondition, Optional<Action> action, Set<Stereotype> stereotypes) {
        return new Transition<>() {

            @Override
            public TransitionSource getSource() {
                return source;
            }

            @Override
            public TransitionTarget getTarget() {
                return target;
            }

            @Override
            public Optional getEvent() {
                return Optional.empty();
            }

            @Override
            public Optional<GuardCondition> getGuardCondition() {
                return guardCondition;
            }

            @Override
            public Optional<Action> getAction() {
                return action;
            }

            @Override
            public Set<Stereotype> getStereotypes() {
                return stereotypes;
            }

            @Override
            public String toString() {
                StringBuilder string = new StringBuilder();
                guardCondition.ifPresent(condition -> string.append('[').append(condition).append("] "));
                string.append(getSource()).append(" \u279D ").append(getTarget());
                string.append(stereotypes.stream().map(Util::display).collect(Collectors.joining()));
                return string.toString();
            }

        };
    }

    private static Set<Stereotype> createStereotypes(String... stereotypes) {
        return Arrays.stream(stereotypes).map(PascalCompiler::createStereotype).collect(Collectors.toSet());
    }

    private static Stereotype createStereotype(String name) {
        return () -> name;
    }


    public class Statement {

        public Statement(PascalParser.Node node) {
            this(Optional.empty(), node);
        }

        public Statement(PascalParser.Node assignable, PascalParser.Node expression) {
            this.assignable = Optional.of(assignable);
            this.expression = Objects.requireNonNull(expression);
        }

        public Statement(Optional<PascalParser.Node> assignable, PascalParser.Node expression) {
            this.assignable = Objects.requireNonNull(assignable);
            this.expression = Objects.requireNonNull(expression);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder().append('(').append(expression.getSymbol()).append(") ");
            if (assignable.isPresent()) {
                builder.append(assignable.get().content()).append(" \u21D0 ");
            }
            builder.append(expression.content());
            return builder.toString();
        }

        public void getTransitions(Collection<Transition<Event, GuardCondition, Action>> transitions, Collection<TransitionSource> leaves) {
            if (expression.getChildren().isEmpty()) {
                Logger.getLogger(PascalCompiler.class.getName()).log(Level.WARNING, "Empty statenemt: {0}", toString());
                return;
            }
            if ("IF\\b".equals(expression.getChildren().getFirst().getSymbol())) {
                Decision decision = createDecision(createParseTreeExpression(expression.getChildren().get(1).getSymbol(), expression.getChildren().get(1).getChildren()));
                leaves.forEach(leave -> transitions.add(createTransition(leave, decision)));
                leaves.clear();
                leaves.add(decision);
                createTransitions(expression.getChildren().get(3), transitions, leaves);
                addGuardCondition(transitions, transition -> decision.equals(transition.getSource()), pass(decision), "then");
                if (expression.getChildren().get(4).getChildren().isEmpty()) {
                    leaves.add(decision);
                }
                else {
                    Collection<TransitionSource> elseLeaves = new ArrayList<>(List.of(decision));
                    createTransitions(expression.getChildren().get(4).getChildren().get(1), transitions, elseLeaves);
                    addStereotype(transitions, transition -> decision.equals(transition.getSource()) && transition.getGuardCondition().isEmpty(), "else");
                    leaves.addAll(elseLeaves);
                }
            }
            else if ("FOR\\b".equals(expression.getChildren().getFirst().getSymbol())) {
                PascalParser.Node identifier = expression.getChildren().get(1);
                ActionState<Action> loopInitialization = createActionState(new Statement(identifier, expression.getChildren().get(3)));
                leaves.forEach(leave -> transitions.add(createTransition(leave, loopInitialization)));
                leaves.clear();
                Decision decision = createDecision(createLessEqualExpression(identifier, expression.getChildren().get(5)));
                transitions.add(createTransition(loopInitialization, decision));
                leaves.add(decision);
                createTransitions(expression.getChildren().get(7), transitions, leaves);
                Action incrementAction = new Action() {
                    @Override
                    public void perform(Memory memory) throws StateMachineException {
                        memory.store(identifier.content(), ((Integer) memory.load(identifier.content())) + 1);
                    }

                    @Override
                    public String toString() {
                        return ".INC. " + identifier.content();
                    }
                };
                ActionState<Action> incrementActionState = createActionState(incrementAction);
                leaves.forEach(leave -> transitions.add(createTransition(leave, incrementActionState)));
                leaves.clear();
                addGuardCondition(transitions, transition -> decision.equals(transition.getSource()), pass(decision), "for");
                transitions.add(createTransition(incrementActionState, decision));
                leaves.add(decision);
            }
            else if ("WHILE\\b".equals(expression.getChildren().getFirst().getSymbol())) {
                Decision decision = createDecision(createParseTreeExpression(expression.getChildren().get(1).getSymbol(), expression.getChildren().get(1).getChildren()));
                leaves.forEach(leave -> transitions.add(createTransition(leave, decision)));
                leaves.clear();
                leaves.add(decision);
                createTransitions(expression.getChildren().get(3), transitions, leaves);
                addGuardCondition(transitions, transition -> decision.equals(transition.getSource()), pass(decision), "while");
                leaves.forEach(leave -> transitions.add(createTransition(leave, decision, createStereotypes("loop"))));
                leaves.clear();
                leaves.add(decision);
            }
            else if ("REPEAT\\b".equals(expression.getChildren().getFirst().getSymbol())) {
                TransitionSource loopRoot = leaves.stream().findAny().get();
                createTransitions(expression.getChildren().get(1), transitions, leaves);
                ParseTreeExpression condition = createParseTreeExpression(expression.getChildren().get(3).getSymbol(), expression.getChildren().get(3).getChildren());
                Decision decision = createDecision(condition);
                leaves.forEach(leave -> transitions.add(createTransition(leave, decision)));
                TransitionTarget loopStart = transitions.stream().filter(transition -> loopRoot.equals(transition.getSource())).findAny().get().getTarget();
                transitions.add(createTransition(decision, loopStart, fail(decision), createStereotypes("repeat")));
                leaves.clear();
                leaves.add(decision);
            }
            else if ("Assignable".equals(expression.getChildren().getFirst().getSymbol())) {
                ActionState<Action> assignment = createActionState(new Statement(expression.getChildren().getFirst(), expression.getChildren().get(2)));
                leaves.forEach(leave -> transitions.add(createTransition(leave, assignment)));
                leaves.clear();
                leaves.add(assignment);
            }
            else {
                throw new IllegalStateException("Unexpected symbol " + expression.getChildren().getFirst().getSymbol());
            }
        }

        private void addGuardCondition(Collection<Transition<Event, GuardCondition, Action>> transitions, Predicate<Transition<Event, GuardCondition, Action>> predicate, GuardCondition guardCondition, String stereotype) {
            Transition<Event, GuardCondition, Action> transition = transitions.stream().filter(predicate).findAny().get();
            transitions.remove(transition);
            transitions.add(createTransition(transition.getSource(), transition.getTarget(), Optional.of(guardCondition), transition.getAction(), createStereotypes(stereotype)));
        }

        private void addStereotype(Collection<Transition<Event, GuardCondition, Action>> transitions, Predicate<Transition<Event, GuardCondition, Action>> predicate, String stereotype) {
            Transition<Event, GuardCondition, Action> transition = transitions.stream().filter(predicate).findAny().get();
            transitions.remove(transition);
            transitions.add(createTransition(transition.getSource(), transition.getTarget(), transition.getGuardCondition(), transition.getAction(), createStereotypes(stereotype)));
        }

        private static GuardCondition pass(Decision decision) {
            return new GuardCondition() {
                @Override
                public boolean applies(Memory memory) throws StateMachineException {
                    return ((ParseTreeExpression) decision.getExpression()).evaluate(memory).get().equals(true);
                }

                @Override
                public String toString() {
                    return decision.getExpression().toString();
                }
            };
        }

        private static GuardCondition fail(Decision decision) {
            return new GuardCondition() {
                @Override
                public boolean applies(Memory memory) throws StateMachineException {
                    return ((ParseTreeExpression) decision.getExpression()).evaluate(memory).get().equals(false);
                }

                @Override
                public String toString() {
                    return "\u00AC (" + decision.getExpression().toString() + ')';
                }
            };
        }

        private void createTransitions(PascalParser.Node statements, Collection<Transition<Event, GuardCondition, Action>> transitions, Collection<TransitionSource> leaves) {
            if ("CompoundStatement".equals(statements.getChildren().getFirst().getSymbol())) {
                createBody(statements.getChildren().getFirst().getChildren().get(1), transitions, leaves);
            }
            else {
                new Statement(statements).getTransitions(transitions, leaves);
            }
        }

        private static String typeOf(List<PascalParser.Node> expression) {
            if (expression.size() == 1) {
                return "*";
            }
            switch (expression.get(1).content()) {
                case "<":
                case "<=":
                case "=":
                case ">":
                case ">=":
                case "<>":
                    return "Boolean";
            }
            return generalType(getExpressionTree(expression.getFirst()).type(), getExpressionTree(expression.getLast()).type());
        }

        public Optional<ParseTreeExpression> getExpressionTree() {
            if ("Expression".equals(expression.getSymbol())) {
                return Optional.of(getExpressionTree(expression));
            }
            throw new IllegalStateException("Not an expression: " + expression);
        }

        private static ParseTreeExpression getExpressionTree(PascalParser.Node expression) {
            if ("Expression".equals(expression.getSymbol())) {
                return createParseTreeExpression(expression.getSymbol(), expression.getChildren());
            }
            if ("Identifier".equals(expression.getSymbol())) {
                return createParseTreeExpression(expression.getSymbol(), expression.getChildren());
            }
            throw new IllegalStateException("Not an expression: " + expression);
        }

        private static ParseTreeExpression createIncrementExpression(PascalParser.Node operand) {
            return new ParseTreeExpression() {
                @Override
                public String type() {
                    return "Boolean";
                }

                @Override
                public String value() {
                    return ".INC. " + operand.content();
                }

                @Override
                public Value evaluate(Memory memory) throws StateMachineException {
                    return new Value() {
                        @Override
                        public java.lang.Object get() throws StateMachineException {
                            Value value = createParseTreeExpression(operand.getSymbol(), operand.getChildren()).evaluate(memory);
                            return ((Integer) value.get()) + 1;
                        }

                        @Override
                        public String type() {
                            return "Integer";
                        }

                    };
                }
            };
        }

        private static ParseTreeExpression createLessEqualExpression(PascalParser.Node leftOperand, PascalParser.Node rightOperand) {
            return new ParseTreeExpression() {
                @Override
                public String type() {
                    return "Boolean";
                }

                @Override
                public String value() {
                    return leftOperand.content() + " .LE. " + rightOperand.content();
                }
                @Override
                public Value evaluate(Memory memory) throws StateMachineException {
                    return new Value() {
                        @Override
                        public java.lang.Object get() throws StateMachineException {
                            Value left = createParseTreeExpression(leftOperand.getSymbol(), leftOperand.getChildren()).evaluate(memory);
                            Value right = createParseTreeExpression(rightOperand.getSymbol(), rightOperand.getChildren()).evaluate(memory);
                            return ((Comparable) left.get()).compareTo((Comparable) right.get()) <= 0;
                        }

                        @Override
                        public String type() {
                            return "Boolean";
                        }

                    };
                }

                @Override
                public String toString() {
                    return value();
                }
            };
        }

        private static ParseTreeExpression createParseTreeExpression(String symbol, List<PascalParser.Node> expression) {
            if (expression.size() == 1) {
                if ("Literal".equals(expression.getFirst().getSymbol())) {
                    return new ParseTreeExpression() {
                        @Override
                        public String type() {
                            return switch (expression.getFirst().getChildren().getFirst().getSymbol()) {
                                case "IntegerLiteral" ->
                                    "Integer";
                                default ->
                                    throw new IllegalArgumentException("Unexpected literal: " + expression.getFirst().getChildren().getFirst().getSymbol());
                            };
                        }

                        @Override
                        public String value() {
                            return expression.getFirst().content();
                        }

                        @Override
                        public Value evaluate(Memory memory) {
                            return switch (expression.getFirst().getChildren().getFirst().getSymbol()) {
                                case "IntegerLiteral" ->
                                    new Value() {
                                        @Override
                                        public java.lang.Object get() {
                                            return Integer.valueOf(expression.getFirst().content());
                                        }

                                        @Override
                                        public String type() {
                                            return "Integer";
                                        }

                                    };
                                case "'" ->
                                    new Value() {
                                        @Override
                                        public java.lang.Object get() {
                                            return Integer.valueOf(expression.get(1).content());
                                        }

                                        @Override
                                        public String type() {
                                            return "String";
                                        }

                                    };
//                                case "Identifier" ->
//                                    new Value() {
//                                        @Override
//                                        public java.lang.Object get() {
//                                            return 0;
//                                        }
//
//                                        @Override
//                                        public String type() {
//                                            return "Integer";
//                                        }
//
//                                    };
                                default ->
                                    throw new IllegalArgumentException("Cannot evaluate literal: " + expression.getFirst().getChildren().getFirst().getSymbol());
                            };
                        }

                    };
                }
                if ("Identifier".equals(expression.getFirst().getSymbol())) {
                    return new ParseTreeExpression() {
                        @Override
                        public String type() {
                            return "*";
                        }

                        @Override
                        public String value() {
                            return expression.getFirst().getChildren().getFirst().content();
                        }

                        @Override
                        public Value evaluate(Memory memory) {
                            return new Value() {
                                @Override
                                public java.lang.Object get() throws StateMachineException {
                                    return memory.load(value());
                                }

                                @Override
                                public String type() {
                                    return "*";
                                }

                            };
                        }

                    };
                }
                if ("Identifier".equals(symbol)) {
                    return new ParseTreeExpression() {
                        @Override
                        public String type() {
                            return "*";
                        }

                        @Override
                        public String value() {
                            return expression.getFirst().content();
                        }

                        @Override
                        public Value evaluate(Memory memory) {
                            return new Value() {
                                @Override
                                public java.lang.Object get() throws StateMachineException {
                                    return memory.load(value());
                                }

                                @Override
                                public String type() {
                                    return "*";
                                }

                            };
                        }

                    };
                }
//                if ("Expression".equals(symbol)) {
//                    return createParseTreeExpression(expression.getFirst().getSymbol(), expression.getFirst().getChildren());
//                }
                throw new IllegalStateException("Cannot create parse tree expression");
            }
            String type = typeOf(expression);
            if (expression.size() == 3 && "BinaryOperator".equals(expression.get(1).getSymbol())) {
                return new ParseTreeExpression() {
                    @Override
                    public String type() {
                        return type;
                    }

                    @Override
                    public String value() {
                        return createParseTreeExpression(expression.getFirst().getSymbol(), expression.getFirst().getChildren()).value()
                            + " (Operator)" + expression.get(1).content() + " "
                            + createParseTreeExpression(expression.getLast().getSymbol(), expression.getLast().getChildren()).value() + ")";
                    }
                    @Override
                    public Value evaluate(Memory memory) throws StateMachineException {
                        return getBinaryOperator(expression.get(1)).evaluate(
                            createParseTreeExpression(expression.getFirst().getSymbol(), expression.getFirst().getChildren()),
                            createParseTreeExpression(expression.getLast().getSymbol(), expression.getLast().getChildren()),
                            memory);
                    }
                };
            }
            throw new IllegalStateException("Invalid expression");
        }

        public Optional<PascalParser.Node> getAssignable() {
            return assignable;
        }

        private final Optional<PascalParser.Node> assignable;
        private final PascalParser.Node expression;
    }

    private static String generalType(String type1, String type2) {
        if (type1.equals(type2)) {
            return type1;
        }
        if (type1.equals("*")) {
            return type2;
        }
        if (type2.equals("*")) {
            return type1;
        }
        if (type1.equals("Integer") && type2.equals("Real") || type1.equals("Real") && type2.equals("Integer")) {
            return "Real";
        }
        throw new IllegalStateException("Cannot determine general type");
    }


    public interface ParseTreeExpression extends Expression {

        String type();

        String value();

        Value evaluate(Memory memory) throws StateMachineException;
    }

    public interface Value {

        java.lang.Object get() throws StateMachineException;

        String type();
    }


    private static BinaryOperator getBinaryOperator(PascalParser.Node node) {
        switch (node.content().toLowerCase()) {
            case "^":
                return createArithmicOperator((left, right) -> power(left, right));
            case "*":
                return createArithmicOperator((left, right) -> product(left, right));
            case "/":
                return createArithmicOperator((left, right) -> quotient(left, right));
            case "div":
                return createArithmicOperator((left, right) -> division(left, right));
            case "mod":
                return createArithmicOperator((left, right) -> modulus(left, right));
            case "+":
                return createArithmicOperator((left, right) -> sum(left, right));
            case "-":
                return createArithmicOperator((left, right) -> difference(left, right));
            case "=":
                return createRelationalOperator((left, right) -> left.compareTo(right) == 0);
            case "<>":
                return createRelationalOperator((left, right) -> left.compareTo(right) != 0);
            case "<":
                return createRelationalOperator((left, right) -> left.compareTo(right) < 0);
            case "<=":
                return createRelationalOperator((left, right) -> left.compareTo(right) <= 0);
            case ">":
                return createRelationalOperator((left, right) -> left.compareTo(right) > 0);
            case ">=":
                return createRelationalOperator((left, right) -> left.compareTo(right) > 0);
            case "and":
                return createLogicalOperator((left, right) -> left && right);
            case "or":
                return createLogicalOperator((left, right) -> left || right);
            case "xor":
                return createLogicalOperator((left, right) -> !left.equals(right));
            default:
                throw new IllegalStateException("Unsupported binary operator: '" + node.content() + "'");
        }
    }

    private static Number power(Number left, Number right) {
        if (left instanceof Integer && right instanceof Integer) {
            int result = 1;
            for (int i = 1; i <= right.intValue(); i++) {
                result *= left.intValue();
            }
            return result;
        }
        return Math.pow(left.doubleValue(), right.doubleValue());
    }

    private static Number product(Number left, Number right) {
        if (left instanceof Integer && right instanceof Integer) {
            return left.intValue() * right.intValue();
        }
        return left.doubleValue() * right.doubleValue();
    }

    private static Double quotient(Number left, Number right) {
        return left.doubleValue() / right.doubleValue();
    }

    private static Integer division(Number left, Number right) {
        return left.intValue() / right.intValue();
    }

    private static Integer modulus(Number left, Number right) {
        return left.intValue() % right.intValue();
    }

    private static Number sum(Number left, Number right) {
        if (left instanceof Integer && right instanceof Integer) {
            return left.intValue() + right.intValue();
        }
        return left.doubleValue() + right.doubleValue();
    }

    private static Number difference(Number left, Number right) {
        if (left instanceof Integer && right instanceof Integer) {
            return left.intValue() - right.intValue();
        }
        return left.doubleValue() - right.doubleValue();
    }

    private static BinaryOperator createArithmicOperator(BiFunction<Number, Number, Number> function) {
        return new BinaryOperator() {
            @Override
            public Value evaluate(ParseTreeExpression leftOperand, ParseTreeExpression rightOperand, Memory memory) throws StateMachineException {
                Number value = function.apply(requireNumber(leftOperand, memory), requireNumber(rightOperand, memory));
                return new Value() {
                    @Override
                    public java.lang.Object get() throws StateMachineException {
                        return value;
                    }

                    @Override
                    public String type() {
                        return (value instanceof Integer) ? "Integer" : "Real";
                    }
                };
            }
        };
    }

    private static Number requireNumber(ParseTreeExpression expression, Memory memory) throws StateMachineException {
        if (expression.evaluate(memory).get() instanceof Number number) {
            return number;
        }
        throw new StateMachineException(expression.type() + "is not a comparable");
    }

    private static BinaryOperator createLogicalOperator(BiFunction<Boolean, Boolean, Boolean> function) {
        return new BinaryOperator() {
            @Override
            public Value evaluate(ParseTreeExpression leftOperand, ParseTreeExpression rightOperand, Memory memory) throws StateMachineException {
                return new Value() {

                    @Override
                    public java.lang.Object get() throws StateMachineException {
                        return function.apply(requireBoolean(leftOperand, memory), requireBoolean(rightOperand, memory));
                    }

                    @Override
                    public String type() {
                        return "Boolean";
                    }
                };
            }
        };
    }

    private static Boolean requireBoolean(ParseTreeExpression expression, Memory memory) throws StateMachineException {
        if (expression.evaluate(memory).get() instanceof Boolean bool) {
            return bool;
        }
        throw new StateMachineException(expression.type() + "is not a boolean");
    }

    private static BinaryOperator createRelationalOperator(BiFunction<Comparable, Comparable, Boolean> function) {
        return new BinaryOperator() {
            @Override
            public Value evaluate(ParseTreeExpression leftOperand, ParseTreeExpression rightOperand, Memory memory) throws StateMachineException {
                return new Value() {

                    @Override
                    public java.lang.Object get() throws StateMachineException {
                        return function.apply(requireComparable(leftOperand, memory), requireComparable(rightOperand, memory));
                    }

                    @Override
                    public String type() {
                        return "Boolean";
                    }
                };
            }
        };
    }

    private static Comparable requireComparable(ParseTreeExpression expression, Memory memory) throws StateMachineException {
        if (expression.evaluate(memory).get() instanceof Comparable comparable) {
            return comparable;
        }
        throw new StateMachineException(expression.type() + "is not a comparable");
    }

    private interface BinaryOperator {

        Value evaluate(ParseTreeExpression leftOperand, ParseTreeExpression rightOperand, Memory memory) throws StateMachineException;
    }

    private final Map<Operation, Collection<Transition<Event, GuardCondition, Action>>> methods = new HashMap<>();

}
