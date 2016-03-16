package frontend;

import java.awt.Component;

import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;

public class ImageListCellRenderer extends DefaultListCellRenderer {

	@Override
	public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
			boolean cellHasFocus) {

		super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
		if (!(value instanceof ImageListCell)) {
			System.out.println("Unrecognized type in getListCellRendererComponent");
			return this;
		}
		final ImageListCell c = (ImageListCell) value;
		setIcon(c.getIcon());
		setText(c.getText());
		return this;
	}
}
