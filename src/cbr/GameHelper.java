package cbr;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

/**
 * Helper class to allow for unified game logic
 */
public class GameHelper {

    private HashMap<Player, String[]> cargoForPlayer = new HashMap<Player, String[]>(4);

    /**
     * Saves cargo for each player
     * 
     * @param players
     */
    public void informAboutPlayerCargo(Player[] players) {
	for (Player p : players) {
	    cargoForPlayer.put(p, p.getCargo());
	}
    }

    /**
     * Get all players that can be inspected
     * 
     * @return
     */
    public ArrayList<Player> getInspectablePlayers() {
	ArrayList<Player> aux = new ArrayList<Player>();
	aux.addAll(cargoForPlayer.keySet());
	return aux;
    }

    /**
     * Plays an inspection round. The inspector will try to inspect the
     * inspected player.
     * 
     * <br>
     * <br>
     * First, we check who has the most powerful card. If the inspected player
     * has a more powerful card he can avoid inspection, and instantly wins the
     * cargo + the card used to inspect him.
     * 
     * <br>
     * <br>
     * If the inspected player has less power, then the inspection begins. The
     * inspector will inspect the given indexes of that cargo, and if an illegal
     * card is between the inspected cards, the inspector wins.
     * 
     * <br>
     * <br>
     * The winner will get all the associated value of all the cards in the
     * cargo + the card used to inspect. Those cards are instantly placed in his
     * warehouse.
     * 
     * <br>
     * <br>
     * Public information is also updated, so risk rates can be correctly
     * calculated.
     * 
     * @param inspector
     * @param inspected
     * @param indexes
     */
    public void makeInspection(Player inspector, Player inspected, ArrayList<Integer> indexes, Card cardUsedToInspect) {
	// CHECK VARS
	if (indexes.size() > cardUsedToInspect.getInspectPower())
	    throw new IllegalArgumentException(
		    "You can´t inspect: " + indexes.size() + " cards using the inspector card: "
			    + cardUsedToInspect.getCardName() + " not enought inspecting power.");
	if (!cargoForPlayer.containsKey(inspected))
	    throw new IllegalStateException(
		    "Can´t inspect that player, because no cargo was associated for player: " + inspected.getId());

	System.out.println("********************************************************************************");
	System.out.println("Player: " + inspector.getId() + " is inspecting: " + inspected.getId() + " using card: "
		+ cardUsedToInspect.toString());
	System.out.println("Cargo:   " + Arrays.toString(inspected.getCargo()));
	System.out.println("Indexes: " + Arrays.toString(indexes.toArray()));
	// First, check if the inspected player can avoid being inspected
	// because he has more inspection power

	boolean inspectorCanInspect = true;
	boolean inspectorWins = false;
	int profit = cardUsedToInspect.getCargoValue();
	String[] cargo = cargoForPlayer.remove(inspected);
	Card bestInspectorPowerOnInspectedPlayer = Card.EMPTY;
	for (String cardName : cargo) {
	    Card card = Card.getCardByName(cardName);
	    profit += card.getCargoValue();
	    if (card.getInspectPower() > cardUsedToInspect.getInspectPower()) {
		inspectorCanInspect = false;
		bestInspectorPowerOnInspectedPlayer = card;
	    }
	}
	if (inspectorCanInspect) {
	    // Inspector can inspect, check if he finds any illegal Goods card
	    for (Integer inspectedIndex : indexes) {
		if (Card.getCardByName(cargo[inspectedIndex]).equals(Card.ILLEGAL_GOODS)) {
		    // Inspected player has been discovered!
		    inspectorWins = true;
		    break;
		}
	    }
	} else {
	    System.out.println("Inspected player blocked inspector!!");
	    // Inspected player wins, but we have to remove the cards that they
	    // were using to inform all players
	    PublicGameInfo.getInstance().removePublicCardFromPlayer(cardUsedToInspect, inspector);
	    PublicGameInfo.getInstance().removePublicCardFromPlayer(bestInspectorPowerOnInspectedPlayer, inspected);

	}

	if (inspectorWins) {
	    System.out.println("Inspector found illegal goods!!");
	    // Add all cards into inspector + the card used to inspect (public
	    // info)
	    inspector.addCardsToWarehouse((cargo.length + 1));
	    inspector.addProfit(profit);
	    System.out.println("Added: " + profit + " to inspector profit, because he added: " + (cargo.length + 1)
		    + " cards to his warehouse.");
	    // Update public information
	    for (String cardName : cargo) {
		PublicGameInfo.getInstance().removePublicCardFromPlayer(Card.getCardByName(cardName), inspector);
	    }
	} else {
	    System.out.println("Inspector could not find any illegal goods.");

	    // Add all cards into inspected + the card used to inspect (public
	    // info)
	    System.out.println("Added: " + profit + " to inspected player profit, because he added: "
		    + (cargo.length + 1) + " cards to his warehouse.");

	    inspected.addCardsToWarehouse(cargo.length + 1);
	    inspector.addProfit(profit);
	    for (String cardName : cargo) {
		PublicGameInfo.getInstance().removePublicCardFromPlayer(Card.getCardByName(cardName), inspected);

	    }
	}

	inspected.cleanCargo();

    }

    public String[] getCargoForPlayer(Player p) {
	return cargoForPlayer.get(p);
    }
}
