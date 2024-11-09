package stoneframe.serena.gui.limiters;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.joda.time.LocalDateTime;

import stoneframe.serena.R;
import stoneframe.serena.gui.GlobalState;
import stoneframe.serena.gui.util.SimpleListAdapter;
import stoneframe.serena.gui.util.EditTextButtonEnabledLink;
import stoneframe.serena.gui.util.EditTextCriteria;
import stoneframe.serena.gui.util.SimpleListAdapterBuilder;
import stoneframe.serena.model.Serena;
import stoneframe.serena.model.limiters.Limiter;
import stoneframe.serena.model.limiters.LimiterManager;

public class AllLimitersFragment extends Fragment
{
    private SimpleListAdapter<Limiter> limiterListAdapter;

    private GlobalState globalState;
    private Serena serena;
    private LimiterManager limiterManager;

    @Override
    public View onCreateView(
        LayoutInflater inflater,
        ViewGroup container,
        Bundle savedInstanceState)
    {
        globalState = GlobalState.getInstance();
        serena = globalState.getSerena();
        limiterManager = serena.getLimiterManager();

        View rootView = inflater.inflate(R.layout.fragment_all_limiters, container, false);

        limiterListAdapter = new SimpleListAdapterBuilder<>(
            requireContext(),
            limiterManager::getLimiters,
            Limiter::getName)
            .withSecondaryTextFunction(this::getAvailableText)
            .withBottomTextFunction(this::getReplenishedText)
            .withBackgroundColorFunction(this::getBackgroundColor)
            .withBorderColorFunction(this::getBorderColor)
            .create();

        ListView limiterListView = rootView.findViewById(R.id.all_limiters);
        limiterListView.setAdapter(limiterListAdapter);
        limiterListView.setOnItemClickListener((parent, view, position, id) ->
        {
            Limiter limiter = (Limiter)limiterListAdapter.getItem(position);

            openLimiterActivity(limiter);
        });

        Button addButton = rootView.findViewById(R.id.add_button);
        addButton.setOnClickListener(v ->
        {
            final EditText limiterNameText = new EditText(getContext());

            limiterNameText.setInputType(EditorInfo.TYPE_TEXT_FLAG_CAP_SENTENCES);

            AlertDialog.Builder builder = getBuilder(limiterNameText);

            AlertDialog alertDialog = builder.create();
            alertDialog.show();

            new EditTextButtonEnabledLink(
                alertDialog.getButton(DialogInterface.BUTTON_POSITIVE),
                new EditTextCriteria(limiterNameText, EditTextCriteria.IS_NOT_EMPTY));
        });

        return rootView;
    }

    @Override
    public void onStart()
    {
        super.onStart();

        limiterListAdapter.notifyDataSetChanged();
    }

    @NonNull
    private AlertDialog.Builder getBuilder(EditText limiterNameText)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Create limiter");
        builder.setView(limiterNameText);

        builder.setPositiveButton("OK", (dialog, which) ->
        {
            String limiterName = limiterNameText.getText().toString();

            Limiter limiter = limiterManager.createLimiter(limiterName);

            serena.save();

            limiterListAdapter.notifyDataSetChanged();

            openLimiterActivity(limiter);
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        return builder;
    }

    private void openLimiterActivity(Limiter limiter)
    {
        globalState.setActiveLimiter(limiter);

        Intent intent = new Intent(requireContext(), LimiterActivity.class);
        startActivity(intent);
    }

    @SuppressLint("DefaultLocale")
    private @NonNull String getAvailableText(Limiter l)
    {
        return String.format("Remaining: %d", l.getAvailable(LocalDateTime.now()));
    }

    private @NonNull String getReplenishedText(Limiter l)
    {
        LocalDateTime now = LocalDateTime.now();

        String when = l.getAvailable(now) < 0
            ? l.getReplenishTime(now).toString("yyyy-MM-dd HH:mm")
            : "Now";

        return String.format("Replenished: %s", when);
    }

    private Integer getBackgroundColor(Limiter limiter)
    {
        return limiter.getAvailable(LocalDateTime.now()) >= 0
            ? Color.parseColor("#c3fab6")
            : Color.parseColor("#e6e3e3");
    }

    private int getBorderColor(Limiter limiter)
    {
        return limiter.getAvailable(LocalDateTime.now()) >= 0
            ? Color.parseColor("#018a26")
            : Color.parseColor("#7e807e");
    }
}