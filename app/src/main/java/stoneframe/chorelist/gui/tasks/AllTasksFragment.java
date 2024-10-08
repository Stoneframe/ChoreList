package stoneframe.chorelist.gui.tasks;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import org.joda.time.LocalDate;

import java.util.Objects;

import stoneframe.chorelist.gui.util.CheckboxListAdapter;
import stoneframe.chorelist.gui.GlobalState;
import stoneframe.chorelist.model.ChoreList;
import stoneframe.chorelist.R;
import stoneframe.chorelist.model.tasks.Task;

public class AllTasksFragment extends Fragment
{
    private ActivityResultLauncher<Intent> editTaskLauncher;

    private ChoreList choreList;

    private CheckboxListAdapter<Task> taskAdapter;

    private Task taskUnderEdit;

    @Override
    public View onCreateView(
        LayoutInflater inflater,
        ViewGroup container,
        Bundle savedInstanceState)
    {
        GlobalState globalState = GlobalState.getInstance();

        choreList = globalState.getChoreList();

        View rootView = inflater.inflate(R.layout.fragment_all_tasks, container, false);

        taskAdapter = new CheckboxListAdapter<>(
            requireContext(),
            choreList::getAllTasks,
            Task::getDescription,
            Task::isDone,
            t -> String.format("Deadline: %s", t.getDeadline()));
        taskAdapter.setCheckboxChangedListener((task, isChecked) ->
        {
            if (isChecked)
            {
                choreList.taskDone(task);
            }
            else
            {
                choreList.taskUndone(task);
            }
        });

        ListView taskListView = rootView.findViewById(R.id.all_tasks);
        taskListView.setAdapter(taskAdapter);
        taskListView.setOnItemClickListener((parent, view, position, id) ->
        {
            Task task = (Task)taskAdapter.getItem(position);
            assert task != null;
            startTaskEditor(task, TaskActivity.TASK_ACTION_EDIT);
        });

        Button addButton = rootView.findViewById(R.id.add_button);
        addButton.setOnClickListener(v ->
        {
            Task task = new Task(
                "",
                LocalDate.now(),
                LocalDate.now());

            startTaskEditor(task, TaskActivity.TASK_ACTION_ADD);
        });

        editTaskLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            this::editTaskCallback);

        return rootView;
    }

    @Override
    public void onStart()
    {
        super.onStart();

        taskAdapter.notifyDataSetChanged();
    }

    private void startTaskEditor(Task task, int mode)
    {
        taskUnderEdit = task;

        Intent intent = new Intent(getActivity(), TaskActivity.class);

        intent.putExtra("ACTION", mode);
        intent.putExtra("Description", task.getDescription());
        intent.putExtra("Deadline", task.getDeadline());
        intent.putExtra("IgnoreBefore", task.getIgnoreBefore());
        intent.putExtra("IsDone", task.isDone());

        editTaskLauncher.launch(intent);
    }

    private void editTaskCallback(ActivityResult activityResult)
    {
        if (activityResult.getResultCode() == RESULT_OK)
        {
            Task task = taskUnderEdit;

            Intent intent = Objects.requireNonNull(activityResult.getData());

            switch (intent.getIntExtra("RESULT", -1))
            {
                case TaskActivity.TASK_RESULT_SAVE:
                    task.setDescription(intent.getStringExtra("Description"));
                    task.setDeadline((LocalDate)intent.getSerializableExtra("Deadline"));
                    task.setIgnoreBefore((LocalDate)intent.getSerializableExtra("IgnoreBefore"));

                    if (intent.getIntExtra("ACTION", -1) == TaskActivity.TASK_ACTION_ADD)
                    {
                        choreList.addTask(task);
                    }

                    boolean isDone = intent.getBooleanExtra("IsDone", false);

                    if (isDone == task.isDone()) break;

                    if (isDone)
                    {
                        choreList.taskDone(task);
                    }
                    else
                    {
                        choreList.taskUndone(task);
                    }

                    break;
                case TaskActivity.TASK_RESULT_REMOVE:
                    choreList.removeTask(task);
                    break;
            }
        }

        choreList.save();
    }
}
