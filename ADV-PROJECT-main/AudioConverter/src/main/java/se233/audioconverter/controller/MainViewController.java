package se233.audioconverter.controller;

import se233.audioconverter.Launcher;
import se233.audioconverter.exception.AudioConversionException;
import se233.audioconverter.model.AudioFile;
import se233.audioconverter.model.ConversionSettings;
import se233.audioconverter.model.ConversionPreset;
import se233.audioconverter.service.FFmpegService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class MainViewController {
    // Stage 1: File Drop
    @FXML private StackPane mainStackPane;
    @FXML private VBox fileDropStage;
    @FXML private VBox dropZone;
    @FXML private VBox filePreviewBox;
    @FXML private ListView<AudioFile> filePreviewList;
    @FXML private Label fileCountLabel;
    @FXML private Button nextButton;

    // Stage 2: Configuration
    @FXML private VBox configStage;
    @FXML private Label configFileCountLabel;
    @FXML private ListView<AudioFile> fileListView;
    @FXML private ComboBox<ConversionSettings.OutputFormat> formatComboBox;
    @FXML private Label formatInfoLabel;

    // Preset Management
    @FXML private ComboBox<ConversionPreset> presetComboBox;
    @FXML private Button loadPresetButton;
    @FXML private Label presetDescriptionLabel;

    // Quality Settings
    @FXML private VBox bitrateSettingsBox;
    @FXML private Slider qualitySlider;
    @FXML private Label qualityLabel;
    @FXML private ComboBox<Integer> bitrateComboBox;

    // WAV Quality Settings
    @FXML private VBox wavQualityBox;
    @FXML private Slider wavQualitySlider;
    @FXML private Label wavQualityLabel;

    // Advanced Settings
    @FXML private VBox advancedSettingsBox;
    @FXML private CheckBox showAdvancedCheckBox;
    @FXML private ComboBox<ConversionSettings.SampleRate> sampleRateComboBox;
    @FXML private ComboBox<ConversionSettings.Channels> channelsComboBox;

    // Bitrate Mode
    @FXML private VBox bitrateModeBox;
    @FXML private Label bitrateModeLabel;
    @FXML private HBox bitrateModeRadioBox;
    @FXML private ToggleGroup bitrateToggleGroup;
    @FXML private RadioButton constantBitrateRadio;
    @FXML private RadioButton variableBitrateRadio;
    @FXML private VBox cbrBitrateBox;
    @FXML private VBox vbrQualityBox;
    @FXML private Slider vbrQualitySlider;
    @FXML private Label vbrQualityLabel;

    @FXML private Button convertButton;
    @FXML private Button clearButton;
    @FXML private ProgressBar progressBar;
    @FXML private Label statusLabel;

    // Controllers and Managers
    private ObservableList<AudioFile> audioFiles;
    private ConversionSettings settings;
    private FFmpegService ffmpegService;
    private ExecutorService executorService;

    private FileDropStageController fileDropController;
    private QualitySettingsManager qualityManager;
    private PresetManager presetManager;
    private FormatUIManager formatUIManager;

    @FXML
    public void initialize() {
        audioFiles = FXCollections.observableArrayList();
        settings = new ConversionSettings();

        try {
            ffmpegService = new FFmpegService();
        } catch (IOException e) {
            showError("FFmpeg Initialization Error",
                    "Could not initialize FFmpeg. Make sure FFmpeg is installed and in your PATH.\n\n" +
                            "Error: " + e.getMessage());
            return;
        }

        executorService = Executors.newFixedThreadPool(4);

        initializeControllers();
        setupStage2Components();
        showStage1();
    }

    private void initializeControllers() {
        // File Drop Controller
        fileDropController = new FileDropStageController(
                audioFiles, dropZone, filePreviewList, filePreviewBox, fileCountLabel,
                () -> nextButton.setDisable(audioFiles.isEmpty())
        );

        // Quality Settings Manager
        qualityManager = new QualitySettingsManager(
                qualitySlider, qualityLabel, bitrateComboBox, settings
        );

        // Preset Manager
        presetManager = new PresetManager(
                presetComboBox, presetDescriptionLabel, settings,
                this::handlePresetLoaded
        );

        // Format UI Manager
        formatUIManager = new FormatUIManager(
                formatInfoLabel, bitrateSettingsBox, wavQualityBox,
                bitrateModeBox, bitrateModeLabel, variableBitrateRadio,
                constantBitrateRadio, cbrBitrateBox, vbrQualityBox,
                sampleRateComboBox, bitrateComboBox, settings
        );
    }

    private void setupStage2Components() {
        // File List View
        setupFileListView();

        // Format ComboBox
        formatComboBox.setItems(FXCollections.observableArrayList(
                ConversionSettings.OutputFormat.values()));
        formatComboBox.setValue(ConversionSettings.OutputFormat.MP3);
        formatComboBox.setOnAction(e -> {
            settings.setOutputFormat(formatComboBox.getValue());
            qualityManager.updateForFormat(formatComboBox.getValue());
            formatUIManager.updateForFormat(formatComboBox.getValue());
        });

        // Bitrate ComboBox
        bitrateComboBox.setOnAction(e -> {
            Integer selectedBitrate = bitrateComboBox.getValue();
            if (selectedBitrate != null) {
                settings.setCustomBitrate(selectedBitrate);
            }
        });

        // Bitrate Mode Radios
        constantBitrateRadio.setOnAction(e -> {
            settings.setBitrateMode(ConversionSettings.BitrateMode.CONSTANT);
            formatUIManager.updateBitrateModeUI();
        });

        variableBitrateRadio.setOnAction(e -> {
            settings.setBitrateMode(ConversionSettings.BitrateMode.VARIABLE);
            formatUIManager.updateBitrateModeUI();
        });

        // VBR Quality Slider
        setupVBRQualitySlider();

        // WAV Quality Slider
        setupWAVQualitySlider();

        // Sample Rate and Channels
        sampleRateComboBox.setOnAction(e ->
                settings.setSampleRate(sampleRateComboBox.getValue()));

        channelsComboBox.setItems(FXCollections.observableArrayList(
                ConversionSettings.Channels.values()));
        channelsComboBox.setValue(ConversionSettings.Channels.STEREO);
        channelsComboBox.setOnAction(e ->
                settings.setChannels(channelsComboBox.getValue()));

        // Advanced Settings Toggle
        advancedSettingsBox.setVisible(false);
        advancedSettingsBox.setManaged(false);
        showAdvancedCheckBox.setOnAction(e -> {
            boolean show = showAdvancedCheckBox.isSelected();
            advancedSettingsBox.setVisible(show);
            advancedSettingsBox.setManaged(show);
        });

        // Load Preset Button
        loadPresetButton.setOnAction(e -> onLoadPreset());

        // Action Buttons
        convertButton.setOnAction(e -> onConvert());
        clearButton.setOnAction(e -> onClear());

        progressBar.setProgress(0);
        statusLabel.setText("Ready");

        // Initial format UI update
        qualityManager.updateForFormat(ConversionSettings.OutputFormat.MP3);
        formatUIManager.updateForFormat(ConversionSettings.OutputFormat.MP3);
    }

    private void setupFileListView() {
        fileListView.setItems(audioFiles);
        fileListView.setCellFactory(param -> new ListCell<>() {
            @Override
            protected void updateItem(AudioFile item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item.toString());
                    switch (item.getStatus()) {
                        case PENDING -> setStyle("-fx-text-fill: black;");
                        case PROCESSING -> setStyle("-fx-text-fill: blue; -fx-font-weight: bold;");
                        case COMPLETED -> setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                        case FAILED -> setStyle("-fx-text-fill: red; -fx-font-weight: bold;");
                    }
                }
            }
        });
    }

    private void setupVBRQualitySlider() {
        if (vbrQualitySlider != null) {
            vbrQualitySlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                int vbrQuality = newVal.intValue();
                settings.setVbrQuality(vbrQuality);

                String[] vbrLabels = {"Best", "High", "Normal", "Medium", "Low", "Smallest"};
                if (vbrQualityLabel != null) {
                    vbrQualityLabel.setText(vbrQuality + " (" + vbrLabels[vbrQuality] + ")");
                }
            });
            if (vbrQualityLabel != null) {
                vbrQualityLabel.setText("2 (Normal)");
            }
        }
    }

    private void setupWAVQualitySlider() {
        if (wavQualitySlider != null) {
            wavQualitySlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                int qualityIndex = newVal.intValue();

                int[] sampleRates = {22050, 44100, 48000, 96000};
                String[] qualityLabels = {"Tape (22050 Hz)", "CD Quality (44100 Hz)",
                        "DVD (48000 Hz)", "Extra High (96000 Hz)"};

                int selectedRate = sampleRates[qualityIndex];
                ConversionSettings.SampleRate sr = ConversionSettings.SampleRate.fromRate(selectedRate);
                settings.setSampleRate(sr);

                if (wavQualityLabel != null) {
                    wavQualityLabel.setText(qualityLabels[qualityIndex]);
                }

                if (sampleRateComboBox != null) {
                    sampleRateComboBox.setValue(sr);
                }
            });
            if (wavQualityLabel != null) {
                wavQualityLabel.setText("DVD (48000 Hz)");
            }
        }
    }

    private void handlePresetLoaded(ConversionPreset preset) {
        if (preset == ConversionPreset.NONE) {
            statusLabel.setText("Custom settings mode - Configure manually");
            return;
        }

        formatComboBox.setValue(preset.getFormat());
        channelsComboBox.setValue(preset.getChannels());
        sampleRateComboBox.setValue(preset.getSampleRate());

        qualityManager.updateForFormat(preset.getFormat());
        formatUIManager.updateForFormat(preset.getFormat());

        if (preset.getFormat().supportsBitrate() &&
                preset.getBitrateMode() == ConversionSettings.BitrateMode.CONSTANT) {

            qualityManager.setQualityFromBitrate(preset.getBitrate(), preset.getFormat());
        }

        if (preset.getFormat().supportsBitrate()) {
            if (preset.getBitrateMode() == ConversionSettings.BitrateMode.CONSTANT) {
                constantBitrateRadio.setSelected(true);
                bitrateComboBox.setValue(preset.getBitrate());
            } else {
                variableBitrateRadio.setSelected(true);
                vbrQualitySlider.setValue(preset.getVbrQuality());
            }
            formatUIManager.updateBitrateModeUI();
        }

        statusLabel.setText("Loaded preset: " + preset.getDisplayName());

        showInfo("Preset Loaded",
                "Successfully loaded preset:\n\n" + preset.getDetailedDescription());
    }

    @FXML
    private void onLoadPreset() {
        ConversionPreset preset = presetManager.getSelectedPreset();

        if (preset == null) {
            showError("No Preset Selected", "Please select a preset first.");
            return;
        }

        presetManager.loadSelectedPreset();
    }

    @FXML
    private void onBrowseFiles() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Audio Files");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Audio Files", "*.mp3", "*.wav", "*.m4a", "*.flac"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(Launcher.primaryStage);

        if (selectedFiles != null && !selectedFiles.isEmpty()) {
            fileDropController.addFiles(selectedFiles);
        }
    }

    @FXML
    private void onNextToConfig() {
        if (audioFiles.isEmpty()) {
            showError("No Files", "Please add audio files first.");
            return;
        }
        showStage2();
    }

    @FXML
    private void onBackToFiles() {
        showStage1();
    }

    @FXML
    private void onClearFromStage1() {
        fileDropController.clearFiles();
    }

    @FXML
    private void onConvert() {
        if (audioFiles.isEmpty()) {
            showError("No Files", "Please add audio files to convert.");
            return;
        }

        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Select Output Directory");
        File outputDir = directoryChooser.showDialog(Launcher.primaryStage);

        if (outputDir == null) {
            return;
        }

        startConversion(outputDir);
    }

    private void startConversion(File outputDir) {
        setUIDisabled(true);

        audioFiles.forEach(file -> file.setStatus(AudioFile.ConversionStatus.PENDING));
        fileListView.refresh();

        List<AudioConversionTask> tasks = createConversionTasks(outputDir);
        Task<Void> masterTask = createMasterTask(tasks, outputDir);

        progressBar.progressProperty().bind(masterTask.progressProperty());
        statusLabel.textProperty().bind(masterTask.messageProperty());

        masterTask.setOnSucceeded(e -> handleConversionSuccess(outputDir));
        masterTask.setOnFailed(e -> handleConversionFailure());

        Thread thread = new Thread(masterTask);
        thread.setDaemon(true);
        thread.start();
    }

    private List<AudioConversionTask> createConversionTasks(File outputDir) {
        List<AudioConversionTask> tasks = new ArrayList<>();
        for (AudioFile audioFile : audioFiles) {
            AudioConversionTask task = new AudioConversionTask(
                    audioFile, settings, outputDir.getAbsolutePath(), ffmpegService);

            task.setProgressCallback(new AudioConversionTask.ProgressCallback() {
                @Override
                public void onProgress(double percentage, String message) {
                }

                @Override
                public void onStatusChange(AudioFile.ConversionStatus status) {
                    Platform.runLater(() -> fileListView.refresh());
                }
            });

            tasks.add(task);
        }
        return tasks;
    }

    private Task<Void> createMasterTask(List<AudioConversionTask> tasks, File outputDir) {
        return new Task<>() {
            @Override
            protected Void call() throws Exception {
                CompletionService<Void> completionService =
                        new ExecutorCompletionService<>(executorService);

                int totalTasks = tasks.size();
                int completedTasks = 0;

                for (Callable<Void> task : tasks) {
                    completionService.submit(task);
                }

                for (int i = 0; i < totalTasks; i++) {
                    try {
                        Future<Void> future = completionService.take();
                        future.get();
                        completedTasks++;

                        double progress = (double) completedTasks / totalTasks;
                        updateProgress(progress, 1.0);
                        updateMessage(String.format("Completed %d of %d files",
                                completedTasks, totalTasks));

                    } catch (ExecutionException e) {
                        handleExecutionException(e);
                    }
                }

                return null;
            }
        };
    }

    private void handleExecutionException(ExecutionException e) {
        Throwable cause = e.getCause();
        if (cause instanceof AudioConversionException) {
            AudioConversionException ace = (AudioConversionException) cause;
            final String errorMsg = ace.getUserFriendlyMessage();
            Platform.runLater(() ->
                    showError("Conversion Error", errorMsg));
        } else {
            final String errorMsg = "An unexpected error occurred: " +
                    (cause != null ? cause.getMessage() : "Unknown error");
            Platform.runLater(() ->
                    showError("Unexpected Error", errorMsg));
        }
    }

    private void handleConversionSuccess(File outputDir) {
        long successful = audioFiles.stream()
                .filter(f -> f.getStatus() == AudioFile.ConversionStatus.COMPLETED)
                .count();
        long failed = audioFiles.stream()
                .filter(f -> f.getStatus() == AudioFile.ConversionStatus.FAILED)
                .count();

        statusLabel.textProperty().unbind();
        statusLabel.setText(String.format("Conversion complete: %d successful, %d failed",
                successful, failed));

        showInfo("Conversion Complete",
                String.format("Successfully converted %d file(s).\nFailed: %d\n\nOutput location: %s",
                        successful, failed, outputDir.getAbsolutePath()));

        setUIDisabled(false);
    }

    private void handleConversionFailure() {
        statusLabel.textProperty().unbind();
        statusLabel.setText("Conversion failed");
        showError("Error", "An error occurred during conversion.");
        setUIDisabled(false);
    }

    private void setUIDisabled(boolean disabled) {
        convertButton.setDisable(disabled);
        clearButton.setDisable(disabled);
        formatComboBox.setDisable(disabled);
        qualitySlider.setDisable(disabled);
        bitrateComboBox.setDisable(disabled);
        constantBitrateRadio.setDisable(disabled);
        variableBitrateRadio.setDisable(disabled);
        sampleRateComboBox.setDisable(disabled);
        channelsComboBox.setDisable(disabled);
        showAdvancedCheckBox.setDisable(disabled);
        presetComboBox.setDisable(disabled);
        loadPresetButton.setDisable(disabled);

        if (!disabled) {
            progressBar.progressProperty().unbind();
            progressBar.setProgress(0);
        }
    }

    @FXML
    private void onClear() {
        audioFiles.clear();
        fileListView.refresh();
        statusLabel.setText("Ready");
        progressBar.setProgress(0);
        showStage1();
    }

    @FXML
    private void onClose() {
        if (executorService != null) {
            executorService.shutdown();
        }
        Platform.exit();
    }

    private void showStage1() {
        fileDropStage.setVisible(true);
        fileDropStage.setManaged(true);
        configStage.setVisible(false);
        configStage.setManaged(false);
    }

    private void showStage2() {
        fileDropStage.setVisible(false);
        fileDropStage.setManaged(false);
        configStage.setVisible(true);
        configStage.setManaged(true);

        configFileCountLabel.setText(audioFiles.size() + " file(s) selected");
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
