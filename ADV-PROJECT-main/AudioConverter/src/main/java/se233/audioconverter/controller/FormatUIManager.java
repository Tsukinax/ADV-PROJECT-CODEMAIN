package se233.audioconverter.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import se233.audioconverter.model.ConversionSettings;

import java.util.List;

public class FormatUIManager {
    private final Label formatInfoLabel;
    private final VBox bitrateSettingsBox;
    private final VBox wavQualityBox;
    private final VBox bitrateModeBox;
    private final Label bitrateModeLabel;
    private final RadioButton variableBitrateRadio;
    private final RadioButton constantBitrateRadio;
    private final VBox cbrBitrateBox;
    private final VBox vbrQualityBox;
    private final ComboBox<ConversionSettings.SampleRate> sampleRateComboBox;
    private final ComboBox<Integer> bitrateComboBox;
    private final ConversionSettings settings;

    public FormatUIManager(Label formatInfoLabel,
                           VBox bitrateSettingsBox,
                           VBox wavQualityBox,
                           VBox bitrateModeBox,
                           Label bitrateModeLabel,
                           RadioButton variableBitrateRadio,
                           RadioButton constantBitrateRadio,
                           VBox cbrBitrateBox,
                           VBox vbrQualityBox,
                           ComboBox<ConversionSettings.SampleRate> sampleRateComboBox,
                           ComboBox<Integer> bitrateComboBox,
                           ConversionSettings settings) {
        this.formatInfoLabel = formatInfoLabel;
        this.bitrateSettingsBox = bitrateSettingsBox;
        this.wavQualityBox = wavQualityBox;
        this.bitrateModeBox = bitrateModeBox;
        this.bitrateModeLabel = bitrateModeLabel;
        this.variableBitrateRadio = variableBitrateRadio;
        this.constantBitrateRadio = constantBitrateRadio;
        this.cbrBitrateBox = cbrBitrateBox;
        this.vbrQualityBox = vbrQualityBox;
        this.sampleRateComboBox = sampleRateComboBox;
        this.bitrateComboBox = bitrateComboBox;
        this.settings = settings;
    }

    public void updateForFormat(ConversionSettings.OutputFormat format) {
        updateFormatInfo(format);
        updateVisibility(format);
        updateSampleRateOptions(format);
        updateBitrateOptions(format);
        updateBitrateModeUI();
    }

    private void updateFormatInfo(ConversionSettings.OutputFormat format) {
        if (format.supportsBitrate()) {
            formatInfoLabel.setText("Lossy compression format");
        } else {
            formatInfoLabel.setText("Lossless format");
        }
    }

    private void updateVisibility(ConversionSettings.OutputFormat format) {
        if (format == ConversionSettings.OutputFormat.WAV) {
            bitrateSettingsBox.setVisible(false);
            bitrateSettingsBox.setManaged(false);
            if (wavQualityBox != null) {
                wavQualityBox.setVisible(true);
                wavQualityBox.setManaged(true);
            }
        } else if (format == ConversionSettings.OutputFormat.FLAC) {
            bitrateSettingsBox.setVisible(false);
            bitrateSettingsBox.setManaged(false);
            if (wavQualityBox != null) {
                wavQualityBox.setVisible(false);
                wavQualityBox.setManaged(false);
            }
        } else {
            bitrateSettingsBox.setVisible(true);
            bitrateSettingsBox.setManaged(true);
            if (wavQualityBox != null) {
                wavQualityBox.setVisible(false);
                wavQualityBox.setManaged(false);
            }
        }

        boolean showBitrateMode = (format == ConversionSettings.OutputFormat.MP3 ||
                format == ConversionSettings.OutputFormat.M4A);

        if (bitrateModeBox != null) {
            bitrateModeBox.setVisible(showBitrateMode);
            bitrateModeBox.setManaged(showBitrateMode);
        }

        if (showBitrateMode) {
            if (format == ConversionSettings.OutputFormat.MP3) {
                if (bitrateModeLabel != null) {
                    bitrateModeLabel.setText("Bitrate Mode (MP3)");
                }
                if (variableBitrateRadio != null) {
                    variableBitrateRadio.setVisible(true);
                    variableBitrateRadio.setManaged(true);
                }
            } else if (format == ConversionSettings.OutputFormat.M4A) {
                if (bitrateModeLabel != null) {
                    bitrateModeLabel.setText("Bitrate Mode (M4A)");
                }
                if (variableBitrateRadio != null) {
                    variableBitrateRadio.setVisible(false);
                    variableBitrateRadio.setManaged(false);
                }
                if (constantBitrateRadio != null) {
                    constantBitrateRadio.setSelected(true);
                }
                settings.setBitrateMode(ConversionSettings.BitrateMode.CONSTANT);
            }
        }
    }

    private void updateSampleRateOptions(ConversionSettings.OutputFormat format) {
        List<Integer> sampleRateOptions = format.getSampleRateOptions();
        if (sampleRateComboBox != null) {
            ObservableList<ConversionSettings.SampleRate> availableRates =
                    FXCollections.observableArrayList();

            for (int rate : sampleRateOptions) {
                availableRates.add(ConversionSettings.SampleRate.fromRate(rate));
            }

            sampleRateComboBox.setItems(availableRates);
            sampleRateComboBox.setValue(ConversionSettings.SampleRate.SR_44100);
            settings.setSampleRate(ConversionSettings.SampleRate.SR_44100);
        }
    }

    private void updateBitrateOptions(ConversionSettings.OutputFormat format) {
        List<Integer> bitrateOptions = format.getBitrateOptions();
        if (bitrateComboBox != null && !bitrateOptions.isEmpty()) {
            bitrateComboBox.setItems(FXCollections.observableArrayList(bitrateOptions));
            bitrateComboBox.setValue(format.getDefaultBitrate());
            settings.setCustomBitrate(format.getDefaultBitrate());
        }
    }

    public void updateBitrateModeUI() {
        boolean isVBR = variableBitrateRadio != null && variableBitrateRadio.isSelected();
        ConversionSettings.OutputFormat format = settings.getOutputFormat();
        boolean isMp3 = format == ConversionSettings.OutputFormat.MP3;
        boolean isM4a = format == ConversionSettings.OutputFormat.M4A;

        if (isMp3 || isM4a) {
            if (isVBR && isMp3) {
                if (cbrBitrateBox != null) {
                    cbrBitrateBox.setVisible(false);
                    cbrBitrateBox.setManaged(false);
                }
                if (vbrQualityBox != null) {
                    vbrQualityBox.setVisible(true);
                    vbrQualityBox.setManaged(true);
                }
            } else {
                if (cbrBitrateBox != null) {
                    cbrBitrateBox.setVisible(true);
                    cbrBitrateBox.setManaged(true);
                }
                if (vbrQualityBox != null) {
                    vbrQualityBox.setVisible(false);
                    vbrQualityBox.setManaged(false);
                }
            }
        }
    }
}