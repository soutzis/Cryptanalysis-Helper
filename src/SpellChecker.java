import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SpellChecker {

    //When using the method "getCharByCharFitness", checking for words of length 1,2,3 will produce high fitness
    //for strings that are obviously incorrect. Thus, by setting a 'minimum' of chars, more meaningful results are
    //generated.
    private static final int MIN_WORD_LEN = 3;

    static HashSet<String> loadDictionary (){
        HashSet<String> dictionary = null;

        //Get lines of text (Strings) as a stream
        try (Stream<String> stream = Files.lines(Paths.get(Constants.WORDS_DICTIONARY))){
            // convert stream to a List-type object
            dictionary = (HashSet<String>)stream.collect(Collectors.toSet());
        }

        catch (SecurityException | IOException se){
            System.out.println("Could not read the file provided. Please check if the file exists and that " +
                    "you have permission to access it.");
            System.exit(-1);
        }

        return dictionary;
    }

    private static String arrayToString(String[] array){
        StringBuilder sb = new StringBuilder();
        for(String str : array)
            sb.append(str);

        return sb.toString();
    }

    private static String[] cleanAndTokenize(String str){
        //TODO make regex into one-liner
        str = str.replaceAll("(\\r|\\n)", Constants.EMPTY_STRING) //remove any new line separators, insignificant
                .replaceAll("1st", Constants.EMPTY_STRING)
                .replaceAll("2nd", Constants.EMPTY_STRING)
                .replaceAll("3rd", Constants.EMPTY_STRING)
                .replaceAll("4th", Constants.EMPTY_STRING)
                .replaceAll("5th", Constants.EMPTY_STRING)
                .replaceAll("6th", Constants.EMPTY_STRING)
                .replaceAll("7th", Constants.EMPTY_STRING)
                .replaceAll("8th", Constants.EMPTY_STRING)
                .replaceAll("9th", Constants.EMPTY_STRING)
                .replaceAll("0th", Constants.EMPTY_STRING)
                .replaceAll("\\d","")
                .replaceAll("'d", Constants.EMPTY_STRING)
                .replaceAll("'ed", Constants.EMPTY_STRING)
                .replaceAll(",", Constants.EMPTY_STRING)
                .replaceAll("\\.", Constants.EMPTY_STRING)
                .replaceAll("-", Constants.EMPTY_STRING)
                .replaceAll("!", Constants.EMPTY_STRING)
                .replaceAll("\\?", Constants.EMPTY_STRING)
                .replaceAll("\"", Constants.EMPTY_STRING)
                .toLowerCase(); //make all letters lower case to avoid cases like 'EnIgMa MaCHiNe'

        String[] words = str.split(Constants.SPACE_SEPARATOR);
        List<String> alphabet = Constants.getAlphabetCharList();
        List<String> processedString = new ArrayList<>();
        for(int i=0; i<words.length; i++) {
            words[i] = words[i].trim();//remove trailing whitespace
            String[] wordSymbols = words[i].split(Constants.EMPTY_STRING);
            boolean validWord = true;
            for (String symbol : wordSymbols) {
                if (!alphabet.contains(symbol)) {
                    validWord = false;
                }
            }
            if(validWord)
                processedString.add(words[i]);
        }
        String[] result = new String[processedString.size()];
        return processedString.toArray(result);
    }

    //checks substring-by-substring, due to absence of white-spaces
    static int getCharByCharFitness(String input, HashSet<String> dictionary){
        input = input.toLowerCase();
        int fitness = 0, leftPointer = 0, strLen = input.length();
        int lengthToCheck = MIN_WORD_LEN;
        //not actually pointer. difference from leftPointer specifies length of substring
        int rightPointer = lengthToCheck;
        //don't consider words less than 3 chars long, otherwise results are skewed
        while(lengthToCheck <= strLen){
            String word = input.substring(leftPointer, rightPointer);
            String lowerCaseInitial = word.substring(0,1); //first letter is always lower-case, because of pre-processing
            String upperCaseInitial = lowerCaseInitial.toUpperCase();
            String uppercaseWord = word.replaceFirst(lowerCaseInitial, upperCaseInitial);
            if (dictionary.contains(word) || dictionary.contains(uppercaseWord))
                fitness+=1;

            leftPointer+=1;
            rightPointer+=1;
            if(rightPointer > strLen){
                leftPointer = 0;
                lengthToCheck += 1;
                rightPointer = lengthToCheck;
            }
        }
        return fitness;
    }


    static int getStringFitness(String input, HashSet<String> dictionary){
        int fitness = 0;
        String[] words = cleanAndTokenize(input);
        for(String word : words) {
            if(word.equals(Constants.EMPTY_STRING))
                continue;
            String lowerCaseInitial = word.substring(0,1); //first letter is always lower-case, because of pre-processing
            String upperCaseInitial = lowerCaseInitial.toUpperCase();
            String uppercaseWord = word.replaceFirst(lowerCaseInitial, upperCaseInitial);
            if (dictionary.contains(word) || dictionary.contains(uppercaseWord))
                fitness+=1;
        }

        return fitness;
    }

    static boolean allWordsInStringExist(String input, HashSet<String> dictionary){
        String[] words = cleanAndTokenize(input);
        for(String word : words) {
            if(word.equals(Constants.EMPTY_STRING))
                continue;
            String lowerCaseInitial = word.substring(0,1); //first letter is always lower-case, because of pre-processing
            String upperCaseInitial = lowerCaseInitial.toUpperCase();

            String uppercaseWord = word.replaceFirst(lowerCaseInitial, upperCaseInitial);
            if (!dictionary.contains(word) && !dictionary.contains(uppercaseWord))
                return false;
        }
        return true;
    }
}
