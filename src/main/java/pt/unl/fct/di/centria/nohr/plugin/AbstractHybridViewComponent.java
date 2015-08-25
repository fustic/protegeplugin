/**
 *
 */
package pt.unl.fct.di.centria.nohr.plugin;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.apache.log4j.Logger;
import org.protege.editor.core.Disposable;
import org.protege.editor.owl.model.classexpression.OWLExpressionParserException;
import org.protege.editor.owl.model.event.EventType;
import org.protege.editor.owl.model.event.OWLModelManagerChangeEvent;
import org.protege.editor.owl.model.event.OWLModelManagerListener;
import org.protege.editor.owl.ui.clsdescriptioneditor.OWLExpressionChecker;
import org.protege.editor.owl.ui.view.AbstractOWLViewComponent;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;

import com.declarativa.interprolog.util.IPException;

import pt.unl.fct.di.centria.nohr.deductivedb.PrologEngineCreationException;
import pt.unl.fct.di.centria.nohr.model.Program;
import pt.unl.fct.di.centria.nohr.model.Query;
import pt.unl.fct.di.centria.nohr.model.Rule;
import pt.unl.fct.di.centria.nohr.parsing.NoHRParser;
import pt.unl.fct.di.centria.nohr.parsing.ParseException;
import pt.unl.fct.di.centria.nohr.parsing.Parser;
import pt.unl.fct.di.centria.nohr.plugin.rules.DisposableRuleBase;
import pt.unl.fct.di.centria.nohr.reasoner.HybridKB;
import pt.unl.fct.di.centria.nohr.reasoner.HybridKBImpl;
import pt.unl.fct.di.centria.nohr.reasoner.OWLProfilesViolationsException;
import pt.unl.fct.di.centria.nohr.reasoner.UnsupportedAxiomsException;
import pt.unl.fct.di.centria.nohr.reasoner.VocabularyMapping;
import pt.unl.fct.di.centria.nohr.reasoner.VocabularyMappingImpl;

/**
 * @author nunocosta
 */
public abstract class AbstractHybridViewComponent extends AbstractOWLViewComponent {

	protected class DisposableHybridKB extends HybridKBImpl implements Disposable {

		public DisposableHybridKB(File xsbBinDirectory, OWLOntology ontology, Program ruleBase)
				throws OWLProfilesViolationsException, UnsupportedAxiomsException, IPException, IOException,
				PrologEngineCreationException {
			super(xsbBinDirectory, ontology, ruleBase);
		}

	}

	protected class DisposableObject<T> implements Disposable {

		private T object;

		public DisposableObject(T object) {
			this.object = object;
		}

		@Override
		public void dispose() throws Exception {
			object = null;
		}

		public T getObject() {
			return object;
		}

	}

	class QueryExpressionChecker implements OWLExpressionChecker<Query>, Disposable {

		private Parser parser;

		QueryExpressionChecker(Parser parser) {
			this.parser = parser;
		}

		@Override
		public void check(String str) throws OWLExpressionParserException {
			createObject(str);
		}

		@Override
		public Query createObject(String str) throws OWLExpressionParserException {

			try {
				return parser.parseQuery(str);
			} catch (final ParseException e) {
				throw new OWLExpressionParserException("", 0, 0, false, false, false, false, false, false, null);
			}
		}

		@Override
		public void dispose() throws Exception {
			parser = null;
		}
	};

	class RuleExpressionChecker implements OWLExpressionChecker<Rule>, Disposable {

		private Parser parser;

		RuleExpressionChecker(Parser parser) {
			this.parser = parser;
		}

		@Override
		public void check(String str) throws OWLExpressionParserException {
			createObject(str);
		}

		@Override
		public Rule createObject(String str) throws OWLExpressionParserException {

			try {
				return parser.parseRule(str);
			} catch (final ParseException e) {
				throw new OWLExpressionParserException("", 0, 0, false, false, false, false, false, false, null);
			}
		}

		@Override
		public void dispose() throws Exception {
			parser = null;
		}
	};

	private static final Logger log = Logger.getLogger(AbstractHybridViewComponent.class);

	/**
	 *
	 */
	private static final long serialVersionUID = -2850791395194206722L;

	/**
	 *
	 */
	public AbstractHybridViewComponent() {
		super();
	}

	private DisposableHybridKB createHybridKB(Set<OWLAxiom> axioms) {
		final File xsbBinDirectory = NoHRPreferences.getInstance().getXSBBinDirectory();
		if (xsbBinDirectory == null) {
			MessageDialogs.xsbBinDirectoryNotDefined(this);
			return null;
		}
		try {
			return new DisposableHybridKB(xsbBinDirectory, getOntology(), getRuleBase());
		} catch (final IPException e) {
			MessageDialogs.xsbProblems(this, e);
			return null;
		} catch (final UnsupportedAxiomsException e) {
			final boolean ignoreUnsupportedAxioms = MessageDialogs.violations(this, e);
			if (ignoreUnsupportedAxioms) {
				axioms.removeAll(e.getUnsupportedAxioms());
				return createHybridKB(axioms);
			}
			return null;
		} catch (final IOException e) {
			MessageDialogs.translationFileProblems(this, e);
			return null;
		} catch (final PrologEngineCreationException e) {
			MessageDialogs.xsbDatabaseCreationProblems(this, e);
			return null;
		} catch (final RuntimeException e) {
			if (log.isDebugEnabled())
				log.debug("Exception caught trying to create the HybridKB", e);
			return null;
		}
	}

	protected HybridKB getHybridKB() {
		final DisposableHybridKB hybridKB = getOWLModelManager().get(HybridKBImpl.class);
		if (hybridKB == null)
			throw new NullPointerException();
		return hybridKB;
	}

	protected OWLOntology getOntology() {
		return getOWLModelManager().getActiveOntology();
	}

	protected VocabularyMapping getOntologyIndex() {
		DisposableObject<VocabularyMapping> disposableObject = getOWLModelManager().get(VocabularyMapping.class);
		if (disposableObject == null) {
			disposableObject = new DisposableObject<VocabularyMapping>(new VocabularyMappingImpl(getOntology()));
			getOWLModelManager().put(VocabularyMapping.class, disposableObject);
		}
		return disposableObject.getObject();
	}

	protected Parser getParser() {
		DisposableObject<Parser> disposableObject = getOWLModelManager().get(Parser.class);
		if (disposableObject == null) {
			disposableObject = new DisposableObject<Parser>(new NoHRParser(getOntologyIndex()));
			getOWLModelManager().put(Parser.class, disposableObject);
		}
		return disposableObject.getObject();
	}

	protected OWLExpressionChecker<Query> getQueryExpressionChecker() {
		QueryExpressionChecker queryExpressionChecker = getOWLModelManager().get(Parser.class);
		if (queryExpressionChecker == null) {
			queryExpressionChecker = new QueryExpressionChecker(getParser());
			getOWLModelManager().put(Parser.class, queryExpressionChecker);
		}
		return queryExpressionChecker;
	}

	protected DisposableRuleBase getRuleBase() {
		DisposableRuleBase ruleBase = getOWLModelManager().get(DisposableRuleBase.class);
		if (ruleBase == null) {
			ruleBase = new DisposableRuleBase();
			getOWLModelManager().put(DisposableRuleBase.class, ruleBase);
		}
		return ruleBase;
	}

	protected OWLExpressionChecker<Query> getRuleExpressionChecker() {
		QueryExpressionChecker queryExpressionChecker = getOWLModelManager().get(Parser.class);
		if (queryExpressionChecker == null) {
			queryExpressionChecker = new QueryExpressionChecker(getParser());
			getOWLModelManager().put(Parser.class, queryExpressionChecker);
		}
		return queryExpressionChecker;
	}

	protected boolean isNoHRStarted() {
		return getOWLModelManager().get(HybridKBImpl.class) != null;
	}

	protected void startNoHR() {
		getOWLModelManager().put(VocabularyMapping.class,
				new DisposableObject<VocabularyMapping>(new VocabularyMappingImpl(getOntology())));
		getOWLModelManager().put(HybridKBImpl.class, createHybridKB(getOntology().getAxioms()));
		getOWLModelManager().addListener(new OWLModelManagerListener() {

			@Override
			public void handleChange(OWLModelManagerChangeEvent e) {
				if (e.isType(EventType.ACTIVE_ONTOLOGY_CHANGED)) {
					getOWLModelManager().put(VocabularyMapping.class, new DisposableObject<VocabularyMapping>(
							new VocabularyMappingImpl(getOWLModelManager().getActiveOntology())));
					getOWLModelManager().put(HybridKBImpl.class, createHybridKB(getOntology().getAxioms()));
				}
			}
		});
	}

}
