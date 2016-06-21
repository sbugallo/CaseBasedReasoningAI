package cbr;

import java.util.ArrayList;
import java.util.Arrays;

import FreeCBR.CBR;
import FreeCBR.Feature;

public class InspectCase implements Comparable<InspectCase> {
	/**
	 * Reference for {@link CBR}, initialized to invalid value.Will be correctly
	 * set when this case is added to the platform.
	 */
	private int caseId = -1;
	/** Risk ratio for the player, higher means more risky plays */
	private double riskRatio;
	/** Observed proc of the player having illegal cards */
	private double illegalProc;
	/** Obeserved proc of the player having legal cards */
	private double legalProc;
	/** The id of the player being inspected */
	private long inspectedPlayer;
	/** Max number of cards that can be revealed */
	private long inspectPower;
	/** Number of cards being smuggled by the inspected player */
	private long cargoSize;
	// Case info
	/** Rounds won using this case */
	private long wonRounds;
	/** Rounds lost using this case */
	private long lostRounds;

	// ACTION TO TAKE
	private ArrayList<Integer> action = new ArrayList<Integer>();

	public InspectCase(Player player, Player inspectedPlayer) {
		// Fill information
		PublicGameInfo publicInfo = PublicGameInfo.getInstance();
		riskRatio = player.getRiskRatio();
		illegalProc = publicInfo.getIllegalProcForPlayer(inspectedPlayer);
		legalProc = publicInfo.getLegalProcForPlayer(inspectedPlayer);
		this.inspectedPlayer = inspectedPlayer.getId();
		cargoSize = inspectedPlayer.getCargo().length;
		// Get inspect power
		inspectPower = player.getInspectPower();
	}

	/**
	 * Builds case from features
	 * 
	 * @param features
	 */
	InspectCase(Feature[] features) {
		riskRatio = features[0].getFloatValue();
		illegalProc = features[1].getFloatValue();
		legalProc = features[2].getFloatValue();
		inspectedPlayer = features[3].getIntValue();
		inspectPower = features[4].getIntValue();
		cargoSize = features[5].getIntValue();
		wonRounds = features[6].getIntValue();
		lostRounds = features[7].getIntValue();
		String[] multiStringValue = features[8].getMultiStringValue();
		for (String val : multiStringValue) {
			action.add(Integer.parseInt(val));
		}
	}

	long getWinRatio() {
		if (wonRounds == 0)
			return 0;
		return (long) wonRounds / (long) (wonRounds + lostRounds);
	}

	/**
	 * Makes this case into a feature array
	 * 
	 * @return the case in a {@link Feature Feature[]} format
	 */
	Feature[] getFeatureArray() {

		String[] actionArray = new String[action.size()];
		for (int i = 0; i < actionArray.length; i++) {
			actionArray[i] = String.valueOf(action.get(i));
		}
		return new Feature[] { new Feature(riskRatio), // riskRatio
				new Feature(illegalProc), // illegalProc
				new Feature(legalProc), // legalProc
				new Feature(inspectedPlayer), // inspectedPlayer
				new Feature(inspectPower), // inspectionPower
				new Feature(cargoSize), // Ncards
				new Feature(wonRounds), // wonRounds
				new Feature(lostRounds), // lostRounds
				new Feature(actionArray)// action
		};
	}

	int getCaseId() {
		return caseId;
	}

	void setCaseId(int caseId) {
		this.caseId = caseId;
	}

	// TODO once finished the public modifier should be removed
	public void setInspectIndexes(ArrayList<Integer> bestAction) {
		action = bestAction;
	}

	@Override
	public int compareTo(InspectCase o) {
		if (getWinRatio() > o.getWinRatio())
			return 1;
		else if (getWinRatio() < o.getWinRatio())
			return -1;
		return 0;
	}

	public ArrayList<Integer> getAction() {
		return action;
	}

	@Override
	public String toString() {
		StringBuilder stb = new StringBuilder();
		stb.append("\t\n");
		stb.append("RiskRatio: " + riskRatio);
		stb.append("\t\n");
		stb.append("IllegalProc: " + illegalProc);
		stb.append("\t\n");
		stb.append("InspectedPlayer: " + inspectedPlayer);
		stb.append("\t\n");
		stb.append("inspectPower: " + inspectPower);
		stb.append("\t\n");
		stb.append("CargoSize: " + cargoSize);
		stb.append("\t\n");
		stb.append("ActionToTake: " + Arrays.toString(action.toArray(new Integer[action.size()])));
		stb.append("\t\n");
		stb.append("WinRatio: " + getWinRatio());
		stb.append("\n");

		return stb.toString();
	}

	public static void addAllCases(ArrayList<InspectCase> cases) {
		for (InspectCase individual : cases)
			InspectCBR.getInstance().addCase(individual);
	}

	public void setWinAndLostRatioTest(int won, int lost) {
		wonRounds = won;
		lostRounds = lost;
	}

}
