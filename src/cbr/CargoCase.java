package cbr;

import java.util.ArrayList;
import java.util.Arrays;

import FreeCBR.CBR;
import FreeCBR.Feature;

/**
 * Utility for case container
 * 
 *
 */
public class CargoCase implements Comparable<CargoCase> {
	/**
	 * Reference for {@link CBR}, initialized to invalid value.Will be correctly
	 * set when this case is added to the platform.
	 */
	private int caseId = -1;
	private double riskRate;
	private boolean captainFlag;
	private boolean inspectorFlag;
	private double lieutProc;
	private long legalCards;
	private long illegalCards;
	private long inspectCards;
	private long lieutCards;
	private long captainCards;
	private long wonRounds;
	private long lostRounds;
	private String[] cargo = null;

	CargoCase(Feature[] features) {
		riskRate = features[0].getFloatValue();
		captainFlag = features[1].getBoolValue();
		inspectorFlag = features[2].getBoolValue();
		lieutProc = features[3].getFloatValue();
		legalCards = features[4].getIntValue();
		illegalCards = features[5].getIntValue();
		inspectCards = features[6].getIntValue();
		lieutCards = features[7].getIntValue();
		captainCards = features[8].getIntValue();
		wonRounds = features[9].getIntValue();
		lostRounds = features[10].getIntValue();
		cargo = features[11].getMultiStringValue();
	}

	/**
	 * Constructs a case from publicInformation and a player's info
	 * 
	 * @param publicInfo
	 * @param player
	 */
	CargoCase(PublicGameInfo publicInfo, Player player) {
		riskRate = player.getRiskRatio();
		captainFlag = publicInfo.isCardStillOnPlay(Card.CAPTAIN);
		inspectorFlag = publicInfo.isCardStillOnPlay(Card.INSPECTOR);
		lieutProc = publicInfo.getLieutenantProc();
		// Count cards
		// Recount player's cards
		for (String cardName : player.getHand()) {
			switch (Card.getCardByName(cardName)) {
			case LEGAL_GOODS:
				legalCards++;
				break;
			case ILLEGAL_GOODS:
				illegalCards++;
				break;
			case INSPECTOR:
				inspectCards++;
				break;
			case LIEUTENANT:
				lieutCards++;
				break;
			case CAPTAIN:
				captainCards++;
				break;
			default:// Do nothing

			}
		}

	}

	// TODO remove this constructor once finished
	public CargoCase(double riskRate2, boolean captainFlag2, boolean inspectorFlag2, double lieutProc2,
			long legalCards2, long illegalCards2, long inspectCards2, long lieutCards2, long captainCards2,
			long wonRounds2, long lostRounds2, String[] cargo2) {
		riskRate = riskRate2;
		captainFlag = captainFlag2;
		inspectorFlag = inspectorFlag2;
		lieutProc = lieutProc2;
		legalCards = legalCards2;
		illegalCards = illegalCards2;
		inspectCards = inspectCards2;
		lieutCards = lieutCards2;
		captainCards = captainCards2;
		wonRounds = wonRounds2;
		lostRounds = lostRounds2;
		cargo = cargo2;
	}

	Feature[] getFeatureArray() {
		return new Feature[] { new Feature(riskRate), //
				new Feature(captainFlag), //
				new Feature(inspectorFlag), //
				new Feature(lieutProc), //
				new Feature(legalCards), //
				new Feature(illegalCards), //
				new Feature(inspectCards), //
				new Feature(lieutCards), //
				new Feature(captainCards), //
				new Feature(wonRounds), //
				new Feature(lostRounds), //
				new Feature(cargo)//
		};
	}

	// TOO remove this once finished
	@Override
	public String toString() {
		StringBuilder stb = new StringBuilder();
		stb.append("\tRisk: " + riskRate);
		stb.append("\n\t");
		stb.append("capFlag: " + captainFlag);
		stb.append("\n\t");
		stb.append("InspFlag: " + inspectorFlag);
		stb.append("\n\t");
		stb.append("lieutProc: " + lieutProc);
		stb.append("\n\t");
		stb.append("legalN: " + legalCards);
		stb.append("\n\t");
		stb.append("illegalN: " + illegalCards);
		stb.append("\n\t");
		stb.append("lieutN: " + lieutCards);
		stb.append("\n\t");
		stb.append("capN: " + captainCards);
		stb.append("\n\t");
		stb.append("won: " + wonRounds);
		stb.append("\n\t");
		stb.append("lost: " + lostRounds);
		stb.append("\n\t");
		stb.append("Cargo: " + Arrays.toString(cargo));
		stb.append("\n\t");
		stb.append("***** WIN %: " + getWinRatio() + "*****");
		return stb.toString();

	}

	@Override
	public int compareTo(CargoCase other) {
		if (getWinRatio() < other.getWinRatio()) {
			return -1;
		} else if (getWinRatio() > other.getWinRatio())
			return 1;
		return 0;
	}

	float getWinRatio() {
		if (wonRounds == 0)
			return 0;
		return (float) wonRounds / (float) (lostRounds + wonRounds);
	}

	public ArrayList<String> getCargo() {
		ArrayList<String> retVal = new ArrayList<String>(cargo.length);
		retVal.addAll(Arrays.asList(cargo));
		return retVal;
	}

	void setCaseNum(int numCases) {
		caseId = numCases;

	}

	int getCaseId() {
		return caseId;
	}

	void setBestCargo(ArrayList<String> cargo2) {
		cargo = cargo2.toArray(new String[cargo2.size()]);
	}

	public static void addALlCases(ArrayList<CargoCase> cases) {
		for (CargoCase cas : cases)
			CargoCBR.getInstance().addCase(cas);

	}
}
