/**
 *
 * Copyright (C) 2002-2012 "SYSNET International, Inc."
 * support@sysnetint.com [http://www.sysnetint.com]
 *
 * This file is part of OpenEMPI.
 *
 * OpenEMPI is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */
package org.openempi.webapp.client.mvc;

import java.util.Map;

import org.openempi.webapp.client.AppEvents;
import org.openempi.webapp.client.Constants;
import org.openempi.webapp.client.model.EntityWeb;
import org.openempi.webapp.client.model.PermissionWeb;
import org.openempi.webapp.client.model.SystemConfigurationWeb;
import org.openhie.openempi.model.Permission;

import com.extjs.gxt.ui.client.Registry;
import com.extjs.gxt.ui.client.Style.Orientation;
import com.extjs.gxt.ui.client.event.ButtonEvent;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.event.MenuEvent;
import com.extjs.gxt.ui.client.event.MessageBoxEvent;
import com.extjs.gxt.ui.client.event.SelectionListener;
import com.extjs.gxt.ui.client.mvc.AppEvent;
import com.extjs.gxt.ui.client.mvc.Controller;
import com.extjs.gxt.ui.client.mvc.Dispatcher;
import com.extjs.gxt.ui.client.mvc.View;
import com.extjs.gxt.ui.client.util.IconHelper;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.util.Padding;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.Html;
import com.extjs.gxt.ui.client.widget.Label;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.MessageBox;
import com.extjs.gxt.ui.client.widget.button.Button;
import com.extjs.gxt.ui.client.widget.layout.FormLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayoutData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.extjs.gxt.ui.client.widget.layout.BoxLayout.BoxLayoutPack;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout.HBoxLayoutAlign;
import com.extjs.gxt.ui.client.widget.menu.Menu;
import com.extjs.gxt.ui.client.widget.menu.MenuBar;
import com.extjs.gxt.ui.client.widget.menu.MenuBarItem;
import com.extjs.gxt.ui.client.widget.menu.MenuItem;
import com.extjs.gxt.ui.client.widget.toolbar.FillToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.SeparatorToolItem;
import com.extjs.gxt.ui.client.widget.toolbar.ToolBar;
import com.extjs.gxt.ui.client.widget.Info;
import com.google.gwt.core.client.GWT;

public class MenuToolbarView extends View
{
    private LayoutContainer container;
    private Dialog aboutDialog = null;

    public MenuToolbarView(Controller controller) {
        super(controller);
    }

    private void initUI() {
        SystemConfigurationWeb systemInfo = (SystemConfigurationWeb) Registry.get(Constants.SYSTEM_CONFIGURATION_INFO);
        // Info.display("MenuToolbarView: ", ""+systemInfo.getBlockingAlgorithmName());
        // Info.display("MenuToolbarView: ", ""+systemInfo.getMatchingAlgorithmName());

        Map<String, PermissionWeb> permissions = Registry.get(Constants.LOGIN_USER_PERMISSIONS);
        // if( permissions != null ) {
        // for (Object key: permissions.keySet()) {
        // Info.display("ermissions:", "Key : " + key.toString() + " Value : " + permissions.get(key).getDescription());
        // }
        // }

        container = new LayoutContainer();
        RowLayout rowLayout = new RowLayout(Orientation.VERTICAL);
        container.setLayout(rowLayout);

        // File menu
        Menu fileList = new Menu();
        MenuItem menuItemFileList = new MenuItem("File List", IconHelper.create("images/folder_explore.png"),
                new SelectionListener<MenuEvent>()
                {
                    @Override
                    public void componentSelected(MenuEvent ce) {
                        Dispatcher.get().dispatch(AppEvents.FileListView);
                    }
                });
        fileList.add(menuItemFileList);
        MenuBarItem files = new MenuBarItem("File", fileList);

        // Edit menu
        Menu edit = new Menu();
        MenuItem menuItemAdd = new MenuItem("Add Entity", IconHelper.create("images/entity_add.png"),
                new SelectionListener<MenuEvent>()
                {
                    @Override
                    public void componentSelected(MenuEvent ce) {
                        Dispatcher.get().dispatch(AppEvents.EntityAddView);
                    }
                });
        edit.add(menuItemAdd);

        MenuItem menuItemProfile = new MenuItem("Profile", IconHelper.create("images/user_profile.png"),
                new SelectionListener<MenuEvent>()
                {
                    @Override
                    public void componentSelected(MenuEvent ce) {
                        Dispatcher.get().dispatch(AppEvents.ProfileView);
                    }
                });
        edit.add(menuItemProfile);
        MenuBarItem editItem = new MenuBarItem("Edit", edit);

        // Search menu
        Menu searchSubmenu = new Menu();
        MenuItem menuItemSearchEntity = new MenuItem("Entity Search", IconHelper.create("images/search_adv_16x16.png"),
                new SelectionListener<MenuEvent>()
                {
                    @Override
                    public void componentSelected(MenuEvent ce) {
                        Dispatcher.get().dispatch(AppEvents.EntitySearchView);
                    }
                });
        searchSubmenu.add(menuItemSearchEntity);
        MenuBarItem search = new MenuBarItem("Search", searchSubmenu);

        // Admin menu
        Menu configMenuOptions = new Menu();
        MenuItem menuItemCustomFields = new MenuItem("Custom Fields Configuration",
                IconHelper.create("images/folder_edit.png"), new SelectionListener<MenuEvent>()
                {
                    @Override
                    public void componentSelected(MenuEvent ce) {
                        Dispatcher.get().dispatch(AppEvents.CustomFieldsConfigurationView);
                    }
                });
        configMenuOptions.add(menuItemCustomFields);

        // Check Blocking Algorithm Name
        MenuItem menuItemBlocking = null;
        if (systemInfo != null && systemInfo.getBlockingAlgorithmName().contains("Traditional Blocking Algorithm")) {
            menuItemBlocking = new MenuItem("Traditional Blocking Configuration",
                    IconHelper.create("images/bricks.png"), new SelectionListener<MenuEvent>()
                    {
                        @Override
                        public void componentSelected(MenuEvent ce) {
                            Dispatcher.get().dispatch(AppEvents.BlockingConfigurationView);
                        }
                    });
            configMenuOptions.add(menuItemBlocking);
        } else if (systemInfo != null
                && systemInfo.getBlockingAlgorithmName().contains("Sorted Neighborhood Blocking Algorithm")) {
            menuItemBlocking = new MenuItem("Sorted Neighborhood Blocking Configuration",
                    IconHelper.create("images/bricks.png"), new SelectionListener<MenuEvent>()
                    {
                        @Override
                        public void componentSelected(MenuEvent ce) {
                            Dispatcher.get().dispatch(AppEvents.SortedNeighborhoodBlockingConfigurationView);
                        }
                    });
            configMenuOptions.add(menuItemBlocking);
        } else if (systemInfo != null
                && systemInfo.getBlockingAlgorithmName().contains("Suffix Array Blocking Algorithm")) {
            menuItemBlocking = new MenuItem("Suffix Array Blocking Configuration",
                    IconHelper.create("images/bricks.png"), new SelectionListener<MenuEvent>()
                    {
                        @Override
                        public void componentSelected(MenuEvent ce) {
                            Dispatcher.get().dispatch(AppEvents.SuffixArrayBlockingConfigurationView);
                        }
                    });
            configMenuOptions.add(menuItemBlocking);
        }

        // Check Matching Algorithm Name
        MenuItem menuItemMatching = null;
        if (systemInfo != null && systemInfo.getMatchingAlgorithmName().contains("Deterministic Matching Algorithm")) {
            menuItemMatching = new MenuItem("Deterministic Matching Configuration",
                    IconHelper.create("images/wrench_orange.png"), new SelectionListener<MenuEvent>()
                    {
                        @Override
                        public void componentSelected(MenuEvent ce) {
                            Dispatcher.get().dispatch(AppEvents.DeterministicMatchConfigurationView);
                        }
                    });
            configMenuOptions.add(menuItemMatching);
        } else if (systemInfo != null
                && systemInfo.getMatchingAlgorithmName().contains("Probabilistic Matching Algorithm")) {
            menuItemMatching = new MenuItem("Probabilistic Matching Configuration",
                    IconHelper.create("images/wrench_orange.png"), new SelectionListener<MenuEvent>()
                    {
                        @Override
                        public void componentSelected(MenuEvent ce) {
                            Dispatcher.get().dispatch(AppEvents.MatchConfigurationView);
                        }
                    });
            configMenuOptions.add(menuItemMatching);
        }

        MenuItem menuItemReviewLinks = new MenuItem("Review Entity Links", IconHelper.create("images/entity_link.png"),
                new SelectionListener<MenuEvent>()
                {
                    @Override
                    public void componentSelected(MenuEvent ce) {
                        Dispatcher.get().dispatch(AppEvents.EntityLinkView);
                    }
                });
        configMenuOptions.add(menuItemReviewLinks);

        MenuItem menuItemEventNotification = new MenuItem("Event Notification",
                IconHelper.create("images/email_open_image.png"), new SelectionListener<MenuEvent>()
                {
                    @Override
                    public void componentSelected(MenuEvent ce) {
                        Dispatcher.get().dispatch(AppEvents.EventNotificationView);
                    }
                });
        configMenuOptions.add(menuItemEventNotification);

        MenuItem menuItemManageIdentifier = new MenuItem("Manage Identifier Domains",
                IconHelper.create("images/key.png"), new SelectionListener<MenuEvent>()
                {
                    @Override
                    public void componentSelected(MenuEvent ce) {
                        Dispatcher.get().dispatch(AppEvents.ManageIdentifierDomainView);
                    }
                });
        configMenuOptions.add(menuItemManageIdentifier);
/*
        MenuItem menuItemManageReport = new MenuItem("Manage Report Design", IconHelper.create("images/report.png"),
                new SelectionListener<MenuEvent>()
                {
                    @Override
                    public void componentSelected(MenuEvent ce) {
                        Dispatcher.get().dispatch(AppEvents.ReportDesignView);
                    }
                });
        configMenuOptions.add(menuItemManageReport);

        MenuItem menuItemReportGenerate = new MenuItem("Report Generate", IconHelper.create("images/report.png"),
                new SelectionListener<MenuEvent>()
                {
                    @Override
                    public void componentSelected(MenuEvent ce) {
                        Dispatcher.get().dispatch(AppEvents.ReportGenerateView);
                    }
                });
        configMenuOptions.add(menuItemReportGenerate);
*/
        MenuItem menuItemDataProfile = new MenuItem("Data Profile", IconHelper.create("images/script.png"),
                new SelectionListener<MenuEvent>()
                {
                    @Override
                    public void componentSelected(MenuEvent ce) {
                        Dispatcher.get().dispatch(AppEvents.DataProfileView);
                    }
                });
        configMenuOptions.add(menuItemDataProfile);

        MenuItem menuItemEventViewer = new MenuItem("Entity Event Viewer",
                IconHelper.create("images/search_icon_16x16.png"), new SelectionListener<MenuEvent>()
                {
                    @Override
                    public void componentSelected(MenuEvent ce) {
                        Dispatcher.get().dispatch(AppEvents.AuditEventEntryView);
                    }
                });
        configMenuOptions.add(menuItemEventViewer);

        MenuItem menuItemStartPIXPDQ = new MenuItem("Start PIX/PDQ Server", IconHelper.create("images/clock_play.png"),
                new SelectionListener<MenuEvent>()
                {
                    @Override
                    public void componentSelected(MenuEvent ce) {
                        Dispatcher.get().dispatch(AppEvents.AdminStartPixPdqServer);
                    }
                });
        configMenuOptions.add(menuItemStartPIXPDQ);

        MenuItem menuItemStopPIXPDQ = new MenuItem("Stop PIX/PDQ Server", IconHelper.create("images/clock_stop.png"),
                new SelectionListener<MenuEvent>()
                {
                    @Override
                    public void componentSelected(MenuEvent ce) {
                        Dispatcher.get().dispatch(AppEvents.AdminStopPixPdqServer);
                    }
                });
        configMenuOptions.add(menuItemStopPIXPDQ);

        // Admin-Advanced Sub-menu
        Menu advancedSubmenu = new Menu();

        MenuItem menuItemGlobalIdentifiers = new MenuItem("Assign Global Identifiers",
                IconHelper.create("images/computer.png"), new SelectionListener<MenuEvent>()
                {
                    @Override
                    public void componentSelected(MenuEvent ce) {
                        Dispatcher.get().dispatch(AppEvents.AssignGlobalIdentifier);
                    }
                });
        advancedSubmenu.add(menuItemGlobalIdentifiers);

        MenuItem menuItemInitializeRepository = new MenuItem("Initialize Repository",
                IconHelper.create("images/wrench.png"), new SelectionListener<MenuEvent>()
                {
                    @Override
                    public void componentSelected(MenuEvent ce) {
                        MessageBox
                                .confirm(
                                        "Confirm",
                                        "The initialize repository operation cannot be undone. Are you sure you want to initialize repository from the beginning using the underlying matching algorithm?",
                                        listenInitiailRepository);
                        // Dispatcher.get().dispatch(AppEvents.InitializeRepository);
                    }
                });
        advancedSubmenu.add(menuItemInitializeRepository);

        MenuItem menuItemLinkAllRecord = new MenuItem("Link all Record Pairs",
                IconHelper.create("images/world_link.png"), new SelectionListener<MenuEvent>()
                {
                    @Override
                    public void componentSelected(MenuEvent ce) {
                        MessageBox
                                .confirm(
                                        "Confirm",
                                        "The link all record pairs operation cannot be undone. Are you sure you want to link all record pairs from the beginning using the underlying matching algorithm?",
                                        listenLinkAllRecordPairs);
                        // Dispatcher.get().dispatch(AppEvents.LinkAllRecordPairs);
                    }
                });
        advancedSubmenu.add(menuItemLinkAllRecord);

        MenuItem menuItemInitializeCustomConfiguration = new MenuItem("Initialize Custom Configuration",
                IconHelper.create("images/application_view_list.png"), new SelectionListener<MenuEvent>()
                {
                    @Override
                    public void componentSelected(MenuEvent ce) {
                        MessageBox
                                .confirm(
                                        "Confirm",
                                        "The initialize custom configuration operation cannot be undone. Are you sure you want to initialize custom configuration?",
                                        listenInitiailCustomConfiguration);
                        // Dispatcher.get().dispatch(AppEvents.InitializeCustomConfiguration);
                    }
                });
        advancedSubmenu.add(menuItemInitializeCustomConfiguration);

        MenuItem menuItemRebuildBlockingIndex = new MenuItem("Rebuild Blocking Indexes",
                IconHelper.create("images/rebuild.png"), new SelectionListener<MenuEvent>()
                {
                    @Override
                    public void componentSelected(MenuEvent ce) {
                        MessageBox
                                .confirm(
                                        "Confirm",
                                        "The rebuild blocking indexes operation cannot be undone. Are you sure you want to rebuild blocking indexes?",
                                        listenRebuildBlockingIndex);
                        // Dispatcher.get().dispatch(AppEvents.RebuildBlockingIndex);
                    }
                });
        advancedSubmenu.add(menuItemRebuildBlockingIndex);

        MenuItem advancedMenuItem = new MenuItem("Advanced");
        advancedMenuItem.setIcon(IconHelper.create("images/menu.png"));
        advancedMenuItem.setSubMenu(advancedSubmenu);

        configMenuOptions.add(advancedMenuItem);

        MenuBarItem adminItem = new MenuBarItem("Admin", configMenuOptions);

        // Security menu
        Menu security = new Menu();
        MenuItem menuItemManageUsers = new MenuItem("Manage Users", IconHelper.create("images/user_group.png"),
                new SelectionListener<MenuEvent>()
                {
                    @Override
                    public void componentSelected(MenuEvent ce) {
                        Dispatcher.get().dispatch(AppEvents.ManageUserView);
                    }
                });
        security.add(menuItemManageUsers);

        MenuItem menuItemManageRoles = new MenuItem("Manage Roles", IconHelper.create("images/user_role.png"),
                new SelectionListener<MenuEvent>()
                {
                    @Override
                    public void componentSelected(MenuEvent ce) {
                        Dispatcher.get().dispatch(AppEvents.ManageRoleView);
                    }
                });
        security.add(menuItemManageRoles);
        MenuBarItem securityItem = new MenuBarItem("Security", security);

        // Entity menu
        Menu entity = new Menu();
        MenuItem entityAttributeItem = new MenuItem("Entity Model Design", IconHelper.create("images/entity.png"),
                new SelectionListener<MenuEvent>()
                {
                    @Override
                    public void componentSelected(MenuEvent ce) {
                        Dispatcher.get().dispatch(AppEvents.EntityAttributeView);
                    }
                });
        entity.add(entityAttributeItem);

        MenuItem keyValueSetItem = new MenuItem("Key Value Sets", IconHelper.create("images/key.png"),
                new SelectionListener<MenuEvent>()
                {
                    @Override
                    public void componentSelected(MenuEvent ce) {
                        Dispatcher.get().dispatch(AppEvents.KeyValueSetView);
                    }
                });
        entity.add(keyValueSetItem);
        MenuBarItem entityItem = new MenuBarItem("Entity", entity);

        // Help menu
        Menu help = new Menu();
        MenuItem aboutItem = new MenuItem("About OpenEMPI", IconHelper.create("images/help.png"),
                new SelectionListener<MenuEvent>()
                {
                    @Override
                    public void componentSelected(MenuEvent ce) {

                        buildAboutDialog();

                        aboutDialog.show();
                    }
                });
        help.add(aboutItem);

        MenuBarItem helpItem = new MenuBarItem("Help", help);

        MenuBar bar = new MenuBar();
        bar.setBorders(true);
        bar.add(files);
        bar.add(editItem);
        // bar.setStyleAttribute("borderTop", "none");
        bar.add(search);
        bar.add(adminItem);
        bar.add(securityItem);
        bar.add(entityItem);
        bar.add(helpItem);

        container.add(bar); // , new MarginData(5,0,0,0));

        ToolBar toolbar = new ToolBar();
        toolbar.setBorders(true);
        toolbar.add(new SeparatorToolItem());

        Button buttonFileList = new Button();
        buttonFileList.setToolTip("List Files");
        buttonFileList.setIcon(IconHelper.create("images/folder_explore.png"));
        buttonFileList.addSelectionListener(new SelectionListener<ButtonEvent>()
        {
            @Override
            public void componentSelected(ButtonEvent ce) {
                GWT.log("Dispatched file list view event.", null);
                Dispatcher.get().dispatch(AppEvents.FileListView);
            }
        });
        toolbar.add(buttonFileList);

        toolbar.add(new SeparatorToolItem());

        Button buttonEntitySearch = new Button();
        buttonEntitySearch.setToolTip("Enttity Search");
        buttonEntitySearch.setIcon(IconHelper.create("images/search_adv_16x16.png"));
        buttonEntitySearch.addSelectionListener(new SelectionListener<ButtonEvent>()
        {
            @Override
            public void componentSelected(ButtonEvent ce) {
                Dispatcher.get().dispatch(AppEvents.EntitySearchView);
            }
        });
        toolbar.add(buttonEntitySearch);

        toolbar.add(new SeparatorToolItem());

        Button buttonAddEntity = new Button();
        buttonAddEntity.setToolTip("Add Entity");
        buttonAddEntity.setIcon(IconHelper.create("images/add.png"));
        buttonAddEntity.addSelectionListener(new SelectionListener<ButtonEvent>()
        {
            @Override
            public void componentSelected(ButtonEvent ce) {
                Dispatcher.get().dispatch(AppEvents.EntityAddView);
            }
        });
        toolbar.add(buttonAddEntity);

        toolbar.add(new SeparatorToolItem());

        Button buttonReviewLinks = new Button();
        buttonReviewLinks.setToolTip("Review Links");
        buttonReviewLinks.setIcon(IconHelper.create("images/layout_link.png"));
        buttonReviewLinks.addSelectionListener(new SelectionListener<ButtonEvent>()
        {
            @Override
            public void componentSelected(ButtonEvent ce) {
                Dispatcher.get().dispatch(AppEvents.EntityLinkView);
            }
        });
        toolbar.add(buttonReviewLinks);

        toolbar.add(new SeparatorToolItem());

        Button buttonEventNotifications = new Button();
        buttonEventNotifications.setToolTip("Event Notifications");
        buttonEventNotifications.setIcon(IconHelper.create("images/email_open_image.png"));
        buttonEventNotifications.addSelectionListener(new SelectionListener<ButtonEvent>()
        {
            @Override
            public void componentSelected(ButtonEvent ce) {
                GWT.log("Dispatched event notification view event.", null);
                Dispatcher.get().dispatch(AppEvents.EventNotificationView);
            }
        });
        toolbar.add(buttonEventNotifications);

        // button.setIcon(Examples.ICONS.menu_show());
        toolbar.add(new FillToolItem());

        Button buttonLogout = new Button("Logout");
        buttonLogout.addSelectionListener(new SelectionListener<ButtonEvent>()
        {
            @Override
            public void componentSelected(ButtonEvent ce) {
                Dispatcher.get().dispatch(AppEvents.Logout);
            }
        });
        toolbar.add(buttonLogout);

        container.add(toolbar);

        // check permissions
        if (permissions != null) {

            PermissionWeb permission = permissions.get(Permission.FILE_IMPORT);
            if (permission == null) {
                buttonFileList.disable();
                menuItemFileList.disable();
            }

            permission = permissions.get(Permission.RECORD_VIEW);
            if (permission == null) {
                buttonEntitySearch.disable();
                menuItemSearchEntity.disable();
            }

            permission = permissions.get(Permission.RECORD_ADD);
            if (permission == null) {
                buttonAddEntity.disable();
                menuItemAdd.disable();
            }

            permission = permissions.get(Permission.RECORD_LINKS_REVIEW);
            if (permission == null) {
                buttonReviewLinks.disable();
                menuItemReviewLinks.disable();
            }

            // Disable Report menu option
//            permission = permissions.get(Permission.REPORT_GENERATE);
//            if (permission == null) {
////                menuItemManageReport.disable();
//            }

//            permission = permissions.get(Permission.REPORT_VIEW);
//            if (permission == null) {
////                menuItemReportGenerate.disable();
//            }

            permission = permissions.get(Permission.CUSTOM_FIELDS_CONFIGURE);
            if (permission == null) {
                menuItemCustomFields.disable();
            }

            PermissionWeb blockingPermission = permissions.get(Permission.BLOCKING_CONFIGURE);
            if (blockingPermission == null) {
                menuItemBlocking.disable();
            }

            PermissionWeb machingPermission = permissions.get(Permission.MATCHING_CONFIGURE);
            if (machingPermission == null) {
                menuItemMatching.disable();
            }

            // Protect the Advanced Menu Entry with permissions BLOCKING_CONFIGURATION and MATCHING_CONFIGURATION
            if (blockingPermission == null || machingPermission == null) {
                advancedSubmenu.disable();
            }

            /*
             * permission = permissions.get(Permission.GLOBAL_IDENTIFIERS_EDIT); if (permission == null) {
             * menuItemGlobalIdentifiers.disable(); }
             */

            permission = permissions.get(Permission.EVENT_CONFIGURATION_EDIT);
            if (permission == null) {
                buttonEventNotifications.disable();
                menuItemEventNotification.disable();
            }

            permission = permissions.get(Permission.IDENTIFIER_DOMAIN_VIEW);
            if (permission == null) {
                menuItemManageIdentifier.disable();
            }

            permission = permissions.get(Permission.PIXPDQ_MANAGE);
            if (permission == null) {
                menuItemStartPIXPDQ.disable();
                menuItemStopPIXPDQ.disable();
            }

            permission = permissions.get(Permission.USER_VIEW);
            if (permission == null) {
                menuItemProfile.disable();

                menuItemManageUsers.disable();
                menuItemManageRoles.disable();
            }
        }

        LayoutContainer north = Registry.get(Constants.NORTH_PANEL);
        north.add(container);
        north.layout();
        //
        // Viewport viewport = Registry.get(AppView.VIEWPORT);
        //
        // BorderLayoutData data = new BorderLayoutData(LayoutRegion.NORTH, 52);
        // data.setMargins(new Margins());
        //
        // viewport.add(container, data);
        // viewport.layout();
    }

    private void buildAboutDialog() {

        aboutDialog = new Dialog();
        aboutDialog.setBodyBorder(false);
        aboutDialog.setWidth(300);
        aboutDialog.setHeight(220);
        aboutDialog.setButtons(Dialog.OK);
        aboutDialog.setModal(true);
        aboutDialog.getButtonById(Dialog.OK).addSelectionListener(new SelectionListener<ButtonEvent>()
        {
            @Override
            public void componentSelected(ButtonEvent ce) {

                aboutDialog.close();
            }
        });

        ContentPanel cp = new ContentPanel();
        cp.setHeading("About OpenEMPI");
        cp.setFrame(true);
        cp.setIcon(IconHelper.create("images/openempi.png"));
        FormLayout formLayout = new FormLayout();
        formLayout.setLabelWidth(280);
        cp.setLayout(formLayout);
        cp.setSize(290, 220);

        Html header = new Html("<div id='header'>" + "   <img src='images/openempi.jpg'/> " + "</div>");
        header.setBorders(true);
        header.setWidth("90%");
        header.setHeight(70);

        Label text = new Label("Current Entity Model: None");
        if (Registry.get(Constants.ENTITY_ATTRIBUTE_MODEL) != null) {

            EntityWeb currentEntity = Registry.get(Constants.ENTITY_ATTRIBUTE_MODEL);
            text.setText("Current Entity Model: " + currentEntity.getDisplayName());
        }

        LayoutContainer formContainer = new LayoutContainer();
        HBoxLayout layout = new HBoxLayout();
        layout.setPadding(new Padding(5));
        layout.setHBoxLayoutAlign(HBoxLayoutAlign.MIDDLE);
        layout.setPack(BoxLayoutPack.CENTER);
        formContainer.setLayout(layout);
        HBoxLayoutData layoutData = new HBoxLayoutData(new Margins(15, 0, 0, 0));
        formContainer.add(text, layoutData);

        cp.add(header);
        cp.add(formContainer);
        aboutDialog.add(cp);
    }

    final Listener<MessageBoxEvent> listenInitiailRepository = new Listener<MessageBoxEvent>()
    {
        public void handleEvent(MessageBoxEvent ce) {
            Button btn = ce.getButtonClicked();
            // Info.display("MessageBox1 ", "The '{0}' button was pressed", btn.getText());
            if (btn.getText().equals("Yes")) {
                Dispatcher.get().dispatch(AppEvents.InitializeRepository);
            }
        }
    };

    final Listener<MessageBoxEvent> listenLinkAllRecordPairs = new Listener<MessageBoxEvent>()
    {
        public void handleEvent(MessageBoxEvent ce) {
            Button btn = ce.getButtonClicked();
            // Info.display("MessageBox1 ", "The '{0}' button was pressed", btn.getText());
            if (btn.getText().equals("Yes")) {
                Dispatcher.get().dispatch(AppEvents.LinkAllRecordPairs);
            }
        }
    };

    final Listener<MessageBoxEvent> listenInitiailCustomConfiguration = new Listener<MessageBoxEvent>()
    {
        public void handleEvent(MessageBoxEvent ce) {
            Button btn = ce.getButtonClicked();
            // Info.display("MessageBox1 ", "The '{0}' button was pressed", btn.getText());
            if (btn.getText().equals("Yes")) {
                Dispatcher.get().dispatch(AppEvents.InitializeCustomConfiguration);
            }
        }
    };

    final Listener<MessageBoxEvent> listenRebuildBlockingIndex = new Listener<MessageBoxEvent>()
    {
        public void handleEvent(MessageBoxEvent ce) {
            Button btn = ce.getButtonClicked();
            // Info.display("MessageBox1 ", "The '{0}' button was pressed", btn.getText());
            if (btn.getText().equals("Yes")) {
                Dispatcher.get().dispatch(AppEvents.RebuildBlockingIndex);
            }
        }
    };

    @Override
    protected void handleEvent(AppEvent event) {
        if (event.getType() == AppEvents.InitMenu) {
            initUI();
        }
    }
}
