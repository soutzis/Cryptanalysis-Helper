import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

public class ColumnTransAPI {

    static List<Integer> getDivisorsList(int n) {
        List<Integer> divisors = new ArrayList<>();
        //start from 2. anything modulo 1 will equal 0, so result will be same as cipher.
        for (int i=2;i<n;i++) {
            if (n % i == 0)
                divisors.add(i);
        }
        return divisors;
    }

    /**
     * Recursive method to get all possible permutations of a String of length n
     * @param str The String to get all permutations of
     * @param permutations The list to add all permutations to
     */
    private static void getStringPermutations(String str, List<String> permutations) {
        getStringPermutations(Constants.EMPTY_STRING, str, permutations);
    }

    /**
     * This is an overloaded method that will be called recursively. It will add all possible permutations of a
     * String, to the array-list parsed as a parameter. (Usually an empty array-list {new})
     * @param permutation The string the will hold all characters to form possible permutations.
     * @param str The substring that will determine if a permutation has been formed (via its length)
     * @param permutationList The list that all possible permutations will be added to.
     */
    private static void getStringPermutations(String permutation, String str, List<String> permutationList) {

        //Get length of 'str' (which is a substring of the originally parsed string)
        int n = str.length();
        //terminating condition (if str is empty, then there are no more letters to add to the string 'prefix'
        if (n == 0)
            permutationList.add(permutation);
        else {
            for (int i = 0; i < n; i++) {
                getStringPermutations(
                        permutation + str.charAt(i),
                        str.substring(0, i) + str.substring(i + 1, n),
                        permutationList);
            }
        }
    }

    private static List<String> getKeyPermutations(String str){
        List<String> permutationList = new ArrayList<>();
        getStringPermutations(str, permutationList);

        return permutationList;
    }

    private static String getWordIndices(int len){
        StringBuilder sb = new StringBuilder();

        for(int i=0; i<len; i++){
            sb.append(i);
        }

        return sb.toString();
    }

    private static String rearrangeCipher(String cipher, int columnLength, int keyLength){
        StringBuilder sb = new StringBuilder();
        for(int i=0; i<columnLength; i++){
            for(int j=0; j<keyLength; j++){
                int charPosition = i+(j*columnLength); //position of the character to be appended next
                String character = cipher.substring(charPosition, charPosition+1); //get char as a substring
                sb.append(character); //append and loop
            }
        }
        return sb.toString();
    }

    /**
     * @param n the number to get the factorial value of. E.g n=3, n!=3*2*1=6
     * @return The factorial value of number 'n'
     */
    private static int factorial(int n){

        return n > 0 ? factorial(n-1)*n : 1;
    }

    /**
     * Regex matches an empty string that has the last match (\G) followed by N characters .
     * @param input The String to convert to ngrams
     * @param n The number of characters each String will have.
     * @return Array of NGrams (Strings)
     */
    private static String[] convertToNGrams(String input, int n){
        return input.split(String.format("(?<=\\G.{%1$d})", n));
    }

    private static String applyKeyToFragments(String[] fragments, String key){
        String[] indices = key.split(Constants.EMPTY_STRING);//get each index as string
        StringBuilder sb = new StringBuilder();

        for (String fragment : fragments){
            String[] fragmentLetters = fragment.split(Constants.EMPTY_STRING);
            for (String index : indices) {
                int letterPos = Integer.parseInt(index);
                sb.append(fragmentLetters[letterPos]);
            }
        }
        return sb.toString();
    }

    //TODO MAKE PROCESS MULTI-THREADED, AT 8! PERMUTATIONS -> TOO SLOW
    static void applyColumnTrans(List<String> input){
        Instant start = Instant.now();// get current timestamp to measure execution time
        HashSet<String> dictionary = SpellChecker.loadDictionary();
        String cipher = String.join(Constants.EMPTY_STRING, input); //convert all lines of text into single string
        int cipherLength = cipher.length();
        List<Integer> keyLengths = getDivisorsList(cipherLength);
        keyLengths.removeIf(i -> i > Constants.MAX_KEY_LENGTH_COL_TRANS); //only keys up to length 10
        HashMap<String, String> anagrams = new HashMap<>();
        LinkedHashMap<String, Integer> fitnessScores = new LinkedHashMap<>();
        //int fitnessThreshold = cipherLength/Constants.AVG_WORD_LENGTH;
        String output = null;

        for(Integer keyLength : keyLengths) {
            int columnLength = cipherLength / keyLength; //should never have remainder. if there is, then it is a bug.
            String anagram = rearrangeCipher(cipher, columnLength, keyLength);
            String startPermutationsMessage = "Now applying possible permutations for the following anagram:\n" +
                    "[[[PLEASE WAIT FOR PROCESS TO FINISH]]]\n" +
                    "{KEY LENGTH: " + keyLength + ", POSSIBLE PERMUTATIONS: " + factorial(keyLength) + "}\n" +
                    Constants.LINE + "\n" + anagram + "\n" + Constants.LINE + "\n";
            System.out.println(startPermutationsMessage);
            //If user says yes, start applying permutations. If user says no, continue loop
            //Start applying all possible keys, one by one
            String rowAsIndices = getWordIndices(keyLength); //get string where each char is an index e.g."0123"
            List<String> keys = getKeyPermutations(rowAsIndices); //get all permutations possible (a.k.a all keys)

//            int allTimeHighestFitness = 0, loopHighestFitness = 0;

            //skip first key, because it is the same as the anagram shown to user before.
            for (int i = 1; i < keys.size(); i++) {
                String[] anagramFragments = convertToNGrams(anagram, keyLength);
                String key = keys.get(i);

                String result = applyKeyToFragments(anagramFragments, key);
                int fitness = SpellChecker.getCharByCharFitness(result, dictionary);
                anagrams.put(key, result);
                fitnessScores.put(key, fitness);
//                if(fitness > loopHighestFitness)
//                    loopHighestFitness = fitness;
                //System.out.println(result+", {KEY = "+key+"}");
            }
//            if(loopHighestFitness > allTimeHighestFitness)
//                allTimeHighestFitness = loopHighestFitness;
//            else if(loopHighestFitness < allTimeHighestFitness) {
//                System.out.println("Possible key length detected: "+keyLength+"\nAttempting to find key...");
//                break;
//            }
        }
        //sort scores descending. e.g. 10, 9, 8 ...
        fitnessScores = fitnessScores.entrySet().stream()
                .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue, LinkedHashMap::new));

        //get best score
        Instant finish = Instant.now();
        long minutesElapsed = Duration.between(start, finish).toMinutes();  //in millis
        long secondsElapsed = Duration.between(start, finish).getSeconds() % 60; //For 61 seconds, result is 1
        Map.Entry<String, Integer> bestScore = fitnessScores.entrySet().iterator().next();
        String key = bestScore.getKey();
        System.out.println("Possible Key Found->"+key);
        System.out.println(Constants.LINE+"\n"+anagrams.get(key)+"\n"+Constants.LINE+"\n");
        System.out.println("{Total execution time was: "+minutesElapsed+" minutes and "+secondsElapsed+" seconds}");
    }
}
