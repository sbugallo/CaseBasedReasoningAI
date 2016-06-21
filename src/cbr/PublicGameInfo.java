package cbr;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * This clas will encapsulate all the public information about the game. The
 * concept of public information is:<br>
 * - Anything a player know about the whole game (cards on the deck,undiscovered
 * cards...)
 *
 * @author markoscl
 *
 */
public class PublicGameInfo {
    private static PublicGameInfo instance = null;
    /** Number of cards of wich we don't know the type */
    private int cardsOnDeck = 0;

    private int captains = Deck.CAPTAIN_CARDS;
    private int lieutenants = Deck.LIEUTENANT_CARDS;
    private int inspectors = Deck.INSPECTOR_CARDS;
    private int legalGoods = Deck.LEGAL_GOODS_CARDS;
    private int illegalGoods = Deck.ILLEGAL_GOODS_CARDS;
    private HashMap<Player, ArrayList<Card>> observedCardsForPlayer = new HashMap<Player, ArrayList<Card>>(4);
    private HashMap<Player, Integer> cardsOnWareHouseForPlayer = new HashMap<Player, Integer>();

    /**
     * Method set to private, because this class is a singleton (Use
     * GetInstance())
     */
    private PublicGameInfo() {
    }

    /**
     * Returns the instance of this class
     *
     * @return
     */
    public static PublicGameInfo getInstance() {
	if (instance == null) {
	    instance = new PublicGameInfo();
	}
	return instance;
    }

    /**
     * Updates information. This will check only public information that
     * everyone knows.
     *
     * @param deck
     * @param players
     */
    void updatePublicInformation(Deck deck, Player[] players) {
	// updates cards on deck and visible cards
	cardsOnDeck = deck.getDeckSize();

    }

    /**
     * Adds a card to the known player cards
     * 
     * @param card
     * @param player
     */
    void addPublicCardToPlayer(Card card, Player player) {
	ArrayList<Card> list = observedCardsForPlayer.containsKey(player) ? observedCardsForPlayer.get(player)
		: new ArrayList<Card>();
	list.add(card);
	observedCardsForPlayer.put(player, list);
    }

    /**
     * Removes a card from the known player cards, and updates public info
     * accordingly
     * 
     * @param card
     * @param player
     */
    void removePublicCardFromPlayer(Card card, Player player) {
	ArrayList<Card> list = observedCardsForPlayer.containsKey(player) ? observedCardsForPlayer.get(player)
		: new ArrayList<Card>();
	list.remove(card);
	observedCardsForPlayer.put(player, list);
	if (captains > 0 && card.equals(Card.CAPTAIN)) {
	    captains--;
	} else if (lieutenants > 0 && card.equals(Card.LIEUTENANT)) {
	    lieutenants--;
	} else if (inspectors > 0 && card.equals(Card.INSPECTOR)) {
	    inspectors--;
	} else if (illegalGoods > 0 && card.equals(Card.ILLEGAL_GOODS)) {
	    illegalGoods--;
	} else if (legalGoods > 0 && card.equals(Card.LEGAL_GOODS)) {
	    legalGoods--;
	}
    }

    /**
     * Returns true if a card is still on play (aproximation, only public
     * knowledge)
     *
     * @param card
     * @return
     */
    boolean isCardStillOnPlay(Card card) {
	switch (card) {
	case CAPTAIN:
	    return captains > 0;
	case LIEUTENANT:
	    return lieutenants > 0;
	case INSPECTOR:
	    return inspectors > 0;
	case ILLEGAL_GOODS:
	    return illegalGoods > 0;
	case LEGAL_GOODS:
	    return legalGoods > 0;
	default:// We assume that the card is still on play
	    return true;
	}
    }

    float getLieutenantProc() {
	/** Prob. of at least one lieutenant present in the game */

	if (lieutenants > 0)
	    return 1.00f;
	else
	    return 0.00f;

    }

    int getCardsOnDeck() {
	return cardsOnDeck;
    }

    /**
     * Returns the observed illegal percentage of a player's hand.
     * 
     * @param player
     * @return
     */
    double getIllegalProcForPlayer(Player player) {
	ArrayList<Card> observedCards = observedCardsForPlayer.get(player) == null ? new ArrayList<Card>()
		: observedCardsForPlayer.get(player);
	// Count cards
	// Partials
	int legal = 0;
	int illegal = 0;
	int lieutenant = 0;
	int inspector = 0;
	int captain = 0;
	for (Card card : observedCards) {
	    switch (card) {
	    case CAPTAIN:
		captain++;
		break;
	    case ILLEGAL_GOODS:
		illegal++;
		break;
	    case INSPECTOR:
		inspector++;
		break;
	    case LEGAL_GOODS:
		legal++;
		break;
	    case LIEUTENANT:
		lieutenant++;
		break;
	    default:// Do nothing
		break;
	    }
	}
	float observedIllegalProc = illegal == 0 ? 0
		: ((float) illegal / (float) (captain + inspector + legal + illegal + lieutenant));
	return observedIllegalProc;
    }

    /**
     * Returns the observed legal percentage of a player's hand.
     * 
     * @param player
     * @return
     */
    double getLegalProcForPlayer(Player player) {

	ArrayList<Card> observedCards = observedCardsForPlayer.get(player) == null ? new ArrayList<Card>()
		: observedCardsForPlayer.get(player);
	// Count cards
	// Partials
	int legal = 0;
	int illegal = 0;
	int lieutenant = 0;
	int inspector = 0;
	int captain = 0;
	for (Card card : observedCards) {
	    switch (card) {
	    case CAPTAIN:
		captain++;
		break;
	    case ILLEGAL_GOODS:
		illegal++;
		break;
	    case INSPECTOR:
		inspector++;
		break;
	    case LEGAL_GOODS:
		legal++;
		break;
	    case LIEUTENANT:
		lieutenant++;
		break;
	    default:// Do nothing
		break;
	    }
	}
	float observedLegalProc = legal == 0 ? 0
		: ((float) legal / (float) (captain + inspector + legal + illegal + lieutenant));
	return observedLegalProc;

    }

    /**
     * Register that a player has won a round, so we add cards to his warehouse
     * 
     * @param player
     * @param cards
     */
    void addCardsOnWareHouseForPlayer(Player player, int cards) {
	if (cardsOnWareHouseForPlayer.containsKey(player)) {
	    cardsOnWareHouseForPlayer.put(player, cardsOnWareHouseForPlayer.get(player) + cards);
	}

    }

    @Override
    public String toString() {
	StringBuilder stb = new StringBuilder();
	stb.append("++++++++++++++++++++++++++++PUBLIC INFORMATION++++++++++++++++++++++++++++++");
	stb.append("\n\t");
	stb.append("Cards on deck: " + cardsOnDeck);
	stb.append("\n\t");

	stb.append("LieutenantProc: " + getLieutenantProc());
	stb.append("\n\t");

	stb.append("Captains: " + captains);
	stb.append("\n\t");

	stb.append("Lieutenants: " + lieutenants);
	stb.append("\n\t");

	stb.append("Inspectors: " + inspectors);
	stb.append("\n\t");

	stb.append("Legal goods: " + legalGoods);
	stb.append("\n\t");

	stb.append("IllegalGoods: " + illegalGoods);
	stb.append("\n\t");

	for (Player p : observedCardsForPlayer.keySet()) {
	    stb.append("\nFor player: " + p.getId());
	    stb.append("\nOBSERVED CARDS:\n\t");
	    for (Card card : observedCardsForPlayer.get(p)) {
		stb.append(" | " + card.toString());
	    }
	}
	for (Player p : cardsOnWareHouseForPlayer.keySet()) {
	    stb.append("\nFor player: " + p.getId() + " -> " + cardsOnWareHouseForPlayer.get(p) + "\n");
	}
	stb.append("\n____________________________________________________________________________");

	return stb.toString();

    }

    public void setDeckSize(int size) {
	cardsOnDeck = size;

    }

}
