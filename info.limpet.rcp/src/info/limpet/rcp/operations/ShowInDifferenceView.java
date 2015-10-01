package info.limpet.rcp.operations;

import info.limpet.ICollection;
import info.limpet.ICommand;
import info.limpet.IOperation;
import info.limpet.IStore;
import info.limpet.data.commands.AbstractCommand;
import info.limpet.data.operations.CollectionComplianceTests;
import info.limpet.rcp.xy_plot.DifferencePlotView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IViewReference;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;

public class ShowInDifferenceView implements IOperation<ICollection>
{
	CollectionComplianceTests aTests = new CollectionComplianceTests();
	final private String theId;

	public ShowInDifferenceView(String id)
	{
		theId = id;
	}

	protected CollectionComplianceTests getTests()
	{
		return aTests;
	}

	public Collection<ICommand<ICollection>> actionsFor(
			List<ICollection> selection, IStore destination)
	{
		Collection<ICommand<ICollection>> res = new ArrayList<ICommand<ICollection>>();
		if (appliesTo(selection))
		{
			// ok, we need to let the user select which to measure the error from
			Iterator<ICollection> iter = selection.iterator();
			while (iter.hasNext())
			{
				final ICollection primary = (ICollection) iter.next();

				final List<ICollection> others = new ArrayList<ICollection>();
				Iterator<ICollection> oIter = selection.iterator();
				while (oIter.hasNext())
				{
					ICollection thisOther = (ICollection) oIter.next();
					if (thisOther != primary)
					{
						others.add(thisOther);
					}
				}

				final String title;
				if (selection.size() == 2)
				{
					title = "Show calculated difference from " + primary.getName();
				}
				else
				{
					title = "Show calculated differences from " + primary.getName();
				}

				// ok offer operation for this one
				ICommand<ICollection> newC = new ShowInDifferenceOperation(title,
						primary, others, theId);
				res.add(newC);

			}

		}

		return res;
	}

	protected boolean appliesTo(List<ICollection> selection)
	{
		return aTests.atLeast(selection, 2) && aTests.allQuantity(selection)
				&& aTests.allEqualUnits(selection);
	}

	public static class ShowInDifferenceOperation extends
			AbstractCommand<ICollection>
	{

		final private String _id;
		final private ICollection _primary;
		final private List<ICollection> _others;

		public ShowInDifferenceOperation(String title, ICollection primary,
				List<ICollection> others, String id)
		{
			super(title, "Show selection in specified view", null, null, false,
					false, null);
			_id = id;
			_primary = primary;
			_others = others;

		}

		@Override
		public void execute()
		{
			String secId = _inputs.toString();

			// create a new instance of the specified view
			IWorkbenchWindow window = PlatformUI.getWorkbench()
					.getActiveWorkbenchWindow();
			IWorkbenchPage page = window.getActivePage();

			try
			{

				page.showView(_id, secId, IWorkbenchPage.VIEW_ACTIVATE);
			}
			catch (PartInitException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// try to recover the view
			IViewReference viewRef = page.findViewReference(_id, secId);
			IViewPart theView = viewRef.getView(true);

			// double check it's what we're after
			if (theView instanceof DifferencePlotView)
			{
				DifferencePlotView cv = (DifferencePlotView) theView;

				// set follow selection to off
				cv.follow(getTitle(), _primary, _others);
			}
		}

		@Override
		protected void recalculate()
		{
			// don't worry
		}

	}

}
