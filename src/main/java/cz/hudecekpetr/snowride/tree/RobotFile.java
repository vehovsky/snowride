package cz.hudecekpetr.snowride.tree;

import cz.hudecekpetr.snowride.Extensions;
import cz.hudecekpetr.snowride.NewlineStyle;
import cz.hudecekpetr.snowride.semantics.Setting;

import java.util.ArrayList;
import java.util.List;

public class RobotFile {
    public List<RobotSection> sections = new ArrayList<>();
    public List<Exception> errors = new ArrayList<>();

    public List<HighElement> getHighElements() {
        List<HighElement> he = new ArrayList<>();
        for(RobotSection section : sections) {
            he.addAll(section.getHighElements());
        }
        return he;
    }

    public String serialize(NewlineStyle newlineStyle) {
        StringBuilder sb = new StringBuilder();
        for(RobotSection robotSection : sections) {
            robotSection.serializeInto(sb);
        }
        if (errors.size() > 0) {
            throw new RuntimeException("There were parse errors. Editing or saving is not possible.");
        }
        String str =  sb.toString(); // RIDE leaves the final newline in //Extensions.removeFinalNewlineIfAny(sb.toString());
        return newlineStyle.convertToStyle(str);
    }

    public TestCasesSection findOrCreateTestCasesSection() {
        for (RobotSection section : sections) {
            if (section.header.sectionKind == SectionKind.TEST_CASES) {
                return (TestCasesSection) section;
            }
        }
        TestCasesSection newSection = new TestCasesSection(new SectionHeader(SectionKind.TEST_CASES, "*** Test Cases ***\n"), new ArrayList<>());
        sections.add(newSection);
        return newSection;
    }

    public KeywordsSection findOrCreateKeywordsSection() {
        for (RobotSection section : sections) {
            if (section.header.sectionKind == SectionKind.KEYWORDS) {
                return (KeywordsSection) section;
            }
        }
        KeywordsSection newSection = new KeywordsSection(new SectionHeader(SectionKind.KEYWORDS, "*** Keywords ***\n"), new ArrayList<>());
        sections.add(newSection);
        return newSection;
    }

    public void analyzeSemantics(Suite suite) {
        KeyValuePairSection settings = this.findSettingsSection();
        if (settings != null) {
            for (Setting s : settings.createSettings()) {
                if (s.key.equalsIgnoreCase("Documentation")) {
                    suite.semanticsDocumentation = s.firstValue; // TODO make it all values concatenated
                }
            }
        }
        for (HighElement sc : getHighElements()) {
            if (sc instanceof Scenario) {
                Scenario scc = (Scenario) sc;
                scc.analyzeSemantics();
            }
        }
    }

    public KeyValuePairSection findOrCreateSettingsSection() {
        KeyValuePairSection settings = findSettingsSection();
        if (settings == null) {
            settings = new KeyValuePairSection(new SectionHeader(SectionKind.SETTINGS, "*** Settings ***\n"), new ArrayList<>());
            settings.artificiallyCreated = true;
            sections.add(settings);
        }
        return settings;
    }
    public KeyValuePairSection findOrCreateVariablesSection() {
        KeyValuePairSection settings = findVariablesSection();
        if (settings == null) {
            settings = new KeyValuePairSection(new SectionHeader(SectionKind.VARIABLES, "*** Variables ***\n"), new ArrayList<>());
            settings.artificiallyCreated = true;
            sections.add(settings);
        }
        return settings;
    }
    private KeyValuePairSection findSettingsSection() {
        for (RobotSection section : sections) {
            if (section.header.sectionKind == SectionKind.SETTINGS) {
                return (KeyValuePairSection) section;
            }
        }
        return null;
    }
    private KeyValuePairSection findVariablesSection() {
        for (RobotSection section : sections) {
            if (section.header.sectionKind == SectionKind.VARIABLES) {
                return (KeyValuePairSection) section;
            }
        }
        return null;
    }
}
