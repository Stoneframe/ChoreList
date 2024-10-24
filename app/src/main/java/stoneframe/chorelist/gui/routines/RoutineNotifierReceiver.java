package stoneframe.chorelist.gui.routines;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

import stoneframe.chorelist.gui.GlobalState;
import stoneframe.chorelist.json.SimpleChoreSelectorConverter;
import stoneframe.chorelist.json.WeeklyEffortTrackerConverter;
import stoneframe.chorelist.model.ChoreList;
import stoneframe.chorelist.model.Storage;
import stoneframe.chorelist.model.chores.choreselectors.SimpleChoreSelector;
import stoneframe.chorelist.model.chores.efforttrackers.WeeklyEffortTracker;
import stoneframe.chorelist.model.storages.JsonConverter;
import stoneframe.chorelist.model.storages.SharedPreferencesStorage;
import stoneframe.chorelist.model.timeservices.RealTimeService;

public class RoutineNotifierReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent)
    {
        ChoreList choreList = getChoreList(context);

        RoutineNotifier.showRoutineNotification(context, choreList);
        RoutineNotifier.scheduleRoutineAlarm(
            context,
            choreList.getRoutineManager().getNextProcedureTime());
    }

    @NonNull
    private static ChoreList getChoreList(Context context)
    {
        GlobalState globalState = (GlobalState)context.getApplicationContext();

        ChoreList choreList = globalState.getChoreList();

        if (choreList == null)
        {
            choreList = loadChoreListFromStorage(context);
        }

        return choreList;
    }

    @NonNull
    private static ChoreList loadChoreListFromStorage(Context context)
    {
        Storage storage = new SharedPreferencesStorage(
            context,
            new JsonConverter(
                new SimpleChoreSelectorConverter(),
                new WeeklyEffortTrackerConverter()));

        ChoreList choreList = new ChoreList(
            storage,
            new RealTimeService(),
            new WeeklyEffortTracker(10, 10, 10, 10, 10, 30, 30),
            new SimpleChoreSelector());

        choreList.load();

        return choreList;
    }
}
