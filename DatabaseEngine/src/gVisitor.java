// Generated from C:/Users/user/IdeaProjects/DatabaseEngine/src\g.g4 by ANTLR 4.12.0
import org.antlr.v4.runtime.tree.ParseTreeVisitor;

/**
 * This interface defines a complete generic visitor for a parse tree produced
 * by {@link gParser}.
 *
 * @param <T> The return type of the visit operation. Use {@link Void} for
 * operations with no return type.
 */
public interface gVisitor<T> extends ParseTreeVisitor<T> {
	/**
	 * Visit a parse tree produced by {@link gParser#insertStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitInsertStatement(gParser.InsertStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link gParser#updateStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitUpdateStatement(gParser.UpdateStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link gParser#deleteStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitDeleteStatement(gParser.DeleteStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link gParser#selectStatement}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectStatement(gParser.SelectStatementContext ctx);
	/**
	 * Visit a parse tree produced by {@link gParser#selectCondition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSelectCondition(gParser.SelectConditionContext ctx);
	/**
	 * Visit a parse tree produced by {@link gParser#operator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitOperator(gParser.OperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link gParser#setClause}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSetClause(gParser.SetClauseContext ctx);
	/**
	 * Visit a parse tree produced by {@link gParser#setItem}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitSetItem(gParser.SetItemContext ctx);
	/**
	 * Visit a parse tree produced by {@link gParser#values}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValues(gParser.ValuesContext ctx);
	/**
	 * Visit a parse tree produced by {@link gParser#valueExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitValueExpression(gParser.ValueExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link gParser#literalValue}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitLiteralValue(gParser.LiteralValueContext ctx);
	/**
	 * Visit a parse tree produced by {@link gParser#conditions}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitConditions(gParser.ConditionsContext ctx);
	/**
	 * Visit a parse tree produced by {@link gParser#condition}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitCondition(gParser.ConditionContext ctx);
	/**
	 * Visit a parse tree produced by {@link gParser#comparisonExpression}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComparisonExpression(gParser.ComparisonExpressionContext ctx);
	/**
	 * Visit a parse tree produced by {@link gParser#comparisonOperator}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitComparisonOperator(gParser.ComparisonOperatorContext ctx);
	/**
	 * Visit a parse tree produced by {@link gParser#tableName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitTableName(gParser.TableNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link gParser#columnName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnName(gParser.ColumnNameContext ctx);
	/**
	 * Visit a parse tree produced by {@link gParser#columnNames}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitColumnNames(gParser.ColumnNamesContext ctx);
	/**
	 * Visit a parse tree produced by {@link gParser#functionName}.
	 * @param ctx the parse tree
	 * @return the visitor result
	 */
	T visitFunctionName(gParser.FunctionNameContext ctx);
}