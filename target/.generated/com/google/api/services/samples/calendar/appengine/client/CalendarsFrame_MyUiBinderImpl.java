package com.google.api.services.samples.calendar.appengine.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiBinderUtil;
import com.google.gwt.user.client.ui.VerticalPanel;

public class CalendarsFrame_MyUiBinderImpl implements UiBinder<com.google.gwt.user.client.ui.VerticalPanel, com.google.api.services.samples.calendar.appengine.client.CalendarsFrame>, com.google.api.services.samples.calendar.appengine.client.CalendarsFrame.MyUiBinder {

  interface Template extends SafeHtmlTemplates {
    @Template("Add")
    SafeHtml html1();
     
  }

  Template template = GWT.create(Template.class);

  public com.google.gwt.user.client.ui.VerticalPanel createAndBindUi(final com.google.api.services.samples.calendar.appengine.client.CalendarsFrame owner) {

    com.google.api.services.samples.calendar.appengine.client.CalendarsFrame_MyUiBinderImpl_GenBundle clientBundleFieldNameUnlikelyToCollideWithUserSpecifiedFieldOkay = (com.google.api.services.samples.calendar.appengine.client.CalendarsFrame_MyUiBinderImpl_GenBundle) GWT.create(com.google.api.services.samples.calendar.appengine.client.CalendarsFrame_MyUiBinderImpl_GenBundle.class);
    com.google.gwt.user.client.ui.Label f_Label2 = (com.google.gwt.user.client.ui.Label) GWT.create(com.google.gwt.user.client.ui.Label.class);
    com.google.gwt.user.client.ui.Label f_Label4 = (com.google.gwt.user.client.ui.Label) GWT.create(com.google.gwt.user.client.ui.Label.class);
    com.google.gwt.user.client.ui.TextBox addTextBox = (com.google.gwt.user.client.ui.TextBox) GWT.create(com.google.gwt.user.client.ui.TextBox.class);
    com.google.gwt.user.client.ui.Button addButton = (com.google.gwt.user.client.ui.Button) GWT.create(com.google.gwt.user.client.ui.Button.class);
    com.google.gwt.user.client.ui.HorizontalPanel f_HorizontalPanel3 = (com.google.gwt.user.client.ui.HorizontalPanel) GWT.create(com.google.gwt.user.client.ui.HorizontalPanel.class);
    com.google.gwt.user.client.ui.FlexTable calendarsTable = (com.google.gwt.user.client.ui.FlexTable) GWT.create(com.google.gwt.user.client.ui.FlexTable.class);
    com.google.gwt.user.client.ui.DecoratorPanel f_DecoratorPanel5 = (com.google.gwt.user.client.ui.DecoratorPanel) GWT.create(com.google.gwt.user.client.ui.DecoratorPanel.class);
    com.google.gwt.user.client.ui.VerticalPanel f_VerticalPanel1 = (com.google.gwt.user.client.ui.VerticalPanel) GWT.create(com.google.gwt.user.client.ui.VerticalPanel.class);

    f_Label2.setText("Google Calendar API Web Sample");
    f_Label2.setStyleName("title");
    f_VerticalPanel1.add(f_Label2);
    f_Label4.setText("New Calendar:");
    f_HorizontalPanel3.add(f_Label4);
    f_HorizontalPanel3.add(addTextBox);
    addButton.setHTML(template.html1().asString());
    addButton.setEnabled(false);
    f_HorizontalPanel3.add(addButton);
    f_VerticalPanel1.add(f_HorizontalPanel3);
    calendarsTable.setStyleName("calendarsTable");
    calendarsTable.setCellPadding(3);
    f_DecoratorPanel5.add(calendarsTable);
    f_VerticalPanel1.add(f_DecoratorPanel5);
    f_VerticalPanel1.setSpacing(5);



    final com.google.gwt.event.dom.client.ClickHandler handlerMethodWithNameVeryUnlikelyToCollideWithUserFieldNames1 = new com.google.gwt.event.dom.client.ClickHandler() {
      public void onClick(com.google.gwt.event.dom.client.ClickEvent event) {
        owner.handleAdd(event);
      }
    };
    addButton.addClickHandler(handlerMethodWithNameVeryUnlikelyToCollideWithUserFieldNames1);

    owner.addButton = addButton;
    owner.addTextBox = addTextBox;
    owner.calendarsTable = calendarsTable;

    return f_VerticalPanel1;
  }
}
