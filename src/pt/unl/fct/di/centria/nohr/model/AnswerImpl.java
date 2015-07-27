package pt.unl.fct.di.centria.nohr.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import pt.unl.fct.di.centria.nohr.Utils;

public class AnswerImpl implements Answer {

    private final Query query;

    private final TruthValue truthValue;

    private final List<Term> values;

    private final Map<Variable, Integer> variablesIndex;

    AnswerImpl(Query query, TruthValue truthValue, List<Term> values,
	    Map<Variable, Integer> variablesIndex) {
	this.query = query;
	this.truthValue = truthValue;
	this.values = values;
	this.variablesIndex = variablesIndex;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * pt.unl.fct.di.centria.nohr.model.Answer#acept(pt.unl.fct.di.centria.nohr
     * .model.FormatVisitor)
     */
    @Override
    public String acept(FormatVisitor visitor) {
	return visitor.visit(this);
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * pt.unl.fct.di.centria.nohr.model.Answer#acept(pt.unl.fct.di.centria.nohr
     * .model.Visitor)
     */
    @Override
    public Answer acept(Visitor visitor) {
	final Map<Variable, Integer> varsIdx = new HashMap<Variable, Integer>();
	final List<Term> vals = new ArrayList<Term>();
	for (final Entry<Variable, Integer> entry : variablesIndex.entrySet())
	    varsIdx.put(entry.getKey().acept(visitor), entry.getValue());
	for (final Term val : values)
	    vals.add(val.acept(visitor));
	return new AnswerImpl(query, truthValue, vals, varsIdx);
    }

    @Override
    public List<Literal> apply() {
	final Map<Variable, Term> substitution = new HashMap<Variable, Term>();
	for (final Entry<Variable, Integer> entry : variablesIndex.entrySet())
	    substitution.put(entry.getKey(), values.get(entry.getValue()));
	final List<Literal> literals = new LinkedList<Literal>();
	for (final Literal literal : query.getLiterals())
	    literals.add(literal.apply(substitution));
	return literals;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
	if (this == obj)
	    return true;
	if (obj == null)
	    return false;
	if (!(obj instanceof AnswerImpl))
	    return false;
	final AnswerImpl other = (AnswerImpl) obj;
	if (truthValue != other.truthValue)
	    return false;
	if (values == null) {
	    if (other.values != null)
		return false;
	} else if (!values.equals(other.values))
	    return false;
	if (variablesIndex == null) {
	    if (other.variablesIndex != null)
		return false;
	} else if (!variablesIndex.equals(other.variablesIndex))
	    return false;
	return true;
    }

    @Override
    public Query getQuery() {
	return query;
    }

    @Override
    public TruthValue getValuation() {
	return truthValue;
    }

    @Override
    public Term getValue(Variable var) {
	return values.get(variablesIndex.get(var));
    }

    /*
     * (non-Javadoc)
     * 
     * @see pt.unl.fct.di.centria.nohr.model.Answer#getValues()
     */
    @Override
    public List<Term> getValues() {
	return values;
    }

    /*
     * (non-Javadoc)
     *
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
	final int prime = 31;
	int result = 1;
	result = prime * result
		+ (truthValue == null ? 0 : truthValue.hashCode());
	result = prime * result + (values == null ? 0 : values.hashCode());
	return result;
    }

    @Override
    public String toString() {
	return Utils.concat(",", apply());
    }

}
