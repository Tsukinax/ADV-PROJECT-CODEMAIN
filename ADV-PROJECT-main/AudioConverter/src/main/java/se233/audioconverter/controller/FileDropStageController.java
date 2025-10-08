package se233.audioconverter.controller;

import javafx.collections.ObservableList;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import se233.audioconverter.model.AudioFile;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class FileDropStageController {
    private static final List<String> SUPPORTED_FORMATS = Arrays.asList("mp3", "wav", "m4a", "flac");

    private final ObservableList<AudioFile> audioFiles;
    private final VBox dropZone;
    private final ListView<AudioFile> filePreviewList;
    private final VBox filePreviewBox;
    private final Label fileCountLabel;
    private final Runnable onFileListChanged;

    public FileDropStageController(ObservableList<AudioFile> audioFiles,
                                   VBox dropZone,
                                   ListView<AudioFile> filePreviewList,
                                   VBox filePreviewBox,
                                   Label fileCountLabel,
                                   Runnable onFileListChanged) {
        this.audioFiles = audioFiles;
        this.dropZone = dropZone;
        this.filePreviewList = filePreviewList;
        this.filePreviewBox = filePreviewBox;
        this.fileCountLabel = fileCountLabel;
        this.onFileListChanged = onFileListChanged;

        setupFileList();
        setupDragAndDrop();
    }

    private void setupFileList() {
        filePreviewList.setItems(audioFiles);
        filePreviewList.setCellFactory(param -> new ListCell<AudioFile>() {
            @Override
            protected void updateItem(AudioFile item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    HBox hbox = new HBox(10);
                    hbox.setAlignment(Pos.CENTER_LEFT);

                    Label fileLabel = new Label(item.getName() + " (" + item.getFormat().toUpperCase() + ")");
                    fileLabel.setMaxWidth(Double.MAX_VALUE);
                    HBox.setHgrow(fileLabel, Priority.ALWAYS);

                    Button deleteButton = new Button("×");
                    deleteButton.setStyle(
                        "-fx-background-color: #a1c1c4; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 16; " +
                        "-fx-font-weight: bold; " +
                        "-fx-padding: 2 8; " +
                        "-fx-cursor: hand;"
                    );
                    deleteButton.setOnAction(e -> {
                        audioFiles.remove(item);
                        updateFilePreview();
                    });

                    hbox.getChildren().addAll(fileLabel, deleteButton);
                    setGraphic(hbox);
                    setText(null);
                }
            }
        });
    }

    private void setupDragAndDrop() {
        dropZone.setOnDragOver(event -> {
            Dragboard db = event.getDragboard();
            if (db.hasFiles()) {
                boolean hasValidFile = db.getFiles().stream()
                        .anyMatch(file -> isAudioFile(file.getName()));
                if (hasValidFile) {
                    event.acceptTransferModes(TransferMode.COPY);
                    dropZone.setStyle("-fx-border-color: #2196F3; -fx-border-width: 3; -fx-border-style: dashed; -fx-border-radius: 10; -fx-background-color: #E3F2FD; -fx-background-radius: 10; -fx-padding: 60;");
                }
            }
            event.consume();
        });

        dropZone.setOnDragExited(event -> {
            dropZone.setStyle("-fx-border-color: #4CAF50; -fx-border-width: 3; -fx-border-style: dashed; -fx-border-radius: 10; -fx-background-color: #f9f9f9; -fx-background-radius: 10; -fx-padding: 60;");
            event.consume();
        });

        dropZone.setOnDragDropped(event -> {
            Dragboard db = event.getDragboard();
            boolean success = false;

            if (db.hasFiles()) {
                List<File> validFiles = db.getFiles().stream()
                        .filter(file -> isAudioFile(file.getName()))
                        .toList();

                for (File file : validFiles) {
                    AudioFile audioFile = new AudioFile(file.getAbsolutePath());
                    boolean exists = audioFiles.stream()
                            .anyMatch(af -> af.getFilePath().equals(audioFile.getFilePath()));
                    if (!exists) {
                        audioFiles.add(audioFile);
                    }
                }

                success = !validFiles.isEmpty();
                updateFilePreview();
            }

            dropZone.setStyle("-fx-border-color: #4CAF50; -fx-border-width: 3; -fx-border-style: dashed; -fx-border-radius: 10; -fx-background-color: #f9f9f9; -fx-background-radius: 10; -fx-padding: 60;");
            event.setDropCompleted(success);
            event.consume();
        });
    }

    public void addFiles(List<File> files) {
        for (File file : files) {
            if (isAudioFile(file.getName())) {
                AudioFile audioFile = new AudioFile(file.getAbsolutePath());
                boolean exists = audioFiles.stream()
                        .anyMatch(af -> af.getFilePath().equals(audioFile.getFilePath()));
                if (!exists) {
                    audioFiles.add(audioFile);
                }
            }
        }
        updateFilePreview();
    }

    public void clearFiles() {
        audioFiles.clear();
        updateFilePreview();
    }

    private void updateFilePreview() {
        if (audioFiles.isEmpty()) {
            filePreviewBox.setVisible(false);
            filePreviewBox.setManaged(false);
            fileCountLabel.setText("0 files");
        } else {
            filePreviewBox.setVisible(true);
            filePreviewBox.setManaged(true);
            fileCountLabel.setText(audioFiles.size() + " file(s)");
        }
        filePreviewList.refresh();

        if (onFileListChanged != null) {
            onFileListChanged.run();
        }
    }

    private boolean isAudioFile(String filename) {
        String extension = "";
        int lastDot = filename.lastIndexOf('.');
        if (lastDot > 0) {
            extension = filename.substring(lastDot + 1).toLowerCase();
        }
        return SUPPORTED_FORMATS.contains(extension);
    }

    public static List<String> getSupportedFormats() {
        return SUPPORTED_FORMATS;
    }
}