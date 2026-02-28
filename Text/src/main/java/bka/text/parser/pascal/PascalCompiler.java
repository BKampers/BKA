/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans, humanity, the environment or the universe.
*/

package bka.text.parser.pascal;

import bka.text.parser.Node;
import java.util.*;
import java.util.function.*;
import java.util.stream.*;
import run.*;
import uml.statechart.*;
import uml.structure.*;

/**
 * Creates an executable state machine from a pascal parse tree.
 */
public class PascalCompiler {

    public uml.structure.Class createProgramClass(Node node) {
        if (!"Program".equals(node.getSymbol())) {
            throw new IllegalArgumentException("Program keyword is missing");
        }
        String programName = identifier(node);
        UmlClassBuilder builder = new UmlClassBuilder(programName);
        Node declarationsNode = node.getChild("Declarations");
        createTypes(declarationsNode);
        addProgramVariables(builder, declarationsNode);
        buildOperations(builder, programName, declarationsNode);
        methodBodies.put(programName, createBody(node.getChild("CompoundStatement").getChild("Statements")));
        return builder.build();
    }

    public Collection<Transition<Event, GuardCondition, Action>> getMethod(Operation operation) {
        return Collections.unmodifiableCollection(methodBodies.get(operation.getName().get()));
    }

    public void createTypes(Node declarations) {
        declarations.findChild("TypeDeclaration")
            .ifPresent(typeDeclaration -> types.add(createType(typeDeclaration)));
        declarations.findChild("Declarations")
            .ifPresent(next -> createTypes(next));
    }

    private Type createType(Node typeDeclaration) {
        return createDeclaredType(identifier(typeDeclaration), typeDeclaration.getChild("TypeDeclarationExpression"), Member.Visibility.PUBLIC);
    }

    private void addProgramVariables(UmlClassBuilder builder, Node declarations) {
        declarations.findChild("VariableDeclaration")
            .ifPresent(declaration -> addVariables(builder, declaration));
        declarations.findChild("Declarations")
            .ifPresent(next -> addProgramVariables(builder, next));
    }

    private void addVariables(UmlClassBuilder builder, Node variableDeclaration) {
        createAttributes(builder, variableDeclaration.getChild("VariableDeclarationList"), Member.Visibility.PRIVATE);
    }

    private void createAttributes(UmlClassBuilder builder, Node variableDeclarationList, Member.Visibility visibility) {
        addAttributesFromExpression(builder, variableDeclarationList.getChild("VariableDeclarationExpression"), visibility);
        variableDeclarationList.findChild("VariableDeclarationList")
            .ifPresent(next -> createAttributes(builder, next, visibility));
    }

    private void addAttributesFromExpression(UmlClassBuilder builder, Node variableDeclarationExpression, Member.Visibility visibility) {
        Type type = createType(variableDeclarationExpression.getChild("TypeDeclarationExpression"), visibility);
        createIdentifiers(variableDeclarationExpression.getChild("IdentifierList"))
            .forEach(name -> builder.withAttribute(name, type, visibility));
    }

    private void addPrivateFunctionOperations(UmlClassBuilder builder, Node declarations) {
        declarations.findChild("ProcedureDeclaration")
            .ifPresent(addProcedureDeclaration(builder));
        declarations.findChild("FunctionDeclaration")
            .ifPresent(addFunctionDeclaration(builder));
        declarations.findChild("Declarations")
            .ifPresent(next -> addPrivateFunctionOperations(builder, next));
    }

    private Consumer<Node> addProcedureDeclaration(UmlClassBuilder builder) {
        return procedureDeclaration -> {
            String procedureName = identifier(procedureDeclaration);
            Map<Node, Parameter> parameters = createParameterList(procedureDeclaration.getChild("ParameterDeclaration"));
            builder.withOperation(
                procedureName,
                new ArrayList<>(parameters.values()),
                UmlTypeFactory.create("Void"),
                Member.Visibility.PRIVATE);
            methodBodies.put(procedureName, createBody(procedureDeclaration.getChild("CompoundStatement").getChild("Statements")));
            methodParameters.put(procedureName, new ArrayList<>(parameters.keySet()));
            methodTypes.put(procedureName, "Void");
            methodLocals.put(procedureName, createLocals(procedureDeclaration.getChild("Declarations")));
        };
    }

    private Collection<uml.structure.Object> createLocals(Node declarations) {
        if (declarations.getChildren().isEmpty()) {
            return Collections.emptyList();
        }
        return createVariables(declarations.getChild("VariableDeclaration").getChild("VariableDeclarationList"));
    }

    private static uml.structure.Object createObject(String name, Type type) {
        return new uml.structure.Object() {
            @Override
            public Optional<String> getName() {
                return Optional.of(name);
            }

            @Override
            public Optional<Type> getType() {
                return Optional.of(type);
            }

            @Override
            public Map<Attribute, Expression> getAttributeValues() {
                return Collections.emptyMap();
            }

        };
    }

    private Consumer<Node> addFunctionDeclaration(UmlClassBuilder builder) {
        return functionDeclaration -> {
            String functionName = identifier(functionDeclaration);
            Map<Node, Parameter> parameters = createParameterList(functionDeclaration.getChild("ParameterDeclaration"));
            builder.withOperation(
                functionName,
                new ArrayList<>(parameters.values()),
                UmlTypeFactory.create(functionDeclaration.getChild("TypeExpression").content()),
                Member.Visibility.PRIVATE);
            methodBodies.put(functionName, createBody(functionDeclaration.getChild("CompoundStatement").getChild("Statements")));
            methodParameters.put(functionName, new ArrayList<>(parameters.keySet()));
            methodTypes.put(functionName, functionDeclaration.getChild("TypeExpression").content());
            methodLocals.put(functionName, createLocals(functionDeclaration.getChild("Declarations")));
        };
    }

    private Map<Node, Parameter> createParameterList(Node parameterDeclaration) {
        if (parameterDeclaration.getChildren().isEmpty()) {
            return Collections.emptyMap();
        }
        Map<Node, Parameter> parameters = new LinkedHashMap<>();
        Node parameterList = parameterDeclaration.getChild("ParameterList");
        while (parameterList != null) {
            parameters.put(parameterList.getChild("ParameterExpression"), createParameter(parameterList));
            Optional<Node> remainder = parameterList.findChild("ParameterList");
            parameterList = remainder.orElse(null);
        }
        return parameters;
    }

    private Parameter createParameter(Node parameterList) {
        Node parameterExpression = parameterList.getChild("ParameterExpression");
        return new Parameter() {
            @Override
            public Parameter.Direction getDirection() {
                return (parameterExpression.findChild("VAR\\b").isPresent()) ? Parameter.Direction.INOUT : Parameter.Direction.IN;
            }

            @Override
            public Optional<Type> getType() {
                return Optional.of(createParameterType(parameterExpression.getChild("parameterTypeExpression")));
            }

            @Override
            public Optional<String> getName() {
                return Optional.of(identifier(parameterExpression));
            }
        };
    }

    private Type createParameterType(Node parameterTypeExpression) {
        if (parameterTypeExpression.findChild("ARRAY\\b").isPresent()) {
            return UmlTypeFactory.create(parameterTypeExpression.getChild("TypeExpression").getChildren().getFirst().content(), UmlMultiplicityFactory.createMultiplicity(0));
        }
        return UmlTypeFactory.create(parameterTypeExpression.getChild("TypeExpression").getChildren().getFirst().content());
    }

    private Type createType(Node typeDeclarationExpression, Member.Visibility visibility) {
        Node expression = typeDeclarationExpression.getChildren().getFirst();
        return switch (expression.getSymbol()) {
            case "TypeExpression" ->
                getType(expression);
            case "RangeExpression" ->
                UmlTypeFactory.create(rangeString(expression));
            case "\\(" ->
                createEnumerationType(typeDeclarationExpression.getChild("IdentifierList"));
            case "ARRAY\\b" ->
                createArrayType(null, typeDeclarationExpression.getChild("RangeExpression"), typeDeclarationExpression.getChild("TypeExpression"));
            case "RECORD\\b" ->
                createDeclaredType(null, typeDeclarationExpression, visibility);
            default ->
                throw new IllegalStateException("UnsupportedType " + typeDeclarationExpression.getChildren().getFirst().getSymbol());
        };
    }

    private Type getType(Node expression) {
        Node head = expression.getChildren().getFirst();
        return ("Identifier".equals(head.getSymbol()))
            ? types.stream().filter(typeNameEquals(head)).findAny().orElseThrow(() -> new IllegalStateException("No such type: " + head.content()))
            : UmlTypeFactory.create(head.content());
    }

    private static Predicate<Type> typeNameEquals(Node identifier) {
        return type -> type.getName().get().equalsIgnoreCase(identifier.content());
    }

    private static Type createEnumerationType(Node identifierList) {
        return UmlTypeFactory.create(createIdentifiers(identifierList).stream().collect(Collectors.joining(", ", "( ", " )")));
    }

    private static int intValue(Node node) {
        return switch (node.getSymbol()) {
            case "IntegerLiteral" ->
                Integer.parseInt(node.content());
            case "Identifier" ->
                throw new IllegalStateException("Const not supported yet"); // TODO evaluate consts fitrst
            default ->
                throw new IllegalStateException(node.content() + " is cannot be evaluated to an integer.");
        };
    }

    private Type createDeclaredType(String name, Node typeDeclarationExpression, Member.Visibility visibility) {
        if (typeDeclarationExpression.findChild("VariableDeclarationList").isPresent()) {
            UmlClassBuilder builder = (name == null) ? new UmlClassBuilder() : new UmlClassBuilder(name);
            addRecordFields(builder, typeDeclarationExpression.getChild("VariableDeclarationList"), visibility);
            return builder.build();
        }
        if (typeDeclarationExpression.startsWith("ARRAY\\b")) {
            return createArrayType(name, typeDeclarationExpression.getChild("RangeExpression"), typeDeclarationExpression.getChild("TypeExpression"));
        }
        throw new IllegalStateException("Invalid type declaration expression");
    }

    private static Type createArrayType(String identifier, Node rangeExpression, Node typeExpression) {
        return (identifier == null)
            ? UmlTypeFactory.create(createMultiplicity(rangeExpression))
            : UmlTypeFactory.create(identifier, createMultiplicity(rangeExpression));
    }

    private static Multiplicity createMultiplicity(Node rangeExpression) {
        return UmlMultiplicityFactory.createMultiplicity(intValue(rangeExpression.getChildren().getFirst()), intValue(rangeExpression.getChildren().getLast()));
    }

    private void addRecordFields(UmlClassBuilder builder, Node variableDeclarationList, Member.Visibility visibility) {
        addRecordField(builder, variableDeclarationList.getChild("VariableDeclarationExpression"), visibility);
        variableDeclarationList.findChild("VariableDeclarationList")
            .ifPresent(next -> addRecordFields(builder, next, visibility));
    }

    private void addRecordField(UmlClassBuilder builder, Node variableDeclarationExpression, Member.Visibility visibility) {
        Type type = createType(variableDeclarationExpression.getChild("TypeDeclarationExpression"), visibility);
        createIdentifiers(variableDeclarationExpression.getChild("IdentifierList"))
            .forEach(name -> builder.withAttribute(name, type, visibility));
    }

    private Collection<uml.structure.Object> createVariables(Node variableDeclarationList) {
        Collection<uml.structure.Object> variables = new ArrayList<>();
        variables.addAll(createObjects(variableDeclarationList.getChild("VariableDeclarationExpression")));
        variableDeclarationList.findChild("VariableDeclarationList")
            .ifPresent(next -> variables.addAll(createVariables(next)));
        return variables;
    }

    private Collection<uml.structure.Object> createObjects(Node variableDeclarationExpression) {
        Type type = createType(variableDeclarationExpression.getChild("TypeDeclarationExpression"), Member.Visibility.PRIVATE);
        return createIdentifiers(variableDeclarationExpression.getChild("IdentifierList")).stream()
            .map(identifier -> createObject(identifier, type))
            .collect(Collectors.toList());
    }

    private static String rangeString(Node rangeExpression) {
        return rangeExpression.getChildren().getFirst().content() + " .. " + rangeExpression.getChildren().getLast().content();
    }

    private static List<String> createIdentifiers(Node identifierList) {
        List<String> identifiers = new ArrayList<>();
        identifiers.add(identifier(identifierList));
        identifierList.findChild("IdentifierList")
            .ifPresent(next -> identifiers.addAll(createIdentifiers(next)));
        return identifiers;
    }

    private void buildOperations(UmlClassBuilder builder, String programName, Node declarations) {
        builder.withOperation(programName, Member.Visibility.PUBLIC, UmlStereotypeFactory.createStereotypes("Main"));
        addPrivateFunctionOperations(builder, declarations);
    }

    private Collection<Transition<Event, GuardCondition, Action>> createBody(Node compoundStatement) {
        ActivityDiagramBuilder diagram = new ActivityDiagramBuilder();
        createStatementSequence(compoundStatement, diagram);
        diagram.addFinalState();
        return diagram.getTransitions();
    }

    private void createStatementSequence(Node statements, ActivityDiagramBuilder diagram) {
        Optional<Node> statementNode = Optional.of(statements);
        while (statementNode.isPresent()) {
            Statement statement = new Statement(statementNode.get().getChild("Statement"), new MethodProperties());
            statement.createTransitions(diagram);
            statementNode = statementNode.get().findChild("Statements");
        }
    }

    private static String identifier(Node node) {
        return node.getChild("Identifier").content().toLowerCase();
    }

    public class MethodProperties {

        public Collection<Transition<Event, GuardCondition, Action>> getBody(String name) {
            return methodBodies.get(name);
        }

        public List<Node> getParameters(String name) {
            return Collections.unmodifiableList(methodParameters.get(name));
        }

        public String getType(String name) {
            return methodTypes.get(name);
        }

        public Collection<uml.structure.Object> getLocals(String name) {
            return Collections.unmodifiableCollection(methodLocals.get(name));
        }

    }

    private final Map<String, Collection<Transition<Event, GuardCondition, Action>>> methodBodies = new HashMap<>();
    private final Map<String, List<Node>> methodParameters = new HashMap<>();
    private final Map<String, Collection<uml.structure.Object>> methodLocals = new HashMap<>();
    private final Map<String, String> methodTypes = new HashMap<>();
    private final Collection<Type> types = new ArrayList<>();

}
