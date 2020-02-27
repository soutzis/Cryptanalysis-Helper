import java.util.*;

public class CaesarAPI {
    private static final List<String> alphabetCharList = Constants.getAlphabetCharList();
    private static final Map<String, Integer> inverseCharList = Constants.getInverseCharList();

    static void caesar(List<String> input){
        final String useAutoKey = "Would you like to automatically try all possible keys? (Y/N): ";
        final String askToWriteToFile = "Would you like to write the output to a file? (Y/N): ";
        int mode = UserInterface.askMode();

        //Pretty-print the input onto the screen
        UserInterface.printToUser(input, "Input");
        ArrayList<String> output;

        //Ask user to provide key manually or use automatic feature that tries all possible keys.
        if(UserInterface.getInputYesNo(new Scanner(System.in),useAutoKey)){
            output = applyCaesar(input, mode);
        }
        else{
            int key = askCaesarKey(new Scanner(System.in));
            output = applyCaesar(input, mode, key);
        }
        if(UserInterface.getInputYesNo(new Scanner(System.in), askToWriteToFile))
            UserInterface.writeFileText(output);
    }

    static ArrayList<String> applyCaesar(List<String> input, int mode, int key){
        ArrayList<String> output = new ArrayList<>();
        for(String str : input){
            StringBuilder sb = new StringBuilder();
            String[] characters = str.split(Constants.EMPTY_STRING);
            for(String character : characters) {
                character = character.toLowerCase();
                if (!alphabetCharList.contains(character))
                    sb.append(character);
                else if (mode == Constants.MODE_ENCRYPT)
                    sb.append(alphabetCharList.get(inverseCharList.get(character) + key));
                else if (mode == Constants.MODE_DECRYPT)
                    sb.append(alphabetCharList.get(inverseCharList.get(character) - key));
                else
                    throw new RuntimeException("Something went wrong while applying Caesar cipher.");
            }
            output.add(sb.toString());
        }
        UserInterface.printToUser(output, "Output");
        return output;
    }

    static ArrayList<String> applyCaesar(List<String> input, int mode){
        ArrayList<String> output;
        ArrayList<String> result = new ArrayList<>();

        for(int key=1; key<Constants.ALPHABET_LENGTH; key++){
            output = new ArrayList<>();
            StringBuilder sb = null;
            for(String str : input){
                sb = new StringBuilder();
                String[] characters = str.split(Constants.EMPTY_STRING);
                for(String character : characters) {
                    character = character.toLowerCase();
                    if (!alphabetCharList.contains(character))
                        sb.append(character);
                    else if (mode == Constants.MODE_ENCRYPT)
                        sb.append(alphabetCharList.get(inverseCharList.get(character) + key));
                    else if (mode == Constants.MODE_DECRYPT)
                        sb.append(alphabetCharList.get(inverseCharList.get(character) - key));
                    else
                        throw new RuntimeException("Something went wrong while applying Caesar cipher.");
                }
                output.add(sb.toString());//this will be printed to screen
            }
            result.add("Output (Key="+key+")\n"+Constants.LINE+"\n"+sb.toString()+"\n"+Constants.LINE+"\n\n");
            UserInterface.printToUser(output, "Output (Key="+key+")");
        }
        return result;
    }

    /**
     * Method will take a list of Strings and will return an array containing a set of all the characters that appear.
     * @param text Is a list that contains the set of Strings (lines of text)
     * @return A String array, where each element is a character that exists in 'text'. Each character appears once.
     */
    private static String[] getDistinctCharacters(ArrayList<String> text){

        return text.stream() //convert the list to a stream
                .map(c -> c.split(Constants.EMPTY_STRING))//convert each stream to a String array,each string 1 char
                .flatMap(Arrays::stream) //flatten streams into one array
                .distinct() //get only distinct characters, discard any duplicates
                .toArray(String[]::new); //convert stream to <String[]>
    }

    static int askCaesarKey(Scanner scanner){
        System.out.print("Please enter the key (number of shifts in alphabet):");
        int input;
        try{
            input = scanner.nextInt();
            while(input < 1 || input > 26){
                System.out.println("The key can only take values between 1 and 26.\nNote that if key=26, then PT=CT");
                System.out.print("Key: ");
                input = scanner.nextInt();
            }
        }
        catch (InputMismatchException ime){
            System.out.println("Do you even know what an integer is ?");
            return askCaesarKey(new Scanner(System.in));
        }

        return input;
    }
}
