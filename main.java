import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.stream.Collectors;

class LexerException extends RuntimeException {
	public LexerException(String message) {
		super(message);
	}
}

class Span {
	public long lineNum;
	public int posBegin, posEnd;

	public Span(long lineNum, int posBegin, int posEnd) {
		this.lineNum = lineNum;
		this.posBegin = posBegin;
		this.posEnd = posEnd;
	}

	public int getPosBegin() {
		return posBegin + 1;
	}

	public int getPosEnd() {
		return posEnd + 1;
	}
}

class Token {
	protected Span span;        // диапазон токена
	protected TokenCode code;   // код токена (из TokenCode)


	public Token(TokenCode code, Span span) {
		this.code = code;
		this.span = span;
	}

	public void print() {
		System.out.print(code + " ");
	}

	public void fullPrint() {
		System.out.println(code + " [Line:" + span.lineNum + " pos: " + span.getPosBegin() + ":" + span.getPosEnd() + "]");
	}

}


class Identifier extends Token {
	private String identifier;

	public Identifier(String identifier, Span span) {
		super(TokenCode.IDENTIFIER, span);
		this.identifier = identifier;
	}

	public void print() {
		System.out.print(code + "[" + this.identifier + "] ");
	}

	public void fullPrint() {
		System.out.println(code + " [Val: " + this.identifier + "]" + " [Line:" + span.lineNum + " pos: " + span.getPosBegin() + ":" + span.getPosEnd() + "]");
	}
}


class RealToken extends Token {
	private double value;

	public RealToken(double value, Span span) {
		super(TokenCode.REAL_LITERAL, span);
		this.value = value;
	}

	public void print() {
		System.out.print(code + "[" + this.value + "] ");
	}

	public void fullPrint() {
		System.out.println(code + " [Val: " + this.value + "]" + " [Line:" + span.lineNum + " pos: " + span.getPosBegin() + ":" + span.getPosEnd() + "]");
	}
}

class BooleanToken extends Token {
	private boolean value;

	public BooleanToken(boolean value, Span span) {
		super(TokenCode.BOOLEAN_LITERAL, span);
		this.value = value;
	}

	public void print() {
		System.out.print(code + "[" + this.value + "] ");
	}

	public void fullPrint() {
		System.out.println(code + " [Val: " + this.value + "]" + " [Line:" + span.lineNum + " pos: " + span.getPosBegin() + ":" + span.getPosEnd() + "]");
	}
}

class IntegerToken extends Token {
	private int value;

	public IntegerToken(int value, Span span) {
		super(TokenCode.INTEGER_LITERAL, span);
		this.value = value;
	}

	public void print() {
		System.out.print(code + "[" + this.value + "] ");
	}

	public void fullPrint() {
		System.out.println(code + " [Val: " + this.value + "]" + " [Line:" + span.lineNum + " pos: " + span.getPosBegin() + ":" + span.getPosEnd() + "]");
	}
}


class StringToken extends Token {
	private String value;

	public StringToken(String value, Span span) {
		super(TokenCode.STRING_LITERAL, span);
		this.value = value;
	}

	public void print() {
		System.out.print(code + "[" + this.value + "] ");
	}

	public void fullPrint() {
		System.out.println(code + " [Val: " + this.value + "]" + " [Line:" + span.lineNum + " pos: " + span.getPosBegin() + ":" + span.getPosEnd() + "]");
	}
}


enum TokenCode {
	// Ключевые слова
	VAR, IF, ELSE, WHILE, FOR, RETURN, PRINT, FUNC,
	LOOP, END, IS, THEN, IN, READ_INT, READ_REAL, READ_STRING, LENGTH,

	// Булевые литералы и специальное значение
	TRUE, FALSE, EMPTY,

	// Типы данных
	INT, REAL, BOOL, STRING,

	// Операторы
	ASSIGN, PLUS, MINUS, MULTIPLY, DIVIDE,
	AND, OR, XOR, NOT,
	LESS, LESS_EQUAL, GREATER, GREATER_EQUAL, EQUAL, NOT_EQUAL, IMPLICATION,

	// Специальные операторы
	DOT, LBRACKET, RBRACKET, LPAREN, RPAREN,TWO_DOT, QUOTE, DOUBLE_QUOTE,

	// Литералы
	INTEGER_LITERAL, REAL_LITERAL, STRING_LITERAL, BOOLEAN_LITERAL, TUPLE_LITERAL, ARRAY_LITERAL,

	// Разделители
	SEMICOLON, COMMA, LBRACE, RBRACE,

	// Идентификатор
	IDENTIFIER,

	// Конец файла
	EOF
}


class Lexer {
	private final String code; // Код который мы чекаем
	private int lineNum = 1; // На какой сейчас строчке стоит поинт (для span)
	private int currentCharNum = 0; // На каком индексе сейчас находимся
	List<Character> symbolList = Arrays.asList('(', ')', ',', '+', '/', '-', '=', ':', ';', '>', '<', '[', ']', '{', '}', '.');

	public Lexer (String code) {
		this.code = code;
	}

	private void spacesDelete() {
		while (this.currentCharNum < this.code.length() && Character.isWhitespace(this.code.charAt(this.currentCharNum))) {
			if (this.code.charAt(this.currentCharNum) == '\n') {
				lineNum++;
				System.out.println();
			}

			currentCharNum++;
		}
	}

	private void findWordEnd() {
		while(this.currentCharNum < this.code.length() && !Character.isWhitespace(this.code.charAt(this.currentCharNum)) && (!specSymbolCheck(this.currentCharNum))) {
			currentCharNum++;
		}
	}


	private boolean digitCheck(Span span) {
		for (int i = span.posBegin; i < span.posEnd; i++) {
			if (!(Character.isDigit(this.code.charAt(i)) || this.code.charAt(i) == '.' || this.code.charAt(i) == ',')) {
				return false;
			}
		}
		return true;
	}

	private int numberDelimCounter(Span span) {
		int delNum = 0;
		for (int i = span.posBegin; i < span.posEnd; i++) {
			if (this.code.charAt(i) == '.' || this.code.charAt(i) == ',') delNum++;
		}
		return delNum;
	}

	private Token scanNumber(Span span) {
		if (numberDelimCounter(span) > 1) {
			throw new LexerException("Invalid real number format at line " + this.lineNum);
		} else if (numberDelimCounter(span) == 1) {
			double value = Double.valueOf(this.code.substring(span.posBegin, span.posEnd).replace(',', '.'));
			return new RealToken(value, span);
		} else {
			int value = Integer.valueOf(this.code.substring(span.posBegin, span.posEnd));
			return new IntegerToken(value, span);
		}
	}

	private boolean specSymbolCheck(int num) {
		return symbolList.contains(this.code.charAt(num));
	}

	private Token stringTokenFind() {
		this.currentCharNum++;
		int startNum = this.currentCharNum;
		while (code.charAt(this.currentCharNum) != '"') {
			if (code.charAt(this.currentCharNum) == '\n') {
				lineNum++;
				System.out.println();
			}
			currentCharNum++;
		}
		String value = code.substring(startNum, currentCharNum);
		Span span = new Span(lineNum, startNum, currentCharNum);
		currentCharNum++;
		return new StringToken(value, span);
	}


	public Token tokenFind() {
		spacesDelete();

		if (this.currentCharNum >= code.length() ) {
			return new Token(TokenCode.EOF, new Span(lineNum, currentCharNum, currentCharNum));
		}

		int firstCharNum = currentCharNum;

		if (code.charAt(this.currentCharNum) == '"') {
			return stringTokenFind();
		}

		if (specSymbolCheck(this.currentCharNum)) {
			int offset = 1;
			if (this.currentCharNum+1 < code.length() && specSymbolCheck(this.currentCharNum+1)) offset++;
			TokenCode token = getKeywordTokenCode(this.code.substring(this.currentCharNum, this.currentCharNum+offset));
			if (token == TokenCode.IDENTIFIER) {
				offset = 1;
				token = getKeywordTokenCode(this.code.substring(this.currentCharNum, this.currentCharNum+offset));
			}
			currentCharNum += offset;
			return new Token(token, new Span(lineNum, this.currentCharNum, this.currentCharNum+offset));
		}

		findWordEnd();

		Span span = new Span(lineNum, firstCharNum, currentCharNum);

		if (digitCheck(span)) {
			return scanNumber(span);

		}
		String word = this.code.substring(span.posBegin, span.posEnd).toLowerCase();
		TokenCode token = getKeywordTokenCode(word);

		if (token == TokenCode.IDENTIFIER) {
			return new Identifier(this.code.substring(span.posBegin, span.posEnd), span);
		}

		if (token == TokenCode.TRUE) {
			return new BooleanToken(true, span);
		}

		if (token == TokenCode.FALSE) {
			return new BooleanToken(false, span);
		}

		return new Token(token, span);

	}

	public List<Token> start() {
		List<Token> tokenList = new ArrayList<>();
		while (true) {
			Token token = tokenFind();
			tokenList.add(token);
			token.print();
			//token.fullPrint();
			if (token.code == TokenCode.EOF) {
				return tokenList;
			}
		}
	}


	private TokenCode getKeywordTokenCode(String str) {
		if (str.length() == 1) {
			if (str.equals("<")){
				return TokenCode.LESS;
			}
			else if (str.equals(">")){
				return TokenCode.GREATER;
			}
			else if (str.equals("=")){
				return TokenCode.EQUAL;
			}
			else if (str.equals("*")){
				return TokenCode.MULTIPLY;
			}
			else if (str.equals("/")){
				return TokenCode.DIVIDE;
			}
			else if (str.equals("-")){
				return TokenCode.MINUS;
			}
			else if (str.equals("+")){
				return TokenCode.PLUS;
			}
			else if (str.equals("(")){
				return TokenCode.LPAREN;
			}
			else if (str.equals(")")){
				return TokenCode.RPAREN;
			}
			else if (str.equals("[")){
				return TokenCode.LBRACKET;
			}
			else if (str.equals("]")){
				return TokenCode.RBRACKET;
			}
			else if (str.equals("{")){
				return TokenCode.LBRACE;
			}
			else if (str.equals("}")){
				return TokenCode.RBRACE;
			}
			else if (str.equals(".")){
				return TokenCode.DOT;
			}
			else if (str.equals(",")){
				return TokenCode.COMMA;
			}
			else if (str.equals(";")){
				return TokenCode.SEMICOLON;
			}
			else if (str.equals("'")){
				return TokenCode.QUOTE;
			}
			else if (str.equals("\"")){
				return TokenCode.DOUBLE_QUOTE;
			}

		}
		if (str.length() == 2) {
			if (str.equals("<=")){
				return TokenCode.LESS_EQUAL;
			}
			else if (str.equals("..")){
				return TokenCode.TWO_DOT;
			}
			else if (str.equals(">=")){
				return TokenCode.GREATER_EQUAL;
			}
			else if (str.equals("=>")){
				return TokenCode.IMPLICATION;
			}
			else if (str.equals(":=")){
				return TokenCode.ASSIGN;
			}
			else if (str.equals("/=")){
				return TokenCode.NOT_EQUAL;
			}
			else if (str.equals("or")){
				return TokenCode.OR;
			}
			else if (str.equals("if")){
				return TokenCode.IF;
			}
			else if (str.equals("in")){
				return TokenCode.IN;
			}
			else if (str.equals("is")){
				return TokenCode.IS;
			}
		}
		if (str.length() == 3) {
			if (str.equals("and")) {
				return TokenCode.AND;
			}
			else if (str.equals("for")) {
				return TokenCode.FOR;
			}
			else if (str.equals("xor")) {
				return TokenCode.XOR;
			}
			else if (str.equals("not")) {
				return TokenCode.NOT;
			}
			else if (str.equals("int")) {
				return TokenCode.INT;
			}
			else if (str.equals("var")) {
				return TokenCode.VAR;
			}
			else if (str.equals("end")) {
				return TokenCode.END;
			}
		}
		if (str.length() == 4) {
			if (str.equals("bool")) {
				return TokenCode.BOOL;
			}
			else if (str.equals("real")) {
				return TokenCode.REAL;
			}
			else if (str.equals("true")) {
				return TokenCode.TRUE;
			}
			else if (str.equals("else")) {
				return TokenCode.ELSE;
			}
			else if (str.equals("func")) {
				return TokenCode.FUNC;
			}
			else if (str.equals("loop")) {
				return TokenCode.LOOP;
			}
			else if (str.equals("then")) {
				return TokenCode.THEN;
			}
		}
		if (str.length() == 5) {
			if (str.equals("false")) {
				return TokenCode.FALSE;
			}
			else if (str.equals("empty")) {
				return TokenCode.EMPTY;
			}
			else if (str.equals("print")) {
				return TokenCode.PRINT;
			}
			else if (str.equals("while")) {
				return TokenCode.WHILE;
			}
		}
		if (str.length() == 6) {
			if (str.equals("string")) {
				return TokenCode.STRING;
			}
			else if (str.equals("return")) {
				return TokenCode.RETURN;
			}
			else if (str.equals("length")) {
				return TokenCode.LENGTH;
			}
		}
		if (str.length() == 7) {
			if (str.equals("readInt")) {
				return TokenCode.READ_INT;
			}
		}
		if (str.length() == 8) {
			if (str.equals("readReal")) {
				return TokenCode.READ_REAL;
			}
		}
		if (str.length() == 10) {
			if (str.equals("readString")) {
				return TokenCode.READ_STRING;
			}
		}
		return TokenCode.IDENTIFIER;
	}

	public static void main(String[] args) {
		// Путь к файлу

		for (int i = 0; i <= 7; i++) {
			String filePath = "test" + i + ".d";

			System.out.println();
			System.out.println();
			System.out.println("-----------------  " + "test" + i + ".d" + "  --------------------");
			System.out.println();
			System.out.println();

			try {
				// Чтение содержимого файла в строку
				String str = new String(Files.readAllBytes(Paths.get(filePath)));

				// Передача строки в конструктор Lexer
				Lexer lexer = new Lexer(str);
				List<Token> tokenList = lexer.start();

				// Ваши действия с объектом Lexer

			} catch (IOException e) {
				// Обработка ошибки, если файл не найден или произошла ошибка ввода/вывода
				System.out.println("Ошибка чтения файла: " + e.getMessage());
			}
		}
	}
}


// Базовый класс для всех узлов AST
abstract class Node {
	// Список дочерних узлов
	protected List<Node> children = new ArrayList<>();

	// Добавление дочернего узла
	public void addChild(Node child) {
		children.add(child);
	}

	// Визуализация дерева в виде строки
	public abstract String toString();

	// Получение списка дочерних узлов
	public List<Node> getChildren() {
		return children;
	}
}

class ProgramNode extends Node {
	private final List<Node> statements;

	public ProgramNode() {
		this.statements = new ArrayList<>();
	}

	public void addStatement(Node statement) {
		this.statements.add(statement);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Program: \n");
		for (Node stmt : statements) {
			sb.append(stmt.toString()).append("\n");
		}
		return sb.toString();
	}
}

// Базовый класс для всех деклараций
abstract class DeclarationNode extends Node {
	// Тип декларации: переменная, функция, тип
	public enum DeclarationType {
		VARIABLE,
		FUNCTION
	}

	protected DeclarationType declarationType;

	public DeclarationType getDeclarationType() {
		return declarationType;
	}

	// Метод для печати узла (должен быть реализован в подклассах)
	@Override
	public abstract String toString();
}

class ExpressionNode extends Node {
	private final Node leftOperand;
	private final String operator;
	private final Node rightOperand;

	public ExpressionNode(Node leftOperand, String operator, Node rightOperand) {
		this.leftOperand = leftOperand;
		this.operator = operator;
		this.rightOperand = rightOperand;
	}

	@Override
	public String toString() {
		return leftOperand.toString() + " " + operator + " " + rightOperand.toString();
	}
}

class VariableDeclarationNode extends DeclarationNode {
	String variableName;
	ExpressionNode initializer; // Если переменной присваивается значение при объявлении

	VariableDeclarationNode(String variableName, String variableType, ExpressionNode initializer) {

        this.variableName = variableName;
		this.initializer = initializer;
	}

	@Override
	public String toString() {
		return variableName + (initializer != null ? " = " + initializer.toString() : "");
	}
}

class FunctionDeclarationNode extends DeclarationNode {
	String functionName;
	List<VariableDeclarationNode> parameters;
	Node functionBody;

	FunctionDeclarationNode(String functionName, List<VariableDeclarationNode> parameters, Node functionBody) {
		this.functionName = functionName;
		this.parameters = parameters;
		this.functionBody = functionBody;
	}

	@Override
	public String toString() {
		String params = parameters.stream().map(Object::toString).collect(Collectors.joining(", "));
		return functionName + "(" + params + ") " + functionBody.toString();
	}
}


class AssignmentNode extends Node {
	private final String variableName;
	private final Node expression;

	public AssignmentNode(String variableName, Node expression) {
		this.variableName = variableName;
		this.expression = expression;
	}

	@Override
	public String toString() {
		return variableName + " := " + expression.toString();
	}
}

abstract class StatementNode extends Node {
	private final String statementType;

	public StatementNode(String statementType) {
		this.statementType = statementType;
	}
	public String getStatementType() {
		return statementType;
	}
	@Override
	public String toString() {
		return "statement";
	}
}


class IfNode extends StatementNode {
	private final Node condition;
	private final Node thenBody;
	private final Node elseBody;

	public IfNode(Node condition, Node thenBody, Node elseBody) {
        super("if");
        this.condition = condition;
		this.thenBody = thenBody;
		this.elseBody = elseBody;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("If ").append(condition.toString()).append(" Then: \n");
		sb.append(thenBody.toString());
		if (elseBody != null) {
			sb.append("\nElse: ").append(elseBody.toString());
		}
		return sb.toString();
	}
}

class WhileLoopNode extends StatementNode {
	private final Node condition;
	private final Node body;

	public WhileLoopNode(Node condition, Node body) {
        super("while");
        this.condition = condition;
		this.body = body;
	}

	@Override
	public String toString() {
		return "While " + condition.toString() + " loop: \n" + body.toString();
	}
}

class ForLoopNode extends StatementNode {
	private final Node initialization; // Инициализация (например, i = 1)
	private final Node start;      // Условие (например, i <= arr.length)
	private final Node end;      // Условие (например, i <= arr.length)
	private final Node body;           // Тело цикла (например, print arr[i])

	public ForLoopNode(Node initialization, Node start, Node end, Node body) {
        super("for");
        this.initialization = initialization;
		this.start = start;
		this.end = end;
		this.body = body;
	}

	@Override
	public String toString() {
		return "For (" + initialization.toString() + "; " + start.toString() + "; " + end.toString() + ")";

	}

    public Node getBody() {
        return body;
    }
}

class ReturnNode extends StatementNode {
	private final Node expression;

	public ReturnNode(Node expression) {
        super("return");
        this.expression = expression;
	}

	@Override
	public String toString() {
		return "return " + (expression != null ? expression.toString() : "");
	}
}

class PrintNode extends StatementNode {
	private final Node expression;

	public PrintNode(Node expression) {
        super("print");
        this.expression = expression;
	}

	@Override
	public String toString() {
		return "print " + expression.toString();
	}
}


class LiteralNode extends Node {
	private final Object value;

	public LiteralNode(Object value) {
		this.value = value;
	}

	@Override
	public String toString() {
		return value.toString();
	}
}
