/*
** © Bart Kampers
*/

package bka.text.parser.pascal;

import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import run.*;
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
        PascalParser.Node statements = compoundStatement;
        Collection<TransitionSource> sources = List.of(createInitialState());
        while (statements != null) {
            Statement statement = new Statement(statements.getChildren().getFirst());
            Collection<Transition<Event, GuardCondition, Action>> transitions = statement.getTransitions();
            if (!transitions.isEmpty()) {
                Collection<Transition<Event, GuardCondition, Action>> finals = new ArrayList<>();
                for (Transition<Event, GuardCondition, Action> transition : transitions) {
                    if ("Initial state".equals(transition.getSource().toString())) {
                        body.addAll(createTransitions(sources, transition.getTarget()));
                    }
                    else if ("Final state".equals(transition.getTarget().toString())) {
                        finals.add(transition);
                    }
                    else {
                        body.add(transition);
                    }
                }
                sources = finals.stream().map(Transition::getSource).collect(Collectors.toList());
            }
            else {
//                ActionState<Action> actionState = createActionState(statement);
//                body.addAll(createTransitions(sources, actionState));
//                sources = List.of(actionState);
            }
            if (statements.getChildren().size() > 1) {
                statements = statements.getChildren().getLast();
            }
            else {
                body.addAll(createTransitions(sources, createFinalState()));
                statements = null;
            }
        }
        return body;
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

//    private Decision<Expression> createDecision(Expression expression, Type type) {
//        return new Decision() {
//
//            @Override
//            public Expression getExpression() {
//                return expression;
//            }
//
//            @Override
//            public Optional<Type> getType() {
//                return Optional.of(type);
//            }
//
//            @Override
//            public Optional<String> getName() {
//                return Optional.of(expression.toString());
//            }
//
//            @Override
//            public String toString() {
//                return "Decision (" + type.toString() + ") " + expression;
//            }
//
//        };
//    }

    private static Transition<Event, GuardCondition, Action> createTransition(TransitionSource source, TransitionTarget target) {
        return createTransition(source, target, Collections.emptySet());
    }

    private static Transition<Event, GuardCondition, Action> createTransition(TransitionSource source, TransitionTarget target, Set<Stereotype> stereotypes) {
        return createTransition(source, target, null, stereotypes);
    }

    private static Transition<Event, GuardCondition, Action> createTransition(TransitionSource source, TransitionTarget target, GuardCondition guardCondition) {
        return createTransition(source, target, guardCondition, Collections.emptySet());
    }

    private static Transition<Event, GuardCondition, Action> createTransition(TransitionSource source, TransitionTarget target, GuardCondition guardCondition, Set<Stereotype> stereotypes) {
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
                return Optional.ofNullable(guardCondition);
            }

            @Override
            public Optional getAction() {
                return Optional.empty();
            }

            @Override
            public Set<Stereotype> getStereotypes() {
                return stereotypes;
            }

            @Override
            public String toString() {
                return getSource() + " \u279D " + getTarget();
            }

        };
    }

    private static Set<Stereotype> createStereotypes(String... stereotypes) {
        return Arrays.stream(stereotypes).map(PascalCompiler::createStereotype).collect(Collectors.toSet());
    }

    private static Stereotype createStereotype(String name) {
        return () -> name;
    }


    private Statement createIncrementStatement(PascalParser.Node identifier) {
        return new Statement(identifier);
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
                builder.append(assignable.get().content()).append(" ← ");
            }
            builder.append(expression.content());
            return builder.toString();
        }

        public Collection<Transition<Event, GuardCondition, Action>> getTransitions() {
            if (expression.getChildren().isEmpty()) {
                return Collections.emptyList();
            }
            Collection<Transition<Event, GuardCondition, Action>> transitions = new ArrayList<>();
            if ("IF\\b".equals(expression.getChildren().getFirst().getSymbol())) {
                ParseTreeExpression condition = createParseTreeExpression(expression.getChildren().get(1).getSymbol(), expression.getChildren().get(1).getChildren());
                Decision decision = createDecision(condition);
                transitions.add(createTransition(createInitialState(), decision));
                ActionState<Action> ifClause = createActionState(new Statement(expression.getChildren().get(3)));
// FIXME     transitions.add(createTransition(decision, ifClause, condition, createStereotypes("then")));
                FinalState finalState = createFinalState();
                transitions.add(createTransition(ifClause, finalState));
                if (expression.getChildren().size() == 6) {
                    ActionState<Action> elseClause = createActionState(new Statement(expression.getChildren().get(5)));
                    transitions.add(createTransition(decision, elseClause, createStereotypes("else")));
                    transitions.add(createTransition(elseClause, finalState));
                }
            }
            else if ("FOR\\b".equals(expression.getChildren().getFirst().getSymbol())) {
                PascalParser.Node identifier = expression.getChildren().get(1);
                ActionState<Action> loopInitialization = createActionState(new Statement(identifier, expression.getChildren().get(3)));
                transitions.add(createTransition(createInitialState(), loopInitialization));
                ParseTreeExpression condition = createLessEqualExpression(identifier, expression.getChildren().get(5));
                Decision decision = createDecision(condition);
                transitions.add(createTransition(loopInitialization, decision));
                ActionState<Action> body = createActionState(new Statement(expression.getChildren().get(7)));
// FIXME                transitions.add(createTransition(decision, body, condition));
                transitions.add(createTransition(decision, createFinalState(), createStereotypes("end-for")));
                ActionState<Action> incrementState = createActionState(createIncrementStatement(identifier));
                transitions.add(createTransition(body, incrementState));
                transitions.add(createTransition(incrementState, decision, createStereotypes("loop")));
            }
            else if ("WHILE\\b".equals(expression.getChildren().getFirst().getSymbol())) {
                Decision decision = createDecision(createParseTreeExpression(expression.getChildren().get(1).getSymbol(), expression.getChildren().get(1).getChildren()));
                transitions.add(createTransition(createInitialState(), decision));
                for (Transition<Event, GuardCondition, Action> transition : createBody(expression.getChildren().get(3).getChildren().getFirst().getChildren().get(1))) {
                    if ("Initial state".equals(transition.getSource().toString())) {
                        GuardCondition condition = memory -> ((ParseTreeExpression) decision.getExpression()).evaluate(memory).get().equals(true);
                        Transition<Event, GuardCondition, Action> enterLoop = createTransition(decision, transition.getTarget(), condition);
                        transitions.add(enterLoop);
                    }
                    else if ("Final state".equals(transition.getTarget().toString())) {
                        transitions.add(createTransition(transition.getSource(), decision));
                    }
                    else {
                        transitions.add(transition);
                    }
                }
                transitions.add(createTransition(decision, createFinalState(), createStereotypes("end-while")));
            }
            else if ("REPEAT\\b".equals(expression.getChildren().getFirst().getSymbol())) {
                ActionState<Action> body = createActionState(new Statement(expression.getChildren().get(1)));
                transitions.add(createTransition(createInitialState(), body));
                ParseTreeExpression condition = createParseTreeExpression(expression.getChildren().get(3).getSymbol(), expression.getChildren().get(3).getChildren());
                Decision decision = createDecision(condition);
                transitions.add(createTransition(body, decision));
// FIXME                transitions.add(createTransition(decision, body, condition, createStereotypes("loop")));
                transitions.add(createTransition(decision, createFinalState(), createStereotypes("end-repeat")));
            }
            else if ("Assignable".equals(expression.getChildren().getFirst().getSymbol())) {
                ActionState<Action> assignment = createActionState(new Statement(expression.getChildren().getFirst(), expression.getChildren().get(2)));
                transitions.add(createTransition(createInitialState(), assignment));
                transitions.add(createTransition(assignment, createFinalState()));
            }
            else {
                throw new IllegalStateException("Unexpected symbol " + expression.getChildren().getFirst().getSymbol());
            }
            return transitions;
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
//                switch (expression.getChildren().getFirst().getSymbol()) {
//                    case "Literal":
//                        switch (expression.getChildren().getFirst().getChildren().getFirst().getSymbol()) {
//                            case "IntegerLiteral":
//                                return createParseTreeExpression("Integer", expression.getChildren().getFirst().getChildren().getFirst().getChildren().getFirst().content());
//                            case "RealLiteral":
//                                return createParseTreeExpression("Real", expression.getChildren().getFirst().getChildren().getFirst().getChildren().getFirst().content());
//                            case "'":
//                                return createParseTreeExpression("String", expression.getChildren().getFirst().getChildren().get(1).content());
//                            case "FALSE\\b":
//                                return createParseTreeExpression("Boolean", "false");
//                            case "TRUE\\b":
//                                return createParseTreeExpression("Boolean", "true");
//                        }
//                        throw new IllegalStateException("Unrecognized literal: " + expression);
//                    case "Identifier":
                        return createParseTreeExpression(expression.getSymbol(), expression.getChildren());
//                }
            }
            if ("Identifier".equals(expression.getSymbol())) {
//                return createIdentifierExpression(expression.content());
                return createParseTreeExpression(expression.getSymbol(), expression.getChildren());
            }
            throw new IllegalStateException("Not an expression: " + expression);
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
                public Value evaluate(Memory memory) {
                    return new Value() {
                        @Override
                        public java.lang.Object get() {
                            return false;
                        }

                        @Override
                        public String type() {
                            return "Boolean";
                        }

                    };
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
                throw new IllegalStateException("Cannot create parse tree expression");
//                return new ParseTreeExpression() {
//
//                    @Override
//                    public String type() {
//                        return "Integer";
//                    }
//
//                    @Override
//                    public String value() {
//                        return "0";
//                    }
//
//                    @Override
//                    public Value evaluate() {
//                        return new Value() {
//                            @Override
//                            public java.lang.Object get() {
//                                return 0;
//                            }
//
//                            @Override
//                            public String type() {
//                                return "Integer";
//                            }
//
//                        };
//                    }
//                };
//                return createParseTreeExpression("*", expression.getFirst().content());
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

//        private static ParseTreeExpression createIdentifierExpression(String identifier) {
//            return new ParseTreeExpression() {
//                @Override
//                public String type() {
//                    return "Integer";
//                }
//
//                @Override
//                public String value() {
//                    return identifier;
//                }
//
//                @Override
//                public Value evaluate() {
//                    return new Value() {
//                        @Override
//                        public java.lang.Object get() {
//                            return 0;
//                        }
//
//                        @Override
//                        public String type() {
//                            return "Integer";
//                        }
//
//                    };
//                }
//
//            };
//        }

//        private static ParseTreeExpression createParseTreeExpression(String type, String value) {
//            return new ParseTreeExpression() {
//                @Override
//                public String type() {
//                    return type;
//                }
//
//                @Override
//                public String value() {
//                    return value;
//                }
//
//                @Override
//                public Value evaluate() {
//                    return new Value() {
//                        @Override
//                        public java.lang.Object get() {
//                            switch (type) {
//                                case "Integer":
//                                    return Integer.valueOf(value);
//                                case "Real":
//                                    return Double.valueOf(value);
//                                case "String":
//                                    return value;
//                                default:
//                                    throw new IllegalStateException("Unsuppored type: " + type);
//                            }
//                        }
//
//                        @Override
//                        public String type() {
//                            return type;
//                        }
//
//                    };
//                }
//            };
//        }

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
        switch (node.content()) {
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
