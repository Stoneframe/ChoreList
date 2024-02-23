package stoneframe.chorelist.model.storages;

import org.json.JSONException;
import org.json.JSONObject;

public interface UpgradeScript
{
    int getVersion();

    JSONObject upgrade(JSONObject jsonObject) throws JSONException;
}
