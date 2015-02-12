/*
 *    Geotoolkit - An Open Source Java GIS Toolkit
 *    http://www.geotoolkit.org
 *
 *    (C) 2014-2015, Geomatys
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

package org.geotoolkit.gui.javafx.filter;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;
import org.fxmisc.richtext.CodeArea;
import org.geotoolkit.cql.CQL;
import org.geotoolkit.cql.CQLException;
import org.geotoolkit.cql.CQLLexer;
import org.geotoolkit.cql.CQLParser;
import org.geotoolkit.data.FeatureCollection;
import org.geotoolkit.feature.type.FeatureType;
import org.geotoolkit.feature.type.PropertyDescriptor;
import org.geotoolkit.filter.function.FunctionFactory;
import org.geotoolkit.filter.function.Functions;
import org.geotoolkit.gui.javafx.util.FXOptionDialog;
import org.geotoolkit.internal.GeotkFX;
import org.geotoolkit.map.FeatureMapLayer;
import org.geotoolkit.map.MapLayer;
import org.opengis.filter.Filter;
import org.opengis.filter.expression.Expression;
import org.opengis.parameter.GeneralParameterDescriptor;
import org.opengis.parameter.ParameterDescriptorGroup;

/**
 * CQL editor
 * 
 * @author Johann Sorel (Geomatys)
 */
public class FXCQLEditor extends BorderPane {

    private static final Collection STYLE_DEFAULT = Collections.singleton("default");
    private static final Collection STYLE_COMMENT = Collections.singleton("comment");
    private static final Collection STYLE_LITERAL = Collections.singleton("literal");
    private static final Collection STYLE_FUNCTION = Collections.singleton("function");
    private static final Collection STYLE_PARENTHESE = Collections.singleton("parenthese");
    private static final Collection STYLE_OPERATOR = Collections.singleton("operator");
    private static final Collection STYLE_BINARY = Collections.singleton("binary");
    private static final Collection STYLE_PROPERTY = Collections.singleton("property");
    private static final Collection STYLE_ERROR = Collections.singleton("error");
    
    
    @FXML private ListView<String> uiProperties;
    @FXML private TreeView<Object> uiFunctions;
    
    private final CodeArea codeArea = new CodeArea();
    
    public FXCQLEditor(){
        GeotkFX.loadJRXML(this);
        setCenter(codeArea);
        
        codeArea.textProperty().addListener((obs, oldText, newText) -> {
            updateHightLight();
            //codeArea.setStyleSpans(0, computeHighlight(newText));
        });

        uiProperties.setCellFactory((ListView<String> param) -> new ClickCell());
        uiProperties.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        uiProperties.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                Platform.runLater(uiProperties.getSelectionModel()::clearSelection);
            }
        });

        uiFunctions.setShowRoot(false);
        uiFunctions.setCellFactory((TreeView<Object> param) -> new ClickFCell());
        uiFunctions.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        uiFunctions.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<TreeItem<Object>>() {
            @Override
            public void changed(ObservableValue<? extends TreeItem<Object>> observable, TreeItem<Object> oldValue, TreeItem<Object> newValue) {
                Platform.runLater(uiFunctions.getSelectionModel()::clearSelection);
            }
        });

        final TreeItem<Object> root = new TreeItem<>("root");

        for(FunctionFactory ff : Functions.getFactories()){
            final String factoryName = ff.getIdentifier();
            final TreeItem fnode = new TreeItem(factoryName);
            String[] names = ff.getNames();
            Arrays.sort(names);
            for(String str : names){
                final ParameterDescriptorGroup desc = ff.describeFunction(str);
                final TreeItem enode = new TreeItem(desc);
                fnode.getChildren().add(enode);
            }
            root.getChildren().add(fnode);
        }

        uiFunctions.setRoot(root);

    }
    
    @FXML
    private void putShortcut(ActionEvent event) {
        final Button button = (Button) event.getSource();
        final String text = button.getText();
        codeArea.appendText(" "+text);
    }
    
    public void setTarget(Object candidate){
        FeatureType ft = null;
        if(candidate instanceof FeatureType){
            ft = (FeatureType) candidate;
        }else if(candidate instanceof FeatureCollection) {
            ft = ((FeatureCollection)candidate).getFeatureType();
        }else if(candidate instanceof FeatureMapLayer){
            ft = ((FeatureMapLayer)candidate).getCollection().getFeatureType();
        }
        
        final ObservableList properties = FXCollections.observableArrayList();
        if(ft!=null){
            for(PropertyDescriptor desc : ft.getDescriptors()){
                properties.add(desc.getName().getLocalPart());
            }
        }
        
        uiProperties.setItems(properties);
    }
    
    public void setExpression(Expression candidate){
        codeArea.replaceText(CQL.write(candidate));
    }
    
    public void setFilter(Filter candidate){
        codeArea.replaceText(CQL.write(candidate));
    }
    
    public Expression getExpression() throws CQLException{
        return CQL.parseExpression(codeArea.getText());
    }
    
    public Filter getFilter() throws CQLException{
        return CQL.parseFilter(codeArea.getText());
    }
    
    private void updateHightLight(){
        final String txt = codeArea.getText();
        
        final ParseTree tree = CQL.compile(txt);
        syntaxHighLight(tree);
        
        codeArea.getStylesheets().add(FXCQLEditor.class.getResource("cql.css").toExternalForm());
    }
    
    private void syntaxHighLight(ParseTree tree){
        
        if(tree instanceof ParserRuleContext){
            final ParserRuleContext prc = (ParserRuleContext) tree;
            if(prc.exception!=null){
                //error nodes
                final Token tokenStart = prc.getStart();
                Token tokenEnd = prc.getStop();
                if(tokenEnd==null) tokenEnd = tokenStart;
                final int offset = tokenStart.getStartIndex();
                final int end = tokenEnd.getStopIndex()+1;
                if(end>offset){
                    codeArea.setStyle(offset, end,STYLE_ERROR);
                }
                return;
            }
            
            //special case for functions
            if(prc instanceof CQLParser.ExpressionTermContext){
                final CQLParser.ExpressionTermContext ctx = (CQLParser.ExpressionTermContext) prc;
                if(ctx.NAME()!=null && ctx.LPAREN()!=null){
                    final int nbChild = tree.getChildCount();        
                    for(int i=0;i<nbChild;i++){
                        final ParseTree pt = tree.getChild(i);
                        if(pt instanceof TerminalNode && ((TerminalNode)pt).getSymbol().getType() == CQLLexer.NAME){
                            final TerminalNode tn = (TerminalNode) pt;
                            // if index<0 = missing token
                            final Token token = tn.getSymbol();
                            final int offset = token.getStartIndex();
                            final int end = token.getStopIndex()+1;
                            if(end>offset){
                                codeArea.setStyle(offset, end,STYLE_FUNCTION);
                            }
                        }else{
                            syntaxHighLight(pt);
                        }
                    }
                    return;
                }
            }
            
        }
        
        if(tree instanceof TerminalNode){
            final TerminalNode tn = (TerminalNode) tree;
            // if index<0 = missing token
            final Token token = tn.getSymbol();
            final int offset = token.getStartIndex();
            final int end = token.getStopIndex()+1;
            
            switch(token.getType()){
                
                case CQLLexer.COMMA : 
                case CQLLexer.UNARY : 
                case CQLLexer.MULT : 
                    codeArea.setStyle(offset, end, STYLE_DEFAULT);
                    break;
                    
                // EXpressions -------------------------------------------------
                case CQLLexer.TEXT : 
                case CQLLexer.INT : 
                case CQLLexer.FLOAT : 
                case CQLLexer.DATE : 
                case CQLLexer.DURATION_P : 
                case CQLLexer.DURATION_T : 
                case CQLLexer.POINT : 
                case CQLLexer.LINESTRING : 
                case CQLLexer.POLYGON : 
                case CQLLexer.MPOINT : 
                case CQLLexer.MLINESTRING : 
                case CQLLexer.MPOLYGON : 
                    codeArea.setStyle(offset, end, STYLE_LITERAL);
                    break;
                case CQLLexer.PROPERTY_NAME :
                    codeArea.setStyle(offset, end, STYLE_PROPERTY);
                    break;
                case CQLLexer.NAME :
                    if(tree.getChildCount()==0){
                        //property name
                    codeArea.setStyle(offset, end, STYLE_PROPERTY);
                    }else{
                        //function name
                    codeArea.setStyle(offset, end, STYLE_FUNCTION);
                    }
                    break;
                case CQLLexer.RPAREN : 
                case CQLLexer.LPAREN : 
                    codeArea.setStyle(offset, end, STYLE_PARENTHESE);
                    break;                    
                    
                case CQLLexer.COMPARE :
                case CQLLexer.LIKE :
                case CQLLexer.IS :
                case CQLLexer.BETWEEN :
                case CQLLexer.IN :
                    codeArea.setStyle(offset, end, STYLE_OPERATOR);
                    break;
                case CQLLexer.AND :
                case CQLLexer.OR :
                case CQLLexer.NOT :
                    codeArea.setStyle(offset, end, STYLE_BINARY);
                    break;
                case CQLLexer.BBOX :
                case CQLLexer.BEYOND :
                case CQLLexer.CONTAINS :
                case CQLLexer.CROSSES :
                case CQLLexer.DISJOINT :
                case CQLLexer.DWITHIN :
                case CQLLexer.EQUALS :
                case CQLLexer.INTERSECTS :
                case CQLLexer.OVERLAPS :
                case CQLLexer.TOUCHES :
                case CQLLexer.WITHIN :
                    codeArea.setStyle(offset, end, STYLE_BINARY);
                    break;
                default : 
                    codeArea.setStyle(offset, end, STYLE_ERROR);
                    break;
            }
        }
        
        final int nbChild = tree.getChildCount();        
        for(int i=0;i<nbChild;i++){
            syntaxHighLight(tree.getChild(i));
        }
    }
    
    public static Expression showDialog(Node parent, MapLayer layer, Expression candidate) throws CQLException {
        final FXCQLEditor editor = new FXCQLEditor();
        editor.setExpression(candidate);
        editor.setTarget(layer);
        FXOptionDialog.showOkCancel(parent, editor, "CQL Editor", true);
        return editor.getExpression();
    }
    
    public static Filter showFilterDialog(Node parent, MapLayer layer, Filter candidate) throws CQLException {
        final FXCQLEditor editor = new FXCQLEditor();
        editor.setFilter(candidate);
        editor.setTarget(layer);
        FXOptionDialog.showOkCancel(parent, editor, "CQL Editor", true);
        return editor.getFilter();
    }


    private class ClickCell extends ListCell<String>{

        public ClickCell() {
            setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    final String item = getItem();
                    if(item!=null){
                        if(codeArea.getText().endsWith(" ")){
                            codeArea.appendText(item);
                        }else{
                            codeArea.appendText(" "+item);
                        }
                    }
                }
            });
        }

        @Override
        protected void updateItem(String item, boolean empty) {
            super.updateItem(item, empty);
            setText(item);
        }

    }

    private class ClickFCell extends TreeCell<Object>{

        public ClickFCell() {
            setOnMouseClicked(new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    final Object item = getItem();
                    if(item instanceof ParameterDescriptorGroup){
                        final ParameterDescriptorGroup desc = (ParameterDescriptorGroup) item;
                        final StringBuilder sb = new StringBuilder();
                        sb.append(desc.getName().getCode()).append('(');
                        final List<GeneralParameterDescriptor> gpds = desc.descriptors();
                        for(int i=0;i<gpds.size();i++){
                            if(i>0) sb.append(',');
                            sb.append(gpds.get(i).getName().getCode());
                        }
                        sb.append(')');

                        if(codeArea.getText().endsWith(" ")){
                            codeArea.appendText(sb.toString());
                        }else{
                            codeArea.appendText(" "+sb.toString());
                        }
                    }
                }
            });
        }

        @Override
        protected void updateItem(Object item, boolean empty) {
            super.updateItem(item, empty);
            
            if(item instanceof String){
                setText((String)item);
            }else if(item instanceof ParameterDescriptorGroup){
                final ParameterDescriptorGroup desc = (ParameterDescriptorGroup) item;
                setText(desc.getName().getCode());
            }else{
                setText("");
            }
        }

    }

}
