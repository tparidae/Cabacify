package common;

import java.math.BigInteger;
import java.util.Objects;

public class BinaryCode {

    private BigInteger _code;
    private int _l;

    public BinaryCode(BigInteger code, int l) {
        _code = code;
        _l = l;
    }

    public BigInteger getCode() {
        return _code;
    }

    public int getLength() {
        return _l;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 71 * hash + Objects.hashCode(this._code);
        hash = 71 * hash + this._l;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final BinaryCode other = (BinaryCode) obj;
        if (!Objects.equals(this._code, other._code)) {
            return false;
        }
        if (this._l != other._l) {
            return false;
        }
        return true;
    }

}
