import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import statsPackage.Hasher;
import statsPackage.PredictiveEngine;
import triePackage.Trie;

public class SmartWord {
	Hasher hasher;
	PredictiveEngine predictiveEngine;
	String wordFile;
	Trie vocabTrie;
	int totalVocab, inWordCount;
	ArrayList<String> oldMessages;

	double totalWordCount = 0;

	public SmartWord (String wordFile) throws FileNotFoundException {
		this.vocabTrie = new Trie();
		this.hasher = new Hasher();
		this.predictiveEngine = new PredictiveEngine(hasher, vocabTrie);
		this.wordFile = wordFile;
		this.totalVocab = 0;
		this.inWordCount = 0;
		this.oldMessages = new ArrayList<>();
	}

	public void processOldMessages (String oldMessageFile) throws FileNotFoundException {
		processVocab();
	
		try (BufferedReader tempBR = new BufferedReader(new FileReader(new File(oldMessageFile)))) {
			String line;
			while ((line = tempBR.readLine()) != null) {
				line = line.toLowerCase();
				String[] lineArr = line.split(" ");
				int lineLength = lineArr.length - 1;
	
				for (int i = 0; i < lineArr.length; i++) {
					String temp = lineArr[i];
					String cleanTemp = cleanString(temp);
	
					if (!cleanTemp.isEmpty()) {
						oldMessages.add(cleanTemp);
	
						hasher.addCorrespondence(cleanTemp, i, lineLength);
	
						if (!vocabTrie.contains(temp)) {
							vocabTrie.insert(temp.toCharArray());
						}
					}
				}
				totalWordCount++;
			}
			hasher.hashOldMessages(oldMessages, totalWordCount);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Cleans a word by removing all non-alphabetic characters using StringBuilder.
	 *
	 * @param word The input word to be cleaned.
	 * @return A cleaned word containing only alphabetic characters.
	 */
	private String cleanString (String word) {
		StringBuilder stringConstruct = new StringBuilder();
		for (char tempChar : word.toCharArray()) {
			if (Character.isLetter(tempChar)) {
				stringConstruct.append(tempChar);
			}
		}
		return stringConstruct.toString();
	}

	public void processVocab () throws FileNotFoundException {
		try (final BufferedReader tempBR = new BufferedReader(new FileReader(new File(wordFile)))) {
			String line;
			while ((line = tempBR.readLine()) != null) {
				vocabTrie.insert(line.toCharArray());
			}

			hasher.addVocabTrie(vocabTrie);

		} catch (FileNotFoundException e) {
			throw e;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Use predictive engine to process the best guesses.
	 * @param letter			input letter
	 * @param letterPosition	input letter's position in a word
	 * @param wordPosition		relative position on a line
	 * @return					3 best guesses as a string array list
	 */
	public String[] guess (
	char letter,
	int letterPosition,
	int wordPosition
	) {

		String[] guesses = predictiveEngine.exValKeys(
			letter,
			letterPosition,
			wordPosition
		);

		hasher.computeNewProbabilities(guesses, false);

		return guesses;
	}

	public void feedback (boolean isCorrectGuess, String correctWord) {
		if (isCorrectGuess && correctWord != null && !correctWord.isEmpty()) {
			hasher.previousWord = correctWord;
		}

		if (hasher.probabilityMap.containsKey(correctWord)) {
			hasher.hashNewMessage(correctWord, true);
		} else {
			hasher.addProbability(correctWord, hasher.getAvgProbability());
		}


	}
}
