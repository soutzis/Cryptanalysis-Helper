import java.util.*;
import java.util.stream.Collectors;

//TODO possibly make application to handle all Unicode characters instead of just ASCII characters;
// a possibly helpful SO post: https://stackoverflow.com/questions/5585919/creating-unicode-character-from-its-number
public class PolyAlphStreamCipherAPI {

    static String handleDecryptionLineFeed(String decryptedText){
        String processed;
        int platformLineFeedIndex;
        if (decryptedText.contains("\r\n")) {
            platformLineFeedIndex = decryptedText.lastIndexOf("\r\n");
            processed = decryptedText.substring(0,platformLineFeedIndex);
        }
        else if (decryptedText.contains("\r_")) {
            platformLineFeedIndex = decryptedText.lastIndexOf("\r_");
            processed = decryptedText.substring(0,platformLineFeedIndex);
        }
        else if (decryptedText.contains("_\n")) {
            platformLineFeedIndex = decryptedText.lastIndexOf("_\n");
            processed = decryptedText.substring(0,platformLineFeedIndex);
        }
        else if (decryptedText.contains("\r")) {
            processed = decryptedText.replaceAll("\r","");
        }
        else
            processed = decryptedText;

        return processed;
    }

    static void applyPolyAlphStream(List<String> lines){
        Scanner scanner = new Scanner(System.in);
        ArrayList<String> plaintext = new ArrayList<>();
        List<String> platformSpecificMsg;
        //if only 1 lines, then it means that there is no line feed in text, which means there is no need to generate
        //platform-specific Strings (so much for a cross-platform language...)
        if(lines.size() == 1) {
            platformSpecificMsg = new ArrayList<>();
            platformSpecificMsg.add(lines.get(0));
        }
        else
            platformSpecificMsg = Arrays.asList(
                String.join(Constants.UNIX_NEWLINE_SEPARATOR, lines),
                String.join(Constants.WIN_NEWLINE_SEPARATOR, lines),
                String.join(Constants.MAC_NEWLINE_SEPARATOR, lines)
                );

        //Use unix style '\n' as default string
        char firstCipherChar = platformSpecificMsg.get(0).charAt(0);
        //print the capital letters and their decimal value (all hints are decimal)
        for(int i = Constants.MIN_UPPER_LETTERS_ROW_VALUE; i<Constants.MAX_UPPER_LETTERS_ROW_VALUE; i+=Constants.SYMBOLS_PER_ROW){
            for(int j=0; j<16; j++){
                int value = i+j;
                char symbol = (char)value;
                String str = symbol+"="+value+"\t";
                System.out.print(str);
            }
            System.out.println();
        }
        //char hint = 'L'; //decimal=76
        System.out.print("Please enter the decimal value of the hint (see above table): ");
        int hint = scanner.nextInt(); //get hint from user in order to get key
        byte key1 = (byte)(firstCipherChar ^ hint);

        //Present choices to user
        List<String> modes = new ArrayList<>(Arrays.asList("Apply key-1 only or manually enter key-2",
                "Try all possible keys and print the results", "Automatically detect key-2"));
        int modeSelection = UserInterface.multiChoice("Please choose one of the modes below",
                modes, "Please enter the number of your choice.", new Scanner(System.in));

        switch (modeSelection){
            case 1:
                Integer key2 = null;
                String prompt = "Enter key-2?\n" +
                        "[Select \"Y/y\" if you want to use key-1 on all characters, or if you want to provide key-2 " +
                        "to use alternately with key-1]\n(Y/N): ";
                boolean manuallyEnterKey = UserInterface.getInputYesNo(new Scanner(System.in), prompt);
                if(manuallyEnterKey){
                    System.out.println("Please enter the decimal value of the key\n" +
                            "[If you want to use only one key, then re-enter key-1 here]\n(Key): ");
                    key2 = scanner.nextInt();//89
                    System.out.println();
                }
                int msgCount = platformSpecificMsg.size();
                if(msgCount != 1)
                    System.out.println("There are "+msgCount+" different platform-specific strings.");

                for(int msgIndex=0; msgIndex<msgCount; msgIndex++) {
                    String res = use2KeysOrLeaveK2Blank(platformSpecificMsg.get(msgIndex), key1, key2);

                    if(msgCount != 1)
                        System.out.println("The result of String #" + (msgIndex + 1) + " (out of " + msgCount + "), is: ");

                    System.out.println(res);
                    //If not final loop, ask user if they want to try next string, or if this is the correct one.
                    if (msgIndex != msgCount - 1) {
                        if (!UserInterface.getInputYesNo(new Scanner(System.in), "Try next String? (Y/N): ")) {
                            plaintext.add(res);
                            break;
                        }
                    }
                    plaintext.add(res);
                }
                break;
            case 2:
                bruteForceKey2(platformSpecificMsg, plaintext, key1);
                break;
            case 3:
                //Dictionary needs to be more complete
                autoFindKey2(platformSpecificMsg, plaintext, key1);
                break;
        }

        System.out.println("\nPRINTING RESULTS:");
        UserInterface.printListElements(plaintext);
        UserInterface.askToWriteResultToFile(plaintext);
    }

    static String use2KeysOrLeaveK2Blank(String message, byte key1, Integer key2){
        //This is the problem with platform specific chars. Also, a java byte primitive
        // can only hold numbers from -128 to 127
        byte[] cipherBytes = message.getBytes();
        char[] charArr = new char[message.length()];
        for (int i = 0; i < message.length(); i++) {
            if (i % 2 == 0) {
                charArr[i] = (char)(cipherBytes[i] ^ key1);
            }
            else{
                if(key2 != null)
                    charArr[i] = (char)(cipherBytes[i] ^ key2);
                else
                    charArr[i] = '_';
            }
        }
        String res = handleDecryptionLineFeed(new String(charArr));
        return "Output (Key 1="+key1+", Key 2="+key2+")\n"+Constants.LINE+"\n"+
                res+"\n"+Constants.LINE+"\n\n";

    }

    static void bruteForceKey2(List<String> platformSpecificMsg, List<String> plaintext, byte key1){
        int msgCount = platformSpecificMsg.size();
        System.out.println("There are "+msgCount+" platform specific strings.");
        for(int key2 = 1; key2<Constants.MAX_SYMBOL_VALUE; key2++){
            StringBuilder sb = new StringBuilder(
                    "Output (Key 1=" + key1 + ", Key 2=" + key2 + ")\n" + Constants.LINE + "\n"
            );
            for (String message : platformSpecificMsg) {
                byte[] cipherBytes = message.getBytes();
                char[] charArr = new char[message.length()];
                for (int i = 0; i < message.length(); i++) {
                    if (i % 2 == 0) {
                        charArr[i] = (char) (cipherBytes[i] ^ key1);
                    } else {
                        charArr[i] = (char) (cipherBytes[i] ^ key2);
                    }
                }

                if (message.contains(Constants.WIN_NEWLINE_SEPARATOR))
                    sb.append("CRLF Encoded: ");
                else if (message.contains(Constants.MAC_NEWLINE_SEPARATOR))
                    sb.append("CR Encoded: ");
                else if (message.contains(Constants.UNIX_NEWLINE_SEPARATOR))
                    sb.append("LF Encoded: ");

                String res = handleDecryptionLineFeed(new String(charArr));
                sb.append(res).append("\n");
            }
            sb.append(Constants.LINE + "\n\n");
            plaintext.add(sb.toString());
            System.out.println(sb);
        }
    }


    static void autoFindKey2(List<String> platformSpecificMsg, List<String> plaintextContainer, byte key1){
        HashSet<String> dictionary = SpellChecking.loadDictionary();
        //Key = Key 2 (value of z in loop) Value = fitness score of message using specified key 2
        LinkedHashMap<Integer, Integer> fitnessScores = new LinkedHashMap<>();
        HashMap<String, Map.Entry<Integer, Integer>>finalistMessages = new HashMap<>();
        HashMap<Integer, String> temporaryContainer = new HashMap<>();//holds key2 as key and decrypted message as value

        int fitnessThreshold=platformSpecificMsg.get(0).split(Constants.SPACE_SEPARATOR).length/4;
        for (String message : platformSpecificMsg) {
            temporaryContainer.clear();
            fitnessScores.clear();
            for (int key2 = 1; key2 < Constants.MAX_SYMBOL_VALUE; key2++) {
                byte[] cipherBytes = message.getBytes();
                char[] charArr = new char[message.length()];
                for (int i = 0; i < message.length(); i++) {
                    if (i % 2 == 0) {
                        charArr[i] = (char) (cipherBytes[i] ^ key1);
                    } else {
                        charArr[i] = (char) (cipherBytes[i] ^ key2);
                    }
                }
                String possiblePlaintext = new String(charArr);
                fitnessScores.put(key2, SpellChecking.getStringFitness(possiblePlaintext, dictionary));
                temporaryContainer.put(key2, possiblePlaintext);
            }
            //sort scores descending. e.g. 10, 9, 8 ...
            fitnessScores = fitnessScores.entrySet().stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue,
                            (oldValue, newValue) -> oldValue, LinkedHashMap::new));
            //get first entry (fittest plaintext)
            Map.Entry<Integer, Integer> fittestPlaintext = fitnessScores.entrySet().iterator().next();
            String plaintext = temporaryContainer.get(fittestPlaintext.getKey());
            finalistMessages.put(plaintext, fittestPlaintext);
        }

        String message = null;
        int key2 = -1;
        int fitness = -1;
        for(Map.Entry<String, Map.Entry<Integer, Integer>> entry : finalistMessages.entrySet()){
            int currentFitness = entry.getValue().getValue();//Get Map.Entry and get its value
            if(currentFitness >= fitness) {
                fitness = currentFitness;
                message = entry.getKey();
                key2 = entry.getValue().getKey();
            }
        }

        //check if threshold reached
        if(fitness >= fitnessThreshold){
            System.out.println("Key="+key2+" found!");
            message = handleDecryptionLineFeed(message);
            String res = "Output (Key 1="+key1+", Key 2="+key2+")\n"+Constants.LINE+"\n"+
                    message+"\n"+Constants.LINE+"\n\n";
            plaintextContainer.add(res);
        }
        else{
            System.out.println("Key was not detected. Some words of the plaintext might not be in English," +
                    "or they might be written in some \"unofficial\" way.");
        }
    }

}
