import java.util.List;

public class Driver {

    public static void main(String[] args){
        int tool = UserInterface.askTool();
        List<String> input = UserInterface.askForInput();

        switch (tool){
            case Constants.SHIFT_CIPHER:
                ShiftAPI.applyShiftCipher(input);
                break;
            case Constants.COLUMN_TRANSPOSITION_CIPHER:
                ColumnTransAPI.applyColumnTrans(input);
                break;
            case Constants.XOR_CIPHER:
                SimpleXorAPI.applySimpleXor(input);
                break;
            case Constants.FREQUENCY_ANALYSIS:
                FreqAnalysisAPI.freqAnalysis(input);
                break;
            default:
                System.out.println("This tool does not exist or has not been implemented yet");
                System.exit(0);
        }
    }
}
