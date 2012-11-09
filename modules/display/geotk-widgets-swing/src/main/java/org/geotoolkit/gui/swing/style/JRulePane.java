/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2007 - 2008, Open Source Geospatial Foundation (OSGeo)
 *    (C) 2008 - 2009, Johann Sorel
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package org.geotoolkit.gui.swing.style;

import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.BorderFactory;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import org.geotoolkit.cql.CQLException;
import org.geotoolkit.cql.JCQLTextPane;
import org.geotoolkit.gui.swing.filter.JCQLEditor;
import org.geotoolkit.gui.swing.resource.MessageBundle;
import org.geotoolkit.map.MapLayer;
import org.geotoolkit.style.MutableRule;
import org.opengis.filter.Filter;

/**
 *
 * @author Johann Sorel (Puzzle-GIS)
 * @module pending
 */
public class JRulePane extends StyleElementEditor<MutableRule> {

    private MutableRule rule = null;    
    private MapLayer layer = null;
    

    /** Creates new form JRulePanel */
    public JRulePane() {
        super(MutableRule.class);
        initComponents();
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new JPanel();
        but_edit = new JButton();
        jLabel3 = new JLabel();
        jsp_minscale = new JSpinner();
        jLabel4 = new JLabel();
        jsp_maxscale = new JSpinner();
        jck_else = new JCheckBox();
        guiCQL = new JCQLTextPane();
        jPanel2 = new JPanel();
        jtf_title = new JTextField();
        jLabel1 = new JLabel();
        jLabel6 = new JLabel();
        jtf_abstract = new JTextField();
        jLabel2 = new JLabel();
        jtf_name = new JTextField();

        setOpaque(false);

        jPanel1.setBorder(BorderFactory.createEtchedBorder());
        jPanel1.setOpaque(false);

        but_edit.setText(MessageBundle.getString("edit")); // NOI18N
        but_edit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                but_editActionPerformed(evt);
            }
        });

        jLabel3.setHorizontalAlignment(SwingConstants.RIGHT);
        jLabel3.setText(MessageBundle.getString("minscale")); // NOI18N

        jsp_minscale.setModel(new SpinnerNumberModel(Double.valueOf(0.0d), Double.valueOf(0.0d), null, Double.valueOf(1000.0d)));

        jLabel4.setHorizontalAlignment(SwingConstants.RIGHT);
        jLabel4.setText(MessageBundle.getString("maxscale")); // NOI18N

        jsp_maxscale.setModel(new SpinnerNumberModel(Double.valueOf(0.0d), Double.valueOf(0.0d), null, Double.valueOf(1000.0d)));

        jck_else.setText(MessageBundle.getString("else_filter")); // NOI18N

        GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(but_edit)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(guiCQL, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(Alignment.LEADING)
                            .addGroup(jPanel1Layout.createParallelGroup(Alignment.LEADING, false)
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addComponent(jLabel3)
                                    .addPreferredGap(ComponentPlacement.RELATED)
                                    .addComponent(jsp_minscale, GroupLayout.PREFERRED_SIZE, 146, GroupLayout.PREFERRED_SIZE))
                                .addGroup(jPanel1Layout.createSequentialGroup()
                                    .addComponent(jLabel4)
                                    .addPreferredGap(ComponentPlacement.RELATED)
                                    .addComponent(jsp_maxscale)))
                            .addComponent(jck_else))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );

        jPanel1Layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {jLabel3, jLabel4});

        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jck_else)
                .addGap(4, 4, 4)
                .addGroup(jPanel1Layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(jsp_minscale, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(jLabel4)
                    .addComponent(jsp_maxscale, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(but_edit)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(guiCQL, GroupLayout.DEFAULT_SIZE, 50, Short.MAX_VALUE))
                .addContainerGap())
        );

        jPanel2.setBorder(BorderFactory.createEtchedBorder());
        jPanel2.setOpaque(false);

        jLabel1.setFont(jLabel1.getFont().deriveFont(jLabel1.getFont().getStyle() | Font.BOLD));
        jLabel1.setHorizontalAlignment(SwingConstants.RIGHT);
        jLabel1.setText(MessageBundle.getString("title")); // NOI18N

        jLabel6.setText(MessageBundle.getString("abstract")); // NOI18N

        jLabel2.setHorizontalAlignment(SwingConstants.RIGHT);
        jLabel2.setText(MessageBundle.getString("name")); // NOI18N

        GroupLayout jPanel2Layout = new GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(jtf_name, GroupLayout.DEFAULT_SIZE, 310, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(jtf_title, GroupLayout.DEFAULT_SIZE, 317, Short.MAX_VALUE))
                    .addGroup(Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel6)
                        .addPreferredGap(ComponentPlacement.RELATED)
                        .addComponent(jtf_abstract, GroupLayout.DEFAULT_SIZE, 292, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(jtf_name, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jtf_title, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(Alignment.BASELINE)
                    .addComponent(jLabel6)
                    .addComponent(jtf_abstract, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(Alignment.TRAILING)
                    .addComponent(jPanel1, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(jPanel1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void but_editActionPerformed(final ActionEvent evt) {//GEN-FIRST:event_but_editActionPerformed
        if(rule == null){
            rule = getStyleFactory().rule();
        }
        
        try {
            final Filter nf = JCQLEditor.showDialog(layer, rule.getFilter());
            rule.setFilter(nf);
            guiCQL.setFilter(rule.getFilter());
        } catch (CQLException ex) {
            Logger.getLogger(JRulePane.class.getName()).log(Level.INFO, ex.getMessage(), ex);
        }
                
    }//GEN-LAST:event_but_editActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JButton but_edit;
    private JCQLTextPane guiCQL;
    private JLabel jLabel1;
    private JLabel jLabel2;
    private JLabel jLabel3;
    private JLabel jLabel4;
    private JLabel jLabel6;
    private JPanel jPanel1;
    private JPanel jPanel2;
    private JCheckBox jck_else;
    private JSpinner jsp_maxscale;
    private JSpinner jsp_minscale;
    private JTextField jtf_abstract;
    private JTextField jtf_name;
    private JTextField jtf_title;
    // End of variables declaration//GEN-END:variables

    @Override
    public void setLayer(final MapLayer layer) {
        this.layer = layer;
    }

    @Override
    public MapLayer getLayer() {
        return layer;
    }

    @Override
    public void parse(final MutableRule target) {
        this.rule = target;
        if (rule != null) {
            jtf_name.setText(rule.getName());
            jtf_title.setText(descriptionTitleText(rule.getDescription()));
            jtf_abstract.setText(descriptionAbstractText(rule.getDescription()));
            jck_else.setSelected(rule.isElseFilter());
            
            jsp_maxscale.setValue(rule.getMaxScaleDenominator());
            jsp_minscale.setValue(rule.getMinScaleDenominator());
            
            if (rule.getFilter() != null) {
                guiCQL.setFilter(rule.getFilter());
            }
        }
    }

    @Override
    public MutableRule create() {
        if(rule == null){
            rule = getStyleFactory().rule();
        }
        
        rule.setName(jtf_name.getText());
        rule.setDescription(getStyleFactory().description(
                jtf_title.getText(), 
                jtf_abstract.getText()));
        rule.setMinScaleDenominator( (Double)jsp_minscale.getValue() );
        rule.setMaxScaleDenominator( (Double)jsp_maxscale.getValue() );
        rule.setElseFilter(jck_else.isSelected());

        String str = guiCQL.getText().trim();

        if(!str.isEmpty()){
            try {
                Filter f = guiCQL.getFilter();
                rule.setFilter(f);
            } catch (CQLException ex) {
                System.out.println("Invalide CQL expression : ignore it");
            }
        }
        
        return rule;
    }

    @Override
    public void apply() {
        create();
    }
    
}
