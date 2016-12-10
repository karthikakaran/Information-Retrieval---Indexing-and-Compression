//===========================================================================================================================
//	Program : Class that contains all compression techniques
//===========================================================================================================================
//	@author: Karthika Karunakaran
// 	Date created: 2016/10/26
//===========================================================================================================================
import java.util.*;

public class Compression {
	
	public static List<Long> termPtr;
	public static List<Long> frontCodeTermPtr;
	public static List<String> longDictionaryTerms;
	public static List<String> storeResult;
	//Convert to bit and return byte values
	public static byte[] convertToByteArray(String gammacode) {
		BitSet bitSet = new BitSet(gammacode.length());
		for (int i = 0; i < gammacode.length(); i++) {
			Boolean value = gammacode.charAt(i) == '1' ? true : false;
			bitSet.set(i, value);
		}
		return bitSet.toByteArray();
	}
	//gamma code technique
	public static String gammaCompression (Integer number) {
		String result = "";
		String binString = Integer.toBinaryString(number);
		String offset = binString.substring(1);
		int lengthBin = offset.length();
		char[] chars = new char[lengthBin];
		Arrays.fill(chars, '1');
		String unaryString = new String(chars);	
		unaryString = unaryString.concat("0").concat(offset);
		result = unaryString;
		return result;
	}
	//delta code technique
	public static String deltaCompression (Integer number) {
		String result = "";
		String binString = Integer.toBinaryString(number);
		int lengthBin = binString.length();
		String gammaNumber = gammaCompression(lengthBin);
		String offset = binString.substring(1);
		result = gammaNumber.concat(offset);
		return result;
	}
	//To find the gaps between the docIds
	public static List<Integer> gapsFinder(List<Integer> list) {
		List<Integer> gapsForList = new LinkedList<>();
		int index = 0, nextIndex = 0;
		int firstIndex = list.get(index);
		gapsForList.add(firstIndex);
		while (index < list.size()) {
			index++;
			nextIndex = list.get(index);
			gapsForList.add(firstIndex - nextIndex);
			firstIndex = nextIndex;
		}
		return gapsForList;
	}
	//Blocking compression with K = 8, so pointers are set at every 8 terms
	public static void blockingCompression(TreeMap<String, DictionaryClass> info, boolean frontCoding) {
		int k = 8, wordCount = 0; 
		long totalCount = 0;
		StringBuilder longString = new StringBuilder();
		for(String term : info.keySet()) {
			longString.append(term.length()+term);
		}
		longDictionaryTerms = new ArrayList<>();
		int j = 0; 
		while(j < longString.length() - 1) {
			if (Character.isDigit(longString.charAt(j)) && Character.isDigit(longString.charAt(j + 1))) {
				longDictionaryTerms.add(longString.charAt(j) + "" + longString.charAt(j + 1));
				j++;
			}
			else 
				longDictionaryTerms.add(String.valueOf(longString.charAt(j)));
			j++;
		}
		//Freq	-->	PostingslistPtr	-->	termPtr
		termPtr = new ArrayList<Long>();
		for (String ch : longDictionaryTerms) {
			if (ch == null) break;
			if (ch.matches("\\d+")) {
				if(wordCount % k == 0) {
					termPtr.add(totalCount);
				}
				wordCount++;
			}
			totalCount++;
		}
		//calling the front coding if TRUE is passed
		if (frontCoding) {
			storeResult = new ArrayList<>();
			int ptr = 0;
			while (ptr <  termPtr.size()) {
				if (ptr ==  termPtr.size() - 1)
					frontCoding(longDictionaryTerms.subList(termPtr.get(ptr).intValue(), longDictionaryTerms.size() - 1));
				else
					frontCoding(longDictionaryTerms.subList(termPtr.get(ptr).intValue(), termPtr.get(ptr + 1).intValue() - 1));
				ptr++;
			}
		}
	}

	//Front code technique
	public static void frontCoding (List<String> longDictionaryTerms) {
		frontCodeTermPtr = new ArrayList<>();
		frontCodeTermPtr.addAll(termPtr);
		int iPtr = 1, jPtr = 1, help = 0, tmpiPtr = 0,lastStoreIndex = 0;
		String remain = "";
		while (jPtr < longDictionaryTerms.size()) {
			iPtr = 1;
			tmpiPtr = iPtr;
			while(!isDigit(longDictionaryTerms.get(iPtr))) {
				iPtr++;
			}
			if (jPtr == 1)
				jPtr = iPtr + 1;
			while (jPtr < longDictionaryTerms.size() && longDictionaryTerms.get(tmpiPtr).equals(longDictionaryTerms.get(jPtr))) {
				tmpiPtr++;
				jPtr++;
			}
			//term pointer for front code
			frontCodeTermPtr.add((long) help);
			if (help < tmpiPtr) {
				storeResult.addAll(longDictionaryTerms.subList(help, tmpiPtr));
				help = tmpiPtr;
				storeResult.add("*");
				help++;
				storeResult.addAll(longDictionaryTerms.subList(tmpiPtr, iPtr));
				lastStoreIndex = help + iPtr - tmpiPtr;
				storeResult.add(String.valueOf(iPtr - tmpiPtr)); 
				help = ++lastStoreIndex;
			}
			remain = "$";
			while(jPtr < longDictionaryTerms.size() && !isDigit(longDictionaryTerms.get(jPtr))) {
				remain = remain + " " + longDictionaryTerms.get(jPtr);
				jPtr++;
			}
			remain =  remain + " " + remain.length();
			for(String s : remain.split(" "))
				storeResult.add(s);
			lastStoreIndex = help + remain.length();
			help = lastStoreIndex++;
			jPtr++;
		}
	}
	
	//To check if a string is a digit
	public static boolean isDigit (String digit) {
		boolean flag = true;
		if (!digit.matches("\\d*"))
			flag = false;
		return flag;
	}
}
