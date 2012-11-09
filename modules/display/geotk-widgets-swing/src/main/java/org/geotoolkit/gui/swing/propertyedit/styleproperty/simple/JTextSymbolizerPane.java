/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2012 Geomatys
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
package org.geotoolkit.gui.swing.propertyedit.styleproperty.simple;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.measure.unit.NonSI;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import org.geotoolkit.gui.swing.resource.MessageBundle;
import org.geotoolkit.gui.swing.style.JLabelPlacementPane;
import org.geotoolkit.gui.swing.style.JTextExpressionPane;
import org.geotoolkit.gui.swing.style.StyleElementEditor;
import org.geotoolkit.map.MapLayer;
import org.geotoolkit.style.StyleConstants;
import org.opengis.style.TextSymbolizer;

/**
 * TextSymbolizer editor
 *
 * @author Fabien Rétif (Geomatys)
 * @author Johann Sorel (Geomatys)
 */
public class JTextSymbolizerPane extends StyleElementEditor<TextSymbolizer> {

    private MapLayer layer = null;

    /**
     * Creates new form JTextSymbolizerPane
     */
    public JTextSymbolizerPane() {
        super(TextSymbolizer.class);
        initComponents();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void setLayer(final MapLayer layer) {
        this.layer = layer;
        guiLabel.setLayer(layer);
        guiFont.setLayer(layer);
        guiFill.setLayer(layer);
        guiHalo.setLayer(layer);
        guiLabelPlacement.setLayer(layer);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public MapLayer getLayer() {
        return layer;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void parse(final TextSymbolizer text) {

        if (text != null) {
            guiLabel.parse(text.getLabel());
            guiFont.parse(text.getFont());
            guiFill.parse(text.getFill());
            guiHalo.parse(text.getHalo());
            guiLabelPlacement.parse(text.getLabelPlacement());
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public TextSymbolizer create() {
        return getStyleFactory().textSymbolizer(
                "textSymbolizer",
                    (String)null,
                    StyleConstants.DEFAULT_DESCRIPTION,
                    NonSI.PIXEL,
                    guiLabel.create(),
                    guiFont.create(), 
                    guiLabelPlacement.create(),
                    guiHalo.create(),
                    guiFill.create());
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new ButtonGroup();
        jTabbedPane1 = new JTabbedPane();
        jPanel1 = new JPanel();
        jLabel5 = new JLabel();
        jLabel10 = new JLabel();
        guiLabel = new JTextExpressionPane();
        guiFont = new JFontPane();
        guiFill = new JFillControlPane();
        guiHalo = new JHaloPane();
        guiLabelPlacement = new JLabelPlacementPane();

        jLabel5.setText("Couleur :");

        jLabel10.setText(MessageBundle.getString("label")); // NOI18N

        guiLabel.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                JTextSymbolizerPane.this.propertyChange(evt);
            }
        });

        guiFont.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                JTextSymbolizerPane.this.propertyChange(evt);
            }
        });

        guiFill.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                JTextSymbolizerPane.this.propertyChange(evt);
            }
        });

        guiHalo.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                JTextSymbolizerPane.this.propertyChange(evt);
            }
        });

        GroupLayout jPanel1Layout = new GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(Alignment.LEADING)
                            .addComponent(guiFont, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel5)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(guiFill, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 500, Short.MAX_VALUE)))
                        .addContainerGap())
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(guiHalo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(jLabel10)
                                .addPreferredGap(ComponentPlacement.RELATED)
                                .addComponent(guiLabel, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                        .addGap(12, 12, 12))))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(jLabel10)
                    .addComponent(guiLabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGap(14, 14, 14)
                .addComponent(guiFont, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(jLabel5)
                    .addComponent(guiFill, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(guiHalo, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        jPanel1Layout.linkSize(SwingConstants.VERTICAL, new Component[] {guiFill, jLabel5});

        jPanel1Layout.linkSize(SwingConstants.VERTICAL, new Component[] {guiLabel, jLabel10});

        jTabbedPane1.addTab("Libellé, police et style", jPanel1);

        guiLabelPlacement.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                JTextSymbolizerPane.this.propertyChange(evt);
            }
        });
        jTabbedPane1.addTab("Position", guiLabelPlacement);

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addComponent(jTabbedPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addComponent(jTabbedPane1)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void propertyChange(PropertyChangeEvent evt) {//GEN-FIRST:event_propertyChange
        if (PROPERTY_TARGET.equalsIgnoreCase(evt.getPropertyName())) {
            firePropertyChange(PROPERTY_TARGET, null, create());
        }
    }//GEN-LAST:event_propertyChange
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private ButtonGroup buttonGroup1;
    private JFillControlPane guiFill;
    private JFontPane guiFont;
    private JHaloPane guiHalo;
    private JTextExpressionPane guiLabel;
    private JLabelPlacementPane guiLabelPlacement;
    private JLabel jLabel10;
    private JLabel jLabel5;
    private JPanel jPanel1;
    private JTabbedPane jTabbedPane1;
    // End of variables declaration//GEN-END:variables
}
