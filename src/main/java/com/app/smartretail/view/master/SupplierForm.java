package com.app.smartretail.view.master;
import com.app.smartretail.utils.UITheme;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
/** SupplierForm — data supplier telah dipindahkan ke halaman Purchase. */
public class SupplierForm extends JPanel {
    public SupplierForm() {
        setLayout(new BorderLayout()); setBackground(UITheme.BG_SURFACE); setBorder(new EmptyBorder(40,40,40,40));
        JLabel l=new JLabel("<html><center><b>Data Supplier</b><br><br>Data supplier telah dipindahkan ke halaman <b>Purchase → tab Suppliers</b></center></html>");
        l.setFont(UITheme.FONT_H2); l.setForeground(UITheme.TEXT_SECONDARY); l.setHorizontalAlignment(SwingConstants.CENTER);
        add(l, BorderLayout.CENTER);
    }
}
