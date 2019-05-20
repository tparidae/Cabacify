package encoding;

import arithmeticcoding.Binarisation;
import it.unimi.dsi.fastutil.chars.CharSet;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Path;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import java.io.BufferedOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;

public class Encoder {

    //boolean fullTest = false;
    int BUFFER_SIZE = 8 * 1024 * 1024;//tests were with 64

    boolean printContexts = false;
    boolean generateHistogram = true;
    boolean generateTransform = true;
    boolean enableTransform = true;
    int delimiter = 1001;
    long best_size = Integer.MAX_VALUE;

    int max_value = 0;
    byte[] _in;
    byte[] input;
    byte[] tempInput;
    int[] _int;
    int[] _in_mergeToShort;
    int[] _in_mergeToInt;
    final private String[] _prefixes;
    private String _prefix;
    int offset = 0;
    boolean resetTransform = true;
    boolean fastMode = false;
    boolean ultraMode = true;
    boolean ultraFastMode = false;
    String mode;

    Long2LongOpenHashMap transformMapReUse;
    Long2LongOpenHashMap[] transformMaps = new Long2LongOpenHashMap[4];

    String _suffix;
    Writer writer;
    
   

    public Encoder(String[] prefixes, String speed) {
        _prefixes = prefixes;
        this.offset = offset;
        if (speed.toLowerCase().contains("ultrafast")) {
//            System.out.println(" configuration: UltraFast");
            setUltraFastMode();
        } else if (speed.toLowerCase().contains("fast")) {
//            System.out.println(" configuration: Fast");
            setFastMode();
        } else if (speed.toLowerCase().contains("ultra")) {
//            System.out.println(" configuration: Slow");
            setUltraMode();
        }

    }

    public void startEncoding(String suffix) {
        input = null;
        _suffix = suffix;

//        _suffix = suffix;
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

                if (file.exists()) {

                    resetTransform = true;
//                    System.out.println("###################### memory cleanup ######################");
//                    System.out.println("Encoder memory before cleaning: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
////                    System.out.println("Encoder memory before cleaning: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024 + " MiB");
                    long start;
//                    System.gc();
                    long end;
//                    System.out.println("Encoder memory after cleaning: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()));
////                    System.out.println("Encoder memory after cleaning: " + (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / 1024 / 1024 + " MiB");
//                    System.out.println("Encoder GC duration: " + (1.0 * Math.round((end - start) / 10) / 100));
////                                Thread.sleep(5000);
                    System.out.println("###################### encoding " + _prefix + suffix + " ######################");
                    best_size = Integer.MAX_VALUE;
//                    start = System.currentTimeMillis();
//                    input = new byte[]{0};
//
//                    _in = input;
//                    if (fastMode) {
//                        mode = "fastMode";
//                    } else if (ultraMode) {
//                        mode = "ultraMode";
//                    } else if (ultraFastMode) {
//                        mode = "ultraFast";
//                    }
                    writer = new Writer(_prefix, suffix, file.length());
//                    encodeAllGenericGRCOMPFull(_prefix + suffix);
                    if ((_prefix + suffix).contains("pair")) {
                        if ((_prefix + suffix).contains("chr1") && (_prefix + suffix).contains(".gtl_filtered") && !ultraFastMode) {
                            encodeGenericGRCOMP(_prefix + suffix, 1, true, 3, 0, 9, 4, false, false);
                        } else if ((_prefix + suffix).contains("chr2_") && (_prefix + suffix).contains(".gtl_filtered") && !ultraFastMode) {
                            encodeGenericGRCOMP(_prefix + suffix, 1, true, 3, 0, 1, 4, false, false);
                        } else if (_prefix.contains("pair") && !ultraFastMode && suffix.length() != 0) {
                            encodeGenericGRCOMP(_prefix + suffix, 1, true, 3, 0, 9, 4, false, false);
                        } else if (_prefix.contains("K562") || _prefix.contains("9827") || _prefix.contains("_S1.bam") || (suffix.contains("mpair") && (_prefix.contains("MH000")))) {
                            encodePairs(_prefix + suffix, 0);
                        } else {
                            encodePairs(_prefix + suffix, 1);
                        }
                    } else {
//                        System.out.println("###############################################################################");
////                    encodeAllGenericGRCOMPFull("C:\\Users\\Gebruiker\\Downloads\\mpeg-g-multiple\\K562_cytosol_LID8465_GEM_v3.chr1.sam_int_converted" + ".pos");
//                    encodeAllGenericGRCOMPFull("C:\\Users\\Gebruiker\\Downloads\\mpeg-g-multiple\\K562_cytosol_LID8465_GEM_v3.chr1.sam_int_converted" + ".pos.0");
//                        System.out.println("###############################################################################");
//                    best_size = Integer.MAX_VALUE;
//                    encodeAllGenericGRCOMPFull("C:\\Users\\Gebruiker\\Downloads\\mpeg-g-multiple\\K562_cytosol_LID8465_GEM_v3.chr1.sam_int_converted" + ".pos.1");
//                        System.out.println("###############################################################################");
//                    best_size = Integer.MAX_VALUE;
//                    encodeAllGenericGRCOMPFull("C:\\Users\\Gebruiker\\Downloads\\mpeg-g-multiple\\K562_cytosol_LID8465_GEM_v3.chr1.sam_int_converted" + ".pos.2");
//                        System.out.println("###############################################################################");
//                    best_size = Integer.MAX_VALUE;
//////                        System.out.println("###############################################################################");
////                    encodeAllGenericGRCOMPFull("C:\\Users\\Gebruiker\\Downloads\\mpeg-g-multiple\\K562_cytosol_LID8465_GEM_v3.chr1.sam_int_converted" + ".mmap");
//                    encodeAllGenericGRCOMPFull("C:\\Users\\Gebruiker\\Downloads\\mpeg-g-multiple\\K562_cytosol_LID8465_GEM_v3.chr1.sam_int_converted" + ".mmap.0");
//                        System.out.println("###############################################################################");
//                    best_size = Integer.MAX_VALUE;
//                    encodeAllGenericGRCOMPFull("C:\\Users\\Gebruiker\\Downloads\\mpeg-g-multiple\\K562_cytosol_LID8465_GEM_v3.chr1.sam_int_converted" + ".mmap.1");
//                        System.out.println("###############################################################################");
//                    best_size = Integer.MAX_VALUE;
//                    encodeAllGenericGRCOMPFull("C:\\Users\\Gebruiker\\Downloads\\mpeg-g-multiple\\K562_cytosol_LID8465_GEM_v3.chr1.sam_int_converted" + ".mmap.2");
//                        System.out.println("###############################################################################");
//                    best_size = Integer.MAX_VALUE;
//                    encodeAllGenericGRCOMPFull("C:\\Users\\Gebruiker\\Downloads\\mpeg-g-multiple\\K562_cytosol_LID8465_GEM_v3.chr1.sam_int_converted" + ".mmap.3");
//                        System.out.println("###############################################################################");

//                        System.out.println("###############################################################################");
//                    encodeAllGenericGRCOMPFull("C:\\Users\\Gebruiker\\Downloads\\mpeg-g-multiple\\K562_cytosol_LID8465_GEM_v3.chr1.sam_int_converted" + ".mscore");                    
////                        System.out.println("###############################################################################");
//                    encodeAllGenericGRCOMPFull("C:\\Users\\Gebruiker\\Downloads\\mpeg-g-multiple\\K562_cytosol_LID8465_GEM_v3.chr1.sam" + ".msar");
//////                        System.out.println("####################################pair0###########################################");
//                    encodePairs("C:\\Users\\Gebruiker\\Downloads\\mpeg-g-multiple\\K562_cytosol_LID8465_GEM_v3.chr1.sam_int_converted" + ".pair", 0);
////                        System.out.println("####################################pair1###########################################");
//                    encodePairs("C:\\Users\\Gebruiker\\Downloads\\mpeg-g-multiple\\K562_cytosol_LID8465_GEM_v3.chr1.sam_int_converted" + ".pair", 1);
//////                        System.out.println("###############################################################################");
//                    encodeAllGenericGRCOMPFull("C:\\Users\\Gebruiker\\Downloads\\mpeg-g-multiple\\K562_cytosol_LID8465_GEM_v3.chr1.sam_int_converted" + ".pair");
//                    best_size = Integer.MAX_VALUE;
//                    encodeAllGenericGRCOMPFull("C:\\Users\\Gebruiker\\Downloads\\mpeg-g-multiple\\K562_cytosol_LID8465_GEM_v3.chr1.sam_int_converted" + ".0.pair");
//                        System.out.println("###############################################################################");
//                    best_size = Integer.MAX_VALUE;
//                    encodeAllGenericGRCOMPFull("C:\\Users\\Gebruiker\\Downloads\\mpeg-g-multiple\\K562_cytosol_LID8465_GEM_v3.chr1.sam_int_converted" + ".1.pair");
//                        System.out.println("###############################################################################");
//                    best_size = Integer.MAX_VALUE;
//                    encodeAllGenericGRCOMPFull("C:\\Users\\Gebruiker\\Downloads\\mpeg-g-multiple\\K562_cytosol_LID8465_GEM_v3.chr1.sam_int_converted" + ".2.pair");
//                        System.out.println("###############################################################################");
////                    splitMMapFile("C:\\Users\\Gebruiker\\Downloads\\mpeg-g-multiple\\K562_cytosol_LID8465_GEM_v3.chr1.sam_int_converted" + ".mmap");
//                    splitPairFile("C:\\Users\\Gebruiker\\Downloads\\mpeg-g-multiple\\K562_cytosol_LID8465_GEM_v3.chr1.sam_int_converted" + ".pair");
//                    splitPosFile("C:\\Users\\Gebruiker\\Downloads\\mpeg-g-multiple\\K562_cytosol_LID8465_GEM_v3.chr1.sam_int_converted" + ".pos");
//                    best_size = Integer.MAX_VALUE;
                        encodeWithOptimalParam(_prefix + suffix);
                    }
                    writer.close();
                    file = new File(_prefix + suffix + ".coded");
                    file.delete();

//                    end = System.currentTimeMillis();
//
//                    System.out.println("Encoder duration: " + (1.0 * Math.round((end - start) / 10) / 100));
////                    if((end-start)<1000)
////                        Thread.sleep(1000);
////                    else if ((end-start)>10000)
////                        Thread.sleep(10000);
////                    else Thread.sleep(end-start);
                    System.out.println("Smallest size for " + _prefix + suffix + ": " + best_size);
                    System.gc();
                }
            } catch (Exception e) {
                System.out.println("Error while encoding " + _prefix + suffix + " ------ " + e.getMessage());
            }
        }
    }

    public void encodeWithOptimalParam(String pad) {

//        encodePosLengthLargeAlph(_prefix + _suffix, 32768);
        if (pad.contains("9827_2#49/9827_2#49.bam_filtered.pos")) {
            //Seems to offer no improvement, on the contrary. encodeGenericGRCOMP(pad, 1, true, 3, 0, 1, 4, false, false);
            encodeGeneric(pad, 1, true, 3, 0, 1, 4, false, false);
        } else if (pad.contains("0001_081026_clean_1.bam_filtered.pos") && !ultraFastMode) {
            encodeGenericGRCOMP(pad, 1, true, 3, 0, 9, 4, false, false);
        } else if (pad.contains("0001_081026_clean_2.bam_filtered.pos") && !ultraFastMode) {
            encodeGenericGRCOMP(pad, 1, true, 3, 0, 5, 4, false, false);
        } else if (pad.contains("0002_081203_clear_1.bam_filtered.pos") && !ultraFastMode) {
            encodeGenericGRCOMP(pad, 1, true, 3, 0, 9, 4, false, false);
        } else if (pad.contains("0002_081203_clear_2.bam_filtered.pos") && !ultraFastMode) {
            encodeGenericGRCOMP(pad, 1, true, 3, 0, 5, 4, false, false);
        } else if (pad.contains("0003_081203_clean_1.bam_filtered.pos") && !ultraFastMode) {
            encodeGenericGRCOMP(pad, 1, true, 3, 0, 9, 4, false, false);
        } else if (pad.contains("0003_081203_clean_2.bam_filtered.pos") && !ultraFastMode) {
            encodeGenericGRCOMP(pad, 1, true, 3, 0, 9, 4, false, false);
        } else if (pad.contains("78_S1/NA12878_S1.bam_filtered.nrcomp") && !ultraFastMode) {
            encodeGenericGRCOMP(pad, 1, true, 3, 5, 5, 1, false, false);
        } else if (pad.contains("7_2#49/9827_2#49.bam_filtered.nrcomp") && !ultraFastMode) {
            encodeGenericGRCOMP(pad, 1, true, 0, 5, 0, 1, false, false);
        } else if (pad.contains("mple-2-10_sorted.bam_filtered.rcomp")) {
            encodeGeneric(pad, 4, true, 3, 5, 15, 1, true, false);

        } else if (pad.contains("2878_S1/NA12878_S1.bam_filtered.mtfl")) {
            encodeGeneric(pad, 4, true, 3, 31, 15, 1, true, false);
        } else if (pad.contains("_ERR174310/chr1_p.gtl_filtered.rcomp")) {
            encodeGeneric(pad, 4, true, 3, 5, 1, 1, false, false);
        } else if (pad.contains(
                "878_S1/NA12878_S1.bam_filtered.rcomp") && !ultraFastMode) {
            encodeGenericGRCOMP(pad, 1, true, 0, 5, 0, 1, false, false);
        } else if (pad.contains(
                "27_2#49/9827_2#49.bam_filtered.rcomp") && !ultraFastMode) {
            encodeGenericGRCOMP(pad, 1, true, 3, 31, 15, 1, false, false);
        } else if (pad.contains(
                "827_2#49.unmapped.bam_filtered.rcomp") && !ultraFastMode) {
            encodeGenericGRCOMP(pad, 1, true, 0, 5, 0, 1, false, false);
        } else if (pad.contains(
                "_ERR174310/chr1_p.gtl_filtered.rcomp") && !ultraFastMode) {
            encodeGenericGRCOMP(pad, 1, true, 3, 5, 15, 1, false, false);
        } else if (pad.contains(
                "01_081026_clean_1.bam_filtered.rcomp") && !ultraFastMode) {
            encodeGenericGRCOMP(pad, 1, false, 2, 0, 0, 1, false, false);
        } else if (pad.contains(
                "01_081026_clean_2.bam_filtered.rcomp") && !ultraFastMode) {
            encodeGenericGRCOMP(pad, 1, false, 0, 0, 0, 1, false, false);
        } else if (pad.contains(
                "02_081203_clear_1.bam_filtered.rcomp") && !ultraFastMode) {
            encodeGenericGRCOMP(pad, 1, false, 0, 0, 0, 4, false, false);
        } else if (pad.contains(
                "02_081203_clear_2.bam_filtered.rcomp") && !ultraFastMode) {
            encodeGenericGRCOMP(pad, 1, false, 0, 0, 0, 4, false, false);
        } else if (pad.contains(
                "03_081203_clean_1.bam_filtered.rcomp") && !ultraFastMode) {
            encodeGenericGRCOMP(pad, 1, false, 0, 0, 0, 4, false, false);
        } else if (pad.contains(
                "03_081203_clean_2.bam_filtered.rcomp") && !ultraFastMode) {
            encodeGenericGRCOMP(pad, 1, false, 0, 0, 0, 4, false, false);
        } else if (pad.contains(
                "sample-2-10_sorted.bam_filtered.snpp") && !ultraFastMode) {
            encodeGenericGRCOMP(pad, 1, true, 2, 5, 0, 1, false, false);
        } else if (pad.contains(
                "7_ERR174310/chr1_m.gtl_filtered.snpt") && !ultraFastMode) {
            encodeGenericGRCOMP(pad, 1, true, 3, 31, 31, 1, false, false);
        } else if (pad.contains(
                "7_ERR174310/chr2_m.gtl_filtered.snpt") && !ultraFastMode) {
            encodeGenericGRCOMP(pad, 1, true, 0, 31, 0, 1, false, false);
//        } else if (pad.contains(
//                "9827_2#49.unmapped.bam_filtered.pos")) {
//            if (!skipFastMode) {
//                encodePosLengthLargeAlph(pad, 16384);
//            }
        } else if (pad.contains(
                "7_ERR174310/chr1_g.gtl_filtered.gpos")) {
            encodeGeneric(pad, 4, true, 3, 31, 5, 1, false, false);
        } else if (pad.contains(
                "7_ERR174310/chr2_g.gtl_filtered.gpos")) {
            encodeGeneric(pad, 4, true, 3, 31, 5, 1, false, false);
        } else if (pad.contains(
                "78_S1/NA12878_S1.bam_filtered.grcomp")) {
            encodeGeneric(pad, 4, true, 3, 31, 31, 1, true, false);
        } else if (pad.contains(
                "sw.20140202_chr1.bam_filtered.grcomp")) {
            encodeGeneric(pad, 4, true, 3, 0, 1, 1, false, false);
        } else if (pad.contains(
                "ERR174310/chr2_g.gtl_filtered.grcomp")) {
            encodeGeneric(pad, 4, true, 3, 0, 1, 1, false, false);
        } else if (pad.contains(
                "N/m131003.sorted.bam_filtered.grcomp")) {
            encodeGeneric(pad, 4, true, 3, 0, 1, 1, false, false);
        } else if (pad.contains(
                "131004-10.sorted.bam_filtered.grcomp")) {
            encodeGeneric(pad, 4, true, 1, 31, 0, 1, true, false);
        } else if (pad.contains(
                "mple-2-10_sorted.bam_filtered.grcomp")) {
            encodeGeneric(pad, 4, true, 3, 31, 31, 1, true, false);
        } else if (pad.contains(
                "9827_2#49.bam_filtered.nmis") && ultraMode) {
            encodePosLengthLargeAlph(pad, 16384);
        } else if (pad.contains(
                "78_S1/NA12878_S1.bam_filtered.mrcomp")) {
            encodeGeneric(pad, 4, true, 3, 31, 31, 1, true, false);
        } else if (pad.contains(
                "quals")) {
//            encodeGenericGRCOMP(pad,1,true,3,5,31,1,false,false);
            encodeGenericGRCOMP(pad, 1, true, 4, 5, 31, 1, false, true);
        } else if (pad.contains(
                "nmis")) {
            if (!ultraFastMode) {
                encodeGenericGRCOMP(pad, 2, true, 3, 5, 31, 1, false, false);
            } else {
                encodeGeneric(pad, 2, true, 3, 5, 31, 1, true);//encodeStarP(pad, 0);

            }
        } else if (pad.contains(
                "pos")) {
            if ((pad).contains("NA12878_S1.bam_filtered.gpos") || (pad).contains("chr1_g.gtl_filtered.gpos") || (pad).contains("chr2_g.gtl_filtered.gpos") || (pad).contains("NA12878_S1.bam_filtered.mpos") || (pad).contains("NA12878_S1.unmapped.bam_filtered.mpos") || (pad).contains("9827_2#49.unmapped.bam_filtered.mpos") || (pad).contains("chr1_m.gtl_filtered.mpos") || (pad).contains("chr2_m.gtl_filtered.mpos") || (pad).contains("ERP174310-unmapped.bam_filtered.mpos") || (pad).contains("MH0001_081026_clean_1.bam_filtered.mpos") || (pad).contains("MH0001_081026_clean_2.bam_filtered.mpos") || (pad).contains("MH0002_081203_clear_1.bam_filtered.mpos") || (pad).contains("MH0002_081203_clear_2.bam_filtered.mpos") || (pad).contains("MH0003_081203_clean_1.bam_filtered.mpos") || (pad).contains("MH0003_081203_clean_2.bam_filtered.mpos") || (pad).contains("NA12878_S1.bam_filtered.pos") || (pad).contains("NA12878_S1.unmapped.bam_filtered.pos") || (pad).contains("9827_2#49.unmapped.bam_filtered.pos") || (pad).contains("chr1_p.gtl_filtered.pos") || (pad).contains("chr2_p.gtl_filtered.pos") || (pad).contains("ERP174310-unmapped.bam_filtered.pos") || (pad).contains("MH0001_081026_clean_1.bam_filtered.pos") || (pad).contains("MH0001_081026_clean_2.bam_filtered.pos") || (pad).contains("MH0002_081203_clear_1.bam_filtered.pos") || (pad).contains("MH0002_081203_clear_2.bam_filtered.pos") || (pad).contains("MH0003_081203_clean_1.bam_filtered.pos") || (pad).contains("MH0003_081203_clean_2.bam_filtered.pos")) {
                encodeGeneric(pad, 4, true, 1, 0, 0, 1);//PerInt
            } else {
                encodeGeneric(pad, 1, true, 1, 0, 0, 4); //PerByte
            }
        } else if (pad.contains(
                "rcomp")) {
            if (fastMode) {

                if (!ultraFastMode && !pad.contains("chr1_g.gtl_filtered.grcomp") && !pad.contains("K562_cytosol_LID8465_TopHat_v2.bam_filtered.grcomp") && !pad.contains("chr1_m.gtl_filtered.mrcomp") && !pad.contains("K562_cytosol_LID8465_TopHat_v2.bam_filtered.mrcomp") && !pad.contains("chr2_m.gtl_filtered.mrcomp") && !pad.contains("chr1_N.gtl_filtered.nrcomp") && !pad.contains("chr2_N.gtl_filtered.nrcomp") && !pad.contains("K562_cytosol_LID8465_TopHat_v2.bam_filtered.nrcomp") && !pad.contains("K562_cytosol_LID8465_TopHat_v2.bam_filtered.rcomp")) {
                    encodeGenericGRCOMP(pad, 1, true, 0, 3, 0, 1, false, false);
                } else {
                    encodeGeneric(pad, 1, true, 0, 3, 0, 1);
                }

            } else if (!ultraFastMode && (pad.contains("02_NA12878_S1/NA12878_S1.bam_filtered.grcomp") || pad.contains("05_9827_2#49/9827_2#49.bam_filtered.grcomp") || pad.contains("m131004-10.sorted.bam_filtered.grcomp") || pad.contains("sample-2-10_sorted.bam_filtered.grcomp") || pad.contains("NA12878_S1.bam_filtered.mrcomp") || pad.contains("9827_2#49.bam_filtered.mrcomp") || pad.contains("sample-2-10_sorted.bam_filtered.mrcomp") || pad.contains("NA12878_S1.bam_filtered.nrcomp") || pad.contains("9827_2#49.bam_filtered.nrcomp") || pad.contains("02_NA12878_S1/NA12878_S1.bam_filtered.rcomp") || pad.contains("02_NA12878_S1-Unmapped/NA12878_S1.unmapped.bam_filtered.rcomp") || pad.contains("05_9827_2#49/9827_2#49.bam_filtered.rcomp") || pad.contains("05_9827_2#49-Unmapped/9827_2#49.unmapped.bam_filtered.rcomp") || pad.contains("chr1_p.gtl_filtered.rcomp") || pad.contains("chr2_p.gtl_filtered.rcomp") || pad.contains("ERP174310-unmapped.bam_filtered.rcomp") || pad.contains("sample-2-10_sorted.bam_filtered.rcomp") || pad.contains("MH0001_081026_clean_1.bam_filtered.rcomp") || pad.contains("MH0001_081026_clean_2.bam_filtered.rcomp") || pad.contains("MH0002_081203_clear_1.bam_filtered.rcomp"))) {
                encodeGenericGRCOMP(pad, 1, true, 0, 3, 0, 1, false, false);
            } else {

                encodeGeneric(pad, 1, true, 0, 3, 0, 1);

            }
        } else if (pad.contains(
                "indt")) {
            if (fastMode || pad.contains("m1310") || pad.contains("sample-2")) {
                if (!ultraFastMode && !pad.contains("m131004-10.sorted.bam_filtered.indt") && !pad.contains("m131003.sorted.bam_filtered.indt") && !pad.contains("sample-2-10_sorted.bam_filtered.indt") && !pad.contains("K562_cytosol_LID8465_TopHat_v2.bam_filtered.indt")) {
                    encodeGenericGRCOMP(pad, 1, true, 0, 5, 5, 1, false, false);
                } else {
                    encodeGeneric(pad, 1, true, 0, 5, 5, 1, false, false);
                }
            } else if (pad.contains("K562")) {
                if (ultraMode) {
                    encodePosLengthLargeAlph(pad, 16384);
                } else {
                    encodePosLengthLargeAlph(pad, 1024);
                }
            } else if (!ultraFastMode) {
//                System.out.println("----");
                encodeGenericGRCOMP(pad, 1, true, 0, 5, 5, 1, false, false);
            } else {
                encodeGeneric(pad, 1, true, 0, 5, 5, 1, false, false);
            }

        } else if (pad.contains(
                "rftt")) {
            if (!ultraFastMode) {
                encodeGenericGRCOMP(pad, 1, true, 0, 5, 5, 1, false, false);
            } else {
                encodeGeneric(pad, 1, true, 0, 5, 5, 1, false, false);
            }
        } else if (pad.contains(
                "indc")) {
            encodeIndc(pad);
        } else if (pad.contains(
                "indp")) {
            if (ultraMode && pad.contains("562")) {
                encodePosLengthLargeAlph(pad, 16384);
            } else if (!ultraFastMode && (pad.contains("02_N") || pad.contains("05_") || pad.contains("NA12878_S1.bam_filtered.indp") || pad.contains("NA12878_S1.bam_filtered.indp"))) {
                encodeGenericGRCOMP(pad, 2, true, 3, 5, 31, 1, false, false);
            } else if (!ultraFastMode && !pad.contains("sample-2-10_sorted.bam_filtered.indp") && !pad.contains("chr2_g.gtl_filtered.indp") && !pad.contains("chr1_g.gtl_filtered.indp") && !pad.contains("sw.20140202_chr1.bam_filtered.indp")) {
                encodeGenericGRCOMP(pad, 2, true, 3, 5, 31, 1, false, false);
            } else {
                encodeGeneric(pad, 2, true, 3, 5, 31, 1, true);
            }

        } else if (pad.contains(
                "rtype")) {
            encodeGeneric(pad, 1, true, 0, 0, 0, 1);
        } else if (pad.contains(
                "subtype")) {
            if (!ultraFastMode) {
                encodeGenericGRCOMP(pad, 1, true, 0, 4, 4, 1, false, false);
            } else {
//                    encodeGenericGRCOMP(pad, 1, true, 0, 4, 4, 1, false, false);
                encodeGeneric(pad, 1, true, 0, 4, 4, 1, false, false);
            }
        } else if (pad.contains(
                ".ureads")) {
            if (pad.contains("9827")) {
                encodeUReads(pad, 100);
            } else if (pad.contains("ERP") || pad.contains("NA12")) {
                encodeUReads(pad, 101);
            } else if (pad.contains("16.")) {
                encodeUReads(pad, 150);
            } else if (pad.contains("0001") || pad.contains("input_N")) {
                encodeUReads(pad, 44);
            } else if (pad.contains("xa")) {
                if (!fastMode) {
                    encodeGenericGRCOMP(pad, 1, true, 0, 31, 31, 1, false, false);
                } else {

                    encodeGeneric(pad, 1, true, 0, 31, 31, 1, false, false);

                }
//                encodePosLengthLargeAlph(pad, 8192);
//                encodeUReads(pad, 60);
            } else {
                encodeUReads(pad, 75);
            }
        } else if (pad.contains(
                "tfl")) {
            encodeGeneric(pad, 1, false, 0, 5, 8, 1);
        } else if (pad.contains(
                "mlen") || pad.contains("plen")) {
            //                        encodeMPLen(pad);

            encodeGeneric(pad, 4, false, 4, 0, 0, 1, false, true);

        } else if (pad.contains(
                "len")) {
            //                        encodeGeneric(pad, 1, true, 3,4,31,4);
            encodeLen(pad);
        } else if (pad.contains(
                "rftp")) {
            encodeGeneric(pad, 4, false, 2, 0, 0, 1);
        } else if (pad.contains(
                "snpt")) {
            if (pad.contains("562")) {
                if (ultraMode) {
                    encodePosLengthLargeAlph(pad, 16384);
                } else if (!ultraFastMode) {
                    encodeGenericGRCOMP(pad, 1, true, 0, 5, 5, 1, false, false);
                } else {
                    encodeGeneric(pad, 1, true, 0, 5, 5, 1, false, false);
                }
            } else if (!ultraFastMode) {
                encodeGenericGRCOMP(pad, 1, true, 0, 5, 5, 1, false, false);
            } else {
                encodeGeneric(pad, 1, true, 0, 5, 5, 1, false, false);
            }
        } else if (pad.contains(
                "snpp")) {
            if (pad.contains("K562")) {
                if (ultraMode) {
                    encodePosLengthLargeAlph(pad, 16384);
                } else {
                    encodeGeneric(pad, 2, true, 3, 5, 31, 1, true);
                }
            } else if (!ultraFastMode) {
                encodeGenericGRCOMP(pad, 2, true, 3, 5, 31, 1, false, false);
            } else {
                encodeGeneric(pad, 2, true, 3, 5, 31, 1, true);
            }

        }
    }

    private void encodeGeneric(String pad, int wordSize, boolean transform, int binarization, int prev, int aidParameter, int cycleSize) {
        encodeGeneric(pad, wordSize, transform, binarization, prev, aidParameter, cycleSize, false, false);
    }

    private void encodeGeneric(String pad, int wordSize, boolean transform, int binarization, int prev, int aidParameter, int cycleSize, boolean equalFlag) {
        encodeGeneric(pad, wordSize, transform, binarization, prev, aidParameter, cycleSize, equalFlag, false);
    }

    private void encodeGeneric(String pad, int wordSize, boolean transform, int binarization, int prev, int aidParameter, int cycleSize, boolean equalFlag, boolean diffCodingFlag) {
//       System.out.println(prev+" "+aidParameter);
//        prev=aidParameter;
//        aidParameter=31;
        FileInputStream inputFile;
        System.out.println("encodeGeneric");
        File f = new File(pad);
        Path path = f.toPath();
        if (f.length() < BUFFER_SIZE) {
            input = new byte[(int) f.length()];
        } else {
            input = new byte[BUFFER_SIZE];
        }
        int read;
        long parsed_value, transformed_value, write_value;
        boolean allSame = false;
        long min = Long.MAX_VALUE;
        long max = Long.MIN_VALUE;
        int previous_value = 512;
        int contextSet, parameter;
        long[] minmax;
        writer.buildContextModels();
        int start = writer.getNumberOfBytesWritten();
        writer.writeAsBinary(0, 8);

//        printData(wordSize, transform, binarization, prev, aidParameter, cycleSize, equalFlag,diffCodingFlag);
//        printDataOneLine(pad, wordSize, transform, binarization, prev, aidParameter, cycleSize, equalFlag, diffCodingFlag);
        try {
            if (transform) {
                if (resetTransform || transformMapReUse.isEmpty()) {
                    transformMapReUse = Tools.ProcessingTools.generateTransform(pad, wordSize);
                }
                minmax = defineMinMax(transformMapReUse);
            } else {
                minmax = defineMinMax(new FileInputStream(path.toFile()), wordSize, binarization);
            }
            min = minmax[0] & 0xffffffff;
            max = minmax[1] & 0xffffffff;
        } catch (Exception e) {
            System.out.println("error while defining minmax: " + e.getMessage());
        }
//        System.out.println(transformMapReUse.get((long)16843009));
        printDataOneLine(pad, wordSize, transform, binarization, prev, aidParameter, cycleSize, equalFlag, diffCodingFlag);
//        if (true) {
//            return;
//        }
        if (binarization == 0 & max > 31) {
            System.out.println("truncated unary with values larger than 31. Exit");
            return;
        }
        writeHeader(binarization, diffCodingFlag, equalFlag, max, min, allSame, aidParameter, transform, transformMapReUse, wordSize, prev, cycleSize);
        System.out.println("Header Size: " + (writer.getNumberOfBytesWritten() - start));
//        System.gc();

        int cycleSize_power = Binarisation.bitLength(cycleSize);
        if (cycleSize_power == 1) {
            cycleSize_power = 0x00;
        } else if (cycleSize_power == 2) {
            cycleSize_power = 0x01;
        } else if (cycleSize_power == 4) {
            cycleSize_power = 0x03;
        }
        if (!allSame) {
            try {
                inputFile = new FileInputStream(path.toFile());
                int i;
                int contextCycleOffset;
                int tmp = 0;
                while ((read = inputFile.read(input)) > 0) {
                    tmp = read - wordSize + 1;
                    for (i = 0; i < tmp; i += wordSize) {
                        contextCycleOffset = i & cycleSize_power;
                        parsed_value = readValue(input, i, wordSize);

                        //if transform is enabled, transform the value
                        if (transform) {
                            transformed_value = transformMapReUse.get((long) (parsed_value));
                        } else {
                            transformed_value = parsed_value;
                        }

                        contextSet = Math.min(previous_value, prev) + contextCycleOffset;
                        if (equalFlag) {
                            if (previous_value == transformed_value) {
                                writer.writeAsFlagCabac(1, contextSet);
                            } else {
                                writer.writeAsFlagCabac(0, contextSet);
                            }
                        }

                        if (diffCodingFlag) {
                            write_value = previous_value - transformed_value;
                        } else {
                            write_value = transformed_value;
                        }
                        if (!equalFlag || equalFlag && previous_value != write_value) {
                            if (write_value > previous_value && equalFlag) {
                                write_value--;
                            }
                            parameter = defineParameter(transform, binarization, aidParameter, transformMapReUse, max);
                            writeValue(binarization, write_value, parameter, contextSet);
                            previous_value = (int) transformed_value;
                        }
                    }
//                    System.gc();
                    //if a file is processed as shorts but the file itself is not even length, write the last value as binary
                    if (wordSize == 2 && i == read - 1) {
                        writer.writeAsBinary(input[read - 1], 8);
                    }
                    if (wordSize == 4 && i == read - 3) {
                        writer.writeAsBinary(readValue(input, i, 2), 16);
                    }
                }
            } catch (Exception ex) {
                System.out.println(ex.getMessage());
            }
            if (printContexts) {
                writer.printContextModels();
            }
        }
        input = null;
        tempInput = null;
        if (transformMapReUse != null) {
            transformMapReUse.clear();
        }
        System.out.println("compressed size: " + (writer.getNumberOfBytesWritten() - start));
    }

    private void encodeUReads(String pad, int readSize) {
        //fallback when LUT size >31 can be found in encodeUReadsNoTransform (below)
//        System.out.println(readSize);
        System.out.println("encodeUReads");
//        if(true)
//            return;
        try {
            input = new byte[BUFFER_SIZE / readSize * readSize];//Files.readAllBytes(new File(pad).toPath());
            tempInput = new byte[input.length];
            File f = new File(pad);
            Path path = f.toPath();
            FileInputStream inputFile = new FileInputStream(path.toFile());
            long parsed_value;
            long transformed_value;
            long previous_value;
            int read;
            writer.buildContextModels();
            int start = writer.getNumberOfBytesWritten();
            writer.writeAsBinary(4, 8);
            transformMapReUse = Tools.ProcessingTools.generateTransform(pad, 1);
            writer.writeLUT(transformMapReUse, 8);
            writer.writeAsBinary(readSize, 8);
            int transformMapSize = transformMapReUse.size();
            int i = 0;
            int transferredBytes = 0;
            int tmp = 0;
            while ((read = inputFile.read(tempInput)) > 0) {
                if (f.length() < input.length) {
                    input = new byte[(int) f.length()];
//                    System.gc();
                    System.arraycopy(tempInput, 0, input, 0, (int) f.length());
                } else if (read < input.length) {
                    System.arraycopy(tempInput, 0, input, readSize, read);
                } else {
                    System.arraycopy(tempInput, 0, input, transferredBytes, tempInput.length);
                }
                tmp = read + readSize;
                for (; i < read || (i < tmp && transferredBytes != 0); i++) {
                    parsed_value = readValue(input, i, 1);
                    transformed_value = transformMapReUse.get((long) (parsed_value));
                    if (i < readSize && transferredBytes == 0) {
                        writer.writeAsTruncCabac((int) transformed_value, transformMapSize, 0);
                    } else {
                        previous_value = transformMapReUse.get((long) input[Math.max(0, i - readSize)]);
                        if (transformed_value == previous_value) {
                            writer.writeAsFlagCabac(1, Math.min(5, (int) previous_value));
                        } else {
                            writer.writeAsFlagCabac(0, Math.min(5, (int) previous_value));
                            if (transformed_value > previous_value) {
                                writer.writeAsTruncCabac((int) transformed_value - 1, transformMapSize, Math.min(5, (int) previous_value));
                            } else {
                                writer.writeAsTruncCabac((int) transformed_value, transformMapSize, Math.min(5, (int) previous_value));
                            }
                        }
                    }
                }
                if (i == BUFFER_SIZE / readSize * readSize) {
                    System.arraycopy(input, i - readSize, input, 0, readSize);
                    if (tempInput.length != input.length - readSize) {
                        tempInput = new byte[input.length - readSize];
                        System.gc();
                    }
                }
                i = readSize;
                transferredBytes = readSize;
            }

            if (printContexts) {
                writer.printContextModels();
            }
            System.out.println("compressed size: " + (writer.getNumberOfBytesWritten() - start));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        input = null;
        tempInput = null;
    }

    private void encodePosLengthLargeAlph(String pad, int windowSize) {
        System.out.println("encodePosLengthLargeAlph with window: " + windowSize);
//        if(true)
//            return;
        int start = writer.getNumberOfBytesWritten();
        writer.writeAsBinary(3, 8);
        long read;
        FileInputStream inputFile;

        File f = new File(pad);
        Path path = f.toPath();
        if (f.length() < BUFFER_SIZE) {
            input = new byte[(int) f.length()];
        } else {
            input = new byte[BUFFER_SIZE];
        }
        try {
            writer.buildContextModels();
            int temp;
            int counter = 0;
            inputFile = new FileInputStream(path.toFile());
            int pos_largest_match, length_largest_match;
            long startTime = System.currentTimeMillis();
            int i, k;
            int tmpPos;
            String temp2, temp3;
            int max = 0;
            long max2 = 0;
            int starting_pos = 0;
            startTime = System.currentTimeMillis();
            while ((read = inputFile.read(input)) > 0) {
//                if (counter != 0) {
//                    System.out.println(counter * BUFFER_SIZE + " of " + f.length() + " Bytes or " + 1.0 * Math.round((1.0 * (counter * BUFFER_SIZE) / f.length()) * 10000) / 100 + "% in " + 1.0 * (System.currentTimeMillis() - startTime) / 1000 + " seconds");
//                }
//                counter++;
                temp2 = new String(input, "ISO-8859-1");
//                input = null;
//                System.gc();
                pos_largest_match = 0;
                length_largest_match = 0;

                for (i = 0; i < read; i++) {
                    starting_pos = Math.max(0, i - windowSize);
                    temp3 = temp2.substring(starting_pos, i);
                    max2 = read - i;
                    for (k = 2; (k < 3 || k < length_largest_match + 2) && k < max2; k++) {//look for longer matches, until for the previous x lengths no match was found{                        
//                        tmpPos=org.apache.commons.lang3.StringUtils.lastIndexOf(temp3, temp2.substring(i, i + k));
                        tmpPos = temp3.lastIndexOf(temp2.substring(i, i + k));
                        if (tmpPos != -1) {
                            max = Math.min(temp3.length() - tmpPos, temp2.length() - i);
                            while (k < max && temp3.charAt(tmpPos + k) == temp2.charAt(i + k)) {
                                k++;
                            }
                            length_largest_match = k;
                            pos_largest_match = tmpPos + starting_pos;//tmpPos; 

                        } else {
                            break;
                        }
                    }
                    if (length_largest_match < 2) {
                        writer.writeAsTruncExpGolCabac(0, 3, 0);//length=0
//                        System.out.println(i);
                        writer.writeAsTruncExpGolCabac(temp2.charAt(i), 16, 0);
//                        writer.writeAsTruncExpGolCabac(input[i], 16, 0);
                    } else {
                        if ((length_largest_match & 0x1) == 0) {
                            writer.writeAsTruncExpGolCabac((length_largest_match) >> 1, 3, 0);
                            writer.writeAsFlagCabac(0, 0);

                        } else {
                            writer.writeAsTruncExpGolCabac((length_largest_match) >> 1, 3, 0);
                            writer.writeAsFlagCabac(1, 0);
                        }
                        temp = i - pos_largest_match - length_largest_match;
                        if ((temp & 0x1) == 0) {
                            writer.writeAsExpGolCabac(temp >> 1, 1);
                            writer.writeAsFlagCabac(0, 1);

                        } else {
                            writer.writeAsExpGolCabac(temp >> 1, 1);
                            writer.writeAsFlagCabac(1, 1);
                        }
                        i += length_largest_match - 1;
                    }
                    pos_largest_match = 0;
                    length_largest_match = 0;
                }
                temp2 = null;
                System.gc();
                input = new byte[BUFFER_SIZE];
            }

            if (printContexts) {
                writer.printContextModels();
            }
            if (writer.getNumberOfBytesWritten() - start < best_size) {
                System.out.println("compressed size: " + (writer.getNumberOfBytesWritten() - start) + " !!!BEST!!!");
                best_size = writer.getNumberOfBytesWritten() - start;
            } else {
                System.out.println("compressed size: " + (writer.getNumberOfBytesWritten() - start));
            }
        } catch (Exception e) {
            System.out.println("error");
        }

    }


    private void encodeGenericGRCOMPDecomposed(String pad, int wordSize, boolean transform, int binarization, int prev, int aidParameter, int cycleSize, boolean equalFlag, boolean diffCodingFlag) {
        if (ultraFastMode) {
//            System.out.println("OVERRULE: GRCOMP not allowed in ultrafast => generic");
            encodeGeneric(pad, wordSize, transform, binarization, prev, aidParameter, cycleSize, equalFlag, diffCodingFlag);
        } else {
//            System.out.println("encodeGenericGRCOMP");
            FileInputStream inputFile;
            File f = new File(pad);
            Path path = f.toPath();
            if (f.length() < BUFFER_SIZE) {
                input = new byte[(int) f.length()];
            } else {
                input = new byte[BUFFER_SIZE];
            }
            int read;
            long parsed_value, transformed_value, write_value;
            boolean allSame = false;
            long min = Long.MAX_VALUE;
            long max = Long.MIN_VALUE;
            int previous_value = 0;
            int contextSet;
            int parameter;
            long[] minmax = new long[2];
            writer.buildContextModels();
            int start = writer.getNumberOfBytesWritten();
            writer.writeAsBinary(1, 8);
            try {
                if (transform) {
                    if (resetTransform || transformMapReUse.isEmpty() || transformMaps.length != transformMapReUse.size()) {
                        transformMapReUse = Tools.ProcessingTools.generateTransform(pad, wordSize);
                        transformMaps = Tools.ProcessingTools.generateTransformMapsPrev(pad, wordSize, transformMapReUse);
                    }
                    //minmax = defineMinMax(transformMapReUse);
                    //if this fails, reuse above line.
                    minmax[0] = 0;
                    minmax[1] = transformMapReUse.size() - 1;
                } else {
                    minmax = defineMinMax(new FileInputStream(path.toFile()), wordSize, binarization);
                }
                min = minmax[0] & 0xffffffff;
                max = minmax[1] & 0xffffffff;
            } catch (Exception e) {
                System.out.println("error while defining minmax: " + e.getMessage());
            }
            printDataOneLine(pad, wordSize, transform, binarization, prev, aidParameter, cycleSize, equalFlag, diffCodingFlag);
            writeHeaderRCOMP(binarization, diffCodingFlag, equalFlag, max, min, allSame, aidParameter, transform, transformMapReUse, transformMaps, wordSize, prev, cycleSize);
            System.out.println("Header Size: " + (writer.getNumberOfBytesWritten() - start));
            int cycleSize_power = Binarisation.bitLength(cycleSize);
            if (cycleSize_power == 1) {
                cycleSize_power = 0x00;
            } else if (cycleSize_power == 2) {
                cycleSize_power = 0x01;
            } else if (cycleSize_power == 4) {
                cycleSize_power = 0x03;
            }
            if (!allSame) {
                try {
                    System.gc();
                    inputFile = new FileInputStream(path.toFile());
                    int i = 0;
                    int contextCycleOffset = 0;
                    int tmp = 0;
                    while ((read = inputFile.read(input)) > 0) {
                        tmp = read - wordSize;
                        i = 0;
                        contextCycleOffset = 0;

                        if (!transform && !equalFlag && !diffCodingFlag) {
                            parameter = defineParameter(transform, binarization, aidParameter, transformMapReUse, max);
                            for (i = 0; i <= tmp; i += wordSize) {
                                contextCycleOffset = i & cycleSize_power;//this actually is incorrect for integers and shorts as i+=wordSize
                                parsed_value = readValue(input, i, wordSize);
                                contextSet = Math.min(previous_value, prev) + contextCycleOffset;
                                writeValue(binarization, parsed_value, parameter, contextSet);
                                previous_value = (int) parsed_value;
                            }
                        }

                        if (transform && !equalFlag && !diffCodingFlag) {
                            parameter = defineParameter(transform, binarization, aidParameter, transformMapReUse, max);
                            for (i = 0; i <= tmp; i += wordSize) {
                                contextCycleOffset = i & cycleSize_power;
                                parsed_value = readValue(input, i, wordSize);
                                write_value = transformMaps[previous_value].get(parsed_value);
                                contextSet = Math.min(previous_value, prev) + contextCycleOffset;
                                writeValue(binarization, write_value, parameter, contextSet);
                                previous_value = (int) transformMapReUse.get((long) parsed_value);//possible error here
                            }
                        }

                        if (!transform && !equalFlag && diffCodingFlag) {
                            parameter = defineParameter(transform, binarization, aidParameter, transformMapReUse, max);
                            for (i = 0; i <= tmp; i += wordSize) {
                                contextCycleOffset = i & cycleSize_power;
                                transformed_value = readValue(input, i, wordSize);
                                contextSet = Math.min(previous_value, prev) + contextCycleOffset;
                                write_value = previous_value - transformed_value;
                                writeValue(binarization, write_value, parameter, contextSet);
                                previous_value = (int) transformed_value;
                            }
                        }

                        if (transform && !equalFlag && diffCodingFlag) {
                            parameter = defineParameter(transform, binarization, aidParameter, transformMapReUse, max);
                            for (i = 0; i <= tmp; i += wordSize) {
                                contextCycleOffset = i & cycleSize_power;
                                parsed_value = readValue(input, i, wordSize);
                                transformed_value = transformMaps[previous_value].get(parsed_value);
                                contextSet = Math.min(previous_value, prev) + contextCycleOffset;
                                write_value = previous_value - transformed_value;
                                writeValue(binarization, write_value, parameter, contextSet);
                                previous_value = (int) transformMapReUse.get((long) parsed_value);//possible error here
                            }
                        }

                        if (transform && equalFlag && !diffCodingFlag) {
                            parameter = defineParameter(transform, binarization, aidParameter, transformMapReUse, max);
                            for (i = 0; i <= tmp; i += wordSize) {
                                contextCycleOffset = i & cycleSize_power;
                                parsed_value = readValue(input, i, wordSize);
                                transformed_value = transformMaps[previous_value].get(parsed_value);
                                contextSet = Math.min(previous_value, prev) + contextCycleOffset;
                                if (previous_value == transformed_value) {
                                    writer.writeAsFlagCabac(1, contextSet);
                                } else {
                                    writer.writeAsFlagCabac(0, contextSet);
                                }
                                write_value = transformed_value;
                                if (previous_value != write_value) {
                                    if (write_value > previous_value) {
                                        write_value--;
                                    }
                                    writeValue(binarization, write_value, parameter, contextSet);
                                    previous_value = (int) transformMapReUse.get((long) parsed_value);//possible error here
                                }
                            }
                        }

                        if (!transform && equalFlag && diffCodingFlag) {
                            parameter = defineParameter(transform, binarization, aidParameter, transformMapReUse, max);
                            for (i = 0; i <= tmp; i += wordSize) {
                                contextCycleOffset = i & cycleSize_power;
                                transformed_value = readValue(input, i, wordSize);
                                contextSet = Math.min(previous_value, prev) + contextCycleOffset;
                                if (previous_value == transformed_value) {
                                    writer.writeAsFlagCabac(1, contextSet);
                                } else {
                                    writer.writeAsFlagCabac(0, contextSet);
                                }
                                write_value = previous_value - transformed_value;
                                if (previous_value != write_value) {
                                    if (write_value > previous_value) {
                                        write_value--;
                                    }
                                    writeValue(binarization, write_value, parameter, contextSet);
                                    previous_value = (int) transformed_value;
                                }
                            }
                        }

                        if (transform && equalFlag && diffCodingFlag) {
                            parameter = defineParameter(transform, binarization, aidParameter, transformMapReUse, max);
                            for (i = 0; i <= tmp; i += wordSize) {
                                contextCycleOffset = i & cycleSize_power;
                                parsed_value = readValue(input, i, wordSize);
                                transformed_value = transformMaps[previous_value].get(parsed_value);
                                contextSet = Math.min(previous_value, prev) + contextCycleOffset;
                                if (previous_value == transformed_value) {
                                    writer.writeAsFlagCabac(1, contextSet);
                                } else {
                                    writer.writeAsFlagCabac(0, contextSet);
                                }
                                write_value = previous_value - transformed_value;
                                if (write_value > previous_value) {
                                    write_value--;
                                }
                                writeValue(binarization, write_value, parameter, contextSet);
                                previous_value = (int) transformMapReUse.get((long) parsed_value);//possible error here
                            }
                        }
                        System.gc();
                        //if a file is processed as shorts but the file itself is not even length, write the last value as binary
                        if (wordSize == 2 && i == read - 1) {
                            writer.writeAsBinary(input[read - 1], 8);
                        }
                        if (wordSize == 4 && i == read - 3) {
                            writer.writeAsBinary(readValue(input, i, 2), 16);
                        }
                    }
                } catch (Exception ex) {
                    System.out.println("Error " + ex.getMessage());
                }
                if (printContexts) {
                    writer.printContextModels();
                }
            }
            input = null;
            tempInput = null;
            transformMaps = null;
            System.out.println("compressed size: " + (writer.getNumberOfBytesWritten() - start));

        }
    }

    private void encodeGenericGRCOMP(String pad, int wordSize, boolean transform, int binarization, int prev, int aidParameter, int cycleSize, boolean equalFlag, boolean diffCodingFlag) {
//        System.out.println(prev+" "+aidParameter);
//        prev=aidParameter;
//        aidParameter=31;
        if (equalFlag && diffCodingFlag) {
            System.out.println("equal and diff coding should not be used together");
        }
        if (ultraFastMode) {
//            System.out.println("OVERRULE: GRCOMP not allowed in ultrafast => generic");
            encodeGeneric(pad, wordSize, transform, binarization, prev, aidParameter, cycleSize, equalFlag, diffCodingFlag);
        } else {
            System.out.println("encodeGenericGRCOMP");
            FileInputStream inputFile;
            File f = new File(pad);
            Path path = f.toPath();
            if (f.length() < BUFFER_SIZE) {
                input = new byte[(int) f.length()];
            } else {
                input = new byte[BUFFER_SIZE];
            }
            int read;
            long parsed_value, transformed_value, write_value;
            boolean allSame = false;
            long min = Long.MAX_VALUE;
            long max = Long.MIN_VALUE;
            int previous_value = 0;
            int contextSet;
            int parameter;
            long[] minmax = new long[2];
            writer.buildContextModels();
            int start = writer.getNumberOfBytesWritten();
            writer.writeAsBinary(1, 8);
            try {
                if (transform) {
//                    long a = System.currentTimeMillis();
                    if (resetTransform || transformMapReUse.isEmpty() || transformMaps.length != transformMapReUse.size()) {
                        transformMapReUse = Tools.ProcessingTools.generateTransform(pad, wordSize);
                        transformMaps = Tools.ProcessingTools.generateTransformMapsPrev(pad, wordSize, transformMapReUse);
                    }
//                    System.out.println(System.currentTimeMillis()-a);
                    //minmax = defineMinMax(transformMapReUse);
                    //if this fails, reuse above line.
                    minmax[0] = 0;
                    minmax[1] = transformMapReUse.size() - 1;
                } else {
                    minmax = defineMinMax(new FileInputStream(path.toFile()), wordSize, binarization);
                }
                min = minmax[0] & 0xffffffff;
                max = minmax[1] & 0xffffffff;
                if (binarization == 0 & max > 31) {
                    System.out.println("alphabet size too large for TU");
                    return;
                }
            } catch (Exception e) {
                System.out.println("error while defining minmax: " + e.getMessage());
            }
            printDataOneLine(pad, wordSize, transform, binarization, prev, aidParameter, cycleSize, equalFlag, diffCodingFlag);
//            if (true) {
//                return;
//            }
            writeHeaderRCOMP(binarization, diffCodingFlag, equalFlag, max, min, allSame, aidParameter, transform, transformMapReUse, transformMaps, wordSize, prev, cycleSize);
            System.out.println("Header Size: " + (writer.getNumberOfBytesWritten() - start));
            int cycleSize_power = Binarisation.bitLength(cycleSize);
            if (cycleSize_power == 1) {
                cycleSize_power = 0x00;
            } else if (cycleSize_power == 2) {
                cycleSize_power = 0x01;
            } else if (cycleSize_power == 4) {
                cycleSize_power = 0x03;
            }
            if (!allSame) {
                try {
                    System.gc();
                    inputFile = new FileInputStream(path.toFile());
                    int i = 0;
                    int contextCycleOffset = 0;
                    int tmp = 0;
                    parameter = defineParameter(transform, binarization, aidParameter, transformMapReUse, max);
                    while ((read = inputFile.read(input)) > 0) {
                        tmp = read - wordSize;
                        i = 0;
                        contextCycleOffset = 0;
                        for (i = 0; i <= tmp; i += wordSize) {
                            contextCycleOffset = (contextCycleOffset + 1) & cycleSize_power;
                            parsed_value = readValue(input, i, wordSize);
                            //if transform is enabled, transform the value
                            if (transform) {
                                transformed_value = transformMaps[previous_value].get(parsed_value);
                            } else {
                                transformed_value = parsed_value;
                            }

                            contextSet = Math.min(previous_value, prev) + contextCycleOffset;
                            if (equalFlag) {
                                if (previous_value == transformed_value) {
                                    writer.writeAsFlagCabac(1, contextSet);
                                } else {
                                    writer.writeAsFlagCabac(0, contextSet);
                                }
                            }

                            if (diffCodingFlag) {
                                write_value = previous_value - transformed_value;
                            } else {
                                write_value = transformed_value;
                            }
                            if (!equalFlag || equalFlag && previous_value != write_value) {
                                if (write_value > previous_value && equalFlag) {
                                    write_value--;
                                }
                                writeValue(binarization, write_value, parameter, contextSet);
                                if (transform) {
                                    previous_value = (int) transformMapReUse.get((long) parsed_value); //possible error
                                } else {
                                    previous_value = (int) parsed_value;
                                }
                            }
                        }
                        System.gc();
                        //if a file is processed as shorts but the file itself is not even length, write the last value as binary
                        if (wordSize == 2 && i == read - 1) {
                            writer.writeAsBinary(input[read - 1], 8);
                        }
                        if (wordSize == 4 && i == read - 3) {
                            writer.writeAsBinary(readValue(input, i, 2), 16);
                        }
                    }
                } catch (Exception ex) {
                    System.out.println("Error " + ex.getMessage());
                }
                if (printContexts) {
                    writer.printContextModels();
                }
            }
            input = null;
            tempInput = null;
//Terug enablen TPAR            transformMaps = null;
            if (writer.getNumberOfBytesWritten() - start < best_size) {
                System.out.println("compressed size: " + (writer.getNumberOfBytesWritten() - start) + " !!!BEST!!!");
                best_size = writer.getNumberOfBytesWritten() - start;
            } else {
                System.out.println("compressed size: " + (writer.getNumberOfBytesWritten() - start));
            }

        }
    }

    private void printDataOneLine(String pad, int wordSize, boolean transform, int binarization, int prev, int aidParameter, int cycleSize, boolean equalFlag, boolean diffCodingFlag) {
        String wordsize = "";
        String bin = "";
        String val0 = "";
        String val1 = "";
        String val2 = "";
        if (wordSize == 1) {
            wordsize = "Byte";
        } else if (wordSize == 2) {
            wordsize = "Short";
        } else if (wordSize == 4) {
            wordsize = "Int";
        }
        if (binarization == 0) {
            bin = "TU";
        } else if (binarization == 1) {
            bin = "EG";
        } else if (binarization == 2) {
            bin = "BI";
        } else if (binarization == 3) {
            bin = "TEG";
        } else if (binarization == 4) {
            bin = "SEG";
        } else if (binarization == 5) {
            bin = "STEG";
        }
        if (transform) {
            val0 = "Trans";
        } else {
            val0 = "NoTrans";
        }
        if (equalFlag) {
            val1 = "Equal";
        } else {
            val1 = "NoEqual";
        }
        if (diffCodingFlag) {
            val2 = "Diff";
        } else {
            val2 = "NoDiff";
        }
        System.out.print(pad + "\t" + wordsize + "\t" + bin + "\t" + aidParameter + "\t" + val0 + "\t" + val1 + "\t" + val2 + "\tprev" + prev + "\tc" + cycleSize + ":\t");
    }

    private long[] defineMinMax(Long2LongOpenHashMap transformMap) {
        long[] minMax = new long[]{Long.MAX_VALUE, Long.MIN_VALUE};//first value=min, second value = max        
        long value;
        Object[] temp = transformMap.values().toArray();
        for (int i = 0; i < temp.length; i++) {
            value = (long) temp[i] & 0xffffffffL;
            if (value < minMax[0]) {
                minMax[0] = value;
            }
            if (value > minMax[1]) {
                minMax[1] = value;
            }
        }
        temp = null;

        return minMax;
    }

    private long[] defineMinMax(FileInputStream inputFile, int wordSize, int binarization) {
        long[] minMax = new long[]{Long.MAX_VALUE, Long.MIN_VALUE};//first value=min, second value = max        
        input = new byte[BUFFER_SIZE];
        int read = 0;
        long parsed_value = 0;
        long value;
        try {
            int i = 0;
            while ((read = inputFile.read(input)) > 0) {
                for (i = 0; i < read - wordSize + 1; i += wordSize) {
                    //read next value
                    parsed_value = readValue(input, i, wordSize);
                    value = parsed_value & 0xffffffffL;
                    if (value < minMax[0]) {
                        minMax[0] = value;
                    }
                    if (value > minMax[1]) {
                        minMax[1] = value;
                    }
                }
            }
        } catch (Exception e) {
        }
        return minMax;
    }

    private void writeHeader(int binarization, boolean diffCodingFlag, boolean equalFlag, long max, long min, boolean allSame, int aidParameter, boolean transform, Long2LongOpenHashMap transformMap, int wordSize, int prev, int cycleSize) {
        writer.writeAsBinary(wordSize - 1, 8);
        writer.writeAsBinary(binarization, 8);
        if (diffCodingFlag) {
            writer.writeAsFlag(1);
        } else {
            writer.writeAsFlag(0);
        }
        if (equalFlag) {
            writer.writeAsFlag(1);
        } else {
            writer.writeAsFlag(0);
        }
        if (binarization == 0) {
            writer.writeAsBinary(max, 8);
            if (max == min) {
                writer.writeAsBinary(min, 8);
                allSame = true;
            }
        }
        if (binarization == 2) {
            writer.writeAsBinary(Long.toBinaryString(max).length(), 8);
        }
        if (binarization == 3 || binarization == 5) {
            writer.writeAsBinary(aidParameter, 8);
        }

        if (transform) {
            writer.writeAsFlag(1);
        } else {
            writer.writeAsFlag(0);
        }
        if (transform) {
            writer.writeLUT(transformMap, wordSize * 8);// Long.toBinaryString(max).length());//wordSize * 8);
        }

        writer.writeAsBinary(prev, 8);
        writer.writeAsBinary(cycleSize, 8);

    }

    private void writeHeaderRCOMP(int binarization, boolean diffCodingFlag, boolean equalFlag, long max, long min, boolean allSame, int aidParameter, boolean transform, Long2LongOpenHashMap transformMap, Long2LongOpenHashMap[] transformMaps, int wordSize, int prev, int cycleSize) {
        writer.writeAsBinary(wordSize - 1, 8);
        writer.writeAsBinary(binarization, 8);
        if (diffCodingFlag) {
            writer.writeAsFlag(1);
        } else {
            writer.writeAsFlag(0);
        }
        if (equalFlag) {
            writer.writeAsFlag(1);
        } else {
            writer.writeAsFlag(0);
        }
        if (binarization == 0) {
            writer.writeAsBinary(max, 8);
            if (max == min) {
                writer.writeAsBinary(min, 8);
                allSame = true;
            }
        }
        if (binarization == 2) {
            writer.writeAsBinary(Long.toBinaryString(max).length(), 8);
        }
        if (binarization == 3 || binarization == 5) {
            writer.writeAsBinary(aidParameter, 8);
        }

        if (transform) {
            writer.writeAsFlag(1);
        } else {
            writer.writeAsFlag(0);
        }
        if (transform) {
            writer.writeLUT((Long2LongOpenHashMap) transformMap, wordSize * 8);// Long.toBinaryString(max).length());//wordSize * 8);
            for (int i = 0; i < transformMaps.length; i++) {
                writer.writeLUT((Long2LongOpenHashMap) transformMaps[i], wordSize * 8);// Long.toBinaryString(max).length());//wordSize * 8);
            }
        }

        writer.writeAsBinary(prev, 8);
        writer.writeAsBinary(cycleSize, 8);

    }

    public long readValue(byte[] input, int i, int wordSize) {
        if (wordSize == 1) {
            return input[i] & 0xff;
        } else if (wordSize == 2) {
            return ((input[i + 1] & 0xff) << 8) | (input[i] & 0xff);
//            return ((input[i + 1]) << 8) | input[i];
        } else if (wordSize == 4) {
//            int a=((input[i + 3] & 0xff) << 24) + ((input[i + 2] & 0xff) << 16) + ((input[i + 1] & 0xff) << 8) + (input[i] & 0xff);
//          int b=((input[i + 3] & 0xff) << 24) | ((input[i + 2] & 0xff) << 16) | ((input[i + 1] & 0xff) << 8) | (input[i] & 0xff);
//          if(a!=b){
//              System.out.println(a+"  "+b);
//            System.out.println(" ---------------------- ");
//          }
//            return (((input[i + 3] & 0xff) << 24) + ((input[i + 2] & 0xff) << 16) + ((input[i + 1] & 0xff) << 8) + (input[i] & 0xff));
            return ((input[i + 3] & 0xff) << 24) | ((input[i + 2] & 0xff) << 16) | ((input[i + 1] & 0xff) << 8) | (input[i] & 0xff);
//            return ((input[i + 3] << 24) | (input[i + 2] << 16) | (input[i + 1]<< 8) | input[i])&0xffffffff;
        } else {
            return Integer.MAX_VALUE;
        }
    }

    public int defineParameter(boolean transform, int binarization, int aidParameter, Long2LongOpenHashMap transformMap, long max) {
        //define the parameter
        if (binarization == 3 || binarization == 5) {
            return aidParameter;
        } else if (binarization == 2) {
            if (transform) {
                return Long.toBinaryString(transformMap.size()).length();
            } else {
                return Long.toBinaryString(max).length();
            }
        } else {
            if (transform) {
                return transformMap.size();
            } else {
                return (int) max;//can only happen when max <32;
            }
        }
    }

    public void writeValue(int binarization, long write_value, int parameter, int contextSet) {
        if (binarization == 3) {
            writer.writeAsTruncExpGolCabac((int) write_value, parameter, contextSet);
        } else if (binarization == 0) {
            writer.writeAsTruncCabac((int) write_value, parameter, contextSet);
        } else if (binarization == 1) {
            writer.writeAsExpGolCabac((int) write_value, contextSet);
        } else if (binarization == 2) {
            writer.writeAsBinaryCabac((int) write_value, parameter, contextSet);
        } else if (binarization == 4) {
            writer.writeAsSignedExpGolCabac((int) write_value, contextSet);
        } else if (binarization == 5) {
            writer.writeAsSignedTruncExpGolCabac((int) write_value, parameter, contextSet);
        }
    }

    public void writeValue(int binarization, int write_value, int parameter, int contextSet) {
        if (binarization == 3) {
            writer.writeAsTruncExpGolCabac(write_value, parameter, contextSet);
        } else if (binarization == 0) {
            writer.writeAsTruncCabac(write_value, parameter, contextSet);
        } else if (binarization == 1) {
            writer.writeAsExpGolCabac(write_value, contextSet);
        } else if (binarization == 2) {
            writer.writeAsBinaryCabac(write_value, parameter, contextSet);
        } else if (binarization == 4) {
            writer.writeAsSignedExpGolCabac(write_value, contextSet);
        } else if (binarization == 5) {
            writer.writeAsSignedTruncExpGolCabac(write_value, parameter, contextSet);
        }
    }

    private void encodeLen(String pad) {
        System.out.println("encodeLen");
//        if(true)
//            return;
        int start = writer.getNumberOfBytesWritten();
        writer.buildContextModels();
        writer.writeAsBinary(7, 8);
        FileInputStream inputFile;
        File f = new File(pad);
        Path path = f.toPath();
        if (f.length() < BUFFER_SIZE) {
            input = new byte[(int) f.length()];
        } else {
            input = new byte[BUFFER_SIZE];
        }
        int read = 0;
        int value = 0;
        long min_value = Long.MAX_VALUE;
        long max_value = 0;
        long tmp = 0;
        try {
            inputFile = new FileInputStream(path.toFile());
            int i = 0;
            while ((read = inputFile.read(input)) > 0) {
                tmp = read - 3;
                for (i = 0; i < tmp; i += 4) {
                    value = ((input[i + 3] & 0xff) << 24) + ((input[i + 2] & 0xff) << 16) + ((input[i + 1] & 0xff) << 8) + (input[i + 0] & 0xff);
                    if (value < min_value) {
                        min_value = value;
                    }
                    if (value > max_value) {
                        max_value = value;
                    }
                }
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
//        System.out.println("range: " + min_value + "-" + max_value);
        if (f.length() < BUFFER_SIZE) {
            input = new byte[(int) f.length()];
        } else {
            input = new byte[BUFFER_SIZE];
        }
        read = 0;
        value = 0;
        int bit_size = Long.toBinaryString(max_value - min_value).length();
        int offset = 64;
        writer.writeAsBinary(bit_size, 8);
        writer.writeAsBinary(min_value, 32);
        writer.writeAsBinary(offset, 16);
        int prev = 0;
        long tmp2 = 0;
        try {
            inputFile = new FileInputStream(path.toFile());
            int i = 0;
            while ((read = inputFile.read(input)) > 0) {
                tmp = read - 3;
                for (i = 0; i < tmp; i += 4) {
                    value = ((input[i + 3] & 0xff) << 24) + ((input[i + 2] & 0xff) << 16) + ((input[i + 1] & 0xff) << 8) + (input[i + 0] & 0xff);
                    tmp2 = value - min_value;
                    if (tmp2 < offset) {
                        writer.writeAsFlagCabac(1, 0);
                        writer.writeAsTruncExpGolCabac((int) (tmp2), 1, 0);
                    } else {
                        writer.writeAsFlagCabac(0, 0);
                        writer.writeAsBinaryCabac((int) (tmp2 - offset), bit_size, 0);
                        prev = value;
                    }

                }
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        if (printContexts) {
            writer.printContextModels();
        }
        input = null;
        tempInput = null;
        System.out.println(
                "compressed size: " + (writer.getNumberOfBytesWritten() - start));
    }

    public void encodePairs(String pad, int mode) {
        System.out.println("encodePairs");
//        if(true)
//            return;
        int start = writer.getNumberOfBytesWritten();
        boolean isPairDistance;
        int prev_predictor = 0;
        //tbd: pair descriptors should be reordered
        writer.buildContextModels();
        writer.writeAsBinary(5, 8);
        if (mode == 0) {
            writer.writeAsFlag(0);
        } else {
            writer.writeAsFlag(1);
        }
        int value;
        long[] occurrences = new long[256 * 256];
        for (int i = 0; i < 256 * 256; i++) {
            occurrences[i] = 0;
        }
        Long2LongOpenHashMap transformMap;
        FileInputStream inputFile;
        File f = new File(pad);
        Path path = f.toPath();
        if (f.length() < BUFFER_SIZE) {
            input = new byte[(int) f.length()];
        } else {
            input = new byte[BUFFER_SIZE];
        }
        tempInput = new byte[input.length];
        int[] descriptorCounters = new int[]{0, 0, 0, 0, 0, 0, 0, 0};
        int read;
        long tmp;
        try {
            inputFile = new FileInputStream(path.toFile());
            int transferredBytes = 0;
            int i;
            while ((read = inputFile.read(tempInput)) > 0) {
                System.arraycopy(tempInput, 0, input, transferredBytes, tempInput.length);
                tempInput = null;
                System.gc();
//                System.out.println("read new piece");
                tmp = read + transferredBytes;
                for (i = 0; i < tmp - 11 || (tmp != BUFFER_SIZE && i < tmp); i++) {
                    isPairDistance = false;
                    if ((input[i + 1] & 0xff) == 0x7f) {
                        if ((input[i] & 0xff) == 0xfd) {
                            descriptorCounters[7]++;
                            i += 5;
                        } else if ((input[i] & 0xff) == 0xfe) {
                            descriptorCounters[2]++;
                            i += 6;
                        } else if ((input[i] & 0xff) == 0xff) {
                            descriptorCounters[4]++;
                            i++;
                        } else {
                            isPairDistance = true;
                        }
                    } else if ((input[i + 1] & 0xff) == 0x80) {
                        if ((input[i] & 0xff) == 0x03) {
                            descriptorCounters[1]++;
                            i += 5;
                        } else if ((input[i] & 0xff) == 0x02) {
                            descriptorCounters[3]++;
                            i += 6;
                        } else if ((input[i] & 0xff) == 0x01) {
                            descriptorCounters[5]++;
                            i++;
                        } else if ((input[i] & 0xff) == 0x00) {
                            descriptorCounters[6]++;
                            i++;
                        } else {
                            isPairDistance = true;
                        }
                    } else {
                        isPairDistance = true;
                    }
                    if (isPairDistance) {
                        descriptorCounters[0]++;
                        occurrences[(((input[i + 1] & 0xff) << 8) | (input[i] & 0xff))]++;
                        i++;
                    }
                }
                transferredBytes = read + transferredBytes - i;
                System.arraycopy(input, i, input, 0, transferredBytes);
                tempInput = new byte[input.length - transferredBytes];
                System.gc();
            }

            transformMap = Tools.ProcessingTools.generateTransformFromHistogram(occurrences);
            occurrences = null;
            writer.writeLUT(transformMap, 16);
            read = 0;
            transferredBytes = 0;
//            if (f.length() < BUFFER_SIZE) {
//                input = new byte[(int) f.length()];
//            } else {
//                input = new byte[BUFFER_SIZE];
//            }
            tempInput = new byte[input.length];
            inputFile = new FileInputStream(path.toFile());

            boolean oneDescriptor = false;
            int index = 0;

            for (i = 0; i < descriptorCounters.length; i++) {
                if (descriptorCounters[i] != 0) {
                    if (oneDescriptor == true) {
                        oneDescriptor = false;
                        break;
                    }
                    oneDescriptor = true;
                    index = i;
                }
            }

            if (oneDescriptor) {
                writer.writeAsFlag(1);
                writer.writeAsBinary(index, 3);
                writer.writeAsBinary(descriptorCounters[index], 32);
            } else {
                writer.writeAsFlag(0);

            }
            int prevShortID = 0;

            while ((read = inputFile.read(tempInput)) > 0) {
                System.arraycopy(tempInput, 0, input, transferredBytes, tempInput.length);
                tempInput = null;
                System.gc();
                tmp = read + transferredBytes;
                for (i = 0; i < tmp - 11 || (tmp != BUFFER_SIZE && i < tmp); i++) {
                    isPairDistance = false;
                    if ((input[i + 1] & 0xff) == 0x7f) {
                        if ((input[i] & 0xff) == 0xfd) {
                            if (!oneDescriptor) {
                                writer.writePairDescriptor(7, 7, prev_predictor);
                            }
                            prev_predictor = 7;
                            value = (((input[i + 5] & 0xff) << 24) | ((input[i + 4] & 0xff) << 16) | ((input[i + 3] & 0xff) << 8) | (input[i + 2] & 0xff));
                            writer.writeAsBinaryCabac(value, 32, 0);

                            i += 5;
                        } else if ((input[i] & 0xff) == 0xfe) {
                            if (!oneDescriptor) {
                                writer.writePairDescriptor(2, 7, prev_predictor);
                            }

                            prev_predictor = 2;
                            writer.writeAsBinaryCabac(input[i + 2], 8, 4);

                            value = (((input[i + 6] & 0xff) << 24) | ((input[i + 5] & 0xff) << 16) | ((input[i + 4] & 0xff) << 8) | (input[i + 3] & 0xff));
                            writer.writeAsBinaryCabac(value, 32, 2);
                            i += 6;
                        } else if ((input[i] & 0xff) == 0xff) {
                            if (!oneDescriptor) {
                                writer.writePairDescriptor(4, 7, prev_predictor);
                            }

                            prev_predictor = 4;
                            i++;
                        } else {
                            isPairDistance = true;
                        }
                    } else if ((input[i + 1] & 0xff) == 0x80) {
                        if ((input[i] & 0xff) == 0x03) {
                            if (!oneDescriptor) {
                                writer.writePairDescriptor(1, 7, prev_predictor);
                            }

                            prev_predictor = 1;
                            value = (((input[i + 5] & 0xff) << 24) | ((input[i + 4] & 0xff) << 16) | ((input[i + 3] & 0xff) << 8) | (input[i + 2] & 0xff));
                            writer.writeAsBinaryCabac(value, 32, 1);
                            i += 5;
                        } else if ((input[i] & 0xff) == 0x02) {
                            if (!oneDescriptor) {
                                writer.writePairDescriptor(3, 7, prev_predictor);
                            }

                            prev_predictor = 3;
                            writer.writeAsBinaryCabac(input[i + 2], 8, 4);
                            value = (((input[i + 6] & 0xff) << 24) | ((input[i + 5] & 0xff) << 16) | ((input[i + 4] & 0xff) << 8) | (input[i + 3] & 0xff));
                            writer.writeAsBinaryCabac(value, 32, 3);
                            i += 6;
                        } else if ((input[i] & 0xff) == 0x01) {
                            if (!oneDescriptor) {
                                writer.writePairDescriptor(5, 7, prev_predictor);
                            }
                            prev_predictor = 5;
                            i++;
                        } else if ((input[i] & 0xff) == 0x00) {
                            if (!oneDescriptor) {
                                writer.writePairDescriptor(6, 7, prev_predictor);
                            }
                            prev_predictor = 6;
                            i++;
                        } else {
                            isPairDistance = true;
                        }
                    } else {
                        isPairDistance = true;
                    }
                    if (isPairDistance) {
                        if (!oneDescriptor) {
                            writer.writePairDescriptor(0, 7, prev_predictor);
                        }
                        prev_predictor = 0;
                        value = (((input[i + 1] & 0xff) << 8) | (input[i] & 0xff));
                        if (!transformMap.containsKey((long) value)) {
                            System.out.println(value);
                        }
                        value = (int) transformMap.get((long) value);
                        i++;
                        if (mode == 0) {
                            writer.writeAsExpGolCabac(value, Math.min(4, prevShortID));
                        } else if (mode == 1) {
                            writer.writeAsTruncExpGolCabac(value, 1, Math.min(4, prevShortID) + 1);
                        }
                        prevShortID = value;
                    }
                }

                transferredBytes = read + transferredBytes - i;
                System.arraycopy(input, i, input, 0, transferredBytes);
                tempInput = new byte[input.length - transferredBytes];
                System.gc();
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        if (printContexts) {
            writer.printContextModels();
        }
        input = null;
        tempInput = null;
        //writer.printContextModels();
        if (writer.getNumberOfBytesWritten() - start < best_size) {
            System.out.println("compressed size: " + (writer.getNumberOfBytesWritten() - start) + " !!!BEST!!!");
            best_size = writer.getNumberOfBytesWritten() - start;
        } else {
            System.out.println("compressed size: " + (writer.getNumberOfBytesWritten() - start));
        }
    }

    public void encodeIndc(String pad) {
        System.out.println("encodeIndc");

//        if (true) {
//            return;
//        }
        int start = writer.getNumberOfBytesWritten();
        writer.buildContextModels();
        writer.writeAsBinary(6, 8);
        long[] countersPos = new long[256];
        long[] countersBase = new long[256];
        for (int i = 0; i < 256; i++) {
            countersPos[i] = 0;
            countersBase[i] = 0;
        }

        int id = 0;
        byte position = 0;
        int base;
        int old_id = 0;

        FileInputStream inputFile;
        Path path = new File(pad).toPath();
        input = new byte[BUFFER_SIZE];
        tempInput = new byte[BUFFER_SIZE];
        int read = 0;
        int transferredBytes = 0;

        base = 0x00;
        boolean continueReading = false;
        try {
            inputFile = new FileInputStream(path.toFile());
            int i = 0;
            int tmp = 0;
            int pos = 0;
            while ((read = inputFile.read(tempInput)) > 0) {
                if (read + transferredBytes != BUFFER_SIZE) {
                    input = tempInput;
                } else {
                    System.arraycopy(tempInput, 0, input, transferredBytes, tempInput.length);
                    System.gc();
                }

                tmp = input.length - 6;
                for (; i < tmp;) {
                    base = 0x00;
                    if (!continueReading) {
                        i += 4;
                        pos = input[i++] & 0xff;
                        countersPos[pos]++;
                    }
                    continueReading = false;
                    if (pos > 3) {
                        i += 2;
                        base = input[i++] & 0xff;
                        countersBase[base]++;
                    }
                    while (base < 254 && i < input.length) {
                        base = input[i++] & 0xff;
                        if (base != 0) {
                            countersBase[base]++;
                        }
                    }
                }
                if (base < 254) {
                    continueReading = true;
                }

                transferredBytes = input.length - i;
                System.arraycopy(input, i - 1, input, 0, transferredBytes + 1);
                tempInput = new byte[input.length - transferredBytes];
                System.gc();
                i = 1;
            }

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }

        Long2LongOpenHashMap transformMapPositions;
        Long2LongOpenHashMap transformMapBases;

        transformMapPositions = Tools.ProcessingTools.generateTransformFromHistogram(countersPos);
        transformMapBases = Tools.ProcessingTools.generateTransformFromHistogram(countersBase);
        writer.writeLUT(transformMapPositions, 8);
        writer.writeLUT(transformMapBases, 8);

        byte[] transformMapPosArray = new byte[256];
        byte[] transformMapBaseArray = new byte[256];
        for (int i = 0; i < transformMapPosArray.length; i++) {
            transformMapPosArray[i] = (byte) i;
            transformMapBaseArray[i] = (byte) i;
        }
        byte posTruncLength = (byte) (transformMapPositions.size() - 1);
        Object[] values = transformMapPositions.values().toArray();
        Object[] keys = transformMapPositions.keySet().toArray();
        for (int i = 0; i < keys.length; i++) {
            transformMapPosArray[((Long) keys[i]).byteValue() & 0xff] = ((Long) values[i]).byteValue();
        }
//        transformMapPositions.clear();
        values = transformMapBases.values().toArray();
        keys = transformMapBases.keySet().toArray();
        for (int i = 0; i < keys.length; i++) {
            transformMapBaseArray[((Long) keys[i]).byteValue() & 0xff] = ((Long) values[i]).byteValue();
        }
//        transformMapBases.clear();
        transferredBytes = 0;
        base = (byte) 0x00;
        continueReading = false;
        read = 0;
        long bytecounter = 0;
        int value = 0;
        int tmp = 0;
        try {
            inputFile = new FileInputStream(path.toFile());
            int i = 0;
            while ((read = inputFile.read(tempInput)) > 0) {
                if (read + transferredBytes != BUFFER_SIZE) {
                    System.arraycopy(input, input.length - transferredBytes, input, 0, transferredBytes);
                    System.arraycopy(tempInput, 0, input, transferredBytes, tempInput.length);
//                    input = tempInput;
//                    input = tempInput;
                } else {
                    System.arraycopy(tempInput, 0, input, transferredBytes, tempInput.length);
                    System.gc();
                }
                tmp = input.length - 6;
                for (i = 0; i < tmp;) {
                    base = 0x00;
                    if (!continueReading) {
                        id = (((input[i + 3] & 0xff) << 24) | ((input[i + 2] & 0xff) << 16) | ((input[i + 1] & 0xff) << 8) | (input[i] & 0xff));
                        writer.writeAsSignedTruncExpGolCabac(id - old_id, 18, 0);
                        old_id = id;
                        i += 4;
//                        bytecounter+=4;
                        position = input[i++];
//                        bytecounter++;
                        writer.writeAsTruncCabac(transformMapPosArray[position & 0xff], posTruncLength, 1);
                        //transformMapPositions.get((long) position).intValue(), transformMapPositions.size() - 1, 1);
                    }
                    continueReading = false;

                    if (position > 3) {
                        writer.writeAsBinary(input[i++] & 0xff, 8);
                        writer.writeAsBinary(input[i++] & 0xff, 8);
//                        bytecounter++;
//                        bytecounter++;
                    }
                    while (base < 254 && i < input.length) {
                        base = input[i++] & 0xff;
//                        bytecounter++;
                        if (base != 0) {
                            writer.writeAsTruncExpGolCabac(transformMapBaseArray[base], 3, 2);
                        }
                    }
                }
                if (base < 254) {
                    continueReading = true;
                }

                transferredBytes = input.length - i;
                System.arraycopy(input, i, input, 0, transferredBytes);
                tempInput = new byte[input.length - transferredBytes];
                System.gc();
            }

        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        countersPos = null;
        countersBase = null;
        input = null;
        tempInput = null;
        transformMapPositions = null;
        transformMapBases = null;
        System.out.println("compressed size: " + (writer.getNumberOfBytesWritten() - start));
        if (printContexts) {
            writer.printContextModels();
        }
    }

    public void setFastMode() {
//        System.out.println("FastMode Set");
        fastMode = true;
        ultraMode = false;
        ultraFastMode = false;
    }

    public void setUltraMode() {
//        System.out.println("SlowMode Set");
        fastMode = false;
        ultraMode = true;
        ultraFastMode = false;
    }

    public void setBestMode() {
        fastMode = false;
        ultraMode = false;
        ultraFastMode = false;
    }

    public void setUltraFastMode() {

        fastMode = true;
        ultraMode = false;
        ultraFastMode = true;
    }

    private void encodeAllGenericGRCOMPFull(String pad) {
        int[] wordSizes = new int[]{1};
        boolean[] states = new boolean[]{true, false};
//        int[] binarizations = new int[]{0, 1, 2, 3, 4};
        int[] binarizations = new int[]{0, 1, 2, 3, 4, 5};
        int[] prevs = new int[]{0, 1, 5, 15, 31};
        int[] aidParameters;
        int[] cycleSizes;

//        encodePosLengthLargeAlph(pad, 32768);
        for (int wordSize : wordSizes) {
            if (wordSize == 1) {
                cycleSizes = new int[]{1, 4};
            } else if (wordSize == 2) {
                cycleSizes = new int[]{1};
            } else {
                cycleSizes = new int[]{1};
            }
            for (int cycleSize : cycleSizes) {
                for (boolean equalFlag : states) {
                    resetTransform = true;
                    boolean transform = true;
                    for (int binarization : binarizations) {
                        for (int prev : prevs) {
                            if (binarization == 3 || binarization == 5) {
                                aidParameters = new int[]{1, 5, 15, 31};
                            } else {
                                aidParameters = new int[]{0};
                            }
                            for (int aidParameter : aidParameters) {
                                if (binarization == 4 || binarization == 5) {
                                    if (!equalFlag) {
                                        for (boolean diffCodingflag : states) {
                                            encodeGenericGRCOMP(pad, wordSize, transform, binarization, prev, aidParameter, cycleSize, equalFlag, diffCodingflag);
                                            resetTransform = false;
                                        }
                                    } else {
                                        encodeGenericGRCOMP(pad, wordSize, transform, binarization, prev, aidParameter, cycleSize, equalFlag, false);
                                        resetTransform = false;
                                    }
                                } else {
                                    encodeGenericGRCOMP(pad, wordSize, transform, binarization, prev, aidParameter, cycleSize, equalFlag, false);
                                    resetTransform = false;
                                }
                                writer.reset();
                                System.gc();
                            }
                        }
                    }
//                }
                }
            }
        }
    }

    private void splitPosFile(String pad) throws FileNotFoundException, IOException {
        BufferedOutputStream stream0 = new BufferedOutputStream(new FileOutputStream(pad + ".0"));
        BufferedOutputStream stream1 = new BufferedOutputStream(new FileOutputStream(pad + ".1"));
        BufferedOutputStream stream2 = new BufferedOutputStream(new FileOutputStream(pad + ".2"));
        input = Files.readAllBytes(new File(pad).toPath());
        for (int i = 0; i < input.length - 11;) {
            stream0.write(input[i++]);
            stream0.write(input[i++]);
            stream0.write(input[i++]);
            stream0.write(input[i++]);
            stream1.write(input[i++]);
            stream1.write(input[i++]);
            stream1.write(input[i++]);
            stream1.write(input[i++]);
            stream2.write(input[i++]);
            stream2.write(input[i++]);
            stream2.write(input[i++]);
            stream2.write(input[i++]);

        }
        stream0.close();
        stream1.close();
        stream2.close();

    }

    private void splitPairFile(String pad) throws FileNotFoundException, IOException {
        BufferedOutputStream stream0 = new BufferedOutputStream(new FileOutputStream(pad + ".0"));
        BufferedOutputStream stream1 = new BufferedOutputStream(new FileOutputStream(pad + ".1"));
        BufferedOutputStream stream2 = new BufferedOutputStream(new FileOutputStream(pad + ".2"));
        input = new byte[12 * 1024 * 1024 * 10];
        long read;
        FileInputStream inputFile;
        Path path = new File(pad).toPath();
        try {
            inputFile = new FileInputStream(path.toFile());
            int i = 0;
            int tmp = 0;
            int pos = 0;
            while ((read = inputFile.read(input)) > 0) {
                for (i = 0; i < read - 9;) {
                    stream0.write(input[i++]);
                    stream0.write(input[i++]);
                    stream0.write(input[i++]);
//                stream0.write(input[i++]);
                    stream1.write(input[i++]);
                    stream1.write(input[i++]);
                    stream1.write(input[i++]);
//                stream1.write(input[i++]);
                    stream2.write(input[i++]);
                    stream2.write(input[i++]);
                    stream2.write(input[i++]);
                    stream2.write(input[i++]);

                }
            }
            stream0.close();
            stream1.close();
            stream2.close();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void splitMMapFile(String pad) throws FileNotFoundException, IOException {
        BufferedOutputStream stream0 = new BufferedOutputStream(new FileOutputStream(pad + ".0"));
        BufferedOutputStream stream1 = new BufferedOutputStream(new FileOutputStream(pad + ".1"));
        BufferedOutputStream stream2 = new BufferedOutputStream(new FileOutputStream(pad + ".2"));
        BufferedOutputStream stream3 = new BufferedOutputStream(new FileOutputStream(pad + ".3"));
        input = Files.readAllBytes(new File(pad).toPath());
        for (int i = 0; i < input.length - 12;) {
            stream0.write(input[i++]);
            stream0.write(input[i++]);
            stream0.write(input[i++]);
            stream0.write(input[i++]);
            stream0.write(input[i++]);
            stream0.write(input[i++]);
            stream0.write(input[i++]);
            stream1.write(input[i++]);
            stream1.write(input[i++]);
            stream2.write(input[i++]);
            stream2.write(input[i++]);
            stream3.write(input[i++]);
            stream3.write(input[i++]);

        }
        stream0.close();
        stream1.close();
        stream2.close();
        stream3.close();

    }

}
