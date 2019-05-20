package bittool;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BitOutputStream {

    private int numHeldBits;
    private int heldBits;
    private final OutputStream outputStream;
    private int bytesWritten;

    public BitOutputStream(OutputStream outputStream) {
        this.numHeldBits = 0;
        this.heldBits = 0;
        this.outputStream = outputStream;
        this.bytesWritten = 0;
    }

    private void write(int b) {
        try {
            this.bytesWritten++;
            this.outputStream.write(b);
        } catch (IOException ex) {
            Logger.getLogger(BitOutputStream.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public int getNumberOfBitsWritten() {
        return this.bytesWritten * 8 + this.numHeldBits;
    }

    // should only be called on byte alignment
    public int getNumberOfBytesWritten() {
        if (getNumBitsUntilByteAligned() != 0) {
            System.err.println("dont call getNumberOfBytesWritten when not byte aligned");
        }

        return this.bytesWritten;
    }

    public void write(int bits, int numberOfBits) {

        int numTotalBits = numberOfBits + this.numHeldBits;
        int nextNumHeldBits = numTotalBits &0x07;//% 8;

        int nextHeldBits = bits << (8 - nextNumHeldBits);

        if (numTotalBits < 8) {
            this.heldBits |= nextHeldBits;
            this.numHeldBits = nextNumHeldBits;
        } else {
            int topWord = (numberOfBits - nextNumHeldBits) & ~((1 << 3) - 1);
            int writeBits = (int) (((long) this.heldBits) << topWord)
                    | (bits >>> nextNumHeldBits);

            switch (numTotalBits >>> 3) {
                case 4:
                    write(writeBits >>> 24);
                case 3:
                    write(writeBits >>> 16);
                case 2:
                    write(writeBits >>> 8);
                case 1:
                    write(writeBits);
            }

            this.heldBits = nextHeldBits;
            this.numHeldBits = nextNumHeldBits;
        }

    }

    public void writeBytes(int bits, int numberOfBits) {
            switch (numberOfBits) {
                case 32:
                    write(bits >>> 24);
                case 24:
                    write(bits >>> 16);
                case 16:
                    write(bits >>> 8);
                case 8:
                    write(bits);
            }
    }

    private int getNumBitsUntilByteAligned() {
        return (8 - this.numHeldBits) & 0x7;
    }

    public void writeAlignZero() {
        if (this.numHeldBits == 0) {
            return;
        }
        write(this.heldBits);
        this.heldBits = 0;
        this.numHeldBits = 0;
    }

    public void writeAlignOne() {
        int numBits = getNumBitsUntilByteAligned();
        this.write((1 << numBits) - 1, numBits);
    }

    public void flush() {
        writeAlignZero();
        try {
            this.outputStream.flush();
        } catch (IOException ex) {
            Logger.getLogger(BitOutputStream.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void close() {
        flush();
        try {
            this.outputStream.close();
        } catch (IOException ex) {
            Logger.getLogger(BitOutputStream.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
