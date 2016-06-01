package frontend;

import java.awt.Component;
import java.util.logging.Logger;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

import backend.Log;

class ImageListCellRenderer extends DefaultListCellRenderer {

    private final static Logger log = Log.getLogger();

    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
            boolean cellHasFocus) {

        super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (!(value instanceof ImageListCell)) {
            log.finer("Unrecognized type in getListCellRendererComponent");
            return this;
        }
        final ImageListCell c = (ImageListCell) value;
        setIcon(c.getIcon());
        setToolTipText(c.getText());
        setText("");
        return this;
    }
}
