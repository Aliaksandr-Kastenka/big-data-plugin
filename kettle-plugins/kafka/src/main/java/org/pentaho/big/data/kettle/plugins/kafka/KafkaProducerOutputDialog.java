/*! ******************************************************************************
 *
 * Pentaho Data Integration
 *
 * Copyright (C) 2002-2017 by Pentaho : http://www.pentaho.com
 *
 *******************************************************************************
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 ******************************************************************************/

package org.pentaho.big.data.kettle.plugins.kafka;

import java.util.List;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.plugins.PluginInterface;
import org.pentaho.di.core.plugins.PluginRegistry;
import org.pentaho.di.core.plugins.StepPluginType;
import org.pentaho.di.i18n.BaseMessages;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.BaseStepMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.ui.core.ConstUI;
import org.pentaho.di.ui.core.gui.GUIResource;
import org.pentaho.di.ui.core.widget.ComboVar;
import org.pentaho.di.ui.core.widget.TextVar;
import org.pentaho.di.ui.spoon.Spoon;
import org.pentaho.di.ui.trans.step.BaseStepDialog;
import org.pentaho.metastore.api.exceptions.MetaStoreException;

@SuppressWarnings( { "FieldCanBeLocal", "unused" } )
public class KafkaProducerOutputDialog extends BaseStepDialog implements StepDialogInterface {

  private static Class<?> PKG = KafkaProducerOutputMeta.class;
  // for i18n purposes, needed by Translator2!!   $NON-NLS-1$

  private final KafkaFactory kafkaFactory = KafkaFactory.defaultFactory();

  private static final int SHELL_MIN_WIDTH = 527;
  private static final int SHELL_MIN_HEIGHT = 535;
  private static final int INPUT_WIDTH = 350;

  private KafkaProducerOutputMeta meta;
  private ModifyListener lsMod;
  private Label wlClusterName;
  private ComboVar wClusterName;
  private Label wlClientId;
  private TextVar wClientId;
  private Label wlTopic;
  private ComboVar wTopic;
  private Label wlKeyField;
  private TextVar wKeyField;
  private Label wlMessageField;
  private TextVar wMessageField;
  
  private CTabFolder wTabFolder;
  private CTabItem wSetupTab;
  private CTabItem wOptionsTab;
  
  private Composite wSetupComp;
  private Composite wOptionsComp;

  public KafkaProducerOutputDialog( Shell parent, Object in,
                                    TransMeta transMeta, String stepname ) {
    super( parent, (BaseStepMeta) in, transMeta, stepname );
    meta = (KafkaProducerOutputMeta) in;
  }

  @Override public String open() {
    Shell parent = getParent();
    Display display = parent.getDisplay();
    changed = meta.hasChanged();    

    lsMod = e -> meta.setChanged();
    lsCancel = e -> cancel();
    lsOK = e -> ok();
    lsDef = new SelectionAdapter() {
      public void widgetDefaultSelected( SelectionEvent e ) {
        ok();
      }
    };

    shell = new Shell( parent, SWT.DIALOG_TRIM | SWT.MIN | SWT.MAX | SWT.RESIZE );
    props.setLook( shell );
    setShellImage( shell, meta );
    shell.setMinimumSize( SHELL_MIN_WIDTH, SHELL_MIN_HEIGHT );
    shell.setText( BaseMessages.getString( PKG, "KafkaProducerOutputDialog.Shell.Title" ) );

    FormLayout formLayout = new FormLayout();
    formLayout.marginWidth = 15;
    formLayout.marginHeight = 15;
    shell.setLayout( formLayout );
    shell.addShellListener( new ShellAdapter() {
        public void shellClosed( ShellEvent e ) {
          cancel();
        }
      } );

    Label wicon = new Label( shell, SWT.RIGHT );
    wicon.setImage( getImage() );
    FormData fdlicon = new FormData();
    fdlicon.top = new FormAttachment( 0, 0 );
    fdlicon.right = new FormAttachment( 100, 0 );
    wicon.setLayoutData( fdlicon );
    props.setLook( wicon );

    wlStepname = new Label( shell, SWT.RIGHT );
    wlStepname.setText( BaseMessages.getString( PKG, "KafkaProducerOutputDialog.Stepname.Label" ) );
    props.setLook( wlStepname );
    fdlStepname = new FormData();
    fdlStepname.left = new FormAttachment( 0, 0 );
    fdlStepname.top = new FormAttachment( 0, 0 );
    wlStepname.setLayoutData( fdlStepname );

    wStepname = new Text( shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    wStepname.setText( stepname );
    props.setLook( wStepname );
    wStepname.addModifyListener( lsMod );
    fdStepname = new FormData();
    fdStepname.width = 250;
    fdStepname.left = new FormAttachment( 0, 0 );
    fdStepname.top = new FormAttachment( wlStepname, 5 );
    wStepname.setLayoutData( fdStepname );
    wStepname.addSelectionListener( lsDef );

    Label topSeparator = new Label( shell, SWT.HORIZONTAL | SWT.SEPARATOR );
    FormData fdSpacer = new FormData();
    fdSpacer.height = 2;
    fdSpacer.left = new FormAttachment( 0, 0 );
    fdSpacer.top = new FormAttachment( wStepname, 15 );
    fdSpacer.right = new FormAttachment( 100, 0 );
    topSeparator.setLayoutData( fdSpacer );

    // Start of tabbed display
    wTabFolder = new CTabFolder( shell, SWT.BORDER );
    props.setLook( wTabFolder, Props.WIDGET_STYLE_TAB );
    wTabFolder.setSimple( false );
    wTabFolder.setUnselectedCloseVisible( true );

    wCancel = new Button( shell, SWT.PUSH );
    wCancel.setText( BaseMessages.getString( PKG, "System.Button.Cancel" ) );
    FormData fdCancel = new FormData();
    fdCancel.right = new FormAttachment( 100, 0 );
    fdCancel.bottom = new FormAttachment( 100, 0 );
    wCancel.setLayoutData( fdCancel );
    wCancel.addListener( SWT.Selection, lsCancel );

    wOK = new Button( shell, SWT.PUSH );
    wOK.setText( BaseMessages.getString( PKG, "System.Button.OK" ) );
    FormData fdOk = new FormData();
    fdOk.right = new FormAttachment( wCancel, -5 );
    fdOk.bottom = new FormAttachment( 100, 0 );
    wOK.setLayoutData( fdOk );
    wOK.addListener( SWT.Selection, lsOK );

    Label bottomSeparator = new Label( shell, SWT.HORIZONTAL | SWT.SEPARATOR );
    props.setLook( bottomSeparator );
    FormData fdBottomSeparator = new FormData();
    fdBottomSeparator.height = 2;
    fdBottomSeparator.left = new FormAttachment( 0, 0 );
    fdBottomSeparator.bottom = new FormAttachment( wCancel, -15 );
    fdBottomSeparator.right = new FormAttachment( 100, 0 );
    bottomSeparator.setLayoutData( fdBottomSeparator );

    FormData fdTabFolder = new FormData();
    fdTabFolder.left = new FormAttachment( 0, 0 );
    fdTabFolder.top = new FormAttachment( topSeparator, 15 );
    fdTabFolder.bottom = new FormAttachment( bottomSeparator, -15 );
    fdTabFolder.right = new FormAttachment( 100, 0 );
    wTabFolder.setLayoutData( fdTabFolder );

    buildSetupTab();
    buildOptionsTab();

    getData();

    setSize();

    meta.setChanged( changed );

    wTabFolder.setSelection( 0 );

    shell.open();
    while ( !shell.isDisposed() ) {
      if ( !display.readAndDispatch() ) {
        display.sleep();
      }
    }
    return stepname;
  }

  private void buildSetupTab() {
    wSetupTab = new CTabItem( wTabFolder, SWT.NONE );
    wSetupTab.setText( BaseMessages.getString( PKG, "KafkaProducerOutputDialog.SetupTab" ) );

    wSetupComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wSetupComp );
    FormLayout setupLayout = new FormLayout();
    setupLayout.marginHeight = 15;
    setupLayout.marginWidth = 15;
    wSetupComp.setLayout( setupLayout );

    wlClusterName = new Label( wSetupComp, SWT.LEFT );
    props.setLook( wlClusterName );
    wlClusterName.setText(  BaseMessages.getString( PKG, "KafkaProducerOutputDialog.HadoopCluster" ) );
    FormData fdlServer = new FormData();
    fdlServer.left = new FormAttachment( 0, 15 );
    fdlServer.top = new FormAttachment( 0, 15 );
    fdlServer.right = new FormAttachment( 50, 0 );
    wlClusterName.setLayoutData( fdlServer );

    wClusterName = new ComboVar( transMeta, wSetupComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wClusterName );
    wClusterName.addModifyListener( lsMod );
    FormData fdServer = new FormData();
    fdServer.left = new FormAttachment( 0, 15 );
    fdServer.top = new FormAttachment( wlClusterName, 5 );
    fdServer.right = new FormAttachment( 100, -15 );
    wClusterName.setLayoutData( fdServer );
    wClusterName.addSelectionListener( lsDef );

    wlClientId = new Label( wSetupComp, SWT.LEFT );
    props.setLook( wlClientId );
    wlClientId.setText(   BaseMessages.getString( PKG, "KafkaProducerOutputDialog.ClientId" ) );
    FormData fdlClientId = new FormData();
    fdlClientId.left = new FormAttachment( 0, 15 );
    fdlClientId.top = new FormAttachment( wClusterName, 10 );
    fdlClientId.right = new FormAttachment( 50, 0 );
    wlClientId.setLayoutData( fdlClientId );

    wClientId = new TextVar( transMeta, wSetupComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wClientId );
    wClientId.addModifyListener( lsMod );
    FormData fdClientId = new FormData();
    fdClientId.left = new FormAttachment( 0, 15 );
    fdClientId.top = new FormAttachment( wlClientId, 5 );
    fdClientId.right = new FormAttachment( 100, -15 );
    wClientId.setLayoutData( fdClientId );

    wlTopic = new Label( wSetupComp, SWT.LEFT );
    props.setLook( wlTopic );
    wlTopic.setText(   BaseMessages.getString( PKG, "KafkaProducerOutputDialog.Topic" ) );
    FormData fdlTopic = new FormData();
    fdlTopic.left = new FormAttachment( 0, 15 );
    fdlTopic.top = new FormAttachment( wClientId, 10 );
    fdlTopic.right = new FormAttachment( 50, 0 );
    wlTopic.setLayoutData( fdlTopic );

    wTopic = new ComboVar( transMeta, wSetupComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wTopic );
    wTopic.addModifyListener( lsMod );
    FormData fdTopic = new FormData();
    fdTopic.left = new FormAttachment( 0, 15 );
    fdTopic.top = new FormAttachment( wlTopic, 5 );
    fdTopic.right = new FormAttachment( 100, -15 );
    wTopic.setLayoutData( fdTopic );
    wTopic.getCComboWidget().addListener(
        SWT.FocusIn, new KafkaDialogHelper( wClusterName, wTopic, kafkaFactory, meta.getNamedClusterService(),
            meta.getNamedClusterServiceLocator(), meta.getMetastoreLocator()
        )::clusterNameChanged );

    wlKeyField = new Label( wSetupComp, SWT.LEFT );
    props.setLook( wlKeyField );
    wlKeyField.setText( BaseMessages.getString( PKG, "KafkaProducerOutputDialog.KeyField" ) );
    FormData fdlKeyField = new FormData();
    fdlKeyField.left = new FormAttachment( 0, 15 );
    fdlKeyField.top = new FormAttachment( wTopic, 10 );
    fdlKeyField.right = new FormAttachment( 50, 0 );
    wlKeyField.setLayoutData( fdlKeyField );

    wKeyField = new TextVar( transMeta, wSetupComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wKeyField );
    wKeyField.addModifyListener( lsMod );
    FormData fdKeyField = new FormData();
    fdKeyField.left = new FormAttachment( 0, 15 );
    fdKeyField.top = new FormAttachment( wlKeyField, 5 );
    fdKeyField.right = new FormAttachment( 100, -15 );
    wKeyField.setLayoutData( fdKeyField );

    wlMessageField = new Label( wSetupComp, SWT.LEFT );
    props.setLook( wlMessageField );
    wlMessageField.setText( BaseMessages.getString( PKG, "KafkaProducerOutputDialog.MessageField" ) );
    FormData fdlMessageField = new FormData();
    fdlMessageField.left = new FormAttachment( 0, 15 );
    fdlMessageField.top = new FormAttachment( wKeyField, 10 );
    fdlMessageField.right = new FormAttachment( 50, 0 );
    wlMessageField.setLayoutData( fdlMessageField );

    wMessageField = new TextVar( transMeta, wSetupComp, SWT.SINGLE | SWT.LEFT | SWT.BORDER );
    props.setLook( wMessageField );
    wMessageField.addModifyListener( lsMod );
    FormData fdMessageField = new FormData();
    fdMessageField.left = new FormAttachment( 0, 15 );
    fdMessageField.top = new FormAttachment( wlMessageField, 5 );
    fdMessageField.right = new FormAttachment( 100, -15 );
    wMessageField.setLayoutData( fdMessageField );

    FormData fdSetupComp = new FormData();
    fdSetupComp.left = new FormAttachment( 0, 0 );
    fdSetupComp.top = new FormAttachment( 0, 0 );
    fdSetupComp.right = new FormAttachment( 100, 0 );
    fdSetupComp.bottom = new FormAttachment( 100, 0 );
    wSetupComp.setLayoutData( fdSetupComp );
    wSetupComp.layout();
    wSetupTab.setControl( wSetupComp );
  }

  private void buildOptionsTab() {
    wOptionsTab = new CTabItem( wTabFolder, SWT.NONE );
    wOptionsTab.setText( BaseMessages.getString( PKG, "KafkaProducerOutputDialog.OptionsTab" ) );
    wOptionsComp = new Composite( wTabFolder, SWT.NONE );
    props.setLook( wOptionsComp );
    FormLayout fieldsLayout = new FormLayout();
    fieldsLayout.marginHeight = 15;
    fieldsLayout.marginWidth = 15;
    wOptionsComp.setLayout( fieldsLayout );
  }

  private void getData() {
    try {
      List<String> names = meta.getNamedClusterService().listNames( Spoon.getInstance().getMetaStore() );
      wClusterName.setItems( names.toArray( new String[ names.size() ] ) );
    } catch ( MetaStoreException e ) {
      log.logError( "Failed to get defined named clusters", e );
    }

    if ( meta.getClusterName() != null ) {
      wClusterName.setText( meta.getClusterName() );
    }

    if ( meta.getClientId() != null ) {
      wClientId.setText( meta.getClientId() );
    }
    if ( meta.getTopic() != null ) {
      wTopic.setText( meta.getTopic() );
    }
    if ( meta.getKeyField() != null ) {
      wKeyField.setText( meta.getKeyField() );
    }
    if ( meta.getMessageField() != null ) {
      wMessageField.setText( meta.getMessageField() );
    }
  }

  private Image getImage() {
    PluginInterface plugin =
      PluginRegistry.getInstance().getPlugin( StepPluginType.class, stepMeta.getStepMetaInterface() );
    String id = plugin.getIds()[ 0 ];
    if ( id != null ) {
      return GUIResource.getInstance().getImagesSteps().get( id ).getAsBitmapForSize( shell.getDisplay(),
        ConstUI.ICON_SIZE, ConstUI.ICON_SIZE );
    }
    return null;
  }

  private void cancel() {
    meta.setChanged( false );
    dispose();
  }

  private void ok() {
    stepname = wStepname.getText();
    meta.setClusterName( wClusterName.getText() );
    meta.setClientId( wClientId.getText() );
    meta.setTopic( wTopic.getText() );
    meta.setKeyField( wKeyField.getText() );
    meta.setMessageField( wMessageField.getText() );
    dispose();
  }
}
