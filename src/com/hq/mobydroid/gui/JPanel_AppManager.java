/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.hq.mobydroid.gui;

import com.hq.mobydroid.device.ApkgManager;
import com.hq.apktool.Apkg;
import com.hq.materialdesign.MaterialColor;
import com.hq.materialdesign.MaterialIcons;
import com.hq.mobydroid.MobyDroid;
import com.hq.mobydroid.MobydroidStatic;
import com.hq.mobydroid.Settings;
import java.awt.Color;
import java.awt.Component;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.DefaultRowSorter;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import com.hq.mobydroid.device.TaskListener;
import java.awt.Cursor;
import java.io.File;
import java.util.stream.Collectors;
import javax.swing.JFileChooser;

/**
 *
 * @author Bilux (i.bilux@gmail.com)
 */
public class JPanel_AppManager extends javax.swing.JPanel {

    // ************************ My variable ************************
    private final PackageTableModel packageTableModel = new PackageTableModel();
    private final String[] packageTableColumnNames = {"", "App", "Version", "Size", "Location", "Install Time"};
    // *************************************************************
    private final ListSelectionListener listSelectionListener;

    /**
     * Creates new form JPanel_ManageApps
     *
     */
    public JPanel_AppManager() {
        // initialize components
        initComponents();

        // table dimension
        jTable_Apps.setRowHeight(GuiUtils.APK_ICON_HEIGTH + 6);
        setColumnWidth(0, 32, 32);
        setColumnWidth(1, 256, -1);
        setColumnWidth(2, 64, 128);
        setColumnWidth(3, 64, 128);
        setColumnWidth(4, 64, 128);
        setColumnWidth(5, 96, 128);

        // set Table Row Sorter
        TableRowSorter tableRowSorter = new PackageTableRowSorter(jTable_Apps.getModel());
        jTable_Apps.setRowSorter(tableRowSorter);
        tableRowSorter.setComparator(2, (Comparator<Long>) (o1, o2) -> o1.compareTo(o2));

        // set table header for  0nd column
        jTable_Apps.getColumnModel().getColumn(jTable_Apps.convertColumnIndexToView(0)).setHeaderRenderer(new JCheckBoxTableHeaderCellRenderer());

        // set cell render 1th column
        jTable_Apps.getColumnModel().getColumn(jTable_Apps.convertColumnIndexToView(1)).setCellRenderer(new ApkLablelCellRenderer());

        // right align 3rd column
        DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
        renderer.setHorizontalAlignment(JLabel.RIGHT);
        jTable_Apps.getColumnModel().getColumn(jTable_Apps.convertColumnIndexToView(3)).setCellRenderer(renderer);

        // center align 4th & 5th column
        renderer.setHorizontalAlignment(JLabel.CENTER);
        jTable_Apps.getColumnModel().getColumn(jTable_Apps.convertColumnIndexToView(4)).setCellRenderer(renderer);
        jTable_Apps.getColumnModel().getColumn(jTable_Apps.convertColumnIndexToView(5)).setCellRenderer(renderer);

        // header click event
        jTable_Apps.getTableHeader().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                int column = jTable_Apps.convertColumnIndexToModel(jTable_Apps.getColumnModel().getColumnIndexAtX(mouseEvent.getX()));
                if (mouseEvent.getClickCount() == 1 && column != -1) {
                    packageTableModel.headerClicked(column);
                }
            }
        });

        // set tab action to change focus component outside jtable
        jTable_Apps.getActionMap().put(jTable_Apps.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).get(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0)), new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                KeyboardFocusManager.getCurrentKeyboardFocusManager().focusNextComponent();
            }
        });

        // KeyBinding
        jTable_Apps.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "none");
        jTable_Apps.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, 0), "none");
        jTable_Apps.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_END, 0), "none");

        // set table selection listner
        listSelectionListener = (ListSelectionEvent lse) -> {
            setPackageDetails(packageTableModel.getPackage(jTable_Apps.getSelectionModel().getLeadSelectionIndex()));
        };
        jTable_Apps.getSelectionModel().addListSelectionListener(listSelectionListener);

        // hide for non expert
        if (!Boolean.valueOf(Settings.get("Express_Settings"))) {
            materialButtonH_Backup.setVisible(false);
            materialButtonH_Restore.setVisible(false);
            jTable_Apps.removeColumn(jTable_Apps.getColumnModel().getColumn(5));
        }

        //jTable_Apps.removeColumn(jTable_Apps.getColumnModel().getColumn(1));
        //jTable_Apps.addColumn(jTable_Apps.getColumnModel().getColumn(1));
        //setColumnWidth(2, 0, 0);
        //jTable_Apps.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_TAB, 0), "none");
        /*        
        //Number column1 = jTable_Apps.getColumnModel().getColumnIndex(1);
        Number column2 = jTable_Apps.convertColumnIndexToModel(2);
        jTable_Apps.getTableHeader().putClientProperty("JTableHeader.selectedColumn", column2);
        jTable_Apps.getTableHeader().putClientProperty("JTableHeader.sortDirection", "ascending");
        jTable_Apps.getTableHeader().resizeAndRepaint();
         */
    }

    /**
     * Handle buttons events.
     */
    private void uninstallHandle() {
        uninstallPackages();
    }

    private void backupHandle() {
        backupPackages();
    }

    private void RestoreHandle() {
        restorePackages();
    }

    private void pullHandle() {
        pullPackages();
    }

    private void RefreshHandle() {
        updatePackagesList();
    }

    private boolean isPackageMarked() {
        int marked = (int) packageTableModel.getPackages().stream().filter((pkg) -> (pkg.isMarked())).count();
        if (marked == 0) {
            JOptionPane.showMessageDialog(this, "Please select packages for operation.", "No packages selected", JOptionPane.OK_OPTION, ResourceLoader.MaterialIcons_WARNING);
            return false;
        }
        return true;
    }

    private void enableUI() {
        // enable buttons
        materialButtonH_Backup.setEnabled(true);
        materialButtonH_PullApk.setEnabled(true);
        materialButtonH_Refresh.setEnabled(true);
        materialButtonH_Restore.setEnabled(true);
        materialButtonH_Uninstall.setEnabled(true);
        // add back the ListSelectionListener
        jTable_Apps.getSelectionModel().addListSelectionListener(listSelectionListener);
        // enable jTable
        jTable_Apps.setVisible(true);
        // turn off the wait cursor
        setCursor(null);
    }

    private void disableUI() {
        // turn on the wait cursor
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        // disable jTable
        jTable_Apps.setVisible(false);
        // remove ListSelectionListener
        jTable_Apps.getSelectionModel().removeListSelectionListener(listSelectionListener);
        // disable buttons
        materialButtonH_Backup.setEnabled(false);
        materialButtonH_PullApk.setEnabled(false);
        materialButtonH_Refresh.setEnabled(false);
        materialButtonH_Restore.setEnabled(false);
        materialButtonH_Uninstall.setEnabled(false);
    }

    /**
     * Update packages list.
     */
    public void updatePackagesList() {
        // the old packages to check marked packages
        //List<ApkgManager> oldPackages = packageTableModel.getPackages();

        // run the packages list task
        MobyDroid.getDevice().runPackagesListTask(new TaskListener<Apkg>() {
            @Override
            public void onStart() {
                // disable UI
                disableUI();

                // clear old packages
                packageTableModel.removeAll();
            }

            @Override
            public void onProcess(List<Apkg> list) {
                list.forEach((pkg) -> {
                    packageTableModel.addPackage(new ApkgManager(pkg, false));
                });
            }

            @Override
            public void onDone() {
                // enable UI
                enableUI();
            }
        });
        /*
        SwingWorker<Void, Apkg> worker = new SwingWorker<Void, Apkg>() {
            @Override
            public Void doInBackground() {
                // disable jTable
                jTable_Apps.setEnabled(false);
                // remove ListSelectionListener
                jTable_Apps.getSelectionModel().removeListSelectionListener(listSelectionListener);
                // get packages list
                List<Apkg> pkgs;
                try {
                    pkgs = MobyDroid.getDevice().newPackagesListTask();
                } catch (IOException | JadbException ex) {
                    return null;
                }
                // start publishing
                int progress;
                int counter = 0;
                for (Apkg pkg : pkgs) {
                    // publish pkg for processing
                    publish(pkg);
                    // set progress
                    progress = (counter++) * 100 / pkgs.size();
                    setProgress(progress);
                    MobyDroid.setProgressBarValue(progress);
                }
                return null;
            }

            @Override
            protected void process(List<Apkg> pkgs) {
                pkgs.forEach((pkg) -> {
                    packageTableModel.addPackage(new ApkgManager(pkg, false));
                });
            }

            @Override
            protected void done() {
                // set progress
                setProgress(100);
                MobyDroid.setProgressBarValue(100);
                // add back the ListSelectionListener
                jTable_Apps.getSelectionModel().addListSelectionListener(listSelectionListener);
                // enable jTable
                jTable_Apps.setEnabled(true);
            }
        };
        worker.execute();
         */
    }

    /**
     * Uninstall packages.
     */
    private void uninstallPackages() {
        // check if any packages are marked
        if (!isPackageMarked()) {
            return;
        }
        // confirm uninstalling
        if (JOptionPane.showConfirmDialog(this, "Are you sure?", "Uninstall packages", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, ResourceLoader.MaterialIcons_DELETE_FOREVER) != JOptionPane.YES_OPTION) {
            return;
        }

        // disable UI
        disableUI();

        // start uninstall tasks
        packageTableModel.getPackages().stream().filter((pkg) -> (pkg.isMarked())).forEach((pkg) -> {
            MobyDroid.getDevice().runPackageUninstallTask(pkg);
        });

        // enable UI
        enableUI();
    }

    /**
     * Pull apk file for packages.
     */
    private void pullPackages() {
        // check if any packages are marked
        if (!isPackageMarked()) {
            return;
        }
        // disable UI
        disableUI();

        // start pull tasks
        packageTableModel.getPackages().stream().filter((pkg) -> (pkg.isMarked())).forEach((pkg) -> {
            MobyDroid.getDevice().runPackagePullTask(pkg);
        });

        // enable UI
        enableUI();

    }

    /**
     * Backup packages.
     */
    private void backupPackages() {
        /*
        // check if any packages are marked
        if (!isPackageMarked()) {
            return;
        }
        // disable UI
        disableUI();

        // start backup tasks
        MobyDroid.getDevice().runPackageBackupTask(packageTableModel.getPackages().stream().filter((pkg) -> (pkg.isMarked())).collect(Collectors.toList()));
        //MobyDroid.getDevice().runPackageBackupTask(packageTableModel.getPackages().stream().filter((pkg) -> (pkg.isMarked())));
        //packageTableModel.getPackages().stream().filter((pkg) -> (pkg.isPackageMarked())).forEach((pkg) -> {
        //        MobyDroid.getDevice().runPackageBackupTask(pkg);
        //    });

        // enable UI
        enableUI();
        */
    }

    /**
     * Restore packages.
     */
    private void restorePackages() {
        /*
        // disable UI
        disableUI();

        JFileChooser fileChooser = new JFileChooser();
        File path = new File(Settings.get("AppManager_RestorePath"));
        if (!path.exists()) {
            path = new File(MobydroidStatic.HOME_PATH);
        }
        fileChooser.setCurrentDirectory(path);
        fileChooser.setMultiSelectionEnabled(true);
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File[] files = fileChooser.getSelectedFiles();
            for (File file : files) {
                MobyDroid.getDevice().runPackageRestoreTask(file.getPath());
            }
            // save last directory to settings ..
            Settings.set("AppManager_RestorePath", fileChooser.getSelectedFile().getParent());
            Settings.save();
        }
        // enable UI
        enableUI();
        */
    }

    /**
     * Update the package details view with the details of this package.
     */
    private void setPackageDetails(ApkgManager pkgManager) {
        jLabel_AppIcon.setIcon(pkgManager.getIcon());
        jLabel_AppLabel.setText(pkgManager.getLabel());
        jLabel_AppPackage.setText(pkgManager.getPackage());
        jLabel_AppVersion.setText("Version: " + pkgManager.getVersion());
        jLabel_AppSize.setText("Size: " + GuiUtils.getFormatedSize(pkgManager.getSize()));
        jLabel_Install.setText("Marked: " + (pkgManager.isMarked() ? "Yes" : "No"));
    }

    /**
     *
     */
    private void setColumnWidth(int column, int minWidth, int maxWidth) {
        TableColumn tableColumn = jTable_Apps.getColumnModel().getColumn(jTable_Apps.convertColumnIndexToView(column));
        if (minWidth >= 0 && maxWidth >= 0) {
            tableColumn.setPreferredWidth((minWidth + maxWidth) / 2);
        }
        if (minWidth >= 0) {
            tableColumn.setMinWidth(minWidth);
        }
        if (maxWidth >= 0) {
            tableColumn.setMaxWidth(maxWidth);
        }
    }

    ///////////////////////////////////////////////
    // *************************************************************
    class PopUpDemo extends JPopupMenu {

        /*
        JMenuItem refreshMenuItem = new JMenuItem("Refresh",refreshIcon);
        JMenuItem downloadMenuItem = new JMenuItem("Download",downloadIcon);
        JMenuItem uploadMenuItem = new JMenuItem("Upload",uploadIcon);
        JMenuItem runMenuItem = new JMenuItem("Run",runIcon);
        JMenuItem renameMenuItem = new JMenuItem("Rename",renameIcon);
        JMenuItem deleteMenuItem = new JMenuItem("Delete",deleteIcon);
        JMenuItem mkdirMenuItem = new JMenuItem("Creat folder",folderIcon);
        JMenuItem openAgentFolderMenuItem = new JMenuItem("Open user folder",downloadsIcon);*/
        public PopUpDemo() {
            /*
            refreshMenuItem.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent evt) {refreshCMD(evt);}});
            downloadMenuItem.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent evt) {downloadCMD(evt);}});
            uploadMenuItem.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent evt) {uploadCMD(evt);}});
            runMenuItem.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent evt) {runCMD(evt);}});
            renameMenuItem.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent evt) {renameCMD(evt);}});
            deleteMenuItem.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent evt) {deleteCMD(evt);}});
            mkdirMenuItem.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent evt) {mkdirCMD(evt);}});
            openAgentFolderMenuItem.addActionListener(new ActionListener() {@Override public void actionPerformed(ActionEvent evt) {openAgentFolderCMD(evt);}});
            
            add(refreshMenuItem);
            add(downloadMenuItem);
            add(uploadMenuItem);
            add(runMenuItem);
            add(renameMenuItem);
            add(deleteMenuItem);
            add(mkdirMenuItem);
            add(openAgentFolderMenuItem);
             */
        }
    }

    // ************************************************************* //
    // ************************************************************* //
    private class PackageTableModel extends AbstractTableModel {

        private final List<ApkgManager> packages;

        PackageTableModel() {
            packages = new ArrayList<>();
        }

        @Override
        public Class<?> getColumnClass(int column) {
            switch (column) {
                case 0:
                    return Boolean.class;
                case 1:
                    return JPanel.class;
                case 2:
                    return String.class;
                case 3:
                    return String.class;
                case 4:
                    return String.class;
                case 5:
                    return String.class;
                default:
                    return null;
            }
        }

        @Override
        public boolean isCellEditable(int row, int column) {
            switch (column) {
                case 0:
                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void setValueAt(Object value, int row, int column) {
            ApkgManager pkgManager = packages.get(row);
            switch (column) {
                case 0:
                    pkgManager.setMark(!pkgManager.isMarked());
                    fireTableCellUpdated(row, column);
                    setPackageDetails(pkgManager);
                    break;
            }
        }

        @Override
        public int getColumnCount() {
            return packageTableColumnNames.length;
        }

        @Override
        public int getRowCount() {
            return packages.size();
        }

        @Override
        public String getColumnName(int column) {
            return packageTableColumnNames[column];
        }

        @Override
        public Object getValueAt(int row, int column) {
            ApkgManager pkgManager = packages.get(row);
            switch (column) {
                case 0:
                    return pkgManager.isMarked();
                case 1:
                    return pkgManager;
                case 2:
                    return pkgManager.getVersion();
                case 3:
                    return GuiUtils.getFormatedSize(pkgManager.getSize());
                case 4:
                    return ("SD");
                case 5:
                    return (new SimpleDateFormat("yyyy-MM-dd")).format(new Date(pkgManager.getInstallTime()));
                default:
                    return null;
            }
        }

        public Object getRawValueAt(int row, int column) {
            ApkgManager pkgManager = packages.get(row);
            switch (column) {
                case 0:
                    return pkgManager.isMarked();
                case 1:
                    return pkgManager.getLabel();
                case 2:
                    return pkgManager.getVersion();
                case 3:
                    return pkgManager.getSize();
                case 4:
                    return ("SD");
                case 5:
                    return (new SimpleDateFormat("yyyy-MM-dd")).format(new Date(pkgManager.getInstallTime()));
                default:
                    return null;
            }
        }

        public List<ApkgManager> getPackages() {
            return packages;
        }

        public boolean contains(ApkgManager pkg) {
            return packages.contains(pkg);
        }

        public ApkgManager getPackage(int row) {
            return packages.get(row);
        }

        public void addPackage(ApkgManager pkg) {
            // check if already exist
            for (ApkgManager pkgManager : packages) {
                if (pkgManager.equals(pkg)) {
                    return;
                }
            }
            // add new package
            packages.add(pkg);
            fireTableDataChanged();
        }

        public void removePackage(int row) {
            // remove package
            packages.remove(row);
            fireTableDataChanged();
        }

        public void removeAll() {
            // remove all packages
            packages.clear();
            fireTableDataChanged();
        }

        private void headerClicked(int column) {
            if (column == 0) {
                JCheckBox jCheckBox = (JCheckBox) jTable_Apps.getTableHeader().getColumnModel().getColumn(jTable_Apps.convertColumnIndexToView(column)).getHeaderRenderer().getTableCellRendererComponent(null, null, false, false, 0, 0);
                jCheckBox.setSelected(!jCheckBox.isSelected());
                packages.forEach((pkgManager) -> {
                    pkgManager.setMark(jCheckBox.isSelected());
                });
                // Forces the header to resize and repaint itself
                jTable_Apps.getTableHeader().resizeAndRepaint();
                // fire
                fireTableDataChanged();
            }
        }
    }

    // ************************************************************* //
    // ************************************************************* //
    private class PackageTableRowSorter<M extends TableModel> extends TableRowSorter<M> {

        public PackageTableRowSorter(M model) {
            super(model);
        }

        @Override
        public void modelStructureChanged() {
            // deletes comparators, so we must set again
            super.modelStructureChanged();
        }

        @Override
        public void setModel(M model) {
            // also calls setModelWrapper method
            super.setModel(model);
            // calls modelStructureChanged method
            setModelWrapper(new TableRowSorterModelWrapper(getModelWrapper()));
        }

        /**
         *
         */
        private class TableRowSorterModelWrapper extends DefaultRowSorter.ModelWrapper {

            private final DefaultRowSorter.ModelWrapper modelWrapperImplementation;

            public TableRowSorterModelWrapper(DefaultRowSorter.ModelWrapper modelWrapperImplementation) {
                this.modelWrapperImplementation = modelWrapperImplementation;
            }

            @Override
            public Object getModel() {
                return modelWrapperImplementation.getModel();
            }

            @Override
            public int getColumnCount() {
                return modelWrapperImplementation.getColumnCount();
            }

            @Override
            public int getRowCount() {
                return modelWrapperImplementation.getRowCount();
            }

            @Override
            public Object getIdentifier(int row) {
                return modelWrapperImplementation.getIdentifier(row);
            }

            @Override
            public Object getValueAt(int row, int column) {
                return packageTableModel.getRawValueAt(row, column);
            }
        }
    }

    // ************************************************************* //
    // ************************************************************* //
    private class ApkLablelCellRenderer implements TableCellRenderer {

        private final JPanel jpanel;
        private final JLabel jLabel_Label;
        private final JLabel jLabel_Package;
        private final JLabel jLabel_Icon;

        public ApkLablelCellRenderer() {
            jpanel = new JPanel();
            jLabel_Label = new JLabel();
            jLabel_Package = new JLabel();
            jLabel_Icon = new javax.swing.JLabel();

            jLabel_Label.setFont(new java.awt.Font("Dialog", 1, 12));
            jLabel_Label.setForeground(Color.BLACK);

            jLabel_Package.setFont(new java.awt.Font("Dialog", 1, 10));
            jLabel_Package.setForeground(MaterialColor.GREY_700);

            jLabel_Icon.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);

            javax.swing.GroupLayout layout = new javax.swing.GroupLayout(jpanel);
            jpanel.setLayout(layout);

            layout.setHorizontalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                    .addComponent(jLabel_Icon, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addGap(0, 0, 0)
                                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addComponent(jLabel_Package, javax.swing.GroupLayout.DEFAULT_SIZE, 132, Short.MAX_VALUE)
                                            .addComponent(jLabel_Label, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
            );
            layout.setVerticalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel_Icon, javax.swing.GroupLayout.PREFERRED_SIZE, 36, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                    .addComponent(jLabel_Label, javax.swing.GroupLayout.PREFERRED_SIZE, 21, Short.MAX_VALUE)
                                    .addGap(0, 0, 0)
                                    .addComponent(jLabel_Package, javax.swing.GroupLayout.PREFERRED_SIZE, 15, Short.MAX_VALUE))
            );
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            Apkg pkg = (Apkg) value;
            jLabel_Label.setText(pkg.getLabel());
            jLabel_Package.setText(pkg.getPackage());
            jLabel_Icon.setIcon(pkg.getIcon());

            if (hasFocus) {
                jpanel.setBorder(javax.swing.BorderFactory.createLineBorder(MaterialColor.BLUE_400));
            } else {
                jpanel.setBorder(javax.swing.BorderFactory.createEmptyBorder());
            }

            return jpanel;
        }
    }

    private class JCheckBoxTableHeaderCellRenderer implements TableCellRenderer {

        private final JCheckBox jCheckBox;

        public JCheckBoxTableHeaderCellRenderer() {
            jCheckBox = new JCheckBox();
            jCheckBox.setFont(UIManager.getFont("TableHeader.font"));
            jCheckBox.setBorder(UIManager.getBorder("TableHeader.cellBorder"));
            jCheckBox.setBackground(UIManager.getColor("TableHeader.background"));
            jCheckBox.setForeground(UIManager.getColor("TableHeader.foreground"));
            jCheckBox.setHorizontalAlignment(SwingConstants.CENTER);
            jCheckBox.setBorderPainted(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            jCheckBox.setText((String) value);
            return jCheckBox;
        }
    }
    // ************************************************************* //
    // ************************************************************* //

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jTableScrollPane_Apps = new javax.swing.JScrollPane();
        jTable_Apps = new javax.swing.JTable(){
            @Override
            public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
                Component component = super.prepareRenderer(renderer, row, column);
                if (isRowSelected(row)) {
                    component.setBackground(MaterialColor.BLUE_100);
                }else{
                    component.setBackground(row % 2 == 0 ? Color.white : MaterialColor.GREY_50);
                }
                return component;
            }

            @Override
            public boolean getScrollableTracksViewportWidth() {
                return getPreferredSize().width < getParent().getWidth();
            }
        };
        materialButtonH_Uninstall = new com.hq.mobydroid.gui.MaterialButtonV();
        materialButtonH_Backup = new com.hq.mobydroid.gui.MaterialButtonV();
        materialButtonH_PullApk = new com.hq.mobydroid.gui.MaterialButtonV();
        materialButtonH_Restore = new com.hq.mobydroid.gui.MaterialButtonV();
        materialButtonH_Refresh = new com.hq.mobydroid.gui.MaterialButtonV();
        jPanel_Package = new javax.swing.JPanel();
        jLabel_AppIcon = new javax.swing.JLabel();
        jLabel_AppLabel = new javax.swing.JLabel();
        jLabel_AppPackage = new javax.swing.JLabel();
        jLabel_AppVersion = new javax.swing.JLabel();
        jLabel_AppSize = new javax.swing.JLabel();
        jLabel_Install = new javax.swing.JLabel();
        jLabel_OnSDCard = new javax.swing.JLabel();
        jLabel_Reinstall = new javax.swing.JLabel();
        jLabel_Downgrade = new javax.swing.JLabel();

        setBackground(new java.awt.Color(250, 250, 250));
        setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Install New Apps : ", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 1, 11))); // NOI18N

        jTableScrollPane_Apps.setBackground(new java.awt.Color(250, 250, 250));
        jTableScrollPane_Apps.setComponentPopupMenu(new PopUpDemo());

        jTable_Apps.setBackground(new java.awt.Color(250, 250, 250));
        jTable_Apps.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        jTable_Apps.setFont(new java.awt.Font("Dialog", 1, 12)); // NOI18N
        jTable_Apps.setModel(packageTableModel);
        jTable_Apps.setAutoResizeMode(javax.swing.JTable.AUTO_RESIZE_OFF);
        jTable_Apps.setComponentPopupMenu(new PopUpDemo());
        jTable_Apps.setShowHorizontalLines(false);
        jTable_Apps.setShowVerticalLines(false);
        jTable_Apps.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusGained(java.awt.event.FocusEvent evt) {
                jTable_AppsFocusGained(evt);
            }
        });
        jTable_Apps.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                jTable_AppsKeyPressed(evt);
            }
        });
        jTableScrollPane_Apps.setViewportView(jTable_Apps);

        materialButtonH_Uninstall.setAction(new MaterialButtonAction() {
            @Override
            public void Action() {
                uninstallHandle();
            }
        });
        materialButtonH_Uninstall.setFocusable(true);
        materialButtonH_Uninstall.setIcon(MaterialIcons.DELETE_FOREVER);
        materialButtonH_Uninstall.setText("Uninstall");

        materialButtonH_Backup.setAction(new MaterialButtonAction() {
            @Override
            public void Action() {
                backupHandle();
            }
        });
        materialButtonH_Backup.setFocusable(true);
        materialButtonH_Backup.setIcon(MaterialIcons.UNARCHIVE);
        materialButtonH_Backup.setText("Backup");

        materialButtonH_PullApk.setAction(new MaterialButtonAction() {
            @Override
            public void Action() {
                pullHandle();
            }
        });
        materialButtonH_PullApk.setFocusable(true);
        materialButtonH_PullApk.setIcon(MaterialIcons.SAVE);
        materialButtonH_PullApk.setText("Pull Apk");

        materialButtonH_Restore.setAction(new MaterialButtonAction() {
            @Override
            public void Action() {
                RestoreHandle();
            }
        });
        materialButtonH_Restore.setFocusable(true);
        materialButtonH_Restore.setIcon(MaterialIcons.ARCHIVE);
        materialButtonH_Restore.setText("Restore");

        materialButtonH_Refresh.setAction(new MaterialButtonAction() {
            @Override
            public void Action() {
                RefreshHandle();
            }
        });
        materialButtonH_Refresh.setFocusable(true);
        materialButtonH_Refresh.setIcon(MaterialIcons.REFRESH);
        materialButtonH_Refresh.setText("Refresh");

        jPanel_Package.setBackground(new java.awt.Color(250, 250, 250));
        jPanel_Package.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jPanel_Package.setFocusable(false);
        jPanel_Package.setFont(new java.awt.Font("Dialog", 0, 11)); // NOI18N
        jPanel_Package.setMaximumSize(new java.awt.Dimension(0, 0));

        jLabel_AppIcon.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel_AppIcon.setFocusable(false);
        jLabel_AppIcon.setOpaque(true);

        jLabel_AppLabel.setFocusable(false);
        jLabel_AppLabel.setOpaque(true);

        jLabel_AppPackage.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jLabel_AppPackage.setFocusable(false);
        jLabel_AppPackage.setOpaque(true);

        jLabel_AppVersion.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jLabel_AppVersion.setFocusable(false);
        jLabel_AppVersion.setOpaque(true);

        jLabel_AppSize.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jLabel_AppSize.setFocusable(false);
        jLabel_AppSize.setOpaque(true);

        jLabel_Install.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jLabel_Install.setFocusable(false);
        jLabel_Install.setOpaque(true);

        jLabel_OnSDCard.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jLabel_OnSDCard.setFocusable(false);
        jLabel_OnSDCard.setOpaque(true);

        jLabel_Reinstall.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jLabel_Reinstall.setFocusable(false);
        jLabel_Reinstall.setOpaque(true);

        jLabel_Downgrade.setFont(new java.awt.Font("Dialog", 0, 12)); // NOI18N
        jLabel_Downgrade.setFocusable(false);
        jLabel_Downgrade.setOpaque(true);

        javax.swing.GroupLayout jPanel_PackageLayout = new javax.swing.GroupLayout(jPanel_Package);
        jPanel_Package.setLayout(jPanel_PackageLayout);
        jPanel_PackageLayout.setHorizontalGroup(
            jPanel_PackageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel_PackageLayout.createSequentialGroup()
                .addGap(1, 1, 1)
                .addGroup(jPanel_PackageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel_AppVersion, javax.swing.GroupLayout.PREFERRED_SIZE, 252, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel_PackageLayout.createSequentialGroup()
                        .addComponent(jLabel_AppIcon, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel_PackageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel_AppPackage, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel_AppLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 210, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(jLabel_AppSize, javax.swing.GroupLayout.PREFERRED_SIZE, 252, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel_OnSDCard, javax.swing.GroupLayout.PREFERRED_SIZE, 252, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel_Reinstall, javax.swing.GroupLayout.PREFERRED_SIZE, 252, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel_Downgrade, javax.swing.GroupLayout.PREFERRED_SIZE, 252, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel_Install, javax.swing.GroupLayout.PREFERRED_SIZE, 252, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(1, 1, 1))
        );
        jPanel_PackageLayout.setVerticalGroup(
            jPanel_PackageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel_PackageLayout.createSequentialGroup()
                .addGroup(jPanel_PackageLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel_AppIcon, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel_PackageLayout.createSequentialGroup()
                        .addComponent(jLabel_AppLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jLabel_AppPackage, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel_AppVersion, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel_AppSize, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel_Install, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel_OnSDCard, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel_Reinstall, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jLabel_Downgrade, javax.swing.GroupLayout.PREFERRED_SIZE, 15, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(materialButtonH_Refresh, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(materialButtonH_Uninstall, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(materialButtonH_PullApk, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(materialButtonH_Backup, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(materialButtonH_Restore, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jTableScrollPane_Apps, javax.swing.GroupLayout.DEFAULT_SIZE, 531, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel_Package, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(materialButtonH_Uninstall, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(materialButtonH_Refresh, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(materialButtonH_PullApk, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(materialButtonH_Backup, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(materialButtonH_Restore, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel_Package, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jTableScrollPane_Apps, javax.swing.GroupLayout.DEFAULT_SIZE, 213, Short.MAX_VALUE))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jTable_AppsKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_jTable_AppsKeyPressed
        switch (evt.getKeyCode()) {
            case KeyEvent.VK_TAB:
                //KeyboardFocusManager.getCurrentKeyboardFocusManager().focusNextComponent();
                break;
        }
        /*
        int input = evt.getKeyCode();
        if(input==KeyEvent.VK_ENTER){
            if(currentFile.type.equalsIgnoreCase("desktop") || currentFile.type.equalsIgnoreCase("computer") || currentFile.type.equalsIgnoreCase("hdd") || currentFile.type.equalsIgnoreCase("fdd") || currentFile.type.equalsIgnoreCase("cd") || currentFile.type.equalsIgnoreCase("home") || currentFile.type.equalsIgnoreCase("dir")){
                getChildren();
            }
        }else if(input==KeyEvent.VK_BACK_SPACE){
            MyFile tmpFile =((PackageTableModel)browserTable.getModel()).getFile("..");
            if(tmpFile==null){
                //tmpFile = new MyFile("..","dir","");
                //currentFile = tmpFile;
                ///getChildren();
                getChildren(computerNode);
            }else{
                currentFile = tmpFile;
                setFileDetails(currentFile);
                getChildren();
            }
        }else if(input==KeyEvent.VK_HOME){
            browserTable.changeSelection(0, 0, false, false);
        }else if(input==KeyEvent.VK_END){
            //browserTable.sets
            browserTable.changeSelection(browserTable.getRowCount() - 1, 0, false, false);
        }
         */
 /*int startRow = jTable_Apps.getSelectedRow();
        if (startRow < 0) {
            startRow = 0;
        } else {
            startRow++;
        }
        for (int row = startRow; row < jTable_Apps.getRowCount(); row++) {
            if (((String) jTable_Apps.getValueAt(row, 1)).toLowerCase().startsWith("" + Character.toLowerCase(evt.getKeyChar()))) {
                jTable_Apps.changeSelection(row, 0, false, false);
                return;
            }
        }
        for (int row = 0; row < jTable_Apps.getRowCount(); row++) {
            if (((String) jTable_Apps.getValueAt(row, 1)).toLowerCase().startsWith("" + Character.toLowerCase(evt.getKeyChar()))) {
                jTable_Apps.changeSelection(row, 0, false, false);
                return;
            }
        }*/
    }//GEN-LAST:event_jTable_AppsKeyPressed

    private void jTable_AppsFocusGained(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_jTable_AppsFocusGained
        // select first row if Selection Model is Empty
        if (jTable_Apps.getSelectionModel().isSelectionEmpty() && jTable_Apps.getModel().getRowCount() > 0) {
            jTable_Apps.setRowSelectionInterval(0, 0);
        }
    }//GEN-LAST:event_jTable_AppsFocusGained


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel jLabel_AppIcon;
    private javax.swing.JLabel jLabel_AppLabel;
    private javax.swing.JLabel jLabel_AppPackage;
    private javax.swing.JLabel jLabel_AppSize;
    private javax.swing.JLabel jLabel_AppVersion;
    private javax.swing.JLabel jLabel_Downgrade;
    private javax.swing.JLabel jLabel_Install;
    private javax.swing.JLabel jLabel_OnSDCard;
    private javax.swing.JLabel jLabel_Reinstall;
    private javax.swing.JPanel jPanel_Package;
    private javax.swing.JScrollPane jTableScrollPane_Apps;
    private javax.swing.JTable jTable_Apps;
    private com.hq.mobydroid.gui.MaterialButtonV materialButtonH_Backup;
    private com.hq.mobydroid.gui.MaterialButtonV materialButtonH_PullApk;
    private com.hq.mobydroid.gui.MaterialButtonV materialButtonH_Refresh;
    private com.hq.mobydroid.gui.MaterialButtonV materialButtonH_Restore;
    private com.hq.mobydroid.gui.MaterialButtonV materialButtonH_Uninstall;
    // End of variables declaration//GEN-END:variables
}
