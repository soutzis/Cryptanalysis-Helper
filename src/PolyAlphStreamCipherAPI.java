import java.util.*;
import java.util.stream.Collectors;


public class PolyAlphStreamCipherAPI {

    static void applyPolyAlphStream(List<String> lines){
        Scanner scanner = new Scanner(System.in);
        ArrayList<String> plaintext = new ArrayList<>();
        List<String> platformSpecificMsg = null;
        if(lines.size() == 1) {
            platformSpecificMsg = new ArrayList<>();
            platformSpecificMsg.add(lines.get(0));
        }
        else
            platformSpecificMsg = Arrays.asList(
                String.join(Constants.UNIX_NEWLINE_SEPARATOR, lines),
                //String.join(Constants.WIN_NEWLINE_SEPARATOR, lines), //fixme produces wrong results (unexpected)
                String.join(Constants.MAC_NEWLINE_SEPARATOR, lines)
                );

        //Use unix style '\n' as default string
        char firstCipherChar = platformSpecificMsg.get(0).charAt(0);
        //print the capital letters and their decimal value (all hints are decimal)
        for(int i=64; i<95; i+=16){
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
                String prompt = "Enter key 2? (Y/N): ";
                boolean manuallyEnterKey = UserInterface.getInputYesNo(new Scanner(System.in), prompt);
                if(manuallyEnterKey){
                    System.out.println("Please enter the decimal value of the key: ");
                    key2 = scanner.nextInt();//89
                }
                int msgCount = platformSpecificMsg.size();
                System.out.println("There are "+msgCount+" platform specific strings.");
                for(int msgIndex=0; msgIndex<msgCount; msgIndex++) {
                    String res = useKeysOrLeaveK2Blank(platformSpecificMsg.get(msgIndex), plaintext, key1, key2);
                    System.out.println("The result of String #" + (msgIndex + 1) + " out of " + msgCount + "is: ");
                    System.out.print(res);
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

        UserInterface.printListElements(plaintext);
        UserInterface.askToWriteResultToFile(plaintext);
    }

    static String useKeysOrLeaveK2Blank(String message, List<String> plaintext, byte key1, Integer key2){
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

        return "Output (Key 1="+key1+", Key 2="+key2+")\n"+Constants.LINE+"\n"+
                new String(charArr)+"\n"+Constants.LINE+"\n\n";

    }

    static void bruteForceKey2(List<String> platformSpecificMsg, List<String> plaintext, byte key1){
        int msgCount = platformSpecificMsg.size();
        System.out.println("There are "+msgCount+" platform specific strings.");
        for(int z=1; z<Constants.MAX_ASCII_VALUE; z++){
            StringBuilder sb = new StringBuilder();
            sb.append("Output (Key 1=").append(key1).append(", Key 2=").append(z)
                    .append(")\n").append(Constants.LINE).append("\n");
            for (String message : platformSpecificMsg) {
                byte[] cipherBytes = message.getBytes();
                char[] charArr = new char[message.length()];
                for (int i = 0; i < message.length(); i++) {
                    if (i % 2 == 0) {
                        charArr[i] = (char) (cipherBytes[i] ^ key1);
                    } else {
                        charArr[i] = (char) (cipherBytes[i] ^ z);
                    }
                }
                if (message.contains(Constants.WIN_NEWLINE_SEPARATOR))
                    sb.append("CRLF Encoded: ");
                else if (message.contains(Constants.MAC_NEWLINE_SEPARATOR))
                    sb.append("CR Encoded: ");
                else if (message.contains(Constants.UNIX_NEWLINE_SEPARATOR))
                    sb.append("LF Encoded: ");

                sb.append(new String(charArr)).append("\n");
            }
            sb.append("\n"+Constants.LINE+"\n\n");
            String res = sb.toString();
            plaintext.add(res);
            System.out.println(res);
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
            for (int key2 = 1; key2 < Constants.MAX_ASCII_VALUE; key2++) {
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
