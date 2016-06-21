package test;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

import cbr.Card;
import cbr.CargoCBR;
import cbr.CargoCase;
import cbr.Deck;
import cbr.GameHelper;
import cbr.InspectCBR;
import cbr.InspectCase;
import cbr.Player;
import cbr.PublicGameInfo;

/**
 * @author Markoscl
 *
 */

public class GameTest {
    private static final int INSPECT_CASES = 500;
    private static final int CARGO_CASES = 500;
    private static final Random rand = new Random();

    /**
     * @param args
     * @throws InterruptedException
     */

    public static void main(String[] args) throws InterruptedException {

	// Cargamos datos aleatorios para los casos (Generados con las funciones
	// de ejemplo, solamente se guardan casos únicos, es decir, si se va a
	// guardar un caso, y ya hay uno MUY similar, no se guarda)

	// TODO es muy importante crear una base de datos CORRECTA, es decir,
	// que no meta en un cargo por ejemplo a un inspector, un captain y
	// demás. Una vez creada la db ya se encarga ella sola de actualizarse y
	// tender a los mejores casos y evitar los malos.
	InspectCBR.GetCasesFromFile("RandomInspectCases");
	CargoCBR.GetCasesFromFile("RandomCargoCases");
	printMemoryUssage();
	// Example of how we should use
	// cases and
	// some rounds examples.
	// First, we create 4 players. They MUST have ID's ranging from 0 to 3
	Player[] intelPlayers = new Player[] { new Player(0), new Player(1) };// ,
									      // new
									      // Player(2),
									      // new
									      // Player(3)
									      // };
	// We also create the deck
	Deck deck = new Deck();

	// Once the players have been created, we give them the starting cards
	for (Player player : intelPlayers) {
	    // Give each player starting cards (And remove them from deck)
	    player.addCards(deck.getStartingCards(player));
	}
	System.out.println(PublicGameInfo.getInstance().toString());

	// Initialize our game helper for this "round"
	GameHelper helper = new GameHelper();
	// Now the game starts, let's make an example for the draw
	// algorithm,
	// where every inteligent player will draw cards:
	for (Player p : intelPlayers) {
	    if (deck.getDeckSize() <= 0) {
		break;
	    }
	    p.draw(deck);
	    System.out.println("After drawing cards, cards for player: " + p.getId() + "\n\t"
		    + Arrays.toString(p.getHand().toArray(new String[] {})));

	}
	System.out.println(PublicGameInfo.getInstance().toString());

	// Now every player has a full hand. Let's make an example where
	// each player will search for his best cargo.
	for (Player p : intelPlayers) {
	    ArrayList<String> bestCargo = p.searchForBestCargo();
	    for (String cargoCard : bestCargo) {
		p.addToCargo(cargoCard);
	    }
	}

	helper.informAboutPlayerCargo(intelPlayers);
	// Print each player's cargo:
	for (Player p : intelPlayers) {
	    System.out.println("Cargo for: " + p.getId());
	    System.out.println("\t" + Arrays.toString(p.getCargo()));
	    System.out.println("Hand is:\t\t" + Arrays.toString(p.getHand().toArray(new String[] {})));
	}

	// Now search for the best inspection,player by player
	// Make each player inspect the next player

	/**
	 * Para cada jugador inspeccionando a otro jugador: <br>
	 * Ver quién gana, repartir ganancias y/o cartas.<br>
	 * Actualizar información pública.<br>
	 * Actualizar riesgos para cada jugador.<br>
	 * TODO guardar en public info y de ahí sacar riskRate<br>
	 */
	for (Player p : intelPlayers) {
	    // Para cada jugador primero comprobamos si PUEDE inspeccionar
	    if (p.canInspect() == false) {
		System.out.println("Player : " + p.getId() + " can´t inspect, because his hand is: "
			+ Arrays.toString(p.getHand().toArray(new String[] {})));
		continue;
	    }
	    // Si el jugador puede inspeccionar,elegimos a qué jugador vamos
	    // a
	    // inspeccionar con ayuda de nuestro helper
	    ArrayList<Player> inspectablePlayers = helper.getInspectablePlayers();
	    // Elegimos de los jugadores que podemos inspeccionar, a uno
	    // cualquiera
	    Player inspectedP = inspectablePlayers.get(rand.nextInt(inspectablePlayers.size()));

	    // Buscamos cuales son los mejores indices para inspeccionar a
	    // este jugador
	    // El método busca dentro de los casos aquel con el imsmo ID del
	    // jugador inspeccionado, y con el mismo
	    // número de parámetros similares (tamaño de carga,
	    // riskRate....) y devuevle aquel con mayor % win
	    ArrayList<Integer> bestIndexesToInspect = p.getBestInspectionIndexes(inspectedP);
	    // Recuperamos la carta usada para inspeccionar
	    // Para los jugadores inteligentes usamos la función
	    // getBestCardToInspect, que siempre devuelve la carta con mayor
	    // poder de inspección.

	    // Para los jugadores inteligentes, debemos de recorrer las
	    // cartas
	    // capaces de inspeccionar, seleccionar una y ELIMINARLA de su
	    // mano
	    Card cardUsedToInspect = p.getBestCardToInspect();
	    // La función de realizar inspección se encarga de eliminar las
	    // cartas que sean necesarias, añadirlas al warehouse adecuado,
	    // e
	    // informar a la información pública del suceso. No se requieren
	    // más
	    // acciones.

	    // le decimos al helper cuales son los 2 jugadores involucrados
	    // (inspector, inspected), qué indices va a inspeccionar, y qué
	    // carta está usando para inspeccionar
	    helper.makeInspection(p, inspectedP, bestIndexesToInspect, cardUsedToInspect);
	}
	// Los jugadores que no hayan sido inspeccionados ganan su cargo
	for (Player p : helper.getInspectablePlayers()) {
	    String[] cargoForPlayer = helper.getCargoForPlayer(p);
	    p.addCardsToWarehouse(cargoForPlayer.length);// Información pública
	    // Ahora añadimos el valor que ha ganado
	    for (String cardName : cargoForPlayer) {
		p.addProfit(Card.getCardByName(cardName).getCargoValue());
	    }

	}

	// Una vez todos los jugadores han pasado su turno para
	// inspeccionar, limpiamos a los posibles jugadores que no hayan
	// sido inspeccionados, es decir, limpiamos su cargo

	for (Player p : intelPlayers) {
	    p.cleanCargo();
	    System.out.println("After inspecting, cards for: " + p.getId());
	    System.out.println(Arrays.toString(p.getHand().toArray(new String[] {})));
	}
	System.out.println(PublicGameInfo.getInstance().toString());
	// Se están eliminando cartas en el makeCargo y en el makeinspection, la
	// mano de los jugadores es demasiado pequeña al acabar
	System.out.println("Game has ended, deck is:");
	deck.printDeck();
    }

    private static void printMemoryUssage() {
	Runtime runtime = Runtime.getRuntime();

	NumberFormat format = NumberFormat.getInstance();
	StringBuilder sb = new StringBuilder();
	long maxMemory = runtime.maxMemory();
	long allocatedMemory = runtime.totalMemory();
	long freeMemory = runtime.freeMemory();
	sb.append("-------------------------------------------------------------------------------\n");
	sb.append("free memory:             " + format.format(freeMemory / 1024) + "\n");
	sb.append("allocated memory:        " + format.format(allocatedMemory / 1024) + "\n");
	sb.append("max memory:              " + format.format(maxMemory / 1024) + "\n");
	sb.append("total free memory:       " + format.format((freeMemory + (maxMemory - allocatedMemory)) / 1024)
		+ "\n");
	sb.append("-------------------------------------------------------------------------------\n");

	System.out.println(sb.toString());
	Player.printTotalCases();

    }

    private static void generateInspect() {
	ArrayList<InspectCase> cases = new ArrayList<InspectCase>();
	for (int i = 0; i < INSPECT_CASES; i++) {

	    Deck deck = new Deck();
	    Player inspector = new Player(rand.nextInt(4));
	    int inspectedID = rand.nextInt(4);
	    while (inspectedID == inspector.getId())
		inspectedID = rand.nextInt(4);
	    Player inspected = new Player(inspectedID);
	    inspected.setRiskRatio(rand.nextFloat());
	    inspector.setRiskRatio(rand.nextFloat());
	    inspected.addCards(deck.getStartingCards(inspected));
	    inspected.draw(deck);
	    inspector.addCards(deck.getStartingCards(inspector));
	    inspector.draw(deck);
	    ArrayList<String> cargo = inspected.makeRandomCargo();
	    for (String card : cargo) {
		inspected.addToCargo(card);
	    }
	    InspectCase caseToAdd = new InspectCase(inspector, inspected);
	    caseToAdd.setWinAndLostRatioTest(rand.nextInt(50), rand.nextInt(10));
	    ArrayList<Integer> bestIndexes = inspector.getBestInspectionIndexes(inspected);
	    caseToAdd.setInspectIndexes(bestIndexes);
	    cases.add(caseToAdd);
	}
	InspectCase.addAllCases(cases);
    }

    /**
     * Randomly generates cases THIS FUNCTION IS TEST ONLY
     * 
     * @return
     * @test TEST FUNCTION ONLY
     */
    private static void generateCargos() {
	ArrayList<CargoCase> cases = new ArrayList<CargoCase>();
	double riskRate = 0;
	boolean captainFlag = false;
	boolean inspectorFlag = false;
	double lieutProc = 0;
	long legalCards = 0;
	long illegalCards = 0;
	long inspectCards = 0;
	long lieutCards = 0;
	long captainCards = 0;
	long wonRounds = 0;
	long lostRounds = 0;
	// Add 1000 random cases
	for (int i = 0; i < CARGO_CASES; i++) {

	    riskRate = rand.nextDouble();
	    captainFlag = rand.nextBoolean();
	    inspectorFlag = rand.nextBoolean();
	    lieutProc = rand.nextDouble();
	    wonRounds = rand.nextInt(100);
	    lostRounds = rand.nextInt(100);
	    Deck deck = new Deck();
	    String[] cargo = new String[4];

	    for (int j = 0; j < 4; j++) {
		String card = deck.getNextCard();
		cargo[j] = card;
		if (card.equals(Card.CAPTAIN.getCardName())) {
		    captainCards++;
		} else if (card.equals(Card.ILLEGAL_GOODS.getCardName())) {
		    illegalCards++;
		} else if (card.equals(Card.LEGAL_GOODS.getCardName())) {
		    legalCards++;
		} else if (card.equals(Card.LIEUTENANT.getCardName())) {
		    lieutCards++;
		} else if (card.equals(Card.INSPECTOR.getCardName())) {
		    inspectCards++;
		}
	    }

	    cases.add(new CargoCase(riskRate, captainFlag, inspectorFlag, lieutProc, legalCards, illegalCards,
		    inspectCards, lieutCards, captainCards, wonRounds, lostRounds, cargo));
	}
	// Add cases
	CargoCase.addALlCases(cases);

    }
}
