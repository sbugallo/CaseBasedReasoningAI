package cbr;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

@SuppressWarnings("serial")
public class Deck implements Serializable {

    // TODO SERGIO INICIALIZA ESTAS CONSTANTES SEGÚN TUS NORMAS
    private static final int TOTAL_CARDS = 66;
    public static final int ILLEGAL_GOODS_CARDS = 20;
    public static final int LEGAL_GOODS_CARDS = 40;
    public static final int LIEUTENANT_CARDS = 6;

    public static final int INSPECTOR_CARDS = 0;
    public static final int CAPTAIN_CARDS = 0;

    private ArrayList<String> deck = new ArrayList<String>(TOTAL_CARDS);
    private ArrayList<String> visibleCards = new ArrayList<String>();
    private final Random rand = new Random();

    /**
     * Returns a newly initialized deck.
     */
    public Deck() {
	// Shuffles the deck
	initializeDeck();
	// From the shuffled deck, makes 4 cards visible
	for (int i = 0; i < 4; i++) {
	    visibleCards.add(getNextCard());
	}
    }

    /**
     * Gets the current cards in the deck
     *
     * @return
     */
    public int getDeckSize() {
	return deck.size();
    }

    /**
     * Generates a deck full of cards. The deck is always the same, but we
     * access it randomly (Instead of generating it randomly and accessing it
     * fixed.
     */
    private void initializeDeck() {
	for (int i = 0; i < LEGAL_GOODS_CARDS; i++) {
	    deck.add(Card.LEGAL_GOODS.getCardName());
	}
	for (int i = 0; i < ILLEGAL_GOODS_CARDS; i++) {
	    deck.add(Card.ILLEGAL_GOODS.getCardName());
	}
	for (int i = 0; i < LIEUTENANT_CARDS; i++) {
	    deck.add(Card.LIEUTENANT.getCardName());
	}
	PublicGameInfo.getInstance().setDeckSize(deck.size());

    }

    /**
     * Randomly gets (and removes) a card from the deck.
     */
    public String getNextCard() {
	if (deck.size() == 0)
	    return Card.EMPTY.getCardName();
	PublicGameInfo.getInstance().setDeckSize(deck.size());
	return deck.remove(rand.nextInt(deck.size()));
    }

    // TODO remov this once finished
    ArrayList<String> showVisibleCards() {
	return visibleCards;
    }

    /**
     * Gives player's starting cards
     *
     * @return
     */
    public List<String> getStartingCards(Player player) {
	List<String> retVal = Arrays.asList(new String[] { Card.INSPECTOR.getCardName(), Card.CAPTAIN.getCardName(),
		getNextCard(), getNextCard(), getNextCard(), getNextCard() });
	PublicGameInfo.getInstance().addPublicCardToPlayer(Card.INSPECTOR, player);
	PublicGameInfo.getInstance().addPublicCardToPlayer(Card.CAPTAIN, player);

	PublicGameInfo.getInstance().setDeckSize(deck.size());

	return retVal;

    }

    /**
     * Removes this card from the visible cards, and tries to draw another one
     * from the deck. Since the card removed was removed form the visible cards,
     * we update {@link PublicGameInfo}
     *
     * @param cardToRemove
     */
    String getCardFromVisible(Card cardToRemove, Player player) {
	visibleCards.remove(cardToRemove);
	// Set public information about this event
	PublicGameInfo.getInstance().addPublicCardToPlayer(cardToRemove, player);
	if (!deck.isEmpty())
	    visibleCards.add(getNextCard());
	return cardToRemove.getCardName();

    }

    // REMOVE this once finished
    public void printDeck() {
	System.out.println("\tVisible cards:");
	for (String visible : visibleCards) {
	    System.out.println("\t\t" + visible);
	}
	System.out.println("\tNot visible: " + deck.size());
    }

}