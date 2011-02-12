/*
 * The MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.yosanai.java.aws.console.panel;

import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

import org.apache.commons.lang.StringUtils;

import com.amazonaws.services.ec2.model.DescribeInstancesResult;
import com.amazonaws.services.ec2.model.Instance;
import com.amazonaws.services.ec2.model.Reservation;
import com.amazonaws.services.ec2.model.Tag;
import com.yosanai.java.aws.console.AWSAware;
import com.yosanai.java.aws.console.AWSConnectionProvider;
import com.yosanai.java.swing.editor.ObjectEditorTableModel;

/**
 * 
 * @author Saravana Perumal Shanmugam
 */
@SuppressWarnings("serial")
public class InstancesPanel extends javax.swing.JPanel implements AWSAware {

    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd:HH-mm");

    protected AWSConnectionProvider awsConnectionProvider;

    protected JFrame parentFrame;

    protected String nameTag = "name";

    protected DefaultTableModel instancesTableModel;

    protected ObjectEditorTableModel instanceTableModel;

    public class InstanceObjectWrapper {
        protected Instance instance;

        protected String name;

        /**
         * @param instance
         * @param name
         */
        private InstanceObjectWrapper(Instance instance, String name) {
            super();
            this.instance = instance;
            this.name = name;
        }

        /*
         * (non-Jsdoc)
         * 
         * @see java.lang.Object#toString()
         */
        @Override
        public String toString() {
            String ret = instance.getInstanceId();
            if (null != name) {
                ret += "(" + name + ")";
            }
            return ret;
        }
    }

    /** Creates new form InstancesPanel */
    public InstancesPanel() {
        initComponents();
        DefaultTreeModel treeModel = (DefaultTreeModel) trInstances.getModel();
        DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) treeModel.getRoot();
        rootNode.setAllowsChildren(true);
        rootNode.setUserObject("Instances");
        instancesTableModel = (DefaultTableModel) tblInstances.getModel();
        instanceTableModel = new ObjectEditorTableModel();
        instanceTableModel.setEditable(false);
        instanceTableModel.setExpandAllProperties(true);
        trInstances.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    }

    /**
     * @return the nameTag
     */
    public String getNameTag() {
        return nameTag;
    }

    /**
     * @param nameTag
     *            the nameTag to set
     */
    public void setNameTag(String nameTag) {
        this.nameTag = nameTag;
    }

    public void loadInstances() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                DefaultTreeModel treeModel = (DefaultTreeModel) trInstances.getModel();
                DefaultMutableTreeNode rootNode = (DefaultMutableTreeNode) treeModel.getRoot();
                tblInstances.clearSelection();
                trInstances.clearSelection();
                rootNode.removeAllChildren();
                treeModel.reload();
                tblInstances.setModel(instancesTableModel);
                DescribeInstancesResult result = awsConnectionProvider.getConnection().describeInstances();
                while (0 < instancesTableModel.getRowCount()) {
                    instancesTableModel.removeRow(0);
                }
                for (Reservation reservation : result.getReservations()) {
                    for (Instance instance : reservation.getInstances()) {
                        String name = null;
                        StringBuilder tags = new StringBuilder();
                        for (Tag tag : instance.getTags()) {
                            tags.append(tag.getKey());
                            tags.append("=");
                            tags.append(tag.getValue());
                            if (StringUtils.equalsIgnoreCase(nameTag, tag.getKey())) {
                                name = tag.getValue();
                            }
                        }
                        try {
                            boolean apiTermination = awsConnectionProvider.getApiTermination(instance.getInstanceId());
                            instancesTableModel.addRow(new Object[] { instance.getInstanceId(),
                                    instance.getPublicDnsName(), instance.getPublicIpAddress(),
                                    instance.getPrivateDnsName(), instance.getPrivateIpAddress(),
                                    apiTermination ? "Yes" : "No", instance.getState().getName(),
                                    instance.getInstanceType(), instance.getKeyName(),
                                    StringUtils.join(reservation.getGroupNames(), ","),
                                    instance.getPlacement().getAvailabilityZone(),
                                    DATE_FORMAT.format(instance.getLaunchTime()), tags.toString() });
                            DefaultMutableTreeNode instanceNode = new DefaultMutableTreeNode(new InstanceObjectWrapper(
                                    instance, name), false);
                            rootNode.add(instanceNode);
                            treeModel.reload();
                        } catch (Exception ex) {
                            Logger.getLogger(InstancesPanel.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        }).start();
    }

    protected void setToClipBoard(String data) {
        StringSelection clipData = new StringSelection(data);
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(clipData, clipData);
    }

    /*
     * (non-Jsdoc)
     * 
     * @see com.yosanai.java.aws.console.AWSAware#init()
     */
    @Override
    public void init() {
        loadInstances();
    }

    /**
     * @return the parentFrame
     */
    public JFrame getParentFrame() {
        return parentFrame;
    }

    /**
     * @param parentFrame
     *            the parentFrame to set
     */
    public void setParentFrame(JFrame parentFrame) {
        this.parentFrame = parentFrame;
    }

    public void showPopup(MouseEvent e) {
        if (e.isPopupTrigger()) {
            tblPopup.show(e.getComponent(), e.getX(), e.getY());
        }
    }

    /*
     * (non-Jsdoc)
     * 
     * @see
     * com.yosanai.java.aws.console.AWSAware#setAwsConnectionProvider(com.yosanai
     * .java.aws.console.AWSConnectionProvider)
     */
    @Override
    public void setAwsConnectionProvider(AWSConnectionProvider awsConnectionProvider) {
        this.awsConnectionProvider = awsConnectionProvider;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed"
    // <editor-fold defaultstate="collapsed"
    // <editor-fold defaultstate="collapsed"
    // <editor-fold defaultstate="collapsed"
    // <editor-fold defaultstate="collapsed"
    // <editor-fold defaultstate="collapsed"
    // <editor-fold defaultstate="collapsed"
    // <editor-fold defaultstate="collapsed"
    // <editor-fold defaultstate="collapsed"
    // <editor-fold defaultstate="collapsed"
    // <editor-fold defaultstate="collapsed"
    // <editor-fold defaultstate="collapsed"
    // <editor-fold defaultstate="collapsed"
    // <editor-fold defaultstate="collapsed"
    // <editor-fold defaultstate="collapsed"
    // <editor-fold defaultstate="collapsed"
    // <editor-fold defaultstate="collapsed"
    // <editor-fold defaultstate="collapsed"
    // desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tblPopup = new javax.swing.JPopupMenu();
        mnuStart = new javax.swing.JMenuItem();
        mnuStop = new javax.swing.JMenuItem();
        mnuTerminate = new javax.swing.JMenuItem();
        mnuSepOne = new javax.swing.JPopupMenu.Separator();
        mnuEnableApiTermination = new javax.swing.JMenuItem();
        mnuDisableApiTermination = new javax.swing.JMenuItem();
        mnuSepTwo = new javax.swing.JPopupMenu.Separator();
        mnuCpyInstanceID = new javax.swing.JMenuItem();
        mnuCpyPublicDNS = new javax.swing.JMenuItem();
        mnuCpyPublicIP = new javax.swing.JMenuItem();
        mnuCpyPrivateDNS = new javax.swing.JMenuItem();
        mnuCpyPrivateIP = new javax.swing.JMenuItem();
        mnuSepThree = new javax.swing.JPopupMenu.Separator();
        pnlInstances = new javax.swing.JPanel();
        pnlInstanceMain = new javax.swing.JPanel();
        btnLaunch = new javax.swing.JButton();
        btnRefresh = new javax.swing.JButton();
        spltInstances = new javax.swing.JSplitPane();
        scrInstances = new javax.swing.JScrollPane();
        tblInstances = new javax.swing.JTable();
        scrInsTree = new javax.swing.JScrollPane();
        trInstances = new javax.swing.JTree();

        mnuStart.setText("Start");
        mnuStart.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuStartActionPerformed(evt);
            }
        });
        tblPopup.add(mnuStart);

        mnuStop.setText("Stop");
        mnuStop.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuStopActionPerformed(evt);
            }
        });
        tblPopup.add(mnuStop);

        mnuTerminate.setText("Terminate");
        mnuTerminate.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuTerminateActionPerformed(evt);
            }
        });
        tblPopup.add(mnuTerminate);
        tblPopup.add(mnuSepOne);

        mnuEnableApiTermination.setText("Enable API Termination");
        mnuEnableApiTermination.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuEnableApiTerminationActionPerformed(evt);
            }
        });
        tblPopup.add(mnuEnableApiTermination);

        mnuDisableApiTermination.setText("Disable API Termination");
        mnuDisableApiTermination.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuDisableApiTerminationActionPerformed(evt);
            }
        });
        tblPopup.add(mnuDisableApiTermination);
        tblPopup.add(mnuSepTwo);

        mnuCpyInstanceID.setText("Copy Instance ID(s) to ClipBoard");
        mnuCpyInstanceID.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuCpyInstanceIDActionPerformed(evt);
            }
        });
        tblPopup.add(mnuCpyInstanceID);

        mnuCpyPublicDNS.setText("Copy Public DNS(s) to ClipBoard");
        mnuCpyPublicDNS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuCpyPublicDNSActionPerformed(evt);
            }
        });
        tblPopup.add(mnuCpyPublicDNS);

        mnuCpyPublicIP.setText("Copy Public IP(s) to ClipBoard");
        mnuCpyPublicIP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuCpyPublicIPActionPerformed(evt);
            }
        });
        tblPopup.add(mnuCpyPublicIP);

        mnuCpyPrivateDNS.setText("Copy Private DNS(s) to ClipBoard");
        mnuCpyPrivateDNS.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuCpyPrivateDNSActionPerformed(evt);
            }
        });
        tblPopup.add(mnuCpyPrivateDNS);

        mnuCpyPrivateIP.setText("Copy Private IP(s) to ClipBoard");
        mnuCpyPrivateIP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                mnuCpyPrivateIPActionPerformed(evt);
            }
        });
        tblPopup.add(mnuCpyPrivateIP);
        tblPopup.add(mnuSepThree);

        setLayout(new java.awt.BorderLayout());

        pnlInstances.setLayout(new java.awt.BorderLayout());

        pnlInstanceMain.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        btnLaunch.setText("Launch");
        btnLaunch.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnLaunchActionPerformed(evt);
            }
        });
        pnlInstanceMain.add(btnLaunch);

        btnRefresh.setText("Refresh");
        btnRefresh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnRefreshActionPerformed(evt);
            }
        });
        pnlInstanceMain.add(btnRefresh);

        pnlInstances.add(pnlInstanceMain, java.awt.BorderLayout.PAGE_START);

        spltInstances.setResizeWeight(0.3);

        tblInstances.setAutoCreateRowSorter(true);
        tblInstances.setModel(new javax.swing.table.DefaultTableModel(new Object[][] {

        }, new String[] { "Instance ID", "Public DNS", "Public IP Address", "Private DNS", "Private IP",
                "Terminate via API", "State", "Type", "Key", "Security Group", "Location", "Launched At", "Tag" }) {
            Class[] types = new Class[] { java.lang.String.class, java.lang.String.class, java.lang.Object.class,
                    java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class,
                    java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.String.class,
                    java.lang.String.class, java.lang.String.class };

            boolean[] canEdit = new boolean[] { false, false, false, false, false, false, false, false, false, false,
                    false, false, false };

            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });
        tblInstances.getTableHeader().setReorderingAllowed(false);
        tblInstances.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mousePressed(java.awt.event.MouseEvent evt) {
                tblInstancesMousePressed(evt);
            }

            public void mouseReleased(java.awt.event.MouseEvent evt) {
                tblInstancesMouseReleased(evt);
            }
        });
        scrInstances.setViewportView(tblInstances);

        spltInstances.setRightComponent(scrInstances);

        javax.swing.tree.DefaultMutableTreeNode treeNode1 = new javax.swing.tree.DefaultMutableTreeNode("root");
        trInstances.setModel(new javax.swing.tree.DefaultTreeModel(treeNode1));
        trInstances.addTreeExpansionListener(new javax.swing.event.TreeExpansionListener() {
            public void treeCollapsed(javax.swing.event.TreeExpansionEvent evt) {
                trInstancesTreeCollapsed(evt);
            }

            public void treeExpanded(javax.swing.event.TreeExpansionEvent evt) {
                trInstancesTreeExpanded(evt);
            }
        });
        trInstances.addTreeSelectionListener(new javax.swing.event.TreeSelectionListener() {
            public void valueChanged(javax.swing.event.TreeSelectionEvent evt) {
                trInstancesValueChanged(evt);
            }
        });
        scrInsTree.setViewportView(trInstances);

        spltInstances.setLeftComponent(scrInsTree);

        pnlInstances.add(spltInstances, java.awt.BorderLayout.CENTER);

        add(pnlInstances, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents

    private void trInstancesValueChanged(javax.swing.event.TreeSelectionEvent evt) {// GEN-FIRST:event_trInstancesValueChanged
        Object nodeObj = evt.getPath().getLastPathComponent();
        if (null != nodeObj) {
            if (nodeObj instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) nodeObj;
                Object userObject = treeNode.getUserObject();
                if (userObject instanceof InstanceObjectWrapper) {
                    InstanceObjectWrapper wrapper = (InstanceObjectWrapper) userObject;
                    instanceTableModel.setObject(wrapper.instance);
                    tblInstances.setModel(instanceTableModel);
                } else {
                    tblInstances.setModel(instancesTableModel);
                }

            }
        }
    }// GEN-LAST:event_trInstancesValueChanged

    private void trInstancesTreeCollapsed(javax.swing.event.TreeExpansionEvent evt) {// GEN-FIRST:event_trInstancesTreeCollapsed
        // TODO add your handling code here:
    }// GEN-LAST:event_trInstancesTreeCollapsed

    private void trInstancesTreeExpanded(javax.swing.event.TreeExpansionEvent evt) {// GEN-FIRST:event_trInstancesTreeExpanded
        // TODO add your handling code here:
    }// GEN-LAST:event_trInstancesTreeExpanded

    private void btnLaunchActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnLaunchActionPerformed
        LaunchDialog dialog = new LaunchDialog(parentFrame, true);
        try {
            dialog.setKeyNames(awsConnectionProvider.getKeyPairNames());
            dialog.setSecurityGroups(awsConnectionProvider.getSecurityGroups());
            dialog.setSize(600, 600);
            dialog.setVisible(true);
            if (LaunchDialog.RET_OK == dialog.getReturnStatus()) {
                try {
                    awsConnectionProvider.launchInstance(dialog.getAMIID(), dialog.getSelectedInstanceType(),
                            dialog.getSelectedInstanceCount(), dialog.getKeyName(), dialog.getSecurityGroups(),
                            dialog.getTerminationViaAPI(), dialog.getTags());
                } catch (Exception ex) {
                    Logger.getLogger(InstancesPanel.class.getName()).log(Level.SEVERE, null, ex);
                    JOptionPane.showMessageDialog(this, ex.getLocalizedMessage(), "Failed to launch",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception ex) {
            Logger.getLogger(InstancesPanel.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane
                    .showMessageDialog(this, ex.getLocalizedMessage(), "Failed to launch", JOptionPane.ERROR_MESSAGE);
        }
    }// GEN-LAST:event_btnLaunchActionPerformed

    private void mnuCpyPublicIPActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_mnuCpyPublicIPActionPerformed
        try {
            setToClipBoard(awsConnectionProvider.getInstanceDetails("publicIpAddress", ",", getSelectedInstances()));
        } catch (Exception ex) {
            Logger.getLogger(InstancesPanel.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, ex.getLocalizedMessage(), "Failed to copy", JOptionPane.ERROR_MESSAGE);
        }
    }// GEN-LAST:event_mnuCpyPublicIPActionPerformed

    private void mnuCpyInstanceIDActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_mnuCpyInstanceIDActionPerformed
        try {
            setToClipBoard(awsConnectionProvider.getInstanceDetails("instanceId", ",", getSelectedInstances()));
        } catch (Exception ex) {
            Logger.getLogger(InstancesPanel.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, ex.getLocalizedMessage(), "Failed to copy", JOptionPane.ERROR_MESSAGE);
        }
    }// GEN-LAST:event_mnuCpyInstanceIDActionPerformed

    private void mnuCpyPublicDNSActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_mnuCpyPublicDNSActionPerformed
        try {
            setToClipBoard(awsConnectionProvider.getInstanceDetails("publicDnsName", ",", getSelectedInstances()));
        } catch (Exception ex) {
            Logger.getLogger(InstancesPanel.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, ex.getLocalizedMessage(), "Failed to copy", JOptionPane.ERROR_MESSAGE);
        }
    }// GEN-LAST:event_mnuCpyPublicDNSActionPerformed

    private void mnuCpyPrivateDNSActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_mnuCpyPrivateDNSActionPerformed
        try {
            setToClipBoard(awsConnectionProvider.getInstanceDetails("privateDnsName", ",", getSelectedInstances()));
        } catch (Exception ex) {
            Logger.getLogger(InstancesPanel.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, ex.getLocalizedMessage(), "Failed to copy", JOptionPane.ERROR_MESSAGE);
        }
    }// GEN-LAST:event_mnuCpyPrivateDNSActionPerformed

    private void mnuCpyPrivateIPActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_mnuCpyPrivateIPActionPerformed
        try {
            setToClipBoard(awsConnectionProvider.getInstanceDetails("privateIpAddress", ",", getSelectedInstances()));
        } catch (Exception ex) {
            Logger.getLogger(InstancesPanel.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, ex.getLocalizedMessage(), "Failed to copy", JOptionPane.ERROR_MESSAGE);
        }
    }// GEN-LAST:event_mnuCpyPrivateIPActionPerformed

    private void mnuEnableApiTerminationActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_mnuEnableApiTerminationActionPerformed
        try {
            awsConnectionProvider.setApiTermination(true, getSelectedInstances());
        } catch (Exception ex) {
            Logger.getLogger(InstancesPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }// GEN-LAST:event_mnuEnableApiTerminationActionPerformed

    private void mnuDisableApiTerminationActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_mnuDisableApiTerminationActionPerformed
        try {
            awsConnectionProvider.setApiTermination(false, getSelectedInstances());
        } catch (Exception ex) {
            Logger.getLogger(InstancesPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }// GEN-LAST:event_mnuDisableApiTerminationActionPerformed

    protected String[] getSelectedInstances() {
        String[] ret = null;
        int rows[] = tblInstances.getSelectedRows();
        if (null != rows && 0 < rows.length) {
            ret = new String[rows.length];
            for (int index = 0; index < rows.length; index++) {
                ret[index] = tblInstances.getValueAt(rows[index], 0).toString();
            }
        }
        return ret;
    }

    private void mnuStartActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_mnuStartActionPerformed
        try {
            // GEN-FIRST:event_mnuStartActionPerformed
            awsConnectionProvider.startInstances(getSelectedInstances());
        } catch (Exception ex) {
            Logger.getLogger(InstancesPanel.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, ex.getLocalizedMessage(), "Failed to start", JOptionPane.ERROR_MESSAGE);
        }
    }// GEN-LAST:event_mnuStartActionPerformed

    private void mnuStopActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_mnuStopActionPerformed
        try {
            // GEN-FIRST:event_mnuStopActionPerformed
            awsConnectionProvider.stopInstances(getSelectedInstances());
        } catch (Exception ex) {
            Logger.getLogger(InstancesPanel.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, ex.getLocalizedMessage(), "Failed to stop", JOptionPane.ERROR_MESSAGE);
        }
    }// GEN-LAST:event_mnuStopActionPerformed

    private void mnuTerminateActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_mnuTerminateActionPerformed
        try {
            // GEN-FIRST:event_mnuTerminateActionPerformed
            awsConnectionProvider.terminateInstances(getSelectedInstances());
        } catch (Exception ex) {
            Logger.getLogger(InstancesPanel.class.getName()).log(Level.SEVERE, null, ex);
            JOptionPane.showMessageDialog(this, ex.getLocalizedMessage(), "Failed to terminate",
                    JOptionPane.ERROR_MESSAGE);
        }
    }// GEN-LAST:event_mnuTerminateActionPerformed

    private void tblInstancesMousePressed(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_tblInstancesMousePressed
        showPopup(evt);
    }// GEN-LAST:event_tblInstancesMousePressed

    private void tblInstancesMouseReleased(java.awt.event.MouseEvent evt) {// GEN-FIRST:event_tblInstancesMouseReleased
        showPopup(evt);
    }// GEN-LAST:event_tblInstancesMouseReleased

    private void btnRefreshActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnRefreshActionPerformed
        loadInstances();
    }// GEN-LAST:event_btnRefreshActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnLaunch;

    private javax.swing.JButton btnRefresh;

    private javax.swing.JMenuItem mnuCpyInstanceID;

    private javax.swing.JMenuItem mnuCpyPrivateDNS;

    private javax.swing.JMenuItem mnuCpyPrivateIP;

    private javax.swing.JMenuItem mnuCpyPublicDNS;

    private javax.swing.JMenuItem mnuCpyPublicIP;

    private javax.swing.JMenuItem mnuDisableApiTermination;

    private javax.swing.JMenuItem mnuEnableApiTermination;

    private javax.swing.JPopupMenu.Separator mnuSepOne;

    private javax.swing.JPopupMenu.Separator mnuSepThree;

    private javax.swing.JPopupMenu.Separator mnuSepTwo;

    private javax.swing.JMenuItem mnuStart;

    private javax.swing.JMenuItem mnuStop;

    private javax.swing.JMenuItem mnuTerminate;

    private javax.swing.JPanel pnlInstanceMain;

    private javax.swing.JPanel pnlInstances;

    private javax.swing.JScrollPane scrInsTree;

    private javax.swing.JScrollPane scrInstances;

    private javax.swing.JSplitPane spltInstances;

    private javax.swing.JTable tblInstances;

    private javax.swing.JPopupMenu tblPopup;

    private javax.swing.JTree trInstances;
    // End of variables declaration//GEN-END:variables

}
