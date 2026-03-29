/*
** © Bart Kampers
** This code may not be used for any purpose that harms humans, humanity, the environment or the universe.
*/

package bka.text.parser.pascal;

import bka.text.parser.*;
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

    public static final Type STRING = UmlTypeFactory.create("string");
    public static final Type CHAR = UmlTypeFactory.create("char");
    public static final Type REAL = UmlTypeFactory.create("real");
    public static final Type INTEGER = UmlTypeFactory.create("integer");
    public static final Type BOOLEAN = UmlTypeFactory.create("boolean");

    public PascalCompiler() {
        declaredTypes.add(BOOLEAN);
        declaredTypes.add(INTEGER);
        declaredTypes.add(REAL);
        declaredTypes.add(CHAR);
        declaredTypes.add(STRING);
    }

    public uml.structure.Class createProgramClass(Node node) {
        if (!"Program".equals(node.getSymbol())) {
            throw new IllegalArgumentException("Program keyword is missing");
        }
        String programName = identifier(node);
        UmlClassBuilder builder = new UmlClassBuilder(programName);
        Node declarationsNode = node.getChild("Declarations");
        createTypes(declarationsNode);
        addProgramVariables(builder, declarationsNode);
        buildOperations(builder, declarationsNode);
        Operation mainOperation = builder.withMainOperation();
        methodBodies.put(mainOperation, createBody(mainOperation, node.getChild("CompoundStatement").getChild("Statements")));
        return builder.build();
    }

    public Collection<Transition<Event, GuardCondition, Action>> getMethod(Operation operation) {
        return Collections.unmodifiableCollection(methodBodies.get(operation));
    }

    public void createTypes(Node declarations) {
        declarations.findChild("TypeDeclaration")
            .ifPresent(typeDeclaration -> declaredTypes.add(createType(typeDeclaration)));
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
            .forEach(name -> {
                builder.withAttribute(name, type, visibility);
                globals.add(UmlObjectFactory.create(name, type));
            });
    }

    private void addPrivateFunctionOperations(UmlClassBuilder builder, Node declarations) {
        declarations.findChild("ProcedureDeclaration")
            .ifPresent(procedureDeclaration -> addMethod(builder, VOID_TYPE, procedureDeclaration));
        declarations.findChild("FunctionDeclaration")
            .ifPresent(functionDeclaration -> addMethod(builder, getType(functionDeclaration.getChild("TypeExpression")), functionDeclaration));
        declarations.findChild("Declarations")
            .ifPresent(next -> addPrivateFunctionOperations(builder, next));
    }

    private void addMethod(UmlClassBuilder builder, Type type, Node declaration) {
        String methodName = identifier(declaration);
        List<Parameter> parameters = createParameterList(declaration.getChild("ParameterDeclaration"));
        Operation operation = builder.withOperation(methodName, parameters, type, Member.Visibility.PRIVATE);
        methodParameters.put(operation, parameters);
        methodLocals.put(operation, createLocals(declaration.getChild("Declarations")));
        methodBodies.put(operation, createBody(operation, declaration.getChild("CompoundStatement").getChild("Statements")));

    }


    private Collection<uml.structure.Object> createLocals(Node declarations) {
        if (declarations.getChildren().isEmpty()) {
            return Collections.emptyList();
        }
        return createVariables(declarations.getChild("VariableDeclaration").getChild("VariableDeclarationList"));
    }

    private List<Parameter> createParameterList(Node parameterDeclaration) {
        if (parameterDeclaration.getChildren().isEmpty()) {
            return Collections.emptyList();
        }
        List<Parameter> parameters = new ArrayList<>();
        Node parameterList = parameterDeclaration.getChild("ParameterList");
        do {
            parameters.add(createParameter(parameterList));
            parameterList = parameterList.findChild("ParameterList").orElse(null);
        } while (parameterList != null);
        return parameters;
    }

    private Parameter createParameter(Node parameterList) {
        Node parameterExpression = parameterList.getChild("ParameterExpression");
        return UmlParameterFactory.create(
            (parameterExpression.findChild("VAR\\b").isPresent()) ? Parameter.Direction.INOUT : Parameter.Direction.IN,
            createParameterType(parameterExpression.getChild("ParameterTypeExpression")),
            identifier(parameterExpression));
    }

    private Type createParameterType(Node parameterTypeExpression) {
        if (parameterTypeExpression.findChild("ARRAY\\b").isPresent()) {
            return new ArrayType(createType(parameterTypeExpression.getChild("TypeExpression")), 0, 0);
        }
        return getType(parameterTypeExpression.getChild("TypeExpression"));
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
                createArrayType(null, typeDeclarationExpression.getChild("RangeExpression"), typeDeclarationExpression.getChild("TypeDeclarationExpression"));
            case "RECORD\\b" ->
                createDeclaredType(null, typeDeclarationExpression, visibility);
            default ->
                throw new IllegalStateException("UnsupportedType " + typeDeclarationExpression.getChildren().getFirst().getSymbol());
        };
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
        if (typeDeclarationExpression.startsWith("RECORD\\b")) {
            UmlClassBuilder builder = (name == null) ? new UmlClassBuilder() : new UmlClassBuilder(name);
            addRecordFields(builder, typeDeclarationExpression.getChild("VariableDeclarationList"), visibility);
            return builder.build();
        }
        if (typeDeclarationExpression.startsWith("ARRAY\\b")) {
            return createArrayType(name, typeDeclarationExpression.getChild("RangeExpression"), typeDeclarationExpression.getChild("TypeDeclarationExpression"));
        }
        if (typeDeclarationExpression.startsWith("TypeExpression")) {
            return getType(typeDeclarationExpression.getChild("TypeExpression"));
        }
        throw new IllegalStateException("Invalid type declaration expression: " + typeDeclarationExpression.content());
    }

    private Type getType(Node typeExpression) {
        return declaredTypes.stream()
            .filter(typeNameEquals(typeExpression))
            .findAny()
            .orElseThrow(() -> new IllegalStateException("No such type: " + typeExpression.content()));
    }

    private Type createArrayType(String identifier, Node rangeExpression, Node typeExpression) {
        if (identifier == null) {
            return new ArrayType(
                createDeclaredType(null, typeExpression, Member.Visibility.PUBLIC),
                intValue(rangeExpression.getChildren().getFirst()),
                intValue(rangeExpression.getChildren().getLast()));
        }
        return new ArrayType(
            identifier,
            createDeclaredType(null, typeExpression, Member.Visibility.PUBLIC),
            intValue(rangeExpression.getChildren().getFirst()),
            intValue(rangeExpression.getChildren().getLast()));
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
            .map(identifier -> UmlObjectFactory.create(identifier, type))
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

    private void buildOperations(UmlClassBuilder builder, Node declarations) {
//        builder.withOperation(programName, Member.Visibility.PUBLIC, UmlStereotypeFactory.createStereotypes("Main"));
        addPrivateFunctionOperations(builder, declarations);
    }

    private Collection<Transition<Event, GuardCondition, Action>> createBody(Operation operation, Node compoundStatement) {
        ActivityDiagramBuilder diagram = new ActivityDiagramBuilder();
        createStatementSequence(operation, compoundStatement, diagram);
        diagram.addFinalState();
        return diagram.getTransitions();
    }

    private void createStatementSequence(Operation operation, Node statements, ActivityDiagramBuilder diagram) {
        Node statementNode = statements;
        do {
            Statement statement = new Statement(statementNode.getChild("Statement"), new MethodProperties(operation));
            statement.createTransitions(diagram);
            statementNode = statementNode.findChild("Statements").orElse(null);
        } while (statementNode != null);
    }

    private Type typeOf(Operation operation, String identifier, List<Node> indirections) throws IllegalStateException {
        Type type = null;
        if (operation.getName().isPresent() && identifier.equalsIgnoreCase(operation.getName().get())) {
            type = operation.getType().get();
        }
        if (type == null && methodParameters.containsKey(operation)) {
            Optional<Parameter> parameter = methodParameters.get(operation).stream()
                .filter(p -> identifier.equalsIgnoreCase(p.getName().get()))
                .findAny();
            if (parameter.isPresent()) {
                type = parameter.get().getType().get();
            }
        }
        if (type == null && methodLocals.containsKey(operation)) {
            Optional<uml.structure.Object> local = methodLocals.get(operation).stream()
                .filter(object -> object.getName().get().equalsIgnoreCase(identifier))
                .findAny();
            if (local.isPresent()) {
                type = local.get().getType().get();
            }
        }
        if (type == null) {
            type = globals.stream()
                .filter(global -> global.getName().get().equalsIgnoreCase(identifier))
                .map(global -> global.getType().get())
                .findAny().get();
        }
        for (Node indirection : indirections) {
            type = switch (indirection.getSymbol()) {
                case "Identifier" ->
                    ((uml.structure.Class) type).getAttributes().stream().filter(attribute -> attribute.getName().get().equalsIgnoreCase(indirection.content())).findAny().get().getType().get();
                case "Expression" ->
                    ((ArrayType) type).getElementType();
                default ->
                    throw new IllegalStateException("Invalid Indirection");
            };
        }
        return type;
    }

    // TODO refactor, this method is copied from Statement
    private static List<Node> getIndirections(Node expression) {
        List<Node> indirections = new ArrayList<>();
        Optional<Node> next = expression.findChild("Indirection");
        while (next.isPresent()) {
            Node indirection = next.get();
            if (indirection.getChildren().isEmpty()) {
                next = Optional.empty();
            }
            else {
                if (indirection.startsWith("\\.")) {
                    indirections.add(indirection.getChild("Identifier"));
                }
                else if (indirection.startsWith("\\[")) {
                    indirections.add(indirection.getChild("Expression"));
                }
                else {
                    throw new IllegalStateException("Invalid indirection");
                }
                next = indirection.findChild("Indirection");
            }
        }
        return indirections;
    }


    private static String identifier(Node node) {
        return node.getChild("Identifier").content().toLowerCase();
    }


    public class MethodProperties {

        private MethodProperties(Operation scope) {
            this.scope = scope;
        }

        public Type determineTypeOf(Node expression) {
            return typeOf(scope, identifier(expression), getIndirections(expression));
        }

        public Type determineTypeOf(String identifier) {
            return typeOf(scope, identifier, Collections.emptyList());
        }

        public Collection<Transition<Event, GuardCondition, Action>> getBody(String name) {
            return methodBodies.get(getOperation(name));
        }

        public List<Parameter> getParameters(String name) {
            return Collections.unmodifiableList(methodParameters.get(getOperation(name)));
        }

        public Type getType(String name) {
            return getOperation(name).getType().get();
        }

        public Collection<uml.structure.Object> getLocals(String name) {
            return Collections.unmodifiableCollection(methodLocals.get(getOperation(name)));
        }

        public boolean isProcedure(String name) {
            return VOID_TYPE.equals(getOperation(name).getType().get());
        }

        private Operation getOperation(String name) {
            return methodBodies.keySet().stream()
                .filter(operation -> operation.getName().isPresent())
                .filter(operation -> name.equalsIgnoreCase(operation.getName().get()))
                .findAny().orElse(null);
        }

        private final Operation scope;

    }

    private final Collection<uml.structure.Object> globals = new ArrayList<>();
    private final Map<Operation, Collection<Transition<Event, GuardCondition, Action>>> methodBodies = new HashMap<>();
    private final Map<Operation, List<Parameter>> methodParameters = new HashMap<>();
    private final Map<Operation, Collection<uml.structure.Object>> methodLocals = new HashMap<>();
    private final Collection<Type> declaredTypes = new ArrayList<>();

    private static final Type VOID_TYPE = UmlTypeFactory.create("void");

}
