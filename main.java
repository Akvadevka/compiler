class Span {
	public long lineNum;
	public int posBegin, posEnd;

	public Span(long lineNum, int posBegin, int posEnd) {
		this.lineNum = lineNum;
		this.posBegin = posBegin;
		this.posEnd = posEnd;
	}
}

class Token {
	protected Span span;        // диапазон токена
	protected TokenCode code;   // код токена (из TokenCode)


	public Token(TokenCode code, Span span) {
		this.code = code;
		this.span = span;
	}

}


class Identifier extends Token {
	private String identifier;

	public Identifier(String identifier, Span span) {
		super(TokenCode.IDENTIFIER, span);
		this.identifier = identifier;
	}
}


class RealToken extends Token {
	private double value;

	public RealToken(double value, Span span) {
		super(TokenCode.REAL_LITERAL, span);
		this.value = value;
	}
}

class BooleanToken extends Token {
	private boolean value;

	public BooleanToken(boolean value, Span span) {
		super(TokenCode.BOOLEAN_LITERAL, span);
		this.value = value;
	}
}

class IntegerToken extends Token {
	private int value;

	public IntegerToken(int value, Span span) {
		super(TokenCode.INTEGER_LITERAL, span);
		this.value = value;
	}
}


class StringToken extends Token {
	private String value;

	public StringToken(String value, Span span) {
		super(TokenCode.STRING_LITERAL, span);
		this.value = value;
	}
}


enum TokenCode {
	// Ключевые слова
	VAR, IF, ELSE, WHILE, FOR, RETURN, PRINT, FUNC, LOOP, END, IS, THEN, IN, READ_INT, READ_REAL, READ_STRING,

	// Булевые литералы и специальное значение
	TRUE, FALSE, EMPTY,

	// Типы данных
	INT, REAL, BOOL, STRING, VECTOR_TYPE, TUPLE_TYPE, FUNCTION_TYPE,

	// Операторы
	ASSIGN, PLUS, MINUS, MULTIPLY, DIVIDE,
	AND, OR, XOR, NOT,
	LESS, LESS_EQUAL, GREATER, GREATER_EQUAL, EQUAL, NOT_EQUAL,

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

	private TokenCode getKeywordTokenCode(String str) {
		if (str.length() == 1) {
			if (str.equals("<")){
				return TokenCode.LESS
			}
			else if (str.equals(">")){
				return TokenCode.GREATER
			}
			else if (str.equals(">")){
				return TokenCode.GREATER
			}
			else if (str.equals("=")){
				return TokenCode.EQUAL
			}
			else if (str.equals("*")){
				return TokenCode.MULTIPLY
			}
			else if (str.equals("/")){
				return TokenCode.DIVIDE
			}
			else if (str.equals("-")){
				return TokenCode.MINUS
			}
			else if (str.equals("+")){
				return TokenCode.PLUS
			}
			else if (str.equals("(")){
				return TokenCode.LPAREN
			}
			else if (str.equals(")")){
				return TokenCode.RPAREN
			}
			else if (str.equals("[")){
				return TokenCode.LBRACKET
			}
			else if (str.equals("]")){
				return TokenCode.RBRACKET
			}
			else if (str.equals("{")){
				return TokenCode.LBRACE
			}
			else if (str.equals("}")){
				return TokenCode.RBRACE
			}
			else if (str.equals(".")){
				return TokenCode.DOT
			}
			else if (str.equals("..")){
				return TokenCode.TWO_DOT
			}
			else if (str.equals(",")){
				return TokenCode.COMMA
     		}
			else if (str.equals(";")){
				return TokenCode.SEMICOLON
			}
			else if (str.equals("'")){
				return TokenCode.QUOTE
			}
			else if (str.equals('"')){
				return TokenCode.DOUBLE_QUOTE
			}

		}
		if (str.length() == 2) {
			if (str.equals("<=")){
				return TokenCode.LESS_EQUAL
			}
			else if (str.equals(">=")){
				return TokenCode.GREATER_EQUAL
			}
			else if (str.equals(":=")){
				return TokenCode.ASSIGN
			}
			else if (str.equals("/=")){
				return TokenCode.NOT_EQUAL
			}
			else if (str.equals("or")){
				return TokenCode.OR
			}
			else if (str.equals("if")){
				return TokenCode.IF
			}
			else if (str.equals("in")){
				return TokenCode.IN
			}
			else if (str.equals("is")){
				return TokenCode.IS
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

}