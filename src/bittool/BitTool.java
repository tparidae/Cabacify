package bittool;

public class BitTool {

    public static int bitsNeededToRepresent(int maxValue) {
        return 32 - Integer.numberOfLeadingZeros(maxValue);
    }

    public static int bitLengthToByteLength(int numberOfBits) {
        return (numberOfBits + 7) / 8;
    }

    public static int bitsNeededToRepresentByteBlockLength(int numAlphabets,
            int numPredictors, int blockSize, int largestAlphabetSize) {
        int result = 0;

        result += 1;

        result += bitsNeededToRepresent(blockSize - 1);

        result += bitsNeededToRepresent(numAlphabets - 1);

        result += bitsNeededToRepresent(numPredictors - 1);

        result += blockSize * bitsNeededToRepresent(largestAlphabetSize - 1);

        result += 1;

        int t = BitTool.bitsNeededToRepresent(
                BitTool.bitLengthToByteLength(result));
        result = BitTool.bitsNeededToRepresent(
                BitTool.bitLengthToByteLength(result + t));

        return result;
    }

}
