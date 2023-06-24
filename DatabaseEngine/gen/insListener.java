// Generated from C:/Users/user/IdeaProjects/DatabaseEngine\ins.g4 by ANTLR 4.12.0
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link insParser}.
 */
public interface insListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link insParser#parse}.
	 * @param ctx the parse tree
	 */
	void enterParse(insParser.ParseContext ctx);
	/**
	 * Exit a parse tree produced by {@link insParser#parse}.
	 * @param ctx the parse tree
	 */
	void exitParse(insParser.ParseContext ctx);
	/**
	 * Enter a parse tree produced by {@link insParser#insert_statement}.
	 * @param ctx the parse tree
	 */
	void enterInsert_statement(insParser.Insert_statementContext ctx);
	/**
	 * Exit a parse tree produced by {@link insParser#insert_statement}.
	 * @param ctx the parse tree
	 */
	void exitInsert_statement(insParser.Insert_statementContext ctx);
	/**
	 * Enter a parse tree produced by {@link insParser#table_name}.
	 * @param ctx the parse tree
	 */
	void enterTable_name(insParser.Table_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link insParser#table_name}.
	 * @param ctx the parse tree
	 */
	void exitTable_name(insParser.Table_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link insParser#column_name}.
	 * @param ctx the parse tree
	 */
	void enterColumn_name(insParser.Column_nameContext ctx);
	/**
	 * Exit a parse tree produced by {@link insParser#column_name}.
	 * @param ctx the parse tree
	 */
	void exitColumn_name(insParser.Column_nameContext ctx);
	/**
	 * Enter a parse tree produced by {@link insParser#value}.
	 * @param ctx the parse tree
	 */
	void enterValue(insParser.ValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link insParser#value}.
	 * @param ctx the parse tree
	 */
	void exitValue(insParser.ValueContext ctx);
}