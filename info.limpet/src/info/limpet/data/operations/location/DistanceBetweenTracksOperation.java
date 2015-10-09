package info.limpet.data.operations.location;

import info.limpet.ICommand;
import info.limpet.IQuantityCollection;
import info.limpet.IStore;
import info.limpet.IStore.IStoreItem;
import info.limpet.data.impl.samples.StockTypes;
import info.limpet.data.impl.samples.StockTypes.NonTemporal.Length_M;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.measure.Measure;

import org.geotools.referencing.GeodeticCalculator;
import org.opengis.geometry.primitive.Point;

public class DistanceBetweenTracksOperation extends TwoTrackOperation
{
	
	
	private final class DistanceBetweenOperation extends DistanceOperation
	{
		private DistanceBetweenOperation(String outputName,
				List<IStoreItem> selection, IStore store, String title,
				String description)
		{
			super(outputName, selection, store, title, description);
		}

		protected IQuantityCollection<?> getOutputCollection(String title)
		{
			return new StockTypes.NonTemporal.Length_M("Distance between " + title);
		}

		protected void calcAndStore(final GeodeticCalculator calc, final Point locA, 
				final Point locB)
		{
			// get the output dataset
			Length_M target = (Length_M) getOutputs().get(0);

			// now find the range between them
			calc.setStartingGeographicPoint(locA.getCentroid().getOrdinate(0), locA
					.getCentroid().getOrdinate(1));
			calc.setDestinationGeographicPoint(locB.getCentroid().getOrdinate(0),
					locB.getCentroid().getOrdinate(1));
			double thisDist = calc.getOrthodromicDistance();
			target.add(Measure.valueOf(thisDist, target.getUnits()));
		}
	}

	public Collection<ICommand<IStoreItem>> actionsFor(
			List<IStoreItem> selection, IStore destination)
	{
		Collection<ICommand<IStoreItem>> res = new ArrayList<ICommand<IStoreItem>>();
		if (appliesTo(selection))
		{
			ICommand<IStoreItem> newC = new DistanceBetweenOperation(null, selection, destination, "Distance between tracks", "Calculate distance between two tracks");

			res.add(newC);
		}

		return res;
	}
}
