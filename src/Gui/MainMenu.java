package Gui;

import Actors.Player;
import Engine.ClientEngine;
import Engine.ServerEngine;
import Util.StdDraw;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

/**
 * Created by Gig on 3/27/2017.
 */
public class MainMenu extends JFrame {
    private JPanel mainMenuPanel;
    private JButton startServerButton;
    private JTextField localhostTextField;
    private JButton enterGameButton;
    private JTextField playerNameField;
    private JTextField wpnNameField;
    private JTextField wpnMaxClipField;
    private JTextField wpnReloadRateField;
    private JTextField wpnFireRateField;
    private JTextField chargeTimeField;
    private JCheckBox throwableCheckBox;
    private JCheckBox chargeableCheckBox;
    private JButton generateWeaponStringButton;
    private JTextField outputField;
    private JTextField pRangeField;
    private JTextField pDamageField;
    private JTextField pCountField;
    private JTextField pRadiusField;
    private JTextField pSpeedField;
    private JTextField pImageField;
    private JButton generateProjectileStringButton;
    private JTextField pOutputField;
    private JTextField hitScanTextField;
    private JTextField pPierceCountField;
    private JTabbedPane tabbedPane;
    private JTextField hDamageField;
    private JTextField hPierceCountField;
    private JTextField hCountField;
    private JRadioButton showLineRadioButton;
    private JTextField hRangeField;
    private JButton generateHitScanButton;
    private JTextField hitScanOutput;
    private JComboBox ammoComboBox;
    private JLabel outputLabel;
    private boolean started;

    public MainMenu()  {
        super("Main Menu");
        setSize(500, 500);
        //pack();
        setContentPane(mainMenuPanel);
        setVisible(true);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        tabbedPane.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                switch(tabbedPane.getSelectedIndex()) {
                    case 0: getRootPane().setDefaultButton(enterGameButton); break;
                    case 1: getRootPane().setDefaultButton(startServerButton); break;
                    case 2: getRootPane().setDefaultButton(generateWeaponStringButton); break;
                    case 3: getRootPane().setDefaultButton(generateProjectileStringButton); break;
                    case 4: getRootPane().setDefaultButton(generateHitScanButton); break;
                    default: getRootPane().setDefaultButton(null);
                }
            }
        });

        setListeners();
        setWeaponListeners();
        setProjectileListeners();
        setHitScanListeners();
    }

    private void startGame() {
        ClientEngine ce = new ClientEngine();

        String name = playerNameField.getText();
        if (name.equals(""))
            name = "new player";
        ce.setPlayer(new Player(name));
       //StdDraw.addEngine(ce);
        setVisible(false);
    }

    private void setListeners() {
        startServerButton.addActionListener((ActionEvent e) -> {
            started = true;
            ServerEngine se = new ServerEngine();
        });
        enterGameButton.addActionListener((ActionEvent e) -> {
            startGame();
        });
    }

    private void setWeaponListeners() {
        generateWeaponStringButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                StringBuilder sb = new StringBuilder();
                sb.append(wpnNameField.getText());
                sb.append('/');
                sb.append((String) ammoComboBox.getSelectedItem());
                sb.append('/');
                sb.append(wpnMaxClipField.getText());
                sb.append('/');
                sb.append(wpnFireRateField.getText());
                sb.append('/');
                sb.append(wpnReloadRateField.getText());
                sb.append('/');
                sb.append(throwableCheckBox.isSelected());
                sb.append('/');
                sb.append(chargeableCheckBox.isSelected());
                sb.append('/');
                sb.append(chargeTimeField.getText());
                outputField.setText(sb.toString());
            }

        });
    }

    private void setProjectileListeners() {
        generateProjectileStringButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                StringBuilder sb = new StringBuilder("P/");
                sb.append(pRangeField.getText());
                sb.append("/");
                sb.append(pDamageField.getText());
                sb.append("/");
                sb.append(pPierceCountField.getText());
                sb.append("/");
                sb.append(pCountField.getText());
                sb.append("/");
                sb.append(pRadiusField.getText());
                sb.append("/");
                sb.append(pSpeedField.getText());
                sb.append("/");
                sb.append(pImageField.getText());
                sb.append("/");
                pOutputField.setText(sb.toString());
            }
        });
    }

    private void setHitScanListeners() {
        generateHitScanButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                StringBuilder sb = new StringBuilder("H/");
                sb.append(pRangeField.getText());
                sb.append("/");
                sb.append(pDamageField.getText());
                sb.append("/");
                sb.append(hPierceCountField.getText());
                sb.append("/");
                sb.append(pCountField.getText());
                sb.append("/");
                sb.append(showLineRadioButton.isSelected());
                hitScanOutput.setText(sb.toString());
            }
        });
    }

    public static void main(String[] args) {
        MainMenu mainMenu = new MainMenu();
    }
}
