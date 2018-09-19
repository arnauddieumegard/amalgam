/*******************************************************************************
 * Copyright (c)  2006, 2018 THALES GLOBAL SERVICES and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Thales - initial API and implementation
 *******************************************************************************/
package org.eclipse.amalgam.explorer.activity.ui.api.dialog;

import java.net.URL;
import java.text.MessageFormat;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.PopupDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.browser.IWorkbenchBrowserSupport;
import org.eclipse.ui.forms.HyperlinkGroup;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.statushandlers.StatusManager;

/**
 * Dialog that opens a popup dialog to display content in a {@link FormText}.
 * 
 */
public class DescriptionDialog extends PopupDialog {
	/**
	 * Close the popup dialog.
	 * 
	 */
	private class CloseAction extends Action {
		/**
		 * @see org.eclipse.jface.action.Action#getImageDescriptor()
		 */
		@Override
		public ImageDescriptor getImageDescriptor() {
			return PlatformUI.getWorkbench().getSharedImages().getImageDescriptor(ISharedImages.IMG_TOOL_DELETE);
		}

		/**
		 * @see org.eclipse.jface.action.Action#getToolTipText()
		 */
		@Override
		public String getToolTipText() {
			return Messages.DescriptionDialog_CloseAction_Title;
		}

		/**
		 * @see org.eclipse.jface.action.Action#run()
		 */
		@Override
		public void run() {
			close();
		}
	}

	private Point _anchor;
	private Composite _composite;
	private FormToolkit _toolkit;

	private String _content;

	/**
	 * Constructor.
	 * 
	 * @param parent
	 * @param content_p
	 */
	public DescriptionDialog(Shell parent, String content_p) {
		super(parent, SWT.NONE, true, false, false, false, false, null, null);
		_anchor = parent.getDisplay().getCursorLocation();
		_toolkit = new FormToolkit(parent.getDisplay());
		_content = content_p;
	}

	/**
	 * @see org.eclipse.jface.dialogs.PopupDialog#close()
	 */
	@Override
	public boolean close() {
		if (_toolkit != null) {
			_toolkit.dispose();
			_toolkit = null;
		}
		return super.close();
	}

	/**
	 * @see org.eclipse.jface.dialogs.PopupDialog#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		getShell().setBackground(getShell().getDisplay().getSystemColor(SWT.COLOR_DARK_GRAY));
		initializeBounds();
		return createDialogArea(parent);
	}

	/**
	 * @see org.eclipse.jface.dialogs.PopupDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@SuppressWarnings("synthetic-access")
	@Override
	protected Control createDialogArea(Composite parent) {
		_composite = (Composite) super.createDialogArea(parent);

		ScrolledForm form = _toolkit.createScrolledForm(_composite);
		_toolkit.decorateFormHeading(form.getForm());

		// add a Close button to the toolbar
		form.getToolBarManager().add(new CloseAction());
		form.getToolBarManager().update(true);

		TableWrapLayout layout = new TableWrapLayout();
		layout.leftMargin = 10;
		layout.rightMargin = 10;
		layout.topMargin = 10;
		layout.verticalSpacing = 10;
		form.getBody().setLayout(layout);

		FormText richText = org.eclipse.amalgam.explorer.activity.ui.api.editor.pages.helper.FormHelper.createRichText(
				_toolkit, form.getBody(), _content, null);
		configureHyperLinkSupport(richText, form);
		TableWrapData layoutData = new TableWrapData();
		layoutData.maxWidth = 400;
		richText.setLayoutData(layoutData);
		return _composite;
	}

	private void configureHyperLinkSupport(FormText richText, ScrolledForm form) {
		Display disaply = form.getDisplay();
		HyperlinkGroup group = new HyperlinkGroup(disaply);
		group.setForeground(disaply.getSystemColor(SWT.COLOR_BLUE));
		group.setActiveForeground(disaply.getSystemColor(SWT.COLOR_BLUE));
		richText.setHyperlinkSettings(group);
		richText.addHyperlinkListener(new HyperlinkAdapter() {
			public void linkActivated(HyperlinkEvent e) {
				String href = (String) e.getHref();
				if (href.startsWith("http")) {
					int browserStyle = IWorkbenchBrowserSupport.LOCATION_BAR | IWorkbenchBrowserSupport.AS_EXTERNAL | IWorkbenchBrowserSupport.STATUS | IWorkbenchBrowserSupport.NAVIGATION_BAR;
					IWorkbenchBrowserSupport browserSupport = PlatformUI.getWorkbench().getBrowserSupport();
					try {
						browserSupport.createBrowser(browserStyle, null, null, null).openURL(new URL(href));
					} catch (Exception ex) {
						Status status = new Status(Status.ERROR, null, MessageFormat.format(Messages.DescriptionDialog_exernal_browser_error, ex.getMessage()), ex);
						StatusManager.getManager().handle(status, StatusManager.SHOW);
					}
				}
			}
		});
	}
    
	/**
	 * @see org.eclipse.jface.dialogs.PopupDialog#getFocusControl()
	 */
	@Override
	protected Control getFocusControl() {
		return _composite;
	}

	/**
	 * @see org.eclipse.jface.dialogs.PopupDialog#getInitialLocation(org.eclipse.swt.graphics.Point)
	 */
	@Override
	protected Point getInitialLocation(Point size) {
		if (_anchor == null) {
			return super.getInitialLocation(size);
		}
		Point point = _anchor;
		Rectangle monitor = getShell().getMonitor().getClientArea();
		if (monitor.width < point.x + size.x) {
			point.x = Math.max(0, point.x - size.x);
		}
		if (monitor.height < point.y + size.y) {
			point.y = Math.max(0, point.y - size.y);
		}
		return point;
	}
}
