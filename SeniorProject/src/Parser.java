/*
Author:
Aamir Ibrahim
*/
import java.io.FileNotFoundException;

public class Parser {
	private LexicalAnalyzer lex;
	private int incrementingValue = 0;
	// fileName cannot be null - checked in LexicalAnalyzer
	// throws FileNotFoundException if file cannot be found
	// throws LexicalException
	// postcondition: parser object has been created
	public Parser (String fileName) throws FileNotFoundException, LexicalException {
		lex = new LexicalAnalyzer(fileName);
	}
	// return Program object containing an intermediate representation of the program
	// throws ParserException if a parsing error occurred
	public Program parse () throws ParserException {
		Token tok = getNextToken();
		match (tok, TokenType.FUNCTION_TOK);
		tok = getNextToken();
		match (tok, TokenType.CLASS_TOK);
		Id functionName = getId();
		tok = getNextToken ();
		match (tok, TokenType.LEFT_BRACKET_TOK);
		tok = getNextToken();
		match(tok, TokenType.FUNCTION_TOK);
		tok = getNextToken();
		match(tok, TokenType.STATIC_TOK);
		tok = getNextToken();
		match(tok, TokenType.VOID_TOK);
		tok = getNextToken();
		match(tok, TokenType.MAIN_TOK);
		tok = getNextToken();
		match(tok, TokenType.LEFT_PAREN_TOK);
		tok = getNextToken();
		match(tok, TokenType.STRING_ARR_TOK);
		tok = getNextToken();
		match(tok, TokenType.ARGS_TOK);
		tok = getNextToken();
		match(tok, TokenType.RIGHT_PAREN_TOK);
		tok = getNextToken();
		match (tok, TokenType.LEFT_BRACKET_TOK);

		Block blk = getBlock();

		tok = getNextToken ();
		match (tok, TokenType.RIGHT_BRACKET_TOK);
		tok = getNextToken ();
		match (tok, TokenType.RIGHT_BRACKET_TOK);
		tok = getNextToken();
		if (tok.getTokType() != TokenType.EOS_TOK)
			throw new ParserException ("garbage at end of file");
		return new Program (blk);
	}
	// return Block object
	// throws ParserException if a parsing error occurred
	private Block getBlock() throws ParserException {
		Block blk = new Block();
		Token tok = getLookaheadToken();
		while (isValidStartOfStatement (tok))
		{
			Statement stmt = getStatement();
			blk.add (stmt);
			tok = getLookaheadToken();

		}
		return blk;
	}

	// return statement object
	// throws ParserException if a parsing error occurred
	private Statement getStatement() throws ParserException {
		Statement stmt;
		Token tok = getLookaheadToken();
		if (tok.getTokType() == TokenType.IF_TOK)
			stmt = getIfStatement();
		else if (tok.getTokType() == TokenType.WHILE_TOK)
			stmt = getWhileStatement();
		else if (tok.getTokType() == TokenType.PRINT_TOK)
			stmt = getPrintStatement();
		else if (tok.getTokType() == TokenType.REPEAT_TOK)
			stmt = getRepeatStatement();
		else if (tok.getTokType() == TokenType.INTEGER_TOK || tok.getTokType() == TokenType.ID_TOK)
			stmt = getAssignmentStatement();
		else 
			throw new ParserException ("invalid statement at row " +
				tok.getRowNumber()  + " and column " + tok.getColumnNumber());
		return stmt;
	}
	// return assignment statement
	// throws ParserException if a parsing error occurred
	private Statement getAssignmentStatement() throws ParserException {
		Id var = getId();
		Token tok = getNextToken();
		match (tok, TokenType.ASSIGN_TOK);
		ArithmeticExpression expr = getArithmeticExpression();
		return new AssignmentStatement (var, expr);
	}
	// return repeat statement
	// throws ParserException if a parsing error occurred
	private Statement getRepeatStatement() throws ParserException {
		Token tok = getNextToken();
		match (tok, TokenType.REPEAT_TOK);
		Block blk = getBlock();
		tok = getNextToken();
		match (tok, TokenType.UNTIL_TOK);
		BooleanExpression expr = getBooleanExpression();
		return new RepeatStatement (blk, expr);
	}

	// return print statement
	// throws ParserException if a parsing error occurred
	private Statement getPrintStatement() throws ParserException {
		Token tok = getNextToken();
		match (tok, TokenType.PRINT_TOK);
		tok = getNextToken ();
		match (tok, TokenType.LEFT_PAREN_TOK);
		ArithmeticExpression expr = getArithmeticExpression();
		tok = getNextToken ();
		match (tok, TokenType.RIGHT_PAREN_TOK);
		return new PrintStatement (expr);
	}
	// return while statement
	// throws ParserException if a parsing error occurred
	private Statement getWhileStatement() throws ParserException {
		Token tok = getNextToken ();
		match (tok, TokenType.WHILE_TOK);
		tok = getNextToken();
		match (tok, TokenType.LEFT_PAREN_TOK);
		BooleanExpression expr = getBooleanExpression();
		tok = getNextToken ();
		match (tok, TokenType.RIGHT_PAREN_TOK);
		tok = getNextToken ();
		match (tok, TokenType.LEFT_BRACKET_TOK);
		Block blk = getBlock();
		tok = getNextToken();
		match (tok, TokenType.RIGHT_BRACKET_TOK);
		return new WhileStatement (expr, blk);
	}
	// return if statement
	// throws ParserException if a parsing error occurred
	private Statement getIfStatement() throws ParserException {
		Token tok = getNextToken ();
		match (tok, TokenType.IF_TOK);
		tok = getNextToken ();
		match (tok, TokenType.LEFT_PAREN_TOK);
		BooleanExpression expr = getBooleanExpression();
		tok = getNextToken ();
		match (tok, TokenType.RIGHT_PAREN_TOK);
		tok = getNextToken ();
		match (tok, TokenType.LEFT_BRACKET_TOK);
		Block blk1 = getBlock();
		tok = getNextToken ();
		match (tok, TokenType.RIGHT_BRACKET_TOK);
		tok = getNextToken ();
		match (tok, TokenType.ELSE_TOK);
		tok = getNextToken ();
		match (tok, TokenType.LEFT_BRACKET_TOK);
		Block blk2 = getBlock();
		tok = getNextToken();
		match (tok, TokenType.RIGHT_BRACKET_TOK);
		return new IfStatement (expr, blk1, blk2);
	}

	// tok cannot be null - checked with assertion
	// return whether tok can be the start of a statement
	private boolean isValidStartOfStatement(Token tok) {
		assert (tok != null);
		return tok.getTokType() == TokenType.INTEGER_TOK ||
			tok.getTokType() == TokenType.IF_TOK ||
			tok.getTokType() == TokenType.WHILE_TOK ||
			tok.getTokType() == TokenType.PRINT_TOK ||
			tok.getTokType() == TokenType.REPEAT_TOK ||
			tok.getTokType() == TokenType.ID_TOK;
	}

	// return arithmetic expression
	// throws ParserException if a parsing error occurred
	private ArithmeticExpression getArithmeticExpression() throws ParserException {
		ArithmeticExpression expr;
		Token tok = getLookaheadToken();
		Token tok2 = getNextValueToken();
		if (tok.getTokType() == TokenType.ID_TOK) {
			if (incrementingValue == 0 && tok2.getTokType() == TokenType.ADD_TOK || tok2.getTokType() == TokenType.MUL_TOK && incrementingValue == 0){
				incrementingValue++;
				expr = getBinaryExpression();
			}
			else {
				expr = getId();
			}
		}
		else if (tok.getTokType() == TokenType.LITERAL_INTEGER_TOK)
			expr = getLiteralInteger();
		else
			expr = getBinaryExpression();
		return expr;
	}

	// return binary expression
	// throws ParserException if a parsing error occurred
	private BinaryExpression getBinaryExpression() throws ParserException {
		ArithmeticExpression expr1 = getArithmeticExpression();
		BinaryExpression.ArithmeticOperator op = getArithmeticOperator();
		ArithmeticExpression expr2 = getArithmeticExpression();
		return new BinaryExpression (op, expr1, expr2);
	}
	// return arithmetic operator
	// throws ParserException if a parsing error occurred
	private BinaryExpression.ArithmeticOperator getArithmeticOperator() throws ParserException {
		BinaryExpression.ArithmeticOperator op;
		Token tok = getNextToken();
		if (tok.getTokType() == TokenType.ADD_TOK)
			op = BinaryExpression.ArithmeticOperator.ADD_OP;
		else if (tok.getTokType() == TokenType.SUB_TOK)
			op = BinaryExpression.ArithmeticOperator.SUB_OP;
		else if (tok.getTokType() == TokenType.MUL_TOK)
			op = BinaryExpression.ArithmeticOperator.MUL_OP;
		else if (tok.getTokType() == TokenType.DIV_TOK)
			op = BinaryExpression.ArithmeticOperator.DIV_OP;
		else 
			throw new ParserException ("arithmetic operator expected at row " +
				tok.getRowNumber()  + " and column " + tok.getColumnNumber());
		return op;
	}
	// return literal integer
	// throws ParserException if a parsing error occurred
	private LiteralInteger getLiteralInteger() throws ParserException {
		String answer = "";
		Token tok = getNextToken ();
		if (tok.getTokType() != TokenType.LITERAL_INTEGER_TOK)
			throw new ParserException ("literal integer expected at row " +
				tok.getRowNumber()  + " and column " + tok.getColumnNumber());
		if (tok.getLexeme().contains(";")){
			 answer = tok.getLexeme().replace(";","");
		}
		else {
			answer = tok.getLexeme();
		}
		int value = Integer.parseInt(answer);
		return new LiteralInteger (value);
	}

	// return an id
	// throws ParserException if a parsing error occurred
	private Id getId() throws ParserException {
		Token tok = getNextToken();
		if (tok.getTokType() == TokenType.INTEGER_TOK){
			tok = getNextToken();
		}
		if (tok.getTokType() != TokenType.ID_TOK)
			throw new ParserException ("identifier expected at row " +
				tok.getRowNumber()  + " and column " + tok.getColumnNumber());
		return new Id (tok.getLexeme().charAt(0));
	}

	// return boolean expression
	// throws ParserException if a parsing error occurred
	private BooleanExpression getBooleanExpression() throws ParserException {
		ArithmeticExpression expr1 = getArithmeticExpression();
		BooleanExpression.RelationalOperator op = getRelationalOperator();
		ArithmeticExpression expr2 = getArithmeticExpression ();
		return new BooleanExpression(op, expr1, expr2);
	}

	// return relative operator
	// throws ParserException if a parsing error occurred
	private BooleanExpression.RelationalOperator getRelationalOperator() throws ParserException {
		BooleanExpression.RelationalOperator op;
		Token tok = getNextToken();
		if (tok.getTokType() == TokenType.EQ_TOK)
			op = BooleanExpression.RelationalOperator.EQ_OP;
		else if (tok.getTokType() == TokenType.NE_TOK)
			op = BooleanExpression.RelationalOperator.NE_OP;
		else if (tok.getTokType() == TokenType.GT_TOK)
			op = BooleanExpression.RelationalOperator.GT_OP;
		else if (tok.getTokType() == TokenType.GE_TOK)
			op = BooleanExpression.RelationalOperator.GE_OP;
		else if (tok.getTokType() == TokenType.LT_TOK)
			op = BooleanExpression.RelationalOperator.LT_OP;
		else if (tok.getTokType() == TokenType.LE_TOK)
			op = BooleanExpression.RelationalOperator.LE_OP;
		else
			throw new ParserException ("relational operator expected at row " +
				tok.getRowNumber()  + " and column " + tok.getColumnNumber());
		return op;
	}

	// tok cannot be null - checked by assertion
	// tokType cannot be null - checked by assertion
	private void match(Token tok, TokenType tokType) throws ParserException {
		assert (tok != null);
		assert (tokType != null);
		if (tok.getTokType() != tokType)
			throw new ParserException (tokType + " expected at row " +
				tok.getRowNumber()  + " and column " + tok.getColumnNumber());
	}

	// return copy of next token
	// throws ParserException if there are no more tokens
	private Token getLookaheadToken() throws ParserException {
		Token tok = null;
		try
		{
			tok = lex.getLookaheadToken();
		}
		catch (LexicalException e)
		{
			throw new ParserException ("no more tokens");
		}
		return tok;
	}

	// return next token
	// throws ParserException if there are no more tokens
	private Token getNextToken() throws ParserException {
		Token tok = null;
		try
		{
			tok = lex.getNextToken();
		}
		catch (LexicalException e)
		{
			throw new ParserException ("no more tokens");
		}
		return tok;
	}
	private Token getNextValueToken() throws ParserException {
		Token tok = null;
		try
		{
			tok = lex.getLookNextToken();
		}
		catch (LexicalException e)
		{
			throw new ParserException ("no more tokens");
		}
		return tok;
	}
}
