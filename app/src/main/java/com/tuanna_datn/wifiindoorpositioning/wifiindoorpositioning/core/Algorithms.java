
package com.tuanna_datn.wifiindoorpositioning.wifiindoorpositioning.core;

import com.tuanna_datn.wifiindoorpositioning.wifiindoorpositioning.model.AccessPoint;
import com.tuanna_datn.wifiindoorpositioning.wifiindoorpositioning.model.IndoorProject;
import com.tuanna_datn.wifiindoorpositioning.wifiindoorpositioning.model.LocDistance;
import com.tuanna_datn.wifiindoorpositioning.wifiindoorpositioning.model.LocationWithNearbyPlaces;
import com.tuanna_datn.wifiindoorpositioning.wifiindoorpositioning.model.ReferencePoint;
import com.tuanna_datn.wifiindoorpositioning.wifiindoorpositioning.model.WifiDataNetwork;
import com.tuanna_datn.wifiindoorpositioning.wifiindoorpositioning.utils.AppContants;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.realm.RealmList;

public class Algorithms {

	final static String K = "4";


	public static LocationWithNearbyPlaces processingAlgorithms(List<WifiDataNetwork> latestScanList, IndoorProject proj, int algorithm_choice) {

		int i, j;
		RealmList<AccessPoint> aps = proj.getAps();
		ArrayList<Float> observedRSSValues = new ArrayList<Float>();
		WifiDataNetwork temp_LR;
		int notFoundCounter = 0;
		for (i = 0; i < aps.size(); ++i) {
			for (j = 0; j < latestScanList.size(); ++j) {
				temp_LR = latestScanList.get(j);
				if (aps.get(i).getMac_address().compareTo(temp_LR.getBssid()) == 0) {
					observedRSSValues.add(Float.valueOf(temp_LR.getLevel()).floatValue());
					break;
				}
			}
			if (j == latestScanList.size()) {
				observedRSSValues.add(AppContants.NaN);
				++notFoundCounter;
			}
		}

		if (notFoundCounter == aps.size())
			return null;

		String parameter = readParameter(algorithm_choice);

		if (parameter == null)
			return null;

		switch (algorithm_choice) {

		case 1:
			return KNN_WKNN_Algorithm(proj, observedRSSValues, parameter, false);
		case 2:
			return KNN_WKNN_Algorithm(proj, observedRSSValues, parameter, true);
		case 3:
			return MAP_MMSE_Algorithm(proj, observedRSSValues, parameter, false);
		case 4:
			return MAP_MMSE_Algorithm(proj, observedRSSValues, parameter, true);
		}
		return null;

	}


	private static LocationWithNearbyPlaces KNN_WKNN_Algorithm(IndoorProject proj, ArrayList<Float> observedRSSValues,
											 String parameter, boolean isWeighted) {

		RealmList<AccessPoint> rssValues;
		float curResult = 0;
		ArrayList<LocDistance> locDistanceResultsList = new ArrayList<LocDistance>();
		String myLocation = null;
		int K;

		try {
			K = Integer.parseInt(parameter);
		} catch (Exception e) {
			return null;
		}

		for (ReferencePoint referencePoint : proj.getRps()) {
			rssValues = referencePoint.getReadings();
			curResult = calculateEuclideanDistance(rssValues, observedRSSValues);

			if (curResult == Float.NEGATIVE_INFINITY)
				return null;

			locDistanceResultsList.add(0, new LocDistance(curResult, referencePoint.getLocId(), referencePoint.getName()));
		}

		Collections.sort(locDistanceResultsList, new Comparator<LocDistance>() {
			public int compare(LocDistance gd1, LocDistance gd2) {
				return (gd1.getDistance() > gd2.getDistance() ? 1 : (gd1.getDistance() == gd2.getDistance() ? 0 : -1));
			}
		});

		if (!isWeighted) {
			myLocation = calculateAverageKDistanceLocations(locDistanceResultsList, K);
		} else {
			myLocation = calculateWeightedAverageKDistanceLocations(locDistanceResultsList, K);
		}
		LocationWithNearbyPlaces places = new LocationWithNearbyPlaces(myLocation, locDistanceResultsList);
		return places;

	}

	private static LocationWithNearbyPlaces MAP_MMSE_Algorithm(IndoorProject proj, ArrayList<Float> observedRssValues, String parameter, boolean isWeighted) {
		RealmList<AccessPoint> rssValues;
		double curResult = 0.0d;
		String myLocation = null;
		double highestProbability = Double.NEGATIVE_INFINITY;
		ArrayList<LocDistance> locDistanceResultsList = new ArrayList<LocDistance>();
		float sGreek;

		try {
			sGreek = Float.parseFloat(parameter);
		} catch (Exception e) {
			return null;
		}

		for (ReferencePoint referencePoint : proj.getRps()) {
			rssValues = referencePoint.getReadings();
			curResult = calculateProbability(rssValues, observedRssValues, sGreek);

			if (curResult == Double.NEGATIVE_INFINITY)
				return null;
			else if (curResult > highestProbability) {
				highestProbability = curResult;
				myLocation = referencePoint.getLocId();
			}

			if (isWeighted)
				locDistanceResultsList.add(0, new LocDistance(curResult, referencePoint.getLocId(), referencePoint.getName()));
		}

		if (isWeighted)
			myLocation = calculateWeightedAverageProbabilityLocations(locDistanceResultsList);
		LocationWithNearbyPlaces places = new LocationWithNearbyPlaces(myLocation, locDistanceResultsList);
		return places;
	}

	private static float calculateEuclideanDistance(RealmList<AccessPoint> l1, ArrayList<Float> l2) {

		float finalResult = 0;
		float v1;
		float v2;
		float temp;

		for (int i = 0; i < l1.size(); ++i) {

			try {
				l1.get(i).getMeanRss();
				v1 = (float) l1.get(i).getMeanRss();
				v2 = l2.get(i);
			} catch (Exception e) {
				return Float.NEGATIVE_INFINITY;
			}

			temp = v1 - v2;
			temp *= temp;

			finalResult += temp;
		}
		return ((float) Math.sqrt(finalResult));
	}

	private static double calculateProbability(RealmList<AccessPoint> l1, ArrayList<Float> l2, float sGreek) {
		double finalResult = 1;
		float v1;
		float v2;
		double temp;

		for (int i = 0; i < l1.size(); ++i) {

			try {
				v1 = (float) l1.get(i).getMeanRss();
				v2 = l2.get(i);
			} catch (Exception e) {
				return Double.NEGATIVE_INFINITY;
			}

			temp = v1 - v2;

			temp *= temp;

			temp = -temp;

			temp /= (double) (sGreek * sGreek);
			temp = Math.exp(temp);

			if (finalResult * temp != 0)
				finalResult = finalResult * temp;
		}
		return finalResult;
	}

	private static String calculateAverageKDistanceLocations(ArrayList<LocDistance> LocDistance_Results_List, int K) {
		float sumX = 0.0f;
		float sumY = 0.0f;

		String[] LocationArray = new String[2];
		float x, y;

		int K_Min = K < LocDistance_Results_List.size() ? K : LocDistance_Results_List.size();

		for (int i = 0; i < K_Min; ++i) {
			LocationArray = LocDistance_Results_List.get(i).getLocation().split(" ");

			try {
				x = Float.valueOf(LocationArray[0].trim()).floatValue();
				y = Float.valueOf(LocationArray[1].trim()).floatValue();
			} catch (Exception e) {
				return null;
			}

			sumX += x;
			sumY += y;
		}

		sumX /= K_Min;
		sumY /= K_Min;

		return sumX + " " + sumY;
	}
	private static String calculateWeightedAverageKDistanceLocations(ArrayList<LocDistance> LocDistance_Results_List, int K) {
		double LocationWeight = 0.0f;
		double sumWeights = 0.0f;
		double WeightedSumX = 0.0f;
		double WeightedSumY = 0.0f;

		String[] LocationArray = new String[2];
		float x, y;

		int K_Min = K < LocDistance_Results_List.size() ? K : LocDistance_Results_List.size();

		for (int i = 0; i < K_Min; ++i) {
			if (LocDistance_Results_List.get(i).getDistance() != 0.0) {
				LocationWeight = 1 / LocDistance_Results_List.get(i).getDistance();
			} else {
				LocationWeight = 100;
			}
			LocationArray = LocDistance_Results_List.get(i).getLocation().split(" ");

			try {
				x = Float.valueOf(LocationArray[0].trim()).floatValue();
				y = Float.valueOf(LocationArray[1].trim()).floatValue();
			} catch (Exception e) {
				return null;
			}

			sumWeights += LocationWeight;
			WeightedSumX += LocationWeight * x;
			WeightedSumY += LocationWeight * y;

		}

		WeightedSumX /= sumWeights;
		WeightedSumY /= sumWeights;

		return WeightedSumX + " " + WeightedSumY;
	}
	private static String calculateWeightedAverageProbabilityLocations(ArrayList<LocDistance> LocDistance_Results_List) {
		double sumProbabilities = 0.0f;
		double WeightedSumX = 0.0f;
		double WeightedSumY = 0.0f;
		double NP;
		float x, y;
		String[] LocationArray = new String[2];

		for (int i = 0; i < LocDistance_Results_List.size(); ++i)
			sumProbabilities += LocDistance_Results_List.get(i).getDistance();

		for (int i = 0; i < LocDistance_Results_List.size(); ++i) {
			LocationArray = LocDistance_Results_List.get(i).getLocation().split(" ");

			try {
				x = Float.valueOf(LocationArray[0].trim()).floatValue();
				y = Float.valueOf(LocationArray[1].trim()).floatValue();
			} catch (Exception e) {
				return null;
			}

			NP = LocDistance_Results_List.get(i).getDistance() / sumProbabilities;

			WeightedSumX += (x * NP);
			WeightedSumY += (y * NP);

		}

		return WeightedSumX + " " + WeightedSumY;

	}

	private static String readParameter(File file, int algorithm_choice) {
		String line;
		BufferedReader reader = null;

		String parameter = null;

		try {
			FileReader fr = new FileReader(file.getAbsolutePath().replace(".txt", "-parameters2.txt"));

			reader = new BufferedReader(fr);

			while ((line = reader.readLine()) != null) {

				if (line.startsWith("#") || line.trim().equals("")) {
					continue;
				}

				String[] temp = line.split(":");

				if (temp.length != 2) {
					return null;
				}

				if (algorithm_choice == 0 && temp[0].equals("NaN")) {
					parameter = temp[1];
					break;
				} else if (algorithm_choice == 1 && temp[0].equals("KNN")) {
					parameter = temp[1];
					break;
				} else if (algorithm_choice == 2 && temp[0].equals("WKNN")) {
					parameter = temp[1];
					break;
				} else if (algorithm_choice == 3 && temp[0].equals("MAP")) {
					parameter = temp[1];
					break;
				} else if (algorithm_choice == 4 && temp[0].equals("MMSE")) {
					parameter = temp[1];
					break;
				}

			}

		} catch (Exception e) {
			return null;
		} finally {
			if (reader != null)
				try {
					reader.close();
				} catch (IOException e) {
				}
		}

		return parameter;
	}

	private static String readParameter(int algorithm_choice) {
		String parameter = null;

		if (algorithm_choice == 1) {
			// && ("KNN")
			parameter = K;
		} else if (algorithm_choice == 2) {
			// && ("WKNN")
			parameter = K;
		} else if (algorithm_choice == 3) {
			// && ("MAP")
			parameter = K;
		} else if (algorithm_choice == 4) {
			// && ("MMSE")
			parameter = K;
		}
		return parameter;
	}

}
