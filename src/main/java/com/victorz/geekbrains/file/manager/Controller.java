package com.victorz.geekbrains.file.manager;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.util.Callback;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Comparator;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;


public class Controller implements Initializable {
    @FXML
    ListView<FileInfo> filesList;

    @FXML
    TextField pathField;

    Path root;

    Path selectedCopyFile;
    Path selectedMoveFile;

    public void menuItemFileExitAction(ActionEvent actionEvent) {
        Platform.exit();
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        filesList.setCellFactory(new Callback<ListView<FileInfo>, ListCell<FileInfo>>() {
            @Override
            public ListCell<FileInfo> call(ListView<FileInfo> fileInfoListView) {
                return new ListCell<>(){
                    @Override
                    protected void updateItem(FileInfo item, boolean empty) {
                        super.updateItem(item, empty);
                        if(item==null || empty){
                            setText(null);
                            setStyle("");
                        } else {
                            String formattedFileName = String.format("%-30s",item.getFilename());
                            String formattedFileSize = String.format("%,d bytes",item.getSize());
                            if(item.isDirectory() || item.isUpElement()){
                                formattedFileSize = String.format("%s","[ DIR ]");
                            }
                            String text = String.format("%s %-20s",formattedFileName,formattedFileSize);
                            setText(text);
                        }
                    }
                };
            }
        });
        goToPath(Paths.get("1"));
    }

    public void goToPath(Path path){
        root = path;
        pathField.setText(root.toAbsolutePath().toString());
        filesList.getItems().clear();
        filesList.getItems().add(new FileInfo(FileInfo.UP_TOKEN,-2L));
        filesList.getItems().addAll(scanFiles(path));
        filesList.getItems().sort(new Comparator<FileInfo>() {
            @Override
            public int compare(FileInfo o1, FileInfo o2) {
                if(o1.getFilename()==FileInfo.UP_TOKEN) return -1;
                if(Math.signum(o1.getSize()) != Math.signum(o2.getSize())) {
                    return new Long(o1.getSize() - o2.getSize()).intValue();
                } else {
                    return o1.getFilename().compareTo(o2.getFilename());
                }
            }
        });
    }

    public List<FileInfo> scanFiles(Path root){
        try {
//            List<FileInfo> out = new ArrayList<>();
//            List<Path> pathsInRoot = null;
//            pathsInRoot = Files.list(root).collect(Collectors.toList());
//            for (Path p : pathsInRoot){
//                out.add(new FileInfo(p));
//            }
//            return out;
            return Files.list(root).map(FileInfo::new).collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("Files Scan Exception " + root);
        }

    }

    public void filesListClicked(MouseEvent mouseEvent) {
        if(mouseEvent.getClickCount() == 2){
            FileInfo fileInfo = filesList.getSelectionModel().getSelectedItem();
            if(fileInfo!=null){
                if(fileInfo.isDirectory()){
                    Path pathTo = root.resolve(fileInfo.getFilename());
                    goToPath(pathTo);
                }
                if(fileInfo.isUpElement()){
                    Path pathTo = root.toAbsolutePath().getParent();
                    goToPath(pathTo);
                }
            }
        }
    }

    public void refresh(){
        goToPath(root);
    }

    public void copyAction(ActionEvent actionEvent) {
        FileInfo fileInfo = filesList.getSelectionModel().getSelectedItem();
        if(selectedCopyFile == null && (fileInfo.isDirectory() || fileInfo.isUpElement() || fileInfo==null) ){
            return;
        }
        if( selectedCopyFile == null ){
            selectedCopyFile = root.resolve(fileInfo.getFilename());
            ((Button)actionEvent.getSource()).setText("Копируется: " + fileInfo.getFilename());
            return;
        }
        if(selectedCopyFile !=null){
            try {
                Files.copy(selectedCopyFile, root.resolve(selectedCopyFile.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                selectedCopyFile = null;
                ((Button)actionEvent.getSource()).setText("Копирование");
                refresh();
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Невозможно скопировать файл");
                alert.showAndWait();
            }
        }
    }

    public void moveAction(ActionEvent actionEvent) {
        FileInfo fileInfo = filesList.getSelectionModel().getSelectedItem();
        if(selectedMoveFile == null && (fileInfo.isDirectory() || fileInfo.isUpElement() || fileInfo==null) ){
            return;
        }
        if( selectedMoveFile == null ){
            selectedMoveFile = root.resolve(fileInfo.getFilename());
            ((Button)actionEvent.getSource()).setText("Перемещается: " + fileInfo.getFilename());
            return;
        }
        if(selectedMoveFile !=null){
            try {
                Files.move(selectedMoveFile, root.resolve(selectedMoveFile.getFileName()), StandardCopyOption.REPLACE_EXISTING);
                selectedMoveFile = null;
                ((Button)actionEvent.getSource()).setText("Перемещение");
                refresh();
            } catch (IOException e) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Невозможно скопировать файл");
                alert.showAndWait();
            }
        }
    }

    public void deleteAction(ActionEvent actionEvent) {
        FileInfo fileInfo = filesList.getSelectionModel().getSelectedItem();
        if(fileInfo==null || fileInfo.isUpElement() || fileInfo.isDirectory()){
            return;
        }

        try {
            Files.delete( root.resolve(fileInfo.getFilename()) ) ;
            refresh();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.ERROR, "Невозможно удалить файл");
            alert.showAndWait();
        }
    }
}
