package stoneframe.chorelist.gui.routines;

import android.Manifest;
import android.app.AlarmManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import org.joda.time.LocalDateTime;

import stoneframe.chorelist.R;
import stoneframe.chorelist.gui.GlobalState;
import stoneframe.chorelist.gui.routines.days.DayRoutineActivity;
import stoneframe.chorelist.gui.routines.fortnights.FortnightRoutineActivity;
import stoneframe.chorelist.gui.routines.weeks.WeekRoutineActivity;
import stoneframe.chorelist.gui.util.DialogUtils;
import stoneframe.chorelist.gui.util.SimpleListAdapter;
import stoneframe.chorelist.model.ChoreList;
import stoneframe.chorelist.model.routines.DayRoutine;
import stoneframe.chorelist.model.routines.FortnightRoutine;
import stoneframe.chorelist.model.routines.Routine;
import stoneframe.chorelist.model.routines.RoutineManager;
import stoneframe.chorelist.model.routines.WeekRoutine;

public class AllRoutinesFragment extends Fragment
{
    private ActivityResultLauncher<Intent> editRoutineLauncher;

    private SimpleListAdapter<Routine<?>> routineListAdapter;

    private GlobalState globalState;
    private ChoreList choreList;
    private RoutineManager routineManager;

    @Override
    public View onCreateView(
        LayoutInflater inflater,
        ViewGroup container,
        Bundle savedInstanceState)
    {
        globalState = GlobalState.getInstance();

        choreList = globalState.getChoreList();
        routineManager = choreList.getRoutineManager();

        View rootView = inflater.inflate(R.layout.fragment_all_routines, container, false);

        routineListAdapter = new SimpleListAdapter<>(
            requireContext(),
            routineManager::getAllRoutines,
            Routine::getName,
            this::getRoutineTypeName,
            r -> "");
        ListView routineListView = rootView.findViewById(R.id.all_routines);
        routineListView.setAdapter(routineListAdapter);
        routineListView.setOnItemClickListener((parent, view, position, id) ->
        {
            Routine<?> routine = (Routine<?>)routineListAdapter.getItem(position);
            assert routine != null;
            startRoutineEditor(routine, EditRoutineActivity.ACTION_EDIT);
        });

        Button addDayButton = rootView.findViewById(R.id.add_day_button);
        addDayButton.setOnClickListener(v ->
        {
            Routine<?> routine = routineManager.createDayRoutine();
            startRoutineEditor(routine, DayRoutineActivity.ACTION_ADD);
        });

        Button addWeekButton = rootView.findViewById(R.id.add_week_button);
        addWeekButton.setOnClickListener(v ->
        {
            Routine<?> routine = routineManager.createWeekRoutine();
            startRoutineEditor(routine, WeekRoutineActivity.ACTION_ADD);
        });

        Button addFortnightButton = rootView.findViewById(R.id.add_fortnight_button);
        addFortnightButton.setOnClickListener(v ->
        {
            Routine<?> routine = routineManager.createFortnightRoutine();
            startRoutineEditor(routine, FortnightRoutineActivity.ACTION_ADD);
        });

        editRoutineLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            this::editRoutineCallback);

        checkNotificationAndExactAlarmsPermissions();

        return rootView;
    }

    @Override
    public void onStart()
    {
        super.onStart();

        routineListAdapter.notifyDataSetChanged();
    }

    private String getRoutineTypeName(Routine<?> routine)
    {
        switch (routine.getRoutineType())
        {
            case Routine.DAY_ROUTINE:
                return "Day";
            case Routine.WEEK_ROUTINE:
                return "Week";
            case Routine.FORTNIGHT_ROUTINE:
                return "Fortnight";
            default:
                return "Unknown";
        }
    }

    private void startRoutineEditor(Routine<?> routine, int mode)
    {
        globalState.setActiveRoutine(routine);

        Intent intent;

        if (routine instanceof DayRoutine)
        {
            intent = new Intent(getActivity(), DayRoutineActivity.class);
        }
        else if (routine instanceof WeekRoutine)
        {
            intent = new Intent(getActivity(), WeekRoutineActivity.class);
        }
        else if (routine instanceof FortnightRoutine)
        {
            intent = new Intent(getActivity(), FortnightRoutineActivity.class);
        }
        else
        {
            return;
        }

        intent.putExtra("ACTION", mode);

        editRoutineLauncher.launch(intent);
    }

    private void checkNotificationAndExactAlarmsPermissions()
    {
        ActivityResultLauncher<String> requestNotificationPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            this::requestPermissionsCallback);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
        {
            if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED)
            {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
            else
            {
                checkPermissionToScheduleExactAlarms();
            }
        }
        else
        {
            checkPermissionToScheduleExactAlarms();
        }
    }

    private void requestPermissionsCallback(Boolean isGranted)
    {
        if (isGranted)
        {
            // Permission was granted, you can now send notifications
            Log.d("NotificationPermission", "Notification permission granted.");
        }
        else
        {
            // Permission was denied, handle accordingly
            Log.d("NotificationPermission", "Notification permission denied.");
        }

        checkPermissionToScheduleExactAlarms();
    }

    private void editRoutineCallback(ActivityResult activityResult)
    {
        routineListAdapter.notifyDataSetChanged();

        scheduleNextRoutineAlarm();
    }

    private void scheduleNextRoutineAlarm()
    {
        LocalDateTime nextAlarmTime = routineManager.getNextProcedureTime();

        if (nextAlarmTime == null)
        {
            RoutineNotifier.cancelRoutineAlarm(requireContext());
        }
        else
        {
            RoutineNotifier.scheduleRoutineAlarm(requireContext(), nextAlarmTime);
        }
    }

    private void checkPermissionToScheduleExactAlarms()
    {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
        {
            AlarmManager alarmManager = (AlarmManager)requireActivity().getSystemService(Context.ALARM_SERVICE);

            if (!alarmManager.canScheduleExactAlarms())
            {
                DialogUtils.showWarningDialog(
                    requireContext(),
                    "Alarms Permission",
                    "You need to allow ChoreList to set alarms. Enable \"Allow setting alarms and reminders\"",
                    () ->
                    {
                        Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                        intent.setData(Uri.parse("package:" + requireActivity().getPackageName()));  // Directs the intent to your app's settings
                        startActivity(intent);
                    });
            }
        }
    }
}
