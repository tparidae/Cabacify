package encoding;

import arithmeticcoding.Binarisation;
import arithmeticcoding.ContextModel;
import arithmeticcoding.ContextSelector;
import arithmeticcoding.ContextTables;
import arithmeticcoding.EncBinCABAC;
import bittool.BitOutputStream;
import java.io.BufferedOutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.HashMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;

public final class Writer {

    BitOutputStream _out;
    EncBinCABAC _outCABAC;
    BitOutputStream out = null;
    ContextSelector contextSelector;
    private ContextModel[] _contextModels;
    private String _suffix;
    private String _prefix;
    int cm, bin;
    int i = 0;
    Binarisation bins;
    int suffix_size;
    int suffix_size_minus_1;
    int bitlength;
    int temp;
    int shift;

    public Writer(String prefix, String suffix, long length) {
        contextSelector = new ContextSelector();
        _suffix = suffix;
        _prefix = prefix;

        try {
            out = new BitOutputStream(new BufferedOutputStream(Files.newOutputStream(FileSystems.getDefault().getPath(prefix + suffix + ".coded"))));
        } catch (Exception e) {
            System.err.println("Unable to initialise bitoutputstreams in writer: " + e.getMessage());
//            System.exit(-1);
        }

        _out = out;
        _outCABAC = new EncBinCABAC(_out);
        writeFileSize(length);

    }

    public void reset() {
        contextSelector = new ContextSelector();
        try {
            out.close();
            out = new BitOutputStream(new BufferedOutputStream(Files.newOutputStream(FileSystems.getDefault().getPath(_prefix + _suffix + ".coded"))));
        } catch (Exception e) {
            System.err.println("Unable to initialise bitoutputstreams in writer: " + e.getMessage());
//            System.exit(-1);
        }
        _out = out;
        _outCABAC = new EncBinCABAC(_out);
    }

    public void writePairDescriptor(int input, int treshold, int prev) {
        //gives small gain
//        byte[] prev0 = new byte[]{0,1,2,3,4,5,6,7};
//        byte[] prev1 = new byte[]{0,1,3,2,5,6,4,7};
//        byte[] prev2 = new byte[]{0,2,1,3,5,6,4,7};
//        byte[] prev3 = new byte[]{0,2,3,1,6,4,5,7};
//        byte[] prev4 = new byte[]{0,3,6,4,1,2,5,7};
//        byte[] prev5 = new byte[]{0,3,6,4,1,2,5,7};
//        byte[] prev6 = new byte[]{0,3,4,2,5,6,1,7};
//        
//        if (prev==0){
//            writeAsTruncCabac(prev0[input],treshold,prev0[prev]);
//        }else if (prev==1){
//            writeAsTruncCabac(prev1[input],treshold,prev1[prev]);
//        }else if (prev==2){
//            writeAsTruncCabac(prev2[input],treshold,prev2[prev]);
//        }else if (prev==3){
//            writeAsTruncCabac(prev3[input],treshold,prev3[prev]);
//        }else if (prev==4){
//            writeAsTruncCabac(prev4[input],treshold,prev4[prev]);
//        }else if (prev==5){
//            writeAsTruncCabac(prev5[input],treshold,prev5[prev]);
//        }else if (prev==6){
//            writeAsTruncCabac(prev6[input],treshold,prev6[prev]);
//        }
//        
        writeAsTruncCabac(input, treshold, prev);
    }

    private void encodeRefID(int input) {
        bins = new Binarisation(input, 8);
        bitlength = bins.getNumbins();
        for (int i = 0; i < bitlength; i++) {
            bin = bins.getBins() >>> (bins.getNumbins() - i - 1);
            cm = contextSelector.getContextForBinary(0, i);
            _outCABAC.encodeBin((byte) (bin & 0x1), _contextModels[cm]);
        }
    }

    public void writeAsBinary(int input, int length) {
        _outCABAC.encodeBinsEP(input, length);
    }

    public void writeAsBinary(long input, int length) {
        _outCABAC.encodeBinsEP(input, length);
    }

    public void writeAsBinaryCabac(int input, int length, int offset) {
//        Binarisation bins = new Binarisation(input, length);
        i = 0;
        cm = contextSelector.getContextForBinary(offset, i);
        for (i = 0; i < length; i++) {
            bin = input >>> (length - i - 1);
//            _contextModels[cm].setLPSMode(mode);
            _outCABAC.encodeBin((byte) (bin & 0x1), _contextModels[cm++]);
        }

//        for (; i < length; i++) {
//            bin = bins.getBins() >>> (bins.getNumbins() - i - 1);
//            _outCABAC.encodeBinEP((byte) (bin & 0x1));
//        }
    }

    public void writeAsBinaryCabacOld(int input, int length, int offset, int mode) {
        bins = new Binarisation(input, length);
        for (i = 0; i < length; i++) {
            bin = bins.getBins() >>> (bins.getNumbins() - i - 1);
            cm = contextSelector.getContextForBinary(offset, i);
            _contextModels[cm].setLPSMode(mode);
            _outCABAC.encodeBin((byte) (bin & 0x1), _contextModels[cm]);
        }
    }

    public void writeAsTrunc(int input, int treshold) {
        bins = Binarisation.binariseTruncatedUnary(input, treshold);
        _outCABAC.encodeBinsEP(bins.getBins(), bins.getNumbins());
    }

//    public void writeAsTruncCabac(int input, int treshold, int offset) {
////        System.out.println(input+" "+treshold+" "+offset);
//        writeAsTruncCabac(input, treshold, offset, 0);
//    }
    public void writeAsTruncCabac(int input, int treshold, int offset) {
        cm = contextSelector.getContextForTrunc(offset, 0);
        int i = 0;
        for (; i < input; i++) {
            _outCABAC.encodeBin((byte) (1), _contextModels[cm++]);
        }
        if (input != treshold) {
            _outCABAC.encodeBin((byte) (0), _contextModels[cm]);
        }
    }

    public void writeAsTruncCabacOld(int input, int treshold, int offset, int mode) {
        bins = Binarisation.binariseTruncatedUnary(input, treshold);
        cm = contextSelector.getContextForTrunc(offset, 0);
        int length = bins.getNumbins();
        for (int i = 0; i < length; i++) {
            bin = bins.getBins() >>> (bins.getNumbins() - i - 1);
//            cm = contextSelector.getContextForTrunc(offset, i);
//            _contextModels[cm].setLPSMode(mode);
            _outCABAC.encodeBin((byte) (bin & 0x1), _contextModels[cm++]);
        }
    }

    public void writeAsExpGol(int input) {
        bins = Binarisation.binariseExpGolomb(input & 0xffffffff);
        _outCABAC.encodeBinsEP(bins.getBins(), bins.getNumbins());
    }

    public void writeAsExpGolCabacAllbits(int input, int offset, int mode) {
        bins = Binarisation.binariseExpGolomb(input & 0xffffffff);
        bitlength = bins.getNumbins();
        for (int i = 0; i < bitlength; i++) {
            bin = bins.getBins() >>> (bins.getNumbins() - i - 1);
            cm = contextSelector.getContextForExpGol(offset, i);
            _contextModels[cm].setLPSMode(mode);
            _outCABAC.encodeBin((byte) (bin & 0x1), _contextModels[cm]);
        }
    }

    public void writeAsExpGolCabac(int input, int offset) {
        bins = Binarisation.binariseExpGolomb(input & 0xffffffff);
//        System.out.println(input);
        i = 0;
        //algemene speedup: in for loops maar 1 keer bins.getNumbins gebruiken en dat eerst in variabele zetten;
        cm = contextSelector.getContextForExpGol(offset, i);
        bitlength = bins.getNumbins();
        suffix_size_minus_1 = (int) Math.ceil((bitlength >> 1) + 1) - 1;
        for (; i < suffix_size_minus_1; i++) {
            _outCABAC.encodeBin((byte) (0), _contextModels[cm++]);
        }
        if (i < bitlength) {
            _outCABAC.encodeBin((byte) (1), _contextModels[cm++]);
            i++;
        }
        //System.out.println("enc i: "+i);
        temp = bins.getBins();
        bitlength -= i;
        shift = 32 - bitlength;
        if (bitlength > 0) {
            temp = (temp << shift) >>> shift;
//               temp = temp;
            _outCABAC.encodeBinsEP(temp, bitlength);
        }
    }

    public void writeAsExpGolCabacOld(int input, int offset) {
        bins = Binarisation.binariseExpGolomb(input & 0xffffffff);
        i = 0;
        //algemene speedup: in for loops maar 1 keer bins.getNumbins gebruiken en dat eerst in variabele zetten;
        cm = contextSelector.getContextForExpGol(offset, i);
        bitlength = bins.getNumbins();
        suffix_size = (int) Math.ceil((bitlength >> 1) + 1);
        for (; i < suffix_size - 1; i++) {
            _outCABAC.encodeBin((byte) (0), _contextModels[cm++]);
        }
        if (i < bitlength) {
            _outCABAC.encodeBin((byte) (1), _contextModels[cm++]);
            i++;
        }
        //System.out.println("enc i: "+i);
        temp = bins.getBins();
        for (; i < bitlength; i++) {
            bin = temp >>> (bitlength - i - 1);
            _outCABAC.encodeBinEP((byte) (bin & 0x1));
        }
    }

    public void writeAsExpGolCabacSpeedup(int input, int offset) {
        bins = Binarisation.binariseExpGolomb(input & 0xffffffff);
        i = 0;
        //algemene speedup: in for loops maar 1 keer bins.getNumbins gebruiken en dat eerst in variabele zetten;
        cm = contextSelector.getContextForExpGol(offset, i);
        bitlength = bins.getNumbins();
        suffix_size = (int) Math.ceil((bitlength >> 1) + 1);
        for (; i < suffix_size - 1; i++) {
            _outCABAC.encodeBin((byte) (0), _contextModels[cm++]);
        }
        if (i < bitlength) {
            _outCABAC.encodeBin((byte) (1), _contextModels[cm++]);
            i++;
        }
        //System.out.println("enc i: "+i);
        for (; i < bitlength; i++) {
            bin = bins.getBins() >>> (bins.getNumbins() - i - 1);
            _outCABAC.encodeBinEP((byte) (bin & 0x1));
        }
    }

    public void writeAsFlag(int input) {
        _outCABAC.encodeBinsEP(input & 0x01, 1);
    }

    public void writeAsFlagCabac(int input, int offset) {
        cm = contextSelector.getContextForFlag(offset);
//        _contextModels[cm].setLPSMode(mode);
        _outCABAC.encodeBin((byte) (input & 0x1), _contextModels[cm]);

    }

    public void writeAsSignedExpGol(int input) {
        bins = Binarisation.binariseSignedExpGolomb(input);
        _outCABAC.encodeBinsEP(bins.getBins(), bins.getNumbins());
    }

    public void writeAsSignedExpGolCabac(int input, int offset) {
        bins = Binarisation.binariseSignedExpGolomb(input);
        i = 0;
        //algemene speedup: in for loops maar 1 keer bins.getNumbins gebruiken en dat eerst in variabele zetten;
        cm = contextSelector.getContextForExpGol(offset, i);
        suffix_size = (int) Math.ceil((bins.getNumbins() >> 1) + 1);
        for (; i < suffix_size; i++) {
            bin = bins.getBins() >>> (bins.getNumbins() - i - 1);
            _outCABAC.encodeBin((byte) (bin & 0x1), _contextModels[cm++]);
        }
        //System.out.println("enc i: "+i);
        bitlength = bins.getNumbins();
        for (; i < bitlength; i++) {
            bin = bins.getBins() >>> (bins.getNumbins() - i - 1);
            _outCABAC.encodeBinEP((byte) (bin & 0x1));
        }
    }

    public void writeAsTruncExpGol(int input, int treshold) {
        if ((input & 0xffff) <= treshold) {
            writeAsTrunc((input & 0xffff), treshold);
        } else {
            writeAsTrunc(treshold, treshold);
        }
        if ((input & 0xffff) >= treshold) {
            writeAsExpGol((input & 0xffff) - treshold);
        }

    }

    public void writeAsTruncExpGolCabac(int input, int treshold, int offset) {
        if ((input & 0xffff) <= treshold) {
            writeAsTruncCabac((input & 0xffff), treshold, offset);
        } else {
            writeAsTruncCabac(treshold, treshold, offset);
        }
        if ((input & 0xffff) >= treshold) {
            writeAsExpGolCabac((input & 0xffff) - treshold, offset);
        }

    }

    public void writeAsTruncRice(int input, int treshold, int trailingBits) {
        if (input > treshold + Math.pow(2, trailingBits)) {
            throw new RuntimeException("value too high for treshold-trailingBits combo of TruncRiceCabac");
        }
        writeAsTrunc(input >> trailingBits, treshold >> trailingBits);
        writeAsBinary((int) ((input & 0xffff) % Math.pow(2, trailingBits)), trailingBits);
    }

    public void writeAsTruncRiceCabac(int input, int cMax, int trailingBits, int offset, int mode) {
        if (input > cMax) {
            throw new RuntimeException("value higher than cMax");
        }
        if (input >> trailingBits > 31) {
            throw new RuntimeException("value higher than 31");
        }
        writeAsTruncCabac(input >> trailingBits, cMax >> trailingBits, offset);
        writeAsBinary((int) ((input & 0xffff) % Math.pow(2, trailingBits)), trailingBits);
    }

    public void writeAsTruncRiceCabac(int input, int cMax, int trailingBits, int offset) {
        writeAsTruncRiceCabac(input, cMax, trailingBits, offset, 0);
    }

    public void writeAsSignedTruncExpGol(int input, int treshold) {
        if (Math.abs(input) <= treshold) {
            writeAsTrunc(Math.abs(input), treshold);
        } else {
            writeAsTrunc(treshold, treshold);
        }
        if (Math.abs(input) >= treshold) {
            writeAsExpGol(Math.abs(input) - treshold);
        }
        if (input != 0) {
            if (input < 0) {
                writeAsFlag(1);
            } else {
                writeAsFlag(0);
            }

        }
    }

    public void writeAsSignedTruncExpGolCabac(int input, int treshold, int offset) {
        if (Math.abs(input) <= treshold) {
            writeAsTruncCabac(Math.abs(input), treshold, offset);
        } else {
            writeAsTruncCabac(treshold, treshold, offset);
        }
        if (Math.abs(input) >= treshold) {
            writeAsExpGolCabac(Math.abs(input) - treshold, offset);
        }
        if (input != 0) {
            if (input < 0) {
                writeAsFlagCabac(1, offset);
            } else {
                writeAsFlagCabac(0, offset);
            }

        }
    }

    public void writeAsRLE(ArrayList<Byte> Pos0) {
        byte[] lijst = ListToArrayByte(Pos0);
        byte prev = lijst[0];
        int counter = 1;

        writeAsBinary(lijst.length, 32);
        prev = lijst[0];

        for (int i = 1; i <= lijst.length; i++) {
            while (i < lijst.length && prev == lijst[i]) {
                counter++;
                i++;
            }
            writeAsExpGol(prev & 0xff);
            writeAsExpGol(counter - 1);
            if (i < lijst.length) {
                prev = lijst[i];
            }
            counter = 1;
        }

    }

    public void writeAsRLECabac(ArrayList<Byte> Pos0) {
        byte[] lijst = ListToArrayByte(Pos0);
        byte prev = lijst[0];
        int counter = 1;

        writeAsBinary(lijst.length, 32);
        prev = lijst[0];

        for (int i = 1; i <= lijst.length; i++) {
            while (i < lijst.length && prev == lijst[i]) {
                counter++;
                i++;
            }
            writeAsExpGolCabac(prev & 0xff, 0);
            writeAsExpGolCabac(counter - 1, 1);
            if (i < lijst.length) {
                prev = lijst[i];
            }
            counter = 1;
        }

    }

    public void writeAsRLECabac(byte[] lijst) {
//        byte[] lijst = ListToArrayByte(Pos0);
        byte prev = lijst[0];
        int counter = 1;

        writeAsBinary(lijst.length, 32);
        prev = lijst[0];

        for (int i = 1; i <= lijst.length; i++) {
            while (i < lijst.length && prev == lijst[i]) {
                counter++;
                i++;
            }
            writeAsExpGolCabac(prev & 0xff, 0);
            writeAsTruncCabac(counter - 1, 10, 1);
            if (i < lijst.length) {
                prev = lijst[i];
            }
            counter = 1;
        }

    }

    public void buildContextModels() {
        _contextModels = ContextTables.buildContextTable();
    }

    public void printContextModels() {
        boolean code_ready = false;
        boolean valid_for_printing = false;
        String temp = "";

        if (_contextModels != null) {
            int i = 0;
            if (code_ready) {
                System.out.println("\npublic static final int OFFSET_TRUNCATED_UNARY_1 = 0;     \n    private static final int[] INIT_TRUNCATED_UNARY_1_CTX = {");
            } else {
                System.out.print("Truncated 1: ");
            }
            for (int j = 0; j < 32 && i < _contextModels.length; i++, j++) {
                temp += (_contextModels[i] + ",");
            }
            System.out.println(temp);

            temp = "";

            if (code_ready) {
                System.out.println("};\n    public static final int OFFSET_TRUNCATED_UNARY_2 = OFFSET_TRUNCATED_UNARY_1 + INIT_TRUNCATED_UNARY_1_CTX.length;\n    private static final int[] INIT_TRUNCATED_UNARY_2_CTX = {");
            } else {
                System.out.print("Truncated 2: ");
            }
            for (int j = 0; j < 32 && i < _contextModels.length; i++, j++) {
                temp += (_contextModels[i] + ",");

            }
            System.out.println(temp);

            temp = "";
            valid_for_printing = false;
            if (code_ready) {
                System.out.println("};\n    public static final int OFFSET_TRUNCATED_UNARY_3 = OFFSET_TRUNCATED_UNARY_2 + INIT_TRUNCATED_UNARY_2_CTX.length;\n    private static final int[] INIT_TRUNCATED_UNARY_3_CTX = {");
            } else {
                System.out.print("Truncated 3: ");
            }
            for (int j = 0; j < 32 && i < _contextModels.length; i++, j++) {
                temp += (_contextModels[i] + ",");

            }
            System.out.println(temp);
            temp = "";
            if (code_ready) {
                System.out.println("};\n    public static final int OFFSET_TRUNCATED_UNARY_4 = OFFSET_TRUNCATED_UNARY_3 + INIT_TRUNCATED_UNARY_3_CTX.length;\n    private static final int[] INIT_TRUNCATED_UNARY_4_CTX = {");
            } else {
                System.out.print("Truncated 4: ");
            }
            for (int j = 0; j < 32 && i < _contextModels.length; i++, j++) {
                temp += (_contextModels[i] + ",");
            }
            System.out.println(temp);

            temp = "";
            if (code_ready) {
                System.out.println("};\n    public static final int OFFSET_TRUNCATED_UNARY_5 = OFFSET_TRUNCATED_UNARY_4 + INIT_TRUNCATED_UNARY_4_CTX.length;\n    private static final int[] INIT_TRUNCATED_UNARY_5_CTX = {");
            } else {
                System.out.print("Truncated 5: ");
            }
            for (int j = 0; j < 32 && i < _contextModels.length; i++, j++) {
                temp += (_contextModels[i] + ",");
            }
            System.out.println(temp);
            temp = "";
            if (code_ready) {
                System.out.println("};\n    public static final int OFFSET_TRUNCATED_UNARY_6 = OFFSET_TRUNCATED_UNARY_5 + INIT_TRUNCATED_UNARY_5_CTX.length;\n    private static final int[] INIT_TRUNCATED_UNARY_6_CTX = {");
            } else {
                System.out.print("Truncated 6: ");
            }
            for (int j = 0; j < 32 && i < _contextModels.length; i++, j++) {
                temp += (_contextModels[i] + ",");
            }
            System.out.println(temp);
            temp = "";
            if (code_ready) {
                System.out.println("};\n    public static final int OFFSET_TRUNCATED_UNARY_7 = OFFSET_TRUNCATED_UNARY_6 + INIT_TRUNCATED_UNARY_6_CTX.length;\n    private static final int[] INIT_TRUNCATED_UNARY_7_CTX = {");
            } else {
                System.out.print("Truncated 7: ");
            }
            for (int j = 0; j < 32 && i < _contextModels.length; i++, j++) {
                temp += (_contextModels[i] + ",");
            }
            System.out.println(temp);
            temp = "";
            if (code_ready) {
                System.out.println("};\n    public static final int OFFSET_TRUNCATED_UNARY_8 = OFFSET_TRUNCATED_UNARY_7 + INIT_TRUNCATED_UNARY_7_CTX.length;\n    private static final int[] INIT_TRUNCATED_UNARY_8_CTX = {");
            } else {
                System.out.print("Truncated 8: ");
            }
            for (int j = 0; j < 32 && i < _contextModels.length; i++, j++) {
                temp += (_contextModels[i] + ",");
            }
            System.out.println(temp);
            temp = "";
            if (code_ready) {
                System.out.println("};\n\n    public static final int OFFSET_EXPGOL_1\n            = OFFSET_TRUNCATED_UNARY_5 + INIT_TRUNCATED_UNARY_5_CTX.length;\n    private static final int[] INIT_EXPGOL_1_CTX = {");
            } else {
                System.out.print("ExpGol 1: ");
            }
            for (int j = 0; j < 32 && i < _contextModels.length; i++, j++) {
                temp += (_contextModels[i] + ",");
            }
            System.out.println(temp);

            temp = "";
            if (code_ready) {
                System.out.println("};\n    public static final int OFFSET_EXPGOL_2\n            = OFFSET_EXPGOL_1 + INIT_EXPGOL_1_CTX.length;\n    private static final int[] INIT_EXPGOL_2_CTX = {");
            } else {
                System.out.print("ExpGol 2: ");
            }
            for (int j = 0; j < 32 && i < _contextModels.length; i++, j++) {
                temp += (_contextModels[i] + ",");
            }
            System.out.println(temp);

            temp = "";
            if (code_ready) {
                System.out.println("};\n    public static final int OFFSET_EXPGOL_3\n            = OFFSET_EXPGOL_2 + INIT_EXPGOL_2_CTX.length;    private static final int[] INIT_EXPGOL_3_CTX = {");
            } else {
                System.out.print("ExpGol 3: ");
            }
            for (int j = 0; j < 32 && i < _contextModels.length; i++, j++) {
                temp += (_contextModels[i] + ",");
            }
            System.out.println(temp);

            temp = "";
            if (code_ready) {
                System.out.println("};\n    public static final int OFFSET_EXPGOL_4\n            = OFFSET_EXPGOL_3 + INIT_EXPGOL_3_CTX.length;\nprivate static final int[] INIT_EXPGOL_4_CTX = {");
            } else {
                System.out.print("ExpGol 4: ");
            }
            for (int j = 0; j < 32 && i < _contextModels.length; i++, j++) {
                temp += (_contextModels[i] + ",");
            }
            System.out.println(temp);

            temp = "";
            if (code_ready) {
                System.out.println("};\n    public static final int OFFSET_EXPGOL_5\n            = OFFSET_EXPGOL_4 + INIT_EXPGOL_4_CTX.length;\n    private static final int[] INIT_EXPGOL_5_CTX = {");
            } else {
                System.out.print("ExpGol 5: ");
            }
            for (int j = 0; j < 32 && i < _contextModels.length; i++, j++) {
                temp += (_contextModels[i] + ",");
            }
            System.out.println(temp);
            temp = "";
            if (code_ready) {
                System.out.println("};\n\n    public static final int OFFSET_BINARY_1\n            = OFFSET_EXPGOL_5 + INIT_EXPGOL_5_CTX.length;\n    private static final int[] INIT_BINARY_1_CTX = {");
            } else {
                System.out.print("Binary 1: ");
            }
            for (int j = 0; j < 32 && i < _contextModels.length; i++, j++) {
                temp += (_contextModels[i] + ",");
            }
            System.out.println(temp);
            temp = "";
            if (code_ready) {
                System.out.println("};\n    public static final int OFFSET_BINARY_2\n            = OFFSET_BINARY_1 + INIT_BINARY_1_CTX.length;\n    private static final int[] INIT_BINARY_2_CTX = {");
            } else {
                System.out.print("Binary 2: ");
            }
            for (int j = 0; j < 32 && i < _contextModels.length; i++, j++) {
                temp += (_contextModels[i] + ",");
            }
            System.out.println(temp);

            temp = "";
            if (code_ready) {
                System.out.println("};\n    public static final int OFFSET_BINARY_3\n            = OFFSET_BINARY_2 + INIT_BINARY_2_CTX.length;\n    private static final int[] INIT_BINARY_3_CTX = {");
            } else {
                System.out.print("Binary 3: ");
            }
            for (int j = 0; j < 32 && i < _contextModels.length; i++, j++) {
                temp += (_contextModels[i] + ",");
            }
            System.out.println(temp);
            temp = "";
            if (code_ready) {
                System.out.println("};\n    public static final int OFFSET_BINARY_4\n            = OFFSET_BINARY_3 + INIT_BINARY_3_CTX.length;\n    private static final int[] INIT_BINARY_4_CTX = {");
            } else {
                System.out.print("Binary 4: ");
            }
            for (int j = 0; j < 32 && i < _contextModels.length; i++, j++) {
                temp += (_contextModels[i] + ",");
            }
            System.out.println(temp);
            temp = "";
            if (code_ready) {
                System.out.println("};\n    public static final int OFFSET_BINARY_5\n            = OFFSET_BINARY_4 + INIT_BINARY_4_CTX.length;\n    private static final int[] INIT_BINARY_5_CTX = {");
            } else {
                System.out.print("Binary 5: ");
            }
            for (int j = 0; j < 32 && i < _contextModels.length; i++, j++) {
                temp += (_contextModels[i] + ",");
            }
            System.out.println(temp);
            if (code_ready) {
                System.out.println("};\n    public static final int OFFSET_FLAG_1\n            = OFFSET_BINARY_5 + INIT_BINARY_4_CTX.length;\n    private static final int[] INIT_FLAG_1_CTX = {");
            } else {
                System.out.print("Flag 1: ");
            }
            if (_contextModels[i].getState() != 64) {
                System.out.print(_contextModels[i++]);
            }
            if (code_ready) {
                System.out.println("};\n    public static final int OFFSET_FLAG_2\n            = OFFSET_FLAG_1 + INIT_FLAG_1_CTX.length;\n    private static final int[] INIT_FLAG_2_CTX = {");
            } else {
                System.out.print("\nFlag 2: ");

            }
            if (_contextModels[i].getState() != 64) {
                System.out.print(_contextModels[i++]);
            }
            if (code_ready) {
                System.out.println("};\n    public static final int OFFSET_FLAG_3\n            = OFFSET_FLAG_2 + INIT_FLAG_2_CTX.length;\n    private static final int[] INIT_FLAG_3_CTX = {");
            } else {
                System.out.print("\nFlag 3: ");
            }
            if (_contextModels[i].getState() != 64) {
                System.out.print(_contextModels[i++]);
            }
            if (code_ready) {
                System.out.println("};\n    public static final int OFFSET_FLAG_4\n            = OFFSET_FLAG_3 + INIT_FLAG_3_CTX.length;\n    private static final int[] INIT_FLAG_4_CTX = {");
            } else {
                System.out.print("\nFlag 4: ");
            }
            if (_contextModels[i].getState() != 64) {
                System.out.print(_contextModels[i++]);
            }
            if (code_ready) {
                System.out.println("};\n    public static final int OFFSET_FLAG_5\n            = OFFSET_FLAG_4 + INIT_FLAG_4_CTX.length;\n    private static final int[] INIT_FLAG_5_CTX = {");
            } else {
                System.out.print("\nFlag 5: ");
            }
            if (_contextModels[i].getState() != 64) {
                System.out.print(_contextModels[i++]);
            }
            if (code_ready) {
                System.out.println("};\n");
            }
            if (code_ready) {
                System.out.println("};\n    public static final int OFFSET_FLAG_6\n            = OFFSET_FLAG_4 + INIT_FLAG_5_CTX.length;\n    private static final int[] INIT_FLAG_6_CTX = {");
            } else {
                System.out.print("\nFlag 6: ");
            }
            if (_contextModels[i].getState() != 64) {
                System.out.print(_contextModels[i++]);
            }
            if (code_ready) {
                System.out.println("};\n    public static final int OFFSET_FLAG_7\n            = OFFSET_FLAG_4 + INIT_FLAG_6_CTX.length;\n    private static final int[] INIT_FLAG_7_CTX = {");
            } else {
                System.out.print("\nFlag 7: ");
            }
            if (_contextModels[i].getState() != 64) {
                System.out.print(_contextModels[i++]);
            }
            if (code_ready) {
                System.out.println("};\n    public static final int OFFSET_FLAG_8\n            = OFFSET_FLAG_4 + INIT_FLAG_7_CTX.length;\n    private static final int[] INIT_FLAG_8_CTX = {");
            } else {
                System.out.print("\nFlag 8: ");
            }
            if (_contextModels[i].getState() != 64) {
                System.out.print(_contextModels[i++]);
            }
            if (code_ready) {
                System.out.println("};\n    public static final int OFFSET_FLAG_9\n            = OFFSET_FLAG_4 + INIT_FLAG_8_CTX.length;\n    private static final int[] INIT_FLAG_9_CTX = {");
            } else {
                System.out.print("\nFlag 9: ");
            }
            if (_contextModels[i].getState() != 64) {
                System.out.print(_contextModels[i++]);
            }
            if (code_ready) {
                System.out.println("};\n    public static final int OFFSET_FLAG_10\n            = OFFSET_FLAG_4 + INIT_FLAG_9_CTX.length;\n    private static final int[] INIT_FLAG_10_CTX = {");
            } else {
                System.out.print("\nFlag 10: ");
            }
            if (_contextModels[i].getState() != 64) {
                System.out.print(_contextModels[i++]);
            }
            if (code_ready) {
                System.out.println("};\n    public static final int OFFSET_FLAG_11\n            = OFFSET_FLAG_4 + INIT_FLAG_10_CTX.length;\n    private static final int[] INIT_FLAG_11_CTX = {");
            } else {
                System.out.print("\nFlag 11: ");
            }
            if (_contextModels[i].getState() != 64) {
                System.out.print(_contextModels[i++]);
            }
            if (code_ready) {
                System.out.println("};\n    public static final int OFFSET_FLAG_12\n            = OFFSET_FLAG_4 + INIT_FLAG_11_CTX.length;\n    private static final int[] INIT_FLAG_12_CTX = {");
            } else {
                System.out.print("\nFlag 12: ");
            }
            if (_contextModels[i].getState() != 64) {
                System.out.print(_contextModels[i++]);
            }
            if (code_ready) {
                System.out.println("};\n");
            }
            System.out.println();

        }

    }

//    public void writeLUT(HashMap<Long, Long> transformMap, int KeySizeBits) {
//        long key, val;
//        writeAsBinary(transformMap.size(), 24);
//        writeAsBinary(KeySizeBits, 8);
//        Iterator<Long> itr = transformMap.keySet().iterator();
//        int i=0;
//        while (itr.hasNext()) {
//            key = itr.next();
//            val = transformMap.get(key);
//            writeAsBinary((int) (key & 0xffffffff), KeySizeBits);
//            writeAsBinary((int) (val & 0xffffffff), Integer.toBinaryString(transformMap.size()-1).length());
////            if(i++<10)
//            System.out.println(key+" "+val);
//        }
//    }
    public void writeLUT(Long2LongOpenHashMap transformMap, int keySizeBit) {
        writeKeyValLUT(transformMap, keySizeBit);
//        writeSimpleLUT(transformMap);
    }

    public void writeSimpleLUT(HashMap<Long, Long> transformMap) {
        //goal: write keys corresponding to values, with values in ascending sequence
        long key, val;
        HashMap<Long, Long> transformMapInversed = new HashMap<>();
        int max = Integer.MIN_VALUE;
        int min = Integer.MAX_VALUE;

        for (Long i : transformMap.keySet()) {
            val = transformMap.get(i);
            transformMapInversed.put(val, i);
            if (i > max) {
                max = i.intValue();//=transformMap.get(i);
            } else if (i < min) {
                min = i.intValue();
            }
        }
//        System.out.println(max);
        int KeySizeBits = Math.max(Integer.toBinaryString(min).length(), Integer.toBinaryString(max).length());
        writeAsBinary(transformMapInversed.size(), 24);
        writeAsBinary(KeySizeBits, 8);
        Iterator<Long> itr = transformMapInversed.keySet().iterator();
        int i = 0;
        while (itr.hasNext()) {
            key = itr.next();
            val = transformMapInversed.get(key);
//            writeAsBinary((int) (key & 0xffffffff), KeySizeBits);
            writeAsBinary((int) (val & 0xffffffff), KeySizeBits);
//            if(i++<10)
//            System.out.println(key+" "+val);
        }
    }

    public void writeKeyValLUT(Long2LongOpenHashMap transformMap, int KeySizeBits) {
        long key, val;
        writeAsBinary(transformMap.size(), 24);
        writeAsBinary(KeySizeBits, 8);
        Iterator<Long> itr = transformMap.keySet().iterator();
//        int i = 0;
        while (itr.hasNext()) {
            key = itr.next();
            val = transformMap.get(key);
            writeAsBinary((int) (key & 0xffffffff), KeySizeBits);
            writeAsBinary((int) (val & 0xffffffff), Integer.toBinaryString(transformMap.size() - 1).length());
//            if(i++<10)
//            System.out.println(key+" "+val);
        }
//        System.out.println("<-encoder ------------------------------------");
    }

    public void writeFileSize(long size) {
        writeAsBinary(size, 64);
    }

    public int getNumberOfBitsWritten() {
        return _out.getNumberOfBitsWritten();
    }

    public int getNumberOfBytesWritten() {
        return _out.getNumberOfBytesWritten();
    }

    public void close() {

        _outCABAC.flush();
        _out.close();
        contextSelector = null;
        _contextModels = null;

    }

    public void resetOutput() {

        try {
            out = new BitOutputStream(new BufferedOutputStream(Files.newOutputStream(FileSystems.getDefault().getPath(_prefix + _suffix + ".coded"))));
        } catch (Exception e) {
            System.err.println("Unable to initialise streams: " + e.getMessage());
//            System.exit(-1);
        }
        _out = out;
        _outCABAC = new EncBinCABAC(_out);
    }

    public int[] ListToArrayInt(ArrayList<Integer> list) {
        int[] array = new int[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return array;
    }

    public byte[] ListToArrayByte(ArrayList<Byte> list) {
        byte[] array = new byte[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return array;
    }

    public static byte[] longToBytes(long l) {
        byte[] result = new byte[8];
        for (int i = 7; i >= 0; i--) {
            result[i] = (byte) (l & 0xFF);
            l >>= 8;
        }
        return result;
    }

    public static long bytesToLong(byte[] b) {
        long result = 0;
        for (int i = 0; i < 8; i++) {
            result <<= 8;
            result |= (b[i] & 0xFF);
        }
        return result;
    }
}
