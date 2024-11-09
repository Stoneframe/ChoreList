package stoneframe.serena.gui.routines;

import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import stoneframe.serena.R;
import stoneframe.serena.gui.EditActivity;
import stoneframe.serena.gui.util.EditTextCriteria;
import stoneframe.serena.gui.util.EnableCriteria;
import stoneframe.serena.model.routines.Routine;
import stoneframe.serena.model.routines.RoutineEditor;

public abstract class EditRoutineActivity<TRoutine extends Routine<?>, TRoutineEditor extends RoutineEditor<?>> extends EditActivity
{
    protected EditText nameEditText;
    protected CheckBox enabledCheckBox;

    protected Button addProcedureButton;

    protected TRoutineEditor routineEditor;

    @Override
    protected int getActivityLayoutId()
    {
        return getRoutineContentView();
    }

    @Override
    protected abstract String getActivityTitle();

    @Override
    protected String getEditedObjectName()
    {
        return "Routine";
    }

    @Override
    protected void createActivity()
    {
        routineEditor = getRoutineEditor((TRoutine)globalState.getActiveRoutine());

        nameEditText = findViewById(R.id.nameTextEdit);
        enabledCheckBox = findViewById(R.id.enabledCheckbox);

        addProcedureButton = findViewById(R.id.addProcedureButton);

        nameEditText.setText(routineEditor.getName());
        enabledCheckBox.setChecked(routineEditor.isEnabled());

        addProcedureButton.setOnClickListener(v -> addProcedure());

        createSpecialisedActivity();
    }

    @Override
    protected EnableCriteria[] getSaveEnabledCriteria()
    {
        return new EditTextCriteria[]
            {
                new EditTextCriteria(nameEditText, EditTextCriteria.IS_NOT_EMPTY),
            };
    }

    @Override
    protected void onCancel()
    {
        routineEditor.revert();
    }

    @Override
    protected void onSave(int action)
    {
        if (enabledCheckBox.isChecked() && !routineEditor.isEnabled())
        {
            routineEditor.reset();
        }

        routineEditor.setName(nameEditText.getText().toString().trim());
        routineEditor.setEnabled(enabledCheckBox.isChecked());
        routineEditor.save();

        serena.save();
    }

    @Override
    protected void onRemove()
    {
        routineEditor.remove();
        serena.save();
    }

    protected abstract TRoutineEditor getRoutineEditor(TRoutine routine);

    protected abstract void createSpecialisedActivity();

    protected abstract int getRoutineContentView();

    protected abstract void addProcedure();
}