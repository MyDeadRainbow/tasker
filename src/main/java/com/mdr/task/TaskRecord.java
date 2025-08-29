package com.mdr.task;

import java.time.LocalDateTime;

import org.json.JSONObject;

import com.mdr.Props;

public record TaskRecord(String jarPath, String name, LocalDateTime startTime, int interval, Runnable task) {
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("jarPath", jarPath);
        json.put("name", name);
        json.put("startTime", startTime.toString());
        json.put("interval", interval);
        return json;
    }

    public static TaskRecord fromJson(JSONObject json) {
        String jarPath = json.getString("jarPath");
        String name = json.getString("name");
        LocalDateTime startTime = LocalDateTime.parse(json.getString("startTime"), Props.DATE_FORMAT.get());
        int interval = json.getInt("interval");
        return new TaskRecord(jarPath, name, startTime, interval, null);
    }

    public TaskRecord copyWith(String jarPath, String name, LocalDateTime startTime, int interval, Runnable task) {
        return new TaskRecord(jarPath != null ? jarPath : this.jarPath,
                name != null ? name : this.name,
                startTime != null ? startTime : this.startTime,
                interval != 0 ? interval : this.interval,
                task != null ? task : this.task);
    }
}
