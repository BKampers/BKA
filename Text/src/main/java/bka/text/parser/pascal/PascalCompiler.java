/*
** © Bart Kampers
*/

package bka.text.parser.pascal;

import java.util.*;
import java.util.function.*;
import java.util.logging.*;
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
        return Collections.unmodifiableCollection(methods.get(operation.getName().get()));
    }

    private uml.structure.Class createProgramClass(List<PascalParser.Node> nodes) {
        UmlClassBuilder builder = new UmlClassBuilder(nodes.get(1).content());
        addProgramVariables(builder, nodes.get(3));
        String programName = nodes.get(1).content();
        buildOperations(builder, programName, nodes.get(3));
        methods.put(programName, createBody(nodes.get(4).getChildren().get(1)));
        return builder.build();
    }

    private void addProgramVariables(UmlClassBuilder builder, PascalParser.Node declarations) {
        if (!declarations.getChildren().isEmpty()) {
            if ("VariableDeclaration".equals(declarations.getChildren().getFirst().getSymbol())) {
                addVariables(builder, declarations.getChildren().getFirst());
            }
            addProgramVariables(builder, declarations.getChildren().getLast());
        }
    }

    private void addVariables(UmlClassBuilder builder, PascalParser.Node variableDeclaration) {
        createAttributes(builder, variableDeclaration.getChildren().get(variableDeclaration.getChildren().size() - 2));
    }

    private void createAttributes(UmlClassBuilder builder, PascalParser.Node variableDeclarationList) {
        addAttributesFromExpression(builder, variableDeclarationList.getChildren().getFirst());
        if (variableDeclarationList.getChildren().size() > 1) {
            createAttributes(builder, variableDeclarationList.getChildren().getLast());
        }
    }

    private void addAttributesFromExpression(UmlClassBuilder builder, PascalParser.Node variableDeclarationExpression) {
        Type type = createType(variableDeclarationExpression.getChildren().get(2));
        createIdentifiers(variableDeclarationExpression.getChildren().getFirst()).forEach(name -> builder.withAttribute(name, type));
    }

    private void addPrivateFunctionOperations(UmlClassBuilder builder, PascalParser.Node declarations) {
        if (!declarations.getChildren().isEmpty()) {
            if ("FunctionDeclaration".equals(declarations.getChildren().getFirst().getSymbol())) {
                String functionName = declarations.getChildren().getFirst().getChildren().get(1).content();
                builder.withOperation(functionName, UmlTypeFactory.create(declarations.getChildren().getFirst().getChildren().get(4).content()), Member.Visibility.PRIVATE);
                methods.put(functionName, createBody(declarations.getChildren().getFirst().getChildren().get(7).getChildren().get(1)));
            }
            addPrivateFunctionOperations(builder, declarations.getChildren().getLast());
        }
    }

    private Type createType(PascalParser.Node typeDeclarationExpression) {
        final PascalParser.Node expression = typeDeclarationExpression.getChildren().getFirst();
        return switch (expression.getSymbol()) {
            case "TypeExpression" ->
                UmlTypeFactory.create(expression.getChildren().getFirst().content());
            case "RangeExpression" ->
                UmlTypeFactory.create(rangeString(expression));
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

    private static Type createEnumerationType(PascalParser.Node identifierList) {
        return UmlTypeFactory.create(createIdentifiers(identifierList).stream().collect(Collectors.joining(", ", "( ", " )")));
    }

    private static Type createArrayType(PascalParser.Node rangeExpression, PascalParser.Node typeExpression) {
        return UmlTypeFactory.create("ARRAY [" + rangeString(rangeExpression) + "] OF " + typeExpression.content());
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
        return UmlAttributeFactory.createPrivate(name, type, owner);
    }

    private void buildOperations(UmlClassBuilder builder, String programName, PascalParser.Node declarations) {
        builder.withOperation(programName, Member.Visibility.PUBLIC, createStereotypes("Main"));
        addPrivateFunctionOperations(builder, declarations);
    }

    private Collection<Transition<Event, GuardCondition, Action>> createBody(PascalParser.Node compoundStatement) {
        Collection<Transition<Event, GuardCondition, Action>> body = new ArrayList<>();
        Collection<TransitionSource> leaves = new ArrayList<>(List.of(UmlStateFactory.getInitialState()));
        createStatementSequence(compoundStatement, body, leaves);
        leaves.forEach(leave -> body.add(UmlTransitionFactory.createTransition(leave, UmlStateFactory.getFinalState())));
        return body;
    }

    private void createStatementSequence(PascalParser.Node statements, Collection<Transition<Event, GuardCondition, Action>> transitions, Collection<TransitionSource> leaves) {
        while (statements != null) {
            Statement statement = new Statement(statements.getChildren().getFirst());
            statement.getTransitions(transitions, leaves);
            statements = (statements.getChildren().size() > 1) ? statements.getChildren().getLast() : null;
        }
    }

    private ActionState<Action> createActionState(Statement statement) {
        return UmlStateFactory.createActionState(createAction(statement));
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
                Decision decision = UmlStateFactory.createDecision(createParseTreeExpression(expression.getChildren().get(1).getSymbol(), expression.getChildren().get(1).getChildren()));
                leaves.forEach(leave -> transitions.add(UmlTransitionFactory.createTransition(leave, decision)));
                leaves.clear();
                leaves.add(decision);
                createTransitions(expression.getChildren().get(3), transitions, leaves);
                addGuardCondition(transitions, transition -> decision.equals(transition.getSource()), UmlGuardConditionFactory.pass(decision), "then");
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
                leaves.forEach(leave -> transitions.add(UmlTransitionFactory.createTransition(leave, loopInitialization)));
                leaves.clear();
                Decision decision = UmlStateFactory.createDecision(createLessEqualExpression(identifier, expression.getChildren().get(5)));
                transitions.add(UmlTransitionFactory.createTransition(loopInitialization, decision));
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
                ActionState<Action> incrementActionState = UmlStateFactory.createActionState(incrementAction);
                leaves.forEach(leave -> transitions.add(UmlTransitionFactory.createTransition(leave, incrementActionState)));
                leaves.clear();
                addGuardCondition(transitions, transition -> decision.equals(transition.getSource()), UmlGuardConditionFactory.pass(decision), "for");
                transitions.add(UmlTransitionFactory.createTransition(incrementActionState, decision));
                leaves.add(decision);
            }
            else if ("WHILE\\b".equals(expression.getChildren().getFirst().getSymbol())) {
                Decision decision = UmlStateFactory.createDecision(createParseTreeExpression(expression.getChildren().get(1).getSymbol(), expression.getChildren().get(1).getChildren()));
                leaves.forEach(leave -> transitions.add(UmlTransitionFactory.createTransition(leave, decision)));
                leaves.clear();
                leaves.add(decision);
                createTransitions(expression.getChildren().get(3), transitions, leaves);
                addGuardCondition(transitions, transition -> decision.equals(transition.getSource()), UmlGuardConditionFactory.pass(decision), "while");
                leaves.forEach(leave -> transitions.add(UmlTransitionFactory.createTransition(leave, decision, createStereotypes("loop"))));
                leaves.clear();
                leaves.add(decision);
            }
            else if ("REPEAT\\b".equals(expression.getChildren().getFirst().getSymbol())) {
                TransitionSource loopRoot = leaves.stream().findAny().get();
                createTransitions(expression.getChildren().get(1), transitions, leaves);
                ParseTreeExpression condition = createParseTreeExpression(expression.getChildren().get(3).getSymbol(), expression.getChildren().get(3).getChildren());
                Decision decision = UmlStateFactory.createDecision(condition);
                leaves.forEach(leave -> transitions.add(UmlTransitionFactory.createTransition(leave, decision)));
                TransitionTarget loopStart = transitions.stream().filter(transition -> loopRoot.equals(transition.getSource())).findAny().get().getTarget();
                transitions.add(UmlTransitionFactory.createTransition(decision, loopStart, UmlGuardConditionFactory.fail(decision), createStereotypes("repeat")));
                leaves.clear();
                leaves.add(decision);
            }
            else if ("Assignable".equals(expression.getChildren().getFirst().getSymbol())) {
                ActionState<Action> assignment = createActionState(new Statement(expression.getChildren().getFirst(), expression.getChildren().get(2)));
                leaves.forEach(leave -> transitions.add(UmlTransitionFactory.createTransition(leave, assignment)));
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
            transitions.add(UmlTransitionFactory.copyTransition(transition, Optional.of(guardCondition), createStereotypes(stereotype)));
        }

        private void addStereotype(Collection<Transition<Event, GuardCondition, Action>> transitions, Predicate<Transition<Event, GuardCondition, Action>> predicate, String stereotype) {
            Transition<Event, GuardCondition, Action> transition = transitions.stream().filter(predicate).findAny().get();
            transitions.remove(transition);
            transitions.add(UmlTransitionFactory.copyTransition(transition, transition.getGuardCondition(), createStereotypes(stereotype)));
        }

        private void createTransitions(PascalParser.Node statements, Collection<Transition<Event, GuardCondition, Action>> transitions, Collection<TransitionSource> leaves) {
            if ("CompoundStatement".equals(statements.getChildren().getFirst().getSymbol())) {
                createStatementSequence(statements.getChildren().getFirst().getChildren().get(1), transitions, leaves);
            }
            else if ("Statements".equals(statements.getSymbol())) {
                createStatementSequence(statements, transitions, leaves);
            }
            else {
                new Statement(statements).getTransitions(transitions, leaves);
            }
        }

        private String typeOf(List<PascalParser.Node> expression) {
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

        private ParseTreeExpression getExpressionTree(PascalParser.Node expression) {
            if ("Expression".equals(expression.getSymbol())) {
                return createParseTreeExpression(expression.getSymbol(), expression.getChildren());
            }
            if ("Identifier".equals(expression.getSymbol())) {
                return createParseTreeExpression(expression.getSymbol(), expression.getChildren());
            }
            throw new IllegalStateException("Not an expression: " + expression);
        }

        private ParseTreeExpression createLessEqualExpression(PascalParser.Node leftOperand, PascalParser.Node rightOperand) {
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
                    return Value.of(() -> lessOrEqual(memory), "Boolean");
                }

                private java.lang.Object lessOrEqual(Memory memory) throws StateMachineException {
                    Value left = createParseTreeExpression(leftOperand.getSymbol(), leftOperand.getChildren()).evaluate(memory);
                    Value right = createParseTreeExpression(rightOperand.getSymbol(), rightOperand.getChildren()).evaluate(memory);
                    return ((Comparable) left.get()).compareTo((Comparable) right.get()) <= 0;
                }

                @Override
                public String toString() {
                    return value();
                }
            };
        }

        private ParseTreeExpression createParseTreeExpression(String symbol, List<PascalParser.Node> expression) {
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
                                    Value.of(() -> parseInteger(value()), "Integer");
                                case "'" ->
                                    Value.of(() -> value(), "String");
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
                            return Value.of(() -> invokeFunction(memory), "*");
                        }

                        private java.lang.Object invokeFunction(Memory memory) throws StateMachineException {
                            String functionName = value();
                            Optional<Collection<Transition<Event, GuardCondition, Action>>> method = findMethod(functionName);
                            if (method.isPresent()) {
                                return invoke(method.get(), functionName);
                            }
                            return memory.load(value());
                        }

                        private java.lang.Object invoke(Collection<Transition<Event, GuardCondition, Action>> method, String resultName) throws StateMachineException {
                            StateMachine stateMachine = new StateMachine(method);
                            stateMachine.start();
                            return stateMachine.getMemoryObject(resultName);
                        }

                        private Optional<Collection<Transition<Event, GuardCondition, Action>>> findMethod(String name) {
                            return methods.entrySet().stream()
                                .filter(entry -> name.equals(entry.getKey()))
                                .map(entry -> entry.getValue())
                                .findAny();
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
                            return Value.of(() -> memory.load(value()), "*");
                        }

                    };
                }
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
                        return getDyadicOperator(expression.get(1)).evaluate(
                            createParseTreeExpression(expression.getFirst().getSymbol(), expression.getFirst().getChildren()),
                            createParseTreeExpression(expression.getLast().getSymbol(), expression.getLast().getChildren()),
                            memory);
                    }
                };
            }
            throw new IllegalStateException("Invalid expression");
        }

        private java.lang.Object parseInteger(String literal) {
            return (literal.startsWith("$"))
                ? Integer.parseInt(literal, 1, literal.length(), 0x10)
                : Integer.valueOf(literal);
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

    private static DyadicOperator getDyadicOperator(PascalParser.Node node) {
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

    private static DyadicOperator createArithmicOperator(BiFunction<Number, Number, Number> function) {
        return (ParseTreeExpression leftOperand, ParseTreeExpression rightOperand, Memory memory) -> {
            Number value = function.apply(requireNumber(leftOperand, memory), requireNumber(rightOperand, memory));
            return Value.of(() -> value, (value instanceof Integer) ? "Integer" : "Real");
        };
    }

    private static Number requireNumber(ParseTreeExpression expression, Memory memory) throws StateMachineException {
        if (expression.evaluate(memory).get() instanceof Number number) {
            return number;
        }
        throw new StateMachineException(expression.type() + " is not a number");
    }

    private static DyadicOperator createLogicalOperator(BiFunction<Boolean, Boolean, Boolean> function) {
        return (ParseTreeExpression leftOperand, ParseTreeExpression rightOperand, Memory memory) -> {
            return Value.of(() -> function.apply(requireBoolean(leftOperand, memory), requireBoolean(rightOperand, memory)), "Boolean");
        };
    }

    private static Boolean requireBoolean(ParseTreeExpression expression, Memory memory) throws StateMachineException {
        if (expression.evaluate(memory).get() instanceof Boolean bool) {
            return bool;
        }
        throw new StateMachineException(expression.type() + " is not a boolean");
    }

    private static DyadicOperator createRelationalOperator(BiFunction<Comparable, Comparable, Boolean> function) {
        return (ParseTreeExpression leftOperand, ParseTreeExpression rightOperand, Memory memory) -> {
            return Value.of(() -> function.apply(requireComparable(leftOperand, memory), requireComparable(rightOperand, memory)), "Boolean");
        };
    }

    private static Comparable requireComparable(ParseTreeExpression expression, Memory memory) throws StateMachineException {
        if (expression.evaluate(memory).get() instanceof Comparable comparable) {
            return comparable;
        }
        throw new StateMachineException(expression.type() + " is not a comparable");
    }

    private final Map<String, Collection<Transition<Event, GuardCondition, Action>>> methods = new HashMap<>();

}
