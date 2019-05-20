import arithmeticcoding.Binarisation;
import decoding.Decoder;
import decoding.Reader;
import encoding.Encoder;
import encoding.Writer;
import java.util.ArrayList;

public class DNACompression {

    private String[] _inputFile;
    private final String _mode;
    private int offset = 0;
    private boolean all = true;
    private boolean len = false;
    private boolean pairs = false;
    private boolean pos = false;
    private boolean rcomp = false;
    private boolean tfl = false;
    private boolean snp = false;
    private boolean nmis = true;
    private boolean rtype = false;
    private boolean subtype = false;
    private boolean rft = false;
    private boolean ind = false;
    private boolean ureads = false;
    private boolean qual = false;
    private boolean headers = false;
    private boolean allSubs = false;
    private boolean g = false;
    private boolean p = true;
    private boolean t = false;
    private boolean m = false;
    private boolean n = false;
    private boolean c = false;
    private boolean rest = true;
    private String _speed;
    private int nrOfRuns = 1;

    public DNACompression(String mode, String input, String speed, int nrOfRuns) {
        System.out.println("v2.3.5.1");

//        input_path = input;
        _mode = mode;
        _speed = speed;
        this.nrOfRuns = nrOfRuns;
        if ((input.charAt(input.length() - 1) == '/') || input.charAt(input.length() - 1) == '\\') {
            ArrayList<String> testList = new ArrayList();
            testList.add(input + "02_NA12878_S1/NA12878_S1.bam_filtered");
            testList.add(input + "02_NA12878_S1-Unmapped/NA12878_S1.unmapped.bam_filtered");
            testList.add(input + "03_NA12878.pacbio.bwa-sw.20140202.bam/NA12878.pacbio.bwa-sw.20140202_chr1.bam_filtered");
            testList.add(input + "05_9827_2#49/9827_2#49.bam_filtered");
            testList.add(input + "05_9827_2#49-Unmapped/9827_2#49.unmapped.bam_filtered");
            testList.add(input + "07_ERR174310/chr1_g.gtl_filtered");
            testList.add(input + "07_ERR174310/chr1_m.gtl_filtered");
            testList.add(input + "07_ERR174310/chr1_N.gtl_filtered");
            testList.add(input + "07_ERR174310/chr1_p.gtl_filtered");
            testList.add(input + "07_ERR174310/chr2_g.gtl_filtered");
            testList.add(input + "07_ERR174310/chr2_m.gtl_filtered");
            testList.add(input + "07_ERR174310/chr2_N.gtl_filtered");
            testList.add(input + "07_ERR174310/chr2_p.gtl_filtered");
            testList.add(input + "07_ERR174310-Unmapped/ERP174310-unmapped.bam_filtered");
            testList.add(input + "08_PacBio_m1310NN/m131003.sorted.bam_filtered");
            testList.add(input + "08_PacBio_m1310NN/m131004-10.sorted.bam_filtered");
            testList.add(input + "09_IonTorrent_sample_2-10_sorted.bam/sample-2-10_sorted.bam_filtered");
            testList.add(input + "10_K562_cytosol_LID8465_TopHat_v2.bam/K562_cytosol_LID8465_TopHat_v2.bam_filtered");
            testList.add(input + "20_Metagenomics/MH0001_081026_clean_1.bam_filtered");
            testList.add(input + "20_Metagenomics/MH0001_081026_clean_2.bam_filtered");
            testList.add(input + "20_Metagenomics/MH0002_081203_clear_1.bam_filtered");
            testList.add(input + "20_Metagenomics/MH0002_081203_clear_2.bam_filtered");
            testList.add(input + "20_Metagenomics/MH0003_081203_clean_1.bam_filtered");
            testList.add(input + "20_Metagenomics/MH0003_081203_clean_2.bam_filtered");
////////////        
////////////        //Merged
            testList.add(input + "splitted_ureads/9827_2#49_1.0_524288_resorted.log.merged");
            testList.add(input + "splitted_ureads/ERP174310_1.0_524288_resorted.log.merged");
            testList.add(input + "splitted_ureads/MH0001_1_1.0_524288_resorted.log.merged");
            testList.add(input + "splitted_ureads/MH0001_2_1.0_524288_resorted.log.merged");
            testList.add(input + "splitted_ureads/MH0002_1_1.0_524288_resorted.log.merged");
            testList.add(input + "splitted_ureads/MH0002_2_1.0_524288_resorted.log.merged");
            testList.add(input + "splitted_ureads/MH0003_1_1.0_524288_resorted.log.merged");
            testList.add(input + "splitted_ureads/MH0003_2_1.0_524288_resorted.log.merged");
            testList.add(input + "splitted_ureads/NA12878_S1_1.0_524288_resorted.log.merged");
            _inputFile = new String[testList.size()];
            _inputFile = testList.toArray(_inputFile);
        } else {
            _inputFile = new String[]{input};
        }
    }

    private void start() {
//        testDecodingSpeed();

        switch (_mode) {
            case "ENCODE":
                encode();
                break;
            case "DECODE":
                decode();
                break;
//            case "ANALYZE":
//                analyze();
//                break;
            default:
                System.err.println("invalid mode");
                break;
        }
    }

    private void encode() {
//        testDecodingSpeed();
        boolean decode = true;
        if (all) {
            allSubs = true;
        }
        double[] durations = new double[nrOfRuns];//, 0, 0, 0, 0, 0};
        for (int i = 0; i < durations.length; i++) {
            durations[i] = 0;
        }

        if (_speed.toLowerCase().contains("nodec")) {
            decode = false;
        }

        Decoder decoder = new Decoder(_inputFile);
        Encoder encoder;
        long start = 0;

//        encoder.startEncoding(".headers");
        for (int i = 0; i < durations.length; i++) {
            start = System.currentTimeMillis();
            encoder = new Encoder(_inputFile, _speed);
            if (decode) {
                decoder = new Decoder(_inputFile);
            }
            if (_inputFile[0].substring(_inputFile[0].length() - 8, _inputFile[0].length() - 3).contains(".")) {
                encoder.startEncoding("");
                if (decode) {
                    if (i % nrOfRuns == 0) {
                        decoder.set2ndPass(false);
                    } else {
                        decoder.set2ndPass(true);
                    }
                    decoder.startDecoding("");
                }
            } else {
                if (decode) {
                    if (i % nrOfRuns == 0) {
                        decoder.set2ndPass(false);
                    } else {
                        decoder.set2ndPass(true);
                    }
                }
//                if (i < nrOfRuns) {
////                encoder.setUltraFastMode();
//                    encoder.setFastMode();
//                } else if (i < 2 * nrOfRuns) {
////                encoder.setFastMode();
//                    encoder.setUltraMode();
//                } else if (i < 15) {
////                encoder.setUltraMode();
//                }

                if (all || headers) {
//                encoder.startEncoding(".headers.char");//fullytested
//                encoder.startEncoding(".headers.ddelta");//;inprogress
//                    
////                encoder.startEncoding(".headers.digits");
//                encoder.startEncoding(".headers.digits.byte0");
//                encoder.startEncoding(".headers.digits.byte1");
//                encoder.startEncoding(".headers.digits.byte2");
//                encoder.startEncoding(".headers.digits.byte3");
//                encoder.startEncoding(".headers.digits0length");//fullytested
//                encoder.startEncoding(".headers.digits0value");//fullytested
//                encoder.startEncoding(".headers.diff");//fullytested
//                encoder.startEncoding(".headers.dup");//fullytested
//
//                encoder.startEncoding(".headers.string");//fullytested
//                encoder.startEncoding(".headers.token");//fullytested
////                decoder.startDecoding(".headers.token");//fullytested
//                encoder.startEncoding(".headers.ddelta0");
                }

                if (all || pairs) {
                    if (g || allSubs) {

                        encoder.startEncoding(".gpair");//OK
//                        encoder.startEncoding(".gpair.descriptor");
//                        encoder.startEncoding(".gpair.shorts");
//                        encoder.startEncoding(".gpair.refIDs");
                        if (decode) {
                            decoder.startDecoding(".gpair");
                        }
                    }
                    if (m || allSubs) {

                        encoder.startEncoding(".mpair");//OK
//                        encoder.startEncoding(".mpair.descriptor");
//                        encoder.startEncoding(".mpair.shorts");
//                        encoder.startEncoding(".mpair.refIDs");
                        if (decode) {
                            decoder.startDecoding(".mpair");
                        }
                    }
                    if (n || allSubs) {

                        encoder.startEncoding(".npair");//OK
//                        encoder.startEncoding(".npair.descriptor");
//                        encoder.startEncoding(".npair.shorts");
//                        encoder.startEncoding(".npair.refIDs");
                        if (decode) {
                            decoder.startDecoding(".npair");
                        }

                    }
                    if (rest || allSubs) {

                        encoder.startEncoding(".pair");//OK
//                        encoder.startEncoding(".pair.descriptor");
//                        encoder.startEncoding(".pair.shorts");
//                        encoder.startEncoding(".pair.refIDs");
                        if (decode) {
                            decoder.startDecoding(".pair");
                        }
                    }
                }
                try {
                    //System.out.println("pause for 20 seconds");
                    //Thread.sleep(1000 * 20);
                } catch (Exception e) {
                }

                if (all || pos) {
                    if (g || allSubs) {

                        encoder.startEncoding(".gpos");//OK

                        if (decode) {
                            decoder.startDecoding(".gpos");
                        }
                    }
                    if (m || allSubs) {

                        encoder.startEncoding(".mpos");//OK

                        if (decode) {
                            decoder.startDecoding(".mpos");
                        }
                    }
                    if (n || allSubs) {

                        encoder.startEncoding(".npos");//OK
                        if (decode) {
                            decoder.startDecoding(".npos");
                        }
                    }
                    if (rest || allSubs) {

                        encoder.startEncoding(".pos");//OK

                        if (decode) {
                            decoder.startDecoding(".pos");
                        }
                    }
                }
                try {
                    //System.out.println("pause for 20 seconds");
                    //Thread.sleep(1000 * 20);
                } catch (Exception e) {
                }
                if (all || rcomp) {
                    if (g || allSubs) {

                        encoder.startEncoding(".grcomp");//OK
                        if (decode) {
                            decoder.startDecoding(".grcomp");
                        }
                    }
                    if (m || allSubs) {

                        encoder.startEncoding(".mrcomp");//OK
                        if (decode) {
                            decoder.startDecoding(".mrcomp");
                        }
                    }
                    if (n || allSubs) {

                        encoder.startEncoding(".nrcomp");//OK
                        if (decode) {
                            decoder.startDecoding(".nrcomp");
                        }
                    }
                    if (rest || allSubs) {

                        encoder.startEncoding(".rcomp");//OK
                        if (decode) {
                            decoder.startDecoding(".rcomp");
                        }
                    }
                }
                try {
                    //System.out.println("pause for 20 seconds");
                    //Thread.sleep(1000 * 20);
                } catch (Exception e) {
                }

                if (all || snp) {
                    if (p || allSubs) {

                        encoder.startEncoding(".snpp");//OK
                        if (decode) {
                            decoder.startDecoding(".snpp");
                        }
                    }
                    if (t || allSubs) {

                        encoder.startEncoding(".snpt");//OK  
                        if (decode) {
                            decoder.startDecoding(".snpt");
                        }
                    }
                }
                if (all || ind) {

                    if (c || allSubs) {

                        encoder.startEncoding(".indc");
                        if (decode) {
                            decoder.startDecoding(".indc");
                        }
                    }
                    if (p || allSubs) {

                        encoder.startEncoding(".indp");
                        if (decode) {
                            decoder.startDecoding(".indp");
                        }
                    }
                    if (t || allSubs) {

                        encoder.startEncoding(".indt");
                        if (decode) {
                            decoder.startDecoding(".indt");
                        }
                    }
                }
                try {
                    //System.out.println("pause for 20 seconds");
                    //Thread.sleep(1000 * 20);
                } catch (Exception e) {
                }

                if (all || nmis) {

                    encoder.startEncoding(".nmis");//OK
                    if (decode) {
                        decoder.startDecoding(".nmis");
                    }
                }
                if (all || rtype) {

                    encoder.startEncoding(".rtype");
                    if (decode) {
                        decoder.startDecoding(".rtype");
                    }

                }
                if (all || subtype) {

                    encoder.startEncoding(".subtype");
                    if (decode) {
                        decoder.startDecoding(".subtype");
                    }
                }
                try {
                    //System.out.println("pause for 20 seconds");
                    //Thread.sleep(1000 * 20);
                } catch (Exception e) {
                }
                if (all || ureads) {

                    encoder.startEncoding(".ureads");
                    if (decode) {
                        decoder.startDecoding(".ureads");
                    }
                }
                try {
                    //System.out.println("pause for 20 seconds");
                    //Thread.sleep(1000 * 20);
                } catch (Exception e) {
                }
                if (all || tfl) {
                    if (g || allSubs) {

                        encoder.startEncoding(".gtfl");
                        if (decode) {
                            decoder.startDecoding(".gtfl");
                        }
                    }
                    if (m || allSubs) {

                        encoder.startEncoding(".mtfl");
                        if (decode) {
                            decoder.startDecoding(".mtfl");
                        }
                    }
                    if (n || allSubs) {

                        encoder.startEncoding(".ntfl");
                        if (decode) {
                            decoder.startDecoding(".ntfl");
                        }
                    }
                    if (rest || allSubs) {

                        encoder.startEncoding(".ptfl");
                        if (decode) {
                            decoder.startDecoding(".ptfl");
                        }
                    }
                }
                if (all || qual) {
//                
//                encoder.startEncoding(".mpeg_qvi_0");
//                if (decode) {
//                    decoder.startDecoding(".mpeg_qvi_0");
//                }
//                
//                encoder.startEncoding(".mpeg_qvci_0");
//                if (decode) {
//                    decoder.startDecoding(".mpeg_qvci_0");
//                }
//                
//                encoder.startEncoding(".mpeg_qv_parameters_set");
//                if (decode) {
//                    decoder.startDecoding(".mpeg_qv_parameters_set");
//                }
//                
                }
                if (all || len) {
                    if (g || allSubs) {

                        encoder.startEncoding(".glen");
                        if (decode) {
                            decoder.startDecoding(".glen");
                        }
                    }
                    if (m || allSubs) {

                        encoder.startEncoding(".mlen");
                        if (decode) {
                            decoder.startDecoding(".mlen");
                        }
                    }
                    if (p || allSubs) {

                        encoder.startEncoding(".plen");
                        if (decode) {
                            decoder.startDecoding(".plen");
                        }
                    }
                }
                if (all || rft) {
                    if (p || allSubs) {

                        encoder.startEncoding(".rftp");
                        if (decode) {
                            decoder.startDecoding(".rftp");
                        }
                    }
                    if (t || allSubs) {

                        encoder.startEncoding(".rftt");
                        if (decode) {
                            decoder.startDecoding(".rftt");
                        }

                    }
                }
            }
            durations[i] = (1.0 * System.currentTimeMillis() - start) / 1000;
            System.out.println("duration_full: " + durations[i]);
            encoder = null;
            decoder = null;
            System.gc();
//            try {
//                System.out.println("pause for 100 seconds");
//                Thread.sleep(1000 * 100);
//            } catch (Exception e) {
//            }

        }
        System.out.println(
                "-------------------------------------------------------");
        double min = Double.MAX_VALUE;
        double max = 0;
        double sum = 0.0;
        System.out.println("Total duration per run\n(including overhead tasks such as output validation (for run 1) and resetting the environment\nin between encodes, e.g., garbage collection, reinitializing encoders/decoders)");
        for (int i = 0; i < durations.length; i++) {
            System.out.println("run " + (i + 1) + ": " + durations[i]);
            if (durations[i] < min) {
                min = durations[i];
            }
            if (durations[i] > max) {
                max = durations[i];
            }
            sum += durations[i];
        }

        System.out.println(
                "fastest run: " + min);
        System.out.println(
                "slowest run: " + max);
        System.out.println(
                "avg: " + sum / durations.length);
        System.out.println(
                "-------------------------------------------------------");

    }

    private void decode() {

        Decoder decoder;
        if (all) {
            allSubs = true;
        }
        long start = 0;
        double[] durations = new double[nrOfRuns];//, 0, 0, 0, 0, 0};
        for (int i = 0; i < durations.length; i++) {
            durations[i] = 0;
        }
        for (int i = 0; i < durations.length; i++) {
            decoder = new Decoder(_inputFile);
            start = System.currentTimeMillis();
            if (_inputFile[0].substring(_inputFile[0].length() - 8, _inputFile[0].length() - 3).contains(".")) {
                if (i % nrOfRuns == 0) {
                    decoder.set2ndPass(false);
                } else {
                    decoder.set2ndPass(true);
                }
                decoder.startDecoding("");
            } else {
                if (i % nrOfRuns == 0) {
                    decoder.set2ndPass(false);
                } else {
                    decoder.set2ndPass(true);
                }
                if (qual) {
                    decoder.startDecoding("quals.merged.fastq");
                }
                if (all || pairs) {
                    if (g || all) {
                        decoder.startDecoding(".gpair");
                    }
                    if (m || all) {
                        decoder.startDecoding(".mpair");
                    }
                    if (n || all) {
                        decoder.startDecoding(".npair");
                    }
                    if (rest || all) {
                        decoder.startDecoding(".pair");
                    }
                }
                if (all || pos) {
                    if (g || all) {
                        decoder.startDecoding(".gpos");
                    }
                    if (m || all) {
                        decoder.startDecoding(".mpos");
                    }
                    if (n || all) {
                        decoder.startDecoding(".npos");
                    }
                    if (rest || all) {
                        decoder.startDecoding(".pos");
                    }
                }

                if (all || rcomp) {
                    if (g || all) {
                        decoder.startDecoding(".grcomp");
                    }
                    if (m || all) {
                        decoder.startDecoding(".mrcomp");
                    }
                    if (n || all) {
                        decoder.startDecoding(".nrcomp");
                    }
                    if (rest || all) {
                        decoder.startDecoding(".rcomp");
                    }
                }
                if (all || snp) {
                    if (p || all) {
                        decoder.startDecoding(".snpp");
                    }
                    if (t || all) {
                        decoder.startDecoding(".snpt");
                    }
                }
                if (all || ind) {
                    if (c || all) {
                        decoder.startDecoding(".indc");
                    }
                    if (p || all) {
                        decoder.startDecoding(".indp");
                    }
                    if (t || all) {
                        decoder.startDecoding(".indt");
                    }
                }
                if (all || nmis) {
                    decoder.startDecoding(".nmis");
                }
                if (all || rtype) {
                    decoder.startDecoding(".rtype");
                }
                if (all || subtype) {
                    decoder.startDecoding(".subtype");
                }
                if (all || ureads) {
                    decoder.startDecoding(".ureads");
                }
                if (all || tfl) {
                    if (g || all) {
                        decoder.startDecoding(".gtfl");
                    }
                    if (m || all) {
                        decoder.startDecoding(".mtfl");
                    }
                    if (n || all) {
                        decoder.startDecoding(".ntfl");
                    }
                    if (rest || all) {
                        decoder.startDecoding(".ptfl");
                    }
                }
                if (all || rft) {
                    if (p || all) {
                        decoder.startDecoding(".rftp");
                    }
                    if (t || all) {
                        decoder.startDecoding(".rftt");
                    }
                }
                if (all || qual) {
//            
//            decoder.startDecoding(".mpeg_qvi_0");
//            
//            decoder.startDecoding(".mpeg_qvci_0");
//            
//            decoder.startDecoding(".mpeg_qv_parameters_set");
                }
                if (all || len) {
                    if (g || all) {
                        decoder.startDecoding(".glen");
                    }
                    if (m || all) {
                        decoder.startDecoding(".mlen");
                    }
                    if (p || all) {
                        decoder.startDecoding(".plen");
                    }
                }

            }
            durations[i] = (1.0 * System.currentTimeMillis() - start) / 1000;
            System.out.println("duration_full: " + durations[i]);
            decoder = null;
            System.gc();
        }
        System.out.println(
                "-------------------------------------------------------");
        double min = Double.MAX_VALUE;
        double max = 0;
        double sum = 0.0;
        System.out.println("Total duration per run\n(including overhead tasks such as output validation and resetting the environment\nin between decodes, e.g., garbage collection, reinitializing decoders)");
        for (int i = 0; i < durations.length; i++) {
            System.out.println("run " + (i + 1) + ": " + durations[i]);
            if (durations[i] < min) {
                min = durations[i];
            }
            if (durations[i] > max) {
                max = durations[i];
            }
            sum += durations[i];
        }

        System.out.println(
                "fastest run: " + min);
        System.out.println(
                "slowest run: " + max);
        System.out.println(
                "avg: " + sum / durations.length);

        System.out.println(
                "-------------------------------------------------------");
    }

    public static void main(String[] args) {
        if (args.length != 3 && args.length != 4) {
            System.out.println("Usage:");
            System.out.println("Java [-XX:+UseG1GC] -jar MPEG-G.jar TASK INPUTFILE/PREFIX MODE\n");

            System.out.println("TASK: ENCODE or DECODE. Encoded will by default also run decode and compare the output to the input.");
            System.out.println("To skip decoding when ENCODE is selected, append nodec to MODE (see below). E.g., Fastnodec will run the fast mode but will skip decoding");
            System.out.println("\nINPUTFILE/PREFIX: path to the input file. If the file does not contain an extension, the task will be performed for all valid extensions.");
            System.out.println("E.g., in case of c:\\temp\\02_NA12878_S1 the program will, for each extension, append the extension and perform the corresponding compression on all existing files.\nE.g., c:\\temp\\02_NA12878_S1.pair, c:\\temp\\02_NA12878_S1.mpair, c:\\temp\\02_NA12878_S1.npair, c:\\temp\\02_NA12878_S1.gpos ...");
            System.out.println("\nMODE: Fast or Ultra. Ultra is the slow mode used in the dissertation.");
//            System.out.println("\nNRofRUNS indicates how many the times the testset should be encoded. One run consists of the encoding and decoding in both fast and ultra mode. If NRofRUNS>1, the integrity check of the decoded output is only performed in run 1.");
//            System.out.println("\n\nNote: the large amount of RAM is only required for input and output purposes (buffer, loading files in RAM in case of complex streams such as PAIR), to avoid garbage collection interruptions, and due to memory inefficient LUT generation.");
            System.out.println("\n\nNote: -XX:+UseG1GC enables the G1 Garbage Collector. This garbage collector offers higher efficiency regarding releasing unused Heap Space (hence, RAM) at a speed penalty.");
        } else {
            DNACompression main;
            if (args.length == 3) {
                main = new DNACompression(args[0], args[1], args[2], 1);
            } else {
                main = new DNACompression(args[0], args[1], args[2], Integer.parseInt(args[3]));
            }
            main.start();
        }
    }

    private void testDecodingSpeed() {
        try {
            System.out.println("SignedExpGolTest:");
            Writer writer;
            Reader reader;
            long start;
            long total_time = 0;
//            if (false) {
//                System.out.println("Writer Signed ExpGol");
//                for (int k = 0; k < 100; k++) {
//                    writer = new Writer("encode", "test", 10000);
//                    writer.buildContextModels();
//                    start = System.currentTimeMillis();
//                    for (int i = -500000; i < 500000; i++) {
//                        writer.writeAsSignedExpGolCabac(i % 16384, 0);
//                    }
////                    System.out.println((System.currentTimeMillis() - start));
//                    total_time += System.currentTimeMillis() - start;
//                    writer.close();
//                }
//                System.out.println("average_duration: " + total_time / 100);
//                total_time = 0;
//                System.out.println("Reader Signed ExpGol");
//                for (int k = 0; k < 100; k++) {
//                    reader = new Reader("encode", "test");
//                    reader.buildContextModels();
//                    start = System.currentTimeMillis();
//                    for (int i = -500000; i < 500000; i++) {
//                        if (i % 16384 != reader.readAsSignedExpGolCabac(0)) {
//                            System.out.println("error");
//                        }
//                    }
////                    System.out.println((System.currentTimeMillis() - start));
//                    total_time += System.currentTimeMillis() - start;
//                    reader.close();
//                }
//                System.out.println("average_duration: " + total_time / 100);
//                total_time = 0;
//                System.out.println("Writer ExpGol");
//                for (int k = 0; k < 100; k++) {
//                    writer = new Writer("encode", "test", 10000);
//                    writer.buildContextModels();
//                    start = System.currentTimeMillis();
//                    for (int i = 0; i < 1000000; i++) {
//                        writer.writeAsExpGolCabac(i % 16384, 0);
//                    }
////                    System.out.println((System.currentTimeMillis() - start));
//                    total_time += System.currentTimeMillis() - start;
//                    writer.close();
//                }
//                System.out.println("average_duration: " + total_time / 100);
//                total_time = 0;
//                System.out.println("Reader ExpGol");
//                for (int k = 0; k < 100; k++) {
//                    reader = new Reader("encode", "test");
//                    reader.buildContextModels();
//                    start = System.currentTimeMillis();
//                    for (int i = 0; i < 1000000; i++) {
//                        if (i % 16384 != reader.readAsExpGolCabac(0)) {
//                            System.out.println("error");
//                        }
////            reader.readAsExpGolImprovedCabac(0);
////                System.out.println("error");        
//                    }
////                    System.out.println((System.currentTimeMillis() - start));
//                    total_time += System.currentTimeMillis() - start;
//                    reader.close();
//                }
//                System.out.println("average_duration: " + total_time / 100);
//            total_time = 0;
//                System.out.println("Writer ExpGol");
//                for (int k = 0; k < 100; k++) {
//                    writer = new Writer("encode", "test", 10000);
//                    writer.buildContextModels();
//                    start = System.currentTimeMillis();
//                    for (int i = 0; i < 1000000; i++) {
//                        writer.writeAsExpGolCabac(i % 16384, 0);
//                    }
////                    System.out.println((System.currentTimeMillis() - start));
//                    total_time += System.currentTimeMillis() - start;
//                    writer.close();
//                }
//                System.out.println("average_duration: " + total_time / 100);
//                total_time = 0;
            System.out.println("Reader ExpGol Old");
            for (int k = 0; k < 100; k++) {
                reader = new Reader("encode", "test");
                reader.buildContextModels();
                start = System.currentTimeMillis();
                for (int i = 0; i < 1000000; i++) {
                    if (i % 16384 != reader.readAsExpGolCabac(0)) {
                        System.out.println("error");
                    }
                }
//                    System.out.println((System.currentTimeMillis() - start));
                total_time += System.currentTimeMillis() - start;
                reader.close();
            }
            System.out.println("average_duration: " + total_time / 100);
            total_time = 0;
            System.out.println("Reader ExpGol New");
            for (int k = 0; k < 100; k++) {
                reader = new Reader("encode", "test");
                reader.buildContextModels();
                start = System.currentTimeMillis();
                for (int i = 0; i < 1000000; i++) {
                    if (i % 16384 != reader.readAsExpGolCabac(0)) {
                        System.out.println("error");
                    }
                }
//                    System.out.println((System.currentTimeMillis() - start));
                total_time += System.currentTimeMillis() - start;
                reader.close();
            }
            System.out.println("average_duration: " + total_time / 100);
        } catch (Exception e) {
        }
        System.exit(0);
    }

}
