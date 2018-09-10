package demonworld.view;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JButton;
import javax.swing.JToolBar;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;

public class CustomToolBar extends JToolBar {
	
	
    public CustomToolBar() {
        setBorder((Border) new LineBorder(Color.BLACK, 1));
        setOpaque(false);
    }

    @Override
    protected void addImpl(Component comp, Object constraints, int index) {
        super.addImpl(comp, constraints, index);
        if (comp instanceof JButton) {
            ((JButton) comp).setContentAreaFilled(false);
        }
    }

}