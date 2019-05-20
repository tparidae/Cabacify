package Analysis;

import arithmeticcoding.Binarisation;
import encoding.Writer;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import JavaMI.Entropy;

public class Analyzer {

    int BUFFER_SIZE = 64 * 1024 * 1024;
    int LIST_LIMIT =1024 * 1024 * 1024;// 20*1000*1000;

    boolean printHistograms = true;
    boolean printAllTests = true;
    boolean encoding_test = true;
    boolean printAllSubTests = true;
    int delimiter = 1001;
    long[][] occurrences;
    int max_value = 0;
    byte[] _in;
    int[] _int;
    int[] _in_mergeToShort;
    int[] _in_mergeToInt;
    final private String[] _prefixes;
    private String _prefix;

    String _suffix;

    public Analyzer(String[] prefixes) {
        _prefixes = prefixes;

    }

    public void startAnalysis(String suffix) {
        byte[] input = null;
        _suffix = suffix;

        for (String _prefix : _prefixes) {
            this._prefix = _prefix;
            if (this._prefix.contains("m13100")) {
                delimiter = 0xffff;
            } else if (this._prefix.contains("03_")) {
                delimiter = 0xffff;
            } else if (this._prefix.contains("gtl_filtered")) {
                delimiter = 0x00ff;
            } else {
                delimiter = 1001;
            }
            try {
                File file = new File(_prefix + suffix);
                if (new File(_prefix + suffix).exists()) {
                    System.out.println("######################" + _prefix + suffix + "######################");
                    try {
                        if (!suffix.contains("pair")) {
                            input = new byte[]{0};
                        } else {
                            input = Files.readAllBytes(new File(_prefix + suffix).toPath());
                        }
                    } catch (Exception e) {
                        System.err.println("Unable to initialise analyzer streams.");
                        System.exit(-1);
                    }
                    _in = input;
                    System.out.println(suffix);
                    analyzeByte(_prefix + suffix, suffix);
                }
            } catch (Exception e) {
                System.out.println("Error while analyzing " + _prefix + suffix + " " + e.getMessage());
            }
        }

    }

    public void analyzePairs(byte[] input) {
        System.out.println("analyzePairs");
        boolean isPairDistance;

        int value;

        //printHistogram(input,2);
        ArrayList<Byte> allPairDescriptors = new ArrayList<Byte>() {
        };
        ArrayList<Integer> allIDs0 = new ArrayList<Integer>() {
        };
        ArrayList<Integer> allIDs1 = new ArrayList<Integer>() {
        };
        ArrayList<Integer> allIDs2 = new ArrayList<Integer>() {
        };
        ArrayList<Integer> allIDs3 = new ArrayList<Integer>() {
        };
        ArrayList<Byte> allRefIDs = new ArrayList<Byte>() {
        };
        ArrayList<Integer> allIDsShort = new ArrayList<Integer>() {
        };
        //modes are defined by ce5 summary table in 3.1
        //this is pairs
        for (int i = 0; i < input.length - 1 && allIDsShort.size() < LIST_LIMIT; i++) {
            //writer.writeAsFlagCabac(0,5);
            isPairDistance = false;
            // System.out.println(i+" "+(input[i + 1] & 0xff)+" "+(input[i] & 0xff));
            if ((input[i + 1] & 0xff) == 0x7f) {
                if ((input[i] & 0xff) == 0xfd) {
                    allPairDescriptors.add((byte) 7);
                    value = (((input[i + 5] & 0x000000ff) << 24) + ((input[i + 4] & 0x000000ff) << 16) + ((input[i + 3] & 0x000000ff) << 8) + (input[i + 2] & 0x000000ff));
                    allIDs0.add(value);
                    i += 5;
                } else if ((input[i] & 0xff) == 0xfe) {
                    allPairDescriptors.add((byte) 2);
                    allRefIDs.add(input[i + 2]);
                    value = (((input[i + 6] & 0x000000ff) << 24) + ((input[i + 5] & 0x000000ff) << 16) + ((input[i + 4] & 0x000000ff) << 8) + (input[i + 3] & 0x000000ff));
                    //allIDs0.add(value);
                    allIDs2.add(value);
                    i += 6;
                } else if ((input[i] & 0xff) == 0xff) {
                    allPairDescriptors.add((byte) 4);
                    i++;
                } else {
                    isPairDistance = true;
                }
            } else if ((input[i + 1] & 0xff) == 0x80) {
                if ((input[i] & 0xff) == 0x03) {
                    allPairDescriptors.add((byte) 1);
                    value = (((input[i + 5] & 0x000000ff) << 24) + ((input[i + 4] & 0x000000ff) << 16) + ((input[i + 3] & 0x000000ff) << 8) + (input[i + 2] & 0x000000ff));
                    //allIDs0.add(value);
                    allIDs1.add(value);
                    i += 5;
                } else if ((input[i] & 0xff) == 0x02) {
                    allPairDescriptors.add((byte) 3);
                    allRefIDs.add(input[i + 2]);
                    value = (((input[i + 6] & 0x000000ff) << 24) + ((input[i + 5] & 0x000000ff) << 16) + ((input[i + 4] & 0x000000ff) << 8) + (input[i + 3] & 0x000000ff));
                    //allIDs0.add(value);
                    allIDs3.add(value);
                    i += 6;
                } else if ((input[i] & 0xff) == 0x01) {
                    allPairDescriptors.add((byte) 5);
                    i++;
                } else if ((input[i] & 0xff) == 0x00) {
                    allPairDescriptors.add((byte) 6);
                    i++;
                } else {
                    isPairDistance = true;
                }
            } else {
                isPairDistance = true;
            }
            if (isPairDistance) {
                allPairDescriptors.add((byte) 0);
                value = ((input[i + 1] & 0x000000ff) << 8) + (input[i] & 0x000000ff);
                i++;
                allIDsShort.add(value);
            }
            //System.out.println(prev_predictor);
        }
        System.out.println("\n\n\nPairDescriptors");
        processByteList(allPairDescriptors, 1);
        processByteListConditional(allPairDescriptors, 1, 8);
        System.out.println("\n\n\nIDs 0x7FFD");
        //System.out.println("\n\n\nIDtype");
        processIntList(allIDs0);
        allIDs0.clear();
        System.out.println("\n\n\nIDs 0x8003");
        processIntList(allIDs1);
        allIDs1.clear();
        System.out.println("\n\n\nIDs 0x7FFE");
        processIntList(allIDs2);
        allIDs2.clear();
        System.out.println("\n\n\nIDs 0x8002");
        processIntList(allIDs3);
        allIDs3.clear();
        System.out.println("\n\n\nIDsShort");
        processIntList(allIDsShort);
        allIDsShort.clear();
        System.out.println("\n\n\nrefIDs");
        processByteList(allRefIDs, 1);

    }

    private void analyzeByte(String pad, String name) {
        System.out.println("analyze " + name);
        ArrayList<Byte> allValues = new ArrayList<Byte>() {
        };

        FileInputStream inputFile;
        Path path = new File(pad).toPath();

        byte[] input = new byte[BUFFER_SIZE];
        int read = 0;

        try {
            inputFile = new FileInputStream(path.toFile());
            while ((read = inputFile.read(input)) > 0) {
                int i = 0;
                for (i = 0; i < read && allValues.size() < LIST_LIMIT; i++) {
                    allValues.add((byte) input[i]);
                }
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        System.out.println(
                "\n\n\nall values");
        processByteList(allValues, 1);

    }

    private void analyzeQVCI(String pad, String name) {
        System.out.println("analyze " + name);
        ArrayList<Integer> allPositionOffsets = new ArrayList<Integer>() {
        };
        ArrayList<Integer> allLengths = new ArrayList<Integer>() {
        };
        ArrayList<Byte> allValues = new ArrayList<Byte>() {
        };

        FileInputStream inputFile;
        Path path = new File(pad).toPath();

        byte[] input = new byte[BUFFER_SIZE];
        byte[] tempInput = new byte[BUFFER_SIZE];
        int read = 0;
        int QVCodebookIdentifierBufferSize = 0;
        int offset = 0;
        int transferredBytes = 0;
        int PositionOffset = 0;
        boolean CodeBookReadFinished = true;
        int prev = 0;
        try {
            inputFile = new FileInputStream(path.toFile());
            while ((read = inputFile.read(input)) > 0) {
                offset = 0;
                System.arraycopy(tempInput, 0, input, transferredBytes, read);
                while (((transferredBytes + read == BUFFER_SIZE && offset < transferredBytes + read - 11) || (transferredBytes + read != BUFFER_SIZE && offset < transferredBytes + read)) & allPositionOffsets.size() < LIST_LIMIT) {
                    if (allPositionOffsets.size() > LIST_LIMIT) {
                        break;
                    }
                    if (CodeBookReadFinished) {
                        PositionOffset = 0;
                        QVCodebookIdentifierBufferSize = 0;
                        for (int i = offset; i < offset + 4 && i < input.length; i++) {
                            PositionOffset *= 256;
                            PositionOffset += input[i] & 0xff;
                            allPositionOffsets.add(PositionOffset);
                        }
//                        System.out.println("PositionOffset: " + PositionOffset);
                        for (int i = offset + 4; i < offset + 12 && i < input.length; i++) {
                            QVCodebookIdentifierBufferSize *= 256;
                            QVCodebookIdentifierBufferSize += input[i] & 0xff;
//                            allLengths.add(QVCodebookIdentifierBufferSize);
                        }
//                        System.out.println("QVCodebookIdentifierBufferSize: " + QVCodebookIdentifierBufferSize);
                        offset += 12;
                    } else {
                        CodeBookReadFinished = true;
                    }
                    if (offset + QVCodebookIdentifierBufferSize < input.length) {
                        for (int j = offset; j < offset + QVCodebookIdentifierBufferSize; j++) {
//                            allValues.add(input[j]);                            
                        }
                        offset += QVCodebookIdentifierBufferSize;
                        CodeBookReadFinished = true;
                    } else {

//                        System.out.println("Refresh buffer: ");
                        CodeBookReadFinished = false;
                        transferredBytes = input.length - (offset);
                        if (offset == 0) {
                            System.out.println("Buffer is probably too low");
                        }
                        System.arraycopy(input, offset, input, 0, transferredBytes);
                        tempInput = new byte[BUFFER_SIZE - transferredBytes];
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        System.out.println(
                "\n\n\nall Position Offsets");
        processIntList(allPositionOffsets);
        allPositionOffsets.clear();

        input = new byte[BUFFER_SIZE];
        tempInput = new byte[BUFFER_SIZE];
        read = 0;
        QVCodebookIdentifierBufferSize = 0;
        offset = 0;
        transferredBytes = 0;
        PositionOffset = 0;
        CodeBookReadFinished = true;
        prev = 0;
        tempInput = new byte[BUFFER_SIZE];
        input = new byte[BUFFER_SIZE];
        try {
            inputFile = new FileInputStream(path.toFile());
            while ((read = inputFile.read(input)) > 0) {
                offset = 0;
                System.arraycopy(tempInput, 0, input, transferredBytes, read);
                while (((transferredBytes + read == BUFFER_SIZE && offset < transferredBytes + read - 11) || (transferredBytes + read != BUFFER_SIZE && offset < transferredBytes + read)) && allLengths.size() < LIST_LIMIT) {
                    if (CodeBookReadFinished) {
                        PositionOffset = 0;
                        QVCodebookIdentifierBufferSize = 0;
                        for (int i = offset; i < offset + 4 && i < input.length; i++) {
                            PositionOffset *= 256;
                            PositionOffset += input[i] & 0xff;
//                            allPositionOffsets.add(PositionOffset);
                        }
//                        System.out.println("PositionOffset: " + PositionOffset);
                        for (int i = offset + 4; i < offset + 12 && i < input.length; i++) {
                            QVCodebookIdentifierBufferSize *= 256;
                            QVCodebookIdentifierBufferSize += input[i] & 0xff;
                            allLengths.add(QVCodebookIdentifierBufferSize);
                        }
//                        System.out.println("QVCodebookIdentifierBufferSize: " + QVCodebookIdentifierBufferSize);
                        offset += 12;
                    } else {
                        CodeBookReadFinished = true;
                    }
                    if (offset + QVCodebookIdentifierBufferSize < input.length) {
                        for (int j = offset; j < offset + QVCodebookIdentifierBufferSize; j++) {
//                            allValues.add(input[j]);                            
                        }
                        offset += QVCodebookIdentifierBufferSize;
                        CodeBookReadFinished = true;
                    } else {

//                        System.out.println("Refresh buffer: ");
                        CodeBookReadFinished = false;
                        transferredBytes = input.length - (offset);
                        if (offset == 0) {
                            System.out.println("Buffer is probably too low");
                        }
                        System.arraycopy(input, offset, input, 0, transferredBytes);
                        tempInput = new byte[BUFFER_SIZE - transferredBytes];
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        System.out.println(
                "\n\n\nall lengths");
        processIntList(allLengths);
        allLengths.clear();

        input = new byte[BUFFER_SIZE];
        tempInput = new byte[BUFFER_SIZE];
        read = 0;
        QVCodebookIdentifierBufferSize = 0;
        offset = 0;
        transferredBytes = 0;
        PositionOffset = 0;
        CodeBookReadFinished = true;
        prev = 0;
        tempInput = new byte[BUFFER_SIZE];
        input = new byte[BUFFER_SIZE];
        try {
            inputFile = new FileInputStream(path.toFile());
            while ((read = inputFile.read(tempInput)) > 0) {
                offset = 0;
                System.arraycopy(tempInput, 0, input, transferredBytes, read);
                while (((transferredBytes + read == BUFFER_SIZE && offset < transferredBytes + read - 11) || (transferredBytes + read != BUFFER_SIZE && offset < transferredBytes + read)) && allValues.size() < LIST_LIMIT) {
                    if (allValues.size() > LIST_LIMIT) {
                        break;
                    }
                    if (CodeBookReadFinished) {
                        PositionOffset = 0;
                        QVCodebookIdentifierBufferSize = 0;
                        for (int i = offset; i < offset + 4 && i < input.length; i++) {
                            PositionOffset *= 256;
                            PositionOffset += input[i] & 0xff;
//                            allPositionOffsets.add(PositionOffset);
                        }
//                        System.out.println("PositionOffset: " + PositionOffset);
                        for (int i = offset + 4; i < offset + 12 && i < input.length; i++) {
                            QVCodebookIdentifierBufferSize *= 256;
                            QVCodebookIdentifierBufferSize += input[i] & 0xff;
//                            allLengths.add(QVCodebookIdentifierBufferSize);
                        }
//                        System.out.println("QVCodebookIdentifierBufferSize: " + QVCodebookIdentifierBufferSize);
                        offset += 12;
                    } else {
                        CodeBookReadFinished = true;
                    }
                    if (offset + QVCodebookIdentifierBufferSize < input.length) {
                        for (int j = offset; j < offset + QVCodebookIdentifierBufferSize; j++) {
                            allValues.add(input[j]);
                        }
                        offset += QVCodebookIdentifierBufferSize;
                        CodeBookReadFinished = true;
                    } else {

//                        System.out.println("Refresh buffer: ");
                        CodeBookReadFinished = false;
                        transferredBytes = input.length - (offset);
                        if (offset == 0) {
                            System.out.println("Buffer is probably too low");
                        }
                        System.arraycopy(input, offset, input, 0, transferredBytes);
                        tempInput = new byte[BUFFER_SIZE - transferredBytes];
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        System.out.println(
                "\n\n\nall values");
        processByteList(allValues, 1);
        allValues.clear();
    }

    private void analyzeQVI(String pad, String name) {
        System.out.println("analyze " + name);
        ArrayList<Integer> allLengths = new ArrayList<Integer>() {
        };
        ArrayList<Byte> allValues = new ArrayList<Byte>() {
        };

        FileInputStream inputFile;
        Path path = new File(pad).toPath();

        byte[] input = new byte[BUFFER_SIZE];
        byte[] tempInput = new byte[BUFFER_SIZE];
        int read = 0;
        int QVCodebookIdentifierBufferSize = 0;
        int offset = 0;
        int transferredBytes = 0;
        boolean CodeBookReadFinished = true;
        int prev = 0;
        try {
            inputFile = new FileInputStream(path.toFile());
            while ((read = inputFile.read(tempInput)) > 0) {
                offset = 0;
                System.arraycopy(tempInput, 0, input, transferredBytes, read);
                while (((transferredBytes + read == BUFFER_SIZE && offset < transferredBytes + read - 11) || (transferredBytes + read != BUFFER_SIZE && offset < transferredBytes + read)) && allLengths.size() < LIST_LIMIT / 5000) {
                    if (CodeBookReadFinished) {
                        QVCodebookIdentifierBufferSize = 0;
                        for (int i = offset; i < offset + 8 && i < input.length; i++) {
                            QVCodebookIdentifierBufferSize *= 256;
                            QVCodebookIdentifierBufferSize += input[i] & 0xff;
                            allLengths.add(QVCodebookIdentifierBufferSize);
                        }
//                        System.out.println("QVCodebookIdentifierBufferSize: " + QVCodebookIdentifierBufferSize);
                        offset += 8;
                    } else {
                        CodeBookReadFinished = true;
                    }
                    if (offset + QVCodebookIdentifierBufferSize < input.length) {
                        for (int j = offset; j < offset + QVCodebookIdentifierBufferSize; j++) {
//                            allValues.add(input[j]);                            
                        }
                        offset += QVCodebookIdentifierBufferSize;
                        CodeBookReadFinished = true;
                    } else {

//                        System.out.println("Refresh buffer: ");
                        CodeBookReadFinished = false;
                        transferredBytes = input.length - (offset);
                        if (offset == 0) {
                            System.out.println("Buffer is probably too low");
                        }
                        System.arraycopy(input, offset, input, 0, transferredBytes);
                        tempInput = new byte[BUFFER_SIZE - transferredBytes];
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        System.out.println(
                "\n\n\nall lengths");
        processIntList(allLengths);
        allLengths.clear();

        input = new byte[BUFFER_SIZE];
        tempInput = new byte[BUFFER_SIZE];
        read = 0;
        QVCodebookIdentifierBufferSize = 0;
        offset = 0;
        transferredBytes = 0;
        CodeBookReadFinished = true;
        prev = 0;
        tempInput = new byte[BUFFER_SIZE];
        input = new byte[BUFFER_SIZE];
        try {
            inputFile = new FileInputStream(path.toFile());
            while ((read = inputFile.read(tempInput)) > 0) {
                offset = 0;
                System.arraycopy(tempInput, 0, input, transferredBytes, read);
                while (((transferredBytes + read == BUFFER_SIZE && offset < transferredBytes + read - 11) || (transferredBytes + read != BUFFER_SIZE && offset < transferredBytes + read)) && allValues.size() < LIST_LIMIT) {

                    if (CodeBookReadFinished) {
                        QVCodebookIdentifierBufferSize = 0;
                        for (int i = offset; i < offset + 8 && i < input.length; i++) {
                            QVCodebookIdentifierBufferSize *= 256;
                            QVCodebookIdentifierBufferSize += input[i] & 0xff;
//                            allLengths.add(QVCodebookIdentifierBufferSize);
                        }
//                        System.out.println("QVCodebookIdentifierBufferSize: " + QVCodebookIdentifierBufferSize);
                        offset += 8;
                    } else {
                        CodeBookReadFinished = true;
                    }
                    if (offset + QVCodebookIdentifierBufferSize < input.length) {
                        for (int j = offset; j < offset + QVCodebookIdentifierBufferSize; j++) {
                            allValues.add((byte) (input[j] - 48));
                        }
                        offset += QVCodebookIdentifierBufferSize;
                        CodeBookReadFinished = true;
                    } else {

//                        System.out.println("Refresh buffer: ");
                        CodeBookReadFinished = false;
                        transferredBytes = input.length - (offset);
                        if (offset == 0) {
                            System.out.println("Buffer is probably too low");
                        }
                        System.arraycopy(input, offset, input, 0, transferredBytes);
                        tempInput = new byte[BUFFER_SIZE - transferredBytes];
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        System.out.println(
                "\n\n\nall values");
        processByteList(allValues, 1);
        allValues.clear();

    }

    private void analyzeInt(String pad, String name) {
        System.out.println("analyze " + name);
        ArrayList<Integer> allValues = new ArrayList<Integer>() {
        };

        FileInputStream inputFile;
        Path path = new File(pad).toPath();

        byte[] input = new byte[BUFFER_SIZE];
        int read = 0;

        try {
            inputFile = new FileInputStream(path.toFile());
            while ((read = inputFile.read(input)) > 0) {
                int i = 0;
                for (i = 0; i < read - 3 && allValues.size() < LIST_LIMIT; i += 4) {
                    allValues.add((((input[i + 3] & 0x000000ff) << 24) + ((input[i + 2] & 0x000000ff) << 16) + ((input[i + 1] & 0x000000ff) << 8) + (input[i] & 0x000000ff)));

                }
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        System.out.println(
                "\n\n\nall values");
        processIntList(allValues);

    }

    private void analyzePos(String pad) {
        System.out.println("analyzePos");

        if (BUFFER_SIZE % 4 == 0) {

            ArrayList<Byte> Pos0 = new ArrayList<Byte>() {
            };
            ArrayList<Byte> Pos1 = new ArrayList<Byte>() {
            };
            ArrayList<Byte> Pos2 = new ArrayList<Byte>() {
            };
            ArrayList<Byte> Pos3 = new ArrayList<Byte>() {
            };
            ArrayList<Integer> PosAll = new ArrayList<Integer>() {
            };
            FileInputStream inputFile;
            Path path = new File(pad).toPath();
            byte[] input = new byte[BUFFER_SIZE];
            int read = 0;

            try {
                inputFile = new FileInputStream(path.toFile());
                while ((read = inputFile.read(input)) > 0) {
                    for (int i = 0; i < read - 3 && Pos0.size() < LIST_LIMIT; i += 4) {
                        Pos0.add(input[i]);
                    }
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }

            System.out.println("\n\n\nPos0");
            processByteListConvertNegatives(Pos0, 1);
            Pos0.clear();
            try {
                inputFile = new FileInputStream(path.toFile());
                while ((read = inputFile.read(input)) > 0) {
                    for (int i = 0; i < read - 3 && Pos1.size() < LIST_LIMIT; i += 4) {
                        Pos1.add(input[i + 1]);
                    }
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
            System.out.println("\n\n\nPos1");
            processByteListConvertNegatives(Pos1, 1);
            Pos1.clear();
            try {
                inputFile = new FileInputStream(path.toFile());
                while ((read = inputFile.read(input)) > 0) {
                    for (int i = 0; i < read - 3 && Pos2.size() < LIST_LIMIT; i += 4) {
                        Pos2.add(input[i + 2]);
                    }
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
            System.out.println("\n\n\nPos2");
            processByteListConvertNegatives(Pos2, 1);
            Pos2.clear();
            try {
                inputFile = new FileInputStream(path.toFile());
                while ((read = inputFile.read(input)) > 0) {
                    for (int i = 0; i < read - 3 && Pos3.size() < LIST_LIMIT; i += 4) {
                        Pos3.add(input[i + 3]);
                    }
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
            System.out.println("\n\n\nPos3");
            processByteListConvertNegatives(Pos3, 1);
            Pos3.clear();
            try {
                inputFile = new FileInputStream(path.toFile());
                while ((read = inputFile.read(input)) > 0) {
                    for (int i = 0; i < read - 3 && PosAll.size() < LIST_LIMIT; i += 4) {
                        PosAll.add((((input[i + 3] & 0x000000ff) << 24) + ((input[i + 2] & 0x000000ff) << 16) + ((input[i + 1] & 0x000000ff) << 8) + (input[i] & 0x000000ff)));
                    }
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
            System.out.println("\n\n\nPosAll");
            processIntList(PosAll);
            PosAll.clear();
        }
    }

    private void analyzeAsBytes(byte[] input) {
        ArrayList<Byte> allValues = new ArrayList<Byte>() {
        };

        for (int j = 0; j < input.length && allValues.size() < LIST_LIMIT; j++) {
            allValues.add(input[j]);
        }
        System.out.println("\n\n\nAnalyzeAsBytes");
        processByteList(allValues, 1);

    }

    public void analyzeIndc(String pad, String name) {
        System.out.println("analyze " + name);
        ArrayList<Integer> allIDs = new ArrayList<Integer>() {
        };
        ArrayList<Byte> allPositions = new ArrayList<Byte>() {
        };
        ArrayList<Integer> allLengths = new ArrayList<Integer>() {
        };
        ArrayList<Byte> allBases = new ArrayList<Byte>() {
        };

        int id = 0;
        byte position = 0;
        byte base = 0x00;
        byte terminator = (byte) 0xff;
        byte softClipTerminator = (byte) 0xfe;
        int old_id = 0;
        int read = 0;
        boolean continueReading = false;
        int transferredBytes = 0;
        FileInputStream inputFile;
        Path path = new File(pad).toPath();
        byte[] input = new byte[BUFFER_SIZE];
        byte[] tempInput = new byte[BUFFER_SIZE];
        try {
            inputFile = new FileInputStream(path.toFile());
            int i = 0;
            while ((read = inputFile.read(tempInput)) > 0) {
                if (allIDs.size() > LIST_LIMIT) {
                    break;
                }
                if (read + transferredBytes != BUFFER_SIZE) {
                    input = tempInput;
                } else {
                    System.arraycopy(tempInput, 0, input, transferredBytes, tempInput.length);
                }
                for (i = 0; i < input.length - 6;) {
                    base = (byte) 0x00;
                    if (!continueReading) {
                        id = (((input[i + 3] & 0x000000ff) << 24) + ((input[i + 2] & 0x000000ff) << 16) + ((input[i + 1] & 0x000000ff) << 8) + (input[i] & 0x000000ff));
                        allIDs.add(id - old_id);
                        old_id = id;
                        i += 4;
                        position = input[i++];
//                        allPositions.add(position);
                    }
                    continueReading = false;

                    if (position > 3) {
//                        allLengths.add(((input[i + 1] & 0x000000ff) << 8) + (input[i] & 0x000000ff));
                        i += 2;
                    }
                    while (base != softClipTerminator && base != terminator && i < input.length) {
                        base = input[i++];
                        if (base != 0) {
//                            allBases.add(base);
                        }
                    }
                }
                if (base != softClipTerminator && base != terminator) {
                    continueReading = true;
                }

                transferredBytes = input.length - i;
                System.arraycopy(input, i, input, 0, transferredBytes);
                tempInput = new byte[input.length - transferredBytes];
            }

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        System.out.println("\n\n\nID offsets");
        processIntList(allIDs);
        position = 0;
        read = 0;
        base = (byte) 0x00;
        old_id = 0;
        try {
            inputFile = new FileInputStream(path.toFile());
            int i = 0;
            while ((read = inputFile.read(tempInput)) > 0) {
                if (allPositions.size() > LIST_LIMIT) {
                    break;
                }
                if (read + transferredBytes != BUFFER_SIZE) {
                    input = tempInput;
                } else {
                    System.arraycopy(tempInput, 0, input, transferredBytes, tempInput.length);
                }
                for (i = 0; i < input.length - 6;) {
                    base = (byte) 0x00;
                    if (!continueReading) {
                        id = (((input[i + 3] & 0x000000ff) << 24) + ((input[i + 2] & 0x000000ff) << 16) + ((input[i + 1] & 0x000000ff) << 8) + (input[i] & 0x000000ff));
//                        allIDs.add(id - old_id);
                        old_id = id;
                        i += 4;
                        position = input[i++];
                        allPositions.add(position);
                    }
                    continueReading = false;

                    if (position > 3) {
//                        allLengths.add(((input[i + 1] & 0x000000ff) << 8) + (input[i] & 0x000000ff));
                        i += 2;
                    }
                    while (base != softClipTerminator && base != terminator && i < input.length) {
                        base = input[i++];
                        if (base != 0) {
//                            allBases.add(base);
                        }
                    }
                }
                if (base != softClipTerminator && base != terminator) {
                    continueReading = true;
                }

                transferredBytes = input.length - i;
                System.arraycopy(input, i, input, 0, transferredBytes);
                tempInput = new byte[input.length - transferredBytes];
            }

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        System.out.println("\n\n\nPositions");
        processByteList(allPositions, 1);
        allPositions.clear();
        position = 0;
        read = 0;
        base = (byte) 0x00;
        old_id = 0;
        tempInput = new byte[BUFFER_SIZE];
        input = new byte[BUFFER_SIZE];
        try {
            inputFile = new FileInputStream(path.toFile());
            int i = 0;
            while ((read = inputFile.read(tempInput)) > 0) {
                if (allLengths.size() > LIST_LIMIT) {
                    break;
                }
                if (read + transferredBytes != BUFFER_SIZE) {
                    input = tempInput;
                } else {
                    System.arraycopy(tempInput, 0, input, transferredBytes, tempInput.length);
                }
                for (i = 0; i < input.length - 6;) {
                    base = (byte) 0x00;
                    if (!continueReading) {
                        id = (((input[i + 3] & 0x000000ff) << 24) + ((input[i + 2] & 0x000000ff) << 16) + ((input[i + 1] & 0x000000ff) << 8) + (input[i] & 0x000000ff));
//                        allIDs.add(id - old_id);
                        old_id = id;
                        i += 4;
                        position = input[i++];
//                        allPositions.add(position);
                    }
                    continueReading = false;

                    if (position > 3) {
                        allLengths.add(((input[i + 1] & 0x000000ff) << 8) + (input[i] & 0x000000ff));
                        i += 2;
                    }
                    while (base != softClipTerminator && base != terminator && i < input.length) {
                        base = input[i++];
                        if (base != 0) {
//                            allBases.add(base);
                        }
                    }
                }
                if (base != softClipTerminator && base != terminator) {
                    continueReading = true;
                }

                transferredBytes = input.length - i;
                System.arraycopy(input, i, input, 0, transferredBytes);
                tempInput = new byte[input.length - transferredBytes];
            }

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        System.out.println("\n\n\nlengths");
        processIntList(allLengths);
        allBases.clear();
        position = 0;
        read = 0;
        base = (byte) 0x00;
        old_id = 0;
        tempInput = new byte[BUFFER_SIZE];
        input = new byte[BUFFER_SIZE];
        try {
            inputFile = new FileInputStream(path.toFile());
            int i = 0;
            while ((read = inputFile.read(tempInput)) > 0) {
                if (allBases.size() > LIST_LIMIT) {
                    break;
                }
                if (read + transferredBytes != BUFFER_SIZE) {
                    input = tempInput;
                } else {
                    System.arraycopy(tempInput, 0, input, transferredBytes, tempInput.length);
                }
                for (i = 0; i < input.length - 6;) {
                    base = (byte) 0x00;
                    if (!continueReading) {
                        id = (((input[i + 3] & 0x000000ff) << 24) + ((input[i + 2] & 0x000000ff) << 16) + ((input[i + 1] & 0x000000ff) << 8) + (input[i] & 0x000000ff));
//                        allIDs.add(id - old_id);
                        old_id = id;
                        i += 4;
                        position = input[i++];
//                        allPositions.add(position);
                    }
                    continueReading = false;

                    if (position > 3) {
//                        allLengths.add(((input[i + 1] & 0x000000ff) << 8) + (input[i] & 0x000000ff));
                        i += 2;
                    }
                    while (base != softClipTerminator && base != terminator && i < input.length) {
                        base = input[i++];
                        if (base != 0) {
                            allBases.add(base);
                        }
                    }
                }
                if (base != softClipTerminator && base != terminator) {
                    continueReading = true;
                }

                transferredBytes = input.length - i;
                System.arraycopy(input, i, input, 0, transferredBytes);
                tempInput = new byte[input.length - transferredBytes];
            }

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        System.out.println("\n\n\nBases");
        processByteList(allBases, 1);
        allBases.clear();

    }

    private void analyzeDelimiterShort(String pad, String name) {
        System.out.println("@@@@@@@@@@@analyze" + name + " with nr of mismatches@@@@@@@@@@@");
        ArrayList<Integer> allValues0 = new ArrayList<Integer>() {
        };
        ArrayList<Integer> allValues1 = new ArrayList<Integer>() {
        };
        FileInputStream inputFile;
        Path path = new File(pad).toPath();
        BUFFER_SIZE = 256 * 1024 * 1024;
        byte[] input = new byte[BUFFER_SIZE];
        byte[] tempInput = new byte[BUFFER_SIZE];
        int read = 0;
        int value = 0;
        int nrOfMismatches;
        byte prev = 0;
        int offset = 0;
        try {
            inputFile = new FileInputStream(path.toFile());
            int transferredBytes = 0;
            int i = 0;
            while ((read = inputFile.read(tempInput)) > 0) {
                if (allValues0.size() > LIST_LIMIT) {
                    break;
                }
                offset = 0;
                System.arraycopy(tempInput, 0, input, transferredBytes, read);
                transferredBytes = 0;
                tempInput = null;

                while (offset < input.length) {
                    for (i = offset; i < input.length; i += 2) {
                        value = (((input[i + 1] & 0x00ff) << 8) + (input[i] & 0x000000ff));
                        if (value == delimiter) {
                            nrOfMismatches = (i - offset) / 2;
                            allValues0.add(nrOfMismatches);
                            break;
                        }
                    }

                    if (value == delimiter) {
                        for (; offset < i && offset < input.length; offset += 2) {
//                            allValues1.add((((input[offset + 1] & 0x00ff) << 8) + (input[offset] & 0x000000ff)));
                        }
                        offset += 2;
                        transferredBytes = 0;
                        if (offset == input.length) {
                            tempInput = new byte[input.length];
                        }
                    } else {
                        //System.out.print("Refresh buffer: ");
                        transferredBytes = input.length - (offset);
                        if (offset == 0) {
                            System.out.println("Buffer is probably too low");
                        }
                        System.arraycopy(input, offset, input, 0, transferredBytes);
                        tempInput = new byte[BUFFER_SIZE - transferredBytes];
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        offset = 0;
        read = 0;
        tempInput = new byte[BUFFER_SIZE];
        input = new byte[BUFFER_SIZE];

        System.out.println("\n\n\nlength");
        processIntList(allValues0);
        allValues0.clear();
        try {
            inputFile = new FileInputStream(path.toFile());
            int transferredBytes = 0;
            int i = 0;
            while ((read = inputFile.read(tempInput)) > 0) {
                if (allValues1.size() > LIST_LIMIT) {
                    break;
                }
                offset = 0;
                System.arraycopy(tempInput, 0, input, transferredBytes, read);
                transferredBytes = 0;
                tempInput = null;
                while (offset < input.length) {
                    for (i = offset; i < input.length; i += 2) {
                        value = (((input[i + 1] & 0x00ff) << 8) + (input[i] & 0x000000ff));
                        if (value == delimiter) {
                            nrOfMismatches = (i - offset) / 2;
//                            allValues0.add(nrOfMismatches);
                            break;
                        }
                    }

                    if (value == delimiter) {
                        for (; offset < i && offset < input.length; offset += 2) {
                            allValues1.add((((input[offset + 1] & 0x00ff) << 8) + (input[offset] & 0x000000ff)));
                        }
                        offset += 2;
                        transferredBytes = 0;
                        if (offset == input.length) {
                            tempInput = new byte[input.length];
                        }
                    } else {
                        //System.out.print("Refresh buffer: ");
                        transferredBytes = input.length - (offset);
                        if (offset == 0) {
                            System.out.println("Buffer is probably too low");
                        }
                        System.arraycopy(input, offset, input, 0, transferredBytes);
                        tempInput = new byte[BUFFER_SIZE - transferredBytes];
                        break;
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        System.out.println("\n\n\n" + name + "");
        processIntList(allValues1);
        allValues1.clear();

        System.out.println("@@@@@@@@@@@analyze" + name + " with only values@@@@@@@@@@@");
        offset = 0;
        read = 0;
        tempInput = new byte[BUFFER_SIZE];
        input = new byte[BUFFER_SIZE];
        try {
            inputFile = new FileInputStream(path.toFile());
            while ((read = inputFile.read(input)) > 0) {
                if (allValues1.size() > LIST_LIMIT) {
                    break;
                }
                offset = 0;

                for (int i = 0; i < read && allValues1.size() < LIST_LIMIT; i += 2) {
                    allValues1.add((((input[i + 1] & 0x00ff) << 8) + (input[i] & 0x000000ff)));
                    offset += 2;
                }

            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        System.out.println("\n\n\nASNPP");
        processIntList(allValues1);

    }

    private void analyzeQVCI(byte[] input) {

        ArrayList<Byte> allValues = new ArrayList<Byte>() {
        };

        int size = input.length;
        int PositionOffset = 0;
        long QVCodebookIdentifierBufferSize = 0;
        int offset = 0;

        while (offset < input.length) {
            PositionOffset = 0;
            QVCodebookIdentifierBufferSize = 0;
            for (int i = offset; i < offset + 4 && i < input.length; i++) {
                PositionOffset = PositionOffset & 0xffffffff * 256;
                PositionOffset += input[i] & 0xff;
                //writer.writeAsBinary(input[i] & 0xff, 8);
            }
            for (int i = offset + 4; i < offset + 12 && i < input.length; i++) {
                QVCodebookIdentifierBufferSize *= 256;
                QVCodebookIdentifierBufferSize += input[i] & 0xff;
                // writer.writeAsBinary(input[i] & 0xff, 8);
            }
            byte prev = 0;
            for (int j = offset + 12; j < offset + QVCodebookIdentifierBufferSize + 12 && j < input.length; j++) {
                input[j] = (byte) (input[j] - 48);
                allValues.add(input[j]);
                prev = input[j];//transformMap.get((long)(input[j])).byteValue();
            }
            offset += QVCodebookIdentifierBufferSize + 12;
//            System.out.println(offset);
        }
        System.out.println("\n\n\nQVCI");
        processByteList(allValues, 1);

    }

    private void analyzeQVI(byte[] input) {

        ArrayList<Byte> allValues = new ArrayList<Byte>() {
        };

        int size = input.length;
        int PositionOffset = 0;
        long QVCodebookIdentifierBufferSize = 0;
        int offset = 0;

        while (offset < input.length) {
            QVCodebookIdentifierBufferSize = 0;

            for (int i = offset; i < offset + 8 && i < input.length; i++) {
                QVCodebookIdentifierBufferSize *= 256;
                QVCodebookIdentifierBufferSize += input[i] & 0xff;
                // writer.writeAsBinary(input[i] & 0xff, 8);
            }
            byte prev = 0;
            for (int j = offset + 8; j < offset + QVCodebookIdentifierBufferSize + 8 && j < input.length; j++) {
                input[j] = (byte) (input[j] - 48);
                allValues.add(input[j]);
                prev = input[j];//transformMap.get((long)(input[j])).byteValue();
            }
            offset += QVCodebookIdentifierBufferSize + 8;
//            System.out.println(offset);
        }
        System.out.println("\n\n\nQVCI");
        processByteList(allValues, 1);

    }

    private void analyzeQVCodebookID(byte[] input) {
        ArrayList<Byte> allValues = new ArrayList<Byte>() {
        };

        for (int j = 0; j < input.length; j++) {
            allValues.add(input[j]);
        }
        System.out.println("\n\n\nQVCodebookID");
        processByteList(allValues, 1);

    }

    private void testAllCodingsInt(int[] values, int offset) {
        if (encoding_test) {
            testAllCodings(values, offset, 32);
        }
    }

    private void testAllCodingsShort(int[] values, int offset) {
        if (encoding_test) {
            testAllCodings(values, offset, 16);
        }
    }

    private void testAllCodingsByteConvertNegatives(byte[] values, int offset) {
        if (encoding_test) {
            int[] values2 = new int[values.length];
            for (int i = 0; i < values2.length; i++) {

                values2[i] = (int) values[i] & 0xff;

            }
            //testAllCodings(values2, offset, containsNegatives, 8);
            testAllCodings(values2, offset, 8);
        }
    }

    private void testAllCodingsByte(byte[] values, int offset) {
        if (encoding_test) {
            int[] values2 = new int[values.length];
            for (int i = 0; i < values2.length; i++) {
                values2[i] = (int) values[i];
            }
            testAllCodings(values2, offset, 8);
        }
    }

    private void testAllCodings(int[] values, int offset, int length) {
        boolean containsNegatives = false;
        String bestByPassName = "";
        Writer writer = new Writer(_prefix, _suffix + "_allcodings", values.length);
        int nr_Cabac_modes = 1;
        long start;
        writer.buildContextModels();
        long size;
        long[] best_size = new long[]{Long.MAX_VALUE, Long.MAX_VALUE, Long.MAX_VALUE};
        String[] best_name = new String[]{"", "", ""};
        max_value = 0;
        int min_value = Integer.MAX_VALUE;
        int value = 0;

        for (int i = 0; i < values.length; i++) {
            if (values[i] > max_value) {
                max_value = values[i];
            } else if (values[i] < min_value) {
                min_value = values[i];
            }
        }
        System.out.println("Max Value: " + max_value);
        int test = 0;
        for (int i = 0; i < values.length; i++) {
            test = values[i] & 0xffffffff;
            if (test < 0) {
                containsNegatives = true;
                System.out.println("overruled containsNegatives");
                break;
            }
        }

        int bitsize = Integer.toBinaryString(max_value).length();

        //BINARY
        start = writer.getNumberOfBytesWritten();
        for (int i = 0; i < values.length; i++) {
            if (length == 32) {
                value = values[i] & 0xffffffff;
            } else if (length == 16) {
                value = values[i] & 0xffff;
            } else if (length == 8) {
                value = values[i] & 0xff;
            }
            writer.writeAsBinary(value, bitsize);
        }
        long bestByPassSize = writer.getNumberOfBytesWritten() - start;
        if (printAllTests) {
            System.out.println("Binary: " + bestByPassSize + " bytes");
        }
        writer.resetOutput();

        //BINARYCABAC
        for (int mode = 0; mode < nr_Cabac_modes; mode++) {
            writer.buildContextModels();
            start = writer.getNumberOfBytesWritten();
            for (int i = 0; i < values.length; i++) {
                if (length == 32) {
                    value = values[i] & 0xffffffff;
                } else if (length == 16) {
                    value = values[i] & 0xffff;
                } else if (length == 8) {
                    value = values[i] & 0xff;
                }
                //bitsize can also be length (or vice versa), if not bitsize needs to be signaled
                writer.writeAsBinaryCabac(value, length, offset);
            }
            size = writer.getNumberOfBytesWritten() - start;
            writer.resetOutput();
            if (size < best_size[mode]) {
                best_size[mode] = size;
                best_name[mode] = "Binary CABAC with CABAC mode " + mode;
            }
            if (printAllTests) {
                System.out.println("Binary CABAC with CABAC mode " + mode + ": " + size + " bytes");
            }
        }

        if (!containsNegatives && max_value <= 31) {
            writer.buildContextModels();
            start = writer.getNumberOfBytesWritten();
            for (int i = 0; i < values.length; i++) {
                writer.writeAsTrunc(values[i] & 0xff, max_value);
            }
            size = writer.getNumberOfBytesWritten() - start;
            writer.resetOutput();
            if (size < bestByPassSize) {
                bestByPassSize = size;
                bestByPassName = "TruncatedUnary of size " + max_value;
            }
            if (printAllTests) {
                System.out.println("TruncatedUnary of size " + max_value + ": " + size + " bytes");
            }

        } else {
            System.out.println("TruncatedUnary: 0 bytes");
        }

        if (!containsNegatives && max_value <= 31) {
            for (int mode = 0; mode < nr_Cabac_modes; mode++) {
                writer.buildContextModels();
                start = writer.getNumberOfBytesWritten();
                for (int i = 0; i < values.length; i++) {
                    writer.writeAsTruncCabac(values[i] & 0xff, max_value, offset);
                }
                size = writer.getNumberOfBytesWritten() - start;
                writer.resetOutput();
                if (size < best_size[mode]) {
                    best_size[mode] = size;
                    best_name[mode] = "TruncatedUnary CABAC of size " + max_value + " with CABAC mode " + mode;
                }
                if (printAllTests) {
                    System.out.println("TruncatedUnary CABAC of size " + max_value + " with CABAC mode " + mode + ": " + size + " bytes");
                }
            }
        } else {
            System.out.println("TruncatedUnary CABAC: 0 bytes");
        }
        if (!containsNegatives) {
            writer.buildContextModels();
            start = writer.getNumberOfBytesWritten();
            for (int i = 0; i < values.length; i++) {
                writer.writeAsExpGol(values[i] & 0xffffffff);
            }
            size = writer.getNumberOfBytesWritten() - start;
            writer.resetOutput();
            if (size < bestByPassSize) {
                bestByPassSize = size;
                bestByPassName = "Exponential Golomb";
            }
            if (printAllTests) {
                System.out.println("Exponential Golomb: " + size + " bytes");
            }
        } else {
            if (max_value < 16384 && min_value > -16383) {
                for (int mode = 0; mode < nr_Cabac_modes; mode++) {
                    writer.buildContextModels();
                    start = writer.getNumberOfBytesWritten();
                    for (int i = 0; i < values.length; i++) {
                        writer.writeAsSignedExpGol(values[i] & 0xffffffff);
                    }
                    size = writer.getNumberOfBytesWritten() - start;
                    writer.resetOutput();
                    if (size < best_size[mode]) {
                        bestByPassSize = size;
                        bestByPassName = "Signed Exponential Golomb";
                    }
                    if (printAllTests) {
                        System.out.println("Signed Exponential Golomb: " + size + " bytes");
                    }
                }
            }
        }

        if (!containsNegatives) {
            for (int mode = 0; mode < nr_Cabac_modes; mode++) {
                writer.buildContextModels();
                start = writer.getNumberOfBytesWritten();
                for (int i = 0; i < values.length; i++) {
                    writer.writeAsExpGolCabac(values[i] & 0xffffffff, offset);
                }
                size = writer.getNumberOfBytesWritten() - start;
                writer.resetOutput();
                if (size < best_size[mode]) {
                    best_size[mode] = size;
                    best_name[mode] = "Exponential Golomb CABAC with CABAC mode " + mode;
                }
                if (printAllTests) {
                    System.out.println("Exponential Golomb CABAC with CABAC mode " + mode + ": " + size + " bytes");
                }
            }
        } else {
            if (max_value < 16384 && min_value > -16383) {
                for (int mode = 0; mode < nr_Cabac_modes; mode++) {
                    writer.buildContextModels();
                    start = writer.getNumberOfBytesWritten();
                    for (int i = 0; i < values.length; i++) {
                        writer.writeAsSignedExpGolCabac(values[i] & 0xffffffff, offset);
                    }
                    size = writer.getNumberOfBytesWritten() - start;
                    writer.resetOutput();
                    if (size < best_size[mode]) {
                        best_size[mode] = size;
                        best_name[mode] = "Signed Exponential Golomb CABAC with CABAC mode " + mode;
                    }
                    if (printAllTests) {
                        System.out.println("Signed Exponential Golomb CABAC with CABAC mode " + mode + ": " + size + " bytes");
                    }
                }
            }
        }

        if (!containsNegatives) {
            long TruncExpGolSmallestSize = Long.MAX_VALUE;
            String TruncExpGolSmallestName = "";
            for (int treshold = 1; treshold < 32 && treshold < max_value; treshold ++) {
                writer.buildContextModels();
                start = writer.getNumberOfBytesWritten();
                for (int i = 0; i < values.length; i++) {
                    writer.writeAsTruncExpGol(values[i] & 0xffffffff, treshold);
                }
                size = writer.getNumberOfBytesWritten() - start;
                writer.resetOutput();
                if (size < bestByPassSize) {
                    bestByPassSize = size;
                    bestByPassName = "Truncated Exponential Golomb with treshold " + treshold;
                }
                if (printAllTests) {
                    if (size < TruncExpGolSmallestSize) {
                        TruncExpGolSmallestSize = size;
                        TruncExpGolSmallestName = "Truncated Exponential Golomb with treshold " + treshold;
                    }
                    if (printAllSubTests) {
                        System.out.println("Truncated Exponential Golomb with treshold " + treshold + " " + size + " bytes");
                    }
                }
            }
            System.out.println(TruncExpGolSmallestName + ": " + TruncExpGolSmallestSize + " bytes");
        } else {
            long SignedTruncExpGolSmallestSize = Long.MAX_VALUE;
            String SignedTruncExpGolSmallestName = "";
            for (int treshold = 0; treshold < 32 && treshold < max_value; treshold ++) {
                writer.buildContextModels();
                start = writer.getNumberOfBytesWritten();
                for (int i = 0; i < values.length; i++) {
                    writer.writeAsSignedTruncExpGol(values[i] & 0xffffffff, treshold);
                }
                size = writer.getNumberOfBytesWritten() - start;
                writer.resetOutput();
                if (size < bestByPassSize) {
                    bestByPassSize = size;
                    bestByPassName = "Signed Truncated Exponential Golomb with treshold " + treshold;
                }
                if (printAllTests) {
                    if (size < SignedTruncExpGolSmallestSize) {
                        SignedTruncExpGolSmallestSize = size;
                        SignedTruncExpGolSmallestName = "Signed Truncated Exponential Golomb with treshold " + treshold;
                    }
                    if (printAllSubTests) {
                        System.out.println("Signed Truncated Exponential Golomb with treshold " + treshold + " " + size + " bytes");
                    }
                }
            }
            System.out.println(SignedTruncExpGolSmallestName + ": " + SignedTruncExpGolSmallestSize + " bytes");
        }

        if (!containsNegatives) {
            long TruncExpGolSmallestSize = Long.MAX_VALUE;
            String TruncExpGolSmallestName = "";
            for (int mode = 0; mode < nr_Cabac_modes; mode++) {
                for (int treshold = 1; treshold < 32 && treshold < max_value; treshold ++) {
                    writer.buildContextModels();
                    start = writer.getNumberOfBytesWritten();
                    for (int i = 0; i < values.length; i++) {
                        writer.writeAsTruncExpGolCabac(values[i] & 0xffffffff, treshold, offset);
                    }
                    size = writer.getNumberOfBytesWritten() - start;
                    writer.resetOutput();
                    //System.out.println("Truncated Exponential Golomb with treshold " + treshold + ": " + size);
                    if (size < best_size[mode]) {
                        best_size[mode] = size;
                        best_name[mode] = "Truncated Exponential Golomb CABAC with treshold " + treshold + " with CABAC mode " + mode;
                        //      System.out.println("improvement with " + best_name[mode] + " size: " + best_size[mode]);
                    }
                    if (printAllTests) {
                        if (size < TruncExpGolSmallestSize) {
                            TruncExpGolSmallestSize = size;
                            TruncExpGolSmallestName = "Truncated Exponential Golomb CABAC with treshold " + treshold + " with CABAC mode " + mode;
                        }
                        if (printAllSubTests) {
                            System.out.println("Truncated Exponential Golomb CABAC with treshold " + treshold + " with CABAC mode " + mode + " " + size + " bytes");
                        }
                    }
                }
            }
            if (TruncExpGolSmallestSize == Long.MAX_VALUE) {
                TruncExpGolSmallestSize = 0;
            }
            System.out.println(TruncExpGolSmallestName + ": " + TruncExpGolSmallestSize + " bytes");
        } else {
            long SignedTruncExpGolSmallestSize = Long.MAX_VALUE;
            String SignedTruncExpGolSmallestName = "";
            for (int mode = 0; mode < nr_Cabac_modes; mode++) {
                for (int treshold = 0; treshold < 32 && treshold < max_value; treshold ++) {
                    writer.buildContextModels();
                    start = writer.getNumberOfBytesWritten();
                    for (int i = 0; i < values.length; i++) {
                        writer.writeAsSignedTruncExpGolCabac(values[i] & 0xffffffff, treshold, offset);
                    }
                    size = writer.getNumberOfBytesWritten() - start;
                    writer.resetOutput();
                    if (size < best_size[mode]) {
                        best_size[mode] = size;
                        best_name[mode] = "Signed Truncated Exponential Golomb CABAC with treshold " + treshold + " with CABAC mode " + mode;
                    }
                    if (printAllTests) {
                        if (size < SignedTruncExpGolSmallestSize) {
                            SignedTruncExpGolSmallestSize = size;
                            SignedTruncExpGolSmallestName = "Signed Truncated Exponential Golomb CABAC with treshold " + treshold + " with CABAC mode " + mode;
                        }
                        if (printAllSubTests) {
                            System.out.println("Signed Truncated Exponential Golomb CABAC with treshold " + treshold + " with CABAC mode " + mode + " " + size + " bytes");
                        }
                    }
                }
            }
            if (SignedTruncExpGolSmallestSize == Long.MAX_VALUE) {
                SignedTruncExpGolSmallestSize = 0;
            }
            System.out.println(SignedTruncExpGolSmallestName + ": " + SignedTruncExpGolSmallestSize + " bytes");

        }
        //TRUNCRICE
        if (!containsNegatives) {
            long TruncRiceSmallestSize = Long.MAX_VALUE;
            String TruncRiceSmallestName = "";
            for (int trailingBits = 1; trailingBits < Integer.toBinaryString(max_value).length() && (max_value / Math.pow(2, trailingBits)) < 32; trailingBits++) {
                //select optimal trailingBits                             
                writer.buildContextModels();
                start = writer.getNumberOfBytesWritten();
                for (int i = 0; i < values.length; i++) {
                    writer.writeAsTruncRice(values[i] & 0xffffffff, max_value, trailingBits);
                }
                size = writer.getNumberOfBytesWritten() - start;
                writer.resetOutput();
                if (size < bestByPassSize) {
                    bestByPassSize = size;
                    bestByPassName = "Truncated Rice with cMax " + max_value + " and " + trailingBits + " trailingbits";
                }
                if (printAllTests) {
                    if (size < TruncRiceSmallestSize) {
                        TruncRiceSmallestSize = size;
                        TruncRiceSmallestName = "Truncated Rice with cMax " + max_value + " and " + trailingBits + " trailingbits";
                    }
                    if (printAllSubTests) {
                        System.out.println("Truncated Rice with cMax " + max_value + " and " + trailingBits + " trailingbits" + " " + size + " bytes");
                    }
                }
            }
            if (TruncRiceSmallestSize != Long.MAX_VALUE) {
                System.out.println(TruncRiceSmallestName + ": " + TruncRiceSmallestSize + " bytes");
            }
        }

        //TRUNCRICE
        if (!containsNegatives) {
            long TruncRiceSmallestSize = Long.MAX_VALUE;
            String TruncRiceSmallestName = "";
            for (int mode = 0; mode < nr_Cabac_modes; mode++) {
                for (int trailingBits = 1; trailingBits < Integer.toBinaryString(max_value).length() && max_value / Math.pow(2, trailingBits) < 32; trailingBits++) {
                    //select optimal trailingBits                             
                    writer.buildContextModels();
                    start = writer.getNumberOfBytesWritten();
                    for (int i = 0; i < values.length; i++) {
                        writer.writeAsTruncRiceCabac(values[i] & 0xffffffff, max_value, trailingBits, offset, mode);
                    }
                    size = writer.getNumberOfBytesWritten() - start;
                    writer.resetOutput();
                    if (size < best_size[mode]) {
                        best_size[mode] = size;
                        best_name[mode] = "Truncated Rice CABAC with cMax " + max_value + " and " + trailingBits + " trailingbits with CABAC mode " + mode;
                    }
                    if (printAllTests) {
                        if (size < TruncRiceSmallestSize) {
                            TruncRiceSmallestSize = size;
                            TruncRiceSmallestName = "Truncated Rice CABAC with cMax " + max_value + " and " + trailingBits + " trailingbits with CABAC mode " + mode;
                        }
                        if (printAllSubTests) {
                            System.out.println("Truncated Rice CABAC with cMax " + max_value + " and " + trailingBits + " trailingbits with CABAC mode " + mode + " " + size + " bytes");
                        }
                    }
                }
            }
            if (TruncRiceSmallestSize != Long.MAX_VALUE) {
                System.out.println(TruncRiceSmallestName + ": " + TruncRiceSmallestSize + " bytes");
            }
        }

        if (!containsNegatives && max_value == 1) {
            writer.buildContextModels();
            start = writer.getNumberOfBytesWritten();
            for (int i = 0; i < values.length; i++) {
                writer.writeAsFlag(values[i]);
            }
            size = writer.getNumberOfBytesWritten() - start;
            writer.resetOutput();
            if (size < bestByPassSize) {
                bestByPassSize = size;
                bestByPassName = "Flag";
            }
            if (printAllTests) {
                System.out.println("Flag: " + size);
            }

        }

        if (!containsNegatives && max_value == 1) {
            for (int mode = 0; mode < nr_Cabac_modes; mode++) {
                writer.buildContextModels();
                start = writer.getNumberOfBytesWritten();
                for (int i = 0; i < values.length; i++) {
                    writer.writeAsFlagCabac(values[i], offset);
                }
                size = writer.getNumberOfBytesWritten() - start;
                writer.resetOutput();
                if (size < best_size[mode]) {
                    best_size[mode] = size;
                    best_name[mode] = "Flag CABAC with CABAC mode " + mode;
                }
                if (printAllTests) {
                    System.out.println("Flag CABAC with CABAC mode " + mode + ": " + size);
                }
            }
        }

        for (int mode = 0; mode < nr_Cabac_modes; mode++) {
            System.out.println(
                    "Best compression: \t" + best_name[mode] + " with size " + best_size[mode] + " which is \t" + (Math.round((double) best_size[mode] / bestByPassSize
                            * 100)) + "% of the best bypass representation");
        }

        System.out.println(
                "Best Bypass compression: \t" + bestByPassName + " with size " + bestByPassSize + " Bytes");

    }

    private Map<Integer, Long> sortOccurrences(int[] values) {

        Map<Integer, Long> occur = new HashMap<>();

        for (int value : values) {
            if (occur.containsKey(value)) {
                occur.put(value, occur.get(value) + 1);
            } else {
                occur.put(value, (long) 1);
            }
        }
        //  ValueComparator<Integer, Long> comparator = new ValueComparator<Integer, Long>(occur);
        ValueComparator comparator = new ValueComparator(occur);
        Map<Integer, Long> sortedMap = new TreeMap<Integer, Long>(comparator) {
        };
        sortedMap.putAll(occur);

        return sortedMap;

    }

    class ValueComparator implements Comparator<Object> {
        //source: http://stackoverflow.com/questions/9437120/key-in-treemap-returning-null

        Map<Integer, Long> base;

        public ValueComparator(Map<Integer, Long> base) {
            this.base = base;
        }

        public int compare(Object a, Object b) {

            if (((Long) base.get(a)).intValue() < ((Long) base.get(b)).intValue()) {
                return 1;
            } else if (((Long) base.get(a)).intValue() == ((Long) base.get(b)).intValue()) {
                return ((Integer) a).compareTo(((Integer) b));
            } else {
                return -1;
            }
        }
    }

    private double generateEntropy(int[] in) {
        long[][] counters = generateHistogram(in);
        double probability;
        double entropy = 0;
        for (long[] counter : counters) {
            probability = (double) counter[1] / in.length;
            entropy -= probability * (Math.log(probability) / Math.log(2));
        }
        System.out.println("Entropy: " + ((double) Math.round(entropy * 10000) / 10000) + " bits/value. Optimal size: " + (int) Math.round(entropy * in.length / 8));
        counters = null;
        generateEntropyJavaMI(in);
        return entropy;
    }

    private void generateEntropyJavaMI(int[] in) {
        double[] full = new double[in.length];
        for (int i = 0; i < full.length; i++) {
            full[i] = 1.0 * in[i];
        }
        System.out.println("Size: "+full.length+" values.");
        double entropy = Entropy.calculateEntropy(full);
        System.out.println("Entropy: " + entropy + " bits/value. Optimal size: " + (int) Math.round(entropy * in.length / 8));
        full = null;

        double[] x = new double[in.length - 1];
        double[] y = new double[in.length - 1];
        for (int i = 0; i < x.length - 1; i++) {
            x[i] = 1.0 * in[i];
            y[i] = 1.0 * in[i + 1];
        }
        double conditionalEntropy = Entropy.calculateConditionalEntropy(x, y);
        x=null;
        y=null;
        System.out.println("Conditional Entropy: " + conditionalEntropy + " bits/value");

    }

    private double generateEntropy(byte[] in, int length) {
        long[][] counters = generateHistogram(in, length);
        double probability;
        double entropy = 0;
        for (long[] counter : counters) {
            probability = (double) counter[1] / in.length;
            entropy -= probability * (Math.log(probability) / Math.log(2));
        }
        System.out.println("Entropy: " + ((double) Math.round(entropy * 10000) / 10000) + " bits/value. Expected size: "+Math.round((entropy/8*in.length))+" for "+in.length+" values");
        counters = null;
//        generateEntropyJavaMI(in,length);   
        
        return entropy;
    }

    private void generateEntropyJavaMI(byte[] in, int length) {
        int[] values = new int[in.length / length];
        int offset = 0;
        for (int i = 0; i < values.length; i++) {
            offset=i*length;
            if (length == 1) {
                values[i] = in[offset] & 0xff;
            } else if (length == 2) {
                values[i] = ((in[offset + 1] & 0x000000ff) << 8) + (in[offset] & 0x000000ff);
            } else if (length == 4) {
                values[i] = ((in[offset + 3] & 0x000000ff) << 24) + ((in[offset + 2] & 0x000000ff) << 16) + ((in[offset + 1] & 0x000000ff) << 8) + (in[offset] & 0x000000ff);
            }
        }
        generateEntropyJavaMI(values);
    }



private void printHistogram(byte[] in, int length) {
        if (!printHistograms) {
            return;
        }
        System.out.println("*******************");
        int[] values = new int[in.length / length];

        for (int i = 0; i < in.length - length + 1; i += length) {
            if (length == 1) {
                values[i / length] = in[i] & 0xff;
            } else if (length == 2) {
                values[i / length] = ((in[i + 1] & 0x000000ff) << 8) + (in[i] & 0x000000ff);
            } else if (length == 4) {
                values[i / length] = ((in[i + 3] & 0x000000ff) << 24) + ((in[i + 2] & 0x000000ff) << 16) + ((in[i + 1] & 0x000000ff) << 8) + (in[i] & 0x000000ff);
            }
        }

        printTransformIntHistogram(values);

//        long[][] histogram = generateHistogram(in, length);
//        int sum = 0;
//        if (histogram != null) {
//            for (long[] histogram1 : histogram) {
//                sum += histogram1[1];
//            }
//            for (long[] histogram1 : histogram) {
//                System.out.println(histogram1[0] + ": " + histogram1[1] + "\t" + 1.0 * Math.round(10000.0 * histogram1[1] / sum) / 100 + "%");
//            }
//        }
        System.out.println("*******************");
    }

    private void printHistogram(int[] in) {
        if (!printHistograms) {
            return;
        }

        System.out.println("*******************");
        printTransformIntHistogram(in);
//        long[][] histogram = generateHistogram(in);
//        int sum = 0;
//        if (histogram != null) {
//            for (long[] histogram1 : histogram) {
//                sum += histogram1[1];
//            }
//            for (long[] histogram1 : histogram) {
//                System.out.println(histogram1[0] + ": " + histogram1[1] + "\t" + 1.0 * Math.round(10000.0 * histogram1[1] / sum) / 100 + "%");
//            }
//        }
        System.out.println("*******************");
    }

    private void printConditionalHistogramInt(int[] in, int cardinality) {
        if (!printHistograms) {
            return;
        }
        System.out.println("*******************");
        int[][] histogram = generateConditionalHistogramInt(in, cardinality);
        int sum;
        if (histogram != null) {
            for (int i = 0; i < histogram.length && i<10; i++) {
                sum = 0;
                for (int j = 0; j < histogram[0].length; j++) {
                    sum += histogram[i][j];
                }
                //to avoid printing of histograms that don't contain significant information
                if (sum > cardinality * 10) {
                    System.out.println("Previous was " + i);
                    for (int j = 0; j < histogram[0].length; j++) {
                        if ((1.0 * Math.round(10000.0 * histogram[i][j] / sum) / 100) > 5 || (j <= 20 && histogram[i][j] != 0)) {

                            String text = String.format("%8s", j + ": ");
                            String text2 = String.format("%12s", +histogram[i][j]);
                            String text3 = String.format("%8s", 1.0 * Math.round(10000.0 * histogram[i][j] / sum) / 100 + "%");
                            System.out.println(text + text2 + text3);

//                            System.out.println(j + ": " + histogram[i][j] + "\t" + 1.0 * Math.round(10000.0 * histogram[i][j] / sum) / 100 + "%");
                        }
                    }
                }
            }
        }
        System.out.println("*******************");
    }

    private void printConditionalHistogram(byte[] in, int cardinality) {
        if (!printHistograms) {
            return;
        }
        System.out.println("*******************");
        int[][] histogram = generateConditionalHistogram(in, cardinality);
        int sum;
        if (histogram != null) {
            for (int i = 0; i < histogram.length && i < 10; i++) {
                sum = 0;
                for (int j = 0; j < histogram[0].length; j++) {
                    sum += histogram[i][j];
                }
                if (sum > cardinality * 10) {
                    System.out.println("Previous was " + i);
                    for (int j = 0; j < histogram[0].length; j++) {
                        if ((1.0 * Math.round(10000.0 * histogram[i][j] / sum) / 100) > 5 || (j <= 20 && histogram[i][j] != 0)) {

                            String text = String.format("%8s", j + ": ");
                            String text2 = String.format("%12s", +histogram[i][j]);
                            String text3 = String.format("%8s", 1.0 * Math.round(10000.0 * histogram[i][j] / sum) / 100 + "%");
                            System.out.println(text + text2 + text3);
//                System.out.println(j + ": " + histogram[i][j] + "\t" + 1.0 * Math.round(10000.0 * histogram[i][j] / sum) / 100 + "%");
                        }
                    }
                }
            }
        }
        System.out.println("*******************");
    }

    private long[][] generateHistogram(byte[] in, int length) {
        Map<Long, Integer> histogram = new HashMap<>();
        long value = 0;
        for (int i = 0; i < in.length - length + 1; i += length) {
            if (length == 1) {
                value = in[i] & 0xff;
            } else if (length == 2) {
                value = ((in[i + 1] & 0x000000ff) << 8) + (in[i] & 0x000000ff);
            } else if (length == 4) {
                value = ((in[i + 3] & 0x000000ff) << 24) + ((in[i + 2] & 0x000000ff) << 16) + ((in[i + 1] & 0x000000ff) << 8) + (in[i] & 0x000000ff);
            }
            if (histogram.containsKey(value)) {
                histogram.put(value, histogram.get(value) + 1);
            } else {
                histogram.put(value, 1);
            }
        }
        long[][] counters = new long[histogram.size()][2];
        int i = 0;
        for (Long key : histogram.keySet()) {
            counters[i][0] = key;
            counters[i][1] = histogram.get(key);
            i++;
        }
        return counters;
    }

    private long[][] generateHistogram(int[] in) {
        Map<Long, Integer> histogram = new HashMap<>();
        long value;
        for (int i = 0; i < in.length; i++) {

            value = in[i] & 0xffffffff;

            if (histogram.containsKey(value)) {
                histogram.put(value, histogram.get(value) + 1);
            } else {
                histogram.put(value, 1);
            }
        }
        long[][] counters = new long[histogram.size()][2];
        int i = 0;
        for (Long key : histogram.keySet()) {
            counters[i][0] = key;
            counters[i][1] = histogram.get(key);
            i++;
        }
        return counters;
    }

    private int[][] generateConditionalHistogramInt(int[] in, int cardinality) {

        int[][] counters = new int[cardinality][32768];

        for (int i = 0; i < counters.length; i++) {
            for (int j = 0; j < cardinality; j++) {
                counters[j][i] = 0;
            }
        }
        counters[0][(in[0] & 0xff)]++;
        for (int i = 1; i < in.length; i += 1) {
            counters[in[i - 1] & 0xff][(in[i] & 0xff)]++;
        }
        return counters;
    }

    private int[][] generateConditionalHistogram(byte[] in, int cardinality) {

        int[][] counters = new int[Math.max(cardinality, 256)][Math.max(cardinality, 256)];

        for (int i = 0; i < counters.length; i++) {
            for (int j = 0; j < cardinality; j++) {
                counters[j][i] = 0;
            }
        }
        counters[0][(in[0] & 0xff)]++;
        for (int i = 1; i < in.length; i += 1) {
            counters[in[i - 1] & 0xff][(in[i] & 0xff)]++;
        }
        return counters;
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

    public void processByteList(ArrayList<Byte> list, int width) {
        if (list != null && list.size() != 0) {
            System.out.println("*****processing byte list******");
            byte[] array = ListToArrayByte(list);
            list.clear();
            printHistogram(array, width);
            generateEntropy(array, width);
//            System.out.println("--------Non-transform----------");
            testAllCodingsByte(array, 0);
//            System.out.println("-------Analysis-------");
//            HashMap<Long, Long> transformMap = generateTransform(array);
//            for (int i = 0; i < array.length; i++) {
//                array[i] = transformMap.get((long) array[i]).byteValue();
//            }
//            int[] RLE = calculateRLETable(array);
//            printRLECost(RLE, transformMap.size());
//            printHistogram(array, width);
            int maxVal = 0;
            for (int i = 0; i < array.length; i++) {
                if (array[i] > maxVal) {
                    maxVal = array[i];
                }
            }
//            printConditionalHistogram(array, maxVal + 1);
//            System.out.println("-------Transform !!without tablesignalling-------");
//            testAllCodingsByte(array, 0);
            System.out.println("*****processing byte list END******");
        }
    }

    public void processByteListConvertNegatives(ArrayList<Byte> list, int width) {
        if (list != null && list.size() != 0) {
            System.out.println("*****processing byte list******");
            byte[] array = ListToArrayByte(list);
            list.clear();
            printHistogram(array, width);
            generateEntropy(array, width);
            System.out.println("--------Non-transform----------");
            testAllCodingsByteConvertNegatives(array, 0);
            System.out.println("-------Analysis-------");
            HashMap<Long, Long> transformMap = generateTransformConvertNegatives(array);
            for (int i = 0; i < array.length; i++) {
                array[i] = transformMap.get((long) array[i] & 0xffff).byteValue();
            }
            int[] RLE = calculateRLETable(array);
            printRLECost(RLE, transformMap.size());
            printHistogram(array, width);
            int maxVal = 0;
            for (int i = 0; i < array.length; i++) {
                if (array[i] > maxVal) {
                    maxVal = array[i];
                }
            }
            printConditionalHistogram(array, maxVal + 1);
            System.out.println("-------Transform !!without tablesignalling-------");
            testAllCodingsByteConvertNegatives(array, 0);
            System.out.println("*****processing byte list END******");
        }
    }

    public void processByteListConditional(ArrayList<Byte> list, int width, int cardinality) {
        if (list != null && list.size() != 0) {
            System.out.println("*****processing conditional byte list******");
            byte[] array = ListToArrayByte(list);
            list.clear();
            printHistogram(array, width);
            printConditionalHistogram(array, cardinality);
            generateEntropy(array, width);
            System.out.println("--------Non-transform----------");
            testAllCodingsByte(array, 0);
            System.out.println("-------Analysis-------");
            HashMap<Long, Long> transformMap = generateTransform(array);
            for (int i = 0; i < array.length; i++) {
                array[i] = transformMap.get((long) array[i]).byteValue();
            }
            int[] RLE = calculateRLETable(array);
            printRLECost(RLE, transformMap.size());
            printHistogram(array, width);
            System.out.println("-------Transform !!without tablesignalling-------");
            testAllCodingsByte(array, 0);
            System.out.println("*****processing conditional byte list END******");
        }
    }

    public void processIntList(ArrayList<Integer> list) {
        if (list != null && list.size() != 0) {
            System.out.println("*****processing Int list******");
            int[] array = ListToArrayInt(list);
            list.clear();
            printHistogram(array);
            generateEntropy(array);
            System.out.println("--------Non-transform----------");
            testAllCodingsInt(array, 0);
            System.out.println("--------Analysis----------");
            HashMap<Long, Long> transformMap = generateTransformInt(array);
            for (int i = 0; i < array.length; i++) {
                array[i] = transformMap.get((long) array[i]).intValue();
            }
            int[] RLE = calculateRLETable(array);
            printRLECost(RLE, transformMap.size());
            printHistogram(array);
            int maxVal = 0;
            for (int i = 0; i < array.length; i++) {
                if (array[i] > maxVal) {
                    maxVal = array[i];
                }
            }
            if (maxVal <= 255) {
                printConditionalHistogramInt(array, maxVal + 1);
            }
            System.out.println("--------Transform !!without tablesignalling----------");
            testAllCodingsInt(array, 0);
            System.out.println("*****processing Int list END******");
        }
    }

    public HashMap<Long, Long> generateTransform(byte[] input) {
        int[] values = new int[input.length];
        for (int i = 0; i < input.length; i++) {
            values[i] = input[i];
        }
        return generateTransformInt(values);
    }

    public HashMap<Long, Long> generateTransformConvertNegatives(byte[] input) {
        int[] values = new int[input.length];
        for (int i = 0; i < input.length; i++) {
            values[i] = input[i] & 0xffff;
        }
        return generateTransformInt(values);
    }

    public HashMap<Long, Long> generateTransformInt(int[] input) {
        HashMap<Long, Long> _transform = new HashMap<>();
        Map<Integer, Long> map;
        map = sortOccurrences(input);
        Iterator<Integer> itr = map.keySet().iterator();
        long i = 0;
        while (itr.hasNext()) {
            Integer key = itr.next();
            _transform.put(new Long(key), i++);
        }
        return _transform;
    }

    public void printTransformIntHistogram(int[] input) {
        Map<Integer, Long> map;
        map = sortOccurrences(input);
        Iterator<Integer> itr = map.keySet().iterator();
        boolean first=true;
        while (itr.hasNext()) {
            Integer key = itr.next();
            Long value = map.get(key);
            if (1.0 * Math.round(10000.0 * value / input.length) / 100 < 0.5 && map.size() > 20 && !first) {
                System.out.println("rest of the " + map.size() + " (total_size) values have a lower occurrence than 0.5%");                
                break;
            } else {
                String text = String.format("%8s", key + ": ");
                String text2 = String.format("%8s", +1.0 * Math.round(10000.0 * value / input.length) / 100 + "%");
                String text3 = String.format("%32s", Integer.toBinaryString(key));
                first=false;
                System.out.println(text + text2 + text3);
            }

        }
    }

    public void printTransformHistogram(byte[] input) {
        Map<Integer, Long> map;
        int[] values = new int[input.length];
        for (int i = 0; i < input.length; i++) {
            values[i] = input[i];
        }
        map = sortOccurrences(values);
        Iterator<Integer> itr = map.keySet().iterator();
        while (itr.hasNext()) {
            Integer key = itr.next();
            Long value = map.get(key);
            String text = String.format("%8s", key + ": ");
            String text2 = String.format("%8s", +1.0 * Math.round(10000.0 * value / input.length) / 100 + "%");
            String text3 = String.format("%32s", Integer.toBinaryString(key));
            System.out.println(text + text2 + text3);
            if (1.0 * Math.round(10000.0 * value / input.length) / 100 < 0.1) {
                System.out.println("rest of the values have a lower occurrence than 0.1%");
                break;
            }

        }
    }

    public int[] calculateRLETable(byte[] input) {
        int[] output = new int[input.length];
        for (int i = 0; i < output.length; i++) {
            output[i] = input[i];
        }
        return calculateRLETable(output);
    }

    public int[] calculateRLETable(int[] input) {
        int[] lengthcounter = new int[65535];
        for (int i = 0; i < lengthcounter.length; i++) {
            lengthcounter[i] = 0;
        }
        int prev = input[0];
        int counter = 1;
        for (int i = 1; i < input.length; i++) {
            while (i < input.length && prev == input[i] && counter < lengthcounter.length) {
                counter++;
                i++;
            }
            lengthcounter[counter - 1]++;
            counter = 1;
            prev = input[i - 1];

        }
        return lengthcounter;
    }

    public void printRLECost(int[] RLE, int nrOfDiffValues) {
        long[] cost = new long[3];
        for (int i = 0; i < cost.length; i++) {
            cost[i] = 0;
        }
        int max = 0;
        for (int i = 0; i < RLE.length; i++) {
            if (RLE[i] > max) {
                max = RLE[i];
            }
        }
        int cost_per_length = Integer.toBinaryString(max).length();
        int value_cost = Integer.toBinaryString(nrOfDiffValues).length();
        System.out.println("RLE---------------->");
        System.out.println("Length: #occurrences");
        for (int i = 0; i < RLE.length; i++) {
            if (i < 10 || i == RLE.length - 1) {
                if (RLE[i] != 0) {
                    System.out.println((i + 1) + ": " + RLE[i]);
                    cost[0] += (value_cost + cost_per_length) * RLE[i];
                    cost[1] += (value_cost + Binarisation.binariseExpGolomb(i).getNumbins()) * RLE[i];//hier i omdat 0 toch niet wordt gesignaleerd, dus gewoon length-1;
                }
            }
        }
        System.out.println("cost binary: " + cost[0] / 8 + " bytes \n cost ExpGol: " + cost[1] / 8 + " bytes");
        System.out.println("<----------------RLE");

    }

}
