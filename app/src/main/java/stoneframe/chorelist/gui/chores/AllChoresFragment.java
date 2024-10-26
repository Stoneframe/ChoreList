package stoneframe.chorelist.gui.chores;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import java.util.Comparator;
import java.util.Locale;
import java.util.stream.Collectors;

import stoneframe.chorelist.R;
import stoneframe.chorelist.gui.GlobalState;
import stoneframe.chorelist.gui.util.SimpleListAdapter;
import stoneframe.chorelist.gui.util.TextChangedListener;
import stoneframe.chorelist.model.ChoreList;
import stoneframe.chorelist.model.chores.Chore;
import stoneframe.chorelist.model.chores.ChoreManager;

public class AllChoresFragment extends Fragment
{
    private static final int SORT_BY_DESCRIPTION = 0;
    private static final int SORT_BY_PRIORITY = 1;
    private static final int SORT_BY_EFFORT = 2;
    private static final int SORT_BY_NEXT = 3;
    private static final int SORT_BY_FREQUENCY = 4;

    private int sortBy = SORT_BY_DESCRIPTION;

    private ActivityResultLauncher<Intent> editChoreLauncher;
    private ActivityResultLauncher<Intent> editEffortLauncher;

    private SimpleListAdapter<Chore> choreListAdapter;

    private GlobalState globalState;
    private ChoreList choreList;
    private ChoreManager choreManager;

    @SuppressLint("DefaultLocale")
    @Override
    public View onCreateView(
        LayoutInflater inflater,
        ViewGroup container,
        Bundle savedInstanceState)
    {
        globalState = GlobalState.getInstance();

        choreList = globalState.getChoreList();
        choreManager = choreList.getChoreManager();

        View rootView = inflater.inflate(R.layout.fragment_all_chores, container, false);

        EditText filterEditText = rootView.findViewById(R.id.filterEditText);
        filterEditText.addTextChangedListener(new TextChangedListener(s ->
            choreListAdapter.notifyDataSetChanged()));

        choreListAdapter = new SimpleListAdapter<>(
            requireContext(),
            () -> choreManager.getAllChores()
                .stream()
                .filter(c -> isChoreIncluded(c, filterEditText))
                .sorted(getComparator())
                .collect(Collectors.toList()),
            Chore::getDescription,
            c -> c.getNext().toString(),
            c -> String.format(
                "Priority: %d, Effort: %d, Frequency: %.2f /w",
                c.getPriority(),
                c.getEffort(),
                c.getFrequency()));
        ListView choreListView = rootView.findViewById(R.id.all_tasks);
        choreListView.setAdapter(choreListAdapter);
        choreListView.setOnItemClickListener((parent, view, position, id) ->
        {
            Chore chore = (Chore)choreListAdapter.getItem(position);
            assert chore != null;
            startChoreEditor(chore, EditChoreActivity.ACTION_EDIT);
        });

        Button sortByButton = rootView.findViewById(R.id.sort_by_button);
        sortByButton.setOnClickListener(v -> showSortByDialog());

        Button effortButton = rootView.findViewById(R.id.effort_button);
        effortButton.setOnClickListener(v ->
        {
            Intent intent = new Intent(getActivity(), EffortActivity.class);

            editEffortLauncher.launch(intent);
        });

        Button addButton = rootView.findViewById(R.id.add_button);
        addButton.setOnClickListener(v ->
        {
            Chore chore = choreManager.createChore();

            startChoreEditor(chore, EditChoreActivity.ACTION_ADD);
        });

        editChoreLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            this::editChoreCallback);

        editEffortLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            this::editEffortCallback);

        return rootView;
    }

    @Override
    public void onStart()
    {
        super.onStart();

        choreListAdapter.notifyDataSetChanged();
    }

    private static boolean isChoreIncluded(Chore c, EditText filterEditText)
    {
        String filter = filterEditText.getText().toString().toLowerCase(Locale.ROOT);
        return filter.isEmpty() || c.getDescription().toLowerCase().contains(filter);
    }

    private Comparator<Chore> getComparator()
    {
        if (sortBy == SORT_BY_DESCRIPTION)
        {
            return Comparator.comparing(Chore::getDescription);
        }

        if (sortBy == SORT_BY_PRIORITY)
        {
            return Comparator.comparing(Chore::getPriority);
        }

        if (sortBy == SORT_BY_EFFORT)
        {
            return Comparator.comparing(Chore::getEffort);
        }

        if (sortBy == SORT_BY_NEXT)
        {
            return Comparator.comparing(Chore::getNext);
        }

        if (sortBy == SORT_BY_FREQUENCY)
        {
            return Comparator.comparing(Chore::getFrequency).reversed();
        }

        return Comparator.comparing(Chore::getDescription);
    }

    private void startChoreEditor(Chore chore, int action)
    {
        globalState.setActiveChore(chore);

        Intent intent = new Intent(getActivity(), EditChoreActivity.class);
        intent.putExtra("ACTION", action);

        editChoreLauncher.launch(intent);
    }

    private void editChoreCallback(ActivityResult activityResult)
    {
        choreListAdapter.notifyDataSetChanged();
    }

    private void editEffortCallback(ActivityResult activityResult)
    {
        choreListAdapter.notifyDataSetChanged();
    }

    private void showSortByDialog()
    {
        final Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.dialog_sort_chores_by);

        RadioButton descriptionRadioButton = dialog.findViewById(R.id.descriptionRadioButton);
        RadioButton priorityRadioButton = dialog.findViewById(R.id.priorityRadioButton);
        RadioButton effortRadioButton = dialog.findViewById(R.id.effortRadioButton);
        RadioButton nextRadioButton = dialog.findViewById(R.id.nextRadioButton);
        RadioButton frequencyRadioButton = dialog.findViewById(R.id.frequencyRadioButton);
        Button buttonOK = dialog.findViewById(R.id.buttonOK);

        descriptionRadioButton.setChecked(sortBy == SORT_BY_DESCRIPTION);
        priorityRadioButton.setChecked(sortBy == SORT_BY_PRIORITY);
        effortRadioButton.setChecked(sortBy == SORT_BY_EFFORT);
        nextRadioButton.setChecked(sortBy == SORT_BY_NEXT);
        frequencyRadioButton.setChecked(sortBy == SORT_BY_FREQUENCY);

        buttonOK.setOnClickListener(v ->
        {
            if (descriptionRadioButton.isChecked())
            {
                sortBy = SORT_BY_DESCRIPTION;
            }

            if (priorityRadioButton.isChecked())
            {
                sortBy = SORT_BY_PRIORITY;
            }

            if (effortRadioButton.isChecked())
            {
                sortBy = SORT_BY_EFFORT;
            }

            if (nextRadioButton.isChecked())
            {
                sortBy = SORT_BY_NEXT;
            }

            if (frequencyRadioButton.isChecked())
            {
                sortBy = SORT_BY_FREQUENCY;
            }

            choreListAdapter.notifyDataSetChanged();

            dialog.dismiss();
        });

        dialog.show();
    }
}
