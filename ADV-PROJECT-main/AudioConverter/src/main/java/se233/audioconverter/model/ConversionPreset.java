package se233.audioconverter.model;

public enum ConversionPreset {
    NONE(
            "None (Custom Settings)",
            "Configure settings manually",
            ConversionSettings.OutputFormat.MP3,
            192,
            ConversionSettings.SampleRate.SR_44100,
            ConversionSettings.Channels.STEREO,
            ConversionSettings.BitrateMode.CONSTANT
    ),
    PODCAST_STANDARD(
            "Podcast Standard",
            "Optimized for voice recordings and podcasts",
            ConversionSettings.OutputFormat.MP3,
            128,
            ConversionSettings.SampleRate.SR_44100,
            ConversionSettings.Channels.MONO,
            ConversionSettings.BitrateMode.CONSTANT
    ),

    MUSIC_HIGH_QUALITY(
            "Music High Quality",
            "Best quality for music listening",
            ConversionSettings.OutputFormat.MP3,
            320,
            ConversionSettings.SampleRate.SR_48000,
            ConversionSettings.Channels.STEREO,
            ConversionSettings.BitrateMode.CONSTANT
    ),

    MUSIC_VBR_QUALITY(
            "Music VBR Quality",
            "High quality with smaller file size",
            ConversionSettings.OutputFormat.MP3,
            0, // VBR mode - bitrate not used
            ConversionSettings.SampleRate.SR_48000,
            ConversionSettings.Channels.STEREO,
            ConversionSettings.BitrateMode.VARIABLE
    ),

    VOICE_RECORDING(
            "Voice Recording",
            "Compact format for voice memos",
            ConversionSettings.OutputFormat.M4A,
            64,
            ConversionSettings.SampleRate.SR_32000,
            ConversionSettings.Channels.MONO,
            ConversionSettings.BitrateMode.CONSTANT
    ),

    ARCHIVE_LOSSLESS(
            "Archive/Lossless",
            "Perfect quality preservation",
            ConversionSettings.OutputFormat.FLAC,
            0, // Lossless - no bitrate
            ConversionSettings.SampleRate.SR_48000,
            ConversionSettings.Channels.STEREO,
            ConversionSettings.BitrateMode.CONSTANT
    ),

    SMALL_FILE_SIZE(
            "Small File Size",
            "Minimum file size for sharing",
            ConversionSettings.OutputFormat.MP3,
            64,
            ConversionSettings.SampleRate.SR_32000,
            ConversionSettings.Channels.MONO,
            ConversionSettings.BitrateMode.CONSTANT
    );

    private final String displayName;
    private final String description;
    private final ConversionSettings.OutputFormat format;
    private final int bitrate;
    private final ConversionSettings.SampleRate sampleRate;
    private final ConversionSettings.Channels channels;
    private final ConversionSettings.BitrateMode bitrateMode;

    ConversionPreset(String displayName, String description,
                     ConversionSettings.OutputFormat format,
                     int bitrate,
                     ConversionSettings.SampleRate sampleRate,
                     ConversionSettings.Channels channels,
                     ConversionSettings.BitrateMode bitrateMode) {
        this.displayName = displayName;
        this.description = description;
        this.format = format;
        this.bitrate = bitrate;
        this.sampleRate = sampleRate;
        this.channels = channels;
        this.bitrateMode = bitrateMode;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getDescription() {
        return description;
    }

    public ConversionSettings.OutputFormat getFormat() {
        return format;
    }

    public int getBitrate() {
        return bitrate;
    }

    public ConversionSettings.SampleRate getSampleRate() {
        return sampleRate;
    }

    public ConversionSettings.Channels getChannels() {
        return channels;
    }

    public ConversionSettings.BitrateMode getBitrateMode() {
        return bitrateMode;
    }

    public int getVbrQuality() {
        // Only for Music VBR preset
        if (this == MUSIC_VBR_QUALITY) {
            return 0; // VBR quality 0 = best
        }
        return 2; // default
    }

    @Override
    public String toString() {
        return displayName;
    }

    public String getDetailedDescription() {
        StringBuilder sb = new StringBuilder();
        sb.append(displayName).append("\n");
        sb.append(description).append("\n");
        sb.append("Format: ").append(format.toString()).append("\n");

        if (format.supportsBitrate() && bitrateMode == ConversionSettings.BitrateMode.CONSTANT) {
            sb.append("Bitrate: ").append(bitrate).append(" kbps\n");
        } else if (bitrateMode == ConversionSettings.BitrateMode.VARIABLE) {
            sb.append("Mode: Variable Bitrate (VBR)\n");
        }

        sb.append("Sample Rate: ").append(sampleRate.getLabel()).append("\n");
        sb.append("Channels: ").append(channels.getLabel());

        return sb.toString();
    }
}