package cz.hudecekpetr.snowride.tree;

import java.util.ArrayList;
import java.util.List;

public class TextOnlyRobotSection extends RobotSection {
    private final String text;

    public TextOnlyRobotSection(SectionHeader header, String text) {
        super(header);
        this.text = text;
    }

    @Override
    public void serializeInto(StringBuilder sb) {
        header.serializeInto(sb);
        sb.append(this.text);
    }

    @Override
    public List<HighElement> getHighElements() {
        return new ArrayList<>();
    }

    @Override
    public void optimizeStructure() {
        // do nothing
    }
}
