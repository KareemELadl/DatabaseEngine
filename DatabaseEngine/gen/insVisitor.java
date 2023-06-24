// Generated from C:/Users/user/IdeaProjects/DatabaseEngine\ins.g4 by ANTLR 4.12.0
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link insParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface insVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link insParser#parse}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitParse(insParser.ParseContext ctx);
	/**
	 * Visit a parse tree produced by {@link insParser#insert_statement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInsert_statement(insParser.Insert_statementContext ctx);
	/**
	 * Visit a parse tree produced by {@link insParser#table_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTable_name(insParser.Table_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link insParser#column_name}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumn_name(insParser.Column_nameContext ctx);
	/**
	 * Visit a parse tree produced by {@link insParser#value}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValue(insParser.ValueContext ctx);
}