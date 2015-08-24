package pt.unl.fct.di.centria.nohr.reasoner.translation;

import org.semanticweb.owlapi.model.OWLOntology;

import pt.unl.fct.di.centria.nohr.deductivedb.DeductiveDatabase;
import pt.unl.fct.di.centria.nohr.reasoner.OWLProfilesViolationsException;
import pt.unl.fct.di.centria.nohr.reasoner.UnsupportedAxiomsException;

/**
 * Represents a component that can translate a specified {@link OWLOntology} <i>O</i> to a logic program <i>T</i> and load that program in a specified
 * {@link DeductiveDatabase}. The following properties must be satisfied, where <i>A</i> denotes an atomic concept and the corresponding predicate,
 * <i>P</i> an atomic role and the corresponding predicate, <i>a</i> and <i>b</i> individuals and the corresponding constants:<br>
 * -<i>T&vDash;A(a) iff O&vDash;A(a)</i>;<br>
 * -<i>T&vDash;A<sup>d</sup>(a) iff O&vDash;A(a)</i>;<br>
 * -<i>T&vDash;P(a, b) iff O&vDash;P(a, b)</i>;<br>
 * -<i>T&vDash;P<sup>d</sup>(a, b) iff O&vDash;P(a, b)</i>.
 *
 * @author Nuno Costa
 */
public interface OntologyTranslator {

	/**
	 * Clear the {@link Program program}(s) that maintains the translation.
	 */
	public void clear();

	/**
	 * Returns the {@link DeductiveDatabase} where this {@link OntologyTranslator translator} maintains the translation.
	 *
	 * @return the {@link DeductiveDatabase} where this {@link OntologyTranslator translator} maintains the translation.
	 */
	public DeductiveDatabase getDedutiveDatabase();

	/**
	 * Returns the {@link OWLOntology} that this {@link OntologyTranslator translator} translates.
	 *
	 * @return the {@link OWLOntology} that this {@link OntologyTranslator translator} translates.
	 */
	public OWLOntology getOntology();

	/**
	 * Returns the profile of the {@link OntologyTranslator translator}'s ontology.
	 *
	 * @return the profile of the {@link OntologyTranslator translator}'s ontology.
	 */
	public Profile getProfile();

	/**
	 * Returns true iff the {@link OntologyTranslator translator}'s ontology has disjunctions.
	 *
	 * @return iff the {@link OntologyTranslator translator}'s has disjunctions.
	 */
	public boolean hasDisjunctions();

	/**
	 * Updates the translation {@link Program program}(s) in the {@link OntologyTranslator translator}'s {@link DeductiveDatabase deductive database}
	 * with the translation of the current version of the {@link OntologyTranslator translator}'s ontology.
	 *
	 * @throws UnsupportedAxiomsException
	 *             if the {@link OntologyTranslator translator}'s ontology has some axioms of an unsupported type.
	 * @throws OWLProfilesViolationsException
	 *             if the {@link OntologyTranslator translator}'s ontology isn't in any supported OWL profile.
	 */
	public void updateTranslation() throws OWLProfilesViolationsException, UnsupportedAxiomsException;

}