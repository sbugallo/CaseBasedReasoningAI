package cbr;

import java.io.IOException;

/**
 *
 */

import java.util.ArrayList;
import java.util.Collections;

import FreeCBR.CBR;
import FreeCBR.CBRResult;
import FreeCBR.Feature;

/**
 * @author Markoscl
 */
public class CargoCBR {
    private static CargoCBR instance = null;
    /** Case based reasoning platform */
    private static final CBR cbr = new CBR();

    /** At least 1 player has a lieutenant card */
    private static final String LIEUTENANT_PROC = "LIEUTENANT_PROC";
    /**
     * Risk rate. Higher means the player should risk more (Trying to go for
     * illegal goods)
     */
    private static final String RISK_RATE = "RISK_RATE";
    /** At least 1 of the other players has his captain */
    private static final String CAPTAIN_FLAG = "CAPTAIN_FLAG";
    /** At least 1 of the other player has his inspector */
    private static final String INSPECTOR_FLAG = "INSPECTOR_FLAG";
    /* Player's cards */
    private static final String NUMBER_OF_LEGAL = "LEGAL_NUM";
    private static final String NUMBER_OF_ILLEGAL = "ILLEGAL_NUM";
    private static final String INSPECT_NUMBER = "INSPECT_NUM";
    private static final String LIEUTENANT_NUMBER = "LIEUTENANT_NUM";
    private static final String CAPTAIN_NUMBER = "CAPTAIN_NUM";
    /** Number of rounds won with this strategy */
    private static final String WON_ROUNDS = "WON";
    /** Number of rounds lost with this strategy */
    private static final String LOST_ROUNDS = "LOST";
    /** Cards used on the cargo */
    private static final String CARGO = "CARGO";

    private static int[] searchFeatureNumbers;
    /** Search scales when searching for a cargo case */
    private static final int[] searchScales = new int[] { CBR.SEARCH_SCALE_FUZZY_LINEAR, // RiskRate
	    CBR.SEARCH_SCALE_STRICT, // CaptainFlag
	    CBR.SEARCH_SCALE_STRICT, // InspectorFlag
	    CBR.SEARCH_SCALE_FUZZY_LINEAR, // lieutProc
	    CBR.SEARCH_SCALE_STRICT, // LegalCards
	    CBR.SEARCH_SCALE_STRICT, // IllegalCards
	    CBR.SEARCH_SCALE_STRICT, // InspectCards
	    CBR.SEARCH_SCALE_STRICT, // LieutCards
	    CBR.SEARCH_SCALE_STRICT, // CaptainCards
	    CBR.SEARCH_SCALE_FUZZY_LINEAR, // Won value (ignored)
	    CBR.SEARCH_SCALE_FUZZY_LINEAR, // Lost value (ignored)
	    CBR.SEARCH_SCALE_FUZZY_LINEAR, // Cargo value (ignored)

    };
    private static final int[] searchTerms = new int[] { CBR.SEARCH_TERM_EQUAL, // riskRate
	    CBR.SEARCH_TERM_EQUAL, // captainFlag
	    CBR.SEARCH_TERM_EQUAL, // inspectorFlag
	    CBR.SEARCH_TERM_EQUAL, // lieutProc
	    CBR.SEARCH_TERM_GREATER_OR_EQUAL, // legalCards
	    CBR.SEARCH_TERM_GREATER_OR_EQUAL, // IllegalCards
	    CBR.SEARCH_TERM_GREATER_OR_EQUAL, // InspectCards
	    CBR.SEARCH_TERM_GREATER_OR_EQUAL, // LieutCards
	    CBR.SEARCH_TERM_GREATER_OR_EQUAL, // captainCards
	    CBR.SEARCH_TERM_NOT_EQUAL, // Won value (ignored)
	    CBR.SEARCH_TERM_NOT_EQUAL, // Lost value (ignored)
	    CBR.SEARCH_TERM_NOT_EQUAL // Cargo value (ignored)
    };
    /** Weights values */
    private static final int[] searchWeights = new int[] { 5, // RiskRate
	    10, // captainFlag
	    10, // inspectorFlag
	    5, // lieutProc
	    10, // legalCards
	    10, // illegalCards
	    10, // inspectCards
	    10, // lieutCards
	    10, // captainCards
	    0, // won (ingored)
	    0, // lost (ignored)
	    0 };// cargo (ignored)
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

    /**
     * Private constructor to enforce use of GetInstance()
     */
    private CargoCBR() {
	if (cbr.getNumFeatures() == 0) {
	    // RISK
	    cbr.addFeature(RISK_RATE, Feature.FEATURE_TYPE_FLOAT);
	    // INFO FLAGS
	    cbr.addFeature(CAPTAIN_FLAG, Feature.FEATURE_TYPE_BOOL);
	    cbr.addFeature(INSPECTOR_FLAG, Feature.FEATURE_TYPE_BOOL);
	    cbr.addFeature(LIEUTENANT_PROC, Feature.FEATURE_TYPE_FLOAT);
	    // PLAYER CARDS
	    cbr.addFeature(NUMBER_OF_LEGAL, Feature.FEATURE_TYPE_INT);
	    cbr.addFeature(NUMBER_OF_ILLEGAL, Feature.FEATURE_TYPE_INT);
	    cbr.addFeature(INSPECT_NUMBER, Feature.FEATURE_TYPE_INT);
	    cbr.addFeature(LIEUTENANT_NUMBER, Feature.FEATURE_TYPE_INT);
	    cbr.addFeature(CAPTAIN_NUMBER, Feature.FEATURE_TYPE_INT);
	    // CASE SPECIFIC INFO
	    cbr.addFeature(WON_ROUNDS, Feature.FEATURE_TYPE_INT);
	    cbr.addFeature(LOST_ROUNDS, Feature.FEATURE_TYPE_INT);
	    // CASE RESULT
	    cbr.addFeature(CARGO, Feature.FEATURE_TYPE_MULTISTRING);
	}
	// Build feature numbers
	searchFeatureNumbers = new int[] { cbr.getFeatureNum(RISK_RATE), cbr.getFeatureNum(CAPTAIN_FLAG),
		cbr.getFeatureNum(INSPECTOR_FLAG), cbr.getFeatureNum(LIEUTENANT_PROC),
		cbr.getFeatureNum(NUMBER_OF_LEGAL), cbr.getFeatureNum(NUMBER_OF_ILLEGAL),
		cbr.getFeatureNum(INSPECT_NUMBER), cbr.getFeatureNum(LIEUTENANT_NUMBER),
		cbr.getFeatureNum(CAPTAIN_NUMBER), cbr.getFeatureNum(WON_ROUNDS), cbr.getFeatureNum(LOST_ROUNDS),
		cbr.getFeatureNum(CARGO) };

    }

    /**
     * 
     * @return the single unique instance of this class
     */
    static CargoCBR getInstance() {
	if (instance == null) {
	    instance = new CargoCBR();
	}
	return instance;

    }

    /**
     * Searches all cases, and return the best action to take (What cards to
     * use)
     *
     * @param gameInfo
     * @param player
     * @return
     */
    ArrayList<String> search(PublicGameInfo gameInfo, Player player) {
	// Creates a case from player and public info
	CargoCase playerCase = new CargoCase(gameInfo, player);
	// We have to check if we already have a case similar to this one.If no
	// cases are similar, we have to play randomly
	CargoCase searchBestCaseFor = searchBestCaseFor(playerCase);
	if (searchBestCaseFor != null) {
	    return searchBestCaseFor.getCargo();
	} else {
	    // Create random cargo, and add it to cases
	    ArrayList<String> cargo = player.makeRandomCargo();
	    playerCase.setBestCargo(cargo);
	    addCase(playerCase);
	    return cargo;
	}

    }

    /**
     * Searches between cases that are very similar to the provided case, and
     * returns the best {@link CargoCase}. If we can't find a best Case, null is
     * returned.
     * 
     * @param playerCase
     * @return Best case or Null if no best case is found.
     */
    CargoCase searchBestCaseFor(CargoCase playerCase) {
	if (cbr.getNumCases() == 0)
	    return null;// No cases
	ArrayList<CargoCase> bestMatches = new ArrayList<CargoCase>();
	CBRResult[] results = cbr.search(searchFeatureNumbers, playerCase.getFeatureArray(), searchWeights, searchTerms,
		searchScales, searchOptions);
	for (CBRResult result : results) {
	    CargoCase resultCase = new CargoCase(cbr.getCase(result.caseNum));
	    resultCase.setCaseNum(result.caseNum);
	    System.out.println();
	    if (result.matchPercent > MIN_MATCH_PERCENT && resultCase.getWinRatio() > MIN_WIN_RATIO) {
		bestMatches.add(resultCase);
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
    void addCase(CargoCase caseToAdd) {
	CargoCase playerCase = searchBestCaseFor(caseToAdd);
	if (playerCase == null) {
	    // No similar case was found, create a new one
	    cbr.addCase(caseToAdd.getFeatureArray());
	    // Set case num for this case, used on updates
	    caseToAdd.setCaseNum(cbr.getNumCases() - 1);
	} else {
	    // We found a case very similar to this one. There is no point on
	    // adding another one
	    return;
	}

    }

    static void printCases() {
	for (int i = 0; i < cbr.getNumCases(); i++) {
	    Feature[] case1 = cbr.getCase(i);
	    for (Feature fet : case1)
		System.out.println(fet.toString());
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
	System.out.println("Number of cargo cases: " + cbr.getNumCases());
    }

    public static void SaveCasesToFile(String fileName) {
	try {
	    cbr.saveSet(fileName, false);
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

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
