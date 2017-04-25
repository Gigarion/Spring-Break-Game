package Gui;

import Equipment.ItemStorage;
import Equipment.Weapon;
import Tools.MapBuilder;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.tools.Tool;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.Socket;
import java.util.Scanner;

/**
 * Created by Gig on 4/4/2017.
 */
public class ToolMenu extends JFrame {
    private JPanel mainMenuPanel;
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
    private JButton beginMapBuilderButton;
    private JTextField boxSizeField;
    private JTextField maxXField;
    private JTextField maxYField;
    private JComboBox mapListComboBox;
    private JPanel toolPane;
    private JTextField weaponStringField;
    private JTextField pFactoryStringField;
    private JButton saveWeaponButton;
    private JTextField weaponFileNameField;
    private JTextField weightField;
    private JTextField maxCountField;
    private JButton loadFileButton;
    private JComboBox weaponFileComboBox;
    private JLabel outputLabel;

    public ToolMenu() {
        setSize(1000, 900);
        setContentPane(toolPane);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        setMapBuilderListeners();
        setHitScanListeners();
        setProjectileListeners();
        setWeaponListeners();
        setSaveWeaponListeners();
        setupLoadWeaponUI();

        tabbedPane.addChangeListener((ChangeEvent e) -> {
            switch (tabbedPane.getSelectedIndex()) {
                case 0:
                    getRootPane().setDefaultButton(generateWeaponStringButton);
                    break;
                case 1:
                    getRootPane().setDefaultButton(generateProjectileStringButton);
                    break;
                case 2:
                    getRootPane().setDefaultButton(saveWeaponButton);
                    break;
                case 3:
                    getRootPane().setDefaultButton(generateHitScanButton);
                    break;
                case 4:
                    getRootPane().setDefaultButton(beginMapBuilderButton);
                    break;
                default:
                    getRootPane().setDefaultButton(null);
            }
        });

        setVisible(true);
    }

    private void setWeaponListeners() {
        generateWeaponStringButton.addActionListener((ActionEvent e) -> {
                StringBuilder sb = new StringBuilder();
                sb.append(wpnNameField.getText());
                sb.append('/');
                sb.append(ammoComboBox.getSelectedItem().toString().toUpperCase());
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
                sb.append('/');
                sb.append(weightField.getText());
                sb.append('/');
                sb.append(maxCountField.getText());
                outputField.setText(sb.toString());
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
                sb.append(hRangeField.getText());
                sb.append("/");
                sb.append(hDamageField.getText());
                sb.append("/");
                sb.append(hPierceCountField.getText());
                sb.append("/");
                sb.append(hCountField.getText());
                sb.append("/");
                sb.append(showLineRadioButton.isSelected());
                hitScanOutput.setText(sb.toString());
            }
        });
    }

    private void setMapBuilderListeners() {
        mapListComboBox.addItem("New Map");
        try {
            File folder = new File("data/Maps/");
            System.out.println(folder.getAbsolutePath());
            if (folder.isDirectory()) {
                for (File f : folder.listFiles()) {
                    System.out.println("here");
                    mapListComboBox.addItem(f.getName());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        beginMapBuilderButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent ae) {
                try {
                    int maxX = Integer.parseInt(maxXField.getText());
                    int maxY = Integer.parseInt(maxYField.getText());
                    int boxSize = Integer.parseInt(boxSizeField.getText());
                    String selectedMap = (String) mapListComboBox.getSelectedItem();
                    if (selectedMap.contains(".gm")) {
                        new MapBuilder(selectedMap);
                    } else {
                        new MapBuilder(maxX, maxY, boxSize, "map.png");
                    }
                    setVisible(false);
                } catch (Exception e) {
                    System.out.println("Invalid fields");
                    e.printStackTrace();
                }
            }
        });
    }

    private void setupLoadWeaponUI() {
        try {
            File folder = new File("data/Items/");
            System.out.println(folder.getAbsolutePath());
            if (folder.isDirectory()) {
                for (File f : folder.listFiles()) {
                    System.out.println("here");
                    weaponFileComboBox.addItem(f.getName());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        loadFileButton.addActionListener((e) -> {
            ItemStorage is = ItemStorage.loadItemStore(weaponFileComboBox.getSelectedItem().toString());
            wpnNameField.setText(is.getName());
        });
    }

    private void setSaveWeaponListeners() {
        saveWeaponButton.addActionListener((ActionEvent e) -> {
            Weapon weapon = new Weapon(weaponStringField.getText(), pFactoryStringField.getText());
            ItemStorage is = ItemStorage.getWeaponStore(weapon);
            ItemStorage.writeItemStore(is);
        });
    }

    public static void main(String[] args) {
        new ToolMenu();
    }
}
