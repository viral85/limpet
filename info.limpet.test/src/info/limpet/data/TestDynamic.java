package info.limpet.data;

import info.limpet.ICollection;
import info.limpet.ICommand;
import info.limpet.IQuantityCollection;
import info.limpet.data.impl.CoreChangeListener;
import info.limpet.data.impl.QuantityCollection;
import info.limpet.data.impl.samples.SampleData;
import info.limpet.data.operations.AddQuantityOperation;
import info.limpet.data.store.InMemoryStore;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import junit.framework.TestCase;

public class TestDynamic extends TestCase
{

	@SuppressWarnings(
	{ "unchecked", "rawtypes" })
	public void testSingleQuantityStats()
	{
		// get some data
		InMemoryStore store = new SampleData().getData();

		// ok, let's try one that works
		List<ICollection> selection = new ArrayList<ICollection>();
		ICollection speedOne = store.get(SampleData.SPEED_ONE);
		ICollection speedTwo = store.get(SampleData.SPEED_TWO);
		selection.add(speedOne);
		selection.add(speedTwo);

		int storeSize = store.size();

		Collection<ICommand<?>> actions = new AddQuantityOperation().actionsFor(
				selection, store);
		ICommand<?> firstAction = actions.iterator().next();
		assertEquals("correct action", "Add series", firstAction.getTitle());

		// run the action
		firstAction.execute();

		// check we have new data
		assertEquals("new data created", storeSize + 1, store.size());

		// ok, get the new dataset
		ICollection resSeries = store.get(AddQuantityOperation.SUM_OF_INPUT_SERIES);
		assertNotNull(resSeries);

		// remember the units
		IQuantityCollection<?> iq = (IQuantityCollection<?>) resSeries;
		final String resUnits = iq.getUnits().toString();
		
		// ok, play about with a change
		final List<String> events = new ArrayList<String>();
		CoreChangeListener listener = new CoreChangeListener()
		{
			@Override
			public void dataChanged(ICollection subject)
			{
				events.add("changed!");
			}
		};
		resSeries.addChangeListener(listener);
		assertEquals("empty at start", 0, events.size());

		// ok, now make a change in one of hte input collections
		speedOne.fireChanged();
		assertEquals("change received", 1, events.size());
		
		// check the units haven't changed
		assertEquals("units still valid", resUnits, iq.getUnits().toString());

		// ok, now make another change in one of hte input collections
		speedOne.fireChanged();
		assertEquals("second change received", 2, events.size());

		selection.clear();
		selection.add(speedTwo);
		selection.add(resSeries);

		// ok - now for a further dependent calculation
		actions = new AddQuantityOperation("output2").actionsFor(selection, store);
		firstAction = actions.iterator().next();
		assertEquals("correct action", "Add series", firstAction.getTitle());

		// ok, now create the new series
		firstAction.execute();

		// now check the output changed again
		events.clear();

		ICollection newResSeries = store.get("output2");
		assertNotNull("found new series");
		newResSeries.addChangeListener(listener);

		IQuantityCollection<?> iq2 = (IQuantityCollection<?>) newResSeries;
		final String resUnits2 = iq2.getUnits().toString();

		// ok, fire a change in speed one
		speedOne.fireChanged();
		assertEquals("change received", 2, events.size());

		speedTwo.fireChanged();
		assertEquals("change received", 5, events.size());

		resSeries.fireChanged();
		assertEquals("change received", 7, events.size());
		
		// switch off dynamic update
		Iterator<ICommand<?>> cIter = resSeries.getDependents().iterator();
		while (cIter.hasNext())
		{
			ICommand<?> comm = (ICommand<?>) cIter.next();
			comm.setDynamic(false);
		}
		
		// check that only the change listener event gets fired, not
		// the depedendent operation event.
		resSeries.fireChanged();
		assertEquals("change received", 8, events.size());

		// switch off dynamic update
		cIter = resSeries.getDependents().iterator();
		while (cIter.hasNext())
		{
			ICommand<?> comm = (ICommand<?>) cIter.next();
			comm.setDynamic(true);
		}

		// check we get two updates
		resSeries.fireChanged();
		assertEquals("change received", 10, events.size());

		
		// check the data lengths
		QuantityCollection<?> newResQ = (QuantityCollection<?>) newResSeries;
		assertEquals("correct elements", 10, newResQ.size());
		assertEquals("correct elements", 10, speedTwo.size());
		assertEquals("correct elements", 10, resSeries.size());
		
		// check the units haven't changed
		assertEquals("units still valid", resUnits, iq.getUnits().toString());
		assertEquals("units still valid", resUnits2, iq2.getUnits().toString());


		
	}
}