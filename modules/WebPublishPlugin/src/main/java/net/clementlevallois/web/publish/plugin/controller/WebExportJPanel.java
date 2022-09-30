/*
 * author: ClÃ©ment Levallois
 */
package net.clementlevallois.web.publish.plugin.controller;

import javax.swing.SwingUtilities;
import net.clementlevallois.web.publish.plugin.github.PublishRunnable;
import com.google.gson.JsonObject;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;
import javax.swing.JLabel;

import javax.swing.JTextField;
import javax.swing.Timer;
import static net.clementlevallois.web.publish.plugin.controller.GlobalConfigParams.*;

import net.clementlevallois.web.publish.plugin.github.GithubAuthRunnable;
import net.clementlevallois.web.publish.plugin.exceptions.PublishToGistException;
import net.clementlevallois.web.publish.plugin.model.GitHubModel;
import org.gephi.utils.longtask.api.LongTaskExecutor;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.NbPreferences;

/**
 *
 * @author LEVALLOIS
 *
 * The logic of this plugin follows the logic laid out here:
 * https://github.com/gephi/gephi-plugins/issues/262#issuecomment-1231627948
 *
 */
public class WebExportJPanel extends javax.swing.JPanel {

    private final GitHubModel gitHubModel;

    private static final ResourceBundle bundle = NbBundle.getBundle(WebPublishExporterUI.class);

    public static final String COLOR_SUCCESS = "#45ba48";

    private final LongTaskExecutor executor;

    private String retinaUrl;
    private String gistUrl;

    public WebExportJPanel() {
        initComponents();
        gitHubModel = new GitHubModel();
        Preferences preferences = NbPreferences.forModule(this.getClass());
        String accessToken = preferences.get(ACCESS_TOKEN_KEY_IN_USER_PREFS, "");
        gitHubModel.setAccessToken(accessToken);
        jLabelAlreadyLoggedIn.setVisible(accessToken != null && !accessToken.isBlank());
        jTextFieldGithubErrorMsg.setBackground(Color.WHITE);
        jTextFieldGithubErrorMsg.setText(bundle.getString("general.message.errors_appear.here"));
        jTextFieldGithubErrorMsg.setCaretPosition(0);
        jTextFieldUserCode.setForeground(Color.RED);
        jTextAreaUrls.setText("");
        jButtonOpenViewerlink.setEnabled(false);
        jButtonCopyViewerLink.setEnabled(false);
        jButtonOpenGexf.setEnabled(false);
        jButtonCopyGexfLink.setEnabled(false);

        jButtonCopyViewerLink.addActionListener(e -> {
            String oldText = jButtonCopyViewerLink.getText();
            jButtonCopyViewerLink.setText(bundle.getString("general.message.link_is_copied"));
            Timer timer = new Timer(2000, event -> {
                jButtonCopyViewerLink.setText(oldText);
            });
            timer.setRepeats(false);
            timer.start();
        });

        jButtonCopyGexfLink.addActionListener(e -> {
            String oldText = jButtonCopyGexfLink.getText();
            jButtonCopyGexfLink.setText(bundle.getString("general.message.link_is_copied"));
            Timer timer = new Timer(2000, event -> {
                jButtonCopyGexfLink.setText(oldText);
            });
            timer.setRepeats(false);
            timer.start();
        });

        // Setup executor
        executor = new LongTaskExecutor(true, "WebPublishPlugin");
        executor.setDefaultErrorHandler((Throwable t) -> {
            SwingUtilities.invokeLater(() -> {
                if (t instanceof PublishToGistException) {
                    jTextAreaUrls.setText(t.getMessage());
                    jTextAreaUrls.setCaretPosition(0);
                } else {
                    jTextFieldGithubErrorMsg.setText(t.getMessage());
                    jTextFieldGithubErrorMsg.setCaretPosition(0);
                }
            });
            Exceptions.printStackTrace(t);
        });

        // When task finished
        executor.setLongTaskListener(longTask -> {
            if (longTask instanceof PublishRunnable) {
                retinaUrl = ((PublishRunnable) longTask).getRetinaUrl();
                if (retinaUrl != null) {
                    SwingUtilities.invokeLater(() -> {
                        jButtonCopyViewerLink.setEnabled(true);
                        jButtonOpenViewerlink.setEnabled(true);
                        jTextAreaUrls.setText("");
                        jTextAreaUrls.setCaretPosition(0);
                    });
                } else {
                    SwingUtilities.invokeLater(() -> {
                        jButtonOpenViewerlink.setEnabled(false);
                        jButtonCopyViewerLink.setEnabled(false);
                    });
                }
                gistUrl = ((PublishRunnable) longTask).getGistUrl();
                if (gistUrl != null) {
                    SwingUtilities.invokeLater(() -> {
                        jButtonCopyGexfLink.setEnabled(true);
                        jButtonOpenGexf.setEnabled(true);
                        jTextAreaUrls.setText("");
                        jTextAreaUrls.setCaretPosition(0);
                    });
                } else {
                    jButtonOpenGexf.setEnabled(false);
                    jButtonCopyGexfLink.setEnabled(false);
                }
            } else if (longTask instanceof GithubAuthRunnable) {
                SwingUtilities.invokeLater(() -> {
                    if (gitHubModel.hasAccessToken()) {
                        jTextFieldGithubErrorMsg.setForeground(Color.decode(COLOR_SUCCESS));
                        jTextFieldGithubErrorMsg.setText(
                                bundle.getString("general.message.success_switch_to_publish"));
                        jTextFieldGithubErrorMsg.setCaretPosition(0);
                    } else {
                        jTextFieldGithubErrorMsg.setText(bundle.getString("general.message.error.no_user_code"));
                        jTextFieldGithubErrorMsg.setCaretPosition(0);
                    }
                });
            }
        });
    }

    public JTextField getjTextFieldUserCode() {
        return jTextFieldUserCode;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tabs = new javax.swing.JTabbedPane();
        tabGithub = new javax.swing.JPanel();
        jLabelAlreadyLoggedIn = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jLabel4 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jPanel3 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jLabel5 = new javax.swing.JLabel();
        jPanel4 = new javax.swing.JPanel();
        jButtonConnectToGephiLite = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        jTextFieldUserCode = new javax.swing.JTextField();
        jPanel6 = new javax.swing.JPanel();
        jTextFieldWebsiteLoginUrl = new javax.swing.JTextField();
        jLabel6 = new javax.swing.JLabel();
        jPanel7 = new javax.swing.JPanel();
        jLabel7 = new javax.swing.JLabel();
        jPanel8 = new javax.swing.JPanel();
        jTextFieldGithubErrorMsg = new javax.swing.JTextField();
        jButtonResetLogin = new javax.swing.JButton();
        tabPublish = new javax.swing.JPanel();
        jPanel11 = new javax.swing.JPanel();
        jLabel10 = new javax.swing.JLabel();
        jPanel12 = new javax.swing.JPanel();
        jButtonPublish = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextAreaUrls = new javax.swing.JTextArea();
        jPanel2 = new javax.swing.JPanel();
        jLabel8 = new javax.swing.JLabel();
        jButtonOpenViewerlink = new javax.swing.JButton();
        jButtonCopyViewerLink = new javax.swing.JButton();
        jPanel9 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jButtonCopyGexfLink = new javax.swing.JButton();
        jButtonOpenGexf = new javax.swing.JButton();

        tabs.setMinimumSize(new java.awt.Dimension(700, 454));
        tabs.setPreferredSize(new java.awt.Dimension(700, 454));

        jLabelAlreadyLoggedIn.setFont(new java.awt.Font("Segoe UI", 1, 12)); // NOI18N
        jLabelAlreadyLoggedIn.setForeground(new java.awt.Color(0, 204, 102));
        org.openide.awt.Mnemonics.setLocalizedText(jLabelAlreadyLoggedIn, "<html>"+bundle.getString("general.message.warning_setup_already_done")+"</html>");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, org.openide.util.NbBundle.getMessage(WebExportJPanel.class, "JPanelWebExport.step1.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 1, 12))); // NOI18N

        jLabel4.setFont(new java.awt.Font("Segoe UI", 2, 12)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jLabel4, "<html>"+bundle.getString("general.message.github.create_account")+"</html>");

        jTextField1.setText(org.openide.util.NbBundle.getMessage(WebExportJPanel.class, "WebExportJPanel.jTextField1.text")); // NOI18N

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addGap(16, 16, 16))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, org.openide.util.NbBundle.getMessage(WebExportJPanel.class, "JPanelWebExport.step3.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 1, 12))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel3, org.openide.util.NbBundle.getMessage(WebExportJPanel.class, "JPanelWebExport.step3.info1")); // NOI18N

        jTextField2.setText(org.openide.util.NbBundle.getMessage(WebExportJPanel.class, "WebExportJPanel.jTextField2.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel5, org.openide.util.NbBundle.getMessage(WebExportJPanel.class, "JPanelWebExport.step3.info2")); // NOI18N

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(jLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jLabel3)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabel5)
                .addContainerGap())
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(null, org.openide.util.NbBundle.getMessage(WebExportJPanel.class, "JPanelWebExport.step4.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 1, 12))); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonConnectToGephiLite, org.openide.util.NbBundle.getMessage(WebExportJPanel.class, "WebExportJPanel.jButtonConnectToGephiLite.text")); // NOI18N
        jButtonConnectToGephiLite.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonConnectToGephiLiteActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, bundle.getString("general.message.then_wait_for_code")+ " --> " 
        );

        jTextFieldUserCode.setForeground(new java.awt.Color(255, 0, 0));
        jTextFieldUserCode.setText(org.openide.util.NbBundle.getMessage(WebExportJPanel.class, "WebExportJPanel.jTextFieldUserCode.text")); // NOI18N

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButtonConnectToGephiLite)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel1, javax.swing.GroupLayout.PREFERRED_SIZE, 270, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jTextFieldUserCode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButtonConnectToGephiLite)
                    .addComponent(jLabel1)
                    .addComponent(jTextFieldUserCode, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel6.setBorder(javax.swing.BorderFactory.createTitledBorder(null, org.openide.util.NbBundle.getMessage(WebExportJPanel.class, "JPanelWebExport.step6.title"), javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Segoe UI", 1, 12))); // NOI18N

        jTextFieldWebsiteLoginUrl.setText(org.openide.util.NbBundle.getMessage(WebExportJPanel.class, "WebExportJPanel.jTextFieldWebsiteLoginUrl.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jLabel6, org.openide.util.NbBundle.getMessage(WebExportJPanel.class, "JPanelWebExport.step6.info1")); // NOI18N

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTextFieldWebsiteLoginUrl, javax.swing.GroupLayout.PREFERRED_SIZE, 265, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addComponent(jLabel6)
                .addContainerGap(202, Short.MAX_VALUE))
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jTextFieldWebsiteLoginUrl, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6))
                .addContainerGap(19, Short.MAX_VALUE))
        );

        jPanel7.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        org.openide.awt.Mnemonics.setLocalizedText(jLabel7, "<html>"+bundle.getString("general.message.success_ready_to_publish")+"</html>");

        javax.swing.GroupLayout jPanel7Layout = new javax.swing.GroupLayout(jPanel7);
        jPanel7.setLayout(jPanel7Layout);
        jPanel7Layout.setHorizontalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7, javax.swing.GroupLayout.DEFAULT_SIZE, 278, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel7Layout.setVerticalGroup(
            jPanel7Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel7Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel8.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jTextFieldGithubErrorMsg.setForeground(new java.awt.Color(255, 0, 0));

        javax.swing.GroupLayout jPanel8Layout = new javax.swing.GroupLayout(jPanel8);
        jPanel8.setLayout(jPanel8Layout);
        jPanel8Layout.setHorizontalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTextFieldGithubErrorMsg)
                .addContainerGap())
        );
        jPanel8Layout.setVerticalGroup(
            jPanel8Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel8Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jTextFieldGithubErrorMsg, javax.swing.GroupLayout.PREFERRED_SIZE, 44, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jButtonResetLogin.setBackground(new java.awt.Color(204, 204, 204));
        org.openide.awt.Mnemonics.setLocalizedText(jButtonResetLogin, org.openide.util.NbBundle.getMessage(WebExportJPanel.class, "WebExportJPanel.jButtonResetLogin.text")); // NOI18N
        jButtonResetLogin.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonResetLoginActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout tabGithubLayout = new javax.swing.GroupLayout(tabGithub);
        tabGithub.setLayout(tabGithubLayout);
        tabGithubLayout.setHorizontalGroup(
            tabGithubLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tabGithubLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(tabGithubLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel6, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jLabelAlreadyLoggedIn, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, tabGithubLayout.createSequentialGroup()
                        .addGroup(tabGithubLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addGroup(tabGithubLayout.createSequentialGroup()
                                .addGap(0, 0, Short.MAX_VALUE)
                                .addComponent(jButtonResetLogin, javax.swing.GroupLayout.PREFERRED_SIZE, 280, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(tabGithubLayout.createSequentialGroup()
                                .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jPanel8, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addContainerGap())))
        );
        tabGithubLayout.setVerticalGroup(
            tabGithubLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, tabGithubLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jLabelAlreadyLoggedIn)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(tabGithubLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel7, javax.swing.GroupLayout.PREFERRED_SIZE, 60, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel8, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonResetLogin, javax.swing.GroupLayout.PREFERRED_SIZE, 36, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(130, 130, 130))
        );

        tabs.addTab(org.openide.util.NbBundle.getMessage(WebExportJPanel.class, "WebExportJPanel.tabGithub.TabConstraints.tabTitle"), tabGithub); // NOI18N

        jPanel11.setFocusTraversalPolicyProvider(true);

        jLabel10.setForeground(new java.awt.Color(255, 51, 51));
        jLabel10.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel10, "<html>"+bundle.getString("general.message.warning_confidentiality")+"</html>");

        javax.swing.GroupLayout jPanel11Layout = new javax.swing.GroupLayout(jPanel11);
        jPanel11.setLayout(jPanel11Layout);
        jPanel11Layout.setHorizontalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel10, javax.swing.GroupLayout.DEFAULT_SIZE, 638, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel11Layout.setVerticalGroup(
            jPanel11Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel11Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel10, javax.swing.GroupLayout.PREFERRED_SIZE, 55, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jButtonPublish.setBackground(new java.awt.Color(204, 204, 204));
        jButtonPublish.setFont(new java.awt.Font("Segoe UI", 0, 18)); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(jButtonPublish, org.openide.util.NbBundle.getMessage(WebExportJPanel.class, "WebExportJPanel.jButtonPublish.text")); // NOI18N
        jButtonPublish.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonPublishActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel12Layout = new javax.swing.GroupLayout(jPanel12);
        jPanel12.setLayout(jPanel12Layout);
        jPanel12Layout.setHorizontalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButtonPublish)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel12Layout.setVerticalGroup(
            jPanel12Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel12Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jButtonPublish, javax.swing.GroupLayout.PREFERRED_SIZE, 45, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(17, Short.MAX_VALUE))
        );

        jTextAreaUrls.setColumns(20);
        jTextAreaUrls.setRows(5);
        jTextAreaUrls.setText(org.openide.util.NbBundle.getMessage(WebExportJPanel.class, "general.message.errors_appear.here")); // NOI18N
        jScrollPane1.setViewportView(jTextAreaUrls);

        jPanel2.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        jPanel2.setPreferredSize(new java.awt.Dimension(315, 97));

        jLabel8.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel8, org.openide.util.NbBundle.getMessage(WebExportJPanel.class, "general.message.link_to_network_viz")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonOpenViewerlink, org.openide.util.NbBundle.getMessage(WebExportJPanel.class, "general.message.open_in_web_browser")); // NOI18N
        jButtonOpenViewerlink.setMaximumSize(new java.awt.Dimension(244, 23));
        jButtonOpenViewerlink.setMinimumSize(new java.awt.Dimension(244, 23));
        jButtonOpenViewerlink.setPreferredSize(new java.awt.Dimension(244, 23));
        jButtonOpenViewerlink.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOpenViewerlinkActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jButtonCopyViewerLink, org.openide.util.NbBundle.getMessage(WebExportJPanel.class, "general.message.copy_to_clipboard")); // NOI18N
        jButtonCopyViewerLink.setMaximumSize(new java.awt.Dimension(244, 23));
        jButtonCopyViewerLink.setMinimumSize(new java.awt.Dimension(244, 23));
        jButtonCopyViewerLink.setPreferredSize(new java.awt.Dimension(244, 23));
        jButtonCopyViewerLink.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCopyViewerLinkActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap(25, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jButtonCopyViewerLink, javax.swing.GroupLayout.PREFERRED_SIZE, 244, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jButtonOpenViewerlink, javax.swing.GroupLayout.PREFERRED_SIZE, 244, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel8, javax.swing.GroupLayout.DEFAULT_SIZE, 244, Short.MAX_VALUE)
                        .addGap(34, 34, 34))))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel8)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonCopyViewerLink, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 11, Short.MAX_VALUE)
                .addComponent(jButtonOpenViewerlink, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel9.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));

        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(WebExportJPanel.class, "general.message.link_to_gexf")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(jButtonCopyGexfLink, org.openide.util.NbBundle.getMessage(WebExportJPanel.class, "general.message.copy_to_clipboard")); // NOI18N
        jButtonCopyGexfLink.setMaximumSize(new java.awt.Dimension(244, 23));
        jButtonCopyGexfLink.setMinimumSize(new java.awt.Dimension(244, 23));
        jButtonCopyGexfLink.setPreferredSize(new java.awt.Dimension(244, 23));
        jButtonCopyGexfLink.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonCopyGexfLinkActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(jButtonOpenGexf, org.openide.util.NbBundle.getMessage(WebExportJPanel.class, "general.message.open_in_web_browser")); // NOI18N
        jButtonOpenGexf.setMaximumSize(new java.awt.Dimension(244, 23));
        jButtonOpenGexf.setMinimumSize(new java.awt.Dimension(244, 23));
        jButtonOpenGexf.setPreferredSize(new java.awt.Dimension(244, 23));
        jButtonOpenGexf.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButtonOpenGexfActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel9Layout = new javax.swing.GroupLayout(jPanel9);
        jPanel9.setLayout(jPanel9Layout);
        jPanel9Layout.setHorizontalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 297, Short.MAX_VALUE)
                .addContainerGap())
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addGroup(jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(jButtonOpenGexf, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jButtonCopyGexfLink, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel9Layout.setVerticalGroup(
            jPanel9Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel9Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jLabel2, javax.swing.GroupLayout.PREFERRED_SIZE, 16, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jButtonCopyGexfLink, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButtonOpenGexf, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(11, Short.MAX_VALUE))
        );

        javax.swing.GroupLayout tabPublishLayout = new javax.swing.GroupLayout(tabPublish);
        tabPublish.setLayout(tabPublishLayout);
        tabPublishLayout.setHorizontalGroup(
            tabPublishLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tabPublishLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(tabPublishLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, tabPublishLayout.createSequentialGroup()
                        .addGroup(tabPublishLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jPanel11, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel12, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addGap(70, 70, 70))
                    .addGroup(tabPublishLayout.createSequentialGroup()
                        .addGroup(tabPublishLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(tabPublishLayout.createSequentialGroup()
                                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 309, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jPanel9, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addContainerGap(84, Short.MAX_VALUE))))
        );
        tabPublishLayout.setVerticalGroup(
            tabPublishLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(tabPublishLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel11, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(tabPublishLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel9, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, 103, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 63, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(95, Short.MAX_VALUE))
        );

        tabs.addTab(org.openide.util.NbBundle.getMessage(WebExportJPanel.class, "WebExportJPanel.tabPublish.TabConstraints.tabTitle"), tabPublish); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 26, Short.MAX_VALUE)
                .addComponent(tabs, javax.swing.GroupLayout.DEFAULT_SIZE, 726, Short.MAX_VALUE)
                .addGap(0, 26, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabs, javax.swing.GroupLayout.DEFAULT_SIZE, 475, Short.MAX_VALUE)
                .addContainerGap(27, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButtonResetLoginActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonResetLoginActionPerformed
        Preferences preferences = NbPreferences.forModule(this.getClass());
        preferences.remove(ACCESS_TOKEN_KEY_IN_USER_PREFS);
        jLabelAlreadyLoggedIn.setVisible(false);
        jTextFieldGithubErrorMsg.setText(bundle.getString("general.message.success_reset"));
        jTextFieldGithubErrorMsg.setCaretPosition(0);
    }//GEN-LAST:event_jButtonResetLoginActionPerformed

    private void jButtonConnectToGephiLiteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonConnectToGephiLiteActionPerformed
        jTextFieldGithubErrorMsg.setBackground(Color.WHITE);
        jTextFieldGithubErrorMsg.setText("");
        JsonObject responseGithubConnectAction;
        try {
            responseGithubConnectAction = PublishRunnable.connectToGithub();
        } catch (IOException | InterruptedException ex) {
            Exceptions.printStackTrace(ex);
            jTextFieldUserCode.setForeground(Color.RED);
            jTextFieldGithubErrorMsg.setText(bundle.getString("general.message.error.probably_internet_connection"));
            jTextFieldGithubErrorMsg.setCaretPosition(0);
            return;
        }
        if (!responseGithubConnectAction.has("user_code")) {
            jTextFieldUserCode.setForeground(Color.RED);
            jTextFieldGithubErrorMsg.setText(bundle.getString("general.message.error.cant_retrieve_user_code"));
            jTextFieldGithubErrorMsg.setCaretPosition(0);
        } else {
            String userCode = responseGithubConnectAction.get("user_code").getAsString();
            String deviceCode = responseGithubConnectAction.get("device_code").getAsString();
            gitHubModel.setDeviceCode(deviceCode);
            jTextFieldUserCode.setForeground(Color.decode(COLOR_SUCCESS));
            jTextFieldUserCode.setText(userCode);
            GithubAuthRunnable githubAuthRunnable = new GithubAuthRunnable(gitHubModel);
            executor.execute(githubAuthRunnable, githubAuthRunnable);
        }
    }//GEN-LAST:event_jButtonConnectToGephiLiteActionPerformed

    private void jButtonCopyGexfLinkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCopyGexfLinkActionPerformed
        if (gistUrl == null) {
            return;
        }
        StringSelection stringSelection = new StringSelection(gistUrl);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);
    }//GEN-LAST:event_jButtonCopyGexfLinkActionPerformed

    private void jButtonOpenGexfActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOpenGexfActionPerformed
        openWebpage(URI.create(gistUrl));
    }//GEN-LAST:event_jButtonOpenGexfActionPerformed

    private void jButtonCopyViewerLinkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonCopyViewerLinkActionPerformed
        if (retinaUrl == null) {
            return;
        }
        StringSelection stringSelection = new StringSelection(retinaUrl);
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        clipboard.setContents(stringSelection, null);

    }//GEN-LAST:event_jButtonCopyViewerLinkActionPerformed

    private void jButtonOpenViewerlinkActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonOpenViewerlinkActionPerformed
        openWebpage(URI.create(retinaUrl));
    }//GEN-LAST:event_jButtonOpenViewerlinkActionPerformed

    private void jButtonPublishActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButtonPublishActionPerformed
        // Confirms in case the graph is large
        if (PublishRunnable.isGraphVeryLarge()) {
            JLabel warningMessage = new JLabel();
            warningMessage.setText(bundle.getString("general.message.warning.network_too_big"));
            NotifyDescriptor.Confirmation confirmation = new DialogDescriptor.Confirmation(warningMessage, bundle.getString("general.noun.warning"), NotifyDescriptor.WARNING_MESSAGE, NotifyDescriptor.YES_NO_OPTION);
            if (DialogDisplayer.getDefault().notify(confirmation) != NotifyDescriptor.YES_OPTION) {
                return;
            }
        }

        // Access token
        Preferences preferences = NbPreferences.forModule(this.getClass());
        String accessToken = preferences.get(ACCESS_TOKEN_KEY_IN_USER_PREFS, "");
        gitHubModel.setAccessToken(accessToken);
        if (accessToken == null || accessToken.isBlank()) {
            jTextAreaUrls.setText(bundle.getString("general.message.error.no_token"));
            return;
        }

        // Execute
        PublishRunnable publishRunnable = new PublishRunnable(gitHubModel);
        executor.execute(publishRunnable, publishRunnable);
    }//GEN-LAST:event_jButtonPublishActionPerformed

    public static boolean openWebpage(URI uri) {
        Desktop desktop = Desktop.isDesktopSupported() ? Desktop.getDesktop() : null;
        if (desktop != null && desktop.isSupported(Desktop.Action.BROWSE)) {
            try {
                desktop.browse(uri);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public static boolean openWebpage(URL url) {
        try {
            return openWebpage(url.toURI());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return false;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButtonConnectToGephiLite;
    private javax.swing.JButton jButtonCopyGexfLink;
    private javax.swing.JButton jButtonCopyViewerLink;
    private javax.swing.JButton jButtonOpenGexf;
    private javax.swing.JButton jButtonOpenViewerlink;
    private javax.swing.JButton jButtonPublish;
    private javax.swing.JButton jButtonResetLogin;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel10;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JLabel jLabel8;
    private javax.swing.JLabel jLabelAlreadyLoggedIn;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel11;
    private javax.swing.JPanel jPanel12;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JPanel jPanel7;
    private javax.swing.JPanel jPanel8;
    private javax.swing.JPanel jPanel9;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextArea jTextAreaUrls;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    public static javax.swing.JTextField jTextFieldGithubErrorMsg;
    private javax.swing.JTextField jTextFieldUserCode;
    private javax.swing.JTextField jTextFieldWebsiteLoginUrl;
    private javax.swing.JPanel tabGithub;
    private javax.swing.JPanel tabPublish;
    private javax.swing.JTabbedPane tabs;
    // End of variables declaration//GEN-END:variables
}
