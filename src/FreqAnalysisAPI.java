import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toMap;

public class FreqAnalysisAPI {

    static void freqAnalysis(List<String> input){
        boolean finished = false;
        input.replaceAll(String::toUpperCase);
        System.out.println("\n\nLetters of the cipher will be in Upper-Case and plaintext letters will be Lower-Case.");
        //Order of list elements is important!
        List<String> commands = new ArrayList<>(Arrays.asList("Print Original Cipher", "Print Current Progress",
                "Print Substitution Table", "Print Key", "Print Information Table", "Print cipher text information",
                "Make a letter substitution", "Reset", "Finish (print result and optionally save to file)"));
        String prompt = "Please choose what you want to do.";
        String errMsg = "A valid choice is a number between 1 - "+commands.size();

        ArrayList<String> output = new ArrayList<>(input); //The progress is stored here
        HashMap<String, String> substitutionTable = new HashMap<>();  //This is the substitution table (aka the key).

        String common2LetterWords = getCommonWords(2);
        String common3LetterWords = getCommonWords(3);
        String common4LetterWords = getCommonWords(4);
        String mostCommonBigrams = getMostCommonNGrams(2);
        String mostCommonTrigrams = getMostCommonNGrams(3);
        String mostCommonInitialLetters = getMostCommonXXX(Constants.COMM_INIT_CHARS_PATH);
        String mostCommonFinalLetters = getMostCommonXXX(Constants.COMM_FIN_CHARS_PATH);
        String mostCommonDoubles = getMostCommonXXX(Constants.COMM_DOUBLES_PATH);
        String engLetterFreq = getEnglishLetterFrequency();

        LinkedHashMap<String, String> cipherLetterFreq = getCipherNGramFreq(input, 1);
        LinkedHashMap<String, String> cipherBigrams = getCipherNGramFreq(input, 2);
        LinkedHashMap<String, String> cipherTrigrams = getCipherNGramFreq(input, 3);
        LinkedHashMap<String, Integer> cipherDoubles = getCipherDoublesFreq(input);
        LinkedHashMap<String, Integer> cipher2LWords = getCipherWordsFreq(input, 2);
        LinkedHashMap<String, Integer> cipher3LWords = getCipherWordsFreq(input, 3);
        LinkedHashMap<String, Integer> cipher4LWords = getCipherWordsFreq(input, 4);

        //Pretty-print the input onto the screen
        UserInterface.printToUser(input, "Cipher");
        printEnglishInfoTable(mostCommonDoubles, mostCommonBigrams, mostCommonTrigrams,
                mostCommonInitialLetters, mostCommonFinalLetters, common2LetterWords,
                common3LetterWords, common4LetterWords, engLetterFreq);
        printCipherInfoTable(cipherDoubles, cipherBigrams, cipherTrigrams, cipher2LWords,
                cipher3LWords, cipher4LWords, cipherLetterFreq);

        while(!finished){
            int choice = UserInterface.multiChoice(prompt, commands, errMsg, new Scanner(System.in));
            switch (choice){
                case Constants.PRINT_ORIGINAL_INPUT://1
                    UserInterface.printToUser(input, "Original Cipher");
                    break;
                case Constants.PRINT_CURRENT_PROGRESS://2
                    UserInterface.printToUser(output, "Cryptanalysis Progress");
                    break;
                case Constants.PRINT_SUB_TABLE://3
                    printSubstitutionTable(substitutionTable);
                    break;
                case Constants.PRINT_KEY://4
                    printSubstitutionKey(substitutionTable);
                    break;
                case Constants.PRINT_FREQ_ANAL_INFO://5
                    printEnglishInfoTable(mostCommonDoubles, mostCommonBigrams, mostCommonTrigrams,
                            mostCommonInitialLetters, mostCommonFinalLetters, common2LetterWords,
                            common3LetterWords, common4LetterWords, engLetterFreq);
                    break;
                case Constants.PRINT_CIPHER_INFO://6
                    printCipherInfoTable(cipherDoubles, cipherBigrams, cipherTrigrams, cipher2LWords,
                            cipher3LWords, cipher4LWords, cipherLetterFreq);
                    break;
                case Constants.MAKE_SUBSTITUTION://7
                    makeSubstitution(substitutionTable, output);
                    UserInterface.printToUser(output, "Cryptanalysis Progress");
                    break;
                case Constants.RESET_PROGRESS:
                    substitutionTable = new HashMap<>();
                    output = new ArrayList<>(input);
                    System.out.println("Reset Complete.");
                    break;
                case Constants.FINISH:
                    finished = true;
                    output.add("\nKEY: "+getSubstitutionKey(substitutionTable));
                    UserInterface.printToUser(output, "Result");
                    UserInterface.askToWriteResultToFile(output);
                    break;
            }
        }
    }

    static void makeSubstitution(HashMap<String,String> substitutionTable, List<String> cipher){
        Scanner scanner = new Scanner(System.in);
        Pattern p = Pattern.compile("[a-zA-Z]=[a-zA-Z]");
        Matcher matcher;
        String input;
        do{
            System.out.print("Please enter the substitution you want to make.\n" +
                    "The format is: CIPHER CHARACTER=PLAINTEXT CHARACTER (example: Z=q {case insensitive})\n" +
                    "(Substitution): ");
            input = scanner.nextLine();
            matcher = p.matcher(input);
        }while(!matcher.matches());

        //The order is like this because it is intuitive.
        String key = input.split(Constants.SUBS_FORMAT_SEPARATOR)[1]; //plaintext char
        String value = input.split(Constants.SUBS_FORMAT_SEPARATOR)[0]; //cipher text char
        substitutionTable.put(key.toLowerCase(), value.toLowerCase());
        cipher.replaceAll(s -> s.replaceAll(value.toUpperCase(), key));
    }

    static LinkedHashMap<String, Integer> getCipherDoublesFreq(List<String> cipherText){
        LinkedHashMap<String, Integer> doublesCount = new LinkedHashMap<>();
        List<String> alphabet = Constants.getAlphabetCharList();
        List<String> input = new ArrayList<>(cipherText);
        input.replaceAll(String::toLowerCase);
        Pattern pattern;
        Matcher matcher;
        int incrementByOne = 1;

        for(String line : input){
            for(String letter : alphabet){
                pattern = Pattern.compile("["+letter+"]["+letter+"]");
                matcher = pattern.matcher(line);
                while (matcher.find())
                    doublesCount.merge(letter + letter, incrementByOne, Integer::sum);
            }
        }
        doublesCount = doublesCount.entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                        LinkedHashMap::new));

        return doublesCount;
    }

    static LinkedHashMap<String, Integer> getCipherWordsFreq(List<String> cipherText, int wordLength){
        LinkedHashMap<String, Integer> wordCount = new LinkedHashMap<>();
        List<String> input = new ArrayList<>(cipherText);
        input.replaceAll(String::toLowerCase); //this is necessary for comparing with alphabet (only low for simplicity)
        int incrementByOne = 1;

        for(String line : input){
            String[] words = line.split(Constants.SPACE_SEPARATOR);
            for(String word : words){
                if(word.length() == wordLength)
                    wordCount.merge(word, incrementByOne, Integer::sum);
            }
        }
        wordCount = wordCount.entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                        LinkedHashMap::new));

        return wordCount;
    }

    static LinkedHashMap<String, String> getCipherNGramFreq(List<String> cipherText, int n){
        LinkedHashMap<String, Integer> nGramsCount = new LinkedHashMap<>();
        List<String> alphabet = Constants.getAlphabetCharList();
        List<String> input = new ArrayList<>(cipherText);
        input.replaceAll(String::toLowerCase); //this is necessary for comparing with alphabet (only low for simplicity)
        int incrementByOne = 1;

        for(String line : input){ // each line of the input
            for(int i=0; i<line.length()-(n-1); i++){ //go over the line and parse characters
                String symbol = line.substring(i,i+n); //get the ngram to parse
                String[] tokens = symbol.split(Constants.EMPTY_STRING); //get the tokens that make up the symbol
                boolean validSymbol = true; //innocent until proven guilty :)
                for(String token : tokens){
                    if (!alphabet.contains(token)) {
                        validSymbol = false;
                        break;
                    }
                }
                if(validSymbol) //if symbol is 'good-to-go' then increment occurrence by one
                    nGramsCount.merge(symbol, incrementByOne, Integer::sum); //like put, but if entry exists, do sum.
            }
        }
        //Sort the LinkedHashMap Descending (highest frequency first).
        nGramsCount = nGramsCount.entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e2,
                        LinkedHashMap::new));

        //Get the sum of all letters
        int sum = nGramsCount.values().stream().reduce(0, Integer::sum);
        LinkedHashMap<String, String> sortedFreq = new LinkedHashMap<>();

        DecimalFormat df = new DecimalFormat("0.00");
        for(Map.Entry<String, Integer> entry : nGramsCount.entrySet()){
            String val = df.format(((double)entry.getValue()/sum)*100)+"%";
            String key = entry.getKey();
            sortedFreq.put(key, val);
        }

        return sortedFreq;
    }

    /**
     * Get the frequency of English letters from a TXT file. Letters are ordered from most frequent to least.
     * The frequencies provided are the ones researched from Cornell University
     * @return a HashMap, where K -> String and represents the letter and V -> Float and represents the frequency.
     */
    private static String getEnglishLetterFrequency(){
        String filename = Constants.ENG_LETTER_FREQ_PATH;
        ArrayList<String> letterFreq;
        String[] letters;
        String[] frequencies;
        StringBuilder sb = new StringBuilder();

        try {
            letterFreq = UserInterface.readFileLines(filename);
            letters = letterFreq.get(0).split(" ");
            frequencies = letterFreq.get(1).split(" ");

            for(int i=0; i<letters.length; i++){
                sb.append(letters[i].toUpperCase()).append("=").append(frequencies[i]).append("%");
                if(i != letters.length-1)
                    sb.append(", ");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return sb.toString();
    }

    private static String getMostCommonXXX(String filename){
        String result = null;
        try {
            result = UserInterface.readFileLines(filename).get(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    private static String getMostCommonNGrams(int n){
        String filename = Constants.COMM_NGRAM_PATH_PREFIX+n+Constants.ALLOWED_FILE_EXTENSION, ngrams = null;
        try {
            ngrams = UserInterface.readFileLines(filename).get(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ngrams;
    }

    private static String getCommonWords(int lettersCount){
        String filename = Constants.COMM_WORDS_PATH_PREFIX+lettersCount+Constants.ALLOWED_FILE_EXTENSION, words = null;
        try {
            words = UserInterface.readFileLines(filename).get(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return words;
    }

    static void printCipherInfoTable(Map<String, Integer> doublesCount, Map<String, String> bigramCount,
                                     Map<String, String> trigramCount, Map<String, Integer> twoLetterWordCount,
                                     Map<String, Integer> threeLetterWordCount,
                                     Map<String, Integer> fourLetterWordCount,
                                     Map<String, String> letterFrequency){
        System.out.println("*********CIPHER INFORMATION TABLE*********\n" +
                "(All the values below are sorted from most to least frequent)\n"+Constants.LINE);
        System.out.println("LETTER FREQUENCY: "+letterFrequency+"\n" +
                "BIGRAMS FREQUENCY: "+bigramCount+"\n" +
                "TRIGRAMS FREQUENCY: "+trigramCount+"\n"+
                "\nDOUBLES: "+doublesCount+"\n" +
                "2-LETTER WORDS: "+twoLetterWordCount+"\n" +
                "3-LETTER WORDS: "+threeLetterWordCount+"\n" +
                "4-LETTER WORDS: "+fourLetterWordCount);
        System.out.println(Constants.LINE+"\n");
    }

    static void printEnglishInfoTable(String mostCommonDoubles, String mostCommonBigrams, String mostCommonTrigrams,
                                      String mostCommonInitialLetters, String mostCommonFinalLetters,
                                      String common2LetterWords, String common3LetterWords,
                                      String common4LetterWords, String letterFrequency){
        System.out.println("*********ENGLISH LANGUAGE INFORMATION TABLE*********\n"+Constants.LINE);
        System.out.println("ENGLISH LETTER FREQUENCY: "+letterFrequency+"\n" +
                "\nMOST COMMON BIGRAMS: "+mostCommonBigrams+"\n" +
                "MOST COMMON TRIGRAMS: "+mostCommonTrigrams+"\n"+
                "MOST COMMON INITIAL LETTERS: "+mostCommonInitialLetters+"\n"+
                "MOST COMMON FINAL LETTERS: "+mostCommonFinalLetters+"\n"+
                "MOST COMMON DOUBLES: "+mostCommonDoubles+"\n" +
                "\nCOMMON 2-LETTER WORDS: "+common2LetterWords+"\n" +
                "COMMON 3-LETTER WORDS: "+common3LetterWords+"\n" +
                "COMMON 4-LETTER WORDS: "+common4LetterWords);
        System.out.println(Constants.LINE+"\n");
    }

    /**
     * This method will return the key used for a substitution cipher. The key corresponds to the english alphabet.
     * @param substitutionTable The Map object containing the mappings for each letter of the alphabet
     */
    static void printSubstitutionKey(HashMap<String,String> substitutionTable){
        String[] alphabet = Constants.ALPHABET.split(Constants.EMPTY_STRING);
        String unknownMapping = "?";
        StringBuilder key = new StringBuilder();

        /* For every letter of the alphabet, if that letter exists in known mappings, then add to "Key" string.
        If that cipher table is not known, then add a question mark '?'*/
        for(String letter : alphabet)
            key.append(substitutionTable.getOrDefault(letter, unknownMapping));

        System.out.println(key.toString()+"\n");
    }

    static String getSubstitutionKey(HashMap<String,String> substitutionTable){
        String[] alphabet = Constants.ALPHABET.split(Constants.EMPTY_STRING);
        String unknownMapping = "?";
        StringBuilder key = new StringBuilder();

        /* For every letter of the alphabet, if that letter exists in known mappings, then add to "Key" string.
        If that cipher table is not known, then add a question mark '?'*/
        for(String letter : alphabet)
            key.append(substitutionTable.getOrDefault(letter, unknownMapping));

        return key.toString();
    }

    /**
     * This method will print the letters of the alphabet and their corresponding character (a.k.a the key)
     * @param substitutionTable The Map object containing the mappings for each letter of the alphabet
     */
    static void printSubstitutionTable(HashMap<String,String> substitutionTable){
        String[] alphabet = Constants.ALPHABET.split(Constants.EMPTY_STRING);
        String unknownMapping = "?";

        System.out.println("The table displayed below is: \"PLAINTEXT CHARACTER -> CIPHER CHARACTER\"");
        // For every letter of the alphabet, if that letter exists in known mappings, then print mapping
        for(String letter : alphabet)
            System.out.println(letter+" -> "+substitutionTable.getOrDefault(letter, unknownMapping));

        System.out.println();
    }
}
