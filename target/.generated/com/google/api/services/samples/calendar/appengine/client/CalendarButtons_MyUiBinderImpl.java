package com.google.api.services.samples.calendar.appengine.client;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Element;
import com.google.gwt.safehtml.client.SafeHtmlTemplates;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiBinderUtil;
import com.google.gwt.user.client.ui.HorizontalPanel;

public class CalendarButtons_MyUiBinderImpl implements UiBinder<com.google.gwt.user.client.ui.HorizontalPanel, com.google.api.services.samples.calendar.appengine.client.CalendarButtons>, com.google.api.services.samples.calendar.appengine.client.CalendarButtons.MyUiBinder {

  interface Template extends SafeHtmlTemplates {
    @Template("Update")
    SafeHtml html1();
     
    @Template("Delete")
    SafeHtml html2();
     
  }

  Template template = GWT.create(Template.class);

  public com.google.gwt.user.client.ui.HorizontalPanel createAndBindUi(final com.google.api.services.samples.calendar.appengine.client.CalendarButtons owner) {

    com.google.api.services.samples.calendar.appengine.client.CalendarButtons_MyUiBinderImpl_GenBundle clientBundleFieldNameUnlikelyToCollideWithUserSpecifiedFieldOkay = (com.google.api.services.samples.calendar.appengine.client.CalendarButtons_MyUiBinderImpl_GenBundle) GWT.create(com.google.api.services.samples.calendar.appengine.client.CalendarButtons_MyUiBinderImpl_GenBundle.class);
    com.google.gwt.user.client.ui.Button updateButton = (com.google.gwt.user.client.ui.Button) GWT.create(com.google.gwt.user.client.ui.Button.class);
    com.google.gwt.user.client.ui.Button deleteButton = (com.google.gwt.user.client.ui.Button) GWT.create(com.google.gwt.user.client.ui.Button.class);
    com.google.gwt.user.client.ui.HorizontalPanel f_HorizontalPanel1 = (com.google.gwt.user.client.ui.HorizontalPanel) GWT.create(com.google.gwt.user.client.ui.HorizontalPanel.class);

    updateButton.setHTML(template.html1().asString());
    f_HorizontalPanel1.add(updateButton);
    deleteButton.setHTML(template.html2().asString());
    f_HorizontalPanel1.add(deleteButton);



    final com.google.gwt.event.dom.client.ClickHandler handlerMethodWithNameVeryUnlikelyToCollideWithUserFieldNames1 = new com.google.gwt.event.dom.client.ClickHandler() {
      public void onClick(com.google.gwt.event.dom.client.ClickEvent event) {
        owner.handleDelete(event);
      }
    };
    deleteButton.addClickHandler(handlerMethodWithNameVeryUnlikelyToCollideWithUserFieldNames1);

    final com.google.gwt.event.dom.client.ClickHandler handlerMethodWithNameVeryUnlikelyToCollideWithUserFieldNames2 = new com.google.gwt.event.dom.client.ClickHandler() {
      public void onClick(com.google.gwt.event.dom.client.ClickEvent event) {
        owner.handleUpdate(event);
      }
    };
    updateButton.addClickHandler(handlerMethodWithNameVeryUnlikelyToCollideWithUserFieldNames2);

    owner.deleteButton = deleteButton;
    owner.updateButton = updateButton;

    return f_HorizontalPanel1;
  }
}
