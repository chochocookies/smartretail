package com.app.smartretail;


import javax.swing.SwingUtilities;

import com.app.smartretail.utils.UITheme;
import com.app.smartretail.view.auth.LoginForm;

public class Main {
    public static void main(String[] args) {
        UITheme.apply();
        SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
    }
}
