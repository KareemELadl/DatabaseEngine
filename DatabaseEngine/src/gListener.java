// Generated from C:/Users/user/IdeaProjects/DatabaseEngine/src\g.g4 by ANTLR 4.12.0
import org.antlr.v4.runtime.tree.ParseTreeListener;

/**
 * This interface defines a complete listener for a parse tree produced by
 * {@link gParser}.
 */
public interface gListener extends ParseTreeListener {
	/**
	 * Enter a parse tree produced by {@link gParser#insertStatement}.
	 * @param ctx the parse tree
	 */
	void enterInsertStatement(gParser.InsertStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link gParser#insertStatement}.
	 * @param ctx the parse tree
	 */
	void exitInsertStatement(gParser.InsertStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link gParser#updateStatement}.
	 * @param ctx the parse tree
	 */
	void enterUpdateStatement(gParser.UpdateStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link gParser#updateStatement}.
	 * @param ctx the parse tree
	 */
	void exitUpdateStatement(gParser.UpdateStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link gParser#deleteStatement}.
	 * @param ctx the parse tree
	 */
	void enterDeleteStatement(gParser.DeleteStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link gParser#deleteStatement}.
	 * @param ctx the parse tree
	 */
	void exitDeleteStatement(gParser.DeleteStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link gParser#selectStatement}.
	 * @param ctx the parse tree
	 */
	void enterSelectStatement(gParser.SelectStatementContext ctx);
	/**
	 * Exit a parse tree produced by {@link gParser#selectStatement}.
	 * @param ctx the parse tree
	 */
	void exitSelectStatement(gParser.SelectStatementContext ctx);
	/**
	 * Enter a parse tree produced by {@link gParser#selectCondition}.
	 * @param ctx the parse tree
	 */
	void enterSelectCondition(gParser.SelectConditionContext ctx);
	/**
	 * Exit a parse tree produced by {@link gParser#selectCondition}.
	 * @param ctx the parse tree
	 */
	void exitSelectCondition(gParser.SelectConditionContext ctx);
	/**
	 * Enter a parse tree produced by {@link gParser#operator}.
	 * @param ctx the parse tree
	 */
	void enterOperator(gParser.OperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link gParser#operator}.
	 * @param ctx the parse tree
	 */
	void exitOperator(gParser.OperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link gParser#setClause}.
	 * @param ctx the parse tree
	 */
	void enterSetClause(gParser.SetClauseContext ctx);
	/**
	 * Exit a parse tree produced by {@link gParser#setClause}.
	 * @param ctx the parse tree
	 */
	void exitSetClause(gParser.SetClauseContext ctx);
	/**
	 * Enter a parse tree produced by {@link gParser#setItem}.
	 * @param ctx the parse tree
	 */
	void enterSetItem(gParser.SetItemContext ctx);
	/**
	 * Exit a parse tree produced by {@link gParser#setItem}.
	 * @param ctx the parse tree
	 */
	void exitSetItem(gParser.SetItemContext ctx);
	/**
	 * Enter a parse tree produced by {@link gParser#values}.
	 * @param ctx the parse tree
	 */
	void enterValues(gParser.ValuesContext ctx);
	/**
	 * Exit a parse tree produced by {@link gParser#values}.
	 * @param ctx the parse tree
	 */
	void exitValues(gParser.ValuesContext ctx);
	/**
	 * Enter a parse tree produced by {@link gParser#valueExpression}.
	 * @param ctx the parse tree
	 */
	void enterValueExpression(gParser.ValueExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link gParser#valueExpression}.
	 * @param ctx the parse tree
	 */
	void exitValueExpression(gParser.ValueExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link gParser#literalValue}.
	 * @param ctx the parse tree
	 */
	void enterLiteralValue(gParser.LiteralValueContext ctx);
	/**
	 * Exit a parse tree produced by {@link gParser#literalValue}.
	 * @param ctx the parse tree
	 */
	void exitLiteralValue(gParser.LiteralValueContext ctx);
	/**
	 * Enter a parse tree produced by {@link gParser#conditions}.
	 * @param ctx the parse tree
	 */
	void enterConditions(gParser.ConditionsContext ctx);
	/**
	 * Exit a parse tree produced by {@link gParser#conditions}.
	 * @param ctx the parse tree
	 */
	void exitConditions(gParser.ConditionsContext ctx);
	/**
	 * Enter a parse tree produced by {@link gParser#condition}.
	 * @param ctx the parse tree
	 */
	void enterCondition(gParser.ConditionContext ctx);
	/**
	 * Exit a parse tree produced by {@link gParser#condition}.
	 * @param ctx the parse tree
	 */
	void exitCondition(gParser.ConditionContext ctx);
	/**
	 * Enter a parse tree produced by {@link gParser#comparisonExpression}.
	 * @param ctx the parse tree
	 */
	void enterComparisonExpression(gParser.ComparisonExpressionContext ctx);
	/**
	 * Exit a parse tree produced by {@link gParser#comparisonExpression}.
	 * @param ctx the parse tree
	 */
	void exitComparisonExpression(gParser.ComparisonExpressionContext ctx);
	/**
	 * Enter a parse tree produced by {@link gParser#comparisonOperator}.
	 * @param ctx the parse tree
	 */
	void enterComparisonOperator(gParser.ComparisonOperatorContext ctx);
	/**
	 * Exit a parse tree produced by {@link gParser#comparisonOperator}.
	 * @param ctx the parse tree
	 */
	void exitComparisonOperator(gParser.ComparisonOperatorContext ctx);
	/**
	 * Enter a parse tree produced by {@link gParser#tableName}.
	 * @param ctx the parse tree
	 */
	void enterTableName(gParser.TableNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link gParser#tableName}.
	 * @param ctx the parse tree
	 */
	void exitTableName(gParser.TableNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link gParser#columnName}.
	 * @param ctx the parse tree
	 */
	void enterColumnName(gParser.ColumnNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link gParser#columnName}.
	 * @param ctx the parse tree
	 */
	void exitColumnName(gParser.ColumnNameContext ctx);
	/**
	 * Enter a parse tree produced by {@link gParser#columnNames}.
	 * @param ctx the parse tree
	 */
	void enterColumnNames(gParser.ColumnNamesContext ctx);
	/**
	 * Exit a parse tree produced by {@link gParser#columnNames}.
	 * @param ctx the parse tree
	 */
	void exitColumnNames(gParser.ColumnNamesContext ctx);
	/**
	 * Enter a parse tree produced by {@link gParser#functionName}.
	 * @param ctx the parse tree
	 */
	void enterFunctionName(gParser.FunctionNameContext ctx);
	/**
	 * Exit a parse tree produced by {@link gParser#functionName}.
	 * @param ctx the parse tree
	 */
	void exitFunctionName(gParser.FunctionNameContext ctx);
}