package cbr;
/**
 * 
 */

/**
 * Implements a simple representation for cards. Each card has an associated
 * name, an associated value when that card is successfully smuggled (
 * <b>cargoValue</b>) and a value when that card is not succesfully smuggled,
 * but it still remains on the player's hands once the game has finished(
 * <b>handValue</b>)
 * 
 *
 */
public enum Card {
	// (Name | cargoValue | handValue | InspectPower)

	LEGAL_GOODS("Legal", 2000, 1000, 0), //
	ILLEGAL_GOODS("Illegal", 4000, 2000, 0), //
	LIEUTENANT("Lieutenant", 5000, 2500, 1), //
	CAPTAIN("Captain", 6000, 3000, 2), //
	INSPECTOR("Inspector", 8000, 4000, 3), //
	EMPTY("empty", 0, 0, 0);//
	/** Card name */
	private String cardName;
	/** Value of this card when the card is succesfully smuggled */
	private int cargoValue;
	/** Value of this card when the game finishes and we could not smuggle it */
	private int handValue;
	/** Max number of cards that this card can make visible when inspecting */
	private int inspectPower;

	Card(String cardName, int cargoValue, int handValue, int inspectPower) {
		this.cardName = cardName;
		this.cargoValue = cargoValue;
		this.handValue = handValue;
		this.inspectPower = inspectPower;
	}

	/**
	 * Returns the {@link Card} that has the same name as cardName
	 * 
	 * @param cardName
	 *            The name of the card we want to obtain
	 * @return The obtained Card
	 */
	public static Card getCardByName(String cardName) {
		for (Card card : Card.values())
			if (card.getCardName().equals(cardName))
				return card;
		return EMPTY;
	}

	public String getCardName() {
		return cardName;
	}

	public int getCargoValue() {
		return cargoValue;
	}

	public int getHandValue() {
		return handValue;
	}

	public int getInspectPower() {
		return inspectPower;
	}

}
