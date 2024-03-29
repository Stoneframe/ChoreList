package stoneframe.chorelist.gui;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import org.joda.time.DateTime;

import stoneframe.chorelist.R;

public class TaskActivity extends AppCompatActivity
{
    public static final int TASK_ACTION_ADD = 0;
    public static final int TASK_ACTION_EDIT = 1;

    public static final int TASK_RESULT_SAVE = 0;
    public static final int TASK_RESULT_REMOVE = 1;

    private String description;
    private DateTime deadline;
    private DateTime ignoreBefore;
    private boolean isDone;

    private DatePickerDialog deadlinePickerDialog;
    private DatePickerDialog ignoreBeforePickerDialog;

    private EditText descriptionEditText;
    private EditText deadlineEditText;
    private EditText ignoreBeforeEditText;
    private CheckBox isDoneCheckBox;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task);

        Intent intent = getIntent();

        int action = intent.getIntExtra("ACTION", -1);

        Button button = findViewById(R.id.removeButton);
        button.setVisibility(action == TASK_ACTION_EDIT ? Button.VISIBLE : Button.INVISIBLE);

        description = intent.getStringExtra("Description");
        deadline = (DateTime)intent.getSerializableExtra("Deadline");
        ignoreBefore = (DateTime)intent.getSerializableExtra("IgnoreBefore");
        isDone = intent.getBooleanExtra("IsDone", false);

        deadlinePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) ->
        {
            deadline = new DateTime(year, month + 1, dayOfMonth, 0, 0);
            deadlineEditText.setText(deadline.toString("yyyy-MM-dd"));
        }, deadline.getYear(), deadline.getMonthOfYear() - 1, deadline.getDayOfMonth());

        ignoreBeforePickerDialog = new DatePickerDialog(this, (view, year, month, dayOfMonth) ->
        {
            ignoreBefore = new DateTime(year, month + 1, dayOfMonth, 0, 0);
            ignoreBeforeEditText.setText(ignoreBefore.toString("yyyy-MM-dd"));
        }, ignoreBefore.getYear(), ignoreBefore.getMonthOfYear() - 1, ignoreBefore.getDayOfMonth());

        descriptionEditText = findViewById(R.id.taskDescriptionEditText);
        deadlineEditText = findViewById(R.id.deadlineEditText);
        ignoreBeforeEditText = findViewById(R.id.ignoreBeforeEditText);
        isDoneCheckBox = findViewById(R.id.isDoneCheckBox);

        descriptionEditText.setText(description);
        deadlineEditText.setText(deadline.toString("yyyy-MM-dd"));
        ignoreBeforeEditText.setText(ignoreBefore.toString("yyyy-MM-dd"));
        isDoneCheckBox.setChecked(isDone);

        deadlineEditText.setInputType(InputType.TYPE_NULL);
        deadlineEditText.setOnClickListener(view -> deadlinePickerDialog.show());

        ignoreBeforeEditText.setInputType(InputType.TYPE_NULL);
        ignoreBeforeEditText.setOnClickListener(view -> ignoreBeforePickerDialog.show());
    }

    public void saveClick(View view)
    {
        description = descriptionEditText.getText().toString().trim();
        isDone = isDoneCheckBox.isChecked();

        Intent intent = new Intent();
        intent.putExtra("RESULT", TASK_RESULT_SAVE);
        intent.putExtra("Description", description);
        intent.putExtra("Deadline", deadline);
        intent.putExtra("IgnoreBefore", ignoreBefore);
        intent.putExtra("IsDone", isDone);

        setResult(RESULT_OK, intent);
        finish();
    }

    public void cancelClick(View view)
    {
        setResult(RESULT_CANCELED);
        finish();
    }

    public void removeClick(View view)
    {
        Intent intent = new Intent();
        intent.putExtra("RESULT", TASK_RESULT_REMOVE);

        setResult(RESULT_OK, intent);
        finish();
    }
}
