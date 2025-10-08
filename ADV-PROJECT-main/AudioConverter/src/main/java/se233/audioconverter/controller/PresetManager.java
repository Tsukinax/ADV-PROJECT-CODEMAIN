package se233.audioconverter.controller;

import javafx.collections.FXCollections;
import javafx.scene.control.*;
import se233.audioconverter.model.ConversionPreset;
import se233.audioconverter.model.ConversionSettings;

import java.util.function.Consumer;

public class PresetManager {
    private final ComboBox<ConversionPreset> presetComboBox;
    private final Label presetDescriptionLabel;
    private final ConversionSettings settings;
    private final Consumer<ConversionPreset> onPresetLoaded;

    public PresetManager(ComboBox<ConversionPreset> presetComboBox,
                         Label presetDescriptionLabel,
                         ConversionSettings settings,
                         Consumer<ConversionPreset> onPresetLoaded) {
        this.presetComboBox = presetComboBox;
        this.presetDescriptionLabel = presetDescriptionLabel;
        this.settings = settings;
        this.onPresetLoaded = onPresetLoaded;

        setupPresets();
    }

    private void setupPresets() {
        presetComboBox.setItems(FXCollections.observableArrayList(
                ConversionPreset.values()));

        presetComboBox.setValue(ConversionPreset.NONE);
        presetDescriptionLabel.setText(ConversionPreset.NONE.getDescription());

        presetComboBox.setOnAction(e -> {
            ConversionPreset selected = presetComboBox.getValue();
            if (selected != null) {
                presetDescriptionLabel.setText(selected.getDescription());
            } else {
                presetDescriptionLabel.setText("");
            }
        });
    }

    public void loadSelectedPreset() {
        ConversionPreset preset = presetComboBox.getValue();

        if (preset == null) {
            return;
        }

        if (preset == ConversionPreset.NONE) {
            return;
        }

        settings.loadFromPreset(preset);

        if (onPresetLoaded != null) {
            onPresetLoaded.accept(preset);
        }
    }

    public ConversionPreset getSelectedPreset() {
        return presetComboBox.getValue();
    }
}