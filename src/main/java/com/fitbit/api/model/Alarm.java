package com.fitbit.api.model;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by davidengelmaier(http://www.totodon.com) on 02/03/15.
 */
public class Alarm {

    private String label;
    private String vibe;
    private boolean enabled;
    private String time;
    private boolean syncedToDevice;
    private int snoozeCount;
    private int id;
    private int snoozeLength;
    private boolean recurring;
    private boolean deleted;
    private List<String> weekDays;


    public Alarm(JSONObject json) throws JSONException {
        vibe = json.getString("vibe");
        enabled = json.getBoolean("enabled");
        time = json.getString("time");
        syncedToDevice = json.getBoolean("syncedToDevice");
        snoozeCount = json.getInt("snoozeCount");
        id = json.getInt("alarmId");
        snoozeLength = json.getInt("snoozeLength");
        recurring = json.getBoolean("recurring");
        deleted = json.getBoolean("deleted");
        JSONArray array = json.getJSONArray("weekDays");
        weekDays = new ArrayList<String>(array.length());
        for (int i = 0; i < array.length(); i++) {
            weekDays.add(array.getString(i));
        }
    }

    public Alarm(String time, boolean enabled, boolean recurring, List<String> weekDays) {
        this.time = time;
        this.enabled = enabled;
        this.recurring = recurring;
        this.weekDays = weekDays;
    }

    public String getLabel() { return label; }

    public void setLabel(String label) { this.label = label; }

    public String getVibe() {
        return vibe;
    }

    public void setVibe(String vibe) {
        this.vibe = vibe;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public boolean isSyncedToDevice() {
        return syncedToDevice;
    }

    public void setSyncedToDevice(boolean syncedToDevice) {
        this.syncedToDevice = syncedToDevice;
    }

    public int getSnoozeCount() {
        return snoozeCount;
    }

    public void setSnoozeCount(int snoozeCount) {
        this.snoozeCount = snoozeCount;
    }

    public int getId() {
        return id;
    }

    public void setAlarmId(int id) {
        this.id = id;
    }

    public int getSnoozeLength() {
        return snoozeLength;
    }

    public void setSnoozeLength(int snoozeLength) {
        this.snoozeLength = snoozeLength;
    }

    public boolean isRecurring() {
        return recurring;
    }

    public void setRecurring(boolean recurring) {
        this.recurring = recurring;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public List<String> getWeekDays() {
        return weekDays;
    }

    public void setWeekDays(List<String> weekDays) {
        this.weekDays = weekDays;
    }
}
