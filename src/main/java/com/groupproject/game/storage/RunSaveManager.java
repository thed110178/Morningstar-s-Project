package com.groupproject.game.storage;

import com.groupproject.game.core.GameConfig;
import com.groupproject.game.state.PlayingState;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Map;

/**
 * Quản lý Lưu trữ và Khôi phục Tiến trình Ván Đấu (Save Run Checkpoint Manager).
 *
 * <p>WHY: Cho phép người chơi tạm dừng chiến dịch và quay về Menu hoặc thoát game đột ngột
 * mà không bị mất tiến trình. Tự động kiểm tra tính tương thích phiên bản game: Nếu game có
 * bản cập nhật mới (Mã phiên bản khác với mã trong file lưu), bản lưu cũ sẽ tự động bị xóa
 * để đảm bảo an toàn tuyệt đối và tính nhất quán cho trò chơi.
 */
public final class RunSaveManager {

    private static String saveFilePath = "save/run_checkpoint.dat";

    private RunSaveManager() {
        // Lớp tiện ích tĩnh, ngăn khởi tạo
    }

    /**
     * Cho phép cấu hình đường dẫn file lưu (phục vụ Unit Test độc lập).
     */
    public static void setSaveFilePath(String path) {
        saveFilePath = (path != null && !path.trim().isEmpty()) ? path : "save/run_checkpoint.dat";
    }

    public static String getSaveFilePath() {
        return saveFilePath;
    }

    /**
     * Kiểm tra xem hiện có bản lưu tiến trình hợp lệ cùng phiên bản game hay không.
     * Nếu phát hiện bản lưu thuộc phiên bản game cũ khác biệt, hàm tự động xóa bỏ file lưu và trả về {@code false}.
     *
     * @return {@code true} nếu có bản lưu hợp lệ sẵn sàng để Tiếp tục chơi; {@code false} nếu không có hoặc bản lưu cũ.
     */
    public static boolean hasValidSave() {
        File file = new File(saveFilePath);
        if (!file.exists() || !file.isFile() || file.length() == 0) {
            return false;
        }

        RunProgressData data = loadRun();
        return data != null && data.getCurrentStage() >= 1;
    }

    /**
     * Lấy số Stage của ván đấu đang chơi dở (để hiển thị trên nhãn nút Menu).
     *
     * @return Số Stage (1, 2, 3, 4) hoặc 1 nếu không xác định.
     */
    public static int getSavedStage() {
        RunProgressData data = loadRun();
        if (data != null) {
            return data.getCurrentStage();
        }
        return 1;
    }

    /**
     * Lưu trạng thái ván đấu hiện tại từ PlayingState xuống đĩa.
     *
     * @param state Trạng thái chiến dịch PlayingState đang hoạt động.
     * @return {@code true} nếu lưu thành công; {@code false} nếu thất bại.
     */
    public static boolean saveRun(PlayingState state) {
        if (state == null || state.isStandaloneBossFight() || state.isPlayerDead() || state.isExtracting()) {
            return false;
        }

        RunProgressData data = state.exportSaveData();
        if (data == null) {
            return false;
        }
        return saveRunData(data);
    }

    /**
     * Ghi đối tượng dữ liệu tiến trình RunProgressData xuống file lưu trữ.
     */
    public static boolean saveRunData(RunProgressData data) {
        if (data == null) {
            return false;
        }

        data.setGameVersion(GameConfig.GAME_BUILD_VERSION);
        data.setTimestamp(System.currentTimeMillis());

        File file = new File(saveFilePath);
        File parentDir = file.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }

        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8))) {

            writer.write("# DONTKNOW RUN CHECKPOINT");
            writer.newLine();
            writer.write("version=" + data.getGameVersion());
            writer.newLine();
            writer.write("timestamp=" + data.getTimestamp());
            writer.newLine();
            writer.write("stage=" + data.getCurrentStage());
            writer.newLine();
            writer.write("playerHealth=" + data.getPlayerHealth());
            writer.newLine();
            writer.write("playerMaxHealth=" + data.getPlayerMaxHealth());
            writer.newLine();
            writer.write("playerCredits=" + data.getPlayerCredits());
            writer.newLine();
            writer.write("activeWeaponSlot=" + data.getActiveWeaponSlot());
            writer.newLine();

            // Thống kê chiến trường
            writer.write("enemiesKilled=" + data.getEnemiesKilled());
            writer.newLine();
            writer.write("bossesDefeated=" + data.getBossesDefeated());
            writer.newLine();
            writer.write("damageDealt=" + data.getDamageDealt());
            writer.newLine();
            writer.write("shotsFired=" + data.getShotsFired());
            writer.newLine();
            writer.write("shotsHit=" + data.getShotsHit());
            writer.newLine();
            writer.write("creditsCollected=" + data.getCreditsCollected());
            writer.newLine();

            // Kho dự trữ đạn dược
            writer.newLine();
            writer.write("[AMMO]");
            writer.newLine();
            for (Map.Entry<String, Integer> entry : data.getAmmoReserves().entrySet()) {
                writer.write(entry.getKey() + "=" + entry.getValue());
                writer.newLine();
            }

            // Cấp độ Buff nhân vật
            writer.newLine();
            writer.write("[BUFFS]");
            writer.newLine();
            for (Map.Entry<String, Integer> entry : data.getBuffLevels().entrySet()) {
                writer.write(entry.getKey() + "=" + entry.getValue());
                writer.newLine();
            }

            // Kho vũ khí
            writer.newLine();
            writer.write("[WEAPONS]");
            writer.newLine();
            for (WeaponSaveData w : data.getWeapons()) {
                // Định dạng: weapon=slotIndex;name;level;dmgStars;rpmStars;accStars;critChance;critMultiplier;currentAmmo;sTier
                String wLine = String.format("weapon=%d;%s;%d;%d;%d;%d;%.4f;%.4f;%d;%b",
                        w.getSlotIndex(),
                        w.getWeaponName(),
                        w.getLevel(),
                        w.getDamageStars(),
                        w.getFireRateStars(),
                        w.getAccuracyStars(),
                        w.getCritChance(),
                        w.getCritMultiplier(),
                        w.getCurrentAmmo(),
                        w.isSTier());
                writer.write(wLine);
                writer.newLine();
            }

            writer.flush();
            return true;
        } catch (Exception e) {
            System.err.println("[RunSaveManager] Lỗi khi ghi file lưu checkpoint: " + e.getMessage());
            return false;
        }
    }

    /**
     * Đọc và phân tích bản lưu tiến trình từ đĩa.
     * Nếu phiên bản lưu không khớp với GameConfig.GAME_BUILD_VERSION hiện tại, file save sẽ bị xóa và trả về {@code null}.
     *
     * @return Đối tượng RunProgressData nếu hợp lệ; {@code null} nếu không có, phiên bản cũ, hoặc file lỗi.
     */
    public static RunProgressData loadRun() {
        File file = new File(saveFilePath);
        if (!file.exists() || !file.isFile() || file.length() == 0) {
            return null;
        }

        RunProgressData data = new RunProgressData();
        String currentSection = "HEADER";

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                if (line.startsWith("[") && line.endsWith("]")) {
                    currentSection = line.substring(1, line.length() - 1).toUpperCase();
                    continue;
                }

                int eqIdx = line.indexOf('=');
                if (eqIdx == -1) {
                    continue;
                }
                String key = line.substring(0, eqIdx).trim();
                String val = line.substring(eqIdx + 1).trim();

                switch (currentSection) {
                    case "HEADER" -> {
                        switch (key) {
                            case "version" -> {
                                data.setGameVersion(val);
                                // KIỂM TRA PHIÊN BẢN: Nếu khác phiên bản hiện tại -> Hủy và xóa save!
                                if (!GameConfig.GAME_BUILD_VERSION.equals(val)) {
                                    System.out.println("[RunSaveManager] Phát hiện bản lưu cũ (Phiên bản " + val
                                            + " != " + GameConfig.GAME_BUILD_VERSION + "). Tự động xóa để cập nhật!");
                                    reader.close();
                                    deleteSave();
                                    return null;
                                }
                            }
                            case "timestamp" -> data.setTimestamp(parseLong(val, 0L));
                            case "stage" -> data.setCurrentStage(parseInt(val, 1));
                            case "playerHealth" -> data.setPlayerHealth(parseInt(val, 100));
                            case "playerMaxHealth" -> data.setPlayerMaxHealth(parseInt(val, 100));
                            case "playerCredits" -> data.setPlayerCredits(parseInt(val, 0));
                            case "activeWeaponSlot" -> data.setActiveWeaponSlot(parseInt(val, 1));
                            case "enemiesKilled" -> data.setEnemiesKilled(parseInt(val, 0));
                            case "bossesDefeated" -> data.setBossesDefeated(parseInt(val, 0));
                            case "damageDealt" -> data.setDamageDealt(parseInt(val, 0));
                            case "shotsFired" -> data.setShotsFired(parseInt(val, 0));
                            case "shotsHit" -> data.setShotsHit(parseInt(val, 0));
                            case "creditsCollected" -> data.setCreditsCollected(parseInt(val, 0));
                        }
                    }
                    case "AMMO" -> {
                        data.getAmmoReserves().put(key, parseInt(val, 0));
                    }
                    case "BUFFS" -> {
                        data.getBuffLevels().put(key, parseInt(val, 0));
                    }
                    case "WEAPONS" -> {
                        if ("weapon".equalsIgnoreCase(key)) {
                            WeaponSaveData wData = parseWeaponData(val);
                            if (wData != null) {
                                data.getWeapons().add(wData);
                            }
                        }
                    }
                }
            }

            // Nếu đọc xong mà không có mã phiên bản hoặc không có stage hợp lệ
            if (data.getGameVersion().isEmpty() || data.getCurrentStage() < 1) {
                deleteSave();
                return null;
            }

            return data;
        } catch (Exception e) {
            System.err.println("[RunSaveManager] Lỗi khi đọc file lưu checkpoint: " + e.getMessage());
            deleteSave();
            return null;
        }
    }

    /**
     * Xóa file lưu tiến trình ván đấu (khi tử trận, hoàn thành chiến dịch hoặc bắt đầu chơi mới).
     */
    public static boolean deleteSave() {
        File file = new File(saveFilePath);
        if (file.exists() && file.isFile()) {
            boolean deleted = file.delete();
            if (deleted) {
                System.out.println("[RunSaveManager] Đã xóa bản lưu tiến trình checkpoint: " + saveFilePath);
            }
            return deleted;
        }
        return false;
    }

    private static WeaponSaveData parseWeaponData(String value) {
        try {
            String[] parts = value.split(";");
            if (parts.length < 10) return null;

            int slotIndex = Integer.parseInt(parts[0].trim());
            String name = parts[1].trim();
            int level = Integer.parseInt(parts[2].trim());
            int dmgStars = Integer.parseInt(parts[3].trim());
            int rpmStars = Integer.parseInt(parts[4].trim());
            int accStars = Integer.parseInt(parts[5].trim());
            double critChance = Double.parseDouble(parts[6].trim().replace(',', '.'));
            double critMult = Double.parseDouble(parts[7].trim().replace(',', '.'));
            int currentAmmo = Integer.parseInt(parts[8].trim());
            boolean sTier = Boolean.parseBoolean(parts[9].trim());

            return new WeaponSaveData(slotIndex, name, level, dmgStars, rpmStars, accStars,
                    critChance, critMult, currentAmmo, sTier);
        } catch (Exception e) {
            return null;
        }
    }

    private static int parseInt(String str, int defaultVal) {
        try {
            return Integer.parseInt(str.trim());
        } catch (Exception e) {
            return defaultVal;
        }
    }

    private static long parseLong(String str, long defaultVal) {
        try {
            return Long.parseLong(str.trim());
        } catch (Exception e) {
            return defaultVal;
        }
    }
}
