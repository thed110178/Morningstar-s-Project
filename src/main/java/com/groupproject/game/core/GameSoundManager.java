package com.groupproject.game.core;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.SourceDataLine;
import javazoom.jl.decoder.Bitstream;
import javazoom.jl.decoder.Decoder;
import javazoom.jl.decoder.Header;
import javazoom.jl.decoder.SampleBuffer;

/**
 * Trình quản lý hiệu ứng âm thanh (Sound Effects - SFX) trung tâm của Project DontKnow.
 *
 * <p>Đặc tính kỹ thuật & Tối ưu hóa:
 * <ul>
 *   <li><b>Bộ trộn âm thanh phần mềm thời gian thực (Real-Time Polyphonic PCM Software Mixer)</b>:
 *       Trộn toàn bộ các kênh âm thanh đang phát trực tiếp trong RAM và đẩy ra duy nhất một đường
 *       {@link SourceDataLine} chuẩn 44.1kHz 16-bit Stereo. Loại bỏ triệt để giới hạn số lượng kênh phần cứng
 *       và lỗi race-condition của {@code DirectClip} trên Windows khi sấy súng liên thanh tốc độ cao,
 *       cho phép giữ trọn vẹn tiếng vọng (reverb tail) dài và trầm ấm của súng mà không bao giờ mất tiếng.</li>
 *   <li><b>Bộ nén mềm chống vỡ tiếng (Soft-Knee Polyphonic Limiter)</b>: Tự động cân bằng biên độ khi
 *       hàng chục tiếng súng, tiếng nổ và tiếng va chạm vang lên chồng lấp cùng lúc.</li>
 *   <li><b>Âm lượng mặc định 50% & Lưu cài đặt người chơi</b>: Tự động ghi nhớ và nạp cài đặt âm lượng
 *       và trạng thái Mute tại {@code save/audio_settings.properties}.</li>
 *   <li><b>Đồng bộ thời gian nạp đạn & Hủy nạp tức thì</b>: Hỗ trợ ngắt ngay âm thanh nạp đạn
 *       ({@link #stopReload()}) khi người chơi đổi vũ khí hoặc hủy chu kỳ nạp.</li>
 * </ul>
 */
public final class GameSoundManager {

    private static final Logger LOGGER = Logger.getLogger(GameSoundManager.class.getName());

    /** Định dạng âm thanh chuẩn thống nhất: 44.1kHz, 16-bit signed, Stereo, Little-Endian. */
    private static final AudioFormat MIXER_FORMAT = new AudioFormat(44100.0f, 16, 2, true, false);

    /** Số lượng khung mẫu stereo mỗi chu kỳ trộn (~5.8ms độ trễ siêu thấp). */
    private static final int MIX_FRAMES = 256;
    private static final int MIX_SHORTS = MIX_FRAMES * 2;
    private static final int MIX_BYTES = MIX_SHORTS * 2;

    /** Tệp lưu trữ cấu hình âm thanh của người chơi (hỗ trợ đổi đường dẫn khi chạy Unit Test). */
    private static String settingsFilePath = "save/audio_settings.properties";

    public static void setSettingsFilePath(String path) {
        settingsFilePath = (path != null && !path.trim().isEmpty()) ? path : "save/audio_settings.properties";
    }

    public static String getSettingsFilePath() {
        return settingsFilePath;
    }

    public enum SoundEffect {
        BUY_COMPLETE("buy_complete.wav", 6),
        BUY_FAILED("buy_failed.wav", 6),
        COIN_PICK_UP("coin_pick_up.wav", 8),
        DODGE("dodge.wav", 6),
        ENEMY_BOSS_CHARGE_WARNING("enemy_boss_charge_warning.wav", 4),
        ENEMY_BOSS_EXPLOSION("enemy_boss_explosion.wav", 6),
        ENEMY_BOSS_FIRE("enemy_boss_fire.wav", 10),
        ENEMY_BOSS_WALLCRASH("enemy_boss_wallcrash.wav", 4),
        ENEMY_UNIVERSAL_FIRE("enemy_universal_fire.wav", 10),
        GAME_OVER("gameover.wav", 2),
        GUN_MELEE("gun_melee.wav", 6),
        GUN_PISTOL_DRAW("gun_pistol_draw.wav", 2),
        GUN_PISTOL_RELOAD("gun_pistol_reload.wav", 2),
        GUN_PISTOL_SHOOT("gun_pistol_shoot.wav", 10),
        GUN_RIFLE_DRAW("gun_rifle_draw.wav", 2),
        GUN_RIFLE_RELOAD("gun_rifle_reload.wav", 2),
        GUN_RIFLE_SHOOT("gun_rifle_shoot.wav", 20), // Súng trường sấy auto + đuôi vọng dài: 20 kênh trộn đồng thời
        GUN_SCR_DRAW("gun_scr_draw.wav", 2),
        GUN_SCR_RELOAD("gun_scr_reload.wav", 2),
        GUN_SCR_SHOOT("gun_scr_shoot.wav", 8),
        GUN_SHOTGUN_DRAW("gun_shotgun_draw.wav", 2),
        GUN_SHOTGUN_LOADING_AMMO("gun_shotgun_loadingAmmo.wav", 6), // Tiếng bơm tay lên đạn sau mỗi phát bắn của Shotgun (0.75s)
        GUN_SHOTGUN_RELOAD("gun_shotgun_reload.wav", 8), // Súng shotgun nạp từng viên: 8 kênh độc lập
        GUN_SHOTGUN_SHOOT("gun_shotgun_shoot.wav", 8),
        GUN_SMG_DRAW("gun_smg_draw.wav", 2),
        GUN_SMG_RELOAD("gun_smg_reload.wav", 2),
        GUN_SMG_SHOOT("gun_smg_shoot.wav", 24),     // Súng tiểu liên sấy siêu tốc + đuôi vọng dài: 24 kênh trộn đồng thời
        GUN_SNIPER_DRAW("gun_sniper_draw.wav", 2),
        GUN_SNIPER_LOADING_AMMO("gun_sniper_loadingAmmo.wav", 6), // Tiếng kéo quy lát lên đạn sau mỗi lần bắn của Sniper
        GUN_SNIPER_RELOAD("gun_sniper_reload.wav", 2),
        GUN_SNIPER_SHOOT("gun_sniper_shoot.wav", 8), // Tiếng nổ súng bắn tỉa Sniper
        HEALING("healing.wav", 6),
        HIT_BOSS("hit_boss.wav", 8),
        HIT_CRATE("hit_crate.wav", 8),
        HIT_CRATE_BREAKING("hit_crate_breaking.wav", 6),
        HIT_TARGET("hit_target.wav", 12),
        HIT_UNBREAKABLE_WALL("hit_unbreakable_wall.wav", 8),
        INTERACT("interact.wav", 8),
        UI_INTERACT("ui_interact.wav", 8);

        private final String filename;
        private final int poolSize;
        private final boolean optional;

        SoundEffect(String filename, int poolSize) {
            this(filename, poolSize, false);
        }

        SoundEffect(String filename, int poolSize, boolean optional) {
            this.filename = filename;
            this.poolSize = poolSize;
            this.optional = optional;
        }

        public String getFilename() {
            return filename;
        }

        public int getPoolSize() {
            return poolSize;
        }

        public boolean isOptional() {
            return optional;
        }
    }

    /**
     * Danh mục các bản nhạc nền (Background Music - BGM) của trò chơi.
     */
    public enum BgmTrack {
        MENU("/BGM/In The Sky - Evening Telecast.mp3", true, false, 163.0),
        STAGE_1_3("/BGM/Survival Mode - Blue Deer Studio.mp3", true, false, 129.3),
        THE_RANGE("/BGM/Play Again_ - Evening Telecast.mp3", true, false, 147.0),
        GAME_OVER("/BGM/Game Over - Evening Telecast.mp3", false, false, 140.8),
        BOSS_INTRO("/BGM/360 No Scope - The Soundlings/Intro.wav", false, true, 0.0),
        BOSS_LOOP("/BGM/360 No Scope - The Soundlings/Middle_loop.wav", true, true, 0.0),
        BOSS_END("/BGM/360 No Scope - The Soundlings/end.wav", false, true, 0.0);

        private final String resourcePath;
        private final boolean loop;
        private final boolean wavFormat;
        private final double maxDurationSeconds;

        BgmTrack(String resourcePath, boolean loop, boolean wavFormat, double maxDurationSeconds) {
            this.resourcePath = resourcePath;
            this.loop = loop;
            this.wavFormat = wavFormat;
            this.maxDurationSeconds = maxDurationSeconds;
        }

        public String getResourcePath() {
            return resourcePath;
        }

        public boolean isLoop() {
            return loop;
        }

        public boolean isWavFormat() {
            return wavFormat;
        }

        public double getMaxDurationSeconds() {
            return maxDurationSeconds;
        }
    }

    /**
     * Luồng phát nhạc nền (BGM Stream) thời gian thực hỗ trợ cả WAV (PCM) và MP3 (JLayer),
     * cho phép lặp vô hạn (Gapless Loop), cắt bỏ đoạn đầu kèm Fade-In (cho Boss Intro),
     * và tự động chuyển bài nối tiếp (cho Game Over -> The Range và Boss Intro -> Boss Loop).
     */
    private static final class ActiveBgmStream {
        final BgmTrack track;
        final byte[] rawData;
        final boolean loop;
        volatile BgmTrack nextTrack;
        final int fadeInTotalFrames;
        int framesPlayed = 0;

        // Trạng thái dành cho WAV (PCM 44.1kHz 16-bit Stereo)
        final int wavDataOffset;
        final int wavTotalFrames;
        int wavCurrentFrame;

        // Trạng thái dành cho MP3 (JLayer streaming decoder)
        final int mp3MaxFrames;
        Bitstream bitstream;
        Decoder decoder;
        short[] mp3FramePcm = new short[4608];
        int mp3FramePos = 0;
        int mp3FrameLen = 0;

        ActiveBgmStream(
                BgmTrack track,
                byte[] rawData,
                boolean loop,
                BgmTrack nextTrack,
                double skipFromEndDurationSec,
                double fadeInSec) {
            this.track = track;
            this.rawData = rawData;
            this.loop = loop;
            this.nextTrack = nextTrack;
            this.fadeInTotalFrames = fadeInSec > 0.0 ? (int) Math.round(fadeInSec * 44100.0) : 0;

            if (track.isWavFormat()) {
                this.wavDataOffset = findWavDataOffset(rawData);
                this.wavTotalFrames = Math.max(0, (rawData.length - this.wavDataOffset) / 4);
                if (skipFromEndDurationSec > 0.0) {
                    int keepFrames = (int) Math.round(skipFromEndDurationSec * 44100.0);
                    this.wavCurrentFrame = Math.max(0, this.wavTotalFrames - keepFrames);
                } else {
                    this.wavCurrentFrame = 0;
                }
                this.mp3MaxFrames = 0;
            } else {
                this.wavDataOffset = 0;
                this.wavTotalFrames = 0;
                this.wavCurrentFrame = 0;
                this.mp3MaxFrames = track.getMaxDurationSeconds() > 0.0
                        ? (int) Math.round(track.getMaxDurationSeconds() * 44100.0)
                        : Integer.MAX_VALUE;
                openMp3Stream();
            }
        }

        private void openMp3Stream() {
            closeMp3Stream();
            this.bitstream = new Bitstream(new ByteArrayInputStream(rawData));
            this.decoder = new Decoder();
            this.mp3FramePos = 0;
            this.mp3FrameLen = 0;
            this.framesPlayed = 0;
        }

        void closeMp3Stream() {
            if (bitstream != null) {
                try {
                    bitstream.close();
                } catch (Exception ignored) {
                }
                bitstream = null;
            }
            decoder = null;
        }

        void restartLoop() {
            if (track.isWavFormat()) {
                this.wavCurrentFrame = 0;
                this.framesPlayed = 0;
            } else {
                openMp3Stream();
            }
        }

        boolean readNextStereoFrame(short[] outLR) {
            if (track.isWavFormat()) {
                if (wavCurrentFrame >= wavTotalFrames) {
                    return false;
                }
                int idx = wavDataOffset + wavCurrentFrame * 4;
                if (idx + 3 >= rawData.length) {
                    return false;
                }
                outLR[0] = (short) ((rawData[idx + 1] << 8) | (rawData[idx] & 0xFF));
                outLR[1] = (short) ((rawData[idx + 3] << 8) | (rawData[idx + 2] & 0xFF));
                wavCurrentFrame++;
                framesPlayed++;
                return true;
            } else {
                if (framesPlayed >= mp3MaxFrames) {
                    return false;
                }
                while (mp3FramePos + 1 >= mp3FrameLen) {
                    if (!decodeNextMp3Frame()) {
                        return false;
                    }
                }
                outLR[0] = mp3FramePcm[mp3FramePos];
                outLR[1] = mp3FramePcm[mp3FramePos + 1];
                mp3FramePos += 2;
                framesPlayed++;
                return true;
            }
        }

        private boolean decodeNextMp3Frame() {
            if (bitstream == null || decoder == null) {
                return false;
            }
            try {
                while (true) {
                    Header header = bitstream.readFrame();
                    if (header == null) {
                        return false;
                    }
                    SampleBuffer sb = (SampleBuffer) decoder.decodeFrame(header, bitstream);
                    int len = sb.getBufferLength();
                    int ch = sb.getChannelCount();
                    short[] buf = sb.getBuffer();
                    if (len > 0) {
                        if (ch == 1) {
                            if (mp3FramePcm.length < len * 2) {
                                mp3FramePcm = new short[len * 2];
                            }
                            for (int i = 0; i < len; i++) {
                                short s = buf[i];
                                mp3FramePcm[i * 2] = s;
                                mp3FramePcm[i * 2 + 1] = s;
                            }
                            mp3FrameLen = len * 2;
                        } else {
                            if (mp3FramePcm.length < len) {
                                mp3FramePcm = new short[len];
                            }
                            System.arraycopy(buf, 0, mp3FramePcm, 0, len);
                            mp3FrameLen = len;
                        }
                        mp3FramePos = 0;
                        bitstream.closeFrame();
                        return true;
                    }
                    bitstream.closeFrame();
                }
            } catch (Exception e) {
                return false;
            }
        }

        float currentFadeGain() {
            if (fadeInTotalFrames <= 0 || framesPlayed >= fadeInTotalFrames) {
                return 1.0f;
            }
            return Math.max(0.0f, Math.min(1.0f, (float) framesPlayed / (float) fadeInTotalFrames));
        }
    }

    private static int findWavDataOffset(byte[] wavBytes) {
        if (wavBytes == null || wavBytes.length < 44) {
            return 0;
        }
        int limit = Math.min(wavBytes.length - 8, 512);
        for (int i = 12; i < limit; i++) {
            if (wavBytes[i] == 'd' && wavBytes[i + 1] == 'a' && wavBytes[i + 2] == 't' && wavBytes[i + 3] == 'a') {
                return i + 8;
            }
        }
        return 44;
    }

    /** Biểu diễn một luồng âm thanh đang phát trong bộ trộn phần mềm. */
    private static final class ActiveVoice {
        final SoundEffect effect;
        final short[] samples;
        final float volumeFactor;
        final boolean reloadChannel;
        int cursor = 0;
        volatile boolean stopped = false;

        ActiveVoice(SoundEffect effect, short[] samples, float volumeFactor, boolean reloadChannel) {
            this.effect = effect;
            this.samples = samples;
            this.volumeFactor = volumeFactor;
            this.reloadChannel = reloadChannel;
        }
    }

    private static final Map<SoundEffect, byte[]> SOUND_DATA = new EnumMap<>(SoundEffect.class);
    private static final Map<SoundEffect, short[]> PCM_SAMPLES = new EnumMap<>(SoundEffect.class);
    private static final Map<BgmTrack, byte[]> BGM_DATA = new EnumMap<>(BgmTrack.class);
    private static final CopyOnWriteArrayList<ActiveVoice> ACTIVE_VOICES = new CopyOnWriteArrayList<>();
    private static final Object BGM_LOCK = new Object();
    private static volatile ActiveBgmStream activeBgm = null;

    /** Hệ số âm lượng tương đối của BGM để cân bằng hài hòa với tiếng súng và SFX. */
    private static final float BGM_RELATIVE_GAIN = 0.36f;

    private static volatile boolean initialized = false;
    private static volatile boolean mixerRunning = false;
    private static Thread mixerThread = null;
    private static SourceDataLine mixerLine = null;

    private static volatile boolean muted = false;
    private static volatile boolean sfxMuted = false;
    private static volatile boolean bgmMuted = false;
    private static volatile float masterVolume = 0.5f; // Mặc định 50% theo yêu cầu
    private static volatile float sfxVolume = 0.5f;    // Mặc định 50% cho hiệu ứng âm thanh (SFX)
    private static volatile float bgmVolume = 0.5f;    // Mặc định 50% cho nhạc nền (BGM)

    private GameSoundManager() {
        // Lớp tiện ích tĩnh, không khởi tạo đối tượng trực tiếp
    }

    /**
     * Khởi tạo, tải cài đặt người chơi, giải mã trước toàn bộ mẫu PCM 16-bit vào RAM
     * và khởi chạy luồng trộn âm thanh phần mềm thời gian thực.
     */
    public static synchronized void init() {
        if (initialized) {
            return;
        }
        initialized = true;

        // 1. Tải cài đặt âm lượng đã lưu của người chơi (mặc định 50%)
        loadSettings();

        // 2. Nạp dữ liệu âm thanh và giải mã sang mảng short[] PCM 16-bit Stereo
        for (SoundEffect effect : SoundEffect.values()) {
            byte[] data = loadSoundData(effect.getFilename());
            if (data != null && data.length > 0) {
                SOUND_DATA.put(effect, data);
                short[] pcm = decodeWavToPcmShorts(data);
                if (pcm != null && pcm.length > 0) {
                    PCM_SAMPLES.put(effect, pcm);
                }
            } else {
                LOGGER.log(Level.FINE, "Không tìm thấy file âm thanh: {0}", effect.getFilename());
            }
        }

        // 3. Khởi động luồng Software Audio Mixer trên một SourceDataLine duy nhất
        startSoftwareMixer();
    }

    /**
     * Giải mã mảng byte của tệp WAV chuẩn 16-bit Little-Endian sang mảng mẫu short[].
     */
    private static short[] decodeWavToPcmShorts(byte[] wavFileBytes) {
        try (AudioInputStream ais = AudioSystem.getAudioInputStream(new ByteArrayInputStream(wavFileBytes))) {
            byte[] rawPcm = ais.readAllBytes();
            int sampleCount = rawPcm.length / 2;
            short[] shorts = new short[sampleCount];
            for (int i = 0; i < sampleCount; i++) {
                int lo = rawPcm[i * 2] & 0xFF;
                int hi = rawPcm[i * 2 + 1]; // signed high byte
                shorts[i] = (short) ((hi << 8) | lo);
            }
            return shorts;
        } catch (Exception e) {
            // Fallback nếu header WAV tối giản: bỏ qua 44 bytes đầu
            if (wavFileBytes.length > 44) {
                int sampleCount = (wavFileBytes.length - 44) / 2;
                short[] shorts = new short[sampleCount];
                for (int i = 0; i < sampleCount; i++) {
                    int offset = 44 + i * 2;
                    int lo = wavFileBytes[offset] & 0xFF;
                    int hi = wavFileBytes[offset + 1];
                    shorts[i] = (short) ((hi << 8) | lo);
                }
                return shorts;
            }
            return null;
        }
    }

    /**
     * Khởi chạy luồng trộn âm thanh phần mềm độ trễ thấp (Real-Time Software PCM Mixer).
     */
    private static synchronized void startSoftwareMixer() {
        if (mixerRunning) {
            return;
        }
        try {
            DataLine.Info info = new DataLine.Info(SourceDataLine.class, MIXER_FORMAT);
            if (!AudioSystem.isLineSupported(info)) {
                return;
            }
            mixerLine = (SourceDataLine) AudioSystem.getLine(info);
            // Buffer phần cứng 4096 bytes (~23ms ở 44.1kHz Stereo 16-bit) đảm bảo phản hồi tức thì và không bị vấp
            mixerLine.open(MIXER_FORMAT, 4096);
            mixerLine.start();
            mixerRunning = true;

            mixerThread = new Thread(GameSoundManager::runMixerLoop, "AloneInTheZone-AudioMixer");
            mixerThread.setDaemon(true);
            mixerThread.setPriority(Thread.MAX_PRIORITY);
            mixerThread.start();
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Không thể mở SourceDataLine (môi trường Headless/CI)", e);
            mixerRunning = false;
            mixerLine = null;
        }
    }

    /**
     * Vòng lặp trộn đa kênh PCM thời gian thực.
     * Cộng gộp toàn bộ các luồng âm thanh đang phát với bộ nén mềm (Soft-Knee Limiter),
     * giữ nguyên vẹn tiếng vọng của súng mà không bao giờ bị mất tiếng hay vỡ âm.
     */
    private static void runMixerLoop() {
        final float[] accumBuffer = new float[MIX_SHORTS];
        final byte[] outBytes = new byte[MIX_BYTES];
        final List<ActiveVoice> finishedVoices = new ArrayList<>(32);
        final short[] bgmPair = new short[2];

        while (mixerRunning) {
            SourceDataLine line = mixerLine;
            if (line == null || !line.isOpen()) {
                break;
            }

            boolean sfxEffectiveMuted = muted || sfxMuted || sfxVolume <= 0.001f;
            boolean bgmEffectiveMuted = muted || bgmMuted || bgmVolume <= 0.001f;

            if ((ACTIVE_VOICES.isEmpty() && activeBgm == null) || (sfxEffectiveMuted && bgmEffectiveMuted)) {
                if (!ACTIVE_VOICES.isEmpty() && sfxEffectiveMuted) {
                    ACTIVE_VOICES.clear();
                }
                Arrays.fill(outBytes, (byte) 0);
                try {
                    line.write(outBytes, 0, MIX_BYTES);
                } catch (Exception e) {
                    break;
                }
                continue;
            }

            Arrays.fill(accumBuffer, 0.0f);
            finishedVoices.clear();

            // 1. Trộn luồng nhạc nền (BGM) nếu đang kích hoạt
            ActiveBgmStream bgm = activeBgm;
            if (bgm != null) {
                final float baseBgmGain = bgmEffectiveMuted ? 0.0f : (bgmVolume * BGM_RELATIVE_GAIN);
                synchronized (BGM_LOCK) {
                    bgm = activeBgm;
                    for (int f = 0; f < MIX_FRAMES && bgm != null; f++) {
                        if (!bgm.readNextStereoFrame(bgmPair)) {
                            // Đã chạy hết bài hiện tại -> kiểm tra chuyển bài nối tiếp hoặc lặp lại
                            if (bgm.nextTrack != null) {
                                BgmTrack next = bgm.nextTrack;
                                bgm.closeMp3Stream();
                                byte[] nextBytes = loadBgmData(next);
                                if (nextBytes != null && nextBytes.length > 0) {
                                    bgm = new ActiveBgmStream(next, nextBytes, next.isLoop(), null, 0.0, 0.0);
                                    activeBgm = bgm;
                                } else {
                                    bgm = null;
                                    activeBgm = null;
                                    break;
                                }
                            } else if (bgm.loop) {
                                bgm.restartLoop();
                            } else {
                                bgm.closeMp3Stream();
                                bgm = null;
                                activeBgm = null;
                                break;
                            }
                            if (bgm == null || !bgm.readNextStereoFrame(bgmPair)) {
                                break;
                            }
                        }
                        if (baseBgmGain > 0.0001f) {
                            float gain = baseBgmGain * bgm.currentFadeGain();
                            accumBuffer[f * 2] += bgmPair[0] * gain;
                            accumBuffer[f * 2 + 1] += bgmPair[1] * gain;
                        }
                    }
                }
            }

            // 2. Hệ số headroom 0.78f giúp nhiều viên đạn vang chồng lên nhau vẫn trong trẻo, dày tiếng
            final float masterGain = sfxEffectiveMuted ? 0.0f : (sfxVolume * 0.78f);

            if (!sfxEffectiveMuted) {
                for (ActiveVoice voice : ACTIVE_VOICES) {
                if (voice == null || voice.stopped) {
                    finishedVoices.add(voice);
                    continue;
                }

                final short[] samples = voice.samples;
                final int totalSamples = samples.length;
                int cur = voice.cursor;
                if (cur >= totalSamples) {
                    finishedVoices.add(voice);
                    continue;
                }

                final float gain = masterGain * voice.volumeFactor;
                final int count = Math.min(MIX_SHORTS, totalSamples - cur);
                for (int i = 0; i < count; i++) {
                    accumBuffer[i] += samples[cur + i] * gain;
                }
                cur += count;
                voice.cursor = cur;

                if (cur >= totalSamples) {
                    finishedVoices.add(voice);
                }
                }
            } else if (!ACTIVE_VOICES.isEmpty()) {
                ACTIVE_VOICES.clear();
            }

            if (!finishedVoices.isEmpty()) {
                ACTIVE_VOICES.removeAll(finishedVoices);
            }

            // Chuyển đổi từ float sang 16-bit PCM Little-Endian kèm Soft-Knee Limiter chống rè/vỡ tiếng
            for (int i = 0; i < MIX_SHORTS; i++) {
                float mixed = accumBuffer[i];
                if (mixed > 24000.0f) {
                    mixed = 24000.0f + 8767.0f * (1.0f - (1.0f / (1.0f + (mixed - 24000.0f) / 12000.0f)));
                } else if (mixed < -24000.0f) {
                    mixed = -24000.0f - 8767.0f * (1.0f - (1.0f / (1.0f + (-mixed - 24000.0f) / 12000.0f)));
                }

                int sampleInt = Math.round(mixed);
                if (sampleInt > 32767) sampleInt = 32767;
                else if (sampleInt < -32767) sampleInt = -32767;

                outBytes[i * 2] = (byte) (sampleInt & 0xFF);
                outBytes[i * 2 + 1] = (byte) ((sampleInt >> 8) & 0xFF);
            }

            try {
                line.write(outBytes, 0, MIX_BYTES);
            } catch (Exception e) {
                break;
            }
        }
    }

    /**
     * Nạp dữ liệu byte thô của tệp WAV từ Classpath hoặc thư mục đĩa cứng.
     */
    private static byte[] loadSoundData(String filename) {
        byte[] data = loadSingleSoundFile(filename);
        if (data != null) {
            return data;
        }

        // Hỗ trợ các tên file dự phòng (alias) cho tiếng lên đạn Shotgun, Hồi máu và Nhặt xu
        if ("gun_shotgun_loadingAmmo.wav".equalsIgnoreCase(filename)) {
            String[] aliases = {
                "gun_shotgun_loadingammo.wav",
                "gun_shotgun_pump.wav",
                "gun_shotgun_chamber.wav",
                "gun_shotgun_cock.wav"
            };
            for (String alias : aliases) {
                data = loadSingleSoundFile(alias);
                if (data != null) {
                    return data;
                }
            }
        } else if ("healing.wav".equalsIgnoreCase(filename)) {
            data = loadSingleSoundFile("Healing.wav");
            if (data != null) {
                return data;
            }
        } else if ("coin_pick_up.wav".equalsIgnoreCase(filename)) {
            String[] aliases = {"Coin-pick-up.wav", "coin-pick-up.wav"};
            for (String alias : aliases) {
                data = loadSingleSoundFile(alias);
                if (data != null) {
                    return data;
                }
            }
        } else if ("ui_interact.wav".equalsIgnoreCase(filename)) {
            data = loadSingleSoundFile("UI_interact.wav");
            if (data != null) {
                return data;
            }
        }
        return null;
    }

    private static byte[] loadSingleSoundFile(String filename) {
        // 1. Thử nạp từ Classpath (/sfx/filename)
        String resourcePath = "/sfx/" + filename;
        try (InputStream is = GameSoundManager.class.getResourceAsStream(resourcePath)) {
            if (is != null) {
                return is.readAllBytes();
            }
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Lỗi đọc resource classpath: " + resourcePath, e);
        }

        // 2. Thử nạp từ thư mục sfx/ trên ổ đĩa
        File file = new File("sfx", filename);
        if (file.exists() && file.isFile()) {
            try (InputStream is = new FileInputStream(file)) {
                return is.readAllBytes();
            } catch (Exception e) {
                LOGGER.log(Level.FINE, "Lỗi đọc file đĩa: " + file.getAbsolutePath(), e);
            }
        }

        // 3. Thử nạp từ src/main/resources/sfx/
        File srcFile = new File("src/main/resources/sfx", filename);
        if (srcFile.exists() && srcFile.isFile()) {
            try (InputStream is = new FileInputStream(srcFile)) {
                return is.readAllBytes();
            } catch (Exception e) {
                LOGGER.log(Level.FINE, "Lỗi đọc file src: " + srcFile.getAbsolutePath(), e);
            }
        }

        return null;
    }

    /**
     * Phát một hiệu ứng âm thanh với âm lượng hiện tại.
     *
     * @param sfx Loại âm thanh cần phát.
     */
    public static void play(SoundEffect sfx) {
        play(sfx, 1.0f);
    }

    /**
     * Phát một hiệu ứng âm thanh với tỉ lệ âm lượng tùy chỉnh.
     * Đưa luồng âm thanh mới vào bộ trộn đa kênh phần mềm, cho phép các tiếng súng đè chồng
     * lên nhau mượt mà với đầy đủ tiếng vọng (reverb tail) mà không bao giờ bị mất tiếng.
     *
     * @param sfx Loại âm thanh cần phát.
     * @param volumeFactor Hệ số âm lượng riêng của đợt phát này.
     */
    public static void play(SoundEffect sfx, float volumeFactor) {
        enqueueVoice(sfx, volumeFactor, false);
    }

    private static void enqueueVoice(SoundEffect sfx, float volumeFactor, boolean reloadChannel) {
        if (muted || sfxMuted || sfx == null || sfxVolume <= 0.001f || volumeFactor <= 0.001f) {
            return;
        }

        if (!initialized) {
            init();
        }

        short[] samples = PCM_SAMPLES.get(sfx);
        if (samples == null || samples.length == 0) {
            return;
        }

        // Giới hạn số luồng đồng thời của cùng 1 hiệu ứng theo poolSize để bảo vệ CPU,
        // chỉ loại bỏ luồng cũ nhất đã phát gần hết phần đuôi vọng
        int maxVoicesForEffect = sfx.getPoolSize();
        int activeCount = 0;
        ActiveVoice oldestVoice = null;
        int maxCursor = -1;

        for (ActiveVoice v : ACTIVE_VOICES) {
            if (v != null && !v.stopped && v.effect == sfx) {
                activeCount++;
                if (v.cursor > maxCursor) {
                    maxCursor = v.cursor;
                    oldestVoice = v;
                }
            }
        }

        if (activeCount >= maxVoicesForEffect && oldestVoice != null) {
            oldestVoice.stopped = true;
            ACTIVE_VOICES.remove(oldestVoice);
        }

        ACTIVE_VOICES.add(new ActiveVoice(sfx, samples, volumeFactor, reloadChannel));
    }

    /**
     * Kích hoạt phát âm thanh nạp đạn đặc thù và đánh dấu kênh reload để có thể dừng tức thì.
     *
     * @param sfx Loại âm thanh nạp đạn (GUN_PISTOL_RELOAD, GUN_RIFLE_RELOAD, v.v.)
     */
    public static synchronized void playReload(SoundEffect sfx) {
        // Dừng âm thanh nạp đạn cũ nếu có
        stopReload();
        enqueueVoice(sfx, 1.0f, true);
    }

    /**
     * Phát âm thanh nạp 1 viên đạn chì (shell) cho Shotgun.
     * Mỗi lần nạp 1 viên là 1 lần phát tiếng độc lập, đanh giòn, không bị ngắt đuôi bởi viên tiếp theo.
     */
    public static synchronized void playShotgunShellReload() {
        enqueueVoice(SoundEffect.GUN_SHOTGUN_RELOAD, 1.0f, false);
    }

    /**
     * Phát âm thanh lên đạn cơ học sau mỗi phát bắn (ví dụ: kéo chốt quy lát Bolt-Action của Sniper,
     * hoặc kéo ốp lót tay bơm Pump-Action của Shotgun).
     *
     * @param sfx Hiệu ứng âm thanh lên đạn (GUN_SNIPER_LOADING_AMMO hoặc GUN_SHOTGUN_LOADING_AMMO).
     */
    public static synchronized void playChamber(SoundEffect sfx) {
        enqueueVoice(sfx, 1.0f, false);
    }

    /**
     * Dừng ngay lập tức âm thanh nạp đạn đang phát (khi nạp xong súng thông thường hoặc khi người chơi hủy nạp đạn).
     */
    public static synchronized void stopReload() {
        for (ActiveVoice voice : ACTIVE_VOICES) {
            if (voice != null && (voice.reloadChannel || voice.effect == SoundEffect.GUN_SHOTGUN_RELOAD)) {
                voice.stopped = true;
            }
        }
    }

    public static boolean isMuted() {
        return muted || (sfxMuted && bgmMuted);
    }

    public static void setMuted(boolean isMuted) {
        muted = isMuted;
        sfxMuted = isMuted;
        bgmMuted = isMuted;
        if (muted) {
            ACTIVE_VOICES.clear();
        }
        saveSettings();
    }

    public static boolean isSfxMuted() {
        return muted || sfxMuted;
    }

    public static void setSfxMuted(boolean isMuted) {
        sfxMuted = isMuted;
        if (sfxMuted) {
            ACTIVE_VOICES.clear();
        }
        saveSettings();
    }

    public static boolean isBgmMuted() {
        return muted || bgmMuted;
    }

    public static void setBgmMuted(boolean isMuted) {
        bgmMuted = isMuted;
        saveSettings();
    }

    public static float getMasterVolume() {
        return masterVolume;
    }

    public static void setMasterVolume(float volume) {
        masterVolume = Math.max(0.0f, Math.min(1.0f, volume));
        sfxVolume = masterVolume;
        bgmVolume = masterVolume;
        saveSettings();
    }

    public static float getSfxVolume() {
        return sfxVolume;
    }

    public static void setSfxVolume(float volume) {
        sfxVolume = Math.max(0.0f, Math.min(1.0f, volume));
        masterVolume = sfxVolume;
        saveSettings();
    }

    public static float getBgmVolume() {
        return bgmVolume;
    }

    public static void setBgmVolume(float volume) {
        bgmVolume = Math.max(0.0f, Math.min(1.0f, volume));
        saveSettings();
    }

    /**
     * Tải cài đặt âm thanh từ tệp {@code save/audio_settings.properties}.
     * Hỗ trợ tách biệt âm lượng SFX và BGM, đồng thời tương thích ngược với khóa volume cũ.
     */
    public static void loadSettings() {
        File file = new File(settingsFilePath);
        if (file.exists() && file.isFile()) {
            Properties props = new Properties();
            try (FileInputStream fis = new FileInputStream(file)) {
                props.load(fis);
                String volStr = props.getProperty("volume", "0.5");
                masterVolume = Math.max(0.0f, Math.min(1.0f, Float.parseFloat(volStr)));

                String sfxStr = props.getProperty("sfxVolume", volStr);
                sfxVolume = Math.max(0.0f, Math.min(1.0f, Float.parseFloat(sfxStr)));

                String bgmStr = props.getProperty("bgmVolume", volStr);
                bgmVolume = Math.max(0.0f, Math.min(1.0f, Float.parseFloat(bgmStr)));

                muted = Boolean.parseBoolean(props.getProperty("muted", "false"));
                sfxMuted = Boolean.parseBoolean(props.getProperty("sfxMuted", Boolean.toString(muted)));
                bgmMuted = Boolean.parseBoolean(props.getProperty("bgmMuted", Boolean.toString(muted)));
            } catch (Exception e) {
                LOGGER.log(Level.FINE, "Không thể đọc file audio settings, dùng mặc định 50%", e);
                masterVolume = 0.5f;
                sfxVolume = 0.5f;
                bgmVolume = 0.5f;
                muted = false;
                sfxMuted = false;
                bgmMuted = false;
            }
        } else {
            masterVolume = 0.5f;
            sfxVolume = 0.5f;
            bgmVolume = 0.5f;
            muted = false;
            sfxMuted = false;
            bgmMuted = false;
        }
    }

    /**
     * Lưu cài đặt âm thanh hiện tại của người chơi vào tệp settingsFilePath.
     */
    public static void saveSettings() {
        try {
            File file = new File(settingsFilePath);
            File dir = file.getParentFile();
            if (dir != null && !dir.exists()) {
                dir.mkdirs();
            }
            Properties props = new Properties();
            props.setProperty("volume", String.format(java.util.Locale.US, "%.2f", masterVolume));
            props.setProperty("sfxVolume", String.format(java.util.Locale.US, "%.2f", sfxVolume));
            props.setProperty("bgmVolume", String.format(java.util.Locale.US, "%.2f", bgmVolume));
            props.setProperty("muted", Boolean.toString(muted));
            props.setProperty("sfxMuted", Boolean.toString(sfxMuted));
            props.setProperty("bgmMuted", Boolean.toString(bgmMuted));
            try (FileOutputStream fos = new FileOutputStream(file)) {
                props.store(fos, "Alone in the zone Audio Settings");
            }
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Không thể lưu audio settings", e);
        }
    }

    /**
     * Nạp dữ liệu byte thô của bản nhạc nền (BGM) từ Classpath hoặc thư mục tài nguyên.
     */
    private static synchronized byte[] loadBgmData(BgmTrack track) {
        if (track == null) {
            return null;
        }
        byte[] cached = BGM_DATA.get(track);
        if (cached != null && cached.length > 0) {
            return cached;
        }

        String resPath = track.getResourcePath();
        // 1. Thử nạp từ Classpath (/BGM/...)
        try (InputStream is = GameSoundManager.class.getResourceAsStream(resPath)) {
            if (is != null) {
                byte[] bytes = is.readAllBytes();
                if (bytes.length > 0) {
                    BGM_DATA.put(track, bytes);
                    return bytes;
                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.FINE, "Lỗi đọc BGM classpath: " + resPath, e);
        }

        // 2. Thử nạp từ src/main/resources/BGM/...
        File srcFile = new File("src/main/resources" + resPath);
        if (srcFile.exists() && srcFile.isFile()) {
            try (InputStream is = new FileInputStream(srcFile)) {
                byte[] bytes = is.readAllBytes();
                if (bytes.length > 0) {
                    BGM_DATA.put(track, bytes);
                    return bytes;
                }
            } catch (Exception e) {
                LOGGER.log(Level.FINE, "Lỗi đọc BGM file: " + srcFile.getAbsolutePath(), e);
            }
        }

        return null;
    }

    /**
     * Phát bản nhạc nền chỉ định. Nếu bản nhạc đó đang phát rồi thì giữ nguyên không khởi động lại từ đầu.
     */
    public static void playBgm(BgmTrack track) {
        if (track == null) {
            stopBgm();
            return;
        }
        if (!initialized) {
            init();
        }
        byte[] data = loadBgmData(track);
        if (data == null || data.length == 0) {
            return;
        }
        synchronized (BGM_LOCK) {
            if (activeBgm != null && activeBgm.track == track) {
                return;
            }
            if (activeBgm != null) {
                activeBgm.closeMp3Stream();
            }
            activeBgm = new ActiveBgmStream(track, data, track.isLoop(), null, 0.0, 0.0);
        }
    }

    /**
     * Xử lý nhạc nền khi bước vào căn cứ The Range:
     * <ul>
     *   <li>Nếu người chơi vừa thua trận và bản nhạc {@link BgmTrack#GAME_OVER} đang phát dở,
     *       giữ nguyên cho {@code GAME_OVER} chạy đến hết bài rồi tự động chuyển sang {@link BgmTrack#THE_RANGE}
     *       khi người chơi vẫn đang ở The Range.</li>
     *   <li>Các trường hợp khác: phát ngay {@link BgmTrack#THE_RANGE} (lặp lại khi hết bài).</li>
     * </ul>
     */
    public static void playRangeBgm() {
        if (!initialized) {
            init();
        }
        loadBgmData(BgmTrack.THE_RANGE);
        synchronized (BGM_LOCK) {
            if (activeBgm != null && activeBgm.track == BgmTrack.GAME_OVER) {
                activeBgm.nextTrack = BgmTrack.THE_RANGE;
                return;
            }
        }
        playBgm(BgmTrack.THE_RANGE);
    }

    /**
     * Phát nhạc nền khi người chơi tử trận ({@link BgmTrack#GAME_OVER}).
     * Nhạc sẽ chạy xuyên suốt kể cả khi chuyển cảnh về The Range, và khi hết bài sẽ tự động
     * đổi sang nhạc {@link BgmTrack#THE_RANGE} nếu người chơi vẫn ở The Range.
     */
    public static void playGameOverBgm() {
        if (!initialized) {
            init();
        }
        byte[] data = loadBgmData(BgmTrack.GAME_OVER);
        if (data == null || data.length == 0) {
            return;
        }
        synchronized (BGM_LOCK) {
            if (activeBgm != null) {
                activeBgm.closeMp3Stream();
            }
            activeBgm = new ActiveBgmStream(BgmTrack.GAME_OVER, data, false, BgmTrack.THE_RANGE, 0.0, 0.0);
        }
    }

    /**
     * Điều phối nhạc nền theo tầng hầm ngục (Stage):
     * <ul>
     *   <li>Stage 1 - 3: Phát {@link BgmTrack#STAGE_1_3} ({@code Survival Mode - Blue Deer Studio.mp3}) xuyên suốt và lặp lại.</li>
     *   <li>Stage 4 (Boss Stage): Tạm ngắt nhạc nền Stage 1-3 khi mới vào tầng 4 để tạo bầu không khí căng thẳng,
     *       chờ đến khi bắt đầu Cinematic giới thiệu Boss mới phát đoạn Intro.</li>
     * </ul>
     */
    public static void playStageBgm(int stage) {
        if (stage < 4) {
            playBgm(BgmTrack.STAGE_1_3);
        } else {
            stopBgm();
            // Tiền nạp sẵn 3 phân đoạn nhạc Boss vào RAM để chuyển giao mẫu chính xác 0ms
            loadBgmData(BgmTrack.BOSS_INTRO);
            loadBgmData(BgmTrack.BOSS_LOOP);
            loadBgmData(BgmTrack.BOSS_END);
        }
    }

    /**
     * Bắt đầu phát nhạc mở đầu Boss Fight ({@link BgmTrack#BOSS_INTRO}) đồng bộ với thời lượng Cinematic giới thiệu Boss.
     * Bỏ qua phần đầu của file {@code Intro.wav} (7.0s) và thực hiện Fade-In mượt mà để điểm kết thúc của {@code Intro.wav}
     * trùng khớp chính xác với khoảnh khắc kết thúc Cinematic (2.8s), chuyển giao thẳng sang {@link BgmTrack#BOSS_LOOP}.
     *
     * @param cinematicDurationSeconds Thời lượng của đoạn cắt cảnh giới thiệu Boss (ví dụ: 2.8 giây).
     */
    public static void startBossIntroBgm(double cinematicDurationSeconds) {
        if (!initialized) {
            init();
        }
        byte[] introData = loadBgmData(BgmTrack.BOSS_INTRO);
        loadBgmData(BgmTrack.BOSS_LOOP);
        if (introData == null || introData.length == 0) {
            playBgm(BgmTrack.BOSS_LOOP);
            return;
        }
        synchronized (BGM_LOCK) {
            if (activeBgm != null) {
                activeBgm.closeMp3Stream();
            }
            activeBgm = new ActiveBgmStream(
                    BgmTrack.BOSS_INTRO,
                    introData,
                    false,
                    BgmTrack.BOSS_LOOP,
                    Math.max(0.5, cinematicDurationSeconds),
                    0.75
            );
        }
    }

    /**
     * Chuyển giao ngay lập tức từ đoạn mở đầu ({@link BgmTrack#BOSS_INTRO}) sang đoạn lặp giữa trận ({@link BgmTrack#BOSS_LOOP})
     * khi đoạn cắt cảnh giới thiệu Boss kết thúc hoặc khi người chơi bấm phím bỏ qua (Skip) sớm.
     */
    public static void transitionBossIntroToLoop() {
        synchronized (BGM_LOCK) {
            if (activeBgm != null && activeBgm.track == BgmTrack.BOSS_LOOP) {
                return;
            }
        }
        playBgm(BgmTrack.BOSS_LOOP);
    }

    /**
     * Phát đoạn kết thúc chiến thắng Boss Fight ({@link BgmTrack#BOSS_END} - {@code end.wav})
     * ngay sau khi màn Boss được quét sạch (Clear).
     */
    public static void playBossClearBgm() {
        playBgm(BgmTrack.BOSS_END);
    }

    /**
     * Dừng hoàn toàn nhạc nền hiện tại.
     */
    public static void stopBgm() {
        synchronized (BGM_LOCK) {
            if (activeBgm != null) {
                activeBgm.closeMp3Stream();
                activeBgm = null;
            }
        }
    }

    /**
     * Lấy bản nhạc nền đang phát hiện tại (phục vụ kiểm tra trạng thái & Unit Test).
     */
    public static BgmTrack getCurrentBgmTrack() {
        ActiveBgmStream bgm = activeBgm;
        return bgm != null ? bgm.track : null;
    }

    /**
     * Lấy bản nhạc nền được xếp hàng nối tiếp sau khi bản hiện tại kết thúc (phục vụ kiểm thử).
     */
    public static BgmTrack getQueuedNextBgmTrack() {
        ActiveBgmStream bgm = activeBgm;
        return bgm != null ? bgm.nextTrack : null;
    }

    /**
     * Kiểm tra xem một bản nhạc nền BGM có tồn tại và nạp được dữ liệu hợp lệ hay không.
     */
    public static boolean isBgmLoaded(BgmTrack track) {
        byte[] data = loadBgmData(track);
        return data != null && data.length > 0;
    }

    /**
     * Kiểm tra xem một hiệu ứng âm thanh đã được nạp thành công dữ liệu hay chưa.
     */
    public static boolean isSoundLoaded(SoundEffect sfx) {
        return SOUND_DATA.containsKey(sfx) && SOUND_DATA.get(sfx) != null && PCM_SAMPLES.containsKey(sfx);
    }

    /**
     * Dọn dẹp tài nguyên âm thanh khi thoát game.
     */
    public static synchronized void cleanup() {
        mixerRunning = false;
        ACTIVE_VOICES.clear();
        stopBgm();
        if (mixerLine != null) {
            try {
                mixerLine.stop();
                mixerLine.close();
            } catch (Exception ignored) {
            }
            mixerLine = null;
        }
        SOUND_DATA.clear();
        PCM_SAMPLES.clear();
        initialized = false;
    }
}
