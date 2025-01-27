package cz.hudecekpetr.snowride.filesystem;

import cz.hudecekpetr.snowride.tree.FileSuite;
import cz.hudecekpetr.snowride.tree.FolderSuite;
import cz.hudecekpetr.snowride.tree.RobotFile;
import cz.hudecekpetr.snowride.ui.MainForm;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;

public class Filesystem {
    private MainForm mainForm;

    public Filesystem(MainForm mainForm) {

        this.mainForm = mainForm;
    }

    public void createNewFolderInTree(FolderSuite parentFolder, String newFolder) {
        Path createWhat = parentFolder.directoryPath.toPath().resolve(newFolder);
        File asFile = createWhat.toAbsolutePath().toFile();
        if (!asFile.mkdir()) {
            throw new RuntimeException("Failed to create the folder.");
        }
        FolderSuite folderSuite = new FolderSuite(asFile, null, newFolder, null, new ArrayList<>());
        parentFolder.children.add(folderSuite);
        parentFolder.treeNode.getChildren().add(folderSuite.treeNode);
        folderSuite.parent = parentFolder;
        mainForm.selectProgrammaticallyAndRememberInHistory(folderSuite);
    }

    public void createNewRobotFile(FolderSuite parentFolder, String newFileWithoutExtension) throws IOException {
        Path createWhat = parentFolder.directoryPath.toPath().resolve(newFileWithoutExtension + ".robot");
        File asFile = createWhat.toAbsolutePath().toFile();
        if (!asFile.createNewFile()) {
            throw new RuntimeException("File already exists.");
        }
        FileSuite fs = new FileSuite(asFile, newFileWithoutExtension, "");
        parentFolder.children.add(fs);
        parentFolder.treeNode.getChildren().add(fs.treeNode);
        fs.parent = parentFolder;
        mainForm.selectProgrammaticallyAndRememberInHistory(fs);
    }
}
