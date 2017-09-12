package pt.unl.fct.di.novalincs.nohr.plugin.dbmapping;

import java.awt.Container;

import javax.swing.JOptionPane;

import org.protege.editor.core.ui.util.JOptionPaneEx;
import org.protege.editor.owl.OWLEditorKit;
import org.protege.editor.owl.ui.UIHelper;

import pt.unl.fct.di.novalincs.nohr.model.DBMapping;
import pt.unl.fct.di.novalincs.nohr.model.DBMappingImpl;
import pt.unl.fct.di.novalincs.nohr.plugin.DBMappingViewComponent;

/**
 * An editor, showed in an dialog, that can be used to edit text containing database
 * mappings.
 *
 * @author Vedran Kasalica
 */
public class DBMappingEditor {

    
    private final DBMappingEditForm editor;

    private final OWLEditorKit editorKit;
    
    private final Container dbMappingViewComponent;

    /**
     * 
     *
     */ 
    public DBMappingEditor(OWLEditorKit editorKit, DBMappingViewComponent dbMappingViewComponent) {
        this.editorKit = editorKit;
        editor = new DBMappingEditForm();
        this.dbMappingViewComponent=dbMappingViewComponent;
    }

    public void clear() {
    	editor.refreshComboBox();
    	editor.setODBCDriver(null);
        editor.setTables(null);
        editor.setColumns(null);
        editor.setPredicateText("");
    }

    public void setDBMapping(DBMapping dbMapping) {
    	editor.refreshComboBox();
    	editor.setODBCDriver(dbMapping.getODBC());
    	editor.setTables(dbMapping.getTable());
        editor.setColumns(dbMapping.getColumns());
        editor.setPredicateText(dbMapping.getPredicate());
    }

    public DBMapping show() {
        final int ret = JOptionPaneEx.showConfirmDialog(dbMappingViewComponent, "Database-Mapping Editor", editor,
				JOptionPane.PLAIN_MESSAGE, JOptionPane.OK_CANCEL_OPTION, null);

        if (ret == JOptionPane.OK_OPTION) {
        	DBMapping tmp=editor.getDBMapping();
        	System.out.println("DBMappingEditor.show() - "+tmp.toString());
            return tmp;
        }

        return null;
    }

}
