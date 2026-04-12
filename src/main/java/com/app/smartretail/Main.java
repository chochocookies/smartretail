package com.app.smartretail;

import com.app.smartretail.utils.UITheme;
import com.app.smartretail.view.auth.LoginForm;
import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        UITheme.apply();
        SwingUtilities.invokeLater(() -> new LoginForm().setVisible(true));
    }
}
