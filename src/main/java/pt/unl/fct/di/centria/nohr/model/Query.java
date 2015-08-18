package pt.unl.fct.di.centria.nohr.model;

import java.util.List;
import java.util.Map;

/**
 * Represents a query <i>q(x<sub>1</sub>, ..., x<sub>k</sub>) &larr; A <sub>1</sub>, ..., A<sub>m</sub>, <b>not</b> B<sub>1</sub>, ..., <b>not</b> B
 * <sub>n</sub></i>, where <i>A<sub>i</sub></i> and </i><i>B <sub>i</sub></i> are atoms, and <i>x<sub>i</sub></i> are variables that occur in some
 * atom <sub>i</sub></i> or <i>B<sub>i</sub></i>. <i>x<sub>1</sub>, ..., x <sub>k</sub></i> are called free variables.
 *
 * @author Nuno Costa
 */
public interface Query extends FormatVisitable {

	public Query accept(ModelVisitor visitor);

	/**
	 * Apply, to all the query's literal, a substitution that maps each variable of the list of free variables to the term at the same position in a
	 * specified list values.
	 *
	 * @param values
	 *            the list of terms to assign to free variables <i>x<sub>1</sub> , ...,x<sub>k</sub></i>.
	 * @return this query with each free variable <i>x<sub>i</sub></i> replaced by the <i>i</i>-th term of {@code terms}.
	 */
	public Query apply(List<Term> values);

	public Query getDouble();

	/**
	 * Returns a mapping between the free variable of the query and the position where there values are expected in an {@link Answer answer} value
	 * list.
	 *
	 * @return a mapping between the free variable of the query and the position where there values are expected in an {@link Answer answer} value
	 *         list.
	 */
	Map<Variable, Integer> getIndex();

	/**
	 * Returns the literals of this query.
	 *
	 * @return the literals.
	 */
	public List<Literal> getLiterals();

	// TODO remove
	public Query getOriginal();

	/**
	 * Returns the free variables of this query.
	 *
	 * @return the variables of this query.
	 */
	public List<Variable> getVariables();

}
