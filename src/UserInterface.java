import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UserInterface {

    static String binaryChoice(String prompt, String errorMessage,
                               String errorPrompt, String choice1, String choice2, Scanner scanner) {
        System.out.print(prompt);
        String input = scanner.nextLine();
        while ((!input.equalsIgnoreCase(choice1)) && (!input.equalsIgnoreCase(choice2))) {
            System.out.println(errorMessage);
            System.out.print(errorPrompt);
            input = scanner.nextLine();
        }

        return input;
    }

    static int multiChoice(String prompt, List<String> choices, String errorMsg, Scanner scanner){
        int numberOfChoices = choices.size();
        int choice = -1;
        for(int i = 1; i<numberOfChoices+1; i++)
            System.out.println(i+". "+choices.get(i-1));

        try {
            while(choice < 1 || choice > numberOfChoices) { //make sure choice is within choices and not some random int
                System.out.println(prompt);
                choice = scanner.nextInt();
            }
        }
        //restart procedure if the wrong type of input is provided.
        catch (InputMismatchException ime){
            System.out.println("\n"+errorMsg+"\n");
            return multiChoice(prompt, choices, errorMsg, new Scanner(System.in));
        }

        return choice;
    }

    static int askTool(){
        String prompt = "Please choose what tool you want to use.";
        String errorMessage = "Please enter a number (e.g. 1) that represents your choice.";
        List<String> choices = Constants.getToolNamesList();

        return multiChoice(prompt, choices, errorMessage, new Scanner(System.in));
    }

    /**
     * If the user enters anything else than Y/y or N/n, it will not be accepted.
     * @return true if user enters Y/y, or false if user enters N/n
     */
    static boolean getInputYesNo(Scanner scanner, String prompt){
        String errorPrompt = "(Y/N): ";
        String errorMessage = "Only acceptable inputs are \"Y\" and \"N\" (case insensitive).";
        String yes="y", no="n", input = binaryChoice(prompt, errorMessage, errorPrompt, yes, no, scanner);

        return input.equalsIgnoreCase(yes);
    }

    static int askMode(){
        String prompt = "What do you want to do? Enter 'e' for encryption or 'd' for decryption:";
        String errorMessage = "Only acceptable inputs are \"E\" and \"D\" (case insensitive).";
        String encrypt="e", decrypt="d", errorPrompt = "(E/D): ", input = binaryChoice(prompt, errorMessage,
                errorPrompt, encrypt, decrypt, new Scanner(System.in));

        return input.equalsIgnoreCase(encrypt) ? Constants.MODE_ENCRYPT : Constants.MODE_DECRYPT;
    }

    static String askForStreamCipherFile(){
        Scanner scanner = new Scanner(System.in);
        System.out.println("To ensure that no bytes of the message are altered, you may only use the appropriate file.");
        String filename;
        boolean isInputCorrect;
        do  {
            System.out.print("Enter filename (full path): ");
            filename = scanner.nextLine();
            String askIfCorrectInputGiven ="Is this correct? \"" + filename + "\" (Y/N): ";
            isInputCorrect = getInputYesNo(scanner,askIfCorrectInputGiven);
        }while(!isInputCorrect);

        String message = null;
        try {
            message = readFileBytes(filename);
        } catch (IOException e) {
            e.printStackTrace();
        }

        //if(message.contains(Constants.WIN_NEWLINE_SEPARATOR))
        return message;
    }

    static List<String> askForInput() {
        Scanner scanner = new Scanner(System.in);
        String prompt = "Use a .txt file or type input through terminal?\n1. Text File\n2. Type Here\n";
        String errorMessage = "Only acceptable inputs are \"1\" and \"2\".";
        String errorPrompt = "1. Text File\n2. Type Here\n(Select): ";
        String txt = "1", typeMsg = "2";

        String input = binaryChoice(prompt, errorMessage, errorPrompt, txt, typeMsg, scanner);
        boolean isInputCorrect = false;
        List<String> finalUserInput;

        if (input.equalsIgnoreCase(txt)) {
            String filename = null;
            System.out.print("Enter filename (full path): ");
            try {
                while (!isInputCorrect) {
                    filename = scanner.nextLine();
                    String askIfCorrectInputGiven ="Is this correct? \"" + filename + "\" (Y/N): ";
                    isInputCorrect = getInputYesNo(scanner,askIfCorrectInputGiven);
                    if(!isInputCorrect)
                        System.out.print("Enter filename (path): ");
                }
                finalUserInput = readFileLines(filename);
            } catch (IOException ioe) {
                System.out.println("There was an error while attempting to read \"" + filename + "\"\nResetting...");
                return askForInput();
            }

        } else {
            finalUserInput = new ArrayList<>();
            System.out.println(
                    "Enter the message.\nPress 'Enter' for new line and type '!end!'(in new line) when you're done:");
            input = scanner.nextLine();
            while (!input.equalsIgnoreCase("!end!")) {
                finalUserInput.add(input);
                input = scanner.nextLine();
            }

            System.out.println("The message you entered is: ");
            printListElements(finalUserInput);

            if (!getInputYesNo(new Scanner(System.in), "Was the message you entered correct? (Y/N): ")) {
                //scanner.close(); //for some reason causes NoSuchElementException to be thrown
                return askForInput();
            }
        }

        return finalUserInput;
    }

    /**
     * This method will simply print the elements in a List of Strings
     * @param list The list containing n' Strings
     */
    static void printListElements(List<String> list){
        for(String str : list)
            System.out.println(str);
    }

    static void printToUser(List<String> input, String contextOfMessage){
        System.out.println(contextOfMessage+":\n"+Constants.LINE);
        printListElements(input);
        System.out.println(Constants.LINE+"\n");
    }

    /**
     * Method will read the text from a file and return a List, where each element represents a line of the text.
     * @param filename The name of the file to read (preceded by the path, if not in same dir)
     * @return A List containing each line of the text.
     */
    static ArrayList<String> readFileLines(String filename) throws IOException{
        ArrayList<String> lines = null;

        //Get lines of text (Strings) as a stream
        try (Stream<String> stream = Files.lines(Paths.get(filename), StandardCharsets.UTF_8)){
            // convert stream to a List-type object
            lines = (ArrayList<String>)stream.collect(Collectors.toList());
        }

        catch (SecurityException se){
            System.out.println("Could not read the file provided. Please check if you have permission to access it.");
            System.exit(1);
        }

        return lines;
    }

    /*FIXME There is a problem when reading files encoded on different platforms than the one running (currently
    *  Windows10. For example, a Windows line feed will be CRLF (\r\n). IF the message was encoded on a Mac, the line
    *  feed will be CR (\r), but when reading on Windows, it will be translated to \r\n and alter the meaning of the
    *  message.*/

    static String readFileBytes(String filename) throws IOException {
        File f = new File(filename);
        FileReader fr = new FileReader(f);
        BufferedReader br = new BufferedReader(fr);
        StringBuilder sb = new StringBuilder();
        int c;

        while((c = br.read()) != -1) {
            byte[] charByte = new byte[1];
            charByte[0] = (byte)c;
            byte LF = '\n';
            byte CR = '\r';
            if(c == CR){
                br.mark(1);
                int readAhead = br.read();
                if(readAhead == LF)
                    sb.append(new String(Constants.WIN_NEWLINE_SEPARATOR.getBytes(), StandardCharsets.UTF_8));
                else {
                    br.reset(); //go back to marked point in input stream.
                    sb.append(new String(Constants.MAC_NEWLINE_SEPARATOR.getBytes(), StandardCharsets.UTF_8));
                }
            }
            else if(c == LF)
                sb.append(new String(Constants.WIN_NEWLINE_SEPARATOR.getBytes(), StandardCharsets.UTF_8));
            else
                sb.append(new String(charByte, StandardCharsets.UTF_8));
        }

        return sb.toString();
    }

    static void askToWriteResultToFile(ArrayList<String> outputAndKey){
        String prompt = "Save the result of cryptanalysis to a file? (Y/N): ";
        if(UserInterface.getInputYesNo(new Scanner(System.in), prompt))
            UserInterface.writeFileText(outputAndKey);
    }

    /**
     * Method will take a list containing lines of text and write them to a specified filename
     * @param linesOfText The text to write to the file.
     */
    static void writeFileText (ArrayList<String> linesOfText){

        //The name of the file to write to.
        String filename = Constants.OUTPUT_FILENAME;
        // Specify file location and name
        Path file = Paths.get(filename);
        // StringBuilder will be used to create ONE string of text
        StringBuilder sb = new StringBuilder();
        // Iterate over the list of strings and append them to string-builder with a 'new line' carriage return.
        for( String str : linesOfText){
            sb.append(str).append("\n");
        }
        // Get all bytes of produced string and instantly write them to the file.
        byte[] bytes = sb.toString().getBytes();
        // Write to file
        try{
            Files.write(file, bytes);
        }
        catch(IOException ioe){
            System.out.println("\nCould not write to file \""+filename+"\".\nReason: "+ioe.getMessage());
            System.exit(1);
        }

    }
}
