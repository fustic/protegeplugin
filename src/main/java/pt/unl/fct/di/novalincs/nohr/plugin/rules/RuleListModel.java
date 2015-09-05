/**
 *
 */
package pt.unl.fct.di.novalincs.nohr.plugin.rules;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;
import javax.swing.ListModel;

import org.apache.log4j.Logger;
import org.protege.editor.core.ui.list.MListSectionHeader;
import org.protege.editor.owl.OWLEditorKit;

import com.igormaznitsa.prologparser.exceptions.PrologParserException;

import pt.unl.fct.di.novalincs.nohr.model.Program;
import pt.unl.fct.di.novalincs.nohr.model.Rule;
import pt.unl.fct.di.novalincs.nohr.parsing.ProgramPresistenceManager;

/**
 * An {@link ListModel list model} of {@link Rule rules}.
 */
public class RuleListModel extends AbstractListModel<Object> {

	protected static final Logger log = Logger.getLogger(RuleListModel.class);

	private static final MListSectionHeader HEADER = new MListSectionHeader() {

		@Override
		public boolean canAdd() {
			return true;
		}

		@Override
		public String getName() {
			return "Rules";
		}
	};

	private static final long serialVersionUID = -5766699966244129502L;

	private final Program program;

	private final RuleEditor ruleEditor;

	private final List<Object> ruleItems;

	private final ProgramPresistenceManager programPresistenceManager;

	/**
	 *
	 */

	public RuleListModel(OWLEditorKit editorKit, RuleEditor ruleEditor,
			ProgramPresistenceManager programPresistenceManager, Program program) {
		super();
		this.programPresistenceManager = programPresistenceManager;
		this.ruleEditor = ruleEditor;
		this.program = program;
		ruleItems = new ArrayList<Object>(program.size());
		ruleItems.add(HEADER);
		for (final Rule rule : program)
			ruleItems.add(new RuleListItem(ruleItems.size() - 1, this, rule));
	}

	boolean add(Rule rule) {
		final boolean added = program.add(rule);
		if (added) {
			final int index = ruleItems.size();
			ruleItems.add(new RuleListItem(index, this, rule));
			super.fireIntervalAdded(this, index, index);
		}
		return added;
	}

	public void clear() {
		final int size = ruleItems.size();
		program.clear();
		ruleItems.clear();
		ruleItems.add(HEADER);
		super.fireIntervalRemoved(this, 1, size);
	}

	Rule edit(int index, Rule rule) {
		ruleEditor.setRule(rule);
		final Rule newRule = ruleEditor.show();
		boolean updated = false;
		if (newRule != null)
			updated = program.update(rule, newRule);
		fireContentsChanged(this, index, index);
		return updated ? newRule : null;
	}

	@Override
	public Object getElementAt(int index) {
		return ruleItems.get(index);
	}

	@Override
	public int getSize() {
		return ruleItems.size();
	}

	public void load(File file) throws IOException, PrologParserException {
		final int size = program.size();
		program.clear();
		programPresistenceManager.load(file, program);
		ruleItems.clear();
		ruleItems.add(HEADER);
		for (final Rule rule : program)
			ruleItems.add(new RuleListItem(ruleItems.size() - 1, this, rule));
		super.fireContentsChanged(this, 0, Math.max(program.size() - 1, size - 1));
	}

	boolean remove(int index, Rule rule) {
		final boolean removed = program.remove(rule);
		if (removed) {
			ruleItems.remove(index);
			super.fireIntervalRemoved(this, index, index);
		}
		return removed;
	}

	public void save(File file) throws IOException {
		ProgramPresistenceManager.write(program, file);
	}

}