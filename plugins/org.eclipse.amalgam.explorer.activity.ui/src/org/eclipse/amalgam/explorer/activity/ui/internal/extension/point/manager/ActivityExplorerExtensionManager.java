/*******************************************************************************
 * Copyright (c)  2006, 2016 THALES GLOBAL SERVICES.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Thales - initial API and implementation
 *******************************************************************************/
package org.eclipse.amalgam.explorer.activity.ui.internal.extension.point.manager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.activity.InvalidActivityException;

import org.eclipse.amalgam.explorer.activity.ui.ActivityExplorerActivator;
import org.eclipse.amalgam.explorer.activity.ui.IImageKeys;
import org.eclipse.amalgam.explorer.activity.ui.api.editor.pages.CommonActivityExplorerPage;
import org.eclipse.amalgam.explorer.activity.ui.api.editor.predicates.IPredicate;
import org.eclipse.amalgam.explorer.activity.ui.internal.ActivityExplorerConstants;
import org.eclipse.amalgam.explorer.activity.ui.internal.exceptions.InvalidActivityExplorerIndexException;
import org.eclipse.amalgam.explorer.activity.ui.internal.util.ActivityExplorerLoggerService;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IContributor;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.forms.events.IHyperlinkListener;
import org.osgi.framework.Bundle;

public class ActivityExplorerExtensionManager {

  /**
   * Extension point declaration
   */
  public static final String PROVIDER_PAGES_EXT = "org.eclipse.amalgam.explorer.activity.ui.pagesProvider"; //$NON-NLS-1$
  public static final String PROVIDER_SECTIONS_EXT = "org.eclipse.amalgam.explorer.activity.ui.sectionsProvider"; //$NON-NLS-1$
  public static final String PROVIDER_ACTIVITIES_EXT = "org.eclipse.amalgam.explorer.activity.ui.activitiesProvider"; //$NON-NLS-1$
  /**
   * Page Attributes
   */
  public static final String PAGE = "Page"; //$NON-NLS-1$
  public static final String ATT_CLASS = "class"; //$NON-NLS-1$
  public static final String ATT_TAB_NAME = "tabName"; //$NON-NLS-1$
  public static final String ATT_TITLE = "title"; //$NON-NLS-1$
  public static final String ATT_IMAGE = "image"; //$NON-NLS-1$
  public static final String ATT_IMAGE_OFF = "imageOff"; //$NON-NLS-1$
  public static final String ATT_IMAGE_ON = "imageOn"; //$NON-NLS-1$
  public static final String ATT_ID = "id"; //$NON-NLS-1$
  public static final String ATT_NAME = "name"; //$NON-NLS-1$
  public static final String DESCRIPTION = "Description"; //$NON-NLS-1$
  public static final String ATT_INDEX = "index"; //$NON-NLS-1$
  public static final String ATT_VIEWER = "viewer"; //$NON-NLS-1$

  public static final String OVERVIEW = "Overview"; //$NON-NLS-1$
  public static final String ATT_OVERVIEW_IMG_ON = "imageOn"; //$NON-NLS-1$
  public static final String ATT_OVERVIEW_IMG_OFF = "imageOff"; //$NON-NLS-1$

  /**
   * Section Attributes
   */
  public static final String SECTION = "Section"; //$NON-NLS-1$
  public static final String ATT_EXPANDED = "expanded"; //$NON-NLS-1$
  public static final String ATT_FILTERING = "filtering"; //$NON-NLS-1$
  /**
   * Activity Attributes
   */
  public static final String ACTIVITY = "Activity"; //$NON-NLS-1$
  public static final String ATT_DIAGRAM_REPRESENTATION = "diagram"; //$NON-NLS-1$
  public static final String ATT_VIEWPOINT = "viewpoint"; //$NON-NLS-1$
  public static final String ATT_SECTION_PAGE_ID = "pageId"; //$NON-NLS-1$
  public static final String ATT_ACTIVITY_SECTION_ID = "sectionId"; //$NON-NLS-1$

  /**
   * Predicate
   */
  public static final String PREDICATE = "Predicate"; //$NON-NLS-1$

  /**
   * get all Page Providers
   * 
   * @return
   */
  public static IConfigurationElement[] getAllProviderExtensions() {
    return getExtensionElt(PROVIDER_PAGES_EXT);
  }

  private static IConfigurationElement[] getExtensionElt(String id) {
    IExtensionPoint point = getExtensionPoint(id);
    IExtension[] extensions = point.getExtensions();
    ArrayList<IConfigurationElement> configElements = new ArrayList<IConfigurationElement>();

    for (IExtension extension : extensions) {
      configElements.addAll(Arrays.asList(extension.getConfigurationElements()));
    }

    return configElements.toArray(new IConfigurationElement[] {});
  }

  public static IExtensionPoint getExtensionPoint(String id) {
    return Platform.getExtensionRegistry().getExtensionPoint(id);
  }

  private static CommonActivityExplorerPage getPage(IConfigurationElement element) throws CoreException, InvalidActivityExplorerIndexException {
    CommonActivityExplorerPage page = null;

    if (element != null) {
      CommonActivityExplorerPage.setID(getId(element));
      if (element.getAttribute(ATT_CLASS) != null) {

        page = (CommonActivityExplorerPage) element.createExecutableExtension(ATT_CLASS);
      } else {
        page = new CommonActivityExplorerPage();
        ((IExecutableExtension) page).setInitializationData(element, ATT_CLASS, null);
      }
    }
    
    return accept(page, element);
  }

  /**
   * check if the page is valid
   * @param page
   * @return the page, otherways, launch an exception
   * @throws InvalidActivityException
   */
  private static CommonActivityExplorerPage accept(CommonActivityExplorerPage page, IConfigurationElement element) 
		  throws InvalidActivityExplorerIndexException {
	
	//Never accept null page
	if (page == null){
		StringBuilder message = new StringBuilder(); 
		message.append("ActivityExplorerExtensionManager.accept(...) _ "); //$NON-NLS-1$
		message.append("An error occured while instantianting the class of contribution "); //$NON-NLS-1$
		message.append(getId(element));
		
		throw new NullPointerException(message.toString());
	}
	  
	//Never accept negatif index for pages
	if (page.getPosition() < 0) {
		
		StringBuilder message = new StringBuilder();
		message.append("ActivityExplorerExtensionManager.accept(...) _ "); //$NON-NLS-1$
		message.append("The page "); //$NON-NLS-1$
		message.append(page.getId());
		message.append(" has negatif index. "); //$NON-NLS-1$
		message.append("Only pages win an index upper or equal to zero are allowed"); //$NON-NLS-1$
		
		throw new InvalidActivityExplorerIndexException(message.toString());
	}
	return page;
  }

  public static List<CommonActivityExplorerPage> getAllPages() {
    List<CommonActivityExplorerPage> providers = new ArrayList<CommonActivityExplorerPage>();

    List<IConfigurationElement> extensions = Arrays.asList(getExtensionElt(PROVIDER_PAGES_EXT));
    
    CommonActivityExplorerPage page;
    for (IConfigurationElement extension : extensions) {
      page = null;
      try {
        page = getPage(extension);
		providers.add(page);
      } catch (CoreException e) {
        
    	  ActivityExplorerLoggerService.getInstance().log(IStatus.ERROR, Messages.ActivityExplorerExtensionManager_0, e);
    	  
      } catch (InvalidActivityExplorerIndexException e) {
    	
    	  ActivityExplorerActivator
          .getDefault()
          .getLog()
          .log(new Status(IStatus.WARNING, ActivityExplorerActivator.ID, e.getMessage()));
	  
      } catch (NumberFormatException e){
		  
    	  StringBuilder message = new StringBuilder();
		  message.append("ActivityExplorerExtensionManager.getAllPages(...) _ "); //$NON-NLS-1$
		  message.append("The contribution "); //$NON-NLS-1$
		  message.append(getId(extension));
	 	  message.append(" has wrong index format ("); //$NON-NLS-1$
		  message.append(getIndex(extension));
		  message.append("). Only 0 or positive integers are valid"); //$NON-NLS-1$
		  
		  ActivityExplorerLoggerService.getInstance().log(IStatus.ERROR, message.toString(), e);
		  
      } catch (NullPointerException e){
		
    	  ActivityExplorerLoggerService.getInstance().log(IStatus.ERROR, e.getMessage(), null);
	  
      } catch (Throwable e){
    	  //Unknown error from contribution
    	  
    	  StringBuilder message = new StringBuilder();
		  message.append("ActivityExplorerExtensionManager.getAllPages(...) _ "); //$NON-NLS-1$
		  message.append("Unknown error occurred from contribution "); //$NON-NLS-1$
		  message.append(getId(extension)); 
	 	  message.append(". See the exception stack for more details"); //$NON-NLS-1$
		  
	 	 ActivityExplorerLoggerService.getInstance().log(IStatus.ERROR, message.toString(), e);
      }
    }
    return providers;
  }

  public static List<IConfigurationElement> getAllPagesElt() {
    List<IConfigurationElement> extensions = Arrays.asList(getExtensionElt(PROVIDER_PAGES_EXT));

    return extensions;
  }

  public static List<IConfigurationElement> getAllSectionsExtensions() {
    List<IConfigurationElement> extensions = Arrays.asList(getExtensionElt(PROVIDER_SECTIONS_EXT));

    return extensions;
  }

  public static List<IConfigurationElement> getAllSectionsExtensionForPageId(String id) {
    List<IConfigurationElement> list = new ArrayList<IConfigurationElement>();
    for (IConfigurationElement elt : getAllSectionsExtensions()) {
      if (getPageId(elt).equals(id))
        list.add(elt);
    }
    return list;
  }

  public static List<IConfigurationElement> getAllActivitiesExtensionForSectionId(String id) {
    List<IConfigurationElement> list = new ArrayList<IConfigurationElement>();
    for (IConfigurationElement elt : getAllActivitiesExtensions()) {
      if (getSectionId(elt).equals(id))
        list.add(elt);
    }
    return list;
  }

  public static List<IConfigurationElement> getAllActivitiesExtensions() {
    List<IConfigurationElement> extensions = Arrays.asList(getExtensionElt(PROVIDER_ACTIVITIES_EXT));

    return extensions;
  }

  public static CommonActivityExplorerPage getPageById(String id) {
    CommonActivityExplorerPage result = null;
    for (IConfigurationElement page : Arrays.asList(getExtensionElt(PROVIDER_PAGES_EXT))) {
      if (getId(page).equals(id)) {
        try {
          result = getPage(page);
        } catch (CoreException e) {
        	
        	StringBuilder loggerMessage = new StringBuilder("ActivityExplorerExtensionManager.getPageById(..) _ "); //$NON-NLS-1$
            loggerMessage.append(e.getMessage());
            
            ActivityExplorerLoggerService.getInstance().log(IStatus.ERROR, loggerMessage.toString(), e);
            
        } catch (InvalidActivityExplorerIndexException e) {
        	
        	ActivityExplorerLoggerService.getInstance().log(IStatus.WARNING, e.toString(), e);
        	
		} catch (NumberFormatException e){
			  
			  StringBuilder message = new StringBuilder();
			  message.append("ActivityExplorerExtensionManager.getAllPages(...) _ "); //$NON-NLS-1$
			  message.append("The contribution "); //$NON-NLS-1$
			  message.append(getId(page));
		 	  message.append(" has wrong index format ("); //$NON-NLS-1$
			  message.append(getIndex(page));
			  message.append("). Only 0 or positive integers are valid"); //$NON-NLS-1$
			  
			  ActivityExplorerLoggerService.getInstance().log(IStatus.ERROR, message.toString(), e);
			  
		 } catch (NullPointerException e){
		     
			 ActivityExplorerLoggerService.getInstance().log(IStatus.WARNING, e.getMessage(), e);
			 
		 } catch (Throwable e){
	    	  //Unknown error from contributions
	    	  
	    	  StringBuilder message = new StringBuilder();
			  message.append("ActivityExplorerExtensionManager.getAllPages(...) _ "); //$NON-NLS-1$
			  message.append("Unknown error occurred from contribution "); //$NON-NLS-1$
			  message.append(getId(page));
		 	  message.append(". See the exception stack for more details"); //$NON-NLS-1$
		 	  
		 	 ActivityExplorerLoggerService.getInstance().log(IStatus.WARNING, message.toString(), e);
			  
	      }
      }
    }
    return result;
  }

  public static String getPageId(IConfigurationElement element) {
    return element.getAttribute(ATT_SECTION_PAGE_ID);
  }

  public static String getSectionId(IConfigurationElement element) {
    return element.getAttribute(ATT_ACTIVITY_SECTION_ID);
  }

  public static List<IConfigurationElement> getSectionsFromPageId(String id) {
    List<IConfigurationElement> sections = new ArrayList<IConfigurationElement>();
    for (IConfigurationElement page : Arrays.asList(getExtensionElt(PROVIDER_PAGES_EXT))) {
      if (getId(page).equals(id)) {
        sections.addAll(getSections(page));
      }
    }

    return sections;
  }

  public static List<IConfigurationElement> getActivitiesFromPageId(String id) {
    List<IConfigurationElement> sections = new ArrayList<IConfigurationElement>();
    for (IConfigurationElement page : Arrays.asList(getExtensionElt(PROVIDER_PAGES_EXT))) {
      if (getId(page).equals(id)) {
        sections.addAll(getSections(page));
      }
    }

    return sections;
  }

  public static String getId(IConfigurationElement element) {
    return element.getAttribute(ATT_ID);
  }

  public static String getName(IConfigurationElement element) {
    String att = ATT_NAME;

    if (element.getName().equals(PAGE))
      att = ATT_TITLE;
    String name = element.getAttribute(att);
    return name == null ? ActivityExplorerConstants.NO_NAME : Platform.getResourceString(Platform.getBundle(element.getContributor().getName()), name);
  }

  public static String getTitle(IConfigurationElement element) {
    String title = element.getAttribute(ATT_TITLE);
    return title == null ? ActivityExplorerConstants.NO_TITLE : Platform.getResourceString(Platform.getBundle(element.getContributor().getName()), title);
  }

  public static String getTabName(IConfigurationElement element) {
    return element.getAttribute(ATT_TAB_NAME);
  }

  public static String getIndex(IConfigurationElement element) {
    return element.getAttribute(ATT_INDEX);
  }

  public static Image getImage(IConfigurationElement element) {

    Image image = null;
    String img = element.getAttribute(ATT_IMAGE);
    if (img != null) {
      String pluginId = ActivityExplorerExtensionManager.getPluginId(element);
      image = ActivityExplorerActivator.getDefault().getImage(pluginId, img);
    }
    return image;
  }

  public static Image getImageOff(IConfigurationElement element) {

    Image image = null;
    String img = element.getAttribute(ATT_IMAGE_OFF);
    if (img != null) {
      String pluginId = ActivityExplorerExtensionManager.getPluginId(element);
      image = ActivityExplorerActivator.getDefault().getImage(pluginId, img);
    }
    return image;
  }

  public static Image getImageOn(IConfigurationElement element) {

    Image image = null;
    String img = element.getAttribute(ATT_IMAGE_ON);
    if (img != null) {
      String pluginId = ActivityExplorerExtensionManager.getPluginId(element);
      image = ActivityExplorerActivator.getDefault().getImage(pluginId, img);
    }
    return image;
  }

  public static boolean getIsDisplayViewerInPage(IConfigurationElement element) {
    String bool = element.getAttribute(ATT_VIEWER);
    return Boolean.parseBoolean(bool);
  }

  public static IConfigurationElement getOverviewElement(IConfigurationElement element) {
    return getChild(element, OVERVIEW);
  }

  private static IConfigurationElement getChild(IConfigurationElement element, String name) {
    IConfigurationElement child = null;
    IConfigurationElement[] children = element.getChildren(name);
    if (children.length > 0)
      child = children[0];

    return child;

  }

  public static String getOverviewImageOn(IConfigurationElement element) {
    IConfigurationElement overview = getOverviewElement(element);
    String img = overview.getAttribute(ATT_OVERVIEW_IMG_ON);
    return img == null ? IImageKeys.IMAGE_DEFAULT_OVERVIEW_ON : img;
  }

  public static String getOverviewImageOff(IConfigurationElement element) {
    IConfigurationElement overview = getOverviewElement(element);
    String img = overview.getAttribute(ATT_OVERVIEW_IMG_OFF);
    return img == null ? IImageKeys.IMAGE_DEFAULT_OVERVIEW_OFF : img;
  }

  public static String getOverviewDescription(IConfigurationElement element) {
    IConfigurationElement overview = getOverviewElement(element);
    return getDescription(overview);
  }

  public static String getDescription(IConfigurationElement element) {
    String description = null;
    if (null != element) {
      IConfigurationElement desc = getChild(element, DESCRIPTION);
      if (null != desc) {
        description = desc.getValue();
      }
    }
    return description;
  }

  public static List<IConfigurationElement> getSections(IConfigurationElement element) {
    List<IConfigurationElement> result = new ArrayList<IConfigurationElement>();
    String id = getId(element);
    result.addAll(Arrays.asList(element.getChildren(SECTION)));
    result.addAll(getAllSectionsExtensionForPageId(id));

    return result;
  }

  public static String getPluginId(IConfigurationElement element) {
    IContributor contributor = element.getContributor();
    Bundle bundle = Platform.getBundle(contributor.getName());
    String id = bundle.getSymbolicName();
    return id;
  }

  public static boolean getIsExpanded(IConfigurationElement element) {
    String bool = element.getAttribute(ATT_EXPANDED);
    boolean result = Boolean.parseBoolean(bool);
    return result;
  }

  public static boolean getIsFiltering(IConfigurationElement element) {
    String bool = element.getAttribute(ATT_FILTERING);
    boolean result = Boolean.parseBoolean(bool);
    return result;
  }

  public static List<IConfigurationElement> getActivities(IConfigurationElement element) {
    List<IConfigurationElement> result = new ArrayList<IConfigurationElement>();
    String id = getId(element);
    result.addAll(Arrays.asList(element.getChildren(ACTIVITY)));
    result.addAll(getAllActivitiesExtensionForSectionId(id));
    return result;
  }

  public static IHyperlinkListener getActivityAdapter(IConfigurationElement element) {
    IHyperlinkListener listener = null;

    String type = element.getName();
    String id = getId(element);

    try {
      String c = element.getAttribute(ATT_CLASS);
      if (c != null) {
        listener = (IHyperlinkListener) element.createExecutableExtension(ATT_CLASS);
      }
    } catch (CoreException e) {
      StringBuilder message = new StringBuilder();
      
      message.append(Messages.ActivityExplorerExtensionManager_1);
      message.append(type);
      message.append(Messages.ActivityExplorerExtensionManager_2);
      message.append(id);
      message.append(Messages.ActivityExplorerExtensionManager_3);
      
      ActivityExplorerLoggerService.getInstance().log(IStatus.WARNING, message.toString(), e);
    }
    return listener;
  }

  public static IPredicate getPredicate(IConfigurationElement elem) {
    IPredicate predicate = null;
    IConfigurationElement element = getChild(elem, PREDICATE);
    if (element != null) {
      String type = element.getName();
      try {

        String c = elem.getAttribute(ATT_CLASS);
        if (c != null) {
          predicate = (IPredicate) element.createExecutableExtension(ATT_CLASS);
        }
      } catch (CoreException e) {
    	  StringBuilder message = new StringBuilder();
          
          message.append(Messages.ActivityExplorerExtensionManager_1);
          message.append(type);
          message.append(Messages.ActivityExplorerExtensionManager_2);
          message.append(Messages.ActivityExplorerExtensionManager_3);
          
          ActivityExplorerLoggerService.getInstance().log(IStatus.WARNING, message.toString(), e);
      }
    }
    return predicate;
  }

  /**
   * Test if the id is a ActivityExplorerPage
   * 
   * @param id
   * @return boolean
   */
  public static boolean isPage(String id) {
    return null != ActivityExplorerExtensionManager.getPageById(id);
  }

  /**
   * Test if the id is a ActivityExplorerSection
   * 
   * @param pageId
   * @param sectionId
   * @return
   */
  public static boolean isSection(String pageId, String sectionId) {
    boolean result = false;
    for (IConfigurationElement element : getSectionsFromPageId(pageId)) {
      result |= getId(element).equals(sectionId);
    }
    return result;
  }

  public static boolean isActivity(String pageId, String sectionId, String activityId) {
    boolean result = false;
    for (IConfigurationElement element : getSectionsFromPageId(pageId)) {
      if (getId(element).equals(sectionId)) {
        for (IConfigurationElement element2 : getActivities(element)) {
          result |= getId(element2).equals(activityId);
        }
      }
    }

    return result;
  }

}
