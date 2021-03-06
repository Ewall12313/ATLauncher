/**
 * Copyright 2013-2014 by ATLauncher and Contributors
 *
 * This work is licensed under the Creative Commons Attribution-ShareAlike 3.0 Unported License.
 * To view a copy of this license, visit http://creativecommons.org/licenses/by-sa/3.0/.
 */
package com.atlauncher.gui.card;

import com.atlauncher.App;
import com.atlauncher.LogManager;
import com.atlauncher.data.Instance;
import com.atlauncher.data.Language;
import com.atlauncher.evnt.listener.RelocalizationListener;
import com.atlauncher.evnt.manager.RelocalizationManager;
import com.atlauncher.gui.components.CollapsiblePanel;
import com.atlauncher.gui.components.ImagePanel;
import com.atlauncher.gui.dialogs.BackupDialog;
import com.atlauncher.gui.dialogs.EditModsDialog;
import com.atlauncher.gui.dialogs.InstanceInstallerDialog;
import com.atlauncher.gui.dialogs.ProgressDialog;
import com.atlauncher.gui.dialogs.RenameInstanceDialog;
import com.atlauncher.utils.Utils;

import java.awt.BorderLayout;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.sql.Timestamp;
import java.util.Date;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.filechooser.FileNameExtensionFilter;

/**
 * <p/>
 * Class for displaying instances in the Instance Tab
 *
 * @author Ryan
 */
public class InstanceCard
extends CollapsiblePanel
implements RelocalizationListener{
    private final JSplitPane splitter = new JSplitPane();
    private final Instance instance;
    private final JPanel rightPanel = new JPanel();
    private final JTextArea descArea = new JTextArea();
    private final ImagePanel image;
    private final JButton playButton = new JButton("Play");
    private final JButton reinstallButton = new JButton("Reinstall");
    private final JButton renameButton  = new JButton("Rename");
    private final JButton backupButton = new JButton("Backup");
    private final JButton cloneButton = new JButton("Clone");
    private final JButton deleteButton = new JButton("Delete");
    private final JButton editButton = new JButton("Edit Mods");
    private final JButton openButton = new JButton("Open Folder");

    public InstanceCard(Instance instance){
        super(instance);
        this.instance = instance;
        this.image = new ImagePanel(instance.getImage().getImage());
        this.splitter.setLeftComponent(this.image);
        this.splitter.setRightComponent(this.rightPanel);
        this.splitter.setEnabled(false);

        this.descArea.setText(instance.getPackDescription());
        this.descArea.setBorder(BorderFactory.createEmptyBorder());
        this.descArea.setEditable(false);
        this.descArea.setHighlighter(null);
        this.descArea.setLineWrap(true);
        this.descArea.setWrapStyleWord(true);
        this.descArea.setEditable(false);

        JPanel top = new JPanel(new FlowLayout());
        JPanel bottom = new JPanel(new FlowLayout());
        JSplitPane as = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        as.setEnabled(false);
        as.setTopComponent(top);
        as.setBottomComponent(bottom);
        top.add(this.playButton);
        top.add(this.reinstallButton);
        top.add(this.renameButton);
        top.add(this.backupButton);
        bottom.add(this.cloneButton);
        bottom.add(this.deleteButton);
        bottom.add(this.editButton);
        bottom.add(this.openButton);

        this.rightPanel.setLayout(new BorderLayout());
        this.rightPanel.setPreferredSize(new Dimension(this.rightPanel.getPreferredSize().width, 180));
        this.rightPanel.add(new JScrollPane(this.descArea, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), BorderLayout.CENTER);
        this.rightPanel.add(as, BorderLayout.SOUTH);

        this.getContentPane().setLayout(new BorderLayout());
        this.getContentPane().add(this.splitter, BorderLayout.CENTER);

        RelocalizationManager.addListener(this);

        this.addActionListeners();
        this.addMouseListeners();
        this.validatePlayable();
    }

    private void validatePlayable(){
        if (!instance.isPlayable()) {
            for (ActionListener al : playButton.getActionListeners()) {
                playButton.removeActionListener(al);
            }
            playButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String[] options = {Language.INSTANCE.localize("common.ok")};
                    JOptionPane.showOptionDialog(App.settings.getParent(), Language.INSTANCE.localize("instance" + "" +
                                    ".corruptplay"), Language.INSTANCE.localize("instance.corrupt"),
                            JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
                }
            });
            for (ActionListener al : backupButton.getActionListeners()) {
                backupButton.removeActionListener(al);
            }
            backupButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String[] options = {Language.INSTANCE.localize("common.ok")};
                    JOptionPane.showOptionDialog(App.settings.getParent(), Language.INSTANCE.localize("instance" + "" +
                                    ".corruptbackup"), Language.INSTANCE.localize("instance.corrupt"),
                            JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
                }
            });
            for (ActionListener al : cloneButton.getActionListeners()) {
                cloneButton.removeActionListener(al);
            }
            cloneButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    String[] options = {Language.INSTANCE.localize("common.ok")};
                    JOptionPane.showOptionDialog(App.settings.getParent(), Language.INSTANCE.localize("instance" + "" +
                                    ".corruptclone"), Language.INSTANCE.localize("instance.corrupt"),
                            JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
                }
            });
        }
    }

    private void addActionListeners(){
        this.playButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                if (instance.hasUpdate() && !instance.hasUpdateBeenIgnored(instance.getLatestVersion()) && !instance
                        .isDev()) {
                    String[] options = {Language.INSTANCE.localize("common.yes"), Language.INSTANCE.localize("common"
                            + ".no"), Language.INSTANCE.localize("instance.dontremindmeagain")};
                    int ret = JOptionPane.showOptionDialog(App.settings.getParent(),
                            "<html><p align=\"center\">" + App.settings.getLocalizedString("instance.updatenow",
                                    "<br/><br/>") + "</p></html>", Language.INSTANCE.localize("instance" + "" +
                                    ".updateavailable"), JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null,
                            options, options[0]);
                    if (ret == 0) {
                        if (App.settings.getAccount() == null) {
                            String[] optionss = {Language.INSTANCE.localize("common.ok")};
                            JOptionPane.showOptionDialog(App.settings.getParent(),
                                    Language.INSTANCE.localize("instance.cantupdate"),
                                    Language.INSTANCE.localize("instance.noaccountselected"),
                                    JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, optionss, optionss[0]);
                        } else {
                            new InstanceInstallerDialog(instance, true, false);
                        }
                    } else if (ret == 1 || ret == JOptionPane.CLOSED_OPTION) {
                        if (!App.settings.isMinecraftLaunched()) {
                            if (instance.launch()) {
                                App.settings.setMinecraftLaunched(true);
                            }
                        }
                    } else if (ret == 2) {
                        instance.ignoreUpdate();
                        if (!App.settings.isMinecraftLaunched()) {
                            if (instance.launch()) {
                                App.settings.setMinecraftLaunched(true);
                            }
                        }
                    }
                } else {
                    if (!App.settings.isMinecraftLaunched()) {
                        if (instance.launch()) {
                            App.settings.setMinecraftLaunched(true);
                        }
                    }
                }
            }
        });
        this.reinstallButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                if (App.settings.getAccount() == null) {
                    String[] options = {Language.INSTANCE.localize("common.ok")};
                    JOptionPane.showOptionDialog(App.settings.getParent(), Language.INSTANCE.localize("instance" + "" +
                                    ".cantreinstall"), Language.INSTANCE.localize("instance.noaccountselected"),
                            JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
                } else {
                    new InstanceInstallerDialog(instance);
                }
            }
        });
        this.renameButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                new RenameInstanceDialog(instance);
            }
        });
        this.backupButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                if (App.settings.isAdvancedBackupsEnabled()) {
                    new BackupDialog(instance).setVisible(true);
                } else {
                    if (instance.getSavesDirectory().exists()) {
                        int ret = JOptionPane.showConfirmDialog(App.settings.getParent(),
                                "<html><p align=\"center\">" + App.settings.getLocalizedString("backup.sure",
                                        "<br/><br/>") + "</p></html>", App.settings.getLocalizedString("backup" + "" +
                                        ".backingup", instance.getName()), JOptionPane.YES_NO_OPTION);
                        if (ret == JOptionPane.YES_OPTION) {
                            final JDialog dialog = new JDialog(App.settings.getParent(),
                                    App.settings.getLocalizedString("backup.backingup", instance.getName()),
                                    ModalityType.APPLICATION_MODAL);
                            dialog.setSize(300, 100);
                            dialog.setLocationRelativeTo(App.settings.getParent());
                            dialog.setResizable(false);

                            JPanel topPanel = new JPanel();
                            topPanel.setLayout(new BorderLayout());
                            JLabel doing = new JLabel(App.settings.getLocalizedString("backup.backingup",
                                    instance.getName()));
                            doing.setHorizontalAlignment(JLabel.CENTER);
                            doing.setVerticalAlignment(JLabel.TOP);
                            topPanel.add(doing);

                            JPanel bottomPanel = new JPanel();
                            bottomPanel.setLayout(new BorderLayout());
                            JProgressBar progressBar = new JProgressBar();
                            bottomPanel.add(progressBar, BorderLayout.NORTH);
                            progressBar.setIndeterminate(true);

                            dialog.add(topPanel, BorderLayout.CENTER);
                            dialog.add(bottomPanel, BorderLayout.SOUTH);

                            final Thread backupThread = new Thread() {
                                public void run() {
                                    Timestamp timestamp = new Timestamp(new Date().getTime());
                                    String time = timestamp.toString().replaceAll("[^0-9]", "_");
                                    String filename = instance.getSafeName() + "-" + time.substring(0,
                                            time.lastIndexOf("_")) + ".zip";
                                    Utils.zip(instance.getSavesDirectory(), new File(App.settings.getBackupsDir(),
                                            filename));
                                    dialog.dispose();
                                    App.TOASTER.pop(App.settings.getLocalizedString("backup.backupcomplete",
                                            " " + filename));
                                }
                            };
                            backupThread.start();
                            dialog.addWindowListener(new WindowAdapter() {
                                public void windowClosing(WindowEvent e) {
                                    backupThread.interrupt();
                                    dialog.dispose();
                                }
                            });
                            dialog.setVisible(true);
                        }
                    } else {
                        String[] options = {Language.INSTANCE.localize("common.ok")};
                        JOptionPane.showOptionDialog(App.settings.getParent(), Language.INSTANCE.localize("backup" +
                                        ".nosaves"), Language.INSTANCE.localize("backup.nosavestitle"),
                                JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, options, options[0]);
                    }
                }
            }
        });
        this.editButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                new EditModsDialog(instance);
            }
        });
        this.openButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                Utils.openExplorer(instance.getRootDirectory());
            }
        });
        this.cloneButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                String clonedName = JOptionPane.showInputDialog(App.settings.getParent(),
                        Language.INSTANCE.localize("instance.cloneenter"), Language.INSTANCE.localize("instance" + "" +
                                ".clonetitle"), JOptionPane.INFORMATION_MESSAGE);
                if (clonedName != null && clonedName.length() >= 1 && App.settings.getInstanceByName(clonedName) ==
                        null && App.settings.getInstanceBySafeName(clonedName.replaceAll("[^A-Za-z0-9]",
                        "")) == null && clonedName.replaceAll("[^A-Za-z0-9]", "").length() >= 1) {

                    final String newName = clonedName;
                    final ProgressDialog dialog = new ProgressDialog(App.settings.getLocalizedString("instance" + "" +
                            ".clonetitle"), 0, App.settings.getLocalizedString("instance.cloninginstance"), null);
                    dialog.addThread(new Thread() {
                        @Override
                        public void run() {
                            App.settings.cloneInstance(instance, newName);
                            dialog.close();
                            App.TOASTER.pop(App.settings.getLocalizedString("instance.clonedsuccessfully",
                                    instance.getName()));
                        }
                    });
                    dialog.start();
                } else if (clonedName == null || clonedName.equals("")) {
                    LogManager.error("Error Occured While Cloning Instance! Dialog Closed/Cancelled!");
                    JOptionPane.showMessageDialog(App.settings.getParent(), "<html><p align=\"center\">" + App
                            .settings.getLocalizedString("instance.errorclone", instance.getName() + "<br/><br/>") +
                            "</p></html>", Language.INSTANCE.localize("common.error"), JOptionPane.ERROR_MESSAGE);
                } else if (clonedName.replaceAll("[^A-Za-z0-9]", "").length() == 0) {
                    LogManager.error("Error Occured While Cloning Instance! Invalid Name!");
                    JOptionPane.showMessageDialog(App.settings.getParent(), "<html><p align=\"center\">" + App
                            .settings.getLocalizedString("instance.errorclone", instance.getName() + "<br/><br/>") +
                            "</p></html>", Language.INSTANCE.localize("common.error"), JOptionPane.ERROR_MESSAGE);
                } else {
                    LogManager.error("Error Occured While Cloning Instance! Instance With That Name Already Exists!");
                    JOptionPane.showMessageDialog(App.settings.getParent(), "<html><p align=\"center\">" + App
                            .settings.getLocalizedString("instance.errorclone", instance.getName() + "<br/><br/>") +
                            "</p></html>", Language.INSTANCE.localize("common.error"), JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        this.deleteButton.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e){
                int response = JOptionPane.showConfirmDialog(App.settings.getParent(),
                        Language.INSTANCE.localize("instance.deletesure"), Language.INSTANCE.localize("instance" + "" +
                                ".deleteinstance"), JOptionPane.YES_NO_OPTION);
                if (response == JOptionPane.YES_OPTION) {
                    final ProgressDialog dialog = new ProgressDialog(App.settings.getLocalizedString("instance" + "" +
                            ".deletetitle"), 0, App.settings.getLocalizedString("instance.deletinginstance"), null);
                    dialog.addThread(new Thread() {
                        @Override
                        public void run() {
                            App.settings.removeInstance(instance);
                            dialog.close();
                            App.TOASTER.pop(App.settings.getLocalizedString("instance.deletedsuccessfully",
                                    instance.getName()));
                        }
                    });
                    dialog.start();
                }
            }
        });
    }

    private void addMouseListeners(){
        this.image.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseClicked(MouseEvent e){
                if(e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() >= 2){
                    if(instance.hasUpdate() && !instance.hasUpdateBeenIgnored(instance.getLatestVersion()) &&
                            !instance.isDev()){
                        String[] options = {
                                Language.INSTANCE.localize("common.yes"),
                                Language.INSTANCE.localize("common.no"),
                                Language.INSTANCE.localize("instance" + "" +
                                        ".dontremindmeagain")
                        };
                        int ret = JOptionPane.showOptionDialog(App.settings.getParent(),
                                "<html><p align=\"center\">" + Language.INSTANCE.localize("instance.updatenow",
                                        "<br/><br/>") + "</p></html>", Language.INSTANCE.localize("instance" + "" +
                                        ".updateavailable"), JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE,
                                null, options, options[0]);
                        if(ret == 0){
                            if(App.settings.getAccount() == null){
                                String[] optionss = {Language.INSTANCE.localize("common.ok")};
                                JOptionPane.showOptionDialog(App.settings.getParent(),
                                        App.settings.getLocalizedString("instance.cantupdate"),
                                        App.settings.getLocalizedString("instance.noaccountselected"),
                                        JOptionPane.DEFAULT_OPTION, JOptionPane.ERROR_MESSAGE, null, optionss,
                                        optionss[0]);
                            } else{
                                new InstanceInstallerDialog(instance, true, false);
                            }
                        } else if(ret == 1 || ret == JOptionPane.CLOSED_OPTION){
                            if(!App.settings.isMinecraftLaunched()){
                                if(instance.launch()){
                                    App.settings.setMinecraftLaunched(true);
                                }
                            }
                        } else if(ret == 2){
                            instance.ignoreUpdate();
                            if(!App.settings.isMinecraftLaunched()){
                                if(instance.launch()){
                                    App.settings.setMinecraftLaunched(true);
                                }
                            }
                        }
                    } else{
                        if(!App.settings.isMinecraftLaunched()){
                            if(instance.launch()){
                                App.settings.setMinecraftLaunched(true);
                            }
                        }
                    }
                } else if(e.getButton() == MouseEvent.BUTTON3){
                    JPopupMenu rightClickMenu = new JPopupMenu();
                    JMenuItem changeImageItem = new JMenuItem(Language.INSTANCE.localize("instance.changeimage"));
                    rightClickMenu.add(changeImageItem);
                    rightClickMenu.show(image, e.getX(), e.getY());
                    changeImageItem.addActionListener(new ActionListener(){
                        @Override
                        public void actionPerformed(ActionEvent e){
                            JFileChooser chooser = new JFileChooser();
                            chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                            chooser.setAcceptAllFileFilterUsed(false);
                            chooser.setFileFilter(new FileNameExtensionFilter("PNG Files", "png"));
                            int ret = chooser.showOpenDialog(App.settings.getParent());
                            if(ret == JFileChooser.APPROVE_OPTION){
                                File img = chooser.getSelectedFile();
                                if(img.getAbsolutePath().endsWith(".png")){
                                    try{
                                        Utils.safeCopy(img, new File(instance.getRootDirectory(), "instance.png"));
                                        image.setImage(instance.getImage().getImage());
                                        instance.save();
                                    } catch(IOException e1){
                                        e1.printStackTrace(System.err);
                                    }
                                }
                            }
                        }
                    });
                }
            }
        });
    }

    @Override
    public void onRelocalization() {
        this.playButton.setText(Language.INSTANCE.localize("common.play"));
        this.reinstallButton.setText(Language.INSTANCE.localize("common.reinstall"));
        this.renameButton.setText(Language.INSTANCE.localize("instance.rename"));
        this.backupButton.setText(Language.INSTANCE.localize("common.backup"));
        this.cloneButton.setText(Language.INSTANCE.localize("instance.clone"));
        this.deleteButton.setText(Language.INSTANCE.localize("common.delete"));
        this.editButton.setText(Language.INSTANCE.localize("common.editmods"));
        this.openButton.setText(Language.INSTANCE.localize("common.openfolder"));
    }
}
