/*
 * MainWindow.java
 *
 * Created on April 28, 2008, 1:50 PM
 * 
 * Project: BotoSeis
 * 
 * Federal University of Para, Brazil.
 * Faculty of Geophysics.
 */
package botoseis.ivelan.main;

import gfx.GfxPanelColorbar;
import gfx.AxisPanel;
import gfx.SVPoint2D;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Locale;
import java.util.Vector;
import javax.swing.JOptionPane;

/**
 *
 */
public class MainWindow extends javax.swing.JFrame {

    /** Creates new form MainWindow */
    public MainWindow() {
        initComponents();

        panelCDP.add(gfxPanelCDP);
        panelSemblance.add(gfxPanelSemblance);
        panelCVS.add(gfxPanelCVS);

        nmoCurve.setLineStyle(gfx.SVXYPlot.SOLID);
        nmoCurve.setPointsVisible(false);
        nmoCurve.setDrawColor(java.awt.Color.red);
        nmoCurve.setDrawSize(2);
        gfxPanelCDP.addXYPlot(nmoCurve);

        picksCurve.setLineStyle(gfx.SVXYPlot.SOLID);
        picksCurve.setPointsVisible(true);
        picksCurve.setDrawColor(java.awt.Color.black);
        picksCurve.setDrawSize(2);
        gfxPanelSemblance.addXYPlot(picksCurve);
        gfxPanelCVS.addXYPlot(picksCurve);

        intervalCurve.setLineStyle(gfx.SVXYPlot.SOLID);
        intervalCurve.setPointsVisible(false);
        intervalCurve.setDrawColor(java.awt.Color.blue);
        intervalCurve.setVisible(false);
        intervalCurve.setDrawSize(2);
        
        gfxPanelSemblance.addXYPlot(intervalCurve);

        m_timeAxis = new gfx.SVAxis(gfx.SVAxis.VERTICAL, gfx.SVAxis.AXIS_LEFT, "Time (s)");
        m_cdpOffsetAxis = new gfx.SVAxis(gfx.SVAxis.HORIZONTAL, gfx.SVAxis.AXIS_TOP, "Offset (km)");
        m_velocityAxis = new gfx.SVAxis(gfx.SVAxis.HORIZONTAL, gfx.SVAxis.AXIS_TOP, "Velocity (m/s)");
        m_cvsVelocityAxis = new gfx.SVAxis(gfx.SVAxis.HORIZONTAL, gfx.SVAxis.AXIS_TOP, "Velocity (m/s)");

        m_timeAxis.setLimits(m_tmin, m_tmax);

        gfxPanelCDP.setAxisY(m_timeAxis);
        gfxPanelCDP.setAxisX(m_cdpOffsetAxis);

        gfxPanelSemblance.setAxisY(m_timeAxis);
        gfxPanelSemblance.setAxisX(m_velocityAxis);

        gfxPanelCVS.setAxisY(m_timeAxis);
        gfxPanelCVS.setAxisX(m_cvsVelocityAxis);

        AxisPanel panelT = new AxisPanel(m_timeAxis);
        panelA.add(panelT);

        AxisPanel panelU = new AxisPanel(m_cdpOffsetAxis);
        panelB.add(panelU);

        AxisPanel panelV = new AxisPanel(m_velocityAxis);
        panelC.add(panelV);

        AxisPanel panelX = new AxisPanel(m_cvsVelocityAxis);
        panelD.add(panelX);

        gfxPanelSemblance.addMouseListener(new java.awt.event.MouseAdapter() {

            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (m_ready) {
                    float v = gfxPanelSemblance.getMouseLocation().fx;
                    float t = gfxPanelSemblance.getMouseLocation().fy;

                    int vx = gfxPanelSemblance.getMouseLocation().ix;
                    int vy = gfxPanelSemblance.getMouseLocation().iy;

                    int EPS = 20; // 10 pixels

                    gfx.SVPoint2D pickP = null;
                    int removedIndex = -1;
                    switch (evt.getButton()) {
                        case java.awt.event.MouseEvent.BUTTON1:
                            // Remove pick if user clicked over a previous pick.
                            for (int i = 0; i < m_currentCDPVelocityPicks.size(); i++) {
                                pickP = m_currentCDPVelocityPicks.get(i);
                                if ((Math.abs(pickP.ix - vx) <= EPS) && (Math.abs(pickP.iy - vy) <= EPS)) {
                                    removedIndex = i;
                                    break;
                                }
                            }

                            if (removedIndex >= 0) {
                                m_currentCDPVelocityPicks.remove(removedIndex);
                                m_isModified = true;
                            } else {
                                addVelocityPick(v, t, vx, vy);
                                updateIntervalVelocity();
                                m_isModified = true;
                            }

                            break;
                        case java.awt.event.MouseEvent.BUTTON3:
                            break;
                    }
                }
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (m_ready) {
                    m_cursorOverSemblancemap = false;
                    updateVelocityPicksCurve(0, 0);
                    nmoCurve.setVisible(false);

                    repaint();
                }
            }

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (m_ready) {
                    m_cursorOverSemblancemap = true;
                    nmoCurve.setVisible(true);

                    repaint();
                }
            }
        });

        gfxPanelSemblance.addMouseMotionListener(new java.awt.event.MouseAdapter() {

            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                if (m_ready) {
                    float v = gfxPanelSemblance.getMouseLocation().fx;
                    float t = gfxPanelSemblance.getMouseLocation().fy;
//                    System.err.println(v+" "+t);
                    updateNMOCurve(v, t, 0.0f, 0.0f);
                    updateVelocityPicksCurve(v, t);

                    gfxPanelCDP.repaint();
                }
            }
        });

        gfxPanelCVS.addMouseListener(new java.awt.event.MouseAdapter() {

            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (m_ready) {
                    float v = gfxPanelCVS.getMouseLocation().fx;
                    float t = gfxPanelCVS.getMouseLocation().fy;

                    int vx = gfxPanelCVS.getMouseLocation().ix;
                    int vy = gfxPanelCVS.getMouseLocation().iy;

                    int EPS = 20; // 10 pixels

                    gfx.SVPoint2D pickP = null;
                    int removedIndex = -1;
                    switch (evt.getButton()) {
                        case java.awt.event.MouseEvent.BUTTON1:
                            // Remove pick if user clicked over a previous pick.
                            for (int i = 0; i < m_currentCDPVelocityPicks.size(); i++) {
                                pickP = m_currentCDPVelocityPicks.get(i);
                                if ((Math.abs(pickP.ix - vx) <= EPS) && (Math.abs(pickP.iy - vy) <= EPS)) {
                                    removedIndex = i;
                                    break;
                                }
                            }

                            if (removedIndex >= 0) {
                                m_currentCDPVelocityPicks.remove(removedIndex);
                                m_isModified = true;
                            } else {
                                addVelocityPick(v, t, vx, vy);
                                m_isModified = true;
                            }

                            break;
                        case java.awt.event.MouseEvent.BUTTON3:
                            break;
                    }
                }
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                if (m_ready) {
                    m_cursorOverCVS = false;
                    updateVelocityPicksCurve(0, 0);
                    nmoCurve.setVisible(false);

                    repaint();
                }
            }

            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                if (m_ready) {
                    m_cursorOverCVS = true;
                    nmoCurve.setVisible(true);

                    repaint();
                }
            }
        });

        gfxPanelCVS.addMouseMotionListener(new java.awt.event.MouseAdapter() {

            @Override
            public void mouseMoved(java.awt.event.MouseEvent e) {
                if (m_ready) {
                    float v = gfxPanelCVS.getMouseLocation().fx;
                    float t = gfxPanelCVS.getMouseLocation().fy;
//                    System.out.println(v+" "+t);
                    updateNMOCurve(v, t, 0.0f, 0.0f);
                    updateVelocityPicksCurve(v, t);

                    gfxPanelCDP.repaint();
                }
            }
        });

        fileNew.setVisible(false);
        fileOpen.setVisible(false);
        fileClose.setVisible(false);
        fileSave.setVisible(false);
        fileExportPicks.setVisible(false);
        jSeparator3.setVisible(false);
        jSeparator4.setVisible(false);
        jSeparator1.setVisible(false);
        
        dlgOpt = new botoseis.ivelan.dialogs.VelanDlg(this, true);


    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        jSplitPane2 = new javax.swing.JSplitPane();
        jPanel3 = new javax.swing.JPanel();
        panelB = new javax.swing.JPanel();
        panelCDP = new javax.swing.JPanel();
        panelA = new javax.swing.JPanel();
        colorbarPanel1 = new javax.swing.JPanel();
        labelCDP = new javax.swing.JLabel();
        jPanel6 = new javax.swing.JPanel();
        panelC = new javax.swing.JPanel();
        panelSemblance = new javax.swing.JPanel();
        colorbarPanel = new javax.swing.JPanel();
        jPanel5 = new javax.swing.JPanel();
        panelD = new javax.swing.JPanel();
        panelCVS = new javax.swing.JPanel();
        colorbarPanel2 = new javax.swing.JPanel();
        jLabel2 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        jToolBar1 = new javax.swing.JToolBar();
        btnNext = new javax.swing.JButton();
        btnPrev = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        btnNMO = new javax.swing.JButton();
        btnCDP = new javax.swing.JButton();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new javax.swing.JMenu();
        fileNew = new javax.swing.JMenuItem();
        fileOpen = new javax.swing.JMenuItem();
        fileClose = new javax.swing.JMenuItem();
        jSeparator3 = new javax.swing.JSeparator();
        fileSave = new javax.swing.JMenuItem();
        jSeparator4 = new javax.swing.JSeparator();
        fileExportPicks = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JSeparator();
        fileExit = new javax.swing.JMenuItem();
        jMenu2 = new javax.swing.JMenu();
        optionsVelan = new javax.swing.JMenuItem();
        menuShowIntervalVel = new javax.swing.JCheckBoxMenuItem();
        jMenu3 = new javax.swing.JMenu();
        aboutMenu = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle("BotoSeis - iVelan");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
            public void windowOpened(java.awt.event.WindowEvent evt) {
                formWindowOpened(evt);
            }
        });

        jSplitPane1.setDividerLocation(500);

        jSplitPane2.setDividerLocation(200);

        panelB.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        panelB.setLayout(new javax.swing.BoxLayout(panelB, javax.swing.BoxLayout.LINE_AXIS));

        panelCDP.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        panelCDP.setLayout(new javax.swing.BoxLayout(panelCDP, javax.swing.BoxLayout.LINE_AXIS));

        panelA.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        panelA.setLayout(new javax.swing.BoxLayout(panelA, javax.swing.BoxLayout.LINE_AXIS));

        colorbarPanel1.setBackground(new java.awt.Color(1, 1, 127));
        colorbarPanel1.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        labelCDP.setForeground(java.awt.Color.white);
        labelCDP.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        labelCDP.setText("CDP #");

        javax.swing.GroupLayout colorbarPanel1Layout = new javax.swing.GroupLayout(colorbarPanel1);
        colorbarPanel1.setLayout(colorbarPanel1Layout);
        colorbarPanel1Layout.setHorizontalGroup(
            colorbarPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(labelCDP, javax.swing.GroupLayout.DEFAULT_SIZE, 120, Short.MAX_VALUE)
        );
        colorbarPanel1Layout.setVerticalGroup(
            colorbarPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(labelCDP, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 19, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(panelA, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(colorbarPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(panelCDP, javax.swing.GroupLayout.DEFAULT_SIZE, 124, Short.MAX_VALUE)
                    .addComponent(panelB, javax.swing.GroupLayout.DEFAULT_SIZE, 124, Short.MAX_VALUE)))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel3Layout.createSequentialGroup()
                .addComponent(panelB, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(panelA, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 320, Short.MAX_VALUE)
                    .addComponent(panelCDP, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 320, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(colorbarPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jSplitPane2.setLeftComponent(jPanel3);

        panelC.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        panelC.setLayout(new javax.swing.BoxLayout(panelC, javax.swing.BoxLayout.LINE_AXIS));

        panelSemblance.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        panelSemblance.setLayout(new javax.swing.BoxLayout(panelSemblance, javax.swing.BoxLayout.LINE_AXIS));

        colorbarPanel.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        colorbarPanel.setLayout(new javax.swing.BoxLayout(colorbarPanel, javax.swing.BoxLayout.LINE_AXIS));

        javax.swing.GroupLayout jPanel6Layout = new javax.swing.GroupLayout(jPanel6);
        jPanel6.setLayout(jPanel6Layout);
        jPanel6Layout.setHorizontalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelC, javax.swing.GroupLayout.DEFAULT_SIZE, 294, Short.MAX_VALUE)
            .addComponent(panelSemblance, javax.swing.GroupLayout.DEFAULT_SIZE, 294, Short.MAX_VALUE)
            .addComponent(colorbarPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 294, Short.MAX_VALUE)
        );
        jPanel6Layout.setVerticalGroup(
            jPanel6Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel6Layout.createSequentialGroup()
                .addComponent(panelC, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelSemblance, javax.swing.GroupLayout.DEFAULT_SIZE, 320, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(colorbarPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jSplitPane2.setRightComponent(jPanel6);

        jSplitPane1.setLeftComponent(jSplitPane2);

        panelD.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        panelD.setLayout(new javax.swing.BoxLayout(panelD, javax.swing.BoxLayout.LINE_AXIS));

        panelCVS.setBorder(javax.swing.BorderFactory.createEtchedBorder());
        panelCVS.setLayout(new javax.swing.BoxLayout(panelCVS, javax.swing.BoxLayout.LINE_AXIS));

        colorbarPanel2.setBorder(javax.swing.BorderFactory.createEtchedBorder());

        jLabel2.setForeground(java.awt.Color.white);
        jLabel2.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel2.setText("CVS");

        javax.swing.GroupLayout colorbarPanel2Layout = new javax.swing.GroupLayout(colorbarPanel2);
        colorbarPanel2.setLayout(colorbarPanel2Layout);
        colorbarPanel2Layout.setHorizontalGroup(
            colorbarPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 268, Short.MAX_VALUE)
        );
        colorbarPanel2Layout.setVerticalGroup(
            colorbarPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, 19, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel5Layout = new javax.swing.GroupLayout(jPanel5);
        jPanel5.setLayout(jPanel5Layout);
        jPanel5Layout.setHorizontalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(panelD, javax.swing.GroupLayout.DEFAULT_SIZE, 272, Short.MAX_VALUE)
            .addComponent(colorbarPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addComponent(panelCVS, javax.swing.GroupLayout.DEFAULT_SIZE, 272, Short.MAX_VALUE)
        );
        jPanel5Layout.setVerticalGroup(
            jPanel5Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel5Layout.createSequentialGroup()
                .addComponent(panelD, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(panelCVS, javax.swing.GroupLayout.DEFAULT_SIZE, 320, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(colorbarPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jSplitPane1.setRightComponent(jPanel5);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 822, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 17, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 834, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 20, Short.MAX_VALUE)
        );

        jToolBar1.setBorder(null);
        jToolBar1.setOrientation(javax.swing.SwingConstants.VERTICAL);
        jToolBar1.setRollover(true);

        btnNext.setIcon(new javax.swing.ImageIcon(getClass().getResource("/botoseis/pics/Forward24.gif"))); // NOI18N
        btnNext.setToolTipText("Go to next CDP");
        btnNext.setFocusable(false);
        btnNext.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnNext.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnNext.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNextActionPerformed(evt);
            }
        });
        jToolBar1.add(btnNext);

        btnPrev.setIcon(new javax.swing.ImageIcon(getClass().getResource("/botoseis/pics/Back24.gif"))); // NOI18N
        btnPrev.setToolTipText("Go to previous CDP");
        btnPrev.setFocusable(false);
        btnPrev.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnPrev.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnPrev.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnPrevActionPerformed(evt);
            }
        });
        jToolBar1.add(btnPrev);
        jToolBar1.add(jSeparator2);

        btnNMO.setIcon(new javax.swing.ImageIcon(getClass().getResource("/botoseis/pics/cdpnmo.gif"))); // NOI18N
        btnNMO.setToolTipText("Apply NMO");
        btnNMO.setFocusable(false);
        btnNMO.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnNMO.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnNMO.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnNMOActionPerformed(evt);
            }
        });
        jToolBar1.add(btnNMO);

        btnCDP.setIcon(new javax.swing.ImageIcon(getClass().getResource("/botoseis/pics/cdp.gif"))); // NOI18N
        btnCDP.setToolTipText("Show CDP");
        btnCDP.setFocusable(false);
        btnCDP.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        btnCDP.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        btnCDP.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                btnCDPActionPerformed(evt);
            }
        });
        jToolBar1.add(btnCDP);

        jMenu1.setMnemonic('F');
        jMenu1.setText("File");

        fileNew.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        fileNew.setText("New...");
        fileNew.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileNewActionPerformed(evt);
            }
        });
        jMenu1.add(fileNew);

        fileOpen.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        fileOpen.setText("Open...");
        fileOpen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileOpenActionPerformed(evt);
            }
        });
        jMenu1.add(fileOpen);

        fileClose.setText("Close");
        fileClose.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileCloseActionPerformed(evt);
            }
        });
        jMenu1.add(fileClose);
        jMenu1.add(jSeparator3);

        fileSave.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_DOWN_MASK));
        fileSave.setText("Save");
        fileSave.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileSaveActionPerformed(evt);
            }
        });
        jMenu1.add(fileSave);
        jMenu1.add(jSeparator4);

        fileExportPicks.setText("Export picks...");
        fileExportPicks.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileExportPicksActionPerformed(evt);
            }
        });
        jMenu1.add(fileExportPicks);
        jMenu1.add(jSeparator1);

        fileExit.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.ALT_DOWN_MASK));
        fileExit.setMnemonic('x');
        fileExit.setText("Exit");
        fileExit.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                fileExitActionPerformed(evt);
            }
        });
        jMenu1.add(fileExit);

        jMenuBar1.add(jMenu1);

        jMenu2.setMnemonic('O');
        jMenu2.setText("Options");

        optionsVelan.setText("Velocity analysis...");
        optionsVelan.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                optionsVelanActionPerformed(evt);
            }
        });
        jMenu2.add(optionsVelan);

        menuShowIntervalVel.setText("Show interval velocity");
        menuShowIntervalVel.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuShowIntervalVelActionPerformed(evt);
            }
        });
        jMenu2.add(menuShowIntervalVel);

        jMenuBar1.add(jMenu2);

        jMenu3.setMnemonic('H');
        jMenu3.setText("Help");

        aboutMenu.setMnemonic('A');
        aboutMenu.setText("About");
        aboutMenu.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutMenuActionPerformed(evt);
            }
        });
        jMenu3.add(aboutMenu);

        jMenuBar1.add(jMenu3);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, 38, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 778, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSplitPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jToolBar1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    private void fileExitActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileExitActionPerformed
        //int ret = javax.swing.JOptionPane.NO_OPTION;

        savePicksToFile(new java.io.File(m_picksFilePath));

        //if (m_isModified) {
        //    ret = javax.swing.JOptionPane.showConfirmDialog(this, "Actual work is modified. Save it?");
        //}

        //switch (ret) {
        //    case javax.swing.JOptionPane.YES_OPTION:
        //        save();
        close();
        //        break;
        //    case javax.swing.JOptionPane.NO_OPTION:
        //        close();
        //        break;
        //    default:
        //}

        System.exit(0);
}//GEN-LAST:event_fileExitActionPerformed

    /**
     * suIntVel
     * Converts stack velocities to interval velocities.
     *
     * Code extracted and adapted from "suintvel" program.
     *
     * Copyright (c) Colorado School of Mines, 2007.
     * All rights reserved.  
     * 
     * Adapted from SU sources. 
     * Williams Lima
     */
    private void suIntVel(int n, float[] vs, float[] t0, float[] h, float[] v) {
        float t1, t2;		/* temporaries for one-way times	*/
        float v1, v2;		/* temporaries for stacking v's		*/
        float dt;		/* temporary for t0/2 difference	*/
        float dvh;		/* temporary for v*h difference		*/

        /* Check that vs's and t0's are positive */
        /*for (int i = 0; i < n; i++) {
        if (vs[i] < 0.0)
        fprintf(stderr, "DrawingWindow::suIntVel: vs's must be positive: vs[%d] = %f", i, vs[i]);
        if (t0[i] < 0.0)
        fprintf(stderr, "DrawingWindow::suIntVel: t0's must be positive: t0[%d] = %f", i, t0[i]);
        }*/

        /* Compute h(i), v(i) */
        h[0] = 0.5f * vs[0] * t0[0];
        v[0] = vs[0];
        for (int i = 1; i < n; i++) {
            t2 = 0.5f * t0[i];
            t1 = 0.5f * t0[i - 1];
            v2 = vs[i];
            v1 = vs[i - 1];
            dt = t2 - t1;
            dvh = v2 * v2 * t2 - v1 * v1 * t1;
            h[i] = (float) Math.sqrt(dvh * dt);
            v[i] = (float) Math.sqrt(dvh / dt);
        }
    }

    private void updateIntervalVelocity() {

        int nv = m_currentCDPVelocityPicks.size();

        gfx.SVPoint2D[] pa = new gfx.SVPoint2D[nv];
        m_currentCDPVelocityPicks.toArray(pa);

        SVPoint2DComparator ac = new SVPoint2DComparator();

        java.util.Arrays.sort(pa, ac);

        float[] h = new float[nv];
        float[] v = new float[nv];

        float[] x = new float[nv];
        float[] y = new float[nv];

        float[] lm = gfxPanelSemblance.getAxisLimits();

        float ymin = lm[0];
        float ymax = lm[1];

        for (int i = 0; i < nv; i++) {
            x[i] = pa[i].fx;
            y[i] = pa[i].fy;
        }

        suIntVel(nv, x, y, h, v);

        float[] nx = new float[2 * nv + 1];
        float[] ny = new float[2 * nv + 1];

        int t = 0;
        for (int i = 0; i < nv; i++) {
            nx[t] = v[i];
            if (i == 0) {
                ny[t] = ymin;
            } else {
                ny[t] = y[i-1];
            }

            nx[t + 1] = v[i];
            ny[t + 1] = y[i];

            t += 2;
        }

        nx[t] = nx[t-1];
        ny[t] = ymax;

        intervalCurve.update(nx, ny);

    }

    private void updateNMOCurve(float v, float t, float anis1, float anis2) {
        int np = nmoCurve.getNumberOfPoint();        
        float[] lm = gfxPanelCDP.getAxisLimits();

        float[] x = new float[np];
        float[] y = new float[np];

        float xmin = lm[2];
        float xmax = lm[3];
        float ymin = lm[0];
        float ymax = lm[1];

        float dx = (xmax - xmin) / np;
        float dy = (ymax - ymin) / np;

        for (int i = 0; i < np; i++) {
            x[i] = xmin + i * dx;
            if (m_showingNMO) {
                y[i] = t;
            } else {
                y[i] = (float) (Math.sqrt(Math.pow(t, 2) +
                        (1 / Math.pow(v, 2)) * Math.pow(x[i], 2) +
                        (anis1 / (1 + anis2 * Math.pow(x[i], 2))) * Math.pow(x[i], 4)));
            }
        }

        nmoCurve.update(x, y);

    }

    private void updateVelocityPicksCurve(float v, float t) {
        if (m_currentCDPVelocityPicks.size() > 0) {
            float[] lm = gfxPanelCDP.getAxisLimits();

            float ymin = lm[0];
            float ymax = lm[1];

            java.util.Vector<gfx.SVPoint2D> picksList = new java.util.Vector<gfx.SVPoint2D>();

            gfx.SVPoint2D npick = null;
            for (int i = 0; i < m_currentCDPVelocityPicks.size(); i++) {
                npick = new gfx.SVPoint2D();
                npick.fx = m_currentCDPVelocityPicks.get(i).fx;
                npick.fy = m_currentCDPVelocityPicks.get(i).fy;
                picksList.add(npick);
            }

            if (m_cursorOverSemblancemap || m_cursorOverCVS) {
                npick = new gfx.SVPoint2D();

                npick.fx = v;
                npick.fy = t;
                picksList.add(npick);
            }

            // Sort velocity picks, increasing time
            gfx.SVPoint2D[] pa = new gfx.SVPoint2D[picksList.size()];
            picksList.toArray(pa);

            SVPoint2DComparator ac = new SVPoint2DComparator();

            java.util.Arrays.sort(pa, ac);

            int np = picksList.size();

            float[] x = new float[np + 2];
            float[] y = new float[np + 2];

            x[0] = pa[0].fx;
            y[0] = ymin;

            for (int i = 0; i < np; i++) {
                x[i + 1] = pa[i].fx;
                y[i + 1] = pa[i].fy;
            }

            x[np + 1] = x[np];
            y[np + 1] = ymax;

            picksCurve.update(x, y);

        } else {
            picksCurve.clear();
        }
        panelSemblance.repaint();
        panelCVS.repaint();

    }

private void btnNextActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNextActionPerformed
    if (m_ready) {
        m_curCDP += m_cdpInterval;

        if (m_curCDP == m_cdpMax) {
            btnNext.setEnabled(false);
        }

        workOnCDP(m_curCDP);
        applyGain();

        btnPrev.setEnabled(true);
    }
}//GEN-LAST:event_btnNextActionPerformed

private void formWindowOpened(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowOpened
    if (m_ready) {
        showCDP(m_curCDP);
        showSemblance(m_curCDP);
        showCVS(m_curCDP);
    }
}//GEN-LAST:event_formWindowOpened

private void aboutMenuActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutMenuActionPerformed
/*
    (new dialogs.AboutDlg(this, true)).setVisible(true);//GEN-LAST:event_aboutMenuActionPerformed
*/
    (new botoseis.ivelan.dialogs.AboutDlg(this, true)).setVisible(true);
}

private void fileNewActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileNewActionPerformed
    int ret = javax.swing.JOptionPane.NO_OPTION;//GEN-LAST:event_fileNewActionPerformed

        if (m_isModified) {
            ret = javax.swing.JOptionPane.showConfirmDialog(this, "Actual work is modified. Save it?");
        }

        switch (ret) {
            case javax.swing.JOptionPane.YES_OPTION:
                save();
                close();
                break;
            case javax.swing.JOptionPane.NO_OPTION:
                close();
                break;
            default:
        }

        if (ret != javax.swing.JOptionPane.CANCEL_OPTION) {
            ret = javax.swing.JOptionPane.showConfirmDialog(
                    this, m_panelNewProject, "Setup", javax.swing.JOptionPane.OK_CANCEL_OPTION,
                    javax.swing.JOptionPane.PLAIN_MESSAGE);
            if (ret == javax.swing.JOptionPane.OK_OPTION) {
                m_inputFilePath = m_panelNewProject.inputFile.getText();
                m_picksFilePath = m_panelNewProject.picksFile.getText();
                m_cdpMin = Integer.parseInt(m_panelNewProject.cdpMin.getText());
                m_cdpMax = Integer.parseInt(m_panelNewProject.cdpMax.getText());
                m_cdpInterval = Integer.parseInt(m_panelNewProject.cdpInterval.getText());
                m_tmin = Float.parseFloat(m_panelNewProject.timeMin.getText());
                m_tmax = Float.parseFloat(m_panelNewProject.timeMax.getText());
                m_vmin = Float.parseFloat(m_panelNewProject.vMin.getText());
                m_vmax = Float.parseFloat(m_panelNewProject.vMax.getText());
                m_dv = (m_vmax - m_vmin) / Integer.parseInt(m_panelNewProject.numV.getText());
                m_cvsWindow = Integer.parseInt(m_panelNewProject.cvsWindow.getText());
                m_cvsNumPanels = Integer.parseInt(m_panelNewProject.cvsNumPanels.getText());
                m_cvsvmin = m_vmin;
                m_cvsvmax = m_vmax;

                m_ready = true;

                setupProject();
                workOnCDP(m_cdpMin);


            } else {
                m_ready = false;
            }
        }
    }

private void fileExportPicksActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileExportPicksActionPerformed
    javax.swing.JFileChooser jfc = new javax.swing.JFileChooser();

    int ret = jfc.showOpenDialog(this);

    if (ret == javax.swing.JFileChooser.APPROVE_OPTION) {
        java.io.File outF = new java.io.File(jfc.getSelectedFile().toString());
        savePicksToFile(outF);
    }

}//GEN-LAST:event_fileExportPicksActionPerformed

private void fileCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileCloseActionPerformed

    int ret = javax.swing.JOptionPane.NO_OPTION;

    if (m_isModified) {
        ret = javax.swing.JOptionPane.showConfirmDialog(this, "Actual work is modified. Save it?");
    }

    switch (ret) {
        case javax.swing.JOptionPane.YES_OPTION:
            save();
            close();
            break;
        case javax.swing.JOptionPane.NO_OPTION:
            close();
            break;
        default:
    }


    gfxPanelCDP.repaint();
    gfxPanelSemblance.repaint();
    gfxPanelCVS.repaint();

}//GEN-LAST:event_fileCloseActionPerformed

private void btnPrevActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnPrevActionPerformed
    if (m_ready) {
        m_curCDP -= m_cdpInterval;

        if (m_curCDP == m_cdpMin) {
            btnPrev.setEnabled(false);
        }

        workOnCDP(m_curCDP);
        applyGain();

        btnNext.setEnabled(true);
    }
}//GEN-LAST:event_btnPrevActionPerformed

    private void workOnCDP(int cdp) {
        clearTemps();

        m_curCDP = cdp;

        showCDP(cdp);
        showSemblance(cdp);
        showCVS(cdp);

        labelCDP.setText(String.format("CDP # %d", m_curCDP));
        int idx = (cdp - m_cdpMin) / m_cdpInterval;

        m_currentCDPVelocityPicks = m_velocityPicks.get(idx);

        updateVelocityPicksCurve(0, 0);

        panelCDP.repaint();
        panelSemblance.repaint();
        panelCVS.repaint();
    }

private void fileSaveActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileSaveActionPerformed
    save();
}//GEN-LAST:event_fileSaveActionPerformed

private void fileOpenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_fileOpenActionPerformed
    int ret = javax.swing.JOptionPane.NO_OPTION;

    if (m_isModified) {
        ret = javax.swing.JOptionPane.showConfirmDialog(this, "Actual work is modified. Save it?");
    }

    switch (ret) {
        case javax.swing.JOptionPane.YES_OPTION:
            save();
            close();
            break;
        case javax.swing.JOptionPane.NO_OPTION:
            close();
            break;
        default:
    }

    if (ret != javax.swing.JOptionPane.CANCEL_OPTION) {
        javax.swing.JFileChooser jfc = new javax.swing.JFileChooser();

        ret = jfc.showOpenDialog(this);

        if (ret == javax.swing.JFileChooser.APPROVE_OPTION) {
            m_projectFolder = jfc.getSelectedFile().getParentFile();
            m_projectFile = jfc.getSelectedFile();
            open();
        }
    }
}//GEN-LAST:event_fileOpenActionPerformed

private void btnNMOActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnNMOActionPerformed
    if (m_ready) {
        showNMO(m_curCDP);
        applyGain();
        gfxPanelCDP.repaint();
    }
}//GEN-LAST:event_btnNMOActionPerformed

private void btnCDPActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCDPActionPerformed
    if (m_ready) {
        showCDP(m_curCDP);
        applyGain();
        gfxPanelCDP.repaint();
    }
}//GEN-LAST:event_btnCDPActionPerformed

private void menuShowIntervalVelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuShowIntervalVelActionPerformed
    intervalCurve.setVisible(menuShowIntervalVel.isSelected());
    updateIntervalVelocity();
    gfxPanelSemblance.repaint();
}//GEN-LAST:event_menuShowIntervalVelActionPerformed

private void optionsVelanActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_optionsVelanActionPerformed

    dlgOpt.setVMax(m_vmax);
    dlgOpt.setVMin(m_vmin);
    dlgOpt.setNV((int) ((m_vmax - m_vmin)/m_dv));
    
    dlgOpt.setVisible(true);

    int nv = dlgOpt.getNV();

    if (dlgOpt.isOK()) {
        float vmin = dlgOpt.getVMin();
        float vmax = dlgOpt.getVMax();

        m_vmin = vmin;
        m_vmax = vmax;

        m_dv = (m_vmax - m_vmin) / nv;

        showSemblance(m_curCDP);
        panelSemblance.repaint();

        applyGain();

    }
}//GEN-LAST:event_optionsVelanActionPerformed

private void applyGain() {
        Boolean panel = dlgOpt.getBooleanValue("panel");
        Float tpow = dlgOpt.getFloatValue("tpow");
        Float epow = dlgOpt.getFloatValue("epow");
        Float gpow = dlgOpt.getFloatValue("gpow");
        Boolean agc = dlgOpt.getBooleanValue("agc");
        Boolean gagc = dlgOpt.getBooleanValue("gagc");
        Float wagc = dlgOpt.getFloatValue("wagc");
        Float trap = dlgOpt.getFloatValue("trap");
        Float clip = dlgOpt.getFloatValue("clip");
        Float qclip = dlgOpt.getFloatValue("gclip");
        Boolean qbal = dlgOpt.getBooleanValue("qbal");
        Boolean pbal = dlgOpt.getBooleanValue("pbal");
        Boolean mbal = dlgOpt.getBooleanValue("mbal");
        Boolean maxbal = dlgOpt.getBooleanValue("maxbal");
        Float scale = dlgOpt.getFloatValue("scale");
        Float norm = dlgOpt.getFloatValue("norm");
        Float bias = dlgOpt.getFloatValue("bias");
        Boolean jon = dlgOpt.getBooleanValue("jon");

        Vector<gfx.SVActor> actors_cdp = gfxPanelCDP.getActors();
        for (int i = 0; i < actors_cdp.size(); i++) {
            gfx.SVWiggle actor = (gfx.SVWiggle) actors_cdp.get(i);
            if (actor != null) {
                actor.applyGain(panel, tpow, epow, gpow, agc,
                        gagc, wagc, trap, clip, qclip,
                        qbal, pbal, mbal, maxbal, scale,
                        norm, bias, jon);
            }
        }

        Vector<gfx.SVActor> actors_cvs = gfxPanelCVS.getActors();
        for (int i = 0; i < actors_cvs.size(); i++) {
            gfx.SVColorScale actor = (gfx.SVColorScale) actors_cvs.get(i);
            if (actor != null) {
                actor.applyGain(panel, tpow, epow, gpow, agc,
                        gagc, wagc, trap, clip, qclip,
                        qbal, pbal, mbal, maxbal, scale,
                        norm, bias, jon);
            }
        }
    }

private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
    savePicksToFile(new java.io.File(m_picksFilePath));
}//GEN-LAST:event_formWindowClosing

    private void clearTemps() {
        String cdpF = String.format(m_tmpDir + "/" + "cdp-%d.su", m_curCDP);
        java.io.File f = new java.io.File(cdpF);
        f.delete();

        String smapF = String.format(m_tmpDir + "/" + "smap-%d.su", m_curCDP);
        f = new java.io.File(smapF);
        f.delete();

        String nmoF = String.format(m_tmpDir + "/" + "nmo-%d.su", m_curCDP);
        f = new java.io.File(nmoF);
        f.delete();
    }

    private void addVelocityPick(float v, float t, int vx, int vy) {
        gfx.SVPoint2D p = new gfx.SVPoint2D();

        p.fx = v;
        p.fy = t;
        p.ix = vx;
        p.iy = vy;

        m_currentCDPVelocityPicks.add(p);
        updateVelocityPicksCurve(0, 0);

    }

    private void showNMO(int cdp) {
        java.util.Vector<String> cmd = new java.util.Vector<String>();

        String cdpF = String.format("cdp-%d.su", cdp);

        cmd.add("bvsuwind.sh");
        cmd.add(m_inputFilePath);
        cmd.add(cdpF);
        cmd.add(String.format("min=%d", cdp));
        cmd.add(String.format("max=%d", cdp));

        try {

            String[] sa = new String[cmd.size()];
            cmd.toArray(sa);

            Process p = Runtime.getRuntime().exec(sa);
            showProcessLog(p);
            p.waitFor();

            cmd.clear();

            cmd.add("bvsunmo.sh");
            cmd.add(cdpF);

            String nmoF = String.format("nmo-%d.su", cdp);
            cmd.add(nmoF);

            // Sort velocity picks, increasing time
            gfx.SVPoint2D[] pa = new gfx.SVPoint2D[m_currentCDPVelocityPicks.size()];
            m_currentCDPVelocityPicks.toArray(pa);

            SVPoint2DComparator ac = new SVPoint2DComparator();

            java.util.Arrays.sort(pa, ac);

            String tnmo = "tnmo=";
            String vnmo = "vnmo=";

            int i;
            tnmo += String.format("%.4f,", 0.0);
            vnmo += String.format("%.4f,", pa[0].fx);

            for (i = 0; i < pa.length; i++) {
                tnmo += String.format("%.4f,", pa[i].fy);
                vnmo += String.format("%.4f,", pa[i].fx);
            }

            float[] lm = gfxPanelCDP.getAxisLimits();

            float tmax = lm[1];

            tnmo += String.format("%.4f", tmax);
            vnmo += String.format("%.4f", pa[pa.length - 1].fx);

            cmd.add(tnmo);
            cmd.add(vnmo);

            sa = new String[cmd.size()];
            cmd.toArray(sa);

            p = Runtime.getRuntime().exec(sa);

            p.waitFor();

            usrdata.SUSection sc = new usrdata.SUSection();
            sc.formatFromFile(nmoF);
            sc.readFromFile(nmoF);

            int n1 = sc.getN1();
            int n2 = sc.getN2();
            float f1 = sc.getF1();
            float f2 = sc.getF2();
            float d1 = sc.getD1();
            float d2 = sc.getD2();

            gfxPanelCDP.setAxesLimits(f1, f1 + n1 * d1, f2, f2 + n2 * d2);

            gfx.SVWiggle wgActor = new gfx.SVWiggle();
            wgActor.setData(sc.getData(), n1, f1, d1, n2, f2, d2);

            gfxPanelCDP.removeAllActors();

            gfxPanelCDP.addActor(wgActor);

            gfxPanelCDP.repaint();

            // Clear temporary files
            java.io.File f = new java.io.File(nmoF);
            f.delete();

        } catch (Exception e) {
        }


    }

    private void showCDP(int cdp) {
        labelCDP.setText(String.format("CDP # %d", m_curCDP));
        java.util.Vector<String> cmd = new java.util.Vector<String>();

        String outF = String.format(m_tmpDir + "/" + "cdp-%d.su", cdp);

        cmd.add("bvsuwind.sh");
        cmd.add(m_inputFilePath);
        cmd.add(outF);
        cmd.add(String.format("min=%d", cdp));
        cmd.add(String.format("max=%d", cdp));

        try {

            String[] sa = new String[cmd.size()];
            cmd.toArray(sa);

            String exec = "";
            for (int i = 0; i < sa.length; i++) {
                exec += sa[i]+"  ";
            }
            Process p = Runtime.getRuntime().exec(sa, null, new java.io.File(m_tmpDir));
            System.out.println(exec);
//            Process p = Runtime.getRuntime().exec();
            showProcessLog(p);
            p.waitFor();

            usrdata.SUSection sc = new usrdata.SUSection();
            sc.formatFromFile(outF);
            sc.readFromFile(outF);
            
            int n1 = sc.getN1();
            int n2 = sc.getN2();
            float f1 = sc.getF1();
            float f2 = sc.getF2();
            float d1 = sc.getD1();
            float d2 = sc.getD2();

            gfxPanelCDP.setAxesLimits(f1, f1 + n1 * d1, f2, f2 + n2 * d2);
            m_timeAxis.setLimits(f1, f1 + n1 * d1);
            m_cdpOffsetAxis.setLimits(f2, f2 + n2 * d2);

            gfx.SVWiggle wgActor = new gfx.SVWiggle();
            wgActor.setData(sc.getData(), n1, f1, d1, n2, f2, d2);

            gfxPanelCDP.removeAllActors();

            gfxPanelCDP.addActor(wgActor);

            nmoCurve.clear();

            float[] x = new float[n2];
            float[] y = new float[n2];

            for (int i = 0; i < n2; i++) {
                x[i] = f2 + i * d2;
                y[i] = 0.0f;
            }

            nmoCurve.update(x, y);

        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, e.toString());
        }

    }

    private void showSemblance(int cdp) {

        float dv = m_dv;
        float fv = m_vmin;
        int nv = (int) ((m_vmax - m_vmin) / m_dv);

        float tpow = 0.0f;

        String filter = "1,10,80,100";
        String amps = "0,1,1,0";

        java.util.Vector<String> cmd = new java.util.Vector<String>();

        String cdpF = String.format(m_tmpDir + "/" + "cdp-%d.su", cdp);
//        String cdpF = String.format(m_tmpDir + "/" + "cdpsPraVelan.su");
//        String smapF = String.format(m_tmpDir + "/" + "cdpsPraVelan2.su", cdp);
        String smapF = String.format(m_tmpDir + "/" + "smap-%d.su", cdp);

        cmd.add("bvsuvelan.sh");
        cmd.add(cdpF);
        cmd.add(smapF);
        cmd.add(String.format(Locale.ENGLISH,"%.2f", tpow));
        cmd.add(filter);
        cmd.add(amps);
        cmd.add(String.format("%d", nv));
        cmd.add(String.format("%.0f", dv));
        cmd.add(String.format("%.0f", fv));


        try {
            String[] sa = new String[cmd.size()];
            cmd.toArray(sa);

            String exec = "";
            for (int i = 0; i < sa.length; i++) {
                exec += sa[i]+"  ";
            }
            Process p = Runtime.getRuntime().exec(sa, null, new java.io.File(m_tmpDir));
            System.out.println(exec);
//            Process p = Runtime.getRuntime().exec(exec);
            showProcessLog(p);
            p.waitFor();

            usrdata.SUSection sc = new usrdata.SUSection();
            sc.formatFromFile(smapF);
            sc.readFromFile(smapF);

            int n1 = sc.getN1();
            int n2 = sc.getN2();
            float f1 = sc.getF1();
            float f2 = sc.getF2();
            float d1 = sc.getD1();
            float d2 = sc.getD2();          
            gfxPanelSemblance.setAxesLimits(f1, f1 + n1 * d1, fv, fv + nv * dv);         

            gfx.SVColorScale csActor = new gfx.SVColorScale(3, gfx.SVColorScale.LSBFirst);
            csActor.setData(sc.getData(), n1, f1, d1, n2, fv, dv);

            m_velocityAxis.setLimits(f2, f2 + n2 * d2);

            m_gfxPanelColorbar = new GfxPanelColorbar(csActor, GfxPanelColorbar.HORIZONTAL);
            colorbarPanel.removeAll();
            colorbarPanel.add(m_gfxPanelColorbar);

            gfxPanelSemblance.removeAllActors();

            gfxPanelSemblance.addActor(csActor);


        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, e.toString());
            e.printStackTrace();
        }

        // Clear temporary files
        java.io.File f = new java.io.File(smapF);
//        f.delete();
    }

    private void showCVS(int cdp) {
        try {
            java.util.Vector<String> cmd = new java.util.Vector<String>();

            String cvsF = String.format(m_tmpDir + "/" + "cvs-%d.su", cdp);

            usrdata.SUSection sc = new usrdata.SUSection();
            java.io.File inpF = new java.io.File(m_inputFilePath);
            java.io.FileInputStream ifS = new java.io.FileInputStream(inpF);

            sc.formatFromFile(m_inputFilePath);
            sc.readFromFile(ifS, 2);

            int[] cdps = sc.getListOfCDPs();

            int dcdp = cdps[1] - cdps[0];

            cmd.add("bvcvs.sh");
            cmd.add(m_inputFilePath);
            cmd.add(cvsF);
            cmd.add(String.format("%d", cdp));
            cmd.add(String.format("%d", dcdp));
            cmd.add(String.format("%d", m_cvsWindow));
            cmd.add(String.format("%d", m_cvsNumPanels));
            cmd.add(String.format("%.0f", m_cvsvmin));
            cmd.add(String.format("%.0f", m_cvsvmax));

            String[] sa = new String[cmd.size()];
            cmd.toArray(sa);

             String exec = "";
            for (int i = 0; i < sa.length; i++) {
                exec += sa[i]+"  ";
            }


            System.out.println(exec);
//            Process p = Runtime.getRuntime().exec(exec);
            Process p = Runtime.getRuntime().exec(sa, null, new java.io.File(m_tmpDir));
            showProcessLog(p);

            p.waitFor();

            sc = new usrdata.SUSection();
            sc.formatFromFile(cvsF);
            sc.readFromFile(cvsF);

            int n1 = sc.getN1();
            int n2 = sc.getN2();
            float f1 = sc.getF1();
            float f2 = sc.getF2();
            float d1 = sc.getD1();
            float d2 = sc.getD2();

            float a = ((m_cvsvmax - m_cvsvmin) / m_cvsNumPanels);
            d2 = ((m_cvsvmax + a / 2) - (m_cvsvmin - a / 2)) / n2;

            f2 = f2 - (m_cvsvmax - m_cvsvmin) / (2 * m_cvsNumPanels);

            m_cvsVelocityAxis.setLimits(f2, f2 + n2 * d2);
            gfxPanelCVS.setAxesLimits(f1, f1 + n1 * d1, f2, f2 + n2 * d2);

            gfx.SVColorScale csActor = new gfx.SVColorScale(3, gfx.SVColorScale.LSBFirst);
            csActor.setData(sc.getData(), n1, f1, d1, n2, f2, d2);
            csActor.setRGBColormap(0);

            gfxPanelCVS.removeAllActors();

            gfxPanelCVS.addActor(csActor);

            // Clear temporary files
            java.io.File f = new java.io.File(cvsF);
            f.delete();
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, e.toString());
            e.printStackTrace();
        }


    }

    private void showProcessLog(Process p) throws IOException {
        int a =0;
//        while( (a = p.getErrorStream().read()) >= 0){
////            System.out.print((char)a);
//        }
    }

    private class SVPoint2DComparator implements java.util.Comparator<gfx.SVPoint2D> {

        public int compare(SVPoint2D o1, SVPoint2D o2) {
            if (o1.fy == o2.fy) {
                return 0;
            } else if (o1.fy > o2.fy) {
                return 1;
            } else {
                return -1;
            }
        }
    }

    private void parseCommandLine(String args[]) {
        for (int i = 0; i < args.length; i++) {
            String[] key = args[i].split("=");
            if ("cdpmin".equalsIgnoreCase(key[0])) {
                m_cdpMin = Integer.parseInt(key[1]);
            } else if ("cdpmax".equalsIgnoreCase(key[0])) {
                m_cdpMax = Integer.parseInt(key[1]);
            } else if ("cdpint".equalsIgnoreCase(key[0])) {
                m_cdpInterval = Integer.parseInt(key[1]);
            } else if ("tmin".equalsIgnoreCase(key[0])) {
                m_tmin = Float.parseFloat(key[1]);
            } else if ("tmax".equalsIgnoreCase(key[0])) {
                m_tmax = Float.parseFloat(key[1]);
            } else if ("velpicks".equalsIgnoreCase(key[0])) {
                m_picksFilePath = key[1];
            } else if ("vmin".equalsIgnoreCase(key[0])) {
                m_vmin = Float.parseFloat(key[1]);
            } else if ("vmax".equalsIgnoreCase(key[0])) {
                m_vmax = Float.parseFloat(key[1]);
            } else if ("nv".equalsIgnoreCase(key[0])) {
                m_dv = (m_vmax - m_vmin) / Integer.parseInt(key[1]);
            } else if ("cvsw".equalsIgnoreCase(key[0])) {
                m_cvsWindow = Integer.parseInt(key[1]);
            } else if ("cvsnp".equalsIgnoreCase(key[0])) {
                m_cvsNumPanels = Integer.parseInt(key[1]);
            } else if ("in".equalsIgnoreCase(key[0])) {
                m_inputFilePath = key[1];
            }
        }

        m_cvsvmin = m_vmin;
        m_cvsvmax = m_vmax;

        m_ready = validateParameters();
        
        // set defaul values for the dialog
        if (dlgOpt != null) {
            dlgOpt.setVMinDefault(m_vmin);
            dlgOpt.setVMaxDefault(m_vmax);
            dlgOpt.setNVDefault((int) ((m_vmax-m_vmin)/m_dv));
            dlgOpt.restoreToDefault();
        }

        if (m_ready) {
            setupProject();
            workOnCDP(m_cdpMin);
        }
    }

    private boolean validateParameters() {
        boolean ret = true;

        if (m_inputFilePath.isEmpty()) {
            ret = false;
        }
        if (m_picksFilePath.isEmpty()) {
            m_picksFilePath = "picks.txt";
        }
        if (m_cdpMin == m_cdpMax) {
            ret = false;
        }
        if (m_cdpInterval == 0) {
            ret = false;
        }
        if (m_tmin == m_tmax) {
            ret = false;
        }
        if (m_vmin == m_vmax) {
            ret = false;
        }
        if (m_dv == 0) {
            ret = false;
        }
        if (m_cvsWindow == 0) {
            ret = false;
        }
        if (m_cvsNumPanels == 0) {
            ret = false;
        }

        return ret;
    }

    private void setupProject() {
        m_velocityPicks.clear();
        nmoCurve.clear();
        picksCurve.clear();
        intervalCurve.clear();

        Vector<gfx.SVPoint2D> cdpVelocityPicks;
        for (int cdp = m_cdpMin; cdp <= m_cdpMax; cdp += m_cdpInterval) {
            m_velocityPicks.add(new Vector<gfx.SVPoint2D>());
        }

        m_curCDP = m_cdpMin;
        m_currentCDPVelocityPicks = m_velocityPicks.get(0);
        btnNext.setEnabled(true);
        btnPrev.setEnabled(false);

    }

    public void setCommandLine(String args[]) {
        parseCommandLine(args);
    }

    private void newProject() {
    }

    private void open() {
        try {

            java.util.Scanner sc = new java.util.Scanner(m_projectFile);

            java.util.Scanner sc2;

            // Skip header: 5 Lines plus blank line
            for (int i = 0; i < 6; i++) {
                sc.nextLine();
            }

            // Read user info group            
            sc.nextLine();

            sc2 = new java.util.Scanner(sc.nextLine());
            sc2.next();
            String userName = sc2.next();
            sc2 = new java.util.Scanner(sc.nextLine());
            sc2.next();
            String date = sc2.next();

            // Skip a blank line
            sc.nextLine();

            // Read input group
            sc.nextLine();
            sc2 = new java.util.Scanner(sc.nextLine());
            sc2.next();
            m_inputFilePath = sc2.next();

            // Read output group
            sc.nextLine();
            sc2 = new java.util.Scanner(sc.nextLine());
            sc2.next();
            m_picksFilePath = sc2.next();

            // Skip a blank line
            sc.nextLine();

            // Read CDPs paramters group        
            sc.nextLine();
            sc2 = new java.util.Scanner(sc.nextLine());
            m_cdpMin = sc2.nextInt();
            sc2 = new java.util.Scanner(sc.nextLine());
            m_cdpMax = sc2.nextInt();
            sc2 = new java.util.Scanner(sc.nextLine());
            m_cdpInterval = sc2.nextInt();
            sc2 = new java.util.Scanner(sc.nextLine());
            m_tmin = sc2.nextFloat();
            sc2 = new java.util.Scanner(sc.nextLine());
            m_tmax = sc2.nextFloat();

            // Skip a blank line
            sc.nextLine();

            // Read semblance parameters group
            sc.nextLine();
            sc2 = new java.util.Scanner(sc.nextLine());
            m_vmin = sc2.nextFloat();
            sc2 = new java.util.Scanner(sc.nextLine());
            m_vmax = sc2.nextFloat();
            sc2 = new java.util.Scanner(sc.nextLine());
            m_dv = sc2.nextFloat();

            // Skip a blank line
            sc.nextLine();

            // Read CVS parameters group
            sc.nextLine();
            sc2 = new java.util.Scanner(sc.nextLine());
            m_cvsWindow = sc2.nextInt();
            sc2 = new java.util.Scanner(sc.nextLine());
            m_cvsNumPanels = sc2.nextInt();
            m_cvsvmin = m_vmin;
            m_cvsvmax = m_vmax;

            sc.close();

            loadPicksFromFile(new java.io.File(m_picksFilePath));
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, e);
            e.printStackTrace();
        }
    }

    private void close() {
        gfxPanelCDP.removeAllActors();
        gfxPanelSemblance.removeAllActors();
        gfxPanelCVS.removeAllActors();

        nmoCurve.clear();
        picksCurve.clear();
        intervalCurve.clear();

        m_projectFile = null;
        m_inputFilePath = "";
        m_picksFilePath = "";

        m_cdpMin = 0;
        m_cdpMax = 0;
        m_cdpInterval = 0;
        m_tmin = 0.0f;
        m_tmax = 0.0f;
        m_vmin = 0.0f;
        m_vmax = 0.0f;
        m_dv = 0.0f;
        m_cvsWindow = 0;
        m_cvsNumPanels = 0;
        m_cvsvmin = 0.0f;
        m_cvsvmax = 0.0f;
        m_isModified = false;
        m_ready = false;
    }

    private void save() {
        try {
            if (m_projectFile == null) {
                javax.swing.JFileChooser jfc = new javax.swing.JFileChooser();

                int ret = jfc.showSaveDialog(this);

                if (ret == javax.swing.JFileChooser.APPROVE_OPTION) {
                    m_projectFile = new java.io.File(jfc.getSelectedFile().toString());
                }
            }

            if (m_projectFile != null) {
                java.io.PrintWriter pw = new java.io.PrintWriter(m_projectFile);

                // Write header
                pw.println("/* This file was automatically generated by BotoSeis iVelan module. Do not edit.*/");
                pw.println("iVelan");
                pw.println("BotoSeis project: http://botoseis.sourceforge.net");
                pw.println("Federal University of Para, Brazil");
                pw.println("Institute of Geophysics");

                pw.print("");
                // User info
                pw.println("[User info]");
                pw.println("userName=" + System.getenv("USER"));

                pw.println();

                // Input
                pw.println("[Input]");
                pw.println("dataFile=" + m_inputFilePath);

                pw.println();

                // Output
                pw.println("[Output]");
                pw.println("picksFile=" + m_picksFilePath);

                pw.println();

                // CDPs parameters
                pw.println("[CDPs parameters]");
                pw.println(String.format("cdpMin=%d", m_cdpMin));
                pw.println(String.format("cdpMax=%d", m_cdpMax));
                pw.println(String.format("cdpInterval=%d", m_cdpInterval));
                pw.println(String.format("timeMin=%.2f", m_tmin));
                pw.println(String.format("timeMax=%.2f", m_tmax));

                pw.println();

                // Semblance parameters
                pw.println("[Semblance parameters]");
                pw.println(String.format("vmin=%.2f", m_vmin));
                pw.println(String.format("vmax=%.2f", m_vmax));
                pw.println(String.format("dv=%.2f", m_dv));

                pw.println();

                // CVS parameters
                pw.println("[CVS parameters]");
                pw.println(String.format("window=%d", m_cvsWindow));
                pw.println(String.format("panels=%d", m_cvsNumPanels));

                pw.close();

                savePicksToFile(new java.io.File(m_picksFilePath));
            }
            m_isModified = false;
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, e);
            e.printStackTrace();
        }
    }

    private void loadPicksFromFile(java.io.File inF) {
        try {
            java.util.Scanner sc = new java.util.Scanner(inF);
            java.util.Scanner sc2;

            sc2 = new java.util.Scanner(sc.nextLine()).useDelimiter(",");
            int count = 0;
            while (sc2.hasNext()) {
                m_velocityPicks.add(new Vector<gfx.SVPoint2D>());
                count++;
            }

            Vector<gfx.SVPoint2D> picks;
            gfx.SVPoint2D p;
            for (int i = 0; i < count; i++) {
                sc2 = new java.util.Scanner(sc.nextLine()).useDelimiter(",");
                picks = m_velocityPicks.get(i);
                while (sc2.hasNext()) {
                    p = new gfx.SVPoint2D();
                    p.fy = sc2.nextFloat();
                    picks.add(p);
                }
                sc2 = new java.util.Scanner(sc.nextLine()).useDelimiter(",");
                for (int k = 0; i < picks.size(); k++) {
                    picks.get(k).fx = sc2.nextFloat();
                }
            }
        } catch (FileNotFoundException ex) {
            javax.swing.JOptionPane.showMessageDialog(this, ex);
            ex.printStackTrace();
        }
    }

    private void savePicksToFile(java.io.File outF) {
        try {
            java.io.PrintWriter pw = new java.io.PrintWriter(outF);

            int size = m_velocityPicks.size();
            String line = "cdp=";
            for (int i = 0; i < size; i++) {
                if (m_velocityPicks.get(i).size() > 0) {
                    line += String.format("%d", m_cdpMin + i * m_cdpInterval);
                    if (i < size - 1) {
                        line += ",";
                    }
                }
            }

            pw.println(line);

            Vector<gfx.SVPoint2D> picks;
            String tnmo = "tnmo=";
            String vnmo = "vnmo=";
            for (int i = 0; i < size; i++) {
                picks = m_velocityPicks.get(i);
                int n = picks.size();
                if (n > 0) {
                    tnmo = "tnmo=";
                    vnmo = "vnmo=";
                    for (int j = 0; j < n; j++) {
                        tnmo += String.format(Locale.ENGLISH,"%.2f", picks.get(j).fy);
                        if (j < n - 1) {
                            tnmo += ",";
                        }
                        vnmo += String.format(Locale.ENGLISH,"%.2f", picks.get(j).fx);
                        if (j < n - 1) {
                            vnmo += ",";
                        }
                    }

                    pw.println(tnmo);
                    pw.println(vnmo);
                }
            }
            pw.close();
        } catch (Exception e) {
            javax.swing.JOptionPane.showMessageDialog(this, e);
            e.printStackTrace();
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(final String args[]) {
        java.awt.EventQueue.invokeLater(new Runnable() {

            public void run() {               
                MainWindow wnd = new MainWindow();
                wnd.setCommandLine(args);
                wnd.setVisible(true);
            }
        });
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutMenu;
    private javax.swing.JButton btnCDP;
    private javax.swing.JButton btnNMO;
    private javax.swing.JButton btnNext;
    private javax.swing.JButton btnPrev;
    private javax.swing.JPanel colorbarPanel;
    private javax.swing.JPanel colorbarPanel1;
    private javax.swing.JPanel colorbarPanel2;
    private javax.swing.JMenuItem fileClose;
    private javax.swing.JMenuItem fileExit;
    private javax.swing.JMenuItem fileExportPicks;
    private javax.swing.JMenuItem fileNew;
    private javax.swing.JMenuItem fileOpen;
    private javax.swing.JMenuItem fileSave;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenu jMenu2;
    private javax.swing.JMenu jMenu3;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel5;
    private javax.swing.JPanel jPanel6;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JSplitPane jSplitPane2;
    private javax.swing.JToolBar jToolBar1;
    private javax.swing.JLabel labelCDP;
    private javax.swing.JCheckBoxMenuItem menuShowIntervalVel;
    private javax.swing.JMenuItem optionsVelan;
    private javax.swing.JPanel panelA;
    private javax.swing.JPanel panelB;
    private javax.swing.JPanel panelC;
    private javax.swing.JPanel panelCDP;
    private javax.swing.JPanel panelCVS;
    private javax.swing.JPanel panelD;
    private javax.swing.JPanel panelSemblance;
    // End of variables declaration//GEN-END:variables
    int m_curCDP = -1;
    boolean m_showingNMO = false;
    gfx.SVGraphicsPanel gfxPanelCDP = new gfx.SVGraphicsPanel();
    gfx.SVGraphicsPanel gfxPanelSemblance = new gfx.SVGraphicsPanel();
    gfx.SVGraphicsPanel gfxPanelCVS = new gfx.SVGraphicsPanel();
    gfx.SVXYPlot nmoCurve = new gfx.SVXYPlot();
    gfx.SVXYPlot picksCurve = new gfx.SVXYPlot();
    gfx.SVXYPlot intervalCurve = new gfx.SVXYPlot();
    Vector<Vector<gfx.SVPoint2D>> m_velocityPicks = new Vector<Vector<gfx.SVPoint2D>>();
    Vector<gfx.SVPoint2D> m_currentCDPVelocityPicks = null;
    GfxPanelColorbar m_gfxPanelColorbar = null;
    String m_inputFilePath = "";
    // CDP parameters
    int m_cdpMin = 0;
    private int m_cdpMax = 0;
    private int m_cdpInterval = 0;
    private float m_tmin = 0.0f;
    private float m_tmax = 0.0f;
    private String m_picksFilePath = "";
    // Semblance parameters
    private float m_vmin = 0.0f;
    private float m_vmax = 0.0f;
    private float m_dv = 0.0f;
    // CVS Parameters
    private int m_cvsWindow; // Number of CDPs for windowing.
    private int m_cvsNumPanels; // Number of CVS panels
    private float m_cvsvmin = 0.0f;
    private float m_cvsvmax = 0.0f;
    
    botoseis.ivelan.dialogs.VelanDlg dlgOpt;
    //
    gfx.SVAxis m_timeAxis;
    gfx.SVAxis m_cdpOffsetAxis;
    gfx.SVAxis m_velocityAxis;
    gfx.SVAxis m_cvsVelocityAxis;
    boolean m_cursorOverSemblancemap = false;
    boolean m_cursorOverCVS = false;
    //
    java.io.File m_projectFile = null;
    java.io.File m_projectFolder;
    String m_tmpDir = "/tmp";
    //
    boolean m_isModified = false;
    boolean m_ready = false;
    botoseis.ivelan.dialogs.PanelNewProject m_panelNewProject = new botoseis.ivelan.dialogs.PanelNewProject();
}
