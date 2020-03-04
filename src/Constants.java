import java.util.*;

class Constants {
    final static String WORDS_DICTIONARY = "static-data/all_english_words.txt";
    //final static String DIRECTORY = "static-data/spellcheckerDirectory";
    final static String ALPHABET = "abcdefghijklmnopqrstuvwxyz";
    final static String LINE = "-------------------------------------------------------------------------------";
    final static String OUTPUT_FILENAME = "output.txt";
    final static String EMPTY_STRING = "";
    final static String COMMA_SEPARATOR = ",";
    final static String SPACE_SEPARATOR = " ";
    final static String UNIX_NEWLINE_SEPARATOR = "\n";
    final static String WIN_NEWLINE_SEPARATOR = "\r\n";
    final static String MAC_NEWLINE_SEPARATOR = "\r";
    final static String ALLOWED_FILE_EXTENSION = ".txt";
    final static int MODE_ENCRYPT = 1;
    final static int MODE_DECRYPT = 2;
    final static int ALPHABET_LENGTH = 26;
    final static int AVG_WORD_LENGTH = 5;
    final static int UNDERSCORE_CPOINT = 95;// decimal value of codepoint: '_'

    //Available Tools Constants. The order of tools is important and unfortunately hardcoded below.
    final static String TOOLS_AVAILABLE = "Caesar Cipher," +//Must be separated with comas, in correct order.
            "Columnar Transposition Cipher,Simple Stream Cipher,Frequency Analysis";
    final static int CAESAR_CIPHER = 1;
    final static int COLUMN_TRANSPOSITION_CIPHER = 2;
    final static int ECBS_CIPHER = 3;
    final static int FREQUENCY_ANALYSIS = 4;

    //Stream Cipher Constants
    final static int MAX_SYMBOL_VALUE = 127; //currently only supports ascii (128 out of 917503)
    final static int SYMBOLS_PER_ROW = 16; //from 0 to F (hexadecimal)
    final static int MIN_UPPER_LETTERS_ROW_VALUE = 64;
    final static int MAX_UPPER_LETTERS_ROW_VALUE = 95;

    //Columnar Transposition Constants
    final static int MAX_KEY_LENGTH_COL_TRANS = 10;

    //Frequency Analysis Constants
    static final String COMM_INIT_CHARS_PATH = "static-data/most_common_initial_letters.txt";
    static final String COMM_FIN_CHARS_PATH = "static-data/most_common_final_letters.txt";
    static final String COMM_DOUBLES_PATH = "static-data/most_common_doubles.txt";
    static final String ENG_LETTER_FREQ_PATH = "static-data/frequency_english_letters_cornell.txt";
    static final String COMM_NGRAM_PATH_PREFIX = "static-data/most_common_ngrams=";
    static final String COMM_WORDS_PATH_PREFIX = "static-data/common_words_letters=";
    static final String SUBS_FORMAT_SEPARATOR = "=";

    static final int PRINT_ORIGINAL_INPUT = 1;
    static final int PRINT_CURRENT_PROGRESS = 2;
    static final int PRINT_SUB_TABLE = 3;
    static final int PRINT_KEY = 4;
    static final int PRINT_FREQ_ANAL_INFO = 5;
    static final int PRINT_CIPHER_INFO = 6;
    static final int MAKE_SUBSTITUTION = 7;
    static final int RESET_PROGRESS = 8;
    static final int FINISH = 9;

    static List<String> getToolNamesList(){
        return new ArrayList<>(Arrays.asList(TOOLS_AVAILABLE.split(COMMA_SEPARATOR)));
    }

    //Get the english alphabet as a List, where each element is an individual character of the alphabet.
    static List<String> getAlphabetCharList(){

        return new CircularArrayList<>(Arrays.asList(ALPHABET.split(EMPTY_STRING)));
    }

    //Get the inverse Map of the alphabet, where the Key '2' will yield the object 'c'
    static Map<String, Integer> getInverseCharList(){
        String[] alphabetArray = ALPHABET.split("");
        //example record = <a,0>, <b,1>, <c,2>, <d,3>, etc...
        HashMap<String, Integer> inverseCharList = new HashMap<>();

        for(int i=0; i<alphabetArray.length; i++){
            inverseCharList.put(alphabetArray[i], i);
        }

        return inverseCharList;
    }
}
