package chrislo27.bot.bots.baristabot2.transit;

import org.jgrapht.Graph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;

public class TransitSystems {

	public static final String EXPO_LINE = "Expo Line";
	public static final String MILLENNIUM_LINE = "Millennium Line";
	public static final String CANADA_LINE = "Canada Line";
	public static final String CANADA_LINE_AIRPORT = "Canada Line - Airport Branch";

	private static TransitSystems instance;

	private TransitSystems() {
	}

	public static TransitSystems instance() {
		if (instance == null) {
			instance = new TransitSystems();
			instance.loadResources();
		}
		return instance;
	}

	public Graph<String, LineEdge> system = null;

	private void loadResources() {
		refresh();
	}

	/**
	 * Recreates graph
	 */
	public void refresh() {
		system = new SimpleGraph<String, LineEdge>(LineEdge.class);

		// Vancouver
		addStations("Waterfront", "Burrard", EXPO_LINE);
		addStations("Burrard", "Granville", EXPO_LINE);
		addStations("Granville", "Stadium - Chinatown", EXPO_LINE);
		addStations("Stadium - Chinatown", "Main Street - Science World", EXPO_LINE);
		addStations("Main Street - Science World", "Commercial - Broadway", EXPO_LINE);
		addStations("Commercial - Broadway", "Nanaimo", EXPO_LINE);
		addStations("Nanaimo", "29th Avenue", EXPO_LINE);
		addStations("29th Avenue", "Joyce - Collingwood", EXPO_LINE);
		addStations("Joyce - Collingwood", "Patterson", EXPO_LINE);
		addStations("Patterson", "Metrotown", EXPO_LINE);
		addStations("Metrotown", "Royal Oak", EXPO_LINE);
		addStations("Royal Oak", "Edmonds", EXPO_LINE);
		addStations("Edmonds", "22nd Street", EXPO_LINE);
		addStations("22nd Street", "New Westminster", EXPO_LINE);
		addStations("New Westminster", "Columbia", EXPO_LINE);
		addStations("Columbia", "Scott Road", EXPO_LINE);
		addStations("Scott Road", "Gateway", EXPO_LINE);
		addStations("Gateway", "Surrey Central", EXPO_LINE);
		addStations("Surrey Central", "King George", EXPO_LINE);
		addStations("Columbia", "Sapperton", MILLENNIUM_LINE);
		addStations("Sapperton", "Braid", MILLENNIUM_LINE);
		addStations("Braid", "Lougheed Town Centre", MILLENNIUM_LINE);
		addStations("Lougheed Town Centre", "Production Way - University", MILLENNIUM_LINE);
		addStations("Production Way - University", "Lake City Way", MILLENNIUM_LINE);
		addStations("Lake City Way", "Sperling - Burnaby Lake", MILLENNIUM_LINE);
		addStations("Sperling - Burnaby Lake", "Holdom", MILLENNIUM_LINE);
		addStations("Holdom", "Brentwood Town Centre", MILLENNIUM_LINE);
		addStations("Brentwood Town Centre", "Gilmore", MILLENNIUM_LINE);
		addStations("Gilmore", "Rupert", MILLENNIUM_LINE);
		addStations("Rupert", "Renfrew", MILLENNIUM_LINE);
		addStations("Renfrew", "Commercial - Broadway", MILLENNIUM_LINE);
		addStations("Commercial - Broadway", "VCC - Clark", MILLENNIUM_LINE);
		addStations("Waterfront", "Vancouver City Centre", CANADA_LINE);
		addStations("Vancouver City Centre", "Yaletown - Roundhouse", CANADA_LINE);
		addStations("Yaletown - Roundhouse", "Olympic Village", CANADA_LINE);
		addStations("Olympic Village", "Broadway - City Hall", CANADA_LINE);
		addStations("Broadway - City Hall", "King Edward", CANADA_LINE);
		addStations("King Edward", "Oakridge - 41st Avenue", CANADA_LINE);
		addStations("Oakridge - 41st Avenue", "Langara - 49th Avenue", CANADA_LINE);
		addStations("Langara - 49th Avenue", "Marine Drive", CANADA_LINE);
		addStations("Marine Drive", "Bridgeport", CANADA_LINE);
		addStations("Bridgeport", "Aberdeen", CANADA_LINE);
		addStations("Aberdeen", "Lansdowne", CANADA_LINE);
		addStations("Lansdowne", "Richmond - Brighouse", CANADA_LINE);
		addStations("Bridgeport", "Templeton", CANADA_LINE_AIRPORT);
		addStations("Templeton", "Sea Island Centre", CANADA_LINE_AIRPORT);
		addStations("Sea Island Centre", "YVR - Airport", CANADA_LINE_AIRPORT);

		// RH
		addStations("Earth World", "Heaven World", "RH Line");
		addStations("Heaven World", "Home", "RH Line");
		addStations("Heaven World", "Rhythm Heaven", "kill yourself");
	}

	public void addStations(String start, String end, String line) {
		if (!system.containsVertex(start)) system.addVertex(start);
		if (!system.containsVertex(end)) system.addVertex(end);

		system.addEdge(start, end, new LineEdge(start, end, line));
	}

	public DijkstraShortestPath<String, LineEdge> getPath(String start, String end) {
		return new DijkstraShortestPath<String, LineEdge>(system, start, end);
	}

	public static class LineEdge<V> extends DefaultEdge {

		private V v1;
		private V v2;
		private String line;

		public LineEdge(V v1, V v2, String line) {
			this.v1 = v1;
			this.v2 = v2;
			this.line = line;
		}

		public V getV1() {
			return v1;
		}

		public V getV2() {
			return v2;
		}

		public String getLine() {
			return line;
		}
	}

}
