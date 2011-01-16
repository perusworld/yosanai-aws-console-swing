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

package com.yosanai.java.aws.console;

import java.awt.BorderLayout;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.XMLConfiguration;
import org.apache.commons.lang.StringUtils;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.ec2.AmazonEC2;
import com.amazonaws.services.ec2.AmazonEC2Client;
import com.yosanai.java.swing.config.ConfigDialog;

/**
 * 
 * @author Saravana Perumal Shanmugam
 */
public class AWSConsole extends javax.swing.JFrame implements AWSConnectionProvider {

    /**
     * 
     */
    private static final String CONFIG_PROPERTIES = ".yosanai-aws-config";

    /**
     * 
     */
    public static final String AWS_SECRET = "aws.secret";

    /**
     * 
     */
    public static final String AWS_KEY = "aws.key";

    protected MainAWSPanel console;

    protected XMLConfiguration config;

    protected AmazonEC2 amazonEC2;

    protected Object lock = new Object();

    /** Creates new form AWSConsole */
    public AWSConsole() {
        initComponents();
    }

    /*
     * (non-Jsdoc)
     * 
     * @see com.yosanai.java.aws.console.AWSConnectionProvider#getConnection()
     */
    @Override
    public AmazonEC2 getConnection() {
        AmazonEC2 ret = null;
        synchronized (lock) {
            ret = amazonEC2;
        }
        return ret;
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
    // desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tlMain = new javax.swing.JToolBar();
        btnAccess = new javax.swing.JButton();
        btnConfig = new javax.swing.JButton();
        btnExit = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("Yosanai - AWS Console");

        tlMain.setRollover(true);

        btnAccess.setText("Console");
        btnAccess.setFocusable(false);
        btnAccess.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnAccess.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnAccess.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnAccessActionPerformed(evt);
            }
        });
        tlMain.add(btnAccess);

        btnConfig.setText("Config");
        btnConfig.setFocusable(false);
        btnConfig.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnConfig.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnConfig.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnConfigActionPerformed(evt);
            }
        });
        tlMain.add(btnConfig);

        btnExit.setText("Exit");
        btnExit.setFocusable(false);
        btnExit.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnExit.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnExitActionPerformed(evt);
            }
        });
        tlMain.add(btnExit);

        getContentPane().add(tlMain, java.awt.BorderLayout.PAGE_START);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    protected void showConfig(boolean force) {
        ConfigDialog dialog = new ConfigDialog(this, true);
        dialog.setFile(CONFIG_PROPERTIES);
        dialog.init(AWS_KEY, AWS_SECRET);
        boolean updated = false;
        if (!force && StringUtils.isNotBlank(dialog.getConfiguration().getString(AWS_KEY))) {
            updated = true;
        } else {
            dialog.setVisible(true);
            updated = (ConfigDialog.RET_OK == dialog.getReturnStatus());
        }
        if (updated) {
            config = dialog.getConfiguration();
            try {
                config.save();
                updateEC2Config(false);
            } catch (ConfigurationException ex) {
                Logger.getLogger(AWSConsole.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void btnConfigActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnConfigActionPerformed
        showConfig(true);
    }// GEN-LAST:event_btnConfigActionPerformed

    private void btnExitActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnExitActionPerformed
        System.exit(0);
    }// GEN-LAST:event_btnExitActionPerformed

    private void btnAccessActionPerformed(java.awt.event.ActionEvent evt) {// GEN-FIRST:event_btnAccessActionPerformed
        if (null == config) {
            showConfig(false);
        }
        if (StringUtils.isBlank(config.getString(AWS_KEY))) {
            showConfig(true);
        }
        updateEC2Config(true);
        if (null == console && null != amazonEC2) {
            console = new MainAWSPanel();
            console.setAwsConnectionProvider(this);
            getContentPane().add(console, BorderLayout.CENTER);
            invalidate();
            validate();
            console.init();
        }
    }// GEN-LAST:event_btnAccessActionPerformed

    /**
     * 
     */
    protected void updateEC2Config(boolean reuseExisting) {
        if (StringUtils.isNotBlank(config.getString(AWS_KEY))) {
            if (null == amazonEC2 || !reuseExisting) {
                synchronized (lock) {
                    if (null != amazonEC2) {
                        amazonEC2.shutdown();
                        amazonEC2 = null;
                    }
                    try {
                        amazonEC2 = new AmazonEC2Client(new BasicAWSCredentials(config.getString(AWS_KEY, ""),
                                config.getString(AWS_SECRET, "")));
                        amazonEC2.describeInstances();
                    } catch (Exception e) {
                        JOptionPane.showMessageDialog(this, "" + e.getLocalizedMessage(),
                                "Failed to initialize connection to AWS", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        }
    }

    /**
     * @param args
     *            the command line arguments
     */
    public static void main(String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                AWSConsole console = new AWSConsole();
                console.setExtendedState(JFrame.MAXIMIZED_BOTH);
                console.pack();
                console.setVisible(true);
            }
        });
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton btnAccess;

    private javax.swing.JButton btnConfig;

    private javax.swing.JButton btnExit;

    private javax.swing.JToolBar tlMain;
    // End of variables declaration//GEN-END:variables

}
