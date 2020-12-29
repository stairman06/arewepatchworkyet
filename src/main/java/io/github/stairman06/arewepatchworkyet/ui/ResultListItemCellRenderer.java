package io.github.stairman06.arewepatchworkyet.ui;

import io.github.stairman06.arewepatchworkyet.analyze.ClassMember;
import io.github.stairman06.arewepatchworkyet.mappings.MappingUtils;

import javax.swing.*;
import java.awt.*;

public class ResultListItemCellRenderer extends JButton implements ListCellRenderer<ResultListItem> {

    // Make the button look like a JLabel https://stackoverflow.com/a/3026065
    @Override
    public boolean isFocusPainted() {
        return false;
    }

    @Override
    public boolean isBorderPainted() {
        return false;
    }

    @Override
    public Insets getMargin() {
        return new Insets(0, 0, 0, 0);
    }

    @Override
    public int getHorizontalAlignment() {
        return SwingConstants.LEFT;
    }

    @Override
    public boolean isContentAreaFilled() {
        return false;
    }

    @Override
    public boolean isOpaque() {
        return true;
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends ResultListItem> list, ResultListItem item, int index, boolean isSelected, boolean cellHasFocus) {
        setBackground(isSelected ? Color.LIGHT_GRAY : Color.WHITE);

        ResultListItem.Type type = item.getType();
        if (type == ResultListItem.Type.CLASS) {
            setText(MappingUtils.getClassName((String) item.getObject()));
        } else if (type == ResultListItem.Type.CLASS_MEMBER) {
            ClassMember classMember = (ClassMember) item.getObject();

            if (classMember.type == ClassMember.Type.METHOD) {
                setText("      M " + classMember.name + MappingUtils.getMethodDescriptor(classMember.descriptor));
            } else if (classMember.type == ClassMember.Type.FIELD) {
                setText("      F " + MappingUtils.getFieldDescriptor(classMember.descriptor) + " " + classMember.name);
            }
        }

        return this;
    }
}
