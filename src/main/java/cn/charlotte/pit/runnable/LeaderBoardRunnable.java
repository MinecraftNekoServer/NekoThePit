package cn.charlotte.pit.runnable;

import cn.charlotte.pit.ThePit;
import cn.charlotte.pit.data.LeaderBoardEntry;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @Author: EmptyIrony
 * @Date: 2021/1/3 12:57
 */

public class LeaderBoardRunnable implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(LeaderBoardRunnable.class.getName());
    private final ThePit instance;
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public LeaderBoardRunnable(ThePit instance) {
        this.instance = instance;
        scheduler.scheduleAtFixedRate(this, 0, 1, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        try {
            List<Map<String, Object>> documents = loadDocuments();
            List<LeaderBoardEntry> entries = processDocuments(documents);
            updateLeaderBoardEntries(entries);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "更新排行榜数据时发生错误：", e);
        }
    }

    private List<Map<String, Object>> loadDocuments() {
        // TODO: Implement leaderboard loading from MySQL
        // For now, returning an empty list
        // This would require complex MySQL query to fetch player data ordered by totalExp
        return new ArrayList<>();
    }

    private List<LeaderBoardEntry> processDocuments(List<Map<String, Object>> documents) {
        List<LeaderBoardEntry> entries = new ArrayList<>();
        for (Map<String, Object> document : documents) {
            String playerName = (String) document.get("playerName");
            Double totalExp = (Double) document.get("totalExp");
            if (playerName != null && totalExp != null) {
                entries.add(new LeaderBoardEntry(playerName, UUID.randomUUID(), 0, totalExp, 0));
            }
        }
        return entries;
    }

    private Double getExperience(Map<String, Object> document) {
        Object experience = document.get("totalExp");
        if (experience instanceof Integer) {
            return ((Integer) experience).doubleValue();
        } else if (experience instanceof Double) {
            return (Double) experience;
        }
        return null;
    }

    private void updateLeaderBoardEntries(List<LeaderBoardEntry> entries) {
        synchronized (LeaderBoardEntry.class) {
            LeaderBoardEntry.setLeaderBoardEntries(entries);
        }
    }

    public void shutdown() {
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(1, TimeUnit.MINUTES)) {
                LOGGER.warning("排行榜更新任务未能在1分钟内停止，强制关闭...");
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            LOGGER.severe("在等待排行榜更新任务停止时被中断。");
            Thread.currentThread().interrupt();
        }
    }
}