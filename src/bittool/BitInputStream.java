package bittool;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BitInputStream {

    private InputStream in;

    private int numHeldBits;
    private int heldBits;

    private int numBitsRead;

    private byte lastReadByte;
    
    private boolean EOF=false;

    public BitInputStream(InputStream in) {
        this.in = in;

        this.numHeldBits = 0;
        this.heldBits = 0;
        this.numBitsRead = 0;
    }
    
    public boolean bitsLeft(){
        return !EOF;
    }

    public BitInputStream(byte[] buf) {
        this(new ByteArrayInputStream(buf));
    }

    public int read(int numberOfBits) {

        this.numBitsRead += numberOfBits;

        int bits = 0;
        int bytesRead=0;

        if (numberOfBits <= this.numHeldBits) {
            bits = this.heldBits >>> (this.numHeldBits - numberOfBits);
            bits &= ~(0xffffffff << numberOfBits);
            this.numHeldBits -= numberOfBits;
            return bits;
        }

        numberOfBits -= this.numHeldBits;
        bits = this.heldBits & ~(0xffffffff << this.numHeldBits);
        bits <<= numberOfBits;

        byte[] bytes = new byte[4];
        int numBytesToLoad = ((numberOfBits - 1) >>> 3) + 1;
        try {
            bytesRead = this.in.read(bytes, 4 - numBytesToLoad, numBytesToLoad);
            this.lastReadByte = bytes[3];
        } catch (IOException ex) {
            Logger.getLogger(BitInputStream.class.getName()).log(Level.SEVERE, null, ex);
        }
        int alignedWord = ByteBuffer.wrap(bytes).getInt();

        int nextNumHeldBits = (32 - numberOfBits) &0x07;//% 8;

        bits |= alignedWord >>> nextNumHeldBits;

        this.numHeldBits = nextNumHeldBits;
        this.heldBits = alignedWord;
        if (bytesRead<0)
            EOF=true;
        return bits;
    }

    public int readByte() {
        return read(8);
    }

    public int getNumBitsUntilByteAligned() {
        return this.numHeldBits & (0x7);
    }

//    private int getNumBitsLeft() {
//        return 8 * ((UInt) m_fifo -> size() - m_fifo_idx) + m_num_held_bits;
//    }
    public void readByteAligment() {
        int code = read(1);
        if (code != 1) {
            System.err.println("WHoops");
        }
        int numBits = getNumBitsUntilByteAligned();
        if (numBits > 0) {
            // assert (numBits <= getNumBitsLeft());
            code = read(numBits);
            // assert (code == 0);
            if (code != 0) {
                System.err.println("Whooooops");
            }

        }
    }

    public byte peekPreviousByte() {
        return this.lastReadByte;
    }

//    public void readOutTrailingBits() {
//        int bits = 0;
//
//        while ((getNumBitsLeft() > 0) && (getNumBitsUntilByteAligned() != 0)) {
//            bits =read(1);
//        }
//    }
}
