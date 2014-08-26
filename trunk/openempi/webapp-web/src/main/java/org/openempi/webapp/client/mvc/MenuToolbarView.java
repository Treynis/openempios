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
import com.extjs.gxt.ui.client.event.Events;
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
import com.extjs.gxt.ui.client.widget.menu.CheckMenuItem;
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

    private Menu blockingMenu;
    private Menu matchingMenu;
    private MenuItem menuItemBlocking;
    private MenuItem menuItemMatching;

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
        Menu fileListMenu = new Menu();
        MenuItem menuItemFileList = new MenuItem("File List", IconHelper.create("images/folder_explore.png"),
                new SelectionListener<MenuEvent>()
                {
                    @Override
                    public void componentSelected(MenuEvent ce) {
                        Dispatcher.get().dispatch(AppEvents.FileListView);
                    }
                });
        fileListMenu.add(menuItemFileList);
        MenuBarItem fileItem = new MenuBarItem("File", fileListMenu);


        // Edit menu
        Menu editMenu = new Menu();
        MenuItem menuItemEntityModelDesign = new MenuItem("Edit Entity Model", IconHelper.create("images/entity.png"),
                new SelectionListener<MenuEvent>()
                {
                    @Override
                    public void componentSelected(MenuEvent ce) {
                        Dispatcher.get().dispatch(AppEvents.EntityAttributeView);
                    }
                });
        editMenu.add(menuItemEntityModelDesign);
        MenuItem menuItemIdentifierDomains = new MenuItem("Edit Identifier Domains",
                IconHelper.create("images/key.png"), new SelectionListener<MenuEvent>()
                {
                    @Override
                    public void componentSelected(MenuEvent ce) {
                        Dispatcher.get().dispatch(AppEvents.ManageIdentifierDomainView);
                    }
                });
        editMenu.add(menuItemIdentifierDomains);
        MenuItem menuItemAdd = new MenuItem("Add Record", IconHelper.create("images/add.png"),
                new SelectionListener<MenuEvent>()
                {
                    @Override
                    public void componentSelected(MenuEvent ce) {
                        Dispatcher.get().dispatch(AppEvents.EntityAddView);
                    }
                });
        editMenu.add(menuItemAdd);

        MenuItem menuItemProfile = new MenuItem("Edit Profile", IconHelper.create("images/user_profile.png"),
                new SelectionListener<MenuEvent>()
                {
                    @Override
                    public void componentSelected(MenuEvent ce) {
                        Dispatcher.get().dispatch(AppEvents.ProfileView);
                    }
                });
        editMenu.add(menuItemProfile);
        MenuBarItem editItem = new MenuBarItem("Edit", editMenu);


        // Search menu
        Menu searchMenu = new Menu();
        MenuItem menuItemFieldSearchEntity = new MenuItem("Fixed Search", IconHelper.create("images/search_icon_16x16.png"), 
                new SelectionListener<MenuEvent>() 
                {
                  @Override
                  public void componentSelected(MenuEvent ce) {
                      Dispatcher.get().dispatch(AppEvents.EntityFixedSearchView);// EntityFieldSearchView
                  }
                });
        searchMenu.add(menuItemFieldSearchEntity);
        MenuItem menuItemSearchEntity = new MenuItem("Search", IconHelper.create("images/search_adv_16x16.png"),
                new SelectionListener<MenuEvent>()
                {
                    @Override
                    public void componentSelected(MenuEvent ce) {
                        Dispatcher.get().dispatch(AppEvents.EntitySearchView);
                    }
                });
        searchMenu.add(menuItemSearchEntity);
        MenuBarItem searchItem = new MenuBarItem("Search", searchMenu);


        // Custom Field menu
        Menu customFieldMenu = new Menu();
        MenuItem menuItemCustomFields = new MenuItem("Configure Fields",
                IconHelper.create("images/folder_edit.png"), new SelectionListener<MenuEvent>()
                {
                    @Override
                    public void componentSelected(MenuEvent ce) {
                        Dispatcher.get().dispatch(AppEvents.CustomFieldsConfigurationView);
                    }
                });
        customFieldMenu.add(menuItemCustomFields);

        MenuItem menuItemInitializeCustomConfiguration = new MenuItem("Regenerate Fields",
                IconHelper.create("images/application_view_list.png"), new SelectionListener<MenuEvent>()
                {
                    @Override
                    public void componentSelected(MenuEvent ce) {
                        MessageBox
                                .confirm(
                                        "Confirm",
                                        "The regenerate fields operation cannot be undone. Are you sure you want to regenerate fields?",
                                        listenRegenerateFields);
                        // Dispatcher.get().dispatch(AppEvents.InitializeCustomConfiguration);
                    }
                });
        customFieldMenu.add(menuItemInitializeCustomConfiguration);
        MenuBarItem customFieldItem = new MenuBarItem("Custom Fields", customFieldMenu);

        // Blocking menu
        // Check Blocking Algorithm Name
        blockingMenu = new Menu();
        menuItemBlocking = null;
        if (systemInfo != null && systemInfo.getBlockingAlgorithmName().contains("Traditional Blocking Algorithm (High Performance)")) {
            menuItemBlocking = new MenuItem("Blocking Configuration", // "Traditional Blocking High Performance",
                    IconHelper.create("images/bricks.png"), new SelectionListener<MenuEvent>()
                    {
                        @Override
                        public void componentSelected(MenuEvent ce) {
                            Dispatcher.get().dispatch(AppEvents.BlockingHPConfigurationView);
                        }
                    });
            blockingMenu.add(menuItemBlocking);
        } else if (systemInfo != null && systemInfo.getBlockingAlgorithmName().contains("Traditional Blocking Algorithm")) {
            menuItemBlocking = new MenuItem("Blocking Configuration", // "Traditional Blocking Configuration",
                    IconHelper.create("images/bricks.png"), new SelectionListener<MenuEvent>()
                    {
                        @Override
                        public void componentSelected(MenuEvent ce) {
                            Dispatcher.get().dispatch(AppEvents.BlockingConfigurationView);
                        }
                    });
            blockingMenu.add(menuItemBlocking);
        } else if (systemInfo != null
                && systemInfo.getBlockingAlgorithmName().contains("Sorted Neighborhood Blocking Algorithm")) {
            menuItemBlocking = new MenuItem("Blocking Configuration", // "Sorted Neighborhood Blocking",
                    IconHelper.create("images/bricks.png"), new SelectionListener<MenuEvent>()
                    {
                        @Override
                        public void componentSelected(MenuEvent ce) {
                            Dispatcher.get().dispatch(AppEvents.SortedNeighborhoodBlockingConfigurationView);
                        }
                    });
            blockingMenu.add(menuItemBlocking);
        } else if (systemInfo != null
                && systemInfo.getBlockingAlgorithmName().contains("Suffix Array Blocking Algorithm")) {
            menuItemBlocking = new MenuItem("Blocking Configuration", // "Suffix Array Blocking",
                    IconHelper.create("images/bricks.png"), new SelectionListener<MenuEvent>()
                    {
                        @Override
                        public void componentSelected(MenuEvent ce) {
                            Dispatcher.get().dispatch(AppEvents.SuffixArrayBlockingConfigurationView);
                        }
                    });
            blockingMenu.add(menuItemBlocking);
        }

        MenuItem menuItemRebuildBlockingIndex = new MenuItem("Rebuild Indexes",
                IconHelper.create("images/rebuild.png"), new SelectionListener<MenuEvent>()
                {
                    @Override
                    public void componentSelected(MenuEvent ce) {
                        MessageBox
                                .confirm(
                                        "Confirm",
                                        "The rebuild indexes operation cannot be undone. Are you sure you want to rebuild indexes?",
                                        listenRebuildBlockingIndex);
                        // Dispatcher.get().dispatch(AppEvents.RebuildBlockingIndex);
                    }
                });
        blockingMenu.add(menuItemRebuildBlockingIndex);
        MenuBarItem blockingItem = new MenuBarItem("Blocking", blockingMenu);


        // Matching menu
        matchingMenu = new Menu();
        menuItemMatching = null;
        if (systemInfo != null && systemInfo.getMatchingAlgorithmName().contains("Deterministic Matching Algorithm")) {
            menuItemMatching = new MenuItem("Matching Configuration", // "Deterministic Matching Configuration",
                    IconHelper.create("images/wrench_orange.png"), new SelectionListener<MenuEvent>()
                    {
                        @Override
                        public void componentSelected(MenuEvent ce) {
                            Dispatcher.get().dispatch(AppEvents.DeterministicMatchConfigurationView);
                        }
                    });
            matchingMenu.add(menuItemMatching);
        } else if (systemInfo != null
                && systemInfo.getMatchingAlgorithmName().contains("Probabilistic Matching Algorithm")) {
            menuItemMatching = new MenuItem("Matching Configuration", // "Probabilistic Matching Configuration",
                    IconHelper.create("images/wrench_orange.png"), new SelectionListener<MenuEvent>()
                    {
                        @Override
                        public void componentSelected(MenuEvent ce) {
                            Dispatcher.get().dispatch(AppEvents.MatchConfigurationView);
                        }
                    });
            matchingMenu.add(menuItemMatching);
        }

        MenuItem menuItemInitializeRepository = new MenuItem("Initialize Links",
                IconHelper.create("images/wrench.png"), new SelectionListener<MenuEvent>()
                {
                    @Override
                    public void componentSelected(MenuEvent ce) {
                        MessageBox
                                .confirm(
                                        "Confirm",
                                        "The initialize links operation cannot be undone. Are you sure you want to initialize links from the beginning using the underlying matching algorithm?",
                                        listenInitializeLinks);
                        // Dispatcher.get().dispatch(AppEvents.InitializeRepository);
                    }
                });
        matchingMenu.add(menuItemInitializeRepository);

        MenuItem menuItemLinkAllRecord = new MenuItem("Regenerate Links",
                IconHelper.create("images/world_link.png"), new SelectionListener<MenuEvent>()
                {
                    @Override
                    public void componentSelected(MenuEvent ce) {
                        MessageBox
                                .confirm(
                                        "Confirm",
                                        "The regenerate links operation cannot be undone. Are you sure you want to regenerate links from the beginning using the underlying matching algorithm?",
                                        listenRegenerateLinks);
                        // Dispatcher.get().dispatch(AppEvents.LinkAllRecordPairs);
                    }
                });
        matchingMenu.add(menuItemLinkAllRecord);

        MenuItem menuItemReviewLinks = new MenuItem("Review Links", IconHelper.create("images/layout_link.png"),
                new SelectionListener<MenuEvent>()
                {
                    @Override
                    public void componentSelected(MenuEvent ce) {
                        Dispatcher.get().dispatch(AppEvents.EntityLinkView);
                    }
                });
        matchingMenu.add(menuItemReviewLinks);
        MenuBarItem matchingItem = new MenuBarItem("Matching", matchingMenu); 


        // Admin menu
        Menu adminMenu = new Menu();
        MenuItem menuItemEventNotification = new MenuItem("Event Notification",
                IconHelper.create("images/email_open_image.png"), new SelectionListener<MenuEvent>()
                {
                    @Override
                    public void componentSelected(MenuEvent ce) {
                        Dispatcher.get().dispatch(AppEvents.EventNotificationView);
                    }
                });
        adminMenu.add(menuItemEventNotification);

        MenuItem menuItemDataProfile = new MenuItem("Data Profile Viewer", IconHelper.create("images/script.png"),
                new SelectionListener<MenuEvent>()
                {
                    @Override
                    public void componentSelected(MenuEvent ce) {
                        Dispatcher.get().dispatch(AppEvents.DataProfileListView);
                    }
                });
        adminMenu.add(menuItemDataProfile);

        MenuItem menuItemEventViewer = new MenuItem("Event Viewer",
                IconHelper.create("images/search_icon_16x16.png"), new SelectionListener<MenuEvent>()
                {
                    @Override
                    public void componentSelected(MenuEvent ce) {
                        Dispatcher.get().dispatch(AppEvents.AuditEventEntryView);
                    }
                });
        adminMenu.add(menuItemEventViewer);

        MenuItem menuItemGlobalIdentifiers = new MenuItem("Assign Global Identifiers",
                IconHelper.create("images/computer.png"), new SelectionListener<MenuEvent>()
                {
                    @Override
                    public void componentSelected(MenuEvent ce) {
                        Dispatcher.get().dispatch(AppEvents.AssignGlobalIdentifier);
                    }
                });
        adminMenu.add(menuItemGlobalIdentifiers);

        MenuItem menuItemJobQueue = new MenuItem("Job Queue Entries",
                IconHelper.create("images/pictures.png"), new SelectionListener<MenuEvent>()
                {
                    @Override
                    public void componentSelected(MenuEvent ce) {
                        Dispatcher.get().dispatch(AppEvents.JobQueueView);
                    }
                });
        adminMenu.add(menuItemJobQueue);

        MenuItem menuItemStartPIXPDQ = new MenuItem("Start PIX/PDQ Server", IconHelper.create("images/clock_play.png"),
                new SelectionListener<MenuEvent>()
                {
                    @Override
                    public void componentSelected(MenuEvent ce) {
                        Dispatcher.get().dispatch(AppEvents.AdminStartPixPdqServer);
                    }
                });
        adminMenu.add(menuItemStartPIXPDQ);

        MenuItem menuItemStopPIXPDQ = new MenuItem("Stop PIX/PDQ Server", IconHelper.create("images/clock_stop.png"),
                new SelectionListener<MenuEvent>()
                {
                    @Override
                    public void componentSelected(MenuEvent ce) {
                        Dispatcher.get().dispatch(AppEvents.AdminStopPixPdqServer);
                    }
                });
        adminMenu.add(menuItemStopPIXPDQ);
        

        MenuItem menuItemCreateEntityIndexes = new MenuItem("Create Entity Indexes",
                IconHelper.create("images/pictures.png"), new SelectionListener<MenuEvent>()
                {
                    @Override
                    public void componentSelected(MenuEvent ce) {
                        Dispatcher.get().dispatch(AppEvents.CreateEntityIndexes);
                    }
                });
        adminMenu.add(menuItemCreateEntityIndexes);

        MenuItem menuItemDropEntityIndexes = new MenuItem("Drop Entity Indexes",
                IconHelper.create("images/pictures.png"), new SelectionListener<MenuEvent>()
                {
                    @Override
                    public void componentSelected(MenuEvent ce) {
                        Dispatcher.get().dispatch(AppEvents.DropEntityIndexes);
                    }
                });
        adminMenu.add(menuItemDropEntityIndexes);
        
        CheckMenuItem menuItemInfoPanel = new CheckMenuItem("Information Panel");
        Listener<MenuEvent> menuListener = new Listener<MenuEvent>() {
            public void handleEvent(MenuEvent be) {
              if (be.getType() == Events.CheckChange) {
                  if (be.isChecked()) {
                      // Info.display("Info Panel: ", "checked");
                      LayoutContainer wrapper = (LayoutContainer) Registry.get(Constants.SOUTH_PANEL);
                      wrapper.setVisible(true);

                      Dispatcher.get().dispatch(AppEvents.InformationPanelView);

                  } else {
                      // Info.display("Info Panel: ", "unchecked");
                      LayoutContainer wrapper = (LayoutContainer) Registry.get(Constants.SOUTH_PANEL);
                      wrapper.setVisible(false);
                  }
              }
            }
          };
        menuItemInfoPanel.addListener(Events.CheckChange, menuListener);
        adminMenu.add(menuItemInfoPanel);
        MenuBarItem adminItem = new MenuBarItem("Admin", adminMenu);



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
        bar.add(fileItem);
        bar.add(editItem);
        bar.add(searchItem);
        bar.add(customFieldItem);
        bar.add(blockingItem);
        bar.add(matchingItem);
        bar.add(adminItem);
        bar.add(securityItem);
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

        Button buttonEditEntityModel = new Button();
        buttonEditEntityModel.setToolTip("Edit Entity Model");
        buttonEditEntityModel.setIcon(IconHelper.create("images/entity.png"));
        buttonEditEntityModel.addSelectionListener(new SelectionListener<ButtonEvent>()
        {
            @Override
            public void componentSelected(ButtonEvent ce) {
                Dispatcher.get().dispatch(AppEvents.EntityAttributeView);
            }
        });
        toolbar.add(buttonEditEntityModel);

        Button buttonEditIdentifierDomains = new Button();
        buttonEditIdentifierDomains.setToolTip("Edit Identifier Domains");
        buttonEditIdentifierDomains.setIcon(IconHelper.create("images/key.png"));
        buttonEditIdentifierDomains.addSelectionListener(new SelectionListener<ButtonEvent>()
        {
            @Override
            public void componentSelected(ButtonEvent ce) {
                Dispatcher.get().dispatch(AppEvents.ManageIdentifierDomainView);
            }
        });
        toolbar.add(buttonEditIdentifierDomains);

        Button buttonAddRecord = new Button();
        buttonAddRecord.setToolTip("Add Record");
        buttonAddRecord.setIcon(IconHelper.create("images/add.png"));
        buttonAddRecord.addSelectionListener(new SelectionListener<ButtonEvent>()
        {
            @Override
            public void componentSelected(ButtonEvent ce) {
                Dispatcher.get().dispatch(AppEvents.EntityAddView);
            }
        });
        toolbar.add(buttonAddRecord);
        toolbar.add(new SeparatorToolItem());
        
        Button buttonEntityFieldSearch = new Button();
        buttonEntityFieldSearch.setToolTip("Fixed Search");
        buttonEntityFieldSearch.setIcon(IconHelper.create("images/search_icon_16x16.png"));
        buttonEntityFieldSearch.addSelectionListener(new SelectionListener<ButtonEvent>()
        {
            @Override
            public void componentSelected(ButtonEvent ce) {
                Dispatcher.get().dispatch(AppEvents.EntityFixedSearchView);
            }
        });
        toolbar.add(buttonEntityFieldSearch);
        toolbar.add(new SeparatorToolItem());
        
        Button buttonEntitySearch = new Button();
        buttonEntitySearch.setToolTip("Search");
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
        buttonEventNotifications.setToolTip("Event Notification");
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

        Button buttonDataProfileViewer = new Button();
        buttonDataProfileViewer.setToolTip("Data Profile Viewer");
        buttonDataProfileViewer.setIcon(IconHelper.create("images/script.png"));
        buttonDataProfileViewer.addSelectionListener(new SelectionListener<ButtonEvent>()
        {
            @Override
            public void componentSelected(ButtonEvent ce) {
                Dispatcher.get().dispatch(AppEvents.DataProfileListView);
            }
        });
        toolbar.add(buttonDataProfileViewer);
        toolbar.add(new SeparatorToolItem());

        Button buttonManageUsers = new Button();
        buttonManageUsers.setToolTip("Manage Users");
        buttonManageUsers.setIcon(IconHelper.create("images/user_group.png"));
        buttonManageUsers.addSelectionListener(new SelectionListener<ButtonEvent>()
        {
            @Override
            public void componentSelected(ButtonEvent ce) {
                Dispatcher.get().dispatch(AppEvents.ManageUserView);
            }
        });
        toolbar.add(buttonManageUsers);

        Button buttonManageRoles = new Button();
        buttonManageRoles.setToolTip("Manage Roles");
        buttonManageRoles.setIcon(IconHelper.create("images/user_role.png"));
        buttonManageRoles.addSelectionListener(new SelectionListener<ButtonEvent>()
        {
            @Override
            public void componentSelected(ButtonEvent ce) {
                Dispatcher.get().dispatch(AppEvents.ManageRoleView);
            }
        });
        toolbar.add(buttonManageRoles);
        toolbar.add(new SeparatorToolItem());

        Button buttonAbout = new Button();
        buttonAbout.setToolTip("About OpenEMPI");
        buttonAbout.setIcon(IconHelper.create("images/help.png"));
        buttonAbout.addSelectionListener(new SelectionListener<ButtonEvent>()
        {
            @Override
            public void componentSelected(ButtonEvent ce) {
                buildAboutDialog();
                aboutDialog.show();
            }
        });
        toolbar.add(buttonAbout);

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
                buttonEntityFieldSearch.disable();
                menuItemFieldSearchEntity.disable();
                buttonEntitySearch.disable();
                menuItemSearchEntity.disable();
            }

            permission = permissions.get(Permission.RECORD_ADD);
            if (permission == null) {
                buttonAddRecord.disable();
                menuItemAdd.disable();
            }

            permission = permissions.get(Permission.RECORD_LINKS_REVIEW);
            if (permission == null) {
                buttonReviewLinks.disable();
                menuItemReviewLinks.disable();
            }

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
                buttonEditIdentifierDomains.disable();
                menuItemIdentifierDomains.disable();
            }

            permission = permissions.get(Permission.PIXPDQ_MANAGE);
            if (permission == null) {
                menuItemStartPIXPDQ.disable();
                menuItemStopPIXPDQ.disable();
            }

            permission = permissions.get(Permission.USER_VIEW);
            if (permission == null) {
                menuItemProfile.disable();

                buttonManageUsers.disable();
                buttonManageRoles.disable();
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
        cp.setHeading("About OpenEMPI Entity Edition");
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

    final Listener<MessageBoxEvent> listenInitializeLinks = new Listener<MessageBoxEvent>()
    {
        public void handleEvent(MessageBoxEvent ce) {
            Button btn = ce.getButtonClicked();
            // Info.display("MessageBox1 ", "The '{0}' button was pressed", btn.getText());
            if (btn.getText().equals("Yes")) {
                Dispatcher.get().dispatch(AppEvents.InitializeRepository);
            }
        }
    };

    final Listener<MessageBoxEvent> listenRegenerateLinks = new Listener<MessageBoxEvent>()
    {
        public void handleEvent(MessageBoxEvent ce) {
            Button btn = ce.getButtonClicked();
            // Info.display("MessageBox1 ", "The '{0}' button was pressed", btn.getText());
            if (btn.getText().equals("Yes")) {
                Dispatcher.get().dispatch(AppEvents.LinkAllRecordPairs);
            }
        }
    };

    final Listener<MessageBoxEvent> listenRegenerateFields = new Listener<MessageBoxEvent>()
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
        } else if (event.getType() == AppEvents.UpdateConfigurationMenu) {

            SystemConfigurationWeb systemInfo = (SystemConfigurationWeb) Registry.get(Constants.SYSTEM_CONFIGURATION_INFO);
            // Info.display("Information",""+systemInfo.getBlockingAlgorithmName());
            // Info.display("Information",""+systemInfo.getMatchingAlgorithmName());

            // Blocking Algorithm
            int index = 0;
            if (menuItemBlocking != null) {
                index = blockingMenu.indexOf(menuItemBlocking);
                blockingMenu.remove(menuItemBlocking);
            }
            if (systemInfo != null && systemInfo.getBlockingAlgorithmName().contains("Traditional Blocking Algorithm (High Performance)")) {
                menuItemBlocking = new MenuItem("Blocking Configuration",// "Traditional Blocking High Performance",
                        IconHelper.create("images/bricks.png"), new SelectionListener<MenuEvent>()
                        {
                            @Override
                            public void componentSelected(MenuEvent ce) {
                                Dispatcher.get().dispatch(AppEvents.BlockingHPConfigurationView);
                            }
                        });
                blockingMenu.insert(menuItemBlocking, index);
            } else if (systemInfo != null && systemInfo.getBlockingAlgorithmName().contains("Traditional Blocking Algorithm")) {
                    menuItemBlocking = new MenuItem("Blocking Configuration",// "Traditional Blocking Configuration",
                            IconHelper.create("images/bricks.png"), new SelectionListener<MenuEvent>()
                            {
                                @Override
                                public void componentSelected(MenuEvent ce) {
                                    Dispatcher.get().dispatch(AppEvents.BlockingConfigurationView);
                                }
                            });
                    blockingMenu.insert(menuItemBlocking, index);
            } else if (systemInfo != null
                    && systemInfo.getBlockingAlgorithmName().contains("Sorted Neighborhood Blocking Algorithm")) {
                menuItemBlocking = new MenuItem("Blocking Configuration",// "Sorted Neighborhood Blocking",
                        IconHelper.create("images/bricks.png"), new SelectionListener<MenuEvent>()
                        {
                            @Override
                            public void componentSelected(MenuEvent ce) {
                                Dispatcher.get().dispatch(AppEvents.SortedNeighborhoodBlockingConfigurationView);
                            }
                        });
                blockingMenu.insert(menuItemBlocking, index);
            } else if (systemInfo != null
                    && systemInfo.getBlockingAlgorithmName().contains("Suffix Array Blocking Algorithm")) {
                menuItemBlocking = new MenuItem("Blocking Configuration",// "Suffix Array Blocking",
                        IconHelper.create("images/bricks.png"), new SelectionListener<MenuEvent>()
                        {
                            @Override
                            public void componentSelected(MenuEvent ce) {
                                Dispatcher.get().dispatch(AppEvents.SuffixArrayBlockingConfigurationView);
                            }
                        });
                blockingMenu.insert(menuItemBlocking, index);
            }

            // Matching Algorithm
            index = 0;
            if (menuItemMatching != null) {
                index = matchingMenu.indexOf(menuItemMatching);
                matchingMenu.remove(menuItemMatching);
            }

            if (systemInfo != null && systemInfo.getMatchingAlgorithmName().contains("Deterministic Matching Algorithm")) {
                menuItemMatching = new MenuItem("Matching Configuration",// "Deterministic Matching Configuration",
                        IconHelper.create("images/wrench_orange.png"), new SelectionListener<MenuEvent>()
                        {
                            @Override
                            public void componentSelected(MenuEvent ce) {
                                Dispatcher.get().dispatch(AppEvents.DeterministicMatchConfigurationView);
                            }
                        });
                matchingMenu.insert(menuItemMatching, index);
            } else if (systemInfo != null
                    && systemInfo.getMatchingAlgorithmName().contains("Probabilistic Matching Algorithm")) {
                menuItemMatching = new MenuItem("Matching Configuration",// "Probabilistic Matching Configuration",
                        IconHelper.create("images/wrench_orange.png"), new SelectionListener<MenuEvent>()
                        {
                            @Override
                            public void componentSelected(MenuEvent ce) {
                                Dispatcher.get().dispatch(AppEvents.MatchConfigurationView);
                            }
                        });
                matchingMenu.insert(menuItemMatching, index);
            }
        }
    }
}
