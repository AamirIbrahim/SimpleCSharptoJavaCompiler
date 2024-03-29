/*
Author:
Aamir Ibrahim
*/
public class IfStatement implements Statement {
	private BooleanExpression expr;
	private Block blk1, blk2;
	// expr cannot be null
	// blk1 cannot be null
	// blk2 cannot be null
	// IllegalArgumentException if any argument is null
	public IfStatement(BooleanExpression expr, Block blk1, Block blk2) {
		if (expr == null)
			throw new IllegalArgumentException ("null boolean expression argument");
		if (blk1 == null || blk2 == null)
			throw new IllegalArgumentException ("null block argument");
		this.expr = expr;
		this.blk1 = blk1;
		this.blk2 = blk2;
	}
	@Override
	public void execute() {
		if (expr.evaluate())
			blk1.execute();
		else
			blk2.execute();		
	}
}
