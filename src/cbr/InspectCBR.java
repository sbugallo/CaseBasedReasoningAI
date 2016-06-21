/**
 * 
 */
package cbr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

import FreeCBR.CBR;
import FreeCBR.CBRResult;
import FreeCBR.Feature;

/**
 * @author markoscl TODO remove public
 */
public class InspectCBR {
    private static InspectCBR instance;
    /** Case based reasoning platform */
    private static final CBR cbr = new CBR();

    private static final String RISK_RATIO = "RISK_RATIO";
    private static final String ILLEGAL_PROC = "ILLEGAL_PROC";
    private static final String LEGAL_PROC = "LEGAL_PROC";
    private static final String INSPECTED_PLAYER = "INSPECTED_PLAYER";
    private static final String INSPECT_POWER = "INSPECCT_POWER";
    private static final String CARGO_SIZE = "CARGO_SIZE";
    private static final String WON_ROUNDS = "WON_ROUNDS";
    private static final String LOST_ROUNDS = "LOST_ROUNDS";
    private static final String ACTION = "ACTION";
    private static int[] searchFeatureNumbers;
    /** Search scales when searching for a cargo case */
    private static final int[] searchScales = new int[] { CBR.SEARCH_SCALE_FUZZY_LINEAR, // RiskRate
	    CBR.SEARCH_SCALE_FUZZY_LINEAR, // illegalProc
	    CBR.SEARCH_SCALE_FUZZY_LINEAR, // legalProc
	    CBR.SEARCH_SCALE_STRICT, // inspectedPlayer
	    CBR.SEARCH_SCALE_STRICT, // inspectPower
	    CBR.SEARCH_SCALE_STRICT, // cargoSize
	    CBR.SEARCH_SCALE_FUZZY_LINEAR, // wonRounds (ignored)
	    CBR.SEARCH_SCALE_FUZZY_LINEAR, // lostRounds (ignored)
	    CBR.SEARCH_SCALE_FUZZY_LINEAR // action (ignored)

    };
    private static final int[] searchTerms = new int[] { CBR.SEARCH_TERM_EQUAL, // RiskRate
	    CBR.SEARCH_TERM_EQUAL, // illegalProc
	    CBR.SEARCH_TERM_EQUAL, // legalProc
	    CBR.SEARCH_TERM_EQUAL, // inspectedPlayer (ignored)
	    CBR.SEARCH_TERM_GREATER_OR_EQUAL, // inspectPower
	    CBR.SEARCH_TERM_GREATER_OR_EQUAL, // cargoSize
	    CBR.SEARCH_TERM_GREATER_OR_EQUAL, // wonRounds (ignored)
	    CBR.SEARCH_TERM_GREATER_OR_EQUAL, // lostRounds (ignored)
	    CBR.SEARCH_TERM_GREATER_OR_EQUAL // action (ignored)

    };
    private static final int[] searchWeights = new int[] { 5, // RiskRate
	    5, // illegalProc
	    5, // legalProc
	    0, // inspectedPlayer (ignored)
	    10, // inspectPower
	    10, // cargoSize
	    0, // wonRounds (ignored)
	    0, // lostRounds (ignored)
	    0 // action (ignored)
    };
    /**
     * Minimum value for a case to be considered good. Values with a hit lower
     * than this are automatically discarded
     */
    private static final double MIN_MATCH_PERCENT = 80.0d;
    /**
     * Minimum win ratio value for cases to be considered good. Values with a
     * lower value are automatically discarded
     */
    private static final float MIN_WIN_RATIO = 0.5f;
    /**
     * All search terms are normal (not inverted)
     */
    private static final int[] searchOptions = null;

    // Avoids intialization
    private InspectCBR() {
	if (cbr.getNumFeatures() == 0) {
	    cbr.addFeature(RISK_RATIO, Feature.FEATURE_TYPE_FLOAT);
	    cbr.addFeature(ILLEGAL_PROC, Feature.FEATURE_TYPE_FLOAT);
	    cbr.addFeature(LEGAL_PROC, Feature.FEATURE_TYPE_FLOAT);
	    cbr.addFeature(INSPECTED_PLAYER, Feature.FEATURE_TYPE_INT);
	    cbr.addFeature(INSPECT_POWER, Feature.FEATURE_TYPE_INT);
	    cbr.addFeature(CARGO_SIZE, Feature.FEATURE_TYPE_INT);
	    cbr.addFeature(WON_ROUNDS, Feature.FEATURE_TYPE_INT);
	    cbr.addFeature(LOST_ROUNDS, Feature.FEATURE_TYPE_INT);
	    cbr.addFeature(ACTION, Feature.FEATURE_TYPE_MULTISTRING);
	}
	// Build feature numbers

	searchFeatureNumbers = new int[] { cbr.getFeatureNum(RISK_RATIO), cbr.getFeatureNum(ILLEGAL_PROC),
		cbr.getFeatureNum(LEGAL_PROC), cbr.getFeatureNum(INSPECTED_PLAYER), cbr.getFeatureNum(INSPECT_POWER),
		cbr.getFeatureNum(CARGO_SIZE), cbr.getFeatureNum(WON_ROUNDS), cbr.getFeatureNum(LOST_ROUNDS),
		cbr.getFeatureNum(ACTION) };

    }

    static InspectCBR getInstance() {
	instance = instance != null ? instance : new InspectCBR();
	return instance;
    }

    // TODO remove once finished
    static void printCases() {
	for (int i = 0; i < cbr.getNumCases(); i++) {
	    Feature[] case1 = cbr.getCase(i);
	    for (Feature fet : case1)
		System.out.println(fet.toString());
	}
    }

    /**
     * Searches all cases, and return the best action to take (The indexes of
     * the cards that this player should inspect on the given cargo)
     *
     * @param gameInfo
     * @param inspector
     * @return
     */
    ArrayList<Integer> search(Player inspector, Player inspectedPlayer) {
	// Creates a case from player and public info
	InspectCase playerCase = new InspectCase(inspector, inspectedPlayer);
	// We have to check if we already have a case similar to this one.If no
	// cases are similar, we have to play randomly
	InspectCase bestCase = searchBestCaseFor(playerCase);
	if (bestCase != null) {
	    System.out.println("Case found!");
	    return bestCase.getAction();
	} else {
	    // Create random inspection, and add it to cases
	    ArrayList<Integer> inspectIndexes = inspector.makeRandomInspection(inspectedPlayer.getCargo().length);
	    playerCase.setInspectIndexes(inspectIndexes);
	    addCase(playerCase);// TODO FIXME ESTOY AQUI
	    return inspectIndexes;
	}

    }

    /**
     * Returns true when a case is found that match the given case with, at
     * least, the given matchPercent
     * 
     * @param playerCase
     * @return true if a case already exists with at least that match
     */
    private boolean lookForExactCaseMatch(InspectCase playerCase, double minimumMatchPercent) {

	if (cbr.getNumCases() == 0)
	    return false;// No cases
	CBRResult[] results = cbr.search(searchFeatureNumbers, playerCase.getFeatureArray(), searchWeights, searchTerms,
		searchScales, searchOptions);
	for (CBRResult result : results) {
	    if (result.matchPercent >= minimumMatchPercent) {
		return true;
	    }
	}
	return false;

    }

    private InspectCase searchBestCaseFor(InspectCase playerCase) {
	if (cbr.getNumCases() == 0)
	    return null;// No cases
	ArrayList<InspectCase> bestMatches = new ArrayList<InspectCase>();
	CBRResult[] results = cbr.search(searchFeatureNumbers, playerCase.getFeatureArray(), searchWeights, searchTerms,
		searchScales, searchOptions);
	for (CBRResult result : results) {
	    InspectCase resultCase = new InspectCase(cbr.getCase(result.caseNum));
	    resultCase.setCaseId(result.caseNum);
	    if (result.matchPercent > MIN_MATCH_PERCENT && resultCase.getWinRatio() > MIN_WIN_RATIO) {
		bestMatches.add(resultCase);
		System.out.println("Is good.");
	    }
	}
	// Sort cases by win %
	Collections.sort(bestMatches);

	// Return best case or null, to signal that a random cargo should be
	// made
	return bestMatches.isEmpty() ? null : bestMatches.get(bestMatches.size() - 1);
    }

    /**
     * Adds a case to the existing sets of cases
     * 
     * @param caseToAdd
     */
    void addCase(InspectCase caseToAdd) {
	if (lookForExactCaseMatch(caseToAdd, 95.0) == false) {
	    cbr.addCase(caseToAdd.getFeatureArray());
	    // Set case num for this case, used on updates
	    caseToAdd.setCaseId(cbr.getNumCases() - 1);
	} else {
	    // A very similar case is already on the system, do nothing
	    return;
	}

    }

    /**
     * Updates a case, based on the roundWon parameter
     * 
     * @param caseNum
     *            Ccase to update
     * @param roundWon
     *            True if the case was successfull, false otherwise.
     */
    void updateCase(int caseNum, boolean roundWon) {
	Feature[] editedCase = cbr.getCase(caseNum);
	if (roundWon) {
	    editedCase[cbr.getFeatureNum(WON_ROUNDS)] = new Feature(
		    editedCase[cbr.getFeatureNum(WON_ROUNDS)].getIntValue() + 1);
	} else {
	    editedCase[cbr.getFeatureNum(LOST_ROUNDS)] = new Feature(
		    editedCase[cbr.getFeatureNum(LOST_ROUNDS)].getIntValue() + 1);

	}
	cbr.editCase(caseNum, editedCase);
    }

    public static void printNCases() {
	System.out.println("Number of inspec cases: " + cbr.getNumCases());

    }

    public static void SaveCasesToFile(String fileName) {
	try {
	    cbr.saveSet(fileName, false);
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
	GetCasesFromFile(fileName);
    }

    public static void GetCasesFromFile(String fileName) {
	try {
	    cbr.initialize(fileName, null);
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}
    }
}
