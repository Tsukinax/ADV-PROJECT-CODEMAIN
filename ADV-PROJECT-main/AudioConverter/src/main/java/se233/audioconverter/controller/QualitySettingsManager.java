package se233.audioconverter.controller;

import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import se233.audioconverter.model.ConversionSettings;

public class QualitySettingsManager {
    private final Slider qualitySlider;
    private final Label qualityLabel;
    private final ComboBox<Integer> bitrateComboBox;
    private final ConversionSettings settings;

    public QualitySettingsManager(Slider qualitySlider,
                                  Label qualityLabel,
                                  ComboBox<Integer> bitrateComboBox,
                                  ConversionSettings settings) {
        this.qualitySlider = qualitySlider;
        this.qualityLabel = qualityLabel;
        this.bitrateComboBox = bitrateComboBox;
        this.settings = settings;

        setupQualitySlider();
    }

    private void setupQualitySlider() {
        qualitySlider.setMin(0);
        qualitySlider.setMax(3);
        qualitySlider.setValue(2);
        qualitySlider.setMajorTickUnit(1);
        qualitySlider.setMinorTickCount(0);
        qualitySlider.setSnapToTicks(true);
        qualitySlider.setShowTickMarks(true);
        qualitySlider.setShowTickLabels(true);

        qualitySlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            updateQualityLabel(newVal.intValue());
        });
    }

    private void updateQualityLabel(int index) {
        ConversionSettings.OutputFormat format = settings.getOutputFormat();

        if (format == ConversionSettings.OutputFormat.M4A) {
            ConversionSettings.M4AQuality quality = ConversionSettings.M4AQuality.values()[index];
            qualityLabel.setText(String.format("%s (%d kbps)",
                    quality.getLabel(), quality.getBitrate()));
            settings.setCustomBitrate(quality.getBitrate());
            if (bitrateComboBox != null) {
                bitrateComboBox.setValue(quality.getBitrate());
            }
        } else {
            ConversionSettings.Quality quality = ConversionSettings.Quality.values()[index];
            settings.setQuality(quality);
            qualityLabel.setText(String.format("%s (%d kbps)",
                    quality.getLabel(), quality.getBitrate()));

            if (settings.getOutputFormat().supportsBitrate() && bitrateComboBox != null) {
                bitrateComboBox.setValue(quality.getBitrate());
            }
        }
    }

    public void updateForFormat(ConversionSettings.OutputFormat format) {
        if (format == ConversionSettings.OutputFormat.M4A) {
            qualitySlider.setValue(2);
            qualityLabel.setText("Good (160 kbps)");
            settings.setCustomBitrate(160);
            if (bitrateComboBox != null) {
                bitrateComboBox.setValue(160);
            }
        } else if (format == ConversionSettings.OutputFormat.MP3) {
            qualitySlider.setValue(2);
            qualityLabel.setText("Good (192 kbps)");
            settings.setQuality(ConversionSettings.Quality.GOOD);
            if (bitrateComboBox != null) {
                bitrateComboBox.setValue(192);
            }
        }
    }

    public void setQualityFromBitrate(int bitrate, ConversionSettings.OutputFormat format) {
        if (format == ConversionSettings.OutputFormat.M4A) {
            for (int i = 0; i < ConversionSettings.M4AQuality.values().length; i++) {
                ConversionSettings.M4AQuality q = ConversionSettings.M4AQuality.values()[i];
                if (q.getBitrate() == bitrate) {
                    qualitySlider.setValue(i);
                    qualityLabel.setText(String.format("%s (%d kbps)",
                        q.getLabel(), q.getBitrate()));
                    break;
                }
            }
        } else {
            for (ConversionSettings.Quality q : ConversionSettings.Quality.values()) {
                if (q.getBitrate() == bitrate) {
                    qualitySlider.setValue(q.ordinal());
                    qualityLabel.setText(String.format("%s (%d kbps)",
                        q.getLabel(), q.getBitrate()));
                    break;
                }
            }
        }
    }
}