package pt.unl.fct.di.centria.nohr.xsb;

import static pt.unl.fct.di.centria.nohr.model.Model.ans;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import other.Utils;
import pt.unl.fct.di.centria.nohr.model.Answer;
import pt.unl.fct.di.centria.nohr.model.Config;
import pt.unl.fct.di.centria.nohr.model.ModelException;
import pt.unl.fct.di.centria.nohr.model.Query;
import pt.unl.fct.di.centria.nohr.model.Rule;
import pt.unl.fct.di.centria.nohr.model.Term;
import pt.unl.fct.di.centria.nohr.model.TermModelAdapter;
import pt.unl.fct.di.centria.nohr.model.TruthValue;
import pt.unl.fct.di.centria.nohr.model.Variable;
import pt.unl.fct.di.centria.nohr.model.predicates.Predicate;
import utils.Tracer;

import com.declarativa.interprolog.SolutionIterator;
import com.declarativa.interprolog.TermModel;
import com.declarativa.interprolog.XSBSubprocessEngine;
import com.declarativa.interprolog.util.IPException;

/**
 * The Class QueryEngine.
 */
public class XSBDatabase implements Collection<Rule> {

    /** The xsb engine. */
    protected XSBSubprocessEngine engine;

    private final Set<Rule> rules = new HashSet<Rule>();

    private final Queue<String> rulesBuffer = new LinkedList<String>();

    protected Path xsbPath;

    private int rulesCount;

    /** The is engine started. */
    private boolean isEngineStarted = false;

    private SolutionIterator lastSolutionsIterator;

    /**
     * Instantiates a new query engine
     *
     * @throws Exception
     *             the exception
     */
    public XSBDatabase(Path xsbPath) throws IPException {

	this.xsbPath = xsbPath;

	/**
	 * Env variable which should be responsible for directory where XSB was
	 * installed
	 */
	printLog("Starting query engine" + Config.NL);
	printLog(Config.TEMP_DIR + Config.NL);

	// if (xsbBin != null)
	// xsbBin += "/xsb";
	// else
	// throw new Exception("Please, set up your XSB_BIN_DIRECTORY");
	startEngine(xsbPath.toAbsolutePath().toString());
    }

    public void abolishTables() {
	engine.command("abolish_all_tables");
    }

    public boolean add(Predicate predicate) {
	return engine.deterministicGoal("dynamic " + predicate);
    }

    @Override
    public boolean add(Rule rule) {
	rules.add(rule);
	for (Predicate pred : rule.getPredicates())
	    if (!add(pred))
		return false;
	boolean added = engine.deterministicGoal(String.format("assert((%s))",
		rule));
	if (added) {
	    rules.add(rule);
	    rulesCount++;
	}
	return added;
    }

    public void add(String rule) {
	rulesBuffer.add(rule);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Collection#addAll(java.util.Collection)
     */
    @Override
    public boolean addAll(Collection<? extends Rule> rules) {
	for (Rule rule : rules)
	    if (!add(rule))
		return false;
	return true;
    }

    private void addAnswer(TermModel valuesList,
	    Map<List<Term>, TruthValue> answers) {
	try {
	    TermModel[] termsList = valuesList.flatList();
	    List<Term> vals = new ArrayList<Term>(termsList.length);
	    for (int i = 1; i < termsList.length; i++)
		vals.add(TermModelAdapter.getTerm(termsList[i]));
	    TruthValue truth = TermModelAdapter.getTruthValue(termsList[0]);
	    answers.put(vals, truth);
	} catch (ModelException e) {
	    e.printStackTrace();
	    System.exit(1);
	}
    }

    private Answer answer(Query query, Map<Variable, Integer> varsIdx,
	    TermModel valuesList) {
	try {
	    TermModel[] termsList = valuesList.flatList();
	    TruthValue truth = TermModelAdapter.getTruthValue(termsList[0]);
	    List<Term> vals = new ArrayList<Term>(termsList.length);
	    for (int i = 1; i <= varsIdx.size(); i++)
		vals.add(TermModelAdapter.getTerm(termsList[i]));
	    return ans(query, truth, vals);
	} catch (ModelException e) {
	    e.printStackTrace();
	    System.exit(1);
	    return null;
	}
    }

    /**
     *
     */
    @Override
    public void clear() {
	rules.clear();
	try {
	    startEngine(xsbPath.toAbsolutePath().toString());
	} catch (Exception e) {
	    e.printStackTrace();
	}
	// engine.deterministicGoal("retractall(call(X))");
    }

    private void clearSolutionIterator() {
	if (lastSolutionsIterator != null) {
	    lastSolutionsIterator.cancel();
	    lastSolutionsIterator = null;
	}
    }

    public boolean command(String command) {
	return engine.deterministicGoal(command);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Collection#contains(java.lang.Object)
     */
    @Override
    public boolean contains(Object o) {
	return rules.contains(o);
	// if (!(o instanceof Rule))
	// return false;
	// Rule rule = (Rule) o;
	// return engine.deterministicGoal(String.format("clause((%s, %s))",
	// rule.getHead(), Utils.concat(",", rule.getBody())));
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Collection#containsAll(java.util.Collection)
     */
    @Override
    public boolean containsAll(Collection<?> c) {
	for (Object o : c)
	    if (!contains(o))
		return false;
	return true;
    }

    /**
     * Deterministic goal.
     *
     * @param detGoal
     *            the det goal
     * @return the object[]
     */
    public Object[] deterministicGoal(String detGoal) {
	return engine.deterministicGoal(detGoal, "[TM]");
    }

    /**
     * Deterministic goal bool.
     *
     * @param command
     *            the command
     * @return true, if successful
     */
    public boolean deterministicGoalBool(String command) {
	return engine.deterministicGoal(command);
    }

    private void flush() {
	StringBuilder goal = new StringBuilder();
	String sep = "";
	int c = 0;
	while (!rulesBuffer.isEmpty()) {
	    if (c == 0)
		Tracer.start("xsb assert");
	    goal.append(sep);
	    goal.append("assert((");
	    goal.append(rulesBuffer.remove());
	    goal.append("))");
	    c++;
	    sep = ",";
	    if (c == 10000 | rulesBuffer.isEmpty()) {
		engine.deterministicGoal(goal.toString());
		Tracer.stop("xsb assert", "loading");
		c = 0;
		sep = "";
		goal = new StringBuilder();
	    }
	}
    }

    public boolean hasAnswers(Query query) {
	return hasAnswers(query, null);
    }

    public boolean hasAnswers(Query query, Boolean trueAnswers) {
	Tracer.log("hasAnswers( " + query + ", " + trueAnswers + ")");
	flush();
	Tracer.log("flushed");
	String goal;
	if (trueAnswers == null)
	    goal = query.toString();
	else {
	    String truth = trueAnswers ? "true" : "undefined";
	    goal = String.format("call_tv(%s,%s)", query, truth);
	}
	Tracer.log("goal=" + goal);
	return engine.deterministicGoal(goal);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Collection#isEmpty()
     */
    @Override
    public boolean isEmpty() {
	return rulesCount == 0;
    }

    /**
     * Checks if is engine started.
     *
     * @return true, if is engine started
     */
    public boolean isEngineStarted() {
	return isEngineStarted;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Collection#iterator()
     */
    @Override
    public Iterator<Rule> iterator() {
	return rules.iterator();
    }

    public Iterable<Answer> lazilyQuery(Query query) {
	return lazilyQuery(query, null);
    }

    public Iterable<Answer> lazilyQuery(final Query query, Boolean trueAnswers) {
	if (lastSolutionsIterator != null) {
	    lastSolutionsIterator.cancel();
	    lastSolutionsIterator = null;
	}
	flush();
	String vars = Utils.concat(",", query.getVariables());
	String goal;
	if (trueAnswers == null)
	    goal = String.format("detGoal([%s],(%s),TM)", vars, query);
	else {
	    String truth = trueAnswers ? "true" : "undefined";
	    goal = String
		    .format("detGoal([%s],(%s),%s,TM)", vars, query, truth);
	}
	final Map<Variable, Integer> varsIdx = variablesIndex(query
		.getVariables());
	final SolutionIterator solutions = engine.goal(goal, "[TM]");
	lastSolutionsIterator = solutions;
	final XSBDatabase xsbDatabase = this;
	return new Iterable<Answer>() {

	    @Override
	    public Iterator<Answer> iterator() {
		return new Iterator<Answer>() {

		    private boolean canceled;

		    @Override
		    public boolean hasNext() {
			if (canceled)
			    return false;
			return solutions.hasNext();
		    }

		    @Override
		    public Answer next() {
			Object[] bindings = solutions.next();
			if (!solutions.hasNext()) {
			    solutions.cancel();
			    canceled = true;
			    xsbDatabase.lastSolutionsIterator = null;
			}
			TermModel valuesList = (TermModel) bindings[0];
			return answer(query, varsIdx, valuesList);
		    }

		    @Override
		    public void remove() {
			solutions.remove();
		    }
		};
	    }

	};
    }

    /**
     * Load.
     *
     * @param file
     *            the file
     * @return true, if successful
     */
    public void load(File file) {
	boolean loaded = engine.load_dynAbsolute(file);
	if (!loaded)
	    throw new IPException("file not loaded");
    }

    /**
     * Prints the log.
     *
     * @param message
     *            the message
     */
    private void printLog(String message) {

    }

    public Answer query(Query query) {
	return query(query, null);
    }

    public Answer query(Query query, Boolean trueAnswers) {
	flush();
	String vars = Utils.concat(",", query.getVariables());
	String goal;
	if (trueAnswers == null)
	    goal = String.format("detGoal([%s],(%s),TM)", vars, query);
	else {
	    String truth = trueAnswers ? "true" : "undefined";
	    goal = String
		    .format("detGoal([%s],(%s),%s,TM)", vars, query, truth);
	}
	Object[] bindings = engine.deterministicGoal(goal, "[TM]");
	if (bindings == null)
	    return null;
	return answer(query, variablesIndex(query.getVariables()),
		(TermModel) bindings[0]);
    }

    public Map<List<Term>, TruthValue> queryAll(Query query) {
	return queryAll(query, null);
    }

    public Map<List<Term>, TruthValue> queryAll(Query query, Boolean trueAnswers) {
	clearSolutionIterator();
	flush();
	Map<List<Term>, TruthValue> answers = new HashMap<List<Term>, TruthValue>();
	String vars = Utils.concat(",", query.getVariables());
	String goal;
	if (trueAnswers == null)
	    goal = String.format("nonDetGoal([%s],(%s),TM)", vars, query);
	else {
	    String truth = trueAnswers ? "true" : "undefined";
	    goal = String.format("nonDetGoal([%s],(%s),%s,TM)", vars, query,
		    truth);
	}
	Object[] bindings = engine.deterministicGoal(goal, "[TM]");
	if (bindings == null)
	    return answers;
	TermModel ansList = (TermModel) bindings[0];
	for (TermModel ans : ansList.flatList())
	    addAnswer(ans, answers);
	return answers;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Collection#remove(java.lang.Object)
     */
    @Override
    public boolean remove(Object o) {
	if (!(o instanceof Rule))
	    return false;
	Rule rule = (Rule) o;
	boolean removed = engine.deterministicGoal(String.format(
		"retract((%))", rule));
	if (removed) {
	    rules.remove(o);
	    rulesCount--;
	}
	return removed;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Collection#removeAll(java.util.Collection)
     */
    @Override
    public boolean removeAll(Collection<?> c) {
	for (Object o : c)
	    if (!remove(o))
		return false;
	return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Collection#retainAll(java.util.Collection)
     */
    @Override
    public boolean retainAll(Collection<?> c) {
	return rules.retainAll(c);
    }

    /**
     * Shutdown.
     */
    public void shutdown() {
	engine.shutdown();
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Collection#size()
     */
    @Override
    public int size() {
	return rulesCount;
    }

    /**
     * Start engine.
     *
     * @param xsbBin
     *            the xsb bin
     * @throws Exception
     *             the exception
     */
    public void startEngine(String xsbBin) throws IPException {
	if (xsbBin == null)
	    xsbBin = xsbPath.toAbsolutePath().toString();

	if (engine != null) {

	    engine.shutdown();
	    engine = null;
	}

	isEngineStarted = true;

	engine = new XSBSubprocessEngine(xsbBin);

	// _engine.addPrologOutputListener(this);
	printLog("Engine started" + Config.NL);

	engine.deterministicGoal("assert((detGoal(Vars,G,TM):-call_tv(G,TV),buildTermModel([TV|Vars],TM)))");
	engine.deterministicGoal("assert((detGoal(Vars,G,TV,TM):-call_tv(G,TV),buildTermModel([TV|Vars],TM)))");
	engine.deterministicGoal("assert((nonDetGoal(Vars,G,ListTM):-findall([TV|Vars],call_tv(G,TV),L),buildTermModel(L,ListTM)))");
	engine.deterministicGoal("assert((nonDetGoal(Vars,G,TV,ListTM):-findall([TV|Vars],call_tv(G,TV),L),buildTermModel(L,ListTM)))");

    }

    public boolean table(String pred) {
	return engine.deterministicGoal("table " + pred);
    }

    /*
     * (non-Javadoc)
     * 
     * @see java.util.Collection#toArray()
     */
    @Override
    public Object[] toArray() {
	return rules.toArray();
    }

    /*
     * (non-Javadoc)format
     * 
     * @see java.util.Collection#toArray(java.lang.Object[])
     */
    @Override
    public <T> T[] toArray(T[] a) {
	return rules.toArray(a);
    }

    private SortedMap<Variable, Integer> variablesIndex(List<Variable> variables) {
	SortedMap<Variable, Integer> result = new TreeMap<Variable, Integer>();
	int i = 0;
	for (Variable var : variables)
	    result.put(var, i++);
	return result;
    }
}
