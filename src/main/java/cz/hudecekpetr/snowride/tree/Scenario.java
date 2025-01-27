package cz.hudecekpetr.snowride.tree;

import cz.hudecekpetr.snowride.Extensions;
import cz.hudecekpetr.snowride.filesystem.LastChangeKind;
import cz.hudecekpetr.snowride.fx.bindings.PositionInListProperty;
import cz.hudecekpetr.snowride.lexer.Cell;
import cz.hudecekpetr.snowride.lexer.LogicalLine;
import cz.hudecekpetr.snowride.runner.TestResult;
import cz.hudecekpetr.snowride.ui.Images;
import cz.hudecekpetr.snowride.ui.MainForm;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class Scenario extends HighElement {

    private Cell nameCell;
    private boolean isTestCase;
    public TestResult lastTestResult = TestResult.NOT_YET_RUN;
    public HashSet<Tag> actualTags = new HashSet<>();

    public List<String> getSemanticsArguments() {
        return semanticsArguments;
    }

    private List<String> semanticsArguments = new ArrayList<>();

    public ObservableList<LogicalLine> getLines() {
        return lines;
    }

    private final ObservableList<LogicalLine> lines;

    public Scenario(Cell nameCell, boolean isTestCase, List<LogicalLine> lines) {
        super(nameCell.contents, null, new ArrayList<>());

        this.nameCell = nameCell;
        this.setTestCase(isTestCase);
        this.lines = FXCollections.observableList(lines);
        for (int i = 0; i < lines.size(); i++) {
            this.lines.get(i).lineNumber = new PositionInListProperty<>(this.lines.get(i), this.lines);
            this.lines.get(i).belongsToHighElement = this;
        }
    }

    @Override
    public void saveAll() throws IOException {
        // Saved as part of the file suite.
    }


    @Override
    public void deleteSelf(MainForm mainForm) {
        this.parent.dissociateSelfFromChild(this);
        this.parent.markAsStructurallyChanged(mainForm);
    }

    @Override
    public void renameSelfTo(String newName, MainForm mainForm) {
        this.nameCell = new Cell(newName, nameCell.postTrivia, null);
        this.shortNameProperty.set(newName);
        this.refreshToString();
        this.markAsStructurallyChanged(mainForm);
    }

    @Override
    public void applyText() {
        this.parent.applyText();
    }

    @Override
    public void markAsStructurallyChanged(MainForm mainForm) {
        mainForm.changeOccurredTo(this, LastChangeKind.STRUCTURE_CHANGED);
        this.parent.markAsStructurallyChanged(mainForm);
    }

    @Override
    protected void optimizeStructure() {
        Extensions.optimizeLines(lines);
    }

    @Override
    protected void ancestorRenamed(File oldFile, File newFile) {
        // don't care, this is for files
    }

    @Override
    public Suite asSuite() {
        return (Suite) this.parent;
    }

    public void serializeInto(StringBuilder sb) {
        sb.append(nameCell.contents);
        sb.append(nameCell.postTrivia);
        sb.append("\n");
        lines.forEach(ll -> {
            ll.serializeInto(sb);
        });
    }

    public boolean isTestCase() {
        return isTestCase;
    }

    public void setTestCase(boolean testCase) {
        isTestCase = testCase;
        if (isTestCase) {
            this.imageView.setImage(Images.testIcon);
            this.checkbox.setVisible(true);
        } else {
            this.imageView.setImage(Images.keywordIcon);
            this.checkbox.setVisible(false);
        }
    }

    @Override
    public Image getAutocompleteIcon() {
        return this.isTestCase ? Images.testIcon : Images.keywordIcon;
    }

    @Override
    public String getAutocompleteText() {
        return (this.isTestCase ? "[test] " : "[keyword] ") + toString();
    }

    @Override
    public String getItalicsSubheading() {
        return isTestCase ? "Test case" : "Keyword";
    }

    public void markTestStatus(TestResult lastTestResult) {
        this.lastTestResult = lastTestResult;
        switch (lastTestResult) {
            case NOT_YET_RUN:
                this.imageView.setImage(Images.testIcon);
                break;
            case PASSED:
                this.imageView.setImage(Images.yes);
                break;
            case FAILED:
                this.imageView.setImage(Images.no);
                break;
        }
    }

    @Override
    public String getQuickDocumentationCaption() {
        return toString();
    }

    public void analyzeSemantics() {
        ArrayList<String> argCells = new ArrayList<>();
        for (LogicalLine line : getLines()) {
            if (line.cells.size() >= 3) {
                if (line.cells.get(1).contents.equalsIgnoreCase("[Documentation]")) {
                    List<String> docCells = new ArrayList<>();
                    for (int i = 2; i < line.cells.size(); i++) {
                        docCells.add(line.cells.get(i).contents);
                    }
                    semanticsDocumentation = String.join("\n", docCells);
                }
                else if (line.cells.get(1).contents.equalsIgnoreCase("[Arguments]")) {
                    for (int i = 2; i < line.cells.size(); i++) {
                        argCells.add(line.cells.get(i).contents);
                    }

                }
            }
        }
        this.semanticsArguments = argCells;
        if (argCells.size() > 0) {
            this.semanticsDocumentation =  "*Args:* " + String.join(", ", argCells) + "\n" + (semanticsDocumentation != null ? semanticsDocumentation : "");
        }
    }

    @Override
    public void updateTagsForSelfAndChildren() {
        actualTags = new HashSet<>(parent.forceTagsCumulative);
        boolean foundTags = false;
        for (LogicalLine line : getLines()) {
            if (line.cells.size() >= 2) {
                if (line.cells.get(1).contents.equalsIgnoreCase("[tags]")) {
                    for (int i = 2; i < line.cells.size(); i++) {
                        actualTags.add(new Tag(line.cells.get(i).contents));
                    }
                    foundTags = true;
                }
            }
        }
        if (!foundTags) {
            actualTags.addAll(parent.defaultTags);
        }
    }

}
