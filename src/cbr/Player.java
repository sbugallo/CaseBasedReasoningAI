package cbr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 *
 */

/**
 * @author markoscl
 *
 */
public class Player {

    public static float RISK_LOW = 0.20f;
    public static float RISK_MEDIUM = 0.5f;
    public static float RISK_HIGH = 0.8f;
    /** Player name */
    private int id = 0;
    /** Cards in this player's hand */
    private ArrayList<String> hand = new ArrayList<String>();
    /**
     * Apparent risk ratio for this player, high when loosing and low when
     * winning
     */
    private float riskRatio = 0.0f;
    /** Profit in this player's warehouse */
    private int money = 0;
    /** Cargo for this player */
    private ArrayList<String> cargo = new ArrayList<String>();
    /** Card that this player will use when inspecting */
    private Card cardUsedToInspect = Card.EMPTY;

    /**
     * Initializes a new player with
     *
     * @param id
     */
    public Player(int id) {
	this.id = id;
    }

    /**
     * Adds the given profit to this player's warehouse.
     *
     * @param profit
     */
    public void addProfit(int profit) {
	money += profit;
    }

    public int getMoney() {
	return money;
    }

    /**
     * Adds the given card's to this player's hand
     *
     * @param card
     */
    public void addCards(List<String> list) {
	if (list.size() + hand.size() > 8) {
	    throw new IllegalArgumentException("Can't add this many cards");
	}
	hand.addAll(list);

    }

    public boolean removeAll(String[] cardNames) {
	boolean retVal = true;
	for (String cardName : cardNames) {
	    if (!hand.remove(Card.getCardByName(cardName).getCardName()))
		retVal = false;
	}
	return retVal;
    }

    /**
     * Removes the given card from this player's hand and places it on the cargo
     *
     * @param card
     *            to remove
     */
    public void addToCargo(String card) {
	if (!hand.remove(card)) {
	    throw new IllegalArgumentException("Tried to remove card: " + card + " from player: " + id
		    + " hand, but player's hand was: " + Arrays.deepToString(hand.toArray()));
	} else {
	    cargo.add(card);
	}
    }

    public ArrayList<String> getHand() {
	return hand;
    }

    /**
     *
     */
    public void updateRiskRatio() {
	// TODO actualizar cáculo de riesgo
    }

    public void setRiskRatio(float risk) {
	riskRatio = risk;
    }

    public void addCardsToWarehouse(int cards) {
	PublicGameInfo.getInstance().addCardsOnWareHouseForPlayer(this, cards);
    }

    public int getId() {
	return id;
    }

    /**
     * Returns the risk for this player. Higher risk ratios will mean the player
     * is loosing, and has to risk more to try to win.
     *
     * @return
     */
    public float getRiskRatio() {
	return riskRatio;
    }

    /**
     * Tries to take as much cards from this type as it can, or takes cards
     * randomly if it can't found cards of the given type on the visible deck
     *
     * @param cardType
     * @param deck
     */
    private void tryToTakeCard(Card cardType, Deck deck, ArrayList<String> cardsToAdd) {
	// If we can't take more cards, return
	if (hand.size() >= 8) {
	    System.out.println("I have 8 cards or more, aborting!");
	    return;
	}
	// Tries to take cards from visible deck
	while ((deck.showVisibleCards().contains(cardType)) && (cardsToAdd.size() + hand.size()) < 8
		&& cardsToAdd.size() < 2) {
	    cardsToAdd.add(deck.getCardFromVisible(cardType, this));
	}
	if (cardsToAdd.size() == 2 || (cardsToAdd.size() + hand.size()) >= 8) {
	    return;
	} else {
	    while (deck.getDeckSize() != 0 && (cardsToAdd.size() + hand.size()) < 8) {
		cardsToAdd.add(deck.getNextCard());
	    }
	}
    }

    /**
     * Draws cards from the deck, following the next algorithm: <br>
     *
     *
     * -Si hay teniente lo pillamos por ser la más valiosa.<br>
     * -Siempre que podamos coger 2 cartas, lo hacemos.<br>
     * -Dependiendo del riesgo lo que más nos convenga para los demás casos.
     * Podemos jugar arriesgándonos mediante illegal goods, y si no hay pues
     * boca abajo. Podemos jugar de forma más conservativa pillando legal goods
     * o pillando boca arriba.<br>
     * -Hacer ratio para saber qué cartas quedan con la diferencia de dinero.
     *
     * @return
     *
     */
    public boolean draw(Deck deck) {
	if (hand.size() >= 8)
	    return false;
	ArrayList<String> cardsToAdd = new ArrayList<String>();
	// Check for valuable cards (Take all visible lieutenants if possible)
	tryToTakeCard(Card.LIEUTENANT, deck, cardsToAdd);

	// Now here comes the complicated bit:
	// We will play more aggressive (Illegal goods) if the risk is high.
	// Otherwise, we will play more conservative

	if (riskRatio >= RISK_HIGH) {
	    // We are loosing by quite a bit, let's play as much offensive as we
	    // can
	    // Try to take illegal goods, or random cards
	    tryToTakeCard(Card.ILLEGAL_GOODS, deck, cardsToAdd);
	}
	// Risk is medium, so we want to maximize our winnings but we don't
	// commit to play only illegal if necessary. We try to balance our
	// cards.
	// That means if we have a lot of illlegal goods we go for legal goods
	// cards, and if we have a lot of legal goods cards, we go for illegal.
	else if (riskRatio >= RISK_MEDIUM) {
	    // First try to take illegal goods from visible cards
	    Card preference;
	    int legal = 0, illegal = 0;
	    // Calculate what kind of card we prefer
	    for (String card : hand) {
		if (card.equals(Card.LEGAL_GOODS))
		    legal++;
		else if (card.equals(Card.ILLEGAL_GOODS))
		    illegal++;
	    }
	    if (legal >= illegal)
		preference = Card.ILLEGAL_GOODS;
	    else
		preference = Card.ILLEGAL_GOODS;
	    // Try to balance from visible cards
	    tryToTakeCard(preference, deck, cardsToAdd);
	}
	// Draw legal goods if avaliable, or random otherwise
	else {
	    tryToTakeCard(Card.LEGAL_GOODS, deck, cardsToAdd);
	}

	// This is here as a safeguard, because it can happend that we don't
	// like
	// visible cards (No conditions met), but the deck is empty, so we can
	// not
	// take randomly!!
	// We take ANY visible card at this point
	while ((cardsToAdd.size() + hand.size()) < 8 && cardsToAdd.size() < 2) {
	    cardsToAdd.add(deck.getCardFromVisible(Card.getCardByName(deck.showVisibleCards().get(0)), this));
	}

	// Add the cards to this player
	addCards(cardsToAdd);
	return true;
    }

    /**
     * Searches for best cargo for this player
     *
     * @return
     */
    public ArrayList<String> searchForBestCargo() {
	ArrayList<String> search = CargoCBR.getInstance().search(PublicGameInfo.getInstance(), this);
	return search;
    }

    /**
     * Makes a random cargo and set it on the cargo for this player
     * 
     * @return
     */
    public ArrayList<String> makeRandomCargo() {
	ArrayList<String> auxHand = new ArrayList<String>();
	// Copy hand into auxHand
	auxHand.addAll(hand);
	ArrayList<String> cargo = new ArrayList<String>();
	Random rand = new Random();
	while (cargo.size() < 4 && auxHand.size() != 0) {
	    cargo.add(auxHand.remove(rand.nextInt(auxHand.size())));
	}
	return cargo;
    }

    /**
     * Gets this player's cargo
     * 
     * @return
     */
    public String[] getCargo() {
	return cargo.toArray(new String[cargo.size()]);
    }

    public ArrayList<Integer> makeRandomInspection(int cargoSize) {
	ArrayList<Integer> inspectedIndexes = new ArrayList<Integer>();
	Random rand = new Random();
	// Inspect as much cards as we can
	int powerLeft = getInspectPower();
	ArrayList<Integer> possibleIndexes = new ArrayList<Integer>();
	for (int i = 0; i < cargoSize; i++)
	    possibleIndexes.add(i);
	while (powerLeft != 0 && possibleIndexes.size() > 0) {
	    // Inspect while we have power left and the cargo has cards left to
	    // inspect
	    int nextInt = possibleIndexes.remove(rand.nextInt(possibleIndexes.size()));
	    inspectedIndexes.add(nextInt);
	    powerLeft--;
	}
	return inspectedIndexes;
    }

    /**
     * Make this player inspect the given player
     * 
     * @param player
     * @return The best indexes to inspect
     */
    public ArrayList<Integer> getBestInspectionIndexes(Player player) {
	if (getInspectPower() == 0) {
	    // Player can´t inspect because he does not have enough inspect
	    // power
	    return null;
	}
	ArrayList<Integer> bestInspectionIndexes = InspectCBR.getInstance().search(this, player);
	// System.out.println("----------------------------------------------------------------------------------------");

	return bestInspectionIndexes;
    }

    public int getInspectPower() {
	int inspectPower = 0;
	for (String cardName : getHand()) {
	    Card card = Card.getCardByName(cardName);
	    if (card.getInspectPower() > inspectPower) {
		inspectPower = card.getInspectPower();
	    }
	}
	return inspectPower;
    }

    public static void printTotalCases() {
	InspectCBR.printNCases();
	CargoCBR.printNCases();
    }

    public boolean canInspect() {
	if (getInspectPower() > 0)
	    return true;
	return false;
    }

    public Card getCardUsedToInspect() {
	return cardUsedToInspect;
    }

    // Used by automated agents
    public Card getBestCardToInspect() {
	Card retVal = cardUsedToInspect;
	for (String cardName : hand) {
	    Card card = Card.getCardByName(cardName);
	    if (card.getInspectPower() > retVal.getInspectPower())
		retVal = card;
	}
	hand.remove(retVal.getCardName());
	return retVal;
    }

    public void cleanCargo() {
	cargo = new ArrayList<String>();

    }
}
