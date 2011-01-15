/*
 * The MIT License
 *
 * Copyright 2011 peru.
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

/*
 * AWSConfigDialog.java
 *
 * Created on Jan 14, 2011, 8:02:37 PM
 */

package com.yosanai.java.aws.console;

import javax.swing.table.DefaultTableModel;

import org.apache.commons.configuration.Configuration;

/**
 * 
 * @author peru
 */
public class AWSConfigDialog extends javax.swing.JDialog {
    /** A return status code - returned if Cancel button has been pressed */
    public static final int RET_CANCEL = 0;

    /** A return status code - returned if OK button has been pressed */
    public static final int RET_OK = 1;

    /** Creates new form AWSConfigDialog */
    public AWSConfigDialog(java.awt.Frame parent, boolean modal) {
        super(parent, modal);
        initComponents();
    }

    /** @return the return status of this dialog - one of RET_OK or RET_CANCEL */
    public int getReturnStatus() {
        return returnStatus;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed"
    // <editor-fold defaultstate="collapsed"
    // desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pnlBottom = new javax.swing.JPanel();
        okButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        pnlTop = new javax.swing.JPanel();
        scrConfig = new javax.swing.JScrollPane();
        tblConfig = new javax.swing.JTable();

        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                closeDialog(evt);
            }
        });

        pnlBottom.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));

        okButton.setText("OK");
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });
        pnlBottom.add(okButton);

        cancelButton.setText("Cancel");
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        pnlBottom.add(cancelButton);

        getContentPane().add(pnlBottom, java.awt.BorderLayout.PAGE_END);

        pnlTop.setLayout(new java.awt.BorderLayout());

        tblConfig.setModel(new javax.swing.table.DefaultTableModel(new Object[][] {

        }, new String[] { "Key", "Value" }) {
            Class[] types = new Class[] { java.lang.String.class, java.lang.String.class };

            boolean[] canEdit = new boolean[] { false, true };

            public Class getColumnClass(int columnIndex) {
                return types[columnIndex];
            }

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit[columnIndex];
            }
        });
        scrConfig.setViewportView(tblConfig);
        tblConfig.getColumnModel().getColumn(0).setResizable(false);
        tblConfig.getColumnModel().getColumn(1).setResizable(false);

        pnlTop.add(scrConfig, java.awt.BorderLayout.CENTER);

        getContentPane().add(pnlTop, java.awt.BorderLayout.CENTER);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_okButtonActionPerformed
        doClose(RET_OK);
    }// GEN-LAST:event_okButtonActionPerformed

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_cancelButtonActionPerformed
        doClose(RET_CANCEL);
    }// GEN-LAST:event_cancelButtonActionPerformed

    /** Closes the dialog */
    private void closeDialog(java.awt.event.WindowEvent evt) {// GEN-FIRST:event_closeDialog
        doClose(RET_CANCEL);
    }// GEN-LAST:event_closeDialog

    private void doClose(int retStatus) {
        returnStatus = retStatus;
        setVisible(false);
        dispose();
    }

    /**
     * @param args
     *            the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                AWSConfigDialog dialog = new AWSConfigDialog(new javax.swing.JFrame(), true);
                dialog.addWindowListener(new java.awt.event.WindowAdapter() {
                    public void windowClosing(java.awt.event.WindowEvent e) {
                        System.exit(0);
                    }
                });
                dialog.setVisible(true);
            }
        });
    }

    public void load(Configuration config, String... keys) {
        DefaultTableModel model = (DefaultTableModel) tblConfig.getModel();
        while (0 < model.getRowCount()) {
            model.removeRow(0);
        }
        for (String key : keys) {
            model.addRow(new Object[] { key, config.getString(key, "") });
        }
    }

    public void updateConfig(Configuration config) {
        config.clear();
        for (int index = 0; index < tblConfig.getRowCount(); index++) {
            config.addProperty(tblConfig.getValueAt(index, 0).toString(), tblConfig.getValueAt(index, 1).toString());
        }
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;

    private javax.swing.JButton okButton;

    private javax.swing.JPanel pnlBottom;

    private javax.swing.JPanel pnlTop;

    private javax.swing.JScrollPane scrConfig;

    private javax.swing.JTable tblConfig;

    // End of variables declaration//GEN-END:variables

    private int returnStatus = RET_CANCEL;
}
