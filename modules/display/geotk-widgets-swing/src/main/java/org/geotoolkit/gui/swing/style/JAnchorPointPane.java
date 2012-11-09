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

import org.geotoolkit.gui.swing.resource.MessageBundle;
import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JLabel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import org.geotoolkit.map.MapLayer;

import org.opengis.style.AnchorPoint;

/**
 * Anchor point edition panel
 *
 * @author Johann Sorel
 * @module pending
 */
public class JAnchorPointPane extends StyleElementEditor<AnchorPoint>{

    private MapLayer layer = null;

    /** Creates new form JDisplacementPanel */
    public JAnchorPointPane() {
        super(AnchorPoint.class);
        initComponents();
        init();
    }

    private void init(){
        guiX.setModel(0d, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 1d);
        guiY.setModel(0d, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, 1d);
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void setLayer(final MapLayer layer){
        guiX.setLayer(layer);
        guiY.setLayer(layer);
        this.layer = layer;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public MapLayer getLayer(){
        return layer;
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public void parse(final AnchorPoint anchor) {
        if(anchor != null){
            guiX.parse(anchor.getAnchorPointX());
            guiY.parse(anchor.getAnchorPointY());
        }
    }

    /**
     * {@inheritDoc }
     */
    @Override
    public AnchorPoint create(){
        return getStyleFactory().anchorPoint(
                guiX.create(),
                guiY.create());
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new JLabel();
        jLabel2 = new JLabel();
        guiX = new JNumberExpressionPane();
        guiY = new JNumberExpressionPane();

        jLabel1.setHorizontalAlignment(SwingConstants.RIGHT);
        jLabel1.setText(MessageBundle.getString("x")); // NOI18N

        jLabel2.setHorizontalAlignment(SwingConstants.RIGHT);
        jLabel2.setText(MessageBundle.getString("y")); // NOI18N

        guiX.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                JAnchorPointPane.this.propertyChange(evt);
            }
        });

        guiY.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                JAnchorPointPane.this.propertyChange(evt);
            }
        });

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(guiX, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel2)
                .addPreferredGap(ComponentPlacement.RELATED)
                .addComponent(guiY, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
        );

        layout.linkSize(SwingConstants.HORIZONTAL, new Component[] {jLabel1, jLabel2});

        layout.setVerticalGroup(
            layout.createParallelGroup(Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(guiX, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(Alignment.LEADING)
                    .addComponent(guiY, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel2)))
        );

        layout.linkSize(SwingConstants.VERTICAL, new Component[] {guiX, jLabel1});

        layout.linkSize(SwingConstants.VERTICAL, new Component[] {guiY, jLabel2});

    }// </editor-fold>//GEN-END:initComponents

    private void propertyChange(PropertyChangeEvent evt) {//GEN-FIRST:event_propertyChange
        // TODO add your handling code here:
        if (PROPERTY_TARGET.equalsIgnoreCase(evt.getPropertyName())) {
            firePropertyChange(PROPERTY_TARGET, null, create());
        }
    }//GEN-LAST:event_propertyChange


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JNumberExpressionPane guiX;
    private JNumberExpressionPane guiY;
    private JLabel jLabel1;
    private JLabel jLabel2;
    // End of variables declaration//GEN-END:variables

}
