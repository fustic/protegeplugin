/**
 *
 */
package pt.unl.fct.di.centria.nohr.reasoner.translation;

import static pt.unl.fct.di.centria.nohr.model.Model.atom;
import static pt.unl.fct.di.centria.nohr.model.Model.cons;
import static pt.unl.fct.di.centria.nohr.model.Model.negLiteral;
import static pt.unl.fct.di.centria.nohr.model.Model.rule;
import static pt.unl.fct.di.centria.nohr.model.Model.ruleSet;
import static pt.unl.fct.di.centria.nohr.model.predicates.Predicates.doubPred;
import static pt.unl.fct.di.centria.nohr.model.predicates.Predicates.negPred;
import static pt.unl.fct.di.centria.nohr.model.predicates.Predicates.origPred;

import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLPropertyExpression;

import pt.unl.fct.di.centria.nohr.model.Constant;
import pt.unl.fct.di.centria.nohr.model.Rule;
import pt.unl.fct.di.centria.nohr.model.predicates.Predicate;

/**
 * Auxiliary methods to translate ABox assertions, that can be used in different OWL profiles.
 *
 * @author Nuno Costa
 */
public class AssertionsTranslation {

	/**
	 * Double translation of a given concept assertion.
	 *
	 * @param assertion
	 *            a concept assertion
	 * @return the set of double rules (i.e. those with double meta-predicates) correspondent to the translation of {@code assertion} (see, e.g.,
	 *         {@link <a href= "http://centria.di.fct.unl.pt/~mknorr/ISWC15/resources/ISWC15WithProofs.pdf"> <i>Next Step for NoHR: OWL 2 QL</i></a>},
	 *         <b>Definition 9.</b>, <b>(a1)</b>).
	 * @throws IllegalAccessException
	 *             if {@code assertion} has a non atomic concept.
	 */
	public static Set<Rule> translateDouble(OWLClassAssertionAxiom assertion) {
		final OWLClassExpression c = assertion.getClassExpression();
		if (!(c instanceof OWLClass))
			throw new IllegalArgumentException("assertion's concepts must be atomic");
		final Predicate a = doubPred((OWLClass) c);
		final Predicate na = negPred((OWLClass) c);
		final Constant i = cons(assertion.getIndividual());
		return ruleSet(rule(atom(a, i), negLiteral(na, i)));
	}

	/**
	 * Double translation of a given role assertion.
	 *
	 * @param assertion
	 *            a role assertion.
	 * @return the set of double rules (i.e. those with double meta-predicates) correspondent to the translation of {@code assertion} (see e.g.
	 *         {@link <a href= "http://centria.di.fct.unl.pt/~mknorr/ISWC15/resources/ISWC15WithProofs.pdf"> <i>Next Step for NoHR: OWL 2 QL</i></a>},
	 *         <b>Definition 9.</b>, <b>(a1)</b>).
	 */
	public static Set<Rule> translateDouble(OWLPropertyAssertionAxiom<?, ?> assertion) {
		final OWLPropertyExpression<?, ?> role = assertion.getProperty();
		final Predicate p = doubPred(role);
		final Predicate np = negPred(role);
		final Constant i1 = cons(assertion.getSubject());
		final Constant i2 = cons(assertion.getObject());
		return ruleSet(rule(atom(p, i1, i2), negLiteral(np, i1, i2)));
	}

	/**
	 * Original translation of a given concept assertion.
	 *
	 * @param assertion
	 *            a concept assertion.
	 * @return the set of double rules (i.e. those with original meta-predicates) correspondent to the translation of {@code assertion} (see e.g.
	 *         {@link <a href= "http://centria.di.fct.unl.pt/~mknorr/ISWC15/resources/ISWC15WithProofs.pdf"> <i>Next Step for NoHR: OWL 2 QL</i></a>},
	 *         <b>Definition 9.</b>, <b>(a1)</b>).
	 * @throws IllegalArgumentException
	 *             if {@code assertion} has a non atomic concept.
	 */
	public static Set<Rule> translateOriginal(OWLClassAssertionAxiom assertion) {
		final OWLClassExpression c = assertion.getClassExpression();
		if (c instanceof OWLObjectSomeValuesFrom)
			return ruleSet();
		if (!(c instanceof OWLClass))
			throw new IllegalArgumentException("assertion's concepts must be atomic");
		if (c.isTopEntity() || c.isBottomEntity())
			return ruleSet();
		final Predicate a = origPred((OWLClass) c);
		final Constant i = cons(assertion.getIndividual());
		return ruleSet(rule(atom(a, i)));
	}

	/**
	 * Original translation of a given role assertion.
	 *
	 * @param assertion
	 *            a role assertion.
	 * @return the set of double rules (i.e. those with original meta-predicates) correspondent to the translation of {@code assertion} (see e.g.
	 *         {@link <a href= "http://centria.di.fct.unl.pt/~mknorr/ISWC15/resources/ISWC15WithProofs.pdf"> <i>Next Step for NoHR: OWL 2 QL</i></a>},
	 *         <b>Definition 9.</b>, <b>(a2)</b>).
	 */
	public static Set<Rule> translateOriginal(OWLPropertyAssertionAxiom<?, ?> assertion) {
		final OWLPropertyExpression<?, ?> ope = assertion.getProperty();
		if (ope.isTopEntity() || ope.isBottomEntity())
			return ruleSet();
		final Predicate p = origPred(assertion.getProperty());
		final Constant i1 = cons(assertion.getSubject());
		final Constant i2 = cons(assertion.getObject());
		return ruleSet(rule(atom(p, i1, i2)));
	}

}