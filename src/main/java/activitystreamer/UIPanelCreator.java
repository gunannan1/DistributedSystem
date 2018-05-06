package activitystreamer;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * UIPanelCreator
 * <p>
 * Author Ning Kang
 * Date 5/5/18
 */

public class UIPanelCreator {
	public static JTextArea addTextPanel(JPanel mainPanel, String title) {
		JTextArea textArea;
		JPanel logPanel = new JPanel();
		logPanel.setLayout(new BorderLayout());
		Border lineBorder = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.lightGray), title);
		logPanel.setBorder(lineBorder);
		logPanel.setName(title);

		textArea = new JTextArea();
		textArea.setLineWrap(false);
		textArea.setFont(new Font(Font.DIALOG, Font.PLAIN, 10));
		JScrollPane scrollPane = new JScrollPane(textArea);
		logPanel.add(scrollPane, BorderLayout.CENTER);
		scrollPane.setAutoscrolls(true);
		mainPanel.add(logPanel);

		return textArea;
	}

	public static  DefaultTableModel addTablePanel(JPanel mainPanel, String title, String[] columns) {
		JPanel outerPanel = new JPanel();
		outerPanel.setLayout(new BorderLayout());
		Border lineBorder = BorderFactory.createTitledBorder(BorderFactory.createLineBorder(Color.lightGray), title);
		outerPanel.setBorder(lineBorder);
		outerPanel.setName(title);

		DefaultTableModel model = new DefaultTableModel();
		for(String c:columns) {
			model.addColumn(c);
		}
		JTable table = new JTable(model);
		outerPanel.add(table);
		mainPanel.add(outerPanel);
		return model;
	}
}
